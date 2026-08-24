package com.hfr.builder;

import java.util.*;
import cpw.mods.fml.common.registry.GameData;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import com.hfr.schematic.Schematic;

/** Canonical, identity-based immutable material totals and unsupported mappings. */
public final class BuilderMaterialRequirements {
    public static final class Key {
        public final ItemStack example;private final BuilderMaterialKey identity;
        Key(ItemStack s){example=s.copy();example.stackSize=1;identity=new BuilderMaterialKey(example);}
        @Override public int hashCode(){return identity.hashCode();}
        @Override public boolean equals(Object o){return o instanceof Key&&identity.equals(((Key)o).identity);}
    }
    public static final class Unsupported {public final String block;public final int metadata,quantity,x,y,z;Unsupported(String b,int m,int q,int x,int y,int z){block=b;metadata=m;quantity=q;this.x=x;this.y=y;this.z=z;}}
    public final Map<Key,Integer> totals;public final List<Unsupported> unsupported;public final int totalItems;
    private BuilderMaterialRequirements(Map<Key,Integer> t,List<Unsupported> u){totals=Collections.unmodifiableMap(t);unsupported=Collections.unmodifiableList(u);int n=0;for(Integer i:t.values())n+=i;totalItems=n;}
    public static BuilderMaterialRequirements calculate(Schematic s){Map<Key,Integer> totals=new LinkedHashMap<Key,Integer>();Map<String,int[]> bad=new LinkedHashMap<String,int[]>();for(int x=0;x<s.width;x++)for(int y=0;y<s.height;y++)for(int z=0;z<s.length;z++){Block b=s.resolveBlock(x,y,z);if(b==null||b==Blocks.air)continue;int meta=s.getMetadata(x,y,z);ItemStack item=BuilderMaterialResolver.resolve(b,meta);if(item==null){String name=String.valueOf(GameData.getBlockRegistry().getNameForObject(b)),key=name+":"+meta;int[] v=bad.get(key);if(v==null){v=new int[]{0,x,y,z,meta};bad.put(key,v);}v[0]++;continue;}Key key=new Key(item);Integer old=totals.get(key);totals.put(key,(old==null?0:old)+1);}List<Unsupported> unsupported=new ArrayList<Unsupported>();for(Map.Entry<String,int[]> e:bad.entrySet()){int[] v=e.getValue();String key=e.getKey();unsupported.add(new Unsupported(key.substring(0,key.lastIndexOf(':')),v[4],v[0],v[1],v[2],v[3]));}return new BuilderMaterialRequirements(totals,unsupported);}
    public static int count(ItemStack[] inventory,Key key){int n=0;if(inventory!=null)for(ItemStack s:inventory)if(s!=null&&new Key(s).equals(key))n+=s.stackSize;return n;}
}
