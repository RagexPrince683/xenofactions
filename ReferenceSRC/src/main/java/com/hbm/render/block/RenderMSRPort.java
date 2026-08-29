package com.hbm.render.block;

import org.lwjgl.opengl.GL11;

import com.hbm.blocks.machine.MachineMoltenSaltReactorPort;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.tileentity.machine.TileEntityMoltenSaltReactor;
import com.hbm.tileentity.machine.TileEntityMoltenSaltReactorPort;

import api.hbm.fluid.IFluidConnector;
import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.util.ForgeDirection;

public class RenderMSRPort implements ISimpleBlockRenderingHandler {

	@Override
	public void renderInventoryBlock(Block block, int metadata, int modelId, RenderBlocks renderer) {
		GL11.glTranslatef(-0.5F, -0.5F, -0.5F);
		Tessellator tessellator = Tessellator.instance;
		tessellator.startDrawingQuads();
		renderInventoryBox(block, renderer, tessellator, 0.0D, 0.0D, 0.0D, 1.0D, 1.0D, 1.0D);
		renderInventoryBox(block, renderer, tessellator, 0.25D, 0.25D, -0.125D, 0.75D, 0.75D, 1.125D);
		renderInventoryBox(block, renderer, tessellator, 0.1875D, 0.1875D, 0.0D, 0.8125D, 0.8125D, 0.1875D);
		renderInventoryBox(block, renderer, tessellator, 0.1875D, 0.1875D, 0.8125D, 0.8125D, 0.8125D, 1.0D);
		tessellator.draw();
	}

	private void renderInventoryBox(Block block, RenderBlocks renderer, Tessellator tessellator, double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
		renderer.setRenderBounds(minX, minY, minZ, maxX, maxY, maxZ);
		IIcon icon = block.getIcon(0, 0);
		tessellator.setNormal(0F, -1F, 0F);
		renderer.renderFaceYNeg(block, 0, 0, 0, icon);
		tessellator.setNormal(0F, 1F, 0F);
		renderer.renderFaceYPos(block, 0, 0, 0, icon);
		tessellator.setNormal(0F, 0F, -1F);
		renderer.renderFaceZNeg(block, 0, 0, 0, icon);
		tessellator.setNormal(0F, 0F, 1F);
		renderer.renderFaceZPos(block, 0, 0, 0, icon);
		tessellator.setNormal(-1F, 0F, 0F);
		renderer.renderFaceXNeg(block, 0, 0, 0, icon);
		tessellator.setNormal(1F, 0F, 0F);
		renderer.renderFaceXPos(block, 0, 0, 0, icon);
	}

	@Override
	public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int modelId, RenderBlocks renderer) {
		renderer.setRenderBounds(0.0D, 0.0D, 0.0D, 1.0D, 1.0D, 1.0D);
		renderer.renderStandardBlock(block, x, y, z);

		for(ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
			if(canRenderPipeConnection(world, x, y, z, block, dir)) {
				renderPipeConnection(block, x, y, z, renderer, dir);
			}
		}

		renderer.setRenderBounds(0.0D, 0.0D, 0.0D, 1.0D, 1.0D, 1.0D);
		return true;
	}

	private boolean canRenderPipeConnection(IBlockAccess world, int x, int y, int z, Block block, ForgeDirection dir) {
		TileEntity tile = world.getTileEntity(x + dir.offsetX, y + dir.offsetY, z + dir.offsetZ);
		if(!(tile instanceof IFluidConnector) || tile instanceof TileEntityMoltenSaltReactor) return false;

		FluidType type = Fluids.THORIUM_SALT;
		if(block instanceof MachineMoltenSaltReactorPort && !((MachineMoltenSaltReactorPort) block).isInput()) type = Fluids.THORIUM_SALT_HOT;
		TileEntity here = world.getTileEntity(x, y, z);
		if(here instanceof TileEntityMoltenSaltReactorPort) type = ((TileEntityMoltenSaltReactorPort) here).tank.getTankType();

		return ((IFluidConnector) tile).canConnect(type, dir.getOpposite());
	}

	private void renderPipeConnection(Block block, int x, int y, int z, RenderBlocks renderer, ForgeDirection dir) {
		double minX = 0.25D;
		double minY = 0.25D;
		double minZ = 0.25D;
		double maxX = 0.75D;
		double maxY = 0.75D;
		double maxZ = 0.75D;

		if(dir == ForgeDirection.DOWN) minY = -0.125D;
		if(dir == ForgeDirection.UP) maxY = 1.125D;
		if(dir == ForgeDirection.NORTH) minZ = -0.125D;
		if(dir == ForgeDirection.SOUTH) maxZ = 1.125D;
		if(dir == ForgeDirection.WEST) minX = -0.125D;
		if(dir == ForgeDirection.EAST) maxX = 1.125D;

		renderer.setRenderBounds(minX, minY, minZ, maxX, maxY, maxZ);
		renderer.renderStandardBlock(block, x, y, z);
	}

	@Override
	public boolean shouldRender3DInInventory(int modelId) {
		return true;
	}

	@Override
	public int getRenderId() {
		return MachineMoltenSaltReactorPort.renderID;
	}
}
