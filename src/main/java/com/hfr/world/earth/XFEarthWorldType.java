package com.hfr.world.earth;
import net.minecraft.world.*; import net.minecraft.world.biome.WorldChunkManager; import net.minecraft.world.chunk.IChunkProvider;
public final class XFEarthWorldType extends WorldType {
 public XFEarthWorldType(){super("xf_earth");}
 @Override public IChunkProvider getChunkGenerator(World world,String options){return new XFEarthChunkProvider(world);}
 @Override public WorldChunkManager getChunkManager(World world){return new WorldChunkManager(world);}
 @Override public int getSpawnFuzz(){return 0;}
 @Override public boolean isCustomizable(){return false;}
}
