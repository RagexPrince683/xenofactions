package com.hfr.world.earth.pack;
import java.io.*;
public final class BundledEarthMapSource implements XFEarthMapSource {
 private final String resource;
 public BundledEarthMapSource(String resource){this.resource=resource.startsWith("/")?resource:"/"+resource;}
 public Provider getProvider(){return Provider.BUNDLED_RESOURCE;} public String getDescription(){return resource;}
 public InputStream open()throws IOException{InputStream in=BundledEarthMapSource.class.getResourceAsStream(resource);if(in==null)throw new FileNotFoundException("Missing bundled Earth map: "+resource);return new BufferedInputStream(in);}
 public boolean exists(){InputStream in=BundledEarthMapSource.class.getResourceAsStream(resource);if(in==null)return false;try{in.close();}catch(IOException ignored){}return true;}
}
