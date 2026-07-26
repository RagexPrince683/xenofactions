package com.hfr.client.journeymap;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import com.hfr.main.MainRegistry;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.ModContainer;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.common.network.FMLNetworkEvent.ClientDisconnectionFromServerEvent;
import net.minecraftforge.event.world.WorldEvent;

/** Optional, reflection-only bridge for legacy JourneyMap 5.2.x. */
public final class XFJourneyMapIntegration {
	private JourneyMapReflection reflection; private Object miniProxy, fullscreenProxy, miniState, fullscreenState;
	private boolean incompatible, logged;
	public static void register() {
		if(!Loader.isModLoaded("journeymap")) return;
		XFJourneyMapIntegration value = new XFJourneyMapIntegration();
		cpw.mods.fml.common.FMLCommonHandler.instance().bus().register(value);
		net.minecraftforge.common.MinecraftForge.EVENT_BUS.register(value);
	}
	@SubscribeEvent public void tick(TickEvent.ClientTickEvent event) {
		if(event.phase != TickEvent.Phase.END || incompatible) return;
		try {
			if(reflection == null) { reflection = new JourneyMapReflection(); miniProxy = proxy(true); fullscreenProxy = proxy(false); }
			miniState = attach(true, miniState, miniProxy); fullscreenState = attach(false, fullscreenState, fullscreenProxy);
		} catch(Throwable failure) { fail("required legacy class/member unavailable", failure); }
	}
	private Object proxy(boolean minimap) {
		return Proxy.newProxyInstance(reflection.drawStepClass.getClassLoader(), new Class[] { reflection.drawStepClass }, new JourneyMapClaimDrawHandler(minimap, reflection, this));
	}
	@SuppressWarnings("rawtypes") private Object attach(boolean minimap, Object previous, Object overlay) throws Exception {
		Object state = reflection.state(minimap); if(state == null) return null; List list = reflection.steps(state);
		if(state == previous && list instanceof OverlayPreservingList && ((OverlayPreservingList)list).preserves(overlay)) return state;
		if(!(list instanceof OverlayPreservingList)) reflection.setSteps(state, new OverlayPreservingList(overlay, list)); return state;
	}
	@SubscribeEvent public void disconnect(ClientDisconnectionFromServerEvent event) { reset(); }
	@SubscribeEvent public void unload(WorldEvent.Unload event) { if(event.world != null && event.world.isRemote) reset(); }
	@SuppressWarnings({ "rawtypes", "unchecked" }) private void reset() {
		ClientClaimOverlayCache.clear();
		try { detach(miniState, miniProxy); detach(fullscreenState, fullscreenProxy); } catch(Throwable ignored) { }
		miniState = fullscreenState = miniProxy = fullscreenProxy = null; reflection = null;
	}
	@SuppressWarnings({ "rawtypes", "unchecked" }) private void detach(Object state, Object overlay) throws Exception {
		if(reflection == null || state == null || overlay == null) return;
		List old = reflection.steps(state); ArrayList normal = new ArrayList(old); while(normal.remove(overlay)) { }
		reflection.setSteps(state, normal);
	}
	void fail(String reason, Throwable failure) {
		incompatible = true; ClientClaimOverlayCache.clear(); miniState = fullscreenState = miniProxy = fullscreenProxy = null; reflection = null;
		if(!logged && MainRegistry.logger != null) { logged = true; MainRegistry.logger.warn("JourneyMap claim overlays disabled for this session (" + version() + "): " + reason + ": " + failure, failure); }
	}
	private String version() { ModContainer mod = Loader.instance().getIndexedModList().get("journeymap"); return mod == null ? "unknown JourneyMap version" : "JourneyMap " + mod.getVersion(); }
}
