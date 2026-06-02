package com.hfr.clowder.flag;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URI;
import java.net.URL;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.imageio.ImageIO;
import javax.net.ssl.HttpsURLConnection;

import com.hfr.clowder.Clowder;
import com.hfr.command.CommandClowder;
import com.hfr.data.ClowderData;
import com.hfr.main.MainRegistry;
import com.hfr.packet.PacketDispatcher;
import com.hfr.packet.effect.FactionFlagBytesPacket;
import com.hfr.packet.effect.FactionFlagMetadataPacket;

import cpw.mods.fml.common.Loader;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.World;

public class CustomFlagService {

	public static final int FLAG_SIZE = 64;
	private static final int MAX_DIMENSION = 1024;
	private static final int MAX_DOWNLOAD = 1024 * 1024;
	private static final int TIMEOUT_MS = 5000;
	private static final int MAX_REDIRECTS = 3;
	private static final long RATE_LIMIT_MS = 60000L;
	private static final String[] ALLOWED_HOSTS = new String[] { "postimages.org", "i.postimg.cc" };
	private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();
	private static final Queue<Runnable> MAIN_THREAD_CALLBACKS = new LinkedList<Runnable>();
	private static final Map<String, Long> RATE_LIMITS = new HashMap<String, Long>();

	public static File getServerFlagDir() {
		return new File(Loader.instance().getConfigDir(), "xenofactions/flags");
	}

	public static File getFlagFile(Clowder clowder) {
		return new File(getServerFlagDir(), getFactionFileKey(clowder) + ".png");
	}

	public static String getFactionFileKey(Clowder clowder) {
		String key = clowder.uuid == null || clowder.uuid.length() == 0 ? clowder.name : clowder.uuid;
		return key.replaceAll("[^A-Za-z0-9_.-]", "_");
	}

	public static void tickMainThread() {
		while(true) {
			Runnable runnable;
			synchronized(MAIN_THREAD_CALLBACKS) {
				runnable = MAIN_THREAD_CALLBACKS.poll();
			}
			if(runnable == null)
				return;
			runnable.run();
		}
	}

	public static void importUrl(final EntityPlayerMP player, final Clowder clowder, final String urlText) {
		final String key = getFactionFileKey(clowder) + ":" + player.getDisplayName();
		long now = System.currentTimeMillis();
		Long next = RATE_LIMITS.get(key);
		if(next != null && next.longValue() > now) {
			player.addChatMessage(new ChatComponentText(CommandClowder.ERROR + "Please wait before importing another faction flag."));
			return;
		}
		RATE_LIMITS.put(key, Long.valueOf(now + RATE_LIMIT_MS));
		player.addChatMessage(new ChatComponentText(CommandClowder.INFO + "Importing faction flag from HTTPS. The server will validate and cache it first."));

		EXECUTOR.submit(new Runnable() {
			@Override
			public void run() {
				ImportResult result;
				try {
					result = importAndSave(clowder, urlText);
				} catch(FlagImportException e) {
					MainRegistry.logger.warn("Rejected faction flag for " + clowder.name + ": " + e.getMessage());
					result = new ImportResult(e.getMessage(), null, null);
				} catch(Exception e) {
					MainRegistry.logger.warn("Failed to import faction flag for " + clowder.name + ": " + e.getMessage());
					result = new ImportResult("failed to decode image", null, null);
				}
				final ImportResult finalResult = result;
				enqueueMainThread(new Runnable() {
					@Override
					public void run() {
						EntityPlayerMP livePlayer = MinecraftServer.getServer().getConfigurationManager().func_152612_a(player.getDisplayName());
						Clowder liveClowder = Clowder.getClowderFromUUID(clowder.uuid);
						if(liveClowder == null)
							return;
						if(finalResult.success()) {
							liveClowder.customFlagHash = finalResult.hash;
							World world = livePlayer != null ? livePlayer.worldObj : MinecraftServer.getServer().worldServers[0];
							ClowderData.getData(world).markDirty();
							broadcastMetadata(liveClowder);
							if(livePlayer != null)
								livePlayer.addChatMessage(new ChatComponentText(CommandClowder.INFO + "Custom faction flag imported."));
						} else if(livePlayer != null) {
							livePlayer.addChatMessage(new ChatComponentText(CommandClowder.ERROR + finalResult.message));
						}
					}
				});
			}
		});
	}

	public static void clearFlag(EntityPlayerMP player, Clowder clowder) {
		File file = getFlagFile(clowder);
		if(file.exists() && !file.delete())
			MainRegistry.logger.warn("Could not delete custom faction flag " + file.getName());
		clowder.customFlagHash = "";
		clowder.save(player.worldObj);
		broadcastMetadata(clowder);
		player.addChatMessage(new ChatComponentText(CommandClowder.INFO + "Custom faction flag cleared."));
	}

	public static void reloadFlag(EntityPlayerMP player, Clowder clowder) {
		try {
			File file = getFlagFile(clowder);

			if(!file.exists()) {
				clowder.customFlagHash = "";
			} else {
				clowder.customFlagHash = sha256(readFile(file));
			}

			clowder.save(player.worldObj);
			broadcastMetadata(clowder);

			player.addChatMessage(new ChatComponentText(CommandClowder.INFO + "Custom faction flag metadata reloaded."));

		} catch(Exception e) {
			player.addChatMessage(new ChatComponentText(CommandClowder.ERROR + "Failed to reload custom faction flag."));
			MainRegistry.logger.warn("Could not reload faction flag", e);
		}
	}

	public static void broadcastMetadata(Clowder clowder) {
		PacketDispatcher.wrapper.sendToAll(new FactionFlagMetadataPacket(clowder.name, clowder.customFlagHash));
	}

	public static void sendFlagBytes(EntityPlayerMP player, Clowder clowder) {
		if(clowder == null || clowder.customFlagHash == null || clowder.customFlagHash.length() == 0)
			return;
		try {
			File file = getFlagFile(clowder);
			if(file.exists())
				PacketDispatcher.wrapper.sendTo(new FactionFlagBytesPacket(clowder.name, clowder.customFlagHash, readFile(file)), player);
		} catch(IOException e) {
			MainRegistry.logger.warn("Could not send faction flag bytes for " + clowder.name + ": " + e.getMessage());
		}
	}

	private static void enqueueMainThread(Runnable runnable) {
		synchronized(MAIN_THREAD_CALLBACKS) {
			MAIN_THREAD_CALLBACKS.add(runnable);
		}
	}

	private static ImportResult importAndSave(Clowder clowder, String urlText) throws Exception {
		URL url = validateUrl(urlText);
		byte[] downloaded = download(url, 0);
		BufferedImage input = ImageIO.read(new ByteArrayInputStream(downloaded));
		if(input == null)
			return new ImportResult("failed to decode image", null, null);
		if(input.getWidth() > MAX_DIMENSION || input.getHeight() > MAX_DIMENSION)
			return new ImportResult("image too large", null, null);
		BufferedImage output = resizeToFlag(input);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ImageIO.write(output, "png", out);
		byte[] png = out.toByteArray();
		String hash = sha256(png);
		File dir = getServerFlagDir();
		if(!dir.exists())
			dir.mkdirs();
		File finalFile = getFlagFile(clowder);
		File tmp = new File(dir, finalFile.getName() + ".tmp");
		FileOutputStream fos = new FileOutputStream(tmp);
		try {
			fos.write(png);
		} finally {
			fos.close();
		}
		if(finalFile.exists() && !finalFile.delete())
			throw new IOException("could not replace old flag");
		if(!tmp.renameTo(finalFile))
			throw new IOException("could not move temporary flag into place");
		return new ImportResult(null, hash, png);
	}

	private static URL validateUrl(String urlText) throws Exception {
		URI uri = new URI(urlText);
		if(uri.getUserInfo() != null)
			throw new FlagImportException("invalid URL");
		if(uri.getScheme() == null || !"https".equalsIgnoreCase(uri.getScheme()))
			throw new FlagImportException("invalid URL");
		if(uri.getHost() == null)
			throw new FlagImportException("invalid URL");
		if(!isAllowedHost(uri.getHost()))
			throw new FlagImportException("unsupported image host");
		validateHost(uri.getHost());
		return uri.toURL();
	}

	private static byte[] download(URL url, int redirects) throws Exception {
		URL safeUrl = validateUrl(url.toString());
		HttpURLConnection conn = (HttpURLConnection)safeUrl.openConnection();
		if(!(conn instanceof HttpsURLConnection))
			throw new FlagImportException("invalid URL");
		conn.setInstanceFollowRedirects(false);
		conn.setConnectTimeout(TIMEOUT_MS);
		conn.setReadTimeout(TIMEOUT_MS);
		conn.setRequestProperty("User-Agent", "XenofactionsFlagImporter/1.0");
		try {
			int code = conn.getResponseCode();
			if(code >= 300 && code <= 399) {
				if(redirects >= MAX_REDIRECTS)
					throw new FlagImportException("invalid URL");
				String location = conn.getHeaderField("Location");
				if(location == null)
					throw new FlagImportException("invalid URL");
				URL next = new URL(safeUrl, location);
				if(!"https".equalsIgnoreCase(next.getProtocol()))
					throw new FlagImportException("invalid URL");
				return download(next, redirects + 1);
			}
			String contentType = conn.getContentType();
			if(contentType != null) {
				String lower = contentType.toLowerCase();
				if(!lower.startsWith("image/png") && !lower.startsWith("image/jpeg") && !lower.startsWith("image/jpg"))
					throw new FlagImportException("unsupported image type");
			}
			if(code < 200 || code >= 300)
				throw new FlagImportException("invalid URL");
			int length = conn.getContentLength();
			if(length > MAX_DOWNLOAD)
				throw new FlagImportException("image too large");
			return readLimited(conn.getInputStream(), MAX_DOWNLOAD);
		} catch(java.net.SocketTimeoutException e) {
			throw new FlagImportException("download timed out");
		} catch(FlagImportException e) {
			throw e;
		} catch(IOException e) {
			throw new FlagImportException("failed to decode image");
		} finally {
			conn.disconnect();
		}
	}

	private static boolean isAllowedHost(String host) {
		if(host == null)
			return false;
		String normalized = host.toLowerCase();
		if(normalized.endsWith("."))
			normalized = normalized.substring(0, normalized.length() - 1);
		for(int i = 0; i < ALLOWED_HOSTS.length; i++) {
			if(ALLOWED_HOSTS[i].equals(normalized))
				return true;
		}
		return false;
	}

	private static void validateHost(String host) throws Exception {
		InetAddress[] addresses = InetAddress.getAllByName(host);
		if(addresses == null || addresses.length == 0)
			throw new FlagImportException("unsafe host");
		for(int i = 0; i < addresses.length; i++) {
			if(isUnsafeAddress(addresses[i]))
				throw new FlagImportException("unsafe host");
		}
	}

	public static boolean isUnsafeAddress(InetAddress address) {
		if(address.isAnyLocalAddress() || address.isLoopbackAddress() || address.isLinkLocalAddress() || address.isSiteLocalAddress() || address.isMulticastAddress())
			return true;
		byte[] b = address.getAddress();
		if(b.length == 16) {
			int first = b[0] & 0xff;
			return (first & 0xfe) == 0xfc;
		}
		return false;
	}

	private static byte[] readLimited(InputStream input, int max) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		byte[] buf = new byte[8192];
		int total = 0;
		while(true) {
			int read = input.read(buf);
			if(read < 0)
				break;
			total += read;
			if(total > max)
				throw new FlagImportException("image too large");
			out.write(buf, 0, read);
		}
		return out.toByteArray();
	}

	private static byte[] readFile(File file) throws IOException {
		if(file.length() > MAX_DOWNLOAD)
			throw new EOFException("flag file too large");
		FileInputStream in = new FileInputStream(file);
		try {
			return readLimited(in, MAX_DOWNLOAD);
		} finally {
			in.close();
		}
	}

	private static BufferedImage resizeToFlag(BufferedImage input) {
		BufferedImage output = new BufferedImage(FLAG_SIZE, FLAG_SIZE, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = output.createGraphics();
		try {
			g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
			g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
			int w = input.getWidth();
			int h = input.getHeight();
			int size = Math.min(w, h);
			int sx = (w - size) / 2;
			int sy = (h - size) / 2;
			g.drawImage(input, 0, 0, FLAG_SIZE, FLAG_SIZE, sx, sy, sx + size, sy + size, null);
		} finally {
			g.dispose();
		}
		return output;
	}

	private static String sha256(byte[] data) throws Exception {
		MessageDigest digest = MessageDigest.getInstance("SHA-256");
		byte[] hash = digest.digest(data);
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < hash.length; i++)
			sb.append(String.format("%02x", hash[i] & 0xff));
		return sb.toString();
	}

	private static class ImportResult {
		String message;
		String hash;
		byte[] bytes;
		ImportResult(String message, String hash, byte[] bytes) { this.message = message; this.hash = hash; this.bytes = bytes; }
		boolean success() { return hash != null && hash.length() > 0; }
	}

	private static class FlagImportException extends IOException {
		FlagImportException(String message) { super(message); }
	}
}
