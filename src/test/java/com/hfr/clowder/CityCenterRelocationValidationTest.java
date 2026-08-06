package com.hfr.clowder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.hfr.clowder.ClowderTerritory.Ownership;
import com.hfr.clowder.ClowderTerritory.TerritoryMeta;
import com.hfr.clowder.ClowderTerritory.Zone;

public class CityCenterRelocationValidationTest {
    @Test
    public void stableFactionComparisonUsesNonEmptyUuid() {
        Clowder canonical = faction("same");
        Clowder reloaded = faction("same");
        assertTrue(Clowder.sameFaction(canonical, canonical));
        assertTrue(Clowder.sameFaction(canonical, reloaded));
        assertFalse(Clowder.sameFaction(canonical, faction("different")));
        assertFalse(Clowder.sameFaction(canonical, faction("")));
    }

    @Test
    public void metadataValidationReportsExactFailedCondition() {
        Clowder canonical = faction("faction-id");
        TerritoryMeta meta = metadata(faction("faction-id"), "city-id", 7, -17, 70, 31);
        assertNull(CityCenterRelocationManager.getMetadataInvalidReason(canonical, 7, -17, 70, 31, "city-id", meta));

        meta.flagX = -16;
        assertEquals("metadata flag X differs",
            CityCenterRelocationManager.getMetadataInvalidReason(canonical, 7, -17, 70, 31, "city-id", meta));
        meta.flagX = -17;
        meta.owner = new Ownership(Zone.FACTION, faction("other-faction"));
        assertEquals("metadata faction UUID differs",
            CityCenterRelocationManager.getMetadataInvalidReason(canonical, 7, -17, 70, 31, "city-id", meta));
        meta.owner = new Ownership(Zone.FACTION, faction("faction-id"));
        meta.cityId = "other-city";
        assertEquals("metadata stable city ID differs",
            CityCenterRelocationManager.getMetadataInvalidReason(canonical, 7, -17, 70, 31, "city-id", meta));
    }

    private static Clowder faction(String uuid) {
        Clowder faction = new Clowder();
        faction.uuid = uuid;
        return faction;
    }

    private static TerritoryMeta metadata(Clowder faction, String cityId, int dim, int x, int y, int z) {
        TerritoryMeta meta = new TerritoryMeta(new Ownership(Zone.FACTION, faction), x, y, z);
        meta.dimensionId = dim;
        meta.cityId = cityId;
        return meta;
    }
}
