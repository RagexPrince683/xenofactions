package com.hfr.inventory.gui;

import java.util.*;
import org.lwjgl.input.Keyboard;
import com.hfr.packet.PacketDispatcher;
import com.hfr.packet.builder.*;
import com.hfr.schematic.Schematic;
import com.hfr.schematic.client.SchematicaCompat;
import net.minecraft.client.gui.*;
import net.minecraft.client.resources.I18n;

/** Searchable server-native schematic browser with an independent optional import path. */
public class GUIBuilderSchematicSelection extends GuiScreen {
    private final GUIMachineBuilder parent;private BuilderLibraryPacket.Entry selected;private Schematic imported;private GuiTextField search;private int scroll,lastRequest;
    public GUIBuilderSchematicSelection(GUIMachineBuilder parent){this.parent=parent;this.selected=parent.getNativeSelection();this.imported=parent.getImportedSelection();}
    @Override public void initGui(){Keyboard.enableRepeatEvents(true);search=new GuiTextField(fontRendererObj,width/2-150,34,300,20);buttonList.add(new GuiButton(1,width/2-154,height-28,72,20,I18n.format("gui.builder.back")));buttonList.add(new GuiButton(4,width/2-78,height-28,72,20,I18n.format("gui.builder.refresh")));buttonList.add(new GuiButton(2,width/2+82,height-28,72,20,I18n.format("gui.builder.done")));GuiButton imp=new GuiButton(3,width/2-2,height-28,80,20,I18n.format("gui.builder.import"));imp.enabled=SchematicaCompat.installed();buttonList.add(imp);request(true);}
    private void request(boolean force){PacketDispatcher.wrapper.sendToServer(new BuilderLibraryRequestPacket(mc.thePlayer.dimension,parent.getDepotX(),parent.getDepotY(),parent.getDepotZ(),force));lastRequest=mc.thePlayer.ticksExisted;}
    @Override public void updateScreen(){search.updateCursorCounter();if(mc.thePlayer.ticksExisted-lastRequest>=30)request(false);}
    private List<BuilderLibraryPacket.Entry> filtered(){List<BuilderLibraryPacket.Entry> out=new ArrayList<BuilderLibraryPacket.Entry>();String q=search.getText().toLowerCase(Locale.ROOT);for(BuilderLibraryPacket.Entry e:BuilderLibraryPacket.getClientEntries())if(q.isEmpty()||e.name.toLowerCase(Locale.ROOT).contains(q))out.add(e);return out;}
    @Override protected void actionPerformed(GuiButton b){if(b.id==1)mc.displayGuiScreen(parent);else if(b.id==4)request(true);else if(b.id==2){if(imported!=null)parent.selectImportedSchematic(imported);else if(selected!=null)parent.selectNativeSchematic(selected);mc.displayGuiScreen(parent);}else if(b.id==3){Schematic value=SchematicaCompat.importActive();if(value!=null){imported=value;selected=null;}}}
    @Override public void drawScreen(int mx,int my,float partial){drawDefaultBackground();drawCenteredString(fontRendererObj,I18n.format("gui.builder.selection.title"),width/2,12,0xffffff);search.drawTextBox();List<BuilderLibraryPacket.Entry> list=filtered();int left=width/2-150;for(int i=0;i<9&&i+scroll<list.size();i++){BuilderLibraryPacket.Entry e=list.get(i+scroll);int y=62+i*18;drawRect(left,y,left+190,y+16,e==selected?0xff4f88a8:0xff263746);fontRendererObj.drawString(e.name,left+4,y+4,0xffffff);}int x=width/2+52;String name=imported!=null?imported.name:selected==null?I18n.format("gui.builder.none"):selected.name;drawString(fontRendererObj,I18n.format("gui.builder.selected",name),x,64,0xffffff);if(imported!=null)drawString(fontRendererObj,I18n.format("gui.builder.dimensions",imported.width,imported.height,imported.length),x,82,0xffffff);else if(selected!=null)drawString(fontRendererObj,I18n.format("gui.builder.dimensions",selected.width,selected.height,selected.length),x,82,0xffffff);super.drawScreen(mx,my,partial);}
    @Override protected void mouseClicked(int mx,int my,int b){search.mouseClicked(mx,my,b);if(b==0){List<BuilderLibraryPacket.Entry> list=filtered();int left=width/2-150;for(int i=0;i<9&&i+scroll<list.size();i++)if(mx>=left&&mx<left+190&&my>=62+i*18&&my<78+i*18){selected=list.get(i+scroll);imported=null;}}super.mouseClicked(mx,my,b);}
    @Override public void handleMouseInput(){super.handleMouseInput();int wheel=org.lwjgl.input.Mouse.getEventDWheel();if(wheel!=0)scroll=Math.max(0,Math.min(Math.max(0,filtered().size()-9),scroll+(wheel<0?1:-1)));}
    @Override protected void keyTyped(char c,int key){if(search.textboxKeyTyped(c,key)){scroll=0;return;}if(key==1)mc.displayGuiScreen(parent);}
    @Override public void onGuiClosed(){Keyboard.enableRepeatEvents(false);}
}
