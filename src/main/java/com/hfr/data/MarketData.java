package com.hfr.data;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.hfr.util.XFLog;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.*;

/** Server-owned catalog. Blocks hold IDs, never mutable display-name identities. */
public final class MarketData {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = Paths.get("config", "marketdata.json");
    private static State state = new State();
    private static Map<String, String> names = new HashMap<String, String>();
    private static Map<String, String> exactNames = new HashMap<String, String>();
    private static boolean writable;
    private static long revision;

    private static final class State {
        int schemaVersion = 2;
        Map<String, Shop> shops = new LinkedHashMap<String, Shop>();
        // Preserve aliases after rename/deletion so unloaded blocks never bind to a replacement.
        Map<String, String> legacyNames = new LinkedHashMap<String, String>();
        State() { }
        State(State other) { shops.putAll(other.shops); legacyNames.putAll(other.legacyNames); }
    }
    public static final class Shop {
        public String id, displayName;
        public boolean enabled = true, visibleInFactionTerminal = false, adminOnly = false;
        public String category = "";
        public int sortOrder;
        private List<ItemEntry[]> offers = new ArrayList<ItemEntry[]>();
        private Shop() { }
        private Shop(Shop other) {
            id = other.id; displayName = other.displayName; enabled = other.enabled;
            visibleInFactionTerminal = other.visibleInFactionTerminal; adminOnly = other.adminOnly;
            category = other.category; sortOrder = other.sortOrder;
            offers = new ArrayList<ItemEntry[]>(other.offers);
        }
        public boolean marketEligible() { return enabled && visibleInFactionTerminal && !adminOnly; }
        public int offerCount() { return offers.size(); }
    }
    public static long revision() { return revision; }
    public static Shop get(String id) { return state.shops.get(id); }
    public static Shop resolve(String reference) {
        Shop exact = get(reference);
        if (exact != null) return exact;
        exact = get(exactNames.get(reference));
        if (exact != null) return exact;
        String id = names.get(reference == null ? "" : reference.toLowerCase(Locale.ROOT));
        if (id == null) id = state.legacyNames.get(reference);
        return get(id);
    }
    public static String legacyId(String name) { return state.legacyNames.get(name); }
    public static List<Shop> list() {
        List<Shop> result = new ArrayList<Shop>(state.shops.values());
        Collections.sort(result, new Comparator<Shop>() {
            public int compare(Shop a, Shop b) {
                int order = Integer.compare(a.sortOrder, b.sortOrder);
                if (order == 0) order = a.displayName.compareToIgnoreCase(b.displayName);
                return order == 0 ? a.id.compareTo(b.id) : order;
            }
        });
        return result;
    }
    private static void index() {
        names.clear(); exactNames.clear(); Set<String> ambiguous = new HashSet<String>();
        for (Shop shop : state.shops.values()) {
            exactNames.put(shop.displayName, shop.id); String key = shop.displayName.toLowerCase(Locale.ROOT);
            if (names.containsKey(key)) { names.remove(key); ambiguous.add(key); }
            else if (!ambiguous.contains(key)) names.put(key, shop.id);
        }
    }
    private static String validName(String name) {
        String value = name == null ? "" : name.trim();
        if (value.isEmpty() || value.length() > 80 || value.matches(".*[\\p{Cntrl}].*"))
            throw new IllegalArgumentException("Shop names must contain 1-80 printable characters.");
        return value;
    }
    private static void checkName(String name, String ownId, State catalog) {
        for (Shop shop : catalog.shops.values())
            if (!shop.id.equals(ownId) && (shop.displayName.equalsIgnoreCase(name) || shop.id.equalsIgnoreCase(name)))
                throw new IllegalArgumentException("That shop name or ID is already in use.");
    }
    private static void commit(State next) {
        if (!writable) throw new IllegalStateException("Market catalog failed to load; repair marketdata.json and restart.");
        try {
            Files.createDirectories(FILE.getParent());
            Path temporary = FILE.resolveSibling("marketdata.json.tmp");
            try (Writer writer = Files.newBufferedWriter(temporary, StandardCharsets.UTF_8)) { GSON.toJson(next, writer); }
            try { Files.move(temporary, FILE, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE); }
            catch (AtomicMoveNotSupportedException e) { Files.move(temporary, FILE, StandardCopyOption.REPLACE_EXISTING); }
        } catch (IOException e) { throw new IllegalStateException("Could not save market catalog.", e); }
        state = next; index(); revision++;
    }
    public static void loadMarketData() {
        state = new State(); names.clear(); exactNames.clear(); writable = false; revision++;
        if (!Files.exists(FILE)) { writable = true; return; }
        try {
            JsonObject root;
            try (Reader reader = Files.newBufferedReader(FILE, StandardCharsets.UTF_8)) {
                root = new JsonParser().parse(reader).getAsJsonObject();
            }
            State loaded;
            boolean migration = !(root.has("schemaVersion") && root.get("schemaVersion").isJsonPrimitive());
            if (migration) {
                loaded = new State();
                Map<String, List<ItemEntry[]>> old = GSON.fromJson(root,
                    new TypeToken<LinkedHashMap<String, List<ItemEntry[]>>>() { }.getType());
                for (Map.Entry<String, List<ItemEntry[]>> entry : old.entrySet()) {
                    Shop shop = new Shop(); shop.displayName = entry.getKey();
                    shop.id = UUID.nameUUIDFromBytes(("xshop:" + entry.getKey()).getBytes(StandardCharsets.UTF_8)).toString();
                    if (entry.getValue() != null) shop.offers = entry.getValue();
                    loaded.shops.put(shop.id, shop); loaded.legacyNames.put(entry.getKey(), shop.id);
                }
            } else {
                if (root.get("schemaVersion").getAsInt() != 2) throw new IOException("Unsupported XShop schema version");
                loaded = GSON.fromJson(root, State.class);
            }
            if (loaded.shops == null || loaded.legacyNames == null) throw new IOException("Missing shop registry");
            for (Map.Entry<String, Shop> entry : loaded.shops.entrySet()) {
                Shop shop = entry.getValue();
                if (shop == null || !entry.getKey().equals(shop.id) || shop.displayName == null || shop.offers == null)
                    throw new IOException("Invalid shop definition");
                UUID.fromString(shop.id);
            }
            if (migration) {
                Path backup = FILE.resolveSibling("marketdata.json.legacy.bak");
                if (!Files.exists(backup)) Files.copy(FILE, backup);
                writable = true; commit(loaded);
            } else { state = loaded; index(); writable = true; }
        } catch (Exception e) { writable = false; XFLog.error("XShop catalog load failed; original file preserved and edits disabled", e); }
    }
    public static Shop create(String name) {
        name = validName(name); checkName(name, "", state);
        Shop shop = new Shop(); shop.id = UUID.randomUUID().toString(); shop.displayName = name;
        State next = new State(state); next.shops.put(shop.id, shop); commit(next); return shop;
    }
    public static void delete(String id) {
        require(id); State next = new State(state); next.shops.remove(id); commit(next);
    }
    private static Shop require(String id) {
        Shop shop = get(id); if (shop == null) throw new IllegalArgumentException("Unknown shop."); return shop;
    }
    public static void configure(String id, String name, boolean enabled, boolean visible, boolean adminOnly) {
        Shop shop = new Shop(require(id)); name = validName(name); checkName(name, id, state);
        shop.displayName = name; shop.enabled = enabled; shop.visibleInFactionTerminal = visible; shop.adminOnly = adminOnly;
        State next = new State(state); next.shops.put(id, shop); commit(next);
    }
    public static void addOffer(String id, ItemStack[] items) {
        if (items.length != 4 || items[0] == null || items[1] == null) throw new IllegalArgumentException("Hotbar slots 1 and 2 must contain the sold item and currency.");
        Shop shop = new Shop(require(id)); ItemEntry[] entries = new ItemEntry[4];
        for (int i = 0; i < 4; i++) if (items[i] != null) entries[i] = new ItemEntry(items[i]);
        shop.offers.add(entries); State next = new State(state); next.shops.put(id, shop); commit(next);
    }
    public static void removeOffer(String id, int index) {
        Shop shop = new Shop(require(id));
        if (index < 0 || index >= shop.offers.size()) throw new IllegalArgumentException("Offer index is out of range.");
        shop.offers.remove(index); State next = new State(state); next.shops.put(id, shop); commit(next);
    }
    public static ItemStack[] offer(String id, int index) {
        Shop shop = get(id);
        if (shop == null || index < 0 || index >= shop.offers.size()) return null;
        ItemEntry[] entries = shop.offers.get(index); ItemStack[] result = new ItemStack[4];
        if (entries == null || entries.length < 2 || entries.length > 4) return null;
        for (int i = 0; i < entries.length; i++)
            if (entries[i] != null && (result[i] = entries[i].toItemStack()) == null) return null;
        return result[0] == null || result[1] == null ? null : result;
    }
    public static NBTTagCompound offersToNBT(List<ItemStack[]> offers) {
        NBTTagCompound root = new NBTTagCompound(); NBTTagList list = new NBTTagList();
        for (ItemStack[] offer : offers) {
            NBTTagCompound row = new NBTTagCompound(); NBTTagList items = new NBTTagList();
            for (ItemStack stack : offer) { NBTTagCompound tag = new NBTTagCompound(); if (stack != null) stack.writeToNBT(tag); items.appendTag(tag); }
            row.setTag("items", items); list.appendTag(row);
        }
        root.setTag("offers", list); return root;
    }
    public static List<ItemStack[]> offersFromNBT(NBTTagCompound root) {
        List<ItemStack[]> result = new ArrayList<ItemStack[]>(); NBTTagList rows = root.getTagList("offers", 10);
        for (int i = 0; i < rows.tagCount(); i++) {
            NBTTagList items = rows.getCompoundTagAt(i).getTagList("items", 10); ItemStack[] row = new ItemStack[4];
            for (int j = 0; j < Math.min(4, items.tagCount()); j++) row[j] = ItemStack.loadItemStackFromNBT(items.getCompoundTagAt(j));
            result.add(row);
        }
        return result;
    }
    private static final class ItemEntry {
        String itemName; int count, metadata; String nbtData;
        ItemEntry(ItemStack stack) {
            itemName = Item.itemRegistry.getNameForObject(stack.getItem()); count = stack.stackSize; metadata = stack.getItemDamage();
            nbtData = stack.hasTagCompound() ? stack.getTagCompound().toString() : null;
        }
        ItemStack toItemStack() {
            Item item = (Item)Item.itemRegistry.getObject(itemName);
            if (item == null || count <= 0 || count > item.getItemStackLimit()) return null;
            ItemStack stack = new ItemStack(item, count, metadata);
            if (nbtData != null) try { stack.setTagCompound((NBTTagCompound)JsonToNBT.func_150315_a(nbtData)); }
                catch (Exception e) { return null; }
            return stack;
        }
    }
}
