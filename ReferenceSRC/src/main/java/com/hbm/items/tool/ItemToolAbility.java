package com.hbm.items.tool;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.gas.BlockGasExplosive;
import com.hbm.blocks.gas.BlockGasFlammable;
import com.hbm.handler.ToolAbility;
import com.hbm.handler.ToolAbility.*;
import com.hbm.handler.WeaponAbility;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.trait.FT_Corrosive;
import com.hbm.main.MainRegistry;
import com.hbm.packet.PacketDispatcher;
import com.hbm.packet.toclient.PlayerInformPacket;
import com.hbm.util.ChatBuilder;

import api.hbm.item.IDepthRockTool;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemTool;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public class ItemToolAbility extends ItemTool implements IItemAbility, IDepthRockTool {

	protected boolean isShears = false;
	protected EnumToolType toolType;
	protected EnumRarity rarity = EnumRarity.common;
	//was there a reason for this to be private?
	protected float damage;
	protected double movement;
	protected List<ToolAbility> breakAbility = new ArrayList() {{ add(null); }};
	protected List<WeaponAbility> hitAbility = new ArrayList();

	/** Passive properties belonging to the tool/material rather than a selected mode. */
	protected final Set<ToolTrait> toolTraits = EnumSet.noneOf(ToolTrait.class);

	private static final UUID LIGHTWEIGHT_MOVEMENT_UUID = UUID.fromString("c173c90c-4b2e-4ee0-9dc6-e2c70ea48cf7");

	private static final String NBT_PENDING_OPERATION = "hbmToolPendingOperation";
	private static final String NBT_PENDING_X = "hbmToolPendingX";
	private static final String NBT_PENDING_Y = "hbmToolPendingY";
	private static final String NBT_PENDING_Z = "hbmToolPendingZ";
	private static final String NBT_PENDING_DIM = "hbmToolPendingDim";
	private static final String NBT_PENDING_TIME = "hbmToolPendingTime";

	private static final Set<Block> HOT_BLOCKS =
		Collections.newSetFromMap(new IdentityHashMap<Block, Boolean>());

	private static final Map<Block, Integer> CORROSIVE_BLOCKS =
		new IdentityHashMap<Block, Integer>();

	private static final Map<Block, SparkReaction> SPARK_SENSITIVE_BLOCKS =
		new IdentityHashMap<Block, SparkReaction>();

	/**
	 * Chance that mining a hard block with a conventional sparking tool produces
	 * a spark capable of igniting an adjacent registered hazard.
	 */
	private static float miningSparkChance = 0.10F;

	private static final class SparkReaction {

		private final float explosionStrength;

		private SparkReaction(float explosionStrength) {
			this.explosionStrength = Math.max(0F, explosionStrength);
		}
	}

	public static enum EnumToolType {

		PICKAXE(
			Sets.newHashSet(new Material[] { Material.iron, Material.anvil, Material.rock, Material.glass }),
			Sets.newHashSet(new Block[] { Blocks.cobblestone, Blocks.double_stone_slab, Blocks.stone_slab, Blocks.stone, Blocks.sandstone, Blocks.mossy_cobblestone, Blocks.iron_ore, Blocks.iron_block, Blocks.coal_ore, Blocks.gold_block, Blocks.gold_ore, Blocks.diamond_ore, Blocks.diamond_block, Blocks.ice, Blocks.netherrack, Blocks.lapis_ore, Blocks.lapis_block, Blocks.redstone_ore, Blocks.lit_redstone_ore, Blocks.rail, Blocks.detector_rail, Blocks.golden_rail, Blocks.activator_rail })
		),
		AXE(
			Sets.newHashSet(new Material[] { Material.wood, Material.plants, Material.vine }),
			Sets.newHashSet(new Block[] { Blocks.planks, Blocks.bookshelf, Blocks.log, Blocks.log2, Blocks.chest, Blocks.pumpkin, Blocks.lit_pumpkin })
		),
		SHOVEL(
			Sets.newHashSet(new Material[] { Material.clay, Material.sand, Material.ground, Material.snow, Material.craftedSnow }),
			Sets.newHashSet(new Block[] { Blocks.grass, Blocks.dirt, Blocks.sand, Blocks.gravel, Blocks.snow_layer, Blocks.snow, Blocks.clay, Blocks.farmland, Blocks.soul_sand, Blocks.mycelium })
		),
		MINER(
			Sets.newHashSet(new Material[] { Material.grass, Material.iron, Material.anvil, Material.glass, Material.rock, Material.clay, Material.sand, Material.ground, Material.snow, Material.craftedSnow })
		);

		private EnumToolType(Set<Material> materials) {
			this.materials = materials;
		}

		private EnumToolType(Set<Material> materials, Set<Block> blocks) {
			this.materials = materials;
			this.blocks = blocks;
		}

		public Set<Material> materials = new HashSet();
		public Set<Block> blocks = new HashSet();
	}

	public ItemToolAbility setShears() {
		this.isShears = true;
		return this;
	}

	public ItemToolAbility(float damage, double movement, ToolMaterial material, EnumToolType type) {
		super(0, material, type.blocks);
		this.damage = damage;
		this.movement = movement;
		this.toolType = type;

		// hacky workaround, might be good to rethink this entire system
		if(type == EnumToolType.MINER) {
			this.setHarvestLevel("pickaxe", material.getHarvestLevel());
			this.setHarvestLevel("shovel", material.getHarvestLevel());
		} else {
			this.setHarvestLevel(type.toString().toLowerCase(Locale.US), material.getHarvestLevel());
		}
	}

	public ItemToolAbility addBreakAbility(ToolAbility breakAbility) {
		this.breakAbility.add(breakAbility);
		return this;
	}

	public ItemToolAbility addHitAbility(WeaponAbility weaponAbility) {
		this.hitAbility.add(weaponAbility);
		return this;
	}

	/**
	 * Adds a passive trait. Unlike addBreakAbility, every added trait is active
	 * simultaneously and does not appear in the right-click ability cycle.
	 */
	public ItemToolAbility addToolTrait(ToolTrait trait) {
		if(trait != null)
			this.toolTraits.add(trait);
		return this;
	}

	public ItemToolAbility addToolTraits(ToolTrait... traits) {
		if(traits != null) {
			for(ToolTrait trait : traits)
				addToolTrait(trait);
		}
		return this;
	}

	public boolean hasToolTrait(ToolTrait trait) {
		return trait != null && this.toolTraits.contains(trait);
	}

	public static boolean hasToolTrait(ItemStack stack, ToolTrait trait) {
		return stack != null &&
			stack.getItem() instanceof ItemToolAbility &&
			((ItemToolAbility) stack.getItem()).hasToolTrait(trait);
	}

	public static void registerHotBlocks(Block... blocks) {
		if(blocks == null)
			return;

		for(Block block : blocks) {
			if(block != null)
				HOT_BLOCKS.add(block);
		}
	}

	/**
	 * @param extraDamage additional durability/power cost when this block is mined
	 *                    without CORROSION_RESISTANT.
	 */
	public static void registerCorrosiveBlock(Block block, int extraDamage) {
		if(block != null)
			CORROSIVE_BLOCKS.put(block, Math.max(1, extraDamage));
	}

	/**
	 * Registers a solid fuel, explosive, powder, etc. that should react to a
	 * mining spark. A strength of 0 ignites it; a positive value explodes it.
	 */
	public static void registerSparkSensitiveBlock(Block block, float explosionStrength) {
		if(block != null)
			SPARK_SENSITIVE_BLOCKS.put(block, new SparkReaction(explosionStrength));
	}

	public static void setMiningSparkChance(float chance) {
		miningSparkChance = Math.max(0F, Math.min(1F, chance));
	}

	/**
	 * Shared hook for any fluid/container/exposure system that knows a tool has
	 * contacted a corrosive FluidType.
	 */
	public static void applyFluidCorrosion(ItemStack stack, EntityLivingBase holder, FluidType fluid) {

		if(stack == null || holder == null || fluid == null || !fluid.isCorrosive())
			return;

		FT_Corrosive corrosive = fluid.getTrait(FT_Corrosive.class);
		int rating = corrosive == null ? 25 : corrosive.getRating();
		int damage = Math.max(1, rating / 25);

		applyCorrosionDamage(stack, holder, damage);
	}

	/**
	 * Shared hook for corrosive gases, spills, tile entities and custom blocks.
	 * Resistant tools take only 20% of the supplied corrosion damage.
	 */
	public static void applyCorrosionDamage(ItemStack stack, EntityLivingBase holder, int damage) {

		if(stack == null || holder == null || damage <= 0)
			return;

		if(hasToolTrait(stack, ToolTrait.CORROSION_RESISTANT))
			damage /= 5;

		if(damage > 0)
			stack.damageItem(damage, holder);
	}

	// <insert obvious Rarity joke here>
	public ItemToolAbility setRarity(EnumRarity rarity) {
		this.rarity = rarity;
		return this;
	}

	public EnumRarity getRarity(ItemStack stack) {
		return this.rarity != EnumRarity.common ? this.rarity : super.getRarity(stack);
	}

	public boolean hitEntity(ItemStack stack, EntityLivingBase victim, EntityLivingBase attacker) {

		if(!attacker.worldObj.isRemote && !this.hitAbility.isEmpty() && attacker instanceof EntityPlayer && canOperate(stack)) {

			for(WeaponAbility ability : this.hitAbility) {
				ability.onHit(attacker.worldObj, (EntityPlayer) attacker, victim, this);
			}
		}

		stack.damageItem(2, attacker);

		return true;
	}

	@Override
	public boolean onBlockStartBreak(ItemStack stack, int x, int y, int z, EntityPlayer player) {

		World world = player.worldObj;
		Block block = world.getBlock(x, y, z);
		int meta = world.getBlockMetadata(x, y, z);
		ToolAbility ability = this.getCurrentAbility(stack);

		if(!world.isRemote &&
			(canHarvestBlock(block, stack) || canShearBlock(block, stack, world, x, y, z)) &&
			ability != null &&
			canOperate(stack)) {

			setPendingOperation(stack, ability.getOperation(), world, x, y, z);

			boolean handled = ability.onDig(world, x, y, z, player, block, meta, this);

			/*
			 * Processing/Silk modes may remove the reference block themselves.
			 * Do not leave a stale operation tag behind in that case.
			 */
			if(handled || world.isAirBlock(x, y, z))
				clearPendingOperation(stack);

			return handled;
		}

		clearPendingOperation(stack);
		return false;
	}

	@Override
	public boolean onBlockDestroyed(ItemStack stack, World world, Block block, int x, int y, int z, EntityLivingBase user) {

		if(world.isRemote)
			return true;

		if(block.getBlockHardness(world, x, y, z) == 0F)
			return true;

		ToolOperation operation = ToolAbility.getCurrentOperation();

		if(operation == ToolOperation.NORMAL)
			operation = consumePendingOperation(stack, world, x, y, z);

		int operationalWear = 1;

		if(operation == ToolOperation.NORMAL &&
			hasToolTrait(ToolTrait.WEAR_RESISTANT) &&
			user.getRNG().nextFloat() < 0.25F) {
			operationalWear = 0;
		}

		if(operation == ToolOperation.HAMMER) {

			if(hasToolTrait(ToolTrait.BRITTLE_EDGE))
				operationalWear += 2;

			if(hasToolTrait(ToolTrait.SHOCK_RESISTANT) &&
				user.getRNG().nextFloat() < 0.50F) {
				operationalWear = 0;
			}
		}

		if((operation == ToolOperation.RECURSION || operation == ToolOperation.FELLING) &&
			hasToolTrait(ToolTrait.FATIGUE_RESISTANT) &&
			user.getRNG().nextFloat() < 0.50F) {
			operationalWear = 0;
		}

		int environmentalWear = 0;

		if(isHotBlock(block) && !hasToolTrait(ToolTrait.HOT_HARDNESS))
			environmentalWear++;

		Integer corrosion = CORROSIVE_BLOCKS.get(block);

		if(corrosion != null) {
			int corrosionWear = corrosion.intValue();

			if(hasToolTrait(ToolTrait.CORROSION_RESISTANT))
				corrosionWear /= 5;

			environmentalWear += corrosionWear;
		}

		int totalDamage = operationalWear + environmentalWear;

		if(totalDamage > 0)
			stack.damageItem(totalDamage, user);

		tryGenerateMiningSpark(stack, world, block, x, y, z);

		return true;
	}

	@Override
	public float getDigSpeed(ItemStack stack, Block block, int meta) {

		if(!canOperate(stack))
			return 1F;

		if(toolType == null)
			return super.getDigSpeed(stack, block, meta);

		boolean properMaterial = toolType.blocks.contains(block) || toolType.materials.contains(block.getMaterial());
		float speed = properMaterial ? this.efficiencyOnProperMaterial : super.getDigSpeed(stack, block, meta);

		if(properMaterial && hasToolTrait(ToolTrait.LIGHTWEIGHT))
			speed *= 1.08F;

		if(properMaterial && hasToolTrait(ToolTrait.CARBIDE_EDGE) && isCarbideEffective(block))
			speed *= 1.25F;

		if(isHotBlock(block) && !hasToolTrait(ToolTrait.HOT_HARDNESS))
			speed *= 0.75F;

		return speed;
	}

	private boolean isCarbideEffective(Block block) {

		if(block == null)
			return false;

		Material material = block.getMaterial();

		return material == Material.rock ||
			material == Material.glass ||
			material == Material.clay ||
			material == Material.iron ||
			material == Material.anvil;
	}

	private static boolean isHotBlock(Block block) {
		return block != null && HOT_BLOCKS.contains(block);
	}

	private static boolean canProduceMiningSpark(Block block) {

		if(block == null)
			return false;

		Material material = block.getMaterial();

		return material == Material.rock ||
			material == Material.iron ||
			material == Material.anvil ||
			material == Material.glass;
	}

	private void tryGenerateMiningSpark(ItemStack stack, World world, Block minedBlock, int x, int y, int z) {

		if(world == null ||
			world.isRemote ||
			hasToolTrait(ToolTrait.NON_SPARKING) ||
			!canProduceMiningSpark(minedBlock) ||
			world.rand.nextFloat() >= miningSparkChance)
			return;

		reactToSpark(world, x, y, z);

		for(ForgeDirection direction : ForgeDirection.VALID_DIRECTIONS) {
			reactToSpark(
				world,
				x + direction.offsetX,
				y + direction.offsetY,
				z + direction.offsetZ);
		}
	}

	private void reactToSpark(World world, int x, int y, int z) {

		if(!world.blockExists(x, y, z))
			return;

		Block target = world.getBlock(x, y, z);

		if(target instanceof BlockGasExplosive) {
			world.setBlockToAir(x, y, z);
			world.newExplosion(null, x + 0.5D, y + 0.5D, z + 0.5D, 3F, true, false);
			return;
		}

		if(target instanceof BlockGasFlammable) {
			world.setBlock(x, y, z, Blocks.fire);
			return;
		}

		SparkReaction reaction = SPARK_SENSITIVE_BLOCKS.get(target);

		if(reaction == null)
			return;

		world.setBlockToAir(x, y, z);

		if(reaction.explosionStrength > 0F)
			world.newExplosion(null, x + 0.5D, y + 0.5D, z + 0.5D, reaction.explosionStrength, true, false);
		else
			world.setBlock(x, y, z, Blocks.fire);
	}

	private void setPendingOperation(ItemStack stack, ToolOperation operation, World world, int x, int y, int z) {

		if(stack == null || world == null)
			return;

		if(!stack.hasTagCompound())
			stack.stackTagCompound = new NBTTagCompound();

		NBTTagCompound nbt = stack.stackTagCompound;
		nbt.setInteger(NBT_PENDING_OPERATION, operation == null ? ToolOperation.NORMAL.ordinal() : operation.ordinal());
		nbt.setInteger(NBT_PENDING_X, x);
		nbt.setInteger(NBT_PENDING_Y, y);
		nbt.setInteger(NBT_PENDING_Z, z);
		nbt.setInteger(NBT_PENDING_DIM, world.provider.dimensionId);
		nbt.setLong(NBT_PENDING_TIME, world.getTotalWorldTime());
	}

	private ToolOperation consumePendingOperation(ItemStack stack, World world, int x, int y, int z) {

		if(stack == null || world == null || !stack.hasTagCompound())
			return ToolOperation.NORMAL;

		NBTTagCompound nbt = stack.stackTagCompound;

		if(!nbt.hasKey(NBT_PENDING_OPERATION))
			return ToolOperation.NORMAL;

		long age = Math.abs(world.getTotalWorldTime() - nbt.getLong(NBT_PENDING_TIME));

		boolean matches =
			nbt.getInteger(NBT_PENDING_X) == x &&
				nbt.getInteger(NBT_PENDING_Y) == y &&
				nbt.getInteger(NBT_PENDING_Z) == z &&
				nbt.getInteger(NBT_PENDING_DIM) == world.provider.dimensionId &&
				age <= 20L;

		int ordinal = nbt.getInteger(NBT_PENDING_OPERATION);
		clearPendingOperation(stack);

		if(!matches || ordinal < 0 || ordinal >= ToolOperation.values().length)
			return ToolOperation.NORMAL;

		return ToolOperation.values()[ordinal];
	}

	private void clearPendingOperation(ItemStack stack) {

		if(stack == null || !stack.hasTagCompound())
			return;

		NBTTagCompound nbt = stack.stackTagCompound;
		nbt.removeTag(NBT_PENDING_OPERATION);
		nbt.removeTag(NBT_PENDING_X);
		nbt.removeTag(NBT_PENDING_Y);
		nbt.removeTag(NBT_PENDING_Z);
		nbt.removeTag(NBT_PENDING_DIM);
		nbt.removeTag(NBT_PENDING_TIME);
	}

	@Override
	public boolean canHarvestBlock(Block block, ItemStack stack) {

		if(!canOperate(stack))
			return false;

		if(this.getCurrentAbility(stack) instanceof SilkAbility)
			return true;

		return getDigSpeed(stack, block, 0) > 1;
	}

	@Override
	public Multimap getItemAttributeModifiers() {

		Multimap multimap = HashMultimap.create();
		multimap.put(SharedMonsterAttributes.attackDamage.getAttributeUnlocalizedName(), new AttributeModifier(field_111210_e, "Tool modifier", (double) this.damage, 0));
		multimap.put(SharedMonsterAttributes.movementSpeed.getAttributeUnlocalizedName(), new AttributeModifier(field_111210_e, "Tool modifier", movement, 1));

		if(hasToolTrait(ToolTrait.LIGHTWEIGHT)) {
			multimap.put(
				SharedMonsterAttributes.movementSpeed.getAttributeUnlocalizedName(),
				new AttributeModifier(
					LIGHTWEIGHT_MOVEMENT_UUID,
					"Lightweight tool modifier",
					0.03D,
					1));
		}

		return multimap;
	}

	@SideOnly(Side.CLIENT)
	public boolean hasEffect(ItemStack stack) {
		return getCurrentAbility(stack) != null || stack.isItemEnchanted();
	}

	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean ext) {

		if(!this.toolTraits.isEmpty()) {
			list.add("Material traits:");

			for(ToolTrait trait : this.toolTraits)
				list.add("  " + EnumChatFormatting.AQUA + I18n.format(trait.getTranslationKey()));
		}

		if(this.breakAbility.size() > 1) {
			list.add("Abilities: ");

			for(ToolAbility ability : this.breakAbility) {

				if(ability != null) {

					if(getCurrentAbility(stack) == ability)
						list.add(" >" + EnumChatFormatting.GOLD + ability.getFullName());
					else
						list.add("  " + EnumChatFormatting.GOLD + ability.getFullName());
				}
			}

			list.add("Right click to cycle through abilities!");
			list.add("Sneak-click to turn ability off!");
		}

		if(!this.hitAbility.isEmpty()) {
			list.add("Weapon modifiers: ");

			for(WeaponAbility ability : this.hitAbility) {
				list.add("  " + EnumChatFormatting.RED + ability.getFullName());
			}
		}

		if(this.rockBreaker) {
			list.add("");
			list.add(EnumChatFormatting.RED + "Can break depth rock!");
		}
	}

	public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {

		if(world.isRemote || this.breakAbility.size() < 2 || !canOperate(stack))
			return super.onItemRightClick(stack, world, player);

		int i = getAbility(stack);
		i++;

		if(player.isSneaking())
			i = 0;

		setAbility(stack, i % this.breakAbility.size());

		while(getCurrentAbility(stack) != null && !getCurrentAbility(stack).isAllowed()) {

			PacketDispatcher.wrapper.sendTo(new PlayerInformPacket(ChatBuilder.start("[Ability ").nextTranslation(getCurrentAbility(stack).getName()).next(getCurrentAbility(stack).getExtension() + " is blacklisted!]").colorAll(EnumChatFormatting.RED).flush(), MainRegistry.proxy.ID_TOOLABILITY), (EntityPlayerMP) player);


			i++;
			setAbility(stack, i % this.breakAbility.size());
		}

		if(getCurrentAbility(stack) != null) {
			PacketDispatcher.wrapper.sendTo(new PlayerInformPacket(ChatBuilder.start("[Enabled ").nextTranslation(getCurrentAbility(stack).getName()).next(getCurrentAbility(stack).getExtension() + "]").colorAll(EnumChatFormatting.YELLOW).flush(), MainRegistry.proxy.ID_TOOLABILITY), (EntityPlayerMP) player);
		} else {
			PacketDispatcher.wrapper.sendTo(new PlayerInformPacket(ChatBuilder.start("[Tool ability deactivated]").color(EnumChatFormatting.GOLD).flush(), MainRegistry.proxy.ID_TOOLABILITY), (EntityPlayerMP) player);
		}

		world.playSoundAtEntity(player, "random.orb", 0.25F, getCurrentAbility(stack) == null ? 0.75F : 1.25F);

		return stack;
	}

	private ToolAbility getCurrentAbility(ItemStack stack) {
		int ability = getAbility(stack) % this.breakAbility.size();
		return this.breakAbility.get(ability);
	}

	private int getAbility(ItemStack stack) {

		if(stack.hasTagCompound())
			return stack.stackTagCompound.getInteger("ability");

		return 0;
	}

	private void setAbility(ItemStack stack, int ability) {

		if(!stack.hasTagCompound())
			stack.stackTagCompound = new NBTTagCompound();

		stack.stackTagCompound.setInteger("ability", ability);
	}

	public boolean canOperate(ItemStack stack) {
		return true;
	}

	public ItemToolAbility setDepthRockBreaker() {
		this.rockBreaker = true;
		return this;
	}

	private boolean rockBreaker = false;

	@Override
	public boolean canBreakRock(World world, EntityPlayer player, ItemStack tool, Block block, int x, int y, int z) {
		return canOperate(tool) && this.rockBreaker;
	}

	@Override
	public boolean isShears(ItemStack stack) {
		return this.isShears;
	}
}
