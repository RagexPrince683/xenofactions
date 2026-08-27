package com.hfr.world.earth.pregen;

import java.io.IOException;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.Chunk;

/** Pure Minecraft 1.7.10 terrain conversion; intentionally performs no population pass. */
public final class XFEarthTerrainConverter {
 public static final String VERSION="1";
 private static final int SEA=62;
 private final XFEarthRasterSet rasters;
 public XFEarthTerrainConverter(XFEarthRasterSet rasters){this.rasters=rasters;}
 public Chunk create(World world,int chunkX,int chunkZ)throws IOException{
  Block[] blocks=new Block[16*16*256];byte[] metadata=new byte[blocks.length],biomes=new byte[256];
  for(int lz=0;lz<16;lz++)for(int lx=0;lx<16;lx++){
   int wx=chunkX*16+lx,wz=chunkZ*16+lz,px=wx+10752,pz=wz+5376;
   int raw=rasters.sample("HeightMap20k.png",px,pz),surface=height(raw);
   int biome=biome(rasters.rgb("BiomeMap20k.png",px,pz));boolean river=water(rasters.rgb("WaterMap20k.png",px,pz));boolean ice=water(rasters.rgb("Ice20k.png",px,pz));
   boolean ocean=surface<SEA&&!river;Block top=surface(rasters.rgb("globecover20k.png",px,pz),biome,ocean);Block filler=filler(top,ocean);
   set(blocks,lx,lz,0,Blocks.bedrock);for(int y=1;y<=surface;y++)set(blocks,lx,lz,y,y==surface?top:(y>=surface-3?filler:Blocks.stone));
   if(ocean||river){for(int y=surface+1;y<=SEA;y++)set(blocks,lx,lz,y,Blocks.water);if(ice)set(blocks,lx,lz,SEA,Blocks.ice);}
   if(ocean)biome=surface<SEA-18?BiomeGenBase.deepOcean.biomeID:(ice?BiomeGenBase.frozenOcean.biomeID:BiomeGenBase.ocean.biomeID);else if(river)biome=ice?BiomeGenBase.frozenRiver.biomeID:BiomeGenBase.river.biomeID;
   biomes[lz*16+lx]=(byte)biome;
  }
  Chunk chunk=new Chunk(world,blocks,metadata,chunkX,chunkZ);chunk.setBiomeArray(biomes);chunk.generateSkylightMap();chunk.isTerrainPopulated=true;chunk.isLightPopulated=true;chunk.isModified=true;return chunk;
 }
 /** The source is unsigned 16-bit full-range elevation, with sea level encoded at 62/255. */
 static int height(int sample){int y=(int)Math.round((sample&0xffff)*255.0/65535.0);return Math.max(1,Math.min(254,y));}
 private static void set(Block[] a,int x,int z,int y,Block b){a[(x*16+z)*256+y]=b;}
 private static boolean water(int rgb){return rgb!=0;}
 private static int biome(int rgb){
  int gray=rgb&255;if(((rgb>>16)&255)==gray&&((rgb>>8)&255)==gray&&BiomeGenBase.getBiome(gray)!=null)return gray;
  Integer id=BiomeMappings.IDS.get(Integer.valueOf(rgb));return id==null?BiomeGenBase.plains.biomeID:id.intValue();
 }
 private static Block surface(int rgb,int biome,boolean ocean){if(ocean)return Blocks.sand;Block b=GlobCoverMappings.BLOCKS.get(Integer.valueOf(rgb));if(b!=null)return b;if(biome==BiomeGenBase.desert.biomeID||biome==BiomeGenBase.desertHills.biomeID||biome==BiomeGenBase.beach.biomeID)return Blocks.sand;if(biome==BiomeGenBase.icePlains.biomeID||biome==BiomeGenBase.iceMountains.biomeID)return Blocks.snow;return Blocks.grass;}
 private static Block filler(Block top,boolean ocean){if(ocean||top==Blocks.sand)return Blocks.sand;if(top==Blocks.snow)return Blocks.dirt;return top==Blocks.gravel?Blocks.gravel:Blocks.dirt;}
 /** Color table ported from the XenoEarth 1.7.10 biome map; unknown colors are deliberately plains. */
 static final class BiomeMappings {static final java.util.Map<Integer,Integer> IDS=new java.util.HashMap<Integer,Integer>();static{put(0x000070,0);put(0x0000ff,24);put(0x007fff,7);put(0x00ff00,1);put(0x228b22,4);put(0x006400,29);put(0x7cfc00,21);put(0x8b4513,3);put(0xd2b48c,2);put(0xffff00,2);put(0xffffff,12);put(0xadd8e6,10);put(0x808080,20);put(0xffa500,35);}private static void put(int color,int id){IDS.put(Integer.valueOf(color),Integer.valueOf(id));}}
 /** ESA GlobCover colors used by world_xenofactions_core.js, reduced to 1.7.10 blocks. */
 static final class GlobCoverMappings {static final java.util.Map<Integer,Block> BLOCKS=new java.util.HashMap<Integer,Block>();static{grass(0xa8a800,0xffff00,0xcfaa7d,0xd3ffbe,0x005000,0x006400,0xaac800,0x003c00,0x286400,0x788200,0x8ca000,0xbe9600,0x966400,0xffb432,0x00785a,0x009678,0x00dc82);sand(0xffdcd2,0xffebaf);put(0xc31400,Blocks.hardened_clay);put(0xfff5d7,Blocks.snow);put(0x0046c8,Blocks.sand);put(0xffffff,Blocks.snow);}private static void grass(int...c){for(int x:c)put(x,Blocks.grass);}private static void sand(int...c){for(int x:c)put(x,Blocks.sand);}private static void put(int c,Block b){BLOCKS.put(Integer.valueOf(c),b);}}
}
