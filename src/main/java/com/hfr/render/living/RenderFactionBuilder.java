package com.hfr.render.living;

import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.entity.RenderBiped;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;

/** Reuses the vanilla human model without leaking client classes into common Builder code. */
public final class RenderFactionBuilder extends RenderBiped {
	private static final ResourceLocation TEXTURE=new ResourceLocation("textures/entity/steve.png");
	public RenderFactionBuilder(){super(new ModelBiped(),.5F);}
	@Override protected ResourceLocation getEntityTexture(Entity entity){return TEXTURE;}
}
