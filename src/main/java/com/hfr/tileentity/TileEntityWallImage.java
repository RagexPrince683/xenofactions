package com.hfr.tileentity;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TileEntityWallImage extends TileEntity {
    public String ownerUUID = "";
    public String imageName = ""; // name in player's catalog
    public String imageURL = "";  // actual URL
    public String textureKey = ""; // resource name used with texture manager

    @SideOnly(Side.CLIENT)
    private ResourceLocation texture; // transient
    @SideOnly(Side.CLIENT)
    private boolean downloading = false;

    // simple client-side cache shared by all tile entities (url -> ResourceLocation)
    @SideOnly(Side.CLIENT)
    public static final Map<String, ResourceLocation> clientCache = new HashMap<String, ResourceLocation>();

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        ownerUUID = tag.getString("owner");
        imageName = tag.getString("iname");
        imageURL = tag.getString("iurl");
        textureKey = tag.getString("tkey");
    }

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        tag.setString("owner", ownerUUID == null ? "" : ownerUUID);
        tag.setString("iname", imageName == null ? "" : imageName);
        tag.setString("iurl", imageURL == null ? "" : imageURL);
        tag.setString("tkey", textureKey == null ? "" : textureKey);
    }

    @Override
    public void updateEntity() {
        if (worldObj != null && worldObj.isRemote) {
            if ((texture == null) && (imageURL != null) && (imageURL.length() > 0) && !downloading) {
                if (clientCache.containsKey(imageURL)) {
                    texture = clientCache.get(imageURL);
                } else {
                    downloading = true;
                    final String url = imageURL;
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                BufferedImage img = ImageIO.read(new URL(url));
                                if (img == null) throw new IOException("ImageIO returned null");
                                DynamicTexture dyn = new DynamicTexture(img);
                                ResourceLocation rl = new ResourceLocation("yourmodid", "dynimg_" + Math.abs(url.hashCode()));
                                Minecraft.getMinecraft().getTextureManager().loadTexture(rl, dyn);
                                clientCache.put(url, rl);
                                texture = rl;
                            } catch (IOException e) {
                                e.printStackTrace();
                            } finally {
                                downloading = false;
                            }
                        }
                    }, "WallImageDownloader-" + hashCode()).start();
                }
            }
        }
    }

    @SideOnly(Side.CLIENT)
    public ResourceLocation getTexture() {
        if (texture == null && imageURL != null) {
            texture = clientCache.get(imageURL);
        }
        return texture;
    }
}
