package com.hfr.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTSizeTracker;
import net.minecraft.world.World;

import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MarketData {

	private static final String FILE_NAME = "marketsavedata.json";
	public HashMap<String, List<Offer>> offers = new HashMap<String, List<Offer>>();
	private boolean dirty = false; // A flag to track if data was modified

	// Gson instance for JSON serialization/deserialization
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();



	public static MarketData getData(World world) {
		File file = getMarketFile(world);

		if (!file.exists()) {
			// Create a new MarketData instance if the file does not exist
			MarketData data = new MarketData();
			data.saveToFile(file);
			return data;
		}

		Reader reader = null;
		try {
			reader = new FileReader(file);
			Type type = new TypeToken<HashMap<String, List<Offer>>>() {}.getType();
			MarketData data = new MarketData();
			data.offers = GSON.fromJson(reader, type);

			if (data.offers == null) {
				data.offers = new HashMap<String, List<Offer>>();
			}

			return data;
		} catch (JsonParseException e) {
			// Handle JSON parsing errors
			System.err.println("Error parsing MarketData JSON: " + e.getMessage());
			e.printStackTrace();
			return new MarketData();
		} catch (IOException e) {
			// Handle file I/O errors
			System.err.println("Error reading MarketData JSON: " + e.getMessage());
			e.printStackTrace();
			return new MarketData();
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public void saveToFile(File file) {
		if (!dirty) return; // Only save if the data is marked dirty

		Writer writer = null;
		try {
			writer = new FileWriter(file);
			GSON.toJson(offers, writer);
			dirty = false; // Reset the dirty flag after saving
		} catch (IOException e) {
			System.err.println("Error saving MarketData to JSON: " + e.getMessage());
			e.printStackTrace();
		} finally {
			if (writer != null) {
				try {
					writer.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public void markDirty() {
		this.dirty = true;
	}

	public static class Offer {


		public ItemStack[] offer; // Store ItemStack[] directly
		public int capacity;

		public Offer(ItemStack[] offer, int capacity) {
			this.offer = offer;
			this.capacity = capacity;
		}

		public ItemStack[] getItemStacks() {
			ItemStack[] itemStacks = new ItemStack[offer.length];
			for (int i = 0; i < offer.length; i++) {
				if (offer[i] != null) {
					itemStacks[i] = deserializeItemStack(String.valueOf(offer[i]));
				} else {
					itemStacks[i] = null;
				}
			}
			return itemStacks;
		}

		private static String serializeItemStack(ItemStack itemStack) {
			NBTTagCompound nbt = new NBTTagCompound();
			itemStack.writeToNBT(nbt);
			return nbt.toString();
		}

		private static ItemStack deserializeItemStack(String nbtString) {
			try {
				// Convert the string back to a byte array
				byte[] nbtData = nbtString.getBytes("UTF-8");

				// Use an NBTSizeTracker to deserialize the data
				NBTTagCompound nbt = CompressedStreamTools.func_152457_a(nbtData, new NBTSizeTracker(2097152L)); // 2 MB size limit
				return ItemStack.loadItemStackFromNBT(nbt);
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}
	}

	private static String serializeItemStackArray(ItemStack[] items) {
		NBTTagList nbtList = new NBTTagList();
		for (ItemStack item : items) {
			if (item != null) {
				NBTTagCompound nbt = new NBTTagCompound();
				item.writeToNBT(nbt);
				nbtList.appendTag(nbt);
			}
		}
		NBTTagCompound wrapper = new NBTTagCompound();
		wrapper.setTag("Items", nbtList);
		return wrapper.toString();
	}
	private static ItemStack[] deserializeItemStackArray(String serialized) {
		try {
			NBTTagCompound wrapper = CompressedStreamTools.func_152457_a(serialized.getBytes("UTF-8"), new NBTSizeTracker(2097152L));
			NBTTagList nbtList = wrapper.getTagList("Items", 10);
			ItemStack[] items = new ItemStack[nbtList.tagCount()];
			for (int i = 0; i < nbtList.tagCount(); i++) {
				items[i] = ItemStack.loadItemStackFromNBT(nbtList.getCompoundTagAt(i));
			}
			return items;
		} catch (Exception e) {
			e.printStackTrace();
			return new ItemStack[0];
		}
	}


	public void writeOffers(NBTTagCompound nbt, String name, List<Offer> offers) {
		for (int index = 0; index < offers.size(); index++) {
			NBTTagList list = new NBTTagList();
			Offer offer = offers.get(index);
			ItemStack[] items = offer.getItemStacks();

			for (int i = 0; i < items.length; i++) {
				if (items[i] != null) {
					NBTTagCompound nbt1 = new NBTTagCompound();
					nbt1.setByte("slot" + index, (byte) i);
					items[i].writeToNBT(nbt1);
					list.appendTag(nbt1);
				}
			}

			nbt.setTag("items" + name + index, list);
			nbt.setInteger("count" + name + index, offer.capacity);
		}
	}

	public void writeMarketFromName(NBTTagCompound nbt, String name) {
		List<Offer> market = this.offers.get(name);

		if (market == null)
			return;

		nbt.setString("market", name);
		nbt.setInteger("offercount", market.size());

		writeOffers(nbt, name, market);
	}

	public void readMarketFromPacket(NBTTagCompound nbt) {
		String name = nbt.getString("market");
		int offerCount = nbt.getInteger("offercount");

		for (int off = 0; off < offerCount; off++) {
			readOffers(nbt, name, off);
		}
	}

	public void readOffers(NBTTagCompound nbt, String name, int index) {
		ItemStack[] slots = new ItemStack[4];
		NBTTagList list = nbt.getTagList("items" + name + index, 10);

		for (int j = 0; j < list.tagCount(); j++) {
			NBTTagCompound nbt1 = list.getCompoundTagAt(j);
			byte b0 = nbt1.getByte("slot" + index);
			if (b0 >= 0 && b0 < slots.length) {
				slots[b0] = ItemStack.loadItemStackFromNBT(nbt1);
			}
		}

		int capacity = nbt.getInteger("items" + name + index);

		List<Offer> offers = this.offers.get(name);

		if (offers == null) {
			offers = new ArrayList<Offer>();
		}

		offers.add(new Offer(slots, capacity));
		this.offers.put(name, offers);
	}

	public static File getMarketFile(World world) {
		File worldDir = world.getSaveHandler().getWorldDirectory();
		return new File(worldDir, FILE_NAME);
	}
}