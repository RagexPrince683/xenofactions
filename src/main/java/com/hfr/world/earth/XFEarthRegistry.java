package com.hfr.world.earth;
import com.hfr.config.XFConfig;
public final class XFEarthRegistry { private static XFEarthWorldType type; private XFEarthRegistry(){} public static synchronized void register(){if(XFConfig.enableEarthWorldType&&type==null)type=new XFEarthWorldType();} public static XFEarthWorldType get(){return type;} public static boolean isRegistered(){return type!=null;} }
