package com.hfr.schematic;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.common.registry.FMLControlledNamespacedRegistry;
import cpw.mods.fml.common.registry.GameData;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.init.Blocks;

public class SchematicRenderer {

    private static final FMLControlledNamespacedRegistry<Block> BLOCK_REGISTRY = GameData.getBlockRegistry();
    protected static final RenderBlocks field_94145_f = new RenderBlocks();
	
	public static void render(Schematic schem, float f0, double x, double y, double z, double cap) {
		render(schem,f0,x,y,z,cap,0,false);
	}
	public static void render(Schematic schem, float f0, double x, double y, double z, double cap, int rotation, boolean mirror) {
		
		GL11.glPushMatrix();
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GL11.glDisable(GL11.GL_ALPHA_TEST);
		GL11.glDepthMask(false);GL11.glColor4f(.45F,.85F,1F,.42F);
		int stride=Math.max(1,(schem.size()+32767)/32768),cell=0;
		
		for(int dx = 0; dx < schem.width; dx++) {
			for(int dy = 0; dy < schem.height; dy++) {
				for(int dz = 0; dz < schem.length; dz++) {

					if(cell++%stride!=0)continue;int[] pos=SchematicTransform.position(schem,dx,dy,dz,rotation,mirror);
					double cx = x + pos[0];
					double cy = y + dy;
					double cz = z + pos[2];
					
					if(cap>0&&Math.sqrt(Math.pow(cx, 2) + Math.pow(cy, 2) + Math.pow(cz, 2)) > cap)
						continue;
		
					Block b = schem.resolveBlock(dx,dy,dz);
					if(b == Blocks.air)
						continue;
					
					int meta = SchematicTransform.metadata(b,schem.getMetadata(dx,dy,dz),rotation,mirror);
					
					// 1.7.10 renderBlockAsItem emits inventory geometry around
					// (-.5,-.5,-.5)..(.5,.5,.5).  Move that documented centre to
					// the centre of this world block cube; no TESR/item offset applies.
					GL11.glTranslatef(pos[0]+.5F, pos[1]+.5F, pos[2]+.5F);
					GL11.glPushMatrix();
					field_94145_f.renderBlockAsItem(b, meta, 1);
					GL11.glPopMatrix();
					GL11.glTranslatef(-pos[0]-.5F, -pos[1]-.5F, -pos[2]-.5F);
				}
			}
		}
		// A bright origin pillar and transformed footprint make placement unambiguous.
		int w=SchematicTransform.width(schem,rotation),l=SchematicTransform.length(schem,rotation);
		GL11.glDisable(GL11.GL_TEXTURE_2D);GL11.glLineWidth(2F);GL11.glBegin(GL11.GL_LINES);
		GL11.glColor4f(1F,.35F,.15F,.9F);GL11.glVertex3d(0,0,0);GL11.glVertex3d(0,Math.max(2,schem.height),0);
		GL11.glColor4f(.25F,.9F,1F,.8F);line(0,0,0,w,0,0);line(w,0,0,w,0,l);line(w,0,l,0,0,l);line(0,0,l,0,0,0);line(0,schem.height,0,w,schem.height,0);line(w,schem.height,0,w,schem.height,l);line(w,schem.height,l,0,schem.height,l);line(0,schem.height,l,0,schem.height,0);line(0,0,0,0,schem.height,0);line(w,0,0,w,schem.height,0);line(w,0,l,w,schem.height,l);line(0,0,l,0,schem.height,l);GL11.glEnd();GL11.glEnable(GL11.GL_TEXTURE_2D);
		
		GL11.glColor4f(1,1,1,1);GL11.glDepthMask(true);GL11.glDisable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_ALPHA_TEST);
		
		GL11.glPopMatrix();
	}
	private static void line(double ax,double ay,double az,double bx,double by,double bz){GL11.glVertex3d(ax,ay,az);GL11.glVertex3d(bx,by,bz);}
}
