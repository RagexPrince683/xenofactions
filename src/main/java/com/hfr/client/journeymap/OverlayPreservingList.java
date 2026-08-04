package com.hfr.client.journeymap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

/** Raw by design: MapState's generic List field is compatible through erasure. */
@SuppressWarnings({ "rawtypes", "unchecked" })
public final class OverlayPreservingList extends ArrayList {
	private static final long serialVersionUID = 1L;
	private final Object overlay;
	public OverlayPreservingList(Object overlay, Collection existing) {
		this.overlay = overlay; super.add(overlay); if(existing != null) super.addAll(existing);
	}
	@Override public void clear() { super.clear(); super.add(overlay); }
	@Override public Object remove(int index) { return get(index) == overlay ? overlay : super.remove(index); }
	@Override public boolean remove(Object object) { return object == overlay ? false : super.remove(object); }
	@Override protected void removeRange(int fromIndex, int toIndex) {
		for(Iterator iterator = subList(fromIndex, toIndex).iterator(); iterator.hasNext();) {
			if(iterator.next() != overlay) iterator.remove();
		}
	}
	@Override public Object set(int index, Object element) { return get(index) == overlay ? overlay : super.set(index, element); }
	public boolean preserves(Object candidate) { return overlay == candidate && contains(candidate); }
}
