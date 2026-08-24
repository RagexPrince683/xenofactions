package com.hfr.schematic.client;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import com.hfr.schematic.Schematic;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;

/** Client-only reflection boundary for classic Schematica and Schematica Plus. */
@SideOnly(Side.CLIENT)
public final class SchematicaCompat {
    private static final String[] ACTIVE={"com.github.lunatrius.schematica.proxy.ClientProxy","com.github.lunatrius.schematica.client.world.SchematicWorld","com.github.lunatrius.schematica.world.SchematicWorld"};
    private SchematicaCompat(){}
    public static boolean installed(){return Loader.isModLoaded("Schematica");}
    public static boolean plusLitematicAvailable(){return installed()&&present("com.github.lunatrius.schematica.world.schematic.SchematicLitematica")&&present("com.github.lunatrius.schematica.world.schematic.SchematicFormat");}
    public static Schematic importActive(){
        if(!installed())return null;
        for(String path:ACTIVE)try{Class<?> c=Class.forName(path);for(Field f:c.getDeclaredFields())if(java.lang.reflect.Modifier.isStatic(f.getModifiers())){f.setAccessible(true);Object candidate=f.get(null);Schematic s=normalize(candidate,"schematica-active");if(s!=null)return s;}}catch(Throwable ignored){}
        return null;
    }
    /** Delegates .litematic decoding to Plus; Xenofactions deliberately has no parser. */
    public static Schematic importLitematic(File file){
        if(!plusLitematicAvailable())return null;
        try{Class<?> format=Class.forName("com.github.lunatrius.schematica.world.schematic.SchematicFormat");for(Method m:format.getMethods())if(java.lang.reflect.Modifier.isStatic(m.getModifiers())&&m.getName().toLowerCase().contains("read")){Class<?>[] p=m.getParameterTypes();if(p.length>0&&p[0]==File.class){Object[] args=new Object[p.length];args[0]=file;for(int i=1;i<p.length;i++)args[i]=p[i]==boolean.class?Boolean.FALSE:null;Schematic s=normalize(m.invoke(null,args),"litematic");if(s!=null)return s;}}}catch(Throwable ignored){}return null;
    }
    private static Schematic normalize(Object o,String format)throws Exception{
        if(o==null)return null; Object inner=call(o,"getSchematic");if(inner!=null&&inner!=o)o=inner;
        int w=number(o,"getWidth"),h=number(o,"getHeight"),l=number(o,"getLength");if(w<=0||h<=0||l<=0)return null;
        Schematic s=new Schematic(w,h,l);s.sourceFormat=format;s.name="Active schematic";
        Method block=find(o.getClass(),"getBlock",int.class,int.class,int.class),meta=find(o.getClass(),"getBlockMetadata",int.class,int.class,int.class);if(block==null)return null;
        for(int x=0;x<w;x++)for(int y=0;y<h;y++)for(int z=0;z<l;z++){Object b=block.invoke(o,x,y,z);if(!(b instanceof Block)||!s.setBlock(x,y,z,(Block)b,meta==null?0:((Number)meta.invoke(o,x,y,z)).intValue()))return null;}return s;
    }
    private static Object call(Object o,String n){try{Method m=o.getClass().getMethod(n);return m.invoke(o);}catch(Throwable e){return null;}}
    private static int number(Object o,String n){Object v=call(o,n);return v instanceof Number?((Number)v).intValue():-1;}
    private static Method find(Class<?> c,String n,Class<?>...p){try{return c.getMethod(n,p);}catch(Exception e){return null;}}
    private static boolean present(String n){try{Class.forName(n,false,SchematicaCompat.class.getClassLoader());return true;}catch(Throwable e){return false;}}
}
