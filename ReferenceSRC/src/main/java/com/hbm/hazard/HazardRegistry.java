package com.hbm.hazard;

import static com.hbm.blocks.ModBlocks.*;
import static com.hbm.items.ModItems.*;
import static com.hbm.inventory.OreDictManager.*;

import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.generic.BlockBobble.BobbleType;
import com.hbm.config.GeneralConfig;
import com.hbm.hazard.modifier.*;
import com.hbm.hazard.transformer.*;
import com.hbm.hazard.type.*;
import com.hbm.inventory.OreDictManager.DictFrame;
import com.hbm.inventory.material.MaterialShapes;
import com.hbm.items.ModItems;
import com.hbm.items.machine.ItemBreedingRod.BreedingRodType;
import com.hbm.items.machine.ItemPWRFuel.EnumPWRFuel;
import com.hbm.items.machine.ItemRTGPelletDepleted.DepletedRTGMaterial;
import com.hbm.items.machine.ItemWatzPellet.EnumWatzType;
import com.hbm.items.machine.ItemZirnoxRod.EnumZirnoxType;
import com.hbm.items.special.ItemHolotapeImage.EnumHoloImage;
import com.hbm.util.Compat;
import com.hbm.util.Compat.ReikaIsotope;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

@SuppressWarnings("unused") //shut the fuck up
public class HazardRegistry {

	//CO60		             5a		β−	030.00Rad/s	Spicy
	//SR90		            29a		β−	015.00Rad/s Spicy
	//TC99		       211,000a		β−	002.75Rad/s	Spicy
	//I181		           192h		β−	150.00Rad/s	2 much spice :(
	//XE135		             9h		β−	aaaaaaaaaaaaaaaa
	//CS137		            30a		β−	020.00Rad/s	Spicy
	//AU198		            64h		β−	500.00Rad/s	2 much spice :(
	//PB209		             3h		β−	10,000.00Rad/s mama mia my face is melting off
	//AT209		             5h		β+	like 7.5k or sth idk bruv
	//PO210		           138d		α	075.00Rad/s	Spicy
	//RA226		         1,600a		α	007.50Rad/s
	//AC227		            22a		β−	030.00Rad/s Spicy
	//TH232		14,000,000,000a		α	000.10Rad/s
	//U233		       160,000a		α	005.00Rad/s
	//U235		   700,000,000a		α	001.00Rad/s
	//U238		 4,500,000,000a		α	000.25Rad/s
	//NP237		     2,100,000a		α	002.50Rad/s
	//PU238		            88a		α	010.00Rad/s	Spicy
	//PU239		        24,000a		α	005.00Rad/s
	//PU240		         6,600a		α	007.50Rad/s
	//PU241		            14a		β−	025.00Rad/s	Spicy
	//		           432a		α	008.50Rad/s
	//AM242		           141a		β−	009.50Rad/s

	//from newguy: if the neutron system is a bit convoluted for you (or i'm just retarded), basically just assign the radiation value here as usual, then in OreDictManager add ".neutron(HazardRegistry.yourmaterial/number you want to divide by", which will take the radiation value assigned here and divide it by the number you put there

	//simplified groups for ReC compat

	//NEW NON TARD METHOD: SET THE MATERIAL RADS TO mSv/s EQUIVALENT
	// =====================================================================================
	// GENERIC RADIOACTIVITY GROUPS (mSv/s)
	// based loosely on half-life / practical hazard
	// =====================================================================================

	public static final float gen_S       = 10_000F;
	public static final float gen_H       = 2_000F;
	public static final float gen_10D     = 100F;
	public static final float gen_100D    = 80F;
	public static final float gen_1Y      = 50F;
	public static final float gen_10Y     = 30F;
	public static final float gen_100Y    = 10F;
	public static final float gen_1K      = 7.5F;
	public static final float gen_10K     = 6.25F;
	public static final float gen_100K    = 5F;
	public static final float gen_1M      = 2.5F;
	public static final float gen_10M     = 1.5F;
	public static final float gen_100M    = 1F;
	public static final float gen_1B      = 0.5F;
	public static final float gen_10B     = 0.1F;


	// =====================================================================================
	// FISSION PRODUCTS / REACTOR ISOTOPES
	// =====================================================================================

	public static final float co60  = 30.0F;      // cobalt-60 (major gamma emitter)
	public static final float sr90  = 15.0F;      // strontium-90
	public static final float tc99  = 2.75F;      // technetium-99
	public static final float i131  = 150.0F;     // iodine-131
	public static final float xe135 = 1250.0F;    // xenon-135 (extremely radioactive)
	public static final float cs137 = 20.0F;      // cesium-137
	public static final float au198 = 500.0F;     // gold-198


	// =====================================================================================
	// EXTREMELY HOT / SHORT-LIVED ISOTOPES
	// =====================================================================================

	public static final float pb209 = 10_000.0F;
	public static final float at209 = 7_500.0F;
	public static final float at    = 1_500.0F;


	// =====================================================================================
	// NATURAL / ALPHA EMITTERS
	// =====================================================================================

	public static final float po210 = 75.0F;
	public static final float ra226 = 7.5F;
	public static final float ac227 = 30.0F;
	public static final float th232 = 0.1F;

	// material groups
	public static final float thf = 1.75F;    // thorium fuel
	public static final float u   = 0.35F;    // natural uranium
	public static final float uf  = 0.5F;     // uranium fuel

	// toxic/heavy metals
	public static final float pm = 1.0F;
	public static final float be = 2.0F;
	public static final float pb = 0.8F;
	public static final float as = 6.0F;
	public static final float hg = 0.9F;
	public static final float tl = 100.0F;

	//terbium
	public static final float tb = 0.5F;


	// =====================================================================================
	// URANIUM ISOTOPES
	// =====================================================================================

	public static final float u233 = 5.0F;
	public static final float u235 = 1.0F;
	public static final float u238 = 0.25F;


	// =====================================================================================
	// NEPTUNIUM
	// =====================================================================================

	public static final float np237 = 2.5F;
	public static final float npf   = 1.5F;


	// =====================================================================================
	// PLUTONIUM
	// =====================================================================================

	public static final float pu    = 7.5F;
	public static final float purg  = 6.25F;     // reactor-grade plutonium

	public static final float pu238 = 10.0F;
	public static final float tm170 = 5F;    // thulium-170
	public static final float pu239 = 5.0F;
	public static final float pu240 = 7.5F;
	public static final float pu241 = 25.0F;

	public static final float puf   = 4.25F;


	// =====================================================================================
	// AMERICIUM
	// =====================================================================================

	public static final float am241 = 8.5F;
	public static final float am242 = 9.5F;

	public static final float amrg  = 9.0F;      // reactor-grade americium
	public static final float amf   = 4.75F;


	// =====================================================================================
	// CURIUM
	// =====================================================================================

	public static final float cm242 = 9.0F;      // fertile, ultra-short half-life, extremely active
	public static final float cm243 = 2.8F;      // fissile, still very hot but much calmer than 242
	public static final float cm244 = 4.5F;      // fertile, major neutron source, reactor-significant
	public static final float cm245 = 0.9F;      // fissile, long-lived, lower specific activity
	public static final float cm246 = 1.4F;      // fertile, moderate
	public static final float cm247 = 0.12F;     // fissile, very long-lived, relatively quiet
	public static final float cm248 = 0.25F;     // fertile, long-lived, relatively quiet

	public static final float cmrg  = 6.0F;      // reactor-grade curium
	public static final float cmf   = 2.2F;      // curium fuel

	// fermium
	public static final float fm255 = 40.0F;   // absurdly hot, short-lived
	public static final float fm257 = 0.35F;   // still dangerous, but vastly cooler


	// =====================================================================================
	// PROMETHIUM
	// =====================================================================================

	public static final float pm147 = 5.0F;


	// =====================================================================================
	// HEAVY TRANSURANICS
	// =====================================================================================

	public static final float bk247 = 10.5F;

	public static final float cf251 = 14.3F;
	public static final float cf252 = 15.3F;
	public static final float cf247 = 12.5F;
	public static final float cf248 = 13.5F;
	public static final float cf249 = 14.0F;

	public static final float es253 = 18.3F;
	public static final float es255 = 19.3F;


	// =====================================================================================
	// FUELS / FICTIONAL / MOD MATERIALS
	// =====================================================================================

	public static final float mox   = 2.5F;

	public static final float sa326 = 15.0F;
	public static final float sa327 = 17.5F;
	public static final float saf   = 5.85F;
	public static final float sas3  = 5.0F;

	public static final float gh336 = 5.0F;
	public static final float mud   = 1.0F;
	//public static final float cn989 = 89.0F;


	//weak, negligible or barely radioactive/hazards

	//rubidium
	public static final float Rb    = 0.001F;
	//samarium
	public static final float Sm    = 0.001F;


	// =====================================================================================
	// SOURCES / SPECIAL MIXTURES
	// =====================================================================================

	public static final float radsource_mult = 3.0F;

	public static final float pobe       = po210 * radsource_mult;
	public static final float rabe       = ra226 * radsource_mult;
	public static final float pube       = pu238 * radsource_mult;

	public static final float zfb_bi     = u235 * 0.35F;
	public static final float zfb_pu241  = pu241 * 0.5F;
	public static final float zfb_am_mix = amrg * 0.5F;


	// =====================================================================================
	// Balefire Bullshit
	// =====================================================================================

	//public static final float bf  = 300_000.0F;
	//public static final float bfb = 500_000.0F;


	// =====================================================================================
	// WORLD MATERIALS / WASTE
	// =====================================================================================

	public static final float sr   = sa326 * 0.1F;
	public static final float sb   = sa326 * 0.1F;

	public static final float trx  = 25.0F;      // transuranic mix
	public static final float trn  = 0.1F;       // trinitite (mostly glass)

	public static final float wst  = 15.0F;      // nuclear waste
	public static final float wstv = 7.5F;       // vitrified waste

	public static final float yc   = u;          // yellowcake uranium
	public static final float fo   = 10F;        // fallout


	// =====================================================================================
	// FORM FACTORS / MULTIPLIERS
	// =====================================================================================

	public static final float nugget      = 0.1F;

	public static final float ingot       = 1.0F;
	public static final float gem         = 1.0F;
	public static final float plate       = ingot;
	public static final float plateCast   = plate * 3;

	public static final float powder_mult = 3.0F;
	public static final float powder      = ingot * powder_mult;
	public static final float powder_tiny = nugget * powder_mult;

	// ores are slightly hotter due to gangue contamination
	public static final float ore         = ingot * 1.2F;
	public static final float specore     = ingot;

	public static final float block       = 10.0F;
	public static final float crystal     = block;

	public static final float billet      = 0.5F;
	public static final float rtg         = billet * 3;

	public static final float rod         = 0.5F;
	public static final float rod_dual    = rod * 2;
	public static final float rod_quad    = rod * 4;
	public static final float rod_rbmk    = rod * 8;

	//europium
	//public static final float eu   = 0.001F;

	public static final HazardTypeBase RADIATION = new HazardTypeRadiation();
	//public static final HazardTypeBase DIGAMMA = new HazardTypeDigamma();
	public static final HazardTypeBase HOT = new HazardTypeHot();
	public static final HazardTypeBase BLINDING = new HazardTypeBlinding();
	public static final HazardTypeBase ASBESTOS = new HazardTypeAsbestos();
	public static final HazardTypeBase COAL = new HazardTypeCoal();
	public static final HazardTypeBase HYDROACTIVE = new HazardTypeHydroactive();
	public static final HazardTypeBase EXPLOSIVE = new HazardTypeExplosive();
	public static final HazardTypeBase AUTISM = new HazardTypeAutism();
	public static final HazardTypeBase GLITCH = new HazardTypeGlitch();
	public static final HazardTypeBase NEUTRON = new HazardTypeNeutron();

	public static void registerItems() {

		// ======================================================
		// EXPLOSIVES / ENERGETICS
		// ======================================================

		// Black powder - burns rapidly, relatively low brisance
				HazardSystem.register(Items.gunpowder,
									  makeData(EXPLOSIVE, 0.75F));

		// TNT block - stable explosive, dangerous but not super shock sensitive
				HazardSystem.register(Blocks.tnt,
									  makeData(EXPLOSIVE, 4F));

		// Fertilizer explosive precursor
		// AN itself is fairly stable unless contaminated/confined
				HazardSystem.register(ModItems.ammonium_nitrate,
									  makeData(EXPLOSIVE, 2F));

		// Dynamite ball (larger charge)
				HazardSystem.register(ball_dynamite,
									  makeData(EXPLOSIVE, 3F));

		// Dynamite stick
		// Nitroglycerin-based = less stable than TNT
				HazardSystem.register(stick_dynamite,
									  makeData(EXPLOSIVE, 2.5F));

		// TNT stick
		// Stable military explosive
				HazardSystem.register(stick_tnt,
									  makeData(EXPLOSIVE, 1.5F));

		// Semtex
		// Stable but powerful plastic explosive
				HazardSystem.register(stick_semtex,
									  makeData(EXPLOSIVE, 2F));

		// C4
		// Very stable explosive, hard to accidentally detonate
				HazardSystem.register(stick_c4,
									  makeData(EXPLOSIVE, 1.75F));

		// Propellant - smokeless powder
		// Fire/explosion hazard more than detonation hazard
				HazardSystem.register(cordite,
									  makeData(EXPLOSIVE, 1.5F));

		// Double-base smokeless powder
				HazardSystem.register(ballistite,
									  makeData(EXPLOSIVE, 1.5F));


		// ======================================================
		// RADIOACTIVE / INDUSTRIAL MATERIALS
		// ======================================================

		// Raffinate + ammonia mix for scandium / yttrium extraction
		// Mild thorium + REE contamination from monazite/xenotime processing
				HazardSystem.register(ModItems.REE_sludge,
									  makeData(RADIATION, 1.5F));


		// ======================================================
		// PARTICULATE / RESPIRATORY HAZARDS
		// ======================================================

		// Coal dust - respiratory hazard (black lung)
		// Fine particulates are the real danger
				HazardSystem.register("dustCoal",
									  makeData(COAL, powder));

				HazardSystem.register("dustTinyCoal",
									  makeData(COAL, powder_tiny));

		// Lignite = dirtier, more particulate contamination
				HazardSystem.register("dustLignite",
									  makeData(COAL, powder * 1.15F));

				HazardSystem.register("dustTinyLignite",
									  makeData(COAL, powder_tiny * 1.15F));

		// ======================================================
		// CRITICALITY / EXOTIC SOURCES
		// ======================================================

		// Exposed plutonium core
		// Dangerous if handled or kept nearby for prolonged periods
				HazardSystem.register(demon_core_open,
									  makeData(RADIATION, 75F));

		// Prompt critical / near-critical configuration
		// Severe gamma + neutron flux
				HazardSystem.register(demon_core_closed,
									  makeData()
										  .addEntry(RADIATION, 5000F)
										  .addEntry(NEUTRON, 2500F));

		// Demon lamp = intentionally absurd hotspot
				HazardSystem.register(lamp_demon,
									  makeData()
										  .addEntry(RADIATION, 5000F)
										  .addEntry(NEUTRON, 2500F));


		// ======================================================
		// RADIOISOTOPE CELLS
		// ======================================================

		// Tritium beta emitter
		// Almost harmless externally unless released
				HazardSystem.register(cell_tritium,
									  makeData(RADIATION, 0.0001F));

		// Exotic luminous/radioactive source
				HazardSystem.register(cell_sas3,
									  makeData()
										  .addEntry(RADIATION, sas3)
										  .addEntry(BLINDING, 60F));


		// ======================================================
		// CONSUMER / NOVELTY ITEMS
		// ======================================================

		// "Radium coffee" joke item
		// Mildly spicy but not insane
				HazardSystem.register(coffee_radium,
									  makeData(RADIATION, 0.05F));

		// Chocolate naturally contains trace radioactivity (potassium)
				HazardSystem.register(chocolate,
									  makeData(RADIATION, 0.001F));


		// ======================================================
		// RAW NUCLEAR WASTE
		// ======================================================

		// Long-lived waste
		// Persistent actinides, lower activity
				HazardSystem.register(nuclear_waste_long,
									  makeData(RADIATION, 2.5F));

				HazardSystem.register(nuclear_waste_long_tiny,
									  makeData(RADIATION, 0.25F));

		// Short-lived waste
		// Fresh fission products = MUCH hotter
				HazardSystem.register(nuclear_waste_short,
									  makeData()
										  .addEntry(RADIATION, 35F)
										  .addEntry(HOT, 5F));

				HazardSystem.register(nuclear_waste_short_tiny,
									  makeData()
										  .addEntry(RADIATION, 3.5F)
										  .addEntry(HOT, 5F));


		// ======================================================
		// DEPLETED / AGED WASTE
		// ======================================================

		// After cooling / partial decay
				HazardSystem.register(nuclear_waste_long_depleted,
									  makeData(RADIATION, 0.5F));

				HazardSystem.register(nuclear_waste_long_depleted_tiny,
									  makeData(RADIATION, 0.05F));

				HazardSystem.register(nuclear_waste_short_depleted,
									  makeData(RADIATION, 2.5F));

				HazardSystem.register(nuclear_waste_short_depleted_tiny,
									  makeData(RADIATION, 0.25F));


		// ======================================================
		// GENERAL NUCLEAR MATERIALS
		// ======================================================

		// Random contaminated reactor scrap
				HazardSystem.register(scrap_nuclear,
									  makeData(RADIATION, 1.5F));

		// Fallout glass
		// Slightly radioactive but mostly safe
				HazardSystem.register(trinitite,
									  makeData(RADIATION, trn * ingot));

				HazardSystem.register(block_trinitite,
									  makeData(RADIATION, trn * block));


		// ======================================================
		// STANDARD NUCLEAR WASTE SYSTEM
		// ======================================================

		// Mixed reactor waste
				HazardSystem.register(nuclear_waste,
									  makeData(RADIATION, wst * ingot));

		// Waste barrel
		// Large concentrated source
				HazardSystem.register(yellow_barrel,
									  makeData(RADIATION, wst * ingot * 6));

		// Waste billet
				HazardSystem.register(billet_nuclear_waste,
									  makeData(RADIATION, wst * billet));

		// Tiny waste fragment
				HazardSystem.register(nuclear_waste_tiny,
									  makeData(RADIATION, wst * nugget));


		// ======================================================
		// VITRIFIED WASTE
		// ======================================================

		// Glassified = safer to handle due to immobilization/shielding
				HazardSystem.register(nuclear_waste_vitrified,
									  makeData(RADIATION, wstv * ingot * 0.75F));

				HazardSystem.register(nuclear_waste_vitrified_tiny,
									  makeData(RADIATION, wstv * nugget * 0.75F));


		// ======================================================
		// WASTE BLOCKS
		// ======================================================

		// Massive concentrated storage blocks
		HazardSystem.register(block_waste,
							  makeData(RADIATION, wst * block));

		HazardSystem.register(block_waste_painted,
							  makeData(RADIATION, wst * block));

		HazardSystem.register(block_waste_vitrified,
							  makeData(RADIATION, wstv * block * 0.75F));

		HazardSystem.register(block_corium, makeData(RADIATION, 150F));
		HazardSystem.register(block_corium_cobble, makeData(RADIATION, 150F));

		HazardSystem.register(scorched_stone, makeData(RADIATION, 0.05F));

		// ======================================================
		// LIGHTLY RADIOACTIVE / INDUSTRIAL MATERIALS
		// ======================================================

		// Rubidium salt
		// Mostly Rb-87 (very long-lived, weak beta emitter)
		// Basically harmless
				HazardSystem.register(
					new ItemStack(ModItems.rubidiumsalt),
					makeData(RADIATION, Rb * nugget)
				);

		// Samarium metal
		// Slight natural radioactivity
				HazardSystem.register(
					new ItemStack(ModItems.ingot_samarium),
					makeData(RADIATION, 0.01F)
				);

		// Samarium-cobalt magnet alloy
		// Same general category as samarium
				HazardSystem.register(
					new ItemStack(ModItems.ingot_smco),
					makeData(RADIATION, 0.01F)
				);


		// ======================================================
		// SUPERTHEHEAVY "THIS SHOULD NOT EXIST"
		// ======================================================

		// Rutherfordium
		// The joke is: this should immediately self-destruct.
		// Preserving insanity, but slightly normalized scaling.
		HazardSystem.register(
			new ItemStack(ModItems.rutherfordium_nugget),
			makeData(RADIATION, 1_000_000F)
				.addEntry(NEUTRON, 250_000F)
				.addEntry(HOT, 100_000F)
				.addEntry(BLINDING, 1F)
				.addEntry(EXPLOSIVE, 1F)
				.addEntry(AUTISM, 5F)
				.addEntry(ASBESTOS, 500F)
				.addEntry(COAL, 500F)
				.addEntry(HYDROACTIVE, 1F)
		);

		// "this FUCKING THING FUCKED UP MY PERIODIC TABLE BY EXPLODING!!!!"

		// Dubnium
		// Still catastrophic, but less ridiculous than Rf
		HazardSystem.register(
			new ItemStack(ModItems.dubnium_nugget),
			makeData(RADIATION, 100_000F)
				.addEntry(NEUTRON, 25_000F)
				.addEntry(HOT, 10_000F)
				.addEntry(BLINDING, 0.5F)
				.addEntry(EXPLOSIVE, 0.5F)
				.addEntry(AUTISM, 2.5F)
				.addEntry(ASBESTOS, 250F)
				.addEntry(COAL, 250F)
				.addEntry(HYDROACTIVE, 0.5F)
		);


		// ======================================================
		// CALIFORNIUM ISOTOPES
		// ======================================================

		// Cf light fraction
		// Mix of Cf-249 / 250 / 251 from SILEX split
		// Neutron spicy but not Cf-252 levels
				HazardSystem.register(
					new ItemStack(cf_light_fraction, 1, 0),
					makeData(RADIATION, cf251 * ingot)
						.addEntry(NEUTRON, 10F)
				);

		// Cf-249
		// Strong alpha emitter, moderate neutron relevance
				HazardSystem.register(
					new ItemStack(nugget_cf249),
					makeData(RADIATION, cf251 * nugget)
						.addEntry(NEUTRON, 2F)
				);

				HazardSystem.register(
					new ItemStack(ingot_cf249),
					makeData(RADIATION, cf251 * ingot)
						.addEntry(NEUTRON, 8F)
				);

		// Cf-250
		// More active, stronger spontaneous fission component
		HazardSystem.register(
			new ItemStack(nugget_cf250),
			makeData(RADIATION, cf252 * nugget)
				.addEntry(NEUTRON, 4F)
		);

		// ======================================================
		// ZIRNOX FUEL REGISTRATION
		// ======================================================

		// Natural uranium fuel
				registerOtherFuel(
					rod_zirnox,
					EnumZirnoxType.NATURAL_URANIUM_FUEL.ordinal(),
					u * rod_dual,
					wst * rod_dual * 9F,
					false
				);

		// Enriched uranium fuel
				registerOtherFuel(
					rod_zirnox,
					EnumZirnoxType.URANIUM_FUEL.ordinal(),
					uf * rod_dual,
					wst * rod_dual * 10F,
					false
				);

		// Thorium breeder blanket
				registerOtherFuel(
					rod_zirnox,
					EnumZirnoxType.TH232.ordinal(),
					th232 * rod_dual,
					thf * rod_dual,
					false
				);

		// Thorium/U233 fuel cycle
		// Cleaner waste profile than plutonium
				registerOtherFuel(
					rod_zirnox,
					EnumZirnoxType.THORIUM_FUEL.ordinal(),
					thf * rod_dual,
					wst * rod_dual * 6F,
					false
				);

		// MOX fuel
		// Higher actinides + plutonium buildup
				registerOtherFuel(
					rod_zirnox,
					EnumZirnoxType.MOX_FUEL.ordinal(),
					mox * rod_dual,
					wst * rod_dual * 12F,
					false
				);

		// Dedicated plutonium fuel
		// Nasty spent fuel
				registerOtherFuel(
					rod_zirnox,
					EnumZirnoxType.PLUTONIUM_FUEL.ordinal(),
					puf * rod_dual,
					wst * rod_dual * 14F,
					false
				);

		// U233 breeder fuel
				registerOtherFuel(
					rod_zirnox,
					EnumZirnoxType.U233_FUEL.ordinal(),
					u233 * rod_dual,
					wst * rod_dual * 8F,
					false
				);

		// U235 fuel
				registerOtherFuel(
					rod_zirnox,
					EnumZirnoxType.U235_FUEL.ordinal(),
					u235 * rod_dual,
					wst * rod_dual * 10F,
					false
				);

		// LES fuel (looks intentionally cursed / exotic)
				registerOtherFuel(
					rod_zirnox,
					EnumZirnoxType.LES_FUEL.ordinal(),
					saf * rod_dual,
					wst * rod_dual * 16F,
					false
				);

		// Lithium breeder
				registerOtherFuel(
					rod_zirnox,
					EnumZirnoxType.LITHIUM.ordinal(),
					0,
					0.0001F * rod_dual,
					false
				);

		// ZFB MOX
				registerOtherFuel(
					rod_zirnox,
					EnumZirnoxType.ZFB_MOX.ordinal(),
					mox * rod_dual,
					wst * rod_dual * 7F,
					false
				);


		// ======================================================
		// DEPLETED ZIRNOX RODS
		// ======================================================

		// Natural uranium
				HazardSystem.register(
					rod_zirnox_natural_uranium_fuel_depleted,
					makeData(RADIATION, wst * rod_dual * 9F)
				);

		// Enriched uranium
				HazardSystem.register(
					rod_zirnox_uranium_fuel_depleted,
					makeData(RADIATION, wst * rod_dual * 10F)
				);

		// Thorium cycle
				HazardSystem.register(
					rod_zirnox_thorium_fuel_depleted,
					makeData(RADIATION, wst * rod_dual * 6F)
				);

		// MOX
				HazardSystem.register(
					rod_zirnox_mox_fuel_depleted,
					makeData(RADIATION, wst * rod_dual * 12F)
				);

		// Plutonium
				HazardSystem.register(
					rod_zirnox_plutonium_fuel_depleted,
					makeData(RADIATION, wst * rod_dual * 14F)
				);

		// U233
				HazardSystem.register(
					rod_zirnox_u233_fuel_depleted,
					makeData(RADIATION, wst * rod_dual * 8F)
				);

		// U235
				HazardSystem.register(
					rod_zirnox_u235_fuel_depleted,
					makeData(RADIATION, wst * rod_dual * 10F)
				);

		// LES
				HazardSystem.register(
					rod_zirnox_les_fuel_depleted,
					makeData()
						.addEntry(RADIATION, wst * rod_dual * 16F)
						.addEntry(BLINDING, 20F)
				);

		// Tritium rod
				HazardSystem.register(
					rod_zirnox_tritium,
					makeData(RADIATION, 0.0001F * rod_dual)
				);

		// ZFB MOX
		HazardSystem.register(
			rod_zirnox_zfb_mox_depleted,
			makeData(RADIATION, wst * rod_dual * 7F)
		);

		// ========================================================================
		// ZIRNOX FUEL RODS
		// radiation = fresh fuel handling hazard (mSv-equivalent)
		// waste = depleted/spent rod handling hazard
		// ========================================================================

		// --- Uranium Cycle ---
				registerOtherFuel(
					rod_zirnox,
					EnumZirnoxType.NATURAL_URANIUM_FUEL.ordinal(),
					u * rod_dual,
					wst * rod_dual * 12F,
					false
				);

				registerOtherFuel(
					rod_zirnox,
					EnumZirnoxType.URANIUM_FUEL.ordinal(),
					uf * rod_dual,
					wst * rod_dual * 10F,
					false
				);

				registerOtherFuel(
					rod_zirnox,
					EnumZirnoxType.U233_FUEL.ordinal(),
					u233 * rod_dual,
					wst * rod_dual * 11F,
					false
				);

				registerOtherFuel(
					rod_zirnox,
					EnumZirnoxType.U235_FUEL.ordinal(),
					u235 * rod_dual,
					wst * rod_dual * 11F,
					false
				);

		// --- Thorium Cycle ---
				registerOtherFuel(
					rod_zirnox,
					EnumZirnoxType.TH232.ordinal(),
					th232 * rod_dual,
					thf * rod_dual,
					false
				);

				registerOtherFuel(
					rod_zirnox,
					EnumZirnoxType.THORIUM_FUEL.ordinal(),
					thf * rod_dual,
					wst * rod_dual * 8F,
					false
				);

		// --- Plutonium / MOX ---
				registerOtherFuel(
					rod_zirnox,
					EnumZirnoxType.MOX_FUEL.ordinal(),
					mox * rod_dual,
					wst * rod_dual * 14F,
					false
				);

				registerOtherFuel(
					rod_zirnox,
					EnumZirnoxType.PLUTONIUM_FUEL.ordinal(),
					puf * rod_dual,
					wst * rod_dual * 13F,
					false
				);

		// --- Schrabidium / Exotic ---
				registerOtherFuel(
					rod_zirnox,
					EnumZirnoxType.LES_FUEL.ordinal(),
					saf * rod_dual,
					wst * rod_dual * 16F,
					false
				);

				registerOtherFuel(
					rod_zirnox,
					EnumZirnoxType.ZFB_MOX.ordinal(),
					mox * rod_dual,
					wst * rod_dual * 6F,
					false
				);

		// --- Utility / Non-fissile ---
				registerOtherFuel(
					rod_zirnox,
					EnumZirnoxType.LITHIUM.ordinal(),
					0F,
					0.001F * rod_dual,
					false
				);

		// ========================================================================
		// DEPLETED FUEL RODS
		// ========================================================================

				HazardSystem.register(
					rod_zirnox_natural_uranium_fuel_depleted,
					makeData(RADIATION, wst * rod_dual * 12F)
				);

				HazardSystem.register(
					rod_zirnox_uranium_fuel_depleted,
					makeData(RADIATION, wst * rod_dual * 10F)
				);

				HazardSystem.register(
					rod_zirnox_thorium_fuel_depleted,
					makeData(RADIATION, wst * rod_dual * 8F)
				);

				HazardSystem.register(
					rod_zirnox_mox_fuel_depleted,
					makeData(RADIATION, wst * rod_dual * 14F)
				);

				HazardSystem.register(
					rod_zirnox_plutonium_fuel_depleted,
					makeData(RADIATION, wst * rod_dual * 13F)
				);

				HazardSystem.register(
					rod_zirnox_u233_fuel_depleted,
					makeData(RADIATION, wst * rod_dual * 11F)
				);

				HazardSystem.register(
					rod_zirnox_u235_fuel_depleted,
					makeData(RADIATION, wst * rod_dual * 11F)
				);

				HazardSystem.register(
					rod_zirnox_les_fuel_depleted,
					makeData()
						.addEntry(RADIATION, wst * rod_dual * 16F)
						.addEntry(BLINDING, 20F)
				);

				HazardSystem.register(
					rod_zirnox_tritium,
					makeData(RADIATION, 0.001F * rod_dual)
				);

				HazardSystem.register(
					rod_zirnox_zfb_mox_depleted,
					makeData(RADIATION, wst * rod_dual * 6F)
				);

		// ========================================================================
		// WASTE PRODUCTS / REPROCESSING BILLETS
		// ========================================================================

				registerOtherWaste(waste_natural_uranium, wst * billet * 12F);
				registerOtherWaste(waste_uranium,         wst * billet * 10F);

				registerOtherWaste(waste_thorium,         wst * billet * 8F);

				registerOtherWaste(waste_mox,             wst * billet * 14F);
				registerOtherWaste(waste_plutonium,       wst * billet * 13F);

				registerOtherWaste(waste_u233,            wst * billet * 11F);
				registerOtherWaste(waste_u235,            wst * billet * 11F);

				registerOtherWaste(waste_schrabidium,     wst * billet * 16F);
				registerOtherWaste(waste_zfb_mox,         wst * billet * 6F);

		// ========================================================================
		// FUEL PLATES
		// ========================================================================

				registerOtherFuel(
					plate_fuel_u233,
					u233 * ingot,
					wst * ingot * 11F,
					false
				);

				registerOtherFuel(
					plate_fuel_u235,
					u235 * ingot,
					wst * ingot * 10F,
					false
				);

				registerOtherFuel(
					plate_fuel_mox,
					mox * ingot,
					wst * ingot * 14F,
					false
				);

				registerOtherFuel(
					plate_fuel_pu239,
					pu239 * ingot,
					wst * ingot * 13F,
					false
				);

				//registerOtherFuel(
				//	plate_fuel_sa326,
				//	sa326 * ingot,
				//	wst * ingot * 16F,
				//	true
				//);

		// Alpha-neutron RTG / source plates
		registerOtherFuel(
			plate_fuel_ra226be,
			rabe * billet,
			pobe * nugget * 3F,
			false
		);

		registerOtherFuel(
			plate_fuel_pu238be,
			pube * billet,
			pube * nugget,
			false
		);

		// ========================================================================
		// SPENT FUEL PLATES / REPROCESSING WASTE
		// ========================================================================

				registerOtherWaste(waste_plate_u233,   wst * ingot * 11F);
				registerOtherWaste(waste_plate_u235,   wst * ingot * 10F);

				registerOtherWaste(waste_plate_mox,    wst * ingot * 14F);
				registerOtherWaste(waste_plate_pu239,  wst * ingot * 13F);

				registerOtherWaste(waste_plate_sa326,  wst * ingot * 16F);

		// Alpha-neutron rad source waste
				registerRadSourceWaste(
					waste_plate_ra226be,
					pobe * nugget * 3F
				);

				registerRadSourceWaste(
					waste_plate_pu238be,
					pube * nugget
				);

		// ========================================================================
		// REACTOR DEBRIS / MELTDOWN MATERIAL
		// ========================================================================

		// Highly contaminated irradiated graphite moderator
		// (Chernobyl-style graphite chunks)
				HazardSystem.register(
					debris_graphite,
					makeData()
						.addEntry(RADIATION, 120F)
						.addEntry(HOT, 5F)
				);

		// Contaminated reactor structural metal
		// piping, vessel fragments, activated steel
				HazardSystem.register(
					debris_metal,
					makeData(RADIATION, 12F)
				);

		// Melted fuel / corium fragments
		// "do not touch under any circumstances"
				HazardSystem.register(
					debris_fuel,
					makeData()
						.addEntry(RADIATION, 1500F)
						.addEntry(HOT, 8F)
				);

		// Fallout contaminated / neutron activated concrete
				HazardSystem.register(
					debris_concrete,
					makeData(RADIATION, 20F)
				);

		// Heat exchanger contamination
		// activated coolant residue + metal activation
				HazardSystem.register(
					debris_exchanger,
					makeData(RADIATION, 35F)
				);

		// Low-grade irradiated fragments
				HazardSystem.register(
					debris_shrapnel,
					makeData(RADIATION, 5F)
				);

		// Extremely contaminated reactor internals
		// control elements, vessel internals, mystery hell chunks
		HazardSystem.register(
			debris_element,
			makeData(RADIATION, 250F)
		);

		// ========================================================================
		// PROCESSED FUEL MATERIALS
		// ========================================================================

		// --- Uranium Fuel ---
		// Moderately radioactive enriched fuel material
				HazardSystem.register(
					nugget_uranium_fuel,
					makeData(RADIATION, uf * nugget)
				);
				HazardSystem.register(
					billet_uranium_fuel,
					makeData(RADIATION, uf * billet)
				);
				HazardSystem.register(
					ingot_uranium_fuel,
					makeData(RADIATION, uf * ingot)
				);
				HazardSystem.register(
					block_uranium_fuel,
					makeData(RADIATION, uf * block)
				);

		// --- Plutonium Fuel ---
		// More radiotoxic and generally nastier to handle
				HazardSystem.register(
					nugget_plutonium_fuel,
					makeData(RADIATION, puf * nugget)
				);
				HazardSystem.register(
					billet_plutonium_fuel,
					makeData(RADIATION, puf * billet)
				);
				HazardSystem.register(
					ingot_plutonium_fuel,
					makeData(RADIATION, puf * ingot)
				);
				HazardSystem.register(
					block_plutonium_fuel,
					makeData(RADIATION, puf * block)
				);

		// --- Thorium Fuel ---
		// Relatively mild handling hazard
				HazardSystem.register(
					nugget_thorium_fuel,
					makeData(RADIATION, thf * nugget)
				);
				HazardSystem.register(
					billet_thorium_fuel,
					makeData(RADIATION, thf * billet)
				);
				HazardSystem.register(
					ingot_thorium_fuel,
					makeData(RADIATION, thf * ingot)
				);
				HazardSystem.register(
					block_thorium_fuel,
					makeData(RADIATION, thf * block)
				);

		// ========================================================================
		// PROTACTINIUM
		// ========================================================================

		// Likely Pa-233 (thorium breeding intermediate)
		// Strong gamma emitter, nasty to handle, short-lived
		HazardSystem.register(
			nugget_protactinium,
			makeData(RADIATION, 8F)
		);

		// ========================================================================
		// NEPTUNIUM FUEL
		// ========================================================================
		// More radioactive than uranium fuel,
		// less nasty than plutonium fuel.

				HazardSystem.register(
					nugget_neptunium_fuel,
					makeData(RADIATION, npf * nugget)
				);

				HazardSystem.register(
					billet_neptunium_fuel,
					makeData(RADIATION, npf * billet)
				);

				HazardSystem.register(
					ingot_neptunium_fuel,
					makeData(RADIATION, npf * ingot)
				);


		// ========================================================================
		// MOX FUEL (Mixed Oxide Fuel)
		// ========================================================================
		// Uranium + plutonium mix.
		// Hotter than uranium fuel and unpleasant to handle.

				HazardSystem.register(
					nugget_mox_fuel,
					makeData(RADIATION, mox * nugget)
				);

				HazardSystem.register(
					billet_mox_fuel,
					makeData(RADIATION, mox * billet)
				);

				HazardSystem.register(
					ingot_mox_fuel,
					makeData(RADIATION, mox * ingot)
				);

				HazardSystem.register(
					block_mox_fuel,
					makeData(RADIATION, mox * block)
				);


		// ========================================================================
		// AMERICIUM FUEL
		// ========================================================================
		// Quite nasty radiologically.
		// Should feel spicier than plutonium fuel.

		HazardSystem.register(
			nugget_americium_fuel,
			makeData(RADIATION, amf * nugget)
		);

		HazardSystem.register(
			billet_americium_fuel,
			makeData(RADIATION, amf * billet)
		);

		HazardSystem.register(
			ingot_americium_fuel,
			makeData(RADIATION, amf * ingot)
		);
		// ========================================================================
		// HES / LES FUEL MATERIALS
		// ========================================================================
		// Highly enriched schrabidium fuel.
		// Nasty but still processable in industry.

				HazardSystem.register(
					nugget_hes,
					makeData(RADIATION, saf * nugget)
				);

				HazardSystem.register(
					billet_hes,
					makeData(RADIATION, saf * billet)
				);

				HazardSystem.register(
					ingot_hes,
					makeData(RADIATION, saf * ingot)
				);


		// Low enriched schrabidium
				HazardSystem.register(
					nugget_les,
					makeData(RADIATION, saf * nugget * 0.75F)
				);

				HazardSystem.register(
					billet_les,
					makeData(RADIATION, saf * billet * 0.75F)
				);

				HazardSystem.register(
					ingot_les,
					makeData(RADIATION, saf * ingot * 0.75F)
				);


		// ========================================================================
		// FLASH MATERIALS / EXOTIC HOT MATERIALS
		// ========================================================================

		// Flash gold (Au-198)
		// Strong gamma emitter but short-lived
				HazardSystem.register(
					billet_balefire_gold,
					makeData()
						.addEntry(RADIATION, au198 * billet)
						.addEntry(NEUTRON, au198 * billet * 0.01F)
				);


		// Flashlead (Pb-209)
		// Extremely unstable, violently radioactive
				HazardSystem.register(
					billet_flashlead,
					makeData()
						.addEntry(RADIATION, pb209 * billet * 1.15F)
						.addEntry(HOT, 10F)
						.addEntry(NEUTRON, pb209 * billet * 0.001F)
				);


		// ========================================================================
		// ALPHA-NEUTRON SOURCES
		// ========================================================================
		// Radiation lowered slightly relative to neutron hazard.
		// Main danger is neutron flux.

		HazardSystem.register(
			billet_po210be,
			makeData()
				.addEntry(RADIATION, pobe * billet)
				.addEntry(NEUTRON, pobe * billet * 0.15F)
		);

		HazardSystem.register(
			billet_ra226be,
			makeData()
				.addEntry(RADIATION, rabe * billet)
				.addEntry(NEUTRON, rabe * billet * 0.12F)
		);

		HazardSystem.register(
			billet_pu238be,
			makeData()
				.addEntry(RADIATION, pube * billet)
				.addEntry(NEUTRON, pube * billet * 0.18F)
		);

		// ======================================================
		// RTG PELLETS
		// ======================================================

		// Standard Pu-238 RTG (realistic strong alpha RTG)
				registerRTGPellet(pellet_rtg, pu238 * rtg, 0, 2F);

		// Ra-226 RTG (old-school, dirty, weaker efficiency)
				registerRTGPellet(pellet_rtg_radium, ra226 * rtg, 0);

		// Weak RTG (Pu-238 + depleted uranium blend)
				registerRTGPellet(
					pellet_rtg_weak,
					((pu238 * 0.35F) + (u238 * 2F)) * billet,
					0
				);

		// Industrial beta RTGs
				registerRTGPellet(pellet_rtg_strontium, sr90 * rtg, 0);
				registerRTGPellet(pellet_rtg_cobalt, co60 * rtg, 0);

		// High-power niche RTGs
				registerRTGPellet(pellet_rtg_actinium, ac227 * rtg, 0);

		// Extremely dangerous compact alpha RTG
				registerRTGPellet(pellet_rtg_polonium, po210 * rtg, 0, 3F);

		// Flash materials / unstable exotic heat sources
				registerRTGPellet(
					pellet_rtg_lead,
					pb209 * rtg * 0.35F,
					0,
					6F,
					40F
				);

				registerRTGPellet(
					pellet_rtg_gold,
					au198 * rtg * 0.4F,
					0,
					4F
				);

		// Low-power long-life isotopic RTGs
				registerRTGPellet(pellet_rtg_americium, am241 * rtg, 0);
				registerRTGPellet(pellet_rtg_promethium, pm147 * rtg, 0);

		// Future Curium
		// registerRTGPellet(pellet_rtg_curium, cm244 * rtg, 0);

		// ======================================================
		// TRANSURANIC / EXOTIC RTGs
		// ======================================================

		// Californium is nasty but don't let it instantly exceed demon-core territory
				registerRTGPellet(pellet_rtg_cf251, cf251 * rtg * 0.85F, 0);
				registerRTGPellet(pellet_rtg_cf252, cf252 * rtg, 0);



		// Berkelium experimental fuel
				registerRTGPellet(pellet_rtg_berkelium, bk247 * rtg * 0.9F, 0);

				//tm170
				registerRTGPellet(pellet_rtg_tm170, tm170 * rtg * 0.9F, 0);

		// Depleted RTG remains
				HazardSystem.register(
					new ItemStack(
						pellet_rtg_depleted,
						1,
						DepletedRTGMaterial.NEPTUNIUM.ordinal()
					),
					makeData(RADIATION, np237 * rtg)
				);

				HazardSystem.register(
					new ItemStack(
						pellet_rtg_depleted,
						1,
						DepletedRTGMaterial.BK247.ordinal()
					),
					makeData(RADIATION, bk247 * rtg * 0.5F)

				);

				HazardSystem.register(
					new ItemStack(
						pellet_rtg_depleted,
						1,
						DepletedRTGMaterial.CM248.ordinal()
					),
					makeData(RADIATION, cm248 * rtg * 0.5F) //0.5?
				);

				HazardSystem.register(
					new ItemStack(
						pellet_rtg_depleted,
						1,
						DepletedRTGMaterial.SAMARIUM.ordinal()
					),
					makeData(RADIATION, Sm * rtg * 0.5F) //weak
				);

				HazardSystem.register(
					new ItemStack(
						pellet_rtg_depleted,
						1,
						DepletedRTGMaterial.CALIFORNIUM249.ordinal()
					),
					makeData(RADIATION, cf249 * rtg * 0.5F) //apparently 14
				);


		// ======================================================
		// REACTOR PILE RODS
		// ======================================================

		// Natural uranium pile rod
				HazardSystem.register(
					pile_rod_uranium,
					makeData(RADIATION, u * billet * 3F)
				);

		// Pu239 breeding rod
				HazardSystem.register(
					pile_rod_pu239,
					makeData(
						RADIATION,
						!GeneralConfig.enable528
							? (purg * billet * 0.75F) + (pu239 * billet)
							: (purg * billet * 0.75F) + (wst * billet)
					)
				);

		// Reactor-grade plutonium source rod
				HazardSystem.register(
					pile_rod_plutonium,
					makeData()
						.addEntry(
							RADIATION,
							!GeneralConfig.enable528
								? (purg * billet * 1.25F) + (u * billet)
								: (purg * billet * 1.25F) + (wst * billet)
						)
						.addEntry(
							NEUTRON,
							rabe * billet * 0.08F
						)
				);

		// Dedicated neutron source rod
		HazardSystem.register(
			pile_rod_source,
			makeData()
				.addEntry(
					RADIATION,
					rabe * billet * 1.75F
				)
				.addEntry(
					NEUTRON,
					rabe * billet * 0.15F
				)
		);

		// ======================================================
		// LOW / CONTAINED SOURCES
		// ======================================================

				registerBreedingRodRadiation(BreedingRodType.TRITIUM, 0.001F);

		// ======================================================
		// FISSION PRODUCTS / ACTIVATION PRODUCTS
		// ======================================================

				registerBreedingRodRadiation(BreedingRodType.CO60, co60);
				registerBreedingRodRadiation(BreedingRodType.RA226, ra226);
				registerBreedingRodRadiation(BreedingRodType.AC227, ac227);

		// ======================================================
		// THORIUM / URANIUM CYCLE
		// ======================================================

				registerBreedingRodRadiation(BreedingRodType.TH232, th232);
				registerBreedingRodRadiation(BreedingRodType.THF, thf);

				registerBreedingRodRadiation(BreedingRodType.URANIUM, u);
				registerBreedingRodRadiation(BreedingRodType.U235, u235);
				registerBreedingRodRadiation(BreedingRodType.U238, u238);
				registerBreedingRodRadiation(BreedingRodType.NP237, np237);

		// ======================================================
		// PLUTONIUM CYCLE
		// ======================================================

		// Encapsulated fuel -> slightly safer than loose material
				registerBreedingRodRadiation(BreedingRodType.PU238, pu238 * 0.85F);
				registerBreedingRodRadiation(BreedingRodType.PU239, pu239 * 0.9F);
				registerBreedingRodRadiation(BreedingRodType.RGP, purg * 0.9F);

		// Spent fuel
				registerBreedingRodRadiation(BreedingRodType.WASTE, wst);

		// ======================================================
		// AMERICIUM / CURIUM SERIES
		// ======================================================

				registerBreedingRodRadiation(BreedingRodType.AM241, am241);
				registerBreedingRodRadiation(BreedingRodType.AM242, am242);

				registerBreedingRodRadiation(BreedingRodType.CM242, cm242); // fertile
				registerBreedingRodRadiation(BreedingRodType.CM243, cm243); // fissile
				registerBreedingRodRadiation(BreedingRodType.CM244, cm244); // fertile
				registerBreedingRodRadiation(BreedingRodType.CM245, cm245); // fissile
				registerBreedingRodRadiation(BreedingRodType.CM246, cm246); // fertile
				registerBreedingRodRadiation(BreedingRodType.CM247, cm247); // fissile
				registerBreedingRodRadiation(BreedingRodType.FM255, fm255);
				registerBreedingRodRadiation(BreedingRodType.FM257, fm257);

		// ======================================================
		// HEAVY TRANSURANICS
		// ======================================================

		registerBreedingRodRadiation(
			BreedingRodType.BK247,
			bk247 * 0.9F
		);

		// ======================================================
		// URANIUM CYCLE
		// ======================================================

				registerRBMKRod(rbmk_fuel_ueu,     u * rod_rbmk,      wst * rod_rbmk * 6.0F);
				registerRBMKRod(rbmk_fuel_meu,     uf * rod_rbmk,     wst * rod_rbmk * 7.0F);
				registerRBMKRod(rbmk_fuel_heu233,  u233 * rod_rbmk,   wst * rod_rbmk * 10.0F);
				registerRBMKRod(rbmk_fuel_heu235,  u235 * rod_rbmk,   wst * rod_rbmk * 9.5F);
				registerRBMKRod(rbmk_fuel_thmeu,   thf * rod_rbmk,    wst * rod_rbmk * 5.5F);

		// ======================================================
		// PLUTONIUM / MOX
		// ======================================================

				registerRBMKRod(rbmk_fuel_lep,     puf * rod_rbmk,    wst * rod_rbmk * 8.5F);
				registerRBMKRod(rbmk_fuel_mep,     purg * rod_rbmk,   wst * rod_rbmk * 10.0F);
				registerRBMKRod(rbmk_fuel_hep239,  pu239 * rod_rbmk,  wst * rod_rbmk * 11.0F);
				registerRBMKRod(rbmk_fuel_hep241,  pu241 * rod_rbmk,  wst * rod_rbmk * 12.5F);
				registerRBMKRod(rbmk_fuel_mox,     mox * rod_rbmk,    wst * rod_rbmk * 8.0F);

		// ======================================================
		// AMERICIUM
		// ======================================================

				registerRBMKRod(rbmk_fuel_lea,     amf * rod_rbmk,    wst * rod_rbmk * 9.0F);
				registerRBMKRod(rbmk_fuel_mea,     amrg * rod_rbmk,   wst * rod_rbmk * 10.5F);
				registerRBMKRod(rbmk_fuel_hea241,  am241 * rod_rbmk,  wst * rod_rbmk * 12.0F);
				registerRBMKRod(rbmk_fuel_hea242,  am242 * rod_rbmk,  wst * rod_rbmk * 13.0F);

		// ======================================================
		// TRANSURANICS
		// ======================================================

				registerRBMKRod(rbmk_fuel_bk247,   bk247 * rod_rbmk,  wst * rod_rbmk * 15.0F);

				registerRBMKRod(rbmk_fuel_men,     npf * rod_rbmk,    wst * rod_rbmk * 7.5F);
				registerRBMKRod(rbmk_fuel_hen,     np237 * rod_rbmk,  wst * rod_rbmk * 10.0F);

		// ======================================================
		// SCHRABIDIUM
		// ======================================================

				registerRBMKRod(rbmk_fuel_les,     saf * rod_rbmk,    wst * rod_rbmk * 8.5F);
				registerRBMKRod(rbmk_fuel_mes,     saf * rod_rbmk,    wst * rod_rbmk * 11.0F);
				registerRBMKRod(rbmk_fuel_hes,     saf * rod_rbmk,    wst * rod_rbmk * 18.0F);

		// ======================================================
		// AUSSEN / SPECIAL
		// ======================================================

				registerRBMKRod(rbmk_fuel_leaus,   0F,                wst * rod_rbmk * 12.5F);
				registerRBMKRod(rbmk_fuel_heaus,   0F,                wst * rod_rbmk * 10.5F);

		// ======================================================
		// RADIOISOTOPE / BERYLLIUM SOURCES
		// ======================================================

				registerRBMKRod(
					rbmk_fuel_po210be,
					pobe * rod_rbmk,
					pobe * rod_rbmk * 0.06F,
					true
				);

				registerRBMKRod(
					rbmk_fuel_ra226be,
					rabe * rod_rbmk,
					rabe * rod_rbmk * 0.18F,
					true
				);

				registerRBMKRod(
					rbmk_fuel_pu238be,
					pube * rod_rbmk,
					wst * rod_rbmk * 1.75F
				);

		// ======================================================
		// EXOTIC / FLASH MATERIALS
		// ======================================================

		// registerRBMKRod(rbmk_fuel_balefire_gold, au198 * rod_rbmk, bf * rod_rbmk * 0.5F, true);

				registerRBMKRod(
					rbmk_fuel_flashlead,
					pb209 * 1.25F * rod_rbmk,
					pb209 * nugget * 0.025F * rod_rbmk,
					true
				);

		// registerRBMKRod(rbmk_fuel_balefire, bf * rod_rbmk, bf * rod_rbmk * 100F, true);

		// ======================================================
		// ZFB
		// ======================================================

				registerRBMKRod(
					rbmk_fuel_zfb_bismuth,
					pu241 * rod_rbmk * 0.1F,
					wst * rod_rbmk * 3.5F
				);

				registerRBMKRod(
					rbmk_fuel_zfb_pu241,
					pu239 * rod_rbmk * 0.1F,
					wst * rod_rbmk * 5.0F
				);

				registerRBMKRod(
					rbmk_fuel_zfb_am_mix,
					pu241 * rod_rbmk * 0.1F,
					wst * rod_rbmk * 6.5F
				);

		// ======================================================
		// CURIUM
		// ======================================================

		registerRBMKRod(
			rbmk_fuel_lecm,
			cmrg * 2.2F * rod_rbmk / 5F,
			wst * rod_rbmk * 2F
		);

		registerRBMKRod(
			rbmk_fuel_mecm,
			cmrg * 2.2F * rod_rbmk / 3F,
			wst * rod_rbmk * 4F
		);

		registerRBMKRod(
			rbmk_fuel_hecm,
			cmrg * 2.2F * rod_rbmk / 1.5F,
			wst * rod_rbmk * 6F
		);


		// ======================================================
		// URANIUM CYCLE
		// ======================================================

				registerRBMKPellet(rbmk_pellet_ueu,      u * billet,      wst * billet * 6.0F);
				registerRBMKPellet(rbmk_pellet_meu,      uf * billet,     wst * billet * 7.0F);
				registerRBMKPellet(rbmk_pellet_heu233,   u233 * billet,   wst * billet * 10.0F);
				registerRBMKPellet(rbmk_pellet_heu235,   u235 * billet,   wst * billet * 9.5F);
				registerRBMKPellet(rbmk_pellet_thmeu,    thf * billet,    wst * billet * 5.5F);

		// ======================================================
		// PLUTONIUM / MOX
		// ======================================================

				registerRBMKPellet(rbmk_pellet_lep,      puf * billet,    wst * billet * 8.5F);
				registerRBMKPellet(rbmk_pellet_mep,      purg * billet,   wst * billet * 10.0F);
				registerRBMKPellet(rbmk_pellet_hep239,   pu239 * billet,  wst * billet * 11.0F);
				registerRBMKPellet(rbmk_pellet_hep241,   pu241 * billet,  wst * billet * 12.5F);
				registerRBMKPellet(rbmk_pellet_mox,      mox * billet,    wst * billet * 8.0F);

		// ======================================================
		// AMERICIUM
		// ======================================================

				registerRBMKPellet(rbmk_pellet_lea,      amf * billet,    wst * billet * 9.0F);
				registerRBMKPellet(rbmk_pellet_mea,      amrg * billet,   wst * billet * 10.5F);
				registerRBMKPellet(rbmk_pellet_hea241,   am241 * billet,  wst * billet * 12.0F);
				registerRBMKPellet(rbmk_pellet_hea242,   am242 * billet,  wst * billet * 13.0F);

		// ======================================================
		// TRANSURANICS
		// ======================================================

				registerRBMKPellet(rbmk_pellet_bk247,    bk247 * billet,  wst * billet * 15.0F);

				registerRBMKPellet(rbmk_pellet_men,      npf * billet,    wst * billet * 7.5F);
				registerRBMKPellet(rbmk_pellet_hen,      np237 * billet,  wst * billet * 10.0F);

		// ======================================================
		// SCHRABIDIUM
		// ======================================================

				registerRBMKPellet(rbmk_pellet_les,      saf * billet,    wst * billet * 8.5F);
				registerRBMKPellet(rbmk_pellet_mes,      saf * billet,    wst * billet * 11.0F);
				registerRBMKPellet(rbmk_pellet_hes,      saf * billet,    wst * billet * 18.0F);

		// ======================================================
		// AUSSEN / SPECIAL
		// ======================================================

				registerRBMKPellet(rbmk_pellet_leaus,    0F,              wst * billet * 12.5F);
				registerRBMKPellet(rbmk_pellet_heaus,    0F,              wst * billet * 10.5F);

		// ======================================================
		// RADIOISOTOPE / BERYLLIUM SOURCES
		// ======================================================

				registerRBMKPellet(
					rbmk_pellet_po210be,
					pobe * billet,
					pobe * billet * 0.06F,
					true
				);

				registerRBMKPellet(
					rbmk_pellet_ra226be,
					rabe * billet,
					rabe * billet * 0.18F,
					true
				);

				registerRBMKPellet(
					rbmk_pellet_pu238be,
					pube * billet,
					wst * billet * 1.75F
				);

		// ======================================================
		// EXOTIC / FLASH MATERIALS
		// ======================================================

		// registerRBMKPellet(rbmk_pellet_balefire_gold, au198 * billet, bf * billet * 0.5F, true);

				registerRBMKPellet(
					rbmk_pellet_flashlead,
					pb209 * 1.25F * billet,
					pb209 * nugget * 0.025F,
					true,
					0,
					0
				);

		// registerRBMKPellet(rbmk_pellet_balefire, bf * billet, bf * billet * 100F, true);

		// ======================================================
		// ZFB
		// ======================================================

				registerRBMKPellet(
					rbmk_pellet_zfb_bismuth,
					pu241 * billet * 0.1F,
					wst * billet * 3.5F
				);

				registerRBMKPellet(
					rbmk_pellet_zfb_pu241,
					pu239 * billet * 0.1F,
					wst * billet * 5.0F
				);

				registerRBMKPellet(
					rbmk_pellet_zfb_am_mix,
					pu241 * billet * 0.1F,
					wst * billet * 6.5F
				);

		// registerRBMKPellet(rbmk_pellet_drx, bf * billet, bf * billet * 100F, true, 0F, 1F/24F);

		// ======================================================
		// CURIUM
		// ======================================================

		registerRBMKPellet(
			rbmk_pellet_lecm,
			cmrg * billet,
			wst * billet * 2.0F
		);

		registerRBMKPellet(
			rbmk_pellet_mecm,
			cmrg * billet,
			wst * billet * 4.0F
		);

		registerRBMKPellet(
			rbmk_pellet_hecm,
			cmrg * billet,
			wst * billet * 6.0F
		);

		// =====================================================================
		// PBR FRESH FUEL PELLETS
		// realistic external radiation profile
		// =====================================================================

		// HALEU 19.75%
		// more U-235 than LEU, still mostly U-238
		HazardSystem.register(
			DictFrame.fromOne(
				ModItems.watz_pellet,
				EnumWatzType.HALEU1975
			),
			makeData(
				RADIATION,
				u235 * ingot * 0.35F
			)
		);

		// HALEU 15%
		HazardSystem.register(
			DictFrame.fromOne(
				ModItems.watz_pellet,
				EnumWatzType.HALEU15
			),
			makeData(
				RADIATION,
				u235 * ingot * 0.28F
			)
		);

		// LEU 5%
		HazardSystem.register(
			DictFrame.fromOne(
				ModItems.watz_pellet,
				EnumWatzType.LEU5
			),
			makeData(
				RADIATION,
				u238 * ingot * 0.20F
			)
		);

		// TH232 fertile pellet
		// thorium is weak externally
		HazardSystem.register(
			DictFrame.fromOne(
				ModItems.watz_pellet,
				EnumWatzType.TH232
			),
			makeData(
				RADIATION,
				th232 * ingot * 0.18F
			)
		);

		// U-233 fuel
		// hotter than uranium fuel because of U232 contamination
		HazardSystem.register(
			DictFrame.fromOne(
				ModItems.watz_pellet,
				EnumWatzType.U233
			),
			makeData(
				RADIATION,
				u233 * ingot * 0.65F
			)
		);

		// MOX (Pu-bearing fuel)
		// hotter than uranium fuel
		HazardSystem.register(
			DictFrame.fromOne(
				ModItems.watz_pellet,
				EnumWatzType.MOX241
			),
			makeData(
				RADIATION,
				pu239 * ingot * 0.90F
			)
		);

		// DU absorber
		HazardSystem.register(
			DictFrame.fromOne(
				ModItems.watz_pellet,
				EnumWatzType.DU
			),
			makeData(
				RADIATION,
				u238 * ingot * 0.10F
			)
		);

		// GT6 compatibility weirdos
		HazardSystem.register(
			DictFrame.fromOne(
				ModItems.watz_pellet,
				EnumWatzType.NQD
			),
			makeData(
				RADIATION,
				u235 * ingot * 0.75F
			)
		);

		HazardSystem.register(
			DictFrame.fromOne(
				ModItems.watz_pellet,
				EnumWatzType.NQR
			),
			makeData(
				RADIATION,
				pu239 * ingot * 1.5F
			)
		);

		//TODOne do all watz_pellet_depleted too

		//spent fuel = hot as hell

		HazardSystem.register(
			DictFrame.fromOne(ModItems.watz_pellet_depleted,
							  EnumWatzType.HALEU1975),
			makeData(RADIATION, saf * ingot * 5.0F)
		);

		HazardSystem.register(
			DictFrame.fromOne(ModItems.watz_pellet_depleted,
							  EnumWatzType.HALEU15),
			makeData(RADIATION, saf * ingot * 4.0F)
		);

		HazardSystem.register(
			DictFrame.fromOne(ModItems.watz_pellet_depleted,
							  EnumWatzType.LEU5),
			makeData(RADIATION, saf * ingot * 3.5F)
		);

		HazardSystem.register(
			DictFrame.fromOne(ModItems.watz_pellet_depleted,
							  EnumWatzType.TH232),
			makeData(RADIATION, np237 * ingot * 4.5F)
		);

		HazardSystem.register(
			DictFrame.fromOne(ModItems.watz_pellet_depleted,
							  EnumWatzType.U233),
			makeData(RADIATION, uf * ingot * 6.0F)
		);

		HazardSystem.register(
			DictFrame.fromOne(ModItems.watz_pellet_depleted,
							  EnumWatzType.MOX241),
			makeData(RADIATION, purg * ingot * 8.0F)
		);

		// GRAPHITE MODERATOR
		// activated graphite (C-14, impurities)
		HazardSystem.register(
			DictFrame.fromOne(
				ModItems.watz_pellet_depleted,
				EnumWatzType.GRAPHITE
			),
			makeData(
				RADIATION,
				ingot * 0.35F
			)
		);

		// LEAD ABSORBER
		// mild neutron activation + contamination
		HazardSystem.register(
			DictFrame.fromOne(
				ModItems.watz_pellet_depleted,
				EnumWatzType.LEAD
			),
			makeData(
				RADIATION,
				u238 * ingot * 0.20F
			)
		);

		// BORON ABSORBER
		// neutron poisoned absorber material
		HazardSystem.register(
			DictFrame.fromOne(
				ModItems.watz_pellet_depleted,
				EnumWatzType.BORON
			),
			makeData(
				RADIATION,
				u238 * ingot * 0.40F
			)
		);

		// DEPLETED URANIUM ABSORBER
		// bred transuranics present (Np/Pu traces)
		HazardSystem.register(
			DictFrame.fromOne(
				ModItems.watz_pellet_depleted,
				EnumWatzType.DU
			),
			makeData(
				RADIATION,
				pu239 * ingot * 0.85F
			)
		);


		//powders
		//public static Item powder_spent_haleu;
		//	public static Item powder_spent_leu;
		//	public static Item powder_spent_thorium;
		//	public static Item powder_spent_u233;
		//	public static Item powder_spent_mox;
		//
		//	public static Item dust_graphite;
		//	public static Item powder_lead_irradiated;
		//	public static Item powder_boron_spent;
		//	public static Item powder_du_spent;

		// =====================================================================================
		// SPENT PBR FUEL POWDERS
		// Powdered = worse handling risk than intact pellets
		// Add nausea/toxic dust for pulverized fuel
		// =====================================================================================

		HazardSystem.register(
			ModItems.powder_spent_haleu,
			makeData()
				.addEntry(RADIATION, u235 * ingot * 4.0F)
				.addEntry(AUTISM, 2.0F)
		);

		HazardSystem.register(
			ModItems.powder_spent_leu,
			makeData()
				.addEntry(RADIATION, u238 * ingot * 2.0F)
				.addEntry(AUTISM, 1.5F)
		);

		HazardSystem.register(
			ModItems.powder_spent_thorium,
			makeData()
				.addEntry(RADIATION, th232 * ingot * 2.5F)
				.addEntry(AUTISM, 2.0F)
		);

		HazardSystem.register(
			ModItems.powder_spent_u233,
			makeData()
				.addEntry(RADIATION, u233 * ingot * 5.0F)
				.addEntry(AUTISM, 3.0F)
		);

		HazardSystem.register(
			ModItems.powder_spent_mox,
			makeData()
				.addEntry(RADIATION, pu239 * ingot * 8.0F)
				.addEntry(AUTISM, 5.0F)
		);

		// =====================================================================================
		// SPENT ABSORBER / MODERATOR POWDERS
		// Mostly neutron activation products
		// =====================================================================================

		HazardSystem.register(
			ModItems.dust_graphite,
			makeData()
				.addEntry(RADIATION, ingot * 0.35F)
				.addEntry(AUTISM, 0.5F)
		);

		HazardSystem.register(
			ModItems.powder_lead_irradiated,
			makeData()
				.addEntry(RADIATION, u238 * nugget * 0.25F)
				.addEntry(AUTISM, 3.0F)
		);

		// Boron-10 -> neutron capture products
		HazardSystem.register(
			ModItems.powder_boron_spent,
			makeData()
				.addEntry(RADIATION, nugget * 0.15F)
				.addEntry(AUTISM, 1.0F)
		);

		// Activated DU absorber
		HazardSystem.register(
			ModItems.powder_du_spent,
			makeData()
				.addEntry(RADIATION, u238 * ingot * 2.5F)
				.addEntry(AUTISM, 2.0F)
		);








		//HazardSystem.register(DictFrame.fromOne(ModItems.watz_pellet, EnumWatzType.PU241),
		//					  makeData(RADIATION, pu241 * ingot * 2.5F));   // higher gamma contributor

		//HazardSystem.register(DictFrame.fromOne(ModItems.watz_pellet, EnumWatzType.AMRG),
		//					  makeData(RADIATION, amrg * ingot * 4.0F));    // Am-241 dominant gamma hazard

		//HazardSystem.register(DictFrame.fromOne(ModItems.watz_pellet, EnumWatzType.CMRG),
		//					  makeData(RADIATION, cmrg * ingot * 8.0F));    // Cm mix, strong neutron source

		//HazardSystem.register(DictFrame.fromOne(ModItems.watz_pellet, EnumWatzType.CMF),
		//					  makeData(RADIATION, cmf * ingot * 12.0F));    // curium fuel grade, very active

		//HazardSystem.register(DictFrame.fromOne(ModItems.watz_pellet, EnumWatzType.BK247),
		//					  makeData(RADIATION, bk247 * ingot * 15.0F));   // high alpha, moderate gamma

		//HazardSystem.register(DictFrame.fromOne(ModItems.watz_pellet, EnumWatzType.CF252),
		//					  makeData(RADIATION, cf252 * ingot * 60.0F));   // extreme neutron emitter (dominant hazard)

		//HazardSystem.register(DictFrame.fromOne(ModItems.watz_pellet, EnumWatzType.ES253),
		//					  makeData(RADIATION, es253 * ingot * 25.0F));   // very high specific activity alpha/gamma




		registerPWRFuel(EnumPWRFuel.MEU, uf * billet * 0.8F);
		registerPWRFuel(EnumPWRFuel.HEU233, u233 * billet * 1.2F);
		registerPWRFuel(EnumPWRFuel.HEU235, u235 * billet * 1.0F);
		registerPWRFuel(EnumPWRFuel.MEN, npf * billet * 1.1F);
		registerPWRFuel(EnumPWRFuel.HEN237, np237 * billet * 1.8F);
		registerPWRFuel(EnumPWRFuel.MOX, mox * billet * 1.4F);
		registerPWRFuel(EnumPWRFuel.MEP, purg * billet * 1.3F);
		registerPWRFuel(EnumPWRFuel.HEP239, pu239 * billet * 1.6F);
		registerPWRFuel(EnumPWRFuel.HEP241, pu241 * billet * 2.4F);
		registerPWRFuel(EnumPWRFuel.MEA, amrg * billet * 3.5F);
		registerPWRFuel(EnumPWRFuel.HEA242, am242 * billet * 2.8F);
		//registerPWRFuel(EnumPWRFuel.HES326, sa326 * billet * 4.5F);
		//registerPWRFuel(EnumPWRFuel.HES327, sa327 * billet * 5.5F);
		registerPWRFuel(EnumPWRFuel.BFB_AM_MIX, amrg * billet * 3.2F);
		registerPWRFuel(EnumPWRFuel.BFB_PU241, pu241 * billet * 2.2F);

		HazardSystem.register(powder_yellowcake, makeData(RADIATION, yc * powder * 0.2F));
		HazardSystem.register(block_yellowcake, makeData(RADIATION, yc * block * powder_mult * 0.25F));
		HazardSystem.register(ModItems.fallout, makeData(RADIATION, fo * powder * 1.5F));
		HazardSystem.register(ModBlocks.fallout, makeData(RADIATION, fo * powder * 3.0F));
		HazardSystem.register(ModBlocks.salted_fallout, makeData(RADIATION, fo * powder * 6.0F));
		HazardSystem.register(ModBlocks.block_fallout, makeData(RADIATION, yc * block * powder_mult * 0.35F));

		HazardSystem.register(powder_caesium, makeData().addEntry(HYDROACTIVE, 20F).addEntry(HOT, 30F).addEntry(RADIATION, 5F));
		HazardSystem.register(ingot_cesium, makeData().addEntry(HYDROACTIVE, 50F).addEntry(HOT, 30F).addEntry(RADIATION, 12F));

		HazardSystem.register(francium_ingot, makeData().addEntry(HYDROACTIVE, 100F).addEntry(HOT, 300F).addEntry(RADIATION, 200000F));


		//zirconium fast breeder billets
		HazardSystem.register(billet_zfb_bismuth, makeData().addEntry(RADIATION, 0.08F * ingot)); // essentially low external hazard, Bi-based breeder matrix
		HazardSystem.register(billet_zfb_pu241, makeData().addEntry(RADIATION, 2.4F * ingot));    // Pu-241 → elevated gamma from Am-241 ingrowth
		HazardSystem.register(billet_zfb_am_mix, makeData().addEntry(RADIATION, 3.0F * ingot));    // Am mix → strong gamma/alpha hazard from Am-241 and Cm isotopes


		HazardSystem.register(powder_beryllium, makeData().addEntry(ASBESTOS, be * powder * 2.5F));
		HazardSystem.register(ingot_beryllium, makeData().addEntry(ASBESTOS, be * ingot * 0.15F));
		HazardSystem.register(block_beryllium, makeData().addEntry(ASBESTOS, be * block * 0.05F));
		HazardSystem.register(billet_beryllium, makeData().addEntry(ASBESTOS, be * billet * 0.1F));
		HazardSystem.register(nugget_beryllium, makeData().addEntry(ASBESTOS, be * nugget * 0.1F));
		HazardSystem.register(crystal_beryllium, makeData().addEntry(ASBESTOS, be * crystal * 0.4F));

		HazardSystem.register(powder_emerald, makeData().addEntry(ASBESTOS, 0.35F * powder));

		HazardSystem.register(ore_beryllium, makeData().addEntry(ASBESTOS, be * ore * 0.6F));

		HazardSystem.register(ore_lead, makeData().addEntry(ASBESTOS, pb * ore * 0.2F));
		HazardSystem.register(powder_lead, makeData().addEntry(ASBESTOS, pb * powder * 2.5F).addEntry(BLINDING, 0.2F));
		HazardSystem.register(ingot_lead, makeData().addEntry(ASBESTOS, pb * ingot * 0.08F));
		HazardSystem.register(block_lead, makeData().addEntry(ASBESTOS, pb * block * 0.03F));
		HazardSystem.register(nugget_lead, makeData().addEntry(ASBESTOS, pb * nugget * 0.08F));
		HazardSystem.register(crystal_lead, makeData().addEntry(ASBESTOS, pb * crystal * 0.12F));

		HazardSystem.register(ore_arsenic, makeData().addEntry(ASBESTOS, as * ore * 0.5F));
		HazardSystem.register(arsenic_trioxide, makeData().addEntry(ASBESTOS, as * powder * 3.5F).addEntry(BLINDING, as * powder * 2.0F).addEntry(AUTISM, as * powder * 2.0F).addEntry(COAL, as * powder * 2.5F));
		HazardSystem.register(ingot_arsenic, makeData().addEntry(ASBESTOS, as * ingot * 1.2F).addEntry(BLINDING, as * ingot * 0.8F).addEntry(AUTISM, as * ingot * 0.8F).addEntry(COAL, as * ingot * 1.0F));
		HazardSystem.register(nugget_arsenic, makeData().addEntry(ASBESTOS, as * nugget * 1.2F).addEntry(BLINDING, as * nugget * 0.8F).addEntry(AUTISM, as * nugget * 0.8F).addEntry(COAL, as * nugget * 1.0F));
		HazardSystem.register(ingot_arsenic_bronze,
							  makeData()
								  .addEntry(ASBESTOS, as * ingot * 0.25F)
								  .addEntry(BLINDING, as * ingot * 0.1F)
								  .addEntry(AUTISM, as * ingot * 0.1F)
								  .addEntry(COAL, as * ingot * 0.15F));
		HazardSystem.register(ingot_gaas, makeData().addEntry(ASBESTOS, as * ingot * 0.35F).addEntry(BLINDING, as * ingot * 0.15F).addEntry(AUTISM, as * ingot * 0.15F).addEntry(COAL, as * ingot * 0.2F));
		HazardSystem.register(silver_gallium_arsenide, makeData().addEntry(ASBESTOS, as * ingot * 0.35F).addEntry(BLINDING, as * ingot * 0.15F).addEntry(AUTISM, as * ingot * 0.15F).addEntry(COAL, as * ingot * 0.2F));
		HazardSystem.register(billet_gaas, makeData().addEntry(ASBESTOS, as * billet * 0.35F).addEntry(BLINDING, as * billet * 0.15F).addEntry(AUTISM, as * billet * 0.15F).addEntry(COAL, as * billet * 0.2F));
		HazardSystem.register(nugget_gaas, makeData().addEntry(ASBESTOS, as * nugget * 0.35F).addEntry(BLINDING, as * nugget * 0.15F).addEntry(AUTISM, as * nugget * 0.15F).addEntry(COAL, as * nugget * 0.2F));
		HazardSystem.register(circuit_arsenic, makeData().addEntry(ASBESTOS, as * nugget * 0.08F).addEntry(BLINDING, as * nugget * 0.04F).addEntry(AUTISM, as * nugget * 0.04F).addEntry(COAL, as * nugget * 0.05F));

		//mercury
				HazardSystem.register(ingot_mercury, makeData().addEntry(ASBESTOS, hg * ingot * 0.8F).addEntry(BLINDING, 0.08F * ingot).addEntry(AUTISM, 0.5F * ingot).addEntry(COAL, 0.35F * ingot));
				HazardSystem.register(nugget_mercury, makeData().addEntry(ASBESTOS, hg * nugget * 0.8F).addEntry(BLINDING, 0.08F * nugget).addEntry(AUTISM, 0.5F * nugget).addEntry(COAL, 0.35F * nugget));
				//RTG
		HazardSystem.register(
			new ItemStack(
				pellet_rtg_depleted,
				1,
				DepletedRTGMaterial.MERCURY.ordinal()
			),
			makeData(ASBESTOS, hg * rtg * 0.8F).addEntry(BLINDING, 0.08F * rtg).addEntry(AUTISM, 0.5F * rtg).addEntry(COAL, 0.35F * rtg)

		);

		//thallium
				HazardSystem.register(ingot_thallium, makeData().addEntry(ASBESTOS, tl * ingot * 1.5F).addEntry(BLINDING, tl * ingot * 1.2F).addEntry(AUTISM, tl * ingot * 1.5F).addEntry(COAL, tl * ingot * 1.4F));
				HazardSystem.register(nugget_thallium, makeData().addEntry(ASBESTOS, tl * nugget * 1.5F).addEntry(BLINDING, tl * nugget * 1.2F).addEntry(AUTISM, tl * nugget * 1.5F).addEntry(COAL, tl * nugget * 1.4F));
				HazardSystem.register(powder_thallium, makeData().addEntry(ASBESTOS, tl * powder * 3.0F).addEntry(BLINDING, tl * powder * 2.5F).addEntry(AUTISM, tl * powder * 3.5F).addEntry(COAL, tl * powder * 3.0F));

		//terbium
		HazardSystem.register(ingot_terbium, makeData().addEntry(ASBESTOS, tb * ingot * 0.08F));
		HazardSystem.register(powder_terbium, makeData().addEntry(ASBESTOS, tb * powder * 0.35F));
		HazardSystem.register(powder_terbium_fluoride, makeData().addEntry(ASBESTOS, tb * powder * 0.6F).addEntry(COAL, 0.15F * powder));
		HazardSystem.register(ingot_terbium_impure, makeData().addEntry(ASBESTOS, tb * ingot * 0.15F));
		HazardSystem.register(powder_terbium_tiny, makeData().addEntry(ASBESTOS, tb * powder * 0.35F));
		HazardSystem.register(terbiumsol, makeData().addEntry(ASBESTOS, tb * ingot * 0.2F));
		HazardSystem.register(powder_terbium_oxide, makeData().addEntry(ASBESTOS, tb * powder * 0.25F));
		HazardSystem.register(powder_terbium2, makeData().addEntry(ASBESTOS, tb * powder * 0.35F));
		HazardSystem.register(fragment_terbium, makeData().addEntry(ASBESTOS, tb * nugget * 0.08F));


		HazardSystem.register(brick_asbestos, makeData(ASBESTOS, 2.5F));
		HazardSystem.register(tile_lab_broken, makeData(ASBESTOS, 1.8F));
		//HazardSystem.register(powder_coltan_ore, makeData(ASBESTOS, 3F));

		//crystals
		HazardSystem.register(crystal_uranium, makeData(RADIATION, u * crystal * 0.6F));
		HazardSystem.register(crystal_thorium, makeData(RADIATION, th232 * crystal * 0.35F));
		HazardSystem.register(crystal_plutonium, makeData(RADIATION, pu * crystal * 2.0F));
		HazardSystem.register(crystal_phosphorus, makeData(HOT, 4F * crystal));
		HazardSystem.register(crystal_lithium, makeData(HYDROACTIVE, 1.5F * crystal));

		//nuke parts
		HazardSystem.register(boy_propellant, makeData(EXPLOSIVE, 2F));

		HazardSystem.register(gadget_core, makeData(RADIATION, pu239 * nugget * 16F));      // Trinity-style Pu pit
		HazardSystem.register(boy_target, makeData(RADIATION, u235 * ingot * 1.5F));        // HEU target
		HazardSystem.register(boy_bullet, makeData(RADIATION, u235 * ingot * 1.2F));        // lower mass than target
		HazardSystem.register(man_core, makeData(RADIATION, pu239 * nugget * 18F));          // Fat Man core
		HazardSystem.register(mike_core, makeData(RADIATION, u238 * nugget * 2.0F));         // mostly tamper material
		HazardSystem.register(tsar_core, makeData(RADIATION, pu239 * nugget * 22F));         // high fissile content
		HazardSystem.register(tsar_corelead, makeData(RADIATION, pu239 * nugget * 6F));      // lead tamped version
		HazardSystem.register(tsar_corereal, makeData(RADIATION, pu239 * nugget * 28F));     // U-238 jacketed "real" Tsar



		//larp bullshit (I think)
		//HazardSystem.register(fleija_propellant, makeData().addEntry(RADIATION, 15F).addEntry(EXPLOSIVE, 8F).addEntry(BLINDING, 50F));
		//HazardSystem.register(fleija_core, makeData(RADIATION, 10F));

		//HazardSystem.register(solinium_propellant, makeData(EXPLOSIVE, 10F));
		//HazardSystem.register(solinium_core, makeData().addEntry(RADIATION, sa327 * nugget * 8).addEntry(BLINDING, 45F));

		/*
		 * Blacklist
		 */
		for(String ore : TH232.all(MaterialShapes.ORE)) HazardSystem.blacklist(ore);
		for(String ore : U.all(MaterialShapes.ORE)) HazardSystem.blacklist(ore);


		/*
		 * ReC compat
		 */
		Item recWaste = Compat.tryLoadItem(Compat.MOD_REC, "reactorcraft_item_waste");
		if(recWaste != null) {
			for(ReikaIsotope i : ReikaIsotope.values()) {
				if(i.getRad() > 0) {
					HazardSystem.register(new ItemStack(recWaste, 1, i.ordinal()), makeData(RADIATION, i.getRad()));
				}
			}
		}

		if(Compat.isModLoaded(Compat.MOD_GT6)) {

			Object[][] data = new Object[][] {
				{"Naquadah", u * 1.2F},
				{"Naquadah-Enriched", u235 * 2.0F},
				{"Naquadria", pu239 * 4.0F},
				};

			for(MaterialShapes shape : MaterialShapes.allShapes) {
				if(!shape.noAutogen) for(String prefix : shape.prefixes) {
					for(Object[] o : data) {
						HazardSystem.register(prefix + o[0],
											  new HazardData()
												  .setMutex(0b1)
												  .addEntry(new HazardEntry(
													  RADIATION,
													  (float)o[1] * shape.q(1) / MaterialShapes.INGOT.q(1)
												  ))
						);
					}
				}
			}
		}
	}

	public static void registerTrafos() {
		HazardSystem.trafos.add(new HazardTransformerRadiationNBT());

		if(!(GeneralConfig.enableLBSM && GeneralConfig.enableLBSMSafeCrates))	HazardSystem.trafos.add(new HazardTransformerRadiationContainer());
		if(!(GeneralConfig.enableLBSM && GeneralConfig.enableLBSMSafeMEDrives))	HazardSystem.trafos.add(new HazardTransformerRadiationME());
	}

	private static HazardData makeData() { return new HazardData(); }
	private static HazardData makeData(HazardTypeBase hazard) { return new HazardData().addEntry(hazard); }
	private static HazardData makeData(HazardTypeBase hazard, float level) { return new HazardData().addEntry(hazard, level); }
	private static HazardData makeData(HazardTypeBase hazard, float level, boolean override) { return new HazardData().addEntry(hazard, level, override); }

	private static void registerPWRFuel(EnumPWRFuel fuel, float baseRad) {
		HazardSystem.register(
			DictFrame.fromOne(ModItems.pwr_fuel, fuel),
			makeData(RADIATION, baseRad)
		);

		HazardSystem.register(
			DictFrame.fromOne(ModItems.pwr_fuel_hot, fuel),
			makeData(RADIATION, baseRad * 35F) // fresh-out-of-reactor spent fuel = extreme gamma field
				.addEntry(HOT, 5F)
				.addEntry(BLINDING, 2F)
		);

		HazardSystem.register(
			DictFrame.fromOne(ModItems.pwr_fuel_depleted, fuel),
			makeData(RADIATION, baseRad * 8F) // cooled spent fuel / depleted fuel still nasty but far lower
		);
	}

	private static void registerRBMKPellet(Item pellet, float base, float dep) { registerRBMKPellet(pellet, base, dep, false, 0F, 0F); }
	private static void registerRBMKPellet(Item pellet, float base, float dep, boolean linear) { registerRBMKPellet(pellet, base, dep, linear, 0F, 0F); }
	private static void registerRBMKPellet(Item pellet, float base, float dep, boolean linear, float blinding, float digamma) {

		HazardData data = new HazardData();

		data.addEntry(
			new HazardEntry(RADIATION, base)
				.addMod(new HazardModifierRBMKRadiation(dep, linear))
		);

		if(blinding > 0)
			data.addEntry(new HazardEntry(BLINDING, blinding * 0.5F));

		HazardSystem.register(pellet, data);
	}

	private static void registerRBMKRod(Item rod, float base, float dep) {
		registerRBMK(rod, base, dep, true, false, 0F, 0F);
	}

	private static void registerRBMKRod(Item rod, float base, float dep, float blinding) {
		registerRBMK(rod, base, dep, true, false, blinding, 0F);
	}

	private static void registerRBMKRod(Item rod, float base, float dep, boolean linear) {
		registerRBMK(rod, base, dep, true, linear, 0F, 0F);
	}

	private static void registerRBMK(Item rod, float base, float dep, boolean hot, boolean linear, float blinding, float digamma) {

		HazardData data = new HazardData();

		data.addEntry(
			new HazardEntry(RADIATION, base)
				.addMod(new HazardModifierRBMKRadiation(dep, linear))
		);

		// thermal heating from reactor operation / burnup
		if(hot)
			data.addEntry(
				new HazardEntry(HOT, 0F)
					.addMod(new HazardModifierRBMKHot())
			);

		// gamma flash / eye damage proxy
		if(blinding > 0)
			data.addEntry(
				new HazardEntry(BLINDING, blinding * 0.5F)
			);

		//if(digamma > 0)
		//	data.addEntry(new HazardEntry(DIGAMMA, digamma));

		HazardSystem.register(rod, data);
	}

	private static void registerBreedingRodRadiation(BreedingRodType type, float base) {
		HazardSystem.register(
			new ItemStack(ModItems.rod, 1, type.ordinal()),
			makeData(RADIATION, base)
		);

		HazardSystem.register(
			new ItemStack(ModItems.rod_dual, 1, type.ordinal()),
			makeData(RADIATION, base * rod_dual * 0.9F)
		);

		HazardSystem.register(
			new ItemStack(ModItems.rod_quad, 1, type.ordinal()),
			makeData(RADIATION, base * rod_quad * 0.85F)
		);
	}

	private static void registerOtherFuel(Item fuel, float base, float target, boolean blinding) {

		HazardData data = new HazardData();

		data.addEntry(
			new HazardEntry(RADIATION, base)
				.addMod(new HazardModifierFuelRadiation(target))
		);

		if(blinding)
			data.addEntry(BLINDING, 4F);

		HazardSystem.register(fuel, data);
	}

	private static void registerOtherFuel(Item fuel, int meta, float base, float target, boolean blinding) {

		HazardData data = new HazardData();

		data.addEntry(
			new HazardEntry(RADIATION, base)
				.addMod(new HazardModifierFuelRadiation(target))
		);

		if(blinding)
			data.addEntry(BLINDING, 4F);

		HazardSystem.register(new ItemStack(fuel, 1, meta), data);
	}

	private static void registerRTGPellet(Item pellet, float base, float target) {
		registerRTGPellet(pellet, base, target, 0F, 0F);
	}

	private static void registerRTGPellet(Item pellet, float base, float target, float hot) {
		registerRTGPellet(pellet, base, target, hot, 0F);
	}

	private static void registerRTGPellet(Item pellet, float base, float target, float hot, float blinding) {

		HazardData data = new HazardData();

		data.addEntry(
			new HazardEntry(RADIATION, base)
				.addMod(new HazardModifierRTGRadiation(target))
		);

		// RTGs are primarily thermal hazards (Pu-238 etc.)
		if(hot > 0)
			data.addEntry(
				new HazardEntry(HOT, hot * 1.5F)
			);

		// only strong gamma emitters should blind significantly
		if(blinding > 0)
			data.addEntry(
				new HazardEntry(BLINDING, blinding * 0.35F)
			);

		HazardSystem.register(pellet, data);
	}

	private static void registerOtherWaste(Item waste, float base) {

		// cooled / stabilized waste
		HazardSystem.register(
			new ItemStack(waste, 1, 0),
			makeData(RADIATION, base * 0.075F)
		);

		HazardData data = new HazardData();

		// fresh/high activity waste
		data.addEntry(new HazardEntry(RADIATION, base));

		// heat roughly follows activity instead of flat constant
		data.addEntry(
			new HazardEntry(HOT,
							Math.max(1.5F, Math.min(base * 0.12F, 12F))
			)
		);

		HazardSystem.register(
			new ItemStack(waste, 1, 1),
			data
		);
	}

	private static void registerRadSourceWaste(Item waste, float base) {

		HazardSystem.register(
			new ItemStack(waste, 1, 0),
			makeData(RADIATION, base)
		);

		HazardData data = new HazardData();

		data.addEntry(new HazardEntry(RADIATION, base));

		data.addEntry(
			new HazardEntry(HOT,
							Math.max(1.5F, Math.min(base * 0.12F, 12F))
			)
		);

		HazardSystem.register(
			new ItemStack(waste, 1, 1),
			data
		);
	}
}
