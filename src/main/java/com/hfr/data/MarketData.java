package com.hfr.data;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTUtil;

import java.io.*;
import java.lang.reflect.Type;
import java.util.*;

public class MarketData {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final File SAVE_FILE = new File("config/marketdata.json");

	//so this is null on the serverside
	public static HashMap<String, List<ItemEntry[]>> offers = new HashMap<String, List<ItemEntry[]>>();

	public static void saveMarketData() {
		FileWriter writer = null;
		try {
			writer = new FileWriter(SAVE_FILE);
			GSON.toJson(offers, writer);
			System.out.println("Market data saved successfully.");
		} catch (Exception e) {
			System.err.println("Failed to save market data: " + e.getMessage());
		} finally {
			if (writer != null) {
				try {
					writer.close();
					System.out.println("FileWriter closed successfully after saving market data.");
				} catch (Exception e) {
					System.err.println("Failed to close FileWriter: " + e.getMessage());
				}
			}
		}
	}

	public static void loadMarketData() {
		if (!SAVE_FILE.exists()) {
			System.out.println("MarketData file does not exist. Skipping load.");
			return; // No file to load
		}

		FileReader reader = null;
		try {
			reader = new FileReader(SAVE_FILE);
			Type type = new TypeToken<HashMap<String, List<ItemEntry[]>>>() {}.getType();
			offers = GSON.fromJson(reader, type);
			System.out.println("Market data loaded successfully. Offers: " + offers);
		} catch (Exception e) {
			System.err.println("Failed to load market data: " + e.getMessage());
		} finally {
			if (reader != null) {
				try {
					reader.close();
					System.out.println("FileReader closed successfully after loading market data.");
				} catch (Exception e) {
					System.err.println("Failed to close FileReader: " + e.getMessage());
				}
			}
		}
	}

	public static void addOffer(String market, ItemStack[] items) {
		System.out.println("Adding offer to market: " + market);
		List<ItemEntry[]> marketOffers = offers.get(market);

		if (marketOffers == null) {
			marketOffers = new ArrayList<ItemEntry[]>();
			System.out.println("Created new offer list for market: " + market);
		}

		ItemEntry[] entries = new ItemEntry[items.length];

		for (int i = 0; i < items.length; i++) {
			if (items[i] != null) {
				entries[i] = new ItemEntry(items[i]);
				System.out.println("Added item to offer: " + items[i].getDisplayName());
			}
		}

		marketOffers.add(entries);
		offers.put(market, marketOffers);
		System.out.println("Offer added to market: " + market + ". Current offers: " + marketOffers);
		saveMarketData();
	}

	public static List<ItemStack[]> getOffers(String market) {



		System.out.println("Fetching offers for market: " + market);
		List<ItemStack[]> result = new ArrayList<ItemStack[]>();
		List<ItemEntry[]> entryList = offers.get(market);

		//entryList is null on servers
		if (entryList == null) {
			System.out.println("No offers found for market: " + market);
			return result;
		}

		//so somehow this is not running on the server side
		for (ItemEntry[] entryArray : entryList) {
			ItemStack[] stackArray = new ItemStack[entryArray.length];
			for (int i = 0; i < entryArray.length; i++) {
				if (entryArray[i] != null) {
					stackArray[i] = entryArray[i].toItemStack();
					System.out.println("Converted ItemEntry to ItemStack: " + stackArray[i].getDisplayName());
				}
			}
			result.add(stackArray);
		}
		return result;



	}

	public static List<ItemEntry[]> convertToItemEntryList(List<ItemStack[]> stackOffers) {
		List<ItemEntry[]> convertedOffers = new ArrayList<ItemEntry[]>();

		for (ItemStack[] stackArray : stackOffers) {
			ItemEntry[] entryArray = new ItemEntry[stackArray.length];
			for (int i = 0; i < stackArray.length; i++) {
				if (stackArray[i] != null) {
					entryArray[i] = new ItemEntry(stackArray[i]);
				}
			}
			convertedOffers.add(entryArray);
		}

		return convertedOffers;
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
			//this crashed clientside somehow
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
