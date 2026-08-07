package com.hfr.tileentity.machine;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertNotSame;

public class MachineDisplaySnapshotTest {
    @Test public void replacementAndClearingWork() { MachineDisplaySnapshot.clearClientSnapshot(); MachineDisplaySnapshot runtime = MachineDisplaySnapshot.forClientDisplay(); MachineDisplaySnapshot custom = new MachineDisplaySnapshot(1,2,3,4,5,6,7,8,9,10,11,12,13,14); MachineDisplaySnapshot.replaceClientSnapshot(custom); assertSame(custom, MachineDisplaySnapshot.forClientDisplay()); MachineDisplaySnapshot.clearClientSnapshot(); assertNotSame(custom, MachineDisplaySnapshot.forClientDisplay()); assertEquals(runtime.whaleChance, MachineDisplaySnapshot.forClientDisplay().whaleChance); }
}
