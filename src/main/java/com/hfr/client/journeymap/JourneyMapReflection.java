package com.hfr.client.journeymap;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

/** Resolves the verified JourneyMap 5.2.x internals once. */
final class JourneyMapReflection {
	final Class<?> drawStepClass;
	final Method miniState, fullscreenState, getPixel, getBlockPixel, getWidth, getHeight;
	final Field drawStepList;
	JourneyMapReflection() throws Exception {
		Class<?> mini = Class.forName("journeymap.client.ui.minimap.MiniMap");
		Class<?> fullscreen = Class.forName("journeymap.client.ui.fullscreen.Fullscreen");
		Class<?> state = Class.forName("journeymap.client.model.MapState");
		drawStepClass = Class.forName("journeymap.client.render.draw.DrawStep");
		Class<?> grid = Class.forName("journeymap.client.render.map.GridRenderer");
		if(!drawStepClass.isInterface()) throw new NoSuchMethodException("DrawStep is not an interface");
		miniState = mini.getMethod("state"); fullscreenState = fullscreen.getMethod("state");
		drawStepList = state.getDeclaredField("drawStepList"); drawStepList.setAccessible(true);
		getPixel = grid.getMethod("getPixel", double.class, double.class);
		getBlockPixel = grid.getMethod("getBlockPixelInGrid", double.class, double.class);
		getWidth = grid.getMethod("getWidth"); getHeight = grid.getMethod("getHeight");
		drawStepClass.getMethod("draw", double.class, double.class, grid, float.class, double.class, double.class);
	}
	Object state(boolean minimap) throws Exception { return (minimap ? miniState : fullscreenState).invoke(null); }
	@SuppressWarnings("rawtypes") List steps(Object state) throws Exception { return (List)drawStepList.get(state); }
	void setSteps(Object state, List<?> list) throws Exception { drawStepList.set(state, list); }
}
