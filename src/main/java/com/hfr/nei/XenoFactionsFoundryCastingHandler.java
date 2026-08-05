package com.hfr.nei;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class XenoFactionsFoundryCastingHandler extends XenoFactionsMachineHandler {
    public XenoFactionsFoundryCastingHandler() {
        super(FOUNDRY_CAST);
    }
}
