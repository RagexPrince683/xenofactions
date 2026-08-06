package com.hfr.world.earth.pack;
import java.io.*;import java.util.*;import com.hfr.config.XFConfig;
public final class XFEarthMapRepository {
 public static final String CLEAN="xf-earth-16k-clean",POPULATED="xf-earth-16k-populated";
 private final File root,cache;
 public XFEarthMapRepository(File minecraft){root=XFConfig.earthLocalMapDirectory.length()==0?new File(minecraft,"xenofactions/earthmaps"):new File(XFConfig.earthLocalMapDirectory);cache=XFConfig.earthBundledCacheDirectory.length()==0?new File(root,"cache"):new File(XFConfig.earthBundledCacheDirectory);}
 public List<XFEarthMapPack> discover(){List<XFEarthMapPack> out=new ArrayList<XFEarthMapPack>();addBundled(out,CLEAN,"earth-16k-clean.xfmap");addBundled(out,POPULATED,"earth-16k-populated.xfmap");File[] fs=root.listFiles();if(fs!=null)for(File f:fs)if(f.isFile()&&f.getName().endsWith(".xfmap"))try{XFEarthMapManifest m=XFEarthMapVerifier.verify(f);out.add(new XFEarthMapPack(m,new LocalEarthMapSource(f,XFEarthMapSource.Provider.EXTERNAL_INSTALLED),XFEarthMapVerifier.sha256(f)));}catch(Exception ignored){}return out;}
 private void addBundled(List<XFEarthMapPack> out,String id,String name){BundledEarthMapSource s=new BundledEarthMapSource("assets/hfr/earthmaps/bundled/"+name);if(!s.exists())return;try{File f=cacheResource(id,s);XFEarthMapManifest m=XFEarthMapVerifier.verify(f);out.add(new XFEarthMapPack(m,s,XFEarthMapVerifier.sha256(f)));}catch(Exception ignored){}}
 public XFEarthMapPack find(String id){for(XFEarthMapPack p:discover())if(p.manifest.id.equals(id))return p;return null;}
 public File materialize(XFEarthMapPack p)throws IOException{if(p.source instanceof LocalEarthMapSource)return ((LocalEarthMapSource)p.source).getFile();return cacheResource(p.manifest.id,(BundledEarthMapSource)p.source);}
 private File cacheResource(String id,BundledEarthMapSource source)throws IOException{cache.mkdirs();File temp=File.createTempFile(id,".xfmap.tmp",cache);copy(source.open(),new FileOutputStream(temp));String hash=XFEarthMapVerifier.sha256(temp);File target=new File(cache,id+"-"+hash+".xfmap");if(target.isFile()&&hash.equals(XFEarthMapVerifier.sha256(target))){temp.delete();return target;}if(!temp.renameTo(target)){copy(new FileInputStream(temp),new FileOutputStream(target));temp.delete();}return target;}
 private static void copy(InputStream in,OutputStream out)throws IOException{try{byte[]b=new byte[65536];for(int n;(n=in.read(b))!=-1;)out.write(b,0,n);out.flush();}finally{try{in.close();}finally{out.close();}}}
}
