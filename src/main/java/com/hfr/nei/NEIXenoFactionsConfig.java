package com.hfr.nei;

import codechicken.nei.api.API;
import codechicken.nei.api.IConfigureNEI;
import com.hfr.blocks.ModBlocks;
import com.hfr.config.XFConfig;
import com.hfr.lib.RefStrings;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;

@SideOnly(Side.CLIENT)
public class NEIXenoFactionsConfig implements IConfigureNEI {
    public void loadConfig() {
        if (!XFConfig.enableNEIIntegration) return;
        XenoFactionsStoneDropHandler handler = new XenoFactionsStoneDropHandler();
        API.registerRecipeHandler(handler);
        API.registerUsageHandler(handler);
        register(new XenoFactionsMachineHandler(XenoFactionsMachineHandler.BLAST), new ItemStack(ModBlocks.machine_blastfurnace));
        register(new XenoFactionsMachineHandler(XenoFactionsMachineHandler.FOUNDRY_MELT), new ItemStack(ModBlocks.machine_foundry));
        register(new XenoFactionsMachineHandler(XenoFactionsMachineHandler.FOUNDRY_CAST), new ItemStack(ModBlocks.machine_foundry));
        register(new XenoFactionsMachineHandler(XenoFactionsMachineHandler.NET), new ItemStack(ModBlocks.machine_net));
        register(new XenoFactionsMachineHandler(XenoFactionsMachineHandler.WIND), new ItemStack(ModBlocks.machine_windmill));
    }

    private void register(XenoFactionsMachineHandler handler, ItemStack catalyst) {
        API.registerRecipeHandler(handler);
        API.registerUsageHandler(handler);
        API.addRecipeCatalyst(catalyst, handler.getOverlayIdentifier());
    }

    public String getName() { return StatCollector.translateToLocal("hfr.nei.integration.name"); }
    public String getVersion() { return RefStrings.VERSION; }
}
