package com.hfr.packet.effect;

import com.hfr.clowder.ClowderTerritory;
import com.hfr.clowder.ClowderTerritory.Ownership;
import com.hfr.clowder.ClowderTerritory.Zone;
import com.hfr.inventory.gui.GUIAdminEditor;
import com.hfr.tdm.AdminSelectionManager;
import com.hfr.tdm.AdminEditorSession;
import com.hfr.tdm.TDMAdminKitEdit;
import com.hfr.tdm.TDMKitManager;
import com.hfr.tdm.TDMManager;
import com.hfr.tdm.TDMMapOverlaySync;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

/** Read-only, bounded server snapshot for the admin editor. No client data is trusted for authorization. */
public class AdminEditorSnapshotPacket implements IMessage {
    private NBTTagCompound data = new NBTTagCompound();
    public AdminEditorSnapshotPacket() { }
    public AdminEditorSnapshotPacket(EntityPlayerMP player, String requestedMap, TDMManager.Team team, int kitIndex) {
        AdminEditorSession.Context context = AdminEditorSession.openContext(player, requestedMap, team, kitIndex);
        String mapName = context.map;
        team = context.team;
        kitIndex = context.kitIndex;
        data.setString("token", AdminEditorSession.open(player, mapName, team, kitIndex));
        data.setString("map", mapName);
        data.setString("team", team.name);
        data.setInteger("kitIndex", kitIndex);
        data.setInteger("page", context.page); data.setInteger("editorMode", context.mode);
        data.setInteger("spawnIndex", context.spawnIndex); data.setInteger("spawnType", context.spawnType);
        data.setInteger("rewardIndex", context.rewardIndex);
        data.setString("kitEdit", TDMAdminKitEdit.status(player));
        data.setString("selectedMap", TDMManager.getSelectedMap(player.worldObj));
        data.setString("activeMode", TDMManager.getGameMode(player.worldObj).name());
        data.setBoolean("boundaryViewOn", TDMMapOverlaySync.boundary(player));
        data.setInteger("legacySpawns", TDMManager.getSpawnCount(player.worldObj));
        data.setInteger("playerX", (int)Math.floor(player.posX)); data.setInteger("playerZ", (int)Math.floor(player.posZ)); data.setInteger("playerDim", player.dimension);
        NBTTagList maps = new NBTTagList();
        for (String name : TDMManager.getMapNames(player.worldObj)) {
            if (maps.tagCount() >= 128) break;
            NBTTagCompound tag = new NBTTagCompound(); tag.setString("name", name); maps.appendTag(tag);
        }
        data.setTag("maps", maps);
        TDMManager.TDMMap map = TDMManager.getMap(player.worldObj, mapName);
        if (map != null) {
            data.setString("mode", map.mode.name());
            data.setBoolean("votingEnabled", map.votingEnabled);
            for (TDMManager.TDMGameMode gameMode : TDMManager.TDMGameMode.values())
                data.setBoolean("enabled_" + gameMode.name(), map.supportedModes.contains(gameMode));
            data.setString("terroristTeam", map.terroristTeam.name);
            data.setBoolean("hardcore", map.hardcoreRespawns);
            data.setBoolean("mapBorder", map.mapBorderEnabled);
            data.setBoolean("economy", map.buyScoreEnabled);
            data.setBoolean("killstreaks", map.killstreaksEnabled);
            data.setInteger("scoreLimit", map.mode == TDMManager.TDMGameMode.BOMB ? map.bombScoreLimitOverride : map.scoreLimitOverride);
            data.setInteger("roundSeconds", (map.mode == TDMManager.TDMGameMode.BOMB ? map.bombRoundTicksOverride : map.roundTicksOverride) / 20);
            for (TDMManager.TDMGameMode gameMode : TDMManager.TDMGameMode.values()) {
                data.setInteger("score_" + gameMode.name(), gameMode == TDMManager.TDMGameMode.BOMB
                        ? map.bombScoreLimitOverride : map.scoreLimitOverride);
                data.setInteger("seconds_" + gameMode.name(), (gameMode == TDMManager.TDMGameMode.BOMB
                        ? map.bombRoundTicksOverride : map.roundTicksOverride) / 20);
            }
            data.setInteger("killscorereward", map.killScoreReward);
            data.setInteger("killscore", map.killBuyScoreReward);
            data.setInteger("lossscore", map.roundLossBuyScoreReward);
            data.setInteger("roundwinscore", map.roundWinBuyScoreReward);
            data.setInteger("plantscore", map.bombPlantBuyScoreReward);
            data.setInteger("defusescore", map.bombDefuseBuyScoreReward);
            area(data, "bounds", map.bounds);
            area(data, "bombA", map.bombsiteA);
            area(data, "bombB", map.bombsiteB);
            for (TDMManager.TDMGameMode gameMode : TDMManager.TDMGameMode.values()) {
                NBTTagList spawns = new NBTTagList();
                for (TDMManager.SpawnPoint spawn : map.spawns(gameMode)) {
                    if (spawns.tagCount() >= 256) break;
                    NBTTagCompound tag = new NBTTagCompound();
                    tag.setString("team", spawn.team == null ? "ffa" : spawn.team.name);
                    tag.setInteger("dim", spawn.dim); tag.setInteger("x", spawn.x); tag.setInteger("y", spawn.y); tag.setInteger("z", spawn.z);
                    tag.setBoolean("rot", spawn.hasRotation); tag.setFloat("yaw", spawn.yaw); tag.setFloat("pitch", spawn.pitch);
                    spawns.appendTag(tag);
                }
                data.setTag("spawns_" + gameMode.name(), spawns);
                TDMManager.TDMGameMode fallback = map.spawnFallbacks.get(gameMode);
                if (fallback != null) data.setString("fallback_" + gameMode.name(), fallback.name());
            }
            data.setInteger("unassignedSpawns", map.legacyUnassignedSpawns.size());
        }
        for (TDMManager.Team pool : TDMManager.Team.values()) {
            NBTTagList kits = new NBTTagList();
            String[] names = TDMKitManager.getDirectKitNames(mapName, pool);
            for (int i = 0; i < names.length && i < 128; i++) {
                NBTTagCompound tag = new NBTTagCompound(); tag.setString("name", names[i]);
                tag.setInteger("cost", TDMKitManager.getDirectKitCost(mapName, pool, i));
                for (TDMManager.TDMGameMode mode : TDMManager.TDMGameMode.values())
                    tag.setBoolean("disabled_" + mode.name(), TDMKitManager.isDirectKitDisabled(mapName, pool, i, mode));
                kits.appendTag(tag);
            }
            data.setTag(pool.name + "Kits", kits);
        }
        ItemStack[] preview = TDMKitManager.getDirectKitPreview(mapName, team, kitIndex);
        if (preview != null) {
            NBTTagList slots = new NBTTagList();
            for (int i = 0; i < preview.length; i++) if (preview[i] != null) {
                NBTTagCompound tag = new NBTTagCompound(); tag.setByte("slot", (byte)i);
                NBTTagCompound item = new NBTTagCompound(); preview[i].writeToNBT(item); tag.setTag("item", item); slots.appendTag(tag);
            }
            data.setTag("preview", slots);
        }
        AdminSelectionManager.Selection selection = AdminSelectionManager.get(player);
        if (selection != null) {
            NBTTagCompound tag = new NBTTagCompound();
            tag.setString("type", selection.type.name()); tag.setString("map", selection.map); tag.setInteger("dim", selection.dimension);
            tag.setBoolean("hasA", selection.hasA); tag.setBoolean("hasB", selection.hasB);
            tag.setInteger("ax", selection.ax); tag.setInteger("ay", selection.ay); tag.setInteger("az", selection.az);
            tag.setInteger("bx", selection.bx); tag.setInteger("by", selection.by); tag.setInteger("bz", selection.bz);
            data.setTag("selection", tag);
        }
        // Compact local zoning preview. This reads Clowder chunk ownership; maps remain separate.
        NBTTagList zones = new NBTTagList();
        ClowderTerritory.CoordPair center = ClowderTerritory.getCoordPair(player.dimension,
                (int)Math.floor(player.posX), (int)Math.floor(player.posZ));
        int centerX = center.x, centerZ = center.z;
        data.setInteger("zoneCX", centerX); data.setInteger("zoneCZ", centerZ);
        for (int dx = -8; dx <= 8; dx++) for (int dz = -8; dz <= 8; dz++) {
            Ownership owner = ClowderTerritory.getOwner(player.dimension, centerX + dx, centerZ + dz);
            if (owner == null || (owner.zone != Zone.SAFEZONE && owner.zone != Zone.WARZONE)) continue;
            NBTTagCompound tag = new NBTTagCompound(); tag.setByte("x", (byte)dx); tag.setByte("z", (byte)dz);
            tag.setByte("type", (byte)(owner.zone == Zone.SAFEZONE ? 1 : 2)); zones.appendTag(tag);
        }
        data.setTag("zones", zones);
    }
    private static void area(NBTTagCompound parent, String key, TDMManager.Bombsite area) {
        NBTTagCompound tag = new NBTTagCompound(); tag.setBoolean("a", area.hasPos1); tag.setBoolean("b", area.hasPos2);
        tag.setInteger("dim", area.dimension); tag.setInteger("x1", area.x1); tag.setInteger("y1", area.y1); tag.setInteger("z1", area.z1);
        tag.setInteger("x2", area.x2); tag.setInteger("y2", area.y2); tag.setInteger("z2", area.z2); parent.setTag(key, tag);
    }
    public void fromBytes(ByteBuf buf) { data = ByteBufUtils.readTag(buf); if (data == null) data = new NBTTagCompound(); }
    public void toBytes(ByteBuf buf) { ByteBufUtils.writeTag(buf, data); }
    public static class Handler implements IMessageHandler<AdminEditorSnapshotPacket, IMessage> {
        @Override @SideOnly(Side.CLIENT) public IMessage onMessage(final AdminEditorSnapshotPacket message, MessageContext context) {
            Minecraft.getMinecraft().func_152344_a(new Runnable() { public void run() {
                Minecraft.getMinecraft().displayGuiScreen(new GUIAdminEditor(message.data));
            }});
            return null;
        }
    }
}
