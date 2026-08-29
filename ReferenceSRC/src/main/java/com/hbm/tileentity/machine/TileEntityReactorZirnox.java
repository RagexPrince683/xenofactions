package com.hbm.tileentity.machine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ModBlocks;
import com.hbm.config.MobConfig;
import com.hbm.entity.projectile.EntityZirnoxDebris;
import com.hbm.entity.projectile.EntityZirnoxDebris.DebrisType;
import com.hbm.explosion.ExplosionNukeGeneric;
import com.hbm.handler.CompatHandler;
import com.hbm.handler.MultiblockHandlerXR;
import com.hbm.interfaces.IControlReceiver;
import com.hbm.inventory.RecipesCommon.ComparableStack;
import com.hbm.inventory.container.ContainerReactorZirnox;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.inventory.gui.GUIReactorZirnox;
import com.hbm.items.ModItems;
import com.hbm.items.machine.ItemZirnoxRod;
import com.hbm.items.machine.ItemZirnoxRod.EnumZirnoxType;
import com.hbm.main.MainRegistry;
import com.hbm.tileentity.IGUIProvider;
import com.hbm.tileentity.TileEntityMachineBase;
import com.hbm.util.CompatEnergyControl;
import com.hbm.util.EnumUtil;
import com.hbm.util.fauxpointtwelve.DirPos;

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

// Realistification: Magnox/Zirnox Nuclear Reactor
//
// Design identity:
// - graphite moderated
// - CO2 primary coolant
// - water/steam secondary side
// - cheap natural uranium works best
// - exotic fuels work but are hotter/dirtier/riskier
// - high thermal inertia
// - decay heat after shutdown
// - dirty pressure/graphite/cladding failure, not a nuclear detonation

@Optional.InterfaceList({@Optional.Interface(iface = "li.cil.oc.api.network.SimpleComponent", modid = "OpenComputers")})
public class TileEntityReactorZirnox extends TileEntityMachineBase implements IControlReceiver, IFluidStandardTransceiver, SimpleComponent, IGUIProvider, IInfoProviderEC, CompatHandler.OCComponent {

	public int heat;
	public static final int maxHeat = 100000;

	public int pressure;
	public static final int maxPressure = 100000;

	// Kept for GUI/container compatibility.
	// Internally this now maps to control rods:
	// isOn = true  -> control rods withdrawn
	// isOn = false -> SCRAM / rods fully inserted
	public boolean isOn = false;

	public FluidTank steam;
	public FluidTank carbonDioxide;
	public FluidTank water;

	protected int output;

	// ==========================
	// REALISTIFICATION STATE
	// ==========================

	// 0 = full power, 100 = fully shut down/SCRAM.
	public int controlRodInsertion = 100;

	// Hidden thermal mass representing the graphite moderator/core bulk.
	// Uses the same scale as heat: 0..100000 = 20C..800C.
	public int graphiteHeat = 0;

	// Residual decay heat. Stored as a double because it decays smoothly.
	public double decayHeat = 0.0D;

	// Damage accumulators. 100000 means severe failure.
	public int graphiteDamage = 0;
	public int claddingDamage = 0;

	// Represents oxygen/air contamination after hot venting or loss of CO2 cover.
	// Air ingress makes hot graphite much more dangerous.
	public int airIngress = 0;

	// Debug/OC fields.
	public int activePower = 0;
	public int co2Cooling = 0;

	private static final int[] slots_io = new int[] {
		0, 1, 2, 3, 4, 5, 6, 7,
		8, 9, 10, 11, 12, 13, 14, 15,
		16, 17, 18, 19, 20, 21, 22, 23
	};

	// ==========================
	// BALANCE CONSTANTS
	// ==========================

	private static final double TEMP_BASE_C = 20.0D;
	private static final double TEMP_RANGE_C = 780.0D;

	// Late Magnox-ish target range.
	private static final double NOMINAL_CO2_OUTLET_C = 410.0D;
	private static final double NOMINAL_PRESSURE_BAR = 26.0D;
	private static final double MAX_PRESSURE_BAR = 30.0D;

	private static final double STEAM_START_C = 300.0D;
	private static final double STEAM_FULL_C = 450.0D;

	private static final int NOMINAL_CO2_FILL = 14000;
	private static final int MAX_STEAM_PER_TICK = 55;
	private static final int HEAT_REMOVED_PER_MB_STEAM = 80;

	private static final double CLADDING_DAMAGE_TEMP_C = 500.0D;
	private static final double FUEL_DAMAGE_TEMP_C = 600.0D;
	private static final double GRAPHITE_DAMAGE_TEMP_C = 600.0D;
	private static final double GRAPHITE_FIRE_TEMP_C = 650.0D;

	private static final int MAX_GRAPHITE_DAMAGE = 100000;
	private static final int MAX_CLADDING_DAMAGE = 100000;

	private static final int MELTDOWN_OVERPRESSURE = 0;
	private static final int MELTDOWN_OVERHEAT = 1;
	private static final int MELTDOWN_GRAPHITE_FIRE = 2;
	private static final int MELTDOWN_WATER_INGRESS = 3;

	// Slot flux shaping.
	// Center/near-center channels run hotter; edge channels are slightly weaker.
	private static final float[] FLUX = new float[] {
		0.85F, 0.95F, 0.85F,
		0.90F, 1.05F, 1.05F, 0.90F,
		0.95F, 1.10F, 1.10F,
		0.90F, 1.15F, 1.15F, 0.90F,
		0.95F, 1.10F, 1.10F,
		0.90F, 1.05F, 1.05F, 0.90F,
		0.85F, 0.95F, 0.85F
	};

	public static final HashMap<ComparableStack, ItemStack> fuelMap = new HashMap<ComparableStack, ItemStack>();
	static {
		fuelMap.put(new ComparableStack(ModItems.rod_zirnox, 1, EnumZirnoxType.NATURAL_URANIUM_FUEL.ordinal()), new ItemStack(ModItems.rod_zirnox_natural_uranium_fuel_depleted));
		fuelMap.put(new ComparableStack(ModItems.rod_zirnox, 1, EnumZirnoxType.URANIUM_FUEL.ordinal()), new ItemStack(ModItems.rod_zirnox_uranium_fuel_depleted));
		fuelMap.put(new ComparableStack(ModItems.rod_zirnox, 1, EnumZirnoxType.TH232.ordinal()), new ItemStack(ModItems.rod_zirnox, 1, EnumZirnoxType.THORIUM_FUEL.ordinal()));
		fuelMap.put(new ComparableStack(ModItems.rod_zirnox, 1, EnumZirnoxType.THORIUM_FUEL.ordinal()), new ItemStack(ModItems.rod_zirnox_thorium_fuel_depleted));
		fuelMap.put(new ComparableStack(ModItems.rod_zirnox, 1, EnumZirnoxType.MOX_FUEL.ordinal()), new ItemStack(ModItems.rod_zirnox_mox_fuel_depleted));
		fuelMap.put(new ComparableStack(ModItems.rod_zirnox, 1, EnumZirnoxType.PLUTONIUM_FUEL.ordinal()), new ItemStack(ModItems.rod_zirnox_plutonium_fuel_depleted));
		fuelMap.put(new ComparableStack(ModItems.rod_zirnox, 1, EnumZirnoxType.U233_FUEL.ordinal()), new ItemStack(ModItems.rod_zirnox_u233_fuel_depleted));
		fuelMap.put(new ComparableStack(ModItems.rod_zirnox, 1, EnumZirnoxType.U235_FUEL.ordinal()), new ItemStack(ModItems.rod_zirnox_u235_fuel_depleted));
		fuelMap.put(new ComparableStack(ModItems.rod_zirnox, 1, EnumZirnoxType.LES_FUEL.ordinal()), new ItemStack(ModItems.rod_zirnox_les_fuel_depleted));
		fuelMap.put(new ComparableStack(ModItems.rod_zirnox, 1, EnumZirnoxType.LITHIUM.ordinal()), new ItemStack(ModItems.rod_zirnox_tritium));
		fuelMap.put(new ComparableStack(ModItems.rod_zirnox, 1, EnumZirnoxType.ZFB_MOX.ordinal()), new ItemStack(ModItems.rod_zirnox_zfb_mox_depleted));
	}

	public TileEntityReactorZirnox() {
		super(28);
		steam = new FluidTank(Fluids.SUPERHOTSTEAM, 8000);
		carbonDioxide = new FluidTank(Fluids.CARBONDIOXIDE, 16000);
		water = new FluidTank(Fluids.WATER, 32000);
	}

	@Override
	public String getName() {
		return "container.zirnox";
	}

	@Override
	public int[] getAccessibleSlotsFromSide(int side) {
		return slots_io;
	}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack stack) {
		return i < 24 && stack.getItem() instanceof ItemZirnoxRod;
	}

	@Override
	public boolean canExtractItem(int i, ItemStack stack, int j) {
		return i < 24 && !(stack.getItem() instanceof ItemZirnoxRod);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);

		heat = nbt.getInteger("heat");
		pressure = nbt.getInteger("pressure");
		isOn = nbt.getBoolean("isOn");

		if(nbt.hasKey("controlRodInsertion")) {
			controlRodInsertion = clamp(nbt.getInteger("controlRodInsertion"), 0, 100);
		} else {
			controlRodInsertion = isOn ? 0 : 100;
		}

		if(nbt.hasKey("graphiteHeat")) {
			graphiteHeat = nbt.getInteger("graphiteHeat");
		} else {
			graphiteHeat = heat;
		}

		decayHeat = nbt.getDouble("decayHeat");
		graphiteDamage = nbt.getInteger("graphiteDamage");
		claddingDamage = nbt.getInteger("claddingDamage");
		airIngress = nbt.getInteger("airIngress");
		activePower = nbt.getInteger("activePower");
		co2Cooling = nbt.getInteger("co2Cooling");

		steam.readFromNBT(nbt, "steam");
		carbonDioxide.readFromNBT(nbt, "carbondioxide");
		water.readFromNBT(nbt, "water");
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);

		nbt.setInteger("heat", heat);
		nbt.setInteger("pressure", pressure);
		nbt.setBoolean("isOn", isOn);

		nbt.setInteger("controlRodInsertion", controlRodInsertion);
		nbt.setInteger("graphiteHeat", graphiteHeat);
		nbt.setDouble("decayHeat", decayHeat);
		nbt.setInteger("graphiteDamage", graphiteDamage);
		nbt.setInteger("claddingDamage", claddingDamage);
		nbt.setInteger("airIngress", airIngress);
		nbt.setInteger("activePower", activePower);
		nbt.setInteger("co2Cooling", co2Cooling);

		steam.writeToNBT(nbt, "steam");
		carbonDioxide.writeToNBT(nbt, "carbondioxide");
		water.writeToNBT(nbt, "water");
	}

	public void networkUnpack(NBTTagCompound data) {
		super.networkUnpack(data);

		this.heat = data.getInteger("heat");
		this.pressure = data.getInteger("pressure");
		this.isOn = data.getBoolean("isOn");

		this.controlRodInsertion = data.hasKey("controlRodInsertion") ? clamp(data.getInteger("controlRodInsertion"), 0, 100) : (isOn ? 0 : 100);
		this.graphiteHeat = data.hasKey("graphiteHeat") ? data.getInteger("graphiteHeat") : heat;
		this.decayHeat = data.getDouble("decayHeat");
		this.graphiteDamage = data.getInteger("graphiteDamage");
		this.claddingDamage = data.getInteger("claddingDamage");
		this.airIngress = data.getInteger("airIngress");
		this.activePower = data.getInteger("activePower");
		this.co2Cooling = data.getInteger("co2Cooling");

		steam.readFromNBT(data, "t0");
		carbonDioxide.readFromNBT(data, "t1");
		water.readFromNBT(data, "t2");
	}

	public int getGaugeScaled(int i, int type) {
		switch(type) {
			case 0: return (steam.getFill() * i) / steam.getMaxFill();
			case 1: return (carbonDioxide.getFill() * i) / carbonDioxide.getMaxFill();
			case 2: return (water.getFill() * i) / water.getMaxFill();
			case 3: return (this.heat * i) / maxHeat;
			case 4: return (this.pressure * i) / maxPressure;
			default: return 1;
		}
	}

	private int[] getNeighbouringSlots(int id) {
		switch(id) {
			case 0: return new int[] { 1, 7 };
			case 1: return new int[] { 0, 2, 8 };
			case 2: return new int[] { 1, 9 };
			case 3: return new int[] { 4, 10 };
			case 4: return new int[] { 3, 5, 11 };
			case 5: return new int[] { 4, 6, 12 };
			case 6: return new int[] { 5, 13 };
			case 7: return new int[] { 0, 8, 14 };
			case 8: return new int[] { 1, 7, 9, 15 };
			case 9: return new int[] { 2, 8, 16 };
			case 10: return new int[] { 3, 11, 17 };
			case 11: return new int[] { 4, 10, 12, 18 };
			case 12: return new int[] { 5, 11, 13, 19 };
			case 13: return new int[] { 6, 12, 20 };
			case 14: return new int[] { 7, 15, 21 };
			case 15: return new int[] { 8, 14, 16, 22 };
			case 16: return new int[] { 9, 15, 23 };
			case 17: return new int[] { 10, 18 };
			case 18: return new int[] { 11, 17, 19 };
			case 19: return new int[] { 12, 18, 20 };
			case 20: return new int[] { 13, 19 };
			case 21: return new int[] { 14, 22 };
			case 22: return new int[] { 15, 21, 23 };
			case 23: return new int[] { 16, 22 };
		}

		return null;
	}

	@Override
	public void updateEntity() {
		if(!worldObj.isRemote) {

			this.output = 0;
			this.activePower = 0;
			this.co2Cooling = 0;

			if(worldObj.getTotalWorldTime() % 20 == 0) {
				this.updateConnections();
			}

			carbonDioxide.loadTank(24, 26, slots);
			water.loadTank(25, 27, slots);

			this.activePower = runFuelCycle();
			applyDecayHeat(this.activePower);

			updatePressureFromCO2();

			generateSteam();

			applyPassiveCooling();
			equalizeGraphiteAndOutletHeat();

			updatePressureFromCO2();

			applyDamageModel();
			checkIfMeltdown();

			for(DirPos pos : getConPos()) {
				this.sendFluid(steam, worldObj, pos.getX(), pos.getY(), pos.getZ(), pos.getDir());
			}

			NBTTagCompound data = new NBTTagCompound();

			data.setInteger("heat", heat);
			data.setInteger("pressure", pressure);
			data.setBoolean("isOn", isOn);

			data.setInteger("controlRodInsertion", controlRodInsertion);
			data.setInteger("graphiteHeat", graphiteHeat);
			data.setDouble("decayHeat", decayHeat);
			data.setInteger("graphiteDamage", graphiteDamage);
			data.setInteger("claddingDamage", claddingDamage);
			data.setInteger("airIngress", airIngress);
			data.setInteger("activePower", activePower);
			data.setInteger("co2Cooling", co2Cooling);

			steam.writeToNBT(data, "t0");
			carbonDioxide.writeToNBT(data, "t1");
			water.writeToNBT(data, "t2");

			this.networkPack(data, 150);
		}
	}

	private int runFuelCycle() {
		if(!isOn || controlRodInsertion >= 100)
			return 0;

		int power = 0;

		for(int i = 0; i < 24; i++) {
			if(slots[i] != null && slots[i].getItem() instanceof ItemZirnoxRod) {
				power += decay(i);
			}
		}

		return power;
	}

	private void applyDecayHeat(int activePowerThisTick) {

		// Fission-product decay heat is small compared to full power but persists
		// after shutdown. This makes SCRAM useful, but not magic.
		if(activePowerThisTick > 0) {
			double target = activePowerThisTick * 0.055D;

			if(decayHeat < target)
				decayHeat = target;

			// While running, keep decay heat near its generated value.
			decayHeat *= 0.99995D;
		} else {
			// After shutdown, decay heat fades slowly.
			decayHeat *= 0.9995D;
		}

		if(decayHeat < 0.05D)
			decayHeat = 0.0D;

		graphiteHeat += (int)Math.round(decayHeat);
	}

	private void updatePressureFromCO2() {

		double gasFrac = getCO2FillFraction();
		double tempK = getHeatC() + 273.15D;
		double nominalK = NOMINAL_CO2_OUTLET_C + 273.15D;

		// Closed gas loop approximation:
		// more CO2 inventory = more pressure,
		// hotter gas = higher pressure.
		double pressureBar = gasFrac * NOMINAL_PRESSURE_BAR * (tempK / nominalK);

		this.pressure = barToPressure(pressureBar);
	}

	private void generateSteam() {

		if(this.heat <= 0)
			return;

		if(this.water.getFill() <= 0)
			return;

		if(this.carbonDioxide.getFill() <= 0)
			return;

		if(this.steam.getFill() >= this.steam.getMaxFill())
			return;

		double tempC = getHeatC();

		if(tempC < STEAM_START_C)
			return;

		double tempFactor = clampDouble((tempC - STEAM_START_C) / (STEAM_FULL_C - STEAM_START_C), 0.0D, 1.0D);
		double co2Factor = clampDouble((double)this.carbonDioxide.getFill() / (double)NOMINAL_CO2_FILL, 0.0D, 1.0D);
		double pressureFactor = clampDouble(getPressureBar() / NOMINAL_PRESSURE_BAR, 0.0D, 1.0D);

		int cycle = (int)Math.floor(tempFactor * co2Factor * pressureFactor * MAX_STEAM_PER_TICK);

		if(cycle <= 0)
			return;

		int room = steam.getMaxFill() - steam.getFill();

		cycle = Math.min(cycle, water.getFill());
		cycle = Math.min(cycle, room);

		if(cycle <= 0)
			return;

		this.output = cycle;

		water.setFill(water.getFill() - cycle);
		steam.setFill(steam.getFill() + cycle);

		int removed = cycle * HEAT_REMOVED_PER_MB_STEAM;
		this.co2Cooling = removed;

		// The steam generator removes heat from the graphite/gas system.
		// Direct outlet heat drops a little; most of the cooling hits the large
		// graphite thermal mass.
		graphiteHeat -= removed;
		heat -= removed / 12;

		if(graphiteHeat < 0)
			graphiteHeat = 0;

		if(heat < 0)
			heat = 0;
	}

	private void applyPassiveCooling() {

		// Small ambient/inertial loss. This is intentionally weak: Magnox-style
		// graphite cores are massive and stay hot for a long time.
		if(heat > 0) {
			heat -= Math.max(1, heat / 15000);

			if(heat < 0)
				heat = 0;
		}

		if(graphiteHeat > 0) {
			graphiteHeat -= Math.max(1, graphiteHeat / 25000);

			if(graphiteHeat < 0)
				graphiteHeat = 0;
		}
	}

	private void equalizeGraphiteAndOutletHeat() {

		// Slow outlet temperature response.
		// The player sees heat lag behind the huge graphite moderator/core mass.
		int delta = graphiteHeat - heat;

		if(delta != 0) {
			int exchange = delta / 24;

			if(exchange == 0)
				exchange = delta > 0 ? 1 : -1;

			heat += exchange;
		}

		if(heat < 0)
			heat = 0;

		if(graphiteHeat < 0)
			graphiteHeat = 0;
	}

	private void applyDamageModel() {

		double tempC = getHeatC();
		double co2Frac = getCO2FillFraction();
		double instability = getCoreInstabilityMultiplier();

		// Magnox/Zirnox cladding is the limiting material.
		// Above roughly 500C, it starts taking accelerated damage.
		if(tempC > CLADDING_DAMAGE_TEMP_C) {
			int damage = (int)Math.ceil((tempC - CLADDING_DAMAGE_TEMP_C) / 8.0D);
			damage = Math.max(1, damage);
			damage = (int)Math.ceil(damage * instability);
			claddingDamage += damage;
		}

		// Fuel damage region. This represents fuel swelling, failed cans,
		// contamination of the CO2 loop, and general core ugliness.
		if(tempC > FUEL_DAMAGE_TEMP_C) {
			int damage = (int)Math.ceil((tempC - FUEL_DAMAGE_TEMP_C) / 5.0D);
			damage = Math.max(1, damage);
			claddingDamage += damage;
			graphiteDamage += damage / 2;
		}

		// Graphite damage/oxidation risk. CO2 protects the graphite; loss of CO2
		// and air ingress make the same temperature much more dangerous.
		if(tempC > GRAPHITE_DAMAGE_TEMP_C) {
			int damage = (int)Math.ceil((tempC - GRAPHITE_DAMAGE_TEMP_C) / 10.0D);
			damage = Math.max(1, damage);

			if(co2Frac < 0.35D)
				damage *= 3;

			if(airIngress > 0)
				damage += Math.max(1, airIngress / 100);

			graphiteDamage += damage;
		}

		// Air ingress model.
		// Low CO2 cover at high temperature implies air/oxygen entering the core.
		if(tempC > 450.0D && co2Frac < 0.20D) {
			airIngress += co2Frac < 0.08D ? 6 : 2;
		} else if(co2Frac > 0.70D && airIngress > 0) {
			// Refilled/purged CO2 slowly suppresses the air-ingress condition.
			airIngress--;
		}

		// Hot venting/loss of gas plus water on the secondary side can represent
		// a damaged exchanger/water ingress event. It is not normal operation.
		if(water.getFill() > 0 && tempC > 520.0D && co2Frac < 0.12D) {
			int shock = (int)Math.ceil((tempC - 520.0D) / 8.0D);
			shock = Math.max(1, shock);

			claddingDamage += shock * 12;
			graphiteDamage += shock * 4;
			graphiteHeat += shock * 20;

			// Steam/hot gas shock adds temporary pressure stress.
			pressure += barToPressure(Math.min(3.0D, shock * 0.12D));
		}

		if(graphiteDamage < 0)
			graphiteDamage = 0;

		if(claddingDamage < 0)
			claddingDamage = 0;

		if(airIngress < 0)
			airIngress = 0;
	}

	private boolean hasFuelRod(int id) {
		if(slots[id] != null) {
			if(slots[id].getItem() instanceof ItemZirnoxRod) {
				final EnumZirnoxType num = EnumUtil.grabEnumSafely(EnumZirnoxType.class, slots[id].getItemDamage());

				if(num == null)
					return false;

				return !num.breeding;
			}
		}

		return false;
	}

	private boolean hasBreedingRod(int id) {
		if(slots[id] != null) {
			if(slots[id].getItem() instanceof ItemZirnoxRod) {
				final EnumZirnoxType num = EnumUtil.grabEnumSafely(EnumZirnoxType.class, slots[id].getItemDamage());

				if(num == null)
					return false;

				return num.breeding;
			}
		}

		return false;
	}

	private int getNeighbourCount(int id) {

		int[] neighbours = this.getNeighbouringSlots(id);

		if(neighbours == null)
			return 0;

		int count = 0;

		for(int i = 0; i < neighbours.length; i++) {
			if(hasFuelRod(neighbours[i]))
				count++;
		}

		return count;
	}

	private int getNeighbourAbsorberCount(int id) {

		int[] neighbours = this.getNeighbouringSlots(id);

		if(neighbours == null)
			return 0;

		int count = 0;

		for(int i = 0; i < neighbours.length; i++) {
			if(hasBreedingRod(neighbours[i]))
				count++;
		}

		return count;
	}

	// itemstack in slots[id] has to contain ItemZirnoxRod
	private int decay(int id) {

		final EnumZirnoxType num = EnumUtil.grabEnumSafely(EnumZirnoxType.class, slots[id].getItemDamage());

		if(num == null)
			return 0;

		int neighbourFuel = getNeighbourCount(id);
		int absorberNeighbours = getNeighbourAbsorberCount(id);

		double neutronFlux = neighbourFuel;

		if(!num.breeding)
			neutronFlux += 1.0D;

		// Breeder/target rods absorb neutrons and reduce nearby reactivity.
		neutronFlux -= absorberNeighbours * 0.35D;

		if(neutronFlux <= 0.0D)
			return 0;

		double controlFactor = clampDouble((100.0D - controlRodInsertion) / 100.0D, 0.0D, 1.0D);

		if(controlFactor <= 0.0D)
			return 0;

		double slotFlux = getFluxFactor(id);
		double fuelHeat = getFuelHeatMultiplier(num);

		double effectiveFlux = neutronFlux * slotFlux * controlFactor;

		if(effectiveFlux <= 0.0D)
			return 0;

		// Breeding/target rods should process under flux but should not behave
		// like main power fuel.
		double breedingHeatPenalty = num.breeding ? 0.15D : 1.0D;

		int heatAdded = (int)Math.round(num.heat * effectiveFlux * fuelHeat * breedingHeatPenalty);

		if(heatAdded < 0)
			heatAdded = 0;

		graphiteHeat += heatAdded;

		// Lifetime increment is now based on local neutron flux, not merely
		// hard neighbor count. Natural uranium still burns through relatively
		// fast because Magnox has low burnup.
		int lifeIncrements = (int)Math.round(effectiveFlux * getFuelBurnMultiplier(num));

		if(lifeIncrements < 1)
			lifeIncrements = 1;

		for(int i = 0; i < lifeIncrements; i++) {
			ItemZirnoxRod.incrementLifeTime(slots[id]);

			if(ItemZirnoxRod.getLifeTime(slots[id]) > num.maxLife) {
				ItemStack result = fuelMap.get(new ComparableStack(getStackInSlot(id)));

				if(result != null) {
					slots[id] = result.copy();
				} else {
					slots[id] = null;
				}

				break;
			}
		}

		// Exotic fuels are allowed, but punish hot operation.
		if(heatAdded > 0 && getHeatC() > 450.0D) {
			double instability = getFuelInstabilityMultiplier(num) - 1.0D;

			if(instability > 0.0D) {
				claddingDamage += (int)Math.ceil(instability * heatAdded * 0.03D);
			}
		}

		return heatAdded;
	}

	private double getFluxFactor(int id) {
		if(id >= 0 && id < FLUX.length)
			return FLUX[id];

		return 1.0D;
	}

	private double getFuelHeatMultiplier(EnumZirnoxType type) {
		switch(type) {
			case NATURAL_URANIUM_FUEL:
				return 0.75D;
			case URANIUM_FUEL:
				return 1.00D;
			case TH232:
				return 0.05D;
			case THORIUM_FUEL:
				return 0.65D;
			case MOX_FUEL:
				return 1.30D;
			case PLUTONIUM_FUEL:
				return 1.55D;
			case U233_FUEL:
				return 1.25D;
			case U235_FUEL:
				return 1.40D;
			case LES_FUEL:
				return 1.10D;
			case LITHIUM:
				return 0.02D;
			case ZFB_MOX:
				return 1.35D;
			default:
				return 1.0D;
		}
	}

	private double getFuelBurnMultiplier(EnumZirnoxType type) {
		switch(type) {
			case NATURAL_URANIUM_FUEL:
				return 1.30D;
			case URANIUM_FUEL:
				return 1.15D;
			case TH232:
				return 0.75D;
			case THORIUM_FUEL:
				return 1.05D;
			case MOX_FUEL:
				return 1.55D;
			case PLUTONIUM_FUEL:
				return 1.75D;
			case U233_FUEL:
				return 1.45D;
			case U235_FUEL:
				return 1.60D;
			case LES_FUEL:
				return 1.25D;
			case LITHIUM:
				return 0.70D;
			case ZFB_MOX:
				return 1.60D;
			default:
				return 1.0D;
		}
	}

	private double getFuelInstabilityMultiplier(EnumZirnoxType type) {
		switch(type) {
			case NATURAL_URANIUM_FUEL:
				return 1.00D;
			case URANIUM_FUEL:
				return 1.05D;
			case TH232:
				return 1.00D;
			case THORIUM_FUEL:
				return 1.10D;
			case MOX_FUEL:
				return 1.35D;
			case PLUTONIUM_FUEL:
				return 1.55D;
			case U233_FUEL:
				return 1.25D;
			case U235_FUEL:
				return 1.45D;
			case LES_FUEL:
				return 1.15D;
			case LITHIUM:
				return 1.00D;
			case ZFB_MOX:
				return 1.45D;
			default:
				return 1.0D;
		}
	}

	private double getCoreInstabilityMultiplier() {

		double instability = 0.0D;
		int count = 0;

		for(int i = 0; i < 24; i++) {
			if(slots[i] != null && slots[i].getItem() instanceof ItemZirnoxRod) {
				final EnumZirnoxType num = EnumUtil.grabEnumSafely(EnumZirnoxType.class, slots[i].getItemDamage());

				if(num != null && !num.breeding) {
					instability += getFuelInstabilityMultiplier(num) - 1.0D;
					count++;
				}
			}
		}

		if(count <= 0)
			return 1.0D;

		return 1.0D + instability / count;
	}

	private void checkIfMeltdown() {

		if(this.pressure > maxPressure) {
			meltdown(MELTDOWN_OVERPRESSURE);
			return;
		}

		if(this.heat > maxHeat || this.graphiteHeat > maxHeat + 5000) {
			meltdown(MELTDOWN_OVERHEAT);
			return;
		}

		if(this.graphiteDamage >= MAX_GRAPHITE_DAMAGE) {
			meltdown(MELTDOWN_GRAPHITE_FIRE);
			return;
		}

		if(this.claddingDamage >= MAX_CLADDING_DAMAGE && getHeatC() > 620.0D) {
			meltdown(MELTDOWN_WATER_INGRESS);
			return;
		}

		if(getHeatC() > GRAPHITE_FIRE_TEMP_C && airIngress > 12000) {
			meltdown(MELTDOWN_GRAPHITE_FIRE);
		}
	}

	private void spawnDebris(DebrisType type) {

		EntityZirnoxDebris debris = new EntityZirnoxDebris(worldObj, xCoord + 0.5D, yCoord + 4D, zCoord + 0.5D, type);
		debris.motionX = worldObj.rand.nextGaussian() * 0.75D;
		debris.motionZ = worldObj.rand.nextGaussian() * 0.75D;
		debris.motionY = 0.01D + worldObj.rand.nextDouble() * 1.25D;

		if(type == DebrisType.CONCRETE) {
			debris.motionX *= 0.25D;
			debris.motionY += worldObj.rand.nextDouble();
			debris.motionZ *= 0.25D;
		}

		if(type == DebrisType.EXCHANGER) {
			debris.motionX += 0.5D;
			debris.motionY *= 0.1D;
			debris.motionZ += 0.5D;
		}

		worldObj.spawnEntityInWorld(debris);
	}

	private void zirnoxDebris(int type) {

		for(int i = 0; i < 2; i++) {
			spawnDebris(DebrisType.EXCHANGER);
		}

		for(int i = 0; i < 20; i++) {
			spawnDebris(DebrisType.CONCRETE);
			spawnDebris(DebrisType.BLANK);
		}

		for(int i = 0; i < 10; i++) {
			spawnDebris(DebrisType.ELEMENT);
			spawnDebris(DebrisType.GRAPHITE);
			spawnDebris(DebrisType.SHRAPNEL);
		}

		if(type == MELTDOWN_GRAPHITE_FIRE) {
			for(int i = 0; i < 20; i++) {
				spawnDebris(DebrisType.GRAPHITE);
			}
		}
	}

	private void meltdown(int type) {

		for(int i = 0; i < slots.length; i++) {
			this.slots[i] = null;
		}

		int[] dimensions = { 1, 0, 2, 2, 2, 2 };

		worldObj.setBlock(this.xCoord, this.yCoord, this.zCoord, ModBlocks.zirnox_destroyed, this.getBlockMetadata(), 3);
		MultiblockHandlerXR.fillSpace(worldObj, this.xCoord, this.yCoord, this.zCoord, dimensions, ModBlocks.zirnox_destroyed, ForgeDirection.getOrientation(this.getBlockMetadata() - BlockDummyable.offset));

		float blast = 3.0F;
		int waste = 45;

		switch(type) {
			case MELTDOWN_OVERPRESSURE:
				blast = 5.5F;
				waste = 35;
				break;
			case MELTDOWN_OVERHEAT:
				blast = 3.0F;
				waste = 50;
				break;
			case MELTDOWN_GRAPHITE_FIRE:
				blast = 2.5F;
				waste = 60;
				break;
			case MELTDOWN_WATER_INGRESS:
				blast = 6.0F;
				waste = 50;
				break;
			default:
				blast = 3.0F;
				waste = 45;
				break;
		}

		worldObj.playSoundEffect(xCoord, yCoord + 2, zCoord, "hbm:block.rbmk_explosion", 10.0F, 0.85F);
		worldObj.createExplosion(null, this.xCoord, this.yCoord + 3, this.zCoord, blast, true);

		zirnoxDebris(type);

		// Dirty reactor failure. Lower blast than before, but greater radiological mess.
		ExplosionNukeGeneric.waste(worldObj, this.xCoord, this.yCoord, this.zCoord, waste);

		List<EntityPlayer> players = worldObj.getEntitiesWithinAABB(EntityPlayer.class,
																	AxisAlignedBB.getBoundingBox(xCoord + 0.5, yCoord + 0.5, zCoord + 0.5, xCoord + 0.5, yCoord + 0.5, zCoord + 0.5).expand(100, 100, 100));

		for(EntityPlayer player : players) {
			player.triggerAchievement(MainRegistry.achZIRNOXBoom);
		}

		if(MobConfig.enableElementals) {
			for(EntityPlayer player : players) {
				player.getEntityData().getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG).setBoolean("radMark", true);
			}
		}
	}

	private void updateConnections() {
		for(DirPos pos : getConPos()) {
			this.trySubscribe(water.getTankType(), worldObj, pos.getX(), pos.getY(), pos.getZ(), pos.getDir());
			this.trySubscribe(carbonDioxide.getTankType(), worldObj, pos.getX(), pos.getY(), pos.getZ(), pos.getDir());
		}
	}

	private DirPos[] getConPos() {
		ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata() - BlockDummyable.offset);
		ForgeDirection rot = dir.getRotation(ForgeDirection.UP);

		return new DirPos[] {
			new DirPos(this.xCoord + rot.offsetX * 3, this.yCoord + 1, this.zCoord + rot.offsetZ * 3, rot),
			new DirPos(this.xCoord + rot.offsetX * 3, this.yCoord + 3, this.zCoord + rot.offsetZ * 3, rot),
			new DirPos(this.xCoord + rot.offsetX * -3, this.yCoord + 1, this.zCoord + rot.offsetZ * -3, rot.getOpposite()),
			new DirPos(this.xCoord + rot.offsetX * -3, this.yCoord + 3, this.zCoord + rot.offsetZ * -3, rot.getOpposite())
		};
	}

	public List<FluidTank> getTanks() {
		List<FluidTank> list = new ArrayList<FluidTank>();

		list.add(steam);
		list.add(carbonDioxide);
		list.add(water);

		return list;
	}

	public AxisAlignedBB getRenderBoundingBox() {
		return AxisAlignedBB.getBoundingBox(xCoord - 2, yCoord, zCoord - 2, xCoord + 3, yCoord + 5, zCoord + 3);
	}

	@SideOnly(Side.CLIENT)
	public double getMaxRenderDistanceSquared() {
		return 65536.0D;
	}

	@Override
	public boolean hasPermission(EntityPlayer player) {
		return Vec3.createVectorHelper(xCoord - player.posX, yCoord - player.posY, zCoord - player.posZ).lengthVector() < 20;
	}

	@Override
	public void receiveControl(NBTTagCompound data) {

		if(data.hasKey("control")) {
			this.isOn = !this.isOn;
			this.controlRodInsertion = this.isOn ? 0 : 100;
		}

		if(data.hasKey("scram")) {
			scram();
		}

		if(data.hasKey("controlRodInsertion")) {
			setControlRodInsertion(data.getInteger("controlRodInsertion"));
		}

		if(data.hasKey("vent")) {
			ventCarbonDioxide(1000);
		}

		this.markDirty();
	}

	private void ventCarbonDioxide(int amount) {

		int fill = this.carbonDioxide.getFill();
		this.carbonDioxide.setFill(fill - amount);

		if(this.carbonDioxide.getFill() < 0)
			this.carbonDioxide.setFill(0);

		// Emergency venting saves pressure, but hot venting risks air ingress
		// and contaminates the primary circuit.
		double tempC = getHeatC();

		if(tempC > 450.0D) {
			airIngress += (int)Math.ceil((tempC - 400.0D) / 10.0D);
			claddingDamage += (int)Math.ceil((tempC - 400.0D) / 15.0D);
		}

		updatePressureFromCO2();
	}

	private void scram() {
		this.controlRodInsertion = 100;
		this.isOn = false;
	}

	private void setControlRodInsertion(int insertion) {
		this.controlRodInsertion = clamp(insertion, 0, 100);
		this.isOn = this.controlRodInsertion < 100;
	}

	@Override
	public FluidTank[] getSendingTanks() {
		return new FluidTank[] { steam };
	}

	@Override
	public FluidTank[] getReceivingTanks() {
		return new FluidTank[] { water, carbonDioxide };
	}

	@Override
	public FluidTank[] getAllTanks() {
		return new FluidTank[] { water, steam, carbonDioxide };
	}

	// ==========================
	// HELPERS
	// ==========================

	private double getHeatC() {
		return heat * 1.0E-5D * TEMP_RANGE_C + TEMP_BASE_C;
	}

	private double getGraphiteHeatC() {
		return graphiteHeat * 1.0E-5D * TEMP_RANGE_C + TEMP_BASE_C;
	}

	private double getPressureBar() {
		return pressure * 1.0E-5D * MAX_PRESSURE_BAR;
	}

	private int barToPressure(double bar) {
		return (int)Math.round(bar / MAX_PRESSURE_BAR * maxPressure);
	}

	private double getCO2FillFraction() {
		if(carbonDioxide.getMaxFill() <= 0)
			return 0.0D;

		return clampDouble((double)carbonDioxide.getFill() / (double)carbonDioxide.getMaxFill(), 0.0D, 1.0D);
	}

	private static int clamp(int value, int min, int max) {
		if(value < min)
			return min;

		if(value > max)
			return max;

		return value;
	}

	private static double clampDouble(double value, double min, double max) {
		if(value < min)
			return min;

		if(value > max)
			return max;

		return value;
	}

	// ==========================
	// OpenComputers
	// ==========================

	@Override
	@Optional.Method(modid = "OpenComputers")
	public String getComponentName() {
		return "zirnox_reactor";
	}

	@Callback(direct = true)
	@Optional.Method(modid = "OpenComputers")
	public Object[] getTemp(Context context, Arguments args) {
		return new Object[] { heat };
	}

	@Callback(direct = true)
	@Optional.Method(modid = "OpenComputers")
	public Object[] getTempC(Context context, Arguments args) {
		return new Object[] { getHeatC() };
	}

	@Callback(direct = true)
	@Optional.Method(modid = "OpenComputers")
	public Object[] getGraphiteTemp(Context context, Arguments args) {
		return new Object[] { graphiteHeat };
	}

	@Callback(direct = true)
	@Optional.Method(modid = "OpenComputers")
	public Object[] getGraphiteTempC(Context context, Arguments args) {
		return new Object[] { getGraphiteHeatC() };
	}

	@Callback(direct = true)
	@Optional.Method(modid = "OpenComputers")
	public Object[] getPressure(Context context, Arguments args) {
		return new Object[] { pressure };
	}

	@Callback(direct = true)
	@Optional.Method(modid = "OpenComputers")
	public Object[] getPressureBar(Context context, Arguments args) {
		return new Object[] { getPressureBar() };
	}

	@Callback(direct = true)
	@Optional.Method(modid = "OpenComputers")
	public Object[] getWater(Context context, Arguments args) {
		return new Object[] { water.getFill() };
	}

	@Callback(direct = true)
	@Optional.Method(modid = "OpenComputers")
	public Object[] getSteam(Context context, Arguments args) {
		return new Object[] { steam.getFill() };
	}

	@Callback(direct = true)
	@Optional.Method(modid = "OpenComputers")
	public Object[] getCarbonDioxide(Context context, Arguments args) {
		return new Object[] { carbonDioxide.getFill() };
	}

	@Callback(direct = true)
	@Optional.Method(modid = "OpenComputers")
	public Object[] isActive(Context context, Arguments args) {
		return new Object[] { isOn };
	}

	@Callback(direct = true)
	@Optional.Method(modid = "OpenComputers")
	public Object[] getControlRodInsertion(Context context, Arguments args) {
		return new Object[] { controlRodInsertion };
	}

	@Callback(direct = true)
	@Optional.Method(modid = "OpenComputers")
	public Object[] getGraphiteDamage(Context context, Arguments args) {
		return new Object[] { graphiteDamage };
	}

	@Callback(direct = true)
	@Optional.Method(modid = "OpenComputers")
	public Object[] getCladdingDamage(Context context, Arguments args) {
		return new Object[] { claddingDamage };
	}

	@Callback(direct = true)
	@Optional.Method(modid = "OpenComputers")
	public Object[] getAirIngress(Context context, Arguments args) {
		return new Object[] { airIngress };
	}

	@Callback(direct = true)
	@Optional.Method(modid = "OpenComputers")
	public Object[] getDecayHeat(Context context, Arguments args) {
		return new Object[] { decayHeat };
	}

	@Callback(direct = true)
	@Optional.Method(modid = "OpenComputers")
	public Object[] getActivePower(Context context, Arguments args) {
		return new Object[] { activePower };
	}

	@Callback(direct = true)
	@Optional.Method(modid = "OpenComputers")
	public Object[] getCooling(Context context, Arguments args) {
		return new Object[] { co2Cooling };
	}

	@Callback(direct = true)
	@Optional.Method(modid = "OpenComputers")
	public Object[] getInfo(Context context, Arguments args) {
		return new Object[] {
			heat,
			pressure,
			water.getFill(),
			steam.getFill(),
			carbonDioxide.getFill(),
			isOn,
			controlRodInsertion,
			graphiteHeat,
			graphiteDamage,
			claddingDamage,
			airIngress,
			decayHeat,
			activePower,
			co2Cooling
		};
	}

	@Callback(direct = true, limit = 4)
	@Optional.Method(modid = "OpenComputers")
	public Object[] setActive(Context context, Arguments args) {
		boolean active = args.checkBoolean(0);

		if(active) {
			this.controlRodInsertion = 0;
			this.isOn = true;
		} else {
			scram();
		}

		return new Object[] {};
	}

	@Callback(direct = true, limit = 4)
	@Optional.Method(modid = "OpenComputers")
	public Object[] setControlRodInsertion(Context context, Arguments args) {
		setControlRodInsertion(args.checkInteger(0));
		return new Object[] { controlRodInsertion };
	}

	@Callback(direct = true, limit = 4)
	@Optional.Method(modid = "OpenComputers")
	public Object[] scram(Context context, Arguments args) {
		scram();
		return new Object[] {};
	}

	@Override
	@Optional.Method(modid = "OpenComputers")
	public String[] methods() {
		return new String[] {
			"getTemp",
			"getTempC",
			"getGraphiteTemp",
			"getGraphiteTempC",
			"getPressure",
			"getPressureBar",
			"getWater",
			"getSteam",
			"getCarbonDioxide",
			"isActive",
			"getControlRodInsertion",
			"getGraphiteDamage",
			"getCladdingDamage",
			"getAirIngress",
			"getDecayHeat",
			"getActivePower",
			"getCooling",
			"getInfo",
			"setActive",
			"setControlRodInsertion",
			"scram"
		};
	}

	@Override
	@Optional.Method(modid = "OpenComputers")
	public Object[] invoke(String method, Context context, Arguments args) throws Exception {
		switch(method) {
			case ("getTemp"):
				return getTemp(context, args);
			case ("getTempC"):
				return getTempC(context, args);
			case ("getGraphiteTemp"):
				return getGraphiteTemp(context, args);
			case ("getGraphiteTempC"):
				return getGraphiteTempC(context, args);
			case ("getPressure"):
				return getPressure(context, args);
			case ("getPressureBar"):
				return getPressureBar(context, args);
			case ("getWater"):
				return getWater(context, args);
			case ("getSteam"):
				return getSteam(context, args);
			case ("getCarbonDioxide"):
				return getCarbonDioxide(context, args);
			case ("isActive"):
				return isActive(context, args);
			case ("getControlRodInsertion"):
				return getControlRodInsertion(context, args);
			case ("getGraphiteDamage"):
				return getGraphiteDamage(context, args);
			case ("getCladdingDamage"):
				return getCladdingDamage(context, args);
			case ("getAirIngress"):
				return getAirIngress(context, args);
			case ("getDecayHeat"):
				return getDecayHeat(context, args);
			case ("getActivePower"):
				return getActivePower(context, args);
			case ("getCooling"):
				return getCooling(context, args);
			case ("getInfo"):
				return getInfo(context, args);
			case ("setActive"):
				return setActive(context, args);
			case ("setControlRodInsertion"):
				return setControlRodInsertion(context, args);
			case ("scram"):
				return scram(context, args);
		}

		throw new NoSuchMethodException();
	}

	@Override
	public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new ContainerReactorZirnox(player.inventory, this);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Object provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new GUIReactorZirnox(player.inventory, this);
	}

	@Override
	public void provideExtraInfo(NBTTagCompound data) {
		data.setDouble(CompatEnergyControl.D_HEAT_C, Math.round(heat * 1.0E-5D * TEMP_RANGE_C + TEMP_BASE_C));
		data.setDouble(CompatEnergyControl.D_MAXHEAT_C, Math.round(maxHeat * 1.0E-5D * TEMP_RANGE_C + TEMP_BASE_C));
		data.setLong(CompatEnergyControl.L_PRESSURE_BAR, Math.round(getPressureBar()));
		data.setDouble(CompatEnergyControl.D_CONSUMPTION_MB, output);
		data.setDouble(CompatEnergyControl.D_OUTPUT_MB, output);
	}
}
