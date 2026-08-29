package com.hbm.blocks.machine;

import java.util.ArrayList;
import java.util.List;

import com.hbm.blocks.ILookOverlay;
import com.hbm.blocks.ITooltipProvider;
import com.hbm.blocks.ModBlocks;
import com.hbm.tileentity.machine.TileEntityMoltenSaltReactor;
import com.hbm.tileentity.machine.TileEntityMoltenSaltReactorPort;

import net.minecraft.block.BlockContainer;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderGameOverlayEvent.Pre;

public class MachineMoltenSaltReactorPort extends BlockContainer implements ITooltipProvider, ILookOverlay {

	public static int renderID = RenderingRegistry.getNextAvailableRenderId();

	private final boolean input;

	public MachineMoltenSaltReactorPort(Material mat, boolean input) {
		super(mat);
		this.input = input;
	}

	public boolean isInput() {
		return input;
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		return new TileEntityMoltenSaltReactorPort(input);
	}

	@Override
	public int getRenderType() {
		return renderID;
	}

	@Override
	public boolean isOpaqueCube() {
		return false;
	}

	@Override
	public boolean renderAsNormalBlock() {
		return false;
	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean ext) {
		if(this == ModBlocks.machine_msr_input) {
			list.add(EnumChatFormatting.YELLOW + "Shielding-compatible MSR inlet.");
			list.add(EnumChatFormatting.YELLOW + "Place touching the reactor and pipe liquid thorium salt into it.");
		} else {
			list.add(EnumChatFormatting.YELLOW + "Shielding-compatible MSR outlet.");
			list.add(EnumChatFormatting.YELLOW + "Place touching the reactor and pipe hot thorium salt out of it.");
		}
	}
	@Override
	@SideOnly(Side.CLIENT)
	public void printHook(Pre event, World world, int x, int y, int z) {
		List<String> text = new ArrayList<String>();
		TileEntity tile = world.getTileEntity(x, y, z);

		if(tile instanceof TileEntityMoltenSaltReactorPort) {
			TileEntityMoltenSaltReactorPort port = (TileEntityMoltenSaltReactorPort) tile;
			TileEntityMoltenSaltReactor reactor = port.getReactor();
			text.add((port.isInput() ? "Inlet" : "Outlet") + ": " + port.tank.getFill() + "/" + port.tank.getMaxFill() + " mB");

			if(reactor != null) {
				text.add("MSR Input: " + reactor.tanks[0].getFill() + "/" + reactor.tanks[0].getMaxFill() + " mB");
				text.add("MSR Output: " + reactor.tanks[1].getFill() + "/" + reactor.tanks[1].getMaxFill() + " mB");
				text.add("Rate: " + reactor.output + " mB/t");
				text.add("Corrosion: " + reactor.corrosion + "%");
				text.add(reactor.isShielded() ? "Shielding: OK" : "&[16733525&]Shielding: LEAKING");
			} else {
				text.add("No adjacent MSR core");
			}
		}

		ILookOverlay.printGeneric(event, input ? "MSR Inlet" : "MSR Outlet", input ? 0x7a5542 : 0xffa000, 0x302010, text);
	}
}
