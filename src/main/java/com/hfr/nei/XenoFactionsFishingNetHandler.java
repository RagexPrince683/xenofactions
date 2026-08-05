package com.hfr.nei;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class XenoFactionsFishingNetHandler extends XenoFactionsMachineHandler {
    public XenoFactionsFishingNetHandler() {
        super(NET);
    }
}
