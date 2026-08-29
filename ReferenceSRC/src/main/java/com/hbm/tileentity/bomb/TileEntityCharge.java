package com.hbm.tileentity.bomb;

import com.hbm.blocks.bomb.BlockChargeBase;
import com.hbm.items.ModItems;
import com.hbm.packet.PacketDispatcher;
import com.hbm.packet.toclient.NBTPacket;
import com.hbm.tileentity.INBTPacketReceiver;

import cpw.mods.fml.common.network.NetworkRegistry.TargetPoint;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Vec3;

public class TileEntityCharge extends TileEntity implements INBTPacketReceiver {

	//DISARM???
	public boolean defusePending = false;
	public int defusePendingTicks = 0;
	public static final int DEFUSE_DELAY_TICKS = 100; // 5 seconds (100 ticks)

	public String defusingPlayer = null; // name of the player defusing
	public static final double MAX_DEFUSE_DISTANCE = 5.0D; // distance limit

	public boolean started;
	public int timer;

	@Override
	public boolean canUpdate() {
		return true;
	}

	@Override
	public void updateEntity() {

		if(!worldObj.isRemote) {

			// --- Minimal pending defuse processing (runs server-side) ---
			if(this.defusePending) {

				EntityPlayer defuser = worldObj.getPlayerEntityByName(defusingPlayer);
				boolean validDefuse = false;

				if(defuser != null) {
					double dx = defuser.posX - (xCoord + 0.5D);
					double dy = defuser.posY + defuser.getEyeHeight() - (yCoord + 0.5D);
					double dz = defuser.posZ - (zCoord + 0.5D);
					double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);

					ItemStack held = defuser.getHeldItem();
					boolean holdingDefuser = (held != null && held.getItem() == ModItems.defuser);

					// Check distance, item, and look direction
					if(dist <= MAX_DEFUSE_DISTANCE && holdingDefuser) {
						Vec3 look = defuser.getLookVec();
						Vec3 toBomb = Vec3.createVectorHelper(-dx, -dy, -dz).normalize();
						double dot = look.dotProduct(toBomb); // how close they are to looking at it
						if(dot > 0.5D) validDefuse = true; // roughly within a 60° cone
					}
				}

				if(!validDefuse) {
					// Cancel defuse if they look away, walk away, or stop holding defuser
					this.defusePending = false;
					this.defusingPlayer = null;
					this.defusePendingTicks = 0;
					worldObj.playSoundEffect(xCoord + 0.5D, yCoord + 0.5D, zCoord + 0.5D, "hbm:item.defuseCancel", 0.8F, 1.0F);
					return;
				}

				this.defusePendingTicks--;

				// if timer still positive, play a tick sound occasionally (optional)
				if(this.defusePendingTicks > 0 && this.defusePendingTicks % 20 == 0)
					worldObj.playSoundEffect(xCoord + 0.5D, yCoord + 0.5D, zCoord + 0.5D, "hbm:item.defuseTick", 0.8F, 1.0F);

				if(this.defusePendingTicks <= 0) {
					// Complete the deferred disarm exactly like original code
					this.defusePending = false;
					this.defusePendingTicks = 0;

					// Ensure the bomb won't explode when block removal triggers breakBlock
					BlockChargeBase.safe = true;
					// call the exact dismantle behaviour (this triggers breakBlock -> no explode due to safe)
					((BlockChargeBase)this.getBlockType()).dismantle(worldObj, xCoord, yCoord, zCoord);
					BlockChargeBase.safe = false;

					// mark and update
					this.markDirty();
					worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
				} else {
					// keep tile dirty so network update will carry countdown if you rely on it
					this.markDirty();
					if(this.defusePendingTicks % 10 == 0)
						worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
				}
			}

			if(started) {
				timer--;

				if(timer % 20 == 0 && timer > 0)
					worldObj.playSoundEffect(xCoord, yCoord, zCoord, "hbm:weapon.fstbmbPing", 1.0F, 1.0F);

				if(timer <= 0) {
					((BlockChargeBase)this.getBlockType()).explode(worldObj, xCoord, yCoord, zCoord);
				}
			}

			NBTTagCompound data = new NBTTagCompound();
			data.setInteger("timer", timer);
			data.setBoolean("started", started);
			data.setBoolean("defusePending", defusePending);
			data.setInteger("defusePendingTicks", defusePendingTicks);
			PacketDispatcher.wrapper.sendToAllAround(new NBTPacket(data, xCoord, yCoord, zCoord), new TargetPoint(this.worldObj.provider.dimensionId, xCoord, yCoord, zCoord, 100));

			if (defusePending) {
				// keep the block actively ticking
				worldObj.scheduleBlockUpdate(xCoord, yCoord, zCoord, this.getBlockType(), 1);
			}

		}
	}

	@Override
	public void networkUnpack(NBTTagCompound data) {
		timer = data.getInteger("timer");
		started = data.getBoolean("started");
		defusePending = data.getBoolean("defusePending");
		defusePendingTicks = data.getInteger("defusePendingTicks");
	}

	public String getMinutes() {

		String mins = "" + (timer / 1200);

		if(mins.length() == 1)
			mins = "0" + mins;

		return mins;
	}

	public String getSeconds() {

		String mins = "" + ((timer / 20) % 60);

		if(mins.length() == 1)
			mins = "0" + mins;

		return mins;
	}
}
