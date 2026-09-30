package com.hfr.packet.client;

import com.hfr.command.CommandClowderAdmin;
import com.hfr.command.CommandTDM;
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
import net.minecraft.util.ChatComponentText;

/** GUI actions are leases, rechecked and executed only on the owning server thread. */
public class AdminEditorActionPacket implements IMessage {
    private String token, map, team, command;
    private int kitIndex;
    private boolean refresh;
    public AdminEditorActionPacket() { }
    public AdminEditorActionPacket(String token, String map, String team, int kitIndex, String command, boolean refresh) {
        this.token = token; this.map = map; this.team = team; this.kitIndex = kitIndex; this.command = command; this.refresh = refresh;
    }
    public void fromBytes(ByteBuf buf) {
        token = ByteBufUtils.readUTF8String(buf); map = ByteBufUtils.readUTF8String(buf); team = ByteBufUtils.readUTF8String(buf);
        kitIndex = buf.readInt(); command = ByteBufUtils.readUTF8String(buf); refresh = buf.readBoolean();
    }
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeUTF8String(buf,token); ByteBufUtils.writeUTF8String(buf,map); ByteBufUtils.writeUTF8String(buf,team);
        buf.writeInt(kitIndex); ByteBufUtils.writeUTF8String(buf,command); buf.writeBoolean(refresh);
    }
    public static class Handler implements IMessageHandler<AdminEditorActionPacket,IMessage> {
        public IMessage onMessage(final AdminEditorActionPacket message, MessageContext context) {
            final EntityPlayerMP player = context.getServerHandler().playerEntity;
            TDMServerTaskQueue.schedule(new Runnable() { public void run() {
                if (player == null || player.worldObj == null || !player.worldObj.playerEntities.contains(player)) return;
                TDMManager.Team pool = TDMManager.Team.fromName(message.team);
                if (message.command == null || message.command.length() > 256 || message.map == null || message.map.length() > 64
                        || message.token == null || message.token.length() > 64 || pool == null || message.kitIndex < 0 || message.kitIndex > 127) return;
                boolean tdm = message.command.startsWith("/tdm ");
                boolean xc = message.command.startsWith("/xc editor ");
                if ((!tdm && !xc) || (tdm && !player.canCommandSenderUseCommand(4,"tdm"))
                        || (xc && !player.canCommandSenderUseCommand(3,"xclowder"))) return;
                if (tdm && !(message.command.startsWith("/tdm map ") || message.command.startsWith("/tdm kit ")
                        || message.command.startsWith("/tdm overlay ") || message.command.startsWith("/tdm boundaryview ")
                        || message.command.startsWith("/tdm editor select ") || message.command.startsWith("/tdm editor point ")
                        || message.command.startsWith("/tdm editor commit") || message.command.startsWith("/tdm editor cancel"))) return;
                boolean kitAction = message.command.startsWith("/tdm kit edit ") || message.command.startsWith("/tdm kit clone ")
                        || message.command.startsWith("/tdm kit rename ") || message.command.startsWith("/tdm kit remove ")
                        || message.command.startsWith("/tdm kit cost ");
                if (!AdminEditorSession.consume(player, message.token, message.map, pool, message.kitIndex, kitAction)) {
                    player.addChatMessage(new ChatComponentText("Editor view was stale; no change was made. The panel has been refreshed."));
                    if (player.canCommandSenderUseCommand(4,"tdm"))
                        PacketDispatcher.wrapper.sendTo(new AdminEditorSnapshotPacket(player, message.map.length() == 0 ? "@global" : message.map, pool, message.kitIndex), player);
                    return;
                }
                String[] words = message.command.substring(tdm ? 5 : 4).trim().split(" +");
                if (tdm) new CommandTDM().processCommand(player, words);
                else new CommandClowderAdmin().processCommand(player, words);
                if (message.refresh && player.playerNetServerHandler != null && player.canCommandSenderUseCommand(4,"tdm")) {
                    String nextMap = message.command.startsWith("/tdm map create ") && words.length >= 3
                            && TDMManager.hasMap(player.worldObj, words[2]) ? words[2] : (message.map.length() == 0 ? "@global" : message.map);
                    PacketDispatcher.wrapper.sendTo(new AdminEditorSnapshotPacket(player, nextMap, pool, message.kitIndex), player);
                }
            }});
            return null;
        }
    }
}
