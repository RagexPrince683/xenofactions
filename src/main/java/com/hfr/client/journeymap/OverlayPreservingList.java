package com.hfr.client.journeymap;

import java.util.ArrayList;
import java.util.Collection;

/** Raw by design: MapState's generic List field is compatible through erasure. */
@SuppressWarnings({ "rawtypes", "unchecked" })
public final class OverlayPreservingList extends ArrayList {
	private static final long serialVersionUID = 1L;
	private final Object overlay;
	public OverlayPreservingList(Object overlay, Collection existing) {
		this.overlay = overlay; super.add(overlay); if(existing != null) super.addAll(existing);
	}
	@Override public void clear() { super.clear(); super.add(overlay); }
	public boolean preserves(Object candidate) { return overlay == candidate; }
}
