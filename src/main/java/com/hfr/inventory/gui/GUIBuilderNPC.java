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
/** Compact worker status and control screen; planning remains at the Depot. */
public class GUIBuilderNPC extends GuiContainer {
 private final TileEntityMachineBuilder depot;
 public GUIBuilderNPC(InventoryPlayer p,TileEntityMachineBuilder d){super(new ContainerBuilderNPC());depot=d;xSize=300;ySize=205;}
 @Override public void initGui(){super.initGui();buttons();request();}
 private String tr(String k,Object...a){return I18n.format(k,a);}private BuilderDepotSnapshotPacket snapshot(){return BuilderDepotSnapshotPacket.get(mc.thePlayer.dimension,depot.xCoord,depot.yCoord,depot.zCoord);}
 private void buttons(){buttonList.clear();BuilderDepotSnapshotPacket s=snapshot();boolean job=s!=null&&s.total>0;add(1,10,172,84,"gui.builder.recall",s!=null);add(2,98,172,62,"gui.builder.pause",job&&!"PAUSED".equals(s.state));add(3,164,172,66,"gui.builder.resume",job&&"PAUSED".equals(s.state));boolean near=mc.thePlayer.getDistanceSq(depot.xCoord+.5,depot.yCoord+.5,depot.zCoord+.5)<=64;add(4,234,172,58,"gui.builder.open_depot",near);}
 private void add(int id,int x,int y,int w,String key,boolean on){GuiButton b=new GuiButton(id,guiLeft+x,guiTop+y,w,20,tr(key));b.enabled=on;buttonList.add(b);}private void request(){PacketDispatcher.wrapper.sendToServer(new BuilderDepotRequestPacket(mc.thePlayer.dimension,depot.xCoord,depot.yCoord,depot.zCoord));}
 @Override public void updateScreen(){if(mc.thePlayer.ticksExisted%20==0){request();buttons();}}
 @Override protected void actionPerformed(GuiButton b){int a=b.id==1?BuilderActionPacket.RECALL:b.id==2?BuilderActionPacket.PAUSE:b.id==3?BuilderActionPacket.RESUME:BuilderActionPacket.OPEN_DEPOT;PacketDispatcher.wrapper.sendToServer(new BuilderActionPacket(depot,a));request();}
 @Override protected void drawGuiContainerBackgroundLayer(float p,int x,int y){GL11.glDisable(GL11.GL_LIGHTING);drawRect(guiLeft,guiTop,guiLeft+xSize,guiTop+ySize,0xff18222d);drawRect(guiLeft+6,guiTop+30,guiLeft+xSize-6,guiTop+ySize-6,0xff263746);}
 @Override protected void drawGuiContainerForegroundLayer(int x,int y){fontRendererObj.drawString(tr("gui.builder.npc.title"),10,10,0xffffff);BuilderDepotSnapshotPacket s=snapshot();if(s==null){line(tr("gui.builder.loading"),40);return;}line(s.builderName,40);line(tr("gui.builder.health",Math.round(s.health),Math.round(s.maxHealth)),54);line(tr("gui.builder.faction",none(s.faction)),68);line(tr("gui.builder.city",none(s.city)),82);line(tr("gui.builder.depot_coordinates",depot.xCoord,depot.yCoord,depot.zCoord),96);line(tr("gui.builder.status",tr("builder.state."+s.state)),110);line(tr("gui.builder.current",none(s.schematic)),124);line(tr("gui.builder.carried",none(s.carried)),138);if(s.detail!=null&&!s.detail.isEmpty())fontRendererObj.drawSplitString(s.detail,10,152,350,0xffdddd77);else if(s.blocked)line(tr("gui.builder.blocked",s.bx,s.by,s.bz),152);}
 private String none(String s){return s==null||s.isEmpty()?tr("gui.builder.none"):s;}private void line(String s,int y){fontRendererObj.drawString(s,10,y,0xffffff);}
}
