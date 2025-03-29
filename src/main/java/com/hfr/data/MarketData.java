package com.hfr.data;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTTagCompound;

import java.io.*;
import java.lang.reflect.Type;
import java.util.*;

public class MarketData {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final File SAVE_FILE = new File("config/marketdata.json");

	public static final Map<String, List<ItemStack[]>> offers = new HashMap<String, List<ItemStack[]>>();

	public static void saveMarketData() {
		FileWriter writer = null;
		try {
			//BufferedWriter writer = new BufferedWriter(new FileWriter(SAVE_FILE));
			writer = new FileWriter(SAVE_FILE);
			GSON.toJson(offers, writer);
			writer.close();
		} catch (Exception e) {
			System.err.println("Failed to save market data: " + e.getMessage());
		}
		//finally {
		//	if (writer != null) {
		//		try {
		//			writer.close();
		//		} catch (Exception e) {
		//			System.err.println("Failed to close FileWriter: " + e.getMessage());
		//		}
		//	}
		//}
	}

	public static void loadMarketData() {
		if (!SAVE_FILE.exists()) {
			return; // No file to load
		}

		//FileReader reader = null;
		try {
			BufferedReader reader = new BufferedReader(new FileReader(SAVE_FILE));
			Type type = new TypeToken<Map<String, List<ItemStack[]>>>() {}.getType();
			Map<String, List<ItemStack[]>> data = GSON.fromJson(reader, type);
			if (data != null) {
				offers.clear();
				offers.putAll(data);
			}
			reader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		//finally {
		//	if (reader != null) {
		//		try {
		//			reader.close();
		//		} catch (Exception e) {
		//			System.err.println("Failed to close FileReader: " + e.getMessage());
		//		}
		//	}
		//}
	}



	public static void addOffer(String shopName, ItemStack[] offer) {
		List<ItemStack[]> shopOffers = offers.get(shopName);

		if (shopOffers == null) {
			shopOffers = new ArrayList<ItemStack[]>();
			offers.put(shopName, shopOffers);
		}
		shopOffers.add(offer);
		saveMarketData();
	}

	public static List<ItemStack[]> getOffers(String shopName) {
		List<ItemStack[]> shopOffers = offers.get(shopName);
		return (shopOffers != null) ? shopOffers : new ArrayList<ItemStack[]>();
	}

	public static void removeOffer(String shopName, int index) {
		List<ItemStack[]> shopOffers = offers.get(shopName);
		if (shopOffers != null && index >= 0 && index < shopOffers.size()) {
			shopOffers.remove(index);
			saveMarketData();
		}
	}

	public static List<ItemEntry[]> convertToItemEntryList(List<ItemStack[]> itemStacksList) {
		List<ItemEntry[]> convertedList = new ArrayList<ItemEntry[]>();

		for (ItemStack[] stackArray : itemStacksList) {
			ItemEntry[] entryArray = new ItemEntry[stackArray.length];
			for (int i = 0; i < stackArray.length; i++) {
				entryArray[i] = new ItemEntry(stackArray[i]); // Assuming ItemEntry has a constructor that takes an ItemStack
			}
			convertedList.add(entryArray);
		}

		return convertedList;
	}




	private static class ItemEntry {
		String itemName;
		int count;
		int metadata;
		String nbtData;

		ItemEntry(ItemStack stack) {
			this.itemName = Item.itemRegistry.getNameForObject(stack.getItem());
			this.count = stack.stackSize;
			this.metadata = stack.getItemDamage();
			this.nbtData = stack.hasTagCompound() ? stack.getTagCompound().toString() : null;
		}

		ItemStack toItemStack() {
			Item item = (Item) Item.itemRegistry.getObject(itemName);
			if (item == null) return null;

			ItemStack stack = new ItemStack(item, count, metadata);
			if (nbtData != null) {
				try {
					stack.setTagCompound((NBTTagCompound) JsonToNBT.func_150315_a(nbtData)); // 1.7.10 NBT Parsing
				} catch (Exception e) {
					System.err.println("Failed to parse NBT for item: " + itemName);
				}
			}
			return stack;
		}
	}
}
