package com.hfr.packet.effect;

import com.hfr.main.EventHandlerClient;
import com.hfr.tdm.TDMManager;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;

import java.util.ArrayList;
import java.util.List;

public class TDMMenuDataPacket implements IMessage {
    public String currentTeam;
    public int cooldownSeconds;
    public String[] friendlyLines;
    public String[] enemyLines;
    public TDMMenuDataPacket() {}
    public TDMMenuDataPacket(EntityPlayerMP player, int cooldownSeconds) {
        this.cooldownSeconds = cooldownSeconds;
        this.currentTeam = TDMManager.getOrAssignPlayerTeam(player).name;
        List<String> friend = new ArrayList<String>();
        List<String> enemy = new ArrayList<String>();
        TDMManager.Team self = TDMManager.getOrAssignPlayerTeam(player);
        for (Object obj : MinecraftServer.getServer().getConfigurationManager().playerEntityList) {
            EntityPlayerMP p = (EntityPlayerMP)obj;
            TDMManager.Team team = TDMManager.getPlayerTeam(player.worldObj, p.getCommandSenderName());
            if (team == null) continue;
            int k = TDMManager.getKills(player.worldObj, p.getCommandSenderName());
            int d = TDMManager.getDeaths(player.worldObj, p.getCommandSenderName());
            float kdr = d <= 0 ? (float)k : ((float)k / (float)d);
            String line = p.getCommandSenderName() + " | " + k + "/" + d + " | " + String.format(java.util.Locale.US, "%.2f", kdr);
            if (team == self) {
                friend.add(line);
            } else {
                enemy.add(line);
            }
        }
        this.friendlyLines = friend.toArray(new String[0]);
        this.enemyLines = enemy.toArray(new String[0]);
    }
    public void fromBytes(ByteBuf buf){
        currentTeam = ByteBufUtils.readUTF8String(buf);
        cooldownSeconds = buf.readInt();
        int friendlyCount = buf.readInt();
        friendlyLines = new String[friendlyCount];
        for(int i = 0; i < friendlyCount; i++) friendlyLines[i] = ByteBufUtils.readUTF8String(buf);
        int enemyCount = buf.readInt();
        enemyLines = new String[enemyCount];
        for(int i = 0; i < enemyCount; i++) enemyLines[i] = ByteBufUtils.readUTF8String(buf);
    }
    public void toBytes(ByteBuf buf){
        ByteBufUtils.writeUTF8String(buf, currentTeam);
        buf.writeInt(cooldownSeconds);
        buf.writeInt(friendlyLines.length);
        for(String s : friendlyLines) ByteBufUtils.writeUTF8String(buf, s);
        buf.writeInt(enemyLines.length);
        for(String s : enemyLines) ByteBufUtils.writeUTF8String(buf, s);
    }

    public static class Handler implements IMessageHandler<TDMMenuDataPacket, IMessage> {
        @Override
        @SideOnly(Side.CLIENT)
        public IMessage onMessage(TDMMenuDataPacket message, MessageContext ctx) {
            EventHandlerClient.openTDMMenu(message.currentTeam, message.cooldownSeconds, message.friendlyLines, message.enemyLines);
            return null;
        }
    }
}
