package com.hfr.schematic;

import java.security.MessageDigest;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import com.hfr.config.XFConfig;

/** Server-side bounded chunk assembler. Sessions must be owned by the sending player UUID. */
public final class SchematicUploadSessions {
    public static final int CHUNK_BYTES=24576, EXPIRY_TICKS=20*60;
    private final Map<UUID,Session> sessions=new HashMap<UUID,Session>();
    public synchronized UUID begin(UUID player,int bytes,int chunks,long tick){if(player==null||bytes<=0||bytes>XFConfig.builderMaxUploadBytes||chunks<=0||chunks!=(bytes+CHUNK_BYTES-1)/CHUNK_BYTES)throw new IllegalArgumentException("Invalid upload declaration");expire(tick);UUID id=UUID.randomUUID();sessions.put(id,new Session(player,bytes,chunks,tick));return id;}
    public synchronized Result accept(UUID player,UUID id,int index,byte[] data,long tick){expire(tick);Session s=sessions.get(id);if(s==null||!s.owner.equals(player))throw new IllegalArgumentException("Unknown upload session");if(index<0||index>=s.parts.length||s.received.get(index))throw new IllegalArgumentException("Invalid or duplicate chunk");int expected=index==s.parts.length-1?s.bytes-index*CHUNK_BYTES:CHUNK_BYTES;if(data==null||data.length!=expected)throw new IllegalArgumentException("Invalid chunk size");s.parts[index]=data.clone();s.received.set(index);s.last=tick;if(s.received.cardinality()!=s.parts.length)return null;byte[] all=new byte[s.bytes];for(int i=0,o=0;i<s.parts.length;o+=s.parts[i].length,i++)System.arraycopy(s.parts[i],0,all,o,s.parts[i].length);sessions.remove(id);return new Result(all,sha256(all));}
    public synchronized void cancel(UUID player,UUID id){Session s=sessions.get(id);if(s!=null&&s.owner.equals(player))sessions.remove(id);}
    public synchronized void expire(long tick){for(Iterator<Session> i=sessions.values().iterator();i.hasNext();)if(tick-i.next().last>EXPIRY_TICKS)i.remove();}
    private static String sha256(byte[] b){try{byte[] h=MessageDigest.getInstance("SHA-256").digest(b);StringBuilder s=new StringBuilder(64);for(byte v:h)s.append(String.format("%02x",v&255));return s.toString();}catch(Exception e){throw new IllegalStateException(e);}}
    private static final class Session{final UUID owner;final int bytes;final byte[][] parts;final BitSet received=new BitSet();long last;Session(UUID o,int b,int c,long t){owner=o;bytes=b;parts=new byte[c][];last=t;}}
    public static final class Result{public final byte[] bytes;public final String serverHash;private Result(byte[] b,String h){bytes=b;serverHash=h;}}
}
