package com.hbm.render.postprocess;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

import com.hbm.main.MainRegistry;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResource;
import net.minecraft.util.ResourceLocation;

/** Small GLSL 1.20 program with cached uniform locations and one-shot diagnostics. */
final class DarkAdaptationShader {
	private int program;
	private int vertex;
	private int fragment;
	private int source, depth, texel, projectionScale, adaptation, cone, rod, strength, ambientScotopic, scotopicFloor, noise, centerLoss, quality, time, hasDepth, nearPlane, farPlane, debugView;
	private String failureReason = "none";

	boolean load() {
		destroy();
		try {
			vertex = compile("dark_adaptation.vert", GL20.GL_VERTEX_SHADER);
			fragment = compile("dark_adaptation.frag", GL20.GL_FRAGMENT_SHADER);
			program = GL20.glCreateProgram();
			GL20.glAttachShader(program, vertex); GL20.glAttachShader(program, fragment); GL20.glLinkProgram(program);
			if(GL20.glGetProgrami(program, GL20.GL_LINK_STATUS) == GL11.GL_FALSE)
				throw new IllegalStateException("link: " + GL20.glGetProgramInfoLog(program, 4096));
			source = uniform("source"); depth = uniform("depthSource"); texel = uniform("texel"); projectionScale = uniform("projectionScale"); adaptation = uniform("adaptation");
			cone = uniform("coneAdaptation"); rod = uniform("rodAdaptation"); strength = uniform("strength"); noise = uniform("noiseAmount");
			ambientScotopic = uniform("ambientScotopic"); scotopicFloor = uniform("scotopicFloor");
			centerLoss = uniform("centerLoss"); quality = uniform("quality"); time = uniform("time"); hasDepth = uniform("hasDepth");
			nearPlane = uniform("nearPlane"); farPlane = uniform("farPlane"); debugView = uniform("debugView");
			failureReason = "none";
			return true;
		} catch(Exception ex) {
			failureReason = "Shader load: " + ex.getMessage();
			MainRegistry.logger.warn("Dark adaptation shader disabled (" + ex.getMessage() + ")");
			destroy(); return false;
		}
	}

	private int compile(String name, int stage) throws Exception {
		IResource resource = Minecraft.getMinecraft().getResourceManager().getResource(new ResourceLocation("hbm", "shaders/" + name));
		String text;
		try(BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
			text = reader.lines().collect(Collectors.joining("\n"));
		}
		int shader = GL20.glCreateShader(stage); GL20.glShaderSource(shader, text); GL20.glCompileShader(shader);
		if(GL20.glGetShaderi(shader, GL20.GL_COMPILE_STATUS) == GL11.GL_FALSE)
			throw new IllegalStateException(name + ": " + GL20.glGetShaderInfoLog(shader, 4096));
		return shader;
	}

	private int uniform(String name) { return GL20.glGetUniformLocation(program, name); }
	void use(int width, int height, DarkAdaptationState state, float configuredStrength, float configuredScotopicFloor, float environmentalScotopic, float configuredNoise, float configuredCenter, int configuredQuality, boolean depthAvailable, float configuredFarPlane, float projectionScaleX, float projectionScaleY, int configuredDebugView) {
		GL20.glUseProgram(program);
		GL20.glUniform1i(source, 0); GL20.glUniform1i(depth, 1); GL20.glUniform1i(hasDepth, depthAvailable ? 1 : 0); GL20.glUniform2f(texel, 1F / width, 1F / height);
		GL20.glUniform1f(adaptation, state.getEffectiveAdaptation()); GL20.glUniform1f(cone, state.getConeAdaptation()); GL20.glUniform1f(rod, state.getRodAdaptation());
		GL20.glUniform1f(strength, configuredStrength); GL20.glUniform1f(noise, configuredNoise);
		GL20.glUniform1f(ambientScotopic, environmentalScotopic); GL20.glUniform1f(scotopicFloor, configuredScotopicFloor);
		GL20.glUniform1f(centerLoss, configuredCenter); GL20.glUniform1i(quality, configuredQuality);
		GL20.glUniform2f(projectionScale, projectionScaleX, projectionScaleY);
		GL20.glUniform1f(nearPlane, 0.05F); GL20.glUniform1f(farPlane, configuredFarPlane); GL20.glUniform1i(debugView, configuredDebugView);
		GL20.glUniform1f(time, (System.nanoTime() & 0xFFFFFFL) / 1000000F);
	}
	boolean isLoaded() { return program != 0; }
	String getFailureReason() { return failureReason; }
	void destroy() {
		if(program != 0) { if(vertex != 0) GL20.glDetachShader(program, vertex); if(fragment != 0) GL20.glDetachShader(program, fragment); GL20.glDeleteProgram(program); }
		if(vertex != 0) GL20.glDeleteShader(vertex); if(fragment != 0) GL20.glDeleteShader(fragment);
		program = vertex = fragment = 0;
	}
}
