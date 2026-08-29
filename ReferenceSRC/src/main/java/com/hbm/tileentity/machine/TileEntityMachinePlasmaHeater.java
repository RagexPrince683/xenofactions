package com.hbm.tileentity.machine;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.machine.MachineHTRF4;
import com.hbm.blocks.machine.MachineITER;
import com.hbm.inventory.container.ContainerPlasmaHeater;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.inventory.gui.GUIPlasmaHeater;
import com.hbm.lib.Library;
import com.hbm.tileentity.IFluidCopiable;
import com.hbm.tileentity.IGUIProvider;
import com.hbm.tileentity.TileEntityMachineBase;

import api.hbm.energymk2.IEnergyReceiverMK2;
import api.hbm.fluid.IFluidStandardReceiver;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public class TileEntityMachinePlasmaHeater extends TileEntityMachineBase implements IEnergyReceiverMK2, IFluidStandardReceiver, IGUIProvider, IFluidCopiable {

	public long power;
	public static final long maxPower = 100000000;

	public FluidTank[] tanks;
	public FluidTank plasma;

	/*
	 * Realistic low-refactor interpretation:
	 *
	 * This machine does not store plasma as a bulk fluid.
	 * It only holds one tiny unstable magnetic plasma packet.
	 *
	 * The plasma packet should be injected into ITER/HTRF almost immediately.
	 * If it sits around without containment power, it dissipates.
	 */
	private static final int PLASMA_BUFFER = 1;

	/*
	 * Startup energy: represents ionization + RF/microwave/neutral beam heating
	 * before a stable-enough plasma packet can be formed.
	 */
	private static final int STARTUP_POWER_REQUIRED = 500000;

	/*
	 * Power consumed to produce one unstable plasma packet.
	 */
	private static final int OPERATING_POWER_PER_PACKET = 50000;

	/*
	 * Power consumed every tick while plasma exists in the heater.
	 * This represents magnetic containment / active field maintenance.
	 */
	private static final int CONTAINMENT_POWER_PER_TICK = 10000;

	/*
	 * If the machine has plasma but cannot inject it, it only survives for a few ticks.
	 */
	private static final int MAX_PLASMA_AGE = 5;

	/*
	 * One packet per tick. This keeps the machine acting like an injector,
	 * not a plasma tank or boiler.
	 */
	private static final int MAX_CONVERT_PER_TICK = 1;

	private int startupCharge;
	private int plasmaAge;

	public TileEntityMachinePlasmaHeater() {

		super(5);

		tanks = new FluidTank[2];

		tanks[0] = new FluidTank(Fluids.DEUTERIUM, 16_000);
		tanks[1] = new FluidTank(Fluids.TRITIUM, 16_000);

		/*
		 * This is the main realism fix.
		 * Plasma is not stockpiled. It is only a 1 mB unstable injection buffer.
		 */
		plasma = new FluidTank(Fluids.PLASMA_DT, PLASMA_BUFFER);
	}

	@Override
	public String getName() {
		return "container.plasmaHeater";
	}

	@Override
	public void updateEntity() {

		if(!worldObj.isRemote) {

			if(this.worldObj.getTotalWorldTime() % 20 == 0) {
				this.updateConnections();
			}

			/// START Managing all the internal stuff ///

			power = Library.chargeTEFromItems(slots, 0, power, maxPower);

			tanks[0].setType(1, 2, slots);
			tanks[1].setType(3, 4, slots);

			FluidType plasmaType = getPlasmaTypeFromInputs();

			if(plasma.getFill() <= 0) {

				plasma.setFill(0);
				plasmaAge = 0;

				if(plasmaType != Fluids.NONE) {
					plasma.setTankType(plasmaType);
				} else {
					plasma.setTankType(Fluids.NONE);
					startupCharge = 0;
				}

			} else {

				/*
				 * Existing plasma keeps its type until injected or dissipated.
				 * Do not switch the plasma type while an unstable packet exists.
				 */
				handlePlasmaContainment();
			}

			if(plasmaType != Fluids.NONE && plasma.getFill() < plasma.getMaxFill()) {
				runIonizationCycle(plasmaType);
			} else if(plasmaType == Fluids.NONE) {
				startupCharge = 0;
			}

			/// END Managing all the internal stuff ///

			/// START Loading plasma into the ITER / HTRF ///

			ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata() - BlockDummyable.offset).getOpposite();
			int dist = 11;

			boolean injected = false;

			injected = tryInjectHTRF(dir, dist) || injected;
			injected = tryInjectITER(dir, dist) || injected;

			if(injected) {
				plasmaAge = 0;
			}

			/// END Loading plasma into the ITER / HTRF ///

			/// START Notif packets ///

			NBTTagCompound data = new NBTTagCompound();

			data.setLong("power", power);
			data.setInteger("startupCharge", startupCharge);
			data.setInteger("plasmaAge", plasmaAge);

			tanks[0].writeToNBT(data, "t0");
			tanks[1].writeToNBT(data, "t1");
			plasma.writeToNBT(data, "t2");

			this.networkPack(data, 50);

			/// END Notif packets ///
		}
	}

	private void runIonizationCycle(FluidType plasmaType) {

		if(plasmaType == Fluids.NONE) {
			startupCharge = 0;
			return;
		}

		if(tanks[0].getFill() <= 0 || tanks[1].getFill() <= 0) {
			return;
		}

		if(power <= 0) {
			return;
		}

		/*
		 * Charge the ionization/startup stage first.
		 * This avoids instant cold-fluid-to-plasma conversion.
		 */
		if(startupCharge < STARTUP_POWER_REQUIRED) {

			int charge = (int) Math.min(power, STARTUP_POWER_REQUIRED - startupCharge);

			startupCharge += charge;
			power -= charge;

			return;
		}

		int convert = Math.min(tanks[0].getFill(), tanks[1].getFill());
		convert = Math.min(convert, plasma.getMaxFill() - plasma.getFill());
		convert = Math.min(convert, MAX_CONVERT_PER_TICK);
		convert = Math.min(convert, (int) (power / OPERATING_POWER_PER_PACKET));
		convert = Math.max(0, convert);

		if(convert <= 0) {
			return;
		}

		plasma.setTankType(plasmaType);

		/*
		 * Low-refactor realistic compromise:
		 *
		 * 1 mB fuel A + 1 mB fuel B -> 1 mB unstable plasma packet
		 *
		 * Not volume-conserving, but better represents that this is an energetic
		 * reaction state/injection packet, not a stored liquid mixture.
		 */
		tanks[0].setFill(tanks[0].getFill() - convert);
		tanks[1].setFill(tanks[1].getFill() - convert);

		plasma.setFill(plasma.getFill() + convert);

		power -= convert * OPERATING_POWER_PER_PACKET;

		plasmaAge = 0;

		this.markDirty();
	}

	private void handlePlasmaContainment() {

		if(plasma.getFill() <= 0) {
			plasmaAge = 0;
			return;
		}

		if(power >= CONTAINMENT_POWER_PER_TICK) {

			power -= CONTAINMENT_POWER_PER_TICK;
			plasmaAge++;

			if(plasmaAge > MAX_PLASMA_AGE) {
				dissipatePlasma();
			}

		} else {

			/*
			 * No containment power: plasma immediately dissipates instead of
			 * sitting around like a normal tank fluid.
			 */
			dissipatePlasma();
		}
	}

	private void dissipatePlasma() {

		plasma.setFill(0);
		plasmaAge = 0;

		if(getPlasmaTypeFromInputs() == Fluids.NONE) {
			plasma.setTankType(Fluids.NONE);
		}

		this.markDirty();
	}

	private boolean tryInjectITER(ForgeDirection dir, int dist) {

		if(plasma.getFill() <= 0 || plasma.getTankType() == Fluids.NONE) {
			return false;
		}

		if(worldObj.getBlock(xCoord + dir.offsetX * dist, yCoord + 2, zCoord + dir.offsetZ * dist) != ModBlocks.iter) {
			return false;
		}

		int[] pos = ((MachineITER) ModBlocks.iter).findCore(
			worldObj,
			xCoord + dir.offsetX * dist,
			yCoord + 2,
			zCoord + dir.offsetZ * dist
		);

		if(pos == null) {
			return false;
		}

		TileEntity te = worldObj.getTileEntity(pos[0], pos[1], pos[2]);

		if(!(te instanceof TileEntityITER)) {
			return false;
		}

		TileEntityITER iter = (TileEntityITER) te;

		/*
		 * This part is realistic enough:
		 * plasma can only be injected into an active/magnetized reactor.
		 */
		if(!iter.isOn) {
			return false;
		}

		if(iter.plasma.getFill() == 0 && this.plasma.getTankType() != Fluids.NONE) {
			iter.plasma.setTankType(this.plasma.getTankType());
		}

		if(iter.plasma.getTankType() != this.plasma.getTankType()) {
			return false;
		}

		int toLoad = Math.min(iter.plasma.getMaxFill() - iter.plasma.getFill(), this.plasma.getFill());
		toLoad = Math.min(toLoad, PLASMA_BUFFER);

		if(toLoad <= 0) {
			return false;
		}

		this.plasma.setFill(this.plasma.getFill() - toLoad);
		iter.plasma.setFill(iter.plasma.getFill() + toLoad);

		if(this.plasma.getFill() <= 0) {
			this.plasmaAge = 0;
		}

		this.markDirty();
		iter.markDirty();

		return true;
	}

	private boolean tryInjectHTRF(ForgeDirection dir, int dist) {

		if(plasma.getFill() <= 0 || plasma.getTankType() == Fluids.NONE) {
			return false;
		}

		if(worldObj.getBlock(xCoord + dir.offsetX * dist, yCoord + 1, zCoord + dir.offsetZ * dist) != ModBlocks.machine_htrf4) {
			return false;
		}

		int[] pos = ((MachineHTRF4) ModBlocks.machine_htrf4).findCore(
			worldObj,
			xCoord + dir.offsetX * dist,
			yCoord + 1,
			zCoord + dir.offsetZ * dist
		);

		if(pos == null) {
			return false;
		}

		TileEntity te = worldObj.getTileEntity(pos[0], pos[1], pos[2]);

		if(!(te instanceof TileEntityMachineHTRF4)) {
			return false;
		}

		TileEntityMachineHTRF4 htrf = (TileEntityMachineHTRF4) te;

		if(htrf.tanks[0].getFill() == 0 && this.plasma.getTankType() != Fluids.NONE) {
			htrf.tanks[0].setTankType(this.plasma.getTankType());
		}

		if(htrf.tanks[0].getTankType() != this.plasma.getTankType()) {
			return false;
		}

		int toLoad = Math.min(htrf.tanks[0].getMaxFill() - htrf.tanks[0].getFill(), this.plasma.getFill());
		toLoad = Math.min(toLoad, PLASMA_BUFFER);

		if(toLoad <= 0) {
			return false;
		}

		this.plasma.setFill(this.plasma.getFill() - toLoad);
		htrf.tanks[0].setFill(htrf.tanks[0].getFill() + toLoad);

		if(this.plasma.getFill() <= 0) {
			this.plasmaAge = 0;
		}

		this.markDirty();
		htrf.markDirty();

		return true;
	}

	private void updateConnections() {

		this.getBlockMetadata();

		ForgeDirection dir = ForgeDirection.getOrientation(this.blockMetadata - BlockDummyable.offset);
		ForgeDirection side = dir.getRotation(ForgeDirection.UP);

		for(int i = 1; i < 4; i++) {
			for(int j = -1; j < 2; j++) {

				this.trySubscribe(
					worldObj,
					xCoord + side.offsetX * j + dir.offsetX * 2,
					yCoord + i,
					zCoord + side.offsetZ * j + dir.offsetZ * 2,
					j < 0 ? ForgeDirection.DOWN : ForgeDirection.UP
				);

				this.trySubscribe(
					tanks[0].getTankType(),
					worldObj,
					xCoord + side.offsetX * j + dir.offsetX * 2,
					yCoord + i,
					zCoord + side.offsetZ * j + dir.offsetZ * 2,
					j < 0 ? ForgeDirection.DOWN : ForgeDirection.UP
				);

				this.trySubscribe(
					tanks[1].getTankType(),
					worldObj,
					xCoord + side.offsetX * j + dir.offsetX * 2,
					yCoord + i,
					zCoord + side.offsetZ * j + dir.offsetZ * 2,
					j < 0 ? ForgeDirection.DOWN : ForgeDirection.UP
				);
			}
		}
	}

	public void networkUnpack(NBTTagCompound nbt) {

		super.networkUnpack(nbt);

		this.power = nbt.getLong("power");
		this.startupCharge = nbt.getInteger("startupCharge");
		this.plasmaAge = nbt.getInteger("plasmaAge");

		tanks[0].readFromNBT(nbt, "t0");
		tanks[1].readFromNBT(nbt, "t1");
		plasma.readFromNBT(nbt, "t2");
	}

	private FluidType getPlasmaTypeFromInputs() {

		FluidType a = tanks[0].getTankType();
		FluidType b = tanks[1].getTankType();

		if(isPair(a, b, Fluids.DEUTERIUM, Fluids.TRITIUM)) {
			return Fluids.PLASMA_DT;
		}

		if(isPair(a, b, Fluids.DEUTERIUM, Fluids.HELIUM3)) {
			return Fluids.PLASMA_DH3;
		}

		if(isPair(a, b, Fluids.DEUTERIUM, Fluids.HYDROGEN)) {
			return Fluids.PLASMA_HD;
		}

		if(isPair(a, b, Fluids.HYDROGEN, Fluids.TRITIUM)) {
			return Fluids.PLASMA_HT;
		}

		/*
		 * These are fantasy/HBM-special fuels.
		 * Kept for compatibility.
		 */
		if(isPair(a, b, Fluids.HELIUM4, Fluids.OXYGEN)) {
			return Fluids.PLASMA_XM;
		}

		if(isPair(a, b, Fluids.BALEFIRE, Fluids.AMAT)) {
			return Fluids.PLASMA_BF;
		}

		return Fluids.NONE;
	}

	private boolean isPair(FluidType a, FluidType b, FluidType x, FluidType y) {
		return (a == x && b == y) || (a == y && b == x);
	}

	public long getPowerScaled(int i) {
		return (power * i) / maxPower;
	}

	public long getStartupScaled(int i) {
		return (startupCharge * i) / STARTUP_POWER_REQUIRED;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {

		super.readFromNBT(nbt);

		this.power = nbt.getLong("power");
		this.startupCharge = nbt.getInteger("startupCharge");
		this.plasmaAge = nbt.getInteger("plasmaAge");

		tanks[0].readFromNBT(nbt, "fuel_1");
		tanks[1].readFromNBT(nbt, "fuel_2");
		plasma.readFromNBT(nbt, "plasma");

		if(plasma.getFill() > plasma.getMaxFill()) {
			plasma.setFill(plasma.getMaxFill());
		}
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) {

		super.writeToNBT(nbt);

		nbt.setLong("power", power);
		nbt.setInteger("startupCharge", startupCharge);
		nbt.setInteger("plasmaAge", plasmaAge);

		tanks[0].writeToNBT(nbt, "fuel_1");
		tanks[1].writeToNBT(nbt, "fuel_2");
		plasma.writeToNBT(nbt, "plasma");
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
	public AxisAlignedBB getRenderBoundingBox() {
		return TileEntity.INFINITE_EXTENT_AABB;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public double getMaxRenderDistanceSquared() {
		return 65536.0D;
	}

	@Override
	public FluidTank[] getAllTanks() {
		return new FluidTank[] { tanks[0], tanks[1], plasma };
	}

	@Override
	public FluidTank[] getReceivingTanks() {
		return tanks;
	}

	@Override
	public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new ContainerPlasmaHeater(player.inventory, this);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Object provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new GUIPlasmaHeater(player.inventory, this);
	}

	@Override
	public FluidTank getTankToPaste() {
		return null;
	}
}
