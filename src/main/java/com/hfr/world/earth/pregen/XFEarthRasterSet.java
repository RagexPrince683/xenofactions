package com.hfr.world.earth.pregen;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;
import javax.imageio.*;
import javax.imageio.stream.ImageInputStream;

/** Region-sized lazy PNG reads with a shared bounded LRU. */
public final class XFEarthRasterSet implements Closeable {
 private static final int TILE=512;
 private final Map<String,Source> sources=new HashMap<String,Source>();
 private final LinkedHashMap<Key,BufferedImage> cache;
 public XFEarthRasterSet(File root,final int capacity)throws IOException{
  cache=new LinkedHashMap<Key,BufferedImage>(capacity+1,.75f,true){protected boolean removeEldestEntry(Map.Entry<Key,BufferedImage> e){return size()>capacity;}};
  for(String name:XFEarthSourceManifest.required())sources.put(name,new Source(new File(root,name)));
 }
 public synchronized int sample(String name,int x,int z)throws IOException{return tile(name,x,z).getRaster().getSample(x%TILE,z%TILE,0);}
 public synchronized int rgb(String name,int x,int z)throws IOException{return tile(name,x,z).getRGB(x%TILE,z%TILE)&0xffffff;}
 private BufferedImage tile(String name,int x,int z)throws IOException{
  int tx=x/TILE,tz=z/TILE;Key k=new Key(name,tx,tz);BufferedImage image=cache.get(k);if(image!=null)return image;
  Source s=sources.get(name);if(s==null)throw new IOException("Unknown Earth raster: "+name);ImageReadParam p=s.reader.getDefaultReadParam();p.setSourceRegion(new Rectangle(tx*TILE,tz*TILE,Math.min(TILE,s.width-tx*TILE),Math.min(TILE,s.height-tz*TILE)));image=s.reader.read(0,p);cache.put(k,image);return image;
 }
 public synchronized void close()throws IOException{cache.clear();IOException failure=null;for(Source s:sources.values())try{s.close();}catch(IOException e){failure=e;}sources.clear();if(failure!=null)throw failure;}
 private static final class Source implements Closeable{final ImageInputStream in;final ImageReader reader;final int width,height;Source(File f)throws IOException{in=ImageIO.createImageInputStream(f);if(in==null)throw new IOException("Cannot open "+f);Iterator<ImageReader> i=ImageIO.getImageReaders(in);if(!i.hasNext())throw new IOException("No PNG reader for "+f);reader=i.next();reader.setInput(in,true,true);width=reader.getWidth(0);height=reader.getHeight(0);}public void close()throws IOException{reader.dispose();in.close();}}
 private static final class Key{final String name;final int x,z;Key(String n,int x,int z){name=n;this.x=x;this.z=z;}public int hashCode(){return 31*(31*name.hashCode()+x)+z;}public boolean equals(Object o){if(!(o instanceof Key))return false;Key k=(Key)o;return x==k.x&&z==k.z&&name.equals(k.name);}}
}
