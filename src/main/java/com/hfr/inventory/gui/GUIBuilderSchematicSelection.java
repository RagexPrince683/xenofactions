package com.hfr.inventory.gui;

import java.util.*;
import org.lwjgl.input.Keyboard;
import com.hfr.builder.BuilderMaterialResolver;
import com.hfr.main.MainRegistry;
import com.hfr.schematic.Schematic;
import com.hfr.schematic.client.SchematicaCompat;
import net.minecraft.client.gui.*;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;

/** Dedicated searchable schematic browser; the Depot container remains the player's open container. */
public class GUIBuilderSchematicSelection extends GuiScreen {
    private final GUIMachineBuilder parent;private Schematic selected;private GuiTextField search;private int scroll;
    public GUIBuilderSchematicSelection(GUIMachineBuilder parent,Schematic selected){this.parent=parent;this.selected=selected;}
    @Override public void initGui(){Keyboard.enableRepeatEvents(true);search=new GuiTextField(fontRendererObj,width/2-150,34,300,20);buttonList.add(new GuiButton(1,width/2-154,height-28,100,20,I18n.format("gui.builder.back")));buttonList.add(new GuiButton(2,width/2+54,height-28,100,20,I18n.format("gui.builder.done")));GuiButton imp=new GuiButton(3,width/2-50,height-28,100,20,I18n.format("gui.builder.import"));imp.enabled=SchematicaCompat.installed();buttonList.add(imp);}
    private List<Schematic> filtered(){List<Schematic> out=new ArrayList<Schematic>();String q=search.getText().toLowerCase(Locale.ROOT);for(Schematic s:MainRegistry.schems)if(s!=null&&(q.isEmpty()||s.name.toLowerCase(Locale.ROOT).contains(q)))out.add(s);return out;}
    @Override protected void actionPerformed(GuiButton b){if(b.id==1)mc.displayGuiScreen(parent);else if(b.id==2){if(selected!=null)parent.selectSchematic(selected);mc.displayGuiScreen(parent);}else if(b.id==3){Schematic imported=SchematicaCompat.importActive();if(imported!=null)selected=imported;}}
    @Override public void drawScreen(int mx,int my,float partial){drawDefaultBackground();drawCenteredString(fontRendererObj,I18n.format("gui.builder.selection.title"),width/2,12,0xffffff);search.drawTextBox();List<Schematic> list=filtered();int left=width/2-150;for(int i=0;i<9&&i+scroll<list.size();i++){Schematic s=list.get(i+scroll);int y=62+i*18;drawRect(left,y,left+190,y+16,s==selected?0xff4f88a8:0xff263746);fontRendererObj.drawString(s.name,left+4,y+4,0xffffff);}int x=width/2+52;drawString(fontRendererObj,I18n.format("gui.builder.selected",selected==null?I18n.format("gui.builder.none"):selected.name),x,64,0xffffff);if(selected!=null){drawString(fontRendererObj,I18n.format("gui.builder.dimensions",selected.width,selected.height,selected.length),x,82,0xffffff);drawString(fontRendererObj,I18n.format("gui.builder.materials.required"),x,104,0xffffff);for(String line:summary(selected))fontRendererObj.drawString(line,x,118+summary(selected).indexOf(line)*12,0xdddddd);}super.drawScreen(mx,my,partial);}
    private List<String> summary(Schematic s){Map<String,Integer> m=new LinkedHashMap<String,Integer>();for(int x=0;x<s.width;x++)for(int y=0;y<s.height;y++)for(int z=0;z<s.length;z++){ItemStack a=BuilderMaterialResolver.resolve(s.resolveBlock(x,y,z),s.getMetadata(x,y,z));String k=a==null?I18n.format("gui.builder.unsupported"):a.getDisplayName();m.put(k,(m.containsKey(k)?m.get(k):0)+1);}List<String> out=new ArrayList<String>();for(Map.Entry<String,Integer> e:m.entrySet()){out.add(e.getKey()+" x"+e.getValue());if(out.size()==7)break;}return out;}
    @Override protected void mouseClicked(int mx,int my,int b){search.mouseClicked(mx,my,b);if(b==0){List<Schematic> list=filtered();int left=width/2-150;for(int i=0;i<9&&i+scroll<list.size();i++)if(mx>=left&&mx<left+190&&my>=62+i*18&&my<78+i*18)selected=list.get(i+scroll);}super.mouseClicked(mx,my,b);}
    @Override public void handleMouseInput() throws java.io.IOException{super.handleMouseInput();int wheel=org.lwjgl.input.Mouse.getEventDWheel();if(wheel!=0){scroll=Math.max(0,Math.min(Math.max(0,filtered().size()-9),scroll+(wheel<0?1:-1)));}}
    @Override protected void keyTyped(char c,int key){if(search.textboxKeyTyped(c,key)){scroll=0;return;}if(key==1)mc.displayGuiScreen(parent);}
    @Override public void onGuiClosed(){Keyboard.enableRepeatEvents(false);}
}
