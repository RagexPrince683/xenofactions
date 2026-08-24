package com.hfr.packet.builder;
import java.util.*;import com.hfr.schematic.SchematicLibrary;import cpw.mods.fml.common.network.ByteBufUtils;import cpw.mods.fml.common.network.simpleimpl.*;import cpw.mods.fml.relauncher.*;import io.netty.buffer.ByteBuf;
/** Small server-native library index used by the client selector. */
public class BuilderLibraryPacket implements IMessage {
    public static final class Entry {public final String id,name;public final int width,height,length;private Entry(String id,String name,int w,int h,int l){this.id=id;this.name=name;width=w;height=h;length=l;}}
    private static volatile List<Entry> clientEntries=Collections.emptyList();private final List<Entry> entries=new ArrayList<Entry>();
    public BuilderLibraryPacket(){}public BuilderLibraryPacket(List<SchematicLibrary.Entry> source){for(SchematicLibrary.Entry e:source)entries.add(new Entry(e.id,e.name,e.width,e.height,e.length));}
    public void fromBytes(ByteBuf b){int n=b.readUnsignedShort();for(int i=0;i<n;i++)entries.add(new Entry(ByteBufUtils.readUTF8String(b),ByteBufUtils.readUTF8String(b),b.readInt(),b.readInt(),b.readInt()));}
    public void toBytes(ByteBuf b){b.writeShort(entries.size());for(Entry e:entries){ByteBufUtils.writeUTF8String(b,e.id);ByteBufUtils.writeUTF8String(b,e.name);b.writeInt(e.width);b.writeInt(e.height);b.writeInt(e.length);}}
    public static List<Entry> getClientEntries(){return clientEntries;}
    public static class Handler implements IMessageHandler<BuilderLibraryPacket,IMessage>{public IMessage onMessage(final BuilderLibraryPacket m,MessageContext c){receive(m);return null;}@SideOnly(Side.CLIENT)private void receive(final BuilderLibraryPacket m){net.minecraft.client.Minecraft.getMinecraft().func_152344_a(new Runnable(){public void run(){clientEntries=Collections.unmodifiableList(new ArrayList<Entry>(m.entries));}});}}
}
