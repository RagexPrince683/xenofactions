package com.hfr.schematic;

import java.io.File;
import java.util.*;
import com.hfr.main.MainRegistry;

/** The single server-owned, live native schematic library. */
public final class SchematicLibrary {
    public static final class Entry {
        public final String id,name; public final int width,height,length;
        private final Schematic schematic;
        private Entry(String id,Schematic s){this.id=id;name=s.name;width=s.width;height=s.height;length=s.length;schematic=s;}
    }
    private static final SchematicLibrary INSTANCE=new SchematicLibrary();
    private final Map<String,Entry> entries=new LinkedHashMap<String,Entry>();
    private Map<String,String> snapshot=Collections.emptyMap();
    private File directory; private long nextPoll;
    private SchematicLibrary(){}
    public static SchematicLibrary get(){return INSTANCE;}
    public synchronized void initialize(File forgeConfigDirectory){
        if(directory!=null)return;
        directory=new File(forgeConfigDirectory,"schematics").getAbsoluteFile();
        if(!directory.exists()&&!directory.mkdirs())MainRegistry.logger.error("[XF Builder] Failed to create schematic directory {}",directory);
        MainRegistry.logger.info("[XF Builder] Native schematic directory: {}",directory);
        refresh(true);
    }
    public synchronized boolean refresh(boolean force){
        if(directory==null)return false;
        long now=System.currentTimeMillis();if(!force&&now<nextPoll)return false;nextPoll=now+1500L;
        File[] files=directory.listFiles();
        if(files==null){MainRegistry.logger.error("[XF Builder] Failed to scan schematic directory {}",directory);return false;}
        Arrays.sort(files,new Comparator<File>(){public int compare(File a,File b){return a.getName().compareToIgnoreCase(b.getName());}});
        Map<String,String> current=new LinkedHashMap<String,String>();Map<String,File> acceptedFiles=new LinkedHashMap<String,File>();
        for(File file:files)if(file.isFile()&&file.getName().toLowerCase(Locale.ROOT).endsWith(".schematic")){
            String id=key(file);String stamp=file.getName()+":"+file.length()+":"+file.lastModified();
            current.put(id,stamp);acceptedFiles.put(id,file);
        }
        if(current.equals(snapshot))return false;
        Map<String,Entry> loaded=new LinkedHashMap<String,Entry>();
        for(Map.Entry<String,File> item:acceptedFiles.entrySet()){
            Entry old=entries.get(item.getKey());
            if(old!=null&&current.get(item.getKey()).equals(snapshot.get(item.getKey())))loaded.put(item.getKey(),old);
            else {SchematicLoader.LoadResult result=SchematicLoader.loadFromFile(item.getValue());if(result.succeeded()){loaded.put(item.getKey(),new Entry(item.getKey(),result.schematic));MainRegistry.logger.info("[XF Builder] Loaded schematic {}",item.getValue().getName());}else MainRegistry.logger.error("[XF Builder] Rejected schematic {} in {}: {}",item.getValue().getName(),directory,result.error);}
        }
        boolean changed=!sameEntries(entries,loaded);entries.clear();entries.putAll(loaded);snapshot=current;
        MainRegistry.schems.clear();for(Entry e:entries.values())MainRegistry.schems.add(e.schematic);
        return changed;
    }
    private boolean sameEntries(Map<String,Entry> a,Map<String,Entry> b){if(!a.keySet().equals(b.keySet()))return false;for(String k:a.keySet())if(a.get(k)!=b.get(k))return false;return true;}
    private String key(File file){return file.getName().toLowerCase(Locale.ROOT);}
    public synchronized List<Entry> list(){return new ArrayList<Entry>(entries.values());}
    public synchronized Schematic resolve(String id){Entry e=entries.get(id);return e==null?null:e.schematic;}
}
