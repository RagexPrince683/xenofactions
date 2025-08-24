package com.hfr.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import cpw.mods.fml.common.registry.GameData;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class MarketData {

	private static final String FILE_NAME = "marketsavedata.json";
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

	// Cache MarketData per-world to avoid repeated disk reads
	private static final Map<String, MarketData> CACHE = new ConcurrentHashMap<>();

	// The actual data structure that is JSON-serializable because of SerializableStack
	public Map<String, List<Offer>> offers = new HashMap<>();

	private volatile boolean dirty = false;

	public MarketData() {}

	/**
	 * Get (and load) MarketData for the given world. Cached per-world save dir path.
	 */
	public static MarketData getData(World world) {
		String key = getMarketFile(world).getAbsolutePath();
		MarketData cached = CACHE.get(key);
		if (cached != null) return cached;

		synchronized (CACHE) {
			if (CACHE.containsKey(key)) return CACHE.get(key);

			File file = getMarketFile(world);
			MarketData data;
			if (!file.exists()) {
				data = new MarketData();
				data.markDirty();
				data.saveToFile(file); // initial save
			} else {
				try (Reader r = new BufferedReader(new FileReader(file))) {
					// MarketData's fields are Gson-friendly (SerializableStack inside Offer)
					data = GSON.fromJson(r, MarketData.class);
					if (data == null) data = new MarketData();
				} catch (Exception e) {
					System.err.println("Failed to read market file, creating fresh MarketData. Error: " + e.getMessage());
					e.printStackTrace();
					data = new MarketData();
				}
			}
			CACHE.put(key, data);
			return data;
		}
	}

	/**
	 * Save to file if dirty. Uses temp file + rename for atomicity.
	 */
	public void saveToFile(File file) {
		if (!dirty) return;

		File tmp = new File(file.getAbsolutePath() + ".tmp");
		try (Writer w = new BufferedWriter(new FileWriter(tmp))) {
			GSON.toJson(this, w);
			w.flush();
			if (file.exists()) {
				if (!file.delete()) {
					System.err.println("Warning: failed to delete old market file: " + file);
				}
			}
			if (!tmp.renameTo(file)) {
				// Attempt move copy if rename fails
				try (InputStream in = new FileInputStream(tmp); OutputStream out = new FileOutputStream(file)) {
					byte[] buf = new byte[8192];
					int len;
					while ((len = in.read(buf)) > 0) out.write(buf, 0, len);
				}
				if (!tmp.delete()) tmp.deleteOnExit();
			}
			dirty = false;
		} catch (Exception e) {
			System.err.println("Error saving MarketData to JSON: " + e.getMessage());
			e.printStackTrace();
		}
	}

	public void markDirty() {
		this.dirty = true;
	}

	// --- Serializable wrapper for ItemStack ---
	public static class SerializableStack {

		public String itemName;    // registry name e.g. "minecraft:stone"
		public int meta;
		public int count;
		public String snbt;        // optional NBT in SNBT string form, or null

		public SerializableStack() {}

		public static SerializableStack fromItemStack(ItemStack stack) {
			if (stack == null) return null;
			SerializableStack s = new SerializableStack();
			s.count = stack.stackSize;
			s.meta = stack.getItemDamage();
			String name = Item.itemRegistry.getNameForObject(stack.getItem());
			s.itemName = name != null ? name : stack.getItem().getUnlocalizedName();
			if (stack.hasTagCompound()) s.snbt = stack.getTagCompound().toString();
			else s.snbt = null;
			return s;
		}


		public ItemStack toItemStack() {
			if (itemName == null) return null;

			// Forge 1.7.10: get the item from the registry by its name
			Item item = GameData.getItemRegistry().getObject(itemName);
			if (item == null) return null;

			ItemStack stack = new ItemStack(item, Math.max(1, count), meta);

			if (snbt != null) {
				try {
					NBTTagCompound tag = (NBTTagCompound) JsonToNBT.func_150315_a(snbt);
					stack.setTagCompound(tag);
				} catch (Exception e) {
					// malformed SNBT -> ignore
				}
			}

			return stack;
		}

	}

	// --- Offer class stored in JSON (uses SerializableStack[]) ---
	public static class Offer {
		public SerializableStack[] stacks; // up to 4 entries used by the command/gui
		public int capacity;
		public ItemStack[] offer = this.getItemStacks();

		// Default no-arg ctor required by Gson
		public Offer() {}

		public Offer(ItemStack[] itemStacks, int capacity) {
			if (itemStacks == null) itemStacks = new ItemStack[0];
			this.stacks = new SerializableStack[itemStacks.length];
			for (int i = 0; i < itemStacks.length; i++) {
				this.stacks[i] = SerializableStack.fromItemStack(itemStacks[i]);
			}
			this.capacity = capacity;
		}

		/** Convert back to live ItemStack objects for GUIs or server logic */
		public ItemStack[] getItemStacks() {
			if (stacks == null) return new ItemStack[0];
			ItemStack[] out = new ItemStack[stacks.length];
			for (int i = 0; i < stacks.length; i++) {
				out[i] = stacks[i] != null ? stacks[i].toItemStack() : null;
			}
			return out;
		}
	}

	// --- NBT-based packet helpers (used when sending offers over network) ---
	public void writeOffersToNBT(NBTTagCompound nbt, String marketName, List<Offer> offerList) {
		if (offerList == null) return;
		nbt.setString("market", marketName);
		nbt.setInteger("offercount", offerList.size());

		for (int index = 0; index < offerList.size(); index++) {
			Offer o = offerList.get(index);
			ItemStack[] items = o.getItemStacks();
			NBTTagList list = new NBTTagList();
			for (int i = 0; i < items.length; i++) {
				ItemStack it = items[i];
				if (it == null) continue;
				NBTTagCompound itemTag = new NBTTagCompound();
				itemTag.setByte("slot", (byte) i);
				it.writeToNBT(itemTag);
				list.appendTag(itemTag);
			}
			nbt.setTag("items" + marketName + index, list);
			nbt.setInteger("count" + marketName + index, o.capacity);
		}
	}

	public void readMarketFromPacket(NBTTagCompound nbt) {
		String name = nbt.getString("market");
		int offerCount = nbt.getInteger("offercount");
		if (name == null || name.isEmpty()) return;

		List<Offer> list = this.offers.get(name);
		if (list == null) {
			list = new ArrayList<>();
		} else {
			list.clear();
		}

		for (int i = 0; i < offerCount; i++) {
			ItemStack[] slots = new ItemStack[4];
			NBTTagList tagList = nbt.getTagList("items" + name + i, 10);
			for (int j = 0; j < tagList.tagCount(); j++) {
				NBTTagCompound itemTag = tagList.getCompoundTagAt(j);
				byte slot = itemTag.getByte("slot");
				if (slot >= 0 && slot < slots.length) {
					slots[slot] = ItemStack.loadItemStackFromNBT(itemTag);
				}
			}
			int capacity = nbt.getInteger("count" + name + i);
			Offer o = new Offer(slots, capacity);
			list.add(o);
		}
		this.offers.put(name, list);
	}

	public static File getMarketFile(World world) {
		File worldDir = world.getSaveHandler().getWorldDirectory();
		return new File(worldDir, FILE_NAME);
	}
}
