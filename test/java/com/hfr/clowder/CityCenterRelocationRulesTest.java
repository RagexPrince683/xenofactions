package com.hfr.clowder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.junit.Test;
import org.junit.After;

import com.hfr.config.XFConfig;
import com.hfr.tileentity.clowder.TileEntityFlag;

public class CityCenterRelocationRulesTest {
    @After
    public void clearTerritory() { ClowderTerritory.territories.clear(); }

    @Test
    public void nullAndUnregisteredOwnersAreOrphaned() {
        assertEquals(true, CityCenterRelocationManager.isOrphaned(new TileEntityFlag()));
        assertEquals(false, CityCenterRelocationManager.isLiveFactionOwner(null, Collections.<Clowder>emptyList()));
        Clowder owner=new Clowder(); owner.uuid="owner-id";
        assertEquals(false, CityCenterRelocationManager.isLiveFactionOwner(owner, Collections.<Clowder>emptyList()));
    }

    @Test
    public void registeredInstanceIsLiveEvenWithoutUuid() {
        Clowder owner=new Clowder(); owner.uuid=null;
        assertEquals(true, CityCenterRelocationManager.isLiveFactionOwner(owner, Arrays.asList(owner)));
        owner.uuid="";
        assertEquals(true, CityCenterRelocationManager.isLiveFactionOwner(owner, Arrays.asList(owner)));
    }

    @Test
    public void equivalentRegisteredUuidIsLiveButEmptyUuidsDoNotMatch() {
        Clowder owner=new Clowder(), reloaded=new Clowder();
        owner.uuid="faction-id"; reloaded.uuid="faction-id";
        assertEquals(true, CityCenterRelocationManager.isLiveFactionOwner(owner, Arrays.asList(reloaded)));
        owner.uuid=null; reloaded.uuid=null;
        assertEquals(false, CityCenterRelocationManager.isLiveFactionOwner(owner, Arrays.asList(reloaded)));
        owner.uuid=""; reloaded.uuid="";
        assertEquals(false, CityCenterRelocationManager.isLiveFactionOwner(owner, Arrays.asList(reloaded)));
    }

    @Test
    public void prestigeUsesHorizontalDistanceAndConfiguredFreeAllowance() {
        float oldBase=XFConfig.cityRelocationBasePrestigeCost, oldRate=XFConfig.cityRelocationPrestigePerExtraBlock;
        int oldFree=XFConfig.cityRelocationFreeDistanceBlocks;
        try {
            XFConfig.cityRelocationBasePrestigeCost=0F; XFConfig.cityRelocationPrestigePerExtraBlock=30F; XFConfig.cityRelocationFreeDistanceBlocks=10;
            assertEquals(0F, CityCenterRelocationManager.calculateCost(10D), 0F);
            assertEquals(30F, CityCenterRelocationManager.calculateCost(11D), 0F);
        } finally {
            XFConfig.cityRelocationBasePrestigeCost=oldBase; XFConfig.cityRelocationPrestigePerExtraBlock=oldRate; XFConfig.cityRelocationFreeDistanceBlocks=oldFree;
        }
    }

    @Test
    public void rollingLimitAndThirdMoveCooldownCountSuccessfulTimestamps() {
        Clowder faction=new Clowder(); String city="stable-city"; long now=1_000_000_000L;
        faction.cityRelocationHistory.put(city,new ArrayList<Long>());
        faction.cityRelocationHistory.get(city).add(Long.valueOf(now-60_000L));
        faction.cityRelocationHistory.get(city).add(Long.valueOf(now));
        assertNotNull(CityCenterRelocationManager.cooldownError(faction,city,now));
        assertNull(CityCenterRelocationManager.cooldownError(faction,city,now+30L*60L*1000L));
        faction.cityRelocationHistory.get(city).add(Long.valueOf(now+30L*60L*1000L));
        assertNotNull(CityCenterRelocationManager.cooldownError(faction,city,now+30L*60L*1000L));
        assertNull(CityCenterRelocationManager.cooldownError(faction,city,now+169L*60L*60L*1000L));
    }

    @Test
    public void relocationMovesCircularGeometryOneChunkEastAndPreservesUnrelatedClaims() {
        Clowder faction=new Clowder(); faction.uuid="owner";
        Clowder other=new Clowder(); other.uuid="other";
        String city="stable-city"; int radius=3, y=70;
        Set<ClowderTerritory.CoordPair> oldShape=ClowderTerritory.getCityClaimCoordinates(0,0,0,radius);
        ClowderTerritory.rebuildCityClaims(ClowderTerritory.territories,faction,city,0,0,y,0,"Alpha",2,radius);
        ClowderTerritory.CoordPair otherAt=new ClowderTerritory.CoordPair(0,20,20);
        ClowderTerritory.TerritoryMeta otherMeta=meta(other,"other-city",320,y,320,"Beta",1);
        ClowderTerritory.territories.put(otherAt,otherMeta);
        ClowderTerritory.CoordPair safeAt=new ClowderTerritory.CoordPair(0,21,20);
        ClowderTerritory.TerritoryMeta safeMeta=new ClowderTerritory.TerritoryMeta(ClowderTerritory.SAFEZONE);
        ClowderTerritory.territories.put(safeAt,safeMeta);

        Set<ClowderTerritory.CoordPair> moved=ClowderTerritory.rebuildCityClaims(
            ClowderTerritory.territories,faction,city,0,16,y,0,"Alpha",2,radius);

        assertEquals(ClowderTerritory.getCityClaimCoordinates(0,16,0,radius),moved);
        assertEquals(moved.size(),count(city));
        assertTrue(moved.contains(ClowderTerritory.getCoordPair(0,16,0)));
        for(Map.Entry<ClowderTerritory.CoordPair,ClowderTerritory.TerritoryMeta> entry:ClowderTerritory.territories.entrySet()) {
            if(city.equals(entry.getValue().cityId)) {
                assertEquals(16,entry.getValue().flagX); assertEquals(y,entry.getValue().flagY);
                assertEquals(0,entry.getValue().flagZ); assertSame(faction,entry.getValue().owner.owner);
            }
        }
        Set<ClowderTerritory.CoordPair> western=new java.util.HashSet<ClowderTerritory.CoordPair>(oldShape); western.removeAll(moved);
        Set<ClowderTerritory.CoordPair> eastern=new java.util.HashSet<ClowderTerritory.CoordPair>(moved); eastern.removeAll(oldShape);
        assertTrue(!western.isEmpty() && !eastern.isEmpty());
        for(ClowderTerritory.CoordPair coordinate:western) assertNull(ClowderTerritory.territories.get(coordinate));
        for(ClowderTerritory.CoordPair coordinate:eastern) assertEquals(city,ClowderTerritory.territories.get(coordinate).cityId);
        assertSame(otherMeta,ClowderTerritory.territories.get(otherAt));
        assertSame(safeMeta,ClowderTerritory.territories.get(safeAt));
    }

    @Test
    public void failedRebuildIsAtomicAndRepeatedMovesUseLatestCenter() {
        Clowder faction=new Clowder(); faction.uuid="owner"; String city="stable";
        ClowderTerritory.rebuildCityClaims(ClowderTerritory.territories,faction,city,0,0,64,0,"City",0,2);
        ClowderTerritory.CoordPair collision=ClowderTerritory.getCoordPair(0,32,0);
        ClowderTerritory.TerritoryMeta protectedMeta=new ClowderTerritory.TerritoryMeta(ClowderTerritory.WARZONE);
        ClowderTerritory.territories.put(collision,protectedMeta);
        Map<ClowderTerritory.CoordPair,ClowderTerritory.TerritoryMeta> before=new HashMap<ClowderTerritory.CoordPair,ClowderTerritory.TerritoryMeta>(ClowderTerritory.territories);
        try {
            ClowderTerritory.rebuildCityClaims(ClowderTerritory.territories,faction,city,0,16,64,0,"City",0,2);
            throw new AssertionError("expected overlap failure");
        } catch(IllegalStateException expected) { assertEquals(before,ClowderTerritory.territories); }
        ClowderTerritory.territories.remove(collision);
        ClowderTerritory.rebuildCityClaims(ClowderTerritory.territories,faction,city,0,16,64,0,"City",0,2);
        Set<ClowderTerritory.CoordPair> latest=ClowderTerritory.rebuildCityClaims(ClowderTerritory.territories,faction,city,0,16,64,16,"City",0,2);
        assertEquals(latest.size(),count(city));
        for(ClowderTerritory.TerritoryMeta meta:ClowderTerritory.territories.values()) if(city.equals(meta.cityId)) assertEquals(16,meta.flagZ);
    }

    @Test
    public void cityShapeUsesLegacyConversionAcrossChunkBoundaries() {
        int[] blocks={-17,-16,-1,0,15,16};
        for(int x:blocks) for(int z:blocks) {
            Set<ClowderTerritory.CoordPair> shape=ClowderTerritory.getCityClaimCoordinates(-1,x,z,1);
            assertEquals(1,shape.size());
            assertTrue(shape.contains(ClowderTerritory.getCoordPair(-1,x,z)));
        }
    }

    private static int count(String city) { int count=0; for(ClowderTerritory.TerritoryMeta meta:ClowderTerritory.territories.values()) if(city.equals(meta.cityId)) count++; return count; }
    private static ClowderTerritory.TerritoryMeta meta(Clowder owner,String id,int x,int y,int z,String name,int level) {
        ClowderTerritory.TerritoryMeta meta=new ClowderTerritory.TerritoryMeta(new ClowderTerritory.Ownership(ClowderTerritory.Zone.FACTION,owner),x,y,z);
        meta.cityId=id; meta.name=name; meta.cityName=name; meta.cityLevel=level; return meta;
    }
}
