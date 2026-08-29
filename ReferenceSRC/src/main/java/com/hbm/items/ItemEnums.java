package com.hbm.items;

/**
 * I'm not at all sure if bunching together all these enums in one long class is a good idea
 * but I don't want to make a new class for every multi item to hold the enum
 * since that's entirely against the point of ItemEnumMulti to begin with.
 * @author hbm
 */

/**
 * Please never touch enums again, this is actually the worst shit ever, and it's abysmally broken; please kill me
 * @author Ragex
 */
public class ItemEnums {

	public static enum EnumCokeType {
		COAL,
		LIGNITE,
		PETROLEUM
	}

	public static enum EnumTarType {
		CRUDE,
		CRACK,
		COAL,
		WOOD,
		WAX,
		PARAFFIN
		//6 THINGS
	} //OH MY GOD YOU COULD HAVE JUST FUCKING MADE IT A REGULAR ITEM. THIS IS ACTUALLY FUCKING RETARDED


	public static enum EnumAshType {
		WOOD,
		COAL,
		MISC,
		FLY,
		SOOT,
		FULLERENE
	}

	public static enum EnumBriquetteType {
		COAL,
		LIGNITE,
		WOOD
	}

	public static enum EnumLegendaryType {
		TIER1,
		TIER2,
		TIER3
	}
	//literally just like useless non used crap

	public static enum EnumPlantType {
		TOBACCO,
		ROPE,
		MUSTARDWILLOW,
	}

	public static enum EnumChunkType {
		RARE
	}

	public static enum EnumAchievementType {
		//GOFISH,
		ACID,
		BALLS,
		//DIGAMMASEE,
		//DIGAMMAFEEL,
		//DIGAMMAKNOW,
		//DIGAMMAKAUAIMOHO,
		//DIGAMMAUPONTOP,
		//DIGAMMAFOROURRIGHT,
		//QUESTIONMARK
		//No more human slop plz kthx
	}

	public static enum EnumFuelAdditive {
		ANTIKNOCK,
		DEICER
	}

	public static enum EnumSecretType {
		CANISTER, CONTROLLER, SELENIUM_STEEL
	}

	public static enum EnumCasingType {
		SMALL, LARGE, SMALL_STEEL, LARGE_STEEL, SHOTSHELL, BUCKSHOT, BUCKSHOT_ADVANCED
	}
}
