package com.hfr.nei;

import codechicken.nei.api.API;
import codechicken.nei.api.IConfigureNEI;
import com.hfr.config.XFConfig;
import com.hfr.lib.RefStrings;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.util.StatCollector;

@SideOnly(Side.CLIENT)
public class NEIXenoFactionsConfig implements IConfigureNEI {
    public void loadConfig() {
        if (!XFConfig.enableNEIIntegration) return;
        XenoFactionsStoneDropHandler handler = new XenoFactionsStoneDropHandler();
        API.registerRecipeHandler(handler);
        API.registerUsageHandler(handler);
    }

    public String getName() { return StatCollector.translateToLocal("hfr.nei.integration.name"); }
    public String getVersion() { return RefStrings.VERSION; }
}
