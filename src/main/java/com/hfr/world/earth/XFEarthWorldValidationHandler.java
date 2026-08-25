package com.hfr.world.earth;
import java.io.File; import com.hfr.config.XFConfig; import com.hfr.main.MainRegistry;
import cpw.mods.fml.common.eventhandler.SubscribeEvent; import net.minecraftforge.event.world.WorldEvent; import net.minecraft.world.World;
public final class XFEarthWorldValidationHandler {
 @SubscribeEvent public void onLoad(WorldEvent.Load event){World w=event.world;if(w==null||w.isRemote||w.provider.dimensionId!=0)return; File root=w.getSaveHandler().getWorldDirectory(); boolean profilePresent=XFEarthProfileLoader.exists(root); boolean earth=XFEarthRegistry.get()!=null&&w.getWorldInfo().getTerrainType()==XFEarthRegistry.get();
  if(!earth){if(profilePresent&&MainRegistry.logger!=null)MainRegistry.logger.warn("[XF EARTH] PROFILE PRESENT BUT WORLD NOT ADOPTED. Set level.dat Data.generatorName=earthmap");return;}
  if(!profilePresent){String m="[XF EARTH] Adopted earthmap world is missing "+XFEarthProfileLoader.FILE_NAME; if(XFConfig.earthRequireProfile)throw new IllegalStateException(m); if(MainRegistry.logger!=null)MainRegistry.logger.error(m);return;}
  if(!new File(root,"region").isDirectory())throw new IllegalStateException("[XF EARTH] Adopted Earth save has no region directory");
  try{XFEarthProfile p=XFEarthProfileLoader.load(root);XFEarthBounds b=p.getBounds();if("PROFILE".equals(XFConfig.earthBoundaryMode)){int m=XFConfig.earthBoundarySafetyMargin;MainRegistry.border=true;XFConfig.earthBoundaryEnabled=true;MainRegistry.borderNegX=b.minimumX+m;MainRegistry.borderPosX=b.maximumX-m;MainRegistry.borderNegZ=b.minimumZ+m;MainRegistry.borderPosZ=b.maximumZ-m;} if(MainRegistry.logger!=null)MainRegistry.logger.info("[XF EARTH] profile='"+p.getProfile()+"' scale="+p.getEffectiveScale()+" bounds="+b.blockString()+" chunks="+b.chunkString()+"; Minecraft population is disabled by this provider.");}
  catch(Exception e){throw new IllegalStateException("[XF EARTH] Profile validation failed",e);}
 }
}
