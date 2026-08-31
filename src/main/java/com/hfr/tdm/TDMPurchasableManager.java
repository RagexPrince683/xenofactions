package com.hfr.tdm;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Shared persisted definitions for additive utility and killstreak purchases. */
public final class TDMPurchasableManager {
    public enum Type { UTILITY, KILLSTREAK }
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Map<String, Map<Type, List<Definition>>> definitions = new LinkedHashMap<String, Map<Type, List<Definition>>>();
    private static final Map<String, List<ItemEntry>> pendingKillstreakRewards = new LinkedHashMap<String, List<ItemEntry>>();
    private static File saveFile;
    private TDMPurchasableManager() { }

    public static void init() {
        saveFile = new File(MinecraftServer.getServer().getEntityWorld().getSaveHandler().getWorldDirectory(), "tdm_purchasables.txt");
        load();
    }

    public static int add(String mapName, Type type, EntityPlayer player, int cost) {
        List<ItemEntry> items=new ArrayList<ItemEntry>();
        for(ItemStack stack:player.inventory.mainInventory)if(stack!=null)items.add(new ItemEntry(stack));
        Definition definition=new Definition();definition.name=pretty(type)+" "+(getDirect(mapName,type).size()+1);definition.cost=Math.max(0,cost);definition.items=items;
        getDirect(mapName,type).add(definition);save();return getDirect(mapName,type).size();
    }
    public static boolean remove(String mapName,Type type,int index){List<Definition> list=getDirect(mapName,type);if(index<0||index>=list.size())return false;list.remove(index);save();return true;}
    public static String[] getNames(String mapName,Type type){List<Definition> list=getEffective(mapName,type);String[] result=new String[list.size()];for(int i=0;i<result.length;i++)result[i]=list.get(i).name;return result;}
    public static int[] getCosts(String mapName,Type type){List<Definition> list=getEffective(mapName,type);int[] result=new int[list.size()];for(int i=0;i<result.length;i++)result[i]=Math.max(0,list.get(i).cost);return result;}

    /** Atomic server-authoritative debit/queue operation. */
    public static synchronized boolean purchase(EntityPlayer player,Type type,int index){
        if(player==null||!TDMManager.isCompetitivePlayer(player)||TDMManager.isMapVoteActive(player.worldObj))return false;
        TDMManager.TDMMap map=TDMManager.getSelectedMapData(player.worldObj);if(map==null)return false;
        List<Definition> list=getEffective(map.name,type);if(index<0||index>=list.size())return false;Definition selected=list.get(index);int cost=Math.max(0,selected.cost);
        if(type==Type.UTILITY){
            if(map.mode!=TDMManager.TDMGameMode.BOMB||!map.buyScoreEnabled||!TDMManager.isGlobalBombBuyPeriod(player)||TDMManager.getBuyScore(player)<cost)return false;
            if(!TDMManager.spendBuyScore(player,cost))return false;
            grant(player,selected.items);
        }else{
            TDMManager.KitSelectionContext context=TDMManager.getKitSelectionContext(player);
            if(!map.killstreaksEnabled||(context!=TDMManager.KitSelectionContext.RESPAWN_LOCK&&context!=TDMManager.KitSelectionContext.LOADOUT_SELECTION)||!TDMManager.spendPlayerKillScore(player,cost))return false;
            List<ItemEntry> queued=pendingKillstreakRewards.get(TDMManager.getPlayerKey(player));if(queued==null){queued=new ArrayList<ItemEntry>();pendingKillstreakRewards.put(TDMManager.getPlayerKey(player),queued);}queued.addAll(selected.items);
        }
        return true;
    }
    public static void applyPendingKillstreakRewards(EntityPlayer player){List<ItemEntry> items=pendingKillstreakRewards.remove(TDMManager.getPlayerKey(player));if(items!=null)grant(player,items);}
    public static void clearPending(EntityPlayer player){if(player!=null)pendingKillstreakRewards.remove(TDMManager.getPlayerKey(player));}
    public static void clearAllPending(){pendingKillstreakRewards.clear();}
    private static void grant(EntityPlayer player,List<ItemEntry> items){for(ItemEntry item:items){ItemStack stack=item.stack();if(stack!=null)player.inventory.addItemStackToInventory(stack);}player.inventory.markDirty();player.inventoryContainer.detectAndSendChanges();}
    private static String pretty(Type type){return type==Type.UTILITY?"Utility":"Killstreak";}
    private static List<Definition> getEffective(String mapName,Type type){List<Definition> direct=getDirect(mapName,type);return direct.isEmpty()?getDirect("",type):direct;}
    private static List<Definition> getDirect(String mapName,Type type){String map=TDMManager.normalizeMapName(mapName);Map<Type,List<Definition>> byType=definitions.get(map);if(byType==null){byType=new LinkedHashMap<Type,List<Definition>>();definitions.put(map,byType);}List<Definition> list=byType.get(type);if(list==null){list=new ArrayList<Definition>();byType.put(type,list);}return list;}
    private static void save(){if(saveFile==null)return;try{Writer out=new FileWriter(saveFile);try{GSON.toJson(definitions,out);}finally{out.close();}}catch(Exception e){e.printStackTrace();}}
    private static void load(){definitions.clear();if(saveFile==null||!saveFile.exists())return;try{Reader in=new FileReader(saveFile);try{java.lang.reflect.Type dataType=new TypeToken<Map<String,Map<Type,List<Definition>>>>(){}.getType();Map<String,Map<Type,List<Definition>>> loaded=GSON.fromJson(in,dataType);if(loaded!=null)definitions.putAll(loaded);}finally{in.close();}}catch(Exception e){e.printStackTrace();}}
    private static class Definition { String name; int cost; List<ItemEntry> items=new ArrayList<ItemEntry>(); }
    private static class ItemEntry { String itemName;int count;int metadata;String nbtData;ItemEntry(){}ItemEntry(ItemStack stack){itemName=Item.itemRegistry.getNameForObject(stack.getItem());count=stack.stackSize;metadata=stack.getItemDamage();nbtData=stack.hasTagCompound()?stack.getTagCompound().toString():null;}ItemStack stack(){Item item=(Item)Item.itemRegistry.getObject(itemName);if(item==null)return null;ItemStack stack=new ItemStack(item,count,metadata);if(nbtData!=null)try{stack.setTagCompound((NBTTagCompound)JsonToNBT.func_150315_a(nbtData));}catch(Exception e){return null;}return stack;}}
}
