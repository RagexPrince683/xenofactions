package com.hbm.render.postprocess;

import com.hbm.config.ClientConfig;

/** Time-based cone/rod model, deliberately independent from rendering and frame rate. */
public final class DarkAdaptationState {
	private float coneAdaptation;
	private float rodAdaptation;
	private float exposure = 1F;

	public void update(float measuredExposure, float deltaSeconds, boolean nuclearFlash) {
		exposure = clamp(nuclearFlash ? Math.max(measuredExposure, 1F) : measuredExposure, 0F, 1F);
		float target = 1F - smoothstep(0.055F, 0.38F, exposure);
		if(nuclearFlash) target = 0F;
		float dt = clamp(deltaSeconds, 0F, 0.1F); // lag, focus changes and alt-tab cannot skip adaptation
		float coneSeconds = target < coneAdaptation ? 0.25F : durationToTimeConstant(4.5F);
		float rodSeconds = target < rodAdaptation ? 1.15F : durationToTimeConstant(clamp(ClientConfig.DARK_ADAPTATION_ROD_SECONDS.get(), 10F, 300F));
		coneAdaptation = approach(coneAdaptation, target, dt, coneSeconds);
		rodAdaptation = approach(rodAdaptation, target, dt, rodSeconds);
	}

	private static float approach(float value, float target, float delta, float seconds) {
		return value + (target - value) * (1F - (float)Math.exp(-delta / seconds));
	}

	/** Three exponential time constants reach approximately 95% of a requested transition. */
	private static float durationToTimeConstant(float requestedSeconds) { return requestedSeconds / 3F; }

	private static float smoothstep(float low, float high, float value) {
		float x = clamp((value - low) / (high - low), 0F, 1F);
		return x * x * (3F - 2F * x);
	}

	public void reset() { coneAdaptation = rodAdaptation = 0F; exposure = 1F; }
	public float getConeAdaptation() { return coneAdaptation; }
	public float getRodAdaptation() { return rodAdaptation; }
	public float getEffectiveAdaptation() { return coneAdaptation * 0.25F + rodAdaptation * 0.75F; }
	public float getExposure() { return exposure; }
	static float clamp(float value, float low, float high) { return Math.max(low, Math.min(high, value)); }
}
