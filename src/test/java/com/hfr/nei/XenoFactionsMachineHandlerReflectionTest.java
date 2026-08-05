package com.hfr.nei;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class XenoFactionsMachineHandlerReflectionTest {
    @Test
    public void concreteHandlersRecreateWithStableOverlayIdentifiers() throws Exception {
        assertRecreates(new XenoFactionsBlastFurnaceHandler(), XenoFactionsMachineHandler.BLAST);
        assertRecreates(new XenoFactionsFoundryMeltingHandler(), XenoFactionsMachineHandler.FOUNDRY_MELT);
        assertRecreates(new XenoFactionsFoundryCastingHandler(), XenoFactionsMachineHandler.FOUNDRY_CAST);
        assertRecreates(new XenoFactionsFishingNetHandler(), XenoFactionsMachineHandler.NET);
        assertRecreates(new XenoFactionsGrainMillHandler(), XenoFactionsMachineHandler.GRAIN_MILL);
        assertRecreates(new XenoFactionsUniversityHandler(), XenoFactionsMachineHandler.UNIVERSITY);
        assertRecreates(new XenoFactionsProductionLineHandler(), XenoFactionsMachineHandler.PRODUCTION_LINE);
        assertRecreates(new XenoFactionsTempleHandler(), XenoFactionsMachineHandler.TEMPLE);
        assertRecreates(new XenoFactionsCoalMineHandler(), XenoFactionsMachineHandler.COAL_MINE);
    }

    private void assertRecreates(XenoFactionsMachineHandler handler, String id) throws Exception {
        XenoFactionsMachineHandler recreated = handler.getClass().getConstructor().newInstance();
        assertEquals(id, handler.getOverlayIdentifier());
        assertEquals(handler.getOverlayIdentifier(), recreated.getOverlayIdentifier());
    }
}
