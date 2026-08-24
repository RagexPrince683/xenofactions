package com.hfr.inventory.gui;
import org.lwjgl.opengl.GL11;
import com.hfr.inventory.container.ContainerBuilderNPC;
import com.hfr.packet.PacketDispatcher;
import com.hfr.packet.builder.*;
import com.hfr.tileentity.machine.TileEntityMachineBuilder;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import com.hfr.entity.EntityFactionBuilder;
import com.hfr.util.XFLog;
/** Compact worker status and control screen; planning remains at the Depot. */
public class GUIBuilderNPC extends GuiContainer {
 private final int dimension,depotX,depotY,depotZ;
 public GUIBuilderNPC(InventoryPlayer p,int dimension,int x,int y,int z,TileEntityMachineBuilder d,EntityFactionBuilder b){
  super(ContainerBuilderNPC.createClient(p,d,b));this.dimension=dimension;depotX=x;depotY=y;depotZ=z;xSize=176;ySize=222;
  if(XFLog.isDebugEnabled()&&(!(inventorySlots instanceof ContainerBuilderNPC)||inventorySlots.inventorySlots.size()!=ContainerBuilderNPC.TOTAL_SLOTS))throw new IllegalStateException("Builder NPC GUI requires ContainerBuilderNPC with "+ContainerBuilderNPC.TOTAL_SLOTS+" slots");
 }
 public GUIBuilderNPC(InventoryPlayer p,TileEntityMachineBuilder d,EntityFactionBuilder b){this(p,d.getWorldObj().provider.dimensionId,d.xCoord,d.yCoord,d.zCoord,d,b);}
 @Override public void initGui(){super.initGui();buttons();request();}
 private String tr(String k,Object...a){return I18n.format(k,a);}private BuilderDepotSnapshotPacket snapshot(){return BuilderDepotSnapshotPacket.get(dimension,depotX,depotY,depotZ);}
 private void buttons(){buttonList.clear();BuilderDepotSnapshotPacket s=snapshot();boolean job=s!=null&&s.total>0;add(1,8,48,52,"gui.builder.recall",s!=null);add(2,62,48,50,"gui.builder.pause",job&&!"PAUSED".equals(s.state));add(3,114,48,54,"gui.builder.resume",job&&"PAUSED".equals(s.state));}
 private void add(int id,int x,int y,int w,String key,boolean on){GuiButton b=new GuiButton(id,guiLeft+x,guiTop+y,w,20,tr(key));b.enabled=on;buttonList.add(b);}private void request(){PacketDispatcher.wrapper.sendToServer(new BuilderDepotRequestPacket(dimension,depotX,depotY,depotZ));}
 @Override public void updateScreen(){if(mc.thePlayer.ticksExisted%20==0){request();buttons();}}
 @Override protected void actionPerformed(GuiButton b){int a=b.id==1?BuilderActionPacket.RECALL:b.id==2?BuilderActionPacket.PAUSE:b.id==3?BuilderActionPacket.RESUME:BuilderActionPacket.OPEN_DEPOT;PacketDispatcher.wrapper.sendToServer(new BuilderActionPacket(dimension,depotX,depotY,depotZ,a));request();}
 @Override protected void drawGuiContainerBackgroundLayer(float p,int x,int y){GL11.glDisable(GL11.GL_LIGHTING);drawRect(guiLeft,guiTop,guiLeft+xSize,guiTop+ySize,0xff18222d);drawRect(guiLeft+6,guiTop+68,guiLeft+xSize-6,guiTop+ySize-6,0xff8b8b8b);}
 @Override protected void drawGuiContainerForegroundLayer(int x,int y){fontRendererObj.drawString(tr("gui.builder.npc.title"),8,7,0xffffff);BuilderDepotSnapshotPacket s=snapshot();if(s==null){line(tr("gui.builder.loading"),20);return;}line(s.builderName+" | "+tr("gui.builder.status",tr("builder.state."+s.state)),20);line(tr("gui.builder.current",none(s.schematic)),32);if(s.detail!=null&&!s.detail.isEmpty())fontRendererObj.drawSplitString(s.detail,8,42,160,0xffdddd77);line("Builder inventory",72);line("Player inventory",130);}
 private String none(String s){return s==null||s.isEmpty()?tr("gui.builder.none"):s;}private void line(String s,int y){fontRendererObj.drawString(s,10,y,0xffffff);}
}
