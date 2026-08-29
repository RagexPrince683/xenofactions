package com.hbm.tileentity.machine;

import java.util.ArrayList;
import java.util.List;

import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.machine.MachineITER;
import com.hbm.handler.CompatHandler;
import com.hbm.inventory.container.ContainerITER;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.inventory.fluid.trait.FT_Heatable;
import com.hbm.inventory.fluid.trait.FT_Heatable.HeatingStep;
import com.hbm.inventory.gui.GUIITER;
import com.hbm.inventory.recipes.BreederRecipes;
import com.hbm.inventory.recipes.BreederRecipes.BreederRecipe;
import com.hbm.inventory.recipes.FusionRecipes;
import com.hbm.items.ModItems;
import com.hbm.items.special.ItemFusionShield;
import com.hbm.lib.Library;
import com.hbm.main.MainRegistry;
import com.hbm.sound.AudioWrapper;
import com.hbm.tileentity.IFluidCopiable;
import com.hbm.tileentity.IGUIProvider;
import com.hbm.tileentity.TileEntityMachineBase;
import com.hbm.util.CompatEnergyControl;
import com.hbm.util.fauxpointtwelve.DirPos;

import api.hbm.energymk2.IEnergyReceiverMK2;
import api.hbm.fluid.IFluidStandardTransceiver;
import api.hbm.tile.IInfoProviderEC;
import cpw.mods.fml.common.Optional;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.SimpleComponent;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

@Optional.InterfaceList({
	@Optional.Interface(iface = "li.cil.oc.api.network.SimpleComponent", modid = "OpenComputers")
})
public class TileEntityITER extends TileEntityMachineBase implements IEnergyReceiverMK2, IFluidStandardTransceiver, IGUIProvider, IInfoProviderEC, SimpleComponent, CompatHandler.OCComponent, IFluidCopiable {

	public long power;
	public static final long maxPower = 10000000;
	public static final int powerReq = 100000;

	public FluidTank[] tanks;
	public FluidTank plasma;

	public static final int CoolReq = 1;

	public int progress;
	public static final int duration = 100;
	public long totalRuntime;

	/*
	 * Realism changes:
	 *
	 * - Magnet/power loss causes plasma disruption, not an instant nuclear explosion.
	 * - No shield causes plasma loss/shutdown, not a magic plasma bomb.
	 * - Coolant failure builds heat stress and damages the blanket/first wall.
	 * - Actual explosion only happens from sustained structural/coolant failure.
	 * - ITER outputs hot coolant as the primary thermal product.
	 * - It no longer creates free water/coolant when output tanks are full.
	 */
	private int heatStress;
	private double breedingProgress;

	private static final int MAX_HEAT_STRESS = 600;
	private static final int HEAT_STRESS_PER_FAILED_COOLING = 20;
	private static final int HEAT_STRESS_DECAY = 4;

	private static final int BASE_SHIELD_DAMAGE_PER_TICK = 1;
	private static final int DISRUPTION_SHIELD_DAMAGE = 75;
	private static final int OVERHEAT_SHIELD_DAMAGE = 25;

	private static final int MAX_PLASMA_BURN_PER_TICK = 20;

	/*
	 * Fusion heat model:
	 *
	 * The ITER itself does not output electricity.
	 * It outputs useful thermal energy as hot coolant.
	 *
	 * FusionRecipes.getSteamProduction() is treated as the useful thermal yield
	 * per plasma packet. Shield heat efficiency improves how much of that neutron
	 * heat gets captured by the blanket/coolant loop.
	 */
	//private static final int MIN_HOT_COOLANT_PER_PLASMA = 1;
	//private static final int MAX_HOT_COOLANT_PER_PLASMA = 1000;

	@SideOnly(Side.CLIENT)
	public int blanket;

	public float rotor;
	public float lastRotor;
	public boolean isOn;

	private float rotorSpeed = 0F;

	private AudioWrapper audio;

	//GUI helpers:

	public boolean hasValidShield() {
		return getShield() > 0;
	}

	public boolean isTemperatureSafe() {

		if(!hasValidShield()) {
			return false;
		}

		return getShield() >= getRequiredShieldTemperature();
	}

	public boolean hasCoolingAvailable() {
		return tanks[2].getFill() > 0 && tanks[3].getFill() < tanks[3].getMaxFill();
	}

	public boolean hasEnoughPowerForMagnets() {
		return power >= getActualPowerReq();
	}

	public boolean areMagnetsPowered() {
		return isOn && hasEnoughPowerForMagnets();
	}

	public TileEntityITER() {

		super(6);

		tanks = new FluidTank[4];

		/*
		 * Tank 0 remains WATER for compatibility/input UI, but this reactor no longer
		 * directly boils water. Realistic fusion heat should go through coolant/blanket.
		 */
		tanks[0] = new FluidTank(Fluids.WATER, 1280000);

		/*
		 * Tank 1 remains ULTRAHOTSTEAM for old compatibility, but normal operation will
		 * not fill it anymore. Use a heat exchanger/turbine chain from hot coolant.
		 */
		tanks[1] = new FluidTank(Fluids.ULTRAHOTSTEAM, 128000);

		tanks[2] = new FluidTank(Fluids.COOLANT, 16_000);
		tanks[3] = new FluidTank(Fluids.COOLANT_HOT, 16_000);

		plasma = new FluidTank(Fluids.PLASMA_DT, 16000);
	}

	@Override
	public String getName() {
		return "container.machineITER";
	}

	private ItemFusionShield getShieldItem() {

		if(slots[3] == null || !(slots[3].getItem() instanceof ItemFusionShield)) {
			return null;
		}

		return (ItemFusionShield) slots[3].getItem();
	}

	private double getShieldHeatEfficiency() {

		ItemFusionShield shield = getShieldItem();

		if(shield == null) {
			return 0.0D;
		}

		return shield.heatEfficiency;
	}

	private double getShieldBreedingEfficiency() {

		ItemFusionShield shield = getShieldItem();

		if(shield == null) {
			return 0.0D;
		}

		return shield.breedingEfficiency;
	}

	private double getShieldPowerDrainMultiplier() {

		ItemFusionShield shield = getShieldItem();

		if(shield == null) {
			return 2.0D;
		}

		return shield.powerDrainMultiplier;
	}
	public int getActualPowerReq() {

		double mult = getShieldPowerDrainMultiplier();

		if(mult <= 0.0D) {
			mult = 1.0D;
		}

		return Math.max(1, (int)Math.ceil(powerReq * mult));
	}

	public int getActualCoolantReq() {

		int coolantReq = FusionRecipes.getCoolant(plasma.getTankType());

		if(coolantReq <= 0) {
			coolantReq = 1;
		}

		return Math.max(1, coolantReq);
	}



	public int getRequiredShieldTemperature() {

		if(plasma == null || plasma.getTankType() == null || plasma.getTankType() == Fluids.NONE) {
			return 0;
		}

		if(plasma.getTankType() == Fluids.PLASMA_DT) {
			return 1200; // beryllium-safe
		}

		if(plasma.getTankType() == Fluids.PLASMA_HD) {
			return 1000;
		}

		if(plasma.getTankType() == Fluids.PLASMA_HT) {
			return 1100;
		}

		if(plasma.getTankType() == Fluids.PLASMA_DH3) {
			return 1600;
		}

		if(plasma.getTankType() == Fluids.PLASMA_XM) {
			return 3000;
		}

		if(plasma.getTankType() == Fluids.PLASMA_BF) {
			return 5000;
		}

		return 1500;
	}


	@Override
	public void updateEntity() {

		if(!worldObj.isRemote) {

			this.updateConnections();

			power = Library.chargeTEFromItems(slots, 0, power, maxPower);

			updateHotCoolantType();

			/// START Processing part ///

			if(!isOn && plasma.getFill() > 0) {
				disruptPlasma(false, 0.75F);
			}

			if(plasma.getFill() > 0 && getShield() <= 0) {
				disruptPlasma(false, 0.5F);
			}

			if(plasma.getFill() > 0 && !isTemperatureSafe()) {
				damageShield(OVERHEAT_SHIELD_DAMAGE);
				heatStress += HEAT_STRESS_PER_FAILED_COOLING;

				if(heatStress >= MAX_HEAT_STRESS) {
					structuralFailure();
					return;
				}
			}

			if(isOn) {

				int actualPowerReq = getActualPowerReq();

				if(power < actualPowerReq) {

					/*
					 * Realistic behavior:
					 * loss of magnet/auxiliary power kills confinement. The plasma dumps
					 * into the wall and quenches. It damages the machine, but it is not a
					 * nuclear explosion.
					 */
					if(plasma.getFill() > 0) {
						disruptPlasma(true, 0.65F);
					}

					isOn = false;
				} else {

					power -= actualPowerReq;

					if(plasma.getFill() > 0) {

						this.totalRuntime++;

						int delay = FusionRecipes.getByproductDelay(plasma.getTankType());

						if(delay > 0 && totalRuntime % delay == 0) {
							produceByproduct();
						}

						runFusionTick();
					} else {
						coolDownHeatStress();
					}
				}
			} else {
				coolDownHeatStress();
			}

			doBreederStuff();

			/// END Processing part ///

			/// START Fluid output ///

			for(DirPos pos : getConPos()) {

				if(tanks[1].getFill() > 0) {
					this.sendFluid(tanks[1], worldObj, pos.getX(), pos.getY(), pos.getZ(), pos.getDir());
				}

				if(tanks[3].getFill() > 0) {
					this.sendFluid(tanks[3], worldObj, pos.getX(), pos.getY(), pos.getZ(), pos.getDir());
				}
			}

			/// END Fluid output ///

			/// START Notif packets ///

			NBTTagCompound data = new NBTTagCompound();

			data.setBoolean("isOn", isOn);
			data.setLong("power", power);
			data.setInteger("progress", progress);
			data.setInteger("heatStress", heatStress);

			tanks[0].writeToNBT(data, "water");
			tanks[1].writeToNBT(data, "steam");
			tanks[2].writeToNBT(data, "coolant");
			tanks[3].writeToNBT(data, "hotlant");
			plasma.writeToNBT(data, "plasma");

			if(slots[3] == null) {
				data.setInteger("blanket", 0);
			} else if(slots[3].getItem() == ModItems.fusion_shield_tungsten) {
				data.setInteger("blanket", 1);
			} else if(slots[3].getItem() == ModItems.fusion_shield_beryllium) {
				data.setInteger("blanket", 2);
			} else if(slots[3].getItem() == ModItems.fusion_shield_desh) {
				data.setInteger("blanket", 3);
			} else if(slots[3].getItem() == ModItems.fusion_shield_chlorophyte) {
				data.setInteger("blanket", 4);
			} else if(slots[3].getItem() == ModItems.fusion_shield_vaporwave) {
				data.setInteger("blanket", 5);
			}

			this.networkPack(data, 250);

			/// END Notif packets ///

		} else {

			this.lastRotor = this.rotor;
			this.rotor += this.rotorSpeed;

			if(this.rotor >= 360) {
				this.rotor -= 360;
				this.lastRotor -= 360;
			}

			//if(this.isOn && this.power >= powerReq) {
			if(this.isOn && this.power >= getActualPowerReq()) {

				this.rotorSpeed = Math.max(0F, Math.min(15F, this.rotorSpeed + 0.05F));

				if(audio == null) {
					audio = MainRegistry.proxy.getLoopedSound("hbm:block.fusionReactorRunning", xCoord, yCoord, zCoord, 1.0F, 30F, 1.0F);
					audio.startSound();
				}

				float rotorSpeed = this.rotorSpeed / 15F;
				audio.updateVolume(getVolume(0.5F * rotorSpeed));
				audio.updatePitch(0.25F + 0.75F * rotorSpeed);

			} else {

				this.rotorSpeed = Math.max(0F, Math.min(15F, this.rotorSpeed - 0.1F));

				if(audio != null) {

					if(this.rotorSpeed > 0) {

						float rotorSpeed = this.rotorSpeed / 15F;
						audio.updateVolume(getVolume(0.5F * rotorSpeed));
						audio.updatePitch(0.25F + 0.75F * rotorSpeed);

					} else {

						audio.stopSound();
						audio = null;
					}
				}
			}
		}
	}

	private void updateHotCoolantType() {

		if(tanks[2].getTankType().hasTrait(FT_Heatable.class)) {

			FT_Heatable trait = tanks[2].getTankType().getTrait(FT_Heatable.class);
			HeatingStep step = trait.getFirstStep();

			if(step != null && step.typeProduced != null) {
				tanks[3].setTankType(step.typeProduced);
			}

		} else {

			tanks[2].setTankType(Fluids.NONE);
			tanks[3].setTankType(Fluids.NONE);
		}
	}

	private void runFusionTick() {

		if(plasma.getFill() <= 0) {
			return;
		}

		if(getShield() <= 0) {
			disruptPlasma(false, 0.5F);
			return;
		}

		int coolantReq = getActualCoolantReq();

		if(coolantReq <= 0) {
			coolantReq = 1;
		}

		int operations = Math.min(MAX_PLASMA_BURN_PER_TICK, plasma.getFill());

		for(int i = 0; i < operations; i++) {

			if(plasma.getFill() <= 0) {
				break;
			}

			if(tanks[2].getFill() < coolantReq) {

				heatStress += HEAT_STRESS_PER_FAILED_COOLING;
				damageShield(OVERHEAT_SHIELD_DAMAGE);

				if(heatStress >= MAX_HEAT_STRESS) {
					structuralFailure();
					return;
				}

				break;
			}

			int freeHotCoolantSpace = tanks[3].getMaxFill() - tanks[3].getFill();

			if(freeHotCoolantSpace < coolantReq) {

				heatStress += HEAT_STRESS_PER_FAILED_COOLING;
				damageShield(OVERHEAT_SHIELD_DAMAGE);

				if(heatStress >= MAX_HEAT_STRESS) {
					structuralFailure();
					return;
				}

				break;
			}

			/*
			 * Mass-conserving coolant loop:
			 *
			 * 1 mB cold coolant -> 1 mB hot coolant.
			 *
			 * Fusion energy should be represented by the value/temperature/turbine yield
			 * of the hot coolant, not by creating extra coolant volume.
			 */
			tanks[2].setFill(tanks[2].getFill() - coolantReq);
			tanks[3].setFill(tanks[3].getFill() + coolantReq);

			plasma.setFill(plasma.getFill() - 1);

			damageShield(BASE_SHIELD_DAMAGE_PER_TICK);

			coolDownHeatStress();
		}
	}

	private void coolDownHeatStress() {

		if(heatStress > 0) {
			heatStress -= HEAT_STRESS_DECAY;

			if(heatStress < 0) {
				heatStress = 0;
			}
		}
	}

	private void damageShield(int amount) {

		if(amount <= 0) {
			return;
		}

		if(slots[3] == null || !(slots[3].getItem() instanceof ItemFusionShield)) {
			return;
		}

		ItemFusionShield shield = (ItemFusionShield) slots[3].getItem();

		ItemFusionShield.setShieldDamage(
			slots[3],
			ItemFusionShield.getShieldDamage(slots[3]) + amount
		);

		if(ItemFusionShield.getShieldDamage(slots[3]) > shield.maxDamage) {

			slots[3] = null;

			worldObj.playSoundEffect(
				xCoord + 0.5,
				yCoord + 0.5,
				zCoord + 0.5,
				"hbm:block.shutdown",
				5F,
				1F
			);

			this.isOn = false;
			this.markDirty();
		}
	}

	private void disruptPlasma(boolean major, float pitch) {

		if(plasma.getFill() <= 0) {
			return;
		}

		int damage = major ? DISRUPTION_SHIELD_DAMAGE : DISRUPTION_SHIELD_DAMAGE / 3;

		damageShield(damage);

		heatStress += major ? 120 : 40;

		plasma.setFill(0);
		isOn = false;

		worldObj.playSoundEffect(
			xCoord + 0.5,
			yCoord + 0.5,
			zCoord + 0.5,
			"hbm:block.shutdown",
			5F,
			pitch
		);

		if(heatStress >= MAX_HEAT_STRESS) {
			structuralFailure();
		}
	}

	private void structuralFailure() {

		this.disassemble();

		Vec3 vec = Vec3.createVectorHelper(5.5, 0, 0);
		vec.rotateAroundY(worldObj.rand.nextFloat() * (float) Math.PI * 2F);

		/*
		 * This is no longer treated as "fusion plasma exploded".
		 * It represents coolant/steam/structural failure after the first wall or
		 * blanket is cooked past safe limits.
		 */
		worldObj.newExplosion(
			null,
			xCoord + 0.5 + vec.xCoord,
			yCoord + 0.5 + worldObj.rand.nextGaussian() * 1.5D,
			zCoord + 0.5 + vec.zCoord,
			2.5F,
			true,
			true
		);
	}

	protected List<DirPos> connections;

	private void updateConnections() {

		for(DirPos pos : getConPos()) {

			this.trySubscribe(worldObj, pos.getX(), pos.getY(), pos.getZ(), pos.getDir());

			this.trySubscribe(tanks[0].getTankType(), worldObj, pos.getX(), pos.getY(), pos.getZ(), pos.getDir());
			this.trySubscribe(tanks[2].getTankType(), worldObj, pos.getX(), pos.getY(), pos.getZ(), pos.getDir());
		}
	}

	protected List<DirPos> getConPos() {

		if(connections != null && !connections.isEmpty()) {
			return connections;
		}

		connections = new ArrayList<DirPos>();

		connections.add(new DirPos(xCoord, yCoord + 3, zCoord, ForgeDirection.UP));
		connections.add(new DirPos(xCoord, yCoord - 3, zCoord, ForgeDirection.DOWN));

		Vec3 vec = Vec3.createVectorHelper(5.75, 0, 0);

		for(int i = 0; i < 16; i++) {

			vec.rotateAroundY((float) (Math.PI / 8));

			connections.add(new DirPos(xCoord + (int) vec.xCoord, yCoord + 3, zCoord + (int) vec.zCoord, ForgeDirection.UP));
			connections.add(new DirPos(xCoord + (int) vec.xCoord, yCoord - 3, zCoord + (int) vec.zCoord, ForgeDirection.DOWN));
		}

		return connections;
	}

	private void doBreederStuff() {

		if(plasma.getFill() == 0) {
			this.progress = 0;
			return;
		}

		int level = FusionRecipes.getBreedingLevel(plasma.getTankType());

		BreederRecipe out = BreederRecipes.getOutput(slots[1], level);

		if(out == null) {
			this.progress = 0;
			return;
		}

		if(slots[2] != null && slots[2].stackSize >= slots[2].getMaxStackSize()) {
			this.progress = 0;
			return;
		}

		breedingProgress += Math.max(0.0D, getShieldBreedingEfficiency());

		while(breedingProgress >= 1.0D) {
			progress++;
			breedingProgress -= 1.0D;
		}

		if(progress >= this.duration) {

			this.progress = 0;

			if(slots[2] != null) {
				slots[2].stackSize++;
			} else {
				slots[2] = out.output.copy();
			}

			slots[1].stackSize--;

			if(slots[1].stackSize <= 0) {
				slots[1] = null;
			}

			this.markDirty();
		}
	}

	@Override
	public boolean canExtractItem(int i, ItemStack itemStack, int j) {
		return true;
	}

	@Override
	public int[] getAccessibleSlotsFromSide(int p_94128_1_) {
		return new int[] { 1, 2, 4 };
	}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack itemStack) {

		if(itemStack == null) {
			return false;
		}

		// Battery / power item slot.
		// Leave broad because Library.chargeTEFromItems handles whether it can actually charge.
		if(i == 0) {
			return true;
		}

		// Breeder input.
		if(i == 1 && BreederRecipes.hasRecipe(itemStack)) {
			return true;
		}

		// Breeder output: no insertion.
		if(i == 2) {
			return false;
		}

		// Fusion shield / first wall / blanket slot.
		if(i == 3 && itemStack.getItem() instanceof ItemFusionShield) {
			return true;
		}

		// Byproduct output: no insertion.
		if(i == 4) {
			return false;
		}

		// Extra/upgrade/compat slot.
		if(i == 5) {
			return true;
		}

		return false;
	}

	private void produceByproduct() {

		ItemStack by = FusionRecipes.getByproduct(plasma.getTankType());

		if(by == null) {
			return;
		}

		if(slots[4] == null) {
			slots[4] = by.copy();
			return;
		}

		if(slots[4].getItem() == by.getItem() && slots[4].getItemDamage() == by.getItemDamage() && slots[4].stackSize < slots[4].getMaxStackSize()) {
			slots[4].stackSize++;
		}
	}

	public int getShield() {

		if(slots[3] == null || !(slots[3].getItem() instanceof ItemFusionShield)) {
			return 0;
		}

		return ((ItemFusionShield) slots[3].getItem()).maxTemp;
	}

	@Override
	public void networkUnpack(NBTTagCompound data) {

		super.networkUnpack(data);

		this.isOn = data.getBoolean("isOn");
		this.power = data.getLong("power");
		this.blanket = data.getInteger("blanket");
		this.progress = data.getInteger("progress");
		this.heatStress = data.getInteger("heatStress");

		tanks[0].readFromNBT(data, "water");
		tanks[1].readFromNBT(data, "steam");
		tanks[2].readFromNBT(data, "coolant");
		tanks[3].readFromNBT(data, "hotlant");
		plasma.readFromNBT(data, "plasma");
	}

	@Override
	public void handleButtonPacket(int value, int meta) {

		if(meta == 0) {

			if(!this.isOn) {

				/*
				 * Refuse startup without a valid first wall/blanket.
				 * This avoids shieldless plasma causing instant weird explosions.
				 */
				if(getShield() <= 0) {
					worldObj.playSoundEffect(xCoord + 0.5, yCoord + 0.5, zCoord + 0.5, "hbm:block.shutdown", 5F, 0.5F);
					this.isOn = false;
					return;
				}

				if(tanks[2].getFill() <= 0 || tanks[3].getFill() >= tanks[3].getMaxFill()) {
					worldObj.playSoundEffect(xCoord + 0.5, yCoord + 0.5, zCoord + 0.5, "hbm:block.shutdown", 5F, 0.5F);
					this.isOn = false;
					return;
				}
			}

			this.isOn = !this.isOn;
		}
	}

	public long getPowerScaled(long i) {
		return (power * i) / maxPower;
	}

	public long getProgressScaled(long i) {
		return (progress * i) / duration;
	}

	public long getHeatStressScaled(long i) {
		return (heatStress * i) / MAX_HEAT_STRESS;
	}

	@Override
	public void setPower(long i) {
		this.power = i;
	}

	@Override
	public long getPower() {
		return power;
	}

	@Override
	public long getMaxPower() {
		return maxPower;
	}

	@Override
	public void onChunkUnload() {

		super.onChunkUnload();

		if(audio != null) {
			audio.stopSound();
			audio = null;
		}
	}

	@Override
	public void invalidate() {

		super.invalidate();

		if(audio != null) {
			audio.stopSound();
			audio = null;
		}
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {

		super.readFromNBT(nbt);

		this.power = nbt.getLong("power");
		this.isOn = nbt.getBoolean("isOn");
		this.totalRuntime = nbt.getLong("totalRuntime");
		this.heatStress = nbt.getInteger("heatStress");
		this.breedingProgress = nbt.getDouble("breedingProgress");

		tanks[0].readFromNBT(nbt, "water");
		tanks[1].readFromNBT(nbt, "steam");
		tanks[2].readFromNBT(nbt, "coolant");
		tanks[3].readFromNBT(nbt, "hotlant");
		plasma.readFromNBT(nbt, "plasma");
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) {

		super.writeToNBT(nbt);

		nbt.setLong("power", this.power);
		nbt.setBoolean("isOn", isOn);
		nbt.setLong("totalRuntime", this.totalRuntime);
		nbt.setInteger("heatStress", this.heatStress);
		nbt.setDouble("breedingProgress", this.breedingProgress);

		tanks[0].writeToNBT(nbt, "water");
		tanks[1].writeToNBT(nbt, "steam");
		tanks[2].writeToNBT(nbt, "coolant");
		tanks[3].writeToNBT(nbt, "hotlant");
		plasma.writeToNBT(nbt, "plasma");
	}

	AxisAlignedBB bb = null;

	@Override
	public AxisAlignedBB getRenderBoundingBox() {

		if(bb == null) {

			bb = AxisAlignedBB.getBoundingBox(
				xCoord + 0.5 - 8,
				yCoord + 0.5 - 3,
				zCoord + 0.5 - 8,
				xCoord + 0.5 + 8,
				yCoord + 0.5 + 3,
				zCoord + 0.5 + 8
			);
		}

		return bb;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public double getMaxRenderDistanceSquared() {
		return 65536.0D;
	}

	public void disassemble() {

		MachineITER.drop = false;

		int[][][] layout = TileEntityITERStruct.layout;

		for(int y = 0; y < 5; y++) {
			for(int x = 0; x < layout[0].length; x++) {
				for(int z = 0; z < layout[0][0].length; z++) {

					int ly = y > 2 ? 4 - y : y;

					int width = 7;

					if(x == width && y == 0 && z == width) {
						continue;
					}

					int b = layout[ly][x][z];

					switch(b) {
						case 1:
							worldObj.setBlock(xCoord - width + x, yCoord + y - 2, zCoord - width + z, ModBlocks.fusion_conductor, 1, 3);
							break;
						case 2:
							worldObj.setBlock(xCoord - width + x, yCoord + y - 2, zCoord - width + z, ModBlocks.fusion_center);
							break;
						case 3:
							worldObj.setBlock(xCoord - width + x, yCoord + y - 2, zCoord - width + z, ModBlocks.fusion_motor);
							break;
						case 4:
							worldObj.setBlock(xCoord - width + x, yCoord + y - 2, zCoord - width + z, ModBlocks.reinforced_glass);
							break;
					}
				}
			}
		}

		worldObj.setBlock(xCoord, yCoord - 2, zCoord, ModBlocks.struct_iter_core);

		MachineITER.drop = true;

		List<EntityPlayer> players = worldObj.getEntitiesWithinAABB(
			EntityPlayer.class,
			AxisAlignedBB.getBoundingBox(
				xCoord + 0.5,
				yCoord + 0.5,
				zCoord + 0.5,
				xCoord + 0.5,
				yCoord + 0.5,
				zCoord + 0.5
			).expand(50, 10, 50)
		);

		for(EntityPlayer player : players) {
			player.triggerAchievement(MainRegistry.achMeltdown);
		}
	}

	@Override
	public FluidTank[] getSendingTanks() {
		return new FluidTank[] { tanks[1], tanks[3] };
	}

	@Override
	public FluidTank[] getReceivingTanks() {
		return new FluidTank[] { tanks[0], tanks[2] };
	}

	@Override
	public FluidTank[] getAllTanks() {
		return tanks;
	}

	@Override
	public boolean canConnect(ForgeDirection dir) {
		return dir == ForgeDirection.UP || dir == ForgeDirection.DOWN;
	}

	@Override
	public boolean canConnect(FluidType type, ForgeDirection dir) {
		return dir == ForgeDirection.UP || dir == ForgeDirection.DOWN;
	}

	@Override
	public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new ContainerITER(player.inventory, this);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Object provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new GUIITER(player.inventory, this);
	}

	public int getFusionHeatValue() {

		int heat = FusionRecipes.getSteamProduction(plasma.getTankType());

		if(heat <= 0) {
			heat = 1;
		}

		double heatEfficiency = getShieldHeatEfficiency();

		if(heatEfficiency <= 0.0D) {
			heatEfficiency = 1.0D;
		}

		return Math.max(1, (int) Math.ceil(heat * heatEfficiency));
	}

	@Override
	public void provideExtraInfo(NBTTagCompound data) {

		data.setBoolean(CompatEnergyControl.B_ACTIVE, this.isOn && plasma.getFill() > 0);

		int coolant = getActualCoolantReq();
		int hotCoolant = getFusionHeatValue();

		data.setDouble("consumption", coolant);
		data.setDouble("outputmb", hotCoolant);
		data.setInteger("heatStress", heatStress);
		data.setInteger("reactionHeat", hotCoolant);
		data.setInteger("actualPowerReq", getActualPowerReq());

		data.setDouble("netThermalGainRatio", (double) hotCoolant / (double) Math.max(1, coolant));

		data.setDouble("shieldHeatEfficiency", getShieldHeatEfficiency());
		data.setDouble("shieldBreedingEfficiency", getShieldBreedingEfficiency());
		data.setDouble("shieldPowerDrainMultiplier", getShieldPowerDrainMultiplier());
	}

	@Callback(direct = true)
	@Optional.Method(modid = "OpenComputers")
	public Object[] getShieldStats(Context context, Arguments args) {

		ItemFusionShield shield = getShieldItem();

		if(shield == null) {
			return new Object[] { "N/A", "N/A", "N/A", "N/A" };
		}

		return new Object[] {
			shield.maxTemp,
			shield.heatEfficiency,
			shield.breedingEfficiency,
			shield.powerDrainMultiplier
		};
	}

	@Override
	@Optional.Method(modid = "OpenComputers")
	public String getComponentName() {
		return "ntm_fusion";
	}

	@Callback(direct = true)
	@Optional.Method(modid = "OpenComputers")
	public Object[] getEnergyInfo(Context context, Arguments args) {
		return new Object[] { getPower(), getMaxPower() };
	}

	@Callback(direct = true)
	@Optional.Method(modid = "OpenComputers")
	public Object[] isActive(Context context, Arguments args) {
		return new Object[] { isOn };
	}

	@Callback(direct = true, limit = 4)
	@Optional.Method(modid = "OpenComputers")
	public Object[] setActive(Context context, Arguments args) {

		boolean state = args.checkBoolean(0);

		if(state) {

			if(getShield() <= 0) {
				return new Object[] { false };
			}

			if(tanks[2].getFill() <= 0 || tanks[3].getFill() >= tanks[3].getMaxFill()) {
				return new Object[] { false };
			}
		}

		isOn = state;

		return new Object[] { true };
	}

	@Callback(direct = true)
	@Optional.Method(modid = "OpenComputers")
	public Object[] getFluid(Context context, Arguments args) {
		return new Object[] {
			tanks[0].getFill(),
			tanks[0].getMaxFill(),
			tanks[1].getFill(),
			tanks[1].getMaxFill(),
			tanks[2].getFill(),
			tanks[2].getMaxFill(),
			tanks[3].getFill(),
			tanks[3].getMaxFill(),
			plasma.getFill(),
			plasma.getMaxFill(),
			plasma.getTankType().getUnlocalizedName()
		};
	}

	@Callback(direct = true)
	@Optional.Method(modid = "OpenComputers")
	public Object[] getPlasmaTemp(Context context, Arguments args) {
		return new Object[] { plasma.getTankType().temperature };
	}

	@Callback(direct = true)
	@Optional.Method(modid = "OpenComputers")
	public Object[] getMaxTemp(Context context, Arguments args) {

		if(slots[3] != null && slots[3].getItem() instanceof ItemFusionShield) {
			return new Object[] { ((ItemFusionShield) slots[3].getItem()).maxTemp };
		}

		return new Object[] { "N/A" };
	}

	@Callback(direct = true)
	@Optional.Method(modid = "OpenComputers")
	public Object[] getBlanketDamage(Context context, Arguments args) {

		if(slots[3] != null && slots[3].getItem() instanceof ItemFusionShield) {
			return new Object[] {
				ItemFusionShield.getShieldDamage(slots[3]),
				((ItemFusionShield) slots[3].getItem()).maxDamage
			};
		}

		return new Object[] { "N/A", "N/A" };
	}

	@Callback(direct = true)
	@Optional.Method(modid = "OpenComputers")
	public Object[] getHeatStress(Context context, Arguments args) {
		return new Object[] { heatStress, MAX_HEAT_STRESS };
	}

	@Override
	@Optional.Method(modid = "OpenComputers")
	public String[] methods() {
		return new String[] {
			"getEnergyInfo",
			"isActive",
			"setActive",
			"getFluid",
			"getPlasmaTemp",
			"getMaxTemp",
			"getBlanketDamage",
			"getHeatStress",
			"getShieldStats"
		};
	}

	@Override
	@Optional.Method(modid = "OpenComputers")
	public Object[] invoke(String method, Context context, Arguments args) throws Exception {

		if(method.equals("getEnergyInfo")) {
			return getEnergyInfo(context, args);
		}

		if(method.equals("isActive")) {
			return isActive(context, args);
		}

		if(method.equals("setActive")) {
			return setActive(context, args);
		}

		if(method.equals("getFluid")) {
			return getFluid(context, args);
		}

		if(method.equals("getPlasmaTemp")) {
			return getPlasmaTemp(context, args);
		}

		if(method.equals("getMaxTemp")) {
			return getMaxTemp(context, args);
		}

		if(method.equals("getBlanketDamage")) {
			return getBlanketDamage(context, args);
		}

		if(method.equals("getHeatStress")) {
			return getHeatStress(context, args);
		}

		if(method.equals("getShieldStats")) {
			return getShieldStats(context, args);
		}

		throw new NoSuchMethodException();
	}

	@Override
	public FluidTank getTankToPaste() {
		return null;
	}
}
