package com.hfr.schematic;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.hfr.config.XFConfig;
import cpw.mods.fml.common.registry.GameData;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;

/** Strict native MCEdit/Schematica .schematic reader. */
public final class SchematicLoader {
    private SchematicLoader(){}
    public static Schematic readFromFile(File file){
        if(file==null||!file.isFile()||file.length()>XFConfig.builderMaxUploadBytes)return null;
        try{BufferedInputStream in=new BufferedInputStream(new FileInputStream(file));try{Schematic s=readFromNBT(CompressedStreamTools.readCompressed(in));s.name=strip(file.getName());return s;}finally{in.close();}}
        catch(Exception e){System.err.println("Rejected schematic "+file+": "+e.getMessage());return null;}
    }
    public static Schematic readFromNBT(NBTTagCompound n) throws IOException {
        int w=n.getShort("Width")&0xffff,h=n.getShort("Height")&0xffff,l=n.getShort("Length")&0xffff;
        validateDimensions(w,h,l); int count=checkedCount(w,h,l);
        byte[] ids=n.getByteArray("Blocks"), data=n.getByteArray("Data");
        if(ids.length!=count||data.length!=count)throw new IOException("Blocks/Data length does not match dimensions");
        byte[] add=null; boolean packed=false;
        if(n.hasKey("AddBlocks")){add=n.getByteArray("AddBlocks");packed=true;if(add.length!=(count+1)/2)throw new IOException("Invalid AddBlocks length");}
        else if(n.hasKey("Add")){add=n.getByteArray("Add");if(add.length!=count)throw new IOException("Invalid Add length");}
        Map<Integer,String> mapping=new HashMap<Integer,String>();
        if(n.hasKey("SchematicaMapping")){NBTTagCompound m=n.getCompoundTag("SchematicaMapping");Set<String> keys=m.func_150296_c();for(String key:keys)mapping.put((int)m.getShort(key)&0xffff,key);}
        Schematic out=new Schematic(w,h,l);
        for(int y=0;y<h;y++)for(int z=0;z<l;z++)for(int x=0;x<w;x++){
            int i=x+(y*l+z)*w, high=add==null?0:(packed?((add[i>>1]>>((i&1)*4))&15):(add[i]&255));
            int id=(ids[i]&255)|(high<<8); String name=mapping.get(id);
            if(name==null){Block b=GameData.getBlockRegistry().getObjectById(id);name=b==null?null:GameData.getBlockRegistry().getNameForObject(b);}
            if(name==null||!out.setBlockName(x,y,z,name,data[i]&15))throw new IOException("Unknown block "+id+" at "+x+","+y+","+z);
            if(out.resolveBlock(x,y,z)==Blocks.command_block)throw new IOException("Protected command block at "+x+","+y+","+z);
        }
        return out;
    }
    public static void validateDimensions(int w,int h,int l) throws IOException{if(w<=0||h<=0||l<=0||w>XFConfig.builderMaxSchematicWidth||h>XFConfig.builderMaxSchematicHeight||l>XFConfig.builderMaxSchematicLength)throw new IOException("Schematic dimensions exceed configured limits");checkedCount(w,h,l);}
    private static int checkedCount(int w,int h,int l)throws IOException{long count=(long)w*h*l;if(count>XFConfig.builderMaxSchematicBlocks||count>Integer.MAX_VALUE)throw new IOException("Schematic block count exceeds configured limit");return(int)count;}
    private static String strip(String n){int dot=n.toLowerCase().lastIndexOf(".schematic");return dot<0?n:n.substring(0,dot);}
}
