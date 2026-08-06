package com.hfr.world.earth;
public final class XFEarthMissingChunkException extends RuntimeException {
 public XFEarthMissingChunkException(int x,int z,XFEarthProfile p){super(message(x,z,p));}
 private static String message(int x,int z,XFEarthProfile p){return "Missing pregenerated Earth chunk ("+x+", "+z+") blocks ["+(x*16)+".."+(x*16+15)+", "+(z*16)+".."+(z*16+15)+"] region ("+XFEarthBounds.chunkToRegion(x)+", "+XFEarthBounds.chunkToRegion(z)+") expected profile '"+p.getProfile()+"' ("+p.getWidth()+"x"+p.getHeight()+"). The WorldPainter export is incomplete or incorrectly installed.";}
}
