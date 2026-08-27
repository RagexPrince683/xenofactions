package com.hfr.world.earth.pregen;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.hfr.config.XFConfig;
import com.hfr.main.MainRegistry;
import com.hfr.world.earth.pack.XFEarthMapInstaller;
import java.io.*;
import java.util.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.AnvilChunkLoader;

/** Single-writer, resumable region-order raster pregenerator. */
public final class XFEarthPregenerator implements Runnable {
 public static final String STATE="xfearth-pregen.json",MARKER=".xfearth-pregen-owned";
 private static XFEarthPregenerator active;
 private final WorldServer world;private final File source,target,temp;private volatile boolean paused,cancelled;private Thread thread;private XFEarthPregenState state;
 private XFEarthPregenerator(WorldServer w,File source,File target)throws IOException{world=w;this.source=source;this.target=target;temp=new File(target.getParentFile(),target.getName()+".xfearth-generating");prepare();}
 public static synchronized XFEarthPregenerator start(WorldServer world,File source,File target)throws IOException{if(active!=null&&active.thread!=null&&active.thread.isAlive())throw new IOException("An Earth pregeneration is already running");active=new XFEarthPregenerator(world,source,target);active.thread=new Thread(active,"XF Earth pregenerator");active.thread.setDaemon(true);active.thread.start();return active;}
 public static synchronized XFEarthPregenerator current(){return active;}
 public synchronized void pause(){paused=true;state.completionState="PAUSED";safeState();}
 public synchronized void resume(){paused=false;state.completionState="RUNNING";safeState();notifyAll();}
 public synchronized void cancel(){cancelled=true;paused=false;state.completionState="CANCELLED";safeState();notifyAll();}
 public synchronized String status(){return state==null?"idle":state.completionState+" "+state.completedChunks+"/"+state.totalChunks+" chunks ("+String.format(java.util.Locale.ROOT,"%.2f",100.0*state.completedChunks/state.totalChunks)+"%), region "+state.currentRegion;}
 private void prepare()throws IOException{
  XFEarthSourceManifest m=XFEarthSourceVerifier.verify(source);String hash=XFEarthSourceVerifier.manifestHash(source);if(target.exists())throw new IOException("Existing world will not be overwritten: "+target);
  if(temp.exists()){File marker=new File(temp,MARKER);if(!marker.isFile())throw new IOException("Partial directory is not proven XF-owned: "+temp);state=readState();if(!"earth2000".equals(state.profile)||!hash.equals(state.sourceManifestHash)||!XFEarthTerrainConverter.VERSION.equals(state.generatorVersion))throw new IOException("Partial generation source/profile/generator changed; move it aside before restarting");state.completedChunks=countCompletedChunks(state.completedRegions);state.currentRegion=null;}
  else{if(!temp.mkdirs())throw new IOException("Cannot create partial world: "+temp);state=new XFEarthPregenState();state.profile="earth2000";state.sourceManifestHash=hash;state.generatorVersion=XFEarthTerrainConverter.VERSION;state.totalRegions=42*22;state.totalChunks=903168;state.startTime=System.currentTimeMillis();state.completionState="READY";writeText(new File(temp,MARKER),"XenoFactions owned Earth partial\nprofile=earth2000\nmanifest="+hash+"\n");writeState();}
 }
 public void run(){long started=System.currentTimeMillis();try{
  state.completionState="RUNNING";writeState();log("source verified: "+source+"; dimensions 21504x10752; chunks -672..671 x -336..335; regions -21..20 x -11..10; resume="+(state.completedChunks>0));
  XFEarthRasterSet rasters=new XFEarthRasterSet(source,XFConfig.earthRasterTileCache);AnvilChunkLoader loader=new AnvilChunkLoader(temp);try{XFEarthTerrainConverter converter=new XFEarthTerrainConverter(rasters);
   for(int rz=-11;rz<=10;rz++)for(int rx=-21;rx<=20;rx++){String key=rx+","+rz;if(state.completedRegions.contains(key))continue;waitIfPaused();if(cancelled)return;state.currentRegion=key;writeState();int minX=Math.max(-672,rx*32),maxX=Math.min(671,rx*32+31),minZ=Math.max(-336,rz*32),maxZ=Math.min(335,rz*32+31);
    for(int cz=minZ;cz<=maxZ;cz++)for(int cx=minX;cx<=maxX;cx++){waitIfPaused();if(cancelled)return;Chunk chunk=converter.create(world,cx,cz);loader.saveChunk(world,chunk);state.completedChunks++;}
    loader.saveExtraData();state.completedRegions.add(key);state.currentRegion=null;writeState();double seconds=Math.max(1,(System.currentTimeMillis()-started)/1000.0);log("region "+key+" complete; "+status()+"; "+String.format(Locale.ROOT,"%.1f",state.completedChunks/seconds)+" chunks/s");
   }
   verify(loader);writeMetadata();state.completionState="COMPLETE";writeState();if(!new File(temp,MARKER).delete())throw new IOException("Cannot remove ownership marker");if(!temp.renameTo(target))throw new IOException("Cannot atomically finalize Earth world as "+target);log("final verification succeeded; Earth world ready at "+target);
  }finally{rasters.close();}
 }catch(Throwable e){state.completionState=cancelled?"CANCELLED":"FAILED";safeState();if(MainRegistry.logger!=null)MainRegistry.logger.error("[XF EARTH PREGEN] stopped: "+e.getMessage(),e);}}
 private synchronized void waitIfPaused()throws InterruptedException{while(paused&&!cancelled)wait();}
 private static int countCompletedChunks(List<String> regions){int total=0;for(String key:regions){String[]p=key.split(",");int rx=Integer.parseInt(p[0]),rz=Integer.parseInt(p[1]);int minX=Math.max(-672,rx*32),maxX=Math.min(671,rx*32+31),minZ=Math.max(-336,rz*32),maxZ=Math.min(335,rz*32+31);total+=(maxX-minX+1)*(maxZ-minZ+1);}return total;}
 private void verify(AnvilChunkLoader loader)throws IOException{log("verifying all "+state.totalChunks+" chunks");for(int z=-336;z<=335;z++)for(int x=-672;x<=671;x++)if(!loader.chunkExists(world,x,z))throw new IOException("Final verification found missing chunk "+x+","+z);}
 private void writeMetadata()throws IOException{
  Map<String,Object> p=new LinkedHashMap<String,Object>();p.put("formatVersion",1);p.put("profile","earth2000");p.put("targetMinecraftVersion","1.7.10");p.put("sourceScale",20);p.put("resize",100);p.put("effectiveScale",2000);p.put("width",21504);p.put("height",10752);p.put("minimumX",-10752);p.put("maximumX",10751);p.put("minimumZ",-5376);p.put("maximumZ",5375);p.put("minimumSurfaceY",1);p.put("maximumSurfaceY",254);p.put("seaLevel",62);p.put("projection","equirectangular");p.put("populationMode","pregenerated");p.put("caves",false);p.put("ores",false);p.put("lava",false);p.put("structures",false);p.put("vegetation",false);writeJson(new File(temp,"xenoearth-profile.json"),p);
  XFEarthMapInstaller.Settings s=new XFEarthMapInstaller.Settings();s.levelName=target.getName();s.generatorOptions="earth2000";s.seed=world.getSeed();s.gameType=world.getWorldInfo().getGameType().getID();s.hardcore=world.getWorldInfo().isHardcoreModeEnabled();s.structures=false;s.commands=world.getWorldInfo().areCommandsAllowed();s.bonusChest=false;XFEarthMapInstaller.patchLevel(new File(world.getSaveHandler().getWorldDirectory(),"level.dat"),new File(temp,"level.dat"),s);
  Map<String,Object> install=new LinkedHashMap<String,Object>();install.put("formatVersion",1);install.put("sourceMode","RASTER_PREGEN");install.put("profile","earth2000");install.put("sourceManifestSha256",state.sourceManifestHash);install.put("generatorVersion",XFEarthTerrainConverter.VERSION);install.put("installedAt",System.currentTimeMillis());install.put("populationMode","pregenerated");writeJson(new File(temp,"xfearth-install.json"),install);
 }
 private XFEarthPregenState readState()throws IOException{Reader r=new InputStreamReader(new FileInputStream(new File(temp,STATE)),"UTF-8");try{XFEarthPregenState s=new Gson().fromJson(r,XFEarthPregenState.class);if(s==null||s.formatVersion!=1)throw new IOException("Invalid partial generation state");return s;}finally{r.close();}}
 private void writeState()throws IOException{state.lastUpdateTime=System.currentTimeMillis();writeJson(new File(temp,STATE),state);}
 private void safeState(){try{writeState();}catch(IOException e){if(MainRegistry.logger!=null)MainRegistry.logger.error("[XF EARTH PREGEN] cannot persist state",e);}}
 private static void writeJson(File f,Object value)throws IOException{File t=new File(f.getPath()+".tmp");Writer w=new OutputStreamWriter(new FileOutputStream(t),"UTF-8");try{new GsonBuilder().setPrettyPrinting().create().toJson(value,w);w.write('\n');}finally{w.close();}if(f.exists()&&!f.delete())throw new IOException("Cannot replace "+f);if(!t.renameTo(f))throw new IOException("Cannot atomically write "+f);}
 private static void writeText(File f,String text)throws IOException{Writer w=new OutputStreamWriter(new FileOutputStream(f),"UTF-8");try{w.write(text);}finally{w.close();}}
 private static void log(String s){if(MainRegistry.logger!=null)MainRegistry.logger.info("[XF EARTH PREGEN] "+s);}
 public static File sourceRoot(){return XFConfig.earthSourceDirectory.trim().isEmpty()?new File("xenofactions/earthmaps/sources"):new File(XFConfig.earthSourceDirectory);}
}
