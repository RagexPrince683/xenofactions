package com.hfr.render.tileentity;

import com.hfr.tileentity.machine.TileEntityMachineBuilder;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;

/** The Depot model has no preview geometry; world-space previews render from EventHandlerClient. */
public class RenderBuilder extends TileEntitySpecialRenderer {
    @Override public void renderTileEntityAt(TileEntity te,double x,double y,double z,float partialTicks) { }
}
