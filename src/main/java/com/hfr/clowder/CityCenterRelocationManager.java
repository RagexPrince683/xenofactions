package com.hfr.clowder;

import static net.minecraftforge.common.util.ForgeDirection.UP;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.hfr.blocks.ModBlocks;
import com.hfr.clowder.ClowderTerritory.CoordPair;
import com.hfr.clowder.ClowderTerritory.TerritoryMeta;
import com.hfr.clowder.ClowderTerritory.Zone;
import com.hfr.config.XFConfig;
import com.hfr.data.ClowderData;
import com.hfr.tileentity.clowder.TileEntityFlag;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;

/** The single server-side authority for the two-phase City Center move transaction. */
public final class CityCenterRelocationManager {
    public static final String TOKEN = "xenoCityRelocation";
    private static final ThreadLocal<String> GUARDED_REMOVAL = new ThreadLocal<String>();
    private static final ConcurrentLinkedQueue<Runnable> SERVER_TASKS = new ConcurrentLinkedQueue<Runnable>();

    private CityCenterRelocationManager() { }

    public static void schedule(Runnable task) { if(task != null) SERVER_TASKS.add(task); }
    public static void runScheduledTasks() { Runnable task; while((task = SERVER_TASKS.poll()) != null) task.run(); }

    public static double horizontalDistance(int x1, int z1, int x2, int z2) {
        long dx = (long)x2 - x1, dz = (long)z2 - z1;
        return Math.sqrt(dx * dx + dz * dz);
    }

    public static float calculateCost(double distance) {
        if(distance <= XFConfig.cityRelocationFreeDistanceBlocks) return 0F;
        return XFConfig.cityRelocationBasePrestigeCost
            + (float)Math.ceil(distance - XFConfig.cityRelocationFreeDistanceBlocks) * XFConfig.cityRelocationPrestigePerExtraBlock;
    }

    public static boolean hasPending(Clowder faction) {
        if(faction == null || faction.relocationId.isEmpty()) return false;
        if(faction.relocationExpires <= System.currentTimeMillis() || !faction.activeWars.isEmpty()) clear(faction, DimensionManager.getWorld(faction.relocationDim));
        return !faction.relocationId.isEmpty();
    }

    public static void clear(Clowder faction, World world) {
        if(faction == null) return;
        faction.relocationId = faction.relocationCityId = "";
        faction.relocationStarted = faction.relocationExpires = 0L;
        if(world != null) faction.save(world);
    }

    public static String validateStart(EntityPlayer player, int dim, int x, int y, int z) {
        if(!XFConfig.cityRelocationEnabled) return "City Center relocation is disabled.";
        if(player == null || player instanceof net.minecraftforge.common.util.FakePlayer) return "Only a real faction leader may relocate a City Center.";
        Clowder faction = Clowder.getClowderFromPlayer(player);
        if(faction == null || !faction.isOwner(player)) return "Only the faction leader may relocate a City Center.";
        if(!faction.activeWars.isEmpty()) { clear(faction, player.worldObj); return "City Centers cannot be moved during an active war."; }
        if(hasPending(faction)) return "Your faction already has a pending City Center move.";
        if(player.worldObj.provider.dimensionId != dim || player.worldObj.getBlock(x, y, z) != ModBlocks.clowder_flag) return "The source City Center no longer exists.";
        TileEntity te = player.worldObj.getTileEntity(x, y, z);
        if(!(te instanceof TileEntityFlag)) return "The source City Center is invalid.";
        TileEntityFlag flag = (TileEntityFlag)te;
        TerritoryMeta meta = ClowderTerritory.getMetaFromIntCoords(player.worldObj, x, z);
        if(!sameFaction(flag.owner, faction) || !flag.isClaimed || flag.height < 1F || meta == null || meta.owner == null
            || !sameFaction(meta.owner.owner, faction) || !flag.getCityId().equals(meta.cityId)
            || meta.dimensionId != dim || meta.flagX != x || meta.flagY != y || meta.flagZ != z)
            return "This is not a claimed City Center owned by your faction.";
        return cooldownError(faction, flag.getCityId(), System.currentTimeMillis());
    }

    public static boolean start(EntityPlayerMP player, int dim, int x, int y, int z) {
        String error = validateStart(player, dim, x, y, z);
        if(error != null) { message(player, error); return false; }
        Clowder faction = Clowder.getClowderFromPlayer(player);
        TileEntityFlag flag = (TileEntityFlag)player.worldObj.getTileEntity(x, y, z);
        long now = System.currentTimeMillis();
        faction.relocationId = UUID.randomUUID().toString(); faction.relocationCityId = flag.getCityId();
        faction.relocationDim = dim; faction.relocationX = x; faction.relocationY = y; faction.relocationZ = z;
        faction.relocationStarted = now; faction.relocationExpires = now + XFConfig.cityRelocationPendingMinutes * 60000L;
        faction.save(player.worldObj);
        issueToken(player, faction);
        message(player, "Move started. The old City Center remains active; place the relocation token or use /c city cancelmove.");
        return true;
    }

    public static boolean issueToken(EntityPlayer player, Clowder faction) {
        if(!hasPending(faction)) return false;
        ItemStack stack = new ItemStack(ModBlocks.clowder_flag);
        stack.stackTagCompound = new NBTTagCompound();
        stack.stackTagCompound.setBoolean(TOKEN, true);
        stack.stackTagCompound.setString("relocationId", faction.relocationId);
        stack.stackTagCompound.setString("factionUuid", faction.uuid);
        stack.stackTagCompound.setString("cityId", faction.relocationCityId);
        stack.setStackDisplayName("City Center Relocation Token");
        if(!player.inventory.addItemStackToInventory(stack)) player.dropPlayerItemWithRandomChoice(stack, false);
        player.inventoryContainer.detectAndSendChanges();
        return true;
    }

    public static boolean isToken(ItemStack stack) { return stack != null && stack.hasTagCompound() && stack.stackTagCompound.getBoolean(TOKEN); }

    public static boolean relocate(EntityPlayer player, ItemStack token, World world, int x, int y, int z) {
        if(world.isRemote) return true;
        if(!(player instanceof EntityPlayerMP) || player instanceof net.minecraftforge.common.util.FakePlayer) return fail(player, "Relocation tokens may only be placed by a real player.");
        Clowder faction = Clowder.getClowderFromPlayer(player);
        if(faction == null || !faction.isOwner(player) || !hasPending(faction)) return fail(player, "This relocation token is no longer authorized.");
        if(!faction.activeWars.isEmpty()) { clear(faction, world); return fail(player, "The move was canceled because your faction entered a war."); }
        NBTTagCompound tag = token.stackTagCompound;
        if(!faction.relocationId.equals(tag.getString("relocationId")) || !faction.uuid.equals(tag.getString("factionUuid")) || !faction.relocationCityId.equals(tag.getString("cityId"))) return fail(player, "This relocation token does not match the pending move.");
        String error = validateDestination(faction, world, x, y, z);
        if(error != null) return fail(player, error);
        double distance = horizontalDistance(faction.relocationX, faction.relocationZ, x, z);
        float cost = calculateCost(distance);
        if(faction.getPrestige() < cost) return fail(player, "This move costs " + cost + " prestige; your faction cannot afford it.");
        return commit(player, token, faction, world, x, y, z, cost);
    }

    private static String validateDestination(Clowder faction, World world, int x, int y, int z) {
        if(world.provider.dimensionId != faction.relocationDim) return "The new City Center must be in the source dimension.";
        if(horizontalDistance(faction.relocationX, faction.relocationZ, x, z) > XFConfig.cityRelocationMaxDistanceBlocks) return "That location exceeds the maximum relocation distance.";
        if(!XFConfig.canClaimInDimension(world.provider.dimensionId)) return "City claims are disabled in this dimension.";
        if(y < 45 || y > 200) return "City Centers must be between Y 45 and Y 200.";
		if(!world.getBlock(x, y, z).isReplaceable(world, x, y, z)) return "The destination block is not replaceable.";
        for(int dx=-2; dx<=2; dx++) for(int dz=-2; dz<=2; dz++)
            if(!world.canBlockSeeTheSky(x+dx, y+1, z+dz) || !world.getBlock(x+dx,y-1,z+dz).isSideSolid(world,x+dx,y-1,z+dz,UP)) return "The City Center requires a sky-accessible 5 by 5 foundation.";
        TileEntity oldTe = world.getTileEntity(faction.relocationX, faction.relocationY, faction.relocationZ);
        if(world.getBlock(faction.relocationX, faction.relocationY, faction.relocationZ) != ModBlocks.clowder_flag || !(oldTe instanceof TileEntityFlag) || !sameFaction(((TileEntityFlag)oldTe).owner, faction) || !faction.relocationCityId.equals(((TileEntityFlag)oldTe).getCityId())) return "The source City Center changed; the move cannot continue.";
        int radius = ((TileEntityFlag)oldTe).getRadius();
        for(int dx=-radius; dx<=radius; dx++) for(int dz=-radius; dz<=radius; dz++) if(Math.sqrt(dx*dx+dz*dz)<radius) {
            TerritoryMeta meta = ClowderTerritory.getMetaFromCoords(ClowderTerritory.getCoordPair(world, x+dx*16, z+dz*16));
            if(meta != null && !faction.relocationCityId.equals(meta.cityId)) return meta.owner != null && (meta.owner.zone == Zone.SAFEZONE || meta.owner.zone == Zone.WARZONE) ? "The new radius intersects a protected zone." : "The new radius intersects another city or faction claim.";
        }
        CoordPair center = ClowderTerritory.getCoordPair(world, x, z);
        String spacing = ClowderTerritory.getCityPlacementErrorIgnoring(world.provider.dimensionId, center.x, center.z, faction.relocationCityId);
        return spacing;
    }

    private static boolean commit(EntityPlayer player, ItemStack token, Clowder faction, World world, int x, int y, int z, float cost) {
        TileEntityFlag oldFlag = (TileEntityFlag)world.getTileEntity(faction.relocationX, faction.relocationY, faction.relocationZ);
        NBTTagCompound saved = new NBTTagCompound(); oldFlag.writeToNBT(saved);
        Map<CoordPair, TerritoryMeta> oldTerritory = new HashMap<CoordPair, TerritoryMeta>(ClowderTerritory.territories);
        int oldHomeX=faction.homeX, oldHomeY=faction.homeY, oldHomeZ=faction.homeZ, oldHomeDim=faction.homeDim; boolean oldHomeSet=faction.homeSet;
        boolean charged = false;
        try {
            if(!world.setBlock(x, y, z, ModBlocks.clowder_flag, world.getBlockMetadata(faction.relocationX, faction.relocationY, faction.relocationZ), 3)) throw new IllegalStateException("destination could not be placed");
            TileEntity te = world.getTileEntity(x,y,z); if(!(te instanceof TileEntityFlag)) throw new IllegalStateException("destination tile missing");
            saved.setInteger("x", x); saved.setInteger("y", y); saved.setInteger("z", z);
            TileEntityFlag newFlag=(TileEntityFlag)te; newFlag.readFromNBT(saved); newFlag.setCityId(faction.relocationCityId); newFlag.markDirty();
            faction.addPrestige(-cost, world); charged = true;
            ClowderTerritory.removeClaimsForCityId(world, faction.relocationCityId, false);
            newFlag.generateClaim();
            TerritoryMeta centerMeta = ClowderTerritory.getMetaFromIntCoords(world, x, z);
            if(centerMeta == null || centerMeta.owner == null || !sameFaction(centerMeta.owner.owner, faction)
                || !faction.relocationCityId.equals(centerMeta.cityId) || centerMeta.dimensionId != world.provider.dimensionId
                || centerMeta.flagX != x || centerMeta.flagY != y || centerMeta.flagZ != z)
                throw new IllegalStateException("destination claim metadata was not generated");
            newFlag.markDirty();
            GUARDED_REMOVAL.set(key(world,faction.relocationX,faction.relocationY,faction.relocationZ));
            try { world.setBlockToAir(faction.relocationX,faction.relocationY,faction.relocationZ); } finally { GUARDED_REMOVAL.remove(); }
            moveHomeIfNeeded(faction, oldTerritory, oldHomeX, oldHomeY, oldHomeZ, oldHomeDim, x, y, z, world);
            recordSuccess(faction, faction.relocationCityId, System.currentTimeMillis());
            clear(faction, world); token.stackSize--;
            ClowderData.getData(world).markDirty(); com.hfr.dynmap.XFDynmapIntegration.markDirty();
            message(player, "City Center relocated successfully for " + cost + " prestige.");
            return true;
        } catch(Throwable failure) {
            if(charged) faction.addPrestige(cost, world);
            if(world.getBlock(x,y,z)==ModBlocks.clowder_flag) { GUARDED_REMOVAL.set(key(world,x,y,z)); try { world.setBlockToAir(x,y,z); } finally { GUARDED_REMOVAL.remove(); } }
            ClowderTerritory.territories.clear(); ClowderTerritory.territories.putAll(oldTerritory);
            if(world.getBlock(faction.relocationX,faction.relocationY,faction.relocationZ)!=ModBlocks.clowder_flag) world.setBlock(faction.relocationX,faction.relocationY,faction.relocationZ,ModBlocks.clowder_flag);
            TileEntity restored=world.getTileEntity(faction.relocationX,faction.relocationY,faction.relocationZ); if(restored instanceof TileEntityFlag) ((TileEntityFlag)restored).readFromNBT(saved);
            faction.homeX=oldHomeX; faction.homeY=oldHomeY; faction.homeZ=oldHomeZ; faction.homeDim=oldHomeDim; faction.homeSet=oldHomeSet;
            faction.save(world); ClowderData.getData(world).markDirty();
            return fail(player, "The relocation failed and the original city was restored.");
        }
    }

    private static void moveHomeIfNeeded(Clowder f, Map<CoordPair,TerritoryMeta> oldClaims, int hx,int hy,int hz,int hd,int nx,int ny,int nz,World world) {
        TerritoryMeta oldHome=oldClaims.get(ClowderTerritory.getCoordPair(hd,hx,hz));
        if(!f.homeSet || oldHome==null || !f.relocationCityId.equals(oldHome.cityId)) return;
        int tx=hx+(nx-f.relocationX), tz=hz+(nz-f.relocationZ);
        TerritoryMeta translated=ClowderTerritory.getMetaFromIntCoords(world,tx,tz);
        f.homeX=tx; f.homeY=hy; f.homeZ=tz; f.homeDim=world.provider.dimensionId;
        if(translated==null || !f.relocationCityId.equals(translated.cityId)) { f.homeX=nx; f.homeY=ny+1; f.homeZ=nz; }
        f.save(world); f.notifyAll(world,new ChatComponentText(EnumChatFormatting.YELLOW+"Faction home moved with the relocated City Center."));
    }

    public static String cooldownError(Clowder faction, String cityId, long now) {
        List<Long> history=faction.cityRelocationHistory.get(cityId); if(history==null) return null;
        long window=XFConfig.cityRelocationWindowHours*3600000L;
        for(Iterator<Long> it=history.iterator();it.hasNext();) if(it.next().longValue()<=now-window) it.remove();
        if(history.size()<XFConfig.cityRelocationMoveLimit) return null;
        long wait=history.get(0).longValue()+window-now, hours=wait/3600000L, minutes=(wait%3600000L+59999L)/60000L;
        return "This city has reached its move limit. Try again in "+hours+"h "+minutes+"m.";
    }
    private static void recordSuccess(Clowder f,String id,long now) { cooldownError(f,id,now); List<Long> h=f.cityRelocationHistory.get(id); if(h==null){h=new ArrayList<Long>();f.cityRelocationHistory.put(id,h);} h.add(Long.valueOf(now)); }
    public static boolean isGuardedRemoval(World w,int x,int y,int z){return key(w,x,y,z).equals(GUARDED_REMOVAL.get());}
    private static String key(World w,int x,int y,int z){return System.identityHashCode(w)+":"+w.provider.dimensionId+":"+x+":"+y+":"+z;}
    private static boolean fail(EntityPlayer p,String s){message(p,s);return false;}
    private static boolean sameFaction(Clowder first, Clowder second) {
        return first == second || first != null && second != null && first.uuid != null && first.uuid.equals(second.uuid);
    }
    private static void message(EntityPlayer p,String s){if(p!=null)p.addChatMessage(new ChatComponentText(EnumChatFormatting.RED+s));}
}
