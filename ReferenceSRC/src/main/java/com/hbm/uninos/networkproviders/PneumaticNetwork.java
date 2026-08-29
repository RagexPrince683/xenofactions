package com.hbm.uninos.networkproviders;

import com.hbm.uninos.GenNode;
import com.hbm.uninos.NodeNet;

/**
 * Compatibility placeholder for upstream UniNodespace pneumatic providers.
 * RNT does not currently ship the upstream pneumatic tube subsystem, so this
 * network only participates in common lifecycle/reaping until the subsystem is
 * ported with its tile entities and inventory cache helpers.
 */
public class PneumaticNetwork extends NodeNet<Object, Object, GenNode> {

	@Override
	public void update() { }
}
