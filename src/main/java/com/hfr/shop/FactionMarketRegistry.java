package com.hfr.shop;

import com.hfr.blocks.ModBlocks;
import com.hfr.blocks.machine.MachineMarket.TileEntityMarket;
import com.hfr.clowder.Clowder;
import com.hfr.data.ClowderData;
import java.util.UUID;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;

/** Targeted, event-driven terminal validation. No chunk or faction scans. */
public final class FactionMarketRegistry {
    private FactionMarketRegistry() { }
    public static final class Registration {
        public int dimension, x, y, z;
        public String token;
        public Registration(World world, int x, int y, int z, String token) {
            dimension = world.provider.dimensionId; this.x = x; this.y = y; this.z = z; this.token = token;
        }
        private Registration() { }
        public boolean matches(TileEntityMarket tile) {
            return tile != null && tile.getWorldObj().provider.dimensionId == dimension
                && tile.xCoord == x && tile.yCoord == y && tile.zCoord == z && token.equals(tile.terminalToken);
        }
        public NBTTagCompound write() {
            NBTTagCompound tag = new NBTTagCompound(); tag.setInteger("version", 1);
            tag.setInteger("dimension", dimension); tag.setInteger("x", x); tag.setInteger("y", y); tag.setInteger("z", z);
            tag.setString("token", token); return tag;
        }
        public static Registration read(NBTTagCompound tag) {
            try {
                UUID.fromString(tag.getString("factionId")); UUID.fromString(tag.getString("token"));
                if (tag.getInteger("version") != 1 || !tag.hasKey("dimension", 3) || !tag.hasKey("x", 3)
                    || !tag.hasKey("y", 3) || !tag.hasKey("z", 3) || tag.getInteger("y") < 0 || tag.getInteger("y") > 255) return null;
                Registration r = new Registration(); r.dimension = tag.getInteger("dimension");
                r.x = tag.getInteger("x"); r.y = tag.getInteger("y"); r.z = tag.getInteger("z"); r.token = tag.getString("token");
                if (Math.abs((long)r.x) > 30000000L || Math.abs((long)r.z) > 30000000L) return null;
                return r;
            } catch (IllegalArgumentException e) { return null; }
        }
    }
    public static String placementError(EntityPlayer player, World world, int x, int y, int z) {
        if (world.isRemote) return null;
        ClowderData data = ClowderData.getData(world);
        Clowder faction = Clowder.getClowderFromPlayer(player);
        if (faction == null || faction.getPermLevel(player) < 2) return "Only faction Officers and leaders may place market terminals.";
        if (!faction.isInCapital(world, x, z)) return "Place the market terminal inside your faction's designated capital.";
        Registration registered = data.getMarketTerminal(faction.uuid);
        if (registered == null) return null;
        // Check claims before loading: capital changes must release even an unloaded old terminal.
        if (!faction.isInCapital(registered.dimension, registered.x, registered.z)) {
            data.clearMarketTerminal(faction.uuid); return null;
        }
        World registeredWorld = DimensionManager.getWorld(registered.dimension);
        // Unavailable dimensions are unknown, not missing; do not permit duplicates.
        if (registeredWorld == null) return "The registered terminal's dimension is unavailable. Try again when it is loaded.";
        // Only this registered chunk is loaded to distinguish an unloaded block from a stale coordinate.
        registeredWorld.getChunkFromBlockCoords(registered.x, registered.z);
        TileEntity raw = registeredWorld.getTileEntity(registered.x, registered.y, registered.z);
        if (registeredWorld.getBlock(registered.x, registered.y, registered.z) == ModBlocks.faction_market
            && raw instanceof TileEntityMarket && registered.matches((TileEntityMarket)raw)
            && faction.uuid.equals(((TileEntityMarket)raw).factionId))
            return "Your faction already has an active market terminal.";
        data.clearMarketTerminal(faction.uuid); return null;
    }
    public static boolean register(EntityPlayer player, World world, int x, int y, int z) {
        if (placementError(player, world, x, y, z) != null) return false;
        TileEntity raw = world.getTileEntity(x, y, z);
        if (world.getBlock(x, y, z) != ModBlocks.faction_market || !(raw instanceof TileEntityMarket)) return false;
        Clowder faction = Clowder.getClowderFromPlayer(player); TileEntityMarket tile = (TileEntityMarket)raw;
        tile.factionId = faction.uuid; tile.terminalToken = UUID.randomUUID().toString(); tile.markDirty();
        ClowderData.getData(world).setMarketTerminal(faction.uuid, new Registration(world, x, y, z, tile.terminalToken));
        return true;
    }
    public static boolean canUse(EntityPlayer player, TileEntityMarket tile) {
        if (tile == null || tile.getWorldObj().isRemote || tile.getWorldObj().getBlock(tile.xCoord, tile.yCoord, tile.zCoord) != ModBlocks.faction_market) return false;
        ClowderData data = ClowderData.getData(tile.getWorldObj());
        Clowder faction = Clowder.getClowderFromPlayer(player);
        if (faction == null || !faction.uuid.equals(tile.factionId)) return false;
        Registration r = data.getMarketTerminal(faction.uuid);
        if (r == null || !r.matches(tile)) return false;
        if (!faction.isInCapital(tile.getWorldObj(), tile.xCoord, tile.zCoord)) {
            data.clearMarketTerminal(faction.uuid); return false;
        }
        return true;
    }
    public static void broken(World world, TileEntityMarket tile) {
        if (world.isRemote || tile == null || tile.factionId.isEmpty()) return;
        ClowderData data = ClowderData.getData(world); Registration r = data.getMarketTerminal(tile.factionId);
        if (r != null && r.matches(tile)) data.clearMarketTerminal(tile.factionId);
    }
}
