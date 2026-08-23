package com.hfr.wallart;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.hfr.main.MainRegistry;
import com.hfr.tileentity.TileEntityWallImage;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.WorldServer;

/** Bounded worker pipeline; every world/player/TE operation happens in tick(). */
public final class WallArtService {
    private static ThreadPoolExecutor workers=newPool();
    private static final ConcurrentLinkedQueue<Runnable> RESULTS=new ConcurrentLinkedQueue<Runnable>();
    private static final Map<UUID,Long> RATE=new HashMap<UUID,Long>(); private static final Map<UUID,Boolean> PENDING=new HashMap<UUID,Boolean>();
    private static ThreadPoolExecutor newPool(){return new ThreadPoolExecutor(1,2,30L,TimeUnit.SECONDS,new ArrayBlockingQueue<Runnable>(8),new ThreadPoolExecutor.AbortPolicy());}
    public static void submit(final EntityPlayerMP player,final int dimension,final int x,final int y,final int z,final UUID displayId,final long generation,final int width,final int height,final String url){
        UUID owner=player.getUniqueID();long now=System.currentTimeMillis();Long next=RATE.get(owner);if(next!=null&&next.longValue()>now){message(player,"Please wait before submitting another Wall Art image.");return;}if(PENDING.containsKey(owner)){message(player,"You already have a Wall Art request in progress.");return;}RATE.put(owner,Long.valueOf(now+Math.max(5000L,com.hfr.config.XFConfig.customFlagRateLimitMs)));PENDING.put(owner,Boolean.TRUE);final UUID ownerId=owner;
        try{workers.execute(new Runnable(){public void run(){byte[] data=null;String error=null,hash=null;try{data=SecureWallArtDownloader.downloadAndProcess(url,width,height);hash=sha256(data);}catch(Exception e){error=e.getMessage()==null?"image import failed":e.getMessage();}final byte[] bytes=data;final String resultHash=hash,problem=error;RESULTS.add(new Runnable(){public void run(){apply(ownerId,dimension,x,y,z,displayId,generation,width,height,resultHash,bytes,problem);}});}});}catch(java.util.concurrent.RejectedExecutionException e){PENDING.remove(owner);message(player,"Wall Art queue is busy; try again later.");}
    }
    private static void apply(UUID owner,int dimension,int x,int y,int z,UUID displayId,long generation,int width,int height,String hash,byte[] bytes,String error){PENDING.remove(owner);EntityPlayerMP player=find(owner);if(error!=null){if(player!=null)message(player,error);return;}WorldServer world=MinecraftServer.getServer().worldServerForDimension(dimension);if(world==null)return;TileEntity raw=world.getTileEntity(x,y,z);if(!(raw instanceof TileEntityWallImage))return;TileEntityWallImage tile=(TileEntityWallImage)raw;if(!displayId.equals(tile.getDisplayId())||!owner.equals(tile.getOwnerId())||generation!=tile.getRequestGeneration())return;WallArtSavedData data=WallArtSavedData.get(world);WallArtSavedData.Record previous=data.get(displayId);WallArtSavedData.Record record=new WallArtSavedData.Record(displayId,owner,dimension,x,y,z,tile.getFacing(),width,height,hash);if(!data.put(record)){if(player!=null)message(player,data.count(owner)>=WallArtConstants.MAX_PER_PLAYER?"Wall Art limit reached (30 displays).":"That display overlaps another Wall Art display.");return;}try{writeImage(hash,bytes);tile.configure(width,height,hash);world.markBlockForUpdate(x,y,z);if(previous!=null&&!previous.hash.equals(hash)&&!data.references(previous.hash)){File orphan=imageFile(previous.hash);if(orphan!=null)orphan.delete();}if(player!=null)message(player,"Wall Art configured.");}catch(IOException e){data.remove(displayId);if(previous!=null)data.put(previous);if(player!=null)message(player,"Could not store the Wall Art image.");}}
    public static void tick(){for(int i=0;i<32;i++){Runnable r=RESULTS.poll();if(r==null)break;r.run();}}
    public static void enqueueMainThread(Runnable task) { if(task != null) RESULTS.add(task); }
    public static void shutdown(){workers.shutdownNow();RESULTS.clear();RATE.clear();PENDING.clear();workers=newPool();}
    public static File imageFile(String hash){if(!WallArtConstants.validHash(hash))return null;File root=MinecraftServer.getServer().worldServerForDimension(0).getSaveHandler().getMapFileFromName(WallArtSavedData.ID).getParentFile().getParentFile();return new File(new File(root,"xenofactions/wallart/images"),hash+".png");}
    private static void writeImage(String hash,byte[] bytes)throws IOException{File file=imageFile(hash);if(file.exists())return;File dir=file.getParentFile();if(!dir.exists()&&!dir.mkdirs())throw new IOException();File tmp=new File(dir,hash+".tmp");FileOutputStream out=new FileOutputStream(tmp);try{out.write(bytes);}finally{out.close();}if(!tmp.renameTo(file)){tmp.delete();throw new IOException();}}
    public static byte[] readImage(String hash)throws IOException{File f=imageFile(hash);if(f==null||!f.isFile()||f.length()>WallArtConstants.MAX_PROCESSED_IMAGE_BYTES)throw new IOException();byte[] b=new byte[(int)f.length()];FileInputStream in=new FileInputStream(f);try{int p=0,n;while(p<b.length&&(n=in.read(b,p,b.length-p))>0)p+=n;if(p!=b.length)throw new IOException();return b;}finally{in.close();}}
    public static void remove(TileEntityWallImage tile){if(tile.getDisplayId()==null||tile.getWorldObj()==null||tile.getWorldObj().isRemote)return;WallArtSavedData data=WallArtSavedData.get(tile.getWorldObj());WallArtSavedData.Record old=data.remove(tile.getDisplayId());if(old!=null&&!data.references(old.hash)){File f=imageFile(old.hash);if(f!=null&&f.exists()&&!f.delete())MainRegistry.logger.warn("Could not remove orphan Wall Art image "+old.hash);}}
    private static EntityPlayerMP find(UUID id){for(Object p:MinecraftServer.getServer().getConfigurationManager().playerEntityList)if(p instanceof EntityPlayerMP&&id.equals(((EntityPlayerMP)p).getUniqueID()))return(EntityPlayerMP)p;return null;}
    private static void message(EntityPlayerMP p,String s){p.addChatMessage(new ChatComponentText("[Wall Art] "+s));}
    private static String sha256(byte[] b)throws Exception{byte[] h=MessageDigest.getInstance("SHA-256").digest(b);StringBuilder s=new StringBuilder();for(byte v:h)s.append(String.format("%02x",v&255));return s.toString();}
    private WallArtService() { }
}
