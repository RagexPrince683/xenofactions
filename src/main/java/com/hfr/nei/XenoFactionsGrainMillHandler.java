package com.hfr.nei;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class XenoFactionsGrainMillHandler extends XenoFactionsMachineHandler {
    public XenoFactionsGrainMillHandler() {
        super(GRAIN_MILL);
    }
}
