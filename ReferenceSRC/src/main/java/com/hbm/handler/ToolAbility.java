package com.hbm.handler;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.hbm.config.ToolConfig;
import com.hbm.explosion.ExplosionNT;
import com.hbm.explosion.ExplosionNT.ExAttrib;
import com.hbm.inventory.OreDictManager;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.recipes.CentrifugeRecipes;
import com.hbm.inventory.recipes.CrystallizerRecipes;
import com.hbm.inventory.recipes.CrystallizerRecipes.CrystallizerRecipe;
import com.hbm.inventory.recipes.ShredderRecipes;
import com.hbm.items.ModItems;
import com.hbm.items.tool.IItemAbility;
import com.hbm.util.EnchantmentUtil;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.resources.I18n;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.oredict.OreDictionary;

public abstract class ToolAbility {

	/**
	 * Passive material/tool-construction traits.
	 *
	 * These are deliberately stored here so existing imports such as
	 * import com.hbm.handler.ToolAbility.*;
	 * allow registrations to use ToolTrait.LIGHTWEIGHT directly.
	 *
	 * Their actual mining, movement and durability behavior is implemented by
	 * ItemToolAbility, because ToolAbility only runs when a selectable mode is active.
	 */
	public static enum ToolTrait {
		LIGHTWEIGHT("tool.trait.lightweight"),
		CORROSION_RESISTANT("tool.trait.corrosionresistant"),
		NON_SPARKING("tool.trait.nonsparking"),
		WEAR_RESISTANT("tool.trait.wearresistant"),
		SHOCK_RESISTANT("tool.trait.shockresistant"),
		FATIGUE_RESISTANT("tool.trait.fatigueresistant"),
		HOT_HARDNESS("tool.trait.hothardness"),
		CARBIDE_EDGE("tool.trait.carbideedge"),
		BRITTLE_EDGE("tool.trait.brittleedge");

		private final String translationKey;

		private ToolTrait(String translationKey) {
			this.translationKey = translationKey;
		}

		public String getTranslationKey() {
			return translationKey;
		}
	}

	/**
	 * Identifies why ItemToolAbility.onBlockDestroyed is being called.
	 * The context is thread-local because block harvesting is synchronous and
	 * Item instances are shared globally between every stack/player.
	 */
	public static enum ToolOperation {
		NORMAL,
		HAMMER,
		RECURSION,
		FELLING,
		PROCESSING
	}

	private static final ThreadLocal<ToolOperation> CURRENT_OPERATION = new ThreadLocal<ToolOperation>() {
		@Override
		protected ToolOperation initialValue() {
			return ToolOperation.NORMAL;
		}
	};

	public static ToolOperation getCurrentOperation() {
		ToolOperation operation = CURRENT_OPERATION.get();
		return operation == null ? ToolOperation.NORMAL : operation;
	}

	public static ToolOperation pushOperation(ToolOperation operation) {
		ToolOperation previous = getCurrentOperation();
		CURRENT_OPERATION.set(operation == null ? ToolOperation.NORMAL : operation);
		return previous;
	}

	public static void popOperation(ToolOperation previous) {
		CURRENT_OPERATION.set(previous == null ? ToolOperation.NORMAL : previous);
	}

	//how to potentially save this: cancel the event/operation so that ItemInWorldManager's harvest method falls short, then recreate it with a more sensible structure
	public boolean onDig(World world, int x, int y, int z, EntityPlayer player, Block block, int meta, IItemAbility tool) { return false; }
	public abstract String getName();
	public abstract String getFullName();
	public abstract String getExtension();
	public abstract boolean isAllowed();

	/**
	 * Used by ItemToolAbility to tag the primary/reference block. Extra blocks
	 * receive their context through pushOperation/popOperation below.
	 */
	public ToolOperation getOperation() {
		return ToolOperation.NORMAL;
	}

	public static class RecursionAbility extends ToolAbility {

		int radius;

		public RecursionAbility(int radius) {
			this.radius = radius;
		}

		private Set<ThreeInts> pos = new HashSet<ThreeInts>();

		@Override
		public ToolOperation getOperation() {
			return ToolOperation.RECURSION;
		}

		@Override
		public boolean onDig(World world, int x, int y, int z, EntityPlayer player, Block block, int meta, IItemAbility tool) {

			Block b = world.getBlock(x, y, z);

			if(!ToolConfig.recursiveStone) {
				Item item = Item.getItemFromBlock(b);
				List<ItemStack> stone = OreDictionary.getOres(OreDictManager.KEY_STONE);
				for(ItemStack stack : stone) {
					if(stack.getItem() == item)
						return false;
				}
				List<ItemStack> cobble = OreDictionary.getOres(OreDictManager.KEY_COBBLESTONE);
				for(ItemStack stack : cobble) {
					if(stack.getItem() == item)
						return false;
				}
			}

			if(b == Blocks.netherrack && !ToolConfig.recursiveNetherrack)
				return false;

			List<Integer> indices = Arrays.asList(new Integer[] {0, 1, 2, 3, 4, 5});
			Collections.shuffle(indices);

			pos.clear();

			for(Integer i : indices) {
				switch(i) {
					case 0: breakExtra(world, x + 1, y, z, x, y, z, player, tool, 0); break;
					case 1: breakExtra(world, x - 1, y, z, x, y, z, player, tool, 0); break;
					case 2: breakExtra(world, x, y + 1, z, x, y, z, player, tool, 0); break;
					case 3: breakExtra(world, x, y - 1, z, x, y, z, player, tool, 0); break;
					case 4: breakExtra(world, x, y, z + 1, x, y, z, player, tool, 0); break;
					case 5: breakExtra(world, x, y, z - 1, x, y, z, player, tool, 0); break;
				}
			}
			return false;
		}

		private void breakExtra(World world, int x, int y, int z, int refX, int refY, int refZ, EntityPlayer player, IItemAbility tool, int depth) {

			if(pos.contains(new ThreeInts(x, y, z)))
				return;

			depth += 1;

			if(depth > ToolConfig.recursionDepth)
				return;

			pos.add(new ThreeInts(x, y, z));

			//don't lose the ref block just yet
			if(x == refX && y == refY && z == refZ)
				return;

			if(Vec3.createVectorHelper(x - refX, y - refY, z - refZ).lengthVector() > radius)
				return;

			Block b = world.getBlock(x, y, z);
			Block ref = world.getBlock(refX, refY, refZ);
			int meta = world.getBlockMetadata(x, y, z);
			int refMeta = world.getBlockMetadata(refX, refY, refZ);

			if(!isSameBlock(b, ref))
				return;

			if(meta != refMeta)
				return;

			if(player.getHeldItem() == null)
				return;

			ToolOperation previous = ToolAbility.pushOperation(ToolOperation.RECURSION);
			try {
				tool.breakExtraBlock(world, x, y, z, player, refX, refY, refZ);
			} finally {
				ToolAbility.popOperation(previous);
			}

			List<Integer> indices = Arrays.asList(new Integer[] {0, 1, 2, 3, 4, 5});
			Collections.shuffle(indices);

			for(Integer i : indices) {
				switch(i) {
					case 0: breakExtra(world, x + 1, y, z, refX, refY, refZ, player, tool, depth); break;
					case 1: breakExtra(world, x - 1, y, z, refX, refY, refZ, player, tool, depth); break;
					case 2: breakExtra(world, x, y + 1, z, refX, refY, refZ, player, tool, depth); break;
					case 3: breakExtra(world, x, y - 1, z, refX, refY, refZ, player, tool, depth); break;
					case 4: breakExtra(world, x, y, z + 1, refX, refY, refZ, player, tool, depth); break;
					case 5: breakExtra(world, x, y, z - 1, refX, refY, refZ, player, tool, depth); break;
				}
			}
		}

		private boolean isSameBlock(Block b1, Block b2) {

			if(b1 == b2) return true;
			if((b1 == Blocks.redstone_ore && b2 == Blocks.lit_redstone_ore) || (b1 == Blocks.lit_redstone_ore && b2 == Blocks.redstone_ore)) return true;

			return false;
		}

		@Override
		public String getName() {
			return "tool.ability.recursion";
		}

		@Override
		public String getFullName() {
			return I18n.format(getName()) + getExtension();
		}

		@Override
		public String getExtension() {
			return " (" + radius + ")";
		}

		@Override
		public boolean isAllowed() {
			return ToolConfig.abilityVein;
		}
	}

	/**
	 * Tree-only connected cutting. Unlike RecursionAbility, this can follow
	 * different log blocks/metas but refuses non-wood blocks and has a hard cap.
	 */
	public static class FellingAbility extends ToolAbility {

		private final int horizontalRadius;
		private final int verticalRange;
		private final int maxBlocks;

		public FellingAbility(int horizontalRadius, int verticalRange, int maxBlocks) {
			this.horizontalRadius = Math.max(1, horizontalRadius);
			this.verticalRange = Math.max(1, verticalRange);
			this.maxBlocks = Math.max(1, maxBlocks);
		}

		@Override
		public ToolOperation getOperation() {
			return ToolOperation.FELLING;
		}

		@Override
		public boolean onDig(World world, int x, int y, int z, EntityPlayer player, Block block, int meta, IItemAbility tool) {

			if(player.getHeldItem() == null || !block.isWood(world, x, y, z))
				return false;

			Set<ThreeInts> visited = new HashSet<ThreeInts>();
			Deque<ThreeInts> queue = new ArrayDeque<ThreeInts>();

			enqueueTreeNeighbors(queue, x, y, z);

			int broken = 0;
			int radiusSquared = horizontalRadius * horizontalRadius;

			while(!queue.isEmpty() && broken < maxBlocks && player.getHeldItem() != null) {

				ThreeInts next = queue.removeFirst();

				if(!visited.add(next))
					continue;

				if(next.y < y - 1 || next.y > y + verticalRange)
					continue;

				int dx = next.x - x;
				int dz = next.z - z;

				if(dx * dx + dz * dz > radiusSquared)
					continue;

				if(next.y < 0 || next.y >= world.getHeight() || !world.blockExists(next.x, next.y, next.z))
					continue;

				Block candidate = world.getBlock(next.x, next.y, next.z);

				if(candidate == Blocks.air || !candidate.isWood(world, next.x, next.y, next.z))
					continue;

				ToolOperation previous = ToolAbility.pushOperation(ToolOperation.FELLING);
				try {
					tool.breakExtraBlock(world, next.x, next.y, next.z, player, x, y, z);
				} finally {
					ToolAbility.popOperation(previous);
				}

				if(world.isAirBlock(next.x, next.y, next.z)) {
					broken++;
					enqueueTreeNeighbors(queue, next.x, next.y, next.z);
				}
			}

			return false;
		}

		private void enqueueTreeNeighbors(Deque<ThreeInts> queue, int x, int y, int z) {
			for(int dx = -1; dx <= 1; dx++) {
				for(int dy = -1; dy <= 1; dy++) {
					for(int dz = -1; dz <= 1; dz++) {
						if(dx == 0 && dy == 0 && dz == 0)
							continue;
						queue.addLast(new ThreeInts(x + dx, y + dy, z + dz));
					}
				}
			}
		}

		@Override
		public String getName() {
			return "tool.ability.felling";
		}

		@Override
		public String getFullName() {
			return I18n.format(getName()) + getExtension();
		}

		@Override
		public String getExtension() {
			return " (" + horizontalRadius + "/" + verticalRange + "/" + maxBlocks + ")";
		}

		@Override
		public boolean isAllowed() {
			return ToolConfig.abilityVein;
		}
	}

	public static class HammerAbility extends ToolAbility {

		int range;

		public HammerAbility(int range) {
			this.range = range;
		}

		@Override
		public ToolOperation getOperation() {
			return ToolOperation.HAMMER;
		}

		@Override
		public boolean onDig(World world, int x, int y, int z, EntityPlayer player, Block block, int meta, IItemAbility tool) {

			ToolOperation previous = ToolAbility.pushOperation(ToolOperation.HAMMER);
			try {
				for(int a = x - range; a <= x + range; a++) {
					for(int b = y - range; b <= y + range; b++) {
						for(int c = z - range; c <= z + range; c++) {

							if(a == x && b == y && c == z)
								continue;

							tool.breakExtraBlock(world, a, b ,c, player, x, y, z);
						}
					}
				}
			} finally {
				ToolAbility.popOperation(previous);
			}

			return false;
		}

		@Override
		public String getName() {
			return "tool.ability.hammer";
		}

		@Override
		public String getFullName() {
			return I18n.format(getName()) + getExtension();
		}

		@Override
		public String getExtension() {
			return " (" + range + ")";
		}

		@Override
		public boolean isAllowed() {
			return ToolConfig.abilityHammer;
		}
	}

	public static class HammerSilkAbility extends ToolAbility {

		int range;

		public HammerSilkAbility(int range) {
			this.range = range;
		}

		@Override
		public ToolOperation getOperation() {
			return ToolOperation.HAMMER;
		}

		@Override
		public boolean onDig(World world, int x, int y, int z, EntityPlayer player, Block block, int meta, IItemAbility tool) {
			if(EnchantmentHelper.getSilkTouchModifier(player) || player.getHeldItem() == null)
				return false;

			ItemStack stack = player.getHeldItem();
			EnchantmentUtil.addEnchantment(stack, Enchantment.silkTouch, 1);

			ToolOperation previous = ToolAbility.pushOperation(ToolOperation.HAMMER);
			try {
				for(int a = x - range; a <= x + range; a++) {
					for(int b = y - range; b <= y + range; b++) {
						for(int c = z - range; c <= z + range; c++) {

							if(a == x && b == y && c == z)
								continue;

							tool.breakExtraBlock(world, a, b ,c, player, x, y, z);
						}
					}
				}
				if(player instanceof EntityPlayerMP)
					IItemAbility.standardDigPost(world, x, y, z, (EntityPlayerMP) player);
			} finally {
				ToolAbility.popOperation(previous);
				EnchantmentUtil.removeEnchantment(stack, Enchantment.silkTouch);
			}

			return false;

		}

		@Override
		public String getName() {
			return "tool.ability.hammersilk";
		}

		@Override
		public String getFullName() {
			return I18n.format(getName()) + getExtension();
		}

		@Override
		public String getExtension() {
			return " (" + range + ")";
		}

		@Override
		public boolean isAllowed() {
			return ToolConfig.abilityHammer;
		}
	}

	/** Same mechanics as HammerSilkAbility, named as an engineered precision mode. */
	public static class PrecisionHammerAbility extends HammerSilkAbility {

		public PrecisionHammerAbility(int range) {
			super(range);
		}

		@Override
		public String getName() {
			return "tool.ability.precisionhammer";
		}
	}

	public static class SilkAbility extends ToolAbility {

		@Override
		public boolean onDig(World world, int x, int y, int z, EntityPlayer player, Block block, int meta, IItemAbility tool) {

			if(EnchantmentHelper.getSilkTouchModifier(player) || player.getHeldItem() == null)
				return false;

			ItemStack stack = player.getHeldItem();
			EnchantmentUtil.addEnchantment(stack, Enchantment.silkTouch, 1);

			try {
				if(player instanceof EntityPlayerMP)
					IItemAbility.standardDigPost(world, x, y, z, (EntityPlayerMP) player);
			} finally {
				EnchantmentUtil.removeEnchantment(stack, Enchantment.silkTouch);
			}

			return true;
		}

		@Override
		public String getName() {
			return "tool.ability.silktouch";
		}

		@Override
		public String getFullName() {
			return I18n.format(getName());
		}

		@Override
		public String getExtension() {
			return "";
		}

		@Override
		public boolean isAllowed() {
			return ToolConfig.abilitySilk;
		}
	}

	/** Same mechanics as SilkAbility, named as controlled precision cutting. */
	public static class PrecisionAbility extends SilkAbility {

		@Override
		public String getName() {
			return "tool.ability.precision";
		}
	}

	public static class LuckAbility extends ToolAbility {

		int luck;

		public LuckAbility(int luck) {
			this.luck = luck;
		}

		@Override
		public boolean onDig(World world, int x, int y, int z, EntityPlayer player, Block block, int meta, IItemAbility tool) {

			if(EnchantmentHelper.getFortuneModifier(player) > 0 || player.getHeldItem() == null)
				return false;

			ItemStack stack = player.getHeldItem();
			EnchantmentUtil.addEnchantment(stack, Enchantment.fortune, luck);

			try {
				if(player instanceof EntityPlayerMP)
					IItemAbility.standardDigPost(world, x, y, z, (EntityPlayerMP) player);
			} finally {
				EnchantmentUtil.removeEnchantment(stack, Enchantment.fortune);
			}

			return true;
		}

		@Override
		public String getName() {
			return "tool.ability.luck";
		}

		@Override
		public String getFullName() {
			return I18n.format(getName()) + getExtension();
		}

		@Override
		public String getExtension() {
			return " (" + luck + ")";
		}

		@Override
		public boolean isAllowed() {
			return ToolConfig.abilityLuck;
		}
	}

	/** Same mechanics as LuckAbility, framed as scanner-guided extraction. */
	public static class SelectiveExtractionAbility extends LuckAbility {

		public SelectiveExtractionAbility(int luck) {
			super(luck);
		}

		@Override
		public String getName() {
			return "tool.ability.selectiveextraction";
		}
	}

	public static class SmelterAbility extends ToolAbility {

		private final int operationCost;

		public SmelterAbility() {
			this(1);
		}

		public SmelterAbility(int operationCost) {
			this.operationCost = Math.max(1, operationCost);
		}

		@Override
		public ToolOperation getOperation() {
			return ToolOperation.PROCESSING;
		}

		@Override
		public boolean onDig(World world, int x, int y, int z, EntityPlayer player, Block block, int meta, IItemAbility tool) {

			List<ItemStack> drops = block.getDrops(world, x, y, z, world.getBlockMetadata(x, y, z), 0);

			boolean doesSmelt = false;

			for(int i = 0; i < drops.size(); i++) {
				ItemStack stack = drops.get(i).copy();
				ItemStack result = FurnaceRecipes.smelting().getSmeltingResult(stack);

				if(result != null) {
					result = result.copy();
					result.stackSize *= stack.stackSize;
					drops.set(i, result);
					doesSmelt = true;
				}
			}

			if(doesSmelt) {
				world.setBlockToAir(x, y, z);
				player.getHeldItem().damageItem(operationCost, player);

				for(ItemStack stack : drops)
					world.spawnEntityInWorld(new EntityItem(world, x + 0.5, y + 0.5, z + 0.5, stack.copy()));
			}

			return false;
		}

		@Override
		public String getName() {
			return "tool.ability.smelter";
		}

		@Override
		public String getFullName() {
			return I18n.format(getName());
		}

		@Override
		public String getExtension() {
			return "";
		}

		@Override
		public boolean isAllowed() {
			return ToolConfig.abilityFurnace;
		}
	}

	public static class ShredderAbility extends ToolAbility {

		private final int operationCost;

		public ShredderAbility() {
			this(1);
		}

		public ShredderAbility(int operationCost) {
			this.operationCost = Math.max(1, operationCost);
		}

		@Override
		public ToolOperation getOperation() {
			return ToolOperation.PROCESSING;
		}

		@Override
		public boolean onDig(World world, int x, int y, int z, EntityPlayer player, Block block, int meta, IItemAbility tool) {

			//a band-aid on a gaping wound
			if(block == Blocks.lit_redstone_ore)
				block = Blocks.redstone_ore;

			ItemStack stack = new ItemStack(block, 1, meta);
			ItemStack result = ShredderRecipes.getShredderResult(stack);

			if(result != null && result.getItem() != ModItems.scrap) {
				world.setBlockToAir(x, y, z);
				world.spawnEntityInWorld(new EntityItem(world, x + 0.5, y + 0.5, z + 0.5, result.copy()));
				player.getHeldItem().damageItem(operationCost, player);
			}

			return false;
		}

		@Override
		public String getName() {
			return "tool.ability.shredder";
		}

		@Override
		public String getFullName() {
			return I18n.format(getName());
		}

		@Override
		public String getExtension() {
			return "";
		}

		@Override
		public boolean isAllowed() {
			return ToolConfig.abilityShredder;
		}
	}

	/**
	 * Manual crushing mode. It reuses shredder recipes but only for hard,
	 * brittle/mineral-like blocks, preventing a hand hammer from acting as a
	 * universal machine disassembler.
	 */
	public static class CrusherAbility extends ToolAbility {

		private final int operationCost;

		public CrusherAbility() {
			this(3);
		}

		public CrusherAbility(int operationCost) {
			this.operationCost = Math.max(1, operationCost);
		}

		@Override
		public ToolOperation getOperation() {
			return ToolOperation.PROCESSING;
		}

		@Override
		public boolean onDig(World world, int x, int y, int z, EntityPlayer player, Block block, int meta, IItemAbility tool) {

			if(block == Blocks.lit_redstone_ore)
				block = Blocks.redstone_ore;

			Material material = block.getMaterial();

			if(material != Material.rock &&
				material != Material.glass &&
				material != Material.clay &&
				material != Material.iron &&
				material != Material.anvil)
				return false;

			ItemStack input = new ItemStack(block, 1, meta);
			ItemStack result = ShredderRecipes.getShredderResult(input);

			if(result != null && result.getItem() != ModItems.scrap) {
				world.setBlockToAir(x, y, z);
				world.spawnEntityInWorld(new EntityItem(world, x + 0.5, y + 0.5, z + 0.5, result.copy()));
				player.getHeldItem().damageItem(operationCost, player);
			}

			return false;
		}

		@Override
		public String getName() {
			return "tool.ability.crusher";
		}

		@Override
		public String getFullName() {
			return I18n.format(getName());
		}

		@Override
		public String getExtension() {
			return "";
		}

		@Override
		public boolean isAllowed() {
			return ToolConfig.abilityShredder;
		}
	}

	public static class CentrifugeAbility extends ToolAbility {

		private final int operationCost;

		public CentrifugeAbility() {
			this(1);
		}

		public CentrifugeAbility(int operationCost) {
			this.operationCost = Math.max(1, operationCost);
		}

		@Override
		public ToolOperation getOperation() {
			return ToolOperation.PROCESSING;
		}

		@Override
		public boolean onDig(World world, int x, int y, int z, EntityPlayer player, Block block, int meta, IItemAbility tool) {

			//a band-aid on a gaping wound
			if(block == Blocks.lit_redstone_ore)
				block = Blocks.redstone_ore;

			ItemStack stack = new ItemStack(block, 1, meta);
			ItemStack[] result = CentrifugeRecipes.getOutput(stack);

			if(result != null) {
				world.setBlockToAir(x, y, z);
				player.getHeldItem().damageItem(operationCost, player);

				for(ItemStack st : result) {
					if(st != null)
						world.spawnEntityInWorld(new EntityItem(world, x + 0.5, y + 0.5, z + 0.5, st.copy()));
				}
			}

			return false;
		}

		@Override
		public String getExtension() {
			return "";
		}

		@Override
		public String getName() {
			return "tool.ability.centrifuge";
		}

		@Override
		public String getFullName() {
			return I18n.format(getName());
		}

		@Override
		public boolean isAllowed() {
			return ToolConfig.abilityCentrifuge;
		}
	}

	public static class CrystallizerAbility extends ToolAbility {

		private final int operationCost;

		public CrystallizerAbility() {
			this(1);
		}

		public CrystallizerAbility(int operationCost) {
			this.operationCost = Math.max(1, operationCost);
		}

		@Override
		public ToolOperation getOperation() {
			return ToolOperation.PROCESSING;
		}

		@Override
		public boolean onDig(World world, int x, int y, int z, EntityPlayer player, Block block, int meta, IItemAbility tool) {

			//a band-aid on a gaping wound
			if(block == Blocks.lit_redstone_ore)
				block = Blocks.redstone_ore;

			ItemStack stack = new ItemStack(block, 1, meta);
			CrystallizerRecipe result = CrystallizerRecipes.getOutput(stack, Fluids.PEROXIDE);

			if(result != null) {
				world.setBlockToAir(x, y, z);
				world.spawnEntityInWorld(new EntityItem(world, x + 0.5, y + 0.5, z + 0.5, result.output.copy()));
				player.getHeldItem().damageItem(operationCost, player);
			}

			return false;
		}

		@Override
		public String getExtension() {
			return "";
		}

		@Override
		public String getName() {
			return "tool.ability.crystallizer";
		}

		@Override
		public String getFullName() {
			return I18n.format(getName());
		}

		@Override
		public boolean isAllowed() {
			return ToolConfig.abilityCrystallizer;
		}
	}

	public static class MercuryAbility extends ToolAbility {

		private final int operationCost;

		public MercuryAbility() {
			this(1);
		}

		public MercuryAbility(int operationCost) {
			this.operationCost = Math.max(1, operationCost);
		}

		@Override
		public ToolOperation getOperation() {
			return ToolOperation.PROCESSING;
		}

		@Override
		public boolean onDig(World world, int x, int y, int z, EntityPlayer player, Block block, int meta, IItemAbility tool) {

			//a band-aid on a gaping wound
			if(block == Blocks.lit_redstone_ore)
				block = Blocks.redstone_ore;

			int mercury = 0;

			if(block == Blocks.redstone_ore)
				mercury = player.getRNG().nextInt(5) + 4;
			if(block == Blocks.redstone_block)
				mercury = player.getRNG().nextInt(7) + 8;

			if(mercury > 0) {
				world.setBlockToAir(x, y, z);
				world.spawnEntityInWorld(new EntityItem(world, x + 0.5, y + 0.5, z + 0.5, new ItemStack(ModItems.ingot_mercury, mercury)));
				player.getHeldItem().damageItem(operationCost, player);
			}

			return false;
		}

		@Override
		public String getExtension() {
			return "";
		}

		@Override
		public String getName() {
			return "tool.ability.mercury";
		}

		@Override
		public String getFullName() {
			return I18n.format(getName());
		}

		@Override
		public boolean isAllowed() {
			return ToolConfig.abilityMercury;
		}
	}

	public static class ExplosionAbility extends ToolAbility {

		float strength;
		int operationCost;

		public ExplosionAbility(float strength) {
			this(strength, 1);
		}

		public ExplosionAbility(float strength, int operationCost) {
			this.strength = strength;
			this.operationCost = Math.max(1, operationCost);
		}

		@Override
		public ToolOperation getOperation() {
			return ToolOperation.PROCESSING;
		}

		@Override
		public boolean onDig(World world, int x, int y, int z, EntityPlayer player, Block block, int meta, IItemAbility tool) {

			ExplosionNT ex = new ExplosionNT(player.worldObj, player, x + 0.5, y + 0.5, z + 0.5, strength);
			ex.addAttrib(ExAttrib.ALLDROP);
			ex.addAttrib(ExAttrib.NOHURT);
			ex.addAttrib(ExAttrib.NOPARTICLE);
			ex.doExplosionA();
			ex.doExplosionB(false);

			player.worldObj.createExplosion(player, x + 0.5, y + 0.5, z + 0.5, 0.1F, false);
			player.getHeldItem().damageItem(operationCost, player);

			return true;
		}

		@Override
		public String getExtension() {
			return " (" + strength + ")";
		}

		@Override
		public String getName() {
			return "tool.ability.explosion";
		}

		@Override
		public String getFullName() {
			return I18n.format(getName()) + getExtension();
		}

		@Override
		public boolean isAllowed() {
			return ToolConfig.abilityExplosion;
		}
	}
}
