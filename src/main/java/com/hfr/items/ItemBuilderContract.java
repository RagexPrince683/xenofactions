package com.hfr.items;

import java.util.UUID;

import com.hfr.builder.BuilderJob;
import com.hfr.builder.BuilderJobData;
import com.hfr.clowder.Clowder;
import com.hfr.clowder.ClowderTerritory;
import com.hfr.clowder.ClowderTerritory.Ownership;
import com.hfr.clowder.ClowderTerritory.TerritoryMeta;
import com.hfr.clowder.FactionRole;
import com.hfr.clowder.ClowderTerritory.Zone;
import com.hfr.entity.EntityFactionBuilder;
import com.hfr.tileentity.machine.TileEntityMachineBuilder;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.common.util.FakePlayer;

/** Server-authoritative contract which assigns a Builder to one existing depot. */
public class ItemBuilderContract extends Item {
	@Override
	public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z,
			int side, float hitX, float hitY, float hitZ) {
		if(world.isRemote) return true;
		if(!(player instanceof EntityPlayerMP) || player instanceof FakePlayer) return false;

		TileEntity tile = world.getTileEntity(x, y, z);
		if(!(tile instanceof TileEntityMachineBuilder)) return message(player, "builder.contract.invalid_depot");
		TileEntityMachineBuilder depot = (TileEntityMachineBuilder)tile;
		Clowder faction = Clowder.getClowderFromPlayer(player);
		if(faction == null) return message(player, "builder.contract.wrong_faction");
		Ownership ownership = ClowderTerritory.getOwnerFromInts(world, x, z);
		TerritoryMeta territory = ClowderTerritory.getMetaFromIntCoords(world, x, z);
		if(ownership == null || ownership.zone != Zone.FACTION || ownership.owner == null)
			return message(player, "builder.contract.outside_territory");
		if(!faction.uuid.equals(ownership.owner.uuid)) return message(player, "builder.contract.wrong_faction");
		if(faction.getPermLevel(player) < FactionRole.OFFICER.getPermissionLevel())
			return message(player, "builder.contract.insufficient_permission");
		if(depot.getFactionId() != null && !depot.getFactionId().equals(parse(faction.uuid)))
			return message(player, "builder.contract.wrong_faction");

		boolean replacing = depot.hasAssignedBuilder();
		if(replacing && !depot.clearAssignmentIfConfirmedStale())
			return message(player, "builder.contract.already_assigned");

		UUID factionId = parse(faction.uuid);
		if(factionId == null) return message(player, "builder.contract.wrong_faction");
		EntityFactionBuilder builder = new EntityFactionBuilder(world);
		builder.setLocationAndAngles(x + .5D, y + 1D, z + .5D, world.rand.nextFloat() * 360F, 0F);
		UUID jobId = depot.getActiveJobId();
		builder.assign(factionId, jobId, x, y, z, world.provider.dimensionId);
		if(!world.spawnEntityInWorld(builder)) return message(player, "builder.contract.spawn_failure");
		depot.assign(factionId, builder.getUniqueID(), territory == null ? "" : territory.cityId);
		BuilderJobData jobs = BuilderJobData.get(world);
		BuilderJob job = jobs == null ? null : jobs.get(jobId);
		if(job != null) { job.builderId = builder.getUniqueID(); jobs.markDirty(); }
		if(!player.capabilities.isCreativeMode) --stack.stackSize;
		return message(player, replacing ? "builder.contract.replaced" : "builder.contract.assigned");
	}

	private static boolean message(EntityPlayer player, String key) {
		player.addChatMessage(new ChatComponentTranslation(key));
		return true;
	}

	private static UUID parse(String value) {
		try { return UUID.fromString(value); } catch(Exception ignored) { return null; }
	}
}
