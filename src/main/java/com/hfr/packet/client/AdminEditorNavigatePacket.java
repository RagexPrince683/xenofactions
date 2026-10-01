package com.hfr.packet.client;

import com.hfr.packet.PacketDispatcher;
import com.hfr.packet.effect.AdminEditorSnapshotPacket;
import com.hfr.tdm.AdminEditorSession;
import com.hfr.tdm.TDMManager;
import com.hfr.tdm.TDMServerTaskQueue;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;

/** Saves semantic panel navigation; a target change issues a fresh read-only snapshot. */
public final class AdminEditorNavigatePacket implements IMessage {
    private String token, map, team;
    private int kitIndex, page, mode, spawnIndex, spawnType, rewardIndex;
    private boolean navigate;
    public AdminEditorNavigatePacket() { }
    public AdminEditorNavigatePacket(String token, String map, String team, int kitIndex,
            int page, int mode, int spawnIndex, int spawnType, int rewardIndex, boolean navigate) {
        this.token = token; this.map = map; this.team = team; this.kitIndex = kitIndex;
        this.page = page; this.mode = mode; this.spawnIndex = spawnIndex;
        this.spawnType = spawnType; this.rewardIndex = rewardIndex; this.navigate = navigate;
    }
    public void fromBytes(ByteBuf buf) {
        token = ByteBufUtils.readUTF8String(buf); map = ByteBufUtils.readUTF8String(buf);
        team = ByteBufUtils.readUTF8String(buf); kitIndex = buf.readInt();
        page = buf.readInt(); mode = buf.readInt(); spawnIndex = buf.readInt();
        spawnType = buf.readInt(); rewardIndex = buf.readInt(); navigate = buf.readBoolean();
    }
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeUTF8String(buf, token); ByteBufUtils.writeUTF8String(buf, map);
        ByteBufUtils.writeUTF8String(buf, team); buf.writeInt(kitIndex);
        buf.writeInt(page); buf.writeInt(mode); buf.writeInt(spawnIndex);
        buf.writeInt(spawnType); buf.writeInt(rewardIndex); buf.writeBoolean(navigate);
    }
    public static final class Handler implements IMessageHandler<AdminEditorNavigatePacket, IMessage> {
        public IMessage onMessage(final AdminEditorNavigatePacket message, MessageContext context) {
            final EntityPlayerMP player = context.getServerHandler().playerEntity;
            TDMServerTaskQueue.schedule(new Runnable() { public void run() {
                if (player == null || player.worldObj == null || !player.worldObj.playerEntities.contains(player)
                        || message.token == null || message.token.length() > 64) return;
                if (!AdminEditorSession.updateContext(player, message.token, message.page, message.mode,
                        message.spawnIndex, message.spawnType, message.rewardIndex) || !message.navigate) return;
                TDMManager.Team team = TDMManager.Team.fromName(message.team);
                if (team == null || message.map == null || message.map.length() > 64
                        || message.kitIndex < 0 || message.kitIndex > 127) return;
                PacketDispatcher.wrapper.sendTo(new AdminEditorSnapshotPacket(player, message.map, team, message.kitIndex), player);
            }});
            return null;
        }
    }
}
