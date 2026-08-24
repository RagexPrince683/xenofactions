package com.hfr.schematic;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import com.hfr.config.XFConfig;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

/** Bounded registry-name wire representation used for server-authorized previews. */
public final class SchematicPayload {
    private static final Charset UTF8=Charset.forName("UTF-8");
    private int width,height,length; private String name,format; private String[] palette; private int[] blocks; private byte[] metas;
    public SchematicPayload(Schematic s){width=s.width;height=s.height;length=s.length;name=s.name;format=s.sourceFormat;int count=s.size();blocks=new int[count];metas=new byte[count];Map<String,Integer> p=new LinkedHashMap<String,Integer>();int i=0;for(int x=0;x<width;x++)for(int y=0;y<height;y++)for(int z=0;z<length;z++){String n=s.getBlockName(x,y,z);Integer id=p.get(n);if(id==null){id=p.size();p.put(n,id);}blocks[i]=id;metas[i++]=(byte)s.getMetadata(x,y,z);}palette=p.keySet().toArray(new String[p.size()]);}
    public SchematicPayload(ByteBuf b){read(b);}
    public byte[] encode(){ByteBuf b=Unpooled.buffer();toBytes(b);byte[] out=new byte[b.readableBytes()];b.readBytes(out);return out;}
    public static Schematic decode(byte[] bytes){return new SchematicPayload(Unpooled.wrappedBuffer(bytes)).deserialize();}
    public Schematic deserialize(){Schematic s=new Schematic(width,height,length);s.name=name;s.sourceFormat=format;int i=0;for(int x=0;x<width;x++)for(int y=0;y<height;y++)for(int z=0;z<length;z++){int p=blocks[i];if(p<0||p>=palette.length||!s.setBlockName(x,y,z,palette[p],metas[i]&15))throw new IllegalArgumentException("Unknown preview palette entry");i++;}return s;}
    public void toBytes(ByteBuf b){b.writeInt(width);b.writeInt(height);b.writeInt(length);string(b,name,128);string(b,format,32);b.writeShort(palette.length);for(String p:palette)string(b,p,256);b.writeInt(blocks.length);for(int i=0;i<blocks.length;i++){b.writeShort(blocks[i]);b.writeByte(metas[i]);}}
    private void read(ByteBuf b){int start=b.readerIndex();width=b.readInt();height=b.readInt();length=b.readInt();try{SchematicLoader.validateDimensions(width,height,length);}catch(Exception e){throw new IllegalArgumentException(e.getMessage());}name=string(b,128);format=string(b,32);int pc=b.readUnsignedShort();if(pc<1||pc>4096)throw new IllegalArgumentException("Invalid palette size");palette=new String[pc];for(int i=0;i<pc;i++)palette[i]=string(b,256);int count=b.readInt();long expected=(long)width*height*length;if(count!=expected)throw new IllegalArgumentException("Invalid block count");if((long)(b.readerIndex()-start)+count*3L>XFConfig.builderMaxUploadBytes||b.readableBytes()<count*3)throw new IllegalArgumentException("Payload exceeds limit");blocks=new int[count];metas=new byte[count];for(int i=0;i<count;i++){blocks[i]=b.readUnsignedShort();metas[i]=b.readByte();if(blocks[i]>=pc)throw new IllegalArgumentException("Invalid palette index");}}
    private static void string(ByteBuf b,String s,int max){byte[] a=(s==null?"":s).getBytes(UTF8);if(a.length>max)throw new IllegalArgumentException("String too long");b.writeShort(a.length);b.writeBytes(a);}
    private static String string(ByteBuf b,int max){int n=b.readUnsignedShort();if(n>max||n>b.readableBytes())throw new IllegalArgumentException("Invalid string length");byte[] a=new byte[n];b.readBytes(a);return new String(a,UTF8);}
}
