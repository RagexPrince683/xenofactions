package com.hfr.blocks.machine;

import com.hfr.blocks.ModBlocks;
import com.hfr.data.MarketData;
import com.hfr.lib.RefStrings;
import com.hfr.main.MainRegistry;
import com.hfr.packet.PacketDispatcher;
import com.hfr.packet.tile.OfferPacket;
import cpw.mods.fml.common.network.internal.FMLNetworkHandler;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.Packet;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public class MachineMarket extends BlockContainer {

	public static String name = "";

	@SideOnly(cpw.mods.fml.relauncher.Side.CLIENT)
	private IIcon iconTop;
	@SideOnly(cpw.mods.fml.relauncher.Side.CLIENT)
	private IIcon iconBottom;

	public MachineMarket(Material mat) {
		super(mat);
	}

	@Override
	@SideOnly(cpw.mods.fml.relauncher.Side.CLIENT)
	public void registerBlockIcons(IIconRegister iconRegister) {
		this.iconTop = iconRegister.registerIcon(RefStrings.MODID + ":market_top");
		this.iconBottom = iconRegister.registerIcon(RefStrings.MODID + ":market_bottom");
		this.blockIcon = iconRegister.registerIcon(RefStrings.MODID + ":market_side");
	}

	@Override
	@SideOnly(cpw.mods.fml.relauncher.Side.CLIENT)
	public IIcon getIcon(int side, int metadata) {
		return side == 1 ? this.iconTop : (side == 0 ? this.iconBottom : this.blockIcon);
	}

	@Override
	public boolean onBlockActivated(World world, int x, int y, int z,
									EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
		if (!world.isRemote) {
			TileEntityMarket market = (TileEntityMarket) world.getTileEntity(x, y, z);
			if (market == null) return false;

			// send offers to client opening the GUI
			NBTTagCompound nbt = new NBTTagCompound();
			MarketData data = MarketData.getData(world);
			List<MarketData.Offer> offers = data.offers.get(market.name);
			if (offers == null) offers = new ArrayList<MarketData.Offer>();
			data.writeOffersToNBT(nbt, market.name, offers);

			if (player instanceof EntityPlayerMP) {
				PacketDispatcher.wrapper.sendTo(new OfferPacket(market.name, nbt), (EntityPlayerMP) player);
			}

			// allow renaming with name tag
			if (player.getHeldItem() != null && player.getHeldItem().getItem() == Items.name_tag && player.getHeldItem().hasDisplayName()) {
				market.name = player.getHeldItem().getDisplayName();
				market.markDirty();
				world.markBlockForUpdate(x, y, z); // sync new name to clients
				return true;
			}

			return true;
		} else {
			// client: open GUI; the server will have sent the OfferPacket above, so GUIMachineMarket.offers should be populated.
			if (!player.isSneaking()) {
				FMLNetworkHandler.openGui(player, MainRegistry.instance, ModBlocks.guiID_market, world, x, y, z);
				return true;
			} else {
				return false;
			}
		}
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		return new TileEntityMarket();
	}

	public static class TileEntityMarket extends TileEntity {
		public String name = "";

		@Override
		public void readFromNBT(NBTTagCompound nbt) {
			super.readFromNBT(nbt);
			if (nbt.hasKey("name")) name = nbt.getString("name");
		}

		@Override
		public void writeToNBT(NBTTagCompound nbt) {
			super.writeToNBT(nbt);
			nbt.setString("name", name != null ? name : "");
		}

		@Override
		public Packet getDescriptionPacket() {
			NBTTagCompound nbt = new NBTTagCompound();
			this.writeToNBT(nbt);
			return new S35PacketUpdateTileEntity(xCoord, yCoord, zCoord, 0, nbt);
		}

		@Override
		public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity pkt) {
			this.readFromNBT(pkt.func_148857_g());
		}
	}
}
