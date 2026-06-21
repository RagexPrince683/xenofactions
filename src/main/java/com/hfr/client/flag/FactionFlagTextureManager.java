package com.hfr.client.flag;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import com.hfr.clowder.Clowder;
import com.hfr.packet.PacketDispatcher;
import com.hfr.packet.effect.FactionFlagMetadataPacket;
import com.hfr.render.hud.RenderFlagOverlay;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.util.ResourceLocation;

@SideOnly(Side.CLIENT)
public class FactionFlagTextureManager {

	private static final Map<String, String> HASHES = new HashMap<String, String>();
	private static final Map<String, ResourceLocation> TEXTURES = new HashMap<String, ResourceLocation>();
	private static final ResourceLocation FALLBACK = new ResourceLocation("hfr:textures/flags/flag_tri.png");

	public static ResourceLocation getFlagTexture(Clowder faction) {
		if(faction == null)
			return FALLBACK;
		return getFlagTexture(faction.name);
	}

	public static ResourceLocation getFlagTexture(String factionName) {
		ResourceLocation texture = TEXTURES.get(factionName);
		return texture == null ? FALLBACK : texture;
	}

	public static void requestFlagIfMissing(Clowder faction) {
		if(faction != null)
			requestFlagIfMissing(faction.name);
	}

	public static void requestFlagIfMissing(String factionName) {
		String hash = HASHES.get(factionName);
		if(hash != null && hash.length() > 0 && !TEXTURES.containsKey(factionName))
			PacketDispatcher.wrapper.sendToServer(new FactionFlagMetadataPacket(factionName, hash));
	}

	public static void clearFlag(Clowder faction) {
		if(faction != null)
			clearFlag(faction.name);
	}

	public static void clearFlag(String factionName) {
		HASHES.remove(factionName);
		TEXTURES.remove(factionName);
		File file = getCacheFile(factionName);
		if(file.exists())
			file.delete();
	}

	public static void handleMetadata(String factionName, String hash) {
		if(factionName == null || factionName.length() == 0)
			return;
		if(hash == null || hash.length() == 0) {
			clearFlag(factionName);
			return;
		}
		String old = HASHES.get(factionName);
		ResourceLocation existing = TEXTURES.get(factionName);
		HASHES.put(factionName, hash);
		if(old != null && old.equals(hash) && existing != null)
			return;
		if(old == null || !old.equals(hash))
			TEXTURES.remove(factionName);
		if(!loadCached(factionName, hash))
			requestFlagIfMissing(factionName);
	}

	public static void handleBytes(String factionName, String hash, byte[] png) {
		if(factionName == null || hash == null || png == null || png.length == 0)
			return;
		try {
			if(!hash.equals(sha256(png)))
				return;
			HASHES.put(factionName, hash);
			BufferedImage image = ImageIO.read(new ByteArrayInputStream(png));
			if(image == null)
				return;
			File dir = getCacheDir();
			if(!dir.exists())
				dir.mkdirs();
			FileOutputStream out = new FileOutputStream(getCacheFile(factionName));
			try {
				out.write(png);
			} finally {
				out.close();
			}
			registerTexture(factionName, image);
		} catch(Exception e) { }
	}

	private static boolean loadCached(String factionName, String hash) {
		try {
			File file = getCacheFile(factionName);
			if(!file.exists())
				return false;
			if(!file.getName().equals(safe(factionName) + "." + safe(hash) + ".png"))
				return false;
			BufferedImage image = ImageIO.read(file);
			if(image == null)
				return false;
			registerTexture(factionName, image);
			return true;
		} catch(Exception e) {
			return false;
		}
	}

	private static void registerTexture(String factionName, BufferedImage image) {
		DynamicTexture dynamic = new DynamicTexture(image);
		ResourceLocation location = Minecraft.getMinecraft().getTextureManager().getDynamicTextureLocation("xenofactions_flag_" + safe(factionName), dynamic);
		TEXTURES.put(factionName, location);
		if(factionName != null && factionName.equals(RenderFlagOverlay.customFlagName)) {
			RenderFlagOverlay.flag = location;
			RenderFlagOverlay.overlay = null;
			RenderFlagOverlay.tintFlag = false;
		}
	}

	private static File getCacheDir() {
		return new File(Loader.instance().getConfigDir(), "xenofactions/flag_cache");
	}

	private static File getCacheFile(String factionName) {
		String hash = HASHES.get(factionName);
		if(hash == null || hash.length() == 0)
			hash = "unknown";
		return new File(getCacheDir(), safe(factionName) + "." + safe(hash) + ".png");
	}

	private static String safe(String value) {
		return value == null ? "unknown" : value.replaceAll("[^A-Za-z0-9_.-]", "_");
	}

	private static String sha256(byte[] data) throws Exception {
		MessageDigest digest = MessageDigest.getInstance("SHA-256");
		byte[] hash = digest.digest(data);
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < hash.length; i++)
			sb.append(String.format("%02x", hash[i] & 0xff));
		return sb.toString();
	}
}
