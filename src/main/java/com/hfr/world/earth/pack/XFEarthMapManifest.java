package com.hfr.world.earth.pack;
import java.util.*;
public final class XFEarthMapManifest {
 public int formatVersion,effectiveScale,width,height; public String id,displayName,version,minecraftVersion,populationMode; public boolean bundled,requiresTemplateLevelDat; public long installedSize; public List<String> requiredMods=new ArrayList<String>(); public List<FileEntry> files=new ArrayList<FileEntry>();
 public static final class FileEntry { public String path,sha256; public long size; }
 public void validate(){if(formatVersion!=1)bad("formatVersion must be 1");if(!"1.7.10".equals(minecraftVersion))bad("minecraftVersion must be 1.7.10");if(id==null||!id.matches("[a-z0-9][a-z0-9._-]{2,63}"))bad("invalid pack id");if(version==null||version.trim().isEmpty())bad("version is required");if(effectiveScale<=0)bad("effectiveScale must be positive");if(width<=0||height<=0||width%16!=0||height%16!=0)bad("dimensions must be positive and chunk-aligned");if(files==null)bad("files is required");Set<String>s=new HashSet<String>();for(FileEntry f:files){String p=XFEarthMapVerifier.sanitizePath(f.path);if(!s.add(p))bad("duplicate path: "+p);if(f.size<0||f.sha256==null||!f.sha256.matches("[0-9a-fA-F]{64}"))bad("invalid file metadata: "+p);}}
 private static void bad(String m){throw new IllegalArgumentException("Invalid Earth map manifest: "+m);}
}
