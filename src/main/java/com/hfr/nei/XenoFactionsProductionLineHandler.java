package com.hfr.nei;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class XenoFactionsProductionLineHandler extends XenoFactionsMachineHandler {
    public XenoFactionsProductionLineHandler() {
        super(PRODUCTION_LINE);
    }
}
