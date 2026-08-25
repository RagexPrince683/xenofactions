package com.hfr.compat;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.event.FMLInterModComms;
import cpw.mods.fml.common.registry.GameRegistry;
import com.hfr.main.MainRegistry;
import com.hfr.tdm.TDMBombManager;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** Optional HBM lookup for the CSGO bomb block; never links against HBM classes. */
public final class HbmCsgoChargeIntegration {
    public static final class Snapshot {
        public final boolean blockPresent,tilePresent; public final Boolean started; public final Integer timer; public final TileEntity tile; public final Class<?> tileClass;
        private Snapshot(boolean blockPresent,TileEntity tile,Boolean started,Integer timer){this.blockPresent=blockPresent;this.tilePresent=tile!=null;this.tile=tile;this.tileClass=tile==null?null:tile.getClass();this.started=started;this.timer=timer;}
    }
    private static final class Fields { final Field started,timer; Fields(Class<?> c){started=find(c,"started");timer=find(c,"timer");} }
    private static final Map<Class<?>,Fields> FIELD_CACHE=new ConcurrentHashMap<Class<?>,Fields>();

    private static final String HBM_MOD_ID = "hbm";
    public static final String BOMB_RESULT_KEY = "xenofactions_tdm_csgo_bomb_result";
    private static final String[] REGISTRY_NAMES = { "tile.charge_c4csgo", "charge_c4csgo" };
    private static boolean resolved;
    private static Block csgoCharge;
    private static Item csgoChargeItem;

    private HbmCsgoChargeIntegration() {
    }

    /** Consume HBM's authoritative CSGO lifecycle notifications on the server thread. */
    public static void pollBombResults() {
        if (MainRegistry.instance == null || MinecraftServer.getServer() == null) return;
        for (FMLInterModComms.IMCMessage message : FMLInterModComms.fetchRuntimeMessages(MainRegistry.instance)) {
            if (!HBM_MOD_ID.equals(message.getSender()) || !BOMB_RESULT_KEY.equals(message.key)
                    || !message.isNBTMessage()) continue;
            NBTTagCompound data = message.getNBTValue();
            if (!hasRequiredBombResultFields(data)) continue;
            String result = data.getString("result");
            if (!"DEFUSED".equals(result) && !"DETONATED".equals(result)) continue;
            int dimension = data.getInteger("dimension");
            World world = MinecraftServer.getServer().worldServerForDimension(dimension);
            if (world == null) continue;
            String playerUuid = data.hasKey("player_uuid", 8) ? data.getString("player_uuid") : null;
            String playerName = data.hasKey("player_name", 8) ? data.getString("player_name") : null;
            TDMBombManager.handleHbmBombResult(world, result, dimension,
                    data.getInteger("x"), data.getInteger("y"), data.getInteger("z"), playerUuid, playerName);
        }
    }

    private static boolean hasRequiredBombResultFields(NBTTagCompound data) {
        return data != null && data.hasKey("result", 8) && data.hasKey("dimension", 3)
                && data.hasKey("x", 3) && data.hasKey("y", 3) && data.hasKey("z", 3);
    }

    public static boolean isCsgoCharge(Block block) {
        if (!resolved) {
            resolve();
        }
        return csgoCharge != null && block == csgoCharge;
    }

    public static Snapshot snapshot(World world,int x,int y,int z){
        if(world==null||!isCsgoCharge(world.getBlock(x,y,z)))return new Snapshot(false,null,null,null);
        TileEntity tile=world.getTileEntity(x,y,z);if(tile==null)return new Snapshot(true,null,null,null);
        Fields f=FIELD_CACHE.get(tile.getClass());if(f==null){f=new Fields(tile.getClass());FIELD_CACHE.put(tile.getClass(),f);}
        return new Snapshot(true,tile,readBoolean(f.started,tile),readInt(f.timer,tile));
    }
    private static Field find(Class<?> c,String name){for(Class<?> k=c;k!=null;k=k.getSuperclass())try{Field f=k.getDeclaredField(name);f.setAccessible(true);return f;}catch(NoSuchFieldException ignored){}return null;}
    private static Boolean readBoolean(Field f,Object o){try{return f==null?null:Boolean.valueOf(f.getBoolean(o));}catch(IllegalAccessException e){return null;}}
    private static Integer readInt(Field f,Object o){try{return f==null?null:Integer.valueOf(f.getInt(o));}catch(IllegalAccessException e){return null;}}
    private static boolean implementationDescribed;
    public static synchronized void describeImplementation(World world,int x,int y,int z){if(implementationDescribed)return;implementationDescribed=true;TileEntity tile=world.getTileEntity(x,y,z);String message="Resolved HBM CSGO implementation: block="+world.getBlock(x,y,z).getClass().getName()+", tile="+(tile==null?"<none>":tile.getClass().getName())+"; reflected lifecycle fields: started, timer";if(MainRegistry.logger!=null)MainRegistry.logger.info(message);}

    public static boolean isCsgoChargeStack(ItemStack stack){if(stack==null)return false;if(!resolved)resolve();return csgoChargeItem!=null&&stack.getItem()==csgoChargeItem;}

    /** Runtime registry contract only; no HBM class is linked at compile time. */
    public static boolean isAvailable() {
        if (!resolved) resolve();
        return csgoCharge != null;
    }

    /** Returns a fresh inventory form of the exact HBM CSGO charge, or null when unavailable. */
    public static ItemStack createCsgoChargeStack() {
        if (!resolved) resolve();
        return csgoChargeItem == null ? null : new ItemStack(csgoChargeItem, 1, 0);
    }

    private static synchronized void resolve() {
        if (resolved) return;
        resolved = true;
        if (!Loader.isModLoaded(HBM_MOD_ID)) return;
        for (String name : REGISTRY_NAMES) {
            csgoCharge = GameRegistry.findBlock(HBM_MOD_ID, name);
            if (csgoCharge != null) break;
        }
        if (csgoCharge == null) return;
        csgoChargeItem = Item.getItemFromBlock(csgoCharge);
        if (csgoChargeItem != null) return;
        for (String name : REGISTRY_NAMES) {
            csgoChargeItem = GameRegistry.findItem(HBM_MOD_ID, name);
            if (csgoChargeItem != null) return;
        }
    }
}
