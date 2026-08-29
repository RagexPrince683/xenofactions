package com.hbm.packet.toclient;

import com.hbm.main.ModEventHandlerClient;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import com.hbm.main.MainRegistry;

/** Ground-motion cue; contains physical source data and applies no damage or hurt animation. */
public class NuclearSeismicPacket implements IMessage {
 private double x,y,z,yieldKt,coupling,burialDepth; private int duration; private float baseIntensity;
 public NuclearSeismicPacket() { }
 public NuclearSeismicPacket(double x,double y,double z,double yieldKt,double coupling,double burialDepth,int duration,float baseIntensity){this.x=x;this.y=y;this.z=z;this.yieldKt=yieldKt;this.coupling=coupling;this.burialDepth=burialDepth;this.duration=duration;this.baseIntensity=baseIntensity;}
 public void fromBytes(ByteBuf b){x=b.readDouble();y=b.readDouble();z=b.readDouble();yieldKt=b.readDouble();coupling=b.readDouble();burialDepth=b.readDouble();duration=b.readInt();baseIntensity=b.readFloat();}
 public void toBytes(ByteBuf b){b.writeDouble(x);b.writeDouble(y);b.writeDouble(z);b.writeDouble(yieldKt);b.writeDouble(coupling);b.writeDouble(burialDepth);b.writeInt(duration);b.writeFloat(baseIntensity);}
	public static class Handler implements IMessageHandler<NuclearSeismicPacket, IMessage> {

		@Override
		@SideOnly(Side.CLIENT)
		public IMessage onMessage(NuclearSeismicPacket message, MessageContext context) {

			EntityPlayer player = Minecraft.getMinecraft().thePlayer;
			if(player == null) return null;

			double distance = Math.sqrt(
				player.getDistanceSq(message.x, message.y, message.z)
			);

			double audibleRange = Math.max(
				48D,
				Math.cbrt(Math.max(0.001D, message.yieldKt)) * 80D
			);

			/*
			 * Close players hear and feel more.
			 * The effect fades to zero at audibleRange.
			 */
			double distanceFactor = Math.max(
				0D,
				1D - distance / audibleRange
			);

			float localIntensity = (float)(
				message.baseIntensity
					* message.coupling
					* distanceFactor
			);

			if(localIntensity <= 0F) return null;

			long now = System.currentTimeMillis();

			ModEventHandlerClient.seismicTimestamp = now;
			ModEventHandlerClient.seismicDuration = message.duration;
			ModEventHandlerClient.seismicIntensity = Math.max(
				ModEventHandlerClient.seismicIntensity,
				localIntensity
			);

			/*
			 * Only add this separate muffled rumble for underground shots.
			 * Surface detonations already receive the normal Torex sound.
			 */
			if(message.burialDepth > 0.5D) {

				/*
				 * Deeper explosions become quieter, but retain a 20% floor
				 * at point-blank range so very deep shots are not completely silent.
				 */
				double depthAttenuation =
					0.20D
						+ 0.80D
						* Math.exp(-message.burialDepth / 48D);

				float volume = (float)Math.max(
					0D,
					Math.min(
						1D,
						localIntensity * depthAttenuation * 0.75D
					)
				);

				/*
				 * Lower pitch approximates the loss of high-frequency sound
				 * through soil and rock.
				 */
				float pitch = (float)Math.max(
					0.32D,
					Math.min(
						0.65D,
						0.65D - message.burialDepth / 512D
					)
				);

				if(volume > 0.02F) {

					/*
					 * Play at the player's position because distance attenuation
					 * has already been calculated above. Playing it at the
					 * hypocenter would cause Minecraft to attenuate it a second time.
					 */
					MainRegistry.proxy.playSoundClient(
						player.posX,
						player.posY,
						player.posZ,
						"hbm:misc.rumble",
						volume,
						pitch
					);
				}
			}

			return null;
		}
	}
}
