package com.hbm.render.entity.mob;

import com.hbm.entity.mob.EntityFRIEND;
import com.hbm.lib.RefStrings;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.entity.RenderBiped;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public class RenderFRIEND extends RenderBiped {
	public RenderFRIEND() {
		super(new ModelBiped(0.0F), 0.5F, 1.0F);
	}

	@Override
	protected ResourceLocation getEntityTexture(EntityLiving entity) {
		return this.getEntityTexture((EntityFRIEND) entity);
	}

	@Override
	protected ResourceLocation getEntityTexture(Entity entity) {
		return this.getEntityTexture((EntityFRIEND) entity);
	}


	protected ResourceLocation getEntityTexture(EntityFRIEND entity) {
		return new ResourceLocation(RefStrings.MODID + ":textures/entity/nicefunguy.png");
	}

	@Override
	public void doRender(EntityLiving entity, double x, double y, double z, float yaw, float partialTicks) {
		GL11.glPushMatrix();
		GL11.glEnable(GL11.GL_BLEND);
		// Blend source alpha with inverse destination alpha (standard transparency)
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		// Set RGBA: alpha = 0.5f → 50% transparent
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 0.5F);

		//GL11.glScalef(0.7F, 1.6F, 0.7F); // skinny + tall
		//y offset to keep it from sinking into the ground
		//GL11.glTranslatef(0.0F, 0.6F, 0.0F);

		// Render normally
		super.doRender(entity, x, y, z, yaw, partialTicks);

		// Reset to full opacity for other renders
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		GL11.glDisable(GL11.GL_BLEND);
		GL11.glPopMatrix();
	}

	@Override
	protected void preRenderCallback(EntityLivingBase entity, float partialTickTime) {
		GL11.glScalef(0.7F, 1.6F, 0.7F);
	}



}
