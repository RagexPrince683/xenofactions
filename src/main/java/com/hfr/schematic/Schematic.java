package com.hfr.schematic;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import cpw.mods.fml.common.registry.FMLControlledNamespacedRegistry;
import cpw.mods.fml.common.registry.GameData;
import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;

/** Xenofactions-owned, normalized schematic. No optional-mod type is retained. */
public final class Schematic {
    private static final FMLControlledNamespacedRegistry<Block> REGISTRY = GameData.getBlockRegistry();
    public final short[][][] blocks; // retained for old saved jobs/render call sites
    public final byte[][][] metadata;
    private final String[][][] registryNames;
    private final Map<Integer, NBTTagCompound> safeTiles = new HashMap<Integer, NBTTagCompound>();
    public final int width, height, length;
    public String name = "", sourceFormat = "schematic";
    public int value = 1;

    public Schematic(int width, int height, int length) {
        if (width <= 0 || height <= 0 || length <= 0) throw new IllegalArgumentException("Invalid schematic dimensions");
        this.width=width; this.height=height; this.length=length;
        blocks=new short[width][height][length]; metadata=new byte[width][height][length];
        registryNames=new String[width][height][length];
    }
    public int size(){ return Math.multiplyExact(Math.multiplyExact(width,height),length); }
    public int index(int x,int y,int z){ check(x,y,z); return (x*height+y)*length+z; }
    public boolean setBlock(int x,int y,int z,Block block,int meta){
        if(block==null) return false; String name=REGISTRY.getNameForObject(block);
        return name!=null && setBlockName(x,y,z,name,meta);
    }
    public boolean setBlockName(int x,int y,int z,String name,int meta){
        check(x,y,z); Object object=REGISTRY.getObject(name);
        if(!(object instanceof Block)) return false;
        registryNames[x][y][z]=name; blocks[x][y][z]=(short)REGISTRY.getId((Block)object); metadata[x][y][z]=(byte)(meta&15); return true;
    }
    public String getBlockName(int x,int y,int z){ check(x,y,z); String n=registryNames[x][y][z]; if(n==null){ Block b=REGISTRY.getObjectById(blocks[x][y][z]&0xffff); n=b==null?null:REGISTRY.getNameForObject(b); registryNames[x][y][z]=n; } return n; }
    public Block resolveBlock(int x,int y,int z){ String n=getBlockName(x,y,z); Object b=n==null?null:REGISTRY.getObject(n); return b instanceof Block?(Block)b:null; }
    public int getMetadata(int x,int y,int z){ check(x,y,z); return metadata[x][y][z]&15; }
    public void putSafeTile(int x,int y,int z,NBTTagCompound data){ check(x,y,z); if(data!=null)safeTiles.put(index(x,y,z),(NBTTagCompound)data.copy()); }
    public Map<Integer,NBTTagCompound> getSafeTiles(){ return Collections.unmodifiableMap(safeTiles); }
    private void check(int x,int y,int z){if(x<0||y<0||z<0||x>=width||y>=height||z>=length)throw new IndexOutOfBoundsException();}
}
