package com.hfr.tileentity;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

// inside TileEntityWallImage.java (existing imports kept)
public class TileEntityWallImage extends TileEntity {
    public String ownerUUID = "";
    public String imageName = "";
    public String imageURL = "";
    public String textureKey = "";

    // NEW: track which index from owner's list is currently applied (-1 = none)
    public int currentIndex = -1;

    @SideOnly(Side.CLIENT)
    private ResourceLocation texture;
    @SideOnly(Side.CLIENT)
    private boolean downloading = false;

    // change client cache to key by textureKey if present else url
    @SideOnly(Side.CLIENT)
    public static final Map<String, ResourceLocation> clientCache = new HashMap<String, ResourceLocation>();

    @Override
    public Packet getDescriptionPacket() {
        NBTTagCompound tag = new NBTTagCompound();
        writeToNBT(tag);
        return new S35PacketUpdateTileEntity(xCoord, yCoord, zCoord, 1, tag);
    }

    @Override
    public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity pkt) {
        readFromNBT(pkt.func_148857_g());
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        ownerUUID = tag.getString("owner");
        imageName = tag.getString("iname");
        imageURL = tag.getString("iurl");
        textureKey = tag.getString("tkey");
        currentIndex = tag.hasKey("cindex") ? tag.getInteger("cindex") : -1;
    }

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        tag.setString("owner", ownerUUID == null ? "" : ownerUUID);
        tag.setString("iname", imageName == null ? "" : imageName);
        tag.setString("iurl", imageURL == null ? "" : imageURL);
        tag.setString("tkey", textureKey == null ? "" : textureKey);
        tag.setInteger("cindex", currentIndex);
    }

    private static BufferedImage scaleAndPad(BufferedImage input, int targetSize) {
        int ow = input.getWidth();
        int oh = input.getHeight();

        // scale proportionally
        double scale = Math.min((double)targetSize / ow, (double)targetSize / oh);
        int nw = (int)(ow * scale);
        int nh = (int)(oh * scale);

        // center on a square canvas
        BufferedImage out = new BufferedImage(targetSize, targetSize, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = out.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(input, (targetSize - nw) / 2, (targetSize - nh) / 2, nw, nh, null);
        g.dispose();
        return out;
    }

    @Override
    public void updateEntity() {
        if (worldObj != null && worldObj.isRemote) {
            if ((texture == null) && (imageURL != null) && (imageURL.length() > 0) && !downloading) {
                // compute cache key: prefer textureKey if available so we can force reloads
                final String cacheKey = (textureKey != null && textureKey.length() > 0) ? textureKey : imageURL;

                if (clientCache.containsKey(cacheKey)) {
                    texture = clientCache.get(cacheKey);
                } else {
                    downloading = true;
                    final String url = imageURL;
                    final String finalCacheKey = cacheKey;
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                BufferedImage img = ImageIO.read(new URL(url));
                                if (img == null) throw new IOException("ImageIO returned null for " + url);

                                // resize helper from earlier (keep it if you already added)
                                BufferedImage scaled = scaleAndMaybePad(img, 256, false);

                                DynamicTexture dyn = new DynamicTexture(scaled);

                                // Use the cacheKey as the resource name to allow forced reloads
                                ResourceLocation rl = new ResourceLocation("yourmodid", finalCacheKey);
                                Minecraft.getMinecraft().getTextureManager().loadTexture(rl, dyn);

                                clientCache.put(finalCacheKey, rl);
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
        if (texture == null && (textureKey != null && textureKey.length() > 0)) {
            texture = clientCache.get(textureKey);
        } else if (texture == null && imageURL != null) {
            texture = clientCache.get(imageURL);
        }
        return texture;
    }

    // add your scaleAndMaybePad helper here (or keep the one you already added)
    @SideOnly(Side.CLIENT)
    private static BufferedImage scaleAndMaybePad(BufferedImage src, int maxDim, boolean padSquare) {
        // same code as previously provided — keep it here
        int w = src.getWidth();
        int h = src.getHeight();
        float scale = 1.0f;
        if (w > maxDim || h > maxDim) {
            if (w >= h) scale = (float) maxDim / (float) w;
            else scale = (float) maxDim / (float) h;
        }
        int newW = Math.max(1, Math.round(w * scale));
        int newH = Math.max(1, Math.round(h * scale));

        BufferedImage tmp = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = tmp.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.drawImage(src, 0, 0, newW, newH, null);
        g2.dispose();

        if (padSquare) {
            int side = Math.max(newW, newH);
            BufferedImage square = new BufferedImage(side, side, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = square.createGraphics();
            g.setComposite(AlphaComposite.Clear);
            g.fillRect(0, 0, side, side);
            g.setComposite(AlphaComposite.SrcOver);
            int ox = (side - newW) / 2;
            int oy = (side - newH) / 2;
            g.drawImage(tmp, ox, oy, null);
            g.dispose();
            return square;
        } else {
            return tmp;
        }
    }
}

