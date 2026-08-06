package com.hfr.world.earth;

/** Inclusive block and chunk bounds from an Earth export profile. */
public final class XFEarthBounds {
    public final int minimumX, maximumX, minimumZ, maximumZ;
    public final int minimumChunkX, maximumChunkX, minimumChunkZ, maximumChunkZ;
    public XFEarthBounds(int minX, int maxX, int minZ, int maxZ) {
        minimumX=minX; maximumX=maxX; minimumZ=minZ; maximumZ=maxZ;
        minimumChunkX=floorDiv(minX,16); maximumChunkX=floorDiv(maxX,16);
        minimumChunkZ=floorDiv(minZ,16); maximumChunkZ=floorDiv(maxZ,16);
    }
    public boolean containsChunk(int x,int z) { return x>=minimumChunkX&&x<=maximumChunkX&&z>=minimumChunkZ&&z<=maximumChunkZ; }
    public boolean containsBlock(double x,double z) { return x>=minimumX&&x<=maximumX&&z>=minimumZ&&z<=maximumZ; }
    public static int floorDiv(int value,int divisor) { int q=value/divisor, r=value%divisor; return r!=0 && ((value^divisor)<0) ? q-1:q; }
    public static int chunkToRegion(int chunk) { return floorDiv(chunk,32); }
    public String blockString(){return minimumX+".."+maximumX+" x "+minimumZ+".."+maximumZ;}
    public String chunkString(){return minimumChunkX+".."+maximumChunkX+" x "+minimumChunkZ+".."+maximumChunkZ;}
}
