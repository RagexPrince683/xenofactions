package com.hbm.items;

import com.hbm.blocks.ModBlocks;
import com.hbm.config.BombConfig;
import com.hbm.config.VersatileConfig;
import com.hbm.handler.BucketHandler;
import com.hbm.handler.ToolAbility;
import com.hbm.handler.ToolAbility.LuckAbility;
import com.hbm.handler.WeaponAbility;
import com.hbm.handler.guncfg.*;
import com.hbm.interfaces.ICustomWarhead.SaltedFuel.HalfLifeType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.inventory.material.MaterialShapes;
import com.hbm.inventory.material.Mats;
import com.hbm.items.ItemAmmoEnums.*;
import com.hbm.items.ItemEnums.*;
import com.hbm.items.armor.*;
import com.hbm.items.armor.IArmorDisableModel.EnumPlayerPart;
import com.hbm.items.bomb.*;
import com.hbm.items.food.*;
import com.hbm.items.machine.*;
import com.hbm.items.machine.ItemFELCrystal.EnumWavelengths;
import com.hbm.items.machine.ItemMachineUpgrade.UpgradeType;
import com.hbm.items.machine.ItemPlateFuel.FunctionEnum;
import com.hbm.items.machine.ItemPWRFuel.EnumPWRFuel;
import com.hbm.items.machine.ItemRBMKRod.EnumBurnFunc;
import com.hbm.items.machine.ItemRBMKRod.EnumDepleteFunc;
import com.hbm.items.machine.ItemRTGPelletDepleted.DepletedRTGMaterial;
import com.hbm.items.machine.ItemStamp.StampType;
import com.hbm.items.special.*;
import com.hbm.items.special.ItemPlasticScrap.ScrapType;
import com.hbm.items.tool.*;
import com.hbm.items.tool.ItemToolAbility.EnumToolType;
import com.hbm.items.weapon.*;
import com.hbm.items.weapon.ItemCustomMissilePart.*;
import com.hbm.items.weapon.ItemMissile.MissileFormFactor;
import com.hbm.items.weapon.ItemMissile.MissileFuel;
import com.hbm.items.weapon.ItemMissile.MissileTier;
import com.hbm.items.weapon.sedna.factory.GunFactory;
import com.hbm.lib.RefStrings;
import com.hbm.main.MainRegistry;
import com.hbm.main.RegistrationDiagnostics;
import com.hbm.potion.HbmPotion;
import com.hbm.tileentity.machine.rbmk.IRBMKFluxReceiver.NType;
import com.hbm.util.RTGUtil;
import com.hbm.handler.ToolAbility;

import api.hbm.block.IToolable.ToolType;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.Item.ToolMaterial;
import net.minecraft.item.ItemArmor.ArmorMaterial;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.DamageSource;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.EnumHelper;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;

public class ModItems {



	public static void mainRegistry()
	{
		initializeItem();
		try {
			registerItem();
		} catch(RuntimeException e) {
			RegistrationDiagnostics.failedRegistryEntry("item", "ModItems registration batch", "ModItems", e);
			throw e;
		}
		RegistrationDiagnostics.validateDeclaredEntries(ModItems.class, Item.class, "item");
	}


	public static Item scandium_chloride;
	public static Item scandium_ingot;
	public static Item scandium_nugget;
	public static Item yttrium_ingot;
	public static Item REE_sludge;
	public static Item scandium_oxide;
	public static Item yttrium_oxide;
	public static Item yttrium_chloride;
	public static Item strontium_sulfide;
	public static Item ingot_indium;
	public static Item powder_indium;
	public static Item powder_thallium;
	public static Item ingot_thallium;
	public static Item nugget_indium;
	public static Item nugget_thallium;
	public static Item crystal_zinc;
	public static Item powder_strontium_oxide;
	public static Item strontium_chloride;
	public static Item ingot_gunmetal;
	public static Item magnesium_ingot;
	public static Item magnesium_nugget;
	public static Item slagingot;
	public static Item plate_gunmetal;
	public static Item ingot_weaponsteel;
	public static Item plate_weaponsteel;
	public static Item casing;
	public static Item ingot_raw;
	public static Item part_barrel_light;
	public static Item part_barrel_heavy;
	public static Item part_receiver_light;
	public static Item part_receiver_heavy;
	public static Item part_mechanism;
	public static Item part_stock;
	public static Item part_grip;

	public static Item barium_sulfide;
	public static Item barium_carbonate;
	public static Item barium_oxide;
	public static Item ingot_barium;
	public static Item barium_nugget;
	public static Item rutherfordium_nugget;
	public static Item dubnium_nugget;
	public static Item powder_molybdenite;
	public static Item powder_rhenium;

	public static Item ingot_th232;
	public static Item ingot_uranium;
	public static Item ingot_promethium;
	public static Item ingot_u233;
	public static Item ingot_u235;
	public static Item ingot_u238;
	public static Item ingot_u238m2;
	public static Item ingot_plutonium;
	public static Item ingot_pu238;
	public static Item ingot_pu239;
	public static Item ingot_pu240;
	public static Item ingot_pu241;
	public static Item ingot_pu_mix;
	public static Item ingot_am241;
	public static Item ingot_am242;
	public static Item ingot_am_mix;
	public static Item ingot_cm242;
	public static Item ingot_cm243;
	public static Item ingot_cm244;
	public static Item ingot_cm245;
	public static Item ingot_cm246;
	public static Item ingot_cm247;
	public static Item ingot_cm_fuel;

	public static Item turret_light_ammo;

	public static Item rubidium_ingot;

	public static Item ingot_cm_mix;

	public static Item ingot_bk247;

	public static Item gpmix;

	public static Item ingot_cf251;
	public static Item cf_light_fraction; //used in silex to get cf-249
	public static Item ingot_cf249;
	public static Item ingot_cf248;
	public static Item ingot_yb170;

	public static Item ingot_es253;
	public static Item ingot_es255;

	public static Item ingot_neptunium;
	public static Item ingot_polonium;
	public static Item ingot_technetium;
	public static Item ingot_molybdenum;
	public static Item ingot_co60;
	public static Item ingot_sr90;
	public static Item ingot_au198;
	public static Item ingot_pb209;
	public static Item ingot_ra226;
	public static Item ingot_titanium;
	public static Item ingot_nickel;
	public static Item ingot_cobalt;
	public static Item ingot_gallium;
	public static Item ingot_zinc;
	public static Item ingot_platnium;
	public static Item ingot_gaas;
	public static Item silver_gallium_arsenide;
	public static Item ingot_iridium;
	public static Item powder_iridium;
	public static Item ruthenium_residue;

	public static Item gypsum;
	public static Item sulfur;
	public static Item ingot_palladium;
	public static Item ingot_samarium;
	public static Item ingot_smco;
	public static Item ingot_europium;
	public static Item ingot_terbium;
	public static Item ingot_thulium;
	public static Item ingot_thulium170;
	public static Item nugget_europium;

	public static Item nitra;
	public static Item nitra_small;

	public static Item coke;
	public static Item powder_coke;
	public static Item powder_celestite;
	public static Item lignite;
	public static Item powder_lignite;
	public static Item briquette;
	public static Item coal_infernal;
	public static Item cinnebar;

	//public static Item woodemium_briquette;

	public static Item butter; //////////////////////////////////////////////
	public static Item s_cream;
	public static Item powder_ash;
	public static Item powder_limestone;
	public static Item powder_cement;
	public static Item squirrel;
	public static Item cooked_squirrel;

	public static Item potassium_iodide_powder;

	public static Item potassium_powder;

	public static Item niter;
	public static Item ammonium_chloride;
	public static Item sodium_nitrate;
	public static Item ingot_copper;
	public static Item ingot_red_copper;
	public static Item ingot_tungsten;
	public static Item ingot_aluminium;
	public static Item ingot_tin;
	public static Item fluorite;
	public static Item ingot_beryllium;
	public static Item ingot_schraranium;
	public static Item ingot_schrabidium;
	public static Item ingot_schrabidate;
	public static Item ingot_plutonium_fuel;
	public static Item ingot_neptunium_fuel;
	public static Item ingot_uranium_fuel;
	public static Item ingot_mox_fuel;
	public static Item ingot_americium_fuel;
	public static Item ingot_schrabidium_fuel;
	public static Item ingot_thorium_fuel;
	public static Item nugget_protactinium;
	public static Item nugget_uranium_fuel;
	public static Item nugget_thorium_fuel;
	public static Item nugget_plutonium_fuel;
	public static Item nugget_neptunium_fuel;
	public static Item nugget_mox_fuel;
	public static Item nugget_americium_fuel;
	public static Item nugget_schrabidium_fuel;
	public static Item ingot_advanced_alloy;
	public static Item ingot_tcalloy;
	public static Item ingot_cdalloy;
	public static Item ingot_bismuth_bronze;
	public static Item ingot_arsenic_bronze;
	public static Item ingot_bscco;
	public static Item lithium;
	public static Item ingot_zirconium;
	public static Item ingot_hes;
	public static Item ingot_les;
	public static Item nugget_hes;
	public static Item nugget_les;
	public static Item ingot_magnetized_tungsten;
	public static Item ingot_combine_steel;
	public static Item ingot_solinium;
	public static Item nugget_solinium;
	public static Item ingot_phosphorus;
	public static Item ingot_semtex;
	public static Item ingot_c4;
	public static Item ingot_boron;
	public static Item ingot_graphite;
	public static Item ingot_firebrick;
	public static Item ingot_smore;

	public static Item ingot_gh336;
	public static Item nugget_gh336;

	public static Item ingot_cn989;

	public static Item ingot_australium;
	public static Item ingot_weidanium;
	public static Item ingot_reiium;
	public static Item ingot_unobtainium;
	public static Item ingot_daffergon;
	public static Item ingot_verticium;
	public static Item nugget_australium;
	public static Item nugget_australium_lesser;
	public static Item nugget_australium_greater;
	public static Item nugget_weidanium;
	public static Item nugget_reiium;
	public static Item nugget_unobtainium;
	public static Item nugget_daffergon;
	public static Item nugget_verticium;

	public static Item ingot_desh;
	public static Item nugget_desh;
	public static Item ingot_dineutronium;
	public static Item nugget_dineutronium;
	public static Item powder_dineutronium;
	public static Item ingot_tetraneutronium;
	public static Item nugget_tetraneutronium;
	public static Item powder_tetraneutronium;
	public static Item ingot_starmetal;
	public static Item ingot_saturnite;
	public static Item plate_saturnite;
	public static Item ingot_ferrouranium;
	public static Item ingot_electronium;
	public static Item ingot_gwenium;
	public static Item nugget_zirconium;
	public static Item nugget_nickel;
	public static Item nugget_zinc;
	public static Item nugget_gallium;
	public static Item nugget_platnium;
	public static Item nugget_gaas;
	public static Item nugget_mercury;
	public static Item ingot_mercury; //It's to prevent any ambiguity, as it was treated as a full ingot in the past anyway
	public static Item bottle_mercury;
	public static Item nugget_ruthenium;

	public static Item ingot_tt;
	public static Item ingot_ttas;

	public static Item ore_byproduct; //byproduct of variable purity and quantity, can be treated as a nugget, might require shredding or acidizing, depends on the type

	public static Item ore_bedrock;
	public static Item ore_centrifuged;
	public static Item ore_cleaned;
	public static Item ore_separated;
	public static Item ore_purified;
	public static Item ore_nitrated;
	public static Item ore_nitrocrystalline;
	public static Item ore_deepcleaned;
	public static Item ore_seared;
	//public static Item ore_radcleaned;
	public static Item ore_enriched; //final stage
	public static Item bedrock_ore_base;
	public static Item bedrock_ore;
	public static Item bedrock_ore_fragment;

	public static Item crystal_mineral;
	public static Item crystal_cleaned;
	public static Item mineral_dust;
	public static Item mineral_fragment;

	public static Item billet_uranium;
	public static Item billet_u233;
	public static Item billet_u235;
	public static Item billet_u238;
	public static Item billet_th232;
	public static Item billet_plutonium;
	public static Item billet_pu238;
	public static Item billet_tm170;
	public static Item billet_pm;
	public static Item billet_pu239;
	public static Item billet_pu240;
	public static Item billet_pu241;
	public static Item billet_pu_mix;
	public static Item billet_am241;
	public static Item billet_am242;
	public static Item billet_bk247;
	public static Item billet_cm242;
	public static Item billet_cm243;
	public static Item billet_cm244;
	public static Item billet_cm245;
	public static Item billet_cm246;
	public static Item billet_cm247;
	public static Item billet_cm248;
	public static Item billet_cn989;
	public static Item billet_cm_fuel;
	public static Item billet_cf252;
    public static Item billet_cf251;
	public static Item ingot_cf252;
	public static Item ingot_cf247;
	public static Item ingot_cm248;
	public static Item billet_cm_mix;
	public static Item billet_es253;
	public static Item billet_am_mix;
	public static Item billet_neptunium;
	public static Item billet_polonium;
	public static Item billet_technetium;
	public static Item billet_cobalt;
	public static Item billet_co60;
	public static Item billet_sr90;
	public static Item billet_au198;
	public static Item billet_pb209;
	public static Item billet_ra226;
	public static Item billet_actinium;
	public static Item billet_schrabidium;
	public static Item billet_solinium;
	public static Item billet_gh336;
	public static Item billet_australium;
	public static Item billet_australium_lesser;
	public static Item billet_australium_greater;
	public static Item billet_uranium_fuel;
	public static Item billet_thorium_fuel;
	public static Item billet_plutonium_fuel;
	public static Item billet_neptunium_fuel;
	public static Item billet_mox_fuel;
	public static Item billet_americium_fuel;
	public static Item billet_les;
	public static Item billet_schrabidium_fuel;
	public static Item billet_hes;
	public static Item billet_po210be;
	public static Item billet_ra226be;
	public static Item billet_pu238be;
	public static Item billet_yharonite;
	public static Item billet_balefire_gold;
	public static Item billet_flashlead;
	public static Item billet_zfb_bismuth;
	public static Item billet_zfb_pu241;
	public static Item billet_zfb_am_mix;
	public static Item billet_beryllium;
	public static Item billet_bismuth;
	public static Item billet_zirconium;
	public static Item billet_gaas;
	public static Item billet_nuclear_waste;
	public static Item billet_red_copper;

	public static Item nugget_th232;
	public static Item nugget_uranium;
	public static Item nugget_promethium;
	public static Item nugget_u233;
	public static Item nugget_u235;
	public static Item nugget_u238;
	public static Item nugget_plutonium;
	public static Item nugget_pu238;
	public static Item nugget_tm170;
	public static Item nugget_pu239;
	public static Item nugget_pu240;
	public static Item nugget_pu241;
	public static Item nugget_pu_mix;
	public static Item nugget_am241;
	public static Item nugget_am242;
	public static Item nugget_bk247;
	public static Item nugget_cm242;
	public static Item nugget_cm243;
	public static Item nugget_cm244;
	public static Item nugget_cm245;
	public static Item nugget_cm246;
	public static Item nugget_cm247;
	public static Item nugget_cm248;
	public static Item nugget_cn989;
	public static Item nugget_cm_fuel;
    public static Item nugget_cf251;
	public static Item nugget_cf252;
	public static Item nugget_cf249;
	public static Item nugget_cf248;
	public static Item nugget_cf250;
	public static Item nugget_cm_mix;
	public static Item nugget_es253;
	public static Item nugget_am_mix;
	public static Item nugget_neptunium;
	public static Item nugget_polonium;
	public static Item nugget_technetium;
	public static Item nugget_cobalt;
	public static Item nugget_co60;
	public static Item nugget_sr90;
	public static Item nugget_au198;
	public static Item nugget_pb209;
	public static Item nugget_ra226;
	public static Item nugget_actinium;
	public static Item plate_titanium;
	public static Item plate_nickel;
	public static Item plate_aluminium;
	public static Item neutron_reflector;
	public static Item ingot_steel;
	public static Item ingot_chromium;
	public static Item ingot_vanadium;

	public static Item ingot_stainless;
	public static Item plate_stainless;
	public static Item plate_steel;
	public static Item plate_iron;
	public static Item powder_cn989;
	public static Item plate_cn989;
	public static Item ingot_lead;
	public static Item ingot_silver;
	public static Item nugget_lead;
	public static Item ingot_bismuth;
	public static Item nugget_bismuth;
	public static Item nugget_silver;

	public static Item ingot_arsenic;
	public static Item nugget_arsenic;
	public static Item nugget_antimony;
	public static Item ingot_tantalium;
	public static Item nugget_tantalium;
	public static Item ingot_silicon;
	public static Item billet_silicon;
	public static Item nugget_silicon;
	public static Item calcined_dolomite;
	public static Item ingot_niobium;
	public static Item nugget_niobium;
	public static Item nugget_vanadium;
	public static Item ingot_osmiridium;
	public static Item nugget_osmiridium;
	public static Item ingot_hafnium;
	public static Item nugget_hafnium;
	public static Item plate_lead;
	public static Item plate_dura_steel;
	public static Item nugget_schrabidium;
	public static Item plate_schrabidium;
	public static Item plate_copper;
	public static Item nugget_beryllium;
	public static Item plate_gold;
	public static Item hazmat_cloth;
	public static Item hazmat_cloth_red;
	public static Item hazmat_cloth_grey;
	public static Item asbestos_cloth;
	public static Item rag;
	public static Item rag_damp;
	public static Item rag_piss;
	public static Item rag_blood;
	public static Item filter_coal;
	public static Item plate_advanced_alloy;
	public static Item plate_combine_steel;
	public static Item plate_mixed;
	public static Item plate_paa;
	public static Item pipes_steel;
	public static Item drill_titanium;
	public static Item plate_dalekanium;
	public static Item plate_euphemium;
	public static Item bolt;
	public static Item bolt_spike;
	public static Item plate_polymer;
	public static Item plate_kevlar;
	public static Item plate_dineutronium;
	public static Item plate_desh;
	public static Item plate_bismuth;
	public static Item photo_panel;
	public static Item sat_base;
	public static Item thruster_nuclear;
	public static Item safety_fuse;
	public static Item part_generic;
	public static Item item_secret;
	public static Item chemical_dye;
	public static Item crayon;

	public static Item scuttertail;
	public static Item saltleaf;

	public static Item undefined;

	public static Item ball_resin;
	public static Item ingot_dura_steel;
	public static Item ingot_polymer;
	public static Item ingot_bakelite;
	public static Item ingot_biorubber;
	public static Item ingot_rubber;
	public static Item ingot_pet;
	public static Item ingot_pc;
	public static Item ingot_pvc;

	public static Item ingot_fiberglass;
	public static Item ingot_asbestos;
	public static Item powder_asbestos;
	public static Item gem_ruby;
	public static Item gem_sapphire;
	public static Item ingot_calcium;
	public static Item powder_calcium;
	public static Item ingot_cadmium;
	public static Item powder_cadmium;
	public static Item powder_bismuth;
	public static Item powder_potash;
	public static Item powder_pollucite;
	public static Item powder_osmium;
	//public static Item ingot_mud;
	public static Item ingot_cft;

	public static Item ingot_lanthanium;
	public static Item nugget_lanthanium;
	public static Item ingot_actinium;
	public static Item francium_ingot;

	//public static Item ingot_meteorite;
	//public static Item ingot_meteorite_forged;
	//public static Item blade_meteorite;
	public static Item ingot_steel_dusted;
	public static Item ingot_chainsteel;

	public static Item plate_armor_titanium;
	public static Item plate_armor_ajr;
	public static Item plate_armor_hev;
	public static Item plate_armor_lunar;
	public static Item plate_armor_fau;
	public static Item plate_armor_dnt;

	public static Item oil_tar;
	public static Item solid_fuel;
	public static Item solid_fuel_presto;
	public static Item solid_fuel_presto_triplet;
	public static Item solid_fuel_bf;
	public static Item solid_fuel_presto_bf;
	public static Item solid_fuel_presto_triplet_bf;
	public static Item rocket_fuel;

	public static Item crystal_coal;
	public static Item crystal_iron;
	public static Item crystal_gold;
	public static Item crystal_redstone;
	public static Item crystal_lapis;
	public static Item crystal_diamond;
	public static Item crystal_uranium;
	public static Item crystal_thorium;
	public static Item crystal_plutonium;
	public static Item crystal_titanium;
	public static Item crystal_sulfur;
	public static Item crystal_niter;
	public static Item crystal_copper;
	public static Item crystal_tungsten;
	public static Item crystal_aluminium;
	public static Item crystal_fluorite;
	public static Item crystal_beryllium;
	public static Item crystal_lead;
	public static Item crystal_schraranium;
	public static Item crystal_schrabidium;
	public static Item crystal_rare;
	public static Item crystal_phosphorus;
	public static Item crystal_lithium;
	public static Item crystal_cobalt;
	public static Item crystal_starmetal;
	public static Item crystal_cinnebar;
	public static Item crystal_trixite;
	public static Item crystal_osmiridium;
	public static Item crystal_nickel;
	public static Item crystal_niobium;

	public static Item crystal_basaltic;

	public static Item crystal_meth;


	public static Item itemsalt;
	public static Item magnesium_chloride;
	public static Item rubidiumsalt;

	public static Item quicklime;

	public static Item sodium_sulfate;
	public static Item sodium_sulfide;
	public static Item potassium_sulfate;

	public static Item gem_sodalite;
	public static Item gem_tantalium;
	public static Item gem_volcanic;
	public static Item gem_rad;
	public static Item gem_alexandrite;
	public static Item titanium_sapphire_crystal;
	public static Item cerium_crystal;
	public static Item calcium_fluoride_crystal;
	public static Item KTP_crystal;
	public static Item lithium_niobate_crystal;
	public static Item visible_optics_array;
	public static Item visible_dopant_crystal;
	public static Item BBO_crystal;
	public static Item KDP_crystal;
	public static Item CLBO_crystal;
	public static Item LBO_crystal;

	public static Item powder_lead;
	public static Item powder_silver;
	public static Item powder_tantalium;
	public static Item powder_neptunium;
	public static Item powder_polonium;
	public static Item powder_co60;
	public static Item powder_sr90;
	public static Item powder_sr90_tiny;
	public static Item powder_au198;
	public static Item powder_ra226;
	public static Item powder_i131;
	public static Item powder_i131_tiny;
	public static Item powder_xe135;
	public static Item powder_xe135_tiny;
	public static Item powder_cs137;
	public static Item powder_cs137_tiny;
	public static Item powder_at209;
	public static Item powder_schrabidium;
	public static Item powder_schrabidate;

	public static Item powder_aluminium;
	public static Item powder_beryllium;
	public static Item powder_copper;
	public static Item powder_gold;
	public static Item powder_iron;
	public static Item powder_chromium;
	public static Item powder_titanium;
	public static Item powder_zircon;
	public static Item powder_nickel;
	public static Item powder_gallium_tiny;
	public static Item powder_gallium;
	public static Item powder_germanium;
	public static Item powder_zinc;
	public static Item powder_tungsten;
	public static Item powder_platnium;
	public static Item ingot_germanium;
	public static Item nugget_germanium;

	//PGM
	public static Item powder_rhodium_solution;
	public static Item powder_rhodium;
	public static Item ingot_rhodium;

	public static Item powder_palladium;
	public static Item powder_samarium;

	public static Item powder_uranium;
	public static Item powder_plutonium;
	public static Item powder_columbite;
	public static Item dust;
	public static Item dust_tiny;
	public static Item fallout;
	public static Item powder_power;
	public static Item manganese_powder;

	public static Item powder_thorium;
	public static Item powder_iodine;
	public static Item powder_neodymium;
	public static Item powder_astatine;
	public static Item powder_caesium;
	public static Item ingot_cesium;

	public static Item cesium_salt;
	public static Item arsenic_trioxide;
	public static Item powder_selenium;
	public static Item powder_tellurium_dioxide;
	public static Item powder_tellurium;
	public static Item nugget_tellurium;

	public static Item powder_strontium;
	public static Item powder_cobalt;
	public static Item powder_bromine;
	public static Item powder_niobium;
	public static Item powder_tennessine;
	public static Item powder_cerium;

	public static Item powder_advanced_alloy;
	public static Item powder_tcalloy;
	public static Item powder_coal;
	public static Item powder_coal_tiny;
	public static Item powder_combine_steel;
	public static Item powder_diamond;
	public static Item powder_emerald;
	public static Item powder_lapis;
	public static Item powder_quartz;
	public static Item powder_magnetized_tungsten;
	public static Item powder_chlorophyte;
	public static Item powder_red_copper;
	public static Item powder_steel;
	public static Item powder_lithium;
	public static Item powder_zirconium;
	public static Item powder_sodium;
	public static Item powder_sodium_silicate;
	public static Item redstone_depleted;

	public static Item powder_australium;
	public static Item powder_weidanium;
	public static Item powder_reiium;
	public static Item powder_unobtainium;
	public static Item powder_daffergon;
	public static Item powder_verticium;

	public static Item powder_dura_steel;
	public static Item powder_polymer;
	public static Item powder_bakelite;
	public static Item powder_euphemium;
	//public static Item powder_meteorite;

	//public static Item powder_wd2004_tiny;
	//public static Item powder_wd2004;

	public static Item powder_steel_tiny;
	public static Item powder_lithium_tiny;
	public static Item titanium_trace_metals_slurry;
	public static Item powder_neodymium_tiny;
	public static Item powder_cobalt_tiny;
	public static Item powder_alien_tiny;
	public static Item powder_niobium_tiny;
	public static Item powder_cerium_tiny;
	public static Item powder_lanthanium_tiny;
	public static Item powder_yttrium_tiny;
	public static Item powder_actinium_tiny;
	public static Item powder_praseodymium_tiny;
	public static Item powder_boron_tiny;
	public static Item powder_meteorite_tiny;
	public static Item powder_vanadium_tiny;

	public static Item powder_coltan_ore;
	public static Item powder_coltan;
	public static Item powder_tektite;
	public static Item powder_paleogenite;
	public static Item powder_paleogenite_tiny;
	public static Item powder_osmium_tiny;
	public static Item powder_impure_osmiridium;
	public static Item iridium_rich_residue;
	public static Item iridium_oxide;
	public static Item powder_borax;
	public static Item powder_chlorocalcite;
	public static Item powder_molysite;
	public static Item powder_antimony_trioxide;

	public static Item powder_lanthanium;
	public static Item powder_yttrium;
	public static Item powder_actinium;
	public static Item powder_boron;
	public static Item powder_desh;
	public static Item powder_semtex_mix;
	public static Item powder_desh_mix;
	public static Item powder_desh_ready;
	public static Item powder_nitan_mix;
	public static Item powder_spark_mix;
	public static Item powder_yellowcake;
	public static Item powder_magic;
	public static Item powder_balefire;
	public static Item powder_sawdust;
	public static Item powder_flux;
	public static Item powder_fertilizer;

	public static Item fragment_neodymium;
	public static Item ingot_neodymium;
	public static Item fragment_cobalt;
	public static Item fragment_alien;
	public static Item fragment_niobium;
	public static Item fragment_cerium;
	public static Item fragment_lanthanium;
	public static Item fragment_actinium;
	public static Item fragment_yttrium;
	public static Item fragment_boron;
	public static Item fragment_meteorite;
	public static Item fragment_coltan;
	public static Item fragment_praseodymium;
	public static Item fragment_samarium;
	public static Item fragment_europium;
	public static Item fragment_gadolinium;
	public static Item fragment_terbium;
	public static Item fragment_dysprosium;
	public static Item fragment_holmium;
	public static Item fragment_erbium;
	public static Item fragment_ytterbium;
	public static Item fragment_lutetium;
	public static Item fragment_thulium;
	public static Item powder_terbium_tiny;
	public static Item powder_dysprosium_tiny;
	public static Item powder_holmium_tiny;
	public static Item powder_erbium_tiny;
	public static Item powder_thulium2;
	public static Item powder_thulium;
	public static Item powder_ytterbium_tiny;
	public static Item powder_lutetium_tiny;
	public static Item powder_thulium_tiny;
	public static Item powder_terbium;
	public static Item powder_dysprosium_concentrate;
	public static Item powder_dysprosium;
	public static Item europium_dust_tiny;
	public static Item gadolinium_dust_tiny;
	public static Item gadoliniumsol;
	public static Item terbiumsol;
	public static Item dysprosiumsol;
	public static Item lutetiumsol;
	public static Item thuliumsol;
	public static Item powder_lutetium2;
	public static Item powder_dysprosium2;
	public static Item powder_holmium_oxide;
	public static Item powder_ytterbium_oxide;
	public static Item powder_ytterbium_fluoride;
	public static Item powder_ytterbium;
	public static Item powder_holmium_concentrate;
	public static Item powder_erbium_concentrate;
	public static Item powder_ytterbium_concentrate;
	public static Item powder_lutetium_concentrate;
	public static Item powder_thulium_concentrate;
	public static Item erbium_powder;
	public static Item er_yag_feedstock;
	public static Item yby_feedstock;
	public static Item znse_feedstock;
	public static Item er_yag_sintered;
	public static Item yby_sintered;
	public static Item yttrium_aluminum_erbium_crystal;
	//public static Item ruby_crystal;
	public static Item YbY_crystal;
	public static Item ZnSe_crystal;
	public static Item germanium_crystal;
	public static Item ingot_erbium;
	public static Item ingot_ytterbium;
	public static Item ingot_lutetium;
	public static Item ingot_dysprosium;
	public static Item ingot_holmium;
	public static Item powder_terbium2;
	public static Item powder_terbium_oxide;
	public static Item powder_terbium_fluoride;
	public static Item ingot_terbium_impure;
	public static Item gadolinium_dust_step;
	public static Item powder_gadolinium;
	public static Item powder_gadolinium2;
	public static Item ingot_gadolinium;
	public static Item europiumsol;
	public static Item ingot_praseodymium;
	public static Item nugget_praseodymium;
	public static Item powder_praseodymium;
	public static Item chunk_ore;
	public static Item powder_steel_dusted;


	public static Item chunk_ironoxide;
	public static Item chunk_coppersulfide;
	public static Item powder_vanadium;

	public static Item chunk_leadzincsulfide;
	public static Item chunk_nickelsulfide;
	public static Item chunk_tintungsten;
	public static Item chunk_lithiumpegmatite;
	public static Item chunk_heavymineralsand;
	public static Item chunk_evaporite;
	public static Item chunk_carbon;
	public static Item chunk_chromite;


	//actual ore groups that bob hates:
	//iron
	public static Item magnetite;
	public static Item goethite;
	public static Item fragment_vanadium;

	//copper
	public static Item chalcopyrite;
	public static Item bornite;
	public static Item chalcocite;
	public static Item covellite;
	public static Item molybdenite;

	//the silly platinum cancer
	public static Item osmiridium;
	public static Item pgm_residue;

	//lead/zinc
	public static Item galena;
	public static Item sphalerite;
	public static Item stibnite;

	//nickel
	public static Item pentlandite;
	public static Item pyrrhotite;

	//tin-tungsten
	public static Item cassiterite;
	public static Item wolframite;
	public static Item columbite;

	//lithium
	public static Item spodumene;
	public static Item lepidolite;
	public static Item petalite;

	//heavy mineral sand
	public static Item ilmenite;
	public static Item rutile;
	public static Item zircon;

	//chromite
	public static Item chromite;

	//evaporite
	public static Item carnallite;

	public static Item crushed_spodumene;
	public static Item crushed_petalite;
	//public static Item powder_ilmenite;
	//titanium crystal
	//public static Item roasted_spodumene;
	//basically a lithium crystal, just rename it so the processing makes sense.




	public static Item biomass;
	public static Item flesh;
	public static Item flesh_wafer;
	public static Item grilled_flesh;
	public static Item flesh_burger;
	public static Item biomass_compressed;
	public static Item bio_wafer;
	public static Item plant_item;

	public static Item coil_copper;
	public static Item coil_copper_torus;
	public static Item coil_tungsten;
	public static Item tank_steel;
	public static Item motor;
	public static Item motor_desh;
	public static Item motor_bismuth;
	public static Item centrifuge_element;
	public static Item reactor_core;
	public static Item rtg_unit;
	public static Item levitation_unit;

	public static Item wire_aluminium;
	public static Item wire_copper;
	public static Item wire_zinc;
	public static Item wire_red_copper;
	public static Item wire_tungsten;
	public static Item wire_gold;
	public static Item wire_schrabidium;
	public static Item wire_advanced_alloy;
	public static Item wire_magnetized_tungsten;

	public static Item coil_advanced_alloy;
	public static Item coil_advanced_torus;
	public static Item coil_magnetized_tungsten;
	public static Item coil_gold;
	public static Item coil_gold_torus;
	public static Item magnet_circular;
	public static Item component_limiter;
	public static Item component_emitter;
	public static Item chlorine_pinwheel;
	public static Item oxy_pinwheel;
	public static Item deuterium_filter;

	public static Item parts_legendary;

	public static Item circuit;

	public static Item circuit_raw;
	public static Item circuit_aluminium;
	public static Item circuit_copper;
	public static Item circuit_red_copper;
	public static Item circuit_gold;
	public static Item circuit_schrabidium;
	public static Item circuit_bismuth_raw;
	public static Item circuit_bismuth;
	public static Item circuit_arsenic_raw;
	public static Item circuit_arsenic;
	public static Item circuit_tantalium_raw;
	public static Item circuit_tantalium;

	public static Item circuit_targeting_tier1;
	public static Item circuit_targeting_tier2;
	public static Item circuit_targeting_tier3;
	public static Item circuit_targeting_tier4;
	public static Item circuit_targeting_tier5;
	public static Item circuit_targeting_tier6;

	public static Item crt_display;
	public static ItemEnumMulti circuit_star_piece;
	public static ItemEnumMulti circuit_star_component;
	public static Item circuit_star;

	public static Item mechanism_revolver_1;
	public static Item mechanism_revolver_2;
	public static Item mechanism_rifle_1;
	public static Item mechanism_rifle_2;
	public static Item mechanism_launcher_1;
	public static Item mechanism_launcher_2;
	public static Item mechanism_special;

	public static Item casing_357;
	public static Item casing_44;
	public static Item casing_9;
	public static Item casing_50;
	public static Item casing_buckshot;
	public static Item assembly_iron;
	public static Item assembly_steel;
	public static Item assembly_lead;
	public static Item assembly_gold;
	public static Item assembly_schrabidium;
	public static Item assembly_nightmare;
	public static Item assembly_desh;
	//public static Item assembly_pip;
	public static Item assembly_nopip;
	public static Item assembly_smg;
	public static Item assembly_556;
	public static Item assembly_762;
	public static Item assembly_45;
	public static Item assembly_uzi;
	public static Item assembly_actionexpress;
	public static Item assembly_calamity;
	public static Item assembly_lacunae;
	public static Item assembly_nuke;
	public static Item assembly_luna;

	public static Item folly_shell;
	public static Item folly_bullet;
	public static Item folly_bullet_nuclear;
	public static Item folly_bullet_du;

	public static Item wiring_red_copper;
	public static Item shell;
	public static Item pipe;
	public static Item fins_flat;
	public static Item fins_small_steel;
	public static Item fins_big_steel;
	public static Item fins_tri_steel;
	public static Item fins_quad_titanium;
	public static Item sphere_steel;
	public static Item pedestal_steel;
	public static Item dysfunctional_reactor;
	public static Item blade_titanium;
	public static Item blade_syngas;
	public static Item turbine_titanium;
	public static Item blade_tungsten;
	public static Item turbine_tungsten;
	public static Item turbine_syngas;
	public static Item pellet_coal;
	public static Item ring_starmetal;
	public static Item flywheel_beryllium;

	public static Item gear_large;
	public static Item sawblade;

	public static Item toothpicks;
	public static Item ducttape;
	public static Item catalyst_clay;

	public static Item warhead_generic_small;
	public static Item warhead_generic_medium;
	public static Item warhead_generic_large;
	public static Item warhead_incendiary_small;
	public static Item warhead_incendiary_medium;
	public static Item warhead_incendiary_large;
	public static Item warhead_cluster_small;
	public static Item warhead_cluster_medium;
	public static Item warhead_cluster_large;
	public static Item warhead_buster_small;
	public static Item warhead_buster_medium;
	public static Item warhead_buster_large;
	public static Item warhead_nuclear;
	public static Item warhead_mirv;
	public static Item warhead_volcano;

	public static Item fuel_tank_small;
	public static Item fuel_tank_medium;
	public static Item fuel_tank_large;

	public static Item thruster_small;
	public static Item thruster_medium;
	public static Item thruster_large;

	public static Item sat_head_mapper;
	public static Item sat_head_scanner;
	public static Item sat_head_radar;
	public static Item sat_head_laser;
	public static Item sat_head_resonator;

	public static Item seg_10;
	public static Item seg_15;
	public static Item seg_20;

	public static Item chopper_head;
	public static Item chopper_gun;
	public static Item chopper_torso;
	public static Item chopper_tail;
	public static Item chopper_wing;
	public static Item chopper_blades;
	public static Item combine_scrap;

	public static Item shimmer_head;
	public static Item shimmer_axe_head;
	public static Item shimmer_handle;

	//public static Item telepad;
	public static Item entanglement_kit;

	public static Item stamp_stone_flat;
	public static Item stamp_stone_plate;
	public static Item stamp_stone_wire;
	public static Item stamp_stone_circuit;
	public static Item stamp_iron_flat;
	public static Item stamp_iron_plate;
	public static Item stamp_iron_wire;
	public static Item stamp_iron_circuit;
	public static Item stamp_steel_flat;
	public static Item stamp_steel_plate;
	public static Item stamp_steel_wire;
	public static Item stamp_steel_circuit;
	public static Item stamp_titanium_flat;
	public static Item stamp_titanium_plate;
	public static Item stamp_titanium_wire;
	public static Item stamp_titanium_circuit;
	public static Item stamp_obsidian_flat;
	public static Item stamp_obsidian_plate;
	public static Item stamp_obsidian_wire;
	public static Item stamp_obsidian_circuit;
	public static Item stamp_desh_flat;
	public static Item stamp_desh_plate;
	public static Item stamp_desh_wire;
	public static Item stamp_desh_circuit;
	public static Item stamp_book;

	public static Item stamp_357;
	public static Item stamp_44;
	public static Item stamp_9;
	public static Item stamp_50;

	public static Item stamp_desh_357;
	public static Item stamp_desh_44;
	public static Item stamp_desh_9;
	public static Item stamp_desh_50;

	public static Item blades_steel;
	public static Item blades_titanium;
	public static Item blades_advanced_alloy;
	public static Item blades_desh;

	public static Item mold_base;
	public static Item mold;
	public static Item scraps;
	public static Item plate_cast;
	public static Item plate_welded;
	public static Item heavy_component;
	public static Item wire_fine;
	public static Item wire_dense;

	public static Item part_lithium;
	public static Item part_beryllium;
	public static Item part_carbon;
	public static Item part_copper;
	public static Item part_plutonium;

	public static Item laser_crystal_co2;
	public static Item laser_crystal_bismuth;
	public static Item laser_crystal_cmb;
	public static Item laser_crystal_iron;
	public static Item laser_crystal_dnt;
	//public static Item laser_crystal_digamma;

	public static Item thermo_element;

	public static Item catalytic_converter;
	public static Item crackpipe;

	public static Item pellet_rtg_depleted;

	public static Item pellet_rtg_radium;
	public static Item pellet_rtg_weak;
	public static Item pellet_rtg;
	public static Item pellet_rtg_promethium;
	public static Item pellet_rtg_strontium;
	public static Item pellet_rtg_cobalt;
	public static Item pellet_rtg_actinium;
	public static Item pellet_rtg_polonium;
	public static Item pellet_rtg_americium;
	public static Item pellet_rtg_berkelium;
	public static Item pellet_rtg_tm170;
	public static Item pellet_rtg_gold;
	public static Item pellet_rtg_lead;
	public static Item pellet_rtg_cf251;
	public static Item pellet_rtg_cf252;

	public static Item tritium_deuterium_cake;

	public static Item piston_selenium;
	public static Item piston_set;
	public static Item drillbit;

	//public static Item crystal_energy;
	//public static Item pellet_coolant;

	public static Item rune_blank;
	public static Item rune_isa;
	public static Item rune_dagaz;
	public static Item rune_hagalaz;
	public static Item rune_jera;
	public static Item rune_thurisaz;

	public static Item ams_catalyst_blank;
	public static Item ams_catalyst_aluminium;
	public static Item ams_catalyst_beryllium;
	public static Item ams_catalyst_caesium;
	public static Item ams_catalyst_cerium;
	public static Item ams_catalyst_cobalt;
	public static Item ams_catalyst_copper;
	public static Item ams_catalyst_dineutronium;
	public static Item ams_catalyst_euphemium;
	public static Item ams_catalyst_iron;
	public static Item ams_catalyst_lithium;
	public static Item ams_catalyst_niobium;
	public static Item ams_catalyst_schrabidium;
	public static Item ams_catalyst_strontium;
	public static Item ams_catalyst_thorium;
	public static Item ams_catalyst_tungsten;

	public static Item ams_focus_blank;
	public static Item ams_focus_limiter;
	public static Item ams_focus_booster;

	public static Item ams_muzzle;

	public static Item ams_lens;

	public static Item ams_core_sing;
	public static Item ams_core_wormhole;
	public static Item ams_core_eyeofharmony;
	public static Item ams_core_thingy;

	public static Item fusion_shield_tungsten;
	public static Item fusion_shield_beryllium;
	public static Item fusion_shield_desh;
	public static Item fusion_shield_chlorophyte;
	public static Item fusion_shield_vaporwave;

	public static Item cell_empty;
	public static Item cell_uf6;
	public static Item cell_puf6;
	public static Item cell_deuterium;
	public static Item cell_tritium;
	public static Item cell_sas3;
	public static Item cell_antimatter;
	public static Item cell_anti_schrabidium;
	public static Item cell_balefire;

	public static Item demon_core_open;
	public static Item demon_core_closed;

	public static Item particle_empty;
	public static Item particle_hydrogen;
	public static Item particle_copper;
	public static Item particle_lead;
	public static Item particle_aproton;
	public static Item particle_aelectron;
	public static Item particle_amat;
	public static Item particle_aschrab;
	public static Item particle_higgs;
	public static Item particle_muon;
	public static Item particle_tachyon;
	public static Item particle_strange;
	public static Item particle_dark;
	public static Item particle_sparkticle;
	public static Item particle_helium;
	public static Item particle_astatine;
	public static Item particle_neutron;
	public static Item particle_beta;
	//public static Item particle_digamma;
	public static Item particle_lutece;

	//public static Item pellet_antimatter;
	//public static Item singularity_micro;
	//public static Item singularity;
	//public static Item singularity_counter_resonant;
	//public static Item singularity_super_heated;
	//public static Item black_hole;
	//public static Item singularity_spark;
	//public static Item crystal_xen;
	public static Item inf_water;
	public static Item inf_water_mk2;

	public static Item fuel_additive;

	public static Item canister_empty;
	public static Item canister_full;
	public static Item canister_napalm;

	public static Item gas_empty;
	public static Item gas_full;

	public static Item fluid_tank_full;
	public static Item fluid_tank_empty;
	public static Item fluid_tank_lead_full;
	public static Item fluid_tank_lead_empty;
	public static Item fluid_barrel_full;
	public static Item fluid_barrel_empty;
	public static Item fluid_barrel_infinite;
	public static Item pipette;
	public static Item pipette_boron;
	public static Item pipette_laboratory;
	public static Item siphon;

	public static Item disperser_canister_empty;
	public static Item disperser_canister;
	//public static Item glyphid_gland;
	//public static Item glyphid_gland_empty;

	public static Item syringe_empty;
	public static Item syringe_antidote;
	public static Item syringe_poison;
	public static Item syringe_awesome;
	public static Item syringe_metal_empty;
	public static Item syringe_metal_stimpak;
	public static Item syringe_metal_medx;
	public static Item syringe_metal_psycho;
	public static Item syringe_metal_super;

	public static Item tourniquet;

	//public static Item syringe_taint;
	public static Item syringe_mkunicorn;
	public static Item iv_empty;
	public static Item iv_blood;
	//public static Item iv_xp_empty;
	//public static Item iv_xp;
	public static Item radaway;
	public static Item prussian_blue_powder;
	public static Item prussian_blue_pill;
	public static Item radaway_strong;
	public static Item radaway_flush;
	public static Item radx;
	public static Item siox;
	public static Item pill_herbal;
	public static Item xanax;
	public static Item fmn;
	public static Item five_htp;
	public static Item med_bag;
	public static Item pill_iodine;
	public static Item plan_c;
	//public static Item pill_red;
	//public static Item stealth_boy;
	public static Item gas_mask_filter;
	public static Item gas_mask_filter_mono;
	public static Item gas_mask_filter_combo;
	public static Item gas_mask_filter_rag;
	public static Item gas_mask_filter_piss;
	//public static Item jetpack_tank;
	public static Item lox_tank;
	public static Item gun_kit_1;
	public static Item gun_kit_2;
	//public static Item cbt_device;
	public static Item cigarette;

	//public static Item animan;

	public static Item can_empty;
	public static Item can_smart;
	public static Item can_creature;
	public static Item can_redbomb;
	public static Item can_mrsugar;
	public static Item can_overcharge;
	public static Item can_luna;
	public static Item can_bepis;
	public static Item can_breen;
	public static Item can_mug;
	public static Item mucho_mango;
	public static Item bottle_empty;
	//public static Item bottle_nuka;
	//public static Item bottle_cherry;
	//public static Item bottle_quantum;
	//public static Item bottle_sparkle;
	//public static Item bottle_rad;
	//public static Item bottle2_empty;
	//public static Item bottle2_korl;
	//public static Item bottle2_fritz;
	//public static Item bottle2_korl_special;
	//public static Item bottle2_fritz_special;
	public static Item flask_empty;
	//public static Item flask_infusion;
	public static Item chocolate_milk;
	public static Item coffee;
	public static Item coffee_radium;
	public static Item chocolate;
	public static Item cap_nuka;
	public static Item cap_quantum;
	public static Item cap_sparkle;
	public static Item cap_rad;
	public static Item cap_korl;
	public static Item cap_fritz;
	public static Item ring_pull;
	public static Item bdcl;

	public static Item galaxygas;

	//public static Item canned_beef;
	//public static Item canned_tuna;
	//public static Item canned_mystery;
	//public static Item canned_pashtet;
	//public static Item canned_cheese;
	//public static Item canned_jizz;
	//public static Item canned_milk;
	//public static Item canned_ass;
	//public static Item canned_pizza;
	//public static Item canned_tube;
	//public static Item canned_tomato;
	//public static Item canned_asbestos;
	//public static Item canned_bhole;
	//public static Item canned_hotdogs;
	//public static Item canned_leftovers;
	//public static Item canned_yogurt;
	//public static Item canned_stew;
	//public static Item canned_chinese;
	//public static Item canned_oil;
	//public static Item canned_fist;
	//public static Item canned_spam;
	//public static Item canned_fried;
	//public static Item canned_napalm;
	//public static Item canned_diesel;
	//public static Item canned_kerosene;
	//public static Item canned_recursion;
	//public static Item canned_bark;
	public static ItemEnumMulti canned_conserve;
	public static Item can_key;

	public static Item glass_empty;
	public static Item glass_smilk;
	public static Item strawberry;
	public static Item teaseeds;
	public static Item tea_leaf;
	public static Item bean_raw;
	public static Item bean_roast;
	public static Item powder_coffee;
	public static Item cmug_empty;
	public static Item teacup;
	public static Item teacup_empty;
	public static Item bottle_honey;

	public static Item boat_rubber;
	public static Item cart;
	public static Item train;
	public static Item drone;

	public static Item coin_creeper;
	public static Item coin_radiation;
	public static Item coin_maskman;
	public static Item coin_worm;
	public static Item coin_ufo;
	public static Item coin_airliner;
	//public static Item coin_siege;
	//public static Item source;

	public static Item rod_empty;
	public static Item rod;
	public static Item rod_dual_empty;
	public static Item rod_dual;
	public static Item rod_quad_empty;
	public static Item rod_quad;

	public static Item rod_zirnox_empty;
	public static Item rod_zirnox_tritium;
	public static ItemEnumMulti rod_zirnox;

	public static ItemEnumMulti full_drive;
	public static Item hard_drive;

	public static Item rod_zirnox_natural_uranium_fuel_depleted;
	public static Item rod_zirnox_uranium_fuel_depleted;
	public static Item rod_zirnox_thorium_fuel_depleted;
	public static Item rod_zirnox_mox_fuel_depleted;
	public static Item rod_zirnox_plutonium_fuel_depleted;
	public static Item rod_zirnox_u233_fuel_depleted;
	public static Item rod_zirnox_u235_fuel_depleted;
	public static Item rod_zirnox_les_fuel_depleted;
	public static Item rod_zirnox_zfb_mox_depleted;

	public static Item waste_natural_uranium;
	public static Item waste_uranium;
	public static Item waste_thorium;
	public static Item waste_mox;
	public static Item waste_plutonium;
	public static Item waste_u233;
	public static Item waste_u235;
	public static Item waste_schrabidium;
	public static Item waste_zfb_mox;

	public static Item waste_plate_u233;
	public static Item waste_plate_u235;
	public static Item waste_plate_mox;
	public static Item waste_plate_pu239;
	public static Item waste_plate_sa326;
	public static Item waste_plate_ra226be;
	public static Item waste_plate_pu238be;

	public static Item pile_rod_uranium;
	public static Item pile_rod_pu239;
	public static Item pile_rod_plutonium;
	public static Item pile_rod_source;
	public static Item pile_rod_boron;
	public static Item pile_rod_lithium;
	public static Item pile_rod_detector;

	public static Item plate_fuel_u233;
	public static Item plate_fuel_u235;
	public static Item plate_fuel_mox;
	public static Item plate_fuel_pu239;
	public static Item plate_fuel_sa326;
	public static Item plate_fuel_ra226be;
	public static Item plate_fuel_pu238be;

	public static Item pwr_fuel;
	public static Item pwr_fuel_hot;
	public static Item pwr_fuel_depleted;

	public static Item rbmk_lid;
	public static Item rbmk_lid_glass;
	public static Item rbmk_fuel_empty;
	public static ItemRBMKRod rbmk_fuel_ueu;
	public static ItemRBMKRod rbmk_fuel_meu;
	public static ItemRBMKRod rbmk_fuel_heu233;
	public static ItemRBMKRod rbmk_fuel_heu235;
	public static ItemRBMKRod rbmk_fuel_thmeu;
	public static ItemRBMKRod rbmk_fuel_lep;
	public static ItemRBMKRod rbmk_fuel_mep;
	public static ItemRBMKRod rbmk_fuel_hep239;
	public static ItemRBMKRod rbmk_fuel_hep241;
	public static ItemRBMKRod rbmk_fuel_lea;
	public static ItemRBMKRod rbmk_fuel_mea;
	public static ItemRBMKRod rbmk_fuel_hea241;
	public static ItemRBMKRod rbmk_fuel_hea242;
	public static ItemRBMKRod rbmk_fuel_bk247;
	public static ItemRBMKRod rbmk_fuel_men;
	public static ItemRBMKRod rbmk_fuel_hen;
	public static ItemRBMKRod rbmk_fuel_mox;
	public static ItemRBMKRod rbmk_fuel_les;
	public static ItemRBMKRod rbmk_fuel_mes;
	public static ItemRBMKRod rbmk_fuel_hes;
	public static ItemRBMKRod rbmk_fuel_leaus;
	public static ItemRBMKRod rbmk_fuel_heaus;
	public static ItemRBMKRod rbmk_fuel_po210be;
	public static ItemRBMKRod rbmk_fuel_ra226be;
	public static ItemRBMKRod rbmk_fuel_pu238be;
	public static ItemRBMKRod rbmk_fuel_balefire_gold;
	public static ItemRBMKRod rbmk_fuel_flashlead;
	public static ItemRBMKRod rbmk_fuel_balefire;
	public static ItemRBMKRod rbmk_fuel_zfb_bismuth;
	public static ItemRBMKRod rbmk_fuel_zfb_pu241;
	public static ItemRBMKRod rbmk_fuel_zfb_am_mix;
	//public static ItemRBMKRod rbmk_fuel_drx;
	public static ItemRBMKRod rbmk_fuel_test;
	public static ItemRBMKRod rbmk_fuel_lecm;
	public static ItemRBMKRod rbmk_fuel_mecm;
	public static ItemRBMKRod rbmk_fuel_hecm;
	public static ItemRBMKRod rbmk_fuel_lecf;
	public static ItemRBMKRod rbmk_fuel_mecf;
	public static ItemRBMKRod rbmk_fuel_hecf;
	public static ItemRBMKPellet rbmk_pellet_ueu;
	public static ItemRBMKPellet rbmk_pellet_meu;
	public static ItemRBMKPellet rbmk_pellet_heu233;
	public static ItemRBMKPellet rbmk_pellet_heu235;
	public static ItemRBMKPellet rbmk_pellet_thmeu;
	public static ItemRBMKPellet rbmk_pellet_lep;
	public static ItemRBMKPellet rbmk_pellet_mep;
	public static ItemRBMKPellet rbmk_pellet_hep239;
	public static ItemRBMKPellet rbmk_pellet_hep241;
	public static ItemRBMKPellet rbmk_pellet_lea;
	public static ItemRBMKPellet rbmk_pellet_mea;
	public static ItemRBMKPellet rbmk_pellet_hea241;
	public static ItemRBMKPellet rbmk_pellet_hea242;
	public static ItemRBMKPellet rbmk_pellet_bk247;
	public static ItemRBMKPellet rbmk_pellet_men;
	public static ItemRBMKPellet rbmk_pellet_hen;
	public static ItemRBMKPellet rbmk_pellet_mox;
	public static ItemRBMKPellet rbmk_pellet_les;
	public static ItemRBMKPellet rbmk_pellet_mes;
	public static ItemRBMKPellet rbmk_pellet_hes;
	public static ItemRBMKPellet rbmk_pellet_leaus;
	public static ItemRBMKPellet rbmk_pellet_heaus;
	public static ItemRBMKPellet rbmk_pellet_po210be;
	public static ItemRBMKPellet rbmk_pellet_ra226be;
	public static ItemRBMKPellet rbmk_pellet_pu238be;
	public static ItemRBMKPellet rbmk_pellet_balefire_gold;
	public static ItemRBMKPellet rbmk_pellet_flashlead;
	public static ItemRBMKPellet rbmk_pellet_balefire;
	public static ItemRBMKPellet rbmk_pellet_zfb_bismuth;
	public static ItemRBMKPellet rbmk_pellet_zfb_pu241;
	public static ItemRBMKPellet rbmk_pellet_zfb_am_mix;
	//public static ItemRBMKPellet rbmk_pellet_drx;
	public static ItemRBMKPellet rbmk_pellet_lecm;
	public static ItemRBMKPellet rbmk_pellet_mecm;
	public static ItemRBMKPellet rbmk_pellet_hecm;
	public static ItemRBMKPellet rbmk_pellet_lecf;
	public static ItemRBMKPellet rbmk_pellet_mecf;
	public static ItemRBMKPellet rbmk_pellet_hecf;

	public static Item watz_pellet;
	public static Item watz_pellet_depleted;

	public static Item powder_spent_haleu;
	public static Item powder_spent_leu;
	public static Item powder_spent_thorium;
	public static Item powder_spent_u233;
	public static Item powder_spent_mox;

	public static Item dust_graphite;
	public static Item powder_lead_irradiated;
	public static Item powder_boron_spent;
	public static Item powder_du_spent;

	public static Item icf_pellet_empty;
	public static Item icf_pellet;
	public static Item icf_pellet_depleted;

	public static Item scrap_plastic;
	public static Item scrap;
	public static Item scrap_oil;
	public static Item scrap_nuclear;
	public static Item trinitite;
	public static Item nuclear_waste_long;
	public static Item nuclear_waste_long_tiny;
	public static Item nuclear_waste_short;
	public static Item nuclear_waste_short_tiny;
	public static Item nuclear_waste_long_depleted;
	public static Item nuclear_waste_long_depleted_tiny;
	public static Item nuclear_waste_short_depleted;
	public static Item nuclear_waste_short_depleted_tiny;
	public static Item nuclear_waste;
	public static Item nuclear_waste_tiny;
	public static Item nuclear_waste_vitrified;
	public static Item nuclear_waste_vitrified_tiny;

	public static Item debris_graphite;
	public static Item debris_metal;
	public static Item debris_fuel;
	public static Item debris_concrete;
	public static Item debris_exchanger;
	public static Item debris_shrapnel;
	public static Item debris_element;

	public static Item containment_box;
	public static Item plastic_bag;

	public static Item test_nuke_igniter;
	public static Item test_nuke_propellant;
	public static Item test_nuke_tier1_shielding;
	public static Item test_nuke_tier2_shielding;
	public static Item test_nuke_tier1_bullet;
	public static Item test_nuke_tier2_bullet;
	public static Item test_nuke_tier1_target;
	public static Item test_nuke_tier2_target;

	public static Item cordite;
	public static Item ballistite;
	public static Item ball_dynamite;
	public static Item ball_tnt;
	public static Item ball_tatb;
	public static Item ball_fireclay;
	public static Item ammonium_nitrate;
	public static Item ball_ferric_clay;

	public static Item pellet_cluster;
	public static Item powder_fire;
	public static Item powder_ice;
	public static Item powder_poison;
	public static Item powder_thermite;
	public static Item pellet_gas;
	public static Item magnetron;
	public static Item pellet_buckshot;
	public static Item pellet_flechette;
	public static Item pellet_chlorophyte;
	public static Item pellet_mercury;
	public static Item pellet_meteorite;
	public static Item pellet_canister;
	public static Item pellet_claws;
	public static Item pellet_charged;

	public static Item designator;
	public static Item designator_range;
	public static Item designator_manual;
	public static Item designator_arty_range;
	public static Item linker;
	public static Item reactor_sensor;
	public static Item oil_detector;
	public static Item dosimeter;
	public static Item geiger_counter;
	public static Item digamma_diagnostic;
	public static Item pollution_detector;
	public static Item ore_density_scanner;
	public static Item survey_scanner;
	public static Item mirror_tool;
	public static Item rbmk_tool;
	public static Item coltan_tool;
	public static Item power_net_tool;
	public static Item analysis_tool;
	public static Item coupling_tool;
	public static Item drone_linker;
	public static Item radar_linker;
	public static Item settings_tool;

	public static Item template_folder;
	public static Item journal_pip;
	public static Item journal_bj;
	public static Item journal_silver;
	public static Item assembly_template;
	public static Item chemistry_template;
	public static Item chemistry_icon;
	public static Item crucible_template;
	public static Item fluid_identifier;
	public static Item fluid_identifier_multi;
	public static Item fluid_icon;
	public static Item siren_track;
	public static Item fluid_duct;

	public static Item bobmazon_materials;
	public static Item bobmazon_machines;
	public static Item bobmazon_weapons;
	public static Item bobmazon_tools;
	public static Item bobmazon_hidden;

	public static Item launch_code_piece;
	public static Item launch_code;
	public static Item launch_key;

	public static Item transporter_linker;

	public static Item missile_assembly;
	public static Item missile_generic;
	public static Item missile_anti_ballistic;
	public static Item missile_incendiary;
	public static Item missile_cluster;
	public static Item missile_buster;
	public static Item missile_decoy;
	public static Item missile_strong;
	public static Item missile_incendiary_strong;
	public static Item missile_cluster_strong;
	public static Item missile_buster_strong;
	public static Item missile_emp_strong;
	public static Item missile_burst;
	public static Item missile_inferno;
	public static Item missile_rain;
	public static Item missile_drill;
	public static Item missile_nuclear;
	public static Item missile_nuclear_cluster;
	//public static Item missile_volcano;
	public static Item missile_doomsday;
	public static Item missile_doomsday_rusted;
	//public static Item missile_taint;
	public static Item missile_micro;
	//public static Item missile_bhole;
	//public static Item missile_schrabidium;
	public static Item missile_emp;
	public static Item missile_shuttle;
	public static Item missile_stealth;
	public static Item missile_test;

	public static Item mp_thruster_10_kerosene;
	public static Item mp_thruster_10_kerosene_tec;
	public static Item mp_thruster_10_solid;
	public static Item mp_thruster_10_hydrazine;
	public static Item mp_thruster_10_xenon;
	public static Item mp_thruster_15_kerosene;
	public static Item mp_thruster_15_kerosene_tec;
	public static Item mp_thruster_15_kerosene_dual;
	public static Item mp_thruster_15_kerosene_triple;
	public static Item mp_thruster_15_solid;
	public static Item mp_thruster_15_solid_hexdecuple;
	public static Item mp_thruster_15_hydrogen;
	public static Item mp_thruster_15_hydrogen_dual;
	public static Item mp_thruster_15_balefire_short;
	public static Item mp_thruster_15_balefire;
	public static Item mp_thruster_15_balefire_large;
	public static Item mp_thruster_15_balefire_large_rad;
	public static Item mp_thruster_20_kerosene;
	public static Item mp_thruster_20_kerosene_dual;
	public static Item mp_thruster_20_kerosene_triple;
	public static Item mp_thruster_20_methalox;
	public static Item mp_thruster_20_methalox_dual;
	public static Item mp_thruster_20_methalox_triple;
	public static Item mp_thruster_20_hydrogen;
	public static Item mp_thruster_20_hydrogen_dual;
	public static Item mp_thruster_20_hydrogen_triple;
	public static Item mp_thruster_20_solid;
	public static Item mp_thruster_20_solid_multi;
	public static Item mp_thruster_20_solid_multier;
	public static Item mp_thruster_20_hydrazine;

	public static Item mp_stability_10_flat;
	public static Item mp_stability_10_cruise;
	public static Item mp_stability_10_space;
	public static Item mp_stability_15_flat;
	public static Item mp_stability_15_thin;
	public static Item mp_stability_15_soyuz;
	public static Item mp_stability_20_flat;

	public static Item mp_fuselage_10_kerosene;
	public static Item mp_fuselage_10_kerosene_camo;
	public static Item mp_fuselage_10_kerosene_desert;
	public static Item mp_fuselage_10_kerosene_sky;
	public static Item mp_fuselage_10_kerosene_flames;
	public static Item mp_fuselage_10_kerosene_insulation;
	public static Item mp_fuselage_10_kerosene_sleek;
	public static Item mp_fuselage_10_kerosene_metal;
	public static Item mp_fuselage_10_kerosene_taint;

	public static Item mp_fuselage_10_solid;
	public static Item mp_fuselage_10_solid_flames;
	public static Item mp_fuselage_10_solid_insulation;
	public static Item mp_fuselage_10_solid_sleek;
	public static Item mp_fuselage_10_solid_soviet_glory;
	public static Item mp_fuselage_10_solid_cathedral;
	public static Item mp_fuselage_10_solid_moonlit;
	public static Item mp_fuselage_10_solid_battery;
	public static Item mp_fuselage_10_solid_duracell;

	public static Item mp_fuselage_10_hydrazine;

	public static Item mp_fuselage_10_xenon;
	public static Item mp_fuselage_10_xenon_bhole;

	public static Item mp_fuselage_10_long_kerosene;
	public static Item mp_fuselage_10_long_kerosene_camo;
	public static Item mp_fuselage_10_long_kerosene_desert;
	public static Item mp_fuselage_10_long_kerosene_sky;
	public static Item mp_fuselage_10_long_kerosene_flames;
	public static Item mp_fuselage_10_long_kerosene_insulation;
	public static Item mp_fuselage_10_long_kerosene_sleek;
	public static Item mp_fuselage_10_long_kerosene_metal;
	public static Item mp_fuselage_10_long_kerosene_taint;
	public static Item mp_fuselage_10_long_kerosene_dash;
	public static Item mp_fuselage_10_long_kerosene_vap;

	public static Item mp_fuselage_10_long_solid;
	public static Item mp_fuselage_10_long_solid_flames;
	public static Item mp_fuselage_10_long_solid_insulation;
	public static Item mp_fuselage_10_long_solid_sleek;
	public static Item mp_fuselage_10_long_solid_soviet_glory;
	public static Item mp_fuselage_10_long_solid_bullet;
	public static Item mp_fuselage_10_long_solid_silvermoonlight;

	public static Item mp_fuselage_10_15_kerosene;
	public static Item mp_fuselage_10_15_solid;
	public static Item mp_fuselage_10_15_hydrogen;
	public static Item mp_fuselage_10_15_balefire;

	public static Item mp_fuselage_15_kerosene;
	public static Item mp_fuselage_15_kerosene_camo;
	public static Item mp_fuselage_15_kerosene_desert;
	public static Item mp_fuselage_15_kerosene_sky;
	public static Item mp_fuselage_15_kerosene_insulation;
	public static Item mp_fuselage_15_kerosene_metal;
	public static Item mp_fuselage_15_kerosene_decorated;
	public static Item mp_fuselage_15_kerosene_steampunk;
	public static Item mp_fuselage_15_kerosene_polite;
	public static Item mp_fuselage_15_kerosene_blackjack;
	public static Item mp_fuselage_15_kerosene_lambda;
	public static Item mp_fuselage_15_kerosene_minuteman;
	public static Item mp_fuselage_15_kerosene_pip;
	public static Item mp_fuselage_15_kerosene_taint;
	public static Item mp_fuselage_15_kerosene_yuck;

	public static Item mp_fuselage_15_solid;
	public static Item mp_fuselage_15_solid_insulation;
	public static Item mp_fuselage_15_solid_desh;
	public static Item mp_fuselage_15_solid_soviet_glory;
	public static Item mp_fuselage_15_solid_soviet_stank;
	public static Item mp_fuselage_15_solid_faust;
	public static Item mp_fuselage_15_solid_silvermoonlight;
	public static Item mp_fuselage_15_solid_snowy;
	public static Item mp_fuselage_15_solid_panorama;
	public static Item mp_fuselage_15_solid_roses;
	public static Item mp_fuselage_15_solid_mimi;

	public static Item mp_fuselage_15_hydrogen;
	public static Item mp_fuselage_15_hydrogen_cathedral;

	//public static Item mp_fuselage_15_balefire;

	public static Item mp_fuselage_15_20_kerosene;
	public static Item mp_fuselage_15_20_kerosene_magnusson;
	public static Item mp_fuselage_15_20_solid;


	public static Item mp_fuselage_20_kerosene;

	public static Item mp_warhead_10_he;
	public static Item mp_warhead_10_incendiary;
	public static Item mp_warhead_10_buster;
	public static Item mp_warhead_10_nuclear;
	public static Item mp_warhead_10_nuclear_large;
	//public static Item mp_warhead_10_taint;
	//public static Item mp_warhead_10_cloud;
	public static Item mp_warhead_15_he;
	public static Item mp_warhead_15_incendiary;
	public static Item mp_warhead_15_nuclear;
	public static Item mp_warhead_15_nuclear_shark;
	public static Item mp_warhead_15_nuclear_mimi;
	//public static Item mp_warhead_15_boxcar;
	public static Item mp_warhead_15_n2;
	//public static Item mp_warhead_15_balefire;
	//public static Item mp_warhead_15_turbine;
	public static Item mp_warhead_20_he;

	public static Item mp_chip_1;
	public static Item mp_chip_2;
	public static Item mp_chip_3;
	public static Item mp_chip_4;
	public static Item mp_chip_5;

	public static Item rp_fuselage_20_12;
	public static Item rp_fuselage_20_6;
	public static Item rp_fuselage_20_3;
	public static Item rp_fuselage_20_1;
	public static Item rp_fuselage_20_12_hydrazine;

	public static Item rp_legs_20;

	public static Item rp_capsule_20;
	public static Item rp_station_core_20;
	public static Item rp_pod_20;

	public static Item missile_skin_camo;
	public static Item missile_skin_desert;
	public static Item missile_skin_flames;
	public static Item missile_skin_manly_pink;
	public static Item missile_skin_orange_insulation;
	public static Item missile_skin_sleek;
	public static Item missile_skin_soviet_glory;
	public static Item missile_skin_soviet_stank;
	public static Item missile_skin_metal;

	public static Item missile_custom;
	public static Item rocket_custom;

	public static Item missile_carrier;
	public static Item missile_soyuz;
	public static Item missile_soyuz_lander;
	public static Item sat_mapper;
	public static Item sat_scanner;
	public static Item sat_radar;
	public static Item sat_laser;
	public static Item sat_foeq;
	public static Item sat_resonator;
	public static Item sat_miner;
	public static Item sat_lunar_miner;
	//public static Item sat_gerald;
	public static Item sat_chip;

	public static Item sat_interface;
	public static Item sat_coord;
	public static Item sat_designator;
	public static Item sat_relay;

	public static ItemEnumMulti ammo_misc;
	public static ItemEnumMulti ammo_12gauge;
	public static ItemEnumMulti ammo_20gauge;
	public static ItemEnumMulti ammo_4gauge;
	public static ItemEnumMulti ammo_357;
	public static ItemEnumMulti ammo_44;
	public static ItemEnumMulti ammo_5mm;
	public static ItemEnumMulti ammo_9mm;
	public static ItemEnumMulti ammo_45;
	public static ItemEnumMulti ammo_556;
	public static ItemEnumMulti ammo_762;
	public static ItemEnumMulti ammo_22lr;
	public static ItemEnumMulti ammo_50ae;
	public static ItemEnumMulti ammo_50bmg;
	public static ItemEnumMulti ammo_75bolt;
	public static ItemEnumMulti ammo_rocket;
	public static ItemEnumMulti ammo_grenade;
	public static ItemEnumMulti ammo_shell;
	public static ItemEnumMulti ammo_nuke;
	public static ItemEnumMulti ammo_fuel;
	public static ItemEnumMulti ammo_fireext;
	public static ItemEnumMulti ammo_dart;
	public static ItemEnumMulti ammo_stinger_rocket;
	public static ItemEnumMulti ammo_luna_sniper;
	public static ItemEnumMulti ammo_coilgun;

	public static Item ammo_cell;

	public static Item ammo_folly;
	public static Item ammo_folly_nuclear;
	public static Item ammo_folly_du;
	public static Item ammo_dgk;
	public static Item ammo_arty;
	public static Item ammo_himars;

	public static Item gun_rpg;
	public static Item gun_karl;
	public static Item gun_panzerschreck;
	public static Item gun_quadro;
	//public static Item gun_hk69;
	//public static Item gun_congolake;
	public static Item gun_stinger;
	public static Item gun_skystinger;
	//public static Item gun_revolver;
	//public static Item gun_revolver_saturnite;
	//public static Item gun_revolver_gold;
	//public static Item gun_revolver_schrabidium;
	//public static Item gun_revolver_cursed;
	//public static Item gun_revolver_nightmare;
	//public static Item gun_revolver_nightmare2;
	//public static Item gun_revolver_pip;
	//may add back for FBI idk bob's guns are just annoying//public static Item gun_revolver_nopip;
	//public static Item gun_revolver_blackjack;
	//public static Item gun_revolver_silver;
	//public static Item gun_revolver_red;
	//public static Item gun_bio_revolver;
	//public static Item gun_deagle;
	//public static Item gun_flechette;
	public static Item gun_ar15;
	//admin abuse or something idk
	//public static Item gun_calamity;
	//public static Item gun_minigun;
	//public static Item gun_lacunae;
	//public static Item gun_folly;
	//public static Item gun_fatman;
	//public static Item gun_proto;
	//public static Item gun_mirv;
	//public static Item gun_bf;
	public static Item gun_chemthrower;
	//public static Item gun_mp40;
	//public static Item gun_thompson;
	//public static Item gun_uzi;
	//public static Item gun_uzi_silencer;
	//public static Item gun_uzi_saturnite;
	//public static Item gun_uzi_saturnite_silencer;
	//public static Item gun_uboinik;
	//public static Item gun_remington;
	//public static Item gun_spas12;
	//public static Item gun_supershotgun;
	//public static Item gun_benelli;
	//save//public static Item gun_ks23;
	//public static Item gun_sauer;
	//public static Item gun_lever_action;
	//public static Item gun_lever_action_dark;
	//mc nut device
	public static Item gun_lever_action_sonata;
	//public static Item gun_bolt_action;
	//public static Item gun_bolt_action_green;
	//public static Item gun_bolt_action_saturnite;
	//public static Item gun_mymy;
	//public static Item gun_b92;
	//public static Item gun_b92_ammo;
	//public static Item gun_b93;
	//public static Item gun_coilgun;
	//public static Item gun_xvl1456;
	//public static Item gun_xvl1456_ammo;
	//public static Item gun_osipr;
	//public static Item gun_osipr_ammo;
	//public static Item gun_osipr_ammo2;
	//public static Item gun_immolator;
	//public static Item gun_immolator_ammo;
	public static Item gun_flamer;
	//public static Item gun_cryolator;
	//public static Item gun_cryocannon;
	//public static Item gun_cryolator_ammo;
	public static Item gun_fireext;
	//public static Item gun_mp;
	//public static Item gun_bolter;
	//public static Item gun_bolter_digamma;
	//public static Item gun_zomg;
	//public static Item gun_super_shotgun;
	//public static Item gun_moist_nugget;
	//edgelord maxxing
	public static Item gun_revolver_inverted;
	//public static Item gun_emp;
	//public static Item gun_emp_ammo;
	//public static Item gun_jack;
	//public static Item gun_jack_ammo;
	//public static Item gun_spark;
	//public static Item gun_spark_ammo;
	//ublic static Item gun_hp;
	//public static Item gun_hp_ammo;
	//public static Item gun_euthanasia;
	//public static Item gun_euthanasia_ammo;
	//public static Item gun_defabricator;
	//public static Item gun_defabricator_ammo;
	//public static Item gun_vortex;
	//public static Item gun_waluigi;
	//public static Item gun_darter;
	//public static Item gun_glass_cannon;
	//public static Item gun_m2;
	//public static Item gun_lunatic_marksman;
	//public static Item gun_uac_pistol;

	public static Item gun_debug;
	public static Item ammo_debug;

	public static Item crucible;

	public static Item stick_dynamite;
	public static Item stick_dynamite_fishing;
	public static Item stick_tnt;
	public static Item stick_semtex;
	public static Item stick_c4;

	public static Item grenade_generic;
	public static Item grenade_strong;
	public static Item grenade_frag;
	public static Item grenade_fire;
	public static Item grenade_shrapnel;
	public static Item grenade_cluster;
	public static Item grenade_flare;
	public static Item grenade_electric;
	public static Item grenade_poison;
	public static Item grenade_gas;
	public static Item grenade_pulse;
	public static Item grenade_plasma;
	public static Item grenade_tau;
	public static Item grenade_schrabidium;
	//public static Item grenade_lemon;
	public static Item grenade_gascan;
	public static Item grenade_kyiv;
	public static Item grenade_mk2;
	public static Item grenade_aschrab;
	public static Item grenade_nuke;
	public static Item grenade_nuclear;
	public static Item grenade_zomg;
	public static Item grenade_black_hole;
	public static Item grenade_cloud;
	public static Item grenade_pink_cloud;
	//public static Item ullapool_caber;

	public static Item grenade_if_generic;
	public static Item grenade_if_he;
	public static Item grenade_if_bouncy;
	public static Item grenade_if_sticky;
	public static Item grenade_if_impact;
	public static Item grenade_if_incendiary;
	public static Item grenade_if_toxic;
	public static Item grenade_if_concussion;
	public static Item grenade_if_brimstone;
	public static Item grenade_if_mystery;
	public static Item grenade_if_spark;
	public static Item grenade_if_hopwire;
	public static Item grenade_if_null;

	public static Item grenade_smart;
	public static Item grenade_mirv;
	public static Item grenade_breach;
	public static Item grenade_burst;

	//public static Item nuclear_waste_pearl;

	public static Item weaponized_starblaster_cell;

	//public static Item bomb_waffle;
	public static Item schnitzel_vegan;
	public static Item cotton_candy;
	//public static Item apple_lead;
	//public static Item apple_schrabidium;
	//public static Item tem_flakes;
	//public static Item glowing_stew;
	//public static Item balefire_scrambled;
	//public static Item balefire_and_ham;
	//public static Item lemon;
	public static Item definitelyfood;
	public static Item loops;
	public static Item loop_stew;
	public static Item spongebob_macaroni;
	//public static Item fooditem;
	public static Item twinkie;
	//public static Item static_sandwich;
	public static Item pudding;
	//public static Item pancake;
	public static Item nugget;
	public static Item peas;
	public static Item marshmallow;
	public static Item cheese;
	public static Item quesadilla;
	//public static Item glyphid_meat;
	//public static Item glyphid_meat_grilled;
	//public static Item egg_glyphid;

	public static Item med_ipecac;
	public static Item med_ptsd;
	public static Item med_schizophrenia;

	public static Item canteen_vodka;
	public static Item canteen_fab;

	public static Item defuser;
	public static Item reacher;
	public static Item bismuth_tool;
	public static Item meltdown_tool;

	public static Item flame_pony;
	public static Item flame_conspiracy;
	public static Item flame_politics;
	public static Item flame_opinion;

	//public static Item gadget_explosive;
	public static Item early_explosive_lenses;
	public static Item explosive_lenses;
	public static Item gadget_wireing;
	public static Item gadget_core;
	public static Item boy_igniter;
	public static Item boy_propellant;
	public static Item boy_bullet;
	public static Item boy_target;
	public static Item boy_shielding;
	//public static Item man_explosive;
	public static Item man_igniter;
	public static Item man_core;
	public static Item mike_core;
	public static Item mike_deut;
	public static Item mike_cooling_unit;

	public static Item shrimp_lithium;

	public static Item tsar_corereal;
	public static Item tsar_core;
	public static Item tsar_corelead;
	public static Item fleija_igniter;
	public static Item fleija_propellant;
	public static Item fleija_core;
	public static Item solinium_igniter;
	public static Item solinium_propellant;
	public static Item solinium_core;
	public static Item n2_charge;
	//public static Item egg_balefire_shard;
	//public static Item egg_balefire;

	public static Item custom_tnt;
	public static Item custom_nuke;
	public static Item custom_hydro;
	public static Item custom_amat;
	public static Item custom_dirty;
	public static Item custom_schrab;
	public static Item custom_fall;

	public static Item battery_generic;
	public static Item battery_advanced;
	public static Item battery_lithium;
	public static Item battery_schrabidium;
	public static Item battery_spark;
	public static Item battery_trixite;
	public static Item battery_creative;

	public static Item battery_red_cell;
	public static Item battery_red_cell_6;
	public static Item battery_red_cell_24;
	public static Item battery_advanced_cell;
	public static Item battery_advanced_cell_4;
	public static Item battery_advanced_cell_12;
	public static Item battery_lithium_cell;
	public static Item battery_lithium_cell_3;
	public static Item battery_lithium_cell_6;
	public static Item battery_schrabidium_cell;
	public static Item battery_schrabidium_cell_2;
	public static Item battery_schrabidium_cell_4;
	public static Item battery_spark_cell_6;
	public static Item battery_spark_cell_25;
	public static Item battery_spark_cell_100;
	public static Item battery_spark_cell_1000;
	public static Item battery_spark_cell_2500;
	public static Item battery_spark_cell_10000;
	public static Item battery_spark_cell_power;
	public static Item cube_power;

	public static Item battery_sc_uranium;
	public static Item battery_sc_technetium;
	public static Item battery_sc_plutonium;
	public static Item battery_sc_polonium;
	public static Item battery_sc_gold;
	public static Item battery_sc_lead;
	public static Item battery_sc_americium;

	public static Item battery_potato;
	public static Item battery_potatos;
	public static Item hev_battery;
	public static Item fusion_core;
	public static Item fusion_core_infinite;
	public static Item energy_core;
	public static Item fuse;
	public static Item redcoil_capacitor;
	public static Item euphemium_capacitor;
	//by using these in crafting table recipes, i'm running the risk of making my recipes too greg-ian (which i don't like)
	//in the event that i forget about the meaning of the word "sparingly", please throw a brick at my head
	//how about you get a job
	public static Item screwdriver;
	public static Item screwdriver_desh;
	public static Item hand_drill;
	public static Item hand_drill_desh;
	//what the fuck is the point of this bullshit, they don't even fucking work as hand drills, OH the chicago pile
	public static Item wrench_archineer;
	public static Item chemistry_set;
	public static Item chemistry_set_boron;
	public static Item blowtorch;
	public static Item acetylene_torch;
	public static Item boltgun;
	public static Item overfuse;
	public static Item arc_electrode;
	public static Item arc_electrode_burnt;

	/*public static Item factory_core_titanium;
	public static Item factory_core_advanced;*/

	public static Item upgrade_muffler;

	public static Item upgrade_template;
	public static Item upgrade_speed_1;
	public static Item upgrade_speed_2;
	public static Item upgrade_speed_3;
	public static Item upgrade_effect_1;
	public static Item upgrade_effect_2;
	public static Item upgrade_effect_3;
	public static Item upgrade_power_1;
	public static Item upgrade_power_2;
	public static Item upgrade_power_3;
	public static Item upgrade_fortune_1;
	public static Item upgrade_fortune_2;
	public static Item upgrade_fortune_3;
	public static Item upgrade_afterburn_1;
	public static Item upgrade_afterburn_2;
	public static Item upgrade_afterburn_3;
	public static Item upgrade_overdrive_1;
	public static Item upgrade_overdrive_2;
	public static Item upgrade_overdrive_3;
	public static Item upgrade_radius;
	public static Item upgrade_health;
	public static Item upgrade_smelter;
	public static Item upgrade_shredder;
	public static Item upgrade_centrifuge;
	public static Item upgrade_crystallizer;
	public static Item upgrade_nullifier;
	public static Item upgrade_screm;
	public static Item upgrade_gc_speed;
	public static Item upgrade_5g;
	public static Item upgrade_stack;
	public static Item upgrade_ejector;

	public static Item ingot_euphemium;
	public static Item nugget_euphemium;
	//public static Item euphemium_helmet;
	//public static Item euphemium_plate;
	//public static Item euphemium_legs;
	//public static Item euphemium_boots;
	//public static Item apple_euphemium;
	public static Item watch;
	public static Item euphemium_stopper;

	public static Item goggles;
	public static Item ashglasses;
	public static Item gas_mask;
	public static Item gas_mask_m65;
	public static Item gas_mask_mono;
	public static Item gas_mask_olde;
	public static Item mask_rag;
	public static Item mask_piss;
	public static Item hat;
	public static Item beta;
	public static Item no9;

	public static Item t45_helmet;
	public static Item t45_plate;
	public static Item t45_legs;
	public static Item t45_boots;
	public static Item steamsuit_helmet;
	public static Item steamsuit_plate;
	public static Item steamsuit_legs;
	public static Item steamsuit_boots;
	public static Item dieselsuit_helmet;
	public static Item dieselsuit_plate;
	public static Item dieselsuit_legs;
	public static Item dieselsuit_boots;

	public static Item chainsaw;

	public static Item schrabidium_helmet;
	public static Item schrabidium_plate;
	public static Item schrabidium_legs;
	public static Item schrabidium_boots;
	public static Item titanium_helmet;
	public static Item titanium_plate;
	public static Item titanium_legs;
	public static Item titanium_boots;
	public static Item steel_helmet;
	public static Item steel_plate;
	public static Item steel_legs;
	public static Item steel_boots;
	public static Item alloy_helmet;
	public static Item alloy_plate;
	public static Item alloy_legs;
	public static Item alloy_boots;
	public static Item cmb_helmet;
	public static Item cmb_plate;
	public static Item cmb_legs;
	public static Item cmb_boots;
	public static Item paa_plate;
	public static Item paa_legs;
	public static Item paa_boots;
	public static Item asbestos_helmet;
	public static Item asbestos_plate;
	public static Item asbestos_legs;
	public static Item asbestos_boots;
	public static Item security_helmet;
	public static Item security_plate;
	public static Item security_legs;
	public static Item security_boots;
	public static Item cobalt_helmet;
	public static Item cobalt_plate;
	public static Item cobalt_legs;
	public static Item cobalt_boots;
	//public static Item starmetal_helmet;
	//public static Item starmetal_plate;
	//public static Item starmetal_legs;
	//public static Item starmetal_boots;
	//public static Item dnt_helmet;
	//public static Item dnt_plate;
	//public static Item dnt_legs;
	//public static Item dnt_boots;
	public static Item ajr_helmet;
	public static Item ajr_plate;
	public static Item ajr_legs;
	public static Item ajr_boots;
	public static Item ajro_helmet;
	public static Item ajro_plate;
	public static Item ajro_legs;
	public static Item ajro_boots;
	public static Item rpa_helmet;
	public static Item rpa_plate;
	public static Item rpa_legs;
	public static Item rpa_boots;
	//public static Item bismuth_helmet;
	//public static Item bismuth_plate;
	//public static Item bismuth_legs;
	//public static Item bismuth_boots;
	public static Item bj_helmet;
	public static Item bj_plate;
	public static Item bj_plate_jetpack;
	public static Item bj_legs;
	public static Item bj_boots;
	public static Item envsuit_helmet;
	public static Item envsuit_plate;
	public static Item envsuit_legs;
	public static Item envsuit_boots;
	public static Item hev_helmet;
	public static Item hev_plate;
	public static Item hev_legs;
	public static Item hev_boots;
	public static Item fau_helmet;
	public static Item fau_plate;
	public static Item fau_legs;
	public static Item fau_boots;
	//public static Item dns_helmet;
	//public static Item dns_plate;
	//public static Item dns_legs;
	//public static Item dns_boots;
	public static Item trenchmaster_helmet;
	public static Item trenchmaster_plate;
	public static Item trenchmaster_legs;
	public static Item trenchmaster_boots;
	public static Item zirconium_legs;
	public static Item robes_helmet;
	public static Item robes_plate;
	public static Item robes_legs;
	public static Item robes_boots;

	//public static Item jetpack_boost;
	//public static Item jetpack_break;
	//public static Item jetpack_fly;
	//public static Item jetpack_vector;
	//public static Item wings_limp;
	//public static Item wings_murk;
	public static Item oxy_plss;

	public static Item jackt;
	public static Item jackt2;

	//public static Item schrabidium_sword;
	public static Item schrabidium_pickaxe;
	public static Item schrabidium_axe;
	public static Item schrabidium_shovel;
	public static Item schrabidium_hoe;
	//public static Item titanium_sword;
	public static Item titanium_pickaxe;
	public static Item titanium_axe;
	public static Item titanium_shovel;
	public static Item titanium_hoe;
	//public static Item steel_sword;
	public static Item steel_pickaxe;
	public static Item steel_axe;
	public static Item steel_shovel;
	public static Item steel_hoe;
	//public static Item alloy_sword;
	public static Item alloy_pickaxe;
	public static Item alloy_axe;
	public static Item alloy_shovel;
	public static Item alloy_hoe;
	public static Item alloy_knife;
	//public static Item cmb_sword;
	public static Item cmb_pickaxe;
	public static Item cmb_axe;
	public static Item cmb_shovel;
	public static Item cmb_hoe;
	//public static Item elec_sword;
	public static Item elec_pickaxe;
	public static Item elec_axe;
	public static Item elec_shovel;
	//public static Item desh_sword;
	public static Item desh_pickaxe;
	public static Item desh_axe;
	public static Item desh_shovel;
	public static Item desh_hoe;
	//public static Item cobalt_sword;
	public static Item cobalt_pickaxe;
	public static Item cobalt_axe;
	public static Item cobalt_shovel;
	public static Item cobalt_hoe;
	//public static Item cobalt_decorated_sword;
	public static Item cobalt_decorated_pickaxe;
	public static Item cobalt_decorated_axe;
	public static Item cobalt_decorated_shovel;
	public static Item cobalt_decorated_hoe;
	//public static Item starmetal_sword;
	public static Item starmetal_pickaxe;
	public static Item starmetal_axe;
	public static Item starmetal_shovel;
	public static Item starmetal_hoe;
	public static Item smashing_hammer;
	public static Item centri_stick;
	public static Item drax;
	public static Item drax_mk2;
	public static Item drax_mk3;
	public static Item bismuth_pickaxe;
	public static Item bismuth_axe;
	public static Item volcanic_pickaxe;
	public static Item volcanic_axe;
	public static Item chlorophyte_pickaxe;
	public static Item chlorophyte_axe;
	public static Item mese_pickaxe;
	public static Item mese_axe;
	//public static Item dnt_sword;
	public static Item dwarven_pickaxe;

	//public static Item meteorite_sword;
	//public static Item meteorite_sword_seared;
	//public static Item meteorite_sword_reforged;
	//public static Item meteorite_sword_hardened;
	//public static Item meteorite_sword_alloyed;
	//public static Item meteorite_sword_machined;
	//public static Item meteorite_sword_treated;
	//public static Item meteorite_sword_etched;
	//public static Item meteorite_sword_bred;
	//public static Item meteorite_sword_irradiated;
	//public static Item meteorite_sword_fused;
	//public static Item meteorite_sword_baleful;

	public static Item matchstick;
	//public static Item balefire_and_steel;

	public static Item mask_of_infamy;

	//public static Item schrabidium_hammer;
	//public static Item shimmer_sledge;
	//public static Item shimmer_axe;
	public static Item bottle_opener;
	public static Item pch; //for compat please do not hit me
	public static Item wood_gavel;
	public static Item lead_gavel;
	public static Item diamond_gavel;
	//public static Item mese_gavel;

	public static Item crowbar;

	public static Item wrench;
	//public static Item wrench_flipped;
	public static Item memespoon;

	public static Item multitool_hit;
	public static Item multitool_dig;
	public static Item multitool_silk;
	public static Item multitool_ext;
	public static Item multitool_miner;
	public static Item multitool_beam;
	public static Item multitool_sky;
	public static Item multitool_mega;
	public static Item multitool_joule;
	public static Item multitool_decon;

	public static Item saw;
	public static Item bat;
	public static Item bat_nail;
	public static Item golf_club;
	public static Item pipe_rusty;
	public static Item pipe_lead;
	public static Item reer_graar;
	public static Item stopsign;
	public static Item sopsign;
	public static Item chernobylsign;

	//public static Item slagpowder;

	public static Item crystal_horn;
	public static Item crystal_charred;

	public static Item attachment_mask;
	public static Item attachment_mask_mono;
	public static Item back_tesla;
	public static Item servo_set;
	public static Item servo_set_desh;
	public static Item pads_rubber;
	//public static Item pads_slime;
	public static Item pads_static;
	public static Item cladding_paint;
	public static Item cladding_rubber;
	public static Item cladding_lead;
	//public static Item cladding_desh;
	//public static Item cladding_ghiorsium;
	public static Item cladding_iron;
	public static Item cladding_obsidian;
	public static Item insert_kevlar;
	public static Item insert_sapi;
	public static Item insert_esapi;
	public static Item insert_xsapi;
	public static Item insert_steel;
	//public static Item insert_du;
	//public static Item insert_polonium;
	//public static Item insert_ghiorsium;
	//public static Item insert_cmb;
	//public static Item insert_era;
	//public static Item insert_yharonite;
	//public static Item insert_doxium;
	//public static Item armor_polish;
	//public static Item bandaid;
	//public static Item serum;
	//public static Item quartz_plutonium;
	//public static Item morning_glory;
	public static Item lodestone;
	public static Item horseshoe_magnet;
	public static Item industrial_magnet;
	//public static Item bathwater;
	//public static Item bathwater_mk2;
	//public static Item spider_milk;
	//public static Item ink;
	//public static Item heart_piece;
	//public static Item heart_container;
	//public static Item heart_booster;
	//public static Item heart_fab;
	//public static Item black_diamond;
	//public static Item wd40;
	//public static Item scrumpy;
	//public static Item wild_p;
	//public static Item fabsols_vodka;
	//public static Item shackles;
	//public static Item injector_5htp;
	//public static Item injector_knife;
	//public static Item medal_liquidator;
	//public static Item v1;
	//public static Item protection_charm;
	//public static Item meteor_charm;
	public static Item neutrino_lens;
	public static Item gas_tester;
	//public static Item defuser_gold;
	//public static Item ballistic_gauntlet;
	public static Item night_vision;
	public static Item card_aos;
	public static Item card_qos;
	//public static Item australium_iii;
	public static Item armor_battery;
	public static Item armor_battery_mk2;
	public static Item armor_battery_mk3;

	public static Item hazmat_helmet;
	public static Item hazmat_plate;
	public static Item hazmat_legs;
	public static Item hazmat_boots;
	public static Item hazmat_helmet_red;
	public static Item hazmat_plate_red;
	public static Item hazmat_legs_red;
	public static Item hazmat_boots_red;
	public static Item hazmat_helmet_grey;
	public static Item hazmat_plate_grey;
	public static Item hazmat_legs_grey;
	public static Item hazmat_boots_grey;
	public static Item liquidator_helmet;
	public static Item liquidator_plate;
	public static Item liquidator_legs;
	public static Item liquidator_boots;

	public static Item hazmat_paa_helmet;
	public static Item hazmat_paa_plate;
	public static Item hazmat_paa_legs;
	public static Item hazmat_paa_boots;

	public static Item wand;
	public static Item wand_s;
	public static Item wand_d;
	public static Item bomb_site_wand;

	public static Item structure_single;
	public static Item structure_solid;
	public static Item structure_pattern;
	public static Item structure_randomized;
	public static Item structure_randomly;
	public static Item structure_custommachine;

	public static Item rod_of_discord;

	//public static Item cape_radiation;
	//public static Item cape_gasmask;
	//public static Item cape_schrabidium;
	//public static Item cape_hidden;
	/*public static Item cape_hbm;
	public static Item cape_dafnik;
	public static Item cape_lpkukin;
	public static Item cape_vertice;
	public static Item cape_codered_;
	public static Item cape_ayy;
	public static Item cape_nostalgia;*/

	public static Item nuke_starter_kit;
	public static Item nuke_advanced_kit;
	public static Item nuke_commercially_kit;
	public static Item nuke_electric_kit;
	public static Item gadget_kit;
	public static Item boy_kit;
	public static Item man_kit;
	public static Item mike_kit;
	public static Item tsar_kit;
	public static Item multi_kit;
	public static Item custom_kit;
	public static Item grenade_kit;
	//public static Item fleija_kit;
	//public static Item prototype_kit;
	public static Item missile_kit;
	public static Item t45_kit;
	public static Item euphemium_kit;
	//public static Item solinium_kit;
	public static Item hazmat_kit;
	public static Item hazmat_red_kit;
	public static Item hazmat_grey_kit;
	public static Item kit_custom;
	public static Item kit_toolbox_empty;
	public static Item kit_toolbox;

	public static Item loot_10;
	public static Item loot_15;
	public static Item loot_misc;

	//public static Item clip_revolver_iron;
	//public static Item clip_revolver;
	//public static Item clip_revolver_gold;
	//public static Item clip_revolver_lead;
	//public static Item clip_revolver_schrabidium;
	//public static Item clip_revolver_cursed;
	//public static Item clip_revolver_nightmare;
	//public static Item clip_revolver_nightmare2;
	//public static Item clip_revolver_pip;
	//public static Item clip_revolver_nopip;
	public static Item clip_rpg;
	public static Item clip_stinger;
	//public static Item clip_fatman;
	//public static Item clip_mirv;
	//public static Item clip_bf;
	//public static Item clip_mp40;
	//public static Item clip_uzi;
	//public static Item clip_uboinik;
	//public static Item clip_lever_action;
	//public static Item clip_bolt_action;
	//public static Item clip_osipr;
	public static Item clip_immolator;
	//public static Item clip_cryolator;
	//public static Item clip_mp;
	//public static Item clip_xvl1456;
	//public static Item clip_emp;
	//public static Item clip_jack;
	//public static Item clip_spark;
	//public static Item clip_hp;
	//public static Item clip_euthanasia;
	//public static Item clip_defabricator;

	public static Item ammo_container;

	public static Item igniter;
	public static Item detonator;
	public static Item detonator_multi;
	public static Item detonator_laser;
	public static Item detonator_deadman;
	public static Item detonator_de;
	//public static Item crate_caller;
	public static Item bomb_caller;
	//public static Item meteor_remote;
	public static Item anchor_remote;
	public static Item remote;
	//public static Item turret_control;
	public static Item turret_chip;
	//public static Item turret_biometry;

	//public static Item spawn_chopper;
	public static Item spawn_worm;
	//public static Item spawn_ufo;
	//public static Item spawn_duck;
	public static Item spawn_uap;

	public static Item key;
	public static Item key_red;
	public static Item key_red_cracked;
	public static Item key_kit;
	public static Item key_fake;
	public static Item pin;
	public static Item padlock_rusty;
	public static Item padlock;
	public static Item padlock_reinforced;
	public static Item padlock_unbreakable;

	public static Item mech_key;

	//public static Item bucket_mud;
	public static Item bucket_acid;
	public static Item bucket_toxic;
	public static Item bucket_schrabidic_acid;
	public static Item bucket_sulfuric_acid;
	public static Item bucket_mercury;
	public static Item bucket_bromine;

	public static Item door_metal;
	public static Item door_office;
	public static Item door_bunker;
	public static Item door_red;

	public static Item sliding_blast_door_skin;

	public static Item record_lc;
	public static Item record_ss;
	public static Item record_vc;
	public static Item record_gs;
	public static Item record_gp;
	public static Item record_el;
	public static Item record_glass;

	public static Item book_guide;
	public static Item book_lore;
	public static Item holotape_image;
	public static Item holotape_damaged;

	//public static Item polaroid;
	public static Item glitch;
	public static Item letter;
	//public static Item book_secret;
	//public static Item book_of_;
	//public static Item page_of_;
	//public static Item book_lemegeton;
	public static Item burnt_bark;

	public static Item b_smoke1;
	public static Item b_smoke2;
	public static Item b_smoke3;
	public static Item b_smoke4;
	public static Item b_smoke5;
	public static Item b_smoke6;
	public static Item b_smoke7;
	public static Item b_smoke8;
	public static Item chlorine1;
	public static Item chlorine2;
	public static Item chlorine3;
	public static Item chlorine4;
	public static Item chlorine5;
	public static Item chlorine6;
	public static Item chlorine7;
	public static Item chlorine8;
	public static Item pc1;
	public static Item pc2;
	public static Item pc3;
	public static Item pc4;
	public static Item pc5;
	public static Item pc6;
	public static Item pc7;
	public static Item pc8;
	public static Item cloud1;
	public static Item cloud2;
	public static Item cloud3;
	public static Item cloud4;
	public static Item cloud5;
	public static Item cloud6;
	public static Item cloud7;
	public static Item cloud8;
	public static Item orange1;
	public static Item orange2;
	public static Item orange3;
	public static Item orange4;
	public static Item orange5;
	public static Item orange6;
	public static Item orange7;
	public static Item orange8;
	/*public static Item gasflame1;
	public static Item gasflame2;
	public static Item gasflame3;
	public static Item gasflame4;
	public static Item gasflame5;
	public static Item gasflame6;
	public static Item gasflame7;
	public static Item gasflame8;*/
	public static Item flame_1;
	public static Item flame_2;
	public static Item flame_3;
	public static Item flame_4;
	public static Item flame_5;
	public static Item flame_6;
	public static Item flame_7;
	public static Item flame_8;
	public static Item flame_9;
	public static Item flame_10;
	public static Item ln2_1;
	public static Item ln2_2;
	public static Item ln2_3;
	public static Item ln2_4;
	public static Item ln2_5;
	public static Item ln2_6;
	public static Item ln2_7;
	public static Item ln2_8;
	public static Item ln2_9;
	public static Item ln2_10;
	public static Item nothing;
	public static Item void_anim;

	public static Item achievement_icon;
	public static Item bob_metalworks;
	public static Item bob_assembly;
	public static Item bob_chemistry;
	public static Item bob_oil;
	public static Item bob_nuclear;

	//public static Item mysteryshovel;
	public static Item memory;

	public static void initializeItem()
	{
		//redstone_sword = new RedstoneSword(ToolMaterial.STONE).setUnlocalizedName("redstone_sword").setCreativeTab(CreativeTabs.tabCombat).setTextureName(RefStrings.MODID + ":redstone_sword");
		//big_sword = new BigSword(ToolMaterial.EMERALD).setUnlocalizedName("big_sword").setCreativeTab(CreativeTabs.tabCombat).setTextureName(RefStrings.MODID + ":big_sword");
		//butter_sword = new BSword(ToolMaterial.WOOD).setUnlocalizedName("butter_sword").setCreativeTab(CreativeTabs.tabCombat).setTextureName(RefStrings.MODID + ":budder_sword");


		test_nuke_igniter = new Item().setUnlocalizedName("test_nuke_igniter").setMaxStackSize(1).setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":test_nuke_igniter");
		test_nuke_propellant = new Item().setUnlocalizedName("test_nuke_propellant").setMaxStackSize(1).setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":test_nuke_propellant");
		test_nuke_tier1_shielding = new Item().setUnlocalizedName("test_nuke_tier1_shielding").setMaxStackSize(1).setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":test_nuke_tier1_shielding");
		test_nuke_tier2_shielding = new Item().setUnlocalizedName("test_nuke_tier2_shielding").setMaxStackSize(1).setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":test_nuke_tier2_shielding");
		test_nuke_tier1_bullet = new Item().setUnlocalizedName("test_nuke_tier1_bullet").setMaxStackSize(1).setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":test_nuke_tier1_bullet");
		test_nuke_tier2_bullet = new Item().setUnlocalizedName("test_nuke_tier2_bullet").setMaxStackSize(1).setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":test_nuke_tier2_bullet");
		test_nuke_tier1_target = new Item().setUnlocalizedName("test_nuke_tier1_target").setMaxStackSize(1).setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":test_nuke_tier1_target");
		test_nuke_tier2_target = new Item().setUnlocalizedName("test_nuke_tier2_target").setMaxStackSize(1).setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":test_nuke_tier2_target");

		ingot_th232 = new Item().setUnlocalizedName("ingot_th232").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":ingot_th232");
		ingot_uranium = new Item().setUnlocalizedName("ingot_uranium").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":ingot_uranium");
		ingot_promethium = new Item().setUnlocalizedName("ingot_promethium").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":ingot_promethium");
		ingot_u233 = new Item().setUnlocalizedName("ingot_u233").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":ingot_u233");
		ingot_u235 = new Item().setUnlocalizedName("ingot_u235").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":ingot_u235");
		ingot_u238 = new Item().setUnlocalizedName("ingot_u238").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":ingot_u238");
		ingot_u238m2 = new ItemUnstable(350, 200).setUnlocalizedName("ingot_u238m2").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":ingot_u238m2");
		ingot_plutonium = new Item().setUnlocalizedName("ingot_plutonium").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":ingot_plutonium");
		ingot_pu238 = new Item().setUnlocalizedName("ingot_pu238").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":ingot_pu238");
		ingot_pu239 = new Item().setUnlocalizedName("ingot_pu239").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":ingot_pu239");
		ingot_pu240 = new Item().setUnlocalizedName("ingot_pu240").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":ingot_pu240");
		ingot_pu241 = new Item().setUnlocalizedName("ingot_pu241").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":ingot_pu241");
		ingot_pu_mix = new Item().setUnlocalizedName("ingot_pu_mix").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":ingot_pu_mix");
		ingot_am241 = new Item().setUnlocalizedName("ingot_am241").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":ingot_am241");
		ingot_am242 = new Item().setUnlocalizedName("ingot_am242").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":ingot_am242");
		ingot_am_mix = new Item().setUnlocalizedName("ingot_am_mix").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":ingot_am_mix");
		ingot_cm242 = new Item().setUnlocalizedName("ingot_cm242").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":ingot_cm242");
		ingot_cm243 = new Item().setUnlocalizedName("ingot_cm243").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":ingot_cm243");
		ingot_cm244 = new Item().setUnlocalizedName("ingot_cm244").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":ingot_cm244");
		ingot_cm245 = new Item().setUnlocalizedName("ingot_cm245").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":ingot_cm245");
		ingot_cm246 = new Item().setUnlocalizedName("ingot_cm246").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":ingot_cm246");
		ingot_cm247 = new Item().setUnlocalizedName("ingot_cm247").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":ingot_cm247");
		ingot_cm_fuel = new Item().setUnlocalizedName("ingot_cm_fuel").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":ingot_cm_fuel");
		ingot_cm_mix = new Item().setUnlocalizedName("ingot_cm_mix").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":ingot_cm_mix");
		ingot_bk247 = new Item().setUnlocalizedName("ingot_bk247").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":ingot_bk247");
		ingot_cf251 = new Item().setUnlocalizedName("ingot_cf251").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID +":ingot_cf251");
		ingot_cf252 = new Item().setUnlocalizedName("ingot_cf252").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":ingot_cf252");
		ingot_cf247 = new Item().setUnlocalizedName("ingot_cf247").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":ingot_cf252");
		ingot_cm248 = new Item().setUnlocalizedName("ingot_cm248").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":ingot_cm247");
		cf_light_fraction = new Item().setUnlocalizedName("cf_light_fraction").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":ingot_cf252");
		rubidium_ingot = new Item().setUnlocalizedName("rubidium_ingot").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":ingot_rubidium");
		ingot_cf249 = new Item().setUnlocalizedName("ingot_cf249").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":ingot_cf252");
		ingot_cf248 = new Item().setUnlocalizedName("ingot_cf248").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":ingot_cf252");
		ingot_yb170 = new Item().setUnlocalizedName("ingot_yb170").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":ingot_titanium");
		gpmix = new Item().setUnlocalizedName("gpmix").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":item_salt");

		turret_light_ammo = new ItemTurretAmmo(ModBlocks.turret_ciws, 1550).setUnlocalizedName("turret_light_ammo").setCreativeTab(MainRegistry.weaponTab).setTextureName(RefStrings.MODID + ":ammo_dgk");


		ingot_es253 = new Item().setUnlocalizedName("ingot_es253").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":ingot_es253");
		ingot_es255 = new Item().setUnlocalizedName("ingot_es255").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":ingot_es255");
		ingot_neptunium = new ItemCustomLore().setUnlocalizedName("ingot_neptunium").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":ingot_neptunium");
		ingot_polonium = new Item().setUnlocalizedName("ingot_polonium").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":ingot_polonium");
		ingot_technetium = new Item().setUnlocalizedName("ingot_technetium").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":ingot_technetium");
		ingot_molybdenum = new Item().setUnlocalizedName("ingot_molybdenum").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":ingot_technetium");
		ingot_co60 = new Item().setUnlocalizedName("ingot_co60").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":ingot_co60");
		ingot_sr90 = new Item().setUnlocalizedName("ingot_sr90").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":ingot_sr90");
		ingot_au198 = new Item().setUnlocalizedName("ingot_au198").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":ingot_au198");
		ingot_pb209 = new Item().setUnlocalizedName("ingot_pb209").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":ingot_pb209");
		ingot_ra226 = new Item().setUnlocalizedName("ingot_ra226").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":ingot_ra226");
		ingot_titanium = new Item().setUnlocalizedName("ingot_titanium").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":ingot_titanium");
		ingot_nickel = new Item().setUnlocalizedName("ingot_nickel").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":ingot_nickel");
		ingot_cobalt = new Item().setUnlocalizedName("ingot_cobalt").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":ingot_cobalt");
		ingot_boron = new Item().setUnlocalizedName("ingot_boron").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":ingot_boron");
		ingot_graphite = new Item().setUnlocalizedName("ingot_graphite").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":ingot_graphite");
		ingot_firebrick = new Item().setUnlocalizedName("ingot_firebrick").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":ingot_firebrick");
		ingot_smore = new ItemFood(4, 1.5F, false).setUnlocalizedName("ingot_smore").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":ingot_smore");
		sulfur = new Item().setUnlocalizedName("sulfur").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":sulfur");
		nitra = new Item().setUnlocalizedName("nitra").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":nitra");
		nitra_small = new Item().setUnlocalizedName("nitra_small").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":nitra_small");
		gypsum = new ItemFertilizer().setUnlocalizedName("gypsum").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":powder_quartz");

		ingot_uranium_fuel = new Item().setUnlocalizedName("ingot_uranium_fuel").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":ingot_uranium_fuel");
		ingot_plutonium_fuel = new Item().setUnlocalizedName("ingot_plutonium_fuel").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":ingot_plutonium_fuel");
		ingot_neptunium_fuel = new Item().setUnlocalizedName("ingot_neptunium_fuel").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":ingot_neptunium_fuel");
		ingot_mox_fuel = new Item().setUnlocalizedName("ingot_mox_fuel").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":ingot_mox_fuel");
		ingot_americium_fuel = new Item().setUnlocalizedName("ingot_americium_fuel").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":ingot_americium_fuel");
		ingot_schrabidium_fuel = new Item().setUnlocalizedName("ingot_schrabidium_fuel").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":ingot_schrabidium_fuel");
		ingot_thorium_fuel = new Item().setUnlocalizedName("ingot_thorium_fuel").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":ingot_thorium_fuel");
		nugget_protactinium = new Item().setUnlocalizedName("nugget_protactinium").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":nugget_australium");
		nugget_uranium_fuel = new Item().setUnlocalizedName("nugget_uranium_fuel").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":nugget_uranium_fuel");
		nugget_thorium_fuel = new Item().setUnlocalizedName("nugget_thorium_fuel").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":nugget_thorium_fuel");
		nugget_plutonium_fuel = new Item().setUnlocalizedName("nugget_plutonium_fuel").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":nugget_plutonium_fuel");
		nugget_neptunium_fuel = new Item().setUnlocalizedName("nugget_neptunium_fuel").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":nugget_neptunium_fuel");
		nugget_mox_fuel = new Item().setUnlocalizedName("nugget_mox_fuel").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":nugget_mox_fuel");
		nugget_americium_fuel = new Item().setUnlocalizedName("nugget_americium_fuel").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":nugget_americium_fuel");
		nugget_schrabidium_fuel = new Item().setUnlocalizedName("nugget_schrabidium_fuel").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":nugget_schrabidium_fuel");
		ingot_advanced_alloy = new Item().setUnlocalizedName("ingot_advanced_alloy").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":ingot_advanced_alloy");
		ingot_tcalloy = new Item().setUnlocalizedName("ingot_tcalloy").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":ingot_tcalloy");
		ingot_cdalloy = new Item().setUnlocalizedName("ingot_cdalloy").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":ingot_cdalloy");
		ingot_bismuth_bronze = new Item().setUnlocalizedName("ingot_bismuth_bronze").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":ingot_bismuth_bronze");
		ingot_arsenic_bronze = new Item().setUnlocalizedName("ingot_arsenic_bronze").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":ingot_arsenic_bronze");
		ingot_bscco = new Item().setUnlocalizedName("ingot_bscco").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":ingot_bscco");

		niter = new Item().setUnlocalizedName("niter").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":salpeter");
		ammonium_chloride = new ItemFertilizer().setUnlocalizedName("ammonium_chloride").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":salpeter");
		sodium_nitrate = new ItemFertilizer().setUnlocalizedName("sodium_nitrate").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":salpeter");
		ingot_copper = new Item().setUnlocalizedName("ingot_copper").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":ingot_copper");
		ingot_red_copper = new Item().setUnlocalizedName("ingot_red_copper").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":ingot_red_copper");
		billet_red_copper = new Item().setUnlocalizedName("billet_red_copper").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":billet_red_copper");
		ingot_tungsten = new Item().setUnlocalizedName("ingot_tungsten").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":ingot_tungsten");
		ingot_aluminium = new Item().setUnlocalizedName("ingot_aluminium").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":ingot_aluminium");
		ingot_tin = new Item().setUnlocalizedName("ingot_tin").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":ingot_aluminium");
		fluorite = new Item().setUnlocalizedName("fluorite").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":fluorite");
		ingot_beryllium = new Item().setUnlocalizedName("ingot_beryllium").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":ingot_beryllium");
		ingot_steel = new Item().setUnlocalizedName("ingot_steel").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":ingot_steel");
		ingot_chromium = new Item().setUnlocalizedName("ingot_chromium").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":ingot_aluminium");
		ingot_vanadium = new Item().setUnlocalizedName("ingot_vanadium").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":ingot_bismuth");

		plate_steel = new Item().setUnlocalizedName("plate_steel").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":plate_steel");
		ingot_stainless = new Item().setUnlocalizedName("ingot_stainless").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":ingot_stainless");
		plate_stainless = new Item().setUnlocalizedName("plate_stainless").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":plate_stainless");
		plate_iron = new Item().setUnlocalizedName("plate_iron").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":plate_iron");
		ingot_lead = new Item().setUnlocalizedName("ingot_lead").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":ingot_lead");
		ingot_silver = new Item().setUnlocalizedName("ingot_silver").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":ingot_aluminium");
		plate_lead = new Item().setUnlocalizedName("plate_lead").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":plate_lead");
		plate_dura_steel = new Item().setUnlocalizedName("plate_dura_steel").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":plate_dura_steel");
		ingot_schraranium = new ItemSchraranium().setUnlocalizedName("ingot_schraranium").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":ingot_schraranium");
		ingot_schrabidium = new ItemCustomLore().setRarity(EnumRarity.rare).setUnlocalizedName("ingot_schrabidium").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":ingot_schrabidium");
		ingot_schrabidate = new ItemCustomLore().setRarity(EnumRarity.rare).setUnlocalizedName("ingot_schrabidate").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":ingot_schrabidate");
		plate_schrabidium = new ItemCustomLore().setRarity(EnumRarity.rare).setUnlocalizedName("plate_schrabidium").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":plate_schrabidium");
		plate_copper = new Item().setUnlocalizedName("plate_copper").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":plate_copper");
		plate_gold = new Item().setUnlocalizedName("plate_gold").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":plate_gold");
		plate_advanced_alloy = new Item().setUnlocalizedName("plate_advanced_alloy").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":plate_advanced_alloy");
		lithium = new Item().setUnlocalizedName("lithium").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":lithium");
		ingot_zirconium = new Item().setUnlocalizedName("ingot_zirconium").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":ingot_zirconium");
		//semtex is not a good idea to eat
		ingot_semtex = new Item().setUnlocalizedName("ingot_semtex").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":ingot_semtex");
		ingot_c4 = new Item().setUnlocalizedName("ingot_c4").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":ingot_c4");
		ingot_phosphorus = new Item().setUnlocalizedName("ingot_phosphorus").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":ingot_phosphorus");
		wire_advanced_alloy = new Item().setUnlocalizedName("wire_advanced_alloy").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":wire_advanced_alloy");
		coil_advanced_alloy = new Item().setUnlocalizedName("coil_advanced_alloy").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":coil_advanced_alloy");
		coil_advanced_torus = new Item().setUnlocalizedName("coil_advanced_torus").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":coil_advanced_torus");
		ingot_magnetized_tungsten = new Item().setUnlocalizedName("ingot_magnetized_tungsten").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":ingot_magnetized_tungsten");
		ingot_combine_steel = new ItemCustomLore().setUnlocalizedName("ingot_combine_steel").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":ingot_combine_steel");
		plate_mixed = new Item().setUnlocalizedName("plate_mixed").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":plate_mixed");
		plate_paa = new ItemCustomLore().setRarity(EnumRarity.uncommon).setUnlocalizedName("plate_paa").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":plate_paa");
		pipes_steel = new Item().setUnlocalizedName("pipes_steel").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":pipes_steel");
		drill_titanium = new Item().setUnlocalizedName("drill_titanium").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":drill_titanium");
		plate_dalekanium = new Item().setUnlocalizedName("plate_dalekanium").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":plate_dalekanium");
		plate_euphemium = new ItemCustomLore().setRarity(EnumRarity.epic).setUnlocalizedName("plate_euphemium").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":plate_euphemium");
		bolt = new ItemAutogen(MaterialShapes.BOLT).oun("boltntm").setUnlocalizedName("bolt").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":bolt");
		bolt_spike = new ItemCustomLore().setUnlocalizedName("bolt_spike").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":bolt_spike");
		plate_polymer = new Item().setUnlocalizedName("plate_polymer").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":plate_polymer");
		plate_kevlar = new Item().setUnlocalizedName("plate_kevlar").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":plate_kevlar");
		plate_dineutronium = new Item().setUnlocalizedName("plate_dineutronium").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":plate_dineutronium");
		plate_desh = new Item().setUnlocalizedName("plate_desh").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":plate_desh");
		plate_bismuth = new ItemCustomLore().setUnlocalizedName("plate_bismuth").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":plate_bismuth");
		ingot_solinium = new Item().setUnlocalizedName("ingot_solinium").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":ingot_solinium");
		nugget_solinium = new Item().setUnlocalizedName("nugget_solinium").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":nugget_solinium");
		photo_panel = new Item().setUnlocalizedName("photo_panel").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":photo_panel");
		sat_base = new Item().setUnlocalizedName("sat_base").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":sat_base");
		thruster_nuclear = new Item().setUnlocalizedName("thruster_nuclear").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":thruster_nuclear");
		safety_fuse = new Item().setUnlocalizedName("safety_fuse").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":safety_fuse");
		part_generic = new ItemGenericPart().setUnlocalizedName("part_generic").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":part_generic");
		item_secret = new ItemEnumMulti(EnumSecretType.class, true, true).setUnlocalizedName("item_secret").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":item_secret");
		chemical_dye = new ItemChemicalDye().setUnlocalizedName("chemical_dye").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":chemical_dye");
		crayon = new ItemCrayon().setUnlocalizedName("crayon").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":crayon");

		undefined = new ItemCustomLore().setUnlocalizedName("undefined").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":undefined");

		billet_uranium = new Item().setUnlocalizedName("billet_uranium").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":billet_uranium");
		billet_u233 = new Item().setUnlocalizedName("billet_u233").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":billet_u233");
		billet_u235 = new Item().setUnlocalizedName("billet_u235").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":billet_u235");
		billet_u238 = new Item().setUnlocalizedName("billet_u238").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":billet_u238");
		billet_th232 = new Item().setUnlocalizedName("billet_th232").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":billet_th232");
		billet_plutonium = new Item().setUnlocalizedName("billet_plutonium").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":billet_plutonium");
		billet_pu238 = new Item().setUnlocalizedName("billet_pu238").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":billet_pu238");
		billet_tm170 = new Item().setUnlocalizedName("billet_tm170").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":billet_plutonium");
		billet_pm = new Item().setUnlocalizedName("billet_pm").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":billet_ra226");
		billet_pu239 = new Item().setUnlocalizedName("billet_pu239").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":billet_pu239");
		billet_pu240 = new Item().setUnlocalizedName("billet_pu240").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":billet_pu240");
		billet_pu241 = new Item().setUnlocalizedName("billet_pu241").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":billet_pu241");
		billet_pu_mix = new Item().setUnlocalizedName("billet_pu_mix").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":billet_pu_mix");
		billet_am241 = new Item().setUnlocalizedName("billet_am241").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":billet_am241");
		billet_am242 = new Item().setUnlocalizedName("billet_am242").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":billet_am242");
		billet_am_mix = new Item().setUnlocalizedName("billet_am_mix").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":billet_am_mix");
		billet_bk247 = new Item().setUnlocalizedName("billet_bk247").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":billet_bk247");
		billet_cm242 = new Item().setUnlocalizedName("billet_cm242").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":billet_cm242");
		billet_cm243 = new Item().setUnlocalizedName("billet_cm243").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":billet_cm243");
		billet_cm244 = new Item().setUnlocalizedName("billet_cm244").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":billet_cm244");
		billet_cm245 = new Item().setUnlocalizedName("billet_cm245").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":billet_cm245");
		billet_cm246 = new Item().setUnlocalizedName("billet_cm246").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":billet_cm246");
		billet_cm247 = new Item().setUnlocalizedName("billet_cm247").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":billet_cm247");
		billet_cm248 = new Item().setUnlocalizedName("billet_cm248").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":billet_cm247");
		billet_cf252 = new Item().setUnlocalizedName("billet_cf252").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":billet_cf252");

		ingot_gunmetal = new Item().setUnlocalizedName("ingot_gunmetal").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":ingot_gunmetal");
		strontium_sulfide = new Item().setUnlocalizedName("strontium_sulfide").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":item_salt");
		scandium_oxide = new Item().setUnlocalizedName("scandium_oxide").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":item_salt");
		yttrium_oxide = new Item().setUnlocalizedName("yttrium_oxide").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":item_salt");
		yttrium_chloride = new Item().setUnlocalizedName("yttrium_chloride").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":item_salt");
		REE_sludge = new Item().setUnlocalizedName("REE_sludge").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":item_sludge");
		scandium_chloride = new Item().setUnlocalizedName("scandium_chloride").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":item_salt");
		scandium_ingot = new Item().setUnlocalizedName("scandium_ingot").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":ingot_scandium");
		ingot_indium = new Item().setUnlocalizedName("ingot_indium").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":ingot_scandium");
		ingot_thallium = new Item().setUnlocalizedName("ingot_thallium").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":ingot_scandium");
		powder_indium = new Item().setUnlocalizedName("powder_indium").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":powder_aluminium");
		powder_thallium = new Item().setUnlocalizedName("powder_thallium").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":powder_aluminium");
		scandium_nugget = new Item().setUnlocalizedName("scandium_nugget").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":nugget_scandium");
		nugget_indium = new Item().setUnlocalizedName("nugget_indium").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":nugget_scandium");
		nugget_thallium = new Item().setUnlocalizedName("nugget_thallium").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":nugget_scandium");
		crystal_zinc = new Item().setUnlocalizedName("crystal_zinc").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":crystal_niter");
		yttrium_ingot = new Item().setUnlocalizedName("yttrium_ingot").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":ingot_yttrium");
		powder_strontium_oxide = new Item().setUnlocalizedName("powder_strontium_oxide").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":item_salt");
		strontium_chloride = new Item().setUnlocalizedName("strontium_chloride").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":item_salt");
		magnesium_ingot = new Item().setUnlocalizedName("magnesium_ingot").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":ingot_magnesium");
		magnesium_nugget = new Item().setUnlocalizedName("magnesium_nugget").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":nugget_magnesium");
		slagingot = new Item().setUnlocalizedName("slagingot").setCreativeTab(MainRegistry.partsTab).setTextureName("minecraft" + ":netherbrick");
		plate_gunmetal = new Item().setUnlocalizedName("plate_gunmetal").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":plate_gunmetal");
		ingot_weaponsteel = new Item().setUnlocalizedName("ingot_weaponsteel").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":ingot_gunsteel");
		plate_weaponsteel = new Item().setUnlocalizedName("plate_weaponsteel").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":plate_gunsteel");
		billet_es253 = new Item().setUnlocalizedName("billet_es253").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":billet_es253");
		billet_cf251 = new Item().setUnlocalizedName("billet_cf251").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID +":billet_cf251");
		billet_cm_mix = new Item().setUnlocalizedName("billet_cm_mix").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":billet_cm_mix");
		billet_cm_fuel = new Item().setUnlocalizedName("billet_cm_fuel").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":billet_cm_fuel");
		billet_neptunium = new Item().setUnlocalizedName("billet_neptunium").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":billet_neptunium");
		billet_polonium = new Item().setUnlocalizedName("billet_polonium").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":billet_polonium");
		billet_technetium = new Item().setUnlocalizedName("billet_technetium").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":billet_technetium");
		billet_cobalt = new Item().setUnlocalizedName("billet_cobalt").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":billet_cobalt");
		billet_co60 = new Item().setUnlocalizedName("billet_co60").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":billet_co60");
		billet_sr90 = new Item().setUnlocalizedName("billet_sr90").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":billet_sr90");
		billet_au198 = new Item().setUnlocalizedName("billet_au198").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":billet_au198");
		billet_pb209 = new Item().setUnlocalizedName("billet_pb209").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":billet_pb209");
		billet_ra226 = new Item().setUnlocalizedName("billet_ra226").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":billet_ra226");
		billet_actinium = new Item().setUnlocalizedName("billet_actinium").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":billet_actinium");
		billet_schrabidium = new ItemCustomLore().setRarity(EnumRarity.rare).setUnlocalizedName("billet_schrabidium").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":billet_schrabidium");
		billet_solinium = new Item().setUnlocalizedName("billet_solinium").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":billet_solinium");
		billet_gh336 = new ItemCustomLore().setRarity(EnumRarity.epic).setUnlocalizedName("billet_gh336").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":billet_gh336");
		billet_australium = new ItemCustomLore().setRarity(EnumRarity.uncommon).setUnlocalizedName("billet_australium").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":billet_australium");
		billet_australium_lesser = new ItemCustomLore().setRarity(EnumRarity.uncommon).setUnlocalizedName("billet_australium_lesser").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":billet_australium_lesser");
		billet_australium_greater = new ItemCustomLore().setRarity(EnumRarity.uncommon).setUnlocalizedName("billet_australium_greater").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":billet_australium_greater");
		billet_uranium_fuel = new Item().setUnlocalizedName("billet_uranium_fuel").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":billet_uranium_fuel");
		billet_thorium_fuel = new Item().setUnlocalizedName("billet_thorium_fuel").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":billet_thorium_fuel");
		billet_plutonium_fuel = new Item().setUnlocalizedName("billet_plutonium_fuel").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":billet_plutonium_fuel");
		billet_neptunium_fuel = new Item().setUnlocalizedName("billet_neptunium_fuel").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":billet_neptunium_fuel");
		billet_mox_fuel = new Item().setUnlocalizedName("billet_mox_fuel").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":billet_mox_fuel");
		billet_americium_fuel = new Item().setUnlocalizedName("billet_americium_fuel").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":billet_americium_fuel");
		billet_les = new Item().setUnlocalizedName("billet_les").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":billet_les");
		billet_schrabidium_fuel = new Item().setUnlocalizedName("billet_schrabidium_fuel").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":billet_schrabidium_fuel");
		billet_hes = new Item().setUnlocalizedName("billet_hes").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":billet_hes");
		billet_po210be = new Item().setUnlocalizedName("billet_po210be").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":billet_po210be");
		billet_ra226be = new Item().setUnlocalizedName("billet_ra226be").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":billet_ra226be");
		billet_pu238be = new Item().setUnlocalizedName("billet_pu238be").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":billet_pu238be");
		billet_beryllium = new Item().setUnlocalizedName("billet_beryllium").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":billet_beryllium");
		billet_bismuth = new Item().setUnlocalizedName("billet_bismuth").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":billet_bismuth");
		billet_zirconium = new Item().setUnlocalizedName("billet_zirconium").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":billet_zirconium");
		billet_yharonite = new Item().setUnlocalizedName("billet_yharonite").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":billet_yharonite");
		billet_balefire_gold = new ItemCustomLore().setRarity(EnumRarity.uncommon).setUnlocalizedName("billet_balefire_gold").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":billet_balefire_gold");
		billet_flashlead = new ItemCustomLore().setRarity(EnumRarity.uncommon).setUnlocalizedName("billet_flashlead").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":billet_flashlead");
		billet_zfb_bismuth = new Item().setUnlocalizedName("billet_zfb_bismuth").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":billet_zfb_bismuth");
		billet_zfb_pu241 = new Item().setUnlocalizedName("billet_zfb_pu241").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":billet_zfb_pu241");
		billet_zfb_am_mix = new Item().setUnlocalizedName("billet_zfb_am_mix").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":billet_zfb_am_mix");
		billet_nuclear_waste = new Item().setUnlocalizedName("billet_nuclear_waste").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":billet_nuclear_waste");

		ball_resin = new ItemCustomLore().setUnlocalizedName("ball_resin").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":ball_resin");
		ingot_dura_steel = new ItemCustomLore().setUnlocalizedName("ingot_dura_steel").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":ingot_dura_steel");
		ingot_polymer = new ItemCustomLore().setUnlocalizedName("ingot_polymer").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":ingot_polymer");
		ingot_bakelite = new ItemCustomLore().setUnlocalizedName("ingot_bakelite").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":ingot_bakelite");
		ingot_biorubber = new ItemCustomLore().setUnlocalizedName("ingot_biorubber").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":ingot_biorubber");
		ingot_rubber = new ItemCustomLore().setUnlocalizedName("ingot_rubber").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":ingot_rubber");
		//ingot_pet = new ItemCustomLore().setUnlocalizedName("ingot_pet").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":ingot_pet");
		ingot_pc = new ItemCustomLore().setUnlocalizedName("ingot_pc").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":ingot_pc");
		ingot_pvc = new ItemCustomLore().setUnlocalizedName("ingot_pvc").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":ingot_pvc");
		ingot_desh = new ItemCustomLore().setUnlocalizedName("ingot_desh").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":ingot_desh");
		nugget_desh = new ItemCustomLore().setUnlocalizedName("nugget_desh").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":nugget_desh");
		ingot_dineutronium = new ItemCustomLore().setUnlocalizedName("ingot_dineutronium").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":ingot_dineutronium");
		nugget_dineutronium = new ItemCustomLore().setUnlocalizedName("nugget_dineutronium").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":nugget_dineutronium");
		powder_dineutronium = new ItemCustomLore().setUnlocalizedName("powder_dineutronium").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":powder_dineutronium");
		ingot_starmetal = new ItemStarmetal().setUnlocalizedName("ingot_starmetal").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":ingot_starmetal");
		ingot_saturnite = new ItemCustomLore().setRarity(EnumRarity.rare).setUnlocalizedName("ingot_saturnite").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":ingot_saturnite");
		plate_saturnite = new ItemCustomLore().setRarity(EnumRarity.rare).setUnlocalizedName("plate_saturnite").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":plate_saturnite");
		ingot_ferrouranium = new ItemCustomLore().setUnlocalizedName("ingot_ferrouranium").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":ingot_ferrouranium");
		ingot_fiberglass = new ItemCustomLore().setUnlocalizedName("ingot_fiberglass").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":ingot_fiberglass");
		ingot_asbestos = new ItemCustomLore().setUnlocalizedName("ingot_asbestos").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":ingot_asbestos");
		powder_asbestos = new ItemCustomLore().setUnlocalizedName("powder_asbestos").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":powder_asbestos");
		gem_ruby = new ItemCustomLore().setUnlocalizedName("gem_ruby").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":gem_ruby");
		gem_sapphire = new ItemCustomLore().setUnlocalizedName("gem_sapphire").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":gem_sapphire");
		ingot_electronium = new ItemCustomLore().setUnlocalizedName("ingot_electronium").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":ingot_electronium");
		ingot_gwenium = new ItemCustomLore().setUnlocalizedName("ingot_gwenium").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":ingot_gwenium");
		nugget_zirconium = new ItemCustomLore().setUnlocalizedName("nugget_zirconium").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":nugget_zirconium");
		nugget_nickel = new ItemCustomLore().setUnlocalizedName("nugget_nickel").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":nugget_nickel");
		nugget_mercury = new Item().setUnlocalizedName("nugget_mercury_tiny").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":nugget_mercury_tiny");
		nugget_ruthenium = new Item().setUnlocalizedName("nugget_ruthenium").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":nugget_lead");
		ingot_mercury = new ItemCustomLore().setUnlocalizedName("nugget_mercury").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":nugget_mercury");
		bottle_mercury = new ItemCustomLore().setUnlocalizedName("bottle_mercury").setContainerItem(Items.glass_bottle).setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":bottle_mercury");
		ingot_calcium = new Item().setUnlocalizedName("ingot_calcium").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":ingot_calcium");
		powder_calcium = new Item().setUnlocalizedName("powder_calcium").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":powder_calcium");
		ingot_cadmium = new Item().setUnlocalizedName("ingot_cadmium").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":ingot_cadmium");
		powder_cadmium = new Item().setUnlocalizedName("powder_cadmium").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":powder_cadmium");
		powder_bismuth = new Item().setUnlocalizedName("powder_bismuth").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":powder_bismuth");
		powder_potash = new Item().setUnlocalizedName("powder_potash").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":powder_potash");
		powder_pollucite = new Item().setUnlocalizedName("powder_pollucite").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":item_salt");
		//ingot_mud = new Item().setUnlocalizedName("ingot_mud").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":ingot_mud");
		ingot_cft = new Item().setUnlocalizedName("ingot_cft").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":ingot_cft");

		powder_osmium = new Item().setUnlocalizedName("powder_osmium").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":powder_osmiridium");

		ore_byproduct = new ItemByproduct().setUnlocalizedName("ore_byproduct").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":byproduct");

		//mineral_product = new ItemProduct().setUnlocalizedName("mineral_product").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":clump_base");

		crystal_mineral = new ItemCustomLore().setUnlocalizedName("crystal_mineral").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":crystal_mineral");
		crystal_cleaned = new ItemCustomLore().setUnlocalizedName("crystal_cleaned").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":crystal_mineralcf");
		mineral_dust = new ItemCustomLore().setUnlocalizedName("mineral_dust").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":powder_mineral");
		mineral_fragment = new ItemMineralOre().setUnlocalizedName("mineral_fragment").setCreativeTab(MainRegistry.partsTab);



		ore_bedrock = new ItemBedrockOre().setUnlocalizedName("ore_bedrock").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":ore_bedrock");
		ore_centrifuged = new ItemBedrockOre().setUnlocalizedName("ore_centrifuged").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":ore_centrifuged");
		ore_cleaned = new ItemBedrockOre().setUnlocalizedName("ore_cleaned").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":ore_cleaned");
		ore_separated = new ItemBedrockOre().setUnlocalizedName("ore_separated").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":ore_separated");
		ore_purified = new ItemBedrockOre().setUnlocalizedName("ore_purified").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":ore_purified");
		ore_nitrated = new ItemBedrockOre().setUnlocalizedName("ore_nitrated").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":ore_nitrated");
		ore_nitrocrystalline = new ItemBedrockOre().setUnlocalizedName("ore_nitrocrystalline").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":ore_nitrocrystalline");
		ore_deepcleaned = new ItemBedrockOre().setUnlocalizedName("ore_deepcleaned").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":ore_deepcleaned");
		ore_seared = new ItemBedrockOre().setUnlocalizedName("ore_seared").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":ore_seared");
		//ore_radcleaned = new ItemBedrockOre().setUnlocalizedName("ore_radcleaned").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":ore_radcleaned");
		ore_enriched = new ItemBedrockOre().setUnlocalizedName("ore_enriched").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":ore_enriched");
		bedrock_ore_base = new ItemBedrockOreBase().setUnlocalizedName("bedrock_ore_base").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":bedrock_ore_new");
		bedrock_ore = new ItemBedrockOreNew().setUnlocalizedName("bedrock_ore").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":bedrock_ore_new");
		bedrock_ore_fragment = new ItemAutogen(MaterialShapes.FRAGMENT).aot(Mats.MAT_BISMUTH, "bedrock_ore_fragment_bismuth").setUnlocalizedName("bedrock_ore_fragment").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":bedrock_ore_fragment");

		ingot_lanthanium = new ItemCustomLore().setUnlocalizedName("ingot_lanthanium").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":ingot_lanthanium");
		nugget_lanthanium = new Item().setUnlocalizedName("nugget_lanthanium").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":nugget_lanthanium");
		ingot_actinium = new ItemCustomLore().setUnlocalizedName("ingot_actinium").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":ingot_actinium");
		//TODO add half lifes
		francium_ingot = new ItemCustomLore().setUnlocalizedName("ingot_francium").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":ingot_actinium");

		//ingot_meteorite = new ItemHot(200).setUnlocalizedName("ingot_meteorite").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":ingot_meteorite");
		//ingot_meteorite_forged = new ItemHot(200).setUnlocalizedName("ingot_meteorite_forged").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":ingot_meteorite_forged");
		//blade_meteorite = new ItemHot(200).setUnlocalizedName("blade_meteorite").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":blade_meteorite");
		ingot_steel_dusted = new ItemHotDusted(200).setUnlocalizedName("ingot_steel_dusted").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":ingot_steel_dusted");
		ingot_chainsteel = new ItemHot(100).setUnlocalizedName("ingot_chainsteel").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":ingot_chainsteel");

		ingot_cesium = new Item().setUnlocalizedName("ingot_cesium").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":ingot_cesium");

		plate_armor_titanium = new Item().setUnlocalizedName("plate_armor_titanium").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":plate_armor_titanium");
		plate_armor_ajr = new Item().setUnlocalizedName("plate_armor_ajr").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":plate_armor_ajr");
		plate_armor_hev = new Item().setUnlocalizedName("plate_armor_hev").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":plate_armor_hev");
		plate_armor_lunar = new Item().setUnlocalizedName("plate_armor_lunar").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":plate_armor_lunar");
		plate_armor_fau = new Item().setUnlocalizedName("plate_armor_fau").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":plate_armor_fau");
		plate_armor_dnt = new Item().setUnlocalizedName("plate_armor_dnt").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":plate_armor_dnt");

		oil_tar = new ItemEnumMulti(EnumTarType.class, true, true).setUnlocalizedName("oil_tar").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":oil_tar");
		solid_fuel = new Item().setUnlocalizedName("solid_fuel").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":solid_fuel");
		solid_fuel_presto = new Item().setUnlocalizedName("solid_fuel_presto").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":solid_fuel_presto");
		solid_fuel_presto_triplet = new Item().setUnlocalizedName("solid_fuel_presto_triplet").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":solid_fuel_presto_triplet");
		solid_fuel_bf = new Item().setUnlocalizedName("solid_fuel_bf").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":solid_fuel_bf");
		solid_fuel_presto_bf = new Item().setUnlocalizedName("solid_fuel_presto_bf").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":solid_fuel_presto_bf");
		solid_fuel_presto_triplet_bf = new Item().setUnlocalizedName("solid_fuel_presto_triplet_bf").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":solid_fuel_presto_triplet_bf");
		rocket_fuel = new Item().setUnlocalizedName("rocket_fuel").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":rocket_fuel");
		coke = new ItemEnumMulti(EnumCokeType.class, true, true).setUnlocalizedName("coke").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":coke");
		powder_coke = new ItemEnumMulti(EnumCokeType.class, true, true).setUnlocalizedName("powder_coke").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":powder_coke");
		powder_celestite = new Item().setUnlocalizedName("powder_celestite").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":powder_celestite");
		lignite = new Item().setUnlocalizedName("lignite").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":lignite");
		briquette = new ItemEnumMulti(EnumBriquetteType.class, true, true).setUnlocalizedName("briquette").setCreativeTab(MainRegistry.partsTab);
		powder_lignite = new Item().setUnlocalizedName("powder_lignite").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":powder_lignite");
		coal_infernal = new Item().setUnlocalizedName("coal_infernal").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":coal_infernal");
		cinnebar = new Item().setUnlocalizedName("cinnebar").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":cinnebar");
		//woodemium_briquette = new Item().setUnlocalizedName("woodemium_briquette").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":briquette_woodemium");
		butter = new Item().setUnlocalizedName("butter").setCreativeTab(MainRegistry.consumableTab).setTextureName(RefStrings.MODID + ":ingot_butter");
		s_cream	= new ItemLemon(2, 1.8F, false).setUnlocalizedName("s_cream").setCreativeTab(MainRegistry.consumableTab).setTextureName(RefStrings.MODID + ":ice_cream_s");

		powder_ash = new ItemEnumMulti(EnumAshType.class, true, true).setUnlocalizedName("powder_ash").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":powder_ash");
		powder_limestone = new Item().setUnlocalizedName("powder_limestone").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":powder_limestone");
		powder_cement = new Item().setUnlocalizedName("powder_cement").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":powder_cement");
		squirrel = new ItemLemon(1, 2F, false).setUnlocalizedName("squirrel").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":squirreljak");
		cooked_squirrel = new ItemLemon(4, 2F, false).setUnlocalizedName("cooked_squirrel").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":squirreljakcooked");

		potassium_iodide_powder = new Item().setUnlocalizedName("potassium_iodide_powder").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":powder_aluminium");
		potassium_powder = new Item().setUnlocalizedName("potassium_powder").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":powder_aluminium");
		//visually the same but uhhhhhh

		ingot_gh336 = new ItemCustomLore().setRarity(EnumRarity.epic).setUnlocalizedName("ingot_gh336").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":ingot_gh336");
		nugget_gh336 = new ItemCustomLore().setRarity(EnumRarity.epic).setUnlocalizedName("nugget_gh336").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":nugget_gh336");
		ingot_cn989 = new ItemCustomLore().setRarity(EnumRarity.epic).setUnlocalizedName("ingot_cn989").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":ingot_cn989");
		nugget_cn989 = new ItemCustomLore().setRarity(EnumRarity.epic).setUnlocalizedName("nugget_cn989").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":nugget_cn989");
		billet_cn989 = new ItemCustomLore().setRarity(EnumRarity.epic).setUnlocalizedName("billet_cn989").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":billet_cn989");
		plate_cn989 = new ItemCustomLore().setRarity(EnumRarity.epic).setUnlocalizedName("plate_cn989").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":plate_cn989");
		powder_cn989 = new ItemCustomLore().setRarity(EnumRarity.epic).setUnlocalizedName("powder_cn989").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":powder_cn989");

		ingot_australium = new ItemCustomLore().setRarity(EnumRarity.uncommon).setUnlocalizedName("ingot_australium").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":ingot_australium");
		ingot_weidanium = new ItemCustomLore().setRarity(EnumRarity.uncommon).setUnlocalizedName("ingot_weidanium").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":ingot_weidanium");
		ingot_reiium = new ItemCustomLore().setUnlocalizedName("ingot_reiium").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":ingot_reiium");
		ingot_unobtainium = new ItemCustomLore().setRarity(EnumRarity.uncommon).setUnlocalizedName("ingot_unobtainium").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":ingot_unobtainium");
		ingot_daffergon = new ItemCustomLore().setRarity(EnumRarity.uncommon).setUnlocalizedName("ingot_daffergon").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":ingot_daffergon");
		ingot_verticium = new ItemCustomLore().setRarity(EnumRarity.uncommon).setUnlocalizedName("ingot_verticium").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":ingot_verticium");
		nugget_australium = new ItemCustomLore().setRarity(EnumRarity.uncommon).setUnlocalizedName("nugget_australium").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":nugget_australium");
		nugget_australium_lesser = new ItemCustomLore().setRarity(EnumRarity.uncommon).setUnlocalizedName("nugget_australium_lesser").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":nugget_australium_lesser");
		nugget_australium_greater = new ItemCustomLore().setRarity(EnumRarity.uncommon).setUnlocalizedName("nugget_australium_greater").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":nugget_australium_greater");
		nugget_weidanium = new ItemCustomLore().setRarity(EnumRarity.uncommon).setUnlocalizedName("nugget_weidanium").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":nugget_weidanium");
		nugget_reiium = new ItemCustomLore().setRarity(EnumRarity.uncommon).setUnlocalizedName("nugget_reiium").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":nugget_reiium");
		nugget_unobtainium = new ItemCustomLore().setRarity(EnumRarity.uncommon).setUnlocalizedName("nugget_unobtainium").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":nugget_unobtainium");
		nugget_daffergon = new ItemCustomLore().setRarity(EnumRarity.uncommon).setUnlocalizedName("nugget_daffergon").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":nugget_daffergon");
		nugget_verticium = new ItemCustomLore().setRarity(EnumRarity.uncommon).setUnlocalizedName("nugget_verticium").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":nugget_verticium");

		nugget_th232 = new Item().setUnlocalizedName("nugget_th232").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":nugget_th232");
		nugget_uranium = new Item().setUnlocalizedName("nugget_uranium").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":nugget_uranium");
		nugget_promethium = new Item().setUnlocalizedName("nugget_promethium").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":nugget_promethium");
		nugget_u233 = new Item().setUnlocalizedName("nugget_u233").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":nugget_u233");
		nugget_u235 = new Item().setUnlocalizedName("nugget_u235").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":nugget_u235");
		nugget_u238 = new Item().setUnlocalizedName("nugget_u238").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":nugget_u238");
		nugget_plutonium = new Item().setUnlocalizedName("nugget_plutonium").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":nugget_plutonium");
		nugget_pu238 = new Item().setUnlocalizedName("nugget_pu238").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":nugget_pu238");
		nugget_tm170 = new Item().setUnlocalizedName("nugget_tm170").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":nugget_plutonium");
		nugget_pu239 = new Item().setUnlocalizedName("nugget_pu239").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":nugget_pu239");
		nugget_pu240 = new Item().setUnlocalizedName("nugget_pu240").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":nugget_pu240");
		nugget_pu241 = new Item().setUnlocalizedName("nugget_pu241").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":nugget_pu241");
		nugget_pu_mix = new Item().setUnlocalizedName("nugget_pu_mix").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":nugget_pu_mix");
		nugget_am241 = new Item().setUnlocalizedName("nugget_am241").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":nugget_am241");
		nugget_am242 = new Item().setUnlocalizedName("nugget_am242").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":nugget_am242");
		nugget_am_mix = new Item().setUnlocalizedName("nugget_am_mix").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":nugget_am_mix");
		nugget_bk247 = new Item().setUnlocalizedName("nugget_bk247").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":nugget_bk247");
		nugget_cm242 = new Item().setUnlocalizedName("nugget_cm242").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":nugget_cm242");
		nugget_cm243 = new Item().setUnlocalizedName("nugget_cm243").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":nugget_cm243");
		nugget_cm244 = new Item().setUnlocalizedName("nugget_cm244").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":nugget_cm244");
		nugget_cm245 = new Item().setUnlocalizedName("nugget_cm245").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":nugget_cm245");
		nugget_cm246 = new Item().setUnlocalizedName("nugget_cm246").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":nugget_cm246");
		nugget_cm247 = new Item().setUnlocalizedName("nugget_cm247").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":nugget_cm247");
		nugget_cm248 = new Item().setUnlocalizedName("nugget_cm248").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":nugget_cm247");
		nugget_cf251 = new Item().setUnlocalizedName("nugget_cf251").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID +":nugget_cf251");
		nugget_cf252 = new Item().setUnlocalizedName("nugget_cf252").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":nugget_cf252");
		nugget_cf249 = new Item().setUnlocalizedName("nugget_cf249").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":nugget_cf252");
		nugget_cf248 = new Item().setUnlocalizedName("nugget_cf248").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":nugget_cf252");
		nugget_cf250 = new Item().setUnlocalizedName("nugget_cf250").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":nugget_cf252");
		nugget_cm_fuel = new Item().setUnlocalizedName("nugget_cm_fuel").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":nugget_cm_fuel");
		nugget_es253 = new Item().setUnlocalizedName("nugget_es253").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":nugget_es253");
		nugget_cm_mix = new Item().setUnlocalizedName("nugget_cm_mix").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":nugget_cm_mix");
		nugget_neptunium = new Item().setUnlocalizedName("nugget_neptunium").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":nugget_neptunium");
		nugget_polonium = new Item().setUnlocalizedName("nugget_polonium").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":nugget_polonium");
		nugget_technetium = new Item().setUnlocalizedName("nugget_technetium").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":nugget_technetium");
		nugget_cobalt = new Item().setUnlocalizedName("nugget_cobalt").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":nugget_cobalt");
		nugget_co60 = new Item().setUnlocalizedName("nugget_co60").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":nugget_co60");
		nugget_sr90 = new Item().setUnlocalizedName("nugget_sr90").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":nugget_sr90");
		nugget_au198 = new Item().setUnlocalizedName("nugget_au198").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":nugget_au198");
		nugget_pb209 = new Item().setUnlocalizedName("nugget_pb209").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":nugget_pb209");
		nugget_ra226 = new Item().setUnlocalizedName("nugget_ra226").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":nugget_ra226");
		nugget_actinium = new Item().setUnlocalizedName("nugget_actinium").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":nugget_actinium");
		plate_titanium = new Item().setUnlocalizedName("plate_titanium").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":plate_titanium");
		plate_nickel = new Item().setUnlocalizedName("plate_nickel").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":plate_nickel");
		plate_aluminium = new Item().setUnlocalizedName("plate_aluminium").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":plate_aluminium");
		wire_red_copper = new Item().setUnlocalizedName("wire_red_copper").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":wire_red_copper");
		wire_zinc = new Item().setUnlocalizedName("wire_zinc").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":wire_zinc");
		wire_tungsten = new ItemCustomLore().setUnlocalizedName("wire_tungsten").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":wire_tungsten");
		neutron_reflector = new Item().setUnlocalizedName("neutron_reflector").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":neutron_reflector");
		nugget_lead = new Item().setUnlocalizedName("nugget_lead").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":nugget_lead");
		ingot_bismuth = new ItemCustomLore().setUnlocalizedName("ingot_bismuth").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":ingot_bismuth");
		nugget_bismuth = new Item().setUnlocalizedName("nugget_bismuth").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":nugget_bismuth");
		nugget_silver = new Item().setUnlocalizedName("nugget_silver").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":nugget_tantalium");
		ingot_arsenic = new ItemCustomLore().setUnlocalizedName("ingot_arsenic").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":ingot_arsenic");
		nugget_arsenic = new Item().setUnlocalizedName("nugget_arsenic").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":nugget_arsenic");
		nugget_antimony = new Item().setUnlocalizedName("nugget_antimony").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":nugget_lead");
		ingot_tantalium = new ItemCustomLore().setUnlocalizedName("ingot_tantalium").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":ingot_tantalium");
		nugget_tantalium = new ItemCustomLore().setUnlocalizedName("nugget_tantalium").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":nugget_tantalium");
		ingot_silicon = new Item().setUnlocalizedName("ingot_silicon").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":ingot_silicon");
		billet_silicon = new Item().setUnlocalizedName("billet_silicon").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":billet_silicon");
		nugget_silicon = new Item().setUnlocalizedName("nugget_silicon").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":nugget_silicon");
		calcined_dolomite = new Item().setUnlocalizedName("calcined_dolomite").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":calcined_dolomite");
		ingot_niobium = new Item().setUnlocalizedName("ingot_niobium").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":ingot_niobium");
		ingot_gallium = new Item().setUnlocalizedName("ingot_gallium").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":ingot_gallium");
		nugget_gallium = new Item().setUnlocalizedName("nugget_gallium").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":nugget_gallium");
		powder_gallium = new Item().setUnlocalizedName("powder_gallium").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":powder_gallium");
		powder_germanium = new Item().setUnlocalizedName("powder_germanium").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":powder_lead");
		powder_gallium_tiny= new Item().setUnlocalizedName("powder_gallium_tiny").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":powder_gallium_tiny");
		ingot_tt = new ItemCustomLore().setRarity(EnumRarity.uncommon).setUnlocalizedName("ingot_tt").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":ingot_techtactium");
		ingot_ttas = new ItemCustomLore().setRarity(EnumRarity.rare).setUnlocalizedName("ingot_ttas").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":ingot_techtactium_as");
		ingot_gaas = new ItemCustomLore().setRarity(EnumRarity.rare).setUnlocalizedName("ingot_gaas").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":ingot_gaas1");
		silver_gallium_arsenide = new ItemCustomLore().setRarity(EnumRarity.epic).setUnlocalizedName("silver_gallium_arsenide").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":ingot_gaas1");
		nugget_gaas = new Item().setUnlocalizedName("nugget_gaas").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":nugget_gaas");
		billet_gaas = new Item().setUnlocalizedName("billet_gaas").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":billet_gaas1");
		nugget_zinc = new Item().setUnlocalizedName("nugget_zinc").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":nugget_zinc");
		ingot_zinc = new Item().setUnlocalizedName("ingot_zinc").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":ingot_zinc");
		powder_zinc = new Item().setUnlocalizedName("powder_zinc").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":powder_zinc");
		nugget_platnium = new Item().setUnlocalizedName("nugget_platnium").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":nugget_platnium");
		ingot_platnium = new Item().setUnlocalizedName("ingot_platnium").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":ingot_platnium");
		ingot_germanium = new Item().setUnlocalizedName("ingot_germanium").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":ingot_graphite");
		nugget_germanium = new Item().setUnlocalizedName("nugget_germanium").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":nugget_lead");
		powder_platnium = new Item().setUnlocalizedName("powder_platnium").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":powder_platnium");

		//PGM
		powder_rhodium = new Item().setUnlocalizedName("powder_rhodium").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":powder_platnium");
		powder_rhodium_solution = new Item().setUnlocalizedName("powder_rhodium_solution").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":powder_coal");
		ingot_rhodium = new Item().setUnlocalizedName("ingot_rhodium").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":ingot_platnium");
		powder_palladium = new Item().setUnlocalizedName("powder_palladium").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":powder_red_copper");
		powder_samarium = new Item().setUnlocalizedName("powder_samarium").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":powder_samarium");

		nugget_niobium = new Item().setUnlocalizedName("nugget_niobium").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":nugget_niobium");
		nugget_vanadium = new Item().setUnlocalizedName("nugget_vanadium").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":nugget_bismuth");
		ingot_osmiridium = new ItemCustomLore().setRarity(EnumRarity.rare).setUnlocalizedName("ingot_osmiridium").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":ingot_osmiridium");
		nugget_osmiridium = new ItemCustomLore().setRarity(EnumRarity.rare).setUnlocalizedName("nugget_osmiridium").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":nugget_osmiridium");
		ingot_hafnium = new ItemCustomLore().setUnlocalizedName("ingot_hafnium").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":ingot_hafnium");
		nugget_hafnium = new ItemCustomLore().setUnlocalizedName("nugget_hafnium").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":nugget_hafnium");
		ingot_iridium = new ItemCustomLore().setRarity(EnumRarity.rare).setUnlocalizedName("ingot_iridium").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":ingot_iridium");
		powder_iridium = new ItemCustomLore().setRarity(EnumRarity.rare).setUnlocalizedName("powder_iridium").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":powder_aluminium");
		ruthenium_residue = new ItemCustomLore().setRarity(EnumRarity.rare).setUnlocalizedName("ruthenium_residue").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":powder_red_copper");
		nugget_schrabidium = new ItemCustomLore().setRarity(EnumRarity.rare).setUnlocalizedName("nugget_schrabidium").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":nugget_schrabidium");
		nugget_beryllium = new Item().setUnlocalizedName("nugget_beryllium").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":nugget_beryllium");
		hazmat_cloth = new Item().setUnlocalizedName("hazmat_cloth").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":hazmat_cloth");
		hazmat_cloth_red = new Item().setUnlocalizedName("hazmat_cloth_red").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":hazmat_cloth_red");
		hazmat_cloth_grey = new Item().setUnlocalizedName("hazmat_cloth_grey").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":hazmat_cloth_grey");
		asbestos_cloth = new Item().setUnlocalizedName("asbestos_cloth").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":asbestos_cloth");
		rag = new ItemRag().setUnlocalizedName("rag").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":rag");
		rag_damp = new Item().setUnlocalizedName("rag_damp").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":rag_damp");
		rag_piss = new Item().setUnlocalizedName("rag_piss").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":rag_piss");
		rag_blood = new Item().setUnlocalizedName("rag_blood").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":rag_blood");
		filter_coal = new Item().setUnlocalizedName("filter_coal").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":filter_coal");
		ingot_hes = new Item().setUnlocalizedName("ingot_hes").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":ingot_hes");
		ingot_les = new Item().setUnlocalizedName("ingot_les").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":ingot_les");
		nugget_hes = new Item().setUnlocalizedName("nugget_hes").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":nugget_hes");
		nugget_les = new Item().setUnlocalizedName("nugget_les").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":nugget_les");
		plate_combine_steel = new Item().setUnlocalizedName("plate_combine_steel").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":plate_combine_steel");
		ingot_palladium = new ItemCustomLore().setUnlocalizedName("ingot_palladium").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":ingot_palladium");
		ingot_samarium = new ItemCustomLore().setUnlocalizedName("ingot_samarium").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":ingot_samarium");
		ingot_smco = new ItemCustomLore().setUnlocalizedName("ingot_smco").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":ingot_samarium");
		ingot_europium = new ItemCustomLore().setUnlocalizedName("ingot_europium").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":ingot_samarium");
		ingot_terbium = new ItemCustomLore().setUnlocalizedName("ingot_terbium").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":ingot_samarium");
		ingot_thulium = new ItemCustomLore().setUnlocalizedName("ingot_thulium").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":ingot_plutonium");
		ingot_thulium170 = new ItemCustomLore().setUnlocalizedName("ingot_thulium170").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":ingot_plutonium");
		nugget_europium = new ItemCustomLore().setUnlocalizedName("nugget_europium").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":nugget_technetium");
		scuttertail = new Item().setUnlocalizedName("scuttertail").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":scuttertail");
		saltleaf = new Item().setUnlocalizedName("saltleaf").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":salt_leaf");

		crystal_coal = new Item().setUnlocalizedName("crystal_coal").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":crystal_coal");
		crystal_iron = new Item().setUnlocalizedName("crystal_iron").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":crystal_iron");
		crystal_gold = new Item().setUnlocalizedName("crystal_gold").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":crystal_gold");
		crystal_redstone = new Item().setUnlocalizedName("crystal_redstone").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":crystal_redstone");
		crystal_lapis = new Item().setUnlocalizedName("crystal_lapis").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":crystal_lapis");
		crystal_diamond = new Item().setUnlocalizedName("crystal_diamond").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":crystal_diamond");
		crystal_uranium = new Item().setUnlocalizedName("crystal_uranium").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":crystal_uranium");
		crystal_thorium = new Item().setUnlocalizedName("crystal_thorium").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":crystal_thorium");
		crystal_plutonium = new Item().setUnlocalizedName("crystal_plutonium").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":crystal_plutonium");
		crystal_titanium = new Item().setUnlocalizedName("crystal_titanium").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":crystal_titanium");
		crystal_sulfur = new Item().setUnlocalizedName("crystal_sulfur").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":crystal_sulfur");
		crystal_niter = new Item().setUnlocalizedName("crystal_niter").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":crystal_niter");
		crystal_copper = new Item().setUnlocalizedName("crystal_copper").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":crystal_copper");
		crystal_tungsten = new Item().setUnlocalizedName("crystal_tungsten").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":crystal_tungsten");
		crystal_aluminium = new Item().setUnlocalizedName("crystal_aluminium").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":crystal_aluminium");
		crystal_fluorite = new Item().setUnlocalizedName("crystal_fluorite").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":crystal_fluorite");
		crystal_beryllium = new Item().setUnlocalizedName("crystal_beryllium").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":crystal_beryllium");
		crystal_lead = new Item().setUnlocalizedName("crystal_lead").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":crystal_lead");
		crystal_schraranium = new ItemCustomLore().setRarity(EnumRarity.rare).setUnlocalizedName("crystal_schraranium").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":crystal_schraranium");
		crystal_schrabidium = new ItemCustomLore().setRarity(EnumRarity.rare).setUnlocalizedName("crystal_schrabidium").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":crystal_schrabidium");
		crystal_rare = new Item().setUnlocalizedName("crystal_rare").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":crystal_rare");
		crystal_phosphorus = new Item().setUnlocalizedName("crystal_phosphorus").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":crystal_phosphorus");
		crystal_lithium = new Item().setUnlocalizedName("crystal_lithium").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":crystal_lithium");
		crystal_cobalt = new Item().setUnlocalizedName("crystal_cobalt").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":crystal_cobalt");
		crystal_starmetal = new Item().setUnlocalizedName("crystal_starmetal").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":crystal_starmetal");
		crystal_cinnebar = new Item().setUnlocalizedName("crystal_cinnebar").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":crystal_cinnebar");
		crystal_trixite = new Item().setUnlocalizedName("crystal_trixite").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":crystal_trixite");
		crystal_osmiridium = new Item().setUnlocalizedName("crystal_osmiridium").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":crystal_osmiridium");
		gem_sodalite = new ItemCustomLore().setUnlocalizedName("gem_sodalite").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":gem_sodalite");
		gem_tantalium = new ItemCustomLore().setUnlocalizedName("gem_tantalium").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":gem_tantalium");
		gem_volcanic = new ItemCustomLore().setRarity(EnumRarity.uncommon).setUnlocalizedName("gem_volcanic").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":gem_volcanic");
		gem_rad = new ItemCustomLore().setRarity(EnumRarity.uncommon).setUnlocalizedName("gem_rad").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":gem_rad");
		gem_alexandrite = new ItemAlexandrite().setUnlocalizedName("gem_alexandrite").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":gem_alexandrite");
		titanium_sapphire_crystal = new ItemCustomLore().setUnlocalizedName("titanium_sapphire_crystal").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":crystal_titanium");
		cerium_crystal = new ItemCustomLore().setUnlocalizedName("cerium_crystal").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":crystal_gold");
		calcium_fluoride_crystal = new ItemCustomLore().setUnlocalizedName("calcium_fluoride_crystal").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":crystal_fluorite");
		KTP_crystal = new Item().setUnlocalizedName("KTP_crystal").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":crystal_titanium");
		lithium_niobate_crystal = new Item().setUnlocalizedName("lithium_niobate_crystal").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":crystal_titanium");
		visible_optics_array = new Item().setUnlocalizedName("visible_optics_array").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":crystal_fluorite");
		visible_dopant_crystal = new Item().setUnlocalizedName("visible_dopant_crystal").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":crystal_fluorite");
		BBO_crystal = new Item().setUnlocalizedName("BBO_crystal").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":crystal_titanium");
		KDP_crystal = new Item().setUnlocalizedName("KDP_crystal").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":crystal_titanium");
		CLBO_crystal = new Item().setUnlocalizedName("CLBO_crystal").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":crystal_titanium");
		LBO_crystal = new Item().setUnlocalizedName("LBO_crystal").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":crystal_titanium");
		crystal_nickel = new Item().setUnlocalizedName("crystal_nickel").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":crystal_nickel");
		crystal_niobium = new Item().setUnlocalizedName("crystal_niobium").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":crystal_niobium");

		crystal_meth = new ItemMeth(1).setUnlocalizedName("crystal_meth").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":crystal_fluorite");


		itemsalt = new ItemSalt(1).setUnlocalizedName("itemsalt").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":item_salt");
		rubidiumsalt = new ItemSalt(1).setUnlocalizedName("rubidiumsalt").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":item_salt");
		magnesium_chloride = new Item().setUnlocalizedName("magnesium_chloride").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":item_salt");


		sodium_sulfate = new Item().setUnlocalizedName("sodium_sulfate").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":item_salt");
		sodium_sulfide = new Item().setUnlocalizedName("sodium_sulfide").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":powder_sodium");
		potassium_sulfate = new ItemFertilizer().setUnlocalizedName("potassium_sulfate").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":item_salt");

		quicklime = new Item().setUnlocalizedName("quicklime").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":item_salt");

		//IF I HAVE TO ADD ONE MORE GODDAMN WHITE POWDER IM GONNA SCREAM


		//oh my god how the fuck am I gonna add francium
		//answer: uhhhh let's skip that for now. I already added a shit ton of missing textures.



		crystal_basaltic = new Item().setUnlocalizedName("crystal_basaltic").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":crystal_basaltic");



		//why the fuck is lead registered twice. Yknow what it works I'm not touching this shit.
		powder_lead = new Item().setUnlocalizedName("powder_lead").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":powder_lead");
		powder_silver = new Item().setUnlocalizedName("powder_silver").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":powder_tantalium");
		powder_tantalium = new ItemCustomLore().setUnlocalizedName("powder_tantalium").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":powder_tantalium");
		powder_neptunium = new Item().setUnlocalizedName("powder_neptunium").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":powder_neptunium");
		powder_polonium = new Item().setUnlocalizedName("powder_polonium").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":powder_polonium");
		powder_co60 = new Item().setUnlocalizedName("powder_co60").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":powder_co60");
		powder_sr90 = new Item().setUnlocalizedName("powder_sr90").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":powder_sr90");
		powder_sr90_tiny = new Item().setUnlocalizedName("powder_sr90_tiny").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":powder_sr90_tiny");
		powder_i131 = new Item().setUnlocalizedName("powder_i131").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":powder_i131");
		powder_i131_tiny = new Item().setUnlocalizedName("powder_i131_tiny").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":powder_i131_tiny");
		powder_xe135 = new Item().setUnlocalizedName("powder_xe135").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":powder_xe135");
		powder_xe135_tiny = new Item().setUnlocalizedName("powder_xe135_tiny").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":powder_xe135_tiny");
		powder_cs137 = new Item().setUnlocalizedName("powder_cs137").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":powder_cs137");
		powder_cs137_tiny = new Item().setUnlocalizedName("powder_cs137_tiny").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":powder_cs137_tiny");
		powder_au198 = new Item().setUnlocalizedName("powder_au198").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":powder_au198");
		powder_ra226 = new Item().setUnlocalizedName("powder_ra226").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":powder_ra226");
		powder_at209 = new Item().setUnlocalizedName("powder_at209").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":powder_at209");
		powder_schrabidium = new ItemCustomLore().setRarity(EnumRarity.rare).setUnlocalizedName("powder_schrabidium").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":powder_schrabidium");
		powder_schrabidate = new ItemCustomLore().setRarity(EnumRarity.rare).setUnlocalizedName("powder_schrabidate").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":powder_schrabidate");
		powder_aluminium = new Item().setUnlocalizedName("powder_aluminium").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":powder_aluminium");
		powder_beryllium = new Item().setUnlocalizedName("powder_beryllium").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":powder_beryllium");
		powder_copper = new Item().setUnlocalizedName("powder_copper").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":powder_copper");
		powder_zinc = new Item().setUnlocalizedName("powder_zinc").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":powder_zinc");
		powder_lead = new Item().setUnlocalizedName("powder_lead").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":powder_lead");
		powder_gold = new Item().setUnlocalizedName("powder_gold").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":powder_gold");
		powder_iron = new Item().setUnlocalizedName("powder_iron").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":powder_iron");
		powder_chromium = new Item().setUnlocalizedName("powder_chromium").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":powder_titanium");
		powder_titanium = new Item().setUnlocalizedName("powder_titanium").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":powder_titanium");
		powder_zircon = new Item().setUnlocalizedName("powder_zircon").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":powder_copper");
		powder_nickel = new Item().setUnlocalizedName("powder_nickel").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":powder_nickel");
		powder_tungsten = new Item().setUnlocalizedName("powder_tungsten").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":powder_tungsten");
		powder_uranium = new Item().setUnlocalizedName("powder_uranium").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":powder_uranium");
		powder_plutonium = new Item().setUnlocalizedName("powder_plutonium").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":powder_plutonium");
		powder_columbite = new Item().setUnlocalizedName("powder_columbite").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":powder_coal");
		dust = new ItemCustomLore().setUnlocalizedName("dust").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":dust");
		dust_tiny = new Item().setUnlocalizedName("dust_tiny").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":dust_tiny");
		fallout = new Item().setUnlocalizedName("fallout").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":fallout");
		powder_advanced_alloy = new Item().setUnlocalizedName("powder_advanced_alloy").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":powder_advanced_alloy");
		powder_tcalloy = new Item().setUnlocalizedName("powder_tcalloy").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":powder_tcalloy");
		powder_coal = new Item().setUnlocalizedName("powder_coal").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":powder_coal");
		powder_coal_tiny = new Item().setUnlocalizedName("powder_coal_tiny").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":powder_coal_tiny");
		powder_combine_steel = new Item().setUnlocalizedName("powder_combine_steel").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":powder_combine_steel");
		powder_diamond = new Item().setUnlocalizedName("powder_diamond").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":powder_diamond");
		powder_emerald = new Item().setUnlocalizedName("powder_emerald").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":powder_emerald");
		powder_lapis = new Item().setUnlocalizedName("powder_lapis").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":powder_lapis");
		powder_quartz = new Item().setUnlocalizedName("powder_quartz").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":powder_quartz");
		powder_magnetized_tungsten = new Item().setUnlocalizedName("powder_magnetized_tungsten").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":powder_magnetized_tungsten");
		powder_chlorophyte = new Item().setUnlocalizedName("powder_chlorophyte").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":powder_chlorophyte");
		powder_red_copper = new Item().setUnlocalizedName("powder_red_copper").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":powder_red_copper");
		powder_steel = new Item().setUnlocalizedName("powder_steel").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":powder_steel");
		powder_lithium = new Item().setUnlocalizedName("powder_lithium").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":powder_lithium");
		powder_zirconium = new Item().setUnlocalizedName("powder_zirconium").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":powder_zirconium");
		powder_sodium = new Item().setUnlocalizedName("powder_sodium").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":powder_sodium");
		powder_sodium_silicate = new Item().setUnlocalizedName("powder_sodium_silicate").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":crystal_fluorite");
		redstone_depleted = new Item().setUnlocalizedName("redstone_depleted").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":redstone_depleted");
		powder_power = new ItemCustomLore().setRarity(EnumRarity.uncommon).setUnlocalizedName("powder_power").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":powder_energy_alt");
		powder_iodine = new ItemCustomLore().setRarity(EnumRarity.epic).setUnlocalizedName("powder_iodine").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":powder_iodine");
		powder_thorium = new ItemCustomLore().setRarity(EnumRarity.uncommon).setUnlocalizedName("powder_thorium").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":powder_thorium");
		powder_neodymium = new ItemCustomLore().setRarity(EnumRarity.epic).setUnlocalizedName("powder_neodymium").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":powder_neodymium");
		powder_astatine = new ItemCustomLore().setRarity(EnumRarity.epic).setUnlocalizedName("powder_astatine").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":powder_astatine");
		powder_caesium = new ItemCustomLore().setRarity(EnumRarity.epic).setUnlocalizedName("powder_caesium").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":powder_caesium");
		cesium_salt = new ItemSalt(1).setUnlocalizedName("cesium_salt").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":item_salt");
		arsenic_trioxide = new Item().setUnlocalizedName("arsenic_trioxide").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":item_salt");
		manganese_powder = new Item().setUnlocalizedName("powder_manganese").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":powder_aluminium");
		powder_selenium = new Item().setUnlocalizedName("powder_selenium").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":powder_lead");
		powder_tellurium = new Item().setUnlocalizedName("powder_tellurium").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":powder_lead");
		nugget_tellurium = new Item().setUnlocalizedName("nugget_tellurium").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":nugget_lead");
		powder_tellurium_dioxide = new Item().setUnlocalizedName("powder_tellurium_dioxide").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":item_salt");

		powder_australium = new ItemCustomLore().setRarity(EnumRarity.uncommon).setUnlocalizedName("powder_australium").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":powder_australium");
		powder_weidanium = new ItemCustomLore().setRarity(EnumRarity.uncommon).setUnlocalizedName("powder_weidanium").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":powder_weidanium");
		powder_reiium = new ItemCustomLore().setRarity(EnumRarity.uncommon).setUnlocalizedName("powder_reiium").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":powder_reiium");
		powder_unobtainium = new ItemCustomLore().setRarity(EnumRarity.uncommon).setUnlocalizedName("powder_unobtainium").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":powder_unobtainium");
		powder_daffergon = new ItemCustomLore().setRarity(EnumRarity.uncommon).setUnlocalizedName("powder_daffergon").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":powder_daffergon");
		powder_verticium = new ItemCustomLore().setRarity(EnumRarity.uncommon).setUnlocalizedName("powder_verticium").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":powder_verticium");
		powder_strontium = new ItemCustomLore().setRarity(EnumRarity.epic).setUnlocalizedName("powder_strontium").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":powder_strontium");
		powder_cobalt = new ItemCustomLore().setRarity(EnumRarity.epic).setUnlocalizedName("powder_cobalt").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":powder_cobalt");
		powder_bromine = new ItemCustomLore().setRarity(EnumRarity.epic).setUnlocalizedName("powder_bromine").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":powder_bromine");
		powder_niobium = new ItemCustomLore().setRarity(EnumRarity.epic).setUnlocalizedName("powder_niobium").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":powder_niobium");
		powder_tennessine = new ItemCustomLore().setRarity(EnumRarity.epic).setUnlocalizedName("powder_tennessine").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":powder_tennessine");
		powder_cerium = new ItemCustomLore().setRarity(EnumRarity.epic).setUnlocalizedName("powder_cerium").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":powder_cerium");
		powder_dura_steel = new ItemCustomLore().setUnlocalizedName("powder_dura_steel").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":powder_dura_steel");
		powder_polymer = new ItemCustomLore().setUnlocalizedName("powder_polymer").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":powder_polymer");
		powder_bakelite = new ItemCustomLore().setUnlocalizedName("powder_bakelite").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":powder_bakelite");
		powder_euphemium = new ItemCustomLore().setRarity(EnumRarity.epic).setUnlocalizedName("powder_euphemium").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":powder_euphemium");
		//powder_wd2004_tiny = new ItemCustomLore().setRarity(EnumRarity.epic).setUnlocalizedName("powder_wd2004_tiny").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":powder_wd2004_tiny");
		//powder_wd2004 = new ItemCustomLore().setRarity(EnumRarity.epic).setUnlocalizedName("powder_wd2004").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":powder_wd2004");
		//powder_meteorite = new Item().setUnlocalizedName("powder_meteorite").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":powder_meteorite");
		powder_lanthanium = new ItemCustomLore().setRarity(EnumRarity.epic).setUnlocalizedName("powder_lanthanium").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":powder_lanthanium");
		powder_yttrium = new ItemCustomLore().setRarity(EnumRarity.epic).setUnlocalizedName("powder_yttrium").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":powder_yttrium");
		powder_actinium = new ItemCustomLore().setRarity(EnumRarity.epic).setUnlocalizedName("powder_actinium").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":powder_actinium");
		powder_boron = new ItemCustomLore().setRarity(EnumRarity.epic).setUnlocalizedName("powder_boron").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":powder_boron");
		powder_semtex_mix = new Item().setUnlocalizedName("powder_semtex_mix").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":powder_semtex_mix");
		powder_desh_mix = new Item().setUnlocalizedName("powder_desh_mix").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":powder_desh_mix");
		powder_desh_ready = new Item().setUnlocalizedName("powder_desh_ready").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":powder_desh_ready");
		powder_nitan_mix = new Item().setUnlocalizedName("powder_nitan_mix").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":powder_nitan_mix");
		powder_spark_mix = new Item().setUnlocalizedName("powder_spark_mix").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":powder_spark_mix");
		powder_desh = new Item().setUnlocalizedName("powder_desh").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":powder_desh");
		powder_steel_tiny = new Item().setUnlocalizedName("powder_steel_tiny").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":powder_steel_tiny");
		powder_lithium_tiny = new Item().setUnlocalizedName("powder_lithium_tiny").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":powder_lithium_tiny");
		titanium_trace_metals_slurry = new Item().setUnlocalizedName("titanium_trace_metals_slurry").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":titanium_trace_metals_slurry");
		powder_neodymium_tiny = new Item().setUnlocalizedName("powder_neodymium_tiny").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":powder_neodymium_tiny");
		powder_cobalt_tiny = new Item().setUnlocalizedName("powder_cobalt_tiny").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":powder_cobalt_tiny");
		powder_alien_tiny = new Item().setUnlocalizedName("powder_alien_tiny").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":powder_cobalt_tiny");
		powder_niobium_tiny = new Item().setUnlocalizedName("powder_niobium_tiny").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":powder_niobium_tiny");
		powder_cerium_tiny = new Item().setUnlocalizedName("powder_cerium_tiny").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":powder_cerium_tiny");
		powder_lanthanium_tiny = new Item().setUnlocalizedName("powder_lanthanium_tiny").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":powder_lanthanium_tiny");
		powder_actinium_tiny = new Item().setUnlocalizedName("powder_actinium_tiny").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":powder_actinium_tiny");
		powder_praseodymium_tiny = new Item().setUnlocalizedName("powder_praseodymium_tiny").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":powder_praseodymium_tiny");
		powder_yttrium_tiny = new Item().setUnlocalizedName("powder_yttrium_tiny").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":powder_yttrium_tiny");
		powder_boron_tiny = new Item().setUnlocalizedName("powder_boron_tiny").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":powder_boron_tiny");
		powder_vanadium_tiny = new Item().setUnlocalizedName("powder_vanadium_tiny").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":powder_vanadium_tiny");
		powder_meteorite_tiny = new Item().setUnlocalizedName("powder_meteorite_tiny").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":powder_meteorite_tiny");
		powder_yellowcake = new Item().setUnlocalizedName("powder_yellowcake").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":powder_yellowcake");
		powder_magic = new Item().setUnlocalizedName("powder_magic").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":powder_magic");
		powder_balefire = new Item().setUnlocalizedName("powder_balefire").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":powder_balefire");
		powder_sawdust = new Item().setUnlocalizedName("powder_sawdust").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":powder_sawdust");
		powder_flux = new Item().setUnlocalizedName("powder_flux").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":powder_flux");
		powder_fertilizer = new ItemFertilizer().setUnlocalizedName("powder_fertilizer").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":powder_fertilizer");
		powder_coltan_ore = new Item().setUnlocalizedName("powder_coltan_ore").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":powder_coltan_ore");
		powder_coltan = new Item().setUnlocalizedName("powder_coltan").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":powder_coltan");
		powder_tektite = new Item().setUnlocalizedName("powder_tektite").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":powder_tektite");
		powder_paleogenite = new Item().setUnlocalizedName("powder_paleogenite").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":powder_paleogenite");
		powder_paleogenite_tiny = new Item().setUnlocalizedName("powder_paleogenite_tiny").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":powder_paleogenite_tiny");
		powder_impure_osmiridium = new Item().setUnlocalizedName("powder_impure_osmiridium").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":powder_impure_osmiridium");
		powder_osmium_tiny = new Item().setUnlocalizedName("powder_osmium_tiny").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":powder_osmium_tiny");
		iridium_rich_residue = new Item().setUnlocalizedName("iridium_rich_residue").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":powder_titanium");
		iridium_oxide = new Item().setUnlocalizedName("iridium_oxide").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":powder_coal");
		powder_borax = new Item().setUnlocalizedName("powder_borax").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":powder_borax");
		powder_chlorocalcite = new Item().setUnlocalizedName("powder_chlorocalcite").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":powder_chlorocalcite");
		powder_molysite = new Item().setUnlocalizedName("powder_molysite").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":powder_molysite");
		powder_antimony_trioxide = new Item().setUnlocalizedName("powder_antimony_trioxide").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":powder_lead");

		fragment_neodymium = new Item().setUnlocalizedName("fragment_neodymium").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":fragment_neodymium");
		ingot_neodymium = new Item().setUnlocalizedName("ingot_neodymium").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":ingot_neodymium");
		fragment_cobalt = new Item().setUnlocalizedName("fragment_cobalt").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":fragment_cobalt");
		fragment_alien = new Item().setUnlocalizedName("fragment_alien").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":fragment_cobalt");
		fragment_niobium = new Item().setUnlocalizedName("fragment_niobium").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":fragment_niobium");
		fragment_cerium = new Item().setUnlocalizedName("fragment_cerium").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":fragment_cerium");
		fragment_lanthanium = new Item().setUnlocalizedName("fragment_lanthanium").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":fragment_lanthanium");
		fragment_actinium = new Item().setUnlocalizedName("fragment_actinium").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":fragment_actinium");
		fragment_yttrium = new Item().setUnlocalizedName("fragment_yttrium").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":fragment_yttrium");
		fragment_boron = new Item().setUnlocalizedName("fragment_boron").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":fragment_boron");
		fragment_meteorite = new Item().setUnlocalizedName("fragment_meteorite").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":fragment_meteorite");
		fragment_coltan = new Item().setUnlocalizedName("fragment_coltan").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":fragment_coltan");
		fragment_praseodymium = new Item().setUnlocalizedName("fragment_praseodymium").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":fragment_praseodymium");
		fragment_samarium = new Item().setUnlocalizedName("fragment_samarium").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":fragment_samarium");
		fragment_europium = new Item().setUnlocalizedName("fragment_europium").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":fragment_samarium");
		fragment_gadolinium = new Item().setUnlocalizedName("fragment_gadolinium").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":fragment_praseodymium");
		fragment_terbium = new Item().setUnlocalizedName("fragment_terbium").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":fragment_samarium");
		fragment_dysprosium = new Item().setUnlocalizedName("fragment_dysprosium").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":fragment_samarium");
		fragment_holmium = new Item().setUnlocalizedName("fragment_holmium").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":fragment_meteorite");
		fragment_erbium = new Item().setUnlocalizedName("fragment_erbium").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":fragment_praseodymium");
		fragment_ytterbium = new Item().setUnlocalizedName("fragment_ytterbium").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":fragment_praseodymium");
		fragment_lutetium = new Item().setUnlocalizedName("fragment_lutetium").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":fragment_lanthanium");
		fragment_thulium = new Item().setUnlocalizedName("fragment_thulium").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":fragment_meteorite");
		powder_terbium_tiny = new Item().setUnlocalizedName("powder_terbium_tiny").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":powder_praseodymium_tiny");
		powder_dysprosium_tiny = new Item().setUnlocalizedName("powder_dysprosium_tiny").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":powder_praseodymium_tiny");
		powder_holmium_tiny = new Item().setUnlocalizedName("powder_holmium_tiny").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":powder_coal_tiny");
		powder_erbium_tiny = new Item().setUnlocalizedName("powder_erbium_tiny").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":powder_praseodymium_tiny");
		powder_thulium2 = new Item().setUnlocalizedName("powder_thulium2").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":powder_praseodymium");
		powder_thulium = new Item().setUnlocalizedName("powder_thulium").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":powder_lead");
		powder_ytterbium_tiny = new Item().setUnlocalizedName("powder_ytterbium_tiny").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":powder_neodymium_tiny");
		powder_lutetium_tiny = new Item().setUnlocalizedName("powder_lutetium_tiny").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":powder_steel_tiny");
		powder_thulium_tiny = new Item().setUnlocalizedName("powder_thulium_tiny").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":powder_steel_tiny");
		powder_terbium = new Item().setUnlocalizedName("powder_terbium").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":powder_titanium");
		powder_dysprosium_concentrate = new Item().setUnlocalizedName("powder_dysprosium_concentrate").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":powder_titanium");
		powder_dysprosium = new Item().setUnlocalizedName("powder_dysprosium").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":powder_titanium");
		europium_dust_tiny = new Item().setUnlocalizedName("europium_dust_tiny").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":nugget_mercury_tiny");
		gadolinium_dust_tiny = new Item().setUnlocalizedName("gadolinium_dust_tiny").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":nugget_mercury_tiny");
		gadoliniumsol = new Item().setUnlocalizedName("gadoliniumsol").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":crystal_fluorite");
		terbiumsol = new Item().setUnlocalizedName("terbiumsol").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":crystal_niter");
		dysprosiumsol = new Item().setUnlocalizedName("dysprosiumsol").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":crystal_niter");
		lutetiumsol = new Item().setUnlocalizedName("lutetiumsol").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":crystal_lead");
		thuliumsol = new Item().setUnlocalizedName("thuliumsol").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":powder_lead");
		powder_lutetium2 = new Item().setUnlocalizedName("powder_lutetium2").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":crystal_lead");
		powder_dysprosium2 = new Item().setUnlocalizedName("powder_dysprosium2").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":powder_titanium");
		powder_holmium_oxide = new Item().setUnlocalizedName("powder_holmium_oxide").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":powder_yellowcake");
		powder_ytterbium_oxide = new Item().setUnlocalizedName("powder_ytterbium_oxide").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":powder_platnium");
		powder_ytterbium_fluoride = new Item().setUnlocalizedName("powder_ytterbium_fluoride").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":item_salt");
		powder_ytterbium = new Item().setUnlocalizedName("powder_ytterbium").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":powder_lead");
		powder_holmium_concentrate = new Item().setUnlocalizedName("powder_holmium_concentrate").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":powder_lead");
		powder_erbium_concentrate = new Item().setUnlocalizedName("powder_erbium_concentrate").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":powder_iron");
		powder_ytterbium_concentrate = new Item().setUnlocalizedName("powder_ytterbium_concentrate").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":powder_titanium");
		powder_lutetium_concentrate = new Item().setUnlocalizedName("powder_lutetium_concentrate").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":powder_lead");
		powder_thulium_concentrate = new Item().setUnlocalizedName("powder_thulium_concentrate").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":powder_lanthanium");
		erbium_powder = new Item().setUnlocalizedName("erbium_powder").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":powder_titanium");
		er_yag_feedstock = new Item().setUnlocalizedName("er_yag_feedstock").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":crystal_iron");
		yby_feedstock = new Item().setUnlocalizedName("yby_feedstock").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":crystal_fluorite");
		znse_feedstock = new Item().setUnlocalizedName("znse_feedstock").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":crystal_niter");
		er_yag_sintered = new Item().setUnlocalizedName("er_yag_sintered").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":crystal_tungsten");
		yby_sintered = new Item().setUnlocalizedName("yby_sintered").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":crystal_nickel");
		yttrium_aluminum_erbium_crystal = new Item().setUnlocalizedName("yttrium_aluminum_erbium_crystal").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":crystal_thorium");
		//ruby_crystal = new Item().setUnlocalizedName("ruby_crystal").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":crystal_phosphorus");
		YbY_crystal = new Item().setUnlocalizedName("YbY_crystal").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":crystal_niter");
		ZnSe_crystal = new Item().setUnlocalizedName("ZnSe_crystal").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":crystal_copper");
		germanium_crystal = new Item().setUnlocalizedName("germanium_crystal").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":crystal_basaltic");
		ingot_erbium = new Item().setUnlocalizedName("ingot_erbium").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":ingot_nickel");
		ingot_ytterbium = new Item().setUnlocalizedName("ingot_ytterbium").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":ingot_nickel");
		ingot_lutetium = new Item().setUnlocalizedName("ingot_lutetium").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":ingot_lead");
		ingot_dysprosium = new Item().setUnlocalizedName("ingot_dysprosium").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":ingot_neptunium");
		ingot_holmium = new Item().setUnlocalizedName("ingot_holmium").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":ingot_rubidium");
		powder_terbium2 = new Item().setUnlocalizedName("powder_terbium2").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":powder_platnium");
		powder_terbium_oxide = new Item().setUnlocalizedName("powder_terbium_oxide").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":item_salt");
		powder_terbium_fluoride = new Item().setUnlocalizedName("powder_terbium_fluoride").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":powder_calcium");
		ingot_terbium_impure = new Item().setUnlocalizedName("ingot_terbium_impure").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":ingot_titanium");
		gadolinium_dust_step = new Item().setUnlocalizedName("gadolinium_dust_step").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":powder_iron");
		powder_gadolinium = new Item().setUnlocalizedName("powder_gadolinium").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":powder_zirconium");
		powder_gadolinium2 = new Item().setUnlocalizedName("powder_gadolinium2").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":powder_zirconium");
		ingot_gadolinium = new Item().setUnlocalizedName("ingot_gadolinium").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":ingot_uranium");
		europiumsol = new Item().setUnlocalizedName("europiumsol").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":crystal_fluorite");
		ingot_praseodymium = new Item().setUnlocalizedName("ingot_praseodymium").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":ingot_praseodymium");
		nugget_praseodymium = new Item().setUnlocalizedName("nugget_praseodymium").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":nugget_lead");
		powder_praseodymium = new Item().setUnlocalizedName("powder_praseodymium").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":powder_praseodymium");
		chunk_ore = new ItemEnumMulti(EnumChunkType.class, true, true).setUnlocalizedName("chunk_ore").setCreativeTab(MainRegistry.partsTab);
		//FUCK BOBCAT AND HIS GODDAMN ENUM BULLSHIT
		powder_steel_dusted = new Item().setUnlocalizedName("powder_steel_dusted").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":powder_steel");

		//why the fuck did you need enums... bobcat moment!
		chunk_ironoxide = new Item().setUnlocalizedName("chunk_ironoxide").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":crystal_iron");
		chunk_coppersulfide = new Item().setUnlocalizedName("chunk_coppersulfide").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":crystal_copper");
		powder_vanadium = new Item().setUnlocalizedName("powder_vanadium").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":powder_vanadium");
		chunk_leadzincsulfide = new Item().setUnlocalizedName("chunk_leadzincsulfide").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":crystal_lead");
		chunk_nickelsulfide = new Item().setUnlocalizedName("chunk_nickelsulfide").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":crystal_nickel");
		chunk_tintungsten = new Item().setUnlocalizedName("chunk_tintungsten").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":crystal_iron");
		chunk_lithiumpegmatite = new Item().setUnlocalizedName("chunk_lithiumpegmatite").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":crystal_fluorite");
		chunk_heavymineralsand = new Item().setUnlocalizedName("chunk_heavymineralsand").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":crystal_mineral");
		chunk_evaporite = new Item().setUnlocalizedName("chunk_evaporite").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":crystal_niter");
		chunk_carbon = new Item().setUnlocalizedName("chunk_carbon").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":crystal_tungsten");
		chunk_chromite = new Item().setUnlocalizedName("chunk_chromite").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":crystal_iron");


		//ORE GROUPS
		//iron
		magnetite = new Item().setUnlocalizedName("magnetite").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":crystal_beryllium");
		goethite = new Item().setUnlocalizedName("goethite").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":crystal_beryllium");
		fragment_vanadium = new Item().setUnlocalizedName("fragment_vanadium").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":fragment_vanadium");

		//copper
		chalcopyrite = new Item().setUnlocalizedName("chalcopyrite").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":crystal_copper");
		bornite = new Item().setUnlocalizedName("bornite").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":chunk_ore.rare");
		chalcocite = new Item().setUnlocalizedName("chalcocite").setCreativeTab(MainRegistry.partsTab).setTextureName("minecraft" + ":coal"); //coal but shiny
		covellite = new Item().setUnlocalizedName("covellite").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":crystal_niobium");
		molybdenite = new Item().setUnlocalizedName("molybdenite").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":crystal_tungsten");

		//PLAT
		osmiridium = new Item().setUnlocalizedName("osmiridium").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":crystal_coal");

		pgm_residue = new Item().setUnlocalizedName("pgm_residue").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":crystal_schrabidium");

		//lead-Zinc
		galena = new Item().setUnlocalizedName("galena").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":crystal_nickel");
		sphalerite = new Item().setUnlocalizedName("sphalerite").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":crystal_lead");
		stibnite = new Item().setUnlocalizedName("stibnite").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":crystal_lead");

		//nickel
		pentlandite = new Item().setUnlocalizedName("pentlandite").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":crystal_mineral");
		pyrrhotite = new Item().setUnlocalizedName("pyrrhotite").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":crystal_lead");

		//tin-tungsten
		cassiterite = new Item().setUnlocalizedName("cassiterite").setCreativeTab(MainRegistry.partsTab).setTextureName("minecraft" + ":coal");
		wolframite = new Item().setUnlocalizedName("wolframite").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":crystal_tungsten");
		columbite = new Item().setUnlocalizedName("columbite").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":crystal_nickel");

		//lithium
		spodumene = new Item().setUnlocalizedName("spodumene").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":crystal_lithium");
		lepidolite = new Item().setUnlocalizedName("lepidolite").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":crystal_niobium");
		petalite = new Item().setUnlocalizedName("petalite").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":crystal_lithium");

		//heavy mineral sand
		ilmenite = new Item().setUnlocalizedName("ilmenite").setCreativeTab(MainRegistry.partsTab).setTextureName("minecraft" + ":coal");
		rutile = new Item().setUnlocalizedName("rutile").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":crystal_titanium");
		zircon = new Item().setUnlocalizedName("zircon").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":crystal_copper");

		//chromite
		chromite = new Item().setUnlocalizedName("chromite").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":crystal_iron");

		//evaporite
		carnallite = new Item().setUnlocalizedName("carnallite").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":crystal_copper");

		crushed_spodumene = new Item().setUnlocalizedName("crushed_spodumene").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":powder_lithium");
		crushed_petalite = new Item().setUnlocalizedName("crushed_petalite").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":powder_lithium");
		//roasted_spodumene = new Item().setUnlocalizedName("roasted_spodumene").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":powder_lithium");
		//powder_ilmenite = new Item().setUnlocalizedName("powder_ilmenite").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":powder_titanium");

		biomass = new Item().setUnlocalizedName("biomass").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":biomass");
		biomass_compressed = new Item().setUnlocalizedName("biomass_compressed").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":biomass_compressed");
		bio_wafer = new ItemLemon(4, 2F, false).setUnlocalizedName("bio_wafer").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":bio_wafer");
		//JE's jerky
		flesh = new ItemLemon(1, 2F, false).setUnlocalizedName("flesh").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":flesh");
		flesh_wafer = new ItemLemon(5, 5F, false).setUnlocalizedName("flesh_wafer").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":blood_wafer");
		grilled_flesh = new ItemLemon(6, 10F, false).setUnlocalizedName("grilled_flesh").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":blood_patty");
	    flesh_burger = new ItemLemon(7, 20F, false).setUnlocalizedName("flesh_burger").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":blood_burger");
		plant_item = new ItemEnumMulti(EnumPlantType.class, true, true).setUnlocalizedName("plant_item").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":plant_item");

		coil_copper = new Item().setUnlocalizedName("coil_copper").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":coil_copper");
		coil_copper_torus = new Item().setUnlocalizedName("coil_copper_torus").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":coil_copper_torus");
		coil_tungsten = new Item().setUnlocalizedName("coil_tungsten").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":coil_tungsten");
		tank_steel = new Item().setUnlocalizedName("tank_steel").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":tank_steel");
		motor = new Item().setUnlocalizedName("motor").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":motor");
		motor_desh = new Item().setUnlocalizedName("motor_desh").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":motor_desh");
		motor_bismuth = new Item().setUnlocalizedName("motor_bismuth").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":motor_bismuth");
		centrifuge_element = new Item().setUnlocalizedName("centrifuge_element").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":centrifuge_element");
		reactor_core = new Item().setUnlocalizedName("reactor_core").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":reactor_core");
		rtg_unit = new Item().setUnlocalizedName("rtg_unit").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":rtg_unit");
		levitation_unit = new Item().setUnlocalizedName("levitation_unit").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":levitation_unit");
		wire_aluminium = new Item().setUnlocalizedName("wire_aluminium").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":wire_aluminium");
		wire_copper = new Item().setUnlocalizedName("wire_copper").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":wire_copper");
		wire_gold = new Item().setUnlocalizedName("wire_gold").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":wire_gold");
		wire_schrabidium = new ItemCustomLore().setRarity(EnumRarity.rare).setUnlocalizedName("wire_schrabidium").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":wire_schrabidium");
		wire_magnetized_tungsten = new Item().setUnlocalizedName("wire_magnetized_tungsten").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":wire_magnetized_tungsten");
		coil_magnetized_tungsten = new Item().setUnlocalizedName("coil_magnetized_tungsten").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":coil_magnetized_tungsten");
		coil_gold = new Item().setUnlocalizedName("coil_gold").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":coil_gold");
		coil_gold_torus = new Item().setUnlocalizedName("coil_gold_torus").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":coil_gold_torus");
		magnet_circular = new Item().setUnlocalizedName("magnet_circular").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":magnet_circular");
		pellet_coal = new Item().setUnlocalizedName("pellet_coal").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":pellet_coal");
		component_limiter = new Item().setUnlocalizedName("component_limiter").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":component_limiter");
		component_emitter = new Item().setUnlocalizedName("component_emitter").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":component_emitter");
		chlorine_pinwheel = new ItemInfiniteFluid(Fluids.CHLORINE, 1, 2).setUnlocalizedName("chlorine_pinwheel").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":chlorine_pinwheel");
		FluidTank.noDualUnload.add(chlorine_pinwheel);
		oxy_pinwheel = new ItemInfiniteFluid(Fluids.OXYGEN, 1).setUnlocalizedName("oxy_pinwheel").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":oxy_pinwheel");
		FluidTank.noDualUnload.add(oxy_pinwheel);
		ring_starmetal = new Item().setUnlocalizedName("ring_starmetal").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":ring_starmetal");
		flywheel_beryllium = new Item().setUnlocalizedName("flywheel_beryllium").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":flywheel_beryllium");
		deuterium_filter = new Item().setUnlocalizedName("deuterium_filter").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":deuterium_filter");
		parts_legendary = new ItemEnumMulti(EnumLegendaryType.class, false, true).setUnlocalizedName("parts_legendary").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":parts_legendary");

		gear_large = new ItemGear().setUnlocalizedName("gear_large").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":gear_large");
		sawblade = new Item().setUnlocalizedName("sawblade").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":sawblade");

		shell = new ItemAutogen(MaterialShapes.SHELL).oun("shellntm").setUnlocalizedName("shell").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":shell");
		pipe = new ItemAutogen(MaterialShapes.PIPE).oun("pipentm").setUnlocalizedName("pipe").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":pipe");
		fins_flat = new Item().setUnlocalizedName("fins_flat").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":fins_flat");
		fins_small_steel = new Item().setUnlocalizedName("fins_small_steel").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":fins_small_steel");
		fins_big_steel = new Item().setUnlocalizedName("fins_big_steel").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":fins_big_steel");
		fins_tri_steel = new Item().setUnlocalizedName("fins_tri_steel").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":fins_tri_steel");
		fins_quad_titanium = new Item().setUnlocalizedName("fins_quad_titanium").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":fins_quad_titanium");
		sphere_steel = new Item().setUnlocalizedName("sphere_steel").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":sphere_steel");
		pedestal_steel = new Item().setUnlocalizedName("pedestal_steel").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":pedestal_steel");
		dysfunctional_reactor = new Item().setUnlocalizedName("dysfunctional_reactor").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":dysfunctional_reactor");
		blade_titanium = new Item().setUnlocalizedName("blade_titanium").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":blade_titanium");
		blade_syngas = new Item().setUnlocalizedName("blade_syngas").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":blade_syngas");
		turbine_titanium = new Item().setUnlocalizedName("turbine_titanium").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":turbine_titanium");
		blade_tungsten = new Item().setUnlocalizedName("blade_tungsten").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":blade_tungsten");
		turbine_tungsten = new Item().setUnlocalizedName("turbine_tungsten").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":turbine_tungsten");
		turbine_syngas = new Item().setUnlocalizedName("turbine_syngas").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":turbine_syngas");

		toothpicks = new Item().setUnlocalizedName("toothpicks").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":toothpicks");
		ducttape = new Item().setUnlocalizedName("ducttape").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":ducttape");
		catalyst_clay = new Item().setUnlocalizedName("catalyst_clay").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":catalyst_clay");

		warhead_generic_small = new Item().setUnlocalizedName("warhead_generic_small").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":warhead_generic_small");
		warhead_generic_medium = new Item().setUnlocalizedName("warhead_generic_medium").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":warhead_generic_medium");
		warhead_generic_large = new Item().setUnlocalizedName("warhead_generic_large").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":warhead_generic_large");
		warhead_incendiary_small = new Item().setUnlocalizedName("warhead_incendiary_small").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":warhead_incendiary_small");
		warhead_incendiary_medium = new Item().setUnlocalizedName("warhead_incendiary_medium").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":warhead_incendiary_medium");
		warhead_incendiary_large = new Item().setUnlocalizedName("warhead_incendiary_large").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":warhead_incendiary_large");
		warhead_cluster_small = new Item().setUnlocalizedName("warhead_cluster_small").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":warhead_cluster_small");
		warhead_cluster_medium = new Item().setUnlocalizedName("warhead_cluster_medium").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":warhead_cluster_medium");
		warhead_cluster_large = new Item().setUnlocalizedName("warhead_cluster_large").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":warhead_cluster_large");
		warhead_buster_small = new Item().setUnlocalizedName("warhead_buster_small").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":warhead_buster_small");
		warhead_buster_medium = new Item().setUnlocalizedName("warhead_buster_medium").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":warhead_buster_medium");
		warhead_buster_large = new Item().setUnlocalizedName("warhead_buster_large").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":warhead_buster_large");
		warhead_nuclear = new Item().setUnlocalizedName("warhead_nuclear").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":warhead_nuclear");
		warhead_mirv = new Item().setUnlocalizedName("warhead_mirv").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":warhead_mirv");
		warhead_volcano = new Item().setUnlocalizedName("warhead_volcano").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":warhead_volcano");

		fuel_tank_small = new Item().setUnlocalizedName("fuel_tank_small").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":fuel_tank_small");
		fuel_tank_medium = new Item().setUnlocalizedName("fuel_tank_medium").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":fuel_tank_medium");
		fuel_tank_large = new Item().setUnlocalizedName("fuel_tank_large").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":fuel_tank_large");

		casing = new ItemEnumMulti(ItemEnums.EnumCasingType.class, true, true).setUnlocalizedName("casing").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":casing");
		thruster_small = new Item().setUnlocalizedName("thruster_small").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":thruster_small");
		thruster_medium = new Item().setUnlocalizedName("thruster_medium").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":thruster_medium");
		thruster_large = new Item().setUnlocalizedName("thruster_large").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":thruster_large");

		sat_head_mapper = new Item().setUnlocalizedName("sat_head_mapper").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":sat_head_mapper");
		sat_head_scanner = new Item().setUnlocalizedName("sat_head_scanner").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":sat_head_scanner");
		sat_head_radar = new Item().setUnlocalizedName("sat_head_radar").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":sat_head_radar");
		sat_head_laser = new Item().setUnlocalizedName("sat_head_laser").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":sat_head_laser");
		sat_head_resonator = new Item().setUnlocalizedName("sat_head_resonator").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":sat_head_resonator");

		seg_10 = new Item().setUnlocalizedName("seg_10").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":seg_10");
		seg_15 = new Item().setUnlocalizedName("seg_15").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":seg_15");
		seg_20 = new Item().setUnlocalizedName("seg_20").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":seg_20");

		chopper_head = new Item().setUnlocalizedName("chopper_head").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":chopper_head");
		chopper_gun = new Item().setUnlocalizedName("chopper_gun").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":chopper_gun");
		chopper_torso = new Item().setUnlocalizedName("chopper_torso").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":chopper_torso");
		chopper_tail = new Item().setUnlocalizedName("chopper_tail").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":chopper_tail");
		chopper_wing = new Item().setUnlocalizedName("chopper_wing").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":chopper_wing");
		chopper_blades = new Item().setUnlocalizedName("chopper_blades").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":chopper_blades");
		combine_scrap = new Item().setUnlocalizedName("combine_scrap").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":combine_scrap");

		shimmer_head = new Item().setUnlocalizedName("shimmer_head").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":shimmer_head_original");
		shimmer_axe_head = new Item().setUnlocalizedName("shimmer_axe_head").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":shimmer_axe_head");
		shimmer_handle = new Item().setUnlocalizedName("shimmer_handle").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":shimmer_handle");

		entanglement_kit = new ItemCustomLore().setUnlocalizedName("entanglement_kit").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":entanglement_kit");

		circuit = new ItemCircuit().setUnlocalizedName("circuit").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":circuit");
		circuit_raw = new Item().setUnlocalizedName("circuit_raw").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":circuit_raw");
		circuit_aluminium = new Item().setUnlocalizedName("circuit_aluminium").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":circuit_aluminium");
		circuit_copper = new Item().setUnlocalizedName("circuit_copper").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":circuit_copper");
		circuit_red_copper = new Item().setUnlocalizedName("circuit_red_copper").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":circuit_red_copper");
		circuit_gold = new Item().setUnlocalizedName("circuit_gold").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":circuit_gold");
		circuit_schrabidium = new ItemCustomLore().setRarity(EnumRarity.rare).setUnlocalizedName("circuit_schrabidium").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":circuit_schrabidium");
		circuit_bismuth_raw = new Item().setUnlocalizedName("circuit_bismuth_raw").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":circuit_bismuth_raw");
		circuit_bismuth = new ItemCustomLore().setRarity(EnumRarity.uncommon).setUnlocalizedName("circuit_bismuth").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":circuit_bismuth");
		circuit_arsenic_raw = new Item().setUnlocalizedName("circuit_arsenic_raw").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":circuit_arsenic_raw");
		circuit_arsenic = new ItemCustomLore().setRarity(EnumRarity.uncommon).setUnlocalizedName("circuit_arsenic").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":circuit_arsenic");
		circuit_tantalium_raw = new Item().setUnlocalizedName("circuit_tantalium_raw").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":circuit_tantalium_raw");
		circuit_tantalium = new ItemCustomLore().setRarity(EnumRarity.uncommon).setUnlocalizedName("circuit_tantalium").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":circuit_tantalium");
		crt_display = new Item().setUnlocalizedName("crt_display").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":crt_display");
		circuit_star_piece = (ItemEnumMulti) new ItemEnumMulti(ScrapType.class, true, true).setUnlocalizedName("circuit_star_piece").setCreativeTab(MainRegistry.partsTab);
		circuit_star_component = (ItemEnumMulti) new ItemCircuitStarComponent().setUnlocalizedName("circuit_star_component").setCreativeTab(MainRegistry.partsTab);
		circuit_star = new ItemCustomLore().setRarity(EnumRarity.uncommon).setUnlocalizedName("circuit_star").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":circuit_star");
		circuit_targeting_tier1 = new Item().setUnlocalizedName("circuit_targeting_tier1").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":circuit_targeting_tier1");
		circuit_targeting_tier2 = new Item().setUnlocalizedName("circuit_targeting_tier2").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":circuit_targeting_tier2");
		circuit_targeting_tier3 = new Item().setUnlocalizedName("circuit_targeting_tier3").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":circuit_targeting_tier3");
		circuit_targeting_tier4 = new Item().setUnlocalizedName("circuit_targeting_tier4").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":circuit_targeting_tier4");
		circuit_targeting_tier5 = new Item().setUnlocalizedName("circuit_targeting_tier5").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":circuit_targeting_tier5");
		circuit_targeting_tier6 = new Item().setUnlocalizedName("circuit_targeting_tier6").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":circuit_targeting_tier6");
		mechanism_revolver_1 = new Item().setUnlocalizedName("mechanism_revolver_1").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":mechanism_1");
		mechanism_revolver_2 = new Item().setUnlocalizedName("mechanism_revolver_2").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":mechanism_3");
		mechanism_rifle_1 = new Item().setUnlocalizedName("mechanism_rifle_1").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":mechanism_2");
		mechanism_rifle_2 = new Item().setUnlocalizedName("mechanism_rifle_2").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":mechanism_4");
		mechanism_launcher_1 = new Item().setUnlocalizedName("mechanism_launcher_1").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":mechanism_5");
		mechanism_launcher_2 = new Item().setUnlocalizedName("mechanism_launcher_2").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":mechanism_6");
		mechanism_special = new Item().setUnlocalizedName("mechanism_special").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":mechanism_7");
		casing_357 = new Item().setUnlocalizedName("casing_357").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":casing_357");
		casing_44 = new Item().setUnlocalizedName("casing_44").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":casing_44");
		casing_9 = new Item().setUnlocalizedName("casing_9").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":casing_9");
		casing_50 = new Item().setUnlocalizedName("casing_50").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":casing_50");
		casing_buckshot = new Item().setUnlocalizedName("casing_buckshot").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":casing_buckshot");
		assembly_iron = new Item().setUnlocalizedName("assembly_iron").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":assembly_iron");
		assembly_steel = new Item().setUnlocalizedName("assembly_steel").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":assembly_steel");
		assembly_lead = new Item().setUnlocalizedName("assembly_lead").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":assembly_lead");
		assembly_gold = new Item().setUnlocalizedName("assembly_gold").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":assembly_gold");
		assembly_schrabidium = new Item().setUnlocalizedName("assembly_schrabidium").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":assembly_schrabidium");
		assembly_nightmare = new Item().setUnlocalizedName("assembly_nightmare").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":assembly_nightmare");
		assembly_desh = new Item().setUnlocalizedName("assembly_desh").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":assembly_desh");
		assembly_nopip = new Item().setUnlocalizedName("assembly_nopip").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":assembly_nopip");
		assembly_smg = new Item().setUnlocalizedName("assembly_smg").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":assembly_smg");
		assembly_556 = new Item().setUnlocalizedName("assembly_556").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":assembly_556");
		assembly_762 = new Item().setUnlocalizedName("assembly_762").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":assembly_762");
		assembly_45 = new Item().setUnlocalizedName("assembly_45").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":assembly_45");
		assembly_uzi = new Item().setUnlocalizedName("assembly_uzi").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":assembly_uzi");
		assembly_actionexpress = new Item().setUnlocalizedName("assembly_actionexpress").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":assembly_actionexpress");
		assembly_calamity = new Item().setUnlocalizedName("assembly_calamity").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":assembly_calamity");
		assembly_lacunae = new Item().setUnlocalizedName("assembly_lacunae").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":assembly_lacunae");
		assembly_nuke = new Item().setUnlocalizedName("assembly_nuke").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":assembly_nuke");
		assembly_luna = new Item().setUnlocalizedName("assembly_luna").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":assembly_luna");
		folly_shell = new Item().setUnlocalizedName("folly_shell").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":folly_shell");
		folly_bullet = new Item().setUnlocalizedName("folly_bullet").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":folly_bullet");
		folly_bullet_nuclear = new Item().setUnlocalizedName("folly_bullet_nuclear").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":folly_bullet_nuclear");
		folly_bullet_du = new Item().setUnlocalizedName("folly_bullet_du").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":folly_bullet_du");

		wiring_red_copper = new ItemWiring().setUnlocalizedName("wiring_red_copper").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":wiring_red_copper");

		pellet_rtg_depleted = new ItemRTGPelletDepleted().setContainerItem(plate_iron).setUnlocalizedName("pellet_rtg_depleted").setCreativeTab(MainRegistry.controlTab);

		// Radium-226
		// ~0.03 W/g thermal, primitive historical RTG concept
		pellet_rtg_radium = new ItemRTGPellet(2)
			.setDecays(DepletedRTGMaterial.LEAD,
					   (long)(RTGUtil.getLifespan(1600.0F, HalfLifeType.LONG, false) * 1.5))
			.setUnlocalizedName("pellet_rtg_radium")
			.setCreativeTab(MainRegistry.controlTab)
			.setMaxStackSize(1)
			.setTextureName(RefStrings.MODID + ":pellet_rtg_radium");

		// U238 + Pu238
		pellet_rtg_weak = new ItemRTGPellet(4)
			.setDecays(DepletedRTGMaterial.LEAD,
					   (long)(RTGUtil.getLifespan(87.7F, HalfLifeType.LONG, false) * 1.5))
			.setUnlocalizedName("pellet_rtg_weak")
			.setCreativeTab(MainRegistry.controlTab)
			.setMaxStackSize(1)
			.setTextureName(RefStrings.MODID + ":pellet_rtg_weak");

		// Pu-238
		// ~0.56 W/g thermal, real NASA RTG fuel
		pellet_rtg = new ItemRTGPellet(10)
			.setDecays(DepletedRTGMaterial.LEAD,
					   (long)(RTGUtil.getLifespan(87.7F, HalfLifeType.LONG, false) * 1.5))
			.setUnlocalizedName("pellet_rtg")
			.setCreativeTab(MainRegistry.controlTab)
			.setMaxStackSize(1)
			.setTextureName(RefStrings.MODID + ":pellet_rtg");

		// Pm-147
		pellet_rtg_promethium = new ItemRTGPellet(4)
			.setDecays(DepletedRTGMaterial.SAMARIUM,
					   (long)(RTGUtil.getLifespan(2.6F, HalfLifeType.MEDIUM, false) * 1.5))
			.setUnlocalizedName("pellet_rtg_promethium")
			.setCreativeTab(MainRegistry.controlTab)
			.setMaxStackSize(1)
			.setTextureName(RefStrings.MODID + ":pellet_rtg");

		// Sr-90
		// ~0.93 W/g thermal
		pellet_rtg_strontium = new ItemRTGPellet(13)
			.setDecays(DepletedRTGMaterial.ZIRCONIUM,
					   (long)(RTGUtil.getLifespan(28.8F, HalfLifeType.MEDIUM, false) * 1.5))
			.setUnlocalizedName("pellet_rtg_strontium")
			.setCreativeTab(MainRegistry.controlTab)
			.setTextureName(RefStrings.MODID + ":pellet_rtg_strontium");

		// Co-60
		// Very energetic but dangerous gamma emitter
		pellet_rtg_cobalt = new ItemRTGPellet(14) //25 -> 14
			//needs high radiation leakage
			.setDecays(DepletedRTGMaterial.NICKEL,
					   (long)(RTGUtil.getLifespan(5.27F, HalfLifeType.SHORT, false) * 1.5))
			.setUnlocalizedName("pellet_rtg_cobalt")
			.setCreativeTab(MainRegistry.controlTab)
			.setTextureName(RefStrings.MODID + ":pellet_rtg_cobalt");

		//.
		// Ac-227
		// Exotic alpha emitter
		pellet_rtg_actinium = new ItemRTGPellet(13)
			.setDecays(DepletedRTGMaterial.LEAD,
					   (long)(RTGUtil.getLifespan(21.8F, HalfLifeType.MEDIUM, false) * 1.5))
			.setUnlocalizedName("pellet_rtg_actinium")
			.setCreativeTab(MainRegistry.controlTab)
			.setTextureName(RefStrings.MODID + ":pellet_rtg_actinium");

		// Am-241
		// ESA future RTG candidate
		pellet_rtg_americium = new ItemRTGPellet(8)
			.setDecays(DepletedRTGMaterial.NEPTUNIUM,
					   (long)(RTGUtil.getLifespan(432.0F, HalfLifeType.LONG, false) * 1.5))
			.setUnlocalizedName("pellet_rtg_americium")
			.setCreativeTab(MainRegistry.controlTab)
			.setMaxStackSize(1)
			.setTextureName(RefStrings.MODID + ":pellet_rtg_americium");

		// Bk-247
		pellet_rtg_berkelium = new ItemRTGPellet(11)
			.setDecays(DepletedRTGMaterial.CALIFORNIUM249,
					   (long)(RTGUtil.getLifespan(0.9F, HalfLifeType.SHORT, false) * 1.5))
			.setUnlocalizedName("pellet_rtg_berkelium")
			.setCreativeTab(MainRegistry.controlTab)
			.setMaxStackSize(1)
			.setTextureName(RefStrings.MODID + ":pellet_rtg_berkelium");

		//thulium 170
		pellet_rtg_tm170 = new ItemRTGPellet(12)
			.setDecays(DepletedRTGMaterial.YB170,
					   (long)(RTGUtil.getLifespan(128.6F, HalfLifeType.MEDIUM, false) * 1.5))
			.setUnlocalizedName("pellet_rtg_tm170")
			.setCreativeTab(MainRegistry.controlTab)
			.setMaxStackSize(1)
			.setTextureName(RefStrings.MODID + ":pellet_rtg_lead");

		// Po-210
		// ~140 W/g thermal
		pellet_rtg_polonium = new ItemRTGPellet(60)
			.setDecays(DepletedRTGMaterial.LEAD,
					   (long)(RTGUtil.getLifespan(138.0F, HalfLifeType.SHORT, false) * 1.5))
			.setUnlocalizedName("pellet_rtg_polonium")
			.setCreativeTab(MainRegistry.controlTab)
			.setMaxStackSize(1)
			.setTextureName(RefStrings.MODID + ":pellet_rtg_polonium");

		// Experimental Au-198 RTG
		// Unrealistic but plausible "high output" synthetic isotope
		pellet_rtg_gold = new ItemRTGPellet(
			1)
			.setDecays(DepletedRTGMaterial.MERCURY,
					   (long)(RTGUtil.getLifespan(2.7F, HalfLifeType.SHORT, false) * 1.5))
			.setUnlocalizedName("pellet_rtg_gold")
			.setCreativeTab(MainRegistry.controlTab)
			.setMaxStackSize(1)
			.setTextureName(RefStrings.MODID + ":pellet_rtg_gold");

		// Pb-210 experimental RTG
		pellet_rtg_lead = new ItemRTGPellet(
			6)
			.setDecays(DepletedRTGMaterial.BISMUTH,
					   (long)(RTGUtil.getLifespan(22.3F, HalfLifeType.MEDIUM, false) * 1.5))
			.setUnlocalizedName("pellet_rtg_lead")
			.setCreativeTab(MainRegistry.controlTab)
			.setTextureName(RefStrings.MODID + ":pellet_rtg_lead");

		// Cf-251
		// Neutron-heavy exotic isotope
		pellet_rtg_cf251 = new ItemRTGPellet(
			11)
			.setDecays(DepletedRTGMaterial.BK247, //skip cf-247 as it decays into bk-247 and I don't want to add more shit
					   (long)(RTGUtil.getLifespan(898.0F, HalfLifeType.LONG, false) * 1.5))
			.setUnlocalizedName("pellet_rtg_cf251")
			.setCreativeTab(MainRegistry.controlTab)
			.setTextureName(RefStrings.MODID + ":pellet_rtg_cf251");

		// Cf-252
		// Extremely energetic neutron emitter
		//should kill you
		pellet_rtg_cf252 = new ItemRTGPellet(
			30)
			.setDecays(DepletedRTGMaterial.CM248,
					   (long)(RTGUtil.getLifespan(2.65F, HalfLifeType.SHORT, false) * 1.5))
			.setUnlocalizedName("pellet_rtg_cf252")
			.setCreativeTab(MainRegistry.controlTab)
			.setTextureName(RefStrings.MODID + ":pellet_rtg_cf252");


		tritium_deuterium_cake = new ItemCustomLore().setUnlocalizedName("tritium_deuterium_cake").setCreativeTab(MainRegistry.controlTab).setMaxStackSize(1).setTextureName(RefStrings.MODID + ":tritium_deuterium_cake");

		piston_selenium = new Item().setUnlocalizedName("piston_selenium").setCreativeTab(MainRegistry.controlTab).setTextureName(RefStrings.MODID + ":piston_selenium");
		piston_set = new ItemPistons().setUnlocalizedName("piston_set").setCreativeTab(MainRegistry.controlTab).setMaxStackSize(1);
		drillbit = new ItemDrillbit().setUnlocalizedName("drillbit").setCreativeTab(MainRegistry.controlTab).setMaxStackSize(1);

		rune_blank = new ItemCustomLore().setEffect().setUnlocalizedName("rune_blank").setCreativeTab(MainRegistry.partsTab).setMaxStackSize(1).setTextureName(RefStrings.MODID + ":rune_blank");
		rune_isa = new ItemCustomLore().setEffect().setUnlocalizedName("rune_isa").setCreativeTab(MainRegistry.partsTab).setMaxStackSize(1).setTextureName(RefStrings.MODID + ":rune_isa");
		rune_dagaz = new ItemCustomLore().setEffect().setUnlocalizedName("rune_dagaz").setCreativeTab(MainRegistry.partsTab).setMaxStackSize(1).setTextureName(RefStrings.MODID + ":rune_dagaz");
		rune_hagalaz = new ItemCustomLore().setEffect().setUnlocalizedName("rune_hagalaz").setCreativeTab(MainRegistry.partsTab).setMaxStackSize(1).setTextureName(RefStrings.MODID + ":rune_hagalaz");
		rune_jera = new ItemCustomLore().setEffect().setUnlocalizedName("rune_jera").setCreativeTab(MainRegistry.partsTab).setMaxStackSize(1).setTextureName(RefStrings.MODID + ":rune_jera");
		rune_thurisaz = new ItemCustomLore().setEffect().setUnlocalizedName("rune_thurisaz").setCreativeTab(MainRegistry.partsTab).setMaxStackSize(1).setTextureName(RefStrings.MODID + ":rune_thurisaz");

		ams_catalyst_blank = new Item().setUnlocalizedName("ams_catalyst_blank").setCreativeTab(MainRegistry.controlTab).setMaxStackSize(1).setTextureName(RefStrings.MODID + ":ams_catalyst_blank");
		ams_catalyst_aluminium = 	new ItemCatalyst(0xCCCCCC, 1000000, 1.15F, 	0.85F, 	1.15F).setUnlocalizedName("ams_catalyst_aluminium").setCreativeTab(MainRegistry.controlTab).setMaxStackSize(1).setTextureName(RefStrings.MODID + ":ams_catalyst_aluminium");
		ams_catalyst_beryllium = 	new ItemCatalyst(0x97978B, 0, 		1.25F, 	0.95F, 	1.05F).setUnlocalizedName("ams_catalyst_beryllium").setCreativeTab(MainRegistry.controlTab).setMaxStackSize(1).setTextureName(RefStrings.MODID + ":ams_catalyst_beryllium");
		ams_catalyst_caesium = 		new ItemCatalyst(0x6400FF, 2500000, 1.00F, 	0.85F, 	1.15F).setUnlocalizedName("ams_catalyst_caesium").setCreativeTab(MainRegistry.controlTab).setMaxStackSize(1).setTextureName(RefStrings.MODID + ":ams_catalyst_caesium");
		ams_catalyst_cerium = 		new ItemCatalyst(0x1D3FFF, 1000000, 1.15F, 	1.15F, 	0.85F).setUnlocalizedName("ams_catalyst_cerium").setCreativeTab(MainRegistry.controlTab).setMaxStackSize(1).setTextureName(RefStrings.MODID + ":ams_catalyst_cerium");
		ams_catalyst_cobalt = 		new ItemCatalyst(0x789BBE, 0, 		1.25F, 	1.05F, 	0.95F).setUnlocalizedName("ams_catalyst_cobalt").setCreativeTab(MainRegistry.controlTab).setMaxStackSize(1).setTextureName(RefStrings.MODID + ":ams_catalyst_cobalt");
		ams_catalyst_copper = 		new ItemCatalyst(0xAADE29, 0, 		1.25F, 	1.00F, 	1.00F).setUnlocalizedName("ams_catalyst_copper").setCreativeTab(MainRegistry.controlTab).setMaxStackSize(1).setTextureName(RefStrings.MODID + ":ams_catalyst_copper");
		ams_catalyst_dineutronium = new ItemCatalyst(0x334077, 2500000, 1.00F, 	1.15F, 	0.85F).setUnlocalizedName("ams_catalyst_dineutronium").setCreativeTab(MainRegistry.controlTab).setMaxStackSize(1).setTextureName(RefStrings.MODID + ":ams_catalyst_dineutronium");
		ams_catalyst_euphemium = 	new ItemCatalyst(0xFF9CD2, 2500000, 1.00F, 	1.00F, 	1.00F).setUnlocalizedName("ams_catalyst_euphemium").setCreativeTab(MainRegistry.controlTab).setMaxStackSize(1).setTextureName(RefStrings.MODID + ":ams_catalyst_euphemium");
		ams_catalyst_iron = 		new ItemCatalyst(0xFF7E22, 1000000, 1.15F, 	0.95F, 	1.05F).setUnlocalizedName("ams_catalyst_iron").setCreativeTab(MainRegistry.controlTab).setMaxStackSize(1).setTextureName(RefStrings.MODID + ":ams_catalyst_iron");
		ams_catalyst_lithium = 		new ItemCatalyst(0xFF2727, 0, 		1.25F, 	0.85F, 	1.15F).setUnlocalizedName("ams_catalyst_lithium").setCreativeTab(MainRegistry.controlTab).setMaxStackSize(1).setTextureName(RefStrings.MODID + ":ams_catalyst_lithium");
		ams_catalyst_niobium = 		new ItemCatalyst(0x3BF1B6, 1000000, 1.15F, 	1.05F, 	0.95F).setUnlocalizedName("ams_catalyst_niobium").setCreativeTab(MainRegistry.controlTab).setMaxStackSize(1).setTextureName(RefStrings.MODID + ":ams_catalyst_niobium");
		ams_catalyst_schrabidium = 	new ItemCatalyst(0x32FFFF, 2500000, 1.00F, 	1.05F, 	0.95F).setUnlocalizedName("ams_catalyst_schrabidium").setCreativeTab(MainRegistry.controlTab).setMaxStackSize(1).setTextureName(RefStrings.MODID + ":ams_catalyst_schrabidium");
		ams_catalyst_strontium = 	new ItemCatalyst(0xDD0D35, 1000000, 1.15F, 	1.00F, 	1.00F).setUnlocalizedName("ams_catalyst_strontium").setCreativeTab(MainRegistry.controlTab).setMaxStackSize(1).setTextureName(RefStrings.MODID + ":ams_catalyst_strontium");
		ingot_raw = new ItemAutogen(MaterialShapes.INGOT).setUnlocalizedName("ingot_raw").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":ingot_raw");
		ams_catalyst_thorium = 		new ItemCatalyst(0x653B22, 2500000, 1.00F, 	0.95F, 	1.05F).setUnlocalizedName("ams_catalyst_thorium").setCreativeTab(MainRegistry.controlTab).setMaxStackSize(1).setTextureName(RefStrings.MODID + ":ams_catalyst_thorium");
		ams_catalyst_tungsten = 	new ItemCatalyst(0xF5FF48, 0, 		1.25F, 	1.15F, 	0.85F).setUnlocalizedName("ams_catalyst_tungsten").setCreativeTab(MainRegistry.controlTab).setMaxStackSize(1).setTextureName(RefStrings.MODID + ":ams_catalyst_tungsten");

		cell_empty = new Item().setUnlocalizedName("cell_empty").setCreativeTab(MainRegistry.controlTab).setTextureName(RefStrings.MODID + ":cell_empty");
		cell_uf6 = new Item().setUnlocalizedName("cell_uf6").setCreativeTab(MainRegistry.controlTab).setContainerItem(ModItems.cell_empty).setTextureName(RefStrings.MODID + ":cell_uf6");
		cell_puf6 = new Item().setUnlocalizedName("cell_puf6").setCreativeTab(MainRegistry.controlTab).setContainerItem(ModItems.cell_empty).setTextureName(RefStrings.MODID + ":cell_puf6");
		cell_antimatter = new ItemDrop().setUnlocalizedName("cell_antimatter").setCreativeTab(MainRegistry.controlTab).setContainerItem(ModItems.cell_empty).setTextureName(RefStrings.MODID + ":cell_antimatter");
		cell_deuterium = new Item().setUnlocalizedName("cell_deuterium").setCreativeTab(MainRegistry.controlTab).setContainerItem(ModItems.cell_empty).setTextureName(RefStrings.MODID + ":cell_deuterium");
		cell_tritium = new Item().setUnlocalizedName("cell_tritium").setCreativeTab(MainRegistry.controlTab).setContainerItem(ModItems.cell_empty).setTextureName(RefStrings.MODID + ":cell_tritium");
		cell_sas3 = new ItemCustomLore().setRarity(EnumRarity.rare).setUnlocalizedName("cell_sas3").setCreativeTab(MainRegistry.controlTab).setContainerItem(ModItems.cell_empty).setTextureName(RefStrings.MODID + ":cell_sas3");
		cell_anti_schrabidium = new ItemDrop().setUnlocalizedName("cell_anti_schrabidium").setCreativeTab(MainRegistry.controlTab).setContainerItem(ModItems.cell_empty).setTextureName(RefStrings.MODID + ":cell_anti_schrabidium");
		cell_balefire = new Item().setUnlocalizedName("cell_balefire").setCreativeTab(MainRegistry.controlTab).setContainerItem(ModItems.cell_empty).setTextureName(RefStrings.MODID + ":cell_balefire");
		part_barrel_light = new ItemAutogen(MaterialShapes.LIGHTBARREL).setUnlocalizedName("part_barrel_light").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":part_barrel_light");
		part_barrel_heavy = new ItemAutogen(MaterialShapes.HEAVYBARREL).setUnlocalizedName("part_barrel_heavy").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":part_barrel_heavy");
		part_receiver_light = new ItemAutogen(MaterialShapes.LIGHTRECEIVER).setUnlocalizedName("part_receiver_light").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":part_receiver_light");
		part_receiver_heavy = new ItemAutogen(MaterialShapes.HEAVYRECEIVER).setUnlocalizedName("part_receiver_heavy").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":part_receiver_heavy");
		part_mechanism = new ItemAutogen(MaterialShapes.MECHANISM).setUnlocalizedName("part_mechanism").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":part_mechanism");
		part_stock = new ItemAutogen(MaterialShapes.STOCK).setUnlocalizedName("part_stock").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":part_stock");
		part_grip = new ItemAutogen(MaterialShapes.GRIP).setUnlocalizedName("part_grip").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":part_grip");

		barium_sulfide = new Item().setUnlocalizedName("barium_sulfide").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":powder_coal");
		barium_carbonate = new Item().setUnlocalizedName("barium_carbonate").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":item_salt");
		barium_oxide = new Item().setUnlocalizedName("barium_oxide").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":item_salt");
		ingot_barium = new Item().setUnlocalizedName("ingot_barium").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":ingot_barium");
		barium_nugget = new Item().setUnlocalizedName("barium_nugget").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":nugget_barium");
		rutherfordium_nugget = new Item().setUnlocalizedName("rutherfordium_nugget").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":pellets_charged");
		dubnium_nugget = new Item().setUnlocalizedName("dubnium_nugget").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":pellets_charged");
		powder_molybdenite = new Item().setUnlocalizedName("powder_molybdenite").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":powder_lead");
		powder_rhenium = new Item().setUnlocalizedName("powder_rhenium").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":powder_lead");

		demon_core_open = new ItemDemonCore().setUnlocalizedName("demon_core_open").setCreativeTab(MainRegistry.nukeTab).setTextureName(RefStrings.MODID + ":demon_core_open");
		demon_core_closed = new Item().setUnlocalizedName("demon_core_closed").setCreativeTab(MainRegistry.nukeTab).setTextureName(RefStrings.MODID + ":demon_core_closed");

		particle_empty = new Item().setUnlocalizedName("particle_empty").setCreativeTab(MainRegistry.controlTab).setTextureName(RefStrings.MODID + ":particle_empty");
		particle_hydrogen = new Item().setUnlocalizedName("particle_hydrogen").setCreativeTab(MainRegistry.controlTab).setContainerItem(ModItems.particle_empty).setTextureName(RefStrings.MODID + ":particle_hydrogen");
		particle_copper = new Item().setUnlocalizedName("particle_copper").setCreativeTab(MainRegistry.controlTab).setContainerItem(ModItems.particle_empty).setTextureName(RefStrings.MODID + ":particle_copper");
		particle_lead = new Item().setUnlocalizedName("particle_lead").setCreativeTab(MainRegistry.controlTab).setContainerItem(ModItems.particle_empty).setTextureName(RefStrings.MODID + ":particle_lead");
		particle_aproton = new Item().setUnlocalizedName("particle_aproton").setCreativeTab(MainRegistry.controlTab).setContainerItem(ModItems.particle_empty).setTextureName(RefStrings.MODID + ":particle_aproton");
		particle_aelectron = new Item().setUnlocalizedName("particle_aelectron").setCreativeTab(MainRegistry.controlTab).setContainerItem(ModItems.particle_empty).setTextureName(RefStrings.MODID + ":particle_aelectron");
		particle_amat = new Item().setUnlocalizedName("particle_amat").setCreativeTab(MainRegistry.controlTab).setContainerItem(ModItems.particle_empty).setTextureName(RefStrings.MODID + ":particle_amat");
		particle_aschrab = new Item().setUnlocalizedName("particle_aschrab").setCreativeTab(MainRegistry.controlTab).setContainerItem(ModItems.particle_empty).setTextureName(RefStrings.MODID + ":particle_aschrab");
		particle_higgs = new Item().setUnlocalizedName("particle_higgs").setCreativeTab(MainRegistry.controlTab).setContainerItem(ModItems.particle_empty).setTextureName(RefStrings.MODID + ":particle_higgs");
		particle_muon = new Item().setUnlocalizedName("particle_muon").setCreativeTab(MainRegistry.controlTab).setContainerItem(ModItems.particle_empty).setTextureName(RefStrings.MODID + ":particle_muon");
		particle_tachyon = new Item().setUnlocalizedName("particle_tachyon").setCreativeTab(MainRegistry.controlTab).setContainerItem(ModItems.particle_empty).setTextureName(RefStrings.MODID + ":particle_tachyon");
		particle_strange = new Item().setUnlocalizedName("particle_strange").setCreativeTab(MainRegistry.controlTab).setContainerItem(ModItems.particle_empty).setTextureName(RefStrings.MODID + ":particle_strange");
		particle_dark = new Item().setUnlocalizedName("particle_dark").setCreativeTab(MainRegistry.controlTab).setContainerItem(ModItems.particle_empty).setTextureName(RefStrings.MODID + ":particle_dark");
		particle_sparkticle = new Item().setUnlocalizedName("particle_sparkticle").setCreativeTab(MainRegistry.controlTab).setContainerItem(ModItems.particle_empty).setTextureName(RefStrings.MODID + ":particle_sparkticle");
		//particle_digamma = new ItemDigamma(60).setUnlocalizedName("particle_digamma").setCreativeTab(MainRegistry.controlTab).setContainerItem(ModItems.particle_empty).setTextureName(RefStrings.MODID + ":particle_digamma");
		particle_lutece = new Item().setUnlocalizedName("particle_lutece").setCreativeTab(MainRegistry.controlTab).setContainerItem(ModItems.particle_empty).setTextureName(RefStrings.MODID + ":particle_lutece");
		particle_helium = new Item().setUnlocalizedName("particle_helium").setCreativeTab(MainRegistry.controlTab).setContainerItem(ModItems.particle_empty).setTextureName(RefStrings.MODID + ":particle_hydrogen");
		particle_astatine = new Item().setUnlocalizedName("particle_astatine").setCreativeTab(MainRegistry.controlTab).setContainerItem(ModItems.particle_empty).setTextureName(RefStrings.MODID + ":particle_hydrogen");
		particle_neutron = new Item().setUnlocalizedName("particle_neutron").setCreativeTab(MainRegistry.controlTab).setContainerItem(ModItems.particle_empty).setTextureName(RefStrings.MODID + ":particle_hydrogen");
		particle_beta = new Item().setUnlocalizedName("particle_beta").setCreativeTab(MainRegistry.controlTab).setContainerItem(ModItems.particle_empty).setTextureName(RefStrings.MODID + ":particle_hydrogen");
		//singularity_micro = new ItemDrop().setUnlocalizedName("singularity_micro").setCreativeTab(MainRegistry.controlTab).setContainerItem(ModItems.nuclear_waste).setTextureName(RefStrings.MODID + ":singularity_micro");

		//singularity = new ItemDrop().setUnlocalizedName("singularity").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab).setContainerItem(ModItems.nuclear_waste).setTextureName(RefStrings.MODID + ":singularity");
		//singularity_counter_resonant = new ItemDrop().setUnlocalizedName("singularity_counter_resonant").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab).setContainerItem(ModItems.nuclear_waste).setTextureName(RefStrings.MODID + ":singularity_alt");
		//singularity_super_heated = new ItemDrop().setUnlocalizedName("singularity_super_heated").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab).setContainerItem(ModItems.nuclear_waste).setTextureName(RefStrings.MODID + ":singularity_5");
		//black_hole = new ItemDrop().setUnlocalizedName("black_hole").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab).setContainerItem(ModItems.nuclear_waste).setTextureName(RefStrings.MODID + ":singularity_4");
		//singularity_spark = new ItemDrop().setUnlocalizedName("singularity_spark").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab).setContainerItem(ModItems.nuclear_waste).setTextureName(RefStrings.MODID + ":singularity_spark_alt");
		//pellet_antimatter = new ItemDrop().setUnlocalizedName("pellet_antimatter").setCreativeTab(MainRegistry.controlTab).setContainerItem(ModItems.cell_empty).setTextureName(RefStrings.MODID + ":pellet_antimatter");
		//crystal_xen = new ItemDrop().setUnlocalizedName("crystal_xen").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab).setTextureName(RefStrings.MODID + ":crystal_xen");

		stamp_stone_flat = new ItemStamp(128, StampType.FLAT).setUnlocalizedName("stamp_stone_flat").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab).setTextureName(RefStrings.MODID + ":stamp_stone_flat");
		stamp_stone_plate = new ItemStamp(128, StampType.PLATE).setUnlocalizedName("stamp_stone_plate").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab).setTextureName(RefStrings.MODID + ":stamp_stone_plate");
		stamp_stone_wire = new ItemStamp(128, StampType.WIRE).setUnlocalizedName("stamp_stone_wire").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab).setTextureName(RefStrings.MODID + ":stamp_stone_wire");
		stamp_stone_circuit = new ItemStamp(128, StampType.CIRCUIT).setUnlocalizedName("stamp_stone_circuit").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab).setTextureName(RefStrings.MODID + ":stamp_stone_circuit");

		stamp_iron_flat = new ItemStamp(256, StampType.FLAT).setUnlocalizedName("stamp_iron_flat").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab).setTextureName(RefStrings.MODID + ":stamp_iron_flat");
		stamp_iron_plate = new ItemStamp(256, StampType.PLATE).setUnlocalizedName("stamp_iron_plate").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab).setTextureName(RefStrings.MODID + ":stamp_iron_plate");
		stamp_iron_wire = new ItemStamp(256, StampType.WIRE).setUnlocalizedName("stamp_iron_wire").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab).setTextureName(RefStrings.MODID + ":stamp_iron_wire");
		stamp_iron_circuit = new ItemStamp(256, StampType.CIRCUIT).setUnlocalizedName("stamp_iron_circuit").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab).setTextureName(RefStrings.MODID + ":stamp_iron_circuit");

		stamp_steel_flat = new ItemStamp(768, StampType.FLAT).setUnlocalizedName("stamp_steel_flat").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab).setTextureName(RefStrings.MODID + ":stamp_steel_flat");
		stamp_steel_plate = new ItemStamp(768, StampType.PLATE).setUnlocalizedName("stamp_steel_plate").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab).setTextureName(RefStrings.MODID + ":stamp_steel_plate");
		stamp_steel_wire = new ItemStamp(768, StampType.WIRE).setUnlocalizedName("stamp_steel_wire").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab).setTextureName(RefStrings.MODID + ":stamp_steel_wire");
		stamp_steel_circuit = new ItemStamp(768, StampType.CIRCUIT).setUnlocalizedName("stamp_steel_circuit").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab).setTextureName(RefStrings.MODID + ":stamp_steel_circuit");

		stamp_titanium_flat = new ItemStamp(1024, StampType.FLAT).setUnlocalizedName("stamp_titanium_flat").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab).setTextureName(RefStrings.MODID + ":stamp_titanium_flat");
		stamp_titanium_plate = new ItemStamp(1024, StampType.PLATE).setUnlocalizedName("stamp_titanium_plate").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab).setTextureName(RefStrings.MODID + ":stamp_titanium_plate");
		stamp_titanium_wire = new ItemStamp(1024, StampType.WIRE).setUnlocalizedName("stamp_titanium_wire").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab).setTextureName(RefStrings.MODID + ":stamp_titanium_wire");
		stamp_titanium_circuit = new ItemStamp(1024, StampType.CIRCUIT).setUnlocalizedName("stamp_titanium_circuit").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab).setTextureName(RefStrings.MODID + ":stamp_titanium_circuit");

		stamp_obsidian_flat = new ItemStamp(2048, StampType.FLAT).setUnlocalizedName("stamp_obsidian_flat").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab).setTextureName(RefStrings.MODID + ":stamp_obsidian_flat");
		stamp_obsidian_plate = new ItemStamp(2048, StampType.PLATE).setUnlocalizedName("stamp_obsidian_plate").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab).setTextureName(RefStrings.MODID + ":stamp_obsidian_plate");
		stamp_obsidian_wire = new ItemStamp(2048, StampType.WIRE).setUnlocalizedName("stamp_obsidian_wire").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab).setTextureName(RefStrings.MODID + ":stamp_obsidian_wire");
		stamp_obsidian_circuit = new ItemStamp(2048, StampType.CIRCUIT).setUnlocalizedName("stamp_obsidian_circuit").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab).setTextureName(RefStrings.MODID + ":stamp_obsidian_circuit");
		//tool steel stamps
		stamp_desh_flat = new ItemStamp(0, StampType.FLAT).setUnlocalizedName("stamp_desh_flat").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab).setTextureName(RefStrings.MODID + ":stamp_desh_flat");
		stamp_desh_plate = new ItemStamp(0, StampType.PLATE).setUnlocalizedName("stamp_desh_plate").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab).setTextureName(RefStrings.MODID + ":stamp_desh_plate");
		stamp_desh_wire = new ItemStamp(0, StampType.WIRE).setUnlocalizedName("stamp_desh_wire").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab).setTextureName(RefStrings.MODID + ":stamp_desh_wire");
		stamp_desh_circuit = new ItemStamp(0, StampType.CIRCUIT).setUnlocalizedName("stamp_desh_circuit").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab).setTextureName(RefStrings.MODID + ":stamp_desh_circuit");

		stamp_357 = new ItemStamp(1000, StampType.C357).setUnlocalizedName("stamp_357").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab).setTextureName(RefStrings.MODID + ":stamp_357");
		stamp_44 = new ItemStamp(1000, StampType.C44).setUnlocalizedName("stamp_44").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab).setTextureName(RefStrings.MODID + ":stamp_44");
		stamp_9 = new ItemStamp(1000, StampType.C9).setUnlocalizedName("stamp_9").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab).setTextureName(RefStrings.MODID + ":stamp_9");
		stamp_50 = new ItemStamp(1000, StampType.C50).setUnlocalizedName("stamp_50").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab).setTextureName(RefStrings.MODID + ":stamp_50");
		stamp_book = new ItemStampBook().setUnlocalizedName("stamp_book").setMaxStackSize(1).setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":stamp_book");

		stamp_desh_357 = new ItemStamp(0, StampType.C357).setUnlocalizedName("stamp_desh_357").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab).setTextureName(RefStrings.MODID + ":stamp_357_desh");
		stamp_desh_44 = new ItemStamp(0, StampType.C44).setUnlocalizedName("stamp_desh_44").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab).setTextureName(RefStrings.MODID + ":stamp_44_desh");
		stamp_desh_9 = new ItemStamp(0, StampType.C9).setUnlocalizedName("stamp_desh_9").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab).setTextureName(RefStrings.MODID + ":stamp_9_desh");
		stamp_desh_50 = new ItemStamp(0, StampType.C50).setUnlocalizedName("stamp_desh_50").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab).setTextureName(RefStrings.MODID + ":stamp_50_desh");

		blades_steel = new ItemBlades(800).setUnlocalizedName("blades_steel").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab).setTextureName(RefStrings.MODID + ":blades_steel");
		blades_titanium = new ItemBlades(4000).setUnlocalizedName("blades_titanium").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab).setTextureName(RefStrings.MODID + ":blades_titanium");
		blades_advanced_alloy = new ItemBlades(12000).setUnlocalizedName("blades_advanced_alloy").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab).setTextureName(RefStrings.MODID + ":blades_advanced_alloy");
		blades_desh = new ItemBlades(0).setUnlocalizedName("blades_desh").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab).setTextureName(RefStrings.MODID + ":blades_desh");

		mold_base = new Item().setUnlocalizedName("mold_base").setCreativeTab(MainRegistry.controlTab).setTextureName(RefStrings.MODID + ":mold_base");
		mold = new ItemMold().setUnlocalizedName("mold").setCreativeTab(MainRegistry.controlTab);
		scraps = new ItemScraps().aot(Mats.MAT_BISMUTH, "scraps_bismuth").setUnlocalizedName("scraps").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":scraps");
		plate_cast = new ItemAutogen(MaterialShapes.CASTPLATE).aot(Mats.MAT_BISMUTH, "plate_cast_bismuth").setUnlocalizedName("plate_cast").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":plate_cast");
		plate_welded = new ItemAutogen(MaterialShapes.WELDEDPLATE).setUnlocalizedName("plate_welded").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":plate_welded");
		heavy_component = new ItemAutogen(MaterialShapes.HEAVY_COMPONENT).setUnlocalizedName("heavy_component").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":heavy_component");
		wire_fine = new ItemAutogen(MaterialShapes.WIRE)
				.aot(Mats.MAT_ALUMINIUM, "wire_aluminium").aot(Mats.MAT_COPPER, "wire_copper")
				.aot(Mats.MAT_MINGRADE, "wire_red_copper").aot(Mats.MAT_GOLD, "wire_gold")
				.aot(Mats.MAT_TUNGSTEN, "wire_tungsten").aot(Mats.MAT_ALLOY, "wire_advanced_alloy")
				.aot(Mats.MAT_CARBON, "wire_carbon").aot(Mats.MAT_SCHRABIDIUM, "wire_schrabidium")
				.aot(Mats.MAT_ZINC, "wire_zinc")
				.aot(Mats.MAT_MAGTUNG, "wire_magnetized_tungsten").setUnlocalizedName("wire_fine").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":wire_fine");
		wire_dense = new ItemAutogen(MaterialShapes.DENSEWIRE).setUnlocalizedName("wire_dense").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":wire_dense");

		part_lithium = new Item().setUnlocalizedName("part_lithium").setCreativeTab(MainRegistry.controlTab).setTextureName(RefStrings.MODID + ":part_lithium");
		part_beryllium = new Item().setUnlocalizedName("part_beryllium").setCreativeTab(MainRegistry.controlTab).setTextureName(RefStrings.MODID + ":part_beryllium");
		part_carbon = new Item().setUnlocalizedName("part_carbon").setCreativeTab(MainRegistry.controlTab).setTextureName(RefStrings.MODID + ":part_carbon");
		part_copper = new Item().setUnlocalizedName("part_copper").setCreativeTab(MainRegistry.controlTab).setTextureName(RefStrings.MODID + ":part_copper");
		part_plutonium = new Item().setUnlocalizedName("part_plutonium").setCreativeTab(MainRegistry.controlTab).setTextureName(RefStrings.MODID + ":part_plutonium");

		laser_crystal_co2 = new ItemFELCrystal(EnumWavelengths.IR).setUnlocalizedName("laser_crystal_co2").setCreativeTab(MainRegistry.controlTab).setTextureName(RefStrings.MODID + ":laser_crystal_co2");
		laser_crystal_bismuth = new ItemFELCrystal(EnumWavelengths.VISIBLE).setUnlocalizedName("laser_crystal_bismuth").setCreativeTab(MainRegistry.controlTab).setTextureName(RefStrings.MODID + ":laser_crystal_bismuth");
		laser_crystal_cmb = new ItemFELCrystal(EnumWavelengths.UV).setUnlocalizedName("laser_crystal_cmb").setCreativeTab(MainRegistry.controlTab).setTextureName(RefStrings.MODID + ":laser_crystal_cmb");
		laser_crystal_iron = new ItemFELCrystal(EnumWavelengths.XRAY).setUnlocalizedName("laser_crystal_iron").setCreativeTab(MainRegistry.controlTab).setTextureName(RefStrings.MODID + ":laser_crystal_iron");
		laser_crystal_dnt = new ItemFELCrystal(EnumWavelengths.GAMMA).setUnlocalizedName("laser_crystal_dnt").setCreativeTab(MainRegistry.controlTab).setTextureName(RefStrings.MODID + ":laser_crystal_dnt");
		//laser_crystal_digamma = new ItemFELCrystal(EnumWavelengths.DRX).setUnlocalizedName("laser_crystal_digamma").setCreativeTab(MainRegistry.controlTab).setTextureName(RefStrings.MODID + ":laser_crystal_digamma");

		thermo_element = new Item().setUnlocalizedName("thermo_element").setCreativeTab(MainRegistry.controlTab).setTextureName(RefStrings.MODID + ":thermo_element");
		catalytic_converter = new Item().setUnlocalizedName("catalytic_converter").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab).setTextureName(RefStrings.MODID + ":catalytic_converter");

		fuel_additive = new ItemEnumMulti(ItemEnums.EnumFuelAdditive.class, true, true).setUnlocalizedName("fuel_additive").setCreativeTab(MainRegistry.controlTab).setTextureName(RefStrings.MODID + ":fuel_additive");

		canister_empty = new ItemCustomLore().setUnlocalizedName("canister_empty").setCreativeTab(MainRegistry.controlTab).setTextureName(RefStrings.MODID + ":canister_empty");
		canister_full = new ItemCanister().setUnlocalizedName("canister_full").setCreativeTab(MainRegistry.controlTab).setContainerItem(ModItems.canister_empty).setTextureName(RefStrings.MODID + ":canister_empty");
		canister_napalm = new ItemCustomLore().setUnlocalizedName("canister_napalm").setCreativeTab(MainRegistry.controlTab).setContainerItem(ModItems.canister_empty).setTextureName(RefStrings.MODID + ":canister_napalm");
		gas_empty = new Item().setUnlocalizedName("gas_empty").setCreativeTab(MainRegistry.controlTab).setTextureName(RefStrings.MODID + ":gas_empty");
		gas_full = new ItemGasTank().setUnlocalizedName("gas_full").setCreativeTab(MainRegistry.controlTab).setContainerItem(ModItems.gas_empty).setTextureName(RefStrings.MODID + ":gas_empty");

		syringe_empty = new Item().setUnlocalizedName("syringe_empty").setFull3D().setCreativeTab(MainRegistry.consumableTab).setTextureName(RefStrings.MODID + ":syringe_empty");
		syringe_antidote = new ItemSyringe().setUnlocalizedName("syringe_antidote").setFull3D().setCreativeTab(MainRegistry.consumableTab).setTextureName(RefStrings.MODID + ":syringe_antidote");
		syringe_poison = new ItemSyringe().setUnlocalizedName("syringe_poison").setFull3D().setCreativeTab(MainRegistry.consumableTab).setTextureName(RefStrings.MODID + ":syringe_poison");
		//syringe_awesome = new ItemSyringe().setUnlocalizedName("syringe_awesome").setFull3D().setCreativeTab(MainRegistry.consumableTab).setTextureName(RefStrings.MODID + ":syringe_awesome");
		syringe_metal_empty = new Item().setUnlocalizedName("syringe_metal_empty").setFull3D().setCreativeTab(MainRegistry.consumableTab).setTextureName(RefStrings.MODID + ":syringe_metal_empty");
		syringe_metal_stimpak = new ItemSyringe().setUnlocalizedName("syringe_metal_stimpak").setFull3D().setCreativeTab(MainRegistry.consumableTab).setTextureName(RefStrings.MODID + ":syringe_metal_stimpak");
		tourniquet = new ItemSyringe().setUnlocalizedName("tourniquet").setFull3D().setCreativeTab(MainRegistry.consumableTab).setTextureName(RefStrings.MODID + ":tourniquet");
		syringe_metal_medx = new ItemSyringe().setUnlocalizedName("syringe_metal_medx").setFull3D().setCreativeTab(MainRegistry.consumableTab).setTextureName(RefStrings.MODID + ":syringe_metal_medx");
		syringe_metal_psycho = new ItemSyringe().setUnlocalizedName("syringe_metal_psycho").setFull3D().setCreativeTab(MainRegistry.consumableTab).setTextureName(RefStrings.MODID + ":syringe_metal_psycho");
		syringe_metal_super = new ItemSyringe().setUnlocalizedName("syringe_metal_super").setFull3D().setCreativeTab(MainRegistry.consumableTab).setTextureName(RefStrings.MODID + ":syringe_metal_super");
		//syringe_taint = new ItemSyringe().setUnlocalizedName("syringe_taint").setFull3D().setCreativeTab(MainRegistry.consumableTab).setTextureName(RefStrings.MODID + ":syringe_taint");
		syringe_mkunicorn = new ItemSyringe().setUnlocalizedName("syringe_mkunicorn").setFull3D().setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":syringe_mkunicorn");

		iv_empty = new ItemSimpleConsumable().setUseActionServer((stack, user) -> {
			if(user.hurtResistantTime <= 0) {
				ItemSimpleConsumable.giveSoundAndDecrement(stack, user, "hbm:item.syringe", new ItemStack(ModItems.iv_blood));
				user.attackEntityFrom(DamageSource.magic, 5F);
			}
		}).setUnlocalizedName("iv_empty").setCreativeTab(MainRegistry.consumableTab).setTextureName(RefStrings.MODID + ":iv_empty");

		iv_blood = new ItemSimpleConsumable()
			.setUseActionServer((stack, user) -> {
				// We only run on server side
				if (!user.worldObj.isRemote && user instanceof EntityPlayer) {
					EntityPlayer player = (EntityPlayer)user;

					// Cooldown key name
					String tagKey = "IVBloodLastUse";
					// Cooldown in ticks (5 seconds = 100 ticks)
					int cooldownTicks = 100;

					// Read NBT from player
					if (!player.getEntityData().hasKey(tagKey)) {
						player.getEntityData().setLong(tagKey, 0L);
					}
					long lastUse = player.getEntityData().getLong(tagKey);
					long current = player.worldObj.getTotalWorldTime();

					if (current - lastUse >= cooldownTicks) {
						ItemSimpleConsumable.addPotionEffect(
							user,
							HbmPotion.radaway,
							10, //msv
							0 //amplifier
						);
						// Cooldown has passed, allow use
						player.getEntityData().setLong(tagKey, current);

						// Do the heal & decrement logic
						ItemSimpleConsumable.giveSoundAndDecrement(stack, user, "hbm:item.radaway", new ItemStack(ModItems.iv_empty));
						user.heal(3F);
					} else {
						// Cooldown not yet ended — you can optionally send a message or just skip
						player.addChatMessage(new ChatComponentText("You cannot use this right now!"));
					}
				}
			})
			.setUnlocalizedName("iv_blood")
			.setCreativeTab(MainRegistry.consumableTab)
			.setTextureName(RefStrings.MODID + ":iv_blood");

		radaway = new ItemSimpleConsumable().setUseActionServer((stack, user) -> {

			if (!VersatileConfig.hasPotionSickness(user)) {

				ItemSimpleConsumable.giveSoundAndDecrement(
					stack,
					user,
					"hbm:item.radaway",
					new ItemStack(ModItems.iv_empty)
				);

				// Standard IV anti-rad treatment
				ItemSimpleConsumable.addPotionEffect(
					user,
					HbmPotion.radaway,
					140,
					0
				);

				user.addPotionEffect(
					new PotionEffect(Potion.hunger.id,
									 20 * 15,
									 0)
				);

				VersatileConfig.applyPotionSickness(user,
													1);
			}

			}).setUnlocalizedName("radaway")
			.setCreativeTab(MainRegistry.consumableTab)
			.setTextureName(RefStrings.MODID + ":radaway");


		prussian_blue_powder = new ItemSimpleConsumable().setUseActionServer((stack, user) -> {

				ItemSimpleConsumable.giveSoundAndDecrement(
					stack,
					user,
					"hbm:player.cough",
					null
				);

				// Weak oral contamination treatment
				ItemSimpleConsumable.addPotionEffect(
					user,
					HbmPotion.radaway,
					100,
					0
				);

				// No IV sickness for pill form, give hunger instead (you just ate raw powder)
				user.addPotionEffect(
					new PotionEffect(Potion.hunger.id,
									 20 * 20,
									 0)
				);

			}).setUnlocalizedName("prussian_blue_powder")
			.setCreativeTab(MainRegistry.consumableTab)
			.setTextureName(RefStrings.MODID + ":powder_lapis");

		prussian_blue_pill = new ItemSimpleConsumable().setUseActionServer((stack, user) -> {

				ItemSimpleConsumable.giveSoundAndDecrement(
					stack,
					user,
					"hbm:player.gulp",
					null
				);

				// Stronger oral contamination treatment
				ItemSimpleConsumable.addPotionEffect(
					user,
					HbmPotion.radaway,
					200,
					0
				);

			}).setUnlocalizedName("prussian_blue_pill")
			.setCreativeTab(MainRegistry.consumableTab)
			.setTextureName(RefStrings.MODID + ":pill_prussianblue");


		radaway_strong = new ItemSimpleConsumable().setUseActionServer((stack, user) -> {

			if (!VersatileConfig.hasPotionSickness(user)) {

				ItemSimpleConsumable.giveSoundAndDecrement(
					stack,
					user,
					"hbm:item.radaway",
					new ItemStack(ModItems.iv_empty)
				);

				// Stronger hospital-grade treatment
				ItemSimpleConsumable.addPotionEffect(
					user,
					HbmPotion.radaway,
					350,
					0
				);

				user.addPotionEffect(
					new PotionEffect(Potion.weakness.id,
									 20 * 10,
									 0)
				);

				VersatileConfig.applyPotionSickness(user,
													2);

			}
			}).setUnlocalizedName("radaway_strong")
			.setCreativeTab(MainRegistry.consumableTab)
			.setTextureName(RefStrings.MODID + ":radaway_strong");


		radaway_flush = new ItemSimpleConsumable().setUseActionServer((stack, user) -> {

			if (!VersatileConfig.hasPotionSickness(user)) {

				ItemSimpleConsumable.giveSoundAndDecrement(
					stack,
					user,
					"hbm:item.radaway",
					new ItemStack(ModItems.iv_empty)
				);

			// Emergency decon / aggressive treatment
			ItemSimpleConsumable.addPotionEffect(
				user,
				HbmPotion.radaway,
				500,
				1
			);

			user.addPotionEffect(
				new PotionEffect(Potion.hunger.id,
								 20 * 15,
								 1)
			);

			user.addPotionEffect(
				new PotionEffect(Potion.weakness.id,
								 20 * 12,
								 1)
			);

			VersatileConfig.applyPotionSickness(user,
												1);
		}

			}).setUnlocalizedName("radaway_flush")
			.setCreativeTab(MainRegistry.consumableTab)
			.setTextureName(RefStrings.MODID + ":radaway_flush");

		med_bag = new ItemSyringe().setUnlocalizedName("med_bag").setCreativeTab(MainRegistry.consumableTab).setTextureName(RefStrings.MODID + ":med_bag");
		radx = new ItemPill(0).setUnlocalizedName("radx").setCreativeTab(MainRegistry.consumableTab).setTextureName(RefStrings.MODID + ":radx");
		siox = new ItemPill(0).setUnlocalizedName("siox").setCreativeTab(MainRegistry.consumableTab).setTextureName(RefStrings.MODID + ":siox");
		pill_herbal = new ItemPill(0).setUnlocalizedName("pill_herbal").setCreativeTab(MainRegistry.consumableTab).setTextureName(RefStrings.MODID + ":pill_herbal");
		xanax = new ItemPill(0).setUnlocalizedName("xanax").setCreativeTab(MainRegistry.consumableTab).setTextureName(RefStrings.MODID + ":xanax_2");

		//animan = new ItemPill(0).setUnlocalizedName("animan").setCreativeTab(MainRegistry.consumableTab).setTextureName(RefStrings.MODID + ":animan");


		fmn = new ItemPill(0).setUnlocalizedName("fmn").setCreativeTab(MainRegistry.consumableTab).setTextureName(RefStrings.MODID + ":tablet");
		five_htp = new ItemPill(0).setUnlocalizedName("five_htp").setCreativeTab(MainRegistry.consumableTab).setTextureName(RefStrings.MODID + ":5htp");
		pill_iodine = new ItemPill(0).setUnlocalizedName("pill_iodine").setCreativeTab(MainRegistry.consumableTab).setTextureName(RefStrings.MODID + ":pill_iodine");
		plan_c = new ItemPill(0).setUnlocalizedName("plan_c").setCreativeTab(MainRegistry.consumableTab).setTextureName(RefStrings.MODID + ":plan_c");
		//pill_red = new ItemPill(0).setUnlocalizedName("pill_red").setCreativeTab(MainRegistry.consumableTab).setTextureName(RefStrings.MODID + ":pill_red");
		//stealth_boy = new ItemStarterKit().setUnlocalizedName("stealth_boy").setCreativeTab(MainRegistry.consumableTab).setTextureName(RefStrings.MODID + ":stealth_boy");
		gas_mask_filter = new ItemFilter().setUnlocalizedName("gas_mask_filter").setTextureName(RefStrings.MODID + ":gas_mask_filter");
		gas_mask_filter_mono = new ItemFilter().setUnlocalizedName("gas_mask_filter_mono").setTextureName(RefStrings.MODID + ":gas_mask_filter_mono");
		gas_mask_filter_combo = new ItemFilter().setUnlocalizedName("gas_mask_filter_combo").setTextureName(RefStrings.MODID + ":gas_mask_filter_combo");
		gas_mask_filter_rag = new ItemFilter().setUnlocalizedName("gas_mask_filter_rag").setTextureName(RefStrings.MODID + ":gas_mask_filter_rag");
		gas_mask_filter_piss = new ItemFilter().setUnlocalizedName("gas_mask_filter_piss").setTextureName(RefStrings.MODID + ":gas_mask_filter_piss");
		//jetpack_tank = new ItemSyringe().setUnlocalizedName("jetpack_tank").setMaxStackSize(1).setCreativeTab(MainRegistry.consumableTab).setTextureName(RefStrings.MODID + ":jetpack_tank");
		gun_kit_1 = new ItemSyringe().setUnlocalizedName("gun_kit_1").setMaxStackSize(16).setCreativeTab(MainRegistry.consumableTab).setTextureName(RefStrings.MODID + ":gun_kit_1");
		gun_kit_2 = new ItemSyringe().setUnlocalizedName("gun_kit_2").setMaxStackSize(16).setCreativeTab(MainRegistry.consumableTab).setTextureName(RefStrings.MODID + ":gun_kit_2");
		//cbt_device = new ItemSyringe().setUnlocalizedName("cbt_device").setMaxStackSize(1).setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":cbt_device");
		cigarette = new ItemCigarette().setUnlocalizedName("cigarette").setFull3D().setMaxStackSize(16).setCreativeTab(MainRegistry.consumableTab).setTextureName(RefStrings.MODID + ":cigarette");
		crackpipe = new ItemCigarette().setUnlocalizedName("crackpipe").setFull3D().setMaxStackSize(1).setCreativeTab(MainRegistry.consumableTab).setTextureName(RefStrings.MODID + ":crackpipe");
		bdcl = new ItemBDCL().setUnlocalizedName("bdcl").setCreativeTab(MainRegistry.consumableTab).setTextureName(RefStrings.MODID + ":bdcl");


		galaxygas = new Itemgalaxygas(0).setUnlocalizedName("galaxygas").setCreativeTab(MainRegistry.consumableTab).setTextureName(RefStrings.MODID + ":galaxygas");

		lox_tank = new ItemSyringe().setUnlocalizedName("lox_tank").setMaxStackSize(1).setCreativeTab(MainRegistry.consumableTab).setTextureName(RefStrings.MODID + ":lox_tank");

		attachment_mask = new ItemModGasmask().setUnlocalizedName("attachment_mask").setTextureName(RefStrings.MODID + ":attachment_mask");
		attachment_mask_mono = new ItemModGasmask().setUnlocalizedName("attachment_mask_mono").setTextureName(RefStrings.MODID + ":attachment_mask_mono");
		//back_tesla = new ItemModTesla().setUnlocalizedName("back_tesla").setTextureName(RefStrings.MODID + ":back_tesla");
		servo_set = new ItemModServos().setUnlocalizedName("servo_set").setTextureName(RefStrings.MODID + ":servo_set");
		servo_set_desh = new ItemModServos().setUnlocalizedName("servo_set_desh").setTextureName(RefStrings.MODID + ":servo_set_desh");
		pads_rubber = new ItemModPads(0.25F).setUnlocalizedName("pads_rubber").setTextureName(RefStrings.MODID + ":pads_rubber");
		//pads_slime = new ItemModPads(0.25F).setUnlocalizedName("pads_slime").setTextureName(RefStrings.MODID + ":pads_slime");
		//pads_static = new ItemModPads(0.75F).setUnlocalizedName("pads_static").setTextureName(RefStrings.MODID + ":pads_static");
		cladding_paint = new ItemModCladding(0.15).setUnlocalizedName("cladding_paint").setTextureName(RefStrings.MODID + ":cladding_paint");
		cladding_rubber = new ItemModCladding(0.05).setUnlocalizedName("cladding_rubber").setTextureName(RefStrings.MODID + ":cladding_rubber");
		cladding_lead = new ItemModCladding(0.5).setUnlocalizedName("cladding_lead").setTextureName(RefStrings.MODID + ":cladding_lead");
		//cladding_desh = new ItemModCladding(0.2).setUnlocalizedName("cladding_desh").setTextureName(RefStrings.MODID + ":cladding_desh");
		//cladding_ghiorsium = new ItemModCladding(0.5).setUnlocalizedName("cladding_ghiorsium").setTextureName(RefStrings.MODID + ":cladding_ghiorsium");
		cladding_iron = new ItemModIron().setUnlocalizedName("cladding_iron").setTextureName(RefStrings.MODID + ":cladding_iron");
		cladding_obsidian = new ItemModObsidian().setUnlocalizedName("cladding_obsidian").setTextureName(RefStrings.MODID + ":cladding_obsidian");
		insert_kevlar = new ItemModInsert(5, 1F, 0.97F, 1F, 1F).setUnlocalizedName("insert_kevlar").setTextureName(RefStrings.MODID + ":insert_kevlar");
		insert_sapi = new ItemModInsert(6, 1F, 0.95F, 1F, 0.95F).setUnlocalizedName("insert_sapi").setTextureName(RefStrings.MODID + ":insert_sapi");
		insert_esapi = new ItemModInsert(7, 1F, 0.90F, 1F, 0.93F).setUnlocalizedName("insert_esapi").setTextureName(RefStrings.MODID + ":insert_esapi");
		insert_xsapi = new ItemModInsert(8, 1F, 0.85F, 1F, 0.91F).setUnlocalizedName("insert_xsapi").setTextureName(RefStrings.MODID + ":insert_xsapi");
		insert_steel = new ItemModInsert(50, 1F, 0.96F, 1F, 0.88F).setUnlocalizedName("insert_steel").setTextureName(RefStrings.MODID + ":insert_steel");
		//insert_du = new ItemModInsert(1500, 0.9F, 0.85F, 0.5F, 0.9F).setUnlocalizedName("insert_du").setTextureName(RefStrings.MODID + ":insert_du");
		//insert_polonium = new ItemModInsert(500, 0.9F, 1F, 0.95F, 0.9F).setUnlocalizedName("insert_polonium").setTextureName(RefStrings.MODID + ":insert_polonium");
		//insert_ghiorsium = new ItemModInsert(2000, 0.8F, 0.75F, 0.35F, 0.9F).setUnlocalizedName("insert_ghiorsium").setTextureName(RefStrings.MODID + ":insert_ghiorsium");
		//insert_cmb = new ItemModInsert(9999, 0.7F, 0.9F, 0.4F, 1F).withCorrosionProtection().setUnlocalizedName("insert_cmb").setTextureName(RefStrings.MODID + ":insert_cmb");
		//insert_era = new ItemModInsert(25, 0.5F, 1F, 0.25F, 1F).setUnlocalizedName("insert_era").setTextureName(RefStrings.MODID + ":insert_era");
		//insert_yharonite = new ItemModInsert(9999, 0.01F, 1F, 1F, 1F).withCorrosionProtection().setUnlocalizedName("insert_yharonite").setTextureName(RefStrings.MODID + ":insert_yharonite");
		//insert_doxium = new ItemModInsert(9999, 5.0F, 1F, 1F, 1F).setUnlocalizedName("insert_doxium").setTextureName(RefStrings.MODID + ":insert_doxium");
		//armor_polish = new ItemModPolish().setUnlocalizedName("armor_polish").setTextureName(RefStrings.MODID + ":armor_polish");
		//bandaid = new ItemModBandaid().setUnlocalizedName("bandaid").setTextureName(RefStrings.MODID + ":bandaid");
		//serum = new ItemModSerum().setUnlocalizedName("serum").setTextureName(RefStrings.MODID + ":serum");
		//quartz_plutonium = new ItemModQuartz().setUnlocalizedName("quartz_plutonium").setTextureName(RefStrings.MODID + ":quartz_plutonium");
		//morning_glory = new ItemModMorningGlory().setUnlocalizedName("morning_glory").setTextureName(RefStrings.MODID + ":morning_glory");
		lodestone = new ItemModLodestone(5).setUnlocalizedName("lodestone").setTextureName(RefStrings.MODID + ":lodestone");
		horseshoe_magnet = new ItemModLodestone(8).setUnlocalizedName("horseshoe_magnet").setTextureName(RefStrings.MODID + ":horseshoe_magnet");
		industrial_magnet = new ItemModLodestone(12).setUnlocalizedName("industrial_magnet").setTextureName(RefStrings.MODID + ":industrial_magnet");
		//bathwater = new ItemModBathwater().setUnlocalizedName("bathwater").setTextureName(RefStrings.MODID + ":bathwater");
		//bathwater_mk2 = new ItemModBathwater().setUnlocalizedName("bathwater_mk2").setTextureName(RefStrings.MODID + ":bathwater_mk2");
		//spider_milk = new ItemModMilk().setUnlocalizedName("spider_milk").setTextureName(RefStrings.MODID + ":spider_milk");
		//ink = new ItemModInk().setUnlocalizedName("ink").setTextureName(RefStrings.MODID + ":ink");
		//heart_piece = new ItemModHealth(5F).setUnlocalizedName("heart_piece").setTextureName(RefStrings.MODID + ":heart_piece");
		//heart_container = new ItemModHealth(20F).setUnlocalizedName("heart_container").setTextureName(RefStrings.MODID + ":heart_container");
		//heart_booster = new ItemModHealth(40F).setUnlocalizedName("heart_booster").setTextureName(RefStrings.MODID + ":heart_booster");
		//heart_fab = new ItemModHealth(60F).setUnlocalizedName("heart_fab").setTextureName(RefStrings.MODID + ":heart_fab");
		//black_diamond = new ItemModHealth(40F).setUnlocalizedName("black_diamond").setTextureName(RefStrings.MODID + ":black_diamond");
		//wd40 = new ItemModWD40().setUnlocalizedName("wd40").setTextureName(RefStrings.MODID + ":wd40");
		//scrumpy = new ItemModRevive(1).setUnlocalizedName("scrumpy").setTextureName(RefStrings.MODID + ":scrumpy");
		//wild_p = new ItemModRevive(3).setUnlocalizedName("wild_p").setTextureName(RefStrings.MODID + ":wild_p");
		//fabsols_vodka = new ItemModRevive(9999).setUnlocalizedName("fabsols_vodka").setTextureName(RefStrings.MODID + ":fabsols_vodka");
		//shackles = new ItemModShackles().setUnlocalizedName("shackles").setTextureName(RefStrings.MODID + ":shackles");
		//injector_5htp = new ItemModAuto().setUnlocalizedName("injector_5htp").setTextureName(RefStrings.MODID + ":injector_5htp");
		//injector_knife = new ItemModKnife().setUnlocalizedName("injector_knife").setTextureName(RefStrings.MODID + ":injector_knife");
		//medal_liquidator = new ItemModMedal().setUnlocalizedName("medal_liquidator").setTextureName(RefStrings.MODID + ":medal_liquidator");
		//v1 = new ItemModV1().setUnlocalizedName("v1").setTextureName(RefStrings.MODID + ":v1");
		//protection_charm = new ItemModCharm().setUnlocalizedName("protection_charm").setTextureName(RefStrings.MODID + ":protection_charm");
		//meteor_charm = new ItemModCharm().setUnlocalizedName("meteor_charm").setTextureName(RefStrings.MODID + ":meteor_charm");
		neutrino_lens = new ItemModLens().setUnlocalizedName("neutrino_lens").setTextureName(RefStrings.MODID + ":neutrino_lens");
		gas_tester = new ItemModSensor().setUnlocalizedName("gas_tester").setTextureName(RefStrings.MODID + ":gas_tester");
		//defuser_gold = new ItemModDefuser().setUnlocalizedName("defuser_gold").setTextureName(RefStrings.MODID + ":defuser_gold");
		//ballistic_gauntlet = new ItemModTwoKick().setUnlocalizedName("ballistic_gauntlet").setTextureName(RefStrings.MODID + ":ballistic_gauntlet");
		night_vision = new ItemModNightVision().setUnlocalizedName("night_vision").setTextureName(RefStrings.MODID + ":night_vision");
		card_aos = new ItemModCard().setUnlocalizedName("card_aos").setTextureName(RefStrings.MODID + ":card_aos");
		card_qos = new ItemModCard().setUnlocalizedName("card_qos").setTextureName(RefStrings.MODID + ":card_qos");
		//australium_iii = new ItemModShield(25F).setUnlocalizedName("australium_iii").setTextureName(RefStrings.MODID + ":australium_iii");
		armor_battery = new ItemModBattery(1.25D).setUnlocalizedName("armor_battery").setTextureName(RefStrings.MODID + ":armor_battery");
		armor_battery_mk2 = new ItemModBattery(1.5D).setUnlocalizedName("armor_battery_mk2").setTextureName(RefStrings.MODID + ":armor_battery_mk2");
		armor_battery_mk3 = new ItemModBattery(2D).setUnlocalizedName("armor_battery_mk3").setTextureName(RefStrings.MODID + ":armor_battery_mk3");

		cap_nuka = new Item().setUnlocalizedName("cap_nuka").setCreativeTab(MainRegistry.consumableTab).setTextureName(RefStrings.MODID + ":cap_nuka");
		cap_quantum = new Item().setUnlocalizedName("cap_quantum").setCreativeTab(MainRegistry.consumableTab).setTextureName(RefStrings.MODID + ":cap_quantum");
		cap_sparkle = new Item().setUnlocalizedName("cap_sparkle").setCreativeTab(MainRegistry.consumableTab).setTextureName(RefStrings.MODID + ":cap_sparkle");
		cap_rad = new Item().setUnlocalizedName("cap_rad").setCreativeTab(MainRegistry.consumableTab).setTextureName(RefStrings.MODID + ":cap_rad");
		cap_korl = new Item().setUnlocalizedName("cap_korl").setCreativeTab(MainRegistry.consumableTab).setTextureName(RefStrings.MODID + ":cap_korl");
		cap_fritz = new Item().setUnlocalizedName("cap_fritz").setCreativeTab(MainRegistry.consumableTab).setTextureName(RefStrings.MODID + ":cap_fritz");
		ring_pull = new Item().setUnlocalizedName("ring_pull").setCreativeTab(MainRegistry.consumableTab).setTextureName(RefStrings.MODID + ":ring_pull");
		hard_drive = new Item().setUnlocalizedName("hard_drive").setMaxStackSize(64).setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":votv_e");


		can_empty = new Item().setUnlocalizedName("can_empty").setCreativeTab(MainRegistry.consumableTab).setTextureName(RefStrings.MODID + ":can_empty");
		can_smart = new ItemEnergy().makeCan().setUnlocalizedName("can_smart").setTextureName(RefStrings.MODID + ":can_smart");
		can_creature = new ItemEnergy().makeCan().setUnlocalizedName("can_creature").setTextureName(RefStrings.MODID + ":can_creature");
		can_redbomb = new ItemEnergy().makeCan().setUnlocalizedName("can_redbomb").setTextureName(RefStrings.MODID + ":can_redbomb");
		can_mrsugar = new ItemEnergy().makeCan().setUnlocalizedName("can_mrsugar").setTextureName(RefStrings.MODID + ":can_mrsugar");
		can_overcharge = new ItemEnergy().makeCan().setUnlocalizedName("can_overcharge").setTextureName(RefStrings.MODID + ":can_overcharge");
		can_luna = new ItemEnergy().makeCan().setUnlocalizedName("can_luna").setTextureName(RefStrings.MODID + ":can_luna");
		can_bepis = new ItemEnergy().makeCan().setUnlocalizedName("can_bepis").setTextureName(RefStrings.MODID + ":can_bepis");
		can_breen = new ItemEnergy().makeCan().setUnlocalizedName("can_breen").setTextureName(RefStrings.MODID + ":can_breen");
		can_mug = new ItemEnergy().makeCan().setUnlocalizedName("can_mug").setTextureName(RefStrings.MODID + ":can_mug");
		bottle_empty = new Item().setUnlocalizedName("bottle_empty").setTextureName(RefStrings.MODID + ":bottle_empty");
		//bottle_nuka = new ItemEnergy().makeBottle(bottle_empty, cap_nuka).setUnlocalizedName("bottle_nuka").setTextureName(RefStrings.MODID + ":bottle_nuka");
		//bottle_cherry = new ItemEnergy().makeBottle(bottle_empty, cap_nuka).setUnlocalizedName("bottle_cherry").setContainerItem(ModItems.bottle_empty).setTextureName(RefStrings.MODID + ":bottle_cherry");
		//bottle_quantum = new ItemEnergy().makeBottle(bottle_empty, cap_quantum).setUnlocalizedName("bottle_quantum").setContainerItem(ModItems.bottle_empty).setTextureName(RefStrings.MODID + ":bottle_quantum");
		//bottle_sparkle = new ItemEnergy().makeBottle(bottle_empty, cap_sparkle).setUnlocalizedName("bottle_sparkle").setContainerItem(ModItems.bottle_empty).setTextureName(RefStrings.MODID + ":bottle_sparkle");
		//bottle_rad = new ItemEnergy().makeBottle(bottle_empty, cap_rad).setUnlocalizedName("bottle_rad").setContainerItem(ModItems.bottle_empty).setTextureName(RefStrings.MODID + ":bottle_rad");
		//bottle2_empty = new Item().setUnlocalizedName("bottle2_empty").setTextureName(RefStrings.MODID + ":bottle2_empty");
		//bottle2_korl = new ItemEnergy().makeBottle(bottle2_empty, cap_korl).setUnlocalizedName("bottle2_korl").setContainerItem(ModItems.bottle2_empty).setTextureName(RefStrings.MODID + ":bottle2_korl");
		//bottle2_fritz = new ItemEnergy().makeBottle(bottle2_empty, cap_fritz).setUnlocalizedName("bottle2_fritz").setContainerItem(ModItems.bottle2_empty).setTextureName(RefStrings.MODID + ":bottle2_fritz");
		//bottle2_korl_special = new ItemEnergy().makeBottle(bottle2_empty, cap_korl).setUnlocalizedName("bottle2_korl_special").setContainerItem(ModItems.bottle2_empty).setTextureName(RefStrings.MODID + ":bottle2_korl");
		//bottle2_fritz_special = new ItemEnergy().makeBottle(bottle2_empty, cap_fritz).setUnlocalizedName("bottle2_fritz_special").setContainerItem(ModItems.bottle2_empty).setTextureName(RefStrings.MODID + ":bottle2_fritz");
		//flask_infusion = new ItemFlask().setUnlocalizedName("flask_infusion").setCreativeTab(MainRegistry.consumableTab).setTextureName(RefStrings.MODID + ":flask");
		chocolate_milk = new ItemEnergy().setUnlocalizedName("chocolate_milk").setTextureName(RefStrings.MODID + ":chocolate_milk");
		coffee = new ItemEnergy().setUnlocalizedName("coffee").setTextureName(RefStrings.MODID + ":coffee");
		coffee_radium = new ItemEnergy().setUnlocalizedName("coffee_radium").setTextureName(RefStrings.MODID + ":coffee_radium");
		//chocolate = new ItemPill(0).setUnlocalizedName("chocolate").setCreativeTab(MainRegistry.consumableTab).setTextureName(RefStrings.MODID + ":chocolate");

		glass_smilk = new ItemEnergy().makeGlass().setUnlocalizedName("glass_smilk").setTextureName(RefStrings.MODID + ":glass_smilk");
		glass_empty = new Item().setUnlocalizedName("glass_empty").setCreativeTab(MainRegistry.consumableTab).setTextureName(RefStrings.MODID + ":glass_empty");
		strawberry = new ItemSeed(5, 2, ModBlocks.crop_strawberry).setUnlocalizedName("strawberry").setCreativeTab(MainRegistry.consumableTab).setTextureName(RefStrings.MODID + ":strawberry");
		bean_raw = new ItemBeed(1, 2, ModBlocks.crop_coffee).setUnlocalizedName("bean_raw").setCreativeTab(MainRegistry.consumableTab).setTextureName(RefStrings.MODID + ":coffeebeanraw");
		powder_coffee = new ItemCustomLore().setUnlocalizedName("powder_coffee").setCreativeTab(MainRegistry.consumableTab).setTextureName(RefStrings.MODID + ":powder_coffee");
		bean_roast = new Item().setUnlocalizedName("bean_roast").setCreativeTab(MainRegistry.consumableTab).setTextureName(RefStrings.MODID + ":coffeebeanroast");
		cmug_empty = new Item().setUnlocalizedName("cmug_empty").setCreativeTab(MainRegistry.consumableTab).setTextureName(RefStrings.MODID + ":mug_empty");
		teaseeds = new ItemTeed(0, 0, ModBlocks.crop_tea).setUnlocalizedName("teaseeds").setCreativeTab(MainRegistry.consumableTab).setTextureName(RefStrings.MODID + ":teaseeds");
		tea_leaf = new Item().setUnlocalizedName("tea_leaf").setCreativeTab(MainRegistry.consumableTab).setTextureName(RefStrings.MODID + ":tea_leaf");
		teacup_empty = new Item().setUnlocalizedName("teacup_empty").setCreativeTab(MainRegistry.consumableTab).setTextureName(RefStrings.MODID + ":teacup");
		teacup = new ItemEnergy().makeGlass().setUnlocalizedName("teacup").setCreativeTab(MainRegistry.consumableTab).setTextureName(RefStrings.MODID + ":teacup_full");
		bottle_honey = new ItemEnergy().makeGlass().setUnlocalizedName("bottle_honey").setCreativeTab(MainRegistry.consumableTab).setTextureName(RefStrings.MODID + ":bottle_honey");

		canned_conserve = (ItemEnumMulti) new ItemConserve().setUnlocalizedName("canned_conserve").setCreativeTab(MainRegistry.consumableTab).setTextureName(RefStrings.MODID + ":canned");
		can_key = new Item().setUnlocalizedName("can_key").setCreativeTab(MainRegistry.consumableTab).setTextureName(RefStrings.MODID + ":can_key");

		boat_rubber = new ItemBoatRubber().setUnlocalizedName("boat_rubber").setTextureName(RefStrings.MODID + ":boat_rubber");
		cart = new ItemModMinecart().setUnlocalizedName("cart");
		train = new ItemTrain().setUnlocalizedName("train");
		drone = new ItemDrone().setUnlocalizedName("drone");

		coin_creeper = new ItemCustomLore().setRarity(EnumRarity.uncommon).setUnlocalizedName("coin_creeper").setCreativeTab(MainRegistry.consumableTab).setTextureName(RefStrings.MODID + ":coin_creeper");
		coin_radiation = new ItemCustomLore().setRarity(EnumRarity.uncommon).setUnlocalizedName("coin_radiation").setCreativeTab(MainRegistry.consumableTab).setTextureName(RefStrings.MODID + ":coin_radiation");
		coin_maskman = new ItemCustomLore().setRarity(EnumRarity.uncommon).setUnlocalizedName("coin_maskman").setCreativeTab(MainRegistry.consumableTab).setTextureName(RefStrings.MODID + ":coin_maskman");
		coin_worm = new ItemCustomLore().setRarity(EnumRarity.uncommon).setUnlocalizedName("coin_worm").setCreativeTab(MainRegistry.consumableTab).setTextureName(RefStrings.MODID + ":coin_worm");
		coin_ufo = new ItemCustomLore().setRarity(EnumRarity.uncommon).setUnlocalizedName("coin_ufo").setCreativeTab(MainRegistry.consumableTab).setTextureName(RefStrings.MODID + ":coin_ufo");
		coin_airliner = new ItemCustomLore().setRarity(EnumRarity.uncommon).setUnlocalizedName("coin_airliner").setCreativeTab(MainRegistry.consumableTab).setTextureName(RefStrings.MODID + ":coin_airliner");

		rod_empty = new Item().setUnlocalizedName("rod_empty").setCreativeTab(MainRegistry.controlTab).setTextureName(RefStrings.MODID + ":rod_empty");
		rod = (ItemEnumMulti) new ItemBreedingRod().setUnlocalizedName("rod").setContainerItem(ModItems.rod_empty).setCreativeTab(MainRegistry.controlTab);
		rod_dual_empty = new Item().setUnlocalizedName("rod_dual_empty").setCreativeTab(MainRegistry.controlTab).setTextureName(RefStrings.MODID + ":rod_dual_empty");
		rod_dual = (ItemEnumMulti) new ItemBreedingRod().setUnlocalizedName("rod_dual").setContainerItem(ModItems.rod_dual_empty).setCreativeTab(MainRegistry.controlTab);
		rod_quad_empty = new Item().setUnlocalizedName("rod_quad_empty").setCreativeTab(MainRegistry.controlTab).setTextureName(RefStrings.MODID + ":rod_quad_empty");
		rod_quad = (ItemEnumMulti) new ItemBreedingRod().setUnlocalizedName("rod_quad").setContainerItem(ModItems.rod_quad_empty).setCreativeTab(MainRegistry.controlTab);

		rod_zirnox_empty = new Item().setUnlocalizedName("rod_zirnox_empty").setMaxStackSize(64).setCreativeTab(MainRegistry.controlTab).setTextureName(RefStrings.MODID + ":rod_zirnox_empty");
		//rod_zirnox_natural_uranium_fuel = new ItemZirnoxRodDeprecated(250000, 30).setUnlocalizedName("rod_zirnox_natural_uranium_fuel").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab).setContainerItem(ModItems.rod_zirnox_empty).setTextureName(RefStrings.MODID + ":rod_zirnox_natural_uranium_fuel");
		//rod_zirnox_uranium_fuel = new ItemZirnoxRodDeprecated(200000, 50).setUnlocalizedName("rod_zirnox_uranium_fuel").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab).setContainerItem(ModItems.rod_zirnox_empty).setTextureName(RefStrings.MODID + ":rod_zirnox_uranium_fuel");
		//rod_zirnox_th232 = new ItemZirnoxBreedingRod(20000, 0).setUnlocalizedName("rod_zirnox_th232").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab).setTextureName(RefStrings.MODID + ":rod_zirnox_th232");
		//rod_zirnox_thorium_fuel = new ItemZirnoxRodDeprecated(200000, 40).setUnlocalizedName("rod_zirnox_thorium_fuel").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab).setTextureName(RefStrings.MODID + ":rod_zirnox_thorium_fuel");
		//rod_zirnox_mox_fuel = new ItemZirnoxRodDeprecated(165000, 75).setUnlocalizedName("rod_zirnox_mox_fuel").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab).setTextureName(RefStrings.MODID + ":rod_zirnox_mox_fuel");
		//rod_zirnox_plutonium_fuel = new ItemZirnoxRodDeprecated(175000, 65).setUnlocalizedName("rod_zirnox_plutonium_fuel").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab).setTextureName(RefStrings.MODID + ":rod_zirnox_plutonium_fuel");
		//rod_zirnox_u233_fuel = new ItemZirnoxRodDeprecated(150000, 100).setUnlocalizedName("rod_zirnox_u233_fuel").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab).setTextureName(RefStrings.MODID + ":rod_zirnox_u233_fuel");
		//rod_zirnox_u235_fuel = new ItemZirnoxRodDeprecated(165000, 85).setUnlocalizedName("rod_zirnox_u235_fuel").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab).setTextureName(RefStrings.MODID + ":rod_zirnox_u235_fuel");
		//rod_zirnox_les_fuel = new ItemZirnoxRodDeprecated(150000, 150).setUnlocalizedName("rod_zirnox_les_fuel").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab).setTextureName(RefStrings.MODID + ":rod_zirnox_les_fuel");
		//rod_zirnox_lithium = new ItemZirnoxBreedingRod(20000, 0).setUnlocalizedName("rod_zirnox_lithium").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab).setTextureName(RefStrings.MODID + ":rod_zirnox_lithium");
		rod_zirnox_tritium = new Item().setUnlocalizedName("rod_zirnox_tritium").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab).setContainerItem(ModItems.rod_zirnox_empty).setTextureName(RefStrings.MODID + ":rod_zirnox_tritium");
		//rod_zirnox_zfb_mox = new ItemZirnoxRodDeprecated(50000, 35).setUnlocalizedName("rod_zirnox_zfb_mox").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab).setTextureName(RefStrings.MODID + ":rod_zirnox_zfb_mox");
		rod_zirnox = (ItemEnumMulti) new ItemZirnoxRod().setUnlocalizedName("rod_zirnox").setCreativeTab(MainRegistry.controlTab).setTextureName(RefStrings.MODID + ":rod_zirnox");

		full_drive = (ItemEnumMulti) new ItemVOTVdrive().setUnlocalizedName("hard_drive_full").setCreativeTab(MainRegistry.controlTab).setTextureName(RefStrings.MODID + ":votv_f");


		rod_zirnox_natural_uranium_fuel_depleted = new Item().setUnlocalizedName("rod_zirnox_natural_uranium_fuel_depleted").setCreativeTab(MainRegistry.controlTab).setContainerItem(ModItems.rod_zirnox_empty).setTextureName(RefStrings.MODID + ":rod_zirnox_uranium_fuel_depleted");
		rod_zirnox_uranium_fuel_depleted = new Item().setUnlocalizedName("rod_zirnox_uranium_fuel_depleted").setCreativeTab(MainRegistry.controlTab).setContainerItem(ModItems.rod_zirnox_empty).setTextureName(RefStrings.MODID + ":rod_zirnox_uranium_fuel_depleted");
		rod_zirnox_thorium_fuel_depleted = new Item().setUnlocalizedName("rod_zirnox_thorium_fuel_depleted").setCreativeTab(MainRegistry.controlTab).setContainerItem(ModItems.rod_zirnox_empty).setTextureName(RefStrings.MODID + ":rod_zirnox_thorium_fuel_depleted");
		rod_zirnox_mox_fuel_depleted = new Item().setUnlocalizedName("rod_zirnox_mox_fuel_depleted").setCreativeTab(MainRegistry.controlTab).setContainerItem(ModItems.rod_zirnox_empty).setTextureName(RefStrings.MODID + ":rod_zirnox_mox_fuel_depleted");
		rod_zirnox_plutonium_fuel_depleted =  new Item().setUnlocalizedName("rod_zirnox_plutonium_fuel_depleted").setCreativeTab(MainRegistry.controlTab).setContainerItem(ModItems.rod_zirnox_empty).setTextureName(RefStrings.MODID + ":rod_zirnox_plutonium_fuel_depleted");
		rod_zirnox_u233_fuel_depleted = new Item().setUnlocalizedName("rod_zirnox_u233_fuel_depleted").setCreativeTab(MainRegistry.controlTab).setContainerItem(ModItems.rod_zirnox_empty).setTextureName(RefStrings.MODID + ":rod_zirnox_u233_fuel_depleted");
		rod_zirnox_u235_fuel_depleted = new Item().setUnlocalizedName("rod_zirnox_u235_fuel_depleted").setCreativeTab(MainRegistry.controlTab).setContainerItem(ModItems.rod_zirnox_empty).setTextureName(RefStrings.MODID + ":rod_zirnox_u235_fuel_depleted");
		rod_zirnox_les_fuel_depleted = new Item().setUnlocalizedName("rod_zirnox_les_fuel_depleted").setCreativeTab(MainRegistry.controlTab).setContainerItem(ModItems.rod_zirnox_empty).setTextureName(RefStrings.MODID + ":rod_zirnox_les_fuel_depleted");
		rod_zirnox_zfb_mox_depleted = new Item().setUnlocalizedName("rod_zirnox_zfb_mox_depleted").setCreativeTab(MainRegistry.controlTab).setContainerItem(ModItems.rod_zirnox_empty).setTextureName(RefStrings.MODID + ":rod_zirnox_zfb_mox_depleted");

		waste_natural_uranium = new ItemDepletedFuel().setUnlocalizedName("waste_natural_uranium").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":waste_uranium");
		waste_uranium = new ItemDepletedFuel().setUnlocalizedName("waste_uranium").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":waste_uranium");
		waste_thorium = new ItemDepletedFuel().setUnlocalizedName("waste_thorium").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":waste_thorium");
		waste_mox = new ItemDepletedFuel().setUnlocalizedName("waste_mox").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":waste_mox");
		waste_plutonium = new ItemDepletedFuel().setUnlocalizedName("waste_plutonium").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":waste_plutonium");
		waste_u233 = new ItemDepletedFuel().setUnlocalizedName("waste_u233").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":waste_uranium");
		waste_u235 = new ItemDepletedFuel().setUnlocalizedName("waste_u235").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":waste_uranium");
		waste_schrabidium = new ItemDepletedFuel().setUnlocalizedName("waste_schrabidium").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":waste_schrabidium");
		waste_zfb_mox = new ItemDepletedFuel().setUnlocalizedName("waste_zfb_mox").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":waste_zfb_mox");

		waste_plate_u233 = new ItemDepletedFuel().setUnlocalizedName("waste_plate_u233").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":waste_plate_uranium");
		waste_plate_u235 = new ItemDepletedFuel().setUnlocalizedName("waste_plate_u235").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":waste_plate_uranium");
		waste_plate_mox = new ItemDepletedFuel().setUnlocalizedName("waste_plate_mox").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":waste_plate_mox");
		waste_plate_pu239 = new ItemDepletedFuel().setUnlocalizedName("waste_plate_pu239").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":waste_plate_mox");
		waste_plate_sa326 = new ItemDepletedFuel().setUnlocalizedName("waste_plate_sa326").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":waste_plate_sa326");
		waste_plate_ra226be = new ItemDepletedFuel().setUnlocalizedName("waste_plate_ra226be").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":waste_plate_ra226be");
		waste_plate_pu238be = new ItemDepletedFuel().setUnlocalizedName("waste_plate_pu238be").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":waste_plate_pu238be");

		pile_rod_uranium = new ItemPileRod().setUnlocalizedName("pile_rod_uranium").setCreativeTab(MainRegistry.controlTab).setTextureName(RefStrings.MODID + ":pile_rod_uranium");
		pile_rod_pu239 = new ItemPileRod().setUnlocalizedName("pile_rod_pu239").setCreativeTab(MainRegistry.controlTab).setTextureName(RefStrings.MODID + ":pile_rod_pu239");
		pile_rod_plutonium = new ItemPileRod().setUnlocalizedName("pile_rod_plutonium").setCreativeTab(MainRegistry.controlTab).setTextureName(RefStrings.MODID + ":pile_rod_plutonium");
		pile_rod_source = new ItemPileRod().setUnlocalizedName("pile_rod_source").setCreativeTab(MainRegistry.controlTab).setTextureName(RefStrings.MODID + ":pile_rod_source");
		pile_rod_boron = new ItemPileRod().setUnlocalizedName("pile_rod_boron").setCreativeTab(MainRegistry.controlTab).setTextureName(RefStrings.MODID + ":pile_rod_boron");
		pile_rod_lithium = new ItemPileRod().setUnlocalizedName("pile_rod_lithium").setCreativeTab(MainRegistry.controlTab).setTextureName(RefStrings.MODID + ":pile_rod_lithium");
		pile_rod_detector = new ItemPileRod().setUnlocalizedName("pile_rod_detector").setCreativeTab(MainRegistry.controlTab).setTextureName(RefStrings.MODID + ":pile_rod_detector");

		//life, function, reactivity
		plate_fuel_u233 = new ItemPlateFuel(1800000)
			.setFunction(FunctionEnum.SQUARE_ROOT, 60)
			.setUnlocalizedName("plate_fuel_u233")
			.setMaxStackSize(1)
			.setCreativeTab(MainRegistry.controlTab)
			.setTextureName(RefStrings.MODID + ":plate_fuel_u233");
		plate_fuel_u235 = new ItemPlateFuel(2200000)
			.setFunction(FunctionEnum.SQUARE_ROOT, 50)
			.setUnlocalizedName("plate_fuel_u235")
			.setMaxStackSize(1)
			.setCreativeTab(MainRegistry.controlTab)
			.setTextureName(RefStrings.MODID + ":plate_fuel_u235");
		plate_fuel_mox = new ItemPlateFuel(1600000)
			.setFunction(FunctionEnum.LOGARITHM, 35)
			.setUnlocalizedName("plate_fuel_mox")
			.setMaxStackSize(1)
			.setCreativeTab(MainRegistry.controlTab)
			.setTextureName(RefStrings.MODID + ":plate_fuel_mox");
		plate_fuel_pu239 = new ItemPlateFuel(1400000)
			.setFunction(FunctionEnum.NEGATIVE_QUADRATIC, 65)
			.setUnlocalizedName("plate_fuel_pu239")
			.setMaxStackSize(1)
			.setCreativeTab(MainRegistry.controlTab)
			.setTextureName(RefStrings.MODID + ":plate_fuel_pu239");
		plate_fuel_sa326 = new ItemPlateFuel(2000000).setFunction(FunctionEnum.LINEAR, 80).setUnlocalizedName("plate_fuel_sa326").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab).setTextureName(RefStrings.MODID + ":plate_fuel_sa326");
		plate_fuel_ra226be = new ItemPlateFuel(5000000)
			.setFunction(FunctionEnum.PASSIVE, 8)
			.setUnlocalizedName("plate_fuel_ra226be")
			.setMaxStackSize(1)
			.setCreativeTab(MainRegistry.controlTab)
			.setTextureName(RefStrings.MODID + ":plate_fuel_ra226be");
		plate_fuel_pu238be = new ItemPlateFuel(7000000)
			.setFunction(FunctionEnum.PASSIVE, 18)
			.setUnlocalizedName("plate_fuel_pu238be")
			.setMaxStackSize(1)
			.setCreativeTab(MainRegistry.controlTab)
			.setTextureName(RefStrings.MODID + ":plate_fuel_pu238be");
		//unrealistic schrab bullshit below


		pwr_fuel = new ItemPWRFuel().setUnlocalizedName("pwr_fuel").setCreativeTab(MainRegistry.controlTab).setTextureName(RefStrings.MODID + ":pwr_fuel");
		pwr_fuel_hot = new ItemEnumMulti(EnumPWRFuel.class, true, false).setUnlocalizedName("pwr_fuel_hot").setCreativeTab(MainRegistry.controlTab).setTextureName(RefStrings.MODID + ":pwr_fuel_hot");
		pwr_fuel_depleted = new ItemEnumMulti(EnumPWRFuel.class, true, false).setUnlocalizedName("pwr_fuel_depleted").setCreativeTab(MainRegistry.controlTab).setTextureName(RefStrings.MODID + ":pwr_fuel_depleted");

		rbmk_lid = new ItemRBMKLid().setUnlocalizedName("rbmk_lid").setCreativeTab(MainRegistry.controlTab).setTextureName(RefStrings.MODID + ":rbmk_lid");
		rbmk_lid_glass = new ItemRBMKLid().setUnlocalizedName("rbmk_lid_glass").setCreativeTab(MainRegistry.controlTab).setTextureName(RefStrings.MODID + ":rbmk_lid_glass");

		rbmk_pellet_ueu = (ItemRBMKPellet) new ItemRBMKPellet("Unenriched Uranium").setUnlocalizedName("rbmk_pellet_ueu").setTextureName(RefStrings.MODID + ":rbmk_pellet_ueu");
		rbmk_pellet_meu = (ItemRBMKPellet) new ItemRBMKPellet("Medium Enriched Uranium-235").setUnlocalizedName("rbmk_pellet_meu").setTextureName(RefStrings.MODID + ":rbmk_pellet_meu");
		rbmk_pellet_heu233 = (ItemRBMKPellet) new ItemRBMKPellet("Highly Enriched Uranium-233").setUnlocalizedName("rbmk_pellet_heu233").setTextureName(RefStrings.MODID + ":rbmk_pellet_heu233");
		rbmk_pellet_heu235 = (ItemRBMKPellet) new ItemRBMKPellet("Highly Enriched Uranium-235").setUnlocalizedName("rbmk_pellet_heu235").setTextureName(RefStrings.MODID + ":rbmk_pellet_heu235");
		rbmk_pellet_thmeu = (ItemRBMKPellet) new ItemRBMKPellet("Thorium with MEU Driver Fuel").setUnlocalizedName("rbmk_pellet_thmeu").setTextureName(RefStrings.MODID + ":rbmk_pellet_thmeu");
		rbmk_pellet_lep = (ItemRBMKPellet) new ItemRBMKPellet("Low Enriched Plutonium-239").setUnlocalizedName("rbmk_pellet_lep").setTextureName(RefStrings.MODID + ":rbmk_pellet_lep");
		rbmk_pellet_mep = (ItemRBMKPellet) new ItemRBMKPellet("Medium Enriched Plutonium-239").setUnlocalizedName("rbmk_pellet_mep").setTextureName(RefStrings.MODID + ":rbmk_pellet_mep");
		rbmk_pellet_hep239 = (ItemRBMKPellet) new ItemRBMKPellet("Highly Enriched Plutonium-239").setUnlocalizedName("rbmk_pellet_hep239").setTextureName(RefStrings.MODID + ":rbmk_pellet_hep239");
		rbmk_pellet_hep241 = (ItemRBMKPellet) new ItemRBMKPellet("Highly Enriched Plutonium-241").setUnlocalizedName("rbmk_pellet_hep241").setTextureName(RefStrings.MODID + ":rbmk_pellet_hep241");
		rbmk_pellet_lea = (ItemRBMKPellet) new ItemRBMKPellet("Low Enriched Americium-242").setUnlocalizedName("rbmk_pellet_lea").setTextureName(RefStrings.MODID + ":rbmk_pellet_lea");
		rbmk_pellet_mea = (ItemRBMKPellet) new ItemRBMKPellet("Medium Enriched Americium-242").setUnlocalizedName("rbmk_pellet_mea").setTextureName(RefStrings.MODID + ":rbmk_pellet_mea");
		rbmk_pellet_hea241 = (ItemRBMKPellet) new ItemRBMKPellet("Highly Enriched Americium-241").setUnlocalizedName("rbmk_pellet_hea241").setTextureName(RefStrings.MODID + ":rbmk_pellet_hea241");
		rbmk_pellet_hea242 = (ItemRBMKPellet) new ItemRBMKPellet("Highly Enriched Americium-242").setUnlocalizedName("rbmk_pellet_hea242").setTextureName(RefStrings.MODID + ":rbmk_pellet_hea242");
		rbmk_pellet_bk247 = (ItemRBMKPellet) new ItemRBMKPellet("Highly Enriched Berkelium-247").setUnlocalizedName("rbmk_pellet_bk247").setTextureName(RefStrings.MODID + ":rbmk_pellet_bk247");
		rbmk_pellet_men = (ItemRBMKPellet) new ItemRBMKPellet("Medium Enriched Neptunium-237").setUnlocalizedName("rbmk_pellet_men").setTextureName(RefStrings.MODID + ":rbmk_pellet_men");
		rbmk_pellet_hen = (ItemRBMKPellet) new ItemRBMKPellet("Highly Enriched Neptunium-237").setUnlocalizedName("rbmk_pellet_hen").setTextureName(RefStrings.MODID + ":rbmk_pellet_hen");
		rbmk_pellet_mox = (ItemRBMKPellet) new ItemRBMKPellet("Mixed MEU & LEP Oxide").setUnlocalizedName("rbmk_pellet_mox").setTextureName(RefStrings.MODID + ":rbmk_pellet_mox");
		rbmk_pellet_les = (ItemRBMKPellet) new ItemRBMKPellet("Low Enriched Schrabidium-326").setUnlocalizedName("rbmk_pellet_les").setTextureName(RefStrings.MODID + ":rbmk_pellet_les");
		rbmk_pellet_mes = (ItemRBMKPellet) new ItemRBMKPellet("Medium Enriched Schrabidium-326").setUnlocalizedName("rbmk_pellet_mes").setTextureName(RefStrings.MODID + ":rbmk_pellet_mes");
		rbmk_pellet_hes = (ItemRBMKPellet) new ItemRBMKPellet("Highly Enriched Schrabidium-326").setUnlocalizedName("rbmk_pellet_hes").setTextureName(RefStrings.MODID + ":rbmk_pellet_hes");
		rbmk_pellet_leaus = (ItemRBMKPellet) new ItemRBMKPellet("Low Enriched Australium (Tasmanite)").setUnlocalizedName("rbmk_pellet_leaus").setTextureName(RefStrings.MODID + ":rbmk_pellet_leaus");
		rbmk_pellet_heaus = (ItemRBMKPellet) new ItemRBMKPellet("Highly Enriched Australium (Ayerite)").setUnlocalizedName("rbmk_pellet_heaus").setTextureName(RefStrings.MODID + ":rbmk_pellet_heaus");
		rbmk_pellet_po210be = (ItemRBMKPellet) new ItemRBMKPellet("Polonium-210 & Beryllium Neutron Source").disableXenon().setUnlocalizedName("rbmk_pellet_po210be").setTextureName(RefStrings.MODID + ":rbmk_pellet_po210be");
		rbmk_pellet_ra226be = (ItemRBMKPellet) new ItemRBMKPellet("Radium-226 & Beryllium Neutron Source").disableXenon().setUnlocalizedName("rbmk_pellet_ra226be").setTextureName(RefStrings.MODID + ":rbmk_pellet_ra226be");
		rbmk_pellet_pu238be = (ItemRBMKPellet) new ItemRBMKPellet("Plutonium-238 & Beryllium Neutron Source").setUnlocalizedName("rbmk_pellet_pu238be").setTextureName(RefStrings.MODID + ":rbmk_pellet_pu238be");
		rbmk_pellet_balefire_gold = (ItemRBMKPellet) new ItemRBMKPellet("Antihydrogen in a Magnetized Gold-198 Lattice").disableXenon().setUnlocalizedName("rbmk_pellet_balefire_gold").setTextureName(RefStrings.MODID + ":rbmk_pellet_balefire_gold");
		rbmk_pellet_flashlead = (ItemRBMKPellet) new ItemRBMKPellet("Antihydrogen confined by a Magnetized Gold-198 and Lead-209 Lattice").disableXenon().setUnlocalizedName("rbmk_pellet_flashlead").setTextureName(RefStrings.MODID + ":rbmk_pellet_flashlead");
		rbmk_pellet_balefire = (ItemRBMKPellet) new ItemRBMKPellet("Draconic Flames").disableXenon().setUnlocalizedName("rbmk_pellet_balefire").setTextureName(RefStrings.MODID + ":rbmk_pellet_balefire");
		rbmk_pellet_zfb_bismuth = (ItemRBMKPellet) new ItemRBMKPellet("Zirconium Fast Breeder - LEU/HEP-241#Bi").setUnlocalizedName("rbmk_pellet_zfb_bismuth").setTextureName(RefStrings.MODID + ":rbmk_pellet_zfb_bismuth");
		rbmk_pellet_zfb_pu241 = (ItemRBMKPellet) new ItemRBMKPellet("Zirconium Fast Breeder - HEU-235/HEP-240#Pu-241").setUnlocalizedName("rbmk_pellet_zfb_pu241").setTextureName(RefStrings.MODID + ":rbmk_pellet_zfb_pu241");
		rbmk_pellet_zfb_am_mix = (ItemRBMKPellet) new ItemRBMKPellet("Zirconium Fast Breeder - HEP-241#MEA").setUnlocalizedName("rbmk_pellet_zfb_am_mix").setTextureName(RefStrings.MODID + ":rbmk_pellet_zfb_am_mix");
		//rbmk_pellet_drx = (ItemRBMKPellet) new ItemRBMKPellet(EnumChatFormatting.OBFUSCATED + "can't you hear, can't you hear the thunder?").setUnlocalizedName("rbmk_pellet_drx").setTextureName(RefStrings.MODID + ":rbmk_pellet_drx");
		rbmk_pellet_lecm = (ItemRBMKPellet) new ItemRBMKPellet("Low Enriched Curium-245").setUnlocalizedName("rbmk_pellet_lecm").setTextureName(RefStrings.MODID + ":rbmk_pellet_lecm");
		rbmk_pellet_mecm = (ItemRBMKPellet) new ItemRBMKPellet("Medium Enriched Curium-245").setUnlocalizedName("rbmk_pellet_mecm").setTextureName(RefStrings.MODID + ":rbmk_pellet_mecm");
		rbmk_pellet_hecm = (ItemRBMKPellet) new ItemRBMKPellet("Highly Enriched Curium-245").setUnlocalizedName("rbmk_pellet_hecm").setTextureName(RefStrings.MODID + ":rbmk_pellet_hecm");
		rbmk_pellet_lecf = (ItemRBMKPellet) new ItemRBMKPellet("Low Enriched Californium-252").setUnlocalizedName("rbmk_pellet_lecf").setTextureName(RefStrings.MODID + ":rbmk_pellet_lecf");
		rbmk_pellet_mecf = (ItemRBMKPellet) new ItemRBMKPellet("Medium Enriched Californium-252").setUnlocalizedName("rbmk_pellet_mecf").setTextureName(RefStrings.MODID + ":rbmk_pellet_mecf");
		rbmk_pellet_hecf = (ItemRBMKPellet) new ItemRBMKPellet("Highly Enriched Californium-252").setUnlocalizedName("rbmk_pellet_hecf").setTextureName(RefStrings.MODID + ":rbmk_pellet_hecf");

		rbmk_fuel_empty = new Item().setUnlocalizedName("rbmk_fuel_empty").setCreativeTab(MainRegistry.controlTab).setTextureName(RefStrings.MODID + ":rbmk_fuel_empty");
		rbmk_fuel_ueu = (ItemRBMKRod) new ItemRBMKRod(rbmk_pellet_ueu)
			.setYield(115000000D)
			.setStats(18)
			.setFunction(EnumBurnFunc.PLATEU)
			.setDepletionFunction(EnumDepleteFunc.RAISING_SLOPE)
			.setXenon(0.75D, 120D)
			.setHeat(0.60D)
			.setDiffusion(0.028D)
			.setMeltingPoint(2865)
			.setUnlocalizedName("rbmk_fuel_ueu")
			.setTextureName(RefStrings.MODID + ":rbmk_fuel_ueu");

		rbmk_fuel_meu = (ItemRBMKRod) new ItemRBMKRod(rbmk_pellet_meu)
			.setYield(105000000D)
			.setStats(22)
			.setFunction(EnumBurnFunc.PLATEU)
			.setDepletionFunction(EnumDepleteFunc.RAISING_SLOPE)
			.setXenon(0.80D, 115D)
			.setHeat(0.68D)
			.setDiffusion(0.026D)
			.setMeltingPoint(2865)
			.setUnlocalizedName("rbmk_fuel_meu")
			.setTextureName(RefStrings.MODID + ":rbmk_fuel_meu");

		rbmk_fuel_heu233 = (ItemRBMKRod) new ItemRBMKRod(rbmk_pellet_heu233)
			.setYield(95000000D)
			.setStats(30D)
			.setFunction(EnumBurnFunc.PLATEU)
			.setDepletionFunction(EnumDepleteFunc.GENTLE_SLOPE)
			.setXenon(0.65D, 140D)
			.setHeat(0.95D)
			.setDiffusion(0.032D)
			.setMeltingPoint(2865)
			.setUnlocalizedName("rbmk_fuel_heu233")
			.setTextureName(RefStrings.MODID + ":rbmk_fuel_heu233");

		rbmk_fuel_heu235 = (ItemRBMKRod) new ItemRBMKRod(rbmk_pellet_heu235)
			.setYield(90000000D)
			.setStats(28D)
			.setFunction(EnumBurnFunc.PLATEU)
			.setDepletionFunction(EnumDepleteFunc.GENTLE_SLOPE)
			.setXenon(0.70D, 125D)
			.setHeat(0.90D)
			.setDiffusion(0.027D)
			.setMeltingPoint(2865)
			.setUnlocalizedName("rbmk_fuel_heu235")
			.setTextureName(RefStrings.MODID + ":rbmk_fuel_heu235");

		rbmk_fuel_thmeu = (ItemRBMKRod) new ItemRBMKRod(rbmk_pellet_thmeu)
			.setYield(120000000D)
			.setStats(20D)
			.setFunction(EnumBurnFunc.PLATEU)
			.setDepletionFunction(EnumDepleteFunc.BOOSTED_SLOPE)
			.setXenon(0.55D, 160D)
			.setHeat(0.62D)
			.setDiffusion(0.035D)
			.setMeltingPoint(3350)
			.setUnlocalizedName("rbmk_fuel_thmeu")
			.setTextureName(RefStrings.MODID + ":rbmk_fuel_thmeu");

		rbmk_fuel_lep = (ItemRBMKRod) new ItemRBMKRod(rbmk_pellet_lep)
			.setYield(85000000D)
			.setStats(26D)
			.setFunction(EnumBurnFunc.ARCH)
			.setDepletionFunction(EnumDepleteFunc.RAISING_SLOPE)
			.setXenon(0.90D, 100D)
			.setHeat(1.05D)
			.setDiffusion(0.020D)
			.setMeltingPoint(2744)
			.setUnlocalizedName("rbmk_fuel_lep")
			.setTextureName(RefStrings.MODID + ":rbmk_fuel_lep");

		rbmk_fuel_mep = (ItemRBMKRod) new ItemRBMKRod(rbmk_pellet_mep)
			.setYield(78000000D)
			.setStats(30D)
			.setFunction(EnumBurnFunc.ARCH)
			.setDepletionFunction(EnumDepleteFunc.RAISING_SLOPE)
			.setXenon(0.95D, 95D)
			.setHeat(1.15D)
			.setDiffusion(0.018D)
			.setMeltingPoint(2744)
			.setUnlocalizedName("rbmk_fuel_mep")
			.setTextureName(RefStrings.MODID + ":rbmk_fuel_mep");

		rbmk_fuel_hep239 = (ItemRBMKRod) new ItemRBMKRod(rbmk_pellet_hep239)
			.setYield(70000000D)
			.setStats(34D)
			.setFunction(EnumBurnFunc.ARCH)
			.setDepletionFunction(EnumDepleteFunc.GENTLE_SLOPE)
			.setXenon(1.0D, 90D)
			.setHeat(1.25D)
			.setDiffusion(0.015D)
			.setMeltingPoint(2744)
			.setUnlocalizedName("rbmk_fuel_hep")
			.setTextureName(RefStrings.MODID + ":rbmk_fuel_hep");
		rbmk_fuel_hep241 = (ItemRBMKRod) new ItemRBMKRod(rbmk_pellet_hep241)
			.setYield(65000000D)
			.setStats(36D)
			.setFunction(EnumBurnFunc.ARCH)
			.setDepletionFunction(EnumDepleteFunc.RAISING_SLOPE)
			.setXenon(1.10D, 85D)
			.setHeat(1.35D)
			.setDiffusion(0.014D)
			.setMeltingPoint(2744)
			.setUnlocalizedName("rbmk_fuel_hep241")
			.setTextureName(RefStrings.MODID + ":rbmk_fuel_hep241");

		rbmk_fuel_lea = (ItemRBMKRod) new ItemRBMKRod(rbmk_pellet_lea)
			.setYield(85000000D)
			.setStats(28D, 5D)
			.setFunction(EnumBurnFunc.PLATEU)
			.setDepletionFunction(EnumDepleteFunc.RAISING_SLOPE)
			.setXenon(1.00D, 95D)
			.setHeat(1.15D)
			.setDiffusion(0.018D)
			.setMeltingPoint(2386)
			.setUnlocalizedName("rbmk_fuel_lea")
			.setTextureName(RefStrings.MODID + ":rbmk_fuel_lea");

		rbmk_fuel_mea = (ItemRBMKRod) new ItemRBMKRod(rbmk_pellet_mea)
			.setYield(70000000D)
			.setStats(34D, 10D)
			.setFunction(EnumBurnFunc.ARCH)
			.setDepletionFunction(EnumDepleteFunc.GENTLE_SLOPE)
			.setXenon(1.15D, 85D)
			.setHeat(1.40D)
			.setDiffusion(0.014D)
			.setMeltingPoint(2386)
			.setUnlocalizedName("rbmk_fuel_mea")
			.setTextureName(RefStrings.MODID + ":rbmk_fuel_mea");

		rbmk_fuel_hea241 = (ItemRBMKRod) new ItemRBMKRod(rbmk_pellet_hea241)
			.setYield(60000000D)
			.setStats(38D, 12D)
			.setFunction(EnumBurnFunc.ARCH)
			.setDepletionFunction(EnumDepleteFunc.GENTLE_SLOPE)
			.setXenon(1.20D, 80D)
			.setHeat(1.55D)
			.setDiffusion(0.012D)
			.setMeltingPoint(2386)
			.setNeutronTypes(NType.FAST, NType.FAST)
			.setUnlocalizedName("rbmk_fuel_hea241")
			.setTextureName(RefStrings.MODID + ":rbmk_fuel_hea241");

		rbmk_fuel_hea242 = (ItemRBMKRod) new ItemRBMKRod(rbmk_pellet_hea242)
			.setYield(50000000D)
			.setStats(42D)
			.setFunction(EnumBurnFunc.ARCH)
			.setDepletionFunction(EnumDepleteFunc.GENTLE_SLOPE)
			.setXenon(1.30D, 75D)
			.setHeat(1.75D)
			.setDiffusion(0.010D)
			.setMeltingPoint(2386)
			.setUnlocalizedName("rbmk_fuel_hea242")
			.setTextureName(RefStrings.MODID + ":rbmk_fuel_hea242");

		rbmk_fuel_bk247 = (ItemRBMKRod) new ItemRBMKRod(rbmk_pellet_bk247)
			.setYield(40000000D)
			.setStats(46D)
			.setFunction(EnumBurnFunc.ARCH)
			.setDepletionFunction(EnumDepleteFunc.CF_SLOPE)
			.setXenon(1.35D, 70D)
			.setHeat(1.95D)
			.setDiffusion(0.009D)
			.setMeltingPoint(2993)
			.setUnlocalizedName("rbmk_fuel_bk247")
			.setTextureName(RefStrings.MODID + ":rbmk_fuel_bk247");
		// Medium Enriched Neptunium (assumed Np fuel)
		rbmk_fuel_men = (ItemRBMKRod) new ItemRBMKRod(rbmk_pellet_men)
			.setYield(70000000D)
			.setStats(28D)
			.setFunction(EnumBurnFunc.ARCH)
			.setDepletionFunction(EnumDepleteFunc.RAISING_SLOPE)
			.setXenon(1.05D, 90D)
			.setHeat(1.00D)
			.setDiffusion(0.018D)
			.setMeltingPoint(2800)
			.setNeutronTypes(NType.ANY, NType.FAST) // Pu-239 build-up broadens neutron behavior
			.setUnlocalizedName("rbmk_fuel_men")
			.setTextureName(RefStrings.MODID + ":rbmk_fuel_men");

		// High Enriched Neptunium (assumed Np fuel)
		rbmk_fuel_hen = (ItemRBMKRod) new ItemRBMKRod(rbmk_pellet_hen)
			.setYield(55000000D)
			.setStats(34D)
			.setFunction(EnumBurnFunc.ARCH)
			.setDepletionFunction(EnumDepleteFunc.GENTLE_SLOPE)
			.setXenon(1.15D, 80D)
			.setHeat(1.20D)
			.setDiffusion(0.014D)
			.setMeltingPoint(2800)
			.setNeutronTypes(NType.FAST, NType.FAST)
			.setUnlocalizedName("rbmk_fuel_hen")
			.setTextureName(RefStrings.MODID + ":rbmk_fuel_hen");

		// Mixed Oxide Fuel (U/Pu mix)
		rbmk_fuel_mox = (ItemRBMKRod) new ItemRBMKRod(rbmk_pellet_mox)
			.setYield(85000000D)
			.setStats(30D)
			.setFunction(EnumBurnFunc.ARCH)
			.setDepletionFunction(EnumDepleteFunc.RAISING_SLOPE)
			.setXenon(0.95D, 100D)
			.setHeat(0.90D)
			.setDiffusion(0.020D)
			.setMeltingPoint(2815)
			.setUnlocalizedName("rbmk_fuel_mox")
			.setTextureName(RefStrings.MODID + ":rbmk_fuel_mox");

		// Lesser Enriched Schrabidium (legacy Bobcat/HBM material)
		// TODO realism pass intentionally skipped - fictional material
		rbmk_fuel_les = (ItemRBMKRod) new ItemRBMKRod(rbmk_pellet_les)
			.setYield(100000000D)
			.setStats(50)
			.setFunction(EnumBurnFunc.SQUARE_ROOT)
			.setHeat(1.25D)
			.setMeltingPoint(2500)
			.setNeutronTypes(NType.SLOW, NType.SLOW)
			.setUnlocalizedName("rbmk_fuel_les")
			.setTextureName(RefStrings.MODID + ":rbmk_fuel_les");

		// Medium Enriched Schrabidium (legacy Bobcat/HBM material)
		// TODO realism pass intentionally skipped - fictional material
		rbmk_fuel_mes = (ItemRBMKRod) new ItemRBMKRod(rbmk_pellet_mes)
			.setYield(100000000D)
			.setStats(75D)
			.setFunction(EnumBurnFunc.ARCH)
			.setHeat(1.5D)
			.setMeltingPoint(2750)
			.setUnlocalizedName("rbmk_fuel_mes")
			.setTextureName(RefStrings.MODID + ":rbmk_fuel_mes");

		// High Enriched Schrabidium (legacy Bobcat/HBM material)
		// TODO realism pass intentionally skipped - fictional material
		rbmk_fuel_hes = (ItemRBMKRod) new ItemRBMKRod(rbmk_pellet_hes)
			.setYield(100000000D)
			.setStats(90)
			.setFunction(EnumBurnFunc.LINEAR)
			.setDepletionFunction(EnumDepleteFunc.LINEAR)
			.setHeat(1.75D)
			.setMeltingPoint(3000)
			.setUnlocalizedName("rbmk_fuel_hes")
			.setTextureName(RefStrings.MODID + ":rbmk_fuel_hes");
		// austrailium bullshit (IGNORE FOR REALISM)
		rbmk_fuel_leaus = (ItemRBMKRod) new ItemRBMKRod(rbmk_pellet_leaus)
			.setYield(100000000D)
			.setStats(30)
			.setFunction(EnumBurnFunc.SIGMOID)
			.setDepletionFunction(EnumDepleteFunc.LINEAR)
			.setXenon(0.05D, 50D)
			.setHeat(1.5D)
			.setMeltingPoint(7029)
			.setUnlocalizedName("rbmk_fuel_leaus")
			.setTextureName(RefStrings.MODID + ":rbmk_fuel_leaus");

		rbmk_fuel_heaus = (ItemRBMKRod) new ItemRBMKRod(rbmk_pellet_heaus)
			.setYield(100000000D)
			.setStats(35)
			.setFunction(EnumBurnFunc.SQUARE_ROOT)
			.setXenon(0.05D, 50D)
			.setHeat(2D)
			.setMeltingPoint(5211)
			.setUnlocalizedName("rbmk_fuel_heaus")
			.setTextureName(RefStrings.MODID + ":rbmk_fuel_heaus");


		// Polonium-Beryllium neutron source
		// realistic: very strong neutron source, short-lived, startup fuel
		rbmk_fuel_po210be = (ItemRBMKRod) new ItemRBMKRod(rbmk_pellet_po210be)
			.setYield(10000000D)
			.setStats(0D, 65D)
			.setFunction(EnumBurnFunc.PASSIVE)
			.setDepletionFunction(EnumDepleteFunc.LINEAR)
			.setXenon(0.0D, 999D)
			.setHeat(0.25D)
			.setDiffusion(0.030D)
			.setMeltingPoint(1287)
			.setNeutronTypes(NType.SLOW, NType.SLOW) // Be moderation
			.setUnlocalizedName("rbmk_fuel_po210be")
			.setTextureName(RefStrings.MODID + ":rbmk_fuel_po210be");


		// Radium-Beryllium neutron source
		// weaker than Po-Be but very long-lived
		rbmk_fuel_ra226be = (ItemRBMKRod) new ItemRBMKRod(rbmk_pellet_ra226be)
			.setYield(300000000D)
			.setStats(0D, 12D)
			.setFunction(EnumBurnFunc.PASSIVE)
			.setDepletionFunction(EnumDepleteFunc.LINEAR)
			.setXenon(0.0D, 999D)
			.setHeat(0.05D)
			.setDiffusion(0.080D)
			.setMeltingPoint(700)
			.setNeutronTypes(NType.SLOW, NType.SLOW) // Be moderation
			.setUnlocalizedName("rbmk_fuel_ra226be")
			.setTextureName(RefStrings.MODID + ":rbmk_fuel_ra226be");


		// Pu-238 + Be neutron source
		// hotter, stronger, semi-self sustaining but not real fissile reactor fuel
		rbmk_fuel_pu238be = (ItemRBMKRod) new ItemRBMKRod(rbmk_pellet_pu238be)
			.setYield(40000000D)
			.setStats(8D, 45D)
			.setFunction(EnumBurnFunc.PLATEU)
			.setDepletionFunction(EnumDepleteFunc.GENTLE_SLOPE)
			.setXenon(0.0D, 999D)
			.setHeat(0.40D)
			.setDiffusion(0.025D)
			.setMeltingPoint(1287)
			.setNeutronTypes(NType.SLOW, NType.SLOW) // Be moderation
			.setUnlocalizedName("rbmk_fuel_pu238be")
			.setTextureName(RefStrings.MODID + ":rbmk_fuel_pu238be");


		// Balefire Gold (legacy HBM/Bobcat material)
		// TODO realism pass intentionally skipped - fictional material
		rbmk_fuel_balefire_gold = (ItemRBMKRod) new ItemRBMKRod(rbmk_pellet_balefire_gold)
			.setYield(100000000D)
			.setStats(50, 10)
			.setFunction(EnumBurnFunc.ARCH)
			.setDepletionFunction(EnumDepleteFunc.LINEAR)
			.setXenon(0.0D, 50D)
			.setMeltingPoint(2000)
			.setUnlocalizedName("rbmk_fuel_balefire_gold")
			.setTextureName(RefStrings.MODID + ":rbmk_fuel_balefire_gold");


		// Flashlead (legacy HBM/Bobcat material)
		// TODO realism pass intentionally skipped - fictional material
		rbmk_fuel_flashlead = (ItemRBMKRod) new ItemRBMKRod(rbmk_pellet_flashlead)
			.setYield(250000000D)
			.setStats(40, 50)
			.setFunction(EnumBurnFunc.ARCH)
			.setDepletionFunction(EnumDepleteFunc.LINEAR)
			.setXenon(0.0D, 50D)
			.setMeltingPoint(2050)
			.setUnlocalizedName("rbmk_fuel_flashlead")
			.setTextureName(RefStrings.MODID + ":rbmk_fuel_flashlead");
		// Balefire (legacy HBM/Bobcat material)
		// TODO realism pass intentionally skipped - fictional material
		rbmk_fuel_balefire = (ItemRBMKRod) new ItemRBMKRod(rbmk_pellet_balefire)
			.setYield(100000000D)
			.setStats(100, 35)
			.setFunction(EnumBurnFunc.LINEAR)
			.setXenon(0.0D, 50D)
			.setHeat(3D)
			.setMeltingPoint(3652)
			.setUnlocalizedName("rbmk_fuel_balefire")
			.setTextureName(RefStrings.MODID + ":rbmk_fuel_balefire");


		// Zirconium Fast Breeder Bismuth fuel
		// assumed breeder blanket / transmutation support fuel
		rbmk_fuel_zfb_bismuth = (ItemRBMKRod) new ItemRBMKRod(rbmk_pellet_zfb_bismuth)
			.setYield(35000000D)
			.setStats(16D)
			.setFunction(EnumBurnFunc.PLATEU)
			.setDepletionFunction(EnumDepleteFunc.GENTLE_SLOPE)
			.setXenon(0.75D, 115D)
			.setHeat(1.20D)
			.setDiffusion(0.025D)
			.setMeltingPoint(2744)
			.setNeutronTypes(NType.FAST, NType.FAST)
			.setUnlocalizedName("rbmk_fuel_zfb_bismuth")
			.setTextureName(RefStrings.MODID + ":rbmk_fuel_zfb_bismuth");


		// Fast breeder Pu-241 fuel
		rbmk_fuel_zfb_pu241 = (ItemRBMKRod) new ItemRBMKRod(rbmk_pellet_zfb_pu241)
			.setYield(45000000D)
			.setStats(24D)
			.setFunction(EnumBurnFunc.ARCH)
			.setDepletionFunction(EnumDepleteFunc.GENTLE_SLOPE)
			.setXenon(0.90D, 95D)
			.setHeat(1.30D)
			.setDiffusion(0.018D)
			.setMeltingPoint(2865)
			.setNeutronTypes(NType.FAST, NType.FAST)
			.setUnlocalizedName("rbmk_fuel_zfb_pu241")
			.setTextureName(RefStrings.MODID + ":rbmk_fuel_zfb_pu241");


		// Fast breeder americium mix
		rbmk_fuel_zfb_am_mix = (ItemRBMKRod) new ItemRBMKRod(rbmk_pellet_zfb_am_mix)
			.setYield(38000000D)
			.setStats(20D, 12D)
			.setFunction(EnumBurnFunc.ARCH)
			.setDepletionFunction(EnumDepleteFunc.CF_SLOPE)
			.setXenon(1.10D, 85D)
			.setHeat(1.55D)
			.setDiffusion(0.014D)
			.setMeltingPoint(2744)
			.setNeutronTypes(NType.FAST, NType.FAST)
			.setUnlocalizedName("rbmk_fuel_zfb_am_mix")
			.setTextureName(RefStrings.MODID + ":rbmk_fuel_zfb_am_mix");


		// DRX dev nonsense
		// TODO realism pass intentionally skipped - nonphysical/dev fuel
		// rbmk_fuel_drx = ...


		// test/dev nonsense
		// TODO realism pass intentionally skipped - dev/test fuel
		rbmk_fuel_test = (ItemRBMKRod) new ItemRBMKRod("THE VOICES")
			.setYield(1000000D)
			.setStats(100)
			.setFunction(EnumBurnFunc.EXPERIMENTAL)
			.setHeat(1.0D)
			.setMeltingPoint(100000)
			.setUnlocalizedName("rbmk_fuel_test")
			.setTextureName(RefStrings.MODID + ":rbmk_fuel_test");


		// Low Enriched Curium (assumed)
		rbmk_fuel_lecm = (ItemRBMKRod) new ItemRBMKRod(rbmk_pellet_lecm)
			.setYield(24000000D)
			.setStats(18D, 8D)
			.setFunction(EnumBurnFunc.ARCH)
			.setDepletionFunction(EnumDepleteFunc.CF_SLOPE)
			.setXenon(1.20D, 80D)
			.setHeat(1.45D)
			.setMeltingPoint(1340)
			.setDiffusion(0.025D)
			.setNeutronTypes(NType.FAST, NType.FAST)
			.setUnlocalizedName("rbmk_fuel_lecm")
			.setTextureName(RefStrings.MODID + ":rbmk_fuel_lecm");


		// Medium Enriched Curium (assumed)
		rbmk_fuel_mecm = (ItemRBMKRod) new ItemRBMKRod(rbmk_pellet_mecm)
			.setYield(20000000D)
			.setStats(24D, 12D)
			.setFunction(EnumBurnFunc.ARCH)
			.setDepletionFunction(EnumDepleteFunc.CF_SLOPE)
			.setXenon(1.35D, 70D)
			.setHeat(1.70D)
			.setMeltingPoint(1720)
			.setDiffusion(0.018D)
			.setNeutronTypes(NType.FAST, NType.FAST)
			.setUnlocalizedName("rbmk_fuel_mecm")
			.setTextureName(RefStrings.MODID + ":rbmk_fuel_mecm");
		// High Enriched Curium (assumed)
		rbmk_fuel_hecm = (ItemRBMKRod) new ItemRBMKRod(rbmk_pellet_hecm)
			.setYield(18000000D)
			.setStats(30D, 18D)
			.setFunction(EnumBurnFunc.ARCH)
			.setDepletionFunction(EnumDepleteFunc.CF_SLOPE)
			.setXenon(1.45D, 65D)
			.setHeat(1.90D)
			.setMeltingPoint(1880)
			.setDiffusion(0.015D)
			.setNeutronTypes(NType.FAST, NType.FAST)
			.setUnlocalizedName("rbmk_fuel_hecm")
			.setTextureName(RefStrings.MODID + ":rbmk_fuel_hecm");


		// Low Enriched Californium (assumed)
		rbmk_fuel_lecf = (ItemRBMKRod) new ItemRBMKRod(rbmk_pellet_lecf)
			.setYield(7000000D)
			.setStats(28D, 35D)
			.setFunction(EnumBurnFunc.ARCH)
			.setDepletionFunction(EnumDepleteFunc.CF_SLOPE)
			.setXenon(0.0D, 999D)
			.setHeat(2.10D)
			.setMeltingPoint(1450)
			.setDiffusion(0.012D)
			.setNeutronTypes(NType.FAST, NType.FAST)
			.setUnlocalizedName("rbmk_fuel_lecf")
			.setTextureName(RefStrings.MODID + ":rbmk_fuel_lecf");


		// Medium Enriched Californium (assumed)
		rbmk_fuel_mecf = (ItemRBMKRod) new ItemRBMKRod(rbmk_pellet_mecf)
			.setYield(5000000D)
			.setStats(34D, 45D)
			.setFunction(EnumBurnFunc.ARCH)
			.setDepletionFunction(EnumDepleteFunc.CF_SLOPE)
			.setXenon(0.0D, 999D)
			.setHeat(2.45D)
			.setMeltingPoint(1450)
			.setDiffusion(0.010D)
			.setNeutronTypes(NType.FAST, NType.FAST)
			.setUnlocalizedName("rbmk_fuel_mecf")
			.setTextureName(RefStrings.MODID + ":rbmk_fuel_mecf");


		// High Enriched Californium (assumed)
		rbmk_fuel_hecf = (ItemRBMKRod) new ItemRBMKRod(rbmk_pellet_hecf)
			.setYield(3500000D)
			.setStats(40D, 60D)
			.setFunction(EnumBurnFunc.ARCH)
			.setDepletionFunction(EnumDepleteFunc.CF_SLOPE)
			.setXenon(0.0D, 999D)
			.setHeat(2.90D)
			.setMeltingPoint(1450)
			.setDiffusion(0.008D)
			.setNeutronTypes(NType.FAST, NType.FAST)
			.setUnlocalizedName("rbmk_fuel_hecf")
			.setTextureName(RefStrings.MODID + ":rbmk_fuel_hecf");


		watz_pellet = new ItemWatzPellet().setUnlocalizedName("watz_pellet").setTextureName(RefStrings.MODID + ":watz_pellet");
		watz_pellet_depleted = new ItemWatzPellet().setUnlocalizedName("watz_pellet_depleted").setTextureName(RefStrings.MODID + ":watz_pellet");
		powder_spent_haleu = new Item().setUnlocalizedName("powder_spent_haleu").setTextureName(RefStrings.MODID + ":powder_uranium");
		powder_spent_leu = new Item().setUnlocalizedName("powder_spent_leu").setTextureName(RefStrings.MODID + ":powder_uranium");
		powder_spent_thorium = new Item().setUnlocalizedName("powder_spent_thorium").setTextureName(RefStrings.MODID + ":powder_thorium");
		powder_spent_u233 = new Item().setUnlocalizedName("powder_spent_u233").setTextureName(RefStrings.MODID + ":powder_uranium");
		powder_spent_mox = new Item().setUnlocalizedName("powder_spent_mox").setTextureName(RefStrings.MODID + ":powder_uranium");

		dust_graphite = new Item().setUnlocalizedName("dust_graphite").setTextureName(RefStrings.MODID + ":powder_coal");
		powder_lead_irradiated = new Item().setUnlocalizedName("powder_lead_irradiated").setTextureName(RefStrings.MODID + ":powder_lead");
		powder_boron_spent = new Item().setUnlocalizedName("powder_boron_spent").setTextureName(RefStrings.MODID + ":powder_boron");
		powder_du_spent = new Item().setUnlocalizedName("powder_du_spent").setTextureName(RefStrings.MODID + ":powder_uranium");

		icf_pellet_empty = new Item().setUnlocalizedName("icf_pellet_empty").setCreativeTab(MainRegistry.controlTab).setTextureName(RefStrings.MODID + ":icf_pellet_empty");
		icf_pellet = new ItemICFPellet().setUnlocalizedName("icf_pellet").setCreativeTab(MainRegistry.controlTab).setTextureName(RefStrings.MODID + ":icf_pellet");
		icf_pellet_depleted = new Item().setUnlocalizedName("icf_pellet_depleted").setCreativeTab(MainRegistry.controlTab).setMaxStackSize(1).setTextureName(RefStrings.MODID + ":icf_pellet_depleted");

		trinitite = new ItemNuclearWaste().setUnlocalizedName("trinitite").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":trinitite_new");
		nuclear_waste_long = new ItemWasteLong().setUnlocalizedName("nuclear_waste_long").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":nuclear_waste_long");
		nuclear_waste_long_tiny = new ItemWasteLong().setUnlocalizedName("nuclear_waste_long_tiny").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":nuclear_waste_long_tiny");
		nuclear_waste_short = new ItemWasteShort().setUnlocalizedName("nuclear_waste_short").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":nuclear_waste_short");
		nuclear_waste_short_tiny = new ItemWasteShort().setUnlocalizedName("nuclear_waste_short_tiny").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":nuclear_waste_short_tiny");
		nuclear_waste_long_depleted = new ItemWasteLong().setUnlocalizedName("nuclear_waste_long_depleted").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":nuclear_waste_long_depleted");
		nuclear_waste_long_depleted_tiny = new ItemWasteLong().setUnlocalizedName("nuclear_waste_long_depleted_tiny").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":nuclear_waste_long_depleted_tiny");
		nuclear_waste_short_depleted = new ItemWasteShort().setUnlocalizedName("nuclear_waste_short_depleted").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":nuclear_waste_short_depleted");
		nuclear_waste_short_depleted_tiny = new ItemWasteShort().setUnlocalizedName("nuclear_waste_short_depleted_tiny").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":nuclear_waste_short_depleted_tiny");
		nuclear_waste = new ItemNuclearWaste().setUnlocalizedName("nuclear_waste").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":nuclear_waste");
		nuclear_waste_tiny = new ItemNuclearWaste().setUnlocalizedName("nuclear_waste_tiny").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":nuclear_waste_tiny");
		nuclear_waste_vitrified = new ItemNuclearWaste().setUnlocalizedName("nuclear_waste_vitrified").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":nuclear_waste_vitrified");
		nuclear_waste_vitrified_tiny = new ItemNuclearWaste().setUnlocalizedName("nuclear_waste_vitrified_tiny").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":nuclear_waste_vitrified_tiny");
		scrap_plastic = new ItemPlasticScrap().setUnlocalizedName("scrap_plastic").setTextureName(RefStrings.MODID + ":scrap_plastic");
		scrap = new Item().setUnlocalizedName("scrap").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":scrap");
		scrap_oil = new Item().setUnlocalizedName("scrap_oil").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":scrap_oil");
		scrap_nuclear = new Item().setUnlocalizedName("scrap_nuclear").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":scrap_nuclear");
		containment_box = new ItemLeadBox().setUnlocalizedName("containment_box").setCreativeTab(MainRegistry.consumableTab).setTextureName(RefStrings.MODID + ":containment_box");
		plastic_bag = new ItemPlasticBag().setUnlocalizedName("plastic_bag").setCreativeTab(MainRegistry.consumableTab).setTextureName(RefStrings.MODID + ":plastic_bag");

		debris_graphite = new Item().setUnlocalizedName("debris_graphite").setCreativeTab(MainRegistry.controlTab).setTextureName(RefStrings.MODID + ":debris_graphite");
		debris_metal = new Item().setUnlocalizedName("debris_metal").setCreativeTab(MainRegistry.controlTab).setTextureName(RefStrings.MODID + ":debris_metal");
		debris_fuel = new Item().setUnlocalizedName("debris_fuel").setCreativeTab(MainRegistry.controlTab).setTextureName(RefStrings.MODID + ":debris_fuel");
		debris_concrete = new Item().setUnlocalizedName("debris_concrete").setCreativeTab(MainRegistry.controlTab).setTextureName(RefStrings.MODID + ":debris_concrete");
		debris_exchanger = new Item().setUnlocalizedName("debris_exchanger").setCreativeTab(MainRegistry.controlTab).setTextureName(RefStrings.MODID + ":debris_exchanger");
		debris_shrapnel =new Item().setUnlocalizedName("debris_shrapnel").setCreativeTab(MainRegistry.controlTab).setTextureName(RefStrings.MODID + ":debris_shrapnel");
		debris_element =new Item().setUnlocalizedName("debris_element").setCreativeTab(MainRegistry.controlTab).setTextureName(RefStrings.MODID + ":debris_element");

		pellet_cluster = new ItemCustomLore().setUnlocalizedName("pellet_cluster").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":pellet_cluster");
		powder_fire = new ItemCustomLore().setUnlocalizedName("powder_fire").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":powder_red_phosphorus");
		powder_ice = new ItemCustomLore().setUnlocalizedName("powder_ice").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":powder_ice");
		powder_poison = new ItemCustomLore().setUnlocalizedName("powder_poison").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":powder_poison");
		powder_thermite = new ItemCustomLore().setUnlocalizedName("powder_thermite").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":powder_thermite");
		cordite = new Item().setUnlocalizedName("cordite").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":cordite");
		ammonium_nitrate = new Item().setUnlocalizedName("ammonium_nitrate").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":ammonium_nitrate");
		ballistite = new Item().setUnlocalizedName("ballistite").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":ballistite");
		ball_dynamite = new Item().setUnlocalizedName("ball_dynamite").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":ball_dynamite");
		ball_tnt = new Item().setUnlocalizedName("ball_tnt").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":ball_tnt");
		ball_tatb = new Item().setUnlocalizedName("ball_tatb").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":ball_tatb");
		ball_fireclay = new Item().setUnlocalizedName("ball_fireclay").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":ball_fireclay");
		ball_ferric_clay = new Item().setUnlocalizedName("ball_ferric_clay").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":ferric_clay");
		pellet_gas = new ItemCustomLore().setUnlocalizedName("pellet_gas").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":pellet_gas");
		magnetron = new ItemCustomLore().setUnlocalizedName("magnetron").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":magnetron_alt");
		pellet_buckshot = new Item().setUnlocalizedName("pellet_buckshot").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":pellets_lead");
		pellet_flechette = new Item().setUnlocalizedName("pellet_flechette").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":pellets_flechette");
		pellet_chlorophyte = new Item().setUnlocalizedName("pellet_chlorophyte").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":pellets_chlorophyte");
		pellet_mercury = new Item().setUnlocalizedName("pellet_mercury").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":pellets_mercury");
		pellet_meteorite = new Item().setUnlocalizedName("pellet_meteorite").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":pellets_meteorite");
		pellet_canister = new Item().setUnlocalizedName("pellet_canister").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":pellets_canister");
		pellet_claws = new Item().setUnlocalizedName("pellet_claws").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":pellets_claws");
		pellet_charged = new Item().setUnlocalizedName("pellet_charged").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":pellets_charged");

		designator = new ItemDesingator().setUnlocalizedName("designator").setMaxStackSize(1).setCreativeTab(MainRegistry.missileTab).setTextureName(RefStrings.MODID + ":designator");
		designator_range = new ItemDesingatorRange().setUnlocalizedName("designator_range").setFull3D().setMaxStackSize(1).setCreativeTab(MainRegistry.missileTab).setTextureName(RefStrings.MODID + ":designator_range_alt");
		designator_manual = new ItemDesingatorManual().setUnlocalizedName("designator_manual").setMaxStackSize(1).setCreativeTab(MainRegistry.missileTab).setTextureName(RefStrings.MODID + ":designator_manual");
		designator_arty_range = new ItemDesignatorArtyRange().setUnlocalizedName("designator_arty_range").setCreativeTab(MainRegistry.missileTab).setTextureName(RefStrings.MODID + ":designator_arty_range");
		launch_code_piece = new Item().setUnlocalizedName("launch_code_piece").setMaxStackSize(1).setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":launch_code_piece");
		launch_code = new Item().setUnlocalizedName("launch_code").setMaxStackSize(1).setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":launch_code");
		launch_key = new Item().setUnlocalizedName("launch_key").setMaxStackSize(1).setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":launch_key");
		missile_assembly = new Item().setUnlocalizedName("missile_assembly").setMaxStackSize(1).setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":missile_assembly");
		missile_generic = new ItemMissile(MissileFormFactor.V2, MissileTier.TIER1).setUnlocalizedName("missile_generic").setMaxStackSize(1).setCreativeTab(MainRegistry.missileTab).setTextureName(RefStrings.MODID + ":missile_generic");
		missile_anti_ballistic = new ItemMissile(MissileFormFactor.ABM, MissileTier.TIER1).setUnlocalizedName("missile_anti_ballistic").setMaxStackSize(1).setCreativeTab(MainRegistry.missileTab).setTextureName(RefStrings.MODID + ":missile_anti_ballistic");
		missile_incendiary = new ItemMissile(MissileFormFactor.V2, MissileTier.TIER1).setUnlocalizedName("missile_incendiary").setMaxStackSize(1).setCreativeTab(MainRegistry.missileTab).setTextureName(RefStrings.MODID + ":missile_incendiary");
		missile_cluster = new ItemMissile(MissileFormFactor.V2, MissileTier.TIER1).setUnlocalizedName("missile_cluster").setMaxStackSize(1).setCreativeTab(MainRegistry.missileTab).setTextureName(RefStrings.MODID + ":missile_cluster");
		missile_buster = new ItemMissile(MissileFormFactor.V2, MissileTier.TIER1).setUnlocalizedName("missile_buster").setMaxStackSize(1).setCreativeTab(MainRegistry.missileTab).setTextureName(RefStrings.MODID + ":missile_buster");
		missile_decoy = new ItemMissile(MissileFormFactor.V2, MissileTier.TIER1).setUnlocalizedName("missile_decoy").setMaxStackSize(1).setCreativeTab(MainRegistry.missileTab).setTextureName(RefStrings.MODID + ":missile_decoy");
		missile_strong = new ItemMissile(MissileFormFactor.STRONG, MissileTier.TIER2).setUnlocalizedName("missile_strong").setMaxStackSize(1).setCreativeTab(MainRegistry.missileTab).setTextureName(RefStrings.MODID + ":missile_strong");
		missile_incendiary_strong = new ItemMissile(MissileFormFactor.STRONG, MissileTier.TIER2).setUnlocalizedName("missile_incendiary_strong").setMaxStackSize(1).setCreativeTab(MainRegistry.missileTab).setTextureName(RefStrings.MODID + ":missile_incendiary_strong");
		missile_cluster_strong = new ItemMissile(MissileFormFactor.STRONG, MissileTier.TIER2).setUnlocalizedName("missile_cluster_strong").setMaxStackSize(1).setCreativeTab(MainRegistry.missileTab).setTextureName(RefStrings.MODID + ":missile_cluster_strong");
		missile_buster_strong = new ItemMissile(MissileFormFactor.STRONG, MissileTier.TIER2).setUnlocalizedName("missile_buster_strong").setMaxStackSize(1).setCreativeTab(MainRegistry.missileTab).setTextureName(RefStrings.MODID + ":missile_buster_strong");
		missile_emp_strong = new ItemMissile(MissileFormFactor.STRONG, MissileTier.TIER2).setUnlocalizedName("missile_emp_strong").setMaxStackSize(1).setCreativeTab(MainRegistry.missileTab).setTextureName(RefStrings.MODID + ":missile_emp_strong");
		missile_burst = new ItemMissile(MissileFormFactor.HUGE, MissileTier.TIER3).setUnlocalizedName("missile_burst").setMaxStackSize(1).setCreativeTab(MainRegistry.missileTab).setTextureName(RefStrings.MODID + ":missile_burst");
		missile_inferno = new ItemMissile(MissileFormFactor.HUGE, MissileTier.TIER3).setUnlocalizedName("missile_inferno").setMaxStackSize(1).setCreativeTab(MainRegistry.missileTab).setTextureName(RefStrings.MODID + ":missile_inferno");
		missile_rain = new ItemMissile(MissileFormFactor.HUGE, MissileTier.TIER3).setUnlocalizedName("missile_rain").setMaxStackSize(1).setCreativeTab(MainRegistry.missileTab).setTextureName(RefStrings.MODID + ":missile_rain");
		missile_drill = new ItemMissile(MissileFormFactor.HUGE, MissileTier.TIER3).setUnlocalizedName("missile_drill").setMaxStackSize(1).setCreativeTab(MainRegistry.missileTab).setTextureName(RefStrings.MODID + ":missile_drill");
		missile_nuclear = new ItemMissile(MissileFormFactor.ATLAS, MissileTier.TIER4).setUnlocalizedName("missile_nuclear").setMaxStackSize(1).setCreativeTab(MainRegistry.missileTab).setTextureName(RefStrings.MODID + ":missile_nuclear");
		missile_nuclear_cluster = new ItemMissile(MissileFormFactor.ATLAS, MissileTier.TIER4).setUnlocalizedName("missile_nuclear_cluster").setMaxStackSize(1).setCreativeTab(MainRegistry.missileTab).setTextureName(RefStrings.MODID + ":missile_nuclear_cluster");
		//missile_volcano = new ItemMissile(MissileFormFactor.ATLAS, MissileTier.TIER4).setUnlocalizedName("missile_volcano").setMaxStackSize(1).setCreativeTab(MainRegistry.missileTab).setTextureName(RefStrings.MODID + ":missile_volcano");
		missile_doomsday = new ItemMissile(MissileFormFactor.ATLAS, MissileTier.TIER4).setUnlocalizedName("missile_doomsday").setMaxStackSize(1).setCreativeTab(MainRegistry.missileTab).setTextureName(RefStrings.MODID + ":missile_doomsday");
		missile_doomsday_rusted = new ItemMissile(MissileFormFactor.ATLAS, MissileTier.TIER4).notLaunchable().setUnlocalizedName("missile_doomsday_rusted").setMaxStackSize(1).setCreativeTab(MainRegistry.missileTab).setTextureName(RefStrings.MODID + ":missile_doomsday");
		//missile_taint = new ItemMissile(MissileFormFactor.MICRO, MissileTier.TIER0).setUnlocalizedName("missile_taint").setMaxStackSize(1).setCreativeTab(MainRegistry.missileTab).setTextureName(RefStrings.MODID + ":missile_taint");
		missile_micro = new ItemMissile(MissileFormFactor.MICRO, MissileTier.TIER0).setUnlocalizedName("missile_micro").setMaxStackSize(1).setCreativeTab(MainRegistry.missileTab).setTextureName(RefStrings.MODID + ":missile_micro");
		//missile_bhole = new ItemMissile(MissileFormFactor.MICRO, MissileTier.TIER0).setUnlocalizedName("missile_bhole").setMaxStackSize(1).setCreativeTab(MainRegistry.missileTab).setTextureName(RefStrings.MODID + ":missile_bhole");
		//missile_schrabidium = new ItemMissile(MissileFormFactor.MICRO, MissileTier.TIER0).setUnlocalizedName("missile_schrabidium").setMaxStackSize(1).setCreativeTab(MainRegistry.missileTab).setTextureName(RefStrings.MODID + ":missile_schrabidium");
		missile_emp = new ItemMissile(MissileFormFactor.MICRO, MissileTier.TIER0).setUnlocalizedName("missile_emp").setMaxStackSize(1).setCreativeTab(MainRegistry.missileTab).setTextureName(RefStrings.MODID + ":missile_emp");
		missile_shuttle = new ItemMissile(MissileFormFactor.OTHER, MissileTier.TIER3, MissileFuel.KEROSENE_PEROXIDE).setUnlocalizedName("missile_shuttle").setMaxStackSize(1).setCreativeTab(MainRegistry.missileTab).setTextureName(RefStrings.MODID + ":missile_shuttle");
		missile_stealth = new ItemMissile(MissileFormFactor.STRONG, MissileTier.TIER1).setUnlocalizedName("missile_stealth").setMaxStackSize(1).setCreativeTab(MainRegistry.missileTab).setTextureName(RefStrings.MODID + ":missile_stealth");
		missile_test = new ItemMissile(MissileFormFactor.MICRO, MissileTier.TIER0).setUnlocalizedName("missile_test").setMaxStackSize(1).setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":missile_micro");
		missile_carrier = new Item().setUnlocalizedName("missile_carrier").setMaxStackSize(1).setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":missile_carrier");
		missile_soyuz = new ItemSoyuz().setUnlocalizedName("missile_soyuz").setMaxStackSize(1).setCreativeTab(MainRegistry.missileTab).setTextureName(RefStrings.MODID + ":soyuz");
		missile_soyuz_lander = new ItemCustomLore().setUnlocalizedName("missile_soyuz_lander").setMaxStackSize(1).setCreativeTab(MainRegistry.missileTab).setTextureName(RefStrings.MODID + ":soyuz_lander");

		missile_custom = new ItemCustomMissile().setUnlocalizedName("missile_custom").setMaxStackSize(1).setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":missile_custom");
		rocket_custom = new ItemCustomRocket().setUnlocalizedName("rocket_custom").setMaxStackSize(1).setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":missile_custom");

		sat_mapper = new ItemSatellite().setUnlocalizedName("sat_mapper").setMaxStackSize(1).setCreativeTab(MainRegistry.missileTab).setTextureName(RefStrings.MODID + ":sat_mapper");
		sat_scanner = new ItemSatellite().setUnlocalizedName("sat_scanner").setMaxStackSize(1).setCreativeTab(MainRegistry.missileTab).setTextureName(RefStrings.MODID + ":sat_scanner");
		sat_radar = new ItemSatellite().setUnlocalizedName("sat_radar").setMaxStackSize(1).setCreativeTab(MainRegistry.missileTab).setTextureName(RefStrings.MODID + ":sat_radar");
		sat_laser = new ItemSatellite().setUnlocalizedName("sat_laser").setMaxStackSize(1).setCreativeTab(MainRegistry.missileTab).setTextureName(RefStrings.MODID + ":sat_laser");
		sat_foeq = new ItemSatellite().setUnlocalizedName("sat_foeq").setMaxStackSize(1).setCreativeTab(MainRegistry.missileTab).setTextureName(RefStrings.MODID + ":sat_foeq");
		sat_resonator = new ItemSatellite().setUnlocalizedName("sat_resonator").setMaxStackSize(1).setCreativeTab(MainRegistry.missileTab).setTextureName(RefStrings.MODID + ":sat_resonator");
		sat_miner = new ItemSatellite().setUnlocalizedName("sat_miner").setMaxStackSize(1).setCreativeTab(MainRegistry.missileTab).setTextureName(RefStrings.MODID + ":sat_miner");
		sat_lunar_miner = new ItemSatellite().setUnlocalizedName("sat_lunar_miner").setMaxStackSize(1).setCreativeTab(MainRegistry.missileTab).setTextureName(RefStrings.MODID + ":sat_lunar_miner");
		//sat_gerald = new ItemSatellite(128_000).setUnlocalizedName("sat_gerald").setMaxStackSize(1).setCreativeTab(MainRegistry.missileTab).setTextureName(RefStrings.MODID + ":sat_gerald");
		sat_chip = new ItemSatChip().setUnlocalizedName("sat_chip").setMaxStackSize(1).setCreativeTab(MainRegistry.missileTab).setTextureName(RefStrings.MODID + ":sat_chip");

		sat_interface = new ItemSatInterface().setUnlocalizedName("sat_interface").setMaxStackSize(1).setCreativeTab(MainRegistry.missileTab).setTextureName(RefStrings.MODID + ":sat_interface");
		sat_coord = new ItemSatInterface().setUnlocalizedName("sat_coord").setMaxStackSize(1).setCreativeTab(MainRegistry.missileTab).setTextureName(RefStrings.MODID + ":sat_coord");
		sat_designator = new ItemSatDesignator().setUnlocalizedName("sat_designator").setFull3D().setMaxStackSize(1).setCreativeTab(MainRegistry.missileTab).setTextureName(RefStrings.MODID + ":sat_designator");
		sat_relay = new ItemSatInterface().setUnlocalizedName("sat_relay").setMaxStackSize(1).setCreativeTab(MainRegistry.missileTab).setTextureName(RefStrings.MODID + ":sat_relay");
																														//in kg
		//syntax: FuelType, Consumption, Lift, Size, Thrust, Mass, ISP, Health, etc.
		mp_thruster_10_kerosene = new ItemCustomMissilePart().makeThruster(FuelType.KEROSENE, 1F, 1.5F, PartSize.SIZE_10, 16000, 180, 722).setHealth(10F).setUnlocalizedName("mp_thruster_10_kerosene");
		mp_thruster_10_kerosene_tec = new ItemCustomMissilePart().makeThruster(FuelType.KEROSENE, 1F, 1.5F, PartSize.SIZE_10, 20000, 200, 722).setHealth(10F).setRarity(Rarity.COMMON).setUnlocalizedName("mp_thruster_10_kerosene_tec");
		mp_thruster_10_solid = new ItemCustomMissilePart().makeThruster(FuelType.SOLID, 1F, 1.5F, PartSize.SIZE_10, 60000, 400, 468).setHealth(10F).setUnlocalizedName("mp_thruster_10_solid");
		mp_thruster_10_xenon = new ItemCustomMissilePart().makeThruster(FuelType.XENON, 1F, 1.5F, PartSize.SIZE_10, 2000, 200, 10080).setHealth(10F).setUnlocalizedName("mp_thruster_10_xenon");
		mp_thruster_15_kerosene = new ItemCustomMissilePart().makeThruster(FuelType.KEROSENE, 1F, 7.5F, PartSize.SIZE_15, 120000, 900, 722).setHealth(10F).setUnlocalizedName("mp_thruster_15_kerosene");
		mp_thruster_15_kerosene_tec = new ItemCustomMissilePart().makeThruster(FuelType.KEROSENE, 1F, 7.5F, PartSize.SIZE_15, 140000, 1000, 722).setHealth(10F).setRarity(Rarity.COMMON).setUnlocalizedName("mp_thruster_15_kerosene_tec");
		mp_thruster_15_kerosene_dual = new ItemCustomMissilePart().makeThruster(FuelType.KEROSENE, 1F, 2.5F, PartSize.SIZE_15, 200000, 1600, 722).setHealth(10F).setUnlocalizedName("mp_thruster_15_kerosene_dual");
		mp_thruster_15_kerosene_triple = new ItemCustomMissilePart().makeThruster(FuelType.KEROSENE, 1F, 5F, PartSize.SIZE_15, 280000, 2200, 722).setHealth(10F).setUnlocalizedName("mp_thruster_15_kerosene_triple");
		mp_thruster_15_solid = new ItemCustomMissilePart().makeThruster(FuelType.SOLID, 1F, 5F, PartSize.SIZE_15, 220000, 1000, 468).setHealth(10F).setUnlocalizedName("mp_thruster_15_solid");
		mp_thruster_15_solid_hexdecuple = new ItemCustomMissilePart().makeThruster(FuelType.SOLID, 1F, 5F, PartSize.SIZE_15, 260000, 1200, 468).setHealth(10F).setRarity(Rarity.UNCOMMON).setUnlocalizedName("mp_thruster_15_solid_hexdecuple");
		mp_thruster_15_hydrogen = new ItemCustomMissilePart().makeThruster(FuelType.HYDROGEN, 1F, 7.5F, PartSize.SIZE_15, 100000, 800, 840).setHealth(10F).setUnlocalizedName("mp_thruster_15_hydrogen");
		mp_thruster_15_hydrogen_dual = new ItemCustomMissilePart().makeThruster(FuelType.HYDROGEN, 1F, 2.5F, PartSize.SIZE_15, 200000, 1400, 840).setHealth(10F).setUnlocalizedName("mp_thruster_15_hydrogen_dual");

		//nah fuck you enjoy hydrogen fuel
		mp_thruster_15_balefire_short = new ItemCustomMissilePart().makeThruster(FuelType.HYDROGEN, 1F, 5F, PartSize.SIZE_15, 800000, 1600, 1598).setHealth(10F).setUnlocalizedName("mp_thruster_15_balefire_short");
		mp_thruster_15_balefire = new ItemCustomMissilePart().makeThruster(FuelType.HYDROGEN, 1F, 5F, PartSize.SIZE_15, 1000000, 2000, 1598).setHealth(10F).setUnlocalizedName("mp_thruster_15_balefire");
		mp_thruster_15_balefire_large = new ItemCustomMissilePart().makeThruster(FuelType.HYDROGEN, 1F, 7.5F, PartSize.SIZE_15, 1200000, 2400, 1598).setHealth(10F).setUnlocalizedName("mp_thruster_15_balefire_large");
		mp_thruster_15_balefire_large_rad = new ItemCustomMissilePart().makeThruster(FuelType.HYDROGEN, 1F, 7.5F, PartSize.SIZE_15, 1200000, 2400, 1598).setAuthor("The Master").setHealth(10F).setRarity(Rarity.UNCOMMON).setUnlocalizedName("mp_thruster_15_balefire_large_rad");

		mp_thruster_20_kerosene = new ItemCustomMissilePart().makeThruster(FuelType.KEROSENE, 1F, 100F, PartSize.SIZE_20, 936000, 2400, 722).setHealth(10F).setUnlocalizedName("mp_thruster_20_kerosene");
		mp_thruster_20_kerosene_dual = new ItemCustomMissilePart().makeThruster(FuelType.KEROSENE, 1F, 100F, PartSize.SIZE_20, 1234000, 3200, 722).setHealth(10F).setUnlocalizedName("mp_thruster_20_kerosene_dual");
		mp_thruster_20_kerosene_triple = new ItemCustomMissilePart().makeThruster(FuelType.KEROSENE, 1F, 100F, PartSize.SIZE_20, 1542000, 4400, 722).setHealth(10F).setUnlocalizedName("mp_thruster_20_kerosene_triple");
		mp_thruster_20_methalox = new ItemCustomMissilePart().makeThruster(FuelType.METHALOX, 1F, 100F, PartSize.SIZE_20, 890000, 2400, 768).setHealth(10F).setUnlocalizedName("mp_thruster_20_methalox");
		mp_thruster_20_methalox_dual = new ItemCustomMissilePart().makeThruster(FuelType.METHALOX, 1F, 100F, PartSize.SIZE_20, 1184000, 3200, 768).setHealth(10F).setUnlocalizedName("mp_thruster_20_methalox_dual");
		mp_thruster_20_methalox_triple = new ItemCustomMissilePart().makeThruster(FuelType.METHALOX, 1F, 100F, PartSize.SIZE_20, 1456000, 4400, 768).setHealth(10F).setUnlocalizedName("mp_thruster_20_methalox_triple");
		mp_thruster_20_hydrogen = new ItemCustomMissilePart().makeThruster(FuelType.HYDROGEN, 1F, 100F, PartSize.SIZE_20, 480000, 2600, 840).setHealth(10F).setUnlocalizedName("mp_thruster_20_hydrogen");
		mp_thruster_20_hydrogen_dual = new ItemCustomMissilePart().makeThruster(FuelType.HYDROGEN, 1F, 100F, PartSize.SIZE_20, 640000, 3600, 840).setHealth(10F).setUnlocalizedName("mp_thruster_20_hydrogen_dual");
		mp_thruster_20_hydrogen_triple = new ItemCustomMissilePart().makeThruster(FuelType.HYDROGEN, 1F, 100F, PartSize.SIZE_20, 820000, 4600, 840).setHealth(10F).setUnlocalizedName("mp_thruster_20_hydrogen_triple");
		mp_thruster_20_solid = new ItemCustomMissilePart().makeThruster(FuelType.SOLID, 1F, 100F, PartSize.SIZE_20, 1400000, 2400, 468).setHealth(10F).setWittyText("It's basically just a big hole at the end of the fuel tank.").setUnlocalizedName("mp_thruster_20_solid");
		mp_thruster_20_solid_multi = new ItemCustomMissilePart().makeThruster(FuelType.SOLID, 1F, 100F, PartSize.SIZE_20, 1830000, 3200, 468).setHealth(10F).setUnlocalizedName("mp_thruster_20_solid_multi");
		mp_thruster_20_solid_multier = new ItemCustomMissilePart().makeThruster(FuelType.SOLID, 1F, 100F, PartSize.SIZE_20, 2320000, 4000, 468).setHealth(10F).setWittyText("Did I miscount? Hope not.").setUnlocalizedName("mp_thruster_20_solid_multier");
		mp_thruster_20_hydrazine = new ItemCustomMissilePart().makeThruster(FuelType.HYDRAZINE, 1F, 100F, PartSize.SIZE_20, 600000, 1200, 528).setHealth(10F).setUnlocalizedName("mp_thruster_20_hydrazine");
		mp_thruster_10_hydrazine = new ItemCustomMissilePart().makeThruster(FuelType.HYDRAZINE, 1F, 1.5F, PartSize.SIZE_10, 14000, 120, 528).setHealth(10F).setUnlocalizedName("mp_thruster_10_hydrazine");

		mp_stability_10_flat = new ItemCustomMissilePart().makeStability(0.5F, PartSize.SIZE_10).setHealth(5F).setMaxStackSize(1).setUnlocalizedName("mp_stability_10_flat");
		mp_stability_10_cruise = new ItemCustomMissilePart().makeStability(0.25F, PartSize.SIZE_10).setHealth(5F).setMaxStackSize(1).setUnlocalizedName("mp_stability_10_cruise");
		mp_stability_10_space = new ItemCustomMissilePart().makeStability(0.35F, PartSize.SIZE_10).setHealth(5F).setRarity(Rarity.COMMON).setWittyText("Standing there alone, the ship is waiting / All systems are go, are you sure?").setMaxStackSize(1).setUnlocalizedName("mp_stability_10_space");
		mp_stability_15_flat = new ItemCustomMissilePart().makeStability(0.5F, PartSize.SIZE_15).setHealth(5F).setMaxStackSize(1).setUnlocalizedName("mp_stability_15_flat");
		mp_stability_15_thin = new ItemCustomMissilePart().makeStability(0.35F, PartSize.SIZE_15).setHealth(5F).setMaxStackSize(1).setUnlocalizedName("mp_stability_15_thin");
		mp_stability_15_soyuz = new ItemCustomMissilePart().makeStability(0.25F, PartSize.SIZE_15).setHealth(5F).setRarity(Rarity.COMMON).setWittyText("Союз!").setMaxStackSize(1).setUnlocalizedName("mp_stability_15_soyuz");
		mp_stability_20_flat = new ItemCustomMissilePart().makeStability(0.5F, PartSize.SIZE_20).setMaxStackSize(1).setUnlocalizedName("mp_s_20");

		mp_fuselage_10_kerosene = new ItemCustomMissilePart().makeFuselage(FuelType.KEROSENE, 2500, 1000, PartSize.SIZE_10, PartSize.SIZE_10).setAuthor("Hoboy").setHealth(20F).setUnlocalizedName("mp_fuselage_10_kerosene");
		mp_fuselage_10_kerosene_camo =			((ItemCustomMissilePart) mp_fuselage_10_kerosene).copy().setRarity(Rarity.COMMON).setTitle("Camo").setUnlocalizedName("mp_fuselage_10_kerosene_camo");
		mp_fuselage_10_kerosene_desert = 		((ItemCustomMissilePart) mp_fuselage_10_kerosene).copy().setRarity(Rarity.COMMON).setTitle("Desert Camo").setUnlocalizedName("mp_fuselage_10_kerosene_desert");
		mp_fuselage_10_kerosene_sky = 			((ItemCustomMissilePart) mp_fuselage_10_kerosene).copy().setRarity(Rarity.COMMON).setTitle("Sky Camo").setUnlocalizedName("mp_fuselage_10_kerosene_sky");
		mp_fuselage_10_kerosene_flames = 		((ItemCustomMissilePart) mp_fuselage_10_kerosene).copy().setRarity(Rarity.UNCOMMON).setTitle("Sick Flames").setUnlocalizedName("mp_fuselage_10_kerosene_flames");
		mp_fuselage_10_kerosene_insulation = 	((ItemCustomMissilePart) mp_fuselage_10_kerosene).copy().setRarity(Rarity.COMMON).setTitle("Orange Insulation").setHealth(20F).setUnlocalizedName("mp_fuselage_10_kerosene_insulation");
		mp_fuselage_10_kerosene_sleek = 		((ItemCustomMissilePart) mp_fuselage_10_kerosene).copy().setRarity(Rarity.RARE).setTitle("IF-R&D").setHealth(20F).setUnlocalizedName("mp_fuselage_10_kerosene_sleek");
		mp_fuselage_10_kerosene_metal = 		((ItemCustomMissilePart) mp_fuselage_10_kerosene).copy().setRarity(Rarity.UNCOMMON).setTitle("Bolted Metal").setHealth(20F).setAuthor("Hoboy").setUnlocalizedName("mp_fuselage_10_kerosene_metal");
		mp_fuselage_10_kerosene_taint =			((ItemCustomMissilePart) mp_fuselage_10_kerosene).copy().setRarity(Rarity.UNCOMMON).setAuthor("Sam").setTitle("Tainted").setUnlocalizedName("mp_fuselage_10_kerosene_taint");

		mp_fuselage_10_solid = new ItemCustomMissilePart().makeFuselage(FuelType.SOLID, 2500, 1000, PartSize.SIZE_10, PartSize.SIZE_10).setHealth(20F)			.setUnlocalizedName("mp_fuselage_10_solid");
		mp_fuselage_10_solid_flames = 		((ItemCustomMissilePart) mp_fuselage_10_solid).copy().setRarity(Rarity.UNCOMMON).setTitle("Sick Flames").setUnlocalizedName("mp_fuselage_10_solid_flames");
		mp_fuselage_10_solid_insulation = 	((ItemCustomMissilePart) mp_fuselage_10_solid).copy().setRarity(Rarity.COMMON).setTitle("Orange Insulation").setHealth(20F).setUnlocalizedName("mp_fuselage_10_solid_insulation");
		mp_fuselage_10_solid_sleek = 		((ItemCustomMissilePart) mp_fuselage_10_solid).copy().setRarity(Rarity.RARE).setTitle("IF-R&D").setHealth(20F).setUnlocalizedName("mp_fuselage_10_solid_sleek");
		mp_fuselage_10_solid_soviet_glory = ((ItemCustomMissilePart) mp_fuselage_10_solid).copy().setRarity(Rarity.EPIC).setAuthor("Hoboy").setHealth(20F).setTitle("Soviet Glory").setUnlocalizedName("mp_fuselage_10_solid_soviet_glory");
		mp_fuselage_10_solid_cathedral = 	((ItemCustomMissilePart) mp_fuselage_10_solid).copy().setRarity(Rarity.RARE).setAuthor("Satan").setTitle("Unholy Cathedral").setWittyText("Quakeesque!").setUnlocalizedName("mp_fuselage_10_solid_cathedral");
		mp_fuselage_10_solid_moonlit = 		((ItemCustomMissilePart) mp_fuselage_10_solid).copy().setRarity(Rarity.UNCOMMON).setAuthor("The Master & Hoboy").setTitle("Moonlit").setUnlocalizedName("mp_fuselage_10_solid_moonlit");
		mp_fuselage_10_solid_battery = 		((ItemCustomMissilePart) mp_fuselage_10_solid).copy().setRarity(Rarity.UNCOMMON).setAuthor("wolfmonster222").setHealth(20F).setTitle("Ecstatic").setWittyText("I got caught eating batteries again :(").setUnlocalizedName("mp_fuselage_10_solid_battery");
		mp_fuselage_10_solid_duracell = 	((ItemCustomMissilePart) mp_fuselage_10_solid).copy().setRarity(Rarity.RARE).setAuthor("Hoboy").setTitle("Duracell").setHealth(20F).setWittyText("The crunchiest battery on the market!").setUnlocalizedName("mp_fuselage_10_solid_duracell");

		mp_fuselage_10_xenon = new ItemCustomMissilePart().makeFuselage(FuelType.XENON, 5000, 1000, PartSize.SIZE_10, PartSize.SIZE_10).setHealth(20F)			.setUnlocalizedName("mp_fuselage_10_xenon");
		mp_fuselage_10_xenon_bhole = 	((ItemCustomMissilePart) mp_fuselage_10_xenon).copy().setRarity(Rarity.RARE).setAuthor("Sten89").setTitle("Morceus-1457").setUnlocalizedName("mp_fuselage_10_xenon_bhole");
		mp_fuselage_10_hydrazine = new ItemCustomMissilePart().makeFuselage(FuelType.HYDRAZINE, 2000, 1000, PartSize.SIZE_10, PartSize.SIZE_10).setHealth(20F)			.setUnlocalizedName("mp_fuselage_10_hydrazine");
		rp_fuselage_20_12_hydrazine = new ItemCustomMissilePart().makeFuselage(FuelType.HYDRAZINE, 12500, 1000, PartSize.SIZE_20, PartSize.SIZE_20).setHealth(20F)			.setUnlocalizedName("rp_fuselage_20_12_hydrazine");

		mp_fuselage_10_long_kerosene = new ItemCustomMissilePart().makeFuselage(FuelType.KEROSENE, 5000, 1000, PartSize.SIZE_10, PartSize.SIZE_10).setAuthor("Hoboy").setHealth(20F).setUnlocalizedName("mp_fuselage_10_long_kerosene");
		mp_fuselage_10_long_kerosene_camo = 		((ItemCustomMissilePart) mp_fuselage_10_long_kerosene).copy().setRarity(Rarity.COMMON).setTitle("Camo").setUnlocalizedName("mp_fuselage_10_long_kerosene_camo");
		mp_fuselage_10_long_kerosene_desert = 		((ItemCustomMissilePart) mp_fuselage_10_long_kerosene).copy().setRarity(Rarity.COMMON).setTitle("Desert Camo").setUnlocalizedName("mp_fuselage_10_long_kerosene_desert");
		mp_fuselage_10_long_kerosene_sky = 			((ItemCustomMissilePart) mp_fuselage_10_long_kerosene).copy().setRarity(Rarity.COMMON).setTitle("Sky Camo").setUnlocalizedName("mp_fuselage_10_long_kerosene_sky");
		mp_fuselage_10_long_kerosene_flames = 		((ItemCustomMissilePart) mp_fuselage_10_long_kerosene).copy().setRarity(Rarity.UNCOMMON).setTitle("Sick Flames").setUnlocalizedName("mp_fuselage_10_long_kerosene_flames");
		mp_fuselage_10_long_kerosene_insulation = 	((ItemCustomMissilePart) mp_fuselage_10_long_kerosene).copy().setRarity(Rarity.COMMON).setTitle("Orange Insulation").setHealth(20F).setUnlocalizedName("mp_fuselage_10_long_kerosene_insulation");
		mp_fuselage_10_long_kerosene_sleek =		((ItemCustomMissilePart) mp_fuselage_10_long_kerosene).copy().setRarity(Rarity.RARE).setTitle("IF-R&D").setHealth(20F).setUnlocalizedName("mp_fuselage_10_long_kerosene_sleek");
		mp_fuselage_10_long_kerosene_metal = 		((ItemCustomMissilePart) mp_fuselage_10_long_kerosene).copy().setRarity(Rarity.UNCOMMON).setAuthor("Hoboy").setHealth(20F).setUnlocalizedName("mp_fuselage_10_long_kerosene_metal");
		mp_fuselage_10_long_kerosene_dash = 		((ItemCustomMissilePart) mp_fuselage_10_long_kerosene).copy().setRarity(Rarity.EPIC).setAuthor("Sam").setTitle("Dash").setWittyText("I wash my hands of it.").setCreativeTab(MainRegistry.partsTab).setUnlocalizedName("mp_fuselage_10_long_kerosene_dash");
		mp_fuselage_10_long_kerosene_taint = 		((ItemCustomMissilePart) mp_fuselage_10_long_kerosene).copy().setRarity(Rarity.UNCOMMON).setAuthor("Sam").setTitle("Tainted").setUnlocalizedName("mp_fuselage_10_long_kerosene_taint");
		mp_fuselage_10_long_kerosene_vap = 			((ItemCustomMissilePart) mp_fuselage_10_long_kerosene).copy().setRarity(Rarity.EPIC).setAuthor("VT-6/24").setTitle("Minty Contrail").setWittyText("Upper rivet!").setUnlocalizedName("mp_fuselage_10_long_kerosene_vap");

		mp_fuselage_10_long_solid = new ItemCustomMissilePart().makeFuselage(FuelType.SOLID, 5000, 1000, PartSize.SIZE_10, PartSize.SIZE_10).setHealth(20F)	.setUnlocalizedName("mp_fuselage_10_long_solid");
		mp_fuselage_10_long_solid_flames = 			((ItemCustomMissilePart) mp_fuselage_10_long_solid).copy().setRarity(Rarity.UNCOMMON).setTitle("Sick Flames").setUnlocalizedName("mp_fuselage_10_long_solid_flames");
		mp_fuselage_10_long_solid_insulation = 		((ItemCustomMissilePart) mp_fuselage_10_long_solid).copy().setRarity(Rarity.COMMON).setTitle("Orange Insulation").setHealth(20F).setUnlocalizedName("mp_fuselage_10_long_solid_insulation");
		mp_fuselage_10_long_solid_sleek = 			((ItemCustomMissilePart) mp_fuselage_10_long_solid).copy().setRarity(Rarity.RARE).setTitle("IF-R&D").setHealth(20F).setUnlocalizedName("mp_fuselage_10_long_solid_sleek");
		mp_fuselage_10_long_solid_soviet_glory = 	((ItemCustomMissilePart) mp_fuselage_10_long_solid).copy().setRarity(Rarity.EPIC).setAuthor("Hoboy").setHealth(20F).setTitle("Soviet Glory").setWittyText("Fully Automated Luxury Gay Space Communism!").setUnlocalizedName("mp_fuselage_10_long_solid_soviet_glory");
		mp_fuselage_10_long_solid_bullet = 			((ItemCustomMissilePart) mp_fuselage_10_long_solid).copy().setRarity(Rarity.COMMON).setAuthor("Sam").setTitle("Bullet Bill").setUnlocalizedName("mp_fuselage_10_long_solid_bullet");
		mp_fuselage_10_long_solid_silvermoonlight = ((ItemCustomMissilePart) mp_fuselage_10_long_solid).copy().setRarity(Rarity.UNCOMMON).setAuthor("The Master").setTitle("Silver Moonlight").setUnlocalizedName("mp_fuselage_10_long_solid_silvermoonlight");

		mp_fuselage_10_15_kerosene = new ItemCustomMissilePart().makeFuselage(FuelType.KEROSENE, 10000, 1000, PartSize.SIZE_10, PartSize.SIZE_15).setHealth(20F).setUnlocalizedName("mp_fuselage_10_15_kerosene");
		mp_fuselage_10_15_solid = new ItemCustomMissilePart().makeFuselage(FuelType.SOLID, 10000, 1000, PartSize.SIZE_10, PartSize.SIZE_15).setHealth(20F)		.setUnlocalizedName("mp_fuselage_10_15_solid");
		mp_fuselage_10_15_hydrogen = new ItemCustomMissilePart().makeFuselage(FuelType.HYDROGEN, 10000, 1000, PartSize.SIZE_10, PartSize.SIZE_15).setHealth(20F).setUnlocalizedName("mp_fuselage_10_15_hydrogen");

		mp_fuselage_10_15_balefire = new ItemCustomMissilePart().makeFuselage(FuelType.HYDROGEN, 10000, 1000, PartSize.SIZE_10, PartSize.SIZE_15).setHealth(20F).setUnlocalizedName("mp_fuselage_10_15_balefire");

		mp_fuselage_15_kerosene = new ItemCustomMissilePart().makeFuselage(FuelType.KEROSENE, 15000, 1000, PartSize.SIZE_15, PartSize.SIZE_15).setAuthor("Hoboy").setHealth(20F).setUnlocalizedName("mp_fuselage_15_kerosene");
		mp_fuselage_15_kerosene_camo = 			((ItemCustomMissilePart) mp_fuselage_15_kerosene).copy().setRarity(Rarity.COMMON).setTitle("Camo").setUnlocalizedName("mp_fuselage_15_kerosene_camo");
		mp_fuselage_15_kerosene_desert = 		((ItemCustomMissilePart) mp_fuselage_15_kerosene).copy().setRarity(Rarity.COMMON).setTitle("Desert Camo").setUnlocalizedName("mp_fuselage_15_kerosene_desert");
		mp_fuselage_15_kerosene_sky = 			((ItemCustomMissilePart) mp_fuselage_15_kerosene).copy().setRarity(Rarity.COMMON).setTitle("Sky Camo").setUnlocalizedName("mp_fuselage_15_kerosene_sky");
		mp_fuselage_15_kerosene_insulation = 	((ItemCustomMissilePart) mp_fuselage_15_kerosene).copy().setRarity(Rarity.COMMON).setTitle("Orange Insulation").setHealth(20F).setWittyText("Rest in spaghetti Columbia :(").setUnlocalizedName("mp_fuselage_15_kerosene_insulation");
		mp_fuselage_15_kerosene_metal = 		((ItemCustomMissilePart) mp_fuselage_15_kerosene).copy().setRarity(Rarity.UNCOMMON).setAuthor("Hoboy").setTitle("Bolted Metal").setHealth(20F).setWittyText("Metal frame with metal plating reinforced with bolted metal sheets and metal.").setUnlocalizedName("mp_fuselage_15_kerosene_metal");
		mp_fuselage_15_kerosene_decorated = 	((ItemCustomMissilePart) mp_fuselage_15_kerosene).copy().setRarity(Rarity.UNCOMMON).setAuthor("Hoboy").setTitle("Decorated").setHealth(20F).setUnlocalizedName("mp_fuselage_15_kerosene_decorated");
		mp_fuselage_15_kerosene_steampunk = 	((ItemCustomMissilePart) mp_fuselage_15_kerosene).copy().setRarity(Rarity.RARE).setAuthor("Hoboy").setTitle("Steampunk").setHealth(20F).setUnlocalizedName("mp_fuselage_15_kerosene_steampunk");
		mp_fuselage_15_kerosene_polite = 		((ItemCustomMissilePart) mp_fuselage_15_kerosene).copy().setRarity(Rarity.LEGENDARY).setAuthor("Hoboy").setTitle("Polite").setHealth(20F).setUnlocalizedName("mp_fuselage_15_kerosene_polite");
		mp_fuselage_15_kerosene_blackjack = 	((ItemCustomMissilePart) mp_fuselage_15_kerosene).copy().setRarity(Rarity.LEGENDARY).setTitle("Queen Whiskey").setHealth(20F).setUnlocalizedName("mp_fuselage_15_kerosene_blackjack");
		mp_fuselage_15_kerosene_lambda = 		((ItemCustomMissilePart) mp_fuselage_15_kerosene).copy().setRarity(Rarity.RARE).setAuthor("VT-6/24").setTitle("Lambda Complex").setHealth(20F).setWittyText("MAGNIFICENT MICROWAVE CASSEROLE").setUnlocalizedName("mp_fuselage_15_kerosene_lambda");
		mp_fuselage_15_kerosene_minuteman = 	((ItemCustomMissilePart) mp_fuselage_15_kerosene).copy().setRarity(Rarity.UNCOMMON).setAuthor("Spexta").setTitle("MX 1702").setUnlocalizedName("mp_fuselage_15_kerosene_minuteman");
		mp_fuselage_15_kerosene_pip = 			((ItemCustomMissilePart) mp_fuselage_15_kerosene).copy().setRarity(Rarity.EPIC).setAuthor("The Doctor").setTitle("LittlePip").setWittyText("31!").setCreativeTab(MainRegistry.partsTab).setUnlocalizedName("mp_fuselage_15_kerosene_pip");
		mp_fuselage_15_kerosene_taint = 		((ItemCustomMissilePart) mp_fuselage_15_kerosene).copy().setRarity(Rarity.UNCOMMON).setAuthor("Sam").setTitle("Tainted").setWittyText("DUN-DUN!").setUnlocalizedName("mp_fuselage_15_kerosene_taint");
		mp_fuselage_15_kerosene_yuck = 			((ItemCustomMissilePart) mp_fuselage_15_kerosene).copy().setRarity(Rarity.EPIC).setAuthor("Hoboy").setTitle("Flesh").setWittyText("Note: Never clean DNA vials with your own spit.").setHealth(60F).setUnlocalizedName("mp_fuselage_15_kerosene_yuck");

		mp_fuselage_15_solid = new ItemCustomMissilePart().makeFuselage(FuelType.SOLID, 15000, 1000, PartSize.SIZE_15, PartSize.SIZE_15).setHealth(20F)			.setUnlocalizedName("mp_fuselage_15_solid").setTextureName(RefStrings.MODID + ":mp_fuselage");
		mp_fuselage_15_solid_insulation = 		((ItemCustomMissilePart) mp_fuselage_15_solid).copy().setRarity(Rarity.COMMON).setTitle("Orange Insulation").setHealth(20F).setUnlocalizedName("mp_fuselage_15_solid_insulation");
		mp_fuselage_15_solid_desh = 			((ItemCustomMissilePart) mp_fuselage_15_solid).copy().setRarity(Rarity.RARE).setAuthor("Hoboy").setTitle("Desh Plating").setHealth(20F).setUnlocalizedName("mp_fuselage_15_solid_desh");
		mp_fuselage_15_solid_soviet_glory = 	((ItemCustomMissilePart) mp_fuselage_15_solid).copy().setRarity(Rarity.RARE).setAuthor("Hoboy").setTitle("Soviet Glory").setHealth(20F).setUnlocalizedName("mp_fuselage_15_solid_soviet_glory");
		mp_fuselage_15_solid_soviet_stank = 	((ItemCustomMissilePart) mp_fuselage_15_solid).copy().setRarity(Rarity.EPIC).setAuthor("Hoboy").setTitle("Soviet Stank").setHealth(20F).setWittyText("Aged like a fine wine! Well, almost.").setUnlocalizedName("mp_fuselage_15_solid_soviet_stank");
		mp_fuselage_15_solid_faust = 			((ItemCustomMissilePart) mp_fuselage_15_solid).copy().setRarity(Rarity.LEGENDARY).setAuthor("Dr.Nostalgia").setTitle("Mighty Lauren").setHealth(20F).setWittyText("Welcome to Subway, may I take your order?").setUnlocalizedName("mp_fuselage_15_solid_faust"); //porn addict btw
		mp_fuselage_15_solid_silvermoonlight = 	((ItemCustomMissilePart) mp_fuselage_15_solid).copy().setRarity(Rarity.UNCOMMON).setAuthor("The Master").setTitle("Silver Moonlight").setUnlocalizedName("mp_fuselage_15_solid_silvermoonlight");
		mp_fuselage_15_solid_snowy = 			((ItemCustomMissilePart) mp_fuselage_15_solid).copy().setRarity(Rarity.UNCOMMON).setAuthor("Dr.Nostalgia").setTitle("Chilly Day").setUnlocalizedName("mp_fuselage_15_solid_snowy");
		mp_fuselage_15_solid_panorama = 		((ItemCustomMissilePart) mp_fuselage_15_solid).copy().setRarity(Rarity.RARE).setAuthor("Hoboy").setTitle("Panorama").setUnlocalizedName("mp_fuselage_15_solid_panorama");
		mp_fuselage_15_solid_roses = 			((ItemCustomMissilePart) mp_fuselage_15_solid).copy().setRarity(Rarity.UNCOMMON).setAuthor("Hoboy").setTitle("Bed of roses").setUnlocalizedName("mp_fuselage_15_solid_roses");
		mp_fuselage_15_solid_mimi = 			((ItemCustomMissilePart) mp_fuselage_15_solid).copy().setRarity(Rarity.RARE).setTitle("Mimi-chan").setUnlocalizedName("mp_fuselage_15_solid_mimi");

		mp_fuselage_15_hydrogen = new ItemCustomMissilePart().makeFuselage(FuelType.HYDROGEN, 15000, 1000, PartSize.SIZE_15, PartSize.SIZE_15).setHealth(20F)	.setUnlocalizedName("mp_fuselage_15_hydrogen").setTextureName(RefStrings.MODID + ":mp_fuselage");
		mp_fuselage_15_hydrogen_cathedral = ((ItemCustomMissilePart) mp_fuselage_15_hydrogen).copy().setRarity(Rarity.UNCOMMON).setAuthor("Satan").setTitle("Unholy Cathedral").setUnlocalizedName("mp_fuselage_15_hydrogen_cathedral");

		//mp_fuselage_15_balefire = new ItemCustomMissilePart().makeFuselage(FuelType.BALEFIRE, 15000, 1000, PartSize.SIZE_15, PartSize.SIZE_15).setHealth(75F)	.setUnlocalizedName("mp_fuselage_15_balefire").setTextureName(RefStrings.MODID + ":mp_fuselage");

		mp_fuselage_15_20_kerosene = new ItemCustomMissilePart().makeFuselage(FuelType.KEROSENE, 20000, 1000, PartSize.SIZE_15, PartSize.SIZE_20).setAuthor("Hoboy").setHealth(20F).setUnlocalizedName("mp_fuselage_15_20_kerosene").setCreativeTab(MainRegistry.missileTab).setTextureName(RefStrings.MODID + ":mp_fuselage");
		mp_fuselage_15_20_kerosene_magnusson = ((ItemCustomMissilePart)mp_fuselage_15_20_kerosene).copy().setRarity(Rarity.RARE).setAuthor("VT-6/24").setTitle("White Forest Rocket").setWittyText("And get your cranio-conjugal parasite away from my nose cone!").setUnlocalizedName("mp_fuselage_15_20_kerosene_magnusson");
		mp_fuselage_15_20_solid = new ItemCustomMissilePart().makeFuselage(FuelType.SOLID, 20000, 1000, PartSize.SIZE_15, PartSize.SIZE_20).setHealth(20F).setUnlocalizedName("mp_fuselage_15_20_solid").setTextureName(RefStrings.MODID + ":mp_fuselage");

		mp_warhead_10_he = new ItemCustomMissilePart().makeWarhead(WarheadType.HE, 15F, 1500, PartSize.SIZE_10).setHealth(5F)								.setUnlocalizedName("mp_warhead_10_he").setMaxStackSize(1).setTextureName(RefStrings.MODID + ":mp_warhead");
		mp_warhead_10_incendiary = new ItemCustomMissilePart().makeWarhead(WarheadType.INC, 15F, 1500, PartSize.SIZE_10).setHealth(5F)					.setUnlocalizedName("mp_warhead_10_incendiary").setMaxStackSize(1).setTextureName(RefStrings.MODID + ":mp_warhead");
		mp_warhead_10_buster = new ItemCustomMissilePart().makeWarhead(WarheadType.BUSTER, 5F, 1500, PartSize.SIZE_10).setHealth(5F)						.setUnlocalizedName("mp_warhead_10_buster").setMaxStackSize(1).setTextureName(RefStrings.MODID + ":mp_warhead");
		mp_warhead_10_nuclear = new ItemCustomMissilePart().makeWarhead(WarheadType.NUCLEAR, BombConfig.radiusFromKt(5F), 1500, PartSize.SIZE_10).setTitle("Tater Tot").setHealth(5F).setUnlocalizedName("mp_warhead_10_nuclear").setMaxStackSize(1).setTextureName(RefStrings.MODID + ":mp_warhead");
		mp_warhead_10_nuclear_large = new ItemCustomMissilePart().makeWarhead(WarheadType.NUCLEAR, BombConfig.radiusFromKt(20F), 2500, PartSize.SIZE_10).setTitle("Chernobyl Boris").setHealth(5F).setUnlocalizedName("mp_warhead_10_nuclear_large").setMaxStackSize(1).setTextureName(RefStrings.MODID + ":mp_warhead");
		mp_warhead_15_he = new ItemCustomMissilePart().makeWarhead(WarheadType.HE, 50F, 2500, PartSize.SIZE_15).setHealth(5F)							.setUnlocalizedName("mp_warhead_15_he").setMaxStackSize(1).setTextureName(RefStrings.MODID + ":mp_warhead");
		mp_warhead_15_incendiary = new ItemCustomMissilePart().makeWarhead(WarheadType.INC, 35F, 2500, PartSize.SIZE_15).setHealth(5F)					.setUnlocalizedName("mp_warhead_15_incendiary").setMaxStackSize(1).setTextureName(RefStrings.MODID + ":mp_warhead");
		mp_warhead_15_nuclear = new ItemCustomMissilePart().makeWarhead(WarheadType.NUCLEAR, BombConfig.radiusFromKt(150F), 5000, PartSize.SIZE_15).setTitle("Auntie Bertha").setHealth(5F).setUnlocalizedName("mp_warhead_15_nuclear").setMaxStackSize(1).setTextureName(RefStrings.MODID + ":mp_warhead");
		mp_warhead_15_nuclear_shark = ((ItemCustomMissilePart) mp_warhead_15_nuclear).copy().setRarity(Rarity.UNCOMMON).setTitle("Discount Bullet Bill").setWittyText("Nose art on a cannon bullet? Who does that?").setUnlocalizedName("mp_warhead_15_nuclear_shark");
		mp_warhead_15_nuclear_mimi = ((ItemCustomMissilePart) mp_warhead_15_nuclear).copy().setRarity(Rarity.RARE).setTitle("FASHIONABLE MISSILE").setUnlocalizedName("mp_warhead_15_nuclear_mimi");
		mp_warhead_15_n2 = new ItemCustomMissilePart().makeWarhead(WarheadType.N2, 100F, 5000, PartSize.SIZE_15).setWittyText("[screams geometrically]").setHealth(5F).setRarity(Rarity.RARE).setUnlocalizedName("mp_warhead_15_n2").setMaxStackSize(1).setTextureName(RefStrings.MODID + ":mp_warhead");
		mp_warhead_20_he = new ItemCustomMissilePart().makeWarhead(WarheadType.HE, 15F, 5000, PartSize.SIZE_20)												.setUnlocalizedName("mp_w_20").setMaxStackSize(1).setTextureName(RefStrings.MODID + ":mp_warhead");

		mp_chip_1 = new ItemCustomMissilePart().makeChip(0.13F)	.setUnlocalizedName("mp_c_1").setMaxStackSize(1).setTextureName(RefStrings.MODID + ":mp_c_1");
		mp_chip_2 = new ItemCustomMissilePart().makeChip(0.07F)	.setUnlocalizedName("mp_c_2").setMaxStackSize(1).setTextureName(RefStrings.MODID + ":mp_c_2");
		mp_chip_3 = new ItemCustomMissilePart().makeChip(0.06F)	.setUnlocalizedName("mp_c_3").setMaxStackSize(1).setTextureName(RefStrings.MODID + ":mp_c_3");
		mp_chip_4 = new ItemCustomMissilePart().makeChip(0.008F)	.setUnlocalizedName("mp_c_4").setMaxStackSize(1).setTextureName(RefStrings.MODID + ":mp_c_4");
		mp_chip_5 = new ItemCustomMissilePart().makeChip(0.001F)	.setUnlocalizedName("mp_c_5").setMaxStackSize(1).setTextureName(RefStrings.MODID + ":mp_c_5");

		//space crap
		rp_fuselage_20_12 = new ItemCustomMissilePart().makeFuselage(FuelType.ANY, 64_000, 4000, PartSize.SIZE_20, PartSize.SIZE_20).setUnlocalizedName("rp_f_20_12").setTextureName(RefStrings.MODID + ":mp_fuselage");
		rp_fuselage_20_6 = new ItemCustomMissilePart().makeFuselage(FuelType.ANY, 32_000, 2100, PartSize.SIZE_20, PartSize.SIZE_20).setUnlocalizedName("rp_f_20_6").setTextureName(RefStrings.MODID + ":mp_fuselage");
		rp_fuselage_20_3 = new ItemCustomMissilePart().makeFuselage(FuelType.ANY, 16_000, 1200, PartSize.SIZE_20, PartSize.SIZE_20).setUnlocalizedName("rp_f_20_3").setTextureName(RefStrings.MODID + ":mp_fuselage");
		rp_fuselage_20_1 = new ItemCustomMissilePart().makeFuselage(FuelType.ANY, 6_000, 500, PartSize.SIZE_20, PartSize.SIZE_20).setUnlocalizedName("rp_f_20_1").setTextureName(RefStrings.MODID + ":mp_fuselage");

		rp_legs_20 = new ItemCustomMissilePart().makeStability(0, PartSize.SIZE_20).setMaxStackSize(1).setUnlocalizedName("rp_l_20").setTextureName(RefStrings.MODID + ":mp_fuselage");

		rp_capsule_20 = new ItemCustomMissilePart().makeWarhead(WarheadType.APOLLO, 15F, 8_000, PartSize.SIZE_20).setUnlocalizedName("rp_c_20").setMaxStackSize(1).setCreativeTab(MainRegistry.missileTab).setTextureName(RefStrings.MODID + ":mp_warhead");
		rp_station_core_20 = new ItemCustomMissilePart().makeWarhead(WarheadType.SATELLITE, 15F, 64_000, PartSize.SIZE_20).setUnlocalizedName("rp_sc_20").setMaxStackSize(1).setCreativeTab(MainRegistry.missileTab).setTextureName(RefStrings.MODID + ":mp_warhead");
		rp_pod_20 = new ItemCustomMissilePart().makeWarhead(WarheadType.APOLLO, 15F, 4_000, PartSize.SIZE_20).setUnlocalizedName("rp_pod_20").setMaxStackSize(1).setCreativeTab(MainRegistry.missileTab).setTextureName(RefStrings.MODID + ":mp_warhead");

		missile_skin_camo = new ItemCustomLore().setUnlocalizedName("missile_skin_camo").setMaxStackSize(1).setCreativeTab(MainRegistry.missileTab).setTextureName(RefStrings.MODID + ":missile_skin_camo");
		missile_skin_desert = new ItemCustomLore().setUnlocalizedName("missile_skin_desert").setMaxStackSize(1).setCreativeTab(MainRegistry.missileTab).setTextureName(RefStrings.MODID + ":missile_skin_desert");
		missile_skin_flames = new ItemCustomLore().setUnlocalizedName("missile_skin_flames").setMaxStackSize(1).setCreativeTab(MainRegistry.missileTab).setTextureName(RefStrings.MODID + ":missile_skin_flames");
		missile_skin_manly_pink = new ItemCustomLore().setUnlocalizedName("missile_skin_manly_pink").setMaxStackSize(1).setCreativeTab(MainRegistry.missileTab).setTextureName(RefStrings.MODID + ":missile_skin_manly_pink");
		missile_skin_orange_insulation = new ItemCustomLore().setUnlocalizedName("missile_skin_orange_insulation").setMaxStackSize(1).setCreativeTab(MainRegistry.missileTab).setTextureName(RefStrings.MODID + ":missile_skin_orange_insulation");
		missile_skin_sleek = new ItemCustomLore().setUnlocalizedName("missile_skin_sleek").setMaxStackSize(1).setCreativeTab(MainRegistry.missileTab).setTextureName(RefStrings.MODID + ":missile_skin_sleek");
		missile_skin_soviet_glory = new ItemCustomLore().setUnlocalizedName("missile_skin_soviet_glory").setMaxStackSize(1).setCreativeTab(MainRegistry.missileTab).setTextureName(RefStrings.MODID + ":missile_skin_soviet_glory");
		missile_skin_soviet_stank = new ItemCustomLore().setUnlocalizedName("missile_skin_soviet_stank").setMaxStackSize(1).setCreativeTab(MainRegistry.missileTab).setTextureName(RefStrings.MODID + ":missile_skin_soviet_stank");
		missile_skin_metal = new ItemCustomLore().setUnlocalizedName("missile_skin_metal").setMaxStackSize(1).setCreativeTab(MainRegistry.missileTab).setTextureName(RefStrings.MODID + ":missile_skin_metal");

		ammo_12gauge = new ItemAmmo(Ammo12Gauge.class).setUnlocalizedName("ammo_12gauge");
		ammo_20gauge = new ItemAmmo(Ammo20Gauge.class).setUnlocalizedName("ammo_20gauge");
		ammo_4gauge = new ItemAmmo(Ammo4Gauge.class).setUnlocalizedName("ammo_4gauge");
		ammo_5mm = new ItemAmmo(Ammo5mm.class).setUnlocalizedName("ammo_5mm");
		ammo_9mm = new ItemAmmo(Ammo9mm.class).setUnlocalizedName("ammo_9mm");
		ammo_45 = new ItemAmmo(Ammo45ACP.class).setUnlocalizedName("ammo_45");
		ammo_556 = new ItemAmmo(Ammo556mm.class).setUnlocalizedName("ammo_556");
		ammo_762 = new ItemAmmo(Ammo762NATO.class).setUnlocalizedName("ammo_762");
		ammo_50ae = new ItemAmmo(Ammo50AE.class).setUnlocalizedName("ammo_50ae");
		ammo_50bmg = new ItemAmmo(Ammo50BMG.class).setUnlocalizedName("ammo_50bmg");
		ammo_75bolt = new ItemAmmo(Ammo75Bolt.class).setUnlocalizedName("ammo_75bolt");
		ammo_357 = new ItemAmmo(Ammo357Magnum.class).setUnlocalizedName("ammo_357");
		ammo_44 = new ItemAmmo(Ammo44Magnum.class).setUnlocalizedName("ammo_44");
		ammo_22lr = new ItemAmmo(Ammo22LR.class).setUnlocalizedName("ammo_22lr");
		ammo_rocket = new ItemAmmo(AmmoRocket.class).setUnlocalizedName("ammo_rocket");
		ammo_grenade = new ItemAmmo(AmmoGrenade.class).setUnlocalizedName("ammo_grenade");
		ammo_shell = new ItemAmmo(Ammo240Shell.class).setUnlocalizedName("ammo_shell");
		ammo_dgk = new ItemCustomLore().setUnlocalizedName("ammo_dgk").setCreativeTab(MainRegistry.weaponTab);
		ammo_nuke = new ItemAmmo(AmmoFatman.class).setUnlocalizedName("ammo_nuke");
		ammo_fuel = new ItemAmmo(AmmoFlamethrower.class).setUnlocalizedName("ammo_fuel");
		ammo_fireext = new ItemAmmo(AmmoFireExt.class).setUnlocalizedName("ammo_fireext");
		ammo_cell = new ItemCustomLore().setCreativeTab(MainRegistry.weaponTab).setUnlocalizedName("ammo_cell").setMaxStackSize(16);
		ammo_coilgun = new ItemAmmo(AmmoCoilgun.class).setUnlocalizedName("ammo_coilgun");
		ammo_dart = (ItemEnumMulti) new ItemAmmo(AmmoDart.class).setUnlocalizedName("ammo_dart").setMaxStackSize(16);
		ammo_stinger_rocket = new ItemAmmo(AmmoStinger.class).setUnlocalizedName("ammo_stinger_rocket");
		ammo_luna_sniper = new ItemAmmo(AmmoLunaticSniper.class).setUnlocalizedName("ammo_luna_sniper");
		ammo_misc = new ItemAmmo(AmmoMisc.class).setUnlocalizedName("ammo_misc");
		ammo_folly = new ItemCustomLore().setUnlocalizedName("ammo_folly");
		ammo_folly_nuclear = new ItemCustomLore().setUnlocalizedName("ammo_folly_nuclear");
		ammo_folly_du = new ItemCustomLore().setUnlocalizedName("ammo_folly_du");
		ammo_arty = new ItemAmmoArty().setUnlocalizedName("ammo_arty");
		ammo_himars = new ItemAmmoHIMARS().setUnlocalizedName("ammo_himars");

		gun_rpg = new ItemGunBase(GunRocketFactory.getGustavConfig()).setUnlocalizedName("gun_rpg").setCreativeTab(MainRegistry.weaponTab).setTextureName(RefStrings.MODID + ":gun_rpg");
		gun_karl = new ItemGunBase(GunRocketFactory.getKarlConfig()).setUnlocalizedName("gun_karl").setCreativeTab(MainRegistry.weaponTab).setTextureName(RefStrings.MODID + ":gun_karl");
		gun_panzerschreck = new ItemGunBase(GunRocketFactory.getPanzConfig()).setUnlocalizedName("gun_panzerschreck").setCreativeTab(MainRegistry.weaponTab).setTextureName(RefStrings.MODID + ":gun_panzerschreck");
		gun_quadro = new ItemGunBase(GunRocketFactory.getQuadroConfig()).setUnlocalizedName("gun_quadro").setCreativeTab(MainRegistry.weaponTab).setTextureName(RefStrings.MODID + ":gun_quadro");
		//gun_hk69 = new ItemGunBase(GunGrenadeFactory.getHK69Config()).setUnlocalizedName("gun_hk69").setCreativeTab(MainRegistry.weaponTab).setTextureName(RefStrings.MODID + ":gun_hk69");
		//gun_congolake = new ItemGunBase(GunGrenadeFactory.getCongoConfig()).setUnlocalizedName("gun_congolake").setCreativeTab(MainRegistry.weaponTab).setTextureName(RefStrings.MODID + ":gun_congolake");
		gun_stinger = new ItemGunBase(GunRocketHomingFactory.getStingerConfig()).setUnlocalizedName("gun_stinger").setCreativeTab(MainRegistry.weaponTab).setTextureName(RefStrings.MODID + ":gun_stinger");
		gun_skystinger = new ItemGunBase(GunRocketHomingFactory.getSkyStingerConfig()).setUnlocalizedName("gun_skystinger").setCreativeTab(MainRegistry.weaponTab).setTextureName(RefStrings.MODID + ":gun_skystinger");
		//gun_revolver = new ItemGunBase(Gun357MagnumFactory.getRevolverConfig()).setUnlocalizedName("gun_revolver").setCreativeTab(MainRegistry.weaponTab).setTextureName(RefStrings.MODID + ":gun_revolver");
		//gun_revolver_saturnite = new ItemGunBase(Gun357MagnumFactory.getRevolverSaturniteConfig()).setUnlocalizedName("gun_revolver_saturnite").setCreativeTab(MainRegistry.weaponTab).setTextureName(RefStrings.MODID + ":gun_revolver_saturnite");
		//gun_revolver_gold = new ItemGunBase(Gun357MagnumFactory.getRevolverGoldConfig()).setUnlocalizedName("gun_revolver_gold").setCreativeTab(MainRegistry.weaponTab).setTextureName(RefStrings.MODID + ":gun_revolver_gold");
		//gun_revolver_schrabidium = new ItemGunBase(Gun357MagnumFactory.getRevolverSchrabidiumConfig()).setUnlocalizedName("gun_revolver_schrabidium").setCreativeTab(MainRegistry.weaponTab).setTextureName(RefStrings.MODID + ":gun_revolver_schrabidium");
		//gun_revolver_cursed = new ItemGunBase(Gun357MagnumFactory.getRevolverCursedConfig()).setUnlocalizedName("gun_revolver_cursed").setCreativeTab(MainRegistry.weaponTab).setTextureName(RefStrings.MODID + ":gun_revolver_cursed");
		//gun_revolver_nightmare = new ItemGunBase(Gun357MagnumFactory.getRevolverNightmareConfig()).setUnlocalizedName("gun_revolver_nightmare").setCreativeTab(MainRegistry.weaponTab).setTextureName(RefStrings.MODID + ":gun_revolver_nightmare");
		//gun_revolver_nightmare2 = new ItemGunBase(Gun357MagnumFactory.getRevolverNightmare2Config()).setUnlocalizedName("gun_revolver_nightmare2").setCreativeTab(MainRegistry.weaponTab).setTextureName(RefStrings.MODID + ":gun_revolver_nightmare2");
		//gun_revolver_pip = new ItemGunPip(Gun44MagnumFactory.getMacintoshConfig()).setUnlocalizedName("gun_revolver_pip").setCreativeTab(MainRegistry.weaponTab).setTextureName(RefStrings.MODID + ":gun_revolver_pip");
		//gun_revolver_nopip = new ItemGunBase(Gun44MagnumFactory.getNovacConfig()).setUnlocalizedName("gun_revolver_nopip").setCreativeTab(MainRegistry.weaponTab).setTextureName(RefStrings.MODID + ":gun_revolver_nopip");
		//gun_revolver_blackjack = new ItemGunBase(Gun44MagnumFactory.getBlackjackConfig()).setUnlocalizedName("gun_revolver_blackjack").setCreativeTab(MainRegistry.weaponTab).setTextureName(RefStrings.MODID + ":gun_revolver_blackjack");
		//gun_revolver_silver = new ItemGunBase(Gun44MagnumFactory.getSilverConfig()).setUnlocalizedName("gun_revolver_silver").setCreativeTab(MainRegistry.weaponTab).setTextureName(RefStrings.MODID + ":gun_revolver_silver");
		//gun_revolver_red = new ItemGunBase(Gun44MagnumFactory.getRedConfig()).setUnlocalizedName("gun_revolver_red").setCreativeTab(MainRegistry.weaponTab).setTextureName(RefStrings.MODID + ":gun_revolver_red");
		//gun_deagle = new ItemGunBase(Gun50AEFactory.getDeagleConfig()).setUnlocalizedName("gun_deagle").setFull3D().setCreativeTab(MainRegistry.weaponTab).setTextureName(RefStrings.MODID + ":gun_deagle");
		//gun_bio_revolver = new ItemGunBio(Gun357MagnumFactory.getRevolverBioConfig()).setUnlocalizedName("gun_bio_revolver").setCreativeTab(MainRegistry.weaponTab).setTextureName(RefStrings.MODID + ":gun_bio_revolver");
		//gun_flechette = new ItemGunBase(Gun556mmFactory.getSPIWConfig(), Gun556mmFactory.getGLauncherConfig()).setUnlocalizedName("gun_flechette").setFull3D().setCreativeTab(MainRegistry.weaponTab).setTextureName(RefStrings.MODID + ":gun_flechette");
		gun_ar15 = new ItemGunBase(Gun50BMGFactory.getAR15Config(), Gun50BMGFactory.getAR15BurstConfig()).setUnlocalizedName("gun_ar15").setCreativeTab(MainRegistry.weaponTab).setTextureName(RefStrings.MODID + ":gun_ar15");
		//gun_calamity = new ItemGunBase(Gun762mmFactory.getCalamityConfig()).setUnlocalizedName("gun_calamity").setCreativeTab(MainRegistry.weaponTab).setTextureName(RefStrings.MODID + ":gun_calamity");
		//gun_minigun = new ItemGunLacunae(Gun5mmFactory.get53Config()).setUnlocalizedName("gun_minigun").setCreativeTab(MainRegistry.weaponTab).setTextureName(RefStrings.MODID + ":gun_minigun");
		//gun_lacunae = new ItemGunLacunae(Gun5mmFactory.getLacunaeConfig()).setUnlocalizedName("gun_lacunae").setCreativeTab(MainRegistry.weaponTab).setTextureName(RefStrings.MODID + ":gun_lacunae");
		//gun_folly = new GunFolly().setUnlocalizedName("gun_folly").setCreativeTab(MainRegistry.weaponTab).setTextureName(RefStrings.MODID + ":gun_folly");
		//gun_fatman = new ItemGunBase(GunFatmanFactory.getFatmanConfig()).setUnlocalizedName("gun_fatman").setCreativeTab(MainRegistry.weaponTab).setTextureName(RefStrings.MODID + ":gun_fatman");
		//gun_proto = new ItemGunBase(GunFatmanFactory.getProtoConfig()).setUnlocalizedName("gun_proto").setCreativeTab(MainRegistry.weaponTab).setTextureName(RefStrings.MODID + ":gun_fatman");
		//gun_mirv = new ItemGunBase(GunFatmanFactory.getMIRVConfig()).setUnlocalizedName("gun_mirv").setCreativeTab(MainRegistry.weaponTab).setTextureName(RefStrings.MODID + ":gun_mirv");
		//gun_bf = new ItemGunBase(GunFatmanFactory.getBELConfig()).setUnlocalizedName("gun_bf").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":gun_bf");
		gun_chemthrower = new ItemGunChemthrower().setUnlocalizedName("gun_chemthrower").setCreativeTab(MainRegistry.weaponTab).setTextureName(RefStrings.MODID + ":gun_fatman");
		//gun_mp40 = new ItemGunBase(Gun9mmFactory.getMP40Config()).setUnlocalizedName("gun_mp40").setCreativeTab(MainRegistry.weaponTab).setTextureName(RefStrings.MODID + ":gun_mp40");
		//gun_thompson = new ItemGunBase(Gun45ACPFactory.getThompsonConfig()).setUnlocalizedName("gun_thompson").setCreativeTab(MainRegistry.weaponTab).setTextureName(RefStrings.MODID + ":gun_thompson");
		//gun_uzi = new ItemGunBase(Gun22LRFactory.getUziConfig()).setUnlocalizedName("gun_uzi").setCreativeTab(MainRegistry.weaponTab).setTextureName(RefStrings.MODID + ":gun_uzi");
		//gun_uzi_silencer = new ItemGunBase(Gun22LRFactory.getUziConfig().silenced()).setUnlocalizedName("gun_uzi_silencer").setCreativeTab(MainRegistry.weaponTab).setTextureName(RefStrings.MODID + ":gun_uzi_silencer");
		//gun_uzi_saturnite = new ItemGunBase(Gun22LRFactory.getSaturniteConfig()).setUnlocalizedName("gun_uzi_saturnite").setCreativeTab(MainRegistry.weaponTab).setTextureName(RefStrings.MODID + ":gun_uzi_saturnite");
		//gun_uzi_saturnite_silencer = new ItemGunBase(Gun22LRFactory.getSaturniteConfig().silenced()).setUnlocalizedName("gun_uzi_saturnite_silencer").setCreativeTab(MainRegistry.weaponTab).setTextureName(RefStrings.MODID + ":gun_uzi_saturnite_silencer");
		//gun_uboinik = new ItemGunBase(Gun12GaugeFactory.getUboinikConfig()).setUnlocalizedName("gun_uboinik").setCreativeTab(MainRegistry.weaponTab).setTextureName(RefStrings.MODID + ":gun_uboinik");
		//gun_remington = new ItemGunBase(Gun12GaugeFactory.getRemington870Config()).setUnlocalizedName("gun_remington").setCreativeTab(MainRegistry.weaponTab).setTextureName(RefStrings.MODID + ":gun_spas12");
		//gun_spas12 = new ItemGunBase(Gun12GaugeFactory.getSpas12Config(), Gun12GaugeFactory.getSpas12AltConfig()).setUnlocalizedName("gun_spas12").setCreativeTab(MainRegistry.weaponTab).setTextureName(RefStrings.MODID + ":gun_spas12");
		//gun_benelli = new ItemGunBase(Gun12GaugeFactory.getBenelliModConfig()).setUnlocalizedName("gun_benelli").setCreativeTab(MainRegistry.weaponTab).setTextureName(RefStrings.MODID + ":gun_spas12");
		//gun_supershotgun = new ItemGunShotty(Gun12GaugeFactory.getShottyConfig()).setUnlocalizedName("gun_supershotgun").setCreativeTab(MainRegistry.weaponTab).setTextureName(RefStrings.MODID + ":gun_uboinik");
		//save//gun_ks23 = new ItemGunBase(Gun4GaugeFactory.getKS23Config()).setUnlocalizedName("gun_ks23").setCreativeTab(MainRegistry.weaponTab).setTextureName(RefStrings.MODID + ":gun_uboinik");
		//gun_sauer = new ItemGunBase(Gun4GaugeFactory.getSauerConfig()).setUnlocalizedName("gun_sauer").setCreativeTab(MainRegistry.weaponTab).setTextureName(RefStrings.MODID + ":gun_uboinik");
		//gun_lever_action = new ItemGunBase(Gun20GaugeFactory.getMareConfig()).setUnlocalizedName("gun_lever_action").setCreativeTab(MainRegistry.weaponTab).setTextureName(RefStrings.MODID + ":gun_lever_action");
		//gun_lever_action_dark = new ItemGunBase(Gun20GaugeFactory.getMareDarkConfig()).setUnlocalizedName("gun_lever_action_dark").setCreativeTab(MainRegistry.weaponTab).setTextureName(RefStrings.MODID + ":gun_lever_action_dark");
		//item icons
		gun_lever_action_sonata = new GunLeverActionS().setUnlocalizedName("gun_lever_action_sonata").setCreativeTab(MainRegistry.weaponTab).setTextureName(RefStrings.MODID + ":gun_lever_action_sonata");
		//gun_bolt_action = new ItemGunBase(Gun762mmFactory.getBoltConfig()).setUnlocalizedName("gun_bolt_action").setCreativeTab(MainRegistry.weaponTab).setTextureName(RefStrings.MODID + ":gun_bolt_action");
		//gun_bolt_action_green = new ItemGunBase(Gun762mmFactory.getBoltGreenConfig()).setUnlocalizedName("gun_bolt_action_green").setCreativeTab(MainRegistry.weaponTab).setTextureName(RefStrings.MODID + ":gun_bolt_action_green");
		//gun_bolt_action_saturnite = new ItemGunBase(Gun762mmFactory.getBoltSaturniteConfig()).setUnlocalizedName("gun_bolt_action_saturnite").setCreativeTab(MainRegistry.weaponTab).setTextureName(RefStrings.MODID + ":gun_bolt_action_saturnite");
		//gun_mymy = new ItemGunBase(GunDartFactory.getMymyConfig()).setUnlocalizedName("gun_mymy").setFull3D().setCreativeTab(MainRegistry.weaponTab).setTextureName(RefStrings.MODID + ":gun_mymy");
		//gun_b92_ammo = new GunB92Cell().setUnlocalizedName("gun_b92_ammo").setMaxStackSize(1).setCreativeTab(MainRegistry.weaponTab).setTextureName(RefStrings.MODID + ":gun_b92_ammo_alt");
		//gun_b92 = new GunB92().setUnlocalizedName("gun_b92").setCreativeTab(MainRegistry.weaponTab).setTextureName(RefStrings.MODID + ":gun_b92");
		//gun_b93 = new GunB93().setUnlocalizedName("gun_b93").setCreativeTab(MainRegistry.weaponTab).setTextureName(RefStrings.MODID + ":gun_b93");
		//gun_coilgun = new ItemCoilgun(GunEnergyFactory.getCoilgunConfig()).setUnlocalizedName("gun_coilgun").setCreativeTab(MainRegistry.weaponTab).setTextureName(RefStrings.MODID + ":gun_coilgun");
		//gun_xvl1456_ammo = new Item().setUnlocalizedName("gun_xvl1456_ammo").setCreativeTab(MainRegistry.weaponTab).setTextureName(RefStrings.MODID + ":gun_xvl1456_ammo");
		//gun_xvl1456 = new ItemGunGauss(GunGaussFactory.getXVLConfig(), GunGaussFactory.getChargedConfig()).setUnlocalizedName("gun_xvl1456").setCreativeTab(MainRegistry.weaponTab).setTextureName(RefStrings.MODID + ":gun_xvl1456");
		//gun_osipr_ammo = new Item().setUnlocalizedName("gun_osipr_ammo").setCreativeTab(MainRegistry.weaponTab).setTextureName(RefStrings.MODID + ":gun_osipr_ammo");
		//gun_osipr_ammo2 = new Item().setUnlocalizedName("gun_osipr_ammo2").setCreativeTab(MainRegistry.weaponTab).setTextureName(RefStrings.MODID + ":gun_osipr_ammo2");
		//gun_osipr = new ItemGunOSIPR(GunOSIPRFactory.getOSIPRConfig(), GunOSIPRFactory.getAltConfig()).setUnlocalizedName("gun_osipr").setCreativeTab(MainRegistry.weaponTab).setTextureName(RefStrings.MODID + ":gun_osipr");
		//gun_immolator_ammo = new Item().setUnlocalizedName("gun_immolator_ammo").setCreativeTab(MainRegistry.weaponTab).setTextureName(RefStrings.MODID + ":gun_immolator_ammo");
		//gun_immolator = new GunImmolator().setUnlocalizedName("gun_immolator").setCreativeTab(MainRegistry.weaponTab).setTextureName(RefStrings.MODID + ":gun_immolator");
		gun_flamer = new ItemGunBase(GunEnergyFactory.getFlamerConfig()).setUnlocalizedName("gun_flamer").setCreativeTab(MainRegistry.weaponTab).setTextureName(RefStrings.MODID + ":gun_flamer");
		//gun_cryolator_ammo = new Item().setUnlocalizedName("gun_cryolator_ammo").setCreativeTab(MainRegistry.weaponTab).setTextureName(RefStrings.MODID + ":gun_cryolator_ammo");
		//gun_cryolator = new GunCryolator().setUnlocalizedName("gun_cryolator").setCreativeTab(MainRegistry.weaponTab).setTextureName(RefStrings.MODID + ":gun_cryolator");
		//gun_cryocannon = new ItemCryoCannon(GunEnergyFactory.getCryoCannonConfig()).setUnlocalizedName("gun_cryocannon").setCreativeTab(MainRegistry.weaponTab).setTextureName(RefStrings.MODID + ":gun_cryocannon");
		gun_fireext = new ItemGunBase(GunEnergyFactory.getExtConfig()).setUnlocalizedName("gun_fireext").setCreativeTab(MainRegistry.weaponTab).setTextureName(RefStrings.MODID + ":gun_fireext");
		//gun_mp = new ItemGunBase(Gun556mmFactory.getEuphieConfig()).setUnlocalizedName("gun_mp").setCreativeTab(MainRegistry.weaponTab).setTextureName(RefStrings.MODID + ":gun_pm");
		//gun_bolter = new ItemGunBase(Gun75BoltFactory.getBolterConfig()).setUnlocalizedName("gun_bolter").setCreativeTab(MainRegistry.weaponTab).setTextureName(RefStrings.MODID + ":gun_bolter");
		//gun_bolter_digamma = new ItemGunBase(Gun75BoltFactory.getBolterConfig()).setUnlocalizedName("gun_bolter_digamma").setCreativeTab(MainRegistry.weaponTab).setTextureName(RefStrings.MODID + ":gun_bolter_digamma");
		//gun_zomg = new ItemGunBase(GunEnergyFactory.getZOMGConfig()).setUnlocalizedName("gun_zomg").setCreativeTab(MainRegistry.weaponTab).setTextureName(RefStrings.MODID + ":gun_zomg");
		gun_revolver_inverted = new GunSuicide().setUnlocalizedName("gun_revolver_inverted").setCreativeTab(MainRegistry.weaponTab).setTextureName(RefStrings.MODID + ":gun_revolver_inverted");
		//gun_emp_ammo = new Item().setUnlocalizedName("gun_emp_ammo").setCreativeTab(MainRegistry.weaponTab).setTextureName(RefStrings.MODID + ":gun_emp_ammo");
		//gun_emp = new ItemGunBase(GunEnergyFactory.getEMPConfig()).setUnlocalizedName("gun_emp").setCreativeTab(MainRegistry.weaponTab).setTextureName(RefStrings.MODID + ":gun_emp");
		//gun_jack_ammo = new Item().setUnlocalizedName("gun_jack_ammo").setCreativeTab(MainRegistry.weaponTab).setTextureName(RefStrings.MODID + ":gun_jack_ammo");
		//gun_jack = new GunJack().setUnlocalizedName("gun_jack").setCreativeTab(MainRegistry.weaponTab).setTextureName(RefStrings.MODID + ":gun_jack");
		//gun_spark_ammo = new Item().setUnlocalizedName("gun_spark_ammo").setCreativeTab(MainRegistry.weaponTab).setTextureName(RefStrings.MODID + ":gun_spark_ammo");
		//gun_spark = new GunSpark().setUnlocalizedName("gun_spark").setCreativeTab(MainRegistry.weaponTab).setTextureName(RefStrings.MODID + ":gun_spark");
		//gun_hp_ammo = new Item().setUnlocalizedName("gun_hp_ammo").setCreativeTab(MainRegistry.weaponTab).setTextureName(RefStrings.MODID + ":gun_hp_ammo");
		//gun_hp = new GunHP().setUnlocalizedName("gun_hp").setCreativeTab(MainRegistry.weaponTab).setTextureName(RefStrings.MODID + ":gun_hp");
		//gun_euthanasia_ammo = new Item().setUnlocalizedName("gun_euthanasia_ammo").setCreativeTab(MainRegistry.weaponTab).setTextureName(RefStrings.MODID + ":gun_euthanasia_ammo");
		//gun_euthanasia = new GunEuthanasia().setUnlocalizedName("gun_euthanasia").setCreativeTab(MainRegistry.weaponTab).setTextureName(RefStrings.MODID + ":gun_euthanasia");
		//gun_defabricator_ammo = new Item().setUnlocalizedName("gun_defabricator_ammo").setCreativeTab(MainRegistry.weaponTab).setTextureName(RefStrings.MODID + ":gun_defabricator_ammo");
		//gun_defabricator = new GunDefabricator().setUnlocalizedName("gun_defabricator").setCreativeTab(MainRegistry.weaponTab).setTextureName(RefStrings.MODID + ":gun_defabricator");
		//gun_vortex = new ItemGunBase(Gun556mmFactory.getEuphieConfig()).setUnlocalizedName("gun_vortex").setCreativeTab(MainRegistry.weaponTab).setTextureName(RefStrings.MODID + ":gun_vortex");
		//gun_super_shotgun = new ItemCustomLore().setUnlocalizedName("gun_super_shotgun").setMaxStackSize(1).setFull3D().setCreativeTab(MainRegistry.weaponTab).setTextureName(RefStrings.MODID + ":gun_super_shotgun");
		//gun_moist_nugget = new ItemNugget(3, false).setUnlocalizedName("gun_moist_nugget").setCreativeTab(MainRegistry.weaponTab).setTextureName(RefStrings.MODID + ":gun_moist_nugget");
		//gun_darter = new ItemGunDart(GunDartFactory.getDarterConfig()).setFull3D().setUnlocalizedName("gun_darter").setCreativeTab(MainRegistry.weaponTab).setTextureName(RefStrings.MODID + ":gun_darter");
		//gun_glass_cannon = new ItemEnergyGunBase(GunPoweredFactory.getGlassCannonConfig()).setFull3D().setUnlocalizedName("gun_glass_cannon").setCreativeTab(MainRegistry.weaponTab).setTextureName(RefStrings.MODID + ":gun_darter");
		//gun_m2 = new  ItemGunBase(Gun50BMGFactory.getM2Config()).setFull3D().setUnlocalizedName("gun_m2").setCreativeTab(MainRegistry.weaponTab);

		//gun_m2 = new  ItemGunBase(Gun50BMGFactory.getM2Config()).setFull3D().setUnlocalizedName("gun_m2").setCreativeTab(MainRegistry.weaponTab).setTextureName(RefStrings.MODID + ":gun_darter");
		//gun_lunatic_marksman = new ItemGunBase(Gun50BMGFactory.getLunaticMarksman()).setFull3D().setUnlocalizedName("gun_lunatic_marksman").setCreativeTab(MainRegistry.weaponTab);
		//gun_uac_pistol = new ItemGunBase(Gun45ACPFactory.getUACPistolConfig(), Gun45ACPFactory.getUACPistolBurstConfig()).setFull3D().setUnlocalizedName("gun_uac_pistol").setCreativeTab(MainRegistry.weaponTab);

		ToolMaterial matCrucible = EnumHelper.addToolMaterial("CRUCIBLE", 10, 3, 50.0F, 100.0F, 0);
		crucible = new ItemCrucible(5000, 1F, matCrucible).setUnlocalizedName("crucible").setCreativeTab(MainRegistry.weaponTab).setTextureName(RefStrings.MODID + ":crucible");

		stick_dynamite = new ItemGrenade(3).setUnlocalizedName("stick_dynamite").setCreativeTab(MainRegistry.weaponTab).setTextureName(RefStrings.MODID + ":stick_dynamite");
		stick_dynamite_fishing = new ItemGrenadeFishing(3).setUnlocalizedName("stick_dynamite_fishing").setCreativeTab(MainRegistry.weaponTab).setTextureName(RefStrings.MODID + ":stick_dynamite_fishing");
		stick_tnt = new Item().setUnlocalizedName("stick_tnt").setCreativeTab(MainRegistry.weaponTab).setTextureName(RefStrings.MODID + ":stick_tnt");
		stick_semtex = new Item().setUnlocalizedName("stick_semtex").setCreativeTab(MainRegistry.weaponTab).setTextureName(RefStrings.MODID + ":stick_semtex");
		stick_c4 = new Item().setUnlocalizedName("stick_c4").setCreativeTab(MainRegistry.weaponTab).setTextureName(RefStrings.MODID + ":stick_c4");

		grenade_generic = new ItemGrenade(4).setUnlocalizedName("grenade_generic").setCreativeTab(MainRegistry.weaponTab).setTextureName(RefStrings.MODID + ":grenade_generic");
		grenade_strong = new ItemGrenade(5).setUnlocalizedName("grenade_strong").setCreativeTab(MainRegistry.weaponTab).setTextureName(RefStrings.MODID + ":grenade_strong");
		grenade_frag = new ItemGrenade(4).setUnlocalizedName("grenade_frag").setCreativeTab(MainRegistry.weaponTab).setTextureName(RefStrings.MODID + ":grenade_frag_alt");
		grenade_fire = new ItemGrenade(4).setUnlocalizedName("grenade_fire").setCreativeTab(MainRegistry.weaponTab).setTextureName(RefStrings.MODID + ":grenade_fire_alt");
		grenade_shrapnel = new ItemGrenade(4).setUnlocalizedName("grenade_shrapnel").setCreativeTab(MainRegistry.weaponTab).setTextureName(RefStrings.MODID + ":grenade_shrapnel");
		grenade_cluster = new ItemGrenade(5).setUnlocalizedName("grenade_cluster").setCreativeTab(MainRegistry.weaponTab).setTextureName(RefStrings.MODID + ":grenade_cluster_alt");
		grenade_flare = new ItemGrenade(0).setUnlocalizedName("grenade_flare").setCreativeTab(MainRegistry.weaponTab).setTextureName(RefStrings.MODID + ":grenade_flare_alt");
		grenade_electric = new ItemGrenade(5).setUnlocalizedName("grenade_electric").setCreativeTab(MainRegistry.weaponTab).setTextureName(RefStrings.MODID + ":grenade_electric_alt");
		grenade_poison = new ItemGrenade(4).setUnlocalizedName("grenade_poison").setCreativeTab(MainRegistry.weaponTab).setTextureName(RefStrings.MODID + ":grenade_poison_alt");
		grenade_gas = new ItemGrenade(4).setUnlocalizedName("grenade_gas").setCreativeTab(MainRegistry.weaponTab).setTextureName(RefStrings.MODID + ":grenade_gas_alt");
		grenade_pulse = new ItemGrenade(4).setUnlocalizedName("grenade_pulse").setCreativeTab(MainRegistry.weaponTab).setTextureName(RefStrings.MODID + ":grenade_pulse");
		grenade_plasma = new ItemGrenade(5).setUnlocalizedName("grenade_plasma").setCreativeTab(MainRegistry.weaponTab).setTextureName(RefStrings.MODID + ":grenade_plasma_alt");
		grenade_tau = new ItemGrenade(5).setUnlocalizedName("grenade_tau").setCreativeTab(MainRegistry.weaponTab).setTextureName(RefStrings.MODID + ":grenade_tau_alt");
		grenade_schrabidium = new ItemGrenade(7).setUnlocalizedName("grenade_schrabidium").setCreativeTab(MainRegistry.weaponTab).setTextureName(RefStrings.MODID + ":grenade_schrabidium_alt");
		//grenade_lemon = new ItemGrenade(4).setUnlocalizedName("grenade_lemon").setCreativeTab(MainRegistry.weaponTab).setTextureName(RefStrings.MODID + ":grenade_lemon");
		grenade_gascan = new ItemGrenade(-1).setUnlocalizedName("grenade_gascan").setCreativeTab(MainRegistry.weaponTab).setTextureName(RefStrings.MODID + ":grenade_gascan");
		grenade_kyiv = new ItemGrenadeKyiv(-1).setUnlocalizedName("grenade_kyiv").setCreativeTab(MainRegistry.weaponTab).setTextureName(RefStrings.MODID + ":grenade_kyiv");
		grenade_mk2 = new ItemGrenade(5).setUnlocalizedName("grenade_mk2").setCreativeTab(MainRegistry.weaponTab).setTextureName(RefStrings.MODID + ":grenade_mk2_alt");
		grenade_aschrab = new ItemGrenade(-1).setUnlocalizedName("grenade_aschrab").setCreativeTab(MainRegistry.weaponTab).setTextureName(RefStrings.MODID + ":grenade_aschrab");
		grenade_nuke = new ItemGrenade(-1).setUnlocalizedName("grenade_nuke").setCreativeTab(MainRegistry.weaponTab).setTextureName(RefStrings.MODID + ":grenade_nuke_alt");
		grenade_nuclear = new ItemGrenade(7).setUnlocalizedName("grenade_nuclear").setCreativeTab(MainRegistry.weaponTab).setTextureName(RefStrings.MODID + ":grenade_nuclear");
		grenade_zomg = new ItemGrenade(7).setUnlocalizedName("grenade_zomg").setCreativeTab(MainRegistry.weaponTab).setTextureName(RefStrings.MODID + ":grenade_zomg");
		grenade_black_hole = new ItemGrenade(7).setUnlocalizedName("grenade_black_hole").setCreativeTab(MainRegistry.weaponTab).setTextureName(RefStrings.MODID + ":grenade_black_hole");
		grenade_cloud = new ItemGrenade(-1).setUnlocalizedName("grenade_cloud").setCreativeTab(MainRegistry.weaponTab).setTextureName(RefStrings.MODID + ":grenade_cloud");
		grenade_pink_cloud = new ItemGrenade(-1).setUnlocalizedName("grenade_pink_cloud").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":grenade_pink_cloud");
		//ullapool_caber = new WeaponSpecial(MainRegistry.tMatSteel).setUnlocalizedName("ullapool_caber").setCreativeTab(MainRegistry.weaponTab).setTextureName(RefStrings.MODID + ":ullapool_caber");

		grenade_if_generic = new ItemGrenade(4).setUnlocalizedName("grenade_if_generic").setCreativeTab(MainRegistry.weaponTab).setTextureName(RefStrings.MODID + ":grenade_if_generic");
		grenade_if_he = new ItemGrenade(5).setUnlocalizedName("grenade_if_he").setCreativeTab(MainRegistry.weaponTab).setTextureName(RefStrings.MODID + ":grenade_if_he");
		grenade_if_bouncy = new ItemGrenade(4).setUnlocalizedName("grenade_if_bouncy").setCreativeTab(MainRegistry.weaponTab).setTextureName(RefStrings.MODID + ":grenade_if_bouncy");
		grenade_if_sticky = new ItemGrenade(4).setUnlocalizedName("grenade_if_sticky").setCreativeTab(MainRegistry.weaponTab).setTextureName(RefStrings.MODID + ":grenade_if_sticky");
		grenade_if_impact = new ItemGrenade(-1).setUnlocalizedName("grenade_if_impact").setCreativeTab(MainRegistry.weaponTab).setTextureName(RefStrings.MODID + ":grenade_if_impact");
		grenade_if_incendiary = new ItemGrenade(4).setUnlocalizedName("grenade_if_incendiary").setCreativeTab(MainRegistry.weaponTab).setTextureName(RefStrings.MODID + ":grenade_if_incendiary");
		grenade_if_toxic = new ItemGrenade(4).setUnlocalizedName("grenade_if_toxic").setCreativeTab(MainRegistry.weaponTab).setTextureName(RefStrings.MODID + ":grenade_if_toxic");
		grenade_if_concussion = new ItemGrenade(4).setUnlocalizedName("grenade_if_concussion").setCreativeTab(MainRegistry.weaponTab).setTextureName(RefStrings.MODID + ":grenade_if_concussion");
		grenade_if_brimstone = new ItemGrenade(5).setUnlocalizedName("grenade_if_brimstone").setCreativeTab(MainRegistry.weaponTab).setTextureName(RefStrings.MODID + ":grenade_if_brimstone");
		grenade_if_mystery = new ItemGrenade(5).setUnlocalizedName("grenade_if_mystery").setCreativeTab(MainRegistry.weaponTab).setTextureName(RefStrings.MODID + ":grenade_if_mystery");
		grenade_if_spark = new ItemGrenade(7).setUnlocalizedName("grenade_if_spark").setCreativeTab(MainRegistry.weaponTab).setTextureName(RefStrings.MODID + ":grenade_if_spark");
		grenade_if_hopwire = new ItemGrenade(7).setUnlocalizedName("grenade_if_hopwire").setCreativeTab(MainRegistry.weaponTab).setTextureName(RefStrings.MODID + ":grenade_if_hopwire");
		grenade_if_null = new ItemGrenade(7).setUnlocalizedName("grenade_if_null").setCreativeTab(MainRegistry.weaponTab).setTextureName(RefStrings.MODID + ":grenade_if_null");

		grenade_smart = new ItemGrenade(-1).setUnlocalizedName("grenade_smart").setCreativeTab(MainRegistry.weaponTab).setTextureName(RefStrings.MODID + ":grenade_smart");
		grenade_mirv = new ItemGrenade(1).setUnlocalizedName("grenade_mirv").setCreativeTab(MainRegistry.weaponTab).setTextureName(RefStrings.MODID + ":grenade_mirv");
		grenade_breach = new ItemGrenade(-1).setUnlocalizedName("grenade_breach").setCreativeTab(MainRegistry.weaponTab).setTextureName(RefStrings.MODID + ":grenade_breach");
		grenade_burst = new ItemGrenade(1).setUnlocalizedName("grenade_burst").setCreativeTab(MainRegistry.weaponTab).setTextureName(RefStrings.MODID + ":grenade_burst");
		//nuclear_waste_pearl = new ItemGrenade(-1).setUnlocalizedName("nuclear_waste_pearl").setCreativeTab(MainRegistry.weaponTab).setTextureName(RefStrings.MODID + ":nuclear_waste_pearl");

		weaponized_starblaster_cell = new WeaponizedCell().setUnlocalizedName("weaponized_starblaster_cell").setMaxStackSize(1).setCreativeTab(MainRegistry.weaponTab).setTextureName(RefStrings.MODID + ":gun_b92_ammo_weaponized");

		//bomb_waffle = new ItemWaffle(20, false).setUnlocalizedName("bomb_waffle").setCreativeTab(MainRegistry.consumableTab).setTextureName(RefStrings.MODID + ":bomb_waffle");
		schnitzel_vegan = new ItemSchnitzelVegan(3, true).setUnlocalizedName("schnitzel_vegan").setCreativeTab(MainRegistry.consumableTab).setTextureName(RefStrings.MODID + ":schnitzel_vegan");
		cotton_candy = new ItemCottonCandy(5, false).setUnlocalizedName("cotton_candy").setCreativeTab(MainRegistry.consumableTab).setFull3D().setTextureName(RefStrings.MODID + ":cotton_candy");
		//apple_lead = new ItemAppleSchrabidium(5, 0, false).setUnlocalizedName("apple_lead").setCreativeTab(MainRegistry.consumableTab).setTextureName(RefStrings.MODID + ":apple_lead");
		//apple_schrabidium = new ItemAppleSchrabidium(20, 100, false).setUnlocalizedName("apple_schrabidium").setCreativeTab(MainRegistry.consumableTab).setTextureName(RefStrings.MODID + ":apple_schrabidium");
		//tem_flakes = new ItemTemFlakes(0, 0, false).setUnlocalizedName("tem_flakes").setCreativeTab(MainRegistry.consumableTab).setTextureName(RefStrings.MODID + ":tem_flakes");
		//glowing_stew = new ItemSoup(6).setUnlocalizedName("glowing_stew").setCreativeTab(MainRegistry.consumableTab).setTextureName(RefStrings.MODID + ":glowing_stew");
		//balefire_scrambled = new ItemSoup(6).setUnlocalizedName("balefire_scrambled").setCreativeTab(MainRegistry.consumableTab).setTextureName(RefStrings.MODID + ":balefire_scrambled");
		//balefire_and_ham = new ItemSoup(6).setUnlocalizedName("balefire_and_ham").setCreativeTab(MainRegistry.consumableTab).setTextureName(RefStrings.MODID + ":balefire_and_ham");
		//lemon = new ItemLemon(3, 0.5F, false).setUnlocalizedName("lemon").setCreativeTab(MainRegistry.consumableTab).setTextureName(RefStrings.MODID + ":lemon");
		definitelyfood = new ItemLemon(5, 9.5F, false).setUnlocalizedName("definitelyfood").setCreativeTab(MainRegistry.consumableTab).setTextureName(RefStrings.MODID + ":definitelyfood");
		//high saturation, low actual 'fullness'
		med_ipecac = new Itemmed_ipecac(0).setUnlocalizedName("med_ipecac").setCreativeTab(MainRegistry.consumableTab).setTextureName(RefStrings.MODID + ":med_ipecac_new");

		med_ptsd = new ItemLemon(0, 0, false).setUnlocalizedName("med_ptsd").setCreativeTab(MainRegistry.consumableTab).setTextureName(RefStrings.MODID + ":med_ptsd_new");
		med_schizophrenia = new ItemLemon(0, 0, false).setUnlocalizedName("med_schizophrenia").setCreativeTab(MainRegistry.consumableTab).setTextureName(RefStrings.MODID + ":med_schizophrenia_new");
		loops = new ItemLemon(5, 0.25F, false).setUnlocalizedName("loops").setCreativeTab(MainRegistry.consumableTab).setTextureName(RefStrings.MODID + ":loops");
		loop_stew = new ItemLemon(10, 0.5F, false).setUnlocalizedName("loop_stew").setMaxStackSize(1).setCreativeTab(MainRegistry.consumableTab).setTextureName(RefStrings.MODID + ":loop_stew");
		spongebob_macaroni = new ItemLemon(8, 1.5F, false).setUnlocalizedName("spongebob_macaroni").setCreativeTab(MainRegistry.consumableTab).setTextureName(RefStrings.MODID + ":spongebob_macaroni");
		//fooditem = new ItemLemon(2, 5F, false).setUnlocalizedName("fooditem").setCreativeTab(MainRegistry.consumableTab).setTextureName(RefStrings.MODID + ":fooditem");
		twinkie = new ItemLemon(8, 0.25F, false).setUnlocalizedName("twinkie").setCreativeTab(MainRegistry.consumableTab).setTextureName(RefStrings.MODID + ":twinkie");
		//static_sandwich = new ItemLemon(6, 1F, false).setUnlocalizedName("static_sandwich").setCreativeTab(MainRegistry.consumableTab).setTextureName(RefStrings.MODID + ":static_sandwich");
		pudding = new ItemLemon(7, 1F, false).setUnlocalizedName("pudding").setCreativeTab(MainRegistry.consumableTab).setTextureName(RefStrings.MODID + ":pudding");
		canteen_vodka = new ItemCanteen().setUnlocalizedName("canteen_vodka").setCreativeTab(MainRegistry.consumableTab).setTextureName(RefStrings.MODID + ":canteen_vodka");
		canteen_fab = new ItemCanteen().setUnlocalizedName("canteen_fab").setCreativeTab(MainRegistry.consumableTab).setTextureName(RefStrings.MODID + ":canteen_fab");
		//pancake = new ItemPancake(20, 20, false).setUnlocalizedName("pancake").setCreativeTab(MainRegistry.consumableTab).setTextureName(RefStrings.MODID + ":pancake");
		nugget = new ItemLemon(2, 1F, false).setUnlocalizedName("nugget").setCreativeTab(MainRegistry.consumableTab).setTextureName(RefStrings.MODID + ":nugget");
		peas = new ItemPeas().setUnlocalizedName("peas").setCreativeTab(MainRegistry.consumableTab).setTextureName(RefStrings.MODID + ":peas");
		marshmallow = new ItemMarshmallow().setUnlocalizedName("marshmallow").setMaxStackSize(1).setFull3D().setCreativeTab(MainRegistry.consumableTab).setTextureName(RefStrings.MODID + ":marshmallow");
		cheese = new ItemLemon(7, 0.75F, false).setUnlocalizedName("cheese").setCreativeTab(MainRegistry.consumableTab).setTextureName(RefStrings.MODID + ":cheese");
		chocolate = new ItemLemon(3, 0.25F, false).setUnlocalizedName("chocolate").setCreativeTab(MainRegistry.consumableTab).setTextureName(RefStrings.MODID + ":chocolate");

		quesadilla = new ItemLemon(10, 1F, false).setUnlocalizedName("cheese_quesadilla").setCreativeTab(MainRegistry.consumableTab).setTextureName(RefStrings.MODID + ":quesadilla");
		mucho_mango = new ItemMuchoMango(5).setUnlocalizedName("mucho_mango").setCreativeTab(MainRegistry.consumableTab).setTextureName(RefStrings.MODID + ":mucho_mango");
		//glyphid_meat = new ItemLemon(3, 0.5F, true).setUnlocalizedName("glyphid_meat").setCreativeTab(MainRegistry.consumableTab).setTextureName(RefStrings.MODID + ":glyphid_meat");
		//glyphid_meat_grilled = new ItemLemon(8, 0.75F, true).setPotionEffect(Potion.damageBoost.id, 180, 1, 1F).setUnlocalizedName("glyphid_meat_grilled").setCreativeTab(MainRegistry.consumableTab).setTextureName(RefStrings.MODID + ":glyphid_meat_grilled");
		//egg_glyphid = new Item().setUnlocalizedName("egg_glyphid").setCreativeTab(MainRegistry.consumableTab).setTextureName(RefStrings.MODID + ":egg_glyphid");

		defuser = new ItemTooling(ToolType.DEFUSER, 100).setUnlocalizedName("defuser").setMaxStackSize(1).setFull3D().setCreativeTab(MainRegistry.nukeTab).setTextureName(RefStrings.MODID + ":defuser");
		reacher = new Item().setUnlocalizedName("reacher").setMaxStackSize(1).setFull3D().setCreativeTab(MainRegistry.controlTab).setTextureName(RefStrings.MODID + ":reacher");
		bismuth_tool = new ItemAmatExtractor().setUnlocalizedName("bismuth_tool").setMaxStackSize(1).setFull3D().setCreativeTab(MainRegistry.controlTab).setTextureName(RefStrings.MODID + ":bismuth_tool");
		meltdown_tool = new ItemDyatlov().setUnlocalizedName("meltdown_tool").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab).setTextureName(RefStrings.MODID + ":meltdown_tool");

		flame_pony = new ItemCustomLore().setUnlocalizedName("flame_pony").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":flame_pony");
		flame_conspiracy = new ItemCustomLore().setUnlocalizedName("flame_conspiracy").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":flame_conspiracy");
		flame_politics = new ItemCustomLore().setUnlocalizedName("flame_politics").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":flame_politics");
		flame_opinion = new ItemCustomLore().setUnlocalizedName("flame_opinion").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":flame_opinion");

		//gadget_explosive = new Item().setUnlocalizedName("gadget_explosive").setCreativeTab(MainRegistry.nukeTab).setTextureName(RefStrings.MODID + ":gadget_explosive");
		early_explosive_lenses = new ItemCustomLore().setUnlocalizedName("early_explosive_lenses").setCreativeTab(MainRegistry.nukeTab).setTextureName(RefStrings.MODID + ":gadget_explosive8");
		explosive_lenses = new ItemCustomLore().setUnlocalizedName("explosive_lenses").setCreativeTab(MainRegistry.nukeTab).setTextureName(RefStrings.MODID + ":man_explosive8");
		gadget_wireing = new Item().setUnlocalizedName("gadget_wireing").setMaxStackSize(1).setCreativeTab(MainRegistry.nukeTab).setTextureName(RefStrings.MODID + ":gadget_wireing");
		gadget_core = new ItemCustomLore().setRarity(EnumRarity.uncommon).setUnlocalizedName("gadget_core").setMaxStackSize(1).setCreativeTab(MainRegistry.nukeTab).setTextureName(RefStrings.MODID + ":gadget_core");

		boy_igniter = new Item().setUnlocalizedName("boy_igniter").setMaxStackSize(1).setCreativeTab(MainRegistry.nukeTab).setTextureName(RefStrings.MODID + ":boy_igniter");
		boy_propellant = new Item().setUnlocalizedName("boy_propellant").setMaxStackSize(1).setCreativeTab(MainRegistry.nukeTab).setTextureName(RefStrings.MODID + ":boy_propellant");
		boy_bullet = new ItemCustomLore().setRarity(EnumRarity.uncommon).setUnlocalizedName("boy_bullet").setMaxStackSize(1).setCreativeTab(MainRegistry.nukeTab).setTextureName(RefStrings.MODID + ":boy_bullet");
		boy_target = new ItemCustomLore().setRarity(EnumRarity.uncommon).setUnlocalizedName("boy_target").setMaxStackSize(1).setCreativeTab(MainRegistry.nukeTab).setTextureName(RefStrings.MODID + ":boy_target");
		boy_shielding = new Item().setUnlocalizedName("boy_shielding").setMaxStackSize(1).setCreativeTab(MainRegistry.nukeTab).setTextureName(RefStrings.MODID + ":boy_shielding");

		//man_explosive = new Item().setUnlocalizedName("man_explosive").setCreativeTab(MainRegistry.nukeTab).setTextureName(RefStrings.MODID + ":man_explosive");
		man_igniter = new Item().setUnlocalizedName("man_igniter").setMaxStackSize(1).setCreativeTab(MainRegistry.nukeTab).setTextureName(RefStrings.MODID + ":man_igniter");
		man_core = new ItemCustomLore().setRarity(EnumRarity.uncommon).setUnlocalizedName("man_core").setMaxStackSize(1).setCreativeTab(MainRegistry.nukeTab).setTextureName(RefStrings.MODID + ":man_core");

		mike_core = new Item().setUnlocalizedName("mike_core").setMaxStackSize(1).setCreativeTab(MainRegistry.nukeTab).setTextureName(RefStrings.MODID + ":mike_core");
		mike_deut = new Item().setUnlocalizedName("mike_deut").setMaxStackSize(1).setCreativeTab(MainRegistry.nukeTab).setContainerItem(ModItems.tank_steel).setTextureName(RefStrings.MODID + ":mike_deut");
		mike_cooling_unit = new Item().setUnlocalizedName("mike_cooling_unit").setMaxStackSize(1).setCreativeTab(MainRegistry.nukeTab).setTextureName(RefStrings.MODID + ":mike_cooling_unit");

		shrimp_lithium = new Item().setUnlocalizedName("shrimp_lithium").setMaxStackSize(1).setCreativeTab(MainRegistry.nukeTab).setTextureName(RefStrings.MODID + ":shrimp_lithium");


		tsar_core = new Item().setUnlocalizedName("tsar_core").setMaxStackSize(1).setCreativeTab(MainRegistry.nukeTab).setTextureName(RefStrings.MODID + ":tsar_core");
		tsar_corelead = new Item().setUnlocalizedName("tsar_corelead").setMaxStackSize(1).setCreativeTab(MainRegistry.nukeTab).setTextureName(RefStrings.MODID + ":tsar_corelead");


		tsar_corereal = new Item().setUnlocalizedName("tsar_corereal").setMaxStackSize(1).setCreativeTab(MainRegistry.nukeTab).setTextureName(RefStrings.MODID + ":tsar_corereal");


		fleija_igniter = new ItemFleija().setUnlocalizedName("fleija_igniter").setMaxStackSize(1).setCreativeTab(MainRegistry.nukeTab).setTextureName(RefStrings.MODID + ":fleija_igniter");
		fleija_propellant = new ItemFleija().setUnlocalizedName("fleija_propellant").setMaxStackSize(1).setCreativeTab(MainRegistry.nukeTab).setTextureName(RefStrings.MODID + ":fleija_propellant");
		fleija_core = new ItemFleija().setUnlocalizedName("fleija_core").setMaxStackSize(1).setCreativeTab(MainRegistry.nukeTab).setTextureName(RefStrings.MODID + ":fleija_core");

		solinium_igniter = new ItemSolinium().setUnlocalizedName("solinium_igniter").setMaxStackSize(1).setCreativeTab(MainRegistry.nukeTab).setTextureName(RefStrings.MODID + ":solinium_igniter");
		solinium_propellant = new ItemSolinium().setUnlocalizedName("solinium_propellant").setMaxStackSize(1).setCreativeTab(MainRegistry.nukeTab).setTextureName(RefStrings.MODID + ":solinium_propellant");
		solinium_core = new ItemSolinium().setUnlocalizedName("solinium_core").setMaxStackSize(1).setCreativeTab(MainRegistry.nukeTab).setTextureName(RefStrings.MODID + ":solinium_core");

		n2_charge = new ItemN2().setUnlocalizedName("n2_charge").setMaxStackSize(1).setCreativeTab(MainRegistry.nukeTab).setTextureName(RefStrings.MODID + ":n2_charge");

		//egg_balefire_shard = new Item().setUnlocalizedName("egg_balefire_shard").setMaxStackSize(16).setCreativeTab(MainRegistry.nukeTab).setTextureName(RefStrings.MODID + ":egg_balefire_shard");
		//egg_balefire = new Item().setUnlocalizedName("egg_balefire").setMaxStackSize(1).setCreativeTab(MainRegistry.nukeTab).setTextureName(RefStrings.MODID + ":egg_balefire");

		custom_tnt = new ItemCustomLore().setUnlocalizedName("custom_tnt").setMaxStackSize(1).setCreativeTab(MainRegistry.nukeTab).setTextureName(RefStrings.MODID + ":custom_tnt");
		custom_nuke = new ItemCustomLore().setUnlocalizedName("custom_nuke").setMaxStackSize(1).setCreativeTab(MainRegistry.nukeTab).setTextureName(RefStrings.MODID + ":custom_nuke");
		custom_hydro = new ItemCustomLore().setUnlocalizedName("custom_hydro").setMaxStackSize(1).setCreativeTab(MainRegistry.nukeTab).setTextureName(RefStrings.MODID + ":custom_hydro");
		custom_amat = new ItemCustomLore().setUnlocalizedName("custom_amat").setMaxStackSize(1).setCreativeTab(MainRegistry.nukeTab).setTextureName(RefStrings.MODID + ":custom_amat");
		custom_dirty = new ItemCustomLore().setUnlocalizedName("custom_dirty").setMaxStackSize(1).setCreativeTab(MainRegistry.nukeTab).setTextureName(RefStrings.MODID + ":custom_dirty");
		custom_schrab = new ItemCustomLore().setUnlocalizedName("custom_schrab").setMaxStackSize(1).setCreativeTab(MainRegistry.nukeTab).setTextureName(RefStrings.MODID + ":custom_schrab");
		custom_fall = new ItemCustomLore().setUnlocalizedName("custom_fall").setMaxStackSize(1).setCreativeTab(MainRegistry.nukeTab).setTextureName(RefStrings.MODID + ":custom_fall");

		battery_generic = new ItemBattery(1900, 100, 100).setUnlocalizedName("battery_generic").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab).setTextureName(RefStrings.MODID + ":battery_generic_new");
		battery_advanced = new ItemBattery(2500, 500, 500).setUnlocalizedName("battery_advanced").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab).setTextureName(RefStrings.MODID + ":battery_advanced_new");
		battery_lithium = new ItemBattery(3000, 1000, 1000).setUnlocalizedName("battery_lithium").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab).setTextureName(RefStrings.MODID + ":battery_lithium");
		battery_schrabidium = new ItemBattery(1000000, 5000, 5000).setUnlocalizedName("battery_schrabidium").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab).setTextureName(RefStrings.MODID + ":battery_schrabidium_new");
		battery_spark = new ItemBattery(100000000, 2000000, 2000000).setUnlocalizedName("battery_spark").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab).setTextureName(RefStrings.MODID + ":battery_spark");
		battery_trixite = new ItemBattery(5000000, 40000, 200000).setUnlocalizedName("battery_trixite").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab).setTextureName(RefStrings.MODID + ":battery_trixite");
		battery_creative = new Item().setUnlocalizedName("battery_creative").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab).setTextureName(RefStrings.MODID + ":battery_creative_new");

		battery_red_cell = new ItemBattery(5700, 100, 100).setUnlocalizedName("battery_red_cell").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab).setTextureName(RefStrings.MODID + ":battery_red_cell");
		battery_red_cell_6 = new ItemBattery(5700 * 6, 100, 100).setUnlocalizedName("battery_red_cell_6").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab).setTextureName(RefStrings.MODID + ":battery_red_cell_6");
		battery_red_cell_24 = new ItemBattery(5700 * 24, 100, 100).setUnlocalizedName("battery_red_cell_24").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab).setTextureName(RefStrings.MODID + ":battery_red_cell_24");
		battery_advanced_cell = new ItemBattery(7500, 500, 500).setUnlocalizedName("battery_advanced_cell").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab).setTextureName(RefStrings.MODID + ":battery_advanced_cell");
		battery_advanced_cell_4 = new ItemBattery(7500 * 4, 500, 500).setUnlocalizedName("battery_advanced_cell_4").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab).setTextureName(RefStrings.MODID + ":battery_advanced_cell_4");
		battery_advanced_cell_12 = new ItemBattery(7500 * 12, 500, 500).setUnlocalizedName("battery_advanced_cell_12").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab).setTextureName(RefStrings.MODID + ":battery_advanced_cell_12");
		battery_lithium_cell = new ItemBattery(9000, 1000, 1000).setUnlocalizedName("battery_lithium_cell").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab).setTextureName(RefStrings.MODID + ":battery_lithium_cell");
		battery_lithium_cell_3 = new ItemBattery(9000 * 3, 1000, 1000).setUnlocalizedName("battery_lithium_cell_3").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab).setTextureName(RefStrings.MODID + ":battery_lithium_cell_3");
		battery_lithium_cell_6 = new ItemBattery(9000 * 6, 1000, 1000).setUnlocalizedName("battery_lithium_cell_6").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab).setTextureName(RefStrings.MODID + ":battery_lithium_cell_6");
		battery_schrabidium_cell = new ItemBattery(3000000, 5000, 5000).setUnlocalizedName("battery_schrabidium_cell").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab).setTextureName(RefStrings.MODID + ":battery_schrabidium_cell");
		battery_schrabidium_cell_2 = new ItemBattery(3000000 * 2, 5000, 5000).setUnlocalizedName("battery_schrabidium_cell_2").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab).setTextureName(RefStrings.MODID + ":battery_schrabidium_cell_2");
		battery_schrabidium_cell_4 = new ItemBattery(3000000 * 4, 5000, 5000).setUnlocalizedName("battery_schrabidium_cell_4").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab).setTextureName(RefStrings.MODID + ":battery_schrabidium_cell_4");
		battery_spark_cell_6 = new ItemBattery(100000000L * 6L, 2000000, 2000000).setUnlocalizedName("battery_spark_cell_6").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab).setTextureName(RefStrings.MODID + ":battery_spark_cell_6");
		battery_spark_cell_25 = new ItemBattery(100000000L * 25L, 2000000, 2000000).setUnlocalizedName("battery_spark_cell_25").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab).setTextureName(RefStrings.MODID + ":battery_spark_cell_25");
		battery_spark_cell_100 = new ItemBattery(100000000L * 100L, 2000000, 2000000).setUnlocalizedName("battery_spark_cell_100").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab).setTextureName(RefStrings.MODID + ":battery_spark_cell_100");
		battery_spark_cell_1000 = new ItemBattery(100000000L * 1000L, 20000000, 20000000).setUnlocalizedName("battery_spark_cell_1000").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab).setTextureName(RefStrings.MODID + ":battery_spark_cell_1000");
		battery_spark_cell_2500 = new ItemBattery(100000000L * 2500L, 20000000, 20000000).setUnlocalizedName("battery_spark_cell_2500").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab).setTextureName(RefStrings.MODID + ":battery_spark_cell_2500");
		battery_spark_cell_10000 = new ItemBattery(100000000L * 10000L, 200000000, 200000000).setUnlocalizedName("battery_spark_cell_10000").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab).setTextureName(RefStrings.MODID + ":battery_spark_cell_10000");
		battery_spark_cell_power = new ItemBattery(100000000L * 1000000L, 200000000, 200000000).setUnlocalizedName("battery_spark_cell_power").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab).setTextureName(RefStrings.MODID + ":battery_spark_cell_power");
		cube_power = new ItemBattery(1000000000000000000L, 1000000000000000L, 1000000000000000L).setUnlocalizedName("cube_power").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab).setTextureName(RefStrings.MODID + ":cube_power");

		battery_sc_uranium = new ItemSelfcharger(5).setUnlocalizedName("battery_sc_uranium").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab).setTextureName(RefStrings.MODID + ":battery_sc_uranium");
		battery_sc_technetium = new ItemSelfcharger(25).setUnlocalizedName("battery_sc_technetium").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab).setTextureName(RefStrings.MODID + ":battery_sc_technetium");
		battery_sc_plutonium = new ItemSelfcharger(100).setUnlocalizedName("battery_sc_plutonium").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab).setTextureName(RefStrings.MODID + ":battery_sc_plutonium");
		battery_sc_polonium = new ItemSelfcharger(500).setUnlocalizedName("battery_sc_polonium").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab).setTextureName(RefStrings.MODID + ":battery_sc_polonium");
		battery_sc_gold = new ItemSelfcharger(2500).setUnlocalizedName("battery_sc_gold").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab).setTextureName(RefStrings.MODID + ":battery_sc_gold");
		battery_sc_lead = new ItemSelfcharger(5000).setUnlocalizedName("battery_sc_lead").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab).setTextureName(RefStrings.MODID + ":battery_sc_lead");
		battery_sc_americium = new ItemSelfcharger(10000).setUnlocalizedName("battery_sc_americium").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab).setTextureName(RefStrings.MODID + ":battery_sc_americium");

		battery_potato = new ItemBattery(1000, 0, 100).setUnlocalizedName("battery_potato").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab).setTextureName(RefStrings.MODID + ":battery_potato");
		battery_potatos = new ItemPotatos(500000, 0, 100).setUnlocalizedName("battery_potatos").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab).setTextureName(RefStrings.MODID + ":battery_potatos");
		hev_battery = new ItemFusionCore(150000).setUnlocalizedName("hev_battery").setMaxStackSize(4).setCreativeTab(MainRegistry.controlTab).setTextureName(RefStrings.MODID + ":hev_battery");
		fusion_core = new ItemFusionCore(2500000).setUnlocalizedName("fusion_core").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab).setTextureName(RefStrings.MODID + ":fusion_core");
		fusion_core_infinite = new Item().setUnlocalizedName("fusion_core_infinite").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab).setTextureName(RefStrings.MODID + ":fusion_core_infinite");
		energy_core = new ItemBattery(10000000, 0, 1000).setUnlocalizedName("energy_core").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab).setTextureName(RefStrings.MODID + ":energy_core");
		fuse = new ItemCustomLore().setUnlocalizedName("fuse").setCreativeTab(MainRegistry.controlTab).setTextureName(RefStrings.MODID + ":fuse");
		redcoil_capacitor = new ItemCapacitor(10).setUnlocalizedName("redcoil_capacitor").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab).setTextureName(RefStrings.MODID + ":redcoil_capacitor");
		euphemium_capacitor = new ItemCustomLore().setRarity(EnumRarity.epic).setUnlocalizedName("euphemium_capacitor").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab).setTextureName(RefStrings.MODID + ":redcoil_capacitor_euphemium");
		screwdriver = new ItemTooling(ToolType.SCREWDRIVER, 100).setUnlocalizedName("screwdriver");
		screwdriver_desh = new ItemTooling(ToolType.SCREWDRIVER, 0).setUnlocalizedName("screwdriver_desh");
		hand_drill = new ItemTooling(ToolType.HAND_DRILL, 100).setUnlocalizedName("hand_drill");
		hand_drill_desh = new ItemTooling(ToolType.HAND_DRILL, 0).setUnlocalizedName("hand_drill_desh");
		wrench_archineer = new ItemToolingWeapon(ToolType.WRENCH, 1000, 5F).setUnlocalizedName("wrench_archineer").setTextureName(RefStrings.MODID + ":wrench_archineer_hd");
		chemistry_set = new ItemCraftingDegradation(100).setUnlocalizedName("chemistry_set");
		chemistry_set_boron = new ItemCraftingDegradation(0).setUnlocalizedName("chemistry_set_boron");
		blowtorch = new ItemBlowtorch().setUnlocalizedName("blowtorch");
		acetylene_torch = new ItemBlowtorch().setUnlocalizedName("acetylene_torch");
		boltgun = new ItemBoltgun().setUnlocalizedName("boltgun");
		overfuse = new ItemCustomLore().setUnlocalizedName("overfuse").setMaxStackSize(1).setFull3D().setTextureName(RefStrings.MODID + ":overfuse");
		arc_electrode = new ItemArcElectrode().setUnlocalizedName("arc_electrode").setCreativeTab(MainRegistry.controlTab).setTextureName(RefStrings.MODID + ":arc_electrode");
		arc_electrode_burnt = new ItemArcElectrodeBurnt().setUnlocalizedName("arc_electrode_burnt").setCreativeTab(MainRegistry.controlTab).setTextureName(RefStrings.MODID + ":arc_electrode_burnt");

		ams_focus_blank = new Item().setUnlocalizedName("ams_focus_blank").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab).setTextureName(RefStrings.MODID + ":ams_focus_blank");
		ams_focus_limiter = new ItemCustomLore().setUnlocalizedName("ams_focus_limiter").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab).setTextureName(RefStrings.MODID + ":ams_focus_limiter");
		ams_focus_booster = new ItemCustomLore().setUnlocalizedName("ams_focus_booster").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab).setTextureName(RefStrings.MODID + ":ams_focus_booster");
		ams_muzzle = new ItemCustomLore().setUnlocalizedName("ams_muzzle").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab).setTextureName(RefStrings.MODID + ":ams_muzzle");
		ams_lens = new ItemLens(60 * 60 * 60 * 20 * 100).setUnlocalizedName("ams_lens").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab).setTextureName(RefStrings.MODID + ":ams_lens");
		ams_core_sing = new ItemAMSCore(1000000000L, 200, 10).setUnlocalizedName("ams_core_sing").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab).setTextureName(RefStrings.MODID + ":ams_core_sing");
		ams_core_wormhole = new ItemAMSCore(1500000000L, 200, 15).setUnlocalizedName("ams_core_wormhole").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab).setTextureName(RefStrings.MODID + ":ams_core_wormhole");
		ams_core_eyeofharmony = new ItemAMSCore(2500000000L, 300, 10).setUnlocalizedName("ams_core_eyeofharmony").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab).setTextureName(RefStrings.MODID + ":ams_core_eyeofharmony");
		ams_core_thingy = new ItemAMSCore(5000000000L, 250, 5).setUnlocalizedName("ams_core_thingy").setMaxStackSize(1).setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":ams_core_thingy");

		fusion_shield_tungsten = new ItemFusionShield(
			60L * 60L * 60L * 5L,
			3500,
			1.00D,
			1.00D,
			1.00D
		).setUnlocalizedName("fusion_shield_tungsten")
			.setMaxStackSize(1)
			.setCreativeTab(MainRegistry.controlTab)
			.setTextureName(RefStrings.MODID + ":fusion_shield_tungsten");

		fusion_shield_beryllium = new ItemFusionShield(
			60L * 60L * 60L * 4L,
			1287,
			1.15D,
			1.50D,
			0.85D
		).setUnlocalizedName("fusion_shield_beryllium")
			.setMaxStackSize(1)
			.setCreativeTab(MainRegistry.controlTab)
			.setTextureName(RefStrings.MODID + ":fusion_shield_beryllium");

		fusion_shield_desh = new ItemFusionShield(
			60L * 60L * 60L * 8L,
			1800,
			0.90D,
			0.80D,
			1.10D
		).setUnlocalizedName("fusion_shield_desh")
			.setMaxStackSize(1)
			.setCreativeTab(MainRegistry.controlTab)
			.setTextureName(RefStrings.MODID + ":fusion_shield_desh");

		fusion_shield_chlorophyte = new ItemFusionShield(
			60L * 60L * 60L * 15L,
			9000,
			1.35D,
			1.25D,
			0.70D
		).setUnlocalizedName("fusion_shield_chlorophyte")
			.setMaxStackSize(1)
			.setCreativeTab(MainRegistry.controlTab)
			.setTextureName(RefStrings.MODID + ":fusion_shield_chlorophyte");

		fusion_shield_vaporwave = new ItemFusionShield(
			60L * 60L * 60L * 10L,
			1916169,
			19.16D,
			16.16D,
			0.25D
		).setUnlocalizedName("fusion_shield_vaporwave")
			.setMaxStackSize(1)
			.setCreativeTab(MainRegistry.controlTab)
			.setTextureName(RefStrings.MODID + ":fusion_shield_vaporwave");

		upgrade_muffler = new ItemMuffler().setUnlocalizedName("upgrade_muffler").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":upgrade_muffler");

		upgrade_template = new ItemCustomLore().setUnlocalizedName("upgrade_template").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":upgrade_template");
		upgrade_speed_1 = new ItemMachineUpgrade(UpgradeType.SPEED, 1).setUnlocalizedName("upgrade_speed_1").setCreativeTab(MainRegistry.controlTab).setTextureName(RefStrings.MODID + ":upgrade_speed_1");
		upgrade_speed_2 = new ItemMachineUpgrade(UpgradeType.SPEED, 2).setUnlocalizedName("upgrade_speed_2").setCreativeTab(MainRegistry.controlTab).setTextureName(RefStrings.MODID + ":upgrade_speed_2");
		upgrade_speed_3 = new ItemMachineUpgrade(UpgradeType.SPEED, 3).setUnlocalizedName("upgrade_speed_3").setCreativeTab(MainRegistry.controlTab).setTextureName(RefStrings.MODID + ":upgrade_speed_3");
		upgrade_effect_1 = new ItemMachineUpgrade(UpgradeType.EFFECT, 1).setUnlocalizedName("upgrade_effect_1").setCreativeTab(MainRegistry.controlTab).setTextureName(RefStrings.MODID + ":upgrade_effect_1");
		upgrade_effect_2 = new ItemMachineUpgrade(UpgradeType.EFFECT, 2).setUnlocalizedName("upgrade_effect_2").setCreativeTab(MainRegistry.controlTab).setTextureName(RefStrings.MODID + ":upgrade_effect_2");
		upgrade_effect_3 = new ItemMachineUpgrade(UpgradeType.EFFECT, 3).setUnlocalizedName("upgrade_effect_3").setCreativeTab(MainRegistry.controlTab).setTextureName(RefStrings.MODID + ":upgrade_effect_3");
		upgrade_power_1 = new ItemMachineUpgrade(UpgradeType.POWER, 1).setUnlocalizedName("upgrade_power_1").setCreativeTab(MainRegistry.controlTab).setTextureName(RefStrings.MODID + ":upgrade_power_1");
		upgrade_power_2 = new ItemMachineUpgrade(UpgradeType.POWER, 2).setUnlocalizedName("upgrade_power_2").setCreativeTab(MainRegistry.controlTab).setTextureName(RefStrings.MODID + ":upgrade_power_2");
		upgrade_power_3 = new ItemMachineUpgrade(UpgradeType.POWER, 3).setUnlocalizedName("upgrade_power_3").setCreativeTab(MainRegistry.controlTab).setTextureName(RefStrings.MODID + ":upgrade_power_3");
		upgrade_fortune_1 = new ItemMachineUpgrade(UpgradeType.FORTUNE, 1).setUnlocalizedName("upgrade_fortune_1").setCreativeTab(MainRegistry.controlTab).setTextureName(RefStrings.MODID + ":upgrade_fortune_1");
		upgrade_fortune_2 = new ItemMachineUpgrade(UpgradeType.FORTUNE, 2).setUnlocalizedName("upgrade_fortune_2").setCreativeTab(MainRegistry.controlTab).setTextureName(RefStrings.MODID + ":upgrade_fortune_2");
		upgrade_fortune_3 = new ItemMachineUpgrade(UpgradeType.FORTUNE, 3).setUnlocalizedName("upgrade_fortune_3").setCreativeTab(MainRegistry.controlTab).setTextureName(RefStrings.MODID + ":upgrade_fortune_3");
		upgrade_afterburn_1 = new ItemMachineUpgrade(UpgradeType.AFTERBURN, 1).setUnlocalizedName("upgrade_afterburn_1").setCreativeTab(MainRegistry.controlTab).setTextureName(RefStrings.MODID + ":upgrade_afterburn_1");
		upgrade_afterburn_2 = new ItemMachineUpgrade(UpgradeType.AFTERBURN, 2).setUnlocalizedName("upgrade_afterburn_2").setCreativeTab(MainRegistry.controlTab).setTextureName(RefStrings.MODID + ":upgrade_afterburn_2");
		upgrade_afterburn_3 = new ItemMachineUpgrade(UpgradeType.AFTERBURN, 3).setUnlocalizedName("upgrade_afterburn_3").setCreativeTab(MainRegistry.controlTab).setTextureName(RefStrings.MODID + ":upgrade_afterburn_3");
		upgrade_overdrive_1 = new ItemMachineUpgrade(UpgradeType.OVERDRIVE, 1).setUnlocalizedName("upgrade_overdrive_1").setCreativeTab(MainRegistry.controlTab).setTextureName(RefStrings.MODID + ":upgrade_overdrive_1");
		upgrade_overdrive_2 = new ItemMachineUpgrade(UpgradeType.OVERDRIVE, 2).setUnlocalizedName("upgrade_overdrive_2").setCreativeTab(MainRegistry.controlTab).setTextureName(RefStrings.MODID + ":upgrade_overdrive_2");
		upgrade_overdrive_3 = new ItemMachineUpgrade(UpgradeType.OVERDRIVE, 3).setUnlocalizedName("upgrade_overdrive_3").setCreativeTab(MainRegistry.controlTab).setTextureName(RefStrings.MODID + ":upgrade_overdrive_3");
		upgrade_radius = new ItemMachineUpgrade().setUnlocalizedName("upgrade_radius").setMaxStackSize(16).setCreativeTab(MainRegistry.controlTab).setTextureName(RefStrings.MODID + ":upgrade_radius");
		upgrade_health = new ItemMachineUpgrade().setUnlocalizedName("upgrade_health").setMaxStackSize(16).setCreativeTab(MainRegistry.controlTab).setTextureName(RefStrings.MODID + ":upgrade_health");
		upgrade_smelter = new ItemMachineUpgrade().setUnlocalizedName("upgrade_smelter").setCreativeTab(MainRegistry.controlTab).setTextureName(RefStrings.MODID + ":upgrade_smelter");
		upgrade_shredder = new ItemMachineUpgrade().setUnlocalizedName("upgrade_shredder").setCreativeTab(MainRegistry.controlTab).setTextureName(RefStrings.MODID + ":upgrade_shredder");
		upgrade_centrifuge = new ItemMachineUpgrade().setUnlocalizedName("upgrade_centrifuge").setCreativeTab(MainRegistry.controlTab).setTextureName(RefStrings.MODID + ":upgrade_centrifuge");
		upgrade_crystallizer = new ItemMachineUpgrade().setUnlocalizedName("upgrade_crystallizer").setCreativeTab(MainRegistry.controlTab).setTextureName(RefStrings.MODID + ":upgrade_crystallizer");
		upgrade_nullifier = new ItemMachineUpgrade().setUnlocalizedName("upgrade_nullifier").setCreativeTab(MainRegistry.controlTab).setTextureName(RefStrings.MODID + ":upgrade_nullifier");
		upgrade_screm = new ItemMachineUpgrade().setUnlocalizedName("upgrade_screm").setCreativeTab(MainRegistry.controlTab).setTextureName(RefStrings.MODID + ":upgrade_screm");
		upgrade_gc_speed = new ItemMachineUpgrade().setUnlocalizedName("upgrade_gc_speed").setCreativeTab(MainRegistry.controlTab).setTextureName(RefStrings.MODID + ":upgrade_gc_speed");
		upgrade_5g = new ItemMachineUpgrade().setUnlocalizedName("upgrade_5g").setCreativeTab(MainRegistry.controlTab).setTextureName(RefStrings.MODID + ":upgrade_5g");
		upgrade_stack = new ItemMetaUpgrade(3).setUnlocalizedName("upgrade_stack").setCreativeTab(MainRegistry.controlTab).setTextureName(RefStrings.MODID + ":upgrade_stack");
		upgrade_ejector = new ItemMetaUpgrade(3).setUnlocalizedName("upgrade_ejector").setCreativeTab(MainRegistry.controlTab).setTextureName(RefStrings.MODID + ":upgrade_ejector");

		wand = new ItemWand().setUnlocalizedName("wand_k").setMaxStackSize(1).setCreativeTab(MainRegistry.consumableTab).setFull3D().setTextureName(RefStrings.MODID + ":wand");
		wand_s = new ItemWandS().setUnlocalizedName("wand_s").setMaxStackSize(1).setCreativeTab(MainRegistry.consumableTab).setFull3D().setTextureName(RefStrings.MODID + ":wand_s");
		wand_d = new ItemWandD().setUnlocalizedName("wand_d").setMaxStackSize(1).setCreativeTab(MainRegistry.consumableTab).setFull3D().setTextureName(RefStrings.MODID + ":wand_d");
		bomb_site_wand = new ItemBombSiteWand().setUnlocalizedName("bomb_site_wand").setMaxStackSize(1).setCreativeTab(MainRegistry.consumableTab).setFull3D().setTextureName(RefStrings.MODID + ":wand");

		structure_single = new ItemStructureSingle().setUnlocalizedName("structure_single").setMaxStackSize(1).setCreativeTab(MainRegistry.partsTab).setFull3D().setTextureName(RefStrings.MODID + ":structure_single");
		structure_solid = new ItemStructureSolid().setUnlocalizedName("structure_solid").setMaxStackSize(1).setCreativeTab(MainRegistry.partsTab).setFull3D().setTextureName(RefStrings.MODID + ":structure_solid");
		structure_pattern = new ItemStructurePattern().setUnlocalizedName("structure_pattern").setMaxStackSize(1).setCreativeTab(MainRegistry.partsTab).setFull3D().setTextureName(RefStrings.MODID + ":structure_pattern");
		structure_randomized = new ItemStructureRandomized().setUnlocalizedName("structure_randomized").setMaxStackSize(1).setCreativeTab(MainRegistry.partsTab).setFull3D().setTextureName(RefStrings.MODID + ":structure_randomized");
		structure_randomly = new ItemStructureRandomly().setUnlocalizedName("structure_randomly").setMaxStackSize(1).setCreativeTab(MainRegistry.partsTab).setFull3D().setTextureName(RefStrings.MODID + ":structure_randomly");
		structure_custommachine = new ItemCMStructure().setUnlocalizedName("structure_custommachine").setMaxStackSize(1).setCreativeTab(MainRegistry.consumableTab).setFull3D().setTextureName(RefStrings.MODID + ":structure_custommachine");

		rod_of_discord = new ItemDiscord().setUnlocalizedName("rod_of_discord").setMaxStackSize(1).setCreativeTab(MainRegistry.consumableTab).setFull3D().setTextureName(RefStrings.MODID + ":rod_of_discord");

		nuke_starter_kit = new ItemStarterKit().setUnlocalizedName("nuke_starter_kit").setMaxStackSize(1).setCreativeTab(MainRegistry.consumableTab).setTextureName(RefStrings.MODID + ":nuke_starter_kit");
		nuke_advanced_kit = new ItemStarterKit().setUnlocalizedName("nuke_advanced_kit").setMaxStackSize(1).setCreativeTab(MainRegistry.consumableTab).setTextureName(RefStrings.MODID + ":nuke_advanced_kit");
		nuke_commercially_kit = new ItemStarterKit().setUnlocalizedName("nuke_commercially_kit").setMaxStackSize(1).setCreativeTab(MainRegistry.consumableTab).setTextureName(RefStrings.MODID + ":nuke_commercially_kit");
		nuke_electric_kit = new ItemStarterKit().setUnlocalizedName("nuke_electric_kit").setMaxStackSize(1).setCreativeTab(MainRegistry.consumableTab).setTextureName(RefStrings.MODID + ":nuke_electric_kit");
		gadget_kit = new ItemStarterKit().setUnlocalizedName("gadget_kit").setMaxStackSize(1).setCreativeTab(MainRegistry.nukeTab).setTextureName(RefStrings.MODID + ":gadget_kit");
		boy_kit = new ItemStarterKit().setUnlocalizedName("boy_kit").setMaxStackSize(1).setCreativeTab(MainRegistry.nukeTab).setTextureName(RefStrings.MODID + ":boy_kit");
		man_kit = new ItemStarterKit().setUnlocalizedName("man_kit").setMaxStackSize(1).setCreativeTab(MainRegistry.nukeTab).setTextureName(RefStrings.MODID + ":man_kit");
		mike_kit = new ItemStarterKit().setUnlocalizedName("mike_kit").setMaxStackSize(1).setCreativeTab(MainRegistry.nukeTab).setTextureName(RefStrings.MODID + ":mike_kit");
		tsar_kit = new ItemStarterKit().setUnlocalizedName("tsar_kit").setMaxStackSize(1).setCreativeTab(MainRegistry.nukeTab).setTextureName(RefStrings.MODID + ":tsar_kit");
		multi_kit = new ItemStarterKit().setUnlocalizedName("multi_kit").setMaxStackSize(1).setCreativeTab(MainRegistry.nukeTab).setTextureName(RefStrings.MODID + ":multi_kit");
		custom_kit = new ItemStarterKit().setUnlocalizedName("custom_kit").setMaxStackSize(1).setCreativeTab(MainRegistry.nukeTab).setTextureName(RefStrings.MODID + ":custom_kit");
		grenade_kit = new ItemStarterKit().setUnlocalizedName("grenade_kit").setMaxStackSize(1).setCreativeTab(MainRegistry.weaponTab).setTextureName(RefStrings.MODID + ":grenade_kit");
		//fleija_kit = new ItemStarterKit().setUnlocalizedName("fleija_kit").setMaxStackSize(1).setCreativeTab(MainRegistry.nukeTab).setTextureName(RefStrings.MODID + ":fleija_kit");
		//prototype_kit = new ItemStarterKit().setUnlocalizedName("prototype_kit").setMaxStackSize(1).setCreativeTab(MainRegistry.nukeTab).setTextureName(RefStrings.MODID + ":prototype_kit");
		missile_kit = new ItemStarterKit().setUnlocalizedName("missile_kit").setMaxStackSize(1).setCreativeTab(MainRegistry.missileTab).setTextureName(RefStrings.MODID + ":missile_kit");
		t45_kit = new ItemStarterKit().setUnlocalizedName("t45_kit").setMaxStackSize(1).setCreativeTab(MainRegistry.consumableTab).setTextureName(RefStrings.MODID + ":t45_kit");
		euphemium_kit = new ItemStarterKit().setUnlocalizedName("euphemium_kit").setMaxStackSize(1).setTextureName(RefStrings.MODID + ":code");
		//solinium_kit = new ItemStarterKit().setUnlocalizedName("solinium_kit").setMaxStackSize(1).setCreativeTab(MainRegistry.nukeTab).setTextureName(RefStrings.MODID + ":solinium_kit");
		hazmat_kit = new ItemStarterKit().setUnlocalizedName("hazmat_kit").setMaxStackSize(1).setCreativeTab(MainRegistry.consumableTab).setTextureName(RefStrings.MODID + ":hazmat_kit");
		hazmat_red_kit = new ItemStarterKit().setUnlocalizedName("hazmat_red_kit").setMaxStackSize(1).setCreativeTab(MainRegistry.consumableTab).setTextureName(RefStrings.MODID + ":hazmat_red_kit");
		hazmat_grey_kit = new ItemStarterKit().setUnlocalizedName("hazmat_grey_kit").setMaxStackSize(1).setCreativeTab(MainRegistry.consumableTab).setTextureName(RefStrings.MODID + ":hazmat_grey_kit");
		kit_custom = new ItemKitCustom().setUnlocalizedName("kit_custom").setCreativeTab(MainRegistry.consumableTab).setTextureName(RefStrings.MODID + ":kit");
		kit_toolbox_empty = new Item().setUnlocalizedName("kit_toolbox_empty").setCreativeTab(MainRegistry.consumableTab).setTextureName(RefStrings.MODID + ":kit_toolbox_empty");
		kit_toolbox = new ItemKitNBT().setUnlocalizedName("kit_toolbox").setCreativeTab(MainRegistry.consumableTab).setContainerItem(kit_toolbox_empty).setTextureName(RefStrings.MODID + ":kit_toolbox");

		loot_10 = new ItemLootCrate().setUnlocalizedName("loot_10").setMaxStackSize(1).setCreativeTab(MainRegistry.missileTab).setTextureName(RefStrings.MODID + ":loot_10");
		loot_15 = new ItemLootCrate().setUnlocalizedName("loot_15").setMaxStackSize(1).setCreativeTab(MainRegistry.missileTab).setTextureName(RefStrings.MODID + ":loot_15");
		loot_misc = new ItemLootCrate().setUnlocalizedName("loot_misc").setMaxStackSize(1).setCreativeTab(MainRegistry.missileTab).setTextureName(RefStrings.MODID + ":loot_misc");

		//clip_revolver_iron = new ItemClip(ammo_357.stackFromEnum(20, Ammo357Magnum.IRON)).setUnlocalizedName("clip_revolver_iron").setCreativeTab(MainRegistry.weaponTab).setTextureName(RefStrings.MODID + ":clip_revolver_iron");
		//clip_revolver = new ItemClip(ammo_357.stackFromEnum(12, Ammo357Magnum.LEAD)).setUnlocalizedName("clip_revolver").setCreativeTab(MainRegistry.weaponTab).setTextureName(RefStrings.MODID + ":clip_revolver");
		//clip_revolver_gold = new ItemClip(ammo_357.stackFromEnum(6, Ammo357Magnum.GOLD)).setUnlocalizedName("clip_revolver_gold").setCreativeTab(MainRegistry.weaponTab).setTextureName(RefStrings.MODID + ":clip_revolver_gold");
		//clip_revolver_lead = new ItemClip(ammo_357.stackFromEnum(6, Ammo357Magnum.NUCLEAR)).setUnlocalizedName("clip_revolver_lead").setCreativeTab(MainRegistry.weaponTab).setTextureName(RefStrings.MODID + ":clip_revolver_lead");
		//clip_revolver_schrabidium = new ItemClip(ammo_357.stackFromEnum(2, Ammo357Magnum.SCHRABIDIUM)).setUnlocalizedName("clip_revolver_schrabidium").setCreativeTab(MainRegistry.weaponTab).setTextureName(RefStrings.MODID + ":clip_revolver_schrabidium");
		//clip_revolver_cursed = new ItemClip(ammo_357.stackFromEnum(17, Ammo357Magnum.STEEL)).setUnlocalizedName("clip_revolver_cursed").setCreativeTab(MainRegistry.weaponTab).setTextureName(RefStrings.MODID + ":clip_revolver_cursed");
		//clip_revolver_nightmare = new ItemClip(ammo_357.stackFromEnum(6, Ammo357Magnum.NIGHTMARE1)).setUnlocalizedName("clip_revolver_nightmare").setCreativeTab(MainRegistry.weaponTab).setTextureName(RefStrings.MODID + ":clip_revolver_nightmare");
		//clip_revolver_nightmare2 = new ItemClip(ammo_357.stackFromEnum(6, Ammo357Magnum.NIGHTMARE2)).setUnlocalizedName("clip_revolver_nightmare2").setCreativeTab(MainRegistry.weaponTab).setTextureName(RefStrings.MODID + ":clip_revolver_nightmare2");
		//clip_revolver_pip = new ItemClip(ammo_44.stackFromEnum(6, Ammo44Magnum.PIP)).setUnlocalizedName("clip_revolver_pip").setCreativeTab(MainRegistry.weaponTab).setTextureName(RefStrings.MODID + ":clip_revolver_pip");
		//clip_revolver_nopip = new ItemClip(ammo_44.stackFromEnum(6, Ammo44Magnum.STOCK)).setUnlocalizedName("clip_revolver_nopip").setCreativeTab(MainRegistry.weaponTab).setTextureName(RefStrings.MODID + ":clip_revolver_nopip");
		//clip_rpg = new ItemClip(ammo_rocket.stackFromEnum(4, AmmoRocket.STOCK)).setUnlocalizedName("clip_rpg").setCreativeTab(MainRegistry.weaponTab).setTextureName(RefStrings.MODID + ":clip_rpg_alt");
		clip_stinger = new ItemClip(ammo_stinger_rocket.stackFromEnum(4, AmmoStinger.STOCK)).setUnlocalizedName("clip_stinger").setCreativeTab(MainRegistry.weaponTab).setTextureName(RefStrings.MODID + ":clip_stinger");
		//clip_fatman = new ItemClip(ammo_nuke.stackFromEnum(6, AmmoFatman.STOCK)).setUnlocalizedName("clip_fatman").setCreativeTab(MainRegistry.weaponTab).setTextureName(RefStrings.MODID + ":clip_fatman");
		//clip_mirv = new ItemClip(ammo_nuke.stackFromEnum(3, AmmoFatman.MIRV)).setUnlocalizedName("clip_mirv").setCreativeTab(MainRegistry.weaponTab).setTextureName(RefStrings.MODID + ":clip_mirv");
		//clip_bf = new ItemClip(ammo_nuke.stackFromEnum(2, AmmoFatman.BALEFIRE)).setUnlocalizedName("clip_bf").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":clip_bf");
		//clip_mp40 = new ItemClip(ammo_9mm.stackFromEnum(32, Ammo9mm.STOCK)).setUnlocalizedName("clip_mp40").setCreativeTab(MainRegistry.weaponTab).setTextureName(RefStrings.MODID + ":clip_mp40");
		//clip_uzi = new ItemClip(ammo_22lr.stackFromEnum(32, Ammo22LR.STOCK)).setUnlocalizedName("clip_uzi").setCreativeTab(MainRegistry.weaponTab).setTextureName(RefStrings.MODID + ":clip_uzi");
		//clip_uboinik = new ItemClip(ammo_12gauge.stackFromEnum(12, Ammo12Gauge.STOCK)).setUnlocalizedName("clip_uboinik").setCreativeTab(MainRegistry.weaponTab).setTextureName(RefStrings.MODID + ":clip_uboinik");
		//clip_lever_action = new ItemClip(ammo_20gauge.stackFromEnum(12, Ammo20Gauge.STOCK)).setUnlocalizedName("clip_lever_action").setCreativeTab(MainRegistry.weaponTab).setTextureName(RefStrings.MODID + ":clip_lever_action");
		//clip_bolt_action = new ItemClip(ammo_20gauge.stackFromEnum(12, Ammo20Gauge.SLUG)).setUnlocalizedName("clip_bolt_action").setCreativeTab(MainRegistry.weaponTab).setTextureName(RefStrings.MODID + ":clip_bolt_action");
		//clip_osipr = new ItemClip(new ItemStack(gun_osipr_ammo, 3)).setUnlocalizedName("clip_osipr").setCreativeTab(MainRegistry.weaponTab).setTextureName(RefStrings.MODID + ":clip_osipr");
		//clip_immolator = new ItemClip(new ItemStack(gun_immolator_ammo, 60)).setUnlocalizedName("clip_immolator").setCreativeTab(MainRegistry.weaponTab).setTextureName(RefStrings.MODID + ":clip_immolator");
		//clip_cryolator = new ItemClip(new ItemStack(gun_cryolator_ammo, 60)).setUnlocalizedName("clip_cryolator").setCreativeTab(MainRegistry.weaponTab).setTextureName(RefStrings.MODID + ":clip_cryolator");
		//clip_mp = new ItemClip(ammo_556.stackFromEnum(2, Ammo556mm.GOLD)).setUnlocalizedName("clip_mp").setCreativeTab(MainRegistry.weaponTab).setTextureName(RefStrings.MODID + ":clip_mp");
		//clip_xvl1456 = new ItemClip(new ItemStack(gun_xvl1456_ammo, 50)).setUnlocalizedName("clip_xvl1456").setCreativeTab(MainRegistry.weaponTab).setTextureName(RefStrings.MODID + ":clip_xvl1456");
		//clip_emp = new ItemClip(new ItemStack(gun_emp_ammo, 12)).setUnlocalizedName("clip_emp").setCreativeTab(MainRegistry.weaponTab).setTextureName(RefStrings.MODID + ":clip_emp");
		//clip_jack = new ItemClip(new ItemStack(gun_jack_ammo, 12)).setUnlocalizedName("clip_jack").setCreativeTab(MainRegistry.weaponTab).setTextureName(RefStrings.MODID + ":clip_jack");
		//clip_spark = new ItemClip(new ItemStack(gun_spark_ammo, 12)).setUnlocalizedName("clip_spark").setCreativeTab(MainRegistry.weaponTab).setTextureName(RefStrings.MODID + ":clip_spark");
		//clip_hp = new ItemClip(new ItemStack(gun_hp_ammo, 24)).setUnlocalizedName("clip_hp").setCreativeTab(MainRegistry.weaponTab).setTextureName(RefStrings.MODID + ":clip_hp");
		//clip_euthanasia = new ItemClip(new ItemStack(gun_euthanasia_ammo, 32)).setUnlocalizedName("clip_euthanasia").setCreativeTab(MainRegistry.weaponTab).setTextureName(RefStrings.MODID + ":clip_euthanasia");
		//clip_defabricator = new ItemClip(new ItemStack(gun_defabricator_ammo, 50)).setUnlocalizedName("clip_defabricator").setCreativeTab(MainRegistry.weaponTab).setTextureName(RefStrings.MODID + ":clip_defabricator");

		ammo_container = new ItemAmmoContainer().setUnlocalizedName("ammo_container").setCreativeTab(MainRegistry.weaponTab).setTextureName(RefStrings.MODID + ":ammo_container");

		ingot_euphemium = new ItemCustomLore().setRarity(EnumRarity.epic).setUnlocalizedName("ingot_euphemium").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":ingot_euphemium");
		nugget_euphemium = new ItemCustomLore().setRarity(EnumRarity.epic).setUnlocalizedName("nugget_euphemium").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":nugget_euphemium");
		watch = new ItemCustomLore().setRarity(EnumRarity.epic).setUnlocalizedName("watch").setMaxStackSize(1).setTextureName(RefStrings.MODID + ":watch");
		//apple_euphemium = new ItemAppleEuphemium(20, 100, false).setUnlocalizedName("apple_euphemium").setMaxStackSize(1).setTextureName(RefStrings.MODID + ":apple_euphemium");

		igniter = new ItemCustomLore().setUnlocalizedName("igniter").setMaxStackSize(1).setFull3D().setCreativeTab(MainRegistry.nukeTab).setTextureName(RefStrings.MODID + ":trigger");
		detonator = new ItemDetonator().setUnlocalizedName("detonator").setMaxStackSize(1).setFull3D().setCreativeTab(MainRegistry.nukeTab).setTextureName(RefStrings.MODID + ":detonator");
		detonator_multi = new ItemMultiDetonator().setUnlocalizedName("detonator_multi").setMaxStackSize(1).setFull3D().setCreativeTab(MainRegistry.nukeTab).setTextureName(RefStrings.MODID + ":detonator_multi");
		detonator_laser = new ItemLaserDetonator().setUnlocalizedName("detonator_laser").setMaxStackSize(1).setFull3D().setCreativeTab(MainRegistry.nukeTab).setTextureName(RefStrings.MODID + ":detonator_laser");
		detonator_deadman = new ItemDrop().setUnlocalizedName("detonator_deadman").setMaxStackSize(1).setFull3D().setCreativeTab(MainRegistry.nukeTab).setTextureName(RefStrings.MODID + ":detonator_deadman");
		detonator_de = new ItemDrop().setUnlocalizedName("detonator_de").setMaxStackSize(1).setFull3D().setCreativeTab(MainRegistry.nukeTab).setTextureName(RefStrings.MODID + ":detonator_de");
		//crate_caller = new ItemCrateCaller().setUnlocalizedName("crate_caller").setMaxStackSize(1).setFull3D().setCreativeTab(MainRegistry.consumableTab).setTextureName(RefStrings.MODID + ":crate_caller");
		bomb_caller = new ItemBombCaller().setUnlocalizedName("bomb_caller").setMaxStackSize(1).setFull3D().setCreativeTab(MainRegistry.consumableTab).setTextureName(RefStrings.MODID + ":bomb_caller");
		//meteor_remote = new ItemMeteorRemote().setUnlocalizedName("meteor_remote").setMaxStackSize(1).setFull3D().setCreativeTab(MainRegistry.consumableTab).setTextureName(RefStrings.MODID + ":meteor_remote");
		anchor_remote = new ItemAnchorRemote().setUnlocalizedName("anchor_remote").setMaxStackSize(1).setFull3D().setCreativeTab(MainRegistry.consumableTab).setTextureName(RefStrings.MODID + ":anchor_remote");
		//spawn_chopper = new ItemChopper().setUnlocalizedName("chopper").setMaxStackSize(1).setCreativeTab(MainRegistry.consumableTab).setTextureName(RefStrings.MODID + ":chopper");
		spawn_worm = new ItemChopper().setUnlocalizedName("spawn_worm").setMaxStackSize(1).setCreativeTab(MainRegistry.consumableTab).setTextureName(RefStrings.MODID + ":spawn_worm");
		//spawn_ufo = new ItemChopper().setUnlocalizedName("spawn_ufo").setMaxStackSize(1).setCreativeTab(MainRegistry.consumableTab).setTextureName(RefStrings.MODID + ":spawn_ufo");
		//spawn_duck = new ItemChopper().setUnlocalizedName("spawn_duck").setMaxStackSize(16).setCreativeTab(MainRegistry.consumableTab).setTextureName(RefStrings.MODID + ":spawn_duck");

		spawn_uap = new ItemChopper().setUnlocalizedName("spawn_uap").setMaxStackSize(1).setCreativeTab(MainRegistry.consumableTab).setTextureName(RefStrings.MODID + ":spawn_uap");


		linker = new ItemTeleLink().setUnlocalizedName("linker").setMaxStackSize(1).setCreativeTab(MainRegistry.consumableTab).setTextureName(RefStrings.MODID + ":linker");
		reactor_sensor = new ItemReactorSensor().setUnlocalizedName("reactor_sensor").setMaxStackSize(1).setCreativeTab(MainRegistry.consumableTab).setTextureName(RefStrings.MODID + ":reactor_sensor");
		oil_detector = new ItemOilDetector().setUnlocalizedName("oil_detector").setMaxStackSize(1).setCreativeTab(MainRegistry.consumableTab).setTextureName(RefStrings.MODID + ":oil_detector");
		//turret_control = new ItemTurretControl().setUnlocalizedName("turret_control").setFull3D().setMaxStackSize(1).setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":turret_control");
		turret_chip = new ItemTurretChip().setUnlocalizedName("turret_chip").setMaxStackSize(1).setCreativeTab(MainRegistry.weaponTab).setTextureName(RefStrings.MODID + ":turret_chip");
		//turret_biometry = new ItemTurretBiometry().setUnlocalizedName("turret_biometry").setFull3D().setMaxStackSize(1).setCreativeTab(MainRegistry.weaponTab).setTextureName(RefStrings.MODID + ":rei_scanner");
		dosimeter = new ItemDosimeter().setUnlocalizedName("dosimeter").setMaxStackSize(1).setCreativeTab(MainRegistry.consumableTab).setTextureName(RefStrings.MODID + ":dosimeter");
		geiger_counter = new ItemGeigerCounter().setUnlocalizedName("geiger_counter").setMaxStackSize(1).setCreativeTab(MainRegistry.consumableTab).setTextureName(RefStrings.MODID + ":geiger_counter");
		digamma_diagnostic = new ItemDigammaDiagnostic().setUnlocalizedName("digamma_diagnostic").setMaxStackSize(1).setCreativeTab(MainRegistry.consumableTab).setTextureName(RefStrings.MODID + ":digamma_diagnostic");
		pollution_detector = new ItemPollutionDetector().setUnlocalizedName("pollution_detector").setMaxStackSize(1).setCreativeTab(MainRegistry.consumableTab).setTextureName(RefStrings.MODID + ":pollution_detector");
		ore_density_scanner = new ItemOreDensityScanner().setUnlocalizedName("ore_density_scanner").setMaxStackSize(1).setCreativeTab(MainRegistry.consumableTab).setTextureName(RefStrings.MODID + ":ore_density_scanner");
		survey_scanner = new ItemSurveyScanner().setUnlocalizedName("survey_scanner").setMaxStackSize(1).setCreativeTab(MainRegistry.consumableTab).setTextureName(RefStrings.MODID + ":survey_scanner");
		mirror_tool = new ItemMirrorTool().setUnlocalizedName("mirror_tool").setMaxStackSize(1).setCreativeTab(MainRegistry.consumableTab).setTextureName(RefStrings.MODID + ":mirror_tool");
		rbmk_tool = new ItemRBMKTool().setUnlocalizedName("rbmk_tool").setMaxStackSize(1).setCreativeTab(MainRegistry.consumableTab).setTextureName(RefStrings.MODID + ":rbmk_tool");
		coltan_tool = new ItemColtanCompass().setUnlocalizedName("coltan_tool").setMaxStackSize(1).setCreativeTab(MainRegistry.consumableTab).setTextureName(RefStrings.MODID + ":coltass");
		power_net_tool = new ItemPowerNetTool().setUnlocalizedName("power_net_tool").setMaxStackSize(1).setCreativeTab(MainRegistry.consumableTab).setTextureName(RefStrings.MODID + ":power_net_tool");
		analysis_tool = new ItemAnalysisTool().setUnlocalizedName("analysis_tool").setMaxStackSize(1).setCreativeTab(MainRegistry.consumableTab).setTextureName(RefStrings.MODID + ":analysis_tool");
		coupling_tool = new ItemCouplingTool().setUnlocalizedName("coupling_tool").setMaxStackSize(1).setCreativeTab(MainRegistry.consumableTab).setTextureName(RefStrings.MODID + ":coupling_tool");
		drone_linker = new ItemDroneLinker().setUnlocalizedName("drone_linker").setMaxStackSize(1).setCreativeTab(MainRegistry.consumableTab).setTextureName(RefStrings.MODID + ":drone_linker");
		radar_linker = new ItemRadarLinker().setUnlocalizedName("radar_linker").setMaxStackSize(1).setCreativeTab(MainRegistry.consumableTab).setTextureName(RefStrings.MODID + ":radar_linker");
		settings_tool = new ItemSettingsTool().setUnlocalizedName("settings_tool").setMaxStackSize(1).setCreativeTab(MainRegistry.consumableTab).setTextureName(RefStrings.MODID + ":settings_tool");

		key = new ItemKey().setUnlocalizedName("key").setMaxStackSize(1).setCreativeTab(MainRegistry.consumableTab).setTextureName(RefStrings.MODID + ":key");
		key_red = new ItemCustomLore().setUnlocalizedName("key_red").setMaxStackSize(1).setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":key_red");
		key_red_cracked = new ItemCustomLore().setUnlocalizedName("key_red_cracked").setMaxStackSize(1).setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":key_red_cracked");
		key_kit = new ItemCounterfitKeys().setUnlocalizedName("key_kit").setMaxStackSize(1).setCreativeTab(MainRegistry.consumableTab).setTextureName(RefStrings.MODID + ":key_pair");
		key_fake = new ItemKey().setUnlocalizedName("key_fake").setMaxStackSize(1).setCreativeTab(MainRegistry.consumableTab).setTextureName(RefStrings.MODID + ":key_gold");
		pin = new ItemCustomLore().setUnlocalizedName("pin").setMaxStackSize(8).setCreativeTab(MainRegistry.consumableTab).setTextureName(RefStrings.MODID + ":pin");
		padlock_rusty = new ItemLock(0.65).setUnlocalizedName("padlock_rusty").setMaxStackSize(1).setCreativeTab(MainRegistry.consumableTab).setTextureName(RefStrings.MODID + ":padlock_rusty");
		padlock = new ItemLock(0.4).setUnlocalizedName("padlock").setMaxStackSize(1).setCreativeTab(MainRegistry.consumableTab).setTextureName(RefStrings.MODID + ":padlock");
		padlock_reinforced = new ItemLock(0.05).setUnlocalizedName("padlock_reinforced").setMaxStackSize(1).setCreativeTab(MainRegistry.consumableTab).setTextureName(RefStrings.MODID + ":padlock_reinforced");
		padlock_unbreakable = new ItemLock(0.01).setUnlocalizedName("padlock_unbreakable").setMaxStackSize(1).setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":padlock_unbreakable");

		mech_key = new ItemCustomLore().setUnlocalizedName("mech_key").setMaxStackSize(1).setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":mech_key");

		template_folder = new ItemTemplateFolder().setUnlocalizedName("template_folder").setMaxStackSize(1).setCreativeTab(MainRegistry.templateTab).setTextureName(RefStrings.MODID + ":template_folder");
		journal_pip = new ItemTemplateFolder().setUnlocalizedName("journal_pip").setMaxStackSize(1).setCreativeTab(MainRegistry.templateTab).setTextureName(RefStrings.MODID + ":journal_pip");
		journal_bj = new ItemTemplateFolder().setUnlocalizedName("journal_bj").setMaxStackSize(1).setCreativeTab(MainRegistry.templateTab).setTextureName(RefStrings.MODID + ":journal_bj");
		journal_silver = new ItemTemplateFolder().setUnlocalizedName("journal_silver").setMaxStackSize(1).setCreativeTab(MainRegistry.templateTab).setTextureName(RefStrings.MODID + ":journal_silver");
		assembly_template = new ItemAssemblyTemplate().setUnlocalizedName("assembly_template").setMaxStackSize(1).setCreativeTab(MainRegistry.templateTab).setTextureName(RefStrings.MODID + ":assembly_template");
		chemistry_template = new ItemChemistryTemplate().setUnlocalizedName("chemistry_template").setMaxStackSize(1).setCreativeTab(MainRegistry.templateTab).setTextureName(RefStrings.MODID + ":chemistry_template");
		chemistry_icon = new ItemChemistryIcon().setUnlocalizedName("chemistry_icon").setMaxStackSize(1).setCreativeTab(MainRegistry.partsTab);
		crucible_template = new ItemCrucibleTemplate().setUnlocalizedName("crucible_template").setMaxStackSize(1).setCreativeTab(MainRegistry.templateTab).setTextureName(RefStrings.MODID + ":crucible_template");
		fluid_identifier = new ItemFluidIdentifier().setUnlocalizedName("fluid_identifier").setMaxStackSize(1).setCreativeTab(MainRegistry.templateTab).setTextureName(RefStrings.MODID + ":fluid_identifier");
		fluid_identifier_multi = new ItemFluidIDMulti().setUnlocalizedName("fluid_identifier_multi").setMaxStackSize(1).setCreativeTab(MainRegistry.templateTab).setTextureName(RefStrings.MODID + ":fluid_identifier_multi");
		fluid_icon = new ItemFluidIcon().setUnlocalizedName("fluid_icon").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":fluid_icon");
		fluid_tank_empty = new Item().setUnlocalizedName("fluid_tank_empty").setCreativeTab(MainRegistry.controlTab).setTextureName(RefStrings.MODID + ":fluid_tank");
		fluid_tank_full = new ItemFluidTank().setUnlocalizedName("fluid_tank_full").setContainerItem(ModItems.fluid_tank_empty).setCreativeTab(MainRegistry.controlTab).setTextureName(RefStrings.MODID + ":fluid_tank");
		fluid_tank_lead_empty = new Item().setUnlocalizedName("fluid_tank_lead_empty").setCreativeTab(MainRegistry.controlTab).setTextureName(RefStrings.MODID + ":fluid_tank_lead");
		fluid_tank_lead_full = new ItemFluidTank().setUnlocalizedName("fluid_tank_lead_full").setContainerItem(ModItems.fluid_tank_lead_empty).setCreativeTab(MainRegistry.controlTab).setTextureName(RefStrings.MODID + ":fluid_tank_lead");
		fluid_barrel_empty = new Item().setUnlocalizedName("fluid_barrel_empty").setCreativeTab(MainRegistry.controlTab).setTextureName(RefStrings.MODID + ":fluid_barrel");
		fluid_barrel_full = new ItemFluidTank().setUnlocalizedName("fluid_barrel_full").setContainerItem(ModItems.fluid_barrel_empty).setCreativeTab(MainRegistry.controlTab).setTextureName(RefStrings.MODID + ":fluid_barrel");
		fluid_barrel_infinite = new ItemInfiniteFluid(null, 1_000_000_000).setUnlocalizedName("fluid_barrel_infinite").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab).setTextureName(RefStrings.MODID + ":fluid_barrel_infinite");
		pipette = new ItemPipette().setUnlocalizedName("pipette").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab).setTextureName(RefStrings.MODID + ":pipette");
		pipette_boron = new ItemPipette().setUnlocalizedName("pipette_boron").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab).setTextureName(RefStrings.MODID + ":pipette_boron");
		pipette_laboratory = new ItemPipette().setUnlocalizedName("pipette_laboratory").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab).setTextureName(RefStrings.MODID + ":pipette_laboratory");
		siphon = new ItemFluidSiphon().setUnlocalizedName("siphon").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab).setTextureName(RefStrings.MODID + ":siphon");
		inf_water = new ItemInfiniteFluid(Fluids.WATER, 50, true).setUnlocalizedName("inf_water").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab).setTextureName(RefStrings.MODID + ":inf_water");
		inf_water_mk2 = new ItemInfiniteFluid(Fluids.WATER, 500, true).setUnlocalizedName("inf_water_mk2").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab).setTextureName(RefStrings.MODID + ":inf_water_mk2");

		transporter_linker = new ItemTransporterLinker().setUnlocalizedName("transporter_linker").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab).setTextureName(RefStrings.MODID + ":transporter_linker");

		FluidTank.noDualUnload.add(fluid_barrel_infinite);
		FluidTank.noDualUnload.add(inf_water);
		FluidTank.noDualUnload.add(inf_water_mk2);

		disperser_canister_empty = new Item().setUnlocalizedName("disperser_canister_empty").setCreativeTab(MainRegistry.weaponTab).setTextureName(RefStrings.MODID + ":disperser_canister");
		disperser_canister = new ItemDisperser().setUnlocalizedName("disperser_canister").setContainerItem(ModItems.disperser_canister_empty).setCreativeTab(MainRegistry.weaponTab).setTextureName(RefStrings.MODID + ":disperser_canister");

		//glyphid_gland_empty = new Item().setUnlocalizedName("glyphid_gland_empty").setCreativeTab(MainRegistry.weaponTab).setTextureName(RefStrings.MODID + ":glyphid_gland");
		//glyphid_gland = new ItemDisperser().setUnlocalizedName("glyphid_gland").setContainerItem(ModItems.glyphid_gland_empty).setCreativeTab(MainRegistry.weaponTab).setTextureName(RefStrings.MODID + ":glyphid_gland");

		siren_track = new ItemCassette().setUnlocalizedName("siren_track").setMaxStackSize(1).setCreativeTab(MainRegistry.templateTab).setTextureName(RefStrings.MODID + ":cassette");
		fluid_duct = new ItemFluidDuct().setUnlocalizedName("fluid_duct").setCreativeTab(MainRegistry.templateTab).setTextureName(RefStrings.MODID + ":duct");

		bobmazon_materials = new ItemCatalog().setUnlocalizedName("bobmazon_materials").setMaxStackSize(1).setCreativeTab(MainRegistry.templateTab).setTextureName(RefStrings.MODID + ":bobmazon_materials");
		bobmazon_machines = new ItemCatalog().setUnlocalizedName("bobmazon_machines").setMaxStackSize(1).setCreativeTab(MainRegistry.templateTab).setTextureName(RefStrings.MODID + ":bobmazon_machines");
		bobmazon_weapons = new ItemCatalog().setUnlocalizedName("bobmazon_weapons").setMaxStackSize(1).setCreativeTab(MainRegistry.templateTab).setTextureName(RefStrings.MODID + ":bobmazon_weapons");
		bobmazon_tools = new ItemCatalog().setUnlocalizedName("bobmazon_tools").setMaxStackSize(1).setCreativeTab(MainRegistry.templateTab).setTextureName(RefStrings.MODID + ":bobmazon_tools");
		bobmazon_hidden = new ItemCatalog().setUnlocalizedName("bobmazon_hidden").setMaxStackSize(1).setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":bobmazon_special");

		//euphemium_helmet = new ArmorEuphemium(MainRegistry.aMatEuph, 0).setUnlocalizedName("euphemium_helmet").setMaxStackSize(1).setTextureName(RefStrings.MODID + ":euphemium_helmet");
		//euphemium_plate = new ArmorEuphemium(MainRegistry.aMatEuph, 1).setUnlocalizedName("euphemium_plate").setMaxStackSize(1).setTextureName(RefStrings.MODID + ":euphemium_plate");
		//euphemium_legs = new ArmorEuphemium(MainRegistry.aMatEuph, 2).setUnlocalizedName("euphemium_legs").setMaxStackSize(1).setTextureName(RefStrings.MODID + ":euphemium_legs");
		//euphemium_boots = new ArmorEuphemium(MainRegistry.aMatEuph, 3).setUnlocalizedName("euphemium_boots").setMaxStackSize(1).setTextureName(RefStrings.MODID + ":euphemium_boots");

		ArmorMaterial aMatRags = EnumHelper.addArmorMaterial("HBM_RAGS", 150, new int[] { 1, 1, 1, 1 }, 0);
		aMatRags.customCraftingMaterial = ModItems.rag;

		goggles = new ArmorModel(ArmorMaterial.IRON, 0).setUnlocalizedName("goggles").setMaxStackSize(1).setTextureName(RefStrings.MODID + ":goggles");
		ashglasses = new ArmorAshGlasses(ArmorMaterial.IRON, 0).setUnlocalizedName("ashglasses").setMaxStackSize(1).setTextureName(RefStrings.MODID + ":ashglasses");
		gas_mask = new ArmorGasMask().setUnlocalizedName("gas_mask").setMaxStackSize(1).setTextureName(RefStrings.MODID + ":gas_mask");
		gas_mask_m65 = new ArmorGasMask().setUnlocalizedName("gas_mask_m65").setMaxStackSize(1).setTextureName(RefStrings.MODID + ":gas_mask_m65");
		gas_mask_mono = new ArmorGasMask().setUnlocalizedName("gas_mask_mono").setMaxStackSize(1).setTextureName(RefStrings.MODID + ":gas_mask_mono");
		gas_mask_olde = new ArmorGasMask().setUnlocalizedName("gas_mask_olde").setMaxStackSize(1).setTextureName(RefStrings.MODID + ":gas_mask_olde");
		mask_rag = new ModArmor(aMatRags, 0).setUnlocalizedName("mask_rag").setMaxStackSize(1).setTextureName(RefStrings.MODID + ":mask_rag");
		mask_piss = new ModArmor(aMatRags, 0).setUnlocalizedName("mask_piss").setMaxStackSize(1).setTextureName(RefStrings.MODID + ":mask_piss");
		hat = new ArmorHat(MainRegistry.aMatAlloy, 0).setUnlocalizedName("nossy_hat").setMaxStackSize(1).setTextureName(RefStrings.MODID + ":hat");
		no9 = new ArmorNo9(MainRegistry.aMatSteel, 0).setUnlocalizedName("no9").setMaxStackSize(1).setTextureName(RefStrings.MODID + ":no9");
		beta = new ItemDrop().setUnlocalizedName("beta").setMaxStackSize(1).setTextureName(RefStrings.MODID + ":beta");
		//oxy_mask = new ArmorModel(ArmorMaterial.IRON, 7, 0).setUnlocalizedName("oxy_mask").setMaxStackSize(1).setTextureName(RefStrings.MODID + ":oxy_mask");

		schrabidium_helmet = new ArmorFSB(MainRegistry.aMatSchrab, 0, RefStrings.MODID + ":textures/armor/schrabidium_1.png").setCap(4F).setMod(0.1F)
				//.addEffect(new PotionEffect(Potion.digSpeed.id, 20, 2))
				//.addEffect(new PotionEffect(Potion.damageBoost.id, 20, 2))
				//.addEffect(new PotionEffect(Potion.jump.id, 20, 1))
				//.addEffect(new PotionEffect(Potion.moveSpeed.id, 20, 2))
				.setFireproof(true).setUnlocalizedName("schrabidium_helmet").setTextureName(RefStrings.MODID + ":schrabidium_helmet");
		schrabidium_plate = new ArmorFSB(MainRegistry.aMatSchrab, 1, RefStrings.MODID + ":textures/armor/schrabidium_1.png").cloneStats((ArmorFSB) schrabidium_helmet).setUnlocalizedName("schrabidium_plate").setTextureName(RefStrings.MODID + ":schrabidium_plate");
		schrabidium_legs = new ArmorFSB(MainRegistry.aMatSchrab, 2, RefStrings.MODID + ":textures/armor/schrabidium_2.png").cloneStats((ArmorFSB) schrabidium_helmet).setCap(4F).setMod(0.1F).setUnlocalizedName("schrabidium_legs").setTextureName(RefStrings.MODID + ":schrabidium_legs");
		schrabidium_boots = new ArmorFSB(MainRegistry.aMatSchrab, 3, RefStrings.MODID + ":textures/armor/schrabidium_1.png").cloneStats((ArmorFSB) schrabidium_helmet).setCap(4F).setMod(0.1F).setUnlocalizedName("schrabidium_boots").setTextureName(RefStrings.MODID + ":schrabidium_boots");
		//bismuth_helmet = new ArmorBismuth(MainRegistry.aMatBismuth, 0, RefStrings.MODID + ":textures/armor/starmetal_1.png").setCap(8F).setMod(0.3F)
		//		//.addResistance("fall", 0)
		//		//.addEffect(new PotionEffect(Potion.jump.id, 20, 6))
		//		//.addEffect(new PotionEffect(Potion.moveSpeed.id, 20, 6))
		//		//.addEffect(new PotionEffect(Potion.regeneration.id, 20, 1))
		//		.addEffect(new PotionEffect(Potion.nightVision.id, 15 * 20, 0))
		//		.setDashCount(3)
		//		.setUnlocalizedName("bismuth_helmet").setTextureName(RefStrings.MODID + ":bismuth_helmet");
		//bismuth_plate = new ArmorBismuth(MainRegistry.aMatBismuth, 1, RefStrings.MODID + ":textures/armor/starmetal_2.png").cloneStats((ArmorFSB) bismuth_helmet).setCap(8F).setMod(0.3F).setUnlocalizedName("bismuth_plate").setTextureName(RefStrings.MODID + ":bismuth_plate");
		//bismuth_legs = new ArmorBismuth(MainRegistry.aMatBismuth, 2, RefStrings.MODID + ":textures/armor/starmetal_1.png").cloneStats((ArmorFSB) bismuth_helmet).setCap(8F).setMod(0.3F).setUnlocalizedName("bismuth_legs").setTextureName(RefStrings.MODID + ":bismuth_legs");
		//bismuth_boots = new ArmorBismuth(MainRegistry.aMatBismuth, 3, RefStrings.MODID + ":textures/armor/starmetal_1.png").cloneStats((ArmorFSB) bismuth_helmet).setCap(8F).setMod(0.3F).setUnlocalizedName("bismuth_boots").setTextureName(RefStrings.MODID + ":bismuth_boots");
		titanium_helmet = new ArmorFSB(MainRegistry.aMatTitan, 0, RefStrings.MODID + ":textures/armor/titanium_1.png").setUnlocalizedName("titanium_helmet").setMaxStackSize(1).setTextureName(RefStrings.MODID + ":titanium_helmet");
		titanium_plate = new ArmorFSB(MainRegistry.aMatTitan, 1, RefStrings.MODID + ":textures/armor/titanium_1.png").cloneStats((ArmorFSB) titanium_helmet).setUnlocalizedName("titanium_plate").setMaxStackSize(1).setTextureName(RefStrings.MODID + ":titanium_plate");
		titanium_legs = new ArmorFSB(MainRegistry.aMatTitan, 2, RefStrings.MODID + ":textures/armor/titanium_2.png").cloneStats((ArmorFSB) titanium_helmet).setUnlocalizedName("titanium_legs").setMaxStackSize(1).setTextureName(RefStrings.MODID + ":titanium_legs");
		titanium_boots = new ArmorFSB(MainRegistry.aMatTitan, 3, RefStrings.MODID + ":textures/armor/titanium_1.png").cloneStats((ArmorFSB) titanium_helmet).setUnlocalizedName("titanium_boots").setTextureName(RefStrings.MODID + ":titanium_boots");
		steel_helmet = new ArmorFSB(MainRegistry.aMatSteel, 0, RefStrings.MODID + ":textures/armor/steel_1.png").setUnlocalizedName("steel_helmet").setTextureName(RefStrings.MODID + ":steel_helmet");
		steel_plate = new ArmorFSB(MainRegistry.aMatSteel, 1, RefStrings.MODID + ":textures/armor/steel_1.png").cloneStats((ArmorFSB) steel_helmet).setUnlocalizedName("steel_plate").setTextureName(RefStrings.MODID + ":steel_plate");
		steel_legs = new ArmorFSB(MainRegistry.aMatSteel, 2, RefStrings.MODID + ":textures/armor/steel_2.png").cloneStats((ArmorFSB) steel_helmet).setUnlocalizedName("steel_legs").setTextureName(RefStrings.MODID + ":steel_legs");
		steel_boots = new ArmorFSB(MainRegistry.aMatSteel, 3, RefStrings.MODID + ":textures/armor/steel_1.png").cloneStats((ArmorFSB) steel_helmet).setUnlocalizedName("steel_boots").setTextureName(RefStrings.MODID + ":steel_boots");
		alloy_helmet = new ArmorFSB(MainRegistry.aMatAlloy, 0, RefStrings.MODID + ":textures/armor/alloy_1.png").setMod(0.75F).setUnlocalizedName("alloy_helmet").setTextureName(RefStrings.MODID + ":alloy_helmet");
		alloy_plate = new ArmorFSB(MainRegistry.aMatAlloy, 1, RefStrings.MODID + ":textures/armor/alloy_1.png").cloneStats((ArmorFSB) alloy_helmet).setUnlocalizedName("alloy_plate").setTextureName(RefStrings.MODID + ":alloy_plate");
		alloy_legs = new ArmorFSB(MainRegistry.aMatAlloy, 2, RefStrings.MODID + ":textures/armor/alloy_2.png").cloneStats((ArmorFSB) alloy_helmet).setUnlocalizedName("alloy_legs").setTextureName(RefStrings.MODID + ":alloy_legs");
		alloy_boots = new ArmorFSB(MainRegistry.aMatAlloy, 3, RefStrings.MODID + ":textures/armor/alloy_1.png").cloneStats((ArmorFSB) alloy_helmet).setUnlocalizedName("alloy_boots").setTextureName(RefStrings.MODID + ":alloy_boots");
		cmb_helmet = new ArmorFSB(MainRegistry.aMatCMB, 0, RefStrings.MODID + ":textures/armor/cmb_1.png");
				//.addEffect(new PotionEffect(Potion.moveSpeed.id, 20, 2))
				//.addEffect(new PotionEffect(Potion.digSpeed.id, 20, 2))
				//.addEffect(new PotionEffect(Potion.damageBoost.id, 20, 4))
				//.setFireproof(true).setUnlocalizedName("cmb_helmet").setTextureName(RefStrings.MODID + ":cmb_helmet");
		cmb_plate = new ArmorFSB(MainRegistry.aMatCMB, 1, RefStrings.MODID + ":textures/armor/cmb_1.png").cloneStats((ArmorFSB) cmb_helmet).setUnlocalizedName("cmb_plate").setTextureName(RefStrings.MODID + ":cmb_plate");
		cmb_legs = new ArmorFSB(MainRegistry.aMatCMB, 2, RefStrings.MODID + ":textures/armor/cmb_2.png").cloneStats((ArmorFSB) cmb_helmet).setUnlocalizedName("cmb_legs").setTextureName(RefStrings.MODID + ":cmb_legs");
		cmb_boots = new ArmorFSB(MainRegistry.aMatCMB, 3, RefStrings.MODID + ":textures/armor/cmb_1.png").cloneStats((ArmorFSB) cmb_helmet).setUnlocalizedName("cmb_boots").setTextureName(RefStrings.MODID + ":cmb_boots");
		paa_plate = new ArmorFSB(MainRegistry.aMatPaa, 1, RefStrings.MODID + ":textures/armor/paa_1.png").setNoHelmet(true)
				.addEffect(new PotionEffect(Potion.digSpeed.id, 20, 0)).setUnlocalizedName("paa_plate").setTextureName(RefStrings.MODID + ":paa_plate");
		paa_legs = new ArmorFSB(MainRegistry.aMatPaa, 2, RefStrings.MODID + ":textures/armor/paa_2.png").cloneStats((ArmorFSB) paa_plate).setUnlocalizedName("paa_legs").setTextureName(RefStrings.MODID + ":paa_legs");
		paa_boots = new ArmorFSB(MainRegistry.aMatPaa, 3, RefStrings.MODID + ":textures/armor/paa_1.png").cloneStats((ArmorFSB) paa_plate).setUnlocalizedName("paa_boots").setTextureName(RefStrings.MODID + ":paa_boots");
		asbestos_helmet = new ArmorFSB(MainRegistry.aMatAsbestos, 0, RefStrings.MODID + ":textures/armor/asbestos_1.png").setFireproof(true).setOverlay(RefStrings.MODID + ":textures/misc/overlay_asbestos.png").setUnlocalizedName("asbestos_helmet").setTextureName(RefStrings.MODID + ":asbestos_helmet");
		asbestos_plate = new ArmorFSB(MainRegistry.aMatAsbestos, 1, RefStrings.MODID + ":textures/armor/asbestos_1.png").setFireproof(true).setUnlocalizedName("asbestos_plate").setTextureName(RefStrings.MODID + ":asbestos_plate");
		asbestos_legs = new ArmorFSB(MainRegistry.aMatAsbestos, 2, RefStrings.MODID + ":textures/armor/asbestos_2.png").setFireproof(true).setUnlocalizedName("asbestos_legs").setTextureName(RefStrings.MODID + ":asbestos_legs");
		asbestos_boots = new ArmorFSB(MainRegistry.aMatAsbestos, 3, RefStrings.MODID + ":textures/armor/asbestos_1.png").setFireproof(true).setUnlocalizedName("asbestos_boots").setTextureName(RefStrings.MODID + ":asbestos_boots");
		security_helmet = new ArmorFSB(MainRegistry.aMatSecurity, 0, RefStrings.MODID + ":textures/armor/security_1.png").setProjectileProtection(0.8F).setUnlocalizedName("security_helmet").setTextureName(RefStrings.MODID + ":security_helmet");
		security_plate = new ArmorFSB(MainRegistry.aMatSecurity, 1, RefStrings.MODID + ":textures/armor/security_1.png").cloneStats((ArmorFSB) security_helmet).setUnlocalizedName("security_plate").setTextureName(RefStrings.MODID + ":security_plate");
		security_legs = new ArmorFSB(MainRegistry.aMatSecurity, 2, RefStrings.MODID + ":textures/armor/security_2.png").cloneStats((ArmorFSB) security_helmet).setUnlocalizedName("security_legs").setTextureName(RefStrings.MODID + ":security_legs");
		security_boots = new ArmorFSB(MainRegistry.aMatSecurity, 3, RefStrings.MODID + ":textures/armor/security_1.png").cloneStats((ArmorFSB) security_helmet).setUnlocalizedName("security_boots").setTextureName(RefStrings.MODID + ":security_boots");
		cobalt_helmet = new ArmorFSB(MainRegistry.aMatCobalt, 0, RefStrings.MODID + ":textures/armor/cobalt_1.png").setUnlocalizedName("cobalt_helmet").setTextureName(RefStrings.MODID + ":cobalt_helmet");
		cobalt_plate = new ArmorFSB(MainRegistry.aMatCobalt, 1, RefStrings.MODID + ":textures/armor/cobalt_1.png").cloneStats((ArmorFSB) cobalt_helmet).setUnlocalizedName("cobalt_plate").setTextureName(RefStrings.MODID + ":cobalt_plate");
		cobalt_legs = new ArmorFSB(MainRegistry.aMatCobalt, 2, RefStrings.MODID + ":textures/armor/cobalt_2.png").cloneStats((ArmorFSB) cobalt_helmet).setUnlocalizedName("cobalt_legs").setTextureName(RefStrings.MODID + ":cobalt_legs");
		cobalt_boots = new ArmorFSB(MainRegistry.aMatCobalt, 3, RefStrings.MODID + ":textures/armor/cobalt_1.png").cloneStats((ArmorFSB) cobalt_helmet).setUnlocalizedName("cobalt_boots").setTextureName(RefStrings.MODID + ":cobalt_boots");
		//starmetal_helmet = new ArmorFSB(MainRegistry.aMatStarmetal, 0, RefStrings.MODID + ":textures/armor/starmetal_1.png").setMod(0.5F).setCap(15F);
				//.setFireproof(true).setUnlocalizedName("starmetal_helmet").setTextureName(RefStrings.MODID + ":starmetal_helmet");
		//starmetal_plate = new ArmorFSB(MainRegistry.aMatStarmetal, 1, RefStrings.MODID + ":textures/armor/starmetal_1.png").cloneStats((ArmorFSB) starmetal_helmet).setUnlocalizedName("starmetal_plate").setTextureName(RefStrings.MODID + ":starmetal_plate");
		//starmetal_legs = new ArmorFSB(MainRegistry.aMatStarmetal, 2, RefStrings.MODID + ":textures/armor/starmetal_2.png").cloneStats((ArmorFSB) starmetal_helmet).setUnlocalizedName("starmetal_legs").setTextureName(RefStrings.MODID + ":starmetal_legs");
		//starmetal_boots = new ArmorFSB(MainRegistry.aMatStarmetal, 3, RefStrings.MODID + ":textures/armor/starmetal_1.png").cloneStats((ArmorFSB) starmetal_helmet).setUnlocalizedName("starmetal_boots").setTextureName(RefStrings.MODID + ":starmetal_boots");

		robes_helmet = new ArmorFSB(ArmorMaterial.CHAIN, 0, RefStrings.MODID + ":textures/armor/robes_1.png").setThreshold(1.0F).setUnlocalizedName("robes_helmet").setTextureName(RefStrings.MODID + ":robes_helmet");
		robes_plate = new ArmorFSB(ArmorMaterial.CHAIN, 1, RefStrings.MODID + ":textures/armor/robes_1.png").cloneStats((ArmorFSB) robes_helmet).setUnlocalizedName("robes_plate").setTextureName(RefStrings.MODID + ":robes_plate");
		robes_legs = new ArmorFSB(ArmorMaterial.CHAIN, 2, RefStrings.MODID + ":textures/armor/robes_2.png").cloneStats((ArmorFSB) robes_helmet).setUnlocalizedName("robes_legs").setTextureName(RefStrings.MODID + ":robes_legs");
		robes_boots = new ArmorFSB(ArmorMaterial.CHAIN, 3, RefStrings.MODID + ":textures/armor/robes_1.png").cloneStats((ArmorFSB) robes_helmet).setUnlocalizedName("robes_boots").setTextureName(RefStrings.MODID + ":robes_boots");

		initializeItem2();
	}

	public static void initializeItem2() {

		ArmorMaterial aMatZirconium = EnumHelper.addArmorMaterial("HBM_ZIRCONIUM", 1000, new int[] { 2, 5, 3, 1 }, 100);
		aMatZirconium.customCraftingMaterial = ModItems.ingot_zirconium;
		zirconium_legs = new ArmorFSB(aMatZirconium, 2, RefStrings.MODID + ":textures/armor/zirconium_2.png").setMod(0.0F).setUnlocalizedName("zirconium_legs").setTextureName(RefStrings.MODID + ":zirconium_legs");

		ArmorMaterial aMatDNT = EnumHelper.addArmorMaterial("HBM_DNT_LOLOLOL", 3, new int[] { 1, 1, 1, 1 }, 0);
		aMatDNT.customCraftingMaterial = ModItems.ingot_dineutronium;
		//dnt_helmet = new ArmorFSB(aMatDNT, 0, RefStrings.MODID + ":textures/armor/dnt_1.png")
		//		.setMod(5F).setUnlocalizedName("dnt_helmet").setTextureName(RefStrings.MODID + ":dnt_helmet");
		//dnt_plate = new ArmorFSB(aMatDNT, 1, RefStrings.MODID + ":textures/armor/dnt_1.png").cloneStats((ArmorFSB) dnt_helmet).setUnlocalizedName("dnt_plate").setTextureName(RefStrings.MODID + ":dnt_plate");
		//dnt_legs = new ArmorFSB(aMatDNT, 2, RefStrings.MODID + ":textures/armor/dnt_2.png").cloneStats((ArmorFSB) dnt_helmet).setUnlocalizedName("dnt_legs").setTextureName(RefStrings.MODID + ":dnt_legs");
		//dnt_boots = new ArmorFSB(aMatDNT, 3, RefStrings.MODID + ":textures/armor/dnt_1.png").cloneStats((ArmorFSB) dnt_helmet).setUnlocalizedName("dnt_boots").setTextureName(RefStrings.MODID + ":dnt_boots");

		ArmorMaterial aMatT45 = EnumHelper.addArmorMaterial("HBM_T45", 150, new int[] { 2, 2, 2, 2 }, 0);
		aMatT45.customCraftingMaterial = ModItems.plate_armor_titanium;
		t45_helmet = new ArmorT45(aMatT45, 0, 1000000, 10000, 1000, 5)
				//.setFireproof(true)
				.enableVATS(true)
				.setHasGeigerSound(true)
				.setHasHardLanding(true)
				//.addEffect(new PotionEffect(Potion.damageBoost.id, 20, 0))
				//.setBlastProtection(0.5F)
				//.addResistance("monoxide", 0F)
				//.addResistance("fall", 0)
				.setSealed(true)
				.hides(EnumPlayerPart.HAT)
				.setUnlocalizedName("t45_helmet").setMaxStackSize(1).setTextureName(RefStrings.MODID + ":t45_helmet");
		t45_plate = new ArmorT45(aMatT45, 1, 1000000, 10000, 1000, 5).cloneStats((ArmorFSB) t45_helmet).setUnlocalizedName("t45_plate").setMaxStackSize(1).setTextureName(RefStrings.MODID + ":t45_plate");
		t45_legs = new ArmorT45(aMatT45, 2, 1000000, 10000, 1000, 5).cloneStats((ArmorFSB) t45_helmet).setUnlocalizedName("t45_legs").setMaxStackSize(1).setTextureName(RefStrings.MODID + ":t45_legs");
		t45_boots = new ArmorT45(aMatT45, 3, 1000000, 10000, 1000, 5).cloneStats((ArmorFSB) t45_helmet).setUnlocalizedName("t45_boots").setMaxStackSize(1).setTextureName(RefStrings.MODID + ":t45_boots");

		ArmorMaterial aMatDesh = EnumHelper.addArmorMaterial("HBM_DESH", 150, new int[] { 1, 1, 1, 1 }, 0);
		aMatDesh.customCraftingMaterial = ModItems.ingot_desh;
		steamsuit_helmet = new ArmorDesh(aMatDesh, 0, RefStrings.MODID + ":textures/armor/starmetal_1.png", Fluids.STEAM, 64_000, 500, 50, 1)

				.setHasHardLanding(true)
				//.addEffect(new PotionEffect(Potion.digSpeed.id, 20, 4))


				.setSealed(true)
				.hides(EnumPlayerPart.HAT)
				.setUnlocalizedName("steamsuit_helmet").setMaxStackSize(1).setTextureName(RefStrings.MODID + ":steamsuit_helmet");
		steamsuit_plate = new ArmorDesh(aMatDesh, 1, RefStrings.MODID + ":textures/armor/starmetal_1.png", Fluids.STEAM, 64_000, 500, 50, 1).cloneStats((ArmorFSB) steamsuit_helmet).setUnlocalizedName("steamsuit_plate").setMaxStackSize(1).setTextureName(RefStrings.MODID + ":steamsuit_plate");
		steamsuit_legs = new ArmorDesh(aMatDesh, 2, RefStrings.MODID + ":textures/armor/starmetal_2.png", Fluids.STEAM, 64_000, 500, 50, 1).cloneStats((ArmorFSB) steamsuit_helmet).setUnlocalizedName("steamsuit_legs").setMaxStackSize(1).setTextureName(RefStrings.MODID + ":steamsuit_legs");
		steamsuit_boots = new ArmorDesh(aMatDesh, 3, RefStrings.MODID + ":textures/armor/starmetal_1.png", Fluids.STEAM, 64_000, 500, 50, 1).cloneStats((ArmorFSB) steamsuit_helmet).setUnlocalizedName("steamsuit_boots").setMaxStackSize(1).setTextureName(RefStrings.MODID + ":steamsuit_boots");

		ArmorMaterial aMatDiesel = EnumHelper.addArmorMaterial("HBM_BNUUY", 150, new int[] { 1, 1, 1, 1 }, 0);
		aMatDiesel.customCraftingMaterial = ModItems.plate_copper;
		dieselsuit_helmet = new ArmorDiesel(aMatDiesel, 0, RefStrings.MODID + ":textures/armor/starmetal_1.png", Fluids.DIESEL, 64_000, 500, 50, 1)
				//.addEffect(new PotionEffect(Potion.moveSpeed.id, 20, 2))
				//.addEffect(new PotionEffect(Potion.jump.id, 20, 2))
				.enableThermalSight(true)
				.enableVATS(true)
				.addResistance("fall", 0)
				.setUnlocalizedName("dieselsuit_helmet").setMaxStackSize(1).setTextureName(RefStrings.MODID + ":dieselsuit_helmet");
		dieselsuit_plate = new ArmorDiesel(aMatDiesel, 1, RefStrings.MODID + ":textures/armor/starmetal_1.png", Fluids.DIESEL, 64_000, 500, 50, 1).cloneStats((ArmorFSB) dieselsuit_helmet).setUnlocalizedName("dieselsuit_plate").setMaxStackSize(1).setTextureName(RefStrings.MODID + ":dieselsuit_plate");
		dieselsuit_legs = new ArmorDiesel(aMatDiesel, 2, RefStrings.MODID + ":textures/armor/starmetal_2.png", Fluids.DIESEL, 64_000, 500, 50, 1).cloneStats((ArmorFSB) dieselsuit_helmet).setUnlocalizedName("dieselsuit_legs").setMaxStackSize(1).setTextureName(RefStrings.MODID + ":dieselsuit_legs");
		dieselsuit_boots = new ArmorDiesel(aMatDiesel, 3, RefStrings.MODID + ":textures/armor/starmetal_1.png", Fluids.DIESEL, 64_000, 500, 50, 1).cloneStats((ArmorFSB) dieselsuit_helmet).setUnlocalizedName("dieselsuit_boots").setMaxStackSize(1).setTextureName(RefStrings.MODID + ":dieselsuit_boots");

		ArmorMaterial aMatAJR = EnumHelper.addArmorMaterial("HBM_T45AJR", 150, new int[] { 2, 2, 2, 2 }, 100);
		aMatAJR.customCraftingMaterial = ModItems.plate_armor_ajr;
		ajr_helmet = new ArmorAJR(aMatAJR, 0, RefStrings.MODID + ":textures/armor/starmetal_1.png", 2500000, 10000, 2000, 25)

				.enableVATS(true)
				.setHasGeigerSound(true)
				.setHasHardLanding(true)
				//.addEffect(new PotionEffect(Potion.jump.id, 20, 0))
				//.addEffect(new PotionEffect(Potion.damageBoost.id, 20, 0))

				.setSealed(true)
				.hides(EnumPlayerPart.HAT)
				.setUnlocalizedName("ajr_helmet").setTextureName(RefStrings.MODID + ":ajr_helmet");
		ajr_plate = new ArmorAJR(aMatAJR, 1, RefStrings.MODID + ":textures/armor/starmetal_1.png", 2500000, 10000, 2000, 25).cloneStats((ArmorFSB) ajr_helmet).setUnlocalizedName("ajr_plate").setTextureName(RefStrings.MODID + ":ajr_plate");
		ajr_legs = new ArmorAJR(aMatAJR, 2, RefStrings.MODID + ":textures/armor/starmetal_2.png", 2500000, 10000, 2000, 25).cloneStats((ArmorFSB) ajr_helmet).setUnlocalizedName("ajr_legs").setTextureName(RefStrings.MODID + ":ajr_legs");
		ajr_boots = new ArmorAJR(aMatAJR, 3, RefStrings.MODID + ":textures/armor/starmetal_1.png", 2500000, 10000, 2000, 25).cloneStats((ArmorFSB) ajr_helmet).setUnlocalizedName("ajr_boots").setTextureName(RefStrings.MODID + ":ajr_boots");

		ajro_helmet = new ArmorAJRO(aMatAJR, 0, RefStrings.MODID + ":textures/armor/starmetal_1.png", 2500000, 10000, 2000, 25)

				.enableVATS(true)
				.setHasGeigerSound(true)
				.setHasHardLanding(true)
				//.addEffect(new PotionEffect(Potion.jump.id, 20, 0))
				//.addEffect(new PotionEffect(Potion.damageBoost.id, 20, 0))


				.setSealed(true)
				.hides(EnumPlayerPart.HAT)
				.setUnlocalizedName("ajro_helmet").setTextureName(RefStrings.MODID + ":ajro_helmet");
		ajro_plate = new ArmorAJRO(aMatAJR, 1, RefStrings.MODID + ":textures/armor/starmetal_1.png", 2500000, 10000, 2000, 25).cloneStats((ArmorFSB) ajro_helmet).setUnlocalizedName("ajro_plate").setTextureName(RefStrings.MODID + ":ajro_plate");
		ajro_legs = new ArmorAJRO(aMatAJR, 2, RefStrings.MODID + ":textures/armor/starmetal_2.png", 2500000, 10000, 2000, 25).cloneStats((ArmorFSB) ajro_helmet).setUnlocalizedName("ajro_legs").setTextureName(RefStrings.MODID + ":ajro_legs");
		ajro_boots = new ArmorAJRO(aMatAJR, 3, RefStrings.MODID + ":textures/armor/starmetal_1.png", 2500000, 10000, 2000, 25).cloneStats((ArmorFSB) ajro_helmet).setUnlocalizedName("ajro_boots").setTextureName(RefStrings.MODID + ":ajro_boots");

		rpa_helmet = new ArmorRPA(aMatAJR, 0, RefStrings.MODID + ":textures/armor/starmetal_1.png", 2500000, 10000, 2000, 25)

				.enableVATS(true)
				.setHasGeigerSound(true)
				.setHasHardLanding(true)
				//.addEffect(new PotionEffect(Potion.damageBoost.id, 20, 3))

				.setStep("hbm:step.powered")
				.setJump("hbm:step.powered")
				.setFall("hbm:step.powered")

				.setSealed(true)
				.hides(EnumPlayerPart.HAT)
				.setUnlocalizedName("rpa_helmet").setTextureName(RefStrings.MODID + ":rpa_helmet");
		rpa_plate = new ArmorRPA(aMatAJR, 1, RefStrings.MODID + ":textures/armor/starmetal_1.png", 2500000, 10000, 2000, 25).cloneStats((ArmorFSB) rpa_helmet).setUnlocalizedName("rpa_plate").setTextureName(RefStrings.MODID + ":rpa_plate");
		rpa_legs = new ArmorRPA(aMatAJR, 2, RefStrings.MODID + ":textures/armor/starmetal_2.png", 2500000, 10000, 2000, 25).cloneStats((ArmorFSB) rpa_helmet).setUnlocalizedName("rpa_legs").setTextureName(RefStrings.MODID + ":rpa_legs");
		rpa_boots = new ArmorRPA(aMatAJR, 3, RefStrings.MODID + ":textures/armor/starmetal_1.png", 2500000, 10000, 2000, 25).cloneStats((ArmorFSB) rpa_helmet).setUnlocalizedName("rpa_boots").setTextureName(RefStrings.MODID + ":rpa_boots");

		ArmorMaterial aMatBJ = EnumHelper.addArmorMaterial("HBM_BLACKJACK", 150, new int[] { 1, 3, 3, 2 }, 100);
		aMatBJ.customCraftingMaterial = ModItems.plate_armor_lunar;
		bj_helmet = new ArmorBJ(aMatBJ, 0, RefStrings.MODID + ":textures/armor/starmetal_1.png", 10000000, 10000, 1000, 100)

				.enableVATS(true)
				.enableThermalSight(true)
				.setHasGeigerSound(true)
				.setHasHardLanding(true)
				//.addEffect(new PotionEffect(Potion.moveSpeed.id, 20, 1))
				//.addEffect(new PotionEffect(Potion.jump.id, 20, 0))
				.addEffect(new PotionEffect(Potion.field_76443_y.id, 20, 0))
				.addEffect(new PotionEffect(HbmPotion.radx.id, 20, 0))

				.setStep("hbm:step.metal")
				.setJump("hbm:step.iron_jump")

				.setFall("hbm:step.iron_land").setUnlocalizedName("bj_helmet").setTextureName(RefStrings.MODID + ":bj_helmet");
		bj_plate = new ArmorBJ(aMatBJ, 1, RefStrings.MODID + ":textures/armor/starmetal_1.png", 10000000, 10000, 1000, 100).cloneStats((ArmorFSB) bj_helmet).setUnlocalizedName("bj_plate").setTextureName(RefStrings.MODID + ":bj_plate");
		bj_plate_jetpack = new ArmorBJJetpack(aMatBJ, 1, RefStrings.MODID + ":textures/armor/starmetal_1.png", 10000000, 10000, 1000, 100).cloneStats((ArmorFSB) bj_helmet).setUnlocalizedName("bj_plate_jetpack").setTextureName(RefStrings.MODID + ":bj_plate_jetpack");
		bj_legs = new ArmorBJ(aMatBJ, 2, RefStrings.MODID + ":textures/armor/starmetal_2.png", 10000000, 10000, 1000, 100).cloneStats((ArmorFSB) bj_helmet).setUnlocalizedName("bj_legs").setTextureName(RefStrings.MODID + ":bj_legs");
		bj_boots = new ArmorBJ(aMatBJ, 3, RefStrings.MODID + ":textures/armor/starmetal_1.png", 10000000, 10000, 1000, 100).cloneStats((ArmorFSB) bj_helmet).setUnlocalizedName("bj_boots").setTextureName(RefStrings.MODID + ":bj_boots");

		ArmorMaterial aMatEnv = EnumHelper.addArmorMaterial("HBM_ENV", 150, new int[] { 2, 2, 2, 2 }, 100);
		aMatEnv.customCraftingMaterial = ModItems.plate_armor_hev;
		envsuit_helmet = new ArmorEnvsuit(aMatEnv, 0, RefStrings.MODID + ":textures/armor/starmetal_1.png", 100_000, 1_000, 250, 0)
				//.addEffect(new PotionEffect(Potion.moveSpeed.id, 20, 1))
				//.addEffect(new PotionEffect(Potion.jump.id, 20, 0))

				.setSealed(true)
				.hides(EnumPlayerPart.HAT)
				.setUnlocalizedName("envsuit_helmet").setTextureName(RefStrings.MODID + ":envsuit_helmet");
		envsuit_plate = new ArmorEnvsuit(aMatEnv, 1, RefStrings.MODID + ":textures/armor/starmetal_1.png", 100_000, 1_000, 250, 0).cloneStats((ArmorFSB) envsuit_helmet).setUnlocalizedName("envsuit_plate").setTextureName(RefStrings.MODID + ":envsuit_plate");
		envsuit_legs = new ArmorEnvsuit(aMatEnv, 2, RefStrings.MODID + ":textures/armor/starmetal_2.png", 100_000, 1_000, 250, 0).cloneStats((ArmorFSB) envsuit_helmet).setUnlocalizedName("envsuit_legs").setTextureName(RefStrings.MODID + ":envsuit_legs");
		envsuit_boots = new ArmorEnvsuit(aMatEnv, 3, RefStrings.MODID + ":textures/armor/starmetal_1.png", 100_000, 1_000, 250, 0).cloneStats((ArmorFSB) envsuit_helmet).setUnlocalizedName("envsuit_boots").setTextureName(RefStrings.MODID + ":envsuit_boots");

		ArmorMaterial aMatHEV = EnumHelper.addArmorMaterial("HBM_HEV", 150, new int[] { 1, 3, 3, 2 }, 100);
		aMatHEV.customCraftingMaterial = ModItems.plate_armor_hev;
		hev_helmet = new ArmorHEV(aMatHEV, 0, RefStrings.MODID + ":textures/armor/starmetal_1.png", 1000000, 10000, 2500, 0)
				//.addEffect(new PotionEffect(Potion.moveSpeed.id, 20, 1))
				//.addEffect(new PotionEffect(Potion.jump.id, 20, 0))

				.setHasGeigerSound(true)
				.setHasCustomGeiger(true)

				.setSealed(true)
				.hides(EnumPlayerPart.HAT)
				.setUnlocalizedName("hev_helmet").setTextureName(RefStrings.MODID + ":hev_helmet");
		hev_plate = new ArmorHEV(aMatHEV, 1, RefStrings.MODID + ":textures/armor/starmetal_1.png", 1000000, 10000, 2500, 0).cloneStats((ArmorFSB) hev_helmet).setUnlocalizedName("hev_plate").setTextureName(RefStrings.MODID + ":hev_plate");
		hev_legs = new ArmorHEV(aMatHEV, 2, RefStrings.MODID + ":textures/armor/starmetal_2.png", 1000000, 10000, 2500, 0).cloneStats((ArmorFSB) hev_helmet).setUnlocalizedName("hev_legs").setTextureName(RefStrings.MODID + ":hev_legs");
		hev_boots = new ArmorHEV(aMatHEV, 3, RefStrings.MODID + ":textures/armor/starmetal_1.png", 1000000, 10000, 2500, 0).cloneStats((ArmorFSB) hev_helmet).setUnlocalizedName("hev_boots").setTextureName(RefStrings.MODID + ":hev_boots");

		jackt = new ModArmor(MainRegistry.aMatSteel, 1).setUnlocalizedName("jackt").setTextureName(RefStrings.MODID + ":jackt");
		jackt2 = new ModArmor(MainRegistry.aMatSteel, 1).setUnlocalizedName("jackt2").setTextureName(RefStrings.MODID + ":jackt2");

		ArmorMaterial aMatFau = EnumHelper.addArmorMaterial("HBM_DIGAMMA", 150, new int[] { 3, 4, 4, 3 }, 100); //I assume the numbers here are armor protection bar amounts.
		aMatFau.customCraftingMaterial = ModItems.plate_armor_fau;
		fau_helmet = new ArmorDigamma(aMatFau, 0, RefStrings.MODID + ":textures/armor/starmetal_1.png", 10000000, 10000, 2500, 0)
				.addEffect(new PotionEffect(Potion.jump.id, 20, 1))

				.setHasGeigerSound(true)
				.enableThermalSight(true)
				.setHasHardLanding(true)
				.setStep("hbm:step.metal")
				.setJump("hbm:step.iron_jump")
				.setFall("hbm:step.iron_land")


				.setSealed(true)
				.hides(EnumPlayerPart.HAT)
				.setUnlocalizedName("fau_helmet").setTextureName(RefStrings.MODID + ":fau_helmet");
		fau_plate = new ArmorDigamma(aMatFau, 1, RefStrings.MODID + ":textures/armor/starmetal_1.png", 10000000, 10000, 2500, 0).cloneStats((ArmorFSB) fau_helmet).setFullSetForHide().setUnlocalizedName("fau_plate").setTextureName(RefStrings.MODID + ":fau_plate");
		fau_legs = new ArmorDigamma(aMatFau, 2, RefStrings.MODID + ":textures/armor/starmetal_2.png", 10000000, 10000, 2500, 0).cloneStats((ArmorFSB) fau_helmet).hides(EnumPlayerPart.LEFT_LEG, EnumPlayerPart.RIGHT_LEG).setFullSetForHide().setUnlocalizedName("fau_legs").setTextureName(RefStrings.MODID + ":fau_legs");
		fau_boots = new ArmorDigamma(aMatFau, 3, RefStrings.MODID + ":textures/armor/starmetal_1.png", 10000000, 10000, 2500, 0).cloneStats((ArmorFSB) fau_helmet).setUnlocalizedName("fau_boots").setTextureName(RefStrings.MODID + ":fau_boots");

		//ArmorMaterial aMatDNS = EnumHelper.addArmorMaterial("HBM_DNT_NANO", 150, new int[] { 3, 8, 6, 3 }, 100);
		//aMatDNS.customCraftingMaterial = ModItems.plate_armor_dnt;
		//dns_helmet = new ArmorDNT(aMatDNS, 0, RefStrings.MODID + ":textures/armor/starmetal_1.png", 1000000000, 1000000, 100000, 115)
		//		.addEffect(new PotionEffect(Potion.damageBoost.id, 20, 9))
		//		.addEffect(new PotionEffect(Potion.digSpeed.id, 20, 7))
		//		.addEffect(new PotionEffect(Potion.jump.id, 20, 2))
		//		.setHasGeigerSound(true)
		//		.enableVATS(true)
		//		.enableThermalSight(true)
		//		.setHasHardLanding(true)
		//		.setStep("hbm:step.metal")
		//		.setJump("hbm:step.iron_jump")
		//		.setFall("hbm:step.iron_land")
		//		.setFireproof(true)
		//		.setSealed(true)
		//		.hides(EnumPlayerPart.HAT)
		//		.setUnlocalizedName("dns_helmet").setTextureName(RefStrings.MODID + ":dns_helmet");
		//dns_plate = new ArmorDNT(aMatDNS, 1, RefStrings.MODID + ":textures/armor/starmetal_1.png", 1000000000, 1000000, 100000, 115).cloneStats((ArmorFSB) dns_helmet).setUnlocalizedName("dns_plate").setTextureName(RefStrings.MODID + ":dns_plate");
		//dns_legs = new ArmorDNT(aMatDNS, 2, RefStrings.MODID + ":textures/armor/starmetal_2.png", 1000000000, 1000000, 100000, 115).cloneStats((ArmorFSB) dns_helmet).setUnlocalizedName("dns_legs").setTextureName(RefStrings.MODID + ":dns_legs");
		//dns_boots = new ArmorDNT(aMatDNS, 3, RefStrings.MODID + ":textures/armor/starmetal_1.png", 1000000000, 1000000, 100000, 115).cloneStats((ArmorFSB) dns_helmet).setUnlocalizedName("dns_boots").setTextureName(RefStrings.MODID + ":dns_boots");

		ArmorMaterial aMatTrench = EnumHelper.addArmorMaterial("HBM_TRENCH", 150, new int[] { 3, 3, 2, 2 }, 100);
		aMatTrench.customCraftingMaterial = ModItems.plate_iron;
		trenchmaster_helmet = new ArmorTrenchmaster(aMatTrench, 0, RefStrings.MODID + ":textures/armor/starmetal_1.png")
				//.addEffect(new PotionEffect(Potion.damageBoost.id, 20, 2))
				//.addEffect(new PotionEffect(Potion.digSpeed.id, 20, 1))
				//.addEffect(new PotionEffect(Potion.jump.id, 20, 1))
				//.addEffect(new PotionEffect(Potion.moveSpeed.id, 20, 0))
				.enableVATS(true)
				//.addResistance("fall", 0F)
				//.setFireproof(true)
				.setStepSize(1)
				.setSealed(true)
				.hides(EnumPlayerPart.HAT)
				.setUnlocalizedName("trenchmaster_helmet").setTextureName(RefStrings.MODID + ":trenchmaster_helmet");
		trenchmaster_plate = new ArmorTrenchmaster(aMatTrench, 1, RefStrings.MODID + ":textures/armor/starmetal_1.png").cloneStats((ArmorFSB) trenchmaster_helmet).setUnlocalizedName("trenchmaster_plate").setTextureName(RefStrings.MODID + ":trenchmaster_plate");
		trenchmaster_legs = new ArmorTrenchmaster(aMatTrench, 2, RefStrings.MODID + ":textures/armor/starmetal_2.png").cloneStats((ArmorFSB) trenchmaster_helmet).setUnlocalizedName("trenchmaster_legs").setTextureName(RefStrings.MODID + ":trenchmaster_legs");
		trenchmaster_boots = new ArmorTrenchmaster(aMatTrench, 3, RefStrings.MODID + ":textures/armor/starmetal_1.png").cloneStats((ArmorFSB) trenchmaster_helmet).setUnlocalizedName("trenchmaster_boots").setTextureName(RefStrings.MODID + ":trenchmaster_boots");

		jackt = new ModArmor(MainRegistry.aMatSteel, 1).setUnlocalizedName("jackt").setTextureName(RefStrings.MODID + ":jackt");
		jackt2 = new ModArmor(MainRegistry.aMatSteel, 1).setUnlocalizedName("jackt2").setTextureName(RefStrings.MODID + ":jackt2");

		//actual chainsaw
		chainsaw = new ItemChainsaw(7, -0.05, MainRegistry.tMatChainsaw, EnumToolType.AXE, 5000, 1, 250,
				//Fluids.DIESEL, Fluids.DIESEL_CRACK, Fluids.KEROSENE, don't make much sense to me
				Fluids.BIOFUEL, Fluids.GASOLINE, Fluids.GASOLINE_LEADED, Fluids.PETROIL, Fluids.PETROIL_LEADED, Fluids.COALGAS, Fluids.COALGAS_LEADED)
				.addBreakAbility(new ToolAbility.SilkAbility())
				.addBreakAbility(new ToolAbility.FellingAbility(4, 32, 192))
				.addBreakAbility(new ToolAbility.RecursionAbility(15))
				.addHitAbility(new WeaponAbility.ChainsawAbility(10))
				.addHitAbility(new WeaponAbility.BeheaderAbility())
				.setShears()
				.setUnlocalizedName("chainsaw").setTextureName(RefStrings.MODID + ":chainsaw");

		//bullshit
		schrabidium_pickaxe = new ItemToolAbility(4, 0, MainRegistry.tMatSchrab, EnumToolType.PICKAXE)
				.addHitAbility(new WeaponAbility.RadiationAbility(15F))
				.addBreakAbility(new ToolAbility.HammerAbility(2))
				.addBreakAbility(new ToolAbility.HammerSilkAbility(2))
				.addBreakAbility(new ToolAbility.RecursionAbility(10))
				.addBreakAbility(new ToolAbility.SilkAbility())
				.addBreakAbility(new LuckAbility(5))
				.addBreakAbility(new ToolAbility.SmelterAbility())
				.addBreakAbility(new ToolAbility.ShredderAbility())
				.setRarity(EnumRarity.rare).setUnlocalizedName("schrabidium_pickaxe").setTextureName(RefStrings.MODID + ":schrabidium_pickaxe");

		schrabidium_axe = new ItemToolAbility(5, 0, MainRegistry.tMatSchrab, EnumToolType.AXE)
				.addHitAbility(new WeaponAbility.RadiationAbility(15F))
				.addBreakAbility(new ToolAbility.HammerAbility(2))
				.addBreakAbility(new ToolAbility.RecursionAbility(10))
				.addBreakAbility(new ToolAbility.SilkAbility())
				.addBreakAbility(new LuckAbility(5))
				.addBreakAbility(new ToolAbility.SmelterAbility())
				.addBreakAbility(new ToolAbility.ShredderAbility())
				.addHitAbility(new WeaponAbility.BeheaderAbility())
				.setRarity(EnumRarity.rare).setUnlocalizedName("schrabidium_axe").setTextureName(RefStrings.MODID + ":schrabidium_axe");

		schrabidium_shovel = new ItemToolAbility(3, 0, MainRegistry.tMatSchrab, EnumToolType.SHOVEL)
				.addHitAbility(new WeaponAbility.RadiationAbility(15F))
				.addBreakAbility(new ToolAbility.HammerAbility(2))
				.addBreakAbility(new ToolAbility.RecursionAbility(10))
				.addBreakAbility(new ToolAbility.SilkAbility())
				.addBreakAbility(new LuckAbility(5))
				.addBreakAbility(new ToolAbility.SmelterAbility())
				.addBreakAbility(new ToolAbility.ShredderAbility())
				.setRarity(EnumRarity.rare).setUnlocalizedName("schrabidium_shovel").setTextureName(RefStrings.MODID + ":schrabidium_shovel");

		schrabidium_hoe = new HoeSchrabidium(MainRegistry.tMatSchrab).setUnlocalizedName("schrabidium_hoe").setTextureName(RefStrings.MODID + ":schrabidium_hoe");

		//titanium needs
		//LightweightAbility
		//CorrosionResistantAbility
		//ChemicalToolAbility or some stand in for those abilities
		titanium_pickaxe = new ItemToolAbility(4.5F, 0, MainRegistry.tMatTitan, EnumToolType.PICKAXE)
			.addToolTrait(ToolAbility.ToolTrait.LIGHTWEIGHT)
			.addToolTrait(ToolAbility.ToolTrait.CORROSION_RESISTANT)
			//if you are going to suggest adding these at least fucking implement them when you do a refactor of ToolAbility.java.
			.setUnlocalizedName("titanium_pickaxe").setTextureName(RefStrings.MODID + ":titanium_pickaxe");
		titanium_axe = new ItemToolAbility(5.5F, 0, MainRegistry.tMatTitan, EnumToolType.AXE)
			.addToolTrait(ToolAbility.ToolTrait.LIGHTWEIGHT)
			.addToolTrait(ToolAbility.ToolTrait.CORROSION_RESISTANT)
				.addBreakAbility(new ToolAbility.RecursionAbility(12))
				.addHitAbility(new WeaponAbility.BeheaderAbility()).setUnlocalizedName("titanium_axe").setTextureName(RefStrings.MODID + ":titanium_axe");
		titanium_shovel = new ItemToolAbility(3.5F, 0, MainRegistry.tMatTitan, EnumToolType.SHOVEL)
			.addToolTrait(ToolAbility.ToolTrait.LIGHTWEIGHT)
			.addToolTrait(ToolAbility.ToolTrait.CORROSION_RESISTANT)
			.setUnlocalizedName("titanium_shovel").setTextureName(RefStrings.MODID + ":titanium_shovel");
		titanium_hoe = new ModHoe(MainRegistry.tMatTitan).setUnlocalizedName("titanium_hoe").setTextureName(RefStrings.MODID + ":titanium_hoe");

		//steel
		steel_pickaxe = new ItemToolAbility(5F, 0, MainRegistry.tMatSteel, EnumToolType.PICKAXE)
			.addBreakAbility(new ToolAbility.RecursionAbility(3))

			.setUnlocalizedName("steel_pickaxe").setTextureName(RefStrings.MODID + ":steel_pickaxe");
		steel_axe = new ItemToolAbility(5F, 0, MainRegistry.tMatSteel, EnumToolType.AXE)
				.addBreakAbility(new ToolAbility.RecursionAbility(8))
				.addHitAbility(new WeaponAbility.BeheaderAbility()).setUnlocalizedName("steel_axe").setTextureName(RefStrings.MODID + ":steel_axe");
		steel_shovel = new ItemToolAbility(3F, 0, MainRegistry.tMatSteel, EnumToolType.SHOVEL)
			.addBreakAbility(new ToolAbility.RecursionAbility(2))
			.setUnlocalizedName("steel_shovel").setTextureName(RefStrings.MODID + ":steel_shovel");
		steel_hoe = new ModHoe(MainRegistry.tMatSteel).setUnlocalizedName("steel_hoe").setTextureName(RefStrings.MODID + ":steel_hoe");


		//bronze
		alloy_pickaxe = new ItemToolAbility(4F, 0, MainRegistry.tMatAlloy, EnumToolType.PICKAXE)
				//silk touch represents the non-sparking ability of bronze.
				.addToolTrait(ToolAbility.ToolTrait.NON_SPARKING)
				.addToolTrait(ToolAbility.ToolTrait.CORROSION_RESISTANT)
				.addBreakAbility(new ToolAbility.SilkAbility())
				.addBreakAbility(new ToolAbility.RecursionAbility(2)).setUnlocalizedName("alloy_pickaxe").setTextureName(RefStrings.MODID + ":alloy_pickaxe");


		alloy_axe = new ItemToolAbility(5F, 0, MainRegistry.tMatAlloy, EnumToolType.AXE)
			.addToolTrait(ToolAbility.ToolTrait.NON_SPARKING)
			.addToolTrait(ToolAbility.ToolTrait.CORROSION_RESISTANT)
				.addBreakAbility(new ToolAbility.SilkAbility())
				.addBreakAbility(new ToolAbility.RecursionAbility(2))
				.addHitAbility(new WeaponAbility.BeheaderAbility()).setUnlocalizedName("alloy_axe").setTextureName(RefStrings.MODID + ":alloy_axe");

		alloy_shovel = new ItemToolAbility(3F, 0, MainRegistry.tMatAlloy, EnumToolType.SHOVEL)
			.addToolTrait(ToolAbility.ToolTrait.NON_SPARKING)
			.addToolTrait(ToolAbility.ToolTrait.CORROSION_RESISTANT)
				.addBreakAbility(new ToolAbility.SilkAbility())
				.addBreakAbility(new ToolAbility.RecursionAbility(2)).setUnlocalizedName("alloy_shovel").setTextureName(RefStrings.MODID + ":alloy_shovel");

		alloy_hoe = new ModHoe(MainRegistry.tMatAlloy).setUnlocalizedName("alloy_hoe").setTextureName(RefStrings.MODID + ":alloy_hoe");

		alloy_knife = new ModHoe(MainRegistry.tMatAlloy).setUnlocalizedName("alloy_knife").setTextureName(RefStrings.MODID + ":alloy_knife");


		//CMB == alien tech bullshit *RTM interp of combine*
		cmb_pickaxe = new ItemToolAbility(4F, 0, MainRegistry.tMatCMB, EnumToolType.PICKAXE)
				.addBreakAbility(new ToolAbility.RecursionAbility(5))
				.addBreakAbility(new ToolAbility.SmelterAbility())
				.addBreakAbility(new ToolAbility.SilkAbility())
				.addBreakAbility(new LuckAbility(3)).setUnlocalizedName("cmb_pickaxe").setTextureName(RefStrings.MODID + ":cmb_pickaxe");

		cmb_axe = new ItemToolAbility(5F, 0, MainRegistry.tMatCMB, EnumToolType.AXE)
				.addBreakAbility(new ToolAbility.RecursionAbility(5))
				.addBreakAbility(new ToolAbility.SmelterAbility())
				.addBreakAbility(new ToolAbility.SilkAbility())
				.addBreakAbility(new LuckAbility(3))
				.addHitAbility(new WeaponAbility.BeheaderAbility()).setUnlocalizedName("cmb_axe").setTextureName(RefStrings.MODID + ":cmb_axe");

		cmb_shovel = new ItemToolAbility(3F, 0, MainRegistry.tMatCMB, EnumToolType.SHOVEL)
				.addBreakAbility(new ToolAbility.RecursionAbility(5))
				.addBreakAbility(new ToolAbility.SmelterAbility())
				.addBreakAbility(new ToolAbility.SilkAbility())
				.addBreakAbility(new LuckAbility(3)).setUnlocalizedName("cmb_shovel").setTextureName(RefStrings.MODID + ":cmb_shovel");

		cmb_hoe = new ModHoe(MainRegistry.tMatCMB).setUnlocalizedName("cmb_hoe").setTextureName(RefStrings.MODID + ":cmb_hoe");


		//1st drill
		elec_pickaxe = new ItemToolAbilityPower(4F, 0, MainRegistry.tMatElec, EnumToolType.PICKAXE, 500000, 1000, 100)
				.addBreakAbility(new ToolAbility.HammerAbility(1))
				.addBreakAbility(new ToolAbility.RecursionAbility(5))
				.addBreakAbility(new ToolAbility.SilkAbility())
				.addBreakAbility(new LuckAbility(2)).setUnlocalizedName("elec_pickaxe").setTextureName(RefStrings.MODID + ":elec_drill_anim");

		//1st chainsaw
		elec_axe = new ItemToolAbilityPower(5F, 0, MainRegistry.tMatElec, EnumToolType.AXE, 500000, 1000, 100)
				.addBreakAbility(new ToolAbility.HammerAbility(2))
				.addBreakAbility(new ToolAbility.RecursionAbility(10))
				//.addBreakAbility(new ToolAbility.SilkAbility())
				//.addBreakAbility(new LuckAbility(2))
				.addHitAbility(new WeaponAbility.ChainsawAbility(15))
				.addHitAbility(new WeaponAbility.BeheaderAbility()).setShears().setUnlocalizedName("elec_axe").setTextureName(RefStrings.MODID + ":elec_chainsaw_anim");

		//1st shovel drill
		elec_shovel = new ItemToolAbilityPower(3F, 0, MainRegistry.tMatElec, EnumToolType.SHOVEL, 500000, 1000, 100)
				.addBreakAbility(new ToolAbility.HammerAbility(1))
				.addBreakAbility(new ToolAbility.RecursionAbility(5))
				.addBreakAbility(new ToolAbility.SilkAbility())
				.addBreakAbility(new LuckAbility(2)).setUnlocalizedName("elec_shovel").setTextureName(RefStrings.MODID + ":elec_shovel_anim");


		//tool steel tools
		desh_pickaxe = new ItemToolAbility(4F, -0.05, MainRegistry.tMatDesh, EnumToolType.PICKAXE)
				.addBreakAbility(new ToolAbility.HammerAbility(1))
				//.addBreakAbility(new ToolAbility.HammerSilkAbility(1))
				.addBreakAbility(new ToolAbility.RecursionAbility(3))
				//.addBreakAbility(new ToolAbility.SilkAbility())
				.addBreakAbility(new LuckAbility(2)).setUnlocalizedName("desh_pickaxe").setTextureName(RefStrings.MODID + ":desh_pickaxe");

		desh_axe = new ItemToolAbility(5F, -0.05, MainRegistry.tMatDesh, EnumToolType.AXE)
				.addBreakAbility(new ToolAbility.HammerAbility(1))
				.addBreakAbility(new ToolAbility.RecursionAbility(3))
				//.addBreakAbility(new ToolAbility.SilkAbility())
				//.addBreakAbility(new LuckAbility(2))
				.addHitAbility(new WeaponAbility.BeheaderAbility()).setUnlocalizedName("desh_axe").setTextureName(RefStrings.MODID + ":desh_axe");

		desh_shovel = new ItemToolAbility(3F, -0.05, MainRegistry.tMatDesh, EnumToolType.SHOVEL)
				.addBreakAbility(new ToolAbility.HammerAbility(1))
				.addBreakAbility(new ToolAbility.RecursionAbility(3))
				//.addBreakAbility(new ToolAbility.SilkAbility())
				.addBreakAbility(new LuckAbility(2)).setUnlocalizedName("desh_shovel").setTextureName(RefStrings.MODID + ":desh_shovel");

		desh_hoe = new ModHoe(MainRegistry.tMatDesh).setUnlocalizedName("desh_hoe").setTextureName(RefStrings.MODID + ":desh_hoe");


		//TODO cobalt (ALLOYED) not this! > tool steel
		cobalt_pickaxe = new ItemToolAbility(4F, 0, MainRegistry.tMatCobalt, EnumToolType.PICKAXE)
				.addBreakAbility(new ToolAbility.RecursionAbility(4))
				//.addBreakAbility(new ToolAbility.SilkAbility())
				.addBreakAbility(new LuckAbility(1)).setUnlocalizedName("cobalt_pickaxe").setTextureName(RefStrings.MODID + ":cobalt_pickaxe");
		cobalt_axe = new ItemToolAbility(5F, 0, MainRegistry.tMatCobalt, EnumToolType.AXE)
				.addBreakAbility(new ToolAbility.RecursionAbility(4))
				//.addBreakAbility(new ToolAbility.SilkAbility())
				//.addBreakAbility(new LuckAbility(1))
				.addHitAbility(new WeaponAbility.BeheaderAbility()).setUnlocalizedName("cobalt_axe").setTextureName(RefStrings.MODID + ":cobalt_axe");
		cobalt_shovel = new ItemToolAbility(3F, 0, MainRegistry.tMatCobalt, EnumToolType.SHOVEL)
				.addBreakAbility(new ToolAbility.RecursionAbility(4))
				//.addBreakAbility(new ToolAbility.SilkAbility())
				.addBreakAbility(new LuckAbility(1)).setUnlocalizedName("cobalt_shovel").setTextureName(RefStrings.MODID + ":cobalt_shovel");
		cobalt_hoe = new ModHoe(MainRegistry.tMatCobalt).setUnlocalizedName("cobalt_hoe").setTextureName(RefStrings.MODID + ":cobalt_hoe");

		//bigger/better cobalt tools (**ALLOYED*** (TODO! RECIPE NEEDS TO REFLECT ALLOYING)
		// cemented-carbide-tipped tool.
		ToolMaterial matDecCobalt = EnumHelper.addToolMaterial("HBM_COBALT2", 3, 2500, 15.0F, 2.5F, 75).setRepairItem(new ItemStack(ModItems.ingot_cobalt));
		cobalt_decorated_pickaxe = new ItemToolAbility(4F, 0, matDecCobalt, EnumToolType.PICKAXE)
			.addToolTrait(ToolAbility.ToolTrait.WEAR_RESISTANT)
			.addToolTrait(ToolAbility.ToolTrait.CARBIDE_EDGE)
			.addToolTrait(ToolAbility.ToolTrait.BRITTLE_EDGE)
				.addBreakAbility(new ToolAbility.RecursionAbility(4))
				.addBreakAbility(new ToolAbility.HammerAbility(1))
				//.addBreakAbility(new ToolAbility.SilkAbility())
				.addBreakAbility(new LuckAbility(3)).setUnlocalizedName("cobalt_decorated_pickaxe").setTextureName(RefStrings.MODID + ":cobalt_decorated_pickaxe");
		cobalt_decorated_axe = new ItemToolAbility(5F, 0, matDecCobalt, EnumToolType.AXE)
			.addToolTrait(ToolAbility.ToolTrait.WEAR_RESISTANT)
			.addToolTrait(ToolAbility.ToolTrait.CARBIDE_EDGE)
			.addToolTrait(ToolAbility.ToolTrait.BRITTLE_EDGE)
				.addBreakAbility(new ToolAbility.RecursionAbility(4))
				.addBreakAbility(new ToolAbility.HammerAbility(1))
				//.addBreakAbility(new ToolAbility.SilkAbility())
				.addBreakAbility(new LuckAbility(3))
				.addHitAbility(new WeaponAbility.BeheaderAbility()).setUnlocalizedName("cobalt_decorated_axe").setTextureName(RefStrings.MODID + ":cobalt_decorated_axe");
		cobalt_decorated_shovel = new ItemToolAbility(3F, 0, matDecCobalt, EnumToolType.SHOVEL)
			.addToolTrait(ToolAbility.ToolTrait.WEAR_RESISTANT)
			.addToolTrait(ToolAbility.ToolTrait.CARBIDE_EDGE)
			.addToolTrait(ToolAbility.ToolTrait.BRITTLE_EDGE)
				.addBreakAbility(new ToolAbility.RecursionAbility(4))
				.addBreakAbility(new ToolAbility.HammerAbility(1))
				//.addBreakAbility(new ToolAbility.SilkAbility())
				.addBreakAbility(new LuckAbility(3)).setUnlocalizedName("cobalt_decorated_shovel").setTextureName(RefStrings.MODID + ":cobalt_decorated_shovel");
		cobalt_decorated_hoe = new ModHoe(matDecCobalt).setUnlocalizedName("cobalt_decorated_hoe").setTextureName(RefStrings.MODID + ":cobalt_decorated_hoe");

		//UHSS tools
		ToolMaterial matStarmetal = EnumHelper.addToolMaterial("HBM_STARMETAL", 3, 13000, 15.0F, 2.5F, 100).setRepairItem(new ItemStack(ModItems.ingot_starmetal));

		starmetal_pickaxe = new ItemToolAbility(4F, 0, matStarmetal, EnumToolType.PICKAXE)
				.addBreakAbility(new ToolAbility.RecursionAbility(6))
				.addBreakAbility(new ToolAbility.HammerAbility(1))
				//.addBreakAbility(new ToolAbility.HammerSilkAbility(1))
				//.addBreakAbility(new ToolAbility.SilkAbility())
				//.addBreakAbility(new LuckAbility(5))
				.setUnlocalizedName("starmetal_pickaxe").setTextureName(RefStrings.MODID + ":starmetal_pickaxe");
		starmetal_axe = new ItemToolAbility(5F, 0, matStarmetal, EnumToolType.AXE)
				.addBreakAbility(new ToolAbility.RecursionAbility(6))
				.addBreakAbility(new ToolAbility.HammerAbility(1))
				//.addBreakAbility(new ToolAbility.SilkAbility())
				//.addBreakAbility(new LuckAbility(5))
				.addHitAbility(new WeaponAbility.BeheaderAbility())
				.setUnlocalizedName("starmetal_axe").setTextureName(RefStrings.MODID + ":starmetal_axe");
		starmetal_shovel = new ItemToolAbility(3F, 0, matStarmetal, EnumToolType.SHOVEL)
				.addBreakAbility(new ToolAbility.RecursionAbility(6))
				.addBreakAbility(new ToolAbility.HammerAbility(1))
				//.addBreakAbility(new ToolAbility.SilkAbility())
				//.addBreakAbility(new LuckAbility(5))
				.setUnlocalizedName("starmetal_shovel").setTextureName(RefStrings.MODID + ":starmetal_shovel");
		starmetal_hoe = new ModHoe(matStarmetal).setUnlocalizedName("starmetal_hoe").setTextureName(RefStrings.MODID + ":starmetal_hoe");

		//something
		centri_stick = new ItemToolAbility(3F, 0, MainRegistry.tMatElec, EnumToolType.MINER)
				.addBreakAbility(new ToolAbility.CentrifugeAbility()).setMaxDamage(50).setUnlocalizedName("centri_stick").setTextureName(RefStrings.MODID + ":centri_stick");

		//literally just a steel/tungsten hammer
		smashing_hammer = new ItemToolAbility(5F, -0.1, MainRegistry.tMatSteel, EnumToolType.MINER)
				.addHitAbility(new WeaponAbility.BlendAbility(12))
				.addBreakAbility(new ToolAbility.ShredderAbility()).setMaxDamage(2500).setUnlocalizedName("smashing_hammer").setTextureName(RefStrings.MODID + ":smashing_hammer");

		//1st upgraded drill
		drax = new ItemToolAbilityPower(5F, -0.05, MainRegistry.tMatElec, EnumToolType.MINER, 500000000, 100000, 5000)
				.addBreakAbility(new ToolAbility.SmelterAbility())
				//.addBreakAbility(new ToolAbility.ShredderAbility())
				//.addBreakAbility(new ToolAbility.LuckAbility(2))
				.addBreakAbility(new ToolAbility.HammerAbility(1))
				//.addBreakAbility(new ToolAbility.HammerAbility(2))
				//.addBreakAbility(new ToolAbility.HammerSilkAbility(2))
				.addBreakAbility(new ToolAbility.RecursionAbility(5)).setUnlocalizedName("drax").setTextureName(RefStrings.MODID + ":drax");
		//etc
		drax_mk2 = new ItemToolAbilityPower(5F, -0.10, MainRegistry.tMatElec, EnumToolType.MINER, 1000000000, 250000, 7500)
				.addBreakAbility(new ToolAbility.SmelterAbility())
				.addBreakAbility(new ToolAbility.ShredderAbility())
				//.addBreakAbility(new ToolAbility.CentrifugeAbility())
				//.addBreakAbility(new ToolAbility.LuckAbility(3))
				.addBreakAbility(new ToolAbility.HammerAbility(1))
				.addBreakAbility(new ToolAbility.HammerAbility(2))
				//.addBreakAbility(new ToolAbility.HammerAbility(3))
				.addBreakAbility(new ToolAbility.HammerSilkAbility(1))
				.addBreakAbility(new ToolAbility.RecursionAbility(7)).setUnlocalizedName("drax_mk2").setTextureName(RefStrings.MODID + ":drax_mk2");
		//etc
		drax_mk3 = new ItemToolAbilityPower(5F, -0.15, MainRegistry.tMatElec, EnumToolType.MINER, 2500000000L, 500000, 10000)
				.addBreakAbility(new ToolAbility.SmelterAbility())
				.addBreakAbility(new ToolAbility.ShredderAbility())
				.addBreakAbility(new ToolAbility.CentrifugeAbility())
				.addBreakAbility(new ToolAbility.CrystallizerAbility())
				.addBreakAbility(new ToolAbility.SilkAbility())
				//.addBreakAbility(new ToolAbility.LuckAbility(4))
				.addBreakAbility(new ToolAbility.HammerAbility(1))
				//.addBreakAbility(new ToolAbility.HammerAbility(1))
				//.addBreakAbility(new ToolAbility.HammerAbility(1))
				.addBreakAbility(new ToolAbility.HammerAbility(2))
				.addBreakAbility(new ToolAbility.HammerSilkAbility(2))
				.addBreakAbility(new ToolAbility.RecursionAbility(9)).setUnlocalizedName("drax_mk3").setTextureName(RefStrings.MODID + ":drax_mk3");

		//this isn't even craftable anything under it isn't craftable and is purely left in just to look at for reference.
		ToolMaterial matBismuth = EnumHelper.addToolMaterial("HBM_BISMUTH", 4, 0, 50F, 0.0F, 200).setRepairItem(new ItemStack(ModItems.ingot_bismuth));
		bismuth_pickaxe = new ItemToolAbility(4F, 0, matBismuth, EnumToolType.MINER)
				.addBreakAbility(new ToolAbility.HammerAbility(1))
				.addBreakAbility(new ToolAbility.RecursionAbility(4))
				//.addBreakAbility(new ToolAbility.ShredderAbility())
				//.addBreakAbility(new ToolAbility.LuckAbility(2))
				//.addBreakAbility(new ToolAbility.SilkAbility())
				//.addHitAbility(new WeaponAbility.StunAbility(5))
				//.addHitAbility(new WeaponAbility.VampireAbility(2F))
				//.addHitAbility(new WeaponAbility.BeheaderAbility())
				.setDepthRockBreaker().setUnlocalizedName("bismuth_pickaxe").setTextureName(RefStrings.MODID + ":bismuth_pickaxe");
		bismuth_axe = new ItemToolAbility(5F, 0, matBismuth, EnumToolType.AXE)
				.addBreakAbility(new ToolAbility.HammerAbility(2))
				.addBreakAbility(new ToolAbility.RecursionAbility(8))
				//.addBreakAbility(new ToolAbility.ShredderAbility())
				//.addBreakAbility(new ToolAbility.LuckAbility(2))
				.addBreakAbility(new ToolAbility.SilkAbility())
				//.addHitAbility(new WeaponAbility.StunAbility(10))
				//.addHitAbility(new WeaponAbility.VampireAbility(3F))
				//.addHitAbility(new WeaponAbility.BeheaderAbility())
				.setUnlocalizedName("bismuth_axe").setTextureName(RefStrings.MODID + ":bismuth_axe");


		//bullshit
		ToolMaterial matVolcano = EnumHelper.addToolMaterial("HBM_VOLCANIC", 4, 0, 50F, 0.0F, 200).setRepairItem(new ItemStack(ModItems.ingot_bismuth));
		volcanic_pickaxe = new ItemToolAbility(4F, 0, matVolcano, EnumToolType.MINER)
				.addBreakAbility(new ToolAbility.HammerAbility(1))
				.addBreakAbility(new ToolAbility.RecursionAbility(4))
				//.addBreakAbility(new ToolAbility.SmelterAbility())
				//.addBreakAbility(new ToolAbility.LuckAbility(3))
				.addBreakAbility(new ToolAbility.SilkAbility())
				//.addHitAbility(new WeaponAbility.FireAbility(5))
				//.addHitAbility(new WeaponAbility.VampireAbility(2F))
				//.addHitAbility(new WeaponAbility.BeheaderAbility())
				.setDepthRockBreaker().setUnlocalizedName("volcanic_pickaxe").setTextureName(RefStrings.MODID + ":volcanic_pickaxe");
		volcanic_axe = new ItemToolAbility(5F, 0, matVolcano, EnumToolType.AXE)
				.addBreakAbility(new ToolAbility.HammerAbility(1))
				.addBreakAbility(new ToolAbility.RecursionAbility(4))
				//.addBreakAbility(new ToolAbility.SmelterAbility())
				//.addBreakAbility(new ToolAbility.LuckAbility(3))
				//.addBreakAbility(new ToolAbility.SilkAbility())
				//.addHitAbility(new WeaponAbility.FireAbility(10))
				//.addHitAbility(new WeaponAbility.VampireAbility(3F))
				.addHitAbility(new WeaponAbility.BeheaderAbility()).setUnlocalizedName("volcanic_axe").setTextureName(RefStrings.MODID + ":volcanic_axe");

		//tfw rip off terraria so much you become reddit
		ToolMaterial matChlorophyte = EnumHelper.addToolMaterial("HBM_CHLOROPHYTE", 4, 0, 75F, 0.0F, 200).setRepairItem(new ItemStack(ModItems.powder_chlorophyte));
		chlorophyte_pickaxe = new ItemToolAbility(4F, 0, matChlorophyte, EnumToolType.MINER)
				.addBreakAbility(new ToolAbility.HammerAbility(1))
				.addBreakAbility(new ToolAbility.HammerSilkAbility(2))
				.addBreakAbility(new ToolAbility.RecursionAbility(4))
				.addBreakAbility(new ToolAbility.LuckAbility(4))
				.addBreakAbility(new ToolAbility.CentrifugeAbility())
				.addBreakAbility(new ToolAbility.MercuryAbility())
				.addHitAbility(new WeaponAbility.StunAbility(10))
				.addHitAbility(new WeaponAbility.VampireAbility(5F))
				.addHitAbility(new WeaponAbility.BeheaderAbility())
				.setDepthRockBreaker().setUnlocalizedName("chlorophyte_pickaxe").setTextureName(RefStrings.MODID + ":chlorophyte_pickaxe");
		chlorophyte_axe = new ItemToolAbility(5F, 0, matChlorophyte, EnumToolType.AXE)
				.addBreakAbility(new ToolAbility.HammerAbility(2))
				.addBreakAbility(new ToolAbility.RecursionAbility(4))
				.addBreakAbility(new ToolAbility.LuckAbility(4))
				.addHitAbility(new WeaponAbility.StunAbility(15))
				.addHitAbility(new WeaponAbility.VampireAbility(10F))
				.addHitAbility(new WeaponAbility.BeheaderAbility()).setUnlocalizedName("chlorophyte_axe").setTextureName(RefStrings.MODID + ":chlorophyte_axe");

		//what are we doing bro
		ToolMaterial matMese = EnumHelper.addToolMaterial("HBM_MESE", 4, 0, 100F, 0.0F, 200).setRepairItem(new ItemStack(ModItems.plate_paa));
		mese_pickaxe = new ItemToolAbility(4F, 0, matMese, EnumToolType.MINER)
				.addBreakAbility(new ToolAbility.HammerAbility(3))
				.addBreakAbility(new ToolAbility.HammerSilkAbility(3))
				.addBreakAbility(new ToolAbility.RecursionAbility(5))
				.addBreakAbility(new ToolAbility.CrystallizerAbility())
				.addBreakAbility(new ToolAbility.SilkAbility())
				.addBreakAbility(new ToolAbility.LuckAbility(9))
				.addBreakAbility(new ToolAbility.ExplosionAbility(2.5F))
				.addBreakAbility(new ToolAbility.ExplosionAbility(5F))
				.addBreakAbility(new ToolAbility.ExplosionAbility(10F))
				.addBreakAbility(new ToolAbility.ExplosionAbility(15F))
				.addHitAbility(new WeaponAbility.StunAbility(10))
				.addHitAbility(new WeaponAbility.PhosphorusAbility(60))
				.addHitAbility(new WeaponAbility.BeheaderAbility())
				.addHitAbility(new WeaponAbility.BlendAbility(12))
				.setDepthRockBreaker().setUnlocalizedName("mese_pickaxe").setTextureName(RefStrings.MODID + ":mese_pickaxe");
		mese_axe = new ItemToolAbility(5F, 0, matMese, EnumToolType.AXE)
				.addBreakAbility(new ToolAbility.HammerAbility(3))
				.addBreakAbility(new ToolAbility.RecursionAbility(5))
				.addBreakAbility(new ToolAbility.SilkAbility())
				.addBreakAbility(new ToolAbility.LuckAbility(9))
				.addBreakAbility(new ToolAbility.ExplosionAbility(2.5F))
				.addBreakAbility(new ToolAbility.ExplosionAbility(5F))
				.addBreakAbility(new ToolAbility.ExplosionAbility(10F))
				.addBreakAbility(new ToolAbility.ExplosionAbility(15F))
				.addHitAbility(new WeaponAbility.StunAbility(15))
				.addHitAbility(new WeaponAbility.PhosphorusAbility(90))
				.addHitAbility(new WeaponAbility.BeheaderAbility()).setUnlocalizedName("mese_axe").setTextureName(RefStrings.MODID + ":mese_axe");


		ToolMaterial matDwarf = EnumHelper.addToolMaterial("HBM_DWARVEN", 2, 0, 4F, 0.0F, 10).setRepairItem(new ItemStack(ModItems.ingot_copper));
		//dwarven_pickaxe = new ItemToolAbility(5F, -0.1, matDwarf, EnumToolType.MINER)
		//		.addBreakAbility(new ToolAbility.HammerAbility(1)).setUnlocalizedName("dwarven_pickaxe").setMaxDamage(250).setTextureName(RefStrings.MODID + ":dwarven_pickaxe");


		ToolMaterial matMeteorite = EnumHelper.addToolMaterial("HBM_METEORITE", 4, 0, 50F, 0.0F, 200).setRepairItem(new ItemStack(ModItems.plate_paa));

		mask_of_infamy = new MaskOfInfamy(ArmorMaterial.CLOTH, 0).setUnlocalizedName("mask_of_infamy").setMaxStackSize(1).setTextureName(RefStrings.MODID + ":mask_of_infamy");

		hazmat_helmet = new ArmorHazmatMask(MainRegistry.aMatHaz, 0, RefStrings.MODID + ":textures/armor/hazmat_1.png").setUnlocalizedName("hazmat_helmet").setTextureName(RefStrings.MODID + ":hazmat_helmet");
		hazmat_plate = new ArmorHazmat(MainRegistry.aMatHaz, 1, RefStrings.MODID + ":textures/armor/hazmat_1.png").setUnlocalizedName("hazmat_plate").setTextureName(RefStrings.MODID + ":hazmat_plate");
		hazmat_legs = new ArmorHazmat(MainRegistry.aMatHaz, 2, RefStrings.MODID + ":textures/armor/hazmat_2.png").setUnlocalizedName("hazmat_legs").setTextureName(RefStrings.MODID + ":hazmat_legs");
		hazmat_boots = new ArmorHazmat(MainRegistry.aMatHaz, 3, RefStrings.MODID + ":textures/armor/hazmat_1.png").setUnlocalizedName("hazmat_boots").setTextureName(RefStrings.MODID + ":hazmat_boots");
		hazmat_helmet_red = new ArmorHazmatMask(MainRegistry.aMatHaz2, 0, "hbm:textures/models/ModelHazRed.png").setUnlocalizedName("hazmat_helmet_red").setTextureName(RefStrings.MODID + ":hazmat_helmet_red");
		hazmat_plate_red = new ArmorHazmat(MainRegistry.aMatHaz2, 1, RefStrings.MODID + ":textures/armor/hazmat_1_red.png").setUnlocalizedName("hazmat_plate_red").setTextureName(RefStrings.MODID + ":hazmat_plate_red");
		hazmat_legs_red = new ArmorHazmat(MainRegistry.aMatHaz2, 2, RefStrings.MODID + ":textures/armor/hazmat_2_red.png").setUnlocalizedName("hazmat_legs_red").setTextureName(RefStrings.MODID + ":hazmat_legs_red");
		hazmat_boots_red = new ArmorHazmat(MainRegistry.aMatHaz2, 3, RefStrings.MODID + ":textures/armor/hazmat_1_red.png").setUnlocalizedName("hazmat_boots_red").setTextureName(RefStrings.MODID + ":hazmat_boots_red");
		hazmat_helmet_grey = new ArmorHazmatMask(MainRegistry.aMatHaz3, 0, "hbm:textures/models/ModelHazGrey.png")
				.setUnlocalizedName("hazmat_helmet_grey").setTextureName(RefStrings.MODID + ":hazmat_helmet_grey");
		hazmat_plate_grey = new ArmorHazmat(MainRegistry.aMatHaz3, 1, RefStrings.MODID + ":textures/armor/hazmat_1_grey.png").cloneStats((ArmorFSB) hazmat_helmet_grey).setUnlocalizedName("hazmat_plate_grey").setTextureName(RefStrings.MODID + ":hazmat_plate_grey");
		hazmat_legs_grey = new ArmorHazmat(MainRegistry.aMatHaz3, 2, RefStrings.MODID + ":textures/armor/hazmat_2_grey.png").cloneStats((ArmorFSB) hazmat_helmet_grey).setUnlocalizedName("hazmat_legs_grey").setTextureName(RefStrings.MODID + ":hazmat_legs_grey");
		hazmat_boots_grey = new ArmorHazmat(MainRegistry.aMatHaz3, 3, RefStrings.MODID + ":textures/armor/hazmat_1_grey.png").cloneStats((ArmorFSB) hazmat_helmet_grey).setUnlocalizedName("hazmat_boots_grey").setTextureName(RefStrings.MODID + ":hazmat_boots_grey");
		hazmat_paa_helmet = new ArmorHazmatMask(MainRegistry.aMatPaa, 0, RefStrings.MODID + ":textures/armor/hazmat_paa_1.png")
				.setUnlocalizedName("hazmat_paa_helmet").setTextureName(RefStrings.MODID + ":hazmat_paa_helmet");
		hazmat_paa_plate = new ArmorHazmat(MainRegistry.aMatPaa, 1, RefStrings.MODID + ":textures/armor/hazmat_paa_1.png").cloneStats((ArmorFSB) hazmat_paa_helmet).setUnlocalizedName("hazmat_paa_plate").setTextureName(RefStrings.MODID + ":hazmat_paa_plate");
		hazmat_paa_legs = new ArmorHazmat(MainRegistry.aMatPaa, 2, RefStrings.MODID + ":textures/armor/hazmat_paa_2.png").cloneStats((ArmorFSB) hazmat_paa_helmet).setUnlocalizedName("hazmat_paa_legs").setTextureName(RefStrings.MODID + ":hazmat_paa_legs");
		hazmat_paa_boots = new ArmorHazmat(MainRegistry.aMatPaa, 3, RefStrings.MODID + ":textures/armor/hazmat_paa_1.png").cloneStats((ArmorFSB) hazmat_paa_helmet).setUnlocalizedName("hazmat_paa_boots").setTextureName(RefStrings.MODID + ":hazmat_paa_boots");

		ArmorMaterial aMatLiquidator = EnumHelper.addArmorMaterial("HBM_LIQUIDATOR", 750, new int[] { 3, 4, 4, 3 }, 10);
		aMatLiquidator.customCraftingMaterial = ModItems.plate_lead;
		liquidator_helmet = new ArmorLiquidatorMask(aMatLiquidator, 0, RefStrings.MODID + ":textures/armor/liquidator_helmet.png")

				.setStep("hbm:step.metal")
				.setJump("hbm:step.iron_jump")
				.setFall("hbm:step.iron_land").setUnlocalizedName("liquidator_helmet").setMaxStackSize(1).setTextureName(RefStrings.MODID + ":liquidator_helmet");
		liquidator_plate = new ArmorLiquidator(aMatLiquidator, 1, RefStrings.MODID + ":textures/armor/liquidator_1.png").cloneStats((ArmorFSB) liquidator_helmet).setUnlocalizedName("liquidator_plate").setMaxStackSize(1).setTextureName(RefStrings.MODID + ":liquidator_plate");
		liquidator_legs = new ArmorLiquidator(aMatLiquidator, 2, RefStrings.MODID + ":textures/armor/liquidator_2.png").cloneStats((ArmorFSB) liquidator_helmet).setUnlocalizedName("liquidator_legs").setMaxStackSize(1).setTextureName(RefStrings.MODID + ":liquidator_legs");
		liquidator_boots = new ArmorLiquidator(aMatLiquidator, 3, RefStrings.MODID + ":textures/armor/liquidator_1.png").cloneStats((ArmorFSB) liquidator_helmet).setUnlocalizedName("liquidator_boots").setMaxStackSize(1).setTextureName(RefStrings.MODID + ":liquidator_boots");

		//jetpack_boost = new JetpackBooster(Fluids.BALEFIRE, 32000).setUnlocalizedName("jetpack_boost").setCreativeTab(CreativeTabs.tabCombat).setMaxStackSize(1).setTextureName(RefStrings.MODID + ":jetpack_boost");
		//jetpack_break = new JetpackBreak(Fluids.KEROSENE, 12000).setUnlocalizedName("jetpack_break").setCreativeTab(CreativeTabs.tabCombat).setMaxStackSize(1).setTextureName(RefStrings.MODID + ":jetpack_break");
		//jetpack_fly = new JetpackRegular(Fluids.KEROSENE, 12000).setUnlocalizedName("jetpack_fly").setCreativeTab(CreativeTabs.tabCombat).setMaxStackSize(1).setTextureName(RefStrings.MODID + ":jetpack_fly");
		//jetpack_vector = new JetpackVectorized(Fluids.KEROSENE, 16000).setUnlocalizedName("jetpack_vector").setCreativeTab(CreativeTabs.tabCombat).setMaxStackSize(1).setTextureName(RefStrings.MODID + ":jetpack_vector");
		//wings_murk = new WingsMurk(MainRegistry.aMatCobalt).setUnlocalizedName("wings_murk").setCreativeTab(CreativeTabs.tabCombat).setMaxStackSize(1).setTextureName(RefStrings.MODID + ":wings_murk");
		//wings_limp = new WingsMurk(MainRegistry.aMatCobalt).setUnlocalizedName("wings_limp").setCreativeTab(CreativeTabs.tabCombat).setMaxStackSize(1).setTextureName(RefStrings.MODID + ":wings_limp");
		oxy_plss = new ItemModOxy(16000, 10, 1).setUnlocalizedName("oxy_plss").setCreativeTab(MainRegistry.consumableTab).setMaxStackSize(1).setTextureName(RefStrings.MODID + ":oxy_plss");

		//cape_radiation = new ArmorModel(ArmorMaterial.CHAIN, 1).setUnlocalizedName("cape_radiation").setCreativeTab(MainRegistry.consumableTab).setMaxStackSize(1).setTextureName(RefStrings.MODID + ":cape_radiation");
		//cape_gasmask = new ArmorModel(ArmorMaterial.CHAIN, 1).setUnlocalizedName("cape_gasmask").setCreativeTab(MainRegistry.consumableTab).setMaxStackSize(1).setTextureName(RefStrings.MODID + ":cape_gasmask");
		//cape_schrabidium = new ArmorModel(MainRegistry.aMatSchrab, 1).setUnlocalizedName("cape_schrabidium").setCreativeTab(MainRegistry.consumableTab).setMaxStackSize(1).setTextureName(RefStrings.MODID + ":cape_schrabidium");
		//cape_hidden = new ArmorModel(ArmorMaterial.CHAIN, 1).setUnlocalizedName("cape_hidden").setCreativeTab(MainRegistry.consumableTab).setMaxStackSize(1).setTextureName(RefStrings.MODID + ":cape_unknown");

		//schrabidium_hammer = new WeaponSpecial(MainRegistry.tMatHammmer).setUnlocalizedName("schrabidium_hammer").setMaxStackSize(1).setTextureName(RefStrings.MODID + ":schrabidium_hammer");
		//shimmer_sledge = new WeaponSpecial(MainRegistry.enumToolMaterialSledge).setUnlocalizedName("shimmer_sledge").setMaxStackSize(1).setTextureName(RefStrings.MODID + ":shimmer_sledge_original");
		//shimmer_axe = new WeaponSpecial(MainRegistry.enumToolMaterialSledge).setUnlocalizedName("shimmer_axe").setMaxStackSize(1).setTextureName(RefStrings.MODID + ":shimmer_axe");
		bottle_opener = new WeaponSpecial(MainRegistry.enumToolMaterialBottleOpener).setUnlocalizedName("bottle_opener").setCreativeTab(MainRegistry.consumableTab).setMaxStackSize(1).setTextureName(RefStrings.MODID + ":bottle_opener");
		pch = new WeaponSpecial(MainRegistry.tMatHammmer).setUnlocalizedName("pch").setMaxStackSize(1).setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":schrabidium_hammer");
		euphemium_stopper = new ItemSyringe().setUnlocalizedName("euphemium_stopper").setMaxStackSize(1).setFull3D().setTextureName(RefStrings.MODID + ":euphemium_stopper");
		matchstick = new ItemMatch().setUnlocalizedName("matchstick").setCreativeTab(CreativeTabs.tabTools).setFull3D().setTextureName(RefStrings.MODID + ":matchstick");
		//balefire_and_steel = new ItemBalefireMatch().setUnlocalizedName("balefire_and_steel").setCreativeTab(CreativeTabs.tabTools).setFull3D().setTextureName(RefStrings.MODID + ":balefire_and_steel");
		crowbar = new ModSword(MainRegistry.tMatSteel).setUnlocalizedName("crowbar").setFull3D().setTextureName(RefStrings.MODID + ":crowbar");
		wrench = new WeaponSpecial(MainRegistry.tMatSteel).setUnlocalizedName("wrench").setMaxStackSize(1).setTextureName(RefStrings.MODID + ":wrench");
		//wrench_flipped = new WeaponSpecial(MainRegistry.tMatElec).setUnlocalizedName("wrench_flipped").setMaxStackSize(1).setTextureName(RefStrings.MODID + ":wrench_flipped");
		memespoon = new WeaponSpecial(MainRegistry.tMatSteel).setUnlocalizedName("memespoon").setMaxStackSize(1).setTextureName(RefStrings.MODID + ":memespoon");
		wood_gavel = new WeaponSpecial(ToolMaterial.WOOD).setUnlocalizedName("wood_gavel").setMaxStackSize(1).setTextureName(RefStrings.MODID + ":wood_gavel");
		lead_gavel = new WeaponSpecial(MainRegistry.tMatSteel).setUnlocalizedName("lead_gavel").setMaxStackSize(1).setTextureName(RefStrings.MODID + ":lead_gavel");
		diamond_gavel = new WeaponSpecial(ToolMaterial.EMERALD).setUnlocalizedName("diamond_gavel").setMaxStackSize(1).setTextureName(RefStrings.MODID + ":diamond_gavel");
		ToolMaterial matMeseGavel = EnumHelper.addToolMaterial("HBM_MESEGAVEL", 4, 0, 50F, 0.0F, 200).setRepairItem(new ItemStack(ModItems.plate_paa));
		//mese_gavel = new ItemSwordAbility(250, 1.5, matMeseGavel)
		//		.addHitAbility(new WeaponAbility.PhosphorusAbility(60))
		//		.addHitAbility(new WeaponAbility.RadiationAbility(500))
		//		.addHitAbility(new WeaponAbility.StunAbility(10))
		//		.addHitAbility(new WeaponAbility.VampireAbility(50))
		//		.addHitAbility(new WeaponAbility.BeheaderAbility()).setUnlocalizedName("mese_gavel").setMaxStackSize(1).setTextureName(RefStrings.MODID + ":mese_gavel");

		multitool_hit = new ItemMultitoolPassive().setUnlocalizedName("multitool_hit").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":multitool_fist");
		multitool_dig = new ItemMultitoolTool(4.0F, MainRegistry.enumToolMaterialMultitool, ItemMultitoolTool.getAllBlocks()).setFull3D().setUnlocalizedName("multitool_dig").setCreativeTab(MainRegistry.consumableTab).setTextureName(RefStrings.MODID + ":multitool_claw");
		multitool_silk = new ItemMultitoolTool(4.0F, MainRegistry.enumToolMaterialMultitool, ItemMultitoolTool.getAllBlocks()).setFull3D().setUnlocalizedName("multitool_silk").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":multitool_claw");
		multitool_ext = new ItemMultitoolPassive().setUnlocalizedName("multitool_ext").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":multitool_open");
		multitool_miner = new ItemMultitoolPassive().setUnlocalizedName("multitool_miner").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":multitool_pointer");
		multitool_beam = new ItemMultitoolPassive().setUnlocalizedName("multitool_beam").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":multitool_pointer");
		multitool_sky= new ItemMultitoolPassive().setUnlocalizedName("multitool_sky").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":multitool_open");
		multitool_mega = new ItemMultitoolPassive().setUnlocalizedName("multitool_mega").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":multitool_fist");
		multitool_joule = new ItemMultitoolPassive().setUnlocalizedName("multitool_joule").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":multitool_fist");
		multitool_decon = new ItemMultitoolPassive().setUnlocalizedName("multitool_decon").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":multitool_fist");

		saw = new ModSword(MainRegistry.enumToolMaterialSaw).setUnlocalizedName("weapon_saw").setFull3D().setTextureName(RefStrings.MODID + ":saw");
		bat = new ModSword(MainRegistry.enumToolMaterialBat).setUnlocalizedName("weapon_bat").setFull3D().setTextureName(RefStrings.MODID + ":bat");
		bat_nail = new ModSword(MainRegistry.enumToolMaterialBatNail).setUnlocalizedName("weapon_bat_nail").setFull3D().setTextureName(RefStrings.MODID + ":bat_nail");
		golf_club = new ModSword(MainRegistry.enumToolMaterialGolfClub).setUnlocalizedName("weapon_golf_club").setFull3D().setTextureName(RefStrings.MODID + ":golf_club");
		pipe_rusty = new ModSword(MainRegistry.enumToolMaterialPipeRusty).setUnlocalizedName("weapon_pipe_rusty").setFull3D().setTextureName(RefStrings.MODID + ":pipe_rusty");
		pipe_lead = new ModSword(MainRegistry.enumToolMaterialPipeLead).setUnlocalizedName("weapon_pipe_lead").setFull3D().setTextureName(RefStrings.MODID + ":pipe_lead");
		reer_graar = new ModSword(MainRegistry.tMatTitan).setUnlocalizedName("reer_graar").setFull3D().setTextureName(RefStrings.MODID + ":reer_graar_hd");
		//stopsign = new WeaponSpecial(MainRegistry.tMatAlloy).setUnlocalizedName("stopsign").setTextureName(RefStrings.MODID + ":stopsign");
		//sopsign = new WeaponSpecial(MainRegistry.tMatAlloy).setUnlocalizedName("sopsign").setTextureName(RefStrings.MODID + ":sopsign");
		//chernobylsign = new WeaponSpecial(MainRegistry.tMatAlloy).setUnlocalizedName("chernobylsign").setTextureName(RefStrings.MODID + ":chernobylsign");

		crystal_horn = new ItemCustomLore().setUnlocalizedName("crystal_horn").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":crystal_horn");
		crystal_charred = new ItemCustomLore().setUnlocalizedName("crystal_charred").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":crystal_charred");

		//bucket_mud = new ItemModBucket(ModBlocks.mud_block).setUnlocalizedName("bucket_mud").setContainerItem(Items.bucket).setCreativeTab(MainRegistry.blockTab).setTextureName(RefStrings.MODID + ":bucket_mud");
		bucket_acid = new ItemModBucket(ModBlocks.acid_block).setUnlocalizedName("bucket_acid").setContainerItem(Items.bucket).setCreativeTab(MainRegistry.blockTab).setTextureName(RefStrings.MODID + ":bucket_acid");
		bucket_toxic = new ItemModBucket(ModBlocks.toxic_block).setUnlocalizedName("bucket_toxic").setContainerItem(Items.bucket).setCreativeTab(MainRegistry.blockTab).setTextureName(RefStrings.MODID + ":bucket_toxic");
		bucket_schrabidic_acid = new ItemModBucket(ModBlocks.schrabidic_block).setUnlocalizedName("bucket_schrabidic_acid").setContainerItem(Items.bucket).setCreativeTab(MainRegistry.blockTab).setTextureName(RefStrings.MODID + ":bucket_schrabidic_acid");
		bucket_sulfuric_acid = new ItemModBucket(ModBlocks.sulfuric_acid_block).setUnlocalizedName("bucket_sulfuric_acid").setContainerItem(Items.bucket).setCreativeTab(MainRegistry.blockTab).setTextureName(RefStrings.MODID + ":bucket_sulfuric_acid");
		bucket_mercury = new ItemModBucket(ModBlocks.mercury_block).setUnlocalizedName("bucket_mercury").setContainerItem(Items.bucket).setCreativeTab(MainRegistry.blockTab).setTextureName(RefStrings.MODID + ":bucket_mercury");
		bucket_bromine = new ItemModBucket(ModBlocks.bromine_block).setUnlocalizedName("bucket_bromine").setContainerItem(Items.bucket).setCreativeTab(MainRegistry.blockTab).setTextureName(RefStrings.MODID + ":bucket_bromine");

		door_metal = new ItemModDoor().setUnlocalizedName("door_metal").setCreativeTab(MainRegistry.blockTab).setTextureName(RefStrings.MODID + ":door_metal");
		door_office = new ItemModDoor().setUnlocalizedName("door_office").setCreativeTab(MainRegistry.blockTab).setTextureName(RefStrings.MODID + ":door_office");
		door_bunker = new ItemModDoor().setUnlocalizedName("door_bunker").setCreativeTab(MainRegistry.blockTab).setTextureName(RefStrings.MODID + ":door_bunker");
		door_red = new ItemModDoor().setUnlocalizedName("door_red").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":door_red");

		sliding_blast_door_skin = new ItemSlidingBlastDoorSkin().setUnlocalizedName("sliding_blast_door_skin").setCreativeTab(CreativeTabs.tabMisc).setTextureName(RefStrings.MODID + ":sliding_blast_door_default");

		record_lc = new ItemModRecord("lc").setUnlocalizedName("record_lc").setCreativeTab(CreativeTabs.tabMisc).setTextureName(RefStrings.MODID + ":record_lc");
		record_ss = new ItemModRecord("ss").setUnlocalizedName("record_ss").setCreativeTab(CreativeTabs.tabMisc).setTextureName(RefStrings.MODID + ":record_ss");
		record_vc = new ItemModRecord("vc").setUnlocalizedName("record_vc").setCreativeTab(CreativeTabs.tabMisc).setTextureName(RefStrings.MODID + ":record_vc");
		record_gs = new ItemModRecord("gs").setUnlocalizedName("record_gs").setCreativeTab(CreativeTabs.tabMisc).setTextureName(RefStrings.MODID + ":record_gs");
		record_gp = new ItemModRecord("gp").setUnlocalizedName("record_gp").setCreativeTab(CreativeTabs.tabMisc).setTextureName(RefStrings.MODID + ":record_gp");
		record_el = new ItemModRecord("el").setUnlocalizedName("record_el").setCreativeTab(CreativeTabs.tabMisc).setTextureName(RefStrings.MODID + ":record_el");
		record_glass = new ItemModRecord("glass").setUnlocalizedName("record_glass").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":record_glass");

		book_guide = new ItemGuideBook().setUnlocalizedName("book_guide").setCreativeTab(MainRegistry.consumableTab).setTextureName(RefStrings.MODID + ":book_guide");
		book_lore = new ItemBookLore().setUnlocalizedName("book_lore").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":book_pages");
		holotape_image = new ItemHolotapeImage().setUnlocalizedName("holotape_image").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":holotape");
		holotape_damaged = new Item().setUnlocalizedName("holotape_damaged").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":holotape_damaged");

		//polaroid = new ItemPolaroid().setUnlocalizedName("polaroid").setMaxStackSize(1).setCreativeTab(MainRegistry.consumableTab).setTextureName(RefStrings.MODID + ":polaroid_" + MainRegistry.polaroidID);
		glitch = new ItemGlitch().setUnlocalizedName("glitch").setMaxStackSize(1).setCreativeTab(MainRegistry.consumableTab).setTextureName(RefStrings.MODID + ":glitch_" + MainRegistry.polaroidID);
		letter = new ItemStarterKit().setUnlocalizedName("letter").setCreativeTab(MainRegistry.consumableTab).setTextureName(RefStrings.MODID + ":letter");
		//book_secret = new ItemCustomLore().setUnlocalizedName("book_secret").setCreativeTab(MainRegistry.polaroidID == 11 ? MainRegistry.consumableTab : null).setTextureName(RefStrings.MODID + ":book_secret");
		//book_of_ = new ItemBook().setUnlocalizedName("book_of_").setMaxStackSize(1).setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":book_of_");
		//page_of_ = new ItemEnumMulti(ItemEnums.EnumPages.class, true, false).setUnlocalizedName("page_of_").setMaxStackSize(1).setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":page_of_");
		//book_lemegeton = new ItemBookLemegeton().setUnlocalizedName("book_lemegeton").setMaxStackSize(1).setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":book_lemegeton");
		burnt_bark = new ItemCustomLore().setUnlocalizedName("burnt_bark").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":burnt_bark");

		b_smoke1 = new Item().setUnlocalizedName("b_smoke1").setTextureName(RefStrings.MODID + ":b_smoke1");
		b_smoke2 = new Item().setUnlocalizedName("b_smoke2").setTextureName(RefStrings.MODID + ":b_smoke2");
		b_smoke3 = new Item().setUnlocalizedName("b_smoke3").setTextureName(RefStrings.MODID + ":b_smoke3");
		b_smoke4 = new Item().setUnlocalizedName("b_smoke4").setTextureName(RefStrings.MODID + ":b_smoke4");
		b_smoke5 = new Item().setUnlocalizedName("b_smoke5").setTextureName(RefStrings.MODID + ":b_smoke5");
		b_smoke6 = new Item().setUnlocalizedName("b_smoke6").setTextureName(RefStrings.MODID + ":b_smoke6");
		b_smoke7 = new Item().setUnlocalizedName("b_smoke7").setTextureName(RefStrings.MODID + ":b_smoke7");
		b_smoke8 = new Item().setUnlocalizedName("b_smoke8").setTextureName(RefStrings.MODID + ":b_smoke8");
		chlorine1 = new Item().setUnlocalizedName("chlorine1").setTextureName(RefStrings.MODID + ":chlorine1");
		chlorine2 = new Item().setUnlocalizedName("chlorine2").setTextureName(RefStrings.MODID + ":chlorine2");
		chlorine3 = new Item().setUnlocalizedName("chlorine3").setTextureName(RefStrings.MODID + ":chlorine3");
		chlorine4 = new Item().setUnlocalizedName("chlorine4").setTextureName(RefStrings.MODID + ":chlorine4");
		chlorine5 = new Item().setUnlocalizedName("chlorine5").setTextureName(RefStrings.MODID + ":chlorine5");
		chlorine6 = new Item().setUnlocalizedName("chlorine6").setTextureName(RefStrings.MODID + ":chlorine6");
		chlorine7 = new Item().setUnlocalizedName("chlorine7").setTextureName(RefStrings.MODID + ":chlorine7");
		chlorine8 = new Item().setUnlocalizedName("chlorine8").setTextureName(RefStrings.MODID + ":chlorine8");
		pc1 = new Item().setUnlocalizedName("pc1").setTextureName(RefStrings.MODID + ":pc1");
		pc2 = new Item().setUnlocalizedName("pc2").setTextureName(RefStrings.MODID + ":pc2");
		pc3 = new Item().setUnlocalizedName("pc3").setTextureName(RefStrings.MODID + ":pc3");
		pc4 = new Item().setUnlocalizedName("pc4").setTextureName(RefStrings.MODID + ":pc4");
		pc5 = new Item().setUnlocalizedName("pc5").setTextureName(RefStrings.MODID + ":pc5");
		pc6 = new Item().setUnlocalizedName("pc6").setTextureName(RefStrings.MODID + ":pc6");
		pc7 = new Item().setUnlocalizedName("pc7").setTextureName(RefStrings.MODID + ":pc7");
		pc8 = new Item().setUnlocalizedName("pc8").setTextureName(RefStrings.MODID + ":pc8");
		cloud1 = new Item().setUnlocalizedName("cloud1").setTextureName(RefStrings.MODID + ":cloud1");
		cloud2 = new Item().setUnlocalizedName("cloud2").setTextureName(RefStrings.MODID + ":cloud2");
		cloud3 = new Item().setUnlocalizedName("cloud3").setTextureName(RefStrings.MODID + ":cloud3");
		cloud4 = new Item().setUnlocalizedName("cloud4").setTextureName(RefStrings.MODID + ":cloud4");
		cloud5 = new Item().setUnlocalizedName("cloud5").setTextureName(RefStrings.MODID + ":cloud5");
		cloud6 = new Item().setUnlocalizedName("cloud6").setTextureName(RefStrings.MODID + ":cloud6");
		cloud7 = new Item().setUnlocalizedName("cloud7").setTextureName(RefStrings.MODID + ":cloud7");
		cloud8 = new Item().setUnlocalizedName("cloud8").setTextureName(RefStrings.MODID + ":cloud8");
		orange1 = new Item().setUnlocalizedName("orange1").setTextureName(RefStrings.MODID + ":orange1");
		orange2 = new Item().setUnlocalizedName("orange2").setTextureName(RefStrings.MODID + ":orange2");
		orange3 = new Item().setUnlocalizedName("orange3").setTextureName(RefStrings.MODID + ":orange3");
		orange4 = new Item().setUnlocalizedName("orange4").setTextureName(RefStrings.MODID + ":orange4");
		orange5 = new Item().setUnlocalizedName("orange5").setTextureName(RefStrings.MODID + ":orange5");
		orange6 = new Item().setUnlocalizedName("orange6").setTextureName(RefStrings.MODID + ":orange6");
		orange7 = new Item().setUnlocalizedName("orange7").setTextureName(RefStrings.MODID + ":orange7");
		orange8 = new Item().setUnlocalizedName("orange8").setTextureName(RefStrings.MODID + ":orange8");
		flame_1 = new Item().setUnlocalizedName("flame_1").setTextureName(RefStrings.MODID + ":flame_1");
		flame_2 = new Item().setUnlocalizedName("flame_2").setTextureName(RefStrings.MODID + ":flame_2");
		flame_3 = new Item().setUnlocalizedName("flame_3").setTextureName(RefStrings.MODID + ":flame_3");
		flame_4 = new Item().setUnlocalizedName("flame_4").setTextureName(RefStrings.MODID + ":flame_4");
		flame_5 = new Item().setUnlocalizedName("flame_5").setTextureName(RefStrings.MODID + ":flame_5");
		flame_6 = new Item().setUnlocalizedName("flame_6").setTextureName(RefStrings.MODID + ":flame_6");
		flame_7 = new Item().setUnlocalizedName("flame_7").setTextureName(RefStrings.MODID + ":flame_7");
		flame_8 = new Item().setUnlocalizedName("flame_8").setTextureName(RefStrings.MODID + ":flame_8");
		flame_9 = new Item().setUnlocalizedName("flame_9").setTextureName(RefStrings.MODID + ":flame_9");
		flame_10 = new Item().setUnlocalizedName("flame_10").setTextureName(RefStrings.MODID + ":flame_10");
		ln2_1 = new Item().setUnlocalizedName("ln2_1").setTextureName(RefStrings.MODID + ":ln2_1");
		ln2_2 = new Item().setUnlocalizedName("ln2_2").setTextureName(RefStrings.MODID + ":ln2_2");
		ln2_3 = new Item().setUnlocalizedName("ln2_3").setTextureName(RefStrings.MODID + ":ln2_3");
		ln2_4 = new Item().setUnlocalizedName("ln2_4").setTextureName(RefStrings.MODID + ":ln2_4");
		ln2_5 = new Item().setUnlocalizedName("ln2_5").setTextureName(RefStrings.MODID + ":ln2_5");
		ln2_6 = new Item().setUnlocalizedName("ln2_6").setTextureName(RefStrings.MODID + ":ln2_6");
		ln2_7 = new Item().setUnlocalizedName("ln2_7").setTextureName(RefStrings.MODID + ":ln2_7");
		ln2_8 = new Item().setUnlocalizedName("ln2_8").setTextureName(RefStrings.MODID + ":ln2_8");
		ln2_9 = new Item().setUnlocalizedName("ln2_9").setTextureName(RefStrings.MODID + ":ln2_9");
		ln2_10 = new Item().setUnlocalizedName("ln2_10").setTextureName(RefStrings.MODID + ":ln2_10");
		nothing = new Item().setUnlocalizedName("nothing").setTextureName(RefStrings.MODID + ":nothing");
		void_anim = new Item().setUnlocalizedName("void_anim").setTextureName(RefStrings.MODID + ":void_anim");


		achievement_icon = new ItemEnumMulti(ItemEnums.EnumAchievementType.class, true, true).setUnlocalizedName("achievement_icon");
		bob_metalworks = new Item().setUnlocalizedName("bob_metalworks").setTextureName(RefStrings.MODID + ":bob_metalworks");
		bob_assembly = new Item().setUnlocalizedName("bob_assembly").setTextureName(RefStrings.MODID + ":bob_assembly");
		bob_chemistry = new Item().setUnlocalizedName("bob_chemistry").setTextureName(RefStrings.MODID + ":bob_chemistry");
		bob_oil = new Item().setUnlocalizedName("bob_oil").setTextureName(RefStrings.MODID + ":bob_oil");
		bob_nuclear = new Item().setUnlocalizedName("bob_nuclear").setTextureName(RefStrings.MODID + ":bob_nuclear");

		//mysteryshovel = new ItemMS().setUnlocalizedName("mysteryshovel").setFull3D().setMaxStackSize(1).setTextureName(RefStrings.MODID + ":cursed_shovel");
		memory = new ItemBattery(Long.MAX_VALUE / 100L, 100000000000000L, 100000000000000L).setUnlocalizedName("memory").setMaxStackSize(1).setTextureName(RefStrings.MODID + ":mo8_anim");

		GunFactory.init();

		//FluidContainerRegistry.registerFluidContainer(new FluidStack(ModBlocks.mud_fluid, 1000), new ItemStack(ModItems.bucket_mud), new ItemStack(Items.bucket));
		FluidContainerRegistry.registerFluidContainer(new FluidStack(ModBlocks.acid_fluid, 1000), new ItemStack(ModItems.bucket_acid), new ItemStack(Items.bucket));
		FluidContainerRegistry.registerFluidContainer(new FluidStack(ModBlocks.toxic_fluid, 1000), new ItemStack(ModItems.bucket_toxic), new ItemStack(Items.bucket));
		FluidContainerRegistry.registerFluidContainer(new FluidStack(ModBlocks.schrabidic_fluid, 1000), new ItemStack(ModItems.bucket_schrabidic_acid), new ItemStack(Items.bucket));
		FluidContainerRegistry.registerFluidContainer(new FluidStack(ModBlocks.sulfuric_acid_fluid, 1000), new ItemStack(ModItems.bucket_sulfuric_acid), new ItemStack(Items.bucket));
		FluidContainerRegistry.registerFluidContainer(new FluidStack(ModBlocks.mercury_fluid, 1000), new ItemStack(ModItems.bucket_mercury), new ItemStack(Items.bucket));
		FluidContainerRegistry.registerFluidContainer(new FluidStack(ModBlocks.bromine_fluid, 1000), new ItemStack(ModItems.bucket_bromine), new ItemStack(Items.bucket));
		//BucketHandler.INSTANCE.buckets.put(ModBlocks.mud_block, ModItems.bucket_mud);
		BucketHandler.INSTANCE.buckets.put(ModBlocks.acid_block, ModItems.bucket_acid);
		BucketHandler.INSTANCE.buckets.put(ModBlocks.toxic_block, ModItems.bucket_toxic);
		BucketHandler.INSTANCE.buckets.put(ModBlocks.schrabidic_block, ModItems.bucket_schrabidic_acid);
		BucketHandler.INSTANCE.buckets.put(ModBlocks.sulfuric_acid_block, ModItems.bucket_sulfuric_acid);
		BucketHandler.INSTANCE.buckets.put(ModBlocks.mercury_block, ModItems.bucket_mercury);
		BucketHandler.INSTANCE.buckets.put(ModBlocks.bromine_block, ModItems.bucket_bromine);
		MinecraftForge.EVENT_BUS.register(BucketHandler.INSTANCE);
	}

	private static void registerItem() {
		//Weapons
		//GameRegistry.registerItem(redstone_sword, redstone_sword.getUnlocalizedName());
		//GameRegistry.registerItem(big_sword, big_sword.getUnlocalizedName());
		//GameRegistry.registerItem(butter_sword, butter_sword.getUnlocalizedName());

		//Test Armor

		//Test Nuke
		GameRegistry.registerItem(test_nuke_igniter, test_nuke_igniter.getUnlocalizedName());
		GameRegistry.registerItem(test_nuke_propellant, test_nuke_propellant.getUnlocalizedName());
		GameRegistry.registerItem(test_nuke_tier1_shielding, test_nuke_tier1_shielding.getUnlocalizedName());
		GameRegistry.registerItem(test_nuke_tier2_shielding, test_nuke_tier2_shielding.getUnlocalizedName());
		GameRegistry.registerItem(test_nuke_tier1_bullet, test_nuke_tier1_bullet.getUnlocalizedName());
		GameRegistry.registerItem(test_nuke_tier2_bullet, test_nuke_tier2_bullet.getUnlocalizedName());
		GameRegistry.registerItem(test_nuke_tier1_target, test_nuke_tier1_target.getUnlocalizedName());
		GameRegistry.registerItem(test_nuke_tier2_target, test_nuke_tier2_target.getUnlocalizedName());

		//Ingots
		GameRegistry.registerItem(ingot_uranium, ingot_uranium.getUnlocalizedName());
		GameRegistry.registerItem(ingot_promethium, ingot_promethium.getUnlocalizedName());
		GameRegistry.registerItem(ingot_u233, ingot_u233.getUnlocalizedName());
		GameRegistry.registerItem(ingot_u235, ingot_u235.getUnlocalizedName());
		GameRegistry.registerItem(ingot_u238, ingot_u238.getUnlocalizedName());
		GameRegistry.registerItem(ingot_u238m2, ingot_u238m2.getUnlocalizedName());
		GameRegistry.registerItem(ingot_th232, ingot_th232.getUnlocalizedName());
		GameRegistry.registerItem(ingot_plutonium, ingot_plutonium.getUnlocalizedName());
		GameRegistry.registerItem(ingot_pu238, ingot_pu238.getUnlocalizedName());
		GameRegistry.registerItem(ingot_pu239, ingot_pu239.getUnlocalizedName());
		GameRegistry.registerItem(ingot_pu240, ingot_pu240.getUnlocalizedName());
		GameRegistry.registerItem(ingot_pu241, ingot_pu241.getUnlocalizedName());
		GameRegistry.registerItem(ingot_pu_mix, ingot_pu_mix.getUnlocalizedName());
		GameRegistry.registerItem(ingot_am241, ingot_am241.getUnlocalizedName());
		GameRegistry.registerItem(ingot_am242, ingot_am242.getUnlocalizedName());
		GameRegistry.registerItem(ingot_am_mix, ingot_am_mix.getUnlocalizedName());
		GameRegistry.registerItem(ingot_cm242, ingot_cm242.getUnlocalizedName());
		GameRegistry.registerItem(ingot_cm243, ingot_cm243.getUnlocalizedName());
		GameRegistry.registerItem(ingot_cm244, ingot_cm244.getUnlocalizedName());
		GameRegistry.registerItem(ingot_cm245, ingot_cm245.getUnlocalizedName());
		GameRegistry.registerItem(ingot_cm246, ingot_cm246.getUnlocalizedName());
		GameRegistry.registerItem(ingot_cm247, ingot_cm247.getUnlocalizedName());
		GameRegistry.registerItem(ingot_cm_fuel, ingot_cm_fuel.getUnlocalizedName());
		GameRegistry.registerItem(turret_light_ammo, turret_light_ammo.getUnlocalizedName());
		GameRegistry.registerItem(rubidium_ingot, rubidium_ingot.getUnlocalizedName());
		GameRegistry.registerItem(ingot_cm_mix, ingot_cm_mix.getUnlocalizedName());
		GameRegistry.registerItem(ingot_bk247, ingot_bk247.getUnlocalizedName());
		GameRegistry.registerItem(gpmix, gpmix.getUnlocalizedName());
		GameRegistry.registerItem(ingot_cf251, ingot_cf251.getUnlocalizedName());
		GameRegistry.registerItem(ingot_cf252, ingot_cf252.getUnlocalizedName());
		GameRegistry.registerItem(ingot_cf247, ingot_cf247.getUnlocalizedName());
		GameRegistry.registerItem(ingot_cm248, ingot_cm248.getUnlocalizedName());
		GameRegistry.registerItem(cf_light_fraction, cf_light_fraction.getUnlocalizedName());
		GameRegistry.registerItem(ingot_cf249, ingot_cf249.getUnlocalizedName());
		GameRegistry.registerItem(ingot_cf248, ingot_cf248.getUnlocalizedName());
		GameRegistry.registerItem(ingot_yb170, ingot_yb170.getUnlocalizedName());
		GameRegistry.registerItem(ingot_es253, ingot_es253.getUnlocalizedName());
		GameRegistry.registerItem(ingot_es255, ingot_es255.getUnlocalizedName());
		GameRegistry.registerItem(ingot_neptunium, ingot_neptunium.getUnlocalizedName());
		GameRegistry.registerItem(ingot_polonium, ingot_polonium.getUnlocalizedName());
		GameRegistry.registerItem(ingot_technetium, ingot_technetium.getUnlocalizedName());
		GameRegistry.registerItem(ingot_molybdenum, ingot_molybdenum.getUnlocalizedName());
		GameRegistry.registerItem(ingot_co60, ingot_co60.getUnlocalizedName());
		GameRegistry.registerItem(ingot_sr90, ingot_sr90.getUnlocalizedName());
		GameRegistry.registerItem(ingot_au198, ingot_au198.getUnlocalizedName());
		GameRegistry.registerItem(ingot_pb209, ingot_pb209.getUnlocalizedName());
		GameRegistry.registerItem(ingot_ra226, ingot_ra226.getUnlocalizedName());
		GameRegistry.registerItem(ingot_titanium, ingot_titanium.getUnlocalizedName());
		GameRegistry.registerItem(ingot_nickel, ingot_nickel.getUnlocalizedName());
		GameRegistry.registerItem(ingot_copper, ingot_copper.getUnlocalizedName());
		GameRegistry.registerItem(ingot_red_copper, ingot_red_copper.getUnlocalizedName());
		GameRegistry.registerItem(billet_red_copper, billet_red_copper.getUnlocalizedName());
		GameRegistry.registerItem(ingot_advanced_alloy, ingot_advanced_alloy.getUnlocalizedName());
		GameRegistry.registerItem(ingot_zinc, ingot_zinc.getUnlocalizedName());
		GameRegistry.registerItem(ingot_tungsten, ingot_tungsten.getUnlocalizedName());
		GameRegistry.registerItem(ingot_gallium, ingot_gallium.getUnlocalizedName());
		GameRegistry.registerItem(ingot_gaas, ingot_gaas.getUnlocalizedName());
		GameRegistry.registerItem(silver_gallium_arsenide, silver_gallium_arsenide.getUnlocalizedName());
		GameRegistry.registerItem(ingot_tt, ingot_tt.getUnlocalizedName());
		GameRegistry.registerItem(ingot_ttas, ingot_ttas.getUnlocalizedName());
		GameRegistry.registerItem(ingot_platnium, ingot_platnium.getUnlocalizedName());
		GameRegistry.registerItem(ingot_iridium, ingot_iridium.getUnlocalizedName());
		GameRegistry.registerItem(powder_iridium, powder_iridium.getUnlocalizedName());
		GameRegistry.registerItem(ruthenium_residue, ruthenium_residue.getUnlocalizedName());
		GameRegistry.registerItem(ingot_palladium, ingot_palladium.getUnlocalizedName());
		GameRegistry.registerItem(ingot_samarium, ingot_samarium.getUnlocalizedName());
		GameRegistry.registerItem(ingot_smco, ingot_smco.getUnlocalizedName());
		GameRegistry.registerItem(ingot_europium, ingot_europium.getUnlocalizedName());
		GameRegistry.registerItem(ingot_terbium, ingot_terbium.getUnlocalizedName());
		GameRegistry.registerItem(ingot_thulium, ingot_thulium.getUnlocalizedName());
		GameRegistry.registerItem(ingot_thulium170, ingot_thulium170.getUnlocalizedName());
		GameRegistry.registerItem(nugget_europium, nugget_europium.getUnlocalizedName());
		GameRegistry.registerItem(ingot_aluminium, ingot_aluminium.getUnlocalizedName());
		GameRegistry.registerItem(ingot_tin, ingot_tin.getUnlocalizedName());
		GameRegistry.registerItem(ingot_steel, ingot_steel.getUnlocalizedName());
		GameRegistry.registerItem(ingot_chromium, ingot_chromium.getUnlocalizedName());
		GameRegistry.registerItem(ingot_vanadium, ingot_vanadium.getUnlocalizedName());
		GameRegistry.registerItem(ingot_stainless, ingot_stainless.getUnlocalizedName());
		GameRegistry.registerItem(ingot_tcalloy, ingot_tcalloy.getUnlocalizedName());
		GameRegistry.registerItem(ingot_cdalloy, ingot_cdalloy.getUnlocalizedName());
		GameRegistry.registerItem(ingot_bismuth_bronze, ingot_bismuth_bronze.getUnlocalizedName());
		GameRegistry.registerItem(ingot_arsenic_bronze, ingot_arsenic_bronze.getUnlocalizedName());
		GameRegistry.registerItem(ingot_bscco, ingot_bscco.getUnlocalizedName());
		GameRegistry.registerItem(ingot_lead, ingot_lead.getUnlocalizedName());
		GameRegistry.registerItem(ingot_silver, ingot_silver.getUnlocalizedName());
		GameRegistry.registerItem(ingot_bismuth, ingot_bismuth.getUnlocalizedName());
		GameRegistry.registerItem(ingot_arsenic, ingot_arsenic.getUnlocalizedName());
		GameRegistry.registerItem(ingot_calcium, ingot_calcium.getUnlocalizedName());
		GameRegistry.registerItem(ingot_cadmium, ingot_cadmium.getUnlocalizedName());
		GameRegistry.registerItem(ingot_tantalium, ingot_tantalium.getUnlocalizedName());
		GameRegistry.registerItem(ingot_silicon, ingot_silicon.getUnlocalizedName());
		GameRegistry.registerItem(ingot_niobium, ingot_niobium.getUnlocalizedName());
		GameRegistry.registerItem(ingot_beryllium, ingot_beryllium.getUnlocalizedName());
		GameRegistry.registerItem(ingot_cobalt, ingot_cobalt.getUnlocalizedName());
		GameRegistry.registerItem(ingot_boron, ingot_boron.getUnlocalizedName());
		GameRegistry.registerItem(ingot_graphite, ingot_graphite.getUnlocalizedName());
		GameRegistry.registerItem(ingot_firebrick, ingot_firebrick.getUnlocalizedName());
		GameRegistry.registerItem(ingot_dura_steel, ingot_dura_steel.getUnlocalizedName());
		GameRegistry.registerItem(ingot_polymer, ingot_polymer.getUnlocalizedName());
		GameRegistry.registerItem(ingot_bakelite, ingot_bakelite.getUnlocalizedName());
		GameRegistry.registerItem(ingot_biorubber, ingot_biorubber.getUnlocalizedName());
		GameRegistry.registerItem(ingot_rubber, ingot_rubber.getUnlocalizedName());
		GameRegistry.registerItem(ingot_pc, ingot_pc.getUnlocalizedName());
		GameRegistry.registerItem(ingot_pvc, ingot_pvc.getUnlocalizedName());
		//GameRegistry.registerItem(ingot_mud, ingot_mud.getUnlocalizedName());
		GameRegistry.registerItem(ingot_cft, ingot_cft.getUnlocalizedName());
		GameRegistry.registerItem(ingot_schraranium, ingot_schraranium.getUnlocalizedName());
		GameRegistry.registerItem(ingot_schrabidium, ingot_schrabidium.getUnlocalizedName());
		GameRegistry.registerItem(ingot_schrabidate, ingot_schrabidate.getUnlocalizedName());
		GameRegistry.registerItem(ingot_magnetized_tungsten, ingot_magnetized_tungsten.getUnlocalizedName());
		GameRegistry.registerItem(ingot_combine_steel, ingot_combine_steel.getUnlocalizedName());
		GameRegistry.registerItem(ingot_solinium, ingot_solinium.getUnlocalizedName());
		GameRegistry.registerItem(ingot_gh336, ingot_gh336.getUnlocalizedName());
		GameRegistry.registerItem(ingot_cn989, ingot_cn989.getUnlocalizedName());
		GameRegistry.registerItem(ingot_uranium_fuel, ingot_uranium_fuel.getUnlocalizedName());
		GameRegistry.registerItem(ingot_thorium_fuel, ingot_thorium_fuel.getUnlocalizedName());
		GameRegistry.registerItem(nugget_protactinium, nugget_protactinium.getUnlocalizedName());
		GameRegistry.registerItem(ingot_plutonium_fuel, ingot_plutonium_fuel.getUnlocalizedName());
		GameRegistry.registerItem(ingot_neptunium_fuel, ingot_neptunium_fuel.getUnlocalizedName());
		GameRegistry.registerItem(ingot_mox_fuel, ingot_mox_fuel.getUnlocalizedName());
		GameRegistry.registerItem(ingot_americium_fuel, ingot_americium_fuel.getUnlocalizedName());
		GameRegistry.registerItem(ingot_schrabidium_fuel, ingot_schrabidium_fuel.getUnlocalizedName());
		GameRegistry.registerItem(ingot_hes, ingot_hes.getUnlocalizedName());
		GameRegistry.registerItem(ingot_les, ingot_les.getUnlocalizedName());
		GameRegistry.registerItem(ingot_australium, ingot_australium.getUnlocalizedName());
		GameRegistry.registerItem(ingot_weidanium, ingot_weidanium.getUnlocalizedName());
		GameRegistry.registerItem(ingot_reiium, ingot_reiium.getUnlocalizedName());
		GameRegistry.registerItem(ingot_unobtainium, ingot_unobtainium.getUnlocalizedName());
		GameRegistry.registerItem(ingot_daffergon, ingot_daffergon.getUnlocalizedName());
		GameRegistry.registerItem(ingot_verticium, ingot_verticium.getUnlocalizedName());
		GameRegistry.registerItem(ingot_lanthanium, ingot_lanthanium.getUnlocalizedName());
		GameRegistry.registerItem(ingot_actinium, ingot_actinium.getUnlocalizedName());
		GameRegistry.registerItem(francium_ingot, francium_ingot.getUnlocalizedName());
		//a bigger disappointment than concord
		GameRegistry.registerItem(ingot_desh, ingot_desh.getUnlocalizedName());
		GameRegistry.registerItem(ingot_ferrouranium, ingot_ferrouranium.getUnlocalizedName());
		GameRegistry.registerItem(ingot_starmetal, ingot_starmetal.getUnlocalizedName());
		GameRegistry.registerItem(ingot_gunmetal, ingot_gunmetal.getUnlocalizedName());
		GameRegistry.registerItem(strontium_sulfide, strontium_sulfide.getUnlocalizedName());
		GameRegistry.registerItem(scandium_oxide, scandium_oxide.getUnlocalizedName());
		GameRegistry.registerItem(yttrium_oxide, yttrium_oxide.getUnlocalizedName());
		GameRegistry.registerItem(yttrium_chloride, yttrium_chloride.getUnlocalizedName());
		GameRegistry.registerItem(REE_sludge, REE_sludge.getUnlocalizedName());
		GameRegistry.registerItem(scandium_chloride, scandium_chloride.getUnlocalizedName());
		GameRegistry.registerItem(scandium_ingot, scandium_ingot.getUnlocalizedName());
		GameRegistry.registerItem(ingot_indium, ingot_indium.getUnlocalizedName());
		GameRegistry.registerItem(ingot_thallium, ingot_thallium.getUnlocalizedName());
		GameRegistry.registerItem(powder_indium, powder_indium.getUnlocalizedName());
		GameRegistry.registerItem(powder_thallium, powder_thallium.getUnlocalizedName());
		GameRegistry.registerItem(nugget_indium, nugget_indium.getUnlocalizedName());
		GameRegistry.registerItem(nugget_thallium, nugget_thallium.getUnlocalizedName());
		GameRegistry.registerItem(crystal_zinc, crystal_zinc.getUnlocalizedName());
		GameRegistry.registerItem(scandium_nugget, scandium_nugget.getUnlocalizedName());
		GameRegistry.registerItem(yttrium_ingot, yttrium_ingot.getUnlocalizedName());
		GameRegistry.registerItem(powder_strontium_oxide, powder_strontium_oxide.getUnlocalizedName());
		GameRegistry.registerItem(strontium_chloride, strontium_chloride.getUnlocalizedName());
		GameRegistry.registerItem(magnesium_ingot, magnesium_ingot.getUnlocalizedName());
		GameRegistry.registerItem(magnesium_nugget, magnesium_nugget.getUnlocalizedName());
		GameRegistry.registerItem(slagingot, slagingot.getUnlocalizedName());
		GameRegistry.registerItem(ingot_weaponsteel, ingot_weaponsteel.getUnlocalizedName());
		GameRegistry.registerItem(ingot_saturnite, ingot_saturnite.getUnlocalizedName());
		GameRegistry.registerItem(ingot_euphemium, ingot_euphemium.getUnlocalizedName());
		GameRegistry.registerItem(ingot_dineutronium, ingot_dineutronium.getUnlocalizedName());
		GameRegistry.registerItem(ingot_electronium, ingot_electronium.getUnlocalizedName());
		GameRegistry.registerItem(ingot_gwenium, ingot_gwenium.getUnlocalizedName());
		GameRegistry.registerItem(ingot_smore, ingot_smore.getUnlocalizedName());
		GameRegistry.registerItem(ingot_osmiridium, ingot_osmiridium.getUnlocalizedName());
		GameRegistry.registerItem(ingot_hafnium, ingot_hafnium.getUnlocalizedName());


		//Meteorite Ingots
		GameRegistry.registerItem(ingot_steel_dusted, ingot_steel_dusted.getUnlocalizedName());
		GameRegistry.registerItem(ingot_chainsteel, ingot_chainsteel.getUnlocalizedName());
		//GameRegistry.registerItem(ingot_meteorite, ingot_meteorite.getUnlocalizedName());
		//GameRegistry.registerItem(ingot_meteorite_forged, ingot_meteorite_forged.getUnlocalizedName());
		//GameRegistry.registerItem(blade_meteorite, blade_meteorite.getUnlocalizedName());

		//Misc Ingots
		GameRegistry.registerItem(ingot_phosphorus, ingot_phosphorus.getUnlocalizedName());
		GameRegistry.registerItem(lithium, lithium.getUnlocalizedName());
		GameRegistry.registerItem(ingot_zirconium, ingot_zirconium.getUnlocalizedName());
		GameRegistry.registerItem(ingot_semtex, ingot_semtex.getUnlocalizedName());
		GameRegistry.registerItem(ingot_c4, ingot_c4.getUnlocalizedName());
		GameRegistry.registerItem(oil_tar, oil_tar.getUnlocalizedName());
		GameRegistry.registerItem(solid_fuel, solid_fuel.getUnlocalizedName());
		GameRegistry.registerItem(solid_fuel_presto, solid_fuel_presto.getUnlocalizedName());
		GameRegistry.registerItem(solid_fuel_presto_triplet, solid_fuel_presto_triplet.getUnlocalizedName());
		GameRegistry.registerItem(solid_fuel_bf, solid_fuel_bf.getUnlocalizedName());
		GameRegistry.registerItem(solid_fuel_presto_bf, solid_fuel_presto_bf.getUnlocalizedName());
		GameRegistry.registerItem(solid_fuel_presto_triplet_bf, solid_fuel_presto_triplet_bf.getUnlocalizedName());
		GameRegistry.registerItem(rocket_fuel, rocket_fuel.getUnlocalizedName());
		GameRegistry.registerItem(ingot_fiberglass, ingot_fiberglass.getUnlocalizedName());
		GameRegistry.registerItem(ingot_asbestos, ingot_asbestos.getUnlocalizedName());
		GameRegistry.registerItem(ingot_raw, ingot_raw.getUnlocalizedName());
		GameRegistry.registerItem(casing, casing.getUnlocalizedName());

		//Billets
		GameRegistry.registerItem(billet_uranium, billet_uranium.getUnlocalizedName());
		GameRegistry.registerItem(billet_u233, billet_u233.getUnlocalizedName());
		GameRegistry.registerItem(billet_u235, billet_u235.getUnlocalizedName());
		GameRegistry.registerItem(billet_u238, billet_u238.getUnlocalizedName());
		GameRegistry.registerItem(billet_th232, billet_th232.getUnlocalizedName());
		GameRegistry.registerItem(billet_plutonium, billet_plutonium.getUnlocalizedName());
		GameRegistry.registerItem(billet_pu238, billet_pu238.getUnlocalizedName());
		GameRegistry.registerItem(billet_tm170, billet_tm170.getUnlocalizedName());
		GameRegistry.registerItem(billet_pm, billet_pm.getUnlocalizedName());
		GameRegistry.registerItem(billet_pu239, billet_pu239.getUnlocalizedName());
		GameRegistry.registerItem(billet_pu240, billet_pu240.getUnlocalizedName());
		GameRegistry.registerItem(billet_pu241, billet_pu241.getUnlocalizedName());
		GameRegistry.registerItem(billet_pu_mix, billet_pu_mix.getUnlocalizedName());
		GameRegistry.registerItem(billet_am241, billet_am241.getUnlocalizedName());
		GameRegistry.registerItem(billet_am242, billet_am242.getUnlocalizedName());
		GameRegistry.registerItem(billet_am_mix, billet_am_mix.getUnlocalizedName());
		GameRegistry.registerItem(billet_bk247, billet_bk247.getUnlocalizedName());
		GameRegistry.registerItem(billet_cm242, billet_cm242.getUnlocalizedName());
		GameRegistry.registerItem(billet_cm243, billet_cm243.getUnlocalizedName());
		GameRegistry.registerItem(billet_cm244, billet_cm244.getUnlocalizedName());
		GameRegistry.registerItem(billet_cm245, billet_cm245.getUnlocalizedName());
		GameRegistry.registerItem(billet_cm246, billet_cm246.getUnlocalizedName());
		GameRegistry.registerItem(billet_cm247, billet_cm247.getUnlocalizedName());
		GameRegistry.registerItem(billet_cm248, billet_cm248.getUnlocalizedName());
		GameRegistry.registerItem(billet_cm_fuel, billet_cm_fuel.getUnlocalizedName());
		GameRegistry.registerItem(billet_cm_mix, billet_cm_mix.getUnlocalizedName());
		GameRegistry.registerItem(billet_cf251, billet_cf251.getUnlocalizedName());
		GameRegistry.registerItem(billet_cf252, billet_cf252.getUnlocalizedName());
		GameRegistry.registerItem(billet_es253, billet_es253.getUnlocalizedName());
		GameRegistry.registerItem(billet_cn989, billet_cn989.getUnlocalizedName());
		GameRegistry.registerItem(billet_neptunium, billet_neptunium.getUnlocalizedName());
		GameRegistry.registerItem(billet_polonium, billet_polonium.getUnlocalizedName());
		GameRegistry.registerItem(billet_technetium, billet_technetium.getUnlocalizedName());
		GameRegistry.registerItem(billet_cobalt, billet_cobalt.getUnlocalizedName());
		GameRegistry.registerItem(billet_co60, billet_co60.getUnlocalizedName());
		GameRegistry.registerItem(billet_sr90, billet_sr90.getUnlocalizedName());
		GameRegistry.registerItem(billet_au198, billet_au198.getUnlocalizedName());
		GameRegistry.registerItem(billet_pb209, billet_pb209.getUnlocalizedName());
		GameRegistry.registerItem(billet_ra226, billet_ra226.getUnlocalizedName());
		GameRegistry.registerItem(billet_actinium, billet_actinium.getUnlocalizedName());
		GameRegistry.registerItem(billet_schrabidium, billet_schrabidium.getUnlocalizedName());
		GameRegistry.registerItem(billet_solinium, billet_solinium.getUnlocalizedName());
		GameRegistry.registerItem(billet_gh336, billet_gh336.getUnlocalizedName());
		GameRegistry.registerItem(billet_gaas, billet_gaas.getUnlocalizedName());
		GameRegistry.registerItem(billet_australium, billet_australium.getUnlocalizedName());
		GameRegistry.registerItem(billet_australium_lesser, billet_australium_lesser.getUnlocalizedName());
		GameRegistry.registerItem(billet_australium_greater, billet_australium_greater.getUnlocalizedName());
		GameRegistry.registerItem(billet_uranium_fuel, billet_uranium_fuel.getUnlocalizedName());
		GameRegistry.registerItem(billet_thorium_fuel, billet_thorium_fuel.getUnlocalizedName());
		GameRegistry.registerItem(billet_plutonium_fuel, billet_plutonium_fuel.getUnlocalizedName());
		GameRegistry.registerItem(billet_neptunium_fuel, billet_neptunium_fuel.getUnlocalizedName());
		GameRegistry.registerItem(billet_mox_fuel, billet_mox_fuel.getUnlocalizedName());
		GameRegistry.registerItem(billet_americium_fuel, billet_americium_fuel.getUnlocalizedName());
		GameRegistry.registerItem(billet_les, billet_les.getUnlocalizedName());
		GameRegistry.registerItem(billet_schrabidium_fuel, billet_schrabidium_fuel.getUnlocalizedName());
		GameRegistry.registerItem(billet_hes, billet_hes.getUnlocalizedName());
		GameRegistry.registerItem(billet_po210be, billet_po210be.getUnlocalizedName());
		GameRegistry.registerItem(billet_ra226be, billet_ra226be.getUnlocalizedName());
		GameRegistry.registerItem(billet_pu238be, billet_pu238be.getUnlocalizedName());
		GameRegistry.registerItem(billet_beryllium, billet_beryllium.getUnlocalizedName());
		GameRegistry.registerItem(billet_bismuth, billet_bismuth.getUnlocalizedName());
		GameRegistry.registerItem(billet_silicon, billet_silicon.getUnlocalizedName());
		GameRegistry.registerItem(billet_zirconium, billet_zirconium.getUnlocalizedName());
		GameRegistry.registerItem(billet_zfb_bismuth, billet_zfb_bismuth.getUnlocalizedName());
		GameRegistry.registerItem(billet_zfb_pu241, billet_zfb_pu241.getUnlocalizedName());
		GameRegistry.registerItem(billet_zfb_am_mix, billet_zfb_am_mix.getUnlocalizedName());
		GameRegistry.registerItem(billet_yharonite, billet_yharonite.getUnlocalizedName());
		GameRegistry.registerItem(billet_balefire_gold, billet_balefire_gold.getUnlocalizedName());
		GameRegistry.registerItem(billet_flashlead, billet_flashlead.getUnlocalizedName());
		GameRegistry.registerItem(billet_nuclear_waste, billet_nuclear_waste.getUnlocalizedName());

		//Dusts & Other
		GameRegistry.registerItem(cinnebar, cinnebar.getUnlocalizedName());
		GameRegistry.registerItem(nugget_mercury, nugget_mercury.getUnlocalizedName());
		GameRegistry.registerItem(nugget_ruthenium, nugget_ruthenium.getUnlocalizedName());
		GameRegistry.registerItem(ingot_mercury, ingot_mercury.getUnlocalizedName());
		GameRegistry.registerItem(bottle_mercury, bottle_mercury.getUnlocalizedName());
		GameRegistry.registerItem(coke, coke.getUnlocalizedName());
		GameRegistry.registerItem(powder_coke, powder_coke.getUnlocalizedName());
		GameRegistry.registerItem(powder_celestite, powder_celestite.getUnlocalizedName());
		GameRegistry.registerItem(lignite, lignite.getUnlocalizedName());
		GameRegistry.registerItem(coal_infernal, coal_infernal.getUnlocalizedName());
		GameRegistry.registerItem(briquette, briquette.getUnlocalizedName());
		//GameRegistry.registerItem(woodemium_briquette, woodemium_briquette.getUnlocalizedName());
		GameRegistry.registerItem(sulfur, sulfur.getUnlocalizedName());
		GameRegistry.registerItem(gypsum, gypsum.getUnlocalizedName());
		GameRegistry.registerItem(niter, niter.getUnlocalizedName());
		GameRegistry.registerItem(ammonium_chloride, ammonium_chloride.getUnlocalizedName());
		GameRegistry.registerItem(sodium_nitrate, sodium_nitrate.getUnlocalizedName());
		GameRegistry.registerItem(nitra, nitra.getUnlocalizedName());
		GameRegistry.registerItem(nitra_small, nitra_small.getUnlocalizedName());
		GameRegistry.registerItem(fluorite, fluorite.getUnlocalizedName());
		GameRegistry.registerItem(powder_coal, powder_coal.getUnlocalizedName());
		GameRegistry.registerItem(powder_coal_tiny, powder_coal_tiny.getUnlocalizedName());
		GameRegistry.registerItem(powder_iron, powder_iron.getUnlocalizedName());
		GameRegistry.registerItem(powder_gold, powder_gold.getUnlocalizedName());
		GameRegistry.registerItem(powder_lapis, powder_lapis.getUnlocalizedName());
		GameRegistry.registerItem(powder_quartz, powder_quartz.getUnlocalizedName());
		GameRegistry.registerItem(powder_diamond, powder_diamond.getUnlocalizedName());
		GameRegistry.registerItem(powder_emerald, powder_emerald.getUnlocalizedName());
		GameRegistry.registerItem(powder_uranium, powder_uranium.getUnlocalizedName());
		GameRegistry.registerItem(powder_plutonium, powder_plutonium.getUnlocalizedName());
		GameRegistry.registerItem(powder_columbite, powder_columbite.getUnlocalizedName());
		GameRegistry.registerItem(powder_neptunium, powder_neptunium.getUnlocalizedName());
		GameRegistry.registerItem(powder_polonium, powder_polonium.getUnlocalizedName());
		GameRegistry.registerItem(powder_co60, powder_co60.getUnlocalizedName());
		GameRegistry.registerItem(powder_sr90, powder_sr90.getUnlocalizedName());
		GameRegistry.registerItem(powder_sr90_tiny, powder_sr90_tiny.getUnlocalizedName());
		GameRegistry.registerItem(powder_i131, powder_i131.getUnlocalizedName());
		GameRegistry.registerItem(powder_i131_tiny, powder_i131_tiny.getUnlocalizedName());
		GameRegistry.registerItem(powder_xe135, powder_xe135.getUnlocalizedName());
		GameRegistry.registerItem(powder_xe135_tiny, powder_xe135_tiny.getUnlocalizedName());
		GameRegistry.registerItem(powder_cs137, powder_cs137.getUnlocalizedName());
		GameRegistry.registerItem(powder_cs137_tiny, powder_cs137_tiny.getUnlocalizedName());
		GameRegistry.registerItem(powder_cn989, powder_cn989.getUnlocalizedName());
		GameRegistry.registerItem(powder_au198, powder_au198.getUnlocalizedName());
		GameRegistry.registerItem(powder_ra226, powder_ra226.getUnlocalizedName());
		GameRegistry.registerItem(powder_at209, powder_at209.getUnlocalizedName());
		GameRegistry.registerItem(powder_titanium, powder_titanium.getUnlocalizedName());
		GameRegistry.registerItem(powder_chromium, powder_chromium.getUnlocalizedName());
		GameRegistry.registerItem(powder_zircon, powder_zircon.getUnlocalizedName());
		GameRegistry.registerItem(powder_nickel, powder_nickel.getUnlocalizedName());
		GameRegistry.registerItem(powder_copper, powder_copper.getUnlocalizedName());
		GameRegistry.registerItem(powder_zinc, powder_zinc.getUnlocalizedName());
		GameRegistry.registerItem(powder_platnium, powder_platnium.getUnlocalizedName());
		GameRegistry.registerItem(ingot_germanium, ingot_germanium.getUnlocalizedName());
		GameRegistry.registerItem(nugget_germanium, nugget_germanium.getUnlocalizedName());
		GameRegistry.registerItem(powder_rhodium, powder_rhodium.getUnlocalizedName());
		GameRegistry.registerItem(powder_rhodium_solution, powder_rhodium_solution.getUnlocalizedName());
		GameRegistry.registerItem(ingot_rhodium, ingot_rhodium.getUnlocalizedName());
		GameRegistry.registerItem(powder_palladium, powder_palladium.getUnlocalizedName());
		GameRegistry.registerItem(powder_samarium, powder_samarium.getUnlocalizedName());
		GameRegistry.registerItem(powder_gallium_tiny, powder_gallium_tiny.getUnlocalizedName());
		GameRegistry.registerItem(powder_gallium, powder_gallium.getUnlocalizedName());
		GameRegistry.registerItem(powder_germanium, powder_germanium.getUnlocalizedName());
		GameRegistry.registerItem(powder_red_copper, powder_red_copper.getUnlocalizedName());
		GameRegistry.registerItem(powder_advanced_alloy, powder_advanced_alloy.getUnlocalizedName());
		GameRegistry.registerItem(powder_tungsten, powder_tungsten.getUnlocalizedName());
		GameRegistry.registerItem(powder_aluminium, powder_aluminium.getUnlocalizedName());
		GameRegistry.registerItem(powder_steel, powder_steel.getUnlocalizedName());
		GameRegistry.registerItem(powder_steel_tiny, powder_steel_tiny.getUnlocalizedName());
		GameRegistry.registerItem(powder_tcalloy, powder_tcalloy.getUnlocalizedName());
		GameRegistry.registerItem(powder_lead, powder_lead.getUnlocalizedName());
		GameRegistry.registerItem(powder_silver, powder_silver.getUnlocalizedName());
		GameRegistry.registerItem(powder_bismuth, powder_bismuth.getUnlocalizedName());
		GameRegistry.registerItem(powder_potash, powder_potash.getUnlocalizedName());
		GameRegistry.registerItem(powder_pollucite, powder_pollucite.getUnlocalizedName());
		GameRegistry.registerItem(powder_osmium, powder_osmium.getUnlocalizedName());
		GameRegistry.registerItem(powder_calcium, powder_calcium.getUnlocalizedName());
		GameRegistry.registerItem(powder_cadmium, powder_cadmium.getUnlocalizedName());
		GameRegistry.registerItem(powder_coltan_ore, powder_coltan_ore.getUnlocalizedName());
		GameRegistry.registerItem(powder_coltan, powder_coltan.getUnlocalizedName());
		GameRegistry.registerItem(powder_tantalium, powder_tantalium.getUnlocalizedName());
		GameRegistry.registerItem(powder_tektite, powder_tektite.getUnlocalizedName());
		GameRegistry.registerItem(powder_paleogenite, powder_paleogenite.getUnlocalizedName());
		GameRegistry.registerItem(powder_paleogenite_tiny, powder_paleogenite_tiny.getUnlocalizedName());
		GameRegistry.registerItem(powder_osmium_tiny, powder_osmium_tiny.getUnlocalizedName());
		GameRegistry.registerItem(powder_impure_osmiridium, powder_impure_osmiridium.getUnlocalizedName());
		GameRegistry.registerItem(iridium_rich_residue, iridium_rich_residue.getUnlocalizedName());
		GameRegistry.registerItem(iridium_oxide, iridium_oxide.getUnlocalizedName());
		GameRegistry.registerItem(powder_borax, powder_borax.getUnlocalizedName());
		GameRegistry.registerItem(powder_chlorocalcite, powder_chlorocalcite.getUnlocalizedName());
		GameRegistry.registerItem(powder_molysite, powder_molysite.getUnlocalizedName());
		GameRegistry.registerItem(powder_antimony_trioxide, powder_antimony_trioxide.getUnlocalizedName());
		GameRegistry.registerItem(powder_yellowcake, powder_yellowcake.getUnlocalizedName());
		GameRegistry.registerItem(powder_beryllium, powder_beryllium.getUnlocalizedName());
		GameRegistry.registerItem(powder_dura_steel, powder_dura_steel.getUnlocalizedName());
		GameRegistry.registerItem(powder_polymer, powder_polymer.getUnlocalizedName());
		GameRegistry.registerItem(powder_bakelite, powder_bakelite.getUnlocalizedName());
		GameRegistry.registerItem(powder_schrabidium, powder_schrabidium.getUnlocalizedName());
		GameRegistry.registerItem(powder_schrabidate, powder_schrabidate.getUnlocalizedName());
		GameRegistry.registerItem(powder_magnetized_tungsten, powder_magnetized_tungsten.getUnlocalizedName());
		GameRegistry.registerItem(powder_chlorophyte, powder_chlorophyte.getUnlocalizedName());
		GameRegistry.registerItem(powder_combine_steel, powder_combine_steel.getUnlocalizedName());
		GameRegistry.registerItem(powder_lithium, powder_lithium.getUnlocalizedName());
		GameRegistry.registerItem(powder_lithium_tiny, powder_lithium_tiny.getUnlocalizedName());
		GameRegistry.registerItem(titanium_trace_metals_slurry, titanium_trace_metals_slurry.getUnlocalizedName());
		GameRegistry.registerItem(powder_zirconium, powder_zirconium.getUnlocalizedName());
		GameRegistry.registerItem(powder_sodium, powder_sodium.getUnlocalizedName());
		GameRegistry.registerItem(powder_sodium_silicate, powder_sodium_silicate.getUnlocalizedName());
		GameRegistry.registerItem(powder_lignite, powder_lignite.getUnlocalizedName());
		GameRegistry.registerItem(powder_iodine, powder_iodine.getUnlocalizedName());
		GameRegistry.registerItem(powder_thorium, powder_thorium.getUnlocalizedName());
		GameRegistry.registerItem(powder_neodymium, powder_neodymium.getUnlocalizedName());
		GameRegistry.registerItem(powder_neodymium_tiny, powder_neodymium_tiny.getUnlocalizedName());
		GameRegistry.registerItem(powder_astatine, powder_astatine.getUnlocalizedName());
		GameRegistry.registerItem(powder_caesium, powder_caesium.getUnlocalizedName());
		GameRegistry.registerItem(cesium_salt, cesium_salt.getUnlocalizedName());
		GameRegistry.registerItem(arsenic_trioxide, arsenic_trioxide.getUnlocalizedName());
		GameRegistry.registerItem(powder_selenium, powder_selenium.getUnlocalizedName());
		GameRegistry.registerItem(powder_tellurium_dioxide, powder_tellurium_dioxide.getUnlocalizedName());
		GameRegistry.registerItem(powder_tellurium, powder_tellurium.getUnlocalizedName());
		GameRegistry.registerItem(nugget_tellurium, nugget_tellurium.getUnlocalizedName());
		GameRegistry.registerItem(ingot_cesium, ingot_cesium.getUnlocalizedName());
		GameRegistry.registerItem(powder_australium, powder_australium.getUnlocalizedName());
		GameRegistry.registerItem(powder_weidanium, powder_weidanium.getUnlocalizedName());
		GameRegistry.registerItem(powder_reiium, powder_reiium.getUnlocalizedName());
		GameRegistry.registerItem(powder_unobtainium, powder_unobtainium.getUnlocalizedName());
		GameRegistry.registerItem(powder_daffergon, powder_daffergon.getUnlocalizedName());
		GameRegistry.registerItem(powder_verticium, powder_verticium.getUnlocalizedName());
		GameRegistry.registerItem(powder_strontium, powder_strontium.getUnlocalizedName());
		GameRegistry.registerItem(powder_cobalt, powder_cobalt.getUnlocalizedName());
		GameRegistry.registerItem(powder_cobalt_tiny, powder_cobalt_tiny.getUnlocalizedName());
		GameRegistry.registerItem(powder_alien_tiny, powder_alien_tiny.getUnlocalizedName());
		GameRegistry.registerItem(powder_bromine, powder_bromine.getUnlocalizedName());
		GameRegistry.registerItem(powder_niobium, powder_niobium.getUnlocalizedName());
		GameRegistry.registerItem(powder_niobium_tiny, powder_niobium_tiny.getUnlocalizedName());
		GameRegistry.registerItem(powder_tennessine, powder_tennessine.getUnlocalizedName());
		GameRegistry.registerItem(powder_cerium, powder_cerium.getUnlocalizedName());
		GameRegistry.registerItem(powder_cerium_tiny, powder_cerium_tiny.getUnlocalizedName());
		GameRegistry.registerItem(powder_lanthanium, powder_lanthanium.getUnlocalizedName());
		GameRegistry.registerItem(powder_yttrium, powder_yttrium.getUnlocalizedName());
		GameRegistry.registerItem(powder_lanthanium_tiny, powder_lanthanium_tiny.getUnlocalizedName());
		GameRegistry.registerItem(powder_actinium, powder_actinium.getUnlocalizedName());
		GameRegistry.registerItem(powder_actinium_tiny, powder_actinium_tiny.getUnlocalizedName());
		GameRegistry.registerItem(powder_praseodymium_tiny, powder_praseodymium_tiny.getUnlocalizedName());
		GameRegistry.registerItem(powder_yttrium_tiny, powder_yttrium_tiny.getUnlocalizedName());
		GameRegistry.registerItem(powder_boron, powder_boron.getUnlocalizedName());
		GameRegistry.registerItem(powder_boron_tiny, powder_boron_tiny.getUnlocalizedName());
		GameRegistry.registerItem(powder_asbestos, powder_asbestos.getUnlocalizedName());
		GameRegistry.registerItem(gem_ruby, gem_ruby.getUnlocalizedName());
		GameRegistry.registerItem(gem_sapphire, gem_sapphire.getUnlocalizedName());
		GameRegistry.registerItem(powder_magic, powder_magic.getUnlocalizedName());
		GameRegistry.registerItem(powder_sawdust, powder_sawdust.getUnlocalizedName());
		GameRegistry.registerItem(powder_flux, powder_flux.getUnlocalizedName());
		GameRegistry.registerItem(powder_fertilizer, powder_fertilizer.getUnlocalizedName());
		GameRegistry.registerItem(powder_balefire, powder_balefire.getUnlocalizedName());
		GameRegistry.registerItem(powder_semtex_mix, powder_semtex_mix.getUnlocalizedName());
		GameRegistry.registerItem(powder_desh_mix, powder_desh_mix.getUnlocalizedName());
		GameRegistry.registerItem(powder_desh_ready, powder_desh_ready.getUnlocalizedName());
		GameRegistry.registerItem(powder_desh, powder_desh.getUnlocalizedName());
		GameRegistry.registerItem(powder_nitan_mix, powder_nitan_mix.getUnlocalizedName());
		GameRegistry.registerItem(powder_spark_mix, powder_spark_mix.getUnlocalizedName());
		//GameRegistry.registerItem(powder_meteorite, powder_meteorite.getUnlocalizedName());
		GameRegistry.registerItem(powder_meteorite_tiny, powder_meteorite_tiny.getUnlocalizedName());
		GameRegistry.registerItem(powder_vanadium_tiny, powder_vanadium_tiny.getUnlocalizedName());
		GameRegistry.registerItem(powder_euphemium, powder_euphemium.getUnlocalizedName());
		//GameRegistry.registerItem(powder_wd2004_tiny, powder_wd2004_tiny.getUnlocalizedName());
		//GameRegistry.registerItem(powder_wd2004, powder_wd2004.getUnlocalizedName());
		GameRegistry.registerItem(powder_dineutronium, powder_dineutronium.getUnlocalizedName());
		GameRegistry.registerItem(redstone_depleted, redstone_depleted.getUnlocalizedName());
		GameRegistry.registerItem(dust, dust.getUnlocalizedName());
		GameRegistry.registerItem(dust_tiny, dust_tiny.getUnlocalizedName());
		GameRegistry.registerItem(fallout, fallout.getUnlocalizedName());
		GameRegistry.registerItem(powder_ash, powder_ash.getUnlocalizedName());
		GameRegistry.registerItem(powder_limestone, powder_limestone.getUnlocalizedName());
		GameRegistry.registerItem(powder_cement, powder_cement.getUnlocalizedName());
		GameRegistry.registerItem(squirrel, squirrel.getUnlocalizedName());
		GameRegistry.registerItem(cooked_squirrel, cooked_squirrel.getUnlocalizedName());

		GameRegistry.registerItem(potassium_iodide_powder, potassium_iodide_powder.getUnlocalizedName());
		GameRegistry.registerItem(potassium_powder, potassium_powder.getUnlocalizedName());


		//Powders
		GameRegistry.registerItem(powder_fire, powder_fire.getUnlocalizedName());
		GameRegistry.registerItem(powder_ice, powder_ice.getUnlocalizedName());
		GameRegistry.registerItem(powder_poison, powder_poison.getUnlocalizedName());
		GameRegistry.registerItem(powder_thermite, powder_thermite.getUnlocalizedName());
		GameRegistry.registerItem(powder_power, powder_power.getUnlocalizedName());
		GameRegistry.registerItem(manganese_powder, manganese_powder.getUnlocalizedName());
		GameRegistry.registerItem(cordite, cordite.getUnlocalizedName());
		GameRegistry.registerItem(ammonium_nitrate, ammonium_nitrate.getUnlocalizedName());
		GameRegistry.registerItem(ballistite, ballistite.getUnlocalizedName());
		GameRegistry.registerItem(ball_dynamite, ball_dynamite.getUnlocalizedName());
		GameRegistry.registerItem(ball_tnt, ball_tnt.getUnlocalizedName());
		GameRegistry.registerItem(ball_tatb, ball_tatb.getUnlocalizedName());
		GameRegistry.registerItem(ball_resin, ball_resin.getUnlocalizedName());
		GameRegistry.registerItem(ball_fireclay, ball_fireclay.getUnlocalizedName());
		GameRegistry.registerItem(ball_ferric_clay, ball_ferric_clay.getUnlocalizedName());

		//Ores
		GameRegistry.registerItem(ore_bedrock, ore_bedrock.getUnlocalizedName());
		GameRegistry.registerItem(bedrock_ore_fragment, bedrock_ore_fragment.getUnlocalizedName());

		GameRegistry.registerItem(ore_centrifuged, ore_centrifuged.getUnlocalizedName());
		GameRegistry.registerItem(ore_cleaned, ore_cleaned.getUnlocalizedName());
		GameRegistry.registerItem(ore_separated, ore_separated.getUnlocalizedName());
		GameRegistry.registerItem(ore_purified, ore_purified.getUnlocalizedName());
		GameRegistry.registerItem(ore_nitrated, ore_nitrated.getUnlocalizedName());
		GameRegistry.registerItem(ore_nitrocrystalline, ore_nitrocrystalline.getUnlocalizedName());
		GameRegistry.registerItem(ore_deepcleaned, ore_deepcleaned.getUnlocalizedName());
		GameRegistry.registerItem(ore_seared, ore_seared.getUnlocalizedName());
		GameRegistry.registerItem(ore_enriched, ore_enriched.getUnlocalizedName());
		GameRegistry.registerItem(ore_byproduct, ore_byproduct.getUnlocalizedName());
		GameRegistry.registerItem(bedrock_ore_base, bedrock_ore_base.getUnlocalizedName());
		GameRegistry.registerItem(bedrock_ore, bedrock_ore.getUnlocalizedName());

		GameRegistry.registerItem(crystal_mineral, crystal_mineral.getUnlocalizedName());
		GameRegistry.registerItem(crystal_cleaned, crystal_cleaned.getUnlocalizedName());
		GameRegistry.registerItem(mineral_dust, mineral_dust.getUnlocalizedName());
		GameRegistry.registerItem(mineral_fragment, mineral_fragment.getUnlocalizedName());
		//Crystals
		GameRegistry.registerItem(crystal_coal, crystal_coal.getUnlocalizedName());
		GameRegistry.registerItem(crystal_iron, crystal_iron.getUnlocalizedName());
		GameRegistry.registerItem(crystal_gold, crystal_gold.getUnlocalizedName());
		GameRegistry.registerItem(crystal_redstone, crystal_redstone.getUnlocalizedName());
		GameRegistry.registerItem(crystal_lapis, crystal_lapis.getUnlocalizedName());
		GameRegistry.registerItem(crystal_diamond, crystal_diamond.getUnlocalizedName());
		GameRegistry.registerItem(crystal_uranium, crystal_uranium.getUnlocalizedName());
		GameRegistry.registerItem(crystal_thorium, crystal_thorium.getUnlocalizedName());
		GameRegistry.registerItem(crystal_plutonium, crystal_plutonium.getUnlocalizedName());
		GameRegistry.registerItem(crystal_titanium, crystal_titanium.getUnlocalizedName());
		GameRegistry.registerItem(crystal_sulfur, crystal_sulfur.getUnlocalizedName());
		GameRegistry.registerItem(crystal_niter, crystal_niter.getUnlocalizedName());
		GameRegistry.registerItem(crystal_copper, crystal_copper.getUnlocalizedName());
		GameRegistry.registerItem(crystal_tungsten, crystal_tungsten.getUnlocalizedName());
		GameRegistry.registerItem(crystal_aluminium, crystal_aluminium.getUnlocalizedName());
		GameRegistry.registerItem(crystal_fluorite, crystal_fluorite.getUnlocalizedName());
		GameRegistry.registerItem(crystal_beryllium, crystal_beryllium.getUnlocalizedName());
		GameRegistry.registerItem(crystal_lead, crystal_lead.getUnlocalizedName());
		GameRegistry.registerItem(crystal_schraranium, crystal_schraranium.getUnlocalizedName());
		GameRegistry.registerItem(crystal_schrabidium, crystal_schrabidium.getUnlocalizedName());
		GameRegistry.registerItem(crystal_rare, crystal_rare.getUnlocalizedName());
		GameRegistry.registerItem(crystal_phosphorus, crystal_phosphorus.getUnlocalizedName());
		GameRegistry.registerItem(crystal_lithium, crystal_lithium.getUnlocalizedName());
		GameRegistry.registerItem(crystal_cobalt, crystal_cobalt.getUnlocalizedName());
		GameRegistry.registerItem(crystal_starmetal, crystal_starmetal.getUnlocalizedName());
		GameRegistry.registerItem(crystal_cinnebar, crystal_cinnebar.getUnlocalizedName());
		GameRegistry.registerItem(crystal_trixite, crystal_trixite.getUnlocalizedName());
		GameRegistry.registerItem(crystal_osmiridium, crystal_osmiridium.getUnlocalizedName());
		GameRegistry.registerItem(gem_sodalite, gem_sodalite.getUnlocalizedName());
		GameRegistry.registerItem(gem_tantalium, gem_tantalium.getUnlocalizedName());
		GameRegistry.registerItem(gem_volcanic, gem_volcanic.getUnlocalizedName());
		GameRegistry.registerItem(gem_rad, gem_rad.getUnlocalizedName());
		GameRegistry.registerItem(gem_alexandrite, gem_alexandrite.getUnlocalizedName());
		GameRegistry.registerItem(titanium_sapphire_crystal, titanium_sapphire_crystal.getUnlocalizedName());
		GameRegistry.registerItem(cerium_crystal, cerium_crystal.getUnlocalizedName());
		GameRegistry.registerItem(calcium_fluoride_crystal, calcium_fluoride_crystal.getUnlocalizedName());
		GameRegistry.registerItem(KTP_crystal, KTP_crystal.getUnlocalizedName());
		GameRegistry.registerItem(lithium_niobate_crystal, lithium_niobate_crystal.getUnlocalizedName());
		GameRegistry.registerItem(visible_optics_array, visible_optics_array.getUnlocalizedName());
		GameRegistry.registerItem(visible_dopant_crystal, visible_dopant_crystal.getUnlocalizedName());
		GameRegistry.registerItem(BBO_crystal, BBO_crystal.getUnlocalizedName());
		GameRegistry.registerItem(KDP_crystal, KDP_crystal.getUnlocalizedName());
		GameRegistry.registerItem(CLBO_crystal, CLBO_crystal.getUnlocalizedName());
		GameRegistry.registerItem(LBO_crystal, LBO_crystal.getUnlocalizedName());
		GameRegistry.registerItem(crystal_nickel, crystal_nickel.getUnlocalizedName()); //l like nickel, i dont care, fuck off
		GameRegistry.registerItem(crystal_niobium, crystal_niobium.getUnlocalizedName()); // the true path to enlightenment is ignoring the little green circle

		GameRegistry.registerItem(crystal_basaltic, crystal_basaltic.getUnlocalizedName());

		GameRegistry.registerItem(crystal_meth, crystal_meth.getUnlocalizedName());

		GameRegistry.registerItem(itemsalt, itemsalt.getUnlocalizedName());
		GameRegistry.registerItem(rubidiumsalt, rubidiumsalt.getUnlocalizedName());
		GameRegistry.registerItem(magnesium_chloride, magnesium_chloride.getUnlocalizedName());

		GameRegistry.registerItem(sodium_sulfate, sodium_sulfate.getUnlocalizedName());
		GameRegistry.registerItem(sodium_sulfide, sodium_sulfide.getUnlocalizedName());
		GameRegistry.registerItem(potassium_sulfate, potassium_sulfate.getUnlocalizedName());

		GameRegistry.registerItem(quicklime, quicklime.getUnlocalizedName());

		//Fragments
		GameRegistry.registerItem(fragment_neodymium, fragment_neodymium.getUnlocalizedName());
		GameRegistry.registerItem(ingot_neodymium, ingot_neodymium.getUnlocalizedName());
		GameRegistry.registerItem(fragment_cobalt, fragment_cobalt.getUnlocalizedName());
		GameRegistry.registerItem(fragment_alien, fragment_alien.getUnlocalizedName());
		GameRegistry.registerItem(fragment_niobium, fragment_niobium.getUnlocalizedName());
		GameRegistry.registerItem(fragment_cerium, fragment_cerium.getUnlocalizedName());
		GameRegistry.registerItem(fragment_lanthanium, fragment_lanthanium.getUnlocalizedName());
		GameRegistry.registerItem(fragment_actinium, fragment_actinium.getUnlocalizedName());
		GameRegistry.registerItem(fragment_yttrium, fragment_yttrium.getUnlocalizedName());
		GameRegistry.registerItem(fragment_boron, fragment_boron.getUnlocalizedName());
		GameRegistry.registerItem(fragment_meteorite, fragment_meteorite.getUnlocalizedName());
		GameRegistry.registerItem(fragment_coltan, fragment_coltan.getUnlocalizedName());
		GameRegistry.registerItem(fragment_praseodymium, fragment_praseodymium.getUnlocalizedName());
		GameRegistry.registerItem(fragment_samarium, fragment_samarium.getUnlocalizedName());
		GameRegistry.registerItem(fragment_europium, fragment_europium.getUnlocalizedName());
		GameRegistry.registerItem(fragment_gadolinium, fragment_gadolinium.getUnlocalizedName());
		GameRegistry.registerItem(fragment_terbium, fragment_terbium.getUnlocalizedName());
		GameRegistry.registerItem(fragment_dysprosium, fragment_dysprosium.getUnlocalizedName());
		GameRegistry.registerItem(fragment_holmium, fragment_holmium.getUnlocalizedName());
		GameRegistry.registerItem(fragment_erbium, fragment_erbium.getUnlocalizedName());
		GameRegistry.registerItem(fragment_ytterbium, fragment_ytterbium.getUnlocalizedName());
		GameRegistry.registerItem(fragment_lutetium, fragment_lutetium.getUnlocalizedName());
		GameRegistry.registerItem(fragment_thulium, fragment_thulium.getUnlocalizedName());
		GameRegistry.registerItem(powder_terbium_tiny, powder_terbium_tiny.getUnlocalizedName());
		GameRegistry.registerItem(powder_dysprosium_tiny, powder_dysprosium_tiny.getUnlocalizedName());
		GameRegistry.registerItem(powder_holmium_tiny, powder_holmium_tiny.getUnlocalizedName());
		GameRegistry.registerItem(powder_erbium_tiny, powder_erbium_tiny.getUnlocalizedName());
		GameRegistry.registerItem(powder_thulium2, powder_thulium2.getUnlocalizedName());
		GameRegistry.registerItem(powder_thulium, powder_thulium.getUnlocalizedName());
		GameRegistry.registerItem(powder_ytterbium_tiny, powder_ytterbium_tiny.getUnlocalizedName());
		GameRegistry.registerItem(powder_lutetium_tiny, powder_lutetium_tiny.getUnlocalizedName());
		GameRegistry.registerItem(powder_thulium_tiny, powder_thulium_tiny.getUnlocalizedName());
		GameRegistry.registerItem(powder_terbium, powder_terbium.getUnlocalizedName());
		GameRegistry.registerItem(powder_dysprosium_concentrate, powder_dysprosium_concentrate.getUnlocalizedName());
		GameRegistry.registerItem(powder_dysprosium, powder_dysprosium.getUnlocalizedName());
		GameRegistry.registerItem(europium_dust_tiny, europium_dust_tiny.getUnlocalizedName());
		GameRegistry.registerItem(gadolinium_dust_tiny, gadolinium_dust_tiny.getUnlocalizedName());
		GameRegistry.registerItem(gadoliniumsol, gadoliniumsol.getUnlocalizedName());
		GameRegistry.registerItem(terbiumsol, terbiumsol.getUnlocalizedName());
		GameRegistry.registerItem(dysprosiumsol, dysprosiumsol.getUnlocalizedName());
		GameRegistry.registerItem(lutetiumsol, lutetiumsol.getUnlocalizedName());
		GameRegistry.registerItem(thuliumsol, thuliumsol.getUnlocalizedName());
		GameRegistry.registerItem(powder_lutetium2, powder_lutetium2.getUnlocalizedName());
		GameRegistry.registerItem(powder_dysprosium2, powder_dysprosium2.getUnlocalizedName());
		GameRegistry.registerItem(powder_holmium_oxide, powder_holmium_oxide.getUnlocalizedName());
		GameRegistry.registerItem(powder_ytterbium_oxide, powder_ytterbium_oxide.getUnlocalizedName());
		GameRegistry.registerItem(powder_ytterbium_fluoride, powder_ytterbium_fluoride.getUnlocalizedName());
		GameRegistry.registerItem(powder_ytterbium, powder_ytterbium.getUnlocalizedName());
		GameRegistry.registerItem(powder_holmium_concentrate, powder_holmium_concentrate.getUnlocalizedName());
		GameRegistry.registerItem(powder_erbium_concentrate, powder_erbium_concentrate.getUnlocalizedName());
		GameRegistry.registerItem(powder_ytterbium_concentrate, powder_ytterbium_concentrate.getUnlocalizedName());
		GameRegistry.registerItem(powder_lutetium_concentrate, powder_lutetium_concentrate.getUnlocalizedName());
		GameRegistry.registerItem(powder_thulium_concentrate, powder_thulium_concentrate.getUnlocalizedName());
		GameRegistry.registerItem(erbium_powder, erbium_powder.getUnlocalizedName());
		GameRegistry.registerItem(er_yag_feedstock, er_yag_feedstock.getUnlocalizedName());
		GameRegistry.registerItem(yby_feedstock, yby_feedstock.getUnlocalizedName());
		GameRegistry.registerItem(znse_feedstock, znse_feedstock.getUnlocalizedName());
		GameRegistry.registerItem(er_yag_sintered, er_yag_sintered.getUnlocalizedName());
		GameRegistry.registerItem(yby_sintered, yby_sintered.getUnlocalizedName());
		GameRegistry.registerItem(yttrium_aluminum_erbium_crystal, yttrium_aluminum_erbium_crystal.getUnlocalizedName());
		//GameRegistry.registerItem(ruby_crystal, ruby_crystal.getUnlocalizedName());
		GameRegistry.registerItem(YbY_crystal, YbY_crystal.getUnlocalizedName());
		GameRegistry.registerItem(ZnSe_crystal, ZnSe_crystal.getUnlocalizedName());
		GameRegistry.registerItem(germanium_crystal, germanium_crystal.getUnlocalizedName());
		GameRegistry.registerItem(ingot_erbium, ingot_erbium.getUnlocalizedName());
		GameRegistry.registerItem(ingot_ytterbium, ingot_ytterbium.getUnlocalizedName());
		GameRegistry.registerItem(ingot_lutetium, ingot_lutetium.getUnlocalizedName());
		GameRegistry.registerItem(ingot_dysprosium, ingot_dysprosium.getUnlocalizedName());
		GameRegistry.registerItem(ingot_holmium, ingot_holmium.getUnlocalizedName());
		GameRegistry.registerItem(powder_terbium2, powder_terbium2.getUnlocalizedName());
		GameRegistry.registerItem(powder_terbium_oxide, powder_terbium_oxide.getUnlocalizedName());
		GameRegistry.registerItem(powder_terbium_fluoride, powder_terbium_fluoride.getUnlocalizedName());
		GameRegistry.registerItem(ingot_terbium_impure, ingot_terbium_impure.getUnlocalizedName());
		GameRegistry.registerItem(gadolinium_dust_step, gadolinium_dust_step.getUnlocalizedName());
		GameRegistry.registerItem(powder_gadolinium, powder_gadolinium.getUnlocalizedName());
		GameRegistry.registerItem(powder_gadolinium2, powder_gadolinium2.getUnlocalizedName());
		GameRegistry.registerItem(ingot_gadolinium, ingot_gadolinium.getUnlocalizedName());
		GameRegistry.registerItem(europiumsol, europiumsol.getUnlocalizedName());
		GameRegistry.registerItem(ingot_praseodymium, ingot_praseodymium.getUnlocalizedName());
		GameRegistry.registerItem(nugget_praseodymium, nugget_praseodymium.getUnlocalizedName());
		GameRegistry.registerItem(powder_praseodymium, powder_praseodymium.getUnlocalizedName());
		GameRegistry.registerItem(chunk_ore, chunk_ore.getUnlocalizedName());
		GameRegistry.registerItem(powder_steel_dusted, powder_steel_dusted.getUnlocalizedName());

		//anti retard logic for ores
		GameRegistry.registerItem(chunk_ironoxide, chunk_ironoxide.getUnlocalizedName());
		GameRegistry.registerItem(chunk_coppersulfide, chunk_coppersulfide.getUnlocalizedName());
		GameRegistry.registerItem(powder_vanadium, powder_vanadium.getUnlocalizedName());
		GameRegistry.registerItem(chunk_leadzincsulfide, chunk_leadzincsulfide.getUnlocalizedName());
		GameRegistry.registerItem(chunk_nickelsulfide, chunk_nickelsulfide.getUnlocalizedName());
		GameRegistry.registerItem(chunk_tintungsten, chunk_tintungsten.getUnlocalizedName());
		GameRegistry.registerItem(chunk_lithiumpegmatite, chunk_lithiumpegmatite.getUnlocalizedName());
		GameRegistry.registerItem(chunk_heavymineralsand, chunk_heavymineralsand.getUnlocalizedName());
		GameRegistry.registerItem(chunk_evaporite, chunk_evaporite.getUnlocalizedName());
		GameRegistry.registerItem(chunk_carbon, chunk_carbon.getUnlocalizedName());
		GameRegistry.registerItem(chunk_chromite, chunk_chromite.getUnlocalizedName());


		//ore groups

		//iron
		GameRegistry.registerItem(magnetite, magnetite.getUnlocalizedName());
		GameRegistry.registerItem(goethite, goethite.getUnlocalizedName());
		GameRegistry.registerItem(fragment_vanadium, fragment_vanadium.getUnlocalizedName());

		//copper
		GameRegistry.registerItem(chalcopyrite, chalcopyrite.getUnlocalizedName());
		GameRegistry.registerItem(bornite, bornite.getUnlocalizedName());
		GameRegistry.registerItem(chalcocite, chalcocite.getUnlocalizedName());
		GameRegistry.registerItem(covellite, covellite.getUnlocalizedName());
		GameRegistry.registerItem(molybdenite, molybdenite.getUnlocalizedName());

		//PLAT GROUP
		GameRegistry.registerItem(osmiridium, osmiridium.getUnlocalizedName());
		GameRegistry.registerItem(pgm_residue, pgm_residue.getUnlocalizedName());

		//lead-zinc
		GameRegistry.registerItem(galena, galena.getUnlocalizedName());
		GameRegistry.registerItem(sphalerite, sphalerite.getUnlocalizedName());
		GameRegistry.registerItem(stibnite, stibnite.getUnlocalizedName());

		//nickel
		GameRegistry.registerItem(pentlandite, pentlandite.getUnlocalizedName());
		GameRegistry.registerItem(pyrrhotite, pyrrhotite.getUnlocalizedName());

		//tin-tungsten
		GameRegistry.registerItem(cassiterite, cassiterite.getUnlocalizedName());
		GameRegistry.registerItem(wolframite, wolframite.getUnlocalizedName());
		GameRegistry.registerItem(columbite, columbite.getUnlocalizedName());

		//lithium
		GameRegistry.registerItem(spodumene, spodumene.getUnlocalizedName());
		GameRegistry.registerItem(lepidolite, lepidolite.getUnlocalizedName());
		GameRegistry.registerItem(petalite, petalite.getUnlocalizedName());

		//heavy mineral sand
		GameRegistry.registerItem(ilmenite, ilmenite.getUnlocalizedName());
		GameRegistry.registerItem(rutile, rutile.getUnlocalizedName());
		GameRegistry.registerItem(zircon, zircon.getUnlocalizedName());

		//chromite
		GameRegistry.registerItem(chromite, chromite.getUnlocalizedName());

		//evaporite
		GameRegistry.registerItem(carnallite, carnallite.getUnlocalizedName());

		GameRegistry.registerItem(crushed_spodumene, crushed_spodumene.getUnlocalizedName());
		GameRegistry.registerItem(crushed_petalite, crushed_petalite.getUnlocalizedName());
		//GameRegistry.registerItem(roasted_spodumene, roasted_spodumene.getUnlocalizedName());

		//Things that look like rotten flesh but aren't
		GameRegistry.registerItem(biomass, biomass.getUnlocalizedName());
		GameRegistry.registerItem(biomass_compressed, biomass_compressed.getUnlocalizedName());
		//delicious!
		GameRegistry.registerItem(bio_wafer, bio_wafer.getUnlocalizedName());
		GameRegistry.registerItem(flesh, flesh.getUnlocalizedName());
		GameRegistry.registerItem(flesh_wafer, flesh_wafer.getUnlocalizedName());
		GameRegistry.registerItem(grilled_flesh, grilled_flesh.getUnlocalizedName());
		GameRegistry.registerItem(flesh_burger, flesh_burger.getUnlocalizedName());

		//Nuggets
		GameRegistry.registerItem(nugget_uranium, nugget_uranium.getUnlocalizedName());
		GameRegistry.registerItem(nugget_promethium, nugget_promethium.getUnlocalizedName());
		GameRegistry.registerItem(nugget_u233, nugget_u233.getUnlocalizedName());
		GameRegistry.registerItem(nugget_u235, nugget_u235.getUnlocalizedName());
		GameRegistry.registerItem(nugget_u238, nugget_u238.getUnlocalizedName());
		GameRegistry.registerItem(nugget_th232, nugget_th232.getUnlocalizedName());
		GameRegistry.registerItem(nugget_plutonium, nugget_plutonium.getUnlocalizedName());
		GameRegistry.registerItem(nugget_pu238, nugget_pu238.getUnlocalizedName());
		GameRegistry.registerItem(nugget_tm170, nugget_tm170.getUnlocalizedName());
		GameRegistry.registerItem(nugget_pu239, nugget_pu239.getUnlocalizedName());
		GameRegistry.registerItem(nugget_pu240, nugget_pu240.getUnlocalizedName());
		GameRegistry.registerItem(nugget_pu241, nugget_pu241.getUnlocalizedName());
		GameRegistry.registerItem(nugget_pu_mix, nugget_pu_mix.getUnlocalizedName());
		GameRegistry.registerItem(nugget_am241, nugget_am241.getUnlocalizedName());
		GameRegistry.registerItem(nugget_am242, nugget_am242.getUnlocalizedName());
		GameRegistry.registerItem(nugget_am_mix, nugget_am_mix.getUnlocalizedName());
		GameRegistry.registerItem(nugget_bk247, nugget_bk247.getUnlocalizedName());
		GameRegistry.registerItem(nugget_cm_mix, nugget_cm_mix.getUnlocalizedName());
		GameRegistry.registerItem(nugget_cm242, nugget_cm242.getUnlocalizedName());
		GameRegistry.registerItem(nugget_cm243, nugget_cm243.getUnlocalizedName());
		GameRegistry.registerItem(nugget_cm244, nugget_cm244.getUnlocalizedName());
		GameRegistry.registerItem(nugget_cm245, nugget_cm245.getUnlocalizedName());
		GameRegistry.registerItem(nugget_cm246, nugget_cm246.getUnlocalizedName());
		GameRegistry.registerItem(nugget_cm247, nugget_cm247.getUnlocalizedName());
		GameRegistry.registerItem(nugget_cm248, nugget_cm248.getUnlocalizedName());
		GameRegistry.registerItem(nugget_cm_fuel, nugget_cm_fuel.getUnlocalizedName());
		GameRegistry.registerItem(nugget_cf251, nugget_cf251.getUnlocalizedName());
		GameRegistry.registerItem(nugget_cf252, nugget_cf252.getUnlocalizedName());
		GameRegistry.registerItem(nugget_cf249, nugget_cf249.getUnlocalizedName());
		GameRegistry.registerItem(nugget_cf248, nugget_cf248.getUnlocalizedName());
		GameRegistry.registerItem(nugget_cf250, nugget_cf250.getUnlocalizedName());
		GameRegistry.registerItem(nugget_es253, nugget_es253.getUnlocalizedName());
		GameRegistry.registerItem(nugget_cn989, nugget_cn989.getUnlocalizedName());
		GameRegistry.registerItem(nugget_neptunium, nugget_neptunium.getUnlocalizedName());
		GameRegistry.registerItem(nugget_polonium, nugget_polonium.getUnlocalizedName());
		GameRegistry.registerItem(nugget_cobalt, nugget_cobalt.getUnlocalizedName());
		GameRegistry.registerItem(nugget_co60, nugget_co60.getUnlocalizedName());
		GameRegistry.registerItem(nugget_sr90, nugget_sr90.getUnlocalizedName());
		GameRegistry.registerItem(nugget_technetium, nugget_technetium.getUnlocalizedName());
		GameRegistry.registerItem(nugget_au198, nugget_au198.getUnlocalizedName());
		GameRegistry.registerItem(nugget_pb209, nugget_pb209.getUnlocalizedName());
		GameRegistry.registerItem(nugget_ra226, nugget_ra226.getUnlocalizedName());
		GameRegistry.registerItem(nugget_actinium, nugget_actinium.getUnlocalizedName());
		GameRegistry.registerItem(nugget_lead, nugget_lead.getUnlocalizedName());
		GameRegistry.registerItem(nugget_bismuth, nugget_bismuth.getUnlocalizedName());
		GameRegistry.registerItem(nugget_silver, nugget_silver.getUnlocalizedName());
		GameRegistry.registerItem(nugget_arsenic, nugget_arsenic.getUnlocalizedName());
		GameRegistry.registerItem(nugget_antimony, nugget_antimony.getUnlocalizedName());
		GameRegistry.registerItem(nugget_tantalium, nugget_tantalium.getUnlocalizedName());
		GameRegistry.registerItem(nugget_silicon, nugget_silicon.getUnlocalizedName());
		GameRegistry.registerItem(calcined_dolomite, calcined_dolomite.getUnlocalizedName());
		GameRegistry.registerItem(nugget_niobium, nugget_niobium.getUnlocalizedName());
		GameRegistry.registerItem(nugget_vanadium, nugget_vanadium.getUnlocalizedName());
		GameRegistry.registerItem(nugget_beryllium, nugget_beryllium.getUnlocalizedName());
		GameRegistry.registerItem(nugget_schrabidium, nugget_schrabidium.getUnlocalizedName());
		GameRegistry.registerItem(nugget_solinium, nugget_solinium.getUnlocalizedName());
		GameRegistry.registerItem(nugget_gh336, nugget_gh336.getUnlocalizedName());
		GameRegistry.registerItem(nugget_uranium_fuel, nugget_uranium_fuel.getUnlocalizedName());
		GameRegistry.registerItem(nugget_thorium_fuel, nugget_thorium_fuel.getUnlocalizedName());
		GameRegistry.registerItem(nugget_plutonium_fuel, nugget_plutonium_fuel.getUnlocalizedName());
		GameRegistry.registerItem(nugget_neptunium_fuel, nugget_neptunium_fuel.getUnlocalizedName());
		GameRegistry.registerItem(nugget_mox_fuel, nugget_mox_fuel.getUnlocalizedName());
		GameRegistry.registerItem(nugget_americium_fuel, nugget_americium_fuel.getUnlocalizedName());
		GameRegistry.registerItem(nugget_schrabidium_fuel, nugget_schrabidium_fuel.getUnlocalizedName());
		GameRegistry.registerItem(nugget_hes, nugget_hes.getUnlocalizedName());
		GameRegistry.registerItem(nugget_les, nugget_les.getUnlocalizedName());
		GameRegistry.registerItem(nugget_zirconium, nugget_zirconium.getUnlocalizedName());
		GameRegistry.registerItem(nugget_nickel, nugget_nickel.getUnlocalizedName());
		GameRegistry.registerItem(nugget_platnium, nugget_platnium.getUnlocalizedName());
		GameRegistry.registerItem(nugget_gaas, nugget_gaas.getUnlocalizedName());
		GameRegistry.registerItem(nugget_zinc, nugget_zinc.getUnlocalizedName());
		GameRegistry.registerItem(nugget_gallium, nugget_gallium.getUnlocalizedName());
		GameRegistry.registerItem(nugget_australium, nugget_australium.getUnlocalizedName());
		GameRegistry.registerItem(nugget_australium_lesser, nugget_australium_lesser.getUnlocalizedName());
		GameRegistry.registerItem(nugget_australium_greater, nugget_australium_greater.getUnlocalizedName());
		GameRegistry.registerItem(nugget_weidanium, nugget_weidanium.getUnlocalizedName());
		GameRegistry.registerItem(nugget_reiium, nugget_reiium.getUnlocalizedName());
		GameRegistry.registerItem(nugget_unobtainium, nugget_unobtainium.getUnlocalizedName());
		GameRegistry.registerItem(nugget_daffergon, nugget_daffergon.getUnlocalizedName());
		GameRegistry.registerItem(nugget_verticium, nugget_verticium.getUnlocalizedName());
		GameRegistry.registerItem(nugget_desh, nugget_desh.getUnlocalizedName());
		GameRegistry.registerItem(nugget_euphemium, nugget_euphemium.getUnlocalizedName());
		GameRegistry.registerItem(nugget_dineutronium, nugget_dineutronium.getUnlocalizedName());
		GameRegistry.registerItem(nugget_osmiridium, nugget_osmiridium.getUnlocalizedName());
		GameRegistry.registerItem(nugget_hafnium, nugget_hafnium.getUnlocalizedName());
		GameRegistry.registerItem(nugget_lanthanium, nugget_lanthanium.getUnlocalizedName());

		//Plates
		GameRegistry.registerItem(plate_iron, plate_iron.getUnlocalizedName());
		GameRegistry.registerItem(plate_gold, plate_gold.getUnlocalizedName());
		GameRegistry.registerItem(plate_titanium, plate_titanium.getUnlocalizedName());
		GameRegistry.registerItem(plate_nickel, plate_nickel.getUnlocalizedName());
		GameRegistry.registerItem(plate_aluminium, plate_aluminium.getUnlocalizedName());
		GameRegistry.registerItem(plate_steel, plate_steel.getUnlocalizedName());
		GameRegistry.registerItem(plate_stainless, plate_stainless.getUnlocalizedName());
		GameRegistry.registerItem(plate_lead, plate_lead.getUnlocalizedName());
		GameRegistry.registerItem(plate_copper, plate_copper.getUnlocalizedName());
		GameRegistry.registerItem(plate_advanced_alloy, plate_advanced_alloy.getUnlocalizedName());
		GameRegistry.registerItem(plate_dura_steel, plate_dura_steel.getUnlocalizedName());
		GameRegistry.registerItem(neutron_reflector, neutron_reflector.getUnlocalizedName());
		GameRegistry.registerItem(plate_schrabidium, plate_schrabidium.getUnlocalizedName());
		GameRegistry.registerItem(plate_combine_steel, plate_combine_steel.getUnlocalizedName());
		GameRegistry.registerItem(plate_mixed, plate_mixed.getUnlocalizedName());
		GameRegistry.registerItem(plate_gunmetal, plate_gunmetal.getUnlocalizedName());
		GameRegistry.registerItem(plate_weaponsteel, plate_weaponsteel.getUnlocalizedName());
		GameRegistry.registerItem(plate_saturnite, plate_saturnite.getUnlocalizedName());
		GameRegistry.registerItem(plate_paa, plate_paa.getUnlocalizedName());
		GameRegistry.registerItem(plate_polymer, plate_polymer.getUnlocalizedName());
		GameRegistry.registerItem(plate_kevlar, plate_kevlar.getUnlocalizedName());
		GameRegistry.registerItem(plate_dalekanium, plate_dalekanium.getUnlocalizedName());
		GameRegistry.registerItem(plate_desh, plate_desh.getUnlocalizedName());
		GameRegistry.registerItem(plate_bismuth, plate_bismuth.getUnlocalizedName());
		GameRegistry.registerItem(plate_euphemium, plate_euphemium.getUnlocalizedName());
		GameRegistry.registerItem(plate_dineutronium, plate_dineutronium.getUnlocalizedName());
		GameRegistry.registerItem(plate_cn989, plate_cn989.getUnlocalizedName());

		//Armor Plates
		GameRegistry.registerItem(plate_armor_titanium, plate_armor_titanium.getUnlocalizedName());
		GameRegistry.registerItem(plate_armor_ajr, plate_armor_ajr.getUnlocalizedName());
		GameRegistry.registerItem(plate_armor_hev, plate_armor_hev.getUnlocalizedName());
		GameRegistry.registerItem(plate_armor_lunar, plate_armor_lunar.getUnlocalizedName());
		GameRegistry.registerItem(plate_armor_fau, plate_armor_fau.getUnlocalizedName());
		GameRegistry.registerItem(plate_armor_dnt, plate_armor_dnt.getUnlocalizedName());

		//Heavy/Cast Plate
		GameRegistry.registerItem(plate_cast, plate_cast.getUnlocalizedName());
		GameRegistry.registerItem(plate_welded, plate_welded.getUnlocalizedName());
		GameRegistry.registerItem(shell, shell.getUnlocalizedName());
		GameRegistry.registerItem(pipe, pipe.getUnlocalizedName());
		GameRegistry.registerItem(heavy_component, heavy_component.getUnlocalizedName());

		//Bolts
		GameRegistry.registerItem(bolt, bolt.getUnlocalizedName());
		GameRegistry.registerItem(bolt_spike, bolt_spike.getUnlocalizedName());

		//Cloth
		GameRegistry.registerItem(hazmat_cloth, hazmat_cloth.getUnlocalizedName());
		GameRegistry.registerItem(hazmat_cloth_red, hazmat_cloth_red.getUnlocalizedName());
		GameRegistry.registerItem(hazmat_cloth_grey, hazmat_cloth_grey.getUnlocalizedName());
		GameRegistry.registerItem(asbestos_cloth, asbestos_cloth.getUnlocalizedName());
		GameRegistry.registerItem(rag, rag.getUnlocalizedName());
		GameRegistry.registerItem(rag_damp, rag_damp.getUnlocalizedName());
		GameRegistry.registerItem(rag_piss, rag_piss.getUnlocalizedName());
		GameRegistry.registerItem(rag_blood, rag_blood.getUnlocalizedName());
		GameRegistry.registerItem(filter_coal, filter_coal.getUnlocalizedName());

		//Wires
		GameRegistry.registerItem(wire_aluminium, wire_aluminium.getUnlocalizedName());
		GameRegistry.registerItem(wire_copper, wire_copper.getUnlocalizedName());
		GameRegistry.registerItem(wire_tungsten, wire_tungsten.getUnlocalizedName());
		GameRegistry.registerItem(wire_red_copper, wire_red_copper.getUnlocalizedName());
		GameRegistry.registerItem(wire_advanced_alloy, wire_advanced_alloy.getUnlocalizedName());
		GameRegistry.registerItem(wire_gold, wire_gold.getUnlocalizedName());
		GameRegistry.registerItem(wire_schrabidium, wire_schrabidium.getUnlocalizedName());
		GameRegistry.registerItem(wire_magnetized_tungsten, wire_magnetized_tungsten.getUnlocalizedName());
		GameRegistry.registerItem(wire_zinc, wire_zinc.getUnlocalizedName());
		GameRegistry.registerItem(wire_fine, wire_fine.getUnlocalizedName());
		GameRegistry.registerItem(wire_dense, wire_dense.getUnlocalizedName());

		//Parts
		GameRegistry.registerItem(coil_copper, coil_copper.getUnlocalizedName());
		GameRegistry.registerItem(coil_copper_torus, coil_copper_torus.getUnlocalizedName());
		GameRegistry.registerItem(coil_advanced_alloy, coil_advanced_alloy.getUnlocalizedName());
		GameRegistry.registerItem(coil_advanced_torus, coil_advanced_torus.getUnlocalizedName());
		GameRegistry.registerItem(coil_gold, coil_gold.getUnlocalizedName());
		GameRegistry.registerItem(coil_gold_torus, coil_gold_torus.getUnlocalizedName());
		GameRegistry.registerItem(coil_tungsten, coil_tungsten.getUnlocalizedName());
		GameRegistry.registerItem(coil_magnetized_tungsten, coil_magnetized_tungsten.getUnlocalizedName());
		GameRegistry.registerItem(safety_fuse, safety_fuse.getUnlocalizedName());
		GameRegistry.registerItem(tank_steel, tank_steel.getUnlocalizedName());
		GameRegistry.registerItem(motor, motor.getUnlocalizedName());
		GameRegistry.registerItem(motor_desh, motor_desh.getUnlocalizedName());
		GameRegistry.registerItem(motor_bismuth, motor_bismuth.getUnlocalizedName());
		GameRegistry.registerItem(centrifuge_element, centrifuge_element.getUnlocalizedName());
		//GameRegistry.registerItem(centrifuge_tower, centrifuge_tower.getUnlocalizedName());
		//GameRegistry.registerItem(magnet_dee, magnet_dee.getUnlocalizedName());
		GameRegistry.registerItem(magnet_circular, magnet_circular.getUnlocalizedName());
		//GameRegistry.registerItem(cyclotron_tower, cyclotron_tower.getUnlocalizedName());
		GameRegistry.registerItem(reactor_core, reactor_core.getUnlocalizedName());
		GameRegistry.registerItem(rtg_unit, rtg_unit.getUnlocalizedName());
		//GameRegistry.registerItem(thermo_unit_empty, thermo_unit_empty.getUnlocalizedName());
		//GameRegistry.registerItem(thermo_unit_endo, thermo_unit_endo.getUnlocalizedName());
		//GameRegistry.registerItem(thermo_unit_exo, thermo_unit_exo.getUnlocalizedName());
		GameRegistry.registerItem(levitation_unit, levitation_unit.getUnlocalizedName());
		GameRegistry.registerItem(pipes_steel, pipes_steel.getUnlocalizedName());
		GameRegistry.registerItem(drill_titanium, drill_titanium.getUnlocalizedName());
		GameRegistry.registerItem(photo_panel, photo_panel.getUnlocalizedName());
		GameRegistry.registerItem(chlorine_pinwheel, chlorine_pinwheel.getUnlocalizedName());
		GameRegistry.registerItem(oxy_pinwheel, oxy_pinwheel.getUnlocalizedName());
		GameRegistry.registerItem(ring_starmetal, ring_starmetal.getUnlocalizedName());
		GameRegistry.registerItem(deuterium_filter, deuterium_filter.getUnlocalizedName());
		GameRegistry.registerItem(chemical_dye, chemical_dye.getUnlocalizedName());
		GameRegistry.registerItem(crayon, crayon.getUnlocalizedName());
		GameRegistry.registerItem(part_generic, part_generic.getUnlocalizedName());
		GameRegistry.registerItem(item_secret, item_secret.getUnlocalizedName());
		GameRegistry.registerItem(parts_legendary, parts_legendary.getUnlocalizedName());
		GameRegistry.registerItem(gear_large, gear_large.getUnlocalizedName());
		GameRegistry.registerItem(sawblade, sawblade.getUnlocalizedName());
		GameRegistry.registerItem(part_barrel_light, part_barrel_light.getUnlocalizedName());
		GameRegistry.registerItem(part_barrel_heavy, part_barrel_heavy.getUnlocalizedName());
		GameRegistry.registerItem(part_receiver_light, part_receiver_light.getUnlocalizedName());
		GameRegistry.registerItem(part_receiver_heavy, part_receiver_heavy.getUnlocalizedName());
		GameRegistry.registerItem(part_mechanism, part_mechanism.getUnlocalizedName());
		GameRegistry.registerItem(part_stock, part_stock.getUnlocalizedName());
		GameRegistry.registerItem(part_grip, part_grip.getUnlocalizedName());
		GameRegistry.registerItem(barium_sulfide, barium_sulfide.getUnlocalizedName());
		GameRegistry.registerItem(barium_carbonate, barium_carbonate.getUnlocalizedName());
		GameRegistry.registerItem(barium_oxide, barium_oxide.getUnlocalizedName());
		GameRegistry.registerItem(ingot_barium, ingot_barium.getUnlocalizedName());
		GameRegistry.registerItem(barium_nugget, barium_nugget.getUnlocalizedName());
		GameRegistry.registerItem(rutherfordium_nugget, rutherfordium_nugget.getUnlocalizedName());
		GameRegistry.registerItem(dubnium_nugget, dubnium_nugget.getUnlocalizedName());
		GameRegistry.registerItem(powder_molybdenite, powder_molybdenite.getUnlocalizedName());
		GameRegistry.registerItem(powder_rhenium, powder_rhenium.getUnlocalizedName());
		GameRegistry.registerItem(hard_drive, hard_drive.getUnlocalizedName());
		GameRegistry.registerItem(full_drive, full_drive.getUnlocalizedName());

		//Plant Products
		GameRegistry.registerItem(plant_item, plant_item.getUnlocalizedName());
		GameRegistry.registerItem(scuttertail, scuttertail.getUnlocalizedName());
		GameRegistry.registerItem(saltleaf, saltleaf.getUnlocalizedName());


		//Teleporter Parts
		//GameRegistry.registerItem(telepad, telepad.getUnlocalizedName());
		GameRegistry.registerItem(entanglement_kit, entanglement_kit.getUnlocalizedName());

		//AMS Parts
		GameRegistry.registerItem(component_limiter, component_limiter.getUnlocalizedName());
		GameRegistry.registerItem(component_emitter, component_emitter.getUnlocalizedName());

		//Bomb Parts
		GameRegistry.registerItem(fins_flat, fins_flat.getUnlocalizedName());
		GameRegistry.registerItem(fins_small_steel, fins_small_steel.getUnlocalizedName());
		GameRegistry.registerItem(fins_big_steel, fins_big_steel.getUnlocalizedName());
		GameRegistry.registerItem(fins_tri_steel, fins_tri_steel.getUnlocalizedName());
		GameRegistry.registerItem(fins_quad_titanium, fins_quad_titanium.getUnlocalizedName());
		GameRegistry.registerItem(sphere_steel, sphere_steel.getUnlocalizedName());
		GameRegistry.registerItem(pedestal_steel, pedestal_steel.getUnlocalizedName());
		GameRegistry.registerItem(dysfunctional_reactor, dysfunctional_reactor.getUnlocalizedName());
		GameRegistry.registerItem(blade_titanium, blade_titanium.getUnlocalizedName());
		GameRegistry.registerItem(blade_syngas, blade_syngas.getUnlocalizedName());
		GameRegistry.registerItem(blade_tungsten, blade_tungsten.getUnlocalizedName());
		GameRegistry.registerItem(turbine_titanium, turbine_titanium.getUnlocalizedName());
		GameRegistry.registerItem(turbine_tungsten, turbine_tungsten.getUnlocalizedName());
		GameRegistry.registerItem(turbine_syngas, turbine_syngas.getUnlocalizedName());
		GameRegistry.registerItem(flywheel_beryllium, flywheel_beryllium.getUnlocalizedName());
		GameRegistry.registerItem(toothpicks, toothpicks.getUnlocalizedName());
		GameRegistry.registerItem(ducttape, ducttape.getUnlocalizedName());
		GameRegistry.registerItem(catalyst_clay, catalyst_clay.getUnlocalizedName());
		GameRegistry.registerItem(missile_assembly, missile_assembly.getUnlocalizedName());
		GameRegistry.registerItem(warhead_generic_small, warhead_generic_small.getUnlocalizedName());
		GameRegistry.registerItem(warhead_generic_medium, warhead_generic_medium.getUnlocalizedName());
		GameRegistry.registerItem(warhead_generic_large, warhead_generic_large.getUnlocalizedName());
		GameRegistry.registerItem(warhead_incendiary_small, warhead_incendiary_small.getUnlocalizedName());
		GameRegistry.registerItem(warhead_incendiary_medium, warhead_incendiary_medium.getUnlocalizedName());
		GameRegistry.registerItem(warhead_incendiary_large, warhead_incendiary_large.getUnlocalizedName());
		GameRegistry.registerItem(warhead_cluster_small, warhead_cluster_small.getUnlocalizedName());
		GameRegistry.registerItem(warhead_cluster_medium, warhead_cluster_medium.getUnlocalizedName());
		GameRegistry.registerItem(warhead_cluster_large, warhead_cluster_large.getUnlocalizedName());
		GameRegistry.registerItem(warhead_buster_small, warhead_buster_small.getUnlocalizedName());
		GameRegistry.registerItem(warhead_buster_medium, warhead_buster_medium.getUnlocalizedName());
		GameRegistry.registerItem(warhead_buster_large, warhead_buster_large.getUnlocalizedName());
		GameRegistry.registerItem(warhead_nuclear, warhead_nuclear.getUnlocalizedName());
		GameRegistry.registerItem(warhead_mirv, warhead_mirv.getUnlocalizedName());
		GameRegistry.registerItem(warhead_volcano, warhead_volcano.getUnlocalizedName());
		GameRegistry.registerItem(fuel_tank_small, fuel_tank_small.getUnlocalizedName());
		GameRegistry.registerItem(fuel_tank_medium, fuel_tank_medium.getUnlocalizedName());
		GameRegistry.registerItem(fuel_tank_large, fuel_tank_large.getUnlocalizedName());
		GameRegistry.registerItem(thruster_small, thruster_small.getUnlocalizedName());
		GameRegistry.registerItem(thruster_medium, thruster_medium.getUnlocalizedName());
		GameRegistry.registerItem(thruster_large, thruster_large.getUnlocalizedName());
		GameRegistry.registerItem(thruster_nuclear, thruster_nuclear.getUnlocalizedName());
		GameRegistry.registerItem(sat_base, sat_base.getUnlocalizedName());
		GameRegistry.registerItem(sat_head_mapper, sat_head_mapper.getUnlocalizedName());
		GameRegistry.registerItem(sat_head_scanner, sat_head_scanner.getUnlocalizedName());
		GameRegistry.registerItem(sat_head_radar, sat_head_radar.getUnlocalizedName());
		GameRegistry.registerItem(sat_head_laser, sat_head_laser.getUnlocalizedName());
		GameRegistry.registerItem(sat_head_resonator, sat_head_resonator.getUnlocalizedName());
		GameRegistry.registerItem(seg_10, seg_10.getUnlocalizedName());
		GameRegistry.registerItem(seg_15, seg_15.getUnlocalizedName());
		GameRegistry.registerItem(seg_20, seg_20.getUnlocalizedName());

		//Chopper parts
		GameRegistry.registerItem(chopper_head, chopper_head.getUnlocalizedName());
		GameRegistry.registerItem(chopper_gun, chopper_gun.getUnlocalizedName());
		GameRegistry.registerItem(chopper_torso, chopper_torso.getUnlocalizedName());
		GameRegistry.registerItem(chopper_tail, chopper_tail.getUnlocalizedName());
		GameRegistry.registerItem(chopper_wing, chopper_wing.getUnlocalizedName());
		GameRegistry.registerItem(chopper_blades, chopper_blades.getUnlocalizedName());
		GameRegistry.registerItem(combine_scrap, combine_scrap.getUnlocalizedName());

		//Hammer Parts
		GameRegistry.registerItem(shimmer_head, shimmer_head.getUnlocalizedName());
		GameRegistry.registerItem(shimmer_axe_head, shimmer_axe_head.getUnlocalizedName());
		GameRegistry.registerItem(shimmer_handle, shimmer_handle.getUnlocalizedName());

		//Circuits
		GameRegistry.registerItem(circuit, circuit.getUnlocalizedName());
		GameRegistry.registerItem(circuit_raw, circuit_raw.getUnlocalizedName());
		GameRegistry.registerItem(circuit_aluminium, circuit_aluminium.getUnlocalizedName());
		GameRegistry.registerItem(circuit_copper, circuit_copper.getUnlocalizedName());
		GameRegistry.registerItem(circuit_red_copper, circuit_red_copper.getUnlocalizedName());
		GameRegistry.registerItem(circuit_gold, circuit_gold.getUnlocalizedName());
		GameRegistry.registerItem(circuit_schrabidium, circuit_schrabidium.getUnlocalizedName());
		GameRegistry.registerItem(circuit_bismuth_raw, circuit_bismuth_raw.getUnlocalizedName());
		GameRegistry.registerItem(circuit_bismuth, circuit_bismuth.getUnlocalizedName());
		GameRegistry.registerItem(circuit_arsenic_raw, circuit_arsenic_raw.getUnlocalizedName());
		GameRegistry.registerItem(circuit_arsenic, circuit_arsenic.getUnlocalizedName());
		GameRegistry.registerItem(circuit_tantalium_raw, circuit_tantalium_raw.getUnlocalizedName());
		GameRegistry.registerItem(circuit_tantalium, circuit_tantalium.getUnlocalizedName());
		GameRegistry.registerItem(crt_display, crt_display.getUnlocalizedName());
		GameRegistry.registerItem(circuit_star_piece, circuit_star_piece.getUnlocalizedName());
		GameRegistry.registerItem(circuit_star_component, circuit_star_component.getUnlocalizedName());
		GameRegistry.registerItem(circuit_star, circuit_star.getUnlocalizedName());

		//Military Circuits
		GameRegistry.registerItem(circuit_targeting_tier1, circuit_targeting_tier1.getUnlocalizedName());
		GameRegistry.registerItem(circuit_targeting_tier2, circuit_targeting_tier2.getUnlocalizedName());
		GameRegistry.registerItem(circuit_targeting_tier3, circuit_targeting_tier3.getUnlocalizedName());
		GameRegistry.registerItem(circuit_targeting_tier4, circuit_targeting_tier4.getUnlocalizedName());
		GameRegistry.registerItem(circuit_targeting_tier5, circuit_targeting_tier5.getUnlocalizedName());
		GameRegistry.registerItem(circuit_targeting_tier6, circuit_targeting_tier6.getUnlocalizedName());

		//Gun Mechanisms
		GameRegistry.registerItem(mechanism_revolver_1, mechanism_revolver_1.getUnlocalizedName());
		GameRegistry.registerItem(mechanism_revolver_2, mechanism_revolver_2.getUnlocalizedName());
		GameRegistry.registerItem(mechanism_rifle_1, mechanism_rifle_1.getUnlocalizedName());
		GameRegistry.registerItem(mechanism_rifle_2, mechanism_rifle_2.getUnlocalizedName());
		GameRegistry.registerItem(mechanism_launcher_1, mechanism_launcher_1.getUnlocalizedName());
		GameRegistry.registerItem(mechanism_launcher_2, mechanism_launcher_2.getUnlocalizedName());
		GameRegistry.registerItem(mechanism_special, mechanism_special.getUnlocalizedName());

		//Casings
		GameRegistry.registerItem(casing_357, casing_357.getUnlocalizedName());
		GameRegistry.registerItem(casing_44, casing_44.getUnlocalizedName());
		GameRegistry.registerItem(casing_9, casing_9.getUnlocalizedName());
		GameRegistry.registerItem(casing_50, casing_50.getUnlocalizedName());
		GameRegistry.registerItem(casing_buckshot, casing_buckshot.getUnlocalizedName());

		//Bullet Assemblies
		GameRegistry.registerItem(assembly_iron, assembly_iron.getUnlocalizedName());
		GameRegistry.registerItem(assembly_steel, assembly_steel.getUnlocalizedName());
		GameRegistry.registerItem(assembly_lead, assembly_lead.getUnlocalizedName());
		GameRegistry.registerItem(assembly_gold, assembly_gold.getUnlocalizedName());
		GameRegistry.registerItem(assembly_schrabidium, assembly_schrabidium.getUnlocalizedName());
		GameRegistry.registerItem(assembly_nightmare, assembly_nightmare.getUnlocalizedName());
		GameRegistry.registerItem(assembly_desh, assembly_desh.getUnlocalizedName());
		//GameRegistry.registerItem(assembly_pip, assembly_pip.getUnlocalizedName());
		GameRegistry.registerItem(assembly_nopip, assembly_nopip.getUnlocalizedName());
		GameRegistry.registerItem(assembly_smg, assembly_smg.getUnlocalizedName());
		GameRegistry.registerItem(assembly_556, assembly_556.getUnlocalizedName());
		GameRegistry.registerItem(assembly_762, assembly_762.getUnlocalizedName());
		GameRegistry.registerItem(assembly_45, assembly_45.getUnlocalizedName());
		GameRegistry.registerItem(assembly_uzi, assembly_uzi.getUnlocalizedName());
		GameRegistry.registerItem(assembly_lacunae, assembly_lacunae.getUnlocalizedName());
		GameRegistry.registerItem(assembly_actionexpress, assembly_actionexpress.getUnlocalizedName());
		GameRegistry.registerItem(assembly_calamity, assembly_calamity.getUnlocalizedName());
		GameRegistry.registerItem(assembly_nuke, assembly_nuke.getUnlocalizedName());
		GameRegistry.registerItem(assembly_luna, assembly_luna.getUnlocalizedName());

		//Folly Parts
		GameRegistry.registerItem(folly_shell, folly_shell.getUnlocalizedName());
		GameRegistry.registerItem(folly_bullet, folly_bullet.getUnlocalizedName());
		GameRegistry.registerItem(folly_bullet_nuclear, folly_bullet_nuclear.getUnlocalizedName());
		GameRegistry.registerItem(folly_bullet_du, folly_bullet_du.getUnlocalizedName());

		//Wiring
		GameRegistry.registerItem(wiring_red_copper, wiring_red_copper.getUnlocalizedName());

		//Flame War in a Box
		GameRegistry.registerItem(flame_pony, flame_pony.getUnlocalizedName());
		GameRegistry.registerItem(flame_conspiracy, flame_conspiracy.getUnlocalizedName());
		GameRegistry.registerItem(flame_politics, flame_politics.getUnlocalizedName());
		GameRegistry.registerItem(flame_opinion, flame_opinion.getUnlocalizedName());

		//Pellets
		GameRegistry.registerItem(pellet_rtg_radium, pellet_rtg_radium.getUnlocalizedName());
		GameRegistry.registerItem(pellet_rtg_weak, pellet_rtg_weak.getUnlocalizedName());
		GameRegistry.registerItem(pellet_rtg, pellet_rtg.getUnlocalizedName());
		GameRegistry.registerItem(pellet_rtg_promethium, pellet_rtg_promethium.getUnlocalizedName());
		GameRegistry.registerItem(pellet_rtg_strontium, pellet_rtg_strontium.getUnlocalizedName());
		GameRegistry.registerItem(pellet_rtg_cobalt, pellet_rtg_cobalt.getUnlocalizedName());
		GameRegistry.registerItem(pellet_rtg_actinium, pellet_rtg_actinium.getUnlocalizedName());
		GameRegistry.registerItem(pellet_rtg_polonium, pellet_rtg_polonium.getUnlocalizedName());
		GameRegistry.registerItem(pellet_rtg_americium, pellet_rtg_americium.getUnlocalizedName());
		GameRegistry.registerItem(pellet_rtg_berkelium, pellet_rtg_berkelium.getUnlocalizedName());
		GameRegistry.registerItem(pellet_rtg_tm170, pellet_rtg_tm170.getUnlocalizedName());
		GameRegistry.registerItem(pellet_rtg_gold, pellet_rtg_gold.getUnlocalizedName());
		GameRegistry.registerItem(pellet_rtg_lead, pellet_rtg_lead.getUnlocalizedName());
		GameRegistry.registerItem(pellet_rtg_cf251, pellet_rtg_cf251.getUnlocalizedName());
		GameRegistry.registerItem(pellet_rtg_cf252, pellet_rtg_cf252.getUnlocalizedName());
		GameRegistry.registerItem(pellet_rtg_depleted, pellet_rtg_depleted.getUnlocalizedName());
		GameRegistry.registerItem(tritium_deuterium_cake, tritium_deuterium_cake.getUnlocalizedName());
		GameRegistry.registerItem(pellet_cluster, pellet_cluster.getUnlocalizedName());
		GameRegistry.registerItem(pellet_buckshot, pellet_buckshot.getUnlocalizedName());
		GameRegistry.registerItem(pellet_flechette, pellet_flechette.getUnlocalizedName());
		GameRegistry.registerItem(pellet_chlorophyte, pellet_chlorophyte.getUnlocalizedName());
		GameRegistry.registerItem(pellet_mercury, pellet_mercury.getUnlocalizedName());
		GameRegistry.registerItem(pellet_meteorite, pellet_meteorite.getUnlocalizedName());
		GameRegistry.registerItem(pellet_canister, pellet_canister.getUnlocalizedName());
		GameRegistry.registerItem(pellet_claws, pellet_claws.getUnlocalizedName());
		GameRegistry.registerItem(pellet_charged, pellet_charged.getUnlocalizedName());
		GameRegistry.registerItem(pellet_gas, pellet_gas.getUnlocalizedName());
		GameRegistry.registerItem(magnetron, magnetron.getUnlocalizedName());
		GameRegistry.registerItem(pellet_coal, pellet_coal.getUnlocalizedName());

		//Engine Pieces
		GameRegistry.registerItem(piston_selenium, piston_selenium.getUnlocalizedName());
		GameRegistry.registerItem(piston_set, piston_set.getUnlocalizedName());
		GameRegistry.registerItem(drillbit, drillbit.getUnlocalizedName());

		//Cells
		GameRegistry.registerItem(cell_empty, cell_empty.getUnlocalizedName());
		GameRegistry.registerItem(cell_uf6, cell_uf6.getUnlocalizedName());
		GameRegistry.registerItem(cell_puf6, cell_puf6.getUnlocalizedName());
		GameRegistry.registerItem(cell_deuterium, cell_deuterium.getUnlocalizedName());
		GameRegistry.registerItem(cell_tritium, cell_tritium.getUnlocalizedName());
		GameRegistry.registerItem(cell_sas3, cell_sas3.getUnlocalizedName());
		GameRegistry.registerItem(cell_antimatter, cell_antimatter.getUnlocalizedName());
		GameRegistry.registerItem(cell_anti_schrabidium, cell_anti_schrabidium.getUnlocalizedName());
		GameRegistry.registerItem(cell_balefire, cell_balefire.getUnlocalizedName());

		//DEMON CORE
		GameRegistry.registerItem(demon_core_open, demon_core_open.getUnlocalizedName());
		GameRegistry.registerItem(demon_core_closed, demon_core_closed.getUnlocalizedName());

		//Particle Containers
		GameRegistry.registerItem(particle_empty, particle_empty.getUnlocalizedName());
		GameRegistry.registerItem(particle_hydrogen, particle_hydrogen.getUnlocalizedName());
		GameRegistry.registerItem(particle_copper, particle_copper.getUnlocalizedName());
		GameRegistry.registerItem(particle_lead, particle_lead.getUnlocalizedName());
		GameRegistry.registerItem(particle_aproton, particle_aproton.getUnlocalizedName());
		GameRegistry.registerItem(particle_aelectron, particle_aelectron.getUnlocalizedName());
		GameRegistry.registerItem(particle_amat, particle_amat.getUnlocalizedName());
		GameRegistry.registerItem(particle_aschrab, particle_aschrab.getUnlocalizedName());
		GameRegistry.registerItem(particle_higgs, particle_higgs.getUnlocalizedName());
		GameRegistry.registerItem(particle_muon, particle_muon.getUnlocalizedName());
		GameRegistry.registerItem(particle_tachyon, particle_tachyon.getUnlocalizedName());
		GameRegistry.registerItem(particle_strange, particle_strange.getUnlocalizedName());
		GameRegistry.registerItem(particle_dark, particle_dark.getUnlocalizedName());
		GameRegistry.registerItem(particle_sparkticle, particle_sparkticle.getUnlocalizedName());
		GameRegistry.registerItem(particle_helium, particle_helium.getUnlocalizedName());
		GameRegistry.registerItem(particle_astatine, particle_astatine.getUnlocalizedName());
		GameRegistry.registerItem(particle_neutron, particle_neutron.getUnlocalizedName());
		GameRegistry.registerItem(particle_beta, particle_beta.getUnlocalizedName());
		//GameRegistry.registerItem(particle_digamma, particle_digamma.getUnlocalizedName());
		GameRegistry.registerItem(particle_lutece, particle_lutece.getUnlocalizedName());

		//Singularities, black holes and other cosmic horrors
		//GameRegistry.registerItem(singularity_micro, singularity_micro.getUnlocalizedName());
		//GameRegistry.registerItem(singularity, singularity.getUnlocalizedName());
		//GameRegistry.registerItem(singularity_counter_resonant, singularity_counter_resonant.getUnlocalizedName());
		//GameRegistry.registerItem(singularity_super_heated, singularity_super_heated.getUnlocalizedName());
		//GameRegistry.registerItem(black_hole, black_hole.getUnlocalizedName());
		//GameRegistry.registerItem(singularity_spark, singularity_spark.getUnlocalizedName());
		//GameRegistry.registerItem(crystal_xen, crystal_xen.getUnlocalizedName());
		//GameRegistry.registerItem(pellet_antimatter, pellet_antimatter.getUnlocalizedName());

		//Infinite Tanks
		GameRegistry.registerItem(inf_water, inf_water.getUnlocalizedName());
		GameRegistry.registerItem(inf_water_mk2, inf_water_mk2.getUnlocalizedName());

		GameRegistry.registerItem(transporter_linker, transporter_linker.getUnlocalizedName());

		//Canisters
		GameRegistry.registerItem(fuel_additive, fuel_additive.getUnlocalizedName());
		GameRegistry.registerItem(canister_empty, canister_empty.getUnlocalizedName());
		GameRegistry.registerItem(canister_full, canister_full.getUnlocalizedName());
		GameRegistry.registerItem(canister_napalm, canister_napalm.getUnlocalizedName());

		//Gas Tanks
		GameRegistry.registerItem(gas_empty, gas_empty.getUnlocalizedName());
		GameRegistry.registerItem(gas_full, gas_full.getUnlocalizedName());

		//Universal Tank
		GameRegistry.registerItem(fluid_tank_empty, fluid_tank_empty.getUnlocalizedName());
		GameRegistry.registerItem(fluid_tank_full, fluid_tank_full.getUnlocalizedName());
		GameRegistry.registerItem(fluid_tank_lead_empty, fluid_tank_lead_empty.getUnlocalizedName());
		GameRegistry.registerItem(fluid_tank_lead_full, fluid_tank_lead_full.getUnlocalizedName());
		GameRegistry.registerItem(fluid_barrel_empty, fluid_barrel_empty.getUnlocalizedName());
		GameRegistry.registerItem(fluid_barrel_full, fluid_barrel_full.getUnlocalizedName());
		GameRegistry.registerItem(fluid_barrel_infinite, fluid_barrel_infinite.getUnlocalizedName());

		//Pipette
		GameRegistry.registerItem(pipette, pipette.getUnlocalizedName());
		GameRegistry.registerItem(pipette_boron, pipette_boron.getUnlocalizedName());
		GameRegistry.registerItem(pipette_laboratory, pipette_laboratory.getUnlocalizedName());

		//Siphon
		GameRegistry.registerItem(siphon, siphon.getUnlocalizedName());

		//Disperser Canister
		GameRegistry.registerItem(disperser_canister_empty, disperser_canister_empty.getUnlocalizedName());
		GameRegistry.registerItem(disperser_canister, disperser_canister.getUnlocalizedName());

		//GameRegistry.registerItem(glyphid_gland_empty, glyphid_gland_empty.getUnlocalizedName());
		//GameRegistry.registerItem(glyphid_gland, glyphid_gland.getUnlocalizedName());

		//Batteries
		GameRegistry.registerItem(battery_generic, battery_generic.getUnlocalizedName());
		GameRegistry.registerItem(battery_red_cell, battery_red_cell.getUnlocalizedName());
		GameRegistry.registerItem(battery_red_cell_6, battery_red_cell_6.getUnlocalizedName());
		GameRegistry.registerItem(battery_red_cell_24, battery_red_cell_24.getUnlocalizedName());
		GameRegistry.registerItem(battery_advanced, battery_advanced.getUnlocalizedName());
		GameRegistry.registerItem(battery_advanced_cell, battery_advanced_cell.getUnlocalizedName());
		GameRegistry.registerItem(battery_advanced_cell_4, battery_advanced_cell_4.getUnlocalizedName());
		GameRegistry.registerItem(battery_advanced_cell_12, battery_advanced_cell_12.getUnlocalizedName());
		GameRegistry.registerItem(battery_lithium, battery_lithium.getUnlocalizedName());
		GameRegistry.registerItem(battery_lithium_cell, battery_lithium_cell.getUnlocalizedName());
		GameRegistry.registerItem(battery_lithium_cell_3, battery_lithium_cell_3.getUnlocalizedName());
		GameRegistry.registerItem(battery_lithium_cell_6, battery_lithium_cell_6.getUnlocalizedName());
		GameRegistry.registerItem(battery_schrabidium, battery_schrabidium.getUnlocalizedName());
		GameRegistry.registerItem(battery_schrabidium_cell, battery_schrabidium_cell.getUnlocalizedName());
		GameRegistry.registerItem(battery_schrabidium_cell_2, battery_schrabidium_cell_2.getUnlocalizedName());
		GameRegistry.registerItem(battery_schrabidium_cell_4, battery_schrabidium_cell_4.getUnlocalizedName());
		GameRegistry.registerItem(battery_spark, battery_spark.getUnlocalizedName());
		GameRegistry.registerItem(battery_trixite, battery_trixite.getUnlocalizedName());
		GameRegistry.registerItem(battery_spark_cell_6, battery_spark_cell_6.getUnlocalizedName());
		GameRegistry.registerItem(battery_spark_cell_25, battery_spark_cell_25.getUnlocalizedName());
		GameRegistry.registerItem(battery_spark_cell_100, battery_spark_cell_100.getUnlocalizedName());
		GameRegistry.registerItem(battery_spark_cell_1000, battery_spark_cell_1000.getUnlocalizedName());
		GameRegistry.registerItem(battery_spark_cell_2500, battery_spark_cell_2500.getUnlocalizedName());
		GameRegistry.registerItem(battery_spark_cell_10000, battery_spark_cell_10000.getUnlocalizedName());
		GameRegistry.registerItem(battery_spark_cell_power, battery_spark_cell_power.getUnlocalizedName());
		GameRegistry.registerItem(cube_power, cube_power.getUnlocalizedName());
		GameRegistry.registerItem(battery_creative, battery_creative.getUnlocalizedName());
		GameRegistry.registerItem(battery_potato, battery_potato.getUnlocalizedName());
		GameRegistry.registerItem(battery_potatos, battery_potatos.getUnlocalizedName());
		GameRegistry.registerItem(battery_sc_uranium, battery_sc_uranium.getUnlocalizedName());
		GameRegistry.registerItem(battery_sc_technetium, battery_sc_technetium.getUnlocalizedName());
		GameRegistry.registerItem(battery_sc_plutonium, battery_sc_plutonium.getUnlocalizedName());
		GameRegistry.registerItem(battery_sc_polonium, battery_sc_polonium.getUnlocalizedName());
		GameRegistry.registerItem(battery_sc_gold, battery_sc_gold.getUnlocalizedName());
		GameRegistry.registerItem(battery_sc_lead, battery_sc_lead.getUnlocalizedName());
		GameRegistry.registerItem(battery_sc_americium, battery_sc_americium.getUnlocalizedName());
		GameRegistry.registerItem(hev_battery, hev_battery.getUnlocalizedName());
		GameRegistry.registerItem(fusion_core, fusion_core.getUnlocalizedName());
		GameRegistry.registerItem(energy_core, energy_core.getUnlocalizedName());
		GameRegistry.registerItem(fusion_core_infinite, fusion_core_infinite.getUnlocalizedName());

		//Folders
		GameRegistry.registerItem(template_folder, template_folder.getUnlocalizedName());
		GameRegistry.registerItem(journal_pip, journal_pip.getUnlocalizedName());
		GameRegistry.registerItem(journal_bj, journal_bj.getUnlocalizedName());
		GameRegistry.registerItem(journal_silver, journal_silver.getUnlocalizedName());
		GameRegistry.registerItem(bobmazon_materials, bobmazon_materials.getUnlocalizedName());
		GameRegistry.registerItem(bobmazon_machines, bobmazon_machines.getUnlocalizedName());
		GameRegistry.registerItem(bobmazon_weapons, bobmazon_weapons.getUnlocalizedName());
		GameRegistry.registerItem(bobmazon_tools, bobmazon_tools.getUnlocalizedName());
		GameRegistry.registerItem(bobmazon_hidden, bobmazon_hidden.getUnlocalizedName());

		//Hydraulic Press Stamps
		GameRegistry.registerItem(stamp_stone_flat, stamp_stone_flat.getUnlocalizedName());
		GameRegistry.registerItem(stamp_stone_plate, stamp_stone_plate.getUnlocalizedName());
		GameRegistry.registerItem(stamp_stone_wire, stamp_stone_wire.getUnlocalizedName());
		GameRegistry.registerItem(stamp_stone_circuit, stamp_stone_circuit.getUnlocalizedName());
		GameRegistry.registerItem(stamp_iron_flat, stamp_iron_flat.getUnlocalizedName());
		GameRegistry.registerItem(stamp_iron_plate, stamp_iron_plate.getUnlocalizedName());
		GameRegistry.registerItem(stamp_iron_wire, stamp_iron_wire.getUnlocalizedName());
		GameRegistry.registerItem(stamp_iron_circuit, stamp_iron_circuit.getUnlocalizedName());
		GameRegistry.registerItem(stamp_steel_flat, stamp_steel_flat.getUnlocalizedName());
		GameRegistry.registerItem(stamp_steel_plate, stamp_steel_plate.getUnlocalizedName());
		GameRegistry.registerItem(stamp_steel_wire, stamp_steel_wire.getUnlocalizedName());
		GameRegistry.registerItem(stamp_steel_circuit, stamp_steel_circuit.getUnlocalizedName());
		GameRegistry.registerItem(stamp_titanium_flat, stamp_titanium_flat.getUnlocalizedName());
		GameRegistry.registerItem(stamp_titanium_plate, stamp_titanium_plate.getUnlocalizedName());
		GameRegistry.registerItem(stamp_titanium_wire, stamp_titanium_wire.getUnlocalizedName());
		GameRegistry.registerItem(stamp_titanium_circuit, stamp_titanium_circuit.getUnlocalizedName());
		GameRegistry.registerItem(stamp_obsidian_flat, stamp_obsidian_flat.getUnlocalizedName());
		GameRegistry.registerItem(stamp_obsidian_plate, stamp_obsidian_plate.getUnlocalizedName());
		GameRegistry.registerItem(stamp_obsidian_wire, stamp_obsidian_wire.getUnlocalizedName());
		GameRegistry.registerItem(stamp_obsidian_circuit, stamp_obsidian_circuit.getUnlocalizedName());
		GameRegistry.registerItem(stamp_desh_flat, stamp_desh_flat.getUnlocalizedName());
		GameRegistry.registerItem(stamp_desh_plate, stamp_desh_plate.getUnlocalizedName());
		GameRegistry.registerItem(stamp_desh_wire, stamp_desh_wire.getUnlocalizedName());
		GameRegistry.registerItem(stamp_desh_circuit, stamp_desh_circuit.getUnlocalizedName());
		GameRegistry.registerItem(stamp_357, stamp_357.getUnlocalizedName());
		GameRegistry.registerItem(stamp_44, stamp_44.getUnlocalizedName());
		GameRegistry.registerItem(stamp_9, stamp_9.getUnlocalizedName());
		GameRegistry.registerItem(stamp_50, stamp_50.getUnlocalizedName());

		GameRegistry.registerItem(stamp_desh_357, stamp_desh_357.getUnlocalizedName());
		GameRegistry.registerItem(stamp_desh_44, stamp_desh_44.getUnlocalizedName());
		GameRegistry.registerItem(stamp_desh_9, stamp_desh_9.getUnlocalizedName());
		GameRegistry.registerItem(stamp_desh_50, stamp_desh_50.getUnlocalizedName());
		GameRegistry.registerItem(stamp_book, stamp_book.getUnlocalizedName());

		//Molds
		GameRegistry.registerItem(mold_base, mold_base.getUnlocalizedName());
		GameRegistry.registerItem(mold, mold.getUnlocalizedName());
		GameRegistry.registerItem(scraps, scraps.getUnlocalizedName());

		//Machine Upgrades
		GameRegistry.registerItem(upgrade_muffler, upgrade_muffler.getUnlocalizedName());
		GameRegistry.registerItem(upgrade_template, upgrade_template.getUnlocalizedName());
		GameRegistry.registerItem(upgrade_speed_1, upgrade_speed_1.getUnlocalizedName());
		GameRegistry.registerItem(upgrade_speed_2, upgrade_speed_2.getUnlocalizedName());
		GameRegistry.registerItem(upgrade_speed_3, upgrade_speed_3.getUnlocalizedName());
		GameRegistry.registerItem(upgrade_effect_1, upgrade_effect_1.getUnlocalizedName());
		GameRegistry.registerItem(upgrade_effect_2, upgrade_effect_2.getUnlocalizedName());
		GameRegistry.registerItem(upgrade_effect_3, upgrade_effect_3.getUnlocalizedName());
		GameRegistry.registerItem(upgrade_power_1, upgrade_power_1.getUnlocalizedName());
		GameRegistry.registerItem(upgrade_power_2, upgrade_power_2.getUnlocalizedName());
		GameRegistry.registerItem(upgrade_power_3, upgrade_power_3.getUnlocalizedName());
		GameRegistry.registerItem(upgrade_fortune_1, upgrade_fortune_1.getUnlocalizedName());
		GameRegistry.registerItem(upgrade_fortune_2, upgrade_fortune_2.getUnlocalizedName());
		GameRegistry.registerItem(upgrade_fortune_3, upgrade_fortune_3.getUnlocalizedName());
		GameRegistry.registerItem(upgrade_afterburn_1, upgrade_afterburn_1.getUnlocalizedName());
		GameRegistry.registerItem(upgrade_afterburn_2, upgrade_afterburn_2.getUnlocalizedName());
		GameRegistry.registerItem(upgrade_afterburn_3, upgrade_afterburn_3.getUnlocalizedName());
		GameRegistry.registerItem(upgrade_overdrive_1, upgrade_overdrive_1.getUnlocalizedName());
		GameRegistry.registerItem(upgrade_overdrive_2, upgrade_overdrive_2.getUnlocalizedName());
		GameRegistry.registerItem(upgrade_overdrive_3, upgrade_overdrive_3.getUnlocalizedName());
		GameRegistry.registerItem(upgrade_radius, upgrade_radius.getUnlocalizedName());
		GameRegistry.registerItem(upgrade_health, upgrade_health.getUnlocalizedName());
		GameRegistry.registerItem(upgrade_smelter, upgrade_smelter.getUnlocalizedName());
		GameRegistry.registerItem(upgrade_shredder, upgrade_shredder.getUnlocalizedName());
		GameRegistry.registerItem(upgrade_centrifuge, upgrade_centrifuge.getUnlocalizedName());
		GameRegistry.registerItem(upgrade_crystallizer, upgrade_crystallizer.getUnlocalizedName());
		GameRegistry.registerItem(upgrade_nullifier, upgrade_nullifier.getUnlocalizedName());
		GameRegistry.registerItem(upgrade_screm, upgrade_screm.getUnlocalizedName());
		GameRegistry.registerItem(upgrade_gc_speed, upgrade_gc_speed.getUnlocalizedName());
		GameRegistry.registerItem(upgrade_5g, upgrade_5g.getUnlocalizedName());
		GameRegistry.registerItem(upgrade_stack, upgrade_stack.getUnlocalizedName());
		GameRegistry.registerItem(upgrade_ejector, upgrade_ejector.getUnlocalizedName());

		//Machine Templates
		GameRegistry.registerItem(siren_track, siren_track.getUnlocalizedName());
		GameRegistry.registerItem(fluid_identifier, fluid_identifier.getUnlocalizedName());
		GameRegistry.registerItem(fluid_identifier_multi, fluid_identifier_multi.getUnlocalizedName());
		GameRegistry.registerItem(fluid_icon, fluid_icon.getUnlocalizedName());
		GameRegistry.registerItem(fluid_duct, fluid_duct.getUnlocalizedName());
		GameRegistry.registerItem(assembly_template, assembly_template.getUnlocalizedName());
		GameRegistry.registerItem(chemistry_template, chemistry_template.getUnlocalizedName());
		GameRegistry.registerItem(chemistry_icon, chemistry_icon.getUnlocalizedName());
		GameRegistry.registerItem(crucible_template, crucible_template.getUnlocalizedName());

		//Machine Items
		GameRegistry.registerItem(fuse, fuse.getUnlocalizedName());
		GameRegistry.registerItem(redcoil_capacitor, redcoil_capacitor.getUnlocalizedName());
		GameRegistry.registerItem(euphemium_capacitor, euphemium_capacitor.getUnlocalizedName());
		GameRegistry.registerItem(screwdriver, screwdriver.getUnlocalizedName());
		GameRegistry.registerItem(screwdriver_desh, screwdriver_desh.getUnlocalizedName());
		GameRegistry.registerItem(hand_drill, hand_drill.getUnlocalizedName());
		GameRegistry.registerItem(hand_drill_desh, hand_drill_desh.getUnlocalizedName());
		GameRegistry.registerItem(chemistry_set, chemistry_set.getUnlocalizedName());
		GameRegistry.registerItem(chemistry_set_boron, chemistry_set_boron.getUnlocalizedName());
		GameRegistry.registerItem(blowtorch, blowtorch.getUnlocalizedName());
		GameRegistry.registerItem(acetylene_torch, acetylene_torch.getUnlocalizedName());
		GameRegistry.registerItem(boltgun, boltgun.getUnlocalizedName());
		GameRegistry.registerItem(overfuse, overfuse.getUnlocalizedName());
		GameRegistry.registerItem(arc_electrode, arc_electrode.getUnlocalizedName());
		GameRegistry.registerItem(arc_electrode_burnt, arc_electrode_burnt.getUnlocalizedName());

		//Particle Collider Fuel
		GameRegistry.registerItem(part_lithium, part_lithium.getUnlocalizedName());
		GameRegistry.registerItem(part_beryllium, part_beryllium.getUnlocalizedName());
		GameRegistry.registerItem(part_carbon, part_carbon.getUnlocalizedName());
		GameRegistry.registerItem(part_copper, part_copper.getUnlocalizedName());
		GameRegistry.registerItem(part_plutonium, part_plutonium.getUnlocalizedName());

		//FEL laser crystals
		GameRegistry.registerItem(laser_crystal_co2, laser_crystal_co2.getUnlocalizedName());
		GameRegistry.registerItem(laser_crystal_bismuth, laser_crystal_bismuth.getUnlocalizedName());
		GameRegistry.registerItem(laser_crystal_cmb, laser_crystal_cmb.getUnlocalizedName());
		GameRegistry.registerItem(laser_crystal_iron, laser_crystal_iron.getUnlocalizedName());
		GameRegistry.registerItem(laser_crystal_dnt, laser_crystal_dnt.getUnlocalizedName());
		//GameRegistry.registerItem(laser_crystal_digamma, laser_crystal_digamma.getUnlocalizedName());

		//Catalyst Rune Sigils
		GameRegistry.registerItem(rune_blank, rune_blank.getUnlocalizedName());
		GameRegistry.registerItem(rune_isa, rune_isa.getUnlocalizedName());
		GameRegistry.registerItem(rune_dagaz, rune_dagaz.getUnlocalizedName());
		GameRegistry.registerItem(rune_hagalaz, rune_hagalaz.getUnlocalizedName());
		GameRegistry.registerItem(rune_jera, rune_jera.getUnlocalizedName());
		GameRegistry.registerItem(rune_thurisaz, rune_thurisaz.getUnlocalizedName());

		//AMS Catalysts
		GameRegistry.registerItem(ams_catalyst_blank, ams_catalyst_blank.getUnlocalizedName());
		GameRegistry.registerItem(ams_catalyst_aluminium, ams_catalyst_aluminium.getUnlocalizedName());
		GameRegistry.registerItem(ams_catalyst_beryllium, ams_catalyst_beryllium.getUnlocalizedName());
		GameRegistry.registerItem(ams_catalyst_caesium, ams_catalyst_caesium.getUnlocalizedName());
		GameRegistry.registerItem(ams_catalyst_cerium, ams_catalyst_cerium.getUnlocalizedName());
		GameRegistry.registerItem(ams_catalyst_cobalt, ams_catalyst_cobalt.getUnlocalizedName());
		GameRegistry.registerItem(ams_catalyst_copper, ams_catalyst_copper.getUnlocalizedName());
		GameRegistry.registerItem(ams_catalyst_euphemium, ams_catalyst_euphemium.getUnlocalizedName());
		GameRegistry.registerItem(ams_catalyst_dineutronium, ams_catalyst_dineutronium.getUnlocalizedName());
		GameRegistry.registerItem(ams_catalyst_iron, ams_catalyst_iron.getUnlocalizedName());
		GameRegistry.registerItem(ams_catalyst_lithium, ams_catalyst_lithium.getUnlocalizedName());
		GameRegistry.registerItem(ams_catalyst_niobium, ams_catalyst_niobium.getUnlocalizedName());
		GameRegistry.registerItem(ams_catalyst_schrabidium, ams_catalyst_schrabidium.getUnlocalizedName());
		GameRegistry.registerItem(ams_catalyst_strontium, ams_catalyst_strontium.getUnlocalizedName());
		GameRegistry.registerItem(ams_catalyst_thorium, ams_catalyst_thorium.getUnlocalizedName());
		GameRegistry.registerItem(ams_catalyst_tungsten, ams_catalyst_tungsten.getUnlocalizedName());

		//Shredder Blades
		GameRegistry.registerItem(blades_steel, blades_steel.getUnlocalizedName());
		GameRegistry.registerItem(blades_titanium, blades_titanium.getUnlocalizedName());
		GameRegistry.registerItem(blades_advanced_alloy, blades_advanced_alloy.getUnlocalizedName());
		GameRegistry.registerItem(blades_desh, blades_desh.getUnlocalizedName());

		//Generator Stuff
		GameRegistry.registerItem(thermo_element, thermo_element.getUnlocalizedName());
		GameRegistry.registerItem(catalytic_converter, catalytic_converter.getUnlocalizedName());

		//AMS Components
		GameRegistry.registerItem(ams_focus_blank, ams_focus_blank.getUnlocalizedName());
		GameRegistry.registerItem(ams_focus_limiter, ams_focus_limiter.getUnlocalizedName());
		GameRegistry.registerItem(ams_focus_booster, ams_focus_booster.getUnlocalizedName());
		GameRegistry.registerItem(ams_muzzle, ams_muzzle.getUnlocalizedName());
		GameRegistry.registerItem(ams_lens, ams_lens.getUnlocalizedName());
		GameRegistry.registerItem(ams_core_sing, ams_core_sing.getUnlocalizedName());
		GameRegistry.registerItem(ams_core_wormhole, ams_core_wormhole.getUnlocalizedName());
		GameRegistry.registerItem(ams_core_eyeofharmony, ams_core_eyeofharmony.getUnlocalizedName());
		GameRegistry.registerItem(ams_core_thingy, ams_core_thingy.getUnlocalizedName());

		//Fusion Shields
		GameRegistry.registerItem(fusion_shield_tungsten, fusion_shield_tungsten.getUnlocalizedName());
		GameRegistry.registerItem(fusion_shield_beryllium, fusion_shield_beryllium.getUnlocalizedName());
		GameRegistry.registerItem(fusion_shield_desh, fusion_shield_desh.getUnlocalizedName());
		GameRegistry.registerItem(fusion_shield_chlorophyte, fusion_shield_chlorophyte.getUnlocalizedName());
		GameRegistry.registerItem(fusion_shield_vaporwave, fusion_shield_vaporwave.getUnlocalizedName());

		//Breeding Rods
		GameRegistry.registerItem(rod_empty, rod_empty.getUnlocalizedName());
		GameRegistry.registerItem(rod, rod.getUnlocalizedName());
		GameRegistry.registerItem(rod_dual_empty, rod_dual_empty.getUnlocalizedName());
		GameRegistry.registerItem(rod_dual, rod_dual.getUnlocalizedName());
		GameRegistry.registerItem(rod_quad_empty, rod_quad_empty.getUnlocalizedName());
		GameRegistry.registerItem(rod_quad, rod_quad.getUnlocalizedName());

		//ZIRNOX parts
		GameRegistry.registerItem(rod_zirnox_empty, rod_zirnox_empty.getUnlocalizedName());
		//GameRegistry.registerItem(rod_zirnox_natural_uranium_fuel, rod_zirnox_natural_uranium_fuel.getUnlocalizedName());
		//GameRegistry.registerItem(rod_zirnox_uranium_fuel, rod_zirnox_uranium_fuel.getUnlocalizedName());
		//GameRegistry.registerItem(rod_zirnox_th232, rod_zirnox_th232.getUnlocalizedName());
		//GameRegistry.registerItem(rod_zirnox_thorium_fuel, rod_zirnox_thorium_fuel.getUnlocalizedName());
		//GameRegistry.registerItem(rod_zirnox_mox_fuel, rod_zirnox_mox_fuel.getUnlocalizedName());
		//GameRegistry.registerItem(rod_zirnox_plutonium_fuel, rod_zirnox_plutonium_fuel.getUnlocalizedName());
		//GameRegistry.registerItem(rod_zirnox_u233_fuel, rod_zirnox_u233_fuel.getUnlocalizedName());
		//GameRegistry.registerItem(rod_zirnox_u235_fuel, rod_zirnox_u235_fuel.getUnlocalizedName());
		//GameRegistry.registerItem(rod_zirnox_les_fuel, rod_zirnox_les_fuel.getUnlocalizedName());
		//GameRegistry.registerItem(rod_zirnox_lithium, rod_zirnox_lithium.getUnlocalizedName());
		GameRegistry.registerItem(rod_zirnox_tritium, rod_zirnox_tritium.getUnlocalizedName());
		//GameRegistry.registerItem(rod_zirnox_zfb_mox, rod_zirnox_zfb_mox.getUnlocalizedName());
		GameRegistry.registerItem(rod_zirnox, rod_zirnox.getUnlocalizedName());

		GameRegistry.registerItem(rod_zirnox_natural_uranium_fuel_depleted, rod_zirnox_natural_uranium_fuel_depleted.getUnlocalizedName());
		GameRegistry.registerItem(rod_zirnox_uranium_fuel_depleted, rod_zirnox_uranium_fuel_depleted.getUnlocalizedName());
		GameRegistry.registerItem(rod_zirnox_thorium_fuel_depleted, rod_zirnox_thorium_fuel_depleted.getUnlocalizedName());
		GameRegistry.registerItem(rod_zirnox_mox_fuel_depleted, rod_zirnox_mox_fuel_depleted.getUnlocalizedName());
		GameRegistry.registerItem(rod_zirnox_plutonium_fuel_depleted, rod_zirnox_plutonium_fuel_depleted.getUnlocalizedName());
		GameRegistry.registerItem(rod_zirnox_u233_fuel_depleted, rod_zirnox_u233_fuel_depleted.getUnlocalizedName());
		GameRegistry.registerItem(rod_zirnox_u235_fuel_depleted, rod_zirnox_u235_fuel_depleted.getUnlocalizedName());
		GameRegistry.registerItem(rod_zirnox_les_fuel_depleted, rod_zirnox_les_fuel_depleted.getUnlocalizedName());
		GameRegistry.registerItem(rod_zirnox_zfb_mox_depleted, rod_zirnox_zfb_mox_depleted.getUnlocalizedName());

		//Depleted Fuel
		GameRegistry.registerItem(waste_natural_uranium, waste_natural_uranium.getUnlocalizedName());
		GameRegistry.registerItem(waste_uranium, waste_uranium.getUnlocalizedName());
		GameRegistry.registerItem(waste_thorium, waste_thorium.getUnlocalizedName());
		GameRegistry.registerItem(waste_mox, waste_mox.getUnlocalizedName());
		GameRegistry.registerItem(waste_plutonium, waste_plutonium.getUnlocalizedName());
		GameRegistry.registerItem(waste_u233, waste_u233.getUnlocalizedName());
		GameRegistry.registerItem(waste_u235, waste_u235.getUnlocalizedName());
		GameRegistry.registerItem(waste_schrabidium, waste_schrabidium.getUnlocalizedName());
		GameRegistry.registerItem(waste_zfb_mox, waste_zfb_mox.getUnlocalizedName());

		GameRegistry.registerItem(waste_plate_u233, waste_plate_u233.getUnlocalizedName());
		GameRegistry.registerItem(waste_plate_u235, waste_plate_u235.getUnlocalizedName());
		GameRegistry.registerItem(waste_plate_mox, waste_plate_mox.getUnlocalizedName());
		GameRegistry.registerItem(waste_plate_pu239, waste_plate_pu239.getUnlocalizedName());
		GameRegistry.registerItem(waste_plate_ra226be, waste_plate_ra226be.getUnlocalizedName());
		GameRegistry.registerItem(waste_plate_sa326, waste_plate_sa326.getUnlocalizedName());
		GameRegistry.registerItem(waste_plate_pu238be, waste_plate_pu238be.getUnlocalizedName());

		//Pile parts
		GameRegistry.registerItem(pile_rod_uranium, pile_rod_uranium.getUnlocalizedName());
		GameRegistry.registerItem(pile_rod_pu239, pile_rod_pu239.getUnlocalizedName());
		GameRegistry.registerItem(pile_rod_plutonium, pile_rod_plutonium.getUnlocalizedName());
		GameRegistry.registerItem(pile_rod_source, pile_rod_source.getUnlocalizedName());
		GameRegistry.registerItem(pile_rod_boron, pile_rod_boron.getUnlocalizedName());
		GameRegistry.registerItem(pile_rod_lithium, pile_rod_lithium.getUnlocalizedName());
		GameRegistry.registerItem(pile_rod_detector, pile_rod_detector.getUnlocalizedName());

		//Plate Fuels
		GameRegistry.registerItem(plate_fuel_u233, plate_fuel_u233.getUnlocalizedName());
		GameRegistry.registerItem(plate_fuel_u235, plate_fuel_u235.getUnlocalizedName());
		GameRegistry.registerItem(plate_fuel_mox, plate_fuel_mox.getUnlocalizedName());
		GameRegistry.registerItem(plate_fuel_pu239, plate_fuel_pu239.getUnlocalizedName());
		GameRegistry.registerItem(plate_fuel_sa326, plate_fuel_sa326.getUnlocalizedName());
		GameRegistry.registerItem(plate_fuel_ra226be, plate_fuel_ra226be.getUnlocalizedName());
		GameRegistry.registerItem(plate_fuel_pu238be, plate_fuel_pu238be.getUnlocalizedName());

		//PWR Parts
		GameRegistry.registerItem(pwr_fuel, pwr_fuel.getUnlocalizedName());
		GameRegistry.registerItem(pwr_fuel_hot, pwr_fuel_hot.getUnlocalizedName());
		GameRegistry.registerItem(pwr_fuel_depleted, pwr_fuel_depleted.getUnlocalizedName());

		//RBMK parts
		GameRegistry.registerItem(rbmk_lid, rbmk_lid.getUnlocalizedName());
		GameRegistry.registerItem(rbmk_lid_glass, rbmk_lid_glass.getUnlocalizedName());
		GameRegistry.registerItem(rbmk_fuel_empty, rbmk_fuel_empty.getUnlocalizedName());
		GameRegistry.registerItem(rbmk_fuel_ueu, rbmk_fuel_ueu.getUnlocalizedName());
		GameRegistry.registerItem(rbmk_fuel_meu, rbmk_fuel_meu.getUnlocalizedName());
		GameRegistry.registerItem(rbmk_fuel_heu233, rbmk_fuel_heu233.getUnlocalizedName());
		GameRegistry.registerItem(rbmk_fuel_heu235, rbmk_fuel_heu235.getUnlocalizedName());
		GameRegistry.registerItem(rbmk_fuel_thmeu, rbmk_fuel_thmeu.getUnlocalizedName());
		GameRegistry.registerItem(rbmk_fuel_lep, rbmk_fuel_lep.getUnlocalizedName());
		GameRegistry.registerItem(rbmk_fuel_mep, rbmk_fuel_mep.getUnlocalizedName());
		GameRegistry.registerItem(rbmk_fuel_hep239, rbmk_fuel_hep239.getUnlocalizedName());
		GameRegistry.registerItem(rbmk_fuel_hep241, rbmk_fuel_hep241.getUnlocalizedName());
		GameRegistry.registerItem(rbmk_fuel_lea, rbmk_fuel_lea.getUnlocalizedName());
		GameRegistry.registerItem(rbmk_fuel_mea, rbmk_fuel_mea.getUnlocalizedName());
		GameRegistry.registerItem(rbmk_fuel_hea241, rbmk_fuel_hea241.getUnlocalizedName());
		GameRegistry.registerItem(rbmk_fuel_hea242, rbmk_fuel_hea242.getUnlocalizedName());
		GameRegistry.registerItem( rbmk_fuel_bk247,  rbmk_fuel_bk247.getUnlocalizedName());
		GameRegistry.registerItem(rbmk_fuel_men, rbmk_fuel_men.getUnlocalizedName());
		GameRegistry.registerItem(rbmk_fuel_hen, rbmk_fuel_hen.getUnlocalizedName());
		GameRegistry.registerItem(rbmk_fuel_mox, rbmk_fuel_mox.getUnlocalizedName());
		GameRegistry.registerItem(rbmk_fuel_les, rbmk_fuel_les.getUnlocalizedName());
		GameRegistry.registerItem(rbmk_fuel_mes, rbmk_fuel_mes.getUnlocalizedName());
		GameRegistry.registerItem(rbmk_fuel_hes, rbmk_fuel_hes.getUnlocalizedName());
		GameRegistry.registerItem(rbmk_fuel_leaus, rbmk_fuel_leaus.getUnlocalizedName());
		GameRegistry.registerItem(rbmk_fuel_heaus, rbmk_fuel_heaus.getUnlocalizedName());
		GameRegistry.registerItem(rbmk_fuel_po210be, rbmk_fuel_po210be.getUnlocalizedName());
		GameRegistry.registerItem(rbmk_fuel_ra226be, rbmk_fuel_ra226be.getUnlocalizedName());
		GameRegistry.registerItem(rbmk_fuel_pu238be, rbmk_fuel_pu238be.getUnlocalizedName());
		GameRegistry.registerItem(rbmk_fuel_balefire_gold, rbmk_fuel_balefire_gold.getUnlocalizedName());
		GameRegistry.registerItem(rbmk_fuel_flashlead, rbmk_fuel_flashlead.getUnlocalizedName());
		GameRegistry.registerItem(rbmk_fuel_balefire, rbmk_fuel_balefire.getUnlocalizedName());
		GameRegistry.registerItem(rbmk_fuel_zfb_bismuth, rbmk_fuel_zfb_bismuth.getUnlocalizedName());
		GameRegistry.registerItem(rbmk_fuel_zfb_pu241, rbmk_fuel_zfb_pu241.getUnlocalizedName());
		GameRegistry.registerItem(rbmk_fuel_zfb_am_mix, rbmk_fuel_zfb_am_mix.getUnlocalizedName());
		//GameRegistry.registerItem(rbmk_fuel_drx, rbmk_fuel_drx.getUnlocalizedName());
		GameRegistry.registerItem(rbmk_fuel_test, rbmk_fuel_test.getUnlocalizedName());
		GameRegistry.registerItem(rbmk_fuel_lecm, rbmk_fuel_lecm.getUnlocalizedName());
		GameRegistry.registerItem(rbmk_fuel_mecm, rbmk_fuel_mecm.getUnlocalizedName());
		GameRegistry.registerItem(rbmk_fuel_hecm, rbmk_fuel_hecm.getUnlocalizedName());

		GameRegistry.registerItem(rbmk_pellet_ueu, rbmk_pellet_ueu.getUnlocalizedName());
		GameRegistry.registerItem(rbmk_pellet_meu, rbmk_pellet_meu.getUnlocalizedName());
		GameRegistry.registerItem(rbmk_pellet_heu233, rbmk_pellet_heu233.getUnlocalizedName());
		GameRegistry.registerItem(rbmk_pellet_heu235, rbmk_pellet_heu235.getUnlocalizedName());
		GameRegistry.registerItem(rbmk_pellet_thmeu, rbmk_pellet_thmeu.getUnlocalizedName());
		GameRegistry.registerItem(rbmk_pellet_lep, rbmk_pellet_lep.getUnlocalizedName());
		GameRegistry.registerItem(rbmk_pellet_mep, rbmk_pellet_mep.getUnlocalizedName());
		GameRegistry.registerItem(rbmk_pellet_hep239, rbmk_pellet_hep239.getUnlocalizedName());
		GameRegistry.registerItem(rbmk_pellet_hep241, rbmk_pellet_hep241.getUnlocalizedName());
		GameRegistry.registerItem(rbmk_pellet_lea, rbmk_pellet_lea.getUnlocalizedName());
		GameRegistry.registerItem(rbmk_pellet_mea, rbmk_pellet_mea.getUnlocalizedName());
		GameRegistry.registerItem(rbmk_pellet_hea241, rbmk_pellet_hea241.getUnlocalizedName());
		GameRegistry.registerItem(rbmk_pellet_hea242, rbmk_pellet_hea242.getUnlocalizedName());
		GameRegistry.registerItem(rbmk_pellet_bk247, rbmk_pellet_bk247.getUnlocalizedName());
		GameRegistry.registerItem(rbmk_pellet_men, rbmk_pellet_men.getUnlocalizedName());
		GameRegistry.registerItem(rbmk_pellet_hen, rbmk_pellet_hen.getUnlocalizedName());
		GameRegistry.registerItem(rbmk_pellet_mox, rbmk_pellet_mox.getUnlocalizedName());
		GameRegistry.registerItem(rbmk_pellet_les, rbmk_pellet_les.getUnlocalizedName());
		GameRegistry.registerItem(rbmk_pellet_mes, rbmk_pellet_mes.getUnlocalizedName());
		GameRegistry.registerItem(rbmk_pellet_hes, rbmk_pellet_hes.getUnlocalizedName());
		GameRegistry.registerItem(rbmk_pellet_leaus, rbmk_pellet_leaus.getUnlocalizedName());
		GameRegistry.registerItem(rbmk_pellet_heaus, rbmk_pellet_heaus.getUnlocalizedName());
		GameRegistry.registerItem(rbmk_pellet_po210be, rbmk_pellet_po210be.getUnlocalizedName());
		GameRegistry.registerItem(rbmk_pellet_ra226be, rbmk_pellet_ra226be.getUnlocalizedName());
		GameRegistry.registerItem(rbmk_pellet_pu238be, rbmk_pellet_pu238be.getUnlocalizedName());
		GameRegistry.registerItem(rbmk_pellet_balefire_gold, rbmk_pellet_balefire_gold.getUnlocalizedName());
		GameRegistry.registerItem(rbmk_pellet_flashlead, rbmk_pellet_flashlead.getUnlocalizedName());
		GameRegistry.registerItem(rbmk_pellet_balefire, rbmk_pellet_balefire.getUnlocalizedName());
		GameRegistry.registerItem(rbmk_pellet_zfb_bismuth, rbmk_pellet_zfb_bismuth.getUnlocalizedName());
		GameRegistry.registerItem(rbmk_pellet_zfb_pu241, rbmk_pellet_zfb_pu241.getUnlocalizedName());
		GameRegistry.registerItem(rbmk_pellet_zfb_am_mix, rbmk_pellet_zfb_am_mix.getUnlocalizedName());
		//GameRegistry.registerItem(rbmk_pellet_drx, rbmk_pellet_drx.getUnlocalizedName());
		GameRegistry.registerItem(rbmk_pellet_lecm, rbmk_pellet_lecm.getUnlocalizedName());
		GameRegistry.registerItem(rbmk_pellet_mecm, rbmk_pellet_mecm.getUnlocalizedName());
		GameRegistry.registerItem(rbmk_pellet_hecm, rbmk_pellet_hecm.getUnlocalizedName());

		GameRegistry.registerItem(watz_pellet, watz_pellet.getUnlocalizedName());
		GameRegistry.registerItem(watz_pellet_depleted, watz_pellet_depleted.getUnlocalizedName());
		GameRegistry.registerItem(powder_spent_haleu, powder_spent_haleu.getUnlocalizedName());
		GameRegistry.registerItem(powder_spent_leu, powder_spent_leu.getUnlocalizedName());
		GameRegistry.registerItem(powder_spent_thorium, powder_spent_thorium.getUnlocalizedName());
		GameRegistry.registerItem(powder_spent_u233, powder_spent_u233.getUnlocalizedName());
		GameRegistry.registerItem(powder_spent_mox, powder_spent_mox.getUnlocalizedName());

		GameRegistry.registerItem(dust_graphite, dust_graphite.getUnlocalizedName());
		GameRegistry.registerItem(powder_lead_irradiated, powder_lead_irradiated.getUnlocalizedName());
		GameRegistry.registerItem(powder_boron_spent, powder_boron_spent.getUnlocalizedName());
		GameRegistry.registerItem(powder_du_spent, powder_du_spent.getUnlocalizedName());

		GameRegistry.registerItem(icf_pellet_empty, icf_pellet_empty.getUnlocalizedName());
		GameRegistry.registerItem(icf_pellet, icf_pellet.getUnlocalizedName());
		GameRegistry.registerItem(icf_pellet_depleted, icf_pellet_depleted.getUnlocalizedName());

		GameRegistry.registerItem(debris_graphite, debris_graphite.getUnlocalizedName());
		GameRegistry.registerItem(debris_metal, debris_metal.getUnlocalizedName());
		GameRegistry.registerItem(debris_fuel, debris_fuel.getUnlocalizedName());
		GameRegistry.registerItem(debris_concrete, debris_concrete.getUnlocalizedName());
		GameRegistry.registerItem(debris_exchanger, debris_exchanger.getUnlocalizedName());
		GameRegistry.registerItem(debris_shrapnel, debris_shrapnel.getUnlocalizedName());
		GameRegistry.registerItem(debris_element, debris_element.getUnlocalizedName());
		GameRegistry.registerItem(undefined, undefined.getUnlocalizedName());

		GameRegistry.registerItem(scrap_plastic, scrap_plastic.getUnlocalizedName());
		GameRegistry.registerItem(scrap, scrap.getUnlocalizedName());
		GameRegistry.registerItem(scrap_oil, scrap_oil.getUnlocalizedName());
		GameRegistry.registerItem(scrap_nuclear, scrap_nuclear.getUnlocalizedName());
		GameRegistry.registerItem(trinitite, trinitite.getUnlocalizedName());
		GameRegistry.registerItem(nuclear_waste_long, nuclear_waste_long.getUnlocalizedName());
		GameRegistry.registerItem(nuclear_waste_long_tiny, nuclear_waste_long_tiny.getUnlocalizedName());
		GameRegistry.registerItem(nuclear_waste_short, nuclear_waste_short.getUnlocalizedName());
		GameRegistry.registerItem(nuclear_waste_short_tiny, nuclear_waste_short_tiny.getUnlocalizedName());
		GameRegistry.registerItem(nuclear_waste_long_depleted, nuclear_waste_long_depleted.getUnlocalizedName());
		GameRegistry.registerItem(nuclear_waste_long_depleted_tiny, nuclear_waste_long_depleted_tiny.getUnlocalizedName());
		GameRegistry.registerItem(nuclear_waste_short_depleted, nuclear_waste_short_depleted.getUnlocalizedName());
		GameRegistry.registerItem(nuclear_waste_short_depleted_tiny, nuclear_waste_short_depleted_tiny.getUnlocalizedName());
		GameRegistry.registerItem(nuclear_waste, nuclear_waste.getUnlocalizedName());
		GameRegistry.registerItem(nuclear_waste_tiny, nuclear_waste_tiny.getUnlocalizedName());
		GameRegistry.registerItem(nuclear_waste_vitrified, nuclear_waste_vitrified.getUnlocalizedName());
		GameRegistry.registerItem(nuclear_waste_vitrified_tiny, nuclear_waste_vitrified_tiny.getUnlocalizedName());

		//Spawners
		//GameRegistry.registerItem(spawn_chopper, spawn_chopper.getUnlocalizedName());
		GameRegistry.registerItem(spawn_worm, spawn_worm.getUnlocalizedName());
		//GameRegistry.registerItem(spawn_ufo, spawn_ufo.getUnlocalizedName());
		//GameRegistry.registerItem(spawn_duck, spawn_duck.getUnlocalizedName());
		GameRegistry.registerItem(spawn_uap, spawn_uap.getUnlocalizedName());

		//Computer Tools
		GameRegistry.registerItem(designator, designator.getUnlocalizedName());
		GameRegistry.registerItem(designator_range, designator_range.getUnlocalizedName());
		GameRegistry.registerItem(designator_manual, designator_manual.getUnlocalizedName());
		GameRegistry.registerItem(designator_arty_range, designator_arty_range.getUnlocalizedName());
		GameRegistry.registerItem(turret_chip, turret_chip.getUnlocalizedName());
		GameRegistry.registerItem(linker, linker.getUnlocalizedName());
		GameRegistry.registerItem(reactor_sensor, reactor_sensor.getUnlocalizedName());
		GameRegistry.registerItem(oil_detector, oil_detector.getUnlocalizedName());
		GameRegistry.registerItem(ore_density_scanner, ore_density_scanner.getUnlocalizedName());
		GameRegistry.registerItem(survey_scanner, survey_scanner.getUnlocalizedName());
		GameRegistry.registerItem(mirror_tool, mirror_tool.getUnlocalizedName());
		GameRegistry.registerItem(rbmk_tool, rbmk_tool.getUnlocalizedName());
		GameRegistry.registerItem(drone_linker, drone_linker.getUnlocalizedName());
		GameRegistry.registerItem(radar_linker, radar_linker.getUnlocalizedName());
		GameRegistry.registerItem(coltan_tool, coltan_tool.getUnlocalizedName());
		GameRegistry.registerItem(power_net_tool, power_net_tool.getUnlocalizedName());
		GameRegistry.registerItem(analysis_tool, analysis_tool.getUnlocalizedName());
		GameRegistry.registerItem(coupling_tool, coupling_tool.getUnlocalizedName());
		GameRegistry.registerItem(settings_tool, settings_tool.getUnlocalizedName());
		GameRegistry.registerItem(dosimeter, dosimeter.getUnlocalizedName());
		GameRegistry.registerItem(geiger_counter, geiger_counter.getUnlocalizedName());
		GameRegistry.registerItem(digamma_diagnostic, digamma_diagnostic.getUnlocalizedName());
		GameRegistry.registerItem(pollution_detector, pollution_detector.getUnlocalizedName());
		GameRegistry.registerItem(containment_box, containment_box.getUnlocalizedName());
		GameRegistry.registerItem(plastic_bag, plastic_bag.getUnlocalizedName());

		//Keys and Locks
		GameRegistry.registerItem(key, key.getUnlocalizedName());
		GameRegistry.registerItem(key_red, key_red.getUnlocalizedName());
		GameRegistry.registerItem(key_red_cracked, key_red_cracked.getUnlocalizedName());
		GameRegistry.registerItem(key_kit, key_kit.getUnlocalizedName());
		GameRegistry.registerItem(key_fake, key_fake.getUnlocalizedName());
		GameRegistry.registerItem(mech_key, mech_key.getUnlocalizedName());
		GameRegistry.registerItem(pin, pin.getUnlocalizedName());
		GameRegistry.registerItem(padlock_rusty, padlock_rusty.getUnlocalizedName());
		GameRegistry.registerItem(padlock, padlock.getUnlocalizedName());
		GameRegistry.registerItem(padlock_reinforced, padlock_reinforced.getUnlocalizedName());
		GameRegistry.registerItem(padlock_unbreakable, padlock_unbreakable.getUnlocalizedName());
		GameRegistry.registerItem(launch_code_piece, launch_code_piece.getUnlocalizedName());
		GameRegistry.registerItem(launch_code, launch_code.getUnlocalizedName());
		GameRegistry.registerItem(launch_key, launch_key.getUnlocalizedName());

		//Missiles
		//Tier 0
		GameRegistry.registerItem(missile_test, missile_test.getUnlocalizedName());
		//GameRegistry.registerItem(missile_taint, missile_taint.getUnlocalizedName());
		GameRegistry.registerItem(missile_micro, missile_micro.getUnlocalizedName());
		//GameRegistry.registerItem(missile_bhole, missile_bhole.getUnlocalizedName());
		//GameRegistry.registerItem(missile_schrabidium, missile_schrabidium.getUnlocalizedName());
		GameRegistry.registerItem(missile_emp, missile_emp.getUnlocalizedName());
		//Tier 1
		GameRegistry.registerItem(missile_generic, missile_generic.getUnlocalizedName());
		GameRegistry.registerItem(missile_decoy, missile_decoy.getUnlocalizedName());
		GameRegistry.registerItem(missile_incendiary, missile_incendiary.getUnlocalizedName());
		GameRegistry.registerItem(missile_cluster, missile_cluster.getUnlocalizedName());
		GameRegistry.registerItem(missile_buster, missile_buster.getUnlocalizedName());
		GameRegistry.registerItem(missile_stealth, missile_stealth.getUnlocalizedName());
		GameRegistry.registerItem(missile_anti_ballistic, missile_anti_ballistic.getUnlocalizedName());
		//Tier 2
		GameRegistry.registerItem(missile_strong, missile_strong.getUnlocalizedName());
		GameRegistry.registerItem(missile_incendiary_strong, missile_incendiary_strong.getUnlocalizedName());
		GameRegistry.registerItem(missile_cluster_strong, missile_cluster_strong.getUnlocalizedName());
		GameRegistry.registerItem(missile_buster_strong, missile_buster_strong.getUnlocalizedName());
		GameRegistry.registerItem(missile_emp_strong, missile_emp_strong.getUnlocalizedName());
		//Tier 3
		GameRegistry.registerItem(missile_burst, missile_burst.getUnlocalizedName());
		GameRegistry.registerItem(missile_inferno, missile_inferno.getUnlocalizedName());
		GameRegistry.registerItem(missile_rain, missile_rain.getUnlocalizedName());
		GameRegistry.registerItem(missile_drill, missile_drill.getUnlocalizedName());
		GameRegistry.registerItem(missile_shuttle, missile_shuttle.getUnlocalizedName());
		//Tier 4
		GameRegistry.registerItem(missile_nuclear, missile_nuclear.getUnlocalizedName());
		GameRegistry.registerItem(missile_nuclear_cluster, missile_nuclear_cluster.getUnlocalizedName());
		//GameRegistry.registerItem(missile_volcano, missile_volcano.getUnlocalizedName());
		GameRegistry.registerItem(missile_doomsday, missile_doomsday.getUnlocalizedName());
		GameRegistry.registerItem(missile_doomsday_rusted, missile_doomsday_rusted.getUnlocalizedName());
		//Rockets
		GameRegistry.registerItem(missile_carrier, missile_carrier.getUnlocalizedName());
		GameRegistry.registerItem(missile_soyuz, missile_soyuz.getUnlocalizedName());
		GameRegistry.registerItem(missile_soyuz_lander, missile_soyuz_lander.getUnlocalizedName());
		GameRegistry.registerItem(missile_custom, missile_custom.getUnlocalizedName());
		GameRegistry.registerItem(rocket_custom, rocket_custom.getUnlocalizedName());

		//Missile Parts
		GameRegistry.registerItem(mp_thruster_10_kerosene, mp_thruster_10_kerosene.getUnlocalizedName());
		GameRegistry.registerItem(mp_thruster_10_kerosene_tec, mp_thruster_10_kerosene_tec.getUnlocalizedName());
		GameRegistry.registerItem(mp_thruster_10_solid, mp_thruster_10_solid.getUnlocalizedName());
		GameRegistry.registerItem(mp_thruster_10_hydrazine, mp_thruster_10_hydrazine.getUnlocalizedName());
		GameRegistry.registerItem(mp_thruster_10_xenon, mp_thruster_10_xenon.getUnlocalizedName());
		GameRegistry.registerItem(mp_thruster_15_kerosene, mp_thruster_15_kerosene.getUnlocalizedName());
		GameRegistry.registerItem(mp_thruster_15_kerosene_tec, mp_thruster_15_kerosene_tec.getUnlocalizedName());
		GameRegistry.registerItem(mp_thruster_15_kerosene_dual, mp_thruster_15_kerosene_dual.getUnlocalizedName());
		GameRegistry.registerItem(mp_thruster_15_kerosene_triple, mp_thruster_15_kerosene_triple.getUnlocalizedName());
		GameRegistry.registerItem(mp_thruster_15_solid, mp_thruster_15_solid.getUnlocalizedName());
		GameRegistry.registerItem(mp_thruster_15_solid_hexdecuple, mp_thruster_15_solid_hexdecuple.getUnlocalizedName());
		GameRegistry.registerItem(mp_thruster_15_hydrogen, mp_thruster_15_hydrogen.getUnlocalizedName());
		GameRegistry.registerItem(mp_thruster_15_hydrogen_dual, mp_thruster_15_hydrogen_dual.getUnlocalizedName());
		GameRegistry.registerItem(mp_thruster_15_balefire_short, mp_thruster_15_balefire_short.getUnlocalizedName());
		GameRegistry.registerItem(mp_thruster_15_balefire, mp_thruster_15_balefire.getUnlocalizedName());
		GameRegistry.registerItem(mp_thruster_15_balefire_large, mp_thruster_15_balefire_large.getUnlocalizedName());
		GameRegistry.registerItem(mp_thruster_15_balefire_large_rad, mp_thruster_15_balefire_large_rad.getUnlocalizedName());
		GameRegistry.registerItem(mp_thruster_20_kerosene, mp_thruster_20_kerosene.getUnlocalizedName());
		GameRegistry.registerItem(mp_thruster_20_kerosene_dual, mp_thruster_20_kerosene_dual.getUnlocalizedName());
		GameRegistry.registerItem(mp_thruster_20_kerosene_triple, mp_thruster_20_kerosene_triple.getUnlocalizedName());
		GameRegistry.registerItem(mp_thruster_20_methalox, mp_thruster_20_methalox.getUnlocalizedName());
		GameRegistry.registerItem(mp_thruster_20_methalox_dual, mp_thruster_20_methalox_dual.getUnlocalizedName());
		GameRegistry.registerItem(mp_thruster_20_methalox_triple, mp_thruster_20_methalox_triple.getUnlocalizedName());
		GameRegistry.registerItem(mp_thruster_20_hydrogen, mp_thruster_20_hydrogen.getUnlocalizedName());
		GameRegistry.registerItem(mp_thruster_20_hydrogen_dual, mp_thruster_20_hydrogen_dual.getUnlocalizedName());
		GameRegistry.registerItem(mp_thruster_20_hydrogen_triple, mp_thruster_20_hydrogen_triple.getUnlocalizedName());
		GameRegistry.registerItem(mp_thruster_20_solid, mp_thruster_20_solid.getUnlocalizedName());
		GameRegistry.registerItem(mp_thruster_20_solid_multi, mp_thruster_20_solid_multi.getUnlocalizedName());
		GameRegistry.registerItem(mp_thruster_20_solid_multier, mp_thruster_20_solid_multier.getUnlocalizedName());
		GameRegistry.registerItem(mp_thruster_20_hydrazine, mp_thruster_20_hydrazine.getUnlocalizedName());

		GameRegistry.registerItem(mp_stability_10_flat, mp_stability_10_flat.getUnlocalizedName());
		GameRegistry.registerItem(mp_stability_10_cruise, mp_stability_10_cruise.getUnlocalizedName());
		GameRegistry.registerItem(mp_stability_10_space, mp_stability_10_space.getUnlocalizedName());
		GameRegistry.registerItem(mp_stability_15_flat, mp_stability_15_flat.getUnlocalizedName());
		GameRegistry.registerItem(mp_stability_15_thin, mp_stability_15_thin.getUnlocalizedName());
		GameRegistry.registerItem(mp_stability_15_soyuz, mp_stability_15_soyuz.getUnlocalizedName());
		GameRegistry.registerItem(mp_stability_20_flat, mp_stability_20_flat.getUnlocalizedName());
		GameRegistry.registerItem(mp_fuselage_10_kerosene, mp_fuselage_10_kerosene.getUnlocalizedName());
		GameRegistry.registerItem(mp_fuselage_10_kerosene_camo, mp_fuselage_10_kerosene_camo.getUnlocalizedName());
		GameRegistry.registerItem(mp_fuselage_10_kerosene_desert, mp_fuselage_10_kerosene_desert.getUnlocalizedName());
		GameRegistry.registerItem(mp_fuselage_10_kerosene_sky, mp_fuselage_10_kerosene_sky.getUnlocalizedName());
		GameRegistry.registerItem(mp_fuselage_10_kerosene_flames, mp_fuselage_10_kerosene_flames.getUnlocalizedName());
		GameRegistry.registerItem(mp_fuselage_10_kerosene_insulation, mp_fuselage_10_kerosene_insulation.getUnlocalizedName());
		GameRegistry.registerItem(mp_fuselage_10_kerosene_sleek, mp_fuselage_10_kerosene_sleek.getUnlocalizedName());
		GameRegistry.registerItem(mp_fuselage_10_kerosene_metal, mp_fuselage_10_kerosene_metal.getUnlocalizedName());
		GameRegistry.registerItem(mp_fuselage_10_kerosene_taint, mp_fuselage_10_kerosene_taint.getUnlocalizedName());
		GameRegistry.registerItem(mp_fuselage_10_solid, mp_fuselage_10_solid.getUnlocalizedName());
		GameRegistry.registerItem(mp_fuselage_10_hydrazine, mp_fuselage_10_hydrazine.getUnlocalizedName());
		GameRegistry.registerItem(mp_fuselage_10_solid_flames, mp_fuselage_10_solid_flames.getUnlocalizedName());
		GameRegistry.registerItem(mp_fuselage_10_solid_insulation, mp_fuselage_10_solid_insulation.getUnlocalizedName());
		GameRegistry.registerItem(mp_fuselage_10_solid_sleek, mp_fuselage_10_solid_sleek.getUnlocalizedName());
		GameRegistry.registerItem(mp_fuselage_10_solid_soviet_glory, mp_fuselage_10_solid_soviet_glory.getUnlocalizedName());
		GameRegistry.registerItem(mp_fuselage_10_solid_cathedral, mp_fuselage_10_solid_cathedral.getUnlocalizedName());
		GameRegistry.registerItem(mp_fuselage_10_solid_moonlit, mp_fuselage_10_solid_moonlit.getUnlocalizedName());
		GameRegistry.registerItem(mp_fuselage_10_solid_battery, mp_fuselage_10_solid_battery.getUnlocalizedName());
		GameRegistry.registerItem(mp_fuselage_10_solid_duracell, mp_fuselage_10_solid_duracell.getUnlocalizedName());
		GameRegistry.registerItem(mp_fuselage_10_xenon, mp_fuselage_10_xenon.getUnlocalizedName());
		GameRegistry.registerItem(mp_fuselage_10_xenon_bhole, mp_fuselage_10_xenon_bhole.getUnlocalizedName());
		GameRegistry.registerItem(mp_fuselage_10_long_kerosene, mp_fuselage_10_long_kerosene.getUnlocalizedName());
		GameRegistry.registerItem(mp_fuselage_10_long_kerosene_camo, mp_fuselage_10_long_kerosene_camo.getUnlocalizedName());
		GameRegistry.registerItem(mp_fuselage_10_long_kerosene_desert, mp_fuselage_10_long_kerosene_desert.getUnlocalizedName());
		GameRegistry.registerItem(mp_fuselage_10_long_kerosene_sky, mp_fuselage_10_long_kerosene_sky.getUnlocalizedName());
		GameRegistry.registerItem(mp_fuselage_10_long_kerosene_flames, mp_fuselage_10_long_kerosene_flames.getUnlocalizedName());
		GameRegistry.registerItem(mp_fuselage_10_long_kerosene_insulation, mp_fuselage_10_long_kerosene_insulation.getUnlocalizedName());
		GameRegistry.registerItem(mp_fuselage_10_long_kerosene_sleek, mp_fuselage_10_long_kerosene_sleek.getUnlocalizedName());
		GameRegistry.registerItem(mp_fuselage_10_long_kerosene_metal, mp_fuselage_10_long_kerosene_metal.getUnlocalizedName());
		GameRegistry.registerItem(mp_fuselage_10_long_kerosene_dash, mp_fuselage_10_long_kerosene_dash.getUnlocalizedName());
		GameRegistry.registerItem(mp_fuselage_10_long_kerosene_taint, mp_fuselage_10_long_kerosene_taint.getUnlocalizedName());
		GameRegistry.registerItem(mp_fuselage_10_long_kerosene_vap, mp_fuselage_10_long_kerosene_vap.getUnlocalizedName());
		GameRegistry.registerItem(mp_fuselage_10_long_solid, mp_fuselage_10_long_solid.getUnlocalizedName());
		GameRegistry.registerItem(mp_fuselage_10_long_solid_flames, mp_fuselage_10_long_solid_flames.getUnlocalizedName());
		GameRegistry.registerItem(mp_fuselage_10_long_solid_insulation, mp_fuselage_10_long_solid_insulation.getUnlocalizedName());
		GameRegistry.registerItem(mp_fuselage_10_long_solid_sleek, mp_fuselage_10_long_solid_sleek.getUnlocalizedName());
		GameRegistry.registerItem(mp_fuselage_10_long_solid_soviet_glory, mp_fuselage_10_long_solid_soviet_glory.getUnlocalizedName());
		GameRegistry.registerItem(mp_fuselage_10_long_solid_bullet, mp_fuselage_10_long_solid_bullet.getUnlocalizedName());
		GameRegistry.registerItem(mp_fuselage_10_long_solid_silvermoonlight, mp_fuselage_10_long_solid_silvermoonlight.getUnlocalizedName());
		GameRegistry.registerItem(mp_fuselage_10_15_kerosene, mp_fuselage_10_15_kerosene.getUnlocalizedName());
		GameRegistry.registerItem(mp_fuselage_10_15_solid, mp_fuselage_10_15_solid.getUnlocalizedName());
		GameRegistry.registerItem(mp_fuselage_10_15_hydrogen, mp_fuselage_10_15_hydrogen.getUnlocalizedName());
		GameRegistry.registerItem(mp_fuselage_10_15_balefire, mp_fuselage_10_15_balefire.getUnlocalizedName());
		GameRegistry.registerItem(mp_fuselage_15_kerosene, mp_fuselage_15_kerosene.getUnlocalizedName());
		GameRegistry.registerItem(mp_fuselage_15_kerosene_camo, mp_fuselage_15_kerosene_camo.getUnlocalizedName());
		GameRegistry.registerItem(mp_fuselage_15_kerosene_desert, mp_fuselage_15_kerosene_desert.getUnlocalizedName());
		GameRegistry.registerItem(mp_fuselage_15_kerosene_sky, mp_fuselage_15_kerosene_sky.getUnlocalizedName());
		GameRegistry.registerItem(mp_fuselage_15_kerosene_insulation, mp_fuselage_15_kerosene_insulation.getUnlocalizedName());
		GameRegistry.registerItem(mp_fuselage_15_kerosene_metal, mp_fuselage_15_kerosene_metal.getUnlocalizedName());
		GameRegistry.registerItem(mp_fuselage_15_kerosene_decorated, mp_fuselage_15_kerosene_decorated.getUnlocalizedName());
		GameRegistry.registerItem(mp_fuselage_15_kerosene_steampunk, mp_fuselage_15_kerosene_steampunk.getUnlocalizedName());
		GameRegistry.registerItem(mp_fuselage_15_kerosene_polite, mp_fuselage_15_kerosene_polite.getUnlocalizedName());
		GameRegistry.registerItem(mp_fuselage_15_kerosene_blackjack, mp_fuselage_15_kerosene_blackjack.getUnlocalizedName());
		GameRegistry.registerItem(mp_fuselage_15_kerosene_lambda, mp_fuselage_15_kerosene_lambda.getUnlocalizedName());
		GameRegistry.registerItem(mp_fuselage_15_kerosene_minuteman, mp_fuselage_15_kerosene_minuteman.getUnlocalizedName());
		GameRegistry.registerItem(mp_fuselage_15_kerosene_pip, mp_fuselage_15_kerosene_pip.getUnlocalizedName());
		GameRegistry.registerItem(mp_fuselage_15_kerosene_taint, mp_fuselage_15_kerosene_taint.getUnlocalizedName());
		GameRegistry.registerItem(mp_fuselage_15_kerosene_yuck, mp_fuselage_15_kerosene_yuck.getUnlocalizedName());
		GameRegistry.registerItem(mp_fuselage_15_solid, mp_fuselage_15_solid.getUnlocalizedName());
		GameRegistry.registerItem(mp_fuselage_15_solid_insulation, mp_fuselage_15_solid_insulation.getUnlocalizedName());
		GameRegistry.registerItem(mp_fuselage_15_solid_desh, mp_fuselage_15_solid_desh.getUnlocalizedName());
		GameRegistry.registerItem(mp_fuselage_15_solid_soviet_glory, mp_fuselage_15_solid_soviet_glory.getUnlocalizedName());
		GameRegistry.registerItem(mp_fuselage_15_solid_soviet_stank, mp_fuselage_15_solid_soviet_stank.getUnlocalizedName());
		GameRegistry.registerItem(mp_fuselage_15_solid_faust, mp_fuselage_15_solid_faust.getUnlocalizedName());
		GameRegistry.registerItem(mp_fuselage_15_solid_silvermoonlight, mp_fuselage_15_solid_silvermoonlight.getUnlocalizedName());
		GameRegistry.registerItem(mp_fuselage_15_solid_snowy, mp_fuselage_15_solid_snowy.getUnlocalizedName());
		GameRegistry.registerItem(mp_fuselage_15_solid_panorama, mp_fuselage_15_solid_panorama.getUnlocalizedName());
		GameRegistry.registerItem(mp_fuselage_15_solid_roses, mp_fuselage_15_solid_roses.getUnlocalizedName());
		GameRegistry.registerItem(mp_fuselage_15_solid_mimi, mp_fuselage_15_solid_mimi.getUnlocalizedName());
		GameRegistry.registerItem(mp_fuselage_15_hydrogen, mp_fuselage_15_hydrogen.getUnlocalizedName());
		GameRegistry.registerItem(mp_fuselage_15_hydrogen_cathedral, mp_fuselage_15_hydrogen_cathedral.getUnlocalizedName());
		//GameRegistry.registerItem(mp_fuselage_15_balefire, mp_fuselage_15_balefire.getUnlocalizedName());
		GameRegistry.registerItem(mp_fuselage_15_20_kerosene, mp_fuselage_15_20_kerosene.getUnlocalizedName());
		GameRegistry.registerItem(mp_fuselage_15_20_kerosene_magnusson, mp_fuselage_15_20_kerosene_magnusson.getUnlocalizedName());
		GameRegistry.registerItem(mp_fuselage_15_20_solid, mp_fuselage_15_20_solid.getUnlocalizedName());

		GameRegistry.registerItem(mp_warhead_10_he, mp_warhead_10_he.getUnlocalizedName());
		GameRegistry.registerItem(mp_warhead_10_incendiary, mp_warhead_10_incendiary.getUnlocalizedName());
		GameRegistry.registerItem(mp_warhead_10_buster, mp_warhead_10_buster.getUnlocalizedName());
		GameRegistry.registerItem(mp_warhead_10_nuclear, mp_warhead_10_nuclear.getUnlocalizedName());
		GameRegistry.registerItem(mp_warhead_10_nuclear_large, mp_warhead_10_nuclear_large.getUnlocalizedName());
		//GameRegistry.registerItem(mp_warhead_10_taint, mp_warhead_10_taint.getUnlocalizedName());
		//GameRegistry.registerItem(mp_warhead_10_cloud, mp_warhead_10_cloud.getUnlocalizedName());
		GameRegistry.registerItem(mp_warhead_15_he, mp_warhead_15_he.getUnlocalizedName());
		GameRegistry.registerItem(mp_warhead_15_incendiary, mp_warhead_15_incendiary.getUnlocalizedName());
		GameRegistry.registerItem(mp_warhead_15_nuclear, mp_warhead_15_nuclear.getUnlocalizedName());
		GameRegistry.registerItem(mp_warhead_15_nuclear_shark, mp_warhead_15_nuclear_shark.getUnlocalizedName());
		GameRegistry.registerItem(mp_warhead_15_nuclear_mimi, mp_warhead_15_nuclear_mimi.getUnlocalizedName());
		//GameRegistry.registerItem(mp_warhead_15_boxcar, mp_warhead_15_boxcar.getUnlocalizedName());
		GameRegistry.registerItem(mp_warhead_15_n2, mp_warhead_15_n2.getUnlocalizedName());
		//GameRegistry.registerItem(mp_warhead_15_balefire, mp_warhead_15_balefire.getUnlocalizedName());
		//GameRegistry.registerItem(mp_warhead_15_turbine, mp_warhead_15_turbine.getUnlocalizedName());
		GameRegistry.registerItem(mp_warhead_20_he, mp_warhead_20_he.getUnlocalizedName());

		GameRegistry.registerItem(mp_chip_1, mp_chip_1.getUnlocalizedName());
		GameRegistry.registerItem(mp_chip_2, mp_chip_2.getUnlocalizedName());
		GameRegistry.registerItem(mp_chip_3, mp_chip_3.getUnlocalizedName());
		GameRegistry.registerItem(mp_chip_4, mp_chip_4.getUnlocalizedName());
		GameRegistry.registerItem(mp_chip_5, mp_chip_5.getUnlocalizedName());

		GameRegistry.registerItem(rp_fuselage_20_12, rp_fuselage_20_12.getUnlocalizedName());
		GameRegistry.registerItem(rp_fuselage_20_6, rp_fuselage_20_6.getUnlocalizedName());
		GameRegistry.registerItem(rp_fuselage_20_3, rp_fuselage_20_3.getUnlocalizedName());
		GameRegistry.registerItem(rp_fuselage_20_1, rp_fuselage_20_1.getUnlocalizedName());
		GameRegistry.registerItem(rp_fuselage_20_12_hydrazine, rp_fuselage_20_12_hydrazine.getUnlocalizedName());
		GameRegistry.registerItem(rp_legs_20, rp_legs_20.getUnlocalizedName());
		GameRegistry.registerItem(rp_capsule_20, rp_capsule_20.getUnlocalizedName());
		GameRegistry.registerItem(rp_station_core_20, rp_station_core_20.getUnlocalizedName());
		GameRegistry.registerItem(rp_pod_20, rp_pod_20.getUnlocalizedName());

		/*GameRegistry.registerItem(missile_skin_camo, missile_skin_camo.getUnlocalizedName());
		GameRegistry.registerItem(missile_skin_desert, missile_skin_desert.getUnlocalizedName());
		GameRegistry.registerItem(missile_skin_flames, missile_skin_flames.getUnlocalizedName());
		GameRegistry.registerItem(missile_skin_manly_pink, missile_skin_manly_pink.getUnlocalizedName());
		GameRegistry.registerItem(missile_skin_orange_insulation, missile_skin_orange_insulation.getUnlocalizedName());
		GameRegistry.registerItem(missile_skin_sleek, missile_skin_sleek.getUnlocalizedName());
		GameRegistry.registerItem(missile_skin_soviet_glory, missile_skin_soviet_glory.getUnlocalizedName());
		GameRegistry.registerItem(missile_skin_soviet_stank, missile_skin_soviet_stank.getUnlocalizedName());
		GameRegistry.registerItem(missile_skin_metal, missile_skin_metal.getUnlocalizedName());*/

		//Satellites
		GameRegistry.registerItem(sat_mapper, sat_mapper.getUnlocalizedName());
		GameRegistry.registerItem(sat_scanner, sat_scanner.getUnlocalizedName());
		GameRegistry.registerItem(sat_radar, sat_radar.getUnlocalizedName());
		GameRegistry.registerItem(sat_laser, sat_laser.getUnlocalizedName());
		GameRegistry.registerItem(sat_foeq, sat_foeq.getUnlocalizedName());
		GameRegistry.registerItem(sat_resonator, sat_resonator.getUnlocalizedName());
		GameRegistry.registerItem(sat_miner, sat_miner.getUnlocalizedName());
		GameRegistry.registerItem(sat_lunar_miner, sat_lunar_miner.getUnlocalizedName());
		//GameRegistry.registerItem(sat_gerald, sat_gerald.getUnlocalizedName());
		GameRegistry.registerItem(sat_chip, sat_chip.getUnlocalizedName());
		GameRegistry.registerItem(sat_interface, sat_interface.getUnlocalizedName());
		GameRegistry.registerItem(sat_coord, sat_coord.getUnlocalizedName());
		GameRegistry.registerItem(sat_designator, sat_designator.getUnlocalizedName());
		GameRegistry.registerItem(sat_relay, sat_relay.getUnlocalizedName());

		//Guns
		//GameRegistry.registerItem(gun_revolver, gun_revolver.getUnlocalizedName());
		//GameRegistry.registerItem(gun_revolver_saturnite, gun_revolver_saturnite.getUnlocalizedName());
		//GameRegistry.registerItem(gun_revolver_gold, gun_revolver_gold.getUnlocalizedName());
		//GameRegistry.registerItem(gun_revolver_schrabidium, gun_revolver_schrabidium.getUnlocalizedName());
		//GameRegistry.registerItem(gun_revolver_cursed, gun_revolver_cursed.getUnlocalizedName());
		//GameRegistry.registerItem(gun_revolver_nightmare, gun_revolver_nightmare.getUnlocalizedName());
		//GameRegistry.registerItem(gun_revolver_nightmare2, gun_revolver_nightmare2.getUnlocalizedName());
		//GameRegistry.registerItem(gun_revolver_pip, gun_revolver_pip.getUnlocalizedName());
		//GameRegistry.registerItem(gun_revolver_nopip, gun_revolver_nopip.getUnlocalizedName());
		//GameRegistry.registerItem(gun_revolver_blackjack, gun_revolver_blackjack.getUnlocalizedName());
		//GameRegistry.registerItem(gun_revolver_silver, gun_revolver_silver.getUnlocalizedName());
		//GameRegistry.registerItem(gun_revolver_red, gun_revolver_red.getUnlocalizedName());
		//GameRegistry.registerItem(gun_bio_revolver, gun_bio_revolver.getUnlocalizedName());
		//GameRegistry.registerItem(gun_deagle, gun_deagle.getUnlocalizedName());
		//GameRegistry.registerItem(gun_uac_pistol, gun_uac_pistol.getUnlocalizedName());
		//GameRegistry.registerItem(gun_flechette, gun_flechette.getUnlocalizedName());
		GameRegistry.registerItem(gun_ar15, gun_ar15.getUnlocalizedName());
		//GameRegistry.registerItem(gun_calamity, gun_calamity.getUnlocalizedName());
		//GameRegistry.registerItem(gun_m2, gun_m2.getUnlocalizedName());
		//GameRegistry.registerItem(gun_minigun, gun_minigun.getUnlocalizedName());
		//GameRegistry.registerItem(gun_lacunae, gun_lacunae.getUnlocalizedName());
		//GameRegistry.registerItem(gun_folly, gun_folly.getUnlocalizedName());
		//GameRegistry.registerItem(gun_b92, gun_b92.getUnlocalizedName());
		//GameRegistry.registerItem(gun_b93, gun_b93.getUnlocalizedName());
		GameRegistry.registerItem(gun_rpg, gun_rpg.getUnlocalizedName());
		GameRegistry.registerItem(gun_karl, gun_karl.getUnlocalizedName());
		GameRegistry.registerItem(gun_panzerschreck, gun_panzerschreck.getUnlocalizedName());
		GameRegistry.registerItem(gun_quadro, gun_quadro.getUnlocalizedName());
		//GameRegistry.registerItem(gun_hk69, gun_hk69.getUnlocalizedName());
		//GameRegistry.registerItem(gun_congolake, gun_congolake.getUnlocalizedName());
		GameRegistry.registerItem(gun_stinger, gun_stinger.getUnlocalizedName());
		//GameRegistry.registerItem(gun_fatman, gun_fatman.getUnlocalizedName());
		//GameRegistry.registerItem(gun_proto, gun_proto.getUnlocalizedName());
		//GameRegistry.registerItem(gun_mirv, gun_mirv.getUnlocalizedName());
		//GameRegistry.registerItem(gun_bf, gun_bf.getUnlocalizedName());
		GameRegistry.registerItem(gun_chemthrower, gun_chemthrower.getUnlocalizedName());
		//GameRegistry.registerItem(gun_mp40, gun_mp40.getUnlocalizedName());
		//GameRegistry.registerItem(gun_thompson, gun_thompson.getUnlocalizedName());
		//GameRegistry.registerItem(gun_uzi, gun_uzi.getUnlocalizedName());
		//GameRegistry.registerItem(gun_uzi_silencer,gun_uzi_silencer.getUnlocalizedName());
		//GameRegistry.registerItem(gun_uzi_saturnite, gun_uzi_saturnite.getUnlocalizedName());
		//GameRegistry.registerItem(gun_uzi_saturnite_silencer,gun_uzi_saturnite_silencer.getUnlocalizedName());
		//GameRegistry.registerItem(gun_uboinik, gun_uboinik.getUnlocalizedName());
		//GameRegistry.registerItem(gun_remington, gun_remington.getUnlocalizedName());
		//GameRegistry.registerItem(gun_spas12, gun_spas12.getUnlocalizedName());
		//GameRegistry.registerItem(gun_benelli, gun_benelli.getUnlocalizedName());
		//GameRegistry.registerItem(gun_supershotgun, gun_supershotgun.getUnlocalizedName());
		//save//GameRegistry.registerItem(gun_ks23, gun_ks23.getUnlocalizedName());
		//GameRegistry.registerItem(gun_sauer, gun_sauer.getUnlocalizedName());
		//GameRegistry.registerItem(gun_lever_action, gun_lever_action.getUnlocalizedName());
		//GameRegistry.registerItem(gun_lever_action_dark, gun_lever_action_dark.getUnlocalizedName());
		GameRegistry.registerItem(gun_lever_action_sonata, gun_lever_action_sonata.getUnlocalizedName());
		//GameRegistry.registerItem(gun_bolt_action, gun_bolt_action.getUnlocalizedName());
		//GameRegistry.registerItem(gun_bolt_action_green, gun_bolt_action_green.getUnlocalizedName());
		//GameRegistry.registerItem(gun_bolt_action_saturnite, gun_bolt_action_saturnite.getUnlocalizedName());
		//GameRegistry.registerItem(gun_mymy, gun_mymy.getUnlocalizedName());
		//GameRegistry.registerItem(gun_coilgun, gun_coilgun.getUnlocalizedName());
		//GameRegistry.registerItem(gun_xvl1456, gun_xvl1456.getUnlocalizedName());
		//GameRegistry.registerItem(gun_osipr, gun_osipr.getUnlocalizedName());
		//GameRegistry.registerItem(gun_immolator, gun_immolator.getUnlocalizedName());
		GameRegistry.registerItem(gun_flamer, gun_flamer.getUnlocalizedName());
		//GameRegistry.registerItem(gun_cryolator, gun_cryolator.getUnlocalizedName());
		//GameRegistry.registerItem(gun_cryocannon, gun_cryocannon.getUnlocalizedName());
		GameRegistry.registerItem(gun_fireext, gun_fireext.getUnlocalizedName());
		//GameRegistry.registerItem(gun_mp, gun_mp.getUnlocalizedName());
		//GameRegistry.registerItem(gun_bolter, gun_bolter.getUnlocalizedName());
		//GameRegistry.registerItem(gun_bolter_digamma, gun_bolter_digamma.getUnlocalizedName());
		//GameRegistry.registerItem(gun_zomg, gun_zomg.getUnlocalizedName());
		//GameRegistry.registerItem(gun_emp, gun_emp.getUnlocalizedName());
		GameRegistry.registerItem(gun_revolver_inverted, gun_revolver_inverted.getUnlocalizedName());
		//GameRegistry.registerItem(gun_jack, gun_jack.getUnlocalizedName());
		//GameRegistry.registerItem(gun_spark, gun_spark.getUnlocalizedName());
		//GameRegistry.registerItem(gun_hp, gun_hp.getUnlocalizedName());
		//GameRegistry.registerItem(gun_euthanasia, gun_euthanasia.getUnlocalizedName());
		GameRegistry.registerItem(gun_skystinger, gun_skystinger.getUnlocalizedName());
		//GameRegistry.registerItem(gun_dash, gun_dash.getUnlocalizedName());
		//GameRegistry.registerItem(gun_twigun, gun_twigun.getUnlocalizedName());
		//GameRegistry.registerItem(gun_defabricator, gun_defabricator.getUnlocalizedName());
		//GameRegistry.registerItem(gun_vortex, gun_vortex.getUnlocalizedName());
		//GameRegistry.registerItem(gun_super_shotgun, gun_super_shotgun.getUnlocalizedName());
		//GameRegistry.registerItem(gun_moist_nugget, gun_moist_nugget.getUnlocalizedName());
		//GameRegistry.registerItem(gun_darter, gun_darter.getUnlocalizedName());
		GameRegistry.registerItem(crucible, crucible.getUnlocalizedName());
		//GameRegistry.registerItem(gun_glass_cannon, gun_glass_cannon.getUnlocalizedName());
		//GameRegistry.registerItem(gun_lunatic_marksman, gun_lunatic_marksman.getUnlocalizedName());

		GameRegistry.registerItem(gun_debug, gun_debug.getUnlocalizedName());
		GameRegistry.registerItem(ammo_debug, ammo_debug.getUnlocalizedName());

		//Ammo
		//GameRegistry.registerItem(gun_b92_ammo, gun_b92_ammo.getUnlocalizedName());
		//GameRegistry.registerItem(gun_xvl1456_ammo, gun_xvl1456_ammo.getUnlocalizedName());
		//GameRegistry.registerItem(gun_osipr_ammo, gun_osipr_ammo.getUnlocalizedName());
		//GameRegistry.registerItem(gun_osipr_ammo2, gun_osipr_ammo2.getUnlocalizedName());
		//GameRegistry.registerItem(gun_immolator_ammo, gun_immolator_ammo.getUnlocalizedName());
		//GameRegistry.registerItem(gun_cryolator_ammo, gun_cryolator_ammo.getUnlocalizedName());
		//GameRegistry.registerItem(gun_emp_ammo, gun_emp_ammo.getUnlocalizedName());
		//GameRegistry.registerItem(gun_jack_ammo, gun_jack_ammo.getUnlocalizedName());
		//GameRegistry.registerItem(gun_spark_ammo, gun_spark_ammo.getUnlocalizedName());
		//GameRegistry.registerItem(gun_hp_ammo, gun_hp_ammo.getUnlocalizedName());
		//GameRegistry.registerItem(gun_defabricator_ammo, gun_defabricator_ammo.getUnlocalizedName());
		//GameRegistry.registerItem(gun_euthanasia_ammo, gun_euthanasia_ammo.getUnlocalizedName());

		GameRegistry.registerItem(ammo_12gauge, ammo_12gauge.getUnlocalizedName());
		GameRegistry.registerItem(ammo_20gauge, ammo_20gauge.getUnlocalizedName());
		GameRegistry.registerItem(ammo_4gauge, ammo_4gauge.getUnlocalizedName());
		GameRegistry.registerItem(ammo_357, ammo_357.getUnlocalizedName());
		GameRegistry.registerItem(ammo_44, ammo_44.getUnlocalizedName());
		GameRegistry.registerItem(ammo_45, ammo_45.getUnlocalizedName());
		GameRegistry.registerItem(ammo_5mm, ammo_5mm.getUnlocalizedName());
		GameRegistry.registerItem(ammo_9mm, ammo_9mm.getUnlocalizedName());
		GameRegistry.registerItem(ammo_556, ammo_556.getUnlocalizedName());
		GameRegistry.registerItem(ammo_762, ammo_762.getUnlocalizedName());
		GameRegistry.registerItem(ammo_22lr, ammo_22lr.getUnlocalizedName());
		GameRegistry.registerItem(ammo_50ae, ammo_50ae.getUnlocalizedName());
		GameRegistry.registerItem(ammo_50bmg, ammo_50bmg.getUnlocalizedName());
		GameRegistry.registerItem(ammo_75bolt, ammo_75bolt.getUnlocalizedName());
		GameRegistry.registerItem(ammo_nuke, ammo_nuke.getUnlocalizedName());
		GameRegistry.registerItem(ammo_fuel, ammo_fuel.getUnlocalizedName());
		GameRegistry.registerItem(ammo_fireext, ammo_fireext.getUnlocalizedName());
		GameRegistry.registerItem(ammo_cell, ammo_cell.getUnlocalizedName());
		GameRegistry.registerItem(ammo_dart, ammo_dart.getUnlocalizedName());
		GameRegistry.registerItem(ammo_rocket, ammo_rocket.getUnlocalizedName());
		GameRegistry.registerItem(ammo_stinger_rocket, ammo_stinger_rocket.getUnlocalizedName());
		GameRegistry.registerItem(ammo_luna_sniper, ammo_luna_sniper.getUnlocalizedName());
		GameRegistry.registerItem(ammo_coilgun, ammo_coilgun.getUnlocalizedName());
		GameRegistry.registerItem(ammo_grenade, ammo_grenade.getUnlocalizedName());
		GameRegistry.registerItem(ammo_shell, ammo_shell.getUnlocalizedName());
		GameRegistry.registerItem(ammo_dgk, ammo_dgk.getUnlocalizedName());
		GameRegistry.registerItem(ammo_arty, ammo_arty.getUnlocalizedName());
		GameRegistry.registerItem(ammo_himars, ammo_himars.getUnlocalizedName());
		GameRegistry.registerItem(ammo_folly, ammo_folly.getUnlocalizedName());
		GameRegistry.registerItem(ammo_folly_nuclear, ammo_folly_nuclear.getUnlocalizedName());
		GameRegistry.registerItem(ammo_folly_du, ammo_folly_du.getUnlocalizedName());

		//-C-l-i-p-s- Magazines
		//GameRegistry.registerItem(clip_revolver_iron, clip_revolver_iron.getUnlocalizedName());
		//GameRegistry.registerItem(clip_revolver, clip_revolver.getUnlocalizedName());
		//GameRegistry.registerItem(clip_revolver_gold, clip_revolver_gold.getUnlocalizedName());
		//GameRegistry.registerItem(clip_revolver_lead, clip_revolver_lead.getUnlocalizedName());
		//GameRegistry.registerItem(clip_revolver_schrabidium, clip_revolver_schrabidium.getUnlocalizedName());
		//GameRegistry.registerItem(clip_revolver_cursed, clip_revolver_cursed.getUnlocalizedName());
		//GameRegistry.registerItem(clip_revolver_nightmare, clip_revolver_nightmare.getUnlocalizedName());
		//GameRegistry.registerItem(clip_revolver_nightmare2, clip_revolver_nightmare2.getUnlocalizedName());
		//GameRegistry.registerItem(clip_revolver_pip, clip_revolver_pip.getUnlocalizedName());
		//GameRegistry.registerItem(clip_revolver_nopip, clip_revolver_nopip.getUnlocalizedName());
		//GameRegistry.registerItem(clip_rpg, clip_rpg.getUnlocalizedName());
		//GameRegistry.registerItem(clip_stinger, clip_stinger.getUnlocalizedName());
		//GameRegistry.registerItem(clip_fatman, clip_fatman.getUnlocalizedName());
		//GameRegistry.registerItem(clip_mirv, clip_mirv.getUnlocalizedName());
		//GameRegistry.registerItem(clip_bf, clip_bf.getUnlocalizedName());
		//GameRegistry.registerItem(clip_mp40, clip_mp40.getUnlocalizedName());
		//GameRegistry.registerItem(clip_uzi, clip_uzi.getUnlocalizedName());
		//GameRegistry.registerItem(clip_uboinik, clip_uboinik.getUnlocalizedName());
		//GameRegistry.registerItem(clip_lever_action, clip_lever_action.getUnlocalizedName());
		//GameRegistry.registerItem(clip_bolt_action, clip_bolt_action.getUnlocalizedName());
		//GameRegistry.registerItem(clip_xvl1456, clip_xvl1456.getUnlocalizedName());
		//GameRegistry.registerItem(clip_osipr, clip_osipr.getUnlocalizedName());
		//GameRegistry.registerItem(clip_immolator, clip_immolator.getUnlocalizedName());
		//GameRegistry.registerItem(clip_cryolator, clip_cryolator.getUnlocalizedName());
		//GameRegistry.registerItem(clip_mp, clip_mp.getUnlocalizedName());
		//GameRegistry.registerItem(clip_emp, clip_emp.getUnlocalizedName());
		//GameRegistry.registerItem(clip_jack, clip_jack.getUnlocalizedName());
		//GameRegistry.registerItem(clip_spark, clip_spark.getUnlocalizedName());
		//GameRegistry.registerItem(clip_hp, clip_hp.getUnlocalizedName());
		//GameRegistry.registerItem(clip_euthanasia, clip_euthanasia.getUnlocalizedName());
		//GameRegistry.registerItem(clip_defabricator, clip_defabricator.getUnlocalizedName());

		GameRegistry.registerItem(ammo_container, ammo_container.getUnlocalizedName());

		//Grenades
		GameRegistry.registerItem(stick_dynamite, stick_dynamite.getUnlocalizedName()); //heave-ho!
		GameRegistry.registerItem(stick_dynamite_fishing, stick_dynamite_fishing.getUnlocalizedName());
		GameRegistry.registerItem(stick_tnt, stick_tnt.getUnlocalizedName());
		GameRegistry.registerItem(stick_semtex, stick_semtex.getUnlocalizedName());
		GameRegistry.registerItem(stick_c4, stick_c4.getUnlocalizedName());
		GameRegistry.registerItem(grenade_generic, grenade_generic.getUnlocalizedName());
		GameRegistry.registerItem(grenade_strong, grenade_strong.getUnlocalizedName());
		GameRegistry.registerItem(grenade_frag, grenade_frag.getUnlocalizedName());
		GameRegistry.registerItem(grenade_fire, grenade_fire.getUnlocalizedName());
		GameRegistry.registerItem(grenade_shrapnel, grenade_shrapnel.getUnlocalizedName());
		GameRegistry.registerItem(grenade_cluster, grenade_cluster.getUnlocalizedName());
		GameRegistry.registerItem(grenade_flare, grenade_flare.getUnlocalizedName());
		GameRegistry.registerItem(grenade_electric, grenade_electric.getUnlocalizedName());
		GameRegistry.registerItem(grenade_poison, grenade_poison.getUnlocalizedName());
		GameRegistry.registerItem(grenade_gas, grenade_gas.getUnlocalizedName());
		GameRegistry.registerItem(grenade_cloud, grenade_cloud.getUnlocalizedName());
		GameRegistry.registerItem(grenade_pink_cloud, grenade_pink_cloud.getUnlocalizedName());
		GameRegistry.registerItem(grenade_smart, grenade_smart.getUnlocalizedName());
		GameRegistry.registerItem(grenade_mirv, grenade_mirv.getUnlocalizedName());
		GameRegistry.registerItem(grenade_breach, grenade_breach.getUnlocalizedName());
		GameRegistry.registerItem(grenade_burst, grenade_burst.getUnlocalizedName());
		GameRegistry.registerItem(grenade_pulse, grenade_pulse.getUnlocalizedName());
		GameRegistry.registerItem(grenade_plasma, grenade_plasma.getUnlocalizedName());
		GameRegistry.registerItem(grenade_tau, grenade_tau.getUnlocalizedName());
		GameRegistry.registerItem(grenade_schrabidium, grenade_schrabidium.getUnlocalizedName());
		GameRegistry.registerItem(grenade_nuke, grenade_nuke.getUnlocalizedName());
		//GameRegistry.registerItem(grenade_lemon, grenade_lemon.getUnlocalizedName());
		GameRegistry.registerItem(grenade_gascan, grenade_gascan.getUnlocalizedName());
		GameRegistry.registerItem(grenade_kyiv, grenade_kyiv.getUnlocalizedName());
		GameRegistry.registerItem(grenade_mk2, grenade_mk2.getUnlocalizedName());
		GameRegistry.registerItem(grenade_aschrab, grenade_aschrab.getUnlocalizedName());
		GameRegistry.registerItem(grenade_nuclear, grenade_nuclear.getUnlocalizedName());
		GameRegistry.registerItem(grenade_zomg, grenade_zomg.getUnlocalizedName());
		GameRegistry.registerItem(grenade_black_hole, grenade_black_hole.getUnlocalizedName());

		GameRegistry.registerItem(grenade_if_generic, grenade_if_generic.getUnlocalizedName());
		GameRegistry.registerItem(grenade_if_he, grenade_if_he.getUnlocalizedName());
		GameRegistry.registerItem(grenade_if_bouncy, grenade_if_bouncy.getUnlocalizedName());
		GameRegistry.registerItem(grenade_if_sticky, grenade_if_sticky.getUnlocalizedName());
		GameRegistry.registerItem(grenade_if_impact, grenade_if_impact.getUnlocalizedName());
		GameRegistry.registerItem(grenade_if_incendiary, grenade_if_incendiary.getUnlocalizedName());
		GameRegistry.registerItem(grenade_if_toxic, grenade_if_toxic.getUnlocalizedName());
		GameRegistry.registerItem(grenade_if_concussion, grenade_if_concussion.getUnlocalizedName());
		GameRegistry.registerItem(grenade_if_brimstone, grenade_if_brimstone.getUnlocalizedName());
		GameRegistry.registerItem(grenade_if_mystery, grenade_if_mystery.getUnlocalizedName());
		GameRegistry.registerItem(grenade_if_spark, grenade_if_spark.getUnlocalizedName());
		GameRegistry.registerItem(grenade_if_hopwire, grenade_if_hopwire.getUnlocalizedName());
		GameRegistry.registerItem(grenade_if_null, grenade_if_null.getUnlocalizedName());
		//GameRegistry.registerItem(nuclear_waste_pearl, nuclear_waste_pearl.getUnlocalizedName());

		//GameRegistry.registerItem(ullapool_caber, ullapool_caber.getUnlocalizedName());
		GameRegistry.registerItem(weaponized_starblaster_cell, weaponized_starblaster_cell.getUnlocalizedName());

		//Capes
		//GameRegistry.registerItem(cape_radiation, cape_radiation.getUnlocalizedName());
		//GameRegistry.registerItem(cape_gasmask, cape_gasmask.getUnlocalizedName());
		//GameRegistry.registerItem(cape_schrabidium, cape_schrabidium.getUnlocalizedName());
		//GameRegistry.registerItem(cape_hidden, cape_hidden.getUnlocalizedName());

		//Tools
		//GameRegistry.registerItem(dwarven_pickaxe, dwarven_pickaxe.getUnlocalizedName());
		//GameRegistry.registerItem(schrabidium_sword, schrabidium_sword.getUnlocalizedName());
		//GameRegistry.registerItem(schrabidium_hammer, schrabidium_hammer.getUnlocalizedName());
		//GameRegistry.registerItem(shimmer_sledge, shimmer_sledge.getUnlocalizedName());
		//GameRegistry.registerItem(shimmer_axe, shimmer_axe.getUnlocalizedName());
		//GameRegistry.registerItem(pch, pch.getUnlocalizedName()); //sike, nevermind
		GameRegistry.registerItem(wood_gavel, wood_gavel.getUnlocalizedName());
		GameRegistry.registerItem(lead_gavel, lead_gavel.getUnlocalizedName());
		GameRegistry.registerItem(diamond_gavel, diamond_gavel.getUnlocalizedName());
		//GameRegistry.registerItem(mese_gavel, mese_gavel.getUnlocalizedName());
		GameRegistry.registerItem(schrabidium_pickaxe, schrabidium_pickaxe.getUnlocalizedName());
		GameRegistry.registerItem(schrabidium_axe, schrabidium_axe.getUnlocalizedName());
		GameRegistry.registerItem(schrabidium_shovel, schrabidium_shovel.getUnlocalizedName());
		GameRegistry.registerItem(schrabidium_hoe, schrabidium_hoe.getUnlocalizedName());
		//GameRegistry.registerItem(steel_sword, steel_sword.getUnlocalizedName());
		GameRegistry.registerItem(steel_pickaxe, steel_pickaxe.getUnlocalizedName());
		GameRegistry.registerItem(steel_axe, steel_axe.getUnlocalizedName());
		GameRegistry.registerItem(steel_shovel, steel_shovel.getUnlocalizedName());
		GameRegistry.registerItem(steel_hoe, steel_hoe.getUnlocalizedName());
		//GameRegistry.registerItem(titanium_sword, titanium_sword.getUnlocalizedName());
		GameRegistry.registerItem(titanium_pickaxe, titanium_pickaxe.getUnlocalizedName());
		GameRegistry.registerItem(titanium_axe, titanium_axe.getUnlocalizedName());
		GameRegistry.registerItem(titanium_shovel, titanium_shovel.getUnlocalizedName());
		GameRegistry.registerItem(titanium_hoe, titanium_hoe.getUnlocalizedName());
		//GameRegistry.registerItem(cobalt_sword, cobalt_sword.getUnlocalizedName());
		GameRegistry.registerItem(cobalt_pickaxe, cobalt_pickaxe.getUnlocalizedName());
		GameRegistry.registerItem(cobalt_axe, cobalt_axe.getUnlocalizedName());
		GameRegistry.registerItem(cobalt_shovel, cobalt_shovel.getUnlocalizedName());
		GameRegistry.registerItem(cobalt_hoe, cobalt_hoe.getUnlocalizedName());
		//GameRegistry.registerItem(cobalt_decorated_sword, cobalt_decorated_sword.getUnlocalizedName());
		GameRegistry.registerItem(cobalt_decorated_pickaxe, cobalt_decorated_pickaxe.getUnlocalizedName());
		GameRegistry.registerItem(cobalt_decorated_axe, cobalt_decorated_axe.getUnlocalizedName());
		GameRegistry.registerItem(cobalt_decorated_shovel, cobalt_decorated_shovel.getUnlocalizedName());
		GameRegistry.registerItem(cobalt_decorated_hoe, cobalt_decorated_hoe.getUnlocalizedName());
		//GameRegistry.registerItem(starmetal_sword, starmetal_sword.getUnlocalizedName());
		GameRegistry.registerItem(starmetal_pickaxe, starmetal_pickaxe.getUnlocalizedName());
		GameRegistry.registerItem(starmetal_axe, starmetal_axe.getUnlocalizedName());
		GameRegistry.registerItem(starmetal_shovel, starmetal_shovel.getUnlocalizedName());
		GameRegistry.registerItem(starmetal_hoe, starmetal_hoe.getUnlocalizedName());
		//GameRegistry.registerItem(alloy_sword, alloy_sword.getUnlocalizedName());
		GameRegistry.registerItem(alloy_pickaxe, alloy_pickaxe.getUnlocalizedName());
		GameRegistry.registerItem(alloy_axe, alloy_axe.getUnlocalizedName());
		GameRegistry.registerItem(alloy_shovel, alloy_shovel.getUnlocalizedName());
		GameRegistry.registerItem(alloy_hoe, alloy_hoe.getUnlocalizedName());
		GameRegistry.registerItem(alloy_knife, alloy_knife.getUnlocalizedName());
		//GameRegistry.registerItem(cmb_sword, cmb_sword.getUnlocalizedName());
		GameRegistry.registerItem(cmb_pickaxe, cmb_pickaxe.getUnlocalizedName());
		GameRegistry.registerItem(cmb_axe, cmb_axe.getUnlocalizedName());
		GameRegistry.registerItem(cmb_shovel, cmb_shovel.getUnlocalizedName());
		GameRegistry.registerItem(cmb_hoe, cmb_hoe.getUnlocalizedName());
		//GameRegistry.registerItem(desh_sword, desh_sword.getUnlocalizedName());
		GameRegistry.registerItem(desh_pickaxe, desh_pickaxe.getUnlocalizedName());
		GameRegistry.registerItem(desh_axe, desh_axe.getUnlocalizedName());
		GameRegistry.registerItem(desh_shovel, desh_shovel.getUnlocalizedName());
		GameRegistry.registerItem(desh_hoe, desh_hoe.getUnlocalizedName());
		//GameRegistry.registerItem(elec_sword, elec_sword.getUnlocalizedName());
		GameRegistry.registerItem(elec_pickaxe, elec_pickaxe.getUnlocalizedName());
		GameRegistry.registerItem(elec_axe, elec_axe.getUnlocalizedName());
		GameRegistry.registerItem(elec_shovel, elec_shovel.getUnlocalizedName());
		//GameRegistry.registerItem(dnt_sword, dnt_sword.getUnlocalizedName());
		GameRegistry.registerItem(smashing_hammer, smashing_hammer.getUnlocalizedName());
		GameRegistry.registerItem(centri_stick, centri_stick.getUnlocalizedName());
		GameRegistry.registerItem(drax, drax.getUnlocalizedName());
		GameRegistry.registerItem(drax_mk2, drax_mk2.getUnlocalizedName());
		GameRegistry.registerItem(drax_mk3, drax_mk3.getUnlocalizedName());
		GameRegistry.registerItem(bismuth_pickaxe, bismuth_pickaxe.getUnlocalizedName());
		GameRegistry.registerItem(bismuth_axe, bismuth_axe.getUnlocalizedName());
		GameRegistry.registerItem(volcanic_pickaxe, volcanic_pickaxe.getUnlocalizedName());
		GameRegistry.registerItem(volcanic_axe, volcanic_axe.getUnlocalizedName());
		GameRegistry.registerItem(chlorophyte_pickaxe, chlorophyte_pickaxe.getUnlocalizedName());
		GameRegistry.registerItem(chlorophyte_axe, chlorophyte_axe.getUnlocalizedName());
		GameRegistry.registerItem(mese_pickaxe, mese_pickaxe.getUnlocalizedName());
		GameRegistry.registerItem(mese_axe, mese_axe.getUnlocalizedName());		GameRegistry.registerItem(matchstick, matchstick.getUnlocalizedName());
		//GameRegistry.registerItem(balefire_and_steel, balefire_and_steel.getUnlocalizedName());
		GameRegistry.registerItem(crowbar, crowbar.getUnlocalizedName());
		GameRegistry.registerItem(wrench, wrench.getUnlocalizedName());
		GameRegistry.registerItem(wrench_archineer, wrench_archineer.getUnlocalizedName());
		//GameRegistry.registerItem(wrench_flipped, wrench_flipped.getUnlocalizedName());
		GameRegistry.registerItem(memespoon, memespoon.getUnlocalizedName());
		GameRegistry.registerItem(saw, saw.getUnlocalizedName());
		GameRegistry.registerItem(bat, bat.getUnlocalizedName());
		GameRegistry.registerItem(bat_nail, bat_nail.getUnlocalizedName());
		GameRegistry.registerItem(golf_club, golf_club.getUnlocalizedName());
		GameRegistry.registerItem(pipe_rusty, pipe_rusty.getUnlocalizedName());
		GameRegistry.registerItem(pipe_lead, pipe_lead.getUnlocalizedName());
		GameRegistry.registerItem(reer_graar, reer_graar.getUnlocalizedName());
		//GameRegistry.registerItem(stopsign, stopsign.getUnlocalizedName());
		//GameRegistry.registerItem(sopsign, sopsign.getUnlocalizedName());
		//GameRegistry.registerItem(chernobylsign, chernobylsign.getUnlocalizedName());

		//GameRegistry.registerItem(meteorite_sword, meteorite_sword.getUnlocalizedName());
		//GameRegistry.registerItem(meteorite_sword_seared, meteorite_sword_seared.getUnlocalizedName());
		//GameRegistry.registerItem(meteorite_sword_reforged, meteorite_sword_reforged.getUnlocalizedName());
		//GameRegistry.registerItem(meteorite_sword_hardened, meteorite_sword_hardened.getUnlocalizedName());
		//GameRegistry.registerItem(meteorite_sword_alloyed, meteorite_sword_alloyed.getUnlocalizedName());
		//GameRegistry.registerItem(meteorite_sword_machined, meteorite_sword_machined.getUnlocalizedName());
		//GameRegistry.registerItem(meteorite_sword_treated, meteorite_sword_treated.getUnlocalizedName());
		//GameRegistry.registerItem(meteorite_sword_etched, meteorite_sword_etched.getUnlocalizedName());
		//GameRegistry.registerItem(meteorite_sword_bred, meteorite_sword_bred.getUnlocalizedName());
		//GameRegistry.registerItem(meteorite_sword_irradiated, meteorite_sword_irradiated.getUnlocalizedName());
		//GameRegistry.registerItem(meteorite_sword_fused, meteorite_sword_fused.getUnlocalizedName());
		//GameRegistry.registerItem(meteorite_sword_baleful, meteorite_sword_baleful.getUnlocalizedName());

		//Multitool
		GameRegistry.registerItem(multitool_hit, multitool_hit.getUnlocalizedName());
		GameRegistry.registerItem(multitool_dig, multitool_dig.getUnlocalizedName());
		GameRegistry.registerItem(multitool_silk, multitool_silk.getUnlocalizedName());
		GameRegistry.registerItem(multitool_ext, multitool_ext.getUnlocalizedName());
		GameRegistry.registerItem(multitool_miner, multitool_miner.getUnlocalizedName());
		GameRegistry.registerItem(multitool_beam, multitool_beam.getUnlocalizedName());
		GameRegistry.registerItem(multitool_sky, multitool_sky.getUnlocalizedName());
		GameRegistry.registerItem(multitool_mega, multitool_mega.getUnlocalizedName());
		GameRegistry.registerItem(multitool_joule, multitool_joule.getUnlocalizedName());
		GameRegistry.registerItem(multitool_decon, multitool_decon.getUnlocalizedName());

		//Syringes & Pills
		GameRegistry.registerItem(syringe_empty, syringe_empty.getUnlocalizedName());
		GameRegistry.registerItem(syringe_antidote, syringe_antidote.getUnlocalizedName());
		GameRegistry.registerItem(syringe_poison, syringe_poison.getUnlocalizedName());
		//GameRegistry.registerItem(syringe_awesome, syringe_awesome.getUnlocalizedName());
		GameRegistry.registerItem(syringe_metal_empty, syringe_metal_empty.getUnlocalizedName());
		GameRegistry.registerItem(syringe_metal_stimpak, syringe_metal_stimpak.getUnlocalizedName());
		GameRegistry.registerItem(syringe_metal_medx, syringe_metal_medx.getUnlocalizedName());
		GameRegistry.registerItem(syringe_metal_psycho, syringe_metal_psycho.getUnlocalizedName());
		GameRegistry.registerItem(syringe_metal_super, syringe_metal_super.getUnlocalizedName());

		GameRegistry.registerItem(tourniquet, tourniquet.getUnlocalizedName());

		//GameRegistry.registerItem(syringe_taint, syringe_taint.getUnlocalizedName());
		GameRegistry.registerItem(syringe_mkunicorn, syringe_mkunicorn.getUnlocalizedName());
		GameRegistry.registerItem(med_bag, med_bag.getUnlocalizedName());
		GameRegistry.registerItem(iv_empty, iv_empty.getUnlocalizedName());
		GameRegistry.registerItem(iv_blood, iv_blood.getUnlocalizedName());
		//GameRegistry.registerItem(iv_xp_empty, iv_xp_empty.getUnlocalizedName());
		//GameRegistry.registerItem(iv_xp, iv_xp.getUnlocalizedName());
		GameRegistry.registerItem(radaway, radaway.getUnlocalizedName());
		GameRegistry.registerItem(prussian_blue_powder, prussian_blue_powder.getUnlocalizedName());
		GameRegistry.registerItem(prussian_blue_pill, prussian_blue_pill.getUnlocalizedName());
		GameRegistry.registerItem(radaway_strong, radaway_strong.getUnlocalizedName());
		GameRegistry.registerItem(radaway_flush, radaway_flush.getUnlocalizedName());
		GameRegistry.registerItem(radx, radx.getUnlocalizedName());
		GameRegistry.registerItem(siox, siox.getUnlocalizedName());
		GameRegistry.registerItem(pill_herbal, pill_herbal.getUnlocalizedName());
		GameRegistry.registerItem(pill_iodine, pill_iodine.getUnlocalizedName());
		GameRegistry.registerItem(xanax, xanax.getUnlocalizedName());
		GameRegistry.registerItem(fmn, fmn.getUnlocalizedName());
		GameRegistry.registerItem(five_htp, five_htp.getUnlocalizedName());
		GameRegistry.registerItem(plan_c, plan_c.getUnlocalizedName());
		//GameRegistry.registerItem(pill_red, pill_red.getUnlocalizedName());
		//GameRegistry.registerItem(stealth_boy, stealth_boy.getUnlocalizedName());
		GameRegistry.registerItem(gas_mask_filter, gas_mask_filter.getUnlocalizedName());
		GameRegistry.registerItem(gas_mask_filter_mono, gas_mask_filter_mono.getUnlocalizedName());
		GameRegistry.registerItem(gas_mask_filter_combo, gas_mask_filter_combo.getUnlocalizedName());
		GameRegistry.registerItem(gas_mask_filter_rag, gas_mask_filter_rag.getUnlocalizedName());
		GameRegistry.registerItem(gas_mask_filter_piss, gas_mask_filter_piss.getUnlocalizedName());
		//GameRegistry.registerItem(jetpack_tank, jetpack_tank.getUnlocalizedName());
		GameRegistry.registerItem(lox_tank, lox_tank.getUnlocalizedName());
		GameRegistry.registerItem(gun_kit_1, gun_kit_1.getUnlocalizedName());
		GameRegistry.registerItem(gun_kit_2, gun_kit_2.getUnlocalizedName());

		//GameRegistry.registerItem(animan, animan.getUnlocalizedName());

		//Food
		//GameRegistry.registerItem(bomb_waffle, bomb_waffle.getUnlocalizedName());
		GameRegistry.registerItem(schnitzel_vegan, schnitzel_vegan.getUnlocalizedName());
		GameRegistry.registerItem(cotton_candy, cotton_candy.getUnlocalizedName());
		//GameRegistry.registerItem(apple_lead, apple_lead.getUnlocalizedName());
		//GameRegistry.registerItem(apple_schrabidium, apple_schrabidium.getUnlocalizedName());
		//GameRegistry.registerItem(tem_flakes, tem_flakes.getUnlocalizedName());
		//GameRegistry.registerItem(glowing_stew, glowing_stew.getUnlocalizedName());
		//GameRegistry.registerItem(balefire_scrambled, balefire_scrambled.getUnlocalizedName());
		//GameRegistry.registerItem(balefire_and_ham, balefire_and_ham.getUnlocalizedName());
		//GameRegistry.registerItem(lemon, lemon.getUnlocalizedName());
		GameRegistry.registerItem(definitelyfood, definitelyfood.getUnlocalizedName());
		GameRegistry.registerItem(loops, loops.getUnlocalizedName());
		GameRegistry.registerItem(loop_stew, loop_stew.getUnlocalizedName());
		GameRegistry.registerItem(spongebob_macaroni, spongebob_macaroni.getUnlocalizedName());
		//GameRegistry.registerItem(fooditem, fooditem.getUnlocalizedName());
		GameRegistry.registerItem(twinkie, twinkie.getUnlocalizedName());
		//GameRegistry.registerItem(static_sandwich, static_sandwich.getUnlocalizedName());
		GameRegistry.registerItem(pudding, pudding.getUnlocalizedName());
		//GameRegistry.registerItem(pancake, pancake.getUnlocalizedName());
		GameRegistry.registerItem(butter, butter.getUnlocalizedName());
		GameRegistry.registerItem(s_cream, s_cream.getUnlocalizedName());
		GameRegistry.registerItem(nugget, nugget.getUnlocalizedName());
		GameRegistry.registerItem(peas, peas.getUnlocalizedName());
		GameRegistry.registerItem(marshmallow, marshmallow.getUnlocalizedName());
		GameRegistry.registerItem(cheese, cheese.getUnlocalizedName());
		GameRegistry.registerItem(quesadilla, quesadilla.getUnlocalizedName());
		//GameRegistry.registerItem(glyphid_meat, glyphid_meat.getUnlocalizedName());
		//GameRegistry.registerItem(glyphid_meat_grilled, glyphid_meat_grilled.getUnlocalizedName());
		//GameRegistry.registerItem(egg_glyphid, egg_glyphid.getUnlocalizedName());
		GameRegistry.registerItem(med_ipecac, med_ipecac.getUnlocalizedName());
		GameRegistry.registerItem(med_ptsd, med_ptsd.getUnlocalizedName());
		GameRegistry.registerItem(canteen_vodka, canteen_vodka.getUnlocalizedName());
		GameRegistry.registerItem(canteen_fab, canteen_fab.getUnlocalizedName());
		GameRegistry.registerItem(mucho_mango, mucho_mango.getUnlocalizedName());
		GameRegistry.registerItem(chocolate, chocolate.getUnlocalizedName());

		//Energy Drinks
		GameRegistry.registerItem(can_empty, can_empty.getUnlocalizedName());
		GameRegistry.registerItem(can_smart, can_smart.getUnlocalizedName());
		GameRegistry.registerItem(can_creature, can_creature.getUnlocalizedName());
		GameRegistry.registerItem(can_redbomb, can_redbomb.getUnlocalizedName());
		GameRegistry.registerItem(can_mrsugar, can_mrsugar.getUnlocalizedName());
		GameRegistry.registerItem(can_overcharge, can_overcharge.getUnlocalizedName());
		GameRegistry.registerItem(can_luna, can_luna.getUnlocalizedName());
		GameRegistry.registerItem(can_bepis, can_bepis.getUnlocalizedName());
		GameRegistry.registerItem(can_breen, can_breen.getUnlocalizedName());
		GameRegistry.registerItem(can_mug, can_mug.getUnlocalizedName());

		//Coffee
		GameRegistry.registerItem(coffee, coffee.getUnlocalizedName());
		GameRegistry.registerItem(coffee_radium, coffee_radium.getUnlocalizedName());

		//Cola
		GameRegistry.registerItem(bottle_empty, bottle_empty.getUnlocalizedName());
		//GameRegistry.registerItem(bottle_nuka, bottle_nuka.getUnlocalizedName());
		//GameRegistry.registerItem(bottle_cherry, bottle_cherry.getUnlocalizedName());
		//GameRegistry.registerItem(bottle_quantum, bottle_quantum.getUnlocalizedName());
		//GameRegistry.registerItem(bottle_sparkle, bottle_sparkle.getUnlocalizedName());
		//GameRegistry.registerItem(bottle_rad, bottle_rad.getUnlocalizedName());
		//GameRegistry.registerItem(bottle2_empty, bottle2_empty.getUnlocalizedName());
		//GameRegistry.registerItem(bottle2_korl, bottle2_korl.getUnlocalizedName());
		//GameRegistry.registerItem(bottle2_fritz, bottle2_fritz.getUnlocalizedName());
		//GameRegistry.registerItem(bottle2_korl_special, bottle2_korl_special.getUnlocalizedName());
		//GameRegistry.registerItem(bottle2_fritz_special, bottle2_fritz_special.getUnlocalizedName());
		GameRegistry.registerItem(bottle_opener, bottle_opener.getUnlocalizedName());

		//Flasks
		//GameRegistry.registerItem(flask_infusion, flask_infusion.getUnlocalizedName());

		//Canned Food
		GameRegistry.registerItem(canned_conserve, canned_conserve.getUnlocalizedName());

		//Money
		GameRegistry.registerItem(cap_nuka, cap_nuka.getUnlocalizedName());
		GameRegistry.registerItem(cap_quantum, cap_quantum.getUnlocalizedName());
		GameRegistry.registerItem(cap_sparkle, cap_sparkle.getUnlocalizedName());
		GameRegistry.registerItem(cap_rad, cap_rad.getUnlocalizedName());
		GameRegistry.registerItem(cap_korl, cap_korl.getUnlocalizedName());
		GameRegistry.registerItem(cap_fritz, cap_fritz.getUnlocalizedName());
		GameRegistry.registerItem(ring_pull, ring_pull.getUnlocalizedName());
		GameRegistry.registerItem(can_key, can_key.getUnlocalizedName());
		GameRegistry.registerItem(coin_creeper, coin_creeper.getUnlocalizedName());
		GameRegistry.registerItem(coin_radiation,coin_radiation.getUnlocalizedName());
		GameRegistry.registerItem(coin_maskman, coin_maskman.getUnlocalizedName());
		GameRegistry.registerItem(coin_worm, coin_worm.getUnlocalizedName());
		GameRegistry.registerItem(coin_ufo, coin_ufo.getUnlocalizedName());
		GameRegistry.registerItem(coin_airliner, coin_airliner.getUnlocalizedName());
		//GameRegistry.registerItem(medal_liquidator, medal_liquidator.getUnlocalizedName());
		//GameRegistry.registerItem(v1, v1.getUnlocalizedName());
		//GameRegistry.registerItem(protection_charm, protection_charm.getUnlocalizedName());
		//GameRegistry.registerItem(meteor_charm, meteor_charm.getUnlocalizedName());
		GameRegistry.registerItem(neutrino_lens, neutrino_lens.getUnlocalizedName());
		GameRegistry.registerItem(gas_tester, gas_tester.getUnlocalizedName());
		//GameRegistry.registerItem(defuser_gold, defuser_gold.getUnlocalizedName());
		//GameRegistry.registerItem(ballistic_gauntlet, ballistic_gauntlet.getUnlocalizedName());
		GameRegistry.registerItem(night_vision, night_vision.getUnlocalizedName());
		GameRegistry.registerItem(card_aos, card_aos.getUnlocalizedName());
		GameRegistry.registerItem(card_qos, card_qos.getUnlocalizedName());
		//GameRegistry.registerItem(australium_iii, australium_iii.getUnlocalizedName());
		GameRegistry.registerItem(armor_battery, armor_battery.getUnlocalizedName());
		GameRegistry.registerItem(armor_battery_mk2, armor_battery_mk2.getUnlocalizedName());
		GameRegistry.registerItem(armor_battery_mk3, armor_battery_mk3.getUnlocalizedName());

		//Chaos
		GameRegistry.registerItem(chocolate_milk, chocolate_milk.getUnlocalizedName());
		//GameRegistry.registerItem(cbt_device, cbt_device.getUnlocalizedName());
		GameRegistry.registerItem(cigarette, cigarette.getUnlocalizedName());
		GameRegistry.registerItem(crackpipe, crackpipe.getUnlocalizedName());
		GameRegistry.registerItem(bdcl, bdcl.getUnlocalizedName());
		GameRegistry.registerItem(galaxygas, galaxygas.getUnlocalizedName());


		GameRegistry.registerItem(glass_smilk, glass_smilk.getUnlocalizedName());
		GameRegistry.registerItem(glass_empty, glass_empty.getUnlocalizedName());
		GameRegistry.registerItem(strawberry, strawberry.getUnlocalizedName());
		GameRegistry.registerItem(teaseeds, teaseeds.getUnlocalizedName());
		GameRegistry.registerItem(tea_leaf, tea_leaf.getUnlocalizedName());
		GameRegistry.registerItem(powder_coffee, powder_coffee.getUnlocalizedName());
		GameRegistry.registerItem(bean_raw, bean_raw.getUnlocalizedName());
		GameRegistry.registerItem(bean_roast, bean_roast.getUnlocalizedName());
		GameRegistry.registerItem(cmug_empty, cmug_empty.getUnlocalizedName());
		GameRegistry.registerItem(teacup_empty, teacup_empty.getUnlocalizedName());
		GameRegistry.registerItem(teacup, teacup.getUnlocalizedName());
		GameRegistry.registerItem(bottle_honey, bottle_honey.getUnlocalizedName());


		//Armor mods
		GameRegistry.registerItem(attachment_mask, attachment_mask.getUnlocalizedName());
		GameRegistry.registerItem(attachment_mask_mono, attachment_mask_mono.getUnlocalizedName());
		//GameRegistry.registerItem(back_tesla, back_tesla.getUnlocalizedName());
		GameRegistry.registerItem(servo_set, servo_set.getUnlocalizedName());
		GameRegistry.registerItem(servo_set_desh, servo_set_desh.getUnlocalizedName());
		GameRegistry.registerItem(pads_rubber, pads_rubber.getUnlocalizedName());
		//GameRegistry.registerItem(pads_slime, pads_slime.getUnlocalizedName());
		//GameRegistry.registerItem(pads_static, pads_static.getUnlocalizedName());
		GameRegistry.registerItem(cladding_paint, cladding_paint.getUnlocalizedName());
		GameRegistry.registerItem(cladding_rubber, cladding_rubber.getUnlocalizedName());
		GameRegistry.registerItem(cladding_lead, cladding_lead.getUnlocalizedName());
		//GameRegistry.registerItem(cladding_desh, cladding_desh.getUnlocalizedName());
		//GameRegistry.registerItem(cladding_ghiorsium, cladding_ghiorsium.getUnlocalizedName());
		GameRegistry.registerItem(cladding_iron, cladding_iron.getUnlocalizedName());
		GameRegistry.registerItem(cladding_obsidian, cladding_obsidian.getUnlocalizedName());
		GameRegistry.registerItem(insert_kevlar, insert_kevlar.getUnlocalizedName());
		GameRegistry.registerItem(insert_sapi, insert_sapi.getUnlocalizedName());
		GameRegistry.registerItem(insert_esapi, insert_esapi.getUnlocalizedName());
		GameRegistry.registerItem(insert_xsapi, insert_xsapi.getUnlocalizedName());
		GameRegistry.registerItem(insert_steel, insert_steel.getUnlocalizedName());
		//GameRegistry.registerItem(insert_du, insert_du.getUnlocalizedName());
		//GameRegistry.registerItem(insert_polonium, insert_polonium.getUnlocalizedName());
		//GameRegistry.registerItem(insert_ghiorsium, insert_ghiorsium.getUnlocalizedName());
		//GameRegistry.registerItem(insert_cmb, insert_cmb.getUnlocalizedName());
		//GameRegistry.registerItem(insert_era, insert_era.getUnlocalizedName());
		//GameRegistry.registerItem(insert_yharonite, insert_yharonite.getUnlocalizedName());
		//GameRegistry.registerItem(insert_doxium, insert_doxium.getUnlocalizedName());
		//GameRegistry.registerItem(armor_polish, armor_polish.getUnlocalizedName());
		//GameRegistry.registerItem(bandaid, bandaid.getUnlocalizedName());
		//GameRegistry.registerItem(serum, serum.getUnlocalizedName());
		//GameRegistry.registerItem(quartz_plutonium, quartz_plutonium.getUnlocalizedName());
		//GameRegistry.registerItem(morning_glory, morning_glory.getUnlocalizedName());
		GameRegistry.registerItem(lodestone, lodestone.getUnlocalizedName());
		GameRegistry.registerItem(horseshoe_magnet, horseshoe_magnet.getUnlocalizedName());
		GameRegistry.registerItem(industrial_magnet, industrial_magnet.getUnlocalizedName());
		//GameRegistry.registerItem(bathwater, bathwater.getUnlocalizedName());
		//GameRegistry.registerItem(bathwater_mk2, bathwater_mk2.getUnlocalizedName());
		//GameRegistry.registerItem(spider_milk, spider_milk.getUnlocalizedName());
		//GameRegistry.registerItem(ink, ink.getUnlocalizedName());
		//GameRegistry.registerItem(heart_piece, heart_piece.getUnlocalizedName());
		//GameRegistry.registerItem(heart_container, heart_container.getUnlocalizedName());
		//GameRegistry.registerItem(heart_booster, heart_booster.getUnlocalizedName());
		//GameRegistry.registerItem(heart_fab, heart_fab.getUnlocalizedName());
		//GameRegistry.registerItem(black_diamond, black_diamond.getUnlocalizedName());
		//GameRegistry.registerItem(wd40, wd40.getUnlocalizedName());
		//GameRegistry.registerItem(scrumpy, scrumpy.getUnlocalizedName());
		//GameRegistry.registerItem(wild_p, wild_p.getUnlocalizedName());
		//GameRegistry.registerItem(fabsols_vodka, fabsols_vodka.getUnlocalizedName());
		//GameRegistry.registerItem(shackles, shackles.getUnlocalizedName());
		//GameRegistry.registerItem(injector_5htp, injector_5htp.getUnlocalizedName());
		//GameRegistry.registerItem(injector_knife, injector_knife.getUnlocalizedName());

		//Vehicles
		GameRegistry.registerItem(boat_rubber, boat_rubber.getUnlocalizedName());
		GameRegistry.registerItem(cart, cart.getUnlocalizedName());
		GameRegistry.registerItem(train, train.getUnlocalizedName());
		GameRegistry.registerItem(drone, drone.getUnlocalizedName());

		//High Explosive Lenses
		GameRegistry.registerItem(early_explosive_lenses, early_explosive_lenses.getUnlocalizedName());
		GameRegistry.registerItem(explosive_lenses, explosive_lenses.getUnlocalizedName());

		//The Gadget
		//GameRegistry.registerItem(gadget_explosive, gadget_explosive.getUnlocalizedName());
		GameRegistry.registerItem(gadget_wireing, gadget_wireing.getUnlocalizedName());
		GameRegistry.registerItem(gadget_core, gadget_core.getUnlocalizedName());

		//Little Boy
		GameRegistry.registerItem(boy_shielding, boy_shielding.getUnlocalizedName());
		GameRegistry.registerItem(boy_target, boy_target.getUnlocalizedName());
		GameRegistry.registerItem(boy_bullet, boy_bullet.getUnlocalizedName());
		GameRegistry.registerItem(boy_propellant, boy_propellant.getUnlocalizedName());
		GameRegistry.registerItem(boy_igniter, boy_igniter.getUnlocalizedName());;

		//Fat Man
		//GameRegistry.registerItem(man_explosive, man_explosive.getUnlocalizedName());
		GameRegistry.registerItem(man_igniter, man_igniter.getUnlocalizedName());
		GameRegistry.registerItem(man_core, man_core.getUnlocalizedName());

		//Ivy Mike
		GameRegistry.registerItem(mike_core, mike_core.getUnlocalizedName());
		GameRegistry.registerItem(mike_deut, mike_deut.getUnlocalizedName());
		GameRegistry.registerItem(mike_cooling_unit, mike_cooling_unit.getUnlocalizedName());

		//Castle Bravo
		GameRegistry.registerItem(shrimp_lithium, shrimp_lithium.getUnlocalizedName());


		//Tsar Bomba
		GameRegistry.registerItem(tsar_core, tsar_core.getUnlocalizedName());

		GameRegistry.registerItem(tsar_corelead, tsar_corelead.getUnlocalizedName());

		GameRegistry.registerItem(tsar_corereal, tsar_corereal.getUnlocalizedName());

		//made up bullshit from here on out

		//FLEIJA
		GameRegistry.registerItem(fleija_igniter, fleija_igniter.getUnlocalizedName());
		GameRegistry.registerItem(fleija_propellant, fleija_propellant.getUnlocalizedName());
		GameRegistry.registerItem(fleija_core, fleija_core.getUnlocalizedName());

		//Solinium
		GameRegistry.registerItem(solinium_igniter, solinium_igniter.getUnlocalizedName());
		GameRegistry.registerItem(solinium_propellant, solinium_propellant.getUnlocalizedName());
		GameRegistry.registerItem(solinium_core, solinium_core.getUnlocalizedName());

		//N2
		GameRegistry.registerItem(n2_charge, n2_charge.getUnlocalizedName());

		//FSTBMB
		//GameRegistry.registerItem(egg_balefire_shard, egg_balefire_shard.getUnlocalizedName());
		//GameRegistry.registerItem(egg_balefire, egg_balefire.getUnlocalizedName());

		//Conventional Armor
		GameRegistry.registerItem(goggles, goggles.getUnlocalizedName());
		GameRegistry.registerItem(ashglasses, ashglasses.getUnlocalizedName());
		GameRegistry.registerItem(gas_mask, gas_mask.getUnlocalizedName());
		GameRegistry.registerItem(gas_mask_m65, gas_mask_m65.getUnlocalizedName());
		GameRegistry.registerItem(gas_mask_mono, gas_mask_mono.getUnlocalizedName());
		GameRegistry.registerItem(gas_mask_olde, gas_mask_olde.getUnlocalizedName());
		GameRegistry.registerItem(mask_rag, mask_rag.getUnlocalizedName());
		GameRegistry.registerItem(mask_piss, mask_piss.getUnlocalizedName());
		//GameRegistry.registerItem(oxy_mask, oxy_mask.getUnlocalizedName());
		GameRegistry.registerItem(hat, hat.getUnlocalizedName());
		GameRegistry.registerItem(beta, beta.getUnlocalizedName());
		GameRegistry.registerItem(no9, no9.getUnlocalizedName());

		GameRegistry.registerItem(steel_helmet, steel_helmet.getUnlocalizedName());
		GameRegistry.registerItem(steel_plate, steel_plate.getUnlocalizedName());
		GameRegistry.registerItem(steel_legs, steel_legs.getUnlocalizedName());
		GameRegistry.registerItem(steel_boots, steel_boots.getUnlocalizedName());
		GameRegistry.registerItem(titanium_helmet, titanium_helmet.getUnlocalizedName());
		GameRegistry.registerItem(titanium_plate, titanium_plate.getUnlocalizedName());
		GameRegistry.registerItem(titanium_legs, titanium_legs.getUnlocalizedName());
		GameRegistry.registerItem(titanium_boots, titanium_boots.getUnlocalizedName());
		GameRegistry.registerItem(alloy_helmet, alloy_helmet.getUnlocalizedName());
		GameRegistry.registerItem(alloy_plate, alloy_plate.getUnlocalizedName());
		GameRegistry.registerItem(alloy_legs, alloy_legs.getUnlocalizedName());
		GameRegistry.registerItem(alloy_boots, alloy_boots.getUnlocalizedName());

		//Custom Rods
		GameRegistry.registerItem(custom_tnt, custom_tnt.getUnlocalizedName());
		GameRegistry.registerItem(custom_nuke, custom_nuke.getUnlocalizedName());
		GameRegistry.registerItem(custom_hydro, custom_hydro.getUnlocalizedName());
		GameRegistry.registerItem(custom_amat, custom_amat.getUnlocalizedName());
		GameRegistry.registerItem(custom_dirty, custom_dirty.getUnlocalizedName());
		GameRegistry.registerItem(custom_schrab, custom_schrab.getUnlocalizedName());
		GameRegistry.registerItem(custom_fall, custom_fall.getUnlocalizedName());

		//Power Armor
		GameRegistry.registerItem(steamsuit_helmet, steamsuit_helmet.getUnlocalizedName());
		GameRegistry.registerItem(steamsuit_plate, steamsuit_plate.getUnlocalizedName());
		GameRegistry.registerItem(steamsuit_legs, steamsuit_legs.getUnlocalizedName());
		GameRegistry.registerItem(steamsuit_boots, steamsuit_boots.getUnlocalizedName());
		GameRegistry.registerItem(dieselsuit_helmet, dieselsuit_helmet.getUnlocalizedName());
		GameRegistry.registerItem(dieselsuit_plate, dieselsuit_plate.getUnlocalizedName());
		GameRegistry.registerItem(dieselsuit_legs, dieselsuit_legs.getUnlocalizedName());
		GameRegistry.registerItem(dieselsuit_boots, dieselsuit_boots.getUnlocalizedName());


		GameRegistry.registerItem(t45_helmet, t45_helmet.getUnlocalizedName());
		GameRegistry.registerItem(t45_plate, t45_plate.getUnlocalizedName());
		GameRegistry.registerItem(t45_legs, t45_legs.getUnlocalizedName());
		GameRegistry.registerItem(t45_boots, t45_boots.getUnlocalizedName());
		GameRegistry.registerItem(ajr_helmet, ajr_helmet.getUnlocalizedName());
		GameRegistry.registerItem(ajr_plate, ajr_plate.getUnlocalizedName());
		GameRegistry.registerItem(ajr_legs, ajr_legs.getUnlocalizedName());
		GameRegistry.registerItem(ajr_boots, ajr_boots.getUnlocalizedName());
		GameRegistry.registerItem(ajro_helmet, ajro_helmet.getUnlocalizedName());
		GameRegistry.registerItem(ajro_plate, ajro_plate.getUnlocalizedName());
		GameRegistry.registerItem(ajro_legs, ajro_legs.getUnlocalizedName());
		GameRegistry.registerItem(ajro_boots, ajro_boots.getUnlocalizedName());
		GameRegistry.registerItem(rpa_helmet, rpa_helmet.getUnlocalizedName());
		GameRegistry.registerItem(rpa_plate, rpa_plate.getUnlocalizedName());
		GameRegistry.registerItem(rpa_legs, rpa_legs.getUnlocalizedName());
		GameRegistry.registerItem(rpa_boots, rpa_boots.getUnlocalizedName());
		GameRegistry.registerItem(bj_helmet, bj_helmet.getUnlocalizedName());
		GameRegistry.registerItem(bj_plate, bj_plate.getUnlocalizedName());
		GameRegistry.registerItem(bj_plate_jetpack, bj_plate_jetpack.getUnlocalizedName());
		GameRegistry.registerItem(bj_legs, bj_legs.getUnlocalizedName());
		GameRegistry.registerItem(bj_boots, bj_boots.getUnlocalizedName());
		GameRegistry.registerItem(envsuit_helmet, envsuit_helmet.getUnlocalizedName());
		GameRegistry.registerItem(envsuit_plate, envsuit_plate.getUnlocalizedName());
		GameRegistry.registerItem(envsuit_legs, envsuit_legs.getUnlocalizedName());
		GameRegistry.registerItem(envsuit_boots, envsuit_boots.getUnlocalizedName());
		GameRegistry.registerItem(hev_helmet, hev_helmet.getUnlocalizedName());
		GameRegistry.registerItem(hev_plate, hev_plate.getUnlocalizedName());
		GameRegistry.registerItem(hev_legs, hev_legs.getUnlocalizedName());
		GameRegistry.registerItem(hev_boots, hev_boots.getUnlocalizedName());
		GameRegistry.registerItem(fau_helmet, fau_helmet.getUnlocalizedName());
		GameRegistry.registerItem(fau_plate, fau_plate.getUnlocalizedName());
		GameRegistry.registerItem(fau_legs, fau_legs.getUnlocalizedName());
		GameRegistry.registerItem(fau_boots, fau_boots.getUnlocalizedName());
		//GameRegistry.registerItem(dns_helmet, dns_helmet.getUnlocalizedName());
		//GameRegistry.registerItem(dns_plate, dns_plate.getUnlocalizedName());
		//GameRegistry.registerItem(dns_legs, dns_legs.getUnlocalizedName());
		//GameRegistry.registerItem(dns_boots, dns_boots.getUnlocalizedName());
		GameRegistry.registerItem(trenchmaster_helmet, trenchmaster_helmet.getUnlocalizedName());
		GameRegistry.registerItem(trenchmaster_plate, trenchmaster_plate.getUnlocalizedName());
		GameRegistry.registerItem(trenchmaster_legs, trenchmaster_legs.getUnlocalizedName());
		GameRegistry.registerItem(trenchmaster_boots, trenchmaster_boots.getUnlocalizedName());

		//Nobody will ever read this anyway, so it shouldn't matter.
		GameRegistry.registerItem(chainsaw, chainsaw.getUnlocalizedName());
		GameRegistry.registerItem(igniter, igniter.getUnlocalizedName());
		GameRegistry.registerItem(detonator, detonator.getUnlocalizedName());
		GameRegistry.registerItem(detonator_multi, detonator_multi.getUnlocalizedName());
		GameRegistry.registerItem(detonator_laser, detonator_laser.getUnlocalizedName());
		GameRegistry.registerItem(detonator_deadman, detonator_deadman.getUnlocalizedName());
		GameRegistry.registerItem(detonator_de, detonator_de.getUnlocalizedName());
		//GameRegistry.registerItem(crate_caller, crate_caller.getUnlocalizedName());
		GameRegistry.registerItem(bomb_caller, bomb_caller.getUnlocalizedName());
		//GameRegistry.registerItem(meteor_remote, meteor_remote.getUnlocalizedName());
		GameRegistry.registerItem(anchor_remote, anchor_remote.getUnlocalizedName());
		GameRegistry.registerItem(defuser, defuser.getUnlocalizedName());
		GameRegistry.registerItem(reacher, reacher.getUnlocalizedName());
		GameRegistry.registerItem(bismuth_tool, bismuth_tool.getUnlocalizedName());
		GameRegistry.registerItem(meltdown_tool, meltdown_tool.getUnlocalizedName());

		GameRegistry.registerItem(hazmat_helmet, hazmat_helmet.getUnlocalizedName());
		GameRegistry.registerItem(hazmat_plate, hazmat_plate.getUnlocalizedName());
		GameRegistry.registerItem(hazmat_legs, hazmat_legs.getUnlocalizedName());
		GameRegistry.registerItem(hazmat_boots, hazmat_boots.getUnlocalizedName());
		GameRegistry.registerItem(hazmat_helmet_red, hazmat_helmet_red.getUnlocalizedName());
		GameRegistry.registerItem(hazmat_plate_red, hazmat_plate_red.getUnlocalizedName());
		GameRegistry.registerItem(hazmat_legs_red, hazmat_legs_red.getUnlocalizedName());
		GameRegistry.registerItem(hazmat_boots_red, hazmat_boots_red.getUnlocalizedName());
		GameRegistry.registerItem(hazmat_helmet_grey, hazmat_helmet_grey.getUnlocalizedName());
		GameRegistry.registerItem(hazmat_plate_grey, hazmat_plate_grey.getUnlocalizedName());
		GameRegistry.registerItem(hazmat_legs_grey, hazmat_legs_grey.getUnlocalizedName());
		GameRegistry.registerItem(hazmat_boots_grey, hazmat_boots_grey.getUnlocalizedName());
		GameRegistry.registerItem(hazmat_paa_helmet, hazmat_paa_helmet.getUnlocalizedName());
		GameRegistry.registerItem(hazmat_paa_plate, hazmat_paa_plate.getUnlocalizedName());
		GameRegistry.registerItem(hazmat_paa_legs, hazmat_paa_legs.getUnlocalizedName());
		GameRegistry.registerItem(hazmat_paa_boots, hazmat_paa_boots.getUnlocalizedName());
		GameRegistry.registerItem(liquidator_helmet, liquidator_helmet.getUnlocalizedName());
		GameRegistry.registerItem(liquidator_plate, liquidator_plate.getUnlocalizedName());
		GameRegistry.registerItem(liquidator_legs, liquidator_legs.getUnlocalizedName());
		GameRegistry.registerItem(liquidator_boots, liquidator_boots.getUnlocalizedName());
		GameRegistry.registerItem(cmb_helmet, cmb_helmet.getUnlocalizedName());
		GameRegistry.registerItem(cmb_plate, cmb_plate.getUnlocalizedName());
		GameRegistry.registerItem(cmb_legs, cmb_legs.getUnlocalizedName());
		GameRegistry.registerItem(cmb_boots, cmb_boots.getUnlocalizedName());
		GameRegistry.registerItem(paa_plate, paa_plate.getUnlocalizedName());
		GameRegistry.registerItem(paa_legs, paa_legs.getUnlocalizedName());
		GameRegistry.registerItem(paa_boots, paa_boots.getUnlocalizedName());
		GameRegistry.registerItem(asbestos_helmet, asbestos_helmet.getUnlocalizedName());
		GameRegistry.registerItem(asbestos_plate, asbestos_plate.getUnlocalizedName());
		GameRegistry.registerItem(asbestos_legs, asbestos_legs.getUnlocalizedName());
		GameRegistry.registerItem(asbestos_boots, asbestos_boots.getUnlocalizedName());
		GameRegistry.registerItem(security_helmet, security_helmet.getUnlocalizedName());
		GameRegistry.registerItem(security_plate, security_plate.getUnlocalizedName());
		GameRegistry.registerItem(security_legs, security_legs.getUnlocalizedName());
		GameRegistry.registerItem(security_boots, security_boots.getUnlocalizedName());
		GameRegistry.registerItem(cobalt_helmet, cobalt_helmet.getUnlocalizedName());
		GameRegistry.registerItem(cobalt_plate, cobalt_plate.getUnlocalizedName());
		GameRegistry.registerItem(cobalt_legs, cobalt_legs.getUnlocalizedName());
		GameRegistry.registerItem(cobalt_boots, cobalt_boots.getUnlocalizedName());
		//GameRegistry.registerItem(starmetal_helmet, starmetal_helmet.getUnlocalizedName());
		//GameRegistry.registerItem(starmetal_plate, starmetal_plate.getUnlocalizedName());
		//GameRegistry.registerItem(starmetal_legs, starmetal_legs.getUnlocalizedName());
		//GameRegistry.registerItem(starmetal_boots, starmetal_boots.getUnlocalizedName());
		GameRegistry.registerItem(zirconium_legs, zirconium_legs.getUnlocalizedName());
		//GameRegistry.registerItem(dnt_helmet, dnt_helmet.getUnlocalizedName());
		//GameRegistry.registerItem(dnt_plate, dnt_plate.getUnlocalizedName());
		//GameRegistry.registerItem(dnt_legs, dnt_legs.getUnlocalizedName());
		//GameRegistry.registerItem(dnt_boots, dnt_boots.getUnlocalizedName());
		GameRegistry.registerItem(schrabidium_helmet, schrabidium_helmet.getUnlocalizedName());
		GameRegistry.registerItem(schrabidium_plate, schrabidium_plate.getUnlocalizedName());
		GameRegistry.registerItem(schrabidium_legs, schrabidium_legs.getUnlocalizedName());
		GameRegistry.registerItem(schrabidium_boots, schrabidium_boots.getUnlocalizedName());
		//GameRegistry.registerItem(bismuth_helmet, bismuth_helmet.getUnlocalizedName());
		//GameRegistry.registerItem(bismuth_plate, bismuth_plate.getUnlocalizedName());
		//GameRegistry.registerItem(bismuth_legs, bismuth_legs.getUnlocalizedName());
		//GameRegistry.registerItem(bismuth_boots, bismuth_boots.getUnlocalizedName());
		//GameRegistry.registerItem(euphemium_helmet, euphemium_helmet.getUnlocalizedName());
		//GameRegistry.registerItem(euphemium_plate, euphemium_plate.getUnlocalizedName());
		//GameRegistry.registerItem(euphemium_legs, euphemium_legs.getUnlocalizedName());
		//GameRegistry.registerItem(euphemium_boots, euphemium_boots.getUnlocalizedName());
		GameRegistry.registerItem(robes_helmet, robes_helmet.getUnlocalizedName());
		GameRegistry.registerItem(robes_plate, robes_plate.getUnlocalizedName());
		GameRegistry.registerItem(robes_legs, robes_legs.getUnlocalizedName());
		GameRegistry.registerItem(robes_boots, robes_boots.getUnlocalizedName());
		//GameRegistry.registerItem(apple_euphemium, apple_euphemium.getUnlocalizedName());
		GameRegistry.registerItem(watch, watch.getUnlocalizedName());
		GameRegistry.registerItem(mask_of_infamy, mask_of_infamy.getUnlocalizedName());
		GameRegistry.registerItem(jackt, jackt.getUnlocalizedName());
		GameRegistry.registerItem(jackt2, jackt2.getUnlocalizedName());
		//GameRegistry.registerItem(jetpack_fly, jetpack_fly.getUnlocalizedName());
		//GameRegistry.registerItem(jetpack_break, jetpack_break.getUnlocalizedName());
		//GameRegistry.registerItem(jetpack_vector, jetpack_vector.getUnlocalizedName());
		//GameRegistry.registerItem(jetpack_boost, jetpack_boost.getUnlocalizedName());
		//GameRegistry.registerItem(wings_limp, wings_limp.getUnlocalizedName());
		//GameRegistry.registerItem(wings_murk, wings_murk.getUnlocalizedName());
		GameRegistry.registerItem(oxy_plss, oxy_plss.getUnlocalizedName());
		//GameRegistry.registerItem(australium_iv, australium_iv.getUnlocalizedName());
		//GameRegistry.registerItem(australium_v, australium_v.getUnlocalizedName());

		//Expensive Ass Shit
		GameRegistry.registerItem(crystal_horn, crystal_horn.getUnlocalizedName());
		GameRegistry.registerItem(crystal_charred, crystal_charred.getUnlocalizedName());

		//OP Tools
		GameRegistry.registerItem(wand, wand.getUnlocalizedName());
		GameRegistry.registerItem(wand_s, wand_s.getUnlocalizedName());
		GameRegistry.registerItem(wand_d, wand_d.getUnlocalizedName());
		GameRegistry.registerItem(bomb_site_wand, bomb_site_wand.getUnlocalizedName());
		GameRegistry.registerItem(structure_single, structure_single.getUnlocalizedName());
		GameRegistry.registerItem(structure_solid, structure_solid.getUnlocalizedName());
		GameRegistry.registerItem(structure_pattern, structure_pattern.getUnlocalizedName());
		GameRegistry.registerItem(structure_randomized, structure_randomized.getUnlocalizedName());
		GameRegistry.registerItem(structure_randomly, structure_randomly.getUnlocalizedName());
		GameRegistry.registerItem(structure_custommachine, structure_custommachine.getUnlocalizedName());
		GameRegistry.registerItem(rod_of_discord, rod_of_discord.getUnlocalizedName());
		//GameRegistry.registerItem(analyzer, analyzer.getUnlocalizedName());
		//GameRegistry.registerItem(remote, remote.getUnlocalizedName());
		GameRegistry.registerItem(euphemium_stopper, euphemium_stopper.getUnlocalizedName());
		//GameRegistry.registerItem(polaroid, polaroid.getUnlocalizedName());
		GameRegistry.registerItem(glitch, glitch.getUnlocalizedName());
		GameRegistry.registerItem(burnt_bark, burnt_bark.getUnlocalizedName());

		//Kits
		GameRegistry.registerItem(nuke_starter_kit, nuke_starter_kit.getUnlocalizedName());
		GameRegistry.registerItem(nuke_advanced_kit, nuke_advanced_kit.getUnlocalizedName());
		GameRegistry.registerItem(nuke_commercially_kit, nuke_commercially_kit.getUnlocalizedName());
		GameRegistry.registerItem(nuke_electric_kit, nuke_electric_kit.getUnlocalizedName());
		GameRegistry.registerItem(gadget_kit, gadget_kit.getUnlocalizedName());
		GameRegistry.registerItem(boy_kit, boy_kit.getUnlocalizedName());
		GameRegistry.registerItem(man_kit, man_kit.getUnlocalizedName());
		GameRegistry.registerItem(mike_kit, mike_kit.getUnlocalizedName());
		GameRegistry.registerItem(tsar_kit, tsar_kit.getUnlocalizedName());
		//GameRegistry.registerItem(prototype_kit, prototype_kit.getUnlocalizedName());
		//GameRegistry.registerItem(fleija_kit, fleija_kit.getUnlocalizedName());
		//GameRegistry.registerItem(solinium_kit, solinium_kit.getUnlocalizedName());
		GameRegistry.registerItem(multi_kit, multi_kit.getUnlocalizedName());
		GameRegistry.registerItem(custom_kit, custom_kit.getUnlocalizedName());
		GameRegistry.registerItem(missile_kit, missile_kit.getUnlocalizedName());
		GameRegistry.registerItem(grenade_kit, grenade_kit.getUnlocalizedName());
		GameRegistry.registerItem(t45_kit, t45_kit.getUnlocalizedName());
		GameRegistry.registerItem(hazmat_kit, hazmat_kit.getUnlocalizedName());
		GameRegistry.registerItem(hazmat_red_kit, hazmat_red_kit.getUnlocalizedName());
		GameRegistry.registerItem(hazmat_grey_kit, hazmat_grey_kit.getUnlocalizedName());
		GameRegistry.registerItem(kit_custom, kit_custom.getUnlocalizedName());
		GameRegistry.registerItem(euphemium_kit, euphemium_kit.getUnlocalizedName());
		GameRegistry.registerItem(kit_toolbox_empty, kit_toolbox_empty.getUnlocalizedName());
		GameRegistry.registerItem(kit_toolbox, kit_toolbox.getUnlocalizedName());
		GameRegistry.registerItem(letter, letter.getUnlocalizedName());

		//Misile Loot Boxes
		GameRegistry.registerItem(loot_10, loot_10.getUnlocalizedName());
		GameRegistry.registerItem(loot_15, loot_15.getUnlocalizedName());
		GameRegistry.registerItem(loot_misc, loot_misc.getUnlocalizedName());

		//THIS is a bucket.
		//GameRegistry.registerItem(bucket_mud, bucket_mud.getUnlocalizedName());
		GameRegistry.registerItem(bucket_acid, bucket_acid.getUnlocalizedName());
		GameRegistry.registerItem(bucket_toxic, bucket_toxic.getUnlocalizedName());
		GameRegistry.registerItem(bucket_schrabidic_acid, bucket_schrabidic_acid.getUnlocalizedName());
		GameRegistry.registerItem(bucket_sulfuric_acid, bucket_sulfuric_acid.getUnlocalizedName());
		GameRegistry.registerItem(bucket_mercury, bucket_mercury.getUnlocalizedName());
		GameRegistry.registerItem(bucket_bromine, bucket_bromine.getUnlocalizedName());

		//Door Items
		GameRegistry.registerItem(door_metal, door_metal.getUnlocalizedName());
		GameRegistry.registerItem(door_office, door_office.getUnlocalizedName());
		GameRegistry.registerItem(door_bunker, door_bunker.getUnlocalizedName());
		GameRegistry.registerItem(door_red, door_red.getUnlocalizedName());
		GameRegistry.registerItem(sliding_blast_door_skin, sliding_blast_door_skin.getUnlocalizedName());

		//Records
		GameRegistry.registerItem(record_lc, record_lc.getUnlocalizedName());
		GameRegistry.registerItem(record_ss, record_ss.getUnlocalizedName());
		GameRegistry.registerItem(record_vc, record_vc.getUnlocalizedName());
		GameRegistry.registerItem(record_gs, record_gs.getUnlocalizedName());
		GameRegistry.registerItem(record_gp, record_gp.getUnlocalizedName());
		GameRegistry.registerItem(record_el, record_el.getUnlocalizedName());
		GameRegistry.registerItem(record_glass, record_glass.getUnlocalizedName());

		//wow we're far down the item registry, is this the cellar?
		GameRegistry.registerItem(book_guide, book_guide.getUnlocalizedName());
		GameRegistry.registerItem(book_lore, book_lore.getUnlocalizedName());
		GameRegistry.registerItem(holotape_image, holotape_image.getUnlocalizedName());
		GameRegistry.registerItem(holotape_damaged, holotape_damaged.getUnlocalizedName());

		//Technical Items
		GameRegistry.registerItem(b_smoke1, b_smoke1.getUnlocalizedName());
		GameRegistry.registerItem(b_smoke2, b_smoke2.getUnlocalizedName());
		GameRegistry.registerItem(b_smoke3, b_smoke3.getUnlocalizedName());
		GameRegistry.registerItem(b_smoke4, b_smoke4.getUnlocalizedName());
		GameRegistry.registerItem(b_smoke5, b_smoke5.getUnlocalizedName());
		GameRegistry.registerItem(b_smoke6, b_smoke6.getUnlocalizedName());
		GameRegistry.registerItem(b_smoke7, b_smoke7.getUnlocalizedName());
		GameRegistry.registerItem(b_smoke8, b_smoke8.getUnlocalizedName());
		GameRegistry.registerItem(chlorine1, chlorine1.getUnlocalizedName());
		GameRegistry.registerItem(chlorine2, chlorine2.getUnlocalizedName());
		GameRegistry.registerItem(chlorine3, chlorine3.getUnlocalizedName());
		GameRegistry.registerItem(chlorine4, chlorine4.getUnlocalizedName());
		GameRegistry.registerItem(chlorine5, chlorine5.getUnlocalizedName());
		GameRegistry.registerItem(chlorine6, chlorine6.getUnlocalizedName());
		GameRegistry.registerItem(chlorine7, chlorine7.getUnlocalizedName());
		GameRegistry.registerItem(chlorine8, chlorine8.getUnlocalizedName());
		GameRegistry.registerItem(pc1, pc1.getUnlocalizedName());
		GameRegistry.registerItem(pc2, pc2.getUnlocalizedName());
		GameRegistry.registerItem(pc3, pc3.getUnlocalizedName());
		GameRegistry.registerItem(pc4, pc4.getUnlocalizedName());
		GameRegistry.registerItem(pc5, pc5.getUnlocalizedName());
		GameRegistry.registerItem(pc6, pc6.getUnlocalizedName());
		GameRegistry.registerItem(pc7, pc7.getUnlocalizedName());
		GameRegistry.registerItem(pc8, pc8.getUnlocalizedName());
		GameRegistry.registerItem(cloud1, cloud1.getUnlocalizedName());
		GameRegistry.registerItem(cloud2, cloud2.getUnlocalizedName());
		GameRegistry.registerItem(cloud3, cloud3.getUnlocalizedName());
		GameRegistry.registerItem(cloud4, cloud4.getUnlocalizedName());
		GameRegistry.registerItem(cloud5, cloud5.getUnlocalizedName());
		GameRegistry.registerItem(cloud6, cloud6.getUnlocalizedName());
		GameRegistry.registerItem(cloud7, cloud7.getUnlocalizedName());
		GameRegistry.registerItem(cloud8, cloud8.getUnlocalizedName());
		GameRegistry.registerItem(orange1, orange1.getUnlocalizedName());
		GameRegistry.registerItem(orange2, orange2.getUnlocalizedName());
		GameRegistry.registerItem(orange3, orange3.getUnlocalizedName());
		GameRegistry.registerItem(orange4, orange4.getUnlocalizedName());
		GameRegistry.registerItem(orange5, orange5.getUnlocalizedName());
		GameRegistry.registerItem(orange6, orange6.getUnlocalizedName());
		GameRegistry.registerItem(orange7, orange7.getUnlocalizedName());
		GameRegistry.registerItem(orange8, orange8.getUnlocalizedName());
		GameRegistry.registerItem(flame_1, flame_1.getUnlocalizedName());
		GameRegistry.registerItem(flame_2, flame_2.getUnlocalizedName());
		GameRegistry.registerItem(flame_3, flame_3.getUnlocalizedName());
		GameRegistry.registerItem(flame_4, flame_4.getUnlocalizedName());
		GameRegistry.registerItem(flame_5, flame_5.getUnlocalizedName());
		GameRegistry.registerItem(flame_6, flame_6.getUnlocalizedName());
		GameRegistry.registerItem(flame_7, flame_7.getUnlocalizedName());
		GameRegistry.registerItem(flame_8, flame_8.getUnlocalizedName());
		GameRegistry.registerItem(flame_9, flame_9.getUnlocalizedName());
		GameRegistry.registerItem(flame_10, flame_10.getUnlocalizedName());
		GameRegistry.registerItem(ln2_1, ln2_1.getUnlocalizedName());
		GameRegistry.registerItem(ln2_2, ln2_2.getUnlocalizedName());
		GameRegistry.registerItem(ln2_3, ln2_3.getUnlocalizedName());
		GameRegistry.registerItem(ln2_4, ln2_4.getUnlocalizedName());
		GameRegistry.registerItem(ln2_5, ln2_5.getUnlocalizedName());
		GameRegistry.registerItem(ln2_6, ln2_6.getUnlocalizedName());
		GameRegistry.registerItem(ln2_7, ln2_7.getUnlocalizedName());
		GameRegistry.registerItem(ln2_8, ln2_8.getUnlocalizedName());
		GameRegistry.registerItem(ln2_9, ln2_9.getUnlocalizedName());
		GameRegistry.registerItem(ln2_10, ln2_10.getUnlocalizedName());
		GameRegistry.registerItem(nothing, nothing.getUnlocalizedName());
		GameRegistry.registerItem(void_anim, void_anim.getUnlocalizedName());
		GameRegistry.registerItem(achievement_icon, achievement_icon.getUnlocalizedName());
		GameRegistry.registerItem(bob_metalworks, bob_metalworks.getUnlocalizedName());
		GameRegistry.registerItem(bob_assembly, bob_assembly.getUnlocalizedName());
		GameRegistry.registerItem(bob_chemistry, bob_chemistry.getUnlocalizedName());
		GameRegistry.registerItem(bob_oil, bob_oil.getUnlocalizedName());
		GameRegistry.registerItem(bob_nuclear, bob_nuclear.getUnlocalizedName());
		//GameRegistry.registerItem(mysteryshovel, mysteryshovel.getUnlocalizedName());
		GameRegistry.registerItem(memory, memory.getUnlocalizedName());
	}

	public static void addRemap(String unloc, Item item, Enum sub) {
		addRemap(unloc, item, sub.ordinal());
	}

	public static void addRemap(String unloc, Item item, int meta) {
		Item remap = new ItemRemap(item, meta).setUnlocalizedName(unloc).setTextureName(RefStrings.MODID + ":plate_armor_titanium");
		GameRegistry.registerItem(remap, remap.getUnlocalizedName());
	}
}
