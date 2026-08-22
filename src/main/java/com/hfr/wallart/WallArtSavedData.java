package com.hfr.wallart;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraft.world.WorldSavedData;
import net.minecraft.world.WorldServer;

/** Per-save, overworld-backed ownership, overlap and image-reference index. */
public final class WallArtSavedData extends WorldSavedData {
    public static final String ID = "xenofactions_wall_art";
    private final Map<UUID, Record> records = new HashMap<UUID, Record>();
    private final Map<UUID, Integer> ownerCounts = new HashMap<UUID, Integer>();
    private final Map<String, UUID> occupied = new HashMap<String, UUID>();
    private final Map<String, Integer> hashReferences = new HashMap<String, Integer>();

    public WallArtSavedData() { super(ID); }
    public WallArtSavedData(String id) { super(id); }
    public static WallArtSavedData get(World ignored) {
        WorldServer root = MinecraftServer.getServer().worldServerForDimension(0);
        WallArtSavedData data = (WallArtSavedData)root.perWorldStorage.loadData(WallArtSavedData.class, ID);
        if(data == null) { data = new WallArtSavedData(); root.perWorldStorage.setData(ID, data); }
        return data;
    }
    public synchronized int count(UUID owner) { Integer n = ownerCounts.get(owner); return n == null ? 0 : n.intValue(); }
    public synchronized Record get(UUID id) { return records.get(id); }
    public synchronized boolean references(String hash) { Integer n = hashReferences.get(hash); return n != null && n.intValue() > 0; }
    public synchronized Collection<Record> records() { return new ArrayList<Record>(records.values()); }
    public synchronized boolean overlaps(Record candidate, UUID ignore) {
        for(String key : cells(candidate)) { UUID id = occupied.get(key); if(id != null && !id.equals(ignore)) return true; }
        return false;
    }
    public synchronized boolean put(Record record) {
        Record old = records.get(record.id);
        if(old == null && count(record.owner) >= WallArtConstants.MAX_PER_PLAYER) return false;
        if(overlaps(record, record.id)) return false;
        if(old != null) removeIndexes(old);
        records.put(record.id, record); addIndexes(record); markDirty(); return true;
    }
    public synchronized Record remove(UUID id) { Record old = records.remove(id); if(old != null) { removeIndexes(old); markDirty(); } return old; }
    private void addIndexes(Record r) { increment(ownerCounts, r.owner); increment(hashReferences, r.hash); for(String cell : cells(r)) occupied.put(cell, r.id); }
    private void removeIndexes(Record r) { decrement(ownerCounts, r.owner); decrement(hashReferences, r.hash); for(String cell : cells(r)) if(r.id.equals(occupied.get(cell))) occupied.remove(cell); }
    private static <K> void increment(Map<K,Integer> map, K key) { Integer v=map.get(key); map.put(key, Integer.valueOf(v == null ? 1 : v.intValue()+1)); }
    private static <K> void decrement(Map<K,Integer> map, K key) { Integer v=map.get(key); if(v == null || v.intValue() <= 1) map.remove(key); else map.put(key,Integer.valueOf(v.intValue()-1)); }
    private Set<String> cells(Record r) {
        Set<String> out = new HashSet<String>();
        for(int u=0;u<r.width;u++) for(int v=0;v<r.height;v++) {
            int x=r.x,z=r.z; if(r.facing==2)x+=u; else if(r.facing==3)x-=u; else if(r.facing==4)z-=u; else z+=u;
            out.add(r.dimension+":"+r.facing+":"+x+":"+(r.y+v)+":"+z);
        }
        return out;
    }
    @Override public synchronized void readFromNBT(NBTTagCompound n) {
        records.clear(); NBTTagList list=n.getTagList("displays",10);
        for(int i=0;i<list.tagCount();i++) { Record r=Record.read(list.getCompoundTagAt(i)); if(r != null && !records.containsKey(r.id)) records.put(r.id,r); }
        rebuild();
    }
    private void rebuild() { ownerCounts.clear(); occupied.clear(); hashReferences.clear(); for(Record r : new ArrayList<Record>(records.values())) { if(overlaps(r,r.id) || count(r.owner) >= WallArtConstants.MAX_PER_PLAYER) records.remove(r.id); else addIndexes(r); } }
    @Override public synchronized void writeToNBT(NBTTagCompound n) { NBTTagList list=new NBTTagList(); for(Record r:records.values()) list.appendTag(r.write()); n.setTag("displays",list); }

    public static final class Record {
        public final UUID id, owner; public final int dimension,x,y,z,facing,width,height; public final String hash;
        public Record(UUID id, UUID owner, int dimension, int x, int y, int z, int facing, int width, int height, String hash) { this.id=id;this.owner=owner;this.dimension=dimension;this.x=x;this.y=y;this.z=z;this.facing=facing;this.width=width;this.height=height;this.hash=hash; }
        NBTTagCompound write(){NBTTagCompound n=new NBTTagCompound();n.setString("id",id.toString());n.setString("owner",owner.toString());n.setInteger("dimension",dimension);n.setInteger("x",x);n.setInteger("y",y);n.setInteger("z",z);n.setInteger("facing",facing);n.setInteger("width",width);n.setInteger("height",height);n.setString("hash",hash);return n;}
        static Record read(NBTTagCompound n){try{int w=n.getInteger("width"),h=n.getInteger("height"),f=n.getInteger("facing");String hash=n.getString("hash");if(!WallArtConstants.validSize(w,h)||!WallArtConstants.validFacing(f)||!WallArtConstants.validHash(hash))return null;return new Record(UUID.fromString(n.getString("id")),UUID.fromString(n.getString("owner")),n.getInteger("dimension"),n.getInteger("x"),n.getInteger("y"),n.getInteger("z"),f,w,h,hash);}catch(Exception e){return null;}}
    }
}
