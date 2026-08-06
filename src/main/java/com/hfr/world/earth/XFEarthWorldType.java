package com.hfr.world.earth;

import com.hfr.client.earth.GuiXFEarthCustomize;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiCreateWorld;
import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.WorldChunkManager;
import net.minecraft.world.chunk.IChunkProvider;

public final class XFEarthWorldType extends WorldType {

 public static final String INTERNAL_NAME = "earthmap";

 /*
  * Minecraft 1.7.10 passes this value directly to Random.nextInt().
  *
  * Zero is illegal and crashes when a player joins.
  * One creates no effective random displacement:
  *
  * nextInt(1) == 0
  * 1 / 2 == 0
  *
  * This preserves the exact spawn X/Z stored by the Earth template.
  */
 private static final int SPAWN_FUZZ = 1;

 public XFEarthWorldType() {
  super(INTERNAL_NAME);
 }

 @Override
 public IChunkProvider getChunkGenerator(
         World world,
         String generatorOptions
 ) {
  return new XFEarthChunkProvider(world);
 }

 @Override
 public WorldChunkManager getChunkManager(World world) {
  return new WorldChunkManager(world);
 }

 @Override
 public int getSpawnFuzz() {
  return SPAWN_FUZZ;
 }

 @Override
 public boolean getCanBeCreated() {
  return true;
 }

 @Override
 public boolean isCustomizable() {
  return true;
 }

 @Override
 @SideOnly(Side.CLIENT)
 public void onCustomizeButton(
         Minecraft minecraft,
         GuiCreateWorld parent
 ) {
  minecraft.displayGuiScreen(
          new GuiXFEarthCustomize(parent)
  );
 }
}