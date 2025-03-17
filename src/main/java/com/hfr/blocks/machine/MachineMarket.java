package com.hfr.blocks.machine;

import java.util.List;

import com.hfr.blocks.ModBlocks;
import com.hfr.data.MarketData;
import com.hfr.lib.RefStrings;
import com.hfr.main.MainRegistry;
import com.hfr.packet.PacketDispatcher;
import com.hfr.packet.tile.OfferPacket;

import cpw.mods.fml.common.network.internal.FMLNetworkHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;

public class MachineMarket extends BlockContainer {

	//fatty
	//how about you get a real fucking job instead of trying to entrap people who give a shit about your stupid ass mod
	//it's not like you care about this stupid shit anyways you even said it was dead you fucking plebian.
	//if it bothers you so much out do me instead of sitting on your ass and crying
	//fucking loser, and stay away from adding guns too you germans don't know shit about firearms.
	//two world wars and here I am teaching your ass a lesson using your own stupid fucking project.
	//I'll change all the textures, because that's all you have.
	//You want to fight for the cretins, the scum and the villainous
	//I will destroy you. One dollar, brick, comment, and change at a time.
	//I already have shit going on. If you want to get involved in my personal life I will give you no end but
	//hell to pay over this stupid fucking mod. Fuck you.

	@SideOnly(Side.CLIENT)
	private IIcon iconTop;
	@SideOnly(Side.CLIENT)
	private IIcon iconBottom;
	
	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister iconRegister) {
		this.iconTop = iconRegister.registerIcon(RefStrings.MODID + ":market_top");
		this.iconBottom = iconRegister.registerIcon(RefStrings.MODID + ":market_bottom");
		this.blockIcon = iconRegister.registerIcon(RefStrings.MODID + ":market_side");
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(int side, int metadata) {
		
		return side == 1 ? this.iconTop : (side == 0 ? this.iconBottom : this.blockIcon);
	}

	public MachineMarket(Material p_i45386_1_) {
		super(p_i45386_1_);
	}
	
    public static String name = "";

	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
		if (!world.isRemote) {
			TileEntityMarket market = (TileEntityMarket) world.getTileEntity(x, y, z);
			if (market == null) return false;

			// Get offers from JSON-based MarketData
			List<ItemStack[]> offers = MarketData.getOffers(market.name);

			// Create NBTTagCompound to send offer data
			NBTTagCompound nbt = new NBTTagCompound();
			nbt.setString("market", market.name);
			nbt.setInteger("offercount", offers.size());

			for (int i = 0; i < offers.size(); i++) {
				NBTTagList list = new NBTTagList();
				ItemStack[] offerArray = offers.get(i);

				for (int j = 0; j < offerArray.length; j++) {
					if (offerArray[j] != null) {
						NBTTagCompound itemTag = new NBTTagCompound();
						offerArray[j].writeToNBT(itemTag);
						list.appendTag(itemTag);
					}
				}
				nbt.setTag("items" + i, list);
			}

			// Send updated market offers to client
			PacketDispatcher.wrapper.sendTo(new OfferPacket(market.name, nbt), (EntityPlayerMP) player);

			// Handle renaming the market with a Name Tag
			if (player.getHeldItem() != null && player.getHeldItem().getItem() == Items.name_tag && player.getHeldItem().hasDisplayName()) {
				market.name = player.getHeldItem().getDisplayName();
				market.markDirty();
				return true;
			}

			return true;
		} else if (!player.isSneaking()) {
			// Open GUI for Market
			FMLNetworkHandler.openGui(player, MainRegistry.instance, ModBlocks.guiID_market, world, x, y, z);
			return true;
		} else {
			return false;
		}
	}


	@Override
	public TileEntity createNewTileEntity(World p_149915_1_, int p_149915_2_) {
		return new TileEntityMarket();
	}
	
	public static class TileEntityMarket extends TileEntity {
		
		public String name = "";

	    public void readFromNBT(NBTTagCompound nbt) {
	    	super.readFromNBT(nbt);
	    	name = nbt.getString("name");
	    }

	    public void writeToNBT(NBTTagCompound nbt) {
	    	super.writeToNBT(nbt);
	    	nbt.setString("name", name);
	    }
	}
}
