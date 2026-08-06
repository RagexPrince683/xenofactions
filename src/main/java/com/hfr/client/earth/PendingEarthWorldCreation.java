package com.hfr.client.earth;
import com.google.gson.*;import com.hfr.config.XFConfig;import com.hfr.world.earth.pack.XFEarthMapInstaller;
public final class PendingEarthWorldCreation {
 public String displayName,folderName,seedText,gameMode,generatorOptions,packId; public boolean hardcore,structures,commands,bonusChest;
 public long parseSeed(){long seed=new java.util.Random().nextLong();if(seedText!=null&&!seedText.isEmpty()){try{long n=Long.parseLong(seedText);if(n!=0L)seed=n;}catch(NumberFormatException e){seed=seedText.hashCode();}}return seed;}
 public static String options(String id){JsonObject o=new JsonObject();o.addProperty("xfEarthFormat",1);o.addProperty("packId",validId(id)?id:XFConfig.earthDefaultPackId);return new Gson().toJson(o);}
 public static String packId(String json){try{JsonObject o=new JsonParser().parse(json).getAsJsonObject();String id=o.get("packId").getAsString();return o.get("xfEarthFormat").getAsInt()==1&&validId(id)?id:XFConfig.earthDefaultPackId;}catch(Exception e){return XFConfig.earthDefaultPackId;}}
 private static boolean validId(String id){return id!=null&&id.matches("[a-z0-9][a-z0-9._-]{2,63}");}
 public XFEarthMapInstaller.Settings settings(){XFEarthMapInstaller.Settings s=new XFEarthMapInstaller.Settings();s.levelName=displayName;s.generatorOptions=options(packId);s.seed=parseSeed();s.gameType="creative".equals(gameMode)?1:"adventure".equals(gameMode)?2:0;s.hardcore=hardcore;s.structures=structures;s.commands=commands;s.bonusChest=bonusChest;return s;}
}
