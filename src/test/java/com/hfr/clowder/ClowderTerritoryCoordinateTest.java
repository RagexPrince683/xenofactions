package com.hfr.clowder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import org.junit.After;
import org.junit.Test;

import com.hfr.clowder.ClowderTerritory.CoordPair;
import com.hfr.clowder.ClowderTerritory.Ownership;
import com.hfr.clowder.ClowderTerritory.TerritoryMeta;
import com.hfr.clowder.ClowderTerritory.Zone;

public class ClowderTerritoryCoordinateTest {
    private static final int[] BLOCK_COORDINATES = {-17, -16, -15, -2, -1, 0, 15, 16, 31};
    private static final int[] SHIFTED_CHUNKS = {-1, 0, 0, 0, 0, 0, 1, 1, 2};

    @After
    public void clearTerritories() {
        ClowderTerritory.territories.clear();
    }

    @Test
    public void legacyShiftedConversionIsConsistentOnBothAxes() {
        for(int xIndex = 0; xIndex < BLOCK_COORDINATES.length; xIndex++) {
            for(int zIndex = 0; zIndex < BLOCK_COORDINATES.length; zIndex++) {
                int x = BLOCK_COORDINATES[xIndex];
                int z = BLOCK_COORDINATES[zIndex];
                CoordPair pair = ClowderTerritory.getCoordPair(7, x, z);
                assertEquals(SHIFTED_CHUNKS[xIndex], pair.x);
                assertEquals(SHIFTED_CHUNKS[zIndex], pair.z);

                Ownership ownership = new Ownership(Zone.SAFEZONE);
                TerritoryMeta meta = new TerritoryMeta(ownership);
                ClowderTerritory.territories.put(pair, meta);
                assertSame(meta, ClowderTerritory.getMetaFromIntCoords(7, x, z));
                assertSame(ownership, ClowderTerritory.getOwnerFromInts(7, x, z));
                ClowderTerritory.territories.clear();
            }
        }
    }
}
