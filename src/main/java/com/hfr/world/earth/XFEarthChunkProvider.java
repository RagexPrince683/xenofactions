package com.hfr.world.earth;
import java.util.*;
import com.hfr.config.XFConfig; import com.hfr.main.MainRegistry;
import net.minecraft.entity.EnumCreatureType; import net.minecraft.util.IProgressUpdate;
import net.minecraft.world.*; import net.minecraft.world.biome.BiomeGenBase; import net.minecraft.world.chunk.*;
/** Generator invoked only when Anvil has no saved chunk. It never generates terrain. */
public final class XFEarthChunkProvider implements IChunkProvider {
 private final World world; private final XFEarthProfile profile; private final Set<Long> logged=new HashSet<Long>();
 public XFEarthChunkProvider(World world){this.world=world; XFEarthProfile p=null; try{p=XFEarthProfileLoader.load(world.getSaveHandler().getWorldDirectory());}catch(Exception e){if(XFConfig.earthRequireProfile) throw new IllegalStateException("xf_earth requires a valid "+XFEarthProfileLoader.FILE_NAME,e); if(MainRegistry.logger!=null)MainRegistry.logger.error("[XF EARTH] Profile unavailable; only void fallback is possible",e);} profile=p;}
 public boolean chunkExists(int x,int z){return false;}
 public Chunk provideChunk(int x,int z){return fallback(x,z);}
 public Chunk loadChunk(int x,int z){return fallback(x,z);}
 private Chunk fallback(int x,int z){boolean inside=profile!=null&&profile.getBounds().containsChunk(x,z); long key=((long)x<<32)^(z&0xffffffffL);
  if(inside&&"FAIL".equals(XFConfig.earthMissingChunkPolicy)){XFEarthMissingChunkException ex=new XFEarthMissingChunkException(x,z,profile); if(logged.add(key)&&MainRegistry.logger!=null)MainRegistry.logger.error("[XF EARTH] "+ex.getMessage()); throw ex;}
  if(logged.add(key)&&XFConfig.earthLogFallbackChunks&&MainRegistry.logger!=null)MainRegistry.logger.warn("[XF EARTH] RETURNING VOID for "+(inside?"MISSING IN-BOUNDS":"out-of-bounds")+" chunk ("+x+", "+z+")");
  Chunk chunk=new Chunk(world,x,z); byte[] biomes=new byte[256]; Arrays.fill(biomes,(byte)BiomeGenBase.plains.biomeID); chunk.setBiomeArray(biomes); chunk.generateSkylightMap(); chunk.isTerrainPopulated=true; chunk.isLightPopulated=true; return chunk;
 }
 public void populate(IChunkProvider provider,int x,int z){} public boolean saveChunks(boolean all,IProgressUpdate progress){return true;} public boolean unloadQueuedChunks(){return false;} public boolean canSave(){return true;}
 public String makeString(){return "XFEarthPregeneratedFallback";}
 public List<BiomeGenBase.SpawnListEntry> getPossibleCreatures(EnumCreatureType type,int x,int y,int z){if(profile==null||!profile.getBounds().containsChunk(XFEarthBounds.floorDiv(x,16),XFEarthBounds.floorDiv(z,16)))return Collections.emptyList(); return world.getBiomeGenForCoords(x,z).getSpawnableList(type);}
 public ChunkPosition func_147416_a(World world,String structure,int x,int y,int z){return null;} public int getLoadedChunkCount(){return 0;} public void recreateStructures(int x,int z){} public void saveExtraData(){}
}
