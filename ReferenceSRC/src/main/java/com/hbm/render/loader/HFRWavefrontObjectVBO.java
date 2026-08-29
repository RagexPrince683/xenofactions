package com.hbm.render.loader;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;

import net.minecraftforge.client.model.ModelFormatException;
import net.minecraftforge.client.model.obj.TextureCoordinate;
import net.minecraftforge.client.model.obj.Vertex;

public class HFRWavefrontObjectVBO implements IModelCustomNamed {

	private static final int VERTEX_SIZE = 3;
	private static final int UV_SIZE = 3;
	private static final List<HFRWavefrontObjectVBO> MODELS = new ArrayList<HFRWavefrontObjectVBO>();

	private final HFRWavefrontObject obj;
	private final List<VBOBufferData> groups = new ArrayList<VBOBufferData>();

	private static class VBOBufferData {
		String name;
		int vertices;
		int vertexHandle;
		int uvHandle;
		int normalHandle;
	}

	public HFRWavefrontObjectVBO(HFRWavefrontObject obj) {
		this.obj = obj;
		rebuild();
		MODELS.add(this);
	}

	public static void reloadModels() {
		for(HFRWavefrontObjectVBO model : new ArrayList<HFRWavefrontObjectVBO>(MODELS)) {
			model.reload();
		}
	}

	public static void deleteModels() {
		for(HFRWavefrontObjectVBO model : new ArrayList<HFRWavefrontObjectVBO>(MODELS)) {
			model.deleteBuffers();
		}
	}

	public void reload() {
		deleteBuffers();
		obj.reload();
		rebuild();
	}

	public void deleteBuffers() {
		for(VBOBufferData data : groups) {
			deleteBuffer(data.vertexHandle);
			deleteBuffer(data.uvHandle);
			deleteBuffer(data.normalHandle);
		}
		groups.clear();
	}

	private void deleteBuffer(int handle) {
		if(handle > 0) {
			GL15.glDeleteBuffers(handle);
		}
	}

	private void rebuild() {
		for(S_GroupObject g : obj.groupObjects) {
			if(g == null || g.faces.isEmpty()) {
				continue;
			}

			VBOBufferData data = new VBOBufferData();
			data.name = g.name;

			FloatBuffer vertexData = BufferUtils.createFloatBuffer(g.faces.size() * 3 * VERTEX_SIZE);
			FloatBuffer uvData = BufferUtils.createFloatBuffer(g.faces.size() * 3 * UV_SIZE);
			FloatBuffer normalData = BufferUtils.createFloatBuffer(g.faces.size() * 3 * VERTEX_SIZE);

			for(S_Face face : g.faces) {
				if(face.vertices.length != 3) {
					throw new ModelFormatException("VBO model '" + obj.getFileName() + "' contains non-triangulated group '" + g.name + "'");
				}

				for(int i = 0; i < face.vertices.length; i++) {
					Vertex vert = face.vertices[i];
					TextureCoordinate tex = new TextureCoordinate(0, 0);
					Vertex normal = face.getNormal(i);

					if(face.textureCoordinates != null && face.textureCoordinates.length > i) {
						tex = face.textureCoordinates[i];
					}

					data.vertices++;
					vertexData.put(new float[] { vert.x, vert.y, vert.z });
					uvData.put(new float[] { tex.u, tex.v, tex.w });
					normalData.put(new float[] { normal.x, normal.y, normal.z });
				}
			}

			vertexData.flip();
			uvData.flip();
			normalData.flip();

			data.vertexHandle = upload(vertexData);
			data.uvHandle = upload(uvData);
			data.normalHandle = upload(normalData);
			groups.add(data);
		}
	}

	private int upload(FloatBuffer data) {
		int handle = GL15.glGenBuffers();
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, handle);
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, data, GL15.GL_STATIC_DRAW);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
		return handle;
	}

	@Override
	public String getType() {
		return "obj_vbo";
	}

	private void renderVBO(VBOBufferData data) {
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, data.vertexHandle);
		GL11.glVertexPointer(VERTEX_SIZE, GL11.GL_FLOAT, 0, 0L);

		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, data.uvHandle);
		GL11.glTexCoordPointer(UV_SIZE, GL11.GL_FLOAT, 0, 0L);

		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, data.normalHandle);
		GL11.glNormalPointer(GL11.GL_FLOAT, 0, 0L);

		GL11.glEnableClientState(GL11.GL_VERTEX_ARRAY);
		GL11.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
		GL11.glEnableClientState(GL11.GL_NORMAL_ARRAY);
		GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, data.vertices);
		GL11.glDisableClientState(GL11.GL_VERTEX_ARRAY);
		GL11.glDisableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
		GL11.glDisableClientState(GL11.GL_NORMAL_ARRAY);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
	}

	@Override
	public void renderAll() {
		for(VBOBufferData data : groups) renderVBO(data);
	}

	@Override
	public void renderOnly(String... groupNames) {
		for(VBOBufferData data : groups) for(String name : groupNames) if(data.name.equalsIgnoreCase(name)) renderVBO(data);
	}

	@Override
	public void renderPart(String partName) {
		for(VBOBufferData data : groups) if(data.name.equalsIgnoreCase(partName)) renderVBO(data);
	}

	@Override
	public void renderAllExcept(String... excludedGroupNames) {
		for(VBOBufferData data : groups) {
			boolean skip = false;
			for(String name : excludedGroupNames) if(data.name.equalsIgnoreCase(name)) skip = true;
			if(!skip) renderVBO(data);
		}
	}

	@Override
	public List<String> getPartNames() {
		List<String> names = new ArrayList<String>();
		for(VBOBufferData data : groups) names.add(data.name);
		return names;
	}

	public static List<HFRWavefrontObjectVBO> getLoadedModels() {
		return Collections.unmodifiableList(MODELS);
	}
}
