package com.hfr.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraft.world.WorldSavedData;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class MarketData extends WorldSavedData {
	private static final String FILE_NAME = "marketdata.json";
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

	public HashMap<String, List<ItemStack[]>> offers = new HashMap<String, List<ItemStack[]>>();

	public MarketData() {
		super("hfr_market");
	}

	public MarketData(String name) {
		super(name);
	}

	public static MarketData getData(World world) {
		MarketData data = (MarketData) world.perWorldStorage.loadData(MarketData.class, "hfr_market");
		if (data == null) {
			data = new MarketData();
			world.perWorldStorage.setData("hfr_market", data);
			data.loadFromFile();
		}
		return data;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		int count = nbt.getInteger("count");
		readMarkets(nbt, count);
		loadFromFile();
	}

	public void readMarkets(NBTTagCompound nbt, int count) {
		for (int index = 0; index < count; index++) {
			String name = nbt.getString("market_" + index);
			int offerCount = nbt.getInteger("offercount_" + index);
			for (int off = 0; off < offerCount; off++) {
				readOffers(nbt, name, off);
			}
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
		List<ItemStack[]> existingOffers = offers.get(name);
		if (existingOffers == null) {
			existingOffers = new ArrayList<ItemStack[]>();
			offers.put(name, existingOffers);
		}
		existingOffers.add(slots);
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		nbt.setInteger("count", offers.size());
		writeMarkets(nbt);
		saveToFile();
	}

	public void writeMarkets(NBTTagCompound nbt) {
		int index = 0;
		for (Entry<String, List<ItemStack[]>> entry : offers.entrySet()) {
			nbt.setString("market_" + index, entry.getKey());
			nbt.setInteger("offercount_" + index, entry.getValue().size());
			writeOffers(nbt, entry.getKey(), entry.getValue());
			index++;
		}
	}

	public void writeOffers(NBTTagCompound nbt, String name, List<ItemStack[]> offers) {
		for (int index = 0; index < offers.size(); index++) {
			NBTTagList list = new NBTTagList();
			ItemStack[] offer = offers.get(index);
			for (int i = 0; i < offer.length; i++) {
				if (offer[i] != null) {
					NBTTagCompound nbt1 = new NBTTagCompound();
					nbt1.setByte("slot" + index, (byte) i);
					offer[i].writeToNBT(nbt1);
					list.appendTag(nbt1);
				}
			}
			nbt.setTag("items" + name + index, list);
		}
	}

	private void loadFromFile() {
		File file = new File(FILE_NAME);
		if (file.exists()) {
			FileReader reader = null;
			try {
				reader = new FileReader(file);
				Type type = new TypeToken<HashMap<String, List<ItemStack[]>>>() {}.getType();
				this.offers = GSON.fromJson(reader, type);
			} catch (IOException e) {
				e.printStackTrace();
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
	}

	private void saveToFile() {
		File file = new File(FILE_NAME);
		FileWriter writer = null;
		try {
			writer = new FileWriter(file);
			GSON.toJson(this.offers, writer);
		} catch (IOException e) {
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
}