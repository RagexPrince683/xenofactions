package com.hfr.world.earth;
import net.minecraft.client.Minecraft; import net.minecraft.client.gui.GuiCreateWorld;
import net.minecraft.world.*; import net.minecraft.world.biome.WorldChunkManager; import net.minecraft.world.chunk.IChunkProvider;
import cpw.mods.fml.relauncher.Side; import cpw.mods.fml.relauncher.SideOnly;
public final class XFEarthWorldType extends WorldType {
 public static final String INTERNAL_NAME="earthmap";
 public XFEarthWorldType(){super(INTERNAL_NAME);}
 @Override public IChunkProvider getChunkGenerator(World world,String options){return new XFEarthChunkProvider(world);}
 @Override public WorldChunkManager getChunkManager(World world){return new WorldChunkManager(world);}
 @Override public int getSpawnFuzz(){return 0;}
 @Override public boolean getCanBeCreated(){return true;}
 @Override public boolean isCustomizable(){return true;}
 @Override @SideOnly(Side.CLIENT) public void onCustomizeButton(Minecraft minecraft,GuiCreateWorld parent){minecraft.displayGuiScreen(new com.hfr.client.earth.GuiXFEarthCustomize(parent));}
}
