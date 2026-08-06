package com.hfr.clowder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import org.junit.Test;

import com.hfr.config.XFConfig;
import com.hfr.tileentity.clowder.TileEntityFlag;

public class CityCenterRelocationRulesTest {
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
}
