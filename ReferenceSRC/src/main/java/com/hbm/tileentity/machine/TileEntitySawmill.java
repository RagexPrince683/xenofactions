package com.hbm.tileentity.machine;

import java.util.HashMap;
import java.util.List;

import com.hbm.blocks.BlockDummyable;
import com.hbm.entity.projectile.EntitySawblade;
import com.hbm.inventory.RecipesCommon.OreDictStack;
import com.hbm.items.ModItems;
import com.hbm.lib.ModDamageSource;
import com.hbm.packet.PacketDispatcher;
import com.hbm.packet.toclient.AuxParticlePacketNT;
import com.hbm.tileentity.INBTPacketReceiver;
import com.hbm.tileentity.TileEntityMachineBase;
import com.hbm.tileentity.machine.TileEntityMachineAutocrafter.InventoryCraftingAuto;
import com.hbm.util.ItemStackUtil;

import api.hbm.tile.IHeatSource;
import cpw.mods.fml.common.network.NetworkRegistry.TargetPoint;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.common.util.ForgeDirection;

public class TileEntitySawmill extends TileEntityMachineBase {

	/*
	 * Realistified sawmill model:
	 *
	 * - Heat is no longer directly converted into cutting progress.
	 * - Heat input is treated as power input for the saw drive.
	 * - The blade has persistent rotational speed.
	 * - Cutting consumes blade speed depending on material load.
	 * - The blade can coast down, jam, wear out, and overspeed.
	 * - Logs always produce sawdust as kerf waste.
	 * - Overspeed ejects the blade instead of creating a generic explosion.
	 */

	public int heat;
	public static final double diffusion = 0.1D;

	private int warnCooldown = 0;
	private int overspeed = 0;
	private int jamCooldown = 0;
	private int syncCooldown = 0;
	private boolean forceInventorySync = false;

	public boolean hasBlade = true;
	public int progress = 0;
	public static final int processingTime = 600;

	public float spin;
	public float lastSpin;

	/* Persistent mechanical state. This is synced and saved. */
	public double bladeSpeed = 0.0D;
	public double bladeWear = 0.0D;

	/* Tuning constants. */
	private static final int SYNC_INTERVAL = 5;
	private static final int FULL_SYNC_INTERVAL = 20;
	private static final int MAX_HEAT_DRAW = 120;

	private static final double HEAT_TO_BLADE_SPEED = 0.060D;
	private static final double BLADE_FRICTION = 0.985D;
	private static final double IDLE_DRAG = 0.030D;

	private static final double MIN_CUT_SPEED = 90.0D;
	private static final double NOMINAL_CUT_SPEED = 240.0D;
	private static final double MAX_SAFE_SPEED = 360.0D;
	private static final double FAILURE_SPEED = 460.0D;

	private static final double MAX_WEAR = 3.0D;

	public TileEntitySawmill() {
		super(3);
	}

	@Override
	public String getName() {
		return "";
	}

	@Override
	public void updateEntity() {

		if(!worldObj.isRemote) {

			if(hasBlade) {

				int pulledHeat = tryPullHeat();
				updateBladeSpeed(pulledHeat);

				if(warnCooldown > 0)
					warnCooldown--;

				if(jamCooldown > 0) {
					jamCooldown--;
					bladeSpeed *= 0.965D;
					progress = Math.max(progress - 1, 0);
				} else {
					updateCutting();
				}

				damageEntitiesInBlade();
				updateOverspeed();

			} else {

				heat = 0;
				progress = 0;
				overspeed = 0;
				warnCooldown = 0;
				jamCooldown = 0;

				/* Broken/ejected blade means no meaningful rotor left. */
				bladeSpeed *= 0.80D;
				if(bladeSpeed < 0.05D)
					bladeSpeed = 0.0D;
			}

			sendSyncPacket(false);

		} else {

			this.lastSpin = this.spin;

			if(hasBlade) {
				float momentum = (float)(bladeSpeed * 0.10D);
				this.spin += momentum;
			}

			if(this.spin >= 360F) {
				this.spin -= 360F;
				this.lastSpin -= 360F;
			}
		}
	}

	protected void updateBladeSpeed(int pulledHeat) {

		if(pulledHeat > 0) {
			bladeSpeed += pulledHeat * HEAT_TO_BLADE_SPEED;
		}

		bladeSpeed *= BLADE_FRICTION;
		bladeSpeed -= IDLE_DRAG;

		if(bladeSpeed < 0.0D)
			bladeSpeed = 0.0D;
	}

	protected void updateCutting() {

		ItemStack input = slots[0];
		ItemStack result = getOutput(input);

		if(input == null || result == null) {
			progress = Math.max(progress - 2, 0);
			return;
		}

		if(bladeSpeed < MIN_CUT_SPEED) {

			/*
			 * Do not instantly erase a partially cut item.
			 * The cut cools/stalls/regresses slowly instead.
			 */
			progress = Math.max(progress - 2, 0);

			if(progress > 0 && worldObj.rand.nextInt(140) == 0) {
				jamCooldown = 20 + worldObj.rand.nextInt(30);
			}

			return;
		}

		if(!canCompleteOutput(input, result)) {

			/*
			 * Output is blocked. Keep the cut almost complete instead of
			 * deleting items or overwriting output slots.
			 */
			progress = Math.min(progress, processingTime - 1);
			bladeSpeed *= 0.995D;
			return;
		}

		double load = getCuttingLoad(input);
		double wearMultiplier = 1.0D + Math.min(bladeWear, MAX_WEAR) * 0.65D;

		bladeSpeed -= load * wearMultiplier;
		if(bladeSpeed < 0.0D)
			bladeSpeed = 0.0D;

		double speedEfficiency = (bladeSpeed - MIN_CUT_SPEED) / (NOMINAL_CUT_SPEED - MIN_CUT_SPEED);
		speedEfficiency = clamp(speedEfficiency, 0.10D, 2.0D);

		double wearPenalty = 1.0D / (1.0D + bladeWear * 0.75D);
		int gain = Math.max(1, (int)Math.round(2.0D + speedEfficiency * 8.0D * wearPenalty));

		progress += gain;

		/*
		 * Wear rate is intentionally small. A blade should last a while,
		 * but heavy log cutting should wear it faster than sticks/saplings.
		 */
		bladeWear += load * 0.000020D;
		if(bladeWear > MAX_WEAR)
			bladeWear = MAX_WEAR;

		updateJamChance(speedEfficiency);

		if(progress >= processingTime) {
			completeCut(input, result);
		}
	}

	protected void updateJamChance(double speedEfficiency) {

		int chance = 0;

		if(speedEfficiency < 0.25D)
			chance += 120;

		if(bladeWear > 0.75D)
			chance += 180;

		if(bladeWear > 1.50D)
			chance += 120;

		if(bladeWear > 2.25D)
			chance += 80;

		if(chance > 0 && worldObj.rand.nextInt(chance) == 0) {
			jamCooldown = 30 + worldObj.rand.nextInt(50);
			progress = Math.max(progress - 35, 0);
			bladeSpeed *= 0.65D;
			worldObj.playSoundEffect(xCoord + 0.5, yCoord + 1, zCoord + 0.5, "random.break", 0.75F, 0.7F + worldObj.rand.nextFloat() * 0.2F);
		}
	}

	protected void completeCut(ItemStack input, ItemStack result) {

		ItemStack byproduct = getByproduct(input);

		/*
		 * If the byproduct roll succeeded but slot 2 cannot accept it,
		 * do not complete the recipe. This avoids overwriting output.
		 */
		if(byproduct != null && !canAccept(slots[2], byproduct)) {
			progress = processingTime - 1;
			return;
		}

		progress = 0;

		if(slots[0] != null) {
			slots[0].stackSize--;
			if(slots[0].stackSize <= 0)
				slots[0] = null;
		}

		mergeIntoSlot(1, result);

		if(byproduct != null)
			mergeIntoSlot(2, byproduct);

		forceInventorySync = true;
		this.markDirty();
	}

	protected boolean canCompleteOutput(ItemStack input, ItemStack result) {

		if(!canAccept(slots[1], result))
			return false;

		/*
		 * For recipes that can produce sawdust, reserve byproduct space.
		 * This is conservative for chance-based byproducts, but prevents
		 * automation from silently deleting dust.
		 */
		ItemStack possibleByproduct = getPossibleByproduct(input);

		if(possibleByproduct != null && !canAccept(slots[2], possibleByproduct))
			return false;

		return true;
	}

	protected boolean canAccept(ItemStack slot, ItemStack stack) {

		if(stack == null)
			return true;

		if(slot == null)
			return true;

		if(!slot.isItemEqual(stack))
			return false;

		if(!ItemStack.areItemStackTagsEqual(slot, stack))
			return false;

		int max = Math.min(slot.getMaxStackSize(), stack.getMaxStackSize());

		return slot.stackSize + stack.stackSize <= max;
	}

	protected void mergeIntoSlot(int index, ItemStack stack) {

		if(stack == null)
			return;

		if(slots[index] == null) {
			slots[index] = stack.copy();
		} else {
			slots[index].stackSize += stack.stackSize;
		}
	}

	protected double getCuttingLoad(ItemStack input) {

		if(input == null)
			return 0.0D;

		List<String> names = ItemStackUtil.getOreDictNames(input);

		if(names.contains("logWood"))
			return 3.00D;

		if(names.contains("plankWood"))
			return 1.25D;

		if(names.contains("stickWood"))
			return 0.30D;

		if(names.contains("treeSapling"))
			return 0.75D;

		return 1.50D;
	}

	protected ItemStack getPossibleByproduct(ItemStack input) {

		if(input == null)
			return null;

		List<String> names = ItemStackUtil.getOreDictNames(input);

		if(names.contains("logWood"))
			return new ItemStack(ModItems.powder_sawdust);

		if(names.contains("plankWood"))
			return new ItemStack(ModItems.powder_sawdust);

		if(names.contains("treeSapling"))
			return new ItemStack(ModItems.powder_sawdust);

		return null;
	}

	protected ItemStack getByproduct(ItemStack input) {

		if(input == null)
			return null;

		List<String> names = ItemStackUtil.getOreDictNames(input);

		/*
		 * Logs always create kerf waste.
		 * Planks/saplings can create small incidental sawdust.
		 */
		if(names.contains("logWood"))
			return new ItemStack(ModItems.powder_sawdust);

		if(names.contains("plankWood") && worldObj.rand.nextFloat() < 0.25F)
			return new ItemStack(ModItems.powder_sawdust);

		if(names.contains("treeSapling") && worldObj.rand.nextFloat() < 0.10F)
			return new ItemStack(ModItems.powder_sawdust);

		return null;
	}

	protected void damageEntitiesInBlade() {

		if(bladeSpeed < MIN_CUT_SPEED)
			return;

		/*
		 * Smaller danger box than the old 2-block-tall column.
		 * This represents the actual exposed blade plane.
		 */
		AxisAlignedBB aabb = AxisAlignedBB.getBoundingBox(-1.000D, 0.750D, -0.750D, -0.875D, 1.625D, 0.750D);
		aabb = BlockDummyable.getAABBRotationOffset(
			aabb,
			xCoord + 0.5,
			yCoord,
			zCoord + 0.5,
			ForgeDirection.getOrientation(this.getBlockMetadata() - BlockDummyable.offset).getRotation(ForgeDirection.UP)
		);

		for(Object o : worldObj.getEntitiesWithinAABB(EntityLivingBase.class, aabb)) {

			EntityLivingBase e = (EntityLivingBase) o;

			if(!e.isEntityAlive())
				continue;

			float damage = (float)Math.min(40.0D, 4.0D + bladeSpeed / 12.0D);

			if(e.attackEntityFrom(ModDamageSource.turbofan, damage)) {

				worldObj.playSoundEffect(e.posX, e.posY, e.posZ, "mob.zombie.woodbreak", 1.5F, 0.85F + worldObj.rand.nextFloat() * 0.25F);

				int count = Math.min((int)Math.ceil(e.getMaxHealth() / 6), 160);

				NBTTagCompound data = new NBTTagCompound();
				data.setString("type", "vanillaburst");
				data.setInteger("count", count * 4);
				data.setDouble("motion", 0.075D);
				data.setString("mode", "blockdust");
				data.setInteger("block", Block.getIdFromBlock(Blocks.redstone_block));

				PacketDispatcher.wrapper.sendToAllAround(
					new AuxParticlePacketNT(data, e.posX, e.posY + e.height * 0.5, e.posZ),
					new TargetPoint(e.dimension, e.posX, e.posY, e.posZ, 50)
				);
			}
		}
	}

	protected void updateOverspeed() {

		if(bladeSpeed > MAX_SAFE_SPEED) {

			this.overspeed++;

			if(overspeed > 60 && warnCooldown == 0) {
				warnCooldown = 100;
				//worldObj.playSoundEffect(xCoord + 0.5, yCoord + 1, zCoord + 0.5, "hbm:block.warnOverspeed", 2.0F, 1.0F);
			}

			if(bladeSpeed > FAILURE_SPEED || overspeed > 300) {
				failBlade();
			}

		} else {
			this.overspeed = Math.max(this.overspeed - 2, 0);
		}
	}

	protected void failBlade() {

		this.hasBlade = false;
		this.progress = 0;
		this.overspeed = 0;
		this.jamCooldown = 0;

		worldObj.playSoundEffect(xCoord + 0.5, yCoord + 1, zCoord + 0.5, "random.break", 2.0F, 0.6F + worldObj.rand.nextFloat() * 0.2F);

		int orientation = this.getBlockMetadata() - BlockDummyable.offset;
		ForgeDirection dir = ForgeDirection.getOrientation(orientation);
		ForgeDirection rot = dir.getRotation(ForgeDirection.DOWN);

		EntitySawblade cog = new EntitySawblade(
			worldObj,
			xCoord + 0.5 + dir.offsetX,
			yCoord + 1,
			zCoord + 0.5 + dir.offsetZ
		).setOrientation(orientation);

		double ejection = 0.75D + Math.min((bladeSpeed - MAX_SAFE_SPEED) / 100.0D, 3.0D);

		cog.motionX = rot.offsetX * ejection;
		cog.motionY = 0.35D + ejection * 0.20D;
		cog.motionZ = rot.offsetZ * ejection;

		worldObj.spawnEntityInWorld(cog);

		/*
		 * Small non-block-breaking burst for feedback.
		 * This is not a TNT-style detonation; it is mechanical failure.
		 */
		this.worldObj.newExplosion(null, xCoord + 0.5, yCoord + 1, zCoord + 0.5, 1.25F, false, false);

		forceInventorySync = true;
		this.markDirty();
	}

	protected int tryPullHeat() {

		TileEntity con = worldObj.getTileEntity(xCoord, yCoord - 1, zCoord);

		if(con instanceof IHeatSource) {

			IHeatSource source = (IHeatSource) con;
			int heatSrc = (int)(source.getHeatStored() * diffusion);
			int heatDemand = getHeatDemand();

			heatSrc = Math.min(heatSrc, heatDemand);

			if(heatSrc > MAX_HEAT_DRAW)
				heatSrc = MAX_HEAT_DRAW;

			if(heatSrc > 0) {
				source.useUpHeat(heatSrc);
				this.heat = heatSrc;
				return heatSrc;
			}
		}

		this.heat = 0;
		return 0;
	}

	protected int getHeatDemand() {

		/*
		 * The sawmill is a Stirling-driven machine, not a raw heat dump.
		 * Large heat buffers from fireboxes, ovens, and electric heaters used to
		 * force the saw to pull the full transfer cap every tick. Even the lowest
		 * electric heater setting could therefore push the blade past failure speed
		 * after it had warmed the heater buffer for a short time.
		 *
		 * Pull only the heat needed to approach the useful operating band. Cutting
		 * load still makes the saw draw more heat, but idle or lightly-loaded saws
		 * stop accepting heat before overspeeding.
		 */
		double targetSpeed = NOMINAL_CUT_SPEED;

		if(slots[0] != null && getOutput(slots[0]) != null)
			targetSpeed = MAX_SAFE_SPEED - 20.0D;

		double speedDeficit = targetSpeed - bladeSpeed;

		if(speedDeficit <= 0.0D)
			return 0;

		int demand = (int)Math.ceil(speedDeficit / HEAT_TO_BLADE_SPEED);

		return Math.min(demand, MAX_HEAT_DRAW);
	}

	protected void sendSyncPacket(boolean forceFull) {

		syncCooldown++;

		boolean doFull = forceFull || forceInventorySync || syncCooldown >= FULL_SYNC_INTERVAL;
		boolean doSmall = syncCooldown >= SYNC_INTERVAL;

		if(!doFull && !doSmall)
			return;

		NBTTagCompound data = new NBTTagCompound();
		data.setInteger("heat", heat);
		data.setInteger("progress", progress);
		data.setBoolean("hasBlade", hasBlade);
		data.setDouble("bladeSpeed", bladeSpeed);
		data.setDouble("bladeWear", bladeWear);
		data.setInteger("jamCooldown", jamCooldown);

		if(doFull) {

			NBTTagList list = new NBTTagList();

			for(int i = 0; i < slots.length; i++) {
				if(slots[i] != null) {
					NBTTagCompound nbt1 = new NBTTagCompound();
					nbt1.setByte("slot", (byte)i);
					slots[i].writeToNBT(nbt1);
					list.appendTag(nbt1);
				}
			}

			data.setTag("items", list);
			forceInventorySync = false;
		}

		INBTPacketReceiver.networkPack(this, data, 150);
		syncCooldown = 0;
	}

	@Override
	public void networkUnpack(NBTTagCompound nbt) {

		this.heat = nbt.getInteger("heat");
		this.progress = nbt.getInteger("progress");
		this.hasBlade = nbt.getBoolean("hasBlade");
		this.bladeSpeed = nbt.getDouble("bladeSpeed");
		this.bladeWear = nbt.getDouble("bladeWear");
		this.jamCooldown = nbt.getInteger("jamCooldown");

		if(nbt.hasKey("items")) {

			NBTTagList list = nbt.getTagList("items", 10);

			slots = new ItemStack[3];

			for(int i = 0; i < list.tagCount(); i++) {
				NBTTagCompound nbt1 = list.getCompoundTagAt(i);
				byte b0 = nbt1.getByte("slot");

				if(b0 >= 0 && b0 < slots.length) {
					slots[b0] = ItemStack.loadItemStackFromNBT(nbt1);
				}
			}
		}
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {

		super.readFromNBT(nbt);

		this.hasBlade = !nbt.hasKey("hasBlade") || nbt.getBoolean("hasBlade");
		this.progress = nbt.getInteger("progress");
		this.bladeSpeed = nbt.getDouble("bladeSpeed");
		this.bladeWear = nbt.getDouble("bladeWear");
		this.jamCooldown = nbt.getInteger("jamCooldown");
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) {

		super.writeToNBT(nbt);

		nbt.setBoolean("hasBlade", hasBlade);
		nbt.setInteger("progress", progress);
		nbt.setDouble("bladeSpeed", bladeSpeed);
		nbt.setDouble("bladeWear", bladeWear);
		nbt.setInteger("jamCooldown", jamCooldown);
	}

	protected InventoryCraftingAuto craftingInventory = new InventoryCraftingAuto(1, 1);

	@Override
	public boolean isItemValidForSlot(int i, ItemStack stack) {
		return i == 0 && slots[0] == null && slots[1] == null && slots[2] == null && stack.stackSize == 1 && getOutput(stack) != null;
	}

	@Override
	public boolean canExtractItem(int i, ItemStack itemStack, int j) {
		return i > 0;
	}

	@Override
	public int[] getAccessibleSlotsFromSide(int side) {
		return new int[] {0, 1, 2};
	}

	public ItemStack getOutput(ItemStack input) {

		if(input == null)
			return null;

		craftingInventory.setInventorySlotContents(0, input);

		List<String> names = ItemStackUtil.getOreDictNames(input);

		if(names.contains("stickWood")) {
			return new ItemStack(ModItems.powder_sawdust);
		}

		if(names.contains("logWood")) {

			for(Object o : CraftingManager.getInstance().getRecipeList()) {

				IRecipe recipe = (IRecipe)o;

				if(recipe.matches(craftingInventory, worldObj)) {

					ItemStack out = recipe.getCraftingResult(craftingInventory);

					if(out != null) {

						out = out.copy();

						/*
						 * Realistic-ish sawmill recovery:
						 * vanilla log -> 4 planks.
						 * sawmill log -> 5 planks + guaranteed sawdust.
						 *
						 * If you want the old gameplay upgrade, change 5 to 6.
						 */
						out.stackSize = out.stackSize * 5 / 4;

						if(out.stackSize < 1)
							out.stackSize = 1;

						return out;
					}
				}
			}
		}

		if(names.contains("plankWood")) {
			return new ItemStack(Items.stick, 3);
		}

		if(names.contains("treeSapling")) {
			return new ItemStack(Items.stick, 1);
		}

		return null;
	}

	public static HashMap getRecipes() {

		HashMap<Object, Object[]> recipes = new HashMap<Object, Object[]>();

		recipes.put(
			new OreDictStack("logWood"),
			new ItemStack[] {
				new ItemStack(Blocks.planks, 5),
				new ItemStack(ModItems.powder_sawdust)
			}
		);

		recipes.put(
			new OreDictStack("plankWood"),
			new ItemStack[] {
				new ItemStack(Items.stick, 3),
				ItemStackUtil.addTooltipToStack(new ItemStack(ModItems.powder_sawdust), EnumChatFormatting.RED + "25%")
			}
		);

		recipes.put(
			new OreDictStack("stickWood"),
			new ItemStack[] {
				new ItemStack(ModItems.powder_sawdust)
			}
		);

		recipes.put(
			new OreDictStack("treeSapling"),
			new ItemStack[] {
				new ItemStack(Items.stick, 1),
				ItemStackUtil.addTooltipToStack(new ItemStack(ModItems.powder_sawdust), EnumChatFormatting.RED + "10%")
			}
		);

		return recipes;
	}

	protected double clamp(double val, double min, double max) {

		if(val < min)
			return min;

		if(val > max)
			return max;

		return val;
	}

	AxisAlignedBB bb = null;

	@Override
	public AxisAlignedBB getRenderBoundingBox() {

		if(bb == null) {
			bb = AxisAlignedBB.getBoundingBox(
				xCoord - 1,
				yCoord,
				zCoord - 1,
				xCoord + 2,
				yCoord + 2,
				zCoord + 2
			);
		}

		return bb;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public double getMaxRenderDistanceSquared() {
		return 65536.0D;
	}
}
