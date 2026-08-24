package com.hfr.packet.builder;
import com.hfr.builder.BuilderDepotService;import com.hfr.schematic.SchematicLibrary;import com.hfr.tileentity.machine.TileEntityMachineBuilder;import cpw.mods.fml.common.network.simpleimpl.*;import io.netty.buffer.ByteBuf;import net.minecraft.entity.player.EntityPlayerMP;import net.minecraft.tileentity.TileEntity;
/** Requests metadata only; force is used by the selector's manual Refresh button. */
public class BuilderLibraryRequestPacket implements IMessage {
    public int dimension,x,y,z;public boolean force;
    public BuilderLibraryRequestPacket(){}
    public BuilderLibraryRequestPacket(int d,int x,int y,int z,boolean force){dimension=d;this.x=x;this.y=y;this.z=z;this.force=force;}
    public void fromBytes(ByteBuf b){dimension=b.readInt();x=b.readInt();y=b.readInt();z=b.readInt();force=b.readBoolean();}
    public void toBytes(ByteBuf b){b.writeInt(dimension);b.writeInt(x);b.writeInt(y);b.writeInt(z);b.writeBoolean(force);}
    public static class Handler implements IMessageHandler<BuilderLibraryRequestPacket,IMessage>{public IMessage onMessage(BuilderLibraryRequestPacket m,MessageContext c){EntityPlayerMP p=c.getServerHandler().playerEntity;if(p==null||p.dimension!=m.dimension||p.getDistanceSq(m.x+.5,m.y+.5,m.z+.5)>64)return null;TileEntity te=p.worldObj.getTileEntity(m.x,m.y,m.z);if(!(te instanceof TileEntityMachineBuilder)||!BuilderDepotService.mayView(p,(TileEntityMachineBuilder)te))return null;SchematicLibrary.get().refresh(m.force);return new BuilderLibraryPacket(SchematicLibrary.get().list());}}
}
