package com.hfr.data;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent;

import java.io.*;
import java.lang.reflect.Type;
import java.util.*;

public class MarketData {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static File saveFile;
	private static final SimpleNetworkWrapper NETWORK = NetworkRegistry.INSTANCE.newSimpleChannel("market_data");

	public static HashMap<String, List<ItemEntry[]>> offers = new HashMap<String, List<ItemEntry[]>>();

	public static void init(File worldDir) {
		saveFile = new File(worldDir, "marketdata.json");
		MinecraftForge.EVENT_BUS.register(new MarketData());
		loadMarketData();
	}

	public static void saveMarketData() {
		try (FileWriter writer = new FileWriter(saveFile)) {
			GSON.toJson(offers, writer);
		} catch (Exception e) {
			System.err.println("Failed to save market data: " + e.getMessage());
		}
	}

	public static void loadMarketData() {
		if (!saveFile.exists()) return;
		try (FileReader reader = new FileReader(saveFile)) {
			Type type = new TypeToken<HashMap<String, List<ItemEntry[]>>>() {}.getType();
			HashMap<String, List<ItemEntry[]>> loadedData = GSON.fromJson(reader, type);
			if (loadedData != null) {
				offers.clear();
				offers.putAll(loadedData);
			}
		} catch (Exception e) {
			System.err.println("Failed to load market data: " + e.getMessage());
		}
	}

	public static void addOffer(String market, ItemStack[] items) {
		offers.computeIfAbsent(market, k -> new ArrayList<>());

		ItemEntry[] entries = Arrays.stream(items)
				.filter(Objects::nonNull)
				.map(ItemEntry::new)
				.toArray(ItemEntry[]::new);

		offers.get(market).add(entries);
		saveMarketData();
		syncToClients();
	}

	public static List<ItemStack[]> getOffers(String market) {
		List<ItemStack[]> result = new ArrayList<>();
		List<ItemEntry[]> entryList = offers.get(market);
		if (entryList == null) return result;

		for (ItemEntry[] entryArray : entryList) {
			ItemStack[] stackArray = Arrays.stream(entryArray)
					.map(ItemEntry::toItemStack)
					.toArray(ItemStack[]::new);
			result.add(stackArray);
		}
		return result;
	}

	private static void syncToClients() {
		NETWORK.sendToAll(new PacketMarketData(offers));
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

	@SubscribeEvent
	public void onWorldSave(WorldEvent.Save event) {
		saveMarketData();
	}

	@SubscribeEvent
	public void onWorldLoad(WorldEvent.Load event) {
		loadMarketData();
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
					stack.setTagCompound((NBTTagCompound) JsonToNBT.func_150315_a(nbtData));
				} catch (Exception e) {
					System.err.println("Failed to parse NBT for item: " + itemName);
				}
			}
			return stack;
		}
	}
}
