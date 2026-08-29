package com.hbm.explosion.nuclear;

import java.util.ArrayDeque;
import java.util.HashSet;
import com.hbm.config.BombConfig;
import com.hbm.dim.CelestialBody;
import com.hbm.dim.trait.CBT_Atmosphere;
import com.hbm.util.fauxpointtwelve.BlockPos;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

/** Deterministic classification shared by independent physical and visual factories. */
public final class NuclearBurstResolver {
 public static final double FIREBALL_RADIUS_FACTOR = 0.35D;
 private static final int MAX_RELEASE_SEARCH_NODES = 16384;
 private NuclearBurstResolver() { }
 public static NuclearBurstContext resolve(World world,double x,double y,double z,int legacyRadius) {
  int radius=Math.max(1,legacyRadius), bx=MathHelper.floor_double(x), by=MathHelper.floor_double(y), bz=MathHelper.floor_double(z);
  double yield=BombConfig.ktFromRadius(radius), base=BombConfig.radiusFromKt((float)yield), surface=world.getHeightValue(bx,bz), height=y-surface, fireball=base*FIREBALL_RADIUS_FACTOR;
  double coupling=y-fireball>surface?0D:clamp((surface-(y-fireball))/Math.max(1D,fireball));
  CBT_Atmosphere atmosphere=CelestialBody.getTrait(world,CBT_Atmosphere.class); BurstType type;
  if(CelestialBody.inOrbit(world)||atmosphere==null||atmosphere.getPressure()<0.01D) type=BurstType.VACUUM;
  else if(world.getBlock(bx,by,bz).getMaterial().isLiquid()) type=BurstType.UNDERWATER;
  else if(y<surface) type=BurstType.SUBSURFACE; else if(coupling==0D) type=BurstType.AIR; else type=BurstType.SURFACE;
  double burial=type==BurstType.SUBSURFACE?Math.max(0D,surface-y):0D;
  double predicted=type==BurstType.SUBSURFACE?calculateBreakthrough(world,bx,by,bz,(int)surface,base):(type==BurstType.SURFACE?1D:0D);
  int[] breach=type==BurstType.SUBSURFACE?findOpenRelease(world,bx,by,bz,Math.min(64,Math.max(8,(int)Math.ceil(base*.60D)))):new int[]{bx,(int)surface,bz};
  boolean actual=type!=BurstType.SUBSURFACE||breach!=null, vented=type==BurstType.SUBSURFACE&&actual, contained=type==BurstType.SUBSURFACE&&!actual;
  double release=type==BurstType.SUBSURFACE?(actual?Math.max(.05D,predicted):0D):1D, deform=type==BurstType.SUBSURFACE?predicted:coupling;
  if(breach==null) breach=new int[]{bx,(int)surface,bz};
  NuclearDetonationSpec spec=NuclearDetonationSpec.fromLegacyRadius(radius); spec.burstType=type; spec.burstHeight=height; spec.groundCoupling=coupling; spec.burialDepth=burial; spec.predictedBreakthroughFactor=spec.surfaceBreakthroughFactor=predicted; spec.actualSurfaceBreach=actual; spec.atmosphericReleaseFactor=release; spec.surfaceDeformationFactor=deform; spec.breachConfirmationComplete=false; spec.contained=contained; spec.vented=vented; spec.breachX=breach[0]; spec.breachY=breach[1]; spec.breachZ=breach[2];
  return new NuclearBurstContext(radius,yield,type,surface,height,fireball,coupling,burial,predicted,actual,release,deform,contained,vented,breach[0],breach[1],breach[2],NuclearEffectsSolver.solve(spec));
 }
 /** Bounded air/replaceable-space search. Solids, including a one-block roof, terminate a route. */
 public static int[] findOpenRelease(World world,int x,int y,int z,int radius) {
  ArrayDeque<BlockPos> q=new ArrayDeque<BlockPos>(); HashSet<BlockPos> seen=new HashSet<BlockPos>();
  for(int dx=-1;dx<=1;dx++) for(int dy=-1;dy<=1;dy++) for(int dz=-1;dz<=1;dz++) { BlockPos p=new BlockPos(x+dx,y+dy,z+dz); if(isOpen(world,p.getX(),p.getY(),p.getZ())) { q.add(p); seen.add(p); } }
  while(!q.isEmpty()&&seen.size()<MAX_RELEASE_SEARCH_NODES) { BlockPos p=q.removeFirst(); if(world.canBlockSeeTheSky(p.getX(),p.getY(),p.getZ())) return new int[]{p.getX(),p.getY(),p.getZ()};
   int[][] d={{1,0,0},{-1,0,0},{0,1,0},{0,-1,0},{0,0,1},{0,0,-1}}; for(int[] a:d) { int xx=p.getX()+a[0],yy=p.getY()+a[1],zz=p.getZ()+a[2]; if(yy<0||yy>255||Math.abs(xx-x)>radius||Math.abs(yy-y)>radius||Math.abs(zz-z)>radius) continue; BlockPos n=new BlockPos(xx,yy,zz); if(!seen.contains(n)&&isOpen(world,xx,yy,zz)){seen.add(n);q.addLast(n);} }
  } return null;
 }
 private static boolean isOpen(World w,int x,int y,int z){ Block b=w.getBlock(x,y,z); return b==Blocks.air||b.isReplaceable(w,x,y,z); }
 private static double calculateBreakthrough(World w,int x,int y,int z,int sy,double base){double reach=Math.max(1D,base*.55D),cost=0D;for(int yy=y+1;yy<sy;yy++){Block b=w.getBlock(x,yy,z);if(b==Blocks.air)continue;if(b.getMaterial().isLiquid())cost+=.25D;else cost+=1D+Math.min(8D,Math.max(0D,b.getExplosionResistance(null))/12D);}return clamp((reach-cost)/Math.max(1D,reach*.65D));}
 private static double clamp(double v){return Math.max(0D,Math.min(1D,v));}
}
