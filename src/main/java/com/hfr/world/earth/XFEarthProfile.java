package com.hfr.world.earth;

/** Gson DTO made immutable to callers after validation by the loader. */
public final class XFEarthProfile {
    int formatVersion; String profile; String targetMinecraftVersion;
    int sourceScale, resize, effectiveScale, width, height;
    int minimumX, maximumX, minimumZ, maximumZ;
    int minimumSurfaceY, maximumSurfaceY, seaLevel;
    String projection, populationMode; boolean caves, ores, lava, structures, vegetation;
    private transient XFEarthBounds bounds;
    void validate(String allowedVersion) {
        if(formatVersion!=1) bad("unsupported formatVersion: "+formatVersion);
        if(!allowedVersion.equals(targetMinecraftVersion)) bad("targetMinecraftVersion must be "+allowedVersion);
        if(width<=0||height<=0||width%16!=0||height%16!=0) bad("width and height must be positive multiples of 16");
        if((long)maximumX-minimumX+1!=width || (long)maximumZ-minimumZ+1!=height) bad("coordinate bounds do not match width/height");
        if(minimumSurfaceY<1||maximumSurfaceY>254||minimumSurfaceY>maximumSurfaceY) bad("surface range must be within 1..254");
        if(seaLevel<minimumSurfaceY||seaLevel>maximumSurfaceY) bad("seaLevel must lie inside surface range");
        if(!"pregenerated".equals(populationMode)) bad("populationMode must be pregenerated");
        if(caves||ores||lava||structures) bad("caves, ores, lava, and structures must all be false");
        if(profile==null||profile.trim().isEmpty()) bad("profile name is required");
        bounds=new XFEarthBounds(minimumX,maximumX,minimumZ,maximumZ);
    }
    private static void bad(String message){throw new IllegalArgumentException("Invalid XenoEarth profile: "+message);}
    public String getProfile(){return profile;} public int getEffectiveScale(){return effectiveScale;} public int getWidth(){return width;} public int getHeight(){return height;}
    public int getSeaLevel(){return seaLevel;} public XFEarthBounds getBounds(){return bounds;}
}
