package com.hfr.wallart;

public final class WallArtConstants {
    public static final int MAX_SIZE = 5, MAX_AREA = 25, MAX_PER_PLAYER = 30, PIXELS_PER_BLOCK = 128;
    public static final int MAX_URL_BYTES = 2048, MAX_IMAGE_BYTES = 2 * 1024 * 1024, CHUNK_BYTES = 24 * 1024;
    private WallArtConstants() { }
    public static boolean validSize(int width, int height) { return width >= 1 && width <= MAX_SIZE && height >= 1 && height <= MAX_SIZE && width * height <= MAX_AREA; }
    public static boolean validFacing(int facing) { return facing >= 2 && facing <= 5; }
    public static boolean validHash(String hash) { return hash != null && hash.matches("[0-9a-f]{64}"); }
}
