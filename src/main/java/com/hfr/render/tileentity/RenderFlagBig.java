package com.hfr.render.tileentity;

import org.lwjgl.opengl.GL11;

import com.hfr.client.flag.FactionFlagTextureManager;
import com.hfr.clowder.ClowderFlag;
import com.hfr.main.ResourceManager;
import com.hfr.tileentity.clowder.TileEntityFlagBig;

import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;

public class RenderFlagBig extends TileEntitySpecialRenderer {

	@Override
	public void renderTileEntityAt(TileEntity te, double x, double y, double z, float interp) {
		
        TileEntityFlagBig flagpole = (TileEntityFlagBig)te;
        bindTexture(FlagBeamRenderer.BEACON_BEAM);
        FlagBeamRenderer.render(te, x, y, z, interp, flagpole.color);

        GL11.glPushMatrix();
        GL11.glTranslated(x + 0.5D, y, z + 0.5D);
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_CULL_FACE);
		
		switch(te.getBlockMetadata())
		{
		case 2:
			GL11.glRotatef(90, 0F, 1F, 0F); break;
		case 4:
			GL11.glRotatef(180, 0F, 1F, 0F); break;
		case 3:
			GL11.glRotatef(270, 0F, 1F, 0F); break;
		case 5:
			GL11.glRotatef(0, 0F, 1F, 0F); break;
		}
        
        bindTexture(ResourceManager.flag_tex);
        
        GL11.glShadeModel(GL11.GL_SMOOTH);
        ResourceManager.flag_big.renderOnly("Pole");
        GL11.glShadeModel(GL11.GL_FLAT);

        GL11.glDisable(GL11.GL_CULL_FACE);
        
        ClowderFlag flag = flagpole.flag;
        int color = flagpole.color;
        
        if(flag == ClowderFlag.NONE || flag == null) {
        	flag = ClowderFlag.TRICOLOR;
        }

	    int r = ((color & 0xFF0000) >> 16) / 2;
	    int g = ((color & 0xFF00) >> 8) / 2;
	    int b = (color & 0xFF) / 2;

	    if(!flagpole.isClaimed)
	    	GL11.glTranslatef(0, -8F, 0);

        GL11.glEnable(GL11.GL_BLEND);
        OpenGlHelper.glBlendFunc(770, 771, 1, 0);
        GL11.glDisable(GL11.GL_ALPHA_TEST);
        
        boolean hasCustomFlag = flagpole.customFlagHash != null && flagpole.customFlagHash.length() > 0;
        if(hasCustomFlag)
            FactionFlagTextureManager.handleMetadata(flagpole.ownerName, flagpole.customFlagHash);
        bindTexture(hasCustomFlag ? FactionFlagTextureManager.getFlagTexture(flagpole.ownerName) : flag.getFlag());
        if(hasCustomFlag)
            GL11.glColor3b((byte)127, (byte)127, (byte)127);
        else
            GL11.glColor3b((byte)r, (byte)g, (byte)b);
        ResourceManager.flag_big.renderOnly("Flag");

        if(!hasCustomFlag) {
            bindTexture(flag.getFlagOverlay());
            GL11.glColor3b((byte)127, (byte)127, (byte)127);
            ResourceManager.flag_big.renderOnly("Flag");
        }
	    
        GL11.glEnable(GL11.GL_ALPHA_TEST);
        GL11.glDisable(GL11.GL_BLEND);

        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glPopMatrix();
	}
}
