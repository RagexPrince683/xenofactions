package com.hfr.schematic;

import java.util.HashSet;
import java.util.Set;
import net.minecraft.block.Block;

/** Shared preview/construction transform. Rotation is clockwise in 90 degree steps. */
public final class SchematicTransform {
    private static final Set<String> WARNED=new HashSet<String>();
    private SchematicTransform(){}
    public static int width(Schematic s,int rotation){return (rotation&1)==0?s.width:s.length;}
    public static int length(Schematic s,int rotation){return (rotation&1)==0?s.length:s.width;}
    public static int[] position(Schematic s,int x,int y,int z,int rotation,boolean mirror){
        int mx=mirror?s.width-1-x:x;
        switch(rotation&3){case 1:return new int[]{s.length-1-z,y,mx};case 2:return new int[]{s.width-1-mx,y,s.length-1-z};case 3:return new int[]{z,y,s.width-1-mx};default:return new int[]{mx,y,z};}
    }
    /** The single local-cell to authoritative world-block conversion used by jobs. */
    public static int[] worldPosition(Schematic s,int x,int y,int z,int rotation,boolean mirror,int originX,int originY,int originZ){
        int[] p=position(s,x,y,z,rotation,mirror);return new int[]{originX+p[0],originY+p[1],originZ+p[2]};
    }
    public static int metadata(Block block,int meta,int rotation,boolean mirror){
        if(block==null)return meta; String n=String.valueOf(Block.blockRegistry.getNameForObject(block));
        int m=meta&15, flags=m&12, facing=m&3;
        // Rails use a distinct 0..9 shape table.
        if(n.contains("rail"))return rail(m,rotation,mirror);
        // Torch/button/lever/ladder/sign/door/stairs/trapdoor/bed horizontal encodings.
        if(n.contains("stairs"))return flags|rotateStair(facing,rotation,mirror);
        if(n.contains("torch")||n.contains("button")||n.contains("lever"))return (m&8)|rotateSide(m&7,rotation,mirror);
        if(n.contains("ladder")||n.contains("wall_sign"))return flags|rotateSide(m&7,rotation,mirror);
        if(n.contains("standing_sign")){if(mirror)m=(16-m)&15;return(m+(rotation&3)*4)&15;}
        if(n.contains("door")||n.contains("trapdoor")||n.contains("bed"))return flags|rotateCardinal(facing,rotation,mirror);
        if((rotation!=0||mirror)&&!n.startsWith("minecraft:")&&WARNED.add(n))System.out.println("[Xenofactions] Builder left unsupported modded metadata unchanged: "+n);
        return m;
    }
    private static int rotateCardinal(int f,int r,boolean mirror){if(mirror)f=(4-f)&3;return(f+r)&3;}
    private static int rotateStair(int f,int r,boolean mirror){int[] to={1,3,0,2},from={2,0,3,1};int c=to[f&3];if(mirror)c=(4-c)&3;return from[(c+r)&3];}
    private static int rotateSide(int f,int r,boolean mirror){if(f<2)return f;int[] to={0,0,3,1,2,0};int c=to[f];if(mirror)c=(4-c)&3;int[] from={5,3,4,2};return from[(c+r)&3];}
    private static int rail(int m,int r,boolean mirror){if(m>9)return m;for(int i=0;i<(r&3);i++){if(m==0)m=1;else if(m==1)m=0;else if(m>=2&&m<=5)m=2+((m-2+1)&3);else if(m>=6)m=6+((m-6+1)&3);}if(mirror){if(m==2)m=3;else if(m==3)m=2;else if(m==6)m=9;else if(m==9)m=6;else if(m==7)m=8;else if(m==8)m=7;}return m;}
}
