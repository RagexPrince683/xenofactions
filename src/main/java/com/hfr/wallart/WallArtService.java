package com.hfr.wallart;

import com.hfr.config.XFConfig;
import com.hfr.main.MainRegistry;
import com.hfr.tileentity.TileEntityWallImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.WorldServer;

/**
 * Bounded worker pipeline. Minecraft state is accessed only from the server
 * tick thread.
 */
public final class WallArtService {
  private static ThreadPoolExecutor workers = newWorkerPool();
  private static final ConcurrentLinkedQueue<Runnable> MAIN_THREAD_RESULTS =
      new ConcurrentLinkedQueue<Runnable>();
  private static final Map<UUID, Long> cooldowns = new HashMap<UUID, Long>();
  private static final Map<UUID, Boolean> pending =
      new HashMap<UUID, Boolean>();

  public static boolean submit(EntityPlayerMP player, int dimension, int x,
                               int y, int z, UUID displayId, long generation,
                               int width, int height, String url) {
    UUID owner = player.getUniqueID();
    if (!canSubmit(player, owner))
      return false;

    final ImportRequest request = new ImportRequest(
        owner, dimension, x, y, z, displayId, generation, width, height, url);
    pending.put(owner, Boolean.TRUE);
    try {
      workers.execute(new Runnable() {
        @Override
        public void run() {
          processOffThread(request);
        }
      });
    } catch (RejectedExecutionException exception) {
      pending.remove(owner);
      message(player, "Wall Art queue is busy; try again shortly.");
      MainRegistry.logger.warn("Wall Art worker queue rejected player " +
                               owner);
      return false;
    }
    return true;
  }

  private static boolean canSubmit(EntityPlayerMP player, UUID owner) {
    removeExpiredCooldowns(System.currentTimeMillis());
    if (pending.containsKey(owner)) {
      message(player, "Your previous Wall Art image is still being processed.");
      return false;
    }
    Long expires = cooldowns.get(owner);
    long now = System.currentTimeMillis();
    if (expires != null && expires.longValue() > now) {
      double seconds = (expires.longValue() - now) / 1000.0D;
      message(player, "Please wait " + formatSeconds(seconds) +
                          " before submitting another image.");
      return false;
    }
    return true;
  }

  private static void processOffThread(final ImportRequest request) {
    ImportResult result;
    try {
      byte[] image = SecureWallArtDownloader.downloadAndProcess(
          request.url, request.width, request.height);
      result = ImportResult.success(image, sha256(image));
    } catch (Exception exception) {
      result = ImportResult.failure(exception);
      logFailure(request, exception);
    }
    final ImportResult completed = result;
    MAIN_THREAD_RESULTS.add(new Runnable() {
      @Override
      public void run() {
        applyOnMainThread(request, completed);
      }
    });
  }

  private static void applyOnMainThread(ImportRequest request,
                                        ImportResult result) {
    pending.remove(request.owner);
    EntityPlayerMP player = findPlayer(request.owner);
    if (!result.success()) {
      applyCooldown(request.owner, XFConfig.wallArtFailureCooldownMs);
      if (player != null)
        message(player, result.error);
      return;
    }

    String liveError = storeIfStillValid(request, result);
    if (liveError == null) {
      applyCooldown(request.owner, XFConfig.wallArtSuccessCooldownMs);
      if (player != null)
        message(player, "Wall Art configured.");
    } else {
      applyCooldown(request.owner, XFConfig.wallArtFailureCooldownMs);
      if (player != null)
        message(player, liveError);
    }
  }

  private static String storeIfStillValid(ImportRequest request,
                                          ImportResult result) {
    WorldServer world =
        MinecraftServer.getServer().worldServerForDimension(request.dimension);
    if (world == null)
      return "The target world is no longer available.";
    TileEntity rawTile = world.getTileEntity(request.x, request.y, request.z);
    if (!(rawTile instanceof TileEntityWallImage))
      return "The Wall Art display no longer exists.";
    TileEntityWallImage tile = (TileEntityWallImage)rawTile;
    if (!request.displayId.equals(tile.getDisplayId()) ||
        !request.owner.equals(tile.getOwnerId()) ||
        request.generation != tile.getRequestGeneration()) {
      return "The Wall Art display changed while the image was processing.";
    }

    WallArtSavedData savedData = WallArtSavedData.get(world);
    WallArtSavedData.Record previous = savedData.get(request.displayId);
    WallArtSavedData.Record replacement = new WallArtSavedData.Record(
        request.displayId, request.owner, request.dimension, request.x,
        request.y, request.z, tile.getFacing(), request.width, request.height,
        result.hash);
    if (!savedData.put(replacement)) {
      return savedData.count(request.owner) >= WallArtConstants.MAX_PER_PLAYER
          ? "Wall Art limit reached (30 displays)."
          : "That display overlaps another Wall Art display.";
    }

    try {
      writeImage(result.hash, result.bytes);
      tile.configure(request.width, request.height, result.hash);
      world.markBlockForUpdate(request.x, request.y, request.z);
      deleteOrphan(previous, result.hash, savedData);
      return null;
    } catch (IOException exception) {
      savedData.remove(request.displayId);
      if (previous != null)
        savedData.put(previous);
      MainRegistry.logger.warn(
          "Could not store processed Wall Art image " + result.hash, exception);
      return "Could not store the Wall Art image.";
    }
  }

  private static void logFailure(ImportRequest request, Exception exception) {
    String stage = "processing";
    String target = sanitizedSource(request.url);
    int status = -1;
    if (exception instanceof SecureWallArtDownloader.DownloadException) {
      SecureWallArtDownloader.DownloadException download =
          (SecureWallArtDownloader.DownloadException)exception;
      stage = download.stage;
      target = SecureWallArtDownloader.sanitize(download.url);
      status = download.httpStatus;
    }
    MainRegistry.logger.warn("Wall Art import failed: player=" + request.owner +
                             ", source=" + sanitizedSource(request.url) +
                             ", target=" + target + ", stage=" + stage +
                             (status < 0 ? "" : ", httpStatus=" + status) +
                             ", error=" + safeMessage(exception));
  }

  private static String sanitizedSource(String source) {
    try {
      URI uri = new URI(source);
      String path = uri.getRawPath() == null ? "" : uri.getRawPath();
      return uri.getScheme() + "://" + uri.getHost() + path +
          (uri.getRawQuery() == null ? "" : "?<query omitted>");
    } catch (Exception exception) {
      return "<invalid URL>";
    }
  }

  private static String safeMessage(Exception exception) {
    return exception.getMessage() == null ? "Image import failed."
                                          : exception.getMessage();
  }

  private static void applyCooldown(UUID owner, long duration) {
    if (duration > 0L)
      cooldowns.put(owner, Long.valueOf(System.currentTimeMillis() + duration));
    else
      cooldowns.remove(owner);
  }

  private static void removeExpiredCooldowns(long now) {
    Iterator<Map.Entry<UUID, Long>> iterator = cooldowns.entrySet().iterator();
    while (iterator.hasNext()) {
      if (iterator.next().getValue().longValue() <= now)
        iterator.remove();
    }
  }

  private static String formatSeconds(double seconds) {
    double rounded = Math.ceil(seconds * 10.0D) / 10.0D;
    if (rounded == Math.rint(rounded))
      return ((long)rounded) + " seconds";
    return String.format(java.util.Locale.ROOT, "%.1f seconds", rounded);
  }

  public static void tick() {
    for (int count = 0; count < 32; count++) {
      Runnable result = MAIN_THREAD_RESULTS.poll();
      if (result == null)
        break;
      result.run();
    }
    removeExpiredCooldowns(System.currentTimeMillis());
  }

  public static void enqueueMainThread(Runnable task) {
    if (task != null)
      MAIN_THREAD_RESULTS.add(task);
  }

  public static void shutdown() {
    workers.shutdownNow();
    MAIN_THREAD_RESULTS.clear();
    cooldowns.clear();
    pending.clear();
    workers = newWorkerPool();
  }

  public static File imageFile(String hash) {
    if (!WallArtConstants.validHash(hash))
      return null;
    WorldServer rootWorld =
        MinecraftServer.getServer().worldServerForDimension(0);
    File root = rootWorld.getSaveHandler()
                    .getMapFileFromName(WallArtSavedData.ID)
                    .getParentFile()
                    .getParentFile();
    return new File(new File(root, "xenofactions/wallart/images"),
                    hash + ".png");
  }

  public static byte[] readImage(String hash) throws IOException {
    File file = imageFile(hash);
    if (file == null || !file.isFile() ||
        file.length() > WallArtConstants.MAX_PROCESSED_IMAGE_BYTES) {
      throw new IOException("Invalid stored Wall Art image.");
    }
    byte[] bytes = new byte[(int)file.length()];
    FileInputStream input = new FileInputStream(file);
    try {
      int position = 0;
      int count;
      while (position < bytes.length &&
             (count = input.read(bytes, position, bytes.length - position)) >
                 0) {
        position += count;
      }
      if (position != bytes.length)
        throw new IOException("Incomplete stored Wall Art image.");
      return bytes;
    } finally {
      input.close();
    }
  }

  public static void remove(TileEntityWallImage tile) {
    if (tile.getDisplayId() == null || tile.getWorldObj() == null ||
        tile.getWorldObj().isRemote)
      return;
    WallArtSavedData data = WallArtSavedData.get(tile.getWorldObj());
    WallArtSavedData.Record old = data.remove(tile.getDisplayId());
    if (old != null && !data.references(old.hash)) {
      File file = imageFile(old.hash);
      if (file != null && file.exists() && !file.delete()) {
        MainRegistry.logger.warn("Could not remove orphan Wall Art image " +
                                 old.hash);
      }
    }
  }

  private static void writeImage(String hash, byte[] bytes) throws IOException {
    File file = imageFile(hash);
    if (file == null)
      throw new IOException("Invalid image hash.");
    if (file.exists())
      return;
    File directory = file.getParentFile();
    if (!directory.exists() && !directory.mkdirs())
      throw new IOException("Could not create image directory.");
    File temporary = new File(directory, hash + ".tmp");
    FileOutputStream output = new FileOutputStream(temporary);
    try {
      output.write(bytes);
    } finally {
      output.close();
    }
    if (!temporary.renameTo(file)) {
      temporary.delete();
      throw new IOException("Could not move processed image into place.");
    }
  }

  private static void deleteOrphan(WallArtSavedData.Record previous,
                                   String replacementHash,
                                   WallArtSavedData data) {
    if (previous == null || previous.hash.equals(replacementHash) ||
        data.references(previous.hash))
      return;
    File orphan = imageFile(previous.hash);
    if (orphan != null && orphan.exists() && !orphan.delete()) {
      MainRegistry.logger.warn("Could not remove orphan Wall Art image " +
                               previous.hash);
    }
  }

  private static EntityPlayerMP findPlayer(UUID id) {
    for (Object entry : MinecraftServer.getServer()
                            .getConfigurationManager()
                            .playerEntityList) {
      if (entry instanceof EntityPlayerMP &&
          id.equals(((EntityPlayerMP)entry).getUniqueID())) {
        return (EntityPlayerMP)entry;
      }
    }
    return null;
  }

  private static void message(EntityPlayerMP player, String text) {
    player.addChatMessage(new ChatComponentText("[Wall Art] " + text));
  }

  private static String sha256(byte[] bytes) throws Exception {
    byte[] digest = MessageDigest.getInstance("SHA-256").digest(bytes);
    StringBuilder hash = new StringBuilder(64);
    for (byte value : digest)
      hash.append(String.format("%02x", value & 255));
    return hash.toString();
  }

  private static ThreadPoolExecutor newWorkerPool() {
    return new ThreadPoolExecutor(1, 2, 30L, TimeUnit.SECONDS,
                                  new ArrayBlockingQueue<Runnable>(8),
                                  new ThreadPoolExecutor.AbortPolicy());
  }

  private static final class ImportRequest {
    final UUID owner;
    final int dimension;
    final int x;
    final int y;
    final int z;
    final UUID displayId;
    final long generation;
    final int width;
    final int height;
    final String url;

    ImportRequest(UUID owner, int dimension, int x, int y, int z,
                  UUID displayId, long generation, int width, int height,
                  String url) {
      this.owner = owner;
      this.dimension = dimension;
      this.x = x;
      this.y = y;
      this.z = z;
      this.displayId = displayId;
      this.generation = generation;
      this.width = width;
      this.height = height;
      this.url = url;
    }
  }

  private static final class ImportResult {
    final byte[] bytes;
    final String hash;
    final String error;

    private ImportResult(byte[] bytes, String hash, String error) {
      this.bytes = bytes;
      this.hash = hash;
      this.error = error;
    }

    static ImportResult success(byte[] bytes, String hash) {
      return new ImportResult(bytes, hash, null);
    }

    static ImportResult failure(Exception exception) {
      return new ImportResult(null, null, safeMessage(exception));
    }

    boolean success() { return error == null; }
  }

  private WallArtService() {}
}
