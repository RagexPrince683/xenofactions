package com.hfr.world.earth;
import java.io.*; import java.util.*; import com.google.gson.Gson; import com.hfr.config.XFConfig;
public final class XFEarthProfileLoader {
 public static final String FILE_NAME="xenoearth-profile.json";
 private static final Map<String,XFEarthProfile> CACHE=new HashMap<String,XFEarthProfile>();
 private XFEarthProfileLoader(){}
 public static synchronized XFEarthProfile load(File saveRoot) throws IOException {
  File file=new File(saveRoot,FILE_NAME); String key=file.getCanonicalPath(); if(CACHE.containsKey(key)) return CACHE.get(key);
  Reader reader=new InputStreamReader(new FileInputStream(file),"UTF-8"); try { XFEarthProfile p=new Gson().fromJson(reader,XFEarthProfile.class); if(p==null) throw new IllegalArgumentException("Invalid XenoEarth profile: empty JSON"); p.validate(XFConfig.earthAllowProfileMinecraftVersion); CACHE.put(key,p); return p; } finally {reader.close();}
 }
 public static File path(File root){return new File(root,FILE_NAME);} public static boolean exists(File root){return path(root).isFile();}
 public static synchronized void clear(){CACHE.clear();}
}
