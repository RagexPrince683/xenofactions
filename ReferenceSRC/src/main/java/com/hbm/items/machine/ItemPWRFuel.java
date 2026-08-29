package com.hbm.items.machine;

import java.util.List;

import com.hbm.items.ItemEnumMulti;
import com.hbm.util.EnumUtil;
import com.hbm.util.function.Function;
import com.hbm.util.function.Function.FunctionLogarithmic;
import com.hbm.util.function.Function.FunctionSqrt;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;

public class ItemPWRFuel extends ItemEnumMulti {

	public ItemPWRFuel() {
		super(EnumPWRFuel.class, true, true);
	}

	public static enum EnumPWRFuel {
		// Standard LEU reactor fuel (~3–5% enriched)
		MEU(5.0D, new FunctionLogarithmic(22 * 30).withDiv(3000)),
		// U-233 thorium derivative
		HEU233(5.5D, new FunctionLogarithmic(24 * 30).withDiv(2800)),
		// Highly enriched uranium
		HEU235(5.5D, new FunctionLogarithmic(24 * 30).withDiv(2800)),
		// Neptunium blend (mostly experimental)
		MEN(5.5D, new FunctionLogarithmic(23 * 30).withDiv(3000)),
		// Neptunium
		HEN237(4.5D,
			   new FunctionLogarithmic(18 * 30).withDiv(4000)),
		// MOX behaves slightly hotter due to Pu content
		MOX(6.0D, new FunctionLogarithmic(22 * 30).withDiv(2600)),
		// Pu-rich fuel
		MEP(6.0D, new FunctionLogarithmic(24 * 30).withDiv(2600)),
		HEP239(6.5D, new FunctionLogarithmic(25 * 30).withDiv(2400)),
		HEP241(6.5D, new FunctionLogarithmic(25 * 30).withDiv(2400)),
		// Americium experimental burners
		MEA(6.0D, new FunctionLogarithmic(24 * 30).withDiv(3200)),
		// unrealistic/research fuel
		HEA242(6.5D, new FunctionLogarithmic(26 * 30).withDiv(3400)),
		// schrabidium nonsense — untouched
		HES326(		12.5D,	new FunctionSqrt(27.5)),
		HES327(		12.5D,	new FunctionSqrt(30)),
		BFB_AM_MIX(2.5D, new FunctionSqrt(15), 250_000_000),
		BFB_PU241(2.5D, new FunctionSqrt(15), 250_000_000);

		public double yield = 1_000_000_000;
		public double heatEmission;
		public Function function;

		private EnumPWRFuel(double heatEmission, Function function, double yield) {
			this.heatEmission = heatEmission;
			this.function = function;
			this.yield = yield;
		}

		private EnumPWRFuel(double heatEmission, Function function) {
			this(heatEmission, function, 1_000_000_000);
		}
	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean bool) {

		EnumPWRFuel num = EnumUtil.grabEnumSafely(EnumPWRFuel.class, stack.getItemDamage());

		String color = EnumChatFormatting.GOLD + "";
		String reset = EnumChatFormatting.RESET + "";

		list.add(color + "Heat per flux: " + reset + num.heatEmission + " TU");
		list.add(color + "Reaction function: " + reset + num.function.getLabelForFuel());
		list.add(color + "Fuel type: " + reset + num.function.getDangerFromFuel());
	}
}
