package com.hfr.xfgui;
import java.util.*;
public class XFCommandDefinition {
 public enum Category { FACTION, MEMBERS, TERRITORY, CITIES, WAR, ALLIANCES, ECONOMY, CHAT, UTILITIES, TDM, ADMIN, HELP }
 public final String id; public final Category category; public final String displayKey; public final String descriptionKey; public final String root; public final String sub; public final String[] aliases; public final String usage; public final XFCommandArgument[] arguments; public final int opLevel; public final String factionRank; public final boolean dangerous; public final String configRequirement;
 public XFCommandDefinition(String id, Category category, String displayKey, String descriptionKey, String root, String sub, String[] aliases, String usage, XFCommandArgument[] arguments, int opLevel, String factionRank, boolean dangerous, String configRequirement){this.id=id;this.category=category;this.displayKey=displayKey;this.descriptionKey=descriptionKey;this.root=root;this.sub=sub;this.aliases=aliases==null?new String[0]:aliases;this.usage=usage;this.arguments=arguments==null?new XFCommandArgument[0]:arguments;this.opLevel=opLevel;this.factionRank=factionRank;this.dangerous=dangerous;this.configRequirement=configRequirement;}
 public String buildCommand(List<String> values){StringBuilder b=new StringBuilder("/").append(root); if(sub!=null&&sub.length()>0)b.append(' ').append(sub); for(String v:values){ if(v!=null&&v.length()>0)b.append(' ').append(v);} return b.toString();}
 public boolean isAdmin(){return opLevel>0 || category==Category.ADMIN;}
}
