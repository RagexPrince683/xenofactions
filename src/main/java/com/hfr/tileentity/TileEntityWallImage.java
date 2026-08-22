package com.hfr.tileentity;

import java.util.UUID;

import com.hfr.wallart.WallArtConstants;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;

/** The single, metadata-only controller for a complete Wall Art display. */
public class TileEntityWallImage extends TileEntity {
    private UUID displayId;
    private UUID ownerId;
    private String imageHash = "";
    private int facing = 2;
    private int width = 1;
    private int height = 1;
    private long requestGeneration;

    public UUID getDisplayId() { return displayId; }
    public UUID getOwnerId() { return ownerId; }
    public String getImageHash() { return imageHash; }
    public int getFacing() { return facing; }
    public int getDisplayWidth() { return width; }
    public int getDisplayHeight() { return height; }
    public long getRequestGeneration() { return requestGeneration; }
    public void initialize(UUID display, UUID owner, int face) { displayId = display; ownerId = owner; facing = WallArtConstants.validFacing(face) ? face : 2; }
    public long beginRequest() { return ++requestGeneration; }
    public void configure(int newWidth, int newHeight, String hash) { width = WallArtConstants.validSize(newWidth, newHeight) ? newWidth : 1; height = WallArtConstants.validSize(newWidth, newHeight) ? newHeight : 1; imageHash = WallArtConstants.validHash(hash) ? hash : ""; markDirty(); }

    @Override public Packet getDescriptionPacket() { NBTTagCompound n = new NBTTagCompound(); writeToNBT(n); return new S35PacketUpdateTileEntity(xCoord, yCoord, zCoord, 1, n); }
    @Override public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity packet) { readFromNBT(packet.func_148857_g()); }
    @Override public void readFromNBT(NBTTagCompound n) {
        super.readFromNBT(n);
        displayId = readUuid(n.getString("wallArtId")); ownerId = readUuid(n.getString("wallArtOwner"));
        facing = WallArtConstants.validFacing(n.getInteger("wallArtFacing")) ? n.getInteger("wallArtFacing") : 2;
        int w = n.getInteger("wallArtWidth"), h = n.getInteger("wallArtHeight");
        width = WallArtConstants.validSize(w, h) ? w : 1; height = WallArtConstants.validSize(w, h) ? h : 1;
        imageHash = WallArtConstants.validHash(n.getString("wallArtHash")) ? n.getString("wallArtHash") : "";
        requestGeneration = n.getLong("wallArtGeneration");
        // Legacy URL/image fields are deliberately ignored: clients must never fetch them.
    }
    @Override public void writeToNBT(NBTTagCompound n) {
        super.writeToNBT(n);
        n.setString("wallArtId", displayId == null ? "" : displayId.toString());
        n.setString("wallArtOwner", ownerId == null ? "" : ownerId.toString());
        n.setInteger("wallArtFacing", facing); n.setInteger("wallArtWidth", width); n.setInteger("wallArtHeight", height);
        n.setString("wallArtHash", imageHash == null ? "" : imageHash); n.setLong("wallArtGeneration", requestGeneration);
    }
    private static UUID readUuid(String value) { try { return value == null || value.length() == 0 ? null : UUID.fromString(value); } catch(IllegalArgumentException e) { return null; } }

    @Override public AxisAlignedBB getRenderBoundingBox() {
        double minX = xCoord, maxX = xCoord + 1, minZ = zCoord, maxZ = zCoord + 1;
        if(facing == 2) maxX = xCoord + width; else if(facing == 3) minX = xCoord - width + 1;
        else if(facing == 4) minZ = zCoord - width + 1; else if(facing == 5) maxZ = zCoord + width;
        return AxisAlignedBB.getBoundingBox(minX, yCoord, minZ, maxX, yCoord + height, maxZ);
    }
    @Override public double getMaxRenderDistanceSquared() { return 16384.0D; }
}
