package com.hfr.wallart;

public final class WallArtConstants {
  /** Dedicated Forge 1.7.10 render ID for the Wall Art controller. */
  public static final int BLOCK_RENDER_ID = 10008;
  public static final int MAX_SIZE = 5, MAX_AREA = 25, MAX_PER_PLAYER = 30,
                          PIXELS_PER_BLOCK = 128;
  public static final int MAX_URL_BYTES = 2048;
  /**
   * Bounds untrusted remote input before decoding; independent of faction flag
   * limits.
   */
  public static final int MAX_SOURCE_DOWNLOAD_BYTES = 16 * 1024 * 1024;
  public static final int MAX_SOURCE_DIMENSION = 8192;
  public static final long MAX_SOURCE_PIXELS = 64L * 1024L * 1024L;
  /** Bounds the server-generated PNG stored in the save and sent to clients. */
  public static final int MAX_PROCESSED_IMAGE_BYTES = 2 * 1024 * 1024;
  public static final int CHUNK_BYTES = 24 * 1024;
  private WallArtConstants() {}
  public static boolean validSize(int width, int height) {
    return width >= 1 && width <= MAX_SIZE && height >= 1 &&
        height <= MAX_SIZE && width * height <= MAX_AREA;
  }
  public static boolean validFacing(int facing) {
    return facing >= 2 && facing <= 5;
  }
  public static boolean validHash(String hash) {
    return hash != null && hash.matches("[0-9a-f]{64}");
  }
}
