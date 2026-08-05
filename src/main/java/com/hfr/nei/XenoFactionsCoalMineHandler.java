package com.hfr.nei;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class XenoFactionsCoalMineHandler extends XenoFactionsMachineHandler {
    public XenoFactionsCoalMineHandler() {
        super(COAL_MINE);
    }
}
