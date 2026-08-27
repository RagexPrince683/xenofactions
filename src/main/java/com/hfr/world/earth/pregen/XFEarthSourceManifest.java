package com.hfr.world.earth.pregen;

import java.util.LinkedHashMap;
import java.util.Map;

/** Description of external, trusted raster inputs. No source data is packaged in the mod. */
public final class XFEarthSourceManifest {
 public int formatVersion, effectiveScale, width, height, minimumX, maximumX, minimumZ, maximumZ, seaLevel;
 public String sourceId, profile, minecraftVersion, generationMode, generatorVersion;
 public Map<String,String> rasters=new LinkedHashMap<String,String>();
 public void validate(){
  if(formatVersion!=1)bad("formatVersion must be 1");
  if(!"earth2000".equals(sourceId)||!"earth2000".equals(profile))bad("sourceId/profile must be earth2000");
  if(!"1.7.10".equals(minecraftVersion))bad("minecraftVersion must be 1.7.10");
  if(effectiveScale!=2000||width!=21504||height!=10752)bad("earth2000 scale/dimensions do not match 1:2000");
  if(minimumX!=-10752||maximumX!=10751||minimumZ!=-5376||maximumZ!=5375||seaLevel!=62)bad("earth2000 bounds/sea level do not match the profile");
  if(!"RASTER_PREGEN".equals(generationMode))bad("generationMode must be RASTER_PREGEN");
  if(generatorVersion==null||generatorVersion.trim().isEmpty())bad("generatorVersion is required");
  for(String name:required())if(!rasters.containsKey(name)||!rasters.get(name).matches("[0-9a-fA-F]{64}"))bad("missing or invalid SHA-256 for "+name);
 }
 public static String[] required(){return new String[]{"HeightMap20k.png","BiomeMap20k.png","WaterMap20k.png","Ice20k.png","globecover20k.png"};}
 private static void bad(String message){throw new IllegalArgumentException("Invalid Earth raster source manifest: "+message);}
}
