package com.hbm.blocks.machine;

import java.util.ArrayList;
import java.util.List;

import com.hbm.blocks.ILookOverlay;
import com.hbm.blocks.ITooltipProvider;
import com.hbm.tileentity.machine.TileEntityMoltenSaltReactor;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderGameOverlayEvent.Pre;

public class MachineMoltenSaltReactor extends BlockMachineBase implements ITooltipProvider, ILookOverlay {

	public MachineMoltenSaltReactor(Material mat) {
		super(mat, -1);
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		return new TileEntityMoltenSaltReactor();
	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean ext) {
		list.add(EnumChatFormatting.YELLOW + "Heats liquid thorium salt into hot salt.");
		list.add(EnumChatFormatting.YELLOW + "All six sides need shielding: lead, boron, radiation-resistant concrete,");
		list.add(EnumChatFormatting.YELLOW + "or MSR inlet/outlet ports for pipe access through the shield.");
		list.add(EnumChatFormatting.GOLD + "Corrodes while processing; corrosion causes salt loss.");
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void printHook(Pre event, World world, int x, int y, int z) {
		List<String> text = new ArrayList<String>();
		TileEntity tile = world.getTileEntity(x, y, z);

		if(tile instanceof TileEntityMoltenSaltReactor) {
			TileEntityMoltenSaltReactor reactor = (TileEntityMoltenSaltReactor) tile;
			text.add("Input: " + reactor.tanks[0].getFill() + "/" + reactor.tanks[0].getMaxFill() + " mB");
			text.add("Output: " + reactor.tanks[1].getFill() + "/" + reactor.tanks[1].getMaxFill() + " mB");
			text.add("Rate: " + reactor.output + " mB/t");
			text.add("Corrosion: " + reactor.corrosion + "%");
			text.add(reactor.isShielded() ? "Shielding: OK" : "&[16733525&]Shielding: LEAKING");
			text.add("Use lead/boron/concrete or MSR ports on all sides.");
		} else {
			text.add("Pipe liquid thorium salt in.");
			text.add("Pipe hot thorium salt out.");
		}

		ILookOverlay.printGeneric(event, "Molten Salt Reactor", 0xffa000, 0x402000, text);
	}
}
