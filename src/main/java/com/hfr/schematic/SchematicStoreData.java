package com.hfr.schematic;

import java.security.MessageDigest;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraft.world.WorldSavedData;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;

/** Server-owned normalized schematics referenced by persistent Builder jobs. */
public final class SchematicStoreData extends WorldSavedData {
    private static final String ID="hfr_builder_schematics";
    private final Map<String,Schematic> schematics=new LinkedHashMap<String,Schematic>();
    public SchematicStoreData(){super(ID);} public SchematicStoreData(String id){super(id);}
    public static SchematicStoreData get(World world){
        if(world==null||world.isRemote)return null; WorldServer root=DimensionManager.getWorld(0); if(root==null)return null;
        SchematicStoreData d=(SchematicStoreData)root.perWorldStorage.loadData(SchematicStoreData.class,ID);
        if(d==null){d=new SchematicStoreData();root.perWorldStorage.setData(ID,d);}return d;
    }
    public Schematic getSchematic(String id){return id==null?null:schematics.get(id);}
    public String putSchematic(Schematic s){if(s==null)throw new IllegalArgumentException("Missing schematic");String id=hash(s);if(!schematics.containsKey(id)){schematics.put(id,s);markDirty();}return id;}
    private static String hash(Schematic s){try{MessageDigest d=MessageDigest.getInstance("SHA-256");update(d,s.width);update(d,s.height);update(d,s.length);for(int x=0;x<s.width;x++)for(int y=0;y<s.height;y++)for(int z=0;z<s.length;z++){d.update(s.getBlockName(x,y,z).getBytes("UTF-8"));d.update((byte)0);d.update((byte)s.getMetadata(x,y,z));}StringBuilder b=new StringBuilder();for(byte v:d.digest())b.append(String.format("%02x",v&255));return b.toString();}catch(Exception e){throw new IllegalStateException(e);}}
    private static void update(MessageDigest d,int n){d.update((byte)(n>>>24));d.update((byte)(n>>>16));d.update((byte)(n>>>8));d.update((byte)n);}
    @Override public void writeToNBT(NBTTagCompound root){NBTTagList list=new NBTTagList();for(Map.Entry<String,Schematic> e:schematics.entrySet()){Schematic s=e.getValue();NBTTagCompound n=new NBTTagCompound();n.setString("id",e.getKey());n.setString("name",s.name);n.setString("format",s.sourceFormat);n.setInteger("w",s.width);n.setInteger("h",s.height);n.setInteger("l",s.length);NBTTagList blocks=new NBTTagList();for(int x=0;x<s.width;x++)for(int y=0;y<s.height;y++)for(int z=0;z<s.length;z++){NBTTagCompound b=new NBTTagCompound();b.setString("block",s.getBlockName(x,y,z));b.setByte("meta",(byte)s.getMetadata(x,y,z));blocks.appendTag(b);}n.setTag("blocks",blocks);list.appendTag(n);}root.setTag("schematics",list);}
    @Override public void readFromNBT(NBTTagCompound root){schematics.clear();NBTTagList list=root.getTagList("schematics",10);for(int i=0;i<list.tagCount();i++)try{NBTTagCompound n=list.getCompoundTagAt(i);Schematic s=new Schematic(n.getInteger("w"),n.getInteger("h"),n.getInteger("l"));s.name=n.getString("name");s.sourceFormat=n.getString("format");NBTTagList blocks=n.getTagList("blocks",10);int p=0;for(int x=0;x<s.width;x++)for(int y=0;y<s.height;y++)for(int z=0;z<s.length;z++){NBTTagCompound b=blocks.getCompoundTagAt(p++);if(!s.setBlockName(x,y,z,b.getString("block"),b.getByte("meta")))throw new IllegalArgumentException();}schematics.put(n.getString("id"),s);}catch(Exception ignored){}}
}
