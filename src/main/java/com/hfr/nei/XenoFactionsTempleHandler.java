package com.hfr.nei;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class XenoFactionsTempleHandler extends XenoFactionsMachineHandler {
    public XenoFactionsTempleHandler() {
        super(TEMPLE);
    }
}
