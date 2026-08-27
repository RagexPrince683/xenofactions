package com.hfr.world.earth.pregen;

import com.google.gson.Gson;
import java.awt.image.BufferedImage;
import java.io.*;
import java.security.MessageDigest;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

public final class XFEarthSourceVerifier {
 public static final String MANIFEST="xenoearth-source.json";
 private XFEarthSourceVerifier(){}
 public static XFEarthSourceManifest verify(File directory)throws IOException{
  File mf=new File(directory,MANIFEST);if(!mf.isFile())throw new FileNotFoundException("Missing Earth source manifest: "+mf);
  XFEarthSourceManifest m;Reader r=new InputStreamReader(new FileInputStream(mf),"UTF-8");try{m=new Gson().fromJson(r,XFEarthSourceManifest.class);}finally{r.close();}
  if(m==null)throw new IOException("Empty Earth source manifest: "+mf);try{m.validate();}catch(IllegalArgumentException e){throw new IOException(e.getMessage(),e);}
  for(String name:XFEarthSourceManifest.required()){
   File f=new File(directory,name);if(!f.isFile())throw new FileNotFoundException("Missing required Earth raster: "+f);
   String actual=sha256(f);if(!actual.equalsIgnoreCase(m.rasters.get(name)))throw new IOException("SHA-256 mismatch for "+name+": expected "+m.rasters.get(name)+", got "+actual);
   ImageInputStream in=ImageIO.createImageInputStream(f);if(in==null)throw new IOException("Cannot open PNG: "+f);try{java.util.Iterator<ImageReader> it=ImageIO.getImageReaders(in);if(!it.hasNext())throw new IOException("Unsupported PNG: "+f);ImageReader reader=it.next();try{reader.setInput(in,true,true);if(reader.getWidth(0)!=m.width||reader.getHeight(0)!=m.height)throw new IOException("Raster dimensions for "+name+" must be "+m.width+"x"+m.height+", got "+reader.getWidth(0)+"x"+reader.getHeight(0));}finally{reader.dispose();}}finally{in.close();}
  }
  return m;
 }
 public static String manifestHash(File directory)throws IOException{return sha256(new File(directory,MANIFEST));}
 public static String sha256(File f)throws IOException{try{MessageDigest d=MessageDigest.getInstance("SHA-256");InputStream in=new BufferedInputStream(new FileInputStream(f));try{byte[]b=new byte[65536];for(int n;(n=in.read(b))!=-1;)d.update(b,0,n);}finally{in.close();}StringBuilder s=new StringBuilder();for(byte x:d.digest())s.append(String.format("%02x",x&255));return s.toString();}catch(java.security.NoSuchAlgorithmException e){throw new AssertionError(e);}}
}
