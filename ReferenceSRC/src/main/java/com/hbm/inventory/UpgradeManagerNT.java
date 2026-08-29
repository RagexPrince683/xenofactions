package com.hbm.inventory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import com.hbm.items.machine.ItemMachineUpgrade;
import com.hbm.items.machine.ItemMachineUpgrade.UpgradeType;
import com.hbm.tileentity.IUpgradeInfoProvider;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

/**
 * Per-machine upgrade manager with content-aware slot caching.
 */
public class UpgradeManagerNT {

	private final Map<UpgradeType, Integer> upgrades = new HashMap<UpgradeType, Integer>();
	private UpgradeType mutexType = null;
	private SlotSignature[] cachedSignature = null;
	private boolean invalidated = true;

	public void invalidate() {
		this.invalidated = true;
	}

	public void checkSlots(ItemStack[] slots, int start, int end) {
		SlotSignature[] signature = buildSignature(slots, start, end);
		if(!this.invalidated && Arrays.equals(signature, this.cachedSignature)) return;

		this.cachedSignature = signature;
		this.invalidated = false;
		this.upgrades.clear();
		this.mutexType = null;

		if(slots == null) return;

		for(int i = start; i <= end && i < slots.length; i++) {
			ItemStack stack = slots[i];
			if(stack != null && stack.getItem() instanceof ItemMachineUpgrade) {
				ItemMachineUpgrade item = (ItemMachineUpgrade) stack.getItem();
				if(item.type.mutex) {
					if(this.mutexType == null || this.mutexType.ordinal() < item.type.ordinal()) {
						if(this.mutexType != null) this.upgrades.remove(this.mutexType);
						this.mutexType = item.type;
						this.upgrades.put(item.type, 1);
					}
				} else {
					Integer up = this.upgrades.get(item.type);
					int upgrade = (up == null ? 0 : up);
					upgrade += item.tier;
					this.upgrades.put(item.type, upgrade);
				}
			}
		}
	}

	public void checkSlots(IUpgradeInfoProvider provider, ItemStack[] slots, int start, int end) {
		this.checkSlots(slots, start, end);
		if(provider == null) return;
		for(UpgradeType type : UpgradeType.values()) {
			Integer level = this.upgrades.get(type);
			if(level != null) this.upgrades.put(type, Math.min(level, provider.getMaxLevel(type)));
		}
	}

	public int getLevel(UpgradeType type) {
		Integer up = this.upgrades.get(type);
		return up == null ? 0 : up;
	}

	public UpgradeType getMinerMutex() {
		return this.mutexType;
	}

	private static SlotSignature[] buildSignature(ItemStack[] slots, int start, int end) {
		if(slots == null || end < start) return new SlotSignature[0];
		SlotSignature[] signature = new SlotSignature[end - start + 1];
		for(int i = start; i <= end; i++) {
			signature[i - start] = i >= 0 && i < slots.length ? SlotSignature.from(slots[i]) : SlotSignature.EMPTY;
		}
		return signature;
	}

	private static class SlotSignature {
		private static final SlotSignature EMPTY = new SlotSignature(0, 0, 0, 0);

		private final int itemId;
		private final int meta;
		private final int count;
		private final int nbtHash;

		private SlotSignature(int itemId, int meta, int count, int nbtHash) {
			this.itemId = itemId;
			this.meta = meta;
			this.count = count;
			this.nbtHash = nbtHash;
		}

		private static SlotSignature from(ItemStack stack) {
			if(stack == null) return EMPTY;
			NBTTagCompound tag = stack.getTagCompound();
			return new SlotSignature(Item.getIdFromItem(stack.getItem()), stack.getItemDamage(), stack.stackSize, tag == null ? 0 : tag.hashCode());
		}

		@Override
		public boolean equals(Object obj) {
			if(this == obj) return true;
			if(!(obj instanceof SlotSignature)) return false;
			SlotSignature other = (SlotSignature) obj;
			return this.itemId == other.itemId && this.meta == other.meta && this.count == other.count && this.nbtHash == other.nbtHash;
		}

		@Override
		public int hashCode() {
			int result = this.itemId;
			result = 31 * result + this.meta;
			result = 31 * result + this.count;
			result = 31 * result + this.nbtHash;
			return result;
		}
	}
}
