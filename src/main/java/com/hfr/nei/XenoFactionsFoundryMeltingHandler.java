package com.hfr.nei;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class XenoFactionsFoundryMeltingHandler extends XenoFactionsMachineHandler {
    public XenoFactionsFoundryMeltingHandler() {
        super(FOUNDRY_MELT);
    }
}
