package com.hfr.world.earth.pack;
import java.util.*; import cpw.mods.fml.common.Loader;
public final class XFEarthMapPack { public final XFEarthMapManifest manifest; public final XFEarthMapSource source; public final String archiveSha256;
 public XFEarthMapPack(XFEarthMapManifest m,XFEarthMapSource s,String hash){manifest=m;source=s;archiveSha256=hash;}
 public List<String> missingMods(){List<String> r=new ArrayList<String>();for(String id:manifest.requiredMods)if(!Loader.isModLoaded(id))r.add(id);return r;} public boolean isCompatible(){return missingMods().isEmpty();}
}
