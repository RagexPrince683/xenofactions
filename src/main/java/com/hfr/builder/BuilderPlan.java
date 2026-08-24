package com.hfr.builder;

import net.minecraft.nbt.NBTTagCompound;

/** Server-owned draft which survives closing the Depot and world reloads. */
public final class BuilderPlan {
    public static final String NONE="",NATIVE="native",STORED="stored";
    public String source=NONE,schematicId="",displayName="",validationKey="gui.builder.validation.not_run";
    public int originX,originY,originZ,rotation;
    /** Coordinates have been deliberately initialized; world origin is a valid value. */
    public boolean initialized;
    public boolean mirrored,preview;
    public boolean selected(){return !source.isEmpty()&&!schematicId.isEmpty();}
    public void initializeNear(int x,int y,int z){if(!initialized){originX=x+2;originY=y;originZ=z+2;initialized=true;}}
    public void clear(String reason){source=NONE;schematicId="";displayName="";preview=false;validationKey=reason;}
    public NBTTagCompound write(){NBTTagCompound n=new NBTTagCompound();n.setBoolean("Initialized",initialized);n.setString("Source",source);n.setString("Schematic",schematicId);n.setString("Name",displayName);n.setString("Validation",validationKey);n.setInteger("X",originX);n.setInteger("Y",originY);n.setInteger("Z",originZ);n.setByte("Rotation",(byte)(rotation&3));n.setBoolean("Mirrored",mirrored);n.setBoolean("Preview",preview);return n;}
    public void read(NBTTagCompound n){source=n.getString("Source");schematicId=n.getString("Schematic");displayName=n.getString("Name");validationKey=n.getString("Validation");originX=n.getInteger("X");originY=n.getInteger("Y");originZ=n.getInteger("Z");rotation=n.getByte("Rotation")&3;mirrored=n.getBoolean("Mirrored");preview=n.getBoolean("Preview");initialized=n.hasKey("Initialized")?n.getBoolean("Initialized"):selected();}
}
