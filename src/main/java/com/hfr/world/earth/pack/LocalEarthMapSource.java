package com.hfr.world.earth.pack;
import java.io.*;
public final class LocalEarthMapSource implements XFEarthMapSource {
 private final File file; private final Provider provider;
 public LocalEarthMapSource(File file){this(file,Provider.LOCAL_FILE);} public LocalEarthMapSource(File file,Provider provider){this.file=file;this.provider=provider;}
 public Provider getProvider(){return provider;} public String getDescription(){return file.getAbsolutePath();} public InputStream open()throws IOException{return new BufferedInputStream(new FileInputStream(file));} public File getFile(){return file;}
}
