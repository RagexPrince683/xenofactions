package com.hfr.packet.client;

import com.hfr.packet.PacketDispatcher;
import com.hfr.packet.effect.TDMMenuDataPacket;
import com.hfr.tdm.TDMManager;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;

public class TDMMenuActionPacket implements IMessage {
    private boolean requestSwap;
    public TDMMenuActionPacket() {}
    public TDMMenuActionPacket(boolean requestSwap) { this.requestSwap = requestSwap; }
    public void fromBytes(ByteBuf buf){ requestSwap = buf.readBoolean(); }
    public void toBytes(ByteBuf buf){ buf.writeBoolean(requestSwap); }

    public static class Handler implements IMessageHandler<TDMMenuActionPacket, IMessage> {
        private static final int TEAM_CHANGE_COOLDOWN_TICKS = 120 * 20;
        private static final java.util.Map<String, Long> nextChangeTick = new java.util.HashMap<String, Long>();
        public IMessage onMessage(TDMMenuActionPacket message, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().playerEntity;
            if (!TDMManager.isEnabled(player.worldObj)) return null;
            String key = player.getCommandSenderName().toLowerCase();
            long now = player.worldObj.getTotalWorldTime();
            if (message.requestSwap) {
                Long next = nextChangeTick.get(key);
                if (next == null || now >= next.longValue()) {
                    TDMManager.Team current = TDMManager.getOrAssignPlayerTeam(player);
                    TDMManager.Team newTeam = current == TDMManager.Team.RED ? TDMManager.Team.BLUE : TDMManager.Team.RED;
                    TDMManager.setPlayerTeam(player.worldObj, player.getCommandSenderName(), newTeam);
                    nextChangeTick.put(key, Long.valueOf(now + TEAM_CHANGE_COOLDOWN_TICKS));
                    TDMManager.respawnPlayer(player, new java.util.Random());
                }
            }
            PacketDispatcher.wrapper.sendTo(new TDMMenuDataPacket(player, nextChangeTick.get(key) == null ? 0 : Math.max(0, (int)((nextChangeTick.get(key)-now+19)/20))), player);
            return null;
        }
    }
}
