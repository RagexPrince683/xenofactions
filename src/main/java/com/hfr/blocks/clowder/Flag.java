package com.hfr.blocks.clowder;

import com.hfr.blocks.ModBlocks;
import com.hfr.clowder.CityLevel;
import com.hfr.config.XFConfig;
import com.hfr.clowder.Clowder;
import com.hfr.clowder.ClowderTerritory;
import com.hfr.clowder.ClowderTerritory.CoordPair;
import com.hfr.main.MainRegistry;
import com.hfr.tileentity.clowder.TileEntityFlag;

import cpw.mods.fml.common.network.internal.FMLNetworkHandler;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

public class Flag extends BlockContainer {

	public Flag(Material p_i45386_1_) {
		super(p_i45386_1_);
	}

	@Override
	public TileEntity createNewTileEntity(World p_149915_1_, int p_149915_2_) {
		return new TileEntityFlag();
	}

	@Override
	public int getRenderType(){
		return -1;
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
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
		if(world.isRemote)
			return true;

		TileEntity tile = world.getTileEntity(x, y, z);
		if(tile instanceof TileEntityFlag) {
			TileEntityFlag flag = (TileEntityFlag)tile;
			if(flag.owner == null || !flag.canSeeSky()) {
				world.func_147480_a(x, y, z, false);
				return true;
			}
		}

		if(!player.isSneaking()) {
			FMLNetworkHandler.openGui(player, MainRegistry.instance, ModBlocks.guiID_flag, world, x, y, z);
			return true;
		}

		return true;
	}

	@Override
	public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase player, ItemStack itemStack) {

		int i = MathHelper.floor_double(player.rotationYaw * 4.0F / 360.0F + 0.5D) & 3;

		if(i == 0)
		{
			world.setBlockMetadataWithNotify(x, y, z, 2, 2);
		}
		if(i == 1)
		{
			world.setBlockMetadataWithNotify(x, y, z, 5, 2);
		}
		if(i == 2)
		{
			world.setBlockMetadataWithNotify(x, y, z, 3, 2);
		}
		if(i == 3)
		{
			world.setBlockMetadataWithNotify(x, y, z, 4, 2);
		}

		if(player instanceof EntityPlayer && !world.isRemote) {
			TileEntityFlag flag = (TileEntityFlag)world.getTileEntity(x, y, z);
			EntityPlayer entityPlayer = (EntityPlayer)player;

			Clowder clowder = Clowder.getClowderFromPlayer(entityPlayer);
			String cityName = itemStack.hasTagCompound() ? itemStack.stackTagCompound.getString("cityName") : "";
			CoordPair cityChunk = ClowderTerritory.getCoordPair(x, z);
			String cityError = ClowderTerritory.getCityPlacementError(cityChunk.x, cityChunk.z);

			if(cityName == null || cityName.trim().isEmpty()) {
				world.setBlockToAir(x, y, z);
				entityPlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "City Centers require a name. Use /c claim <city name>."));
				return;
			}
			cityName = cityName.trim();
			if(!ClowderTerritory.isCityNameAvailable(cityName, null)) {
				world.setBlockToAir(x, y, z);
				entityPlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "A city named " + cityName + " already exists."));
				return;
			}
			if(cityError != null) {
				world.setBlockToAir(x, y, z);
				entityPlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + cityError));
				return;
			}

			if(clowder != null && flag.canSeeSky()) {
				float foundingCost = clowder.getCityFoundingCost();
				float foundingUpkeep = XFConfig.cityUpkeep(CityLevel.SETTLEMENT);
				if(clowder.getPrestige() < foundingCost || clowder.getPrestigeReq() + foundingUpkeep > clowder.getPrestige() - foundingCost) {
					world.setBlockToAir(x, y, z);
					entityPlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "Founding a City Center requires " + foundingCost + " prestige and " + foundingUpkeep + " upkeep capacity."));
					return;
				}
				clowder.addPrestige(-foundingCost, world);
				clowder.markCityFounded(world);
				flag.name = cityName;
				flag.setOwner(clowder);
				flag.setMode(1);
				flag.isClaimed = true;
				flag.generateClaim();
			} else {
				flag.height = 0.0F;
				entityPlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "You won't be able to raise this flag. This may be due to:"));
				entityPlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "-The flag lacking a solid 5x5 block foundation"));
				entityPlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "-The flag's foundation not having sky access"));
				entityPlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "-You not being in any faction"));
				entityPlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "-The flag being below Y:45 or above Y:200"));
			}

			flag.markDirty();
		}

		super.onBlockPlacedBy(world, x, y, z, player, itemStack);
	}

	public boolean noProximity(World world, int x, int y, int z) {

		int range = 4;

		for(int ix = x - range; ix <= x + range; ix++) {
			for(int iy = y - 3; iy <= y + 3; iy++) {
				for(int iz = z - range; iz <= z + range; iz++) {

					if(ix == x && iy == y && iz == z)
						continue;

					if(world.getBlock(ix, iy, iz) == ModBlocks.clowder_conquerer) {
						return false;
					}
				}
			}
		}

		return true;
	}

	@Override
	public void breakBlock(World world, int x, int y, int z, Block b, int i)
	{
		TileEntity tile = world.getTileEntity(x, y, z);

		if(tile instanceof TileEntityFlag) {
			TileEntityFlag flag = (TileEntityFlag)tile;

			if(flag.owner != null)
				flag.setOwner(null);

			flag.height = 0.0F;
			ClowderTerritory.removeClaimsForCity(world, x, y, z);
		}

		super.breakBlock(world, x, y, z, b, i);
    }

}
