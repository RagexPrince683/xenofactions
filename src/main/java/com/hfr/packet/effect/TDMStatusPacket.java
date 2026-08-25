package com.hfr.packet.effect;

import com.hfr.main.EventHandlerClient;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;

public class TDMStatusPacket implements IMessage {

    private boolean enabled;
    private boolean voting;
    private int roundSeconds;
    private int voteSeconds;
    private int redScore;
    private int blueScore;
    private String mapName;
    private String mode="DEATHMATCH", bombState="DISABLED", terroristTeam="red", plantedSite="";
    private int redBombWins, blueBombWins, bombSeconds, buyScore, redPlayerCount, bluePlayerCount; private boolean economy;

    public TDMStatusPacket() { }

    public TDMStatusPacket(boolean enabled, boolean voting, int roundSeconds, int voteSeconds, int redScore, int blueScore, String mapName) {
        this(enabled,voting,roundSeconds,voteSeconds,redScore,blueScore,mapName,"DEATHMATCH","DISABLED",0,0,"red",0,"");
    }
    public TDMStatusPacket(boolean enabled,boolean voting,int roundSeconds,int voteSeconds,int redScore,int blueScore,String mapName,String mode,String bombState,int redBombWins,int blueBombWins,String terroristTeam,int bombSeconds,String plantedSite) {
        this.enabled = enabled;
        this.voting = voting;
        this.roundSeconds = roundSeconds;
        this.voteSeconds = voteSeconds;
        this.redScore = redScore;
        this.blueScore = blueScore;
        this.mapName = mapName == null ? "" : mapName;
        this.mode=mode;this.bombState=bombState;this.redBombWins=redBombWins;this.blueBombWins=blueBombWins;this.terroristTeam=terroristTeam;this.bombSeconds=bombSeconds;this.plantedSite=plantedSite==null?"":plantedSite;
    }

    public TDMStatusPacket(boolean enabled,boolean voting,int roundSeconds,int voteSeconds,int redScore,int blueScore,String mapName,String mode,String bombState,int redBombWins,int blueBombWins,String terroristTeam,int bombSeconds,String plantedSite,boolean economy,int buyScore){this(enabled,voting,roundSeconds,voteSeconds,redScore,blueScore,mapName,mode,bombState,redBombWins,blueBombWins,terroristTeam,bombSeconds,plantedSite);this.economy=economy;this.buyScore=Math.max(0,buyScore);}
    public TDMStatusPacket(boolean enabled,boolean voting,int roundSeconds,int voteSeconds,int redScore,int blueScore,String mapName,String mode,String bombState,int redBombWins,int blueBombWins,String terroristTeam,int bombSeconds,String plantedSite,boolean economy,int buyScore,int redPlayerCount,int bluePlayerCount){this(enabled,voting,roundSeconds,voteSeconds,redScore,blueScore,mapName,mode,bombState,redBombWins,blueBombWins,terroristTeam,bombSeconds,plantedSite,economy,buyScore);this.redPlayerCount=Math.max(0,redPlayerCount);this.bluePlayerCount=Math.max(0,bluePlayerCount);}

    @Override
    public void fromBytes(ByteBuf buf) {
        enabled = buf.readBoolean();
        voting = buf.readBoolean();
        roundSeconds = buf.readInt();
        voteSeconds = buf.readInt();
        redScore = buf.readInt();
        blueScore = buf.readInt();
        mapName = ByteBufUtils.readUTF8String(buf);
        mode=ByteBufUtils.readUTF8String(buf);bombState=ByteBufUtils.readUTF8String(buf);redBombWins=buf.readInt();blueBombWins=buf.readInt();terroristTeam=ByteBufUtils.readUTF8String(buf);bombSeconds=buf.readInt();plantedSite=ByteBufUtils.readUTF8String(buf);economy=buf.readBoolean();buyScore=buf.readInt();redPlayerCount=buf.readInt();bluePlayerCount=buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeBoolean(enabled);
        buf.writeBoolean(voting);
        buf.writeInt(roundSeconds);
        buf.writeInt(voteSeconds);
        buf.writeInt(redScore);
        buf.writeInt(blueScore);
        ByteBufUtils.writeUTF8String(buf, mapName);
        ByteBufUtils.writeUTF8String(buf,mode);ByteBufUtils.writeUTF8String(buf,bombState);buf.writeInt(redBombWins);buf.writeInt(blueBombWins);ByteBufUtils.writeUTF8String(buf,terroristTeam);buf.writeInt(bombSeconds);ByteBufUtils.writeUTF8String(buf,plantedSite);buf.writeBoolean(economy);buf.writeInt(buyScore);buf.writeInt(redPlayerCount);buf.writeInt(bluePlayerCount);
    }

    public static class Handler implements IMessageHandler<TDMStatusPacket, IMessage> {

        @Override
        @SideOnly(Side.CLIENT)
        public IMessage onMessage(TDMStatusPacket message, MessageContext ctx) {
            EventHandlerClient.updateTDMStatus(message.enabled,message.voting,message.roundSeconds,message.voteSeconds,message.redScore,message.blueScore,message.mapName,message.mode,message.bombState,message.redBombWins,message.blueBombWins,message.terroristTeam,message.bombSeconds,message.plantedSite,message.economy,message.buyScore,message.redPlayerCount,message.bluePlayerCount);
            return null;
        }
    }
}
