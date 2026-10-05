package com.hfr.command;

import com.hfr.blocks.ModBlocks;
import com.hfr.blocks.machine.MachineMarket.TileEntityMarket;
import com.hfr.data.MarketData;
import com.hfr.data.MarketData.Shop;
import com.hfr.shop.XShopService;
import java.util.*;
import net.minecraft.command.*;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;

public final class CommandXShop extends CommandBase {
    private static final String[] COMMANDS = { "create", "delete", "rename", "list", "edit", "set", "link", "unlink", "add", "removeoffer", "help" };
    public String getCommandName() { return "xshop"; }
    public List getCommandAliases() { return Collections.singletonList("shop"); }
    public int getRequiredPermissionLevel() { return 3; }
    public String getCommandUsage(ICommandSender sender) { return "/xshop <create|delete|rename|list|edit|set|link|unlink|add|removeoffer|help>"; }
    private static void say(ICommandSender sender, String message) { sender.addChatMessage(new ChatComponentText("[XShop] " + message)); }
    private static String join(String[] args, int start) {
        StringBuilder result = new StringBuilder();
        for (int i = start; i < args.length; i++) { if (i > start) result.append(' '); result.append(args[i]); }
        return result.toString();
    }
    private static Shop shop(String reference) {
        Shop shop = MarketData.resolve(reference);
        if (shop == null) throw new IllegalArgumentException("Unknown shop: " + reference);
        return shop;
    }
    private static void length(String[] args, int count, String usage) {
        if (args.length != count) throw new IllegalArgumentException("Usage: " + usage);
    }
    private static boolean flag(String value) {
        if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("on")) return true;
        if (value.equalsIgnoreCase("false") || value.equalsIgnoreCase("off")) return false;
        throw new IllegalArgumentException("Use on/off or true/false.");
    }
    @Override public void processCommand(ICommandSender sender, String[] args) {
        if (!sender.canCommandSenderUseCommand(3, getCommandName())) throw new CommandException("commands.generic.permission");
        if (args.length == 0 || args[0].equalsIgnoreCase("help") || args[0].equalsIgnoreCase("man")) {
            say(sender, "/xshop create <name> | delete <shop> | rename <ID/name> <new name> | list [page] | edit <shop>");
            say(sender, "/xshop set <ID/name> <enabled|visible|adminOnly> <on|off> | link <ID/name> <x> <y> <z> | unlink <x> <y> <z>");
            say(sender, "/xshop add <shop>: hotbar slot 1 sells; slots 2-4 set costs. removeoffer <ID/name> <index> removes an offer.");
            say(sender, "Legacy /xshop delete <index> <shop> still removes an offer. Use IDs when a shop name contains spaces.");
            return;
        }
        try {
            String cmd = args[0].toLowerCase(Locale.ROOT);
            if (cmd.equals("list")) {
                if (args.length > 2) throw new IllegalArgumentException("Usage: /xshop list [page]");
                List<Shop> shops = MarketData.list(); int pages = Math.max(1, (shops.size() + 9) / 10);
                int page = args.length == 2 ? parseIntBounded(sender, args[1], 1, pages) : 1;
                say(sender, "Shops " + page + "/" + pages + ":");
                for (int i = (page - 1) * 10; i < Math.min(shops.size(), page * 10); i++) {
                    Shop shop = shops.get(i);
                    say(sender, shop.displayName + " [" + shop.id + "] offers=" + shop.offerCount()
                        + " enabled=" + shop.enabled + " market=" + shop.visibleInFactionTerminal + " adminOnly=" + shop.adminOnly);
                }
                return;
            }
            if (args.length < 2) throw new IllegalArgumentException(getCommandUsage(sender));
            if (cmd.equals("create")) {
                Shop shop = MarketData.create(join(args, 1)); say(sender, "Created " + shop.displayName + " [" + shop.id + "]"); return;
            }
            if (cmd.equals("delete")) {
                // Keep the legacy three-argument offer removal unambiguous for ordinary names and IDs.
                if (args.length >= 3 && args[1].matches("[0-9]+")) {
                    Shop shop = shop(join(args, 2)); MarketData.removeOffer(shop.id, parseIntWithMin(sender, args[1], 0));
                    say(sender, "Offer removed from " + shop.displayName); return;
                }
                Shop shop = shop(join(args, 1)); MarketData.delete(shop.id);
                say(sender, "Deleted " + shop.displayName + ". Linked blocks are now inactive."); return;
            }
            if (cmd.equals("rename")) {
                if (args.length < 3) throw new IllegalArgumentException("Usage: /xshop rename <ID/name> <new name>");
                Shop shop = shop(args[1]); MarketData.configure(shop.id, join(args, 2), shop.enabled, shop.visibleInFactionTerminal, shop.adminOnly);
                say(sender, "Renamed shop; its ID and all block references are preserved."); return;
            }
            if (cmd.equals("set")) {
                length(args, 4, "/xshop set <ID/name> <enabled|visible|adminOnly> <on|off>");
                Shop shop = shop(args[1]); boolean value = flag(args[3]);
                String key = args[2].toLowerCase(Locale.ROOT);
                if (!key.equals("enabled") && !key.equals("visible") && !key.equals("adminonly")) throw new IllegalArgumentException("Choose enabled, visible, or adminOnly.");
                MarketData.configure(shop.id, shop.displayName, key.equals("enabled") ? value : shop.enabled,
                    key.equals("visible") ? value : shop.visibleInFactionTerminal, key.equals("adminonly") ? value : shop.adminOnly);
                say(sender, "Shop setting updated."); return;
            }
            if (cmd.equals("removeoffer")) {
                length(args, 3, "/xshop removeoffer <ID/name> <index>");
                Shop shop = shop(args[1]); MarketData.removeOffer(shop.id, parseIntWithMin(sender, args[2], 0)); say(sender, "Offer removed."); return;
            }
            EntityPlayerMP player = getCommandSenderAsPlayer(sender);
            if (cmd.equals("edit")) { XShopService.edit(player, shop(join(args, 1))); return; }
            if (cmd.equals("add")) {
                Shop shop = MarketData.resolve(join(args, 1));
                // Preserve legacy add-to-a-new-name workflow, using the same canonical registry.
                if (shop == null) {
                    net.minecraft.item.ItemStack[] items = XShopService.hotbar(player);
                    if (items[0] == null || items[1] == null) throw new IllegalArgumentException("Hotbar slots 1 and 2 require the sold item and currency.");
                    shop = MarketData.create(join(args, 1));
                }
                MarketData.addOffer(shop.id, XShopService.hotbar(player)); say(sender, "Offer added to " + shop.displayName); return;
            }
            if (cmd.equals("link") || cmd.equals("unlink")) {
                boolean link = cmd.equals("link"); length(args, link ? 5 : 4, link ? "/xshop link <ID/name> <x> <y> <z>" : "/xshop unlink <x> <y> <z>");
                int offset = link ? 2 : 1;
                int x = parseIntBounded(sender, args[offset], -30000000, 30000000);
                int y = parseIntBounded(sender, args[offset + 1], 0, 255);
                int z = parseIntBounded(sender, args[offset + 2], -30000000, 30000000);
                if (!player.worldObj.blockExists(x, y, z)) throw new IllegalArgumentException("The target chunk must be loaded.");
                TileEntity raw = player.worldObj.getTileEntity(x, y, z);
                if (player.worldObj.getBlock(x, y, z) != ModBlocks.machine_market || !(raw instanceof TileEntityMarket))
                    throw new IllegalArgumentException("Target an admin XShop block in your dimension.");
                ((TileEntityMarket)raw).link(link ? shop(args[1]).id : ""); say(sender, link ? "Block linked." : "Block unlinked."); return;
            }
            throw new IllegalArgumentException(getCommandUsage(sender));
        } catch (IllegalArgumentException | IllegalStateException e) { say(sender, e.getMessage()); }
    }
    @Override public List addTabCompletionOptions(ICommandSender sender, String[] args) {
        if (!sender.canCommandSenderUseCommand(3, "xshop")) return null;
        if (args.length == 1) return getListOfStringsMatchingLastWord(args, COMMANDS);
        String cmd = args[0].toLowerCase(Locale.ROOT);
        if (cmd.equals("set") && args.length == 3) return getListOfStringsMatchingLastWord(args, "enabled", "visible", "adminOnly");
        if (cmd.equals("set") && args.length == 4) return getListOfStringsMatchingLastWord(args, "on", "off", "true", "false");
        if (cmd.equals("list") && args.length == 2) {
            List<String> pages = new ArrayList<String>();
            for (int i = 1; i <= Math.max(1, (MarketData.list().size() + 9) / 10); i++) pages.add(String.valueOf(i));
            return getListOfStringsFromIterableMatchingLastWord(args, pages);
        }
        if (cmd.equals("removeoffer") && args.length == 3) {
            Shop shop = MarketData.resolve(args[1]); List<String> indices = new ArrayList<String>();
            if (shop != null) for (int i = 0; i < shop.offerCount(); i++) indices.add(String.valueOf(i));
            return getListOfStringsFromIterableMatchingLastWord(args, indices);
        }
        int coordinate = cmd.equals("link") ? args.length - 3 : cmd.equals("unlink") ? args.length - 2 : -1;
        if (coordinate >= 0 && coordinate < 3) {
            net.minecraft.util.ChunkCoordinates position = sender.getPlayerCoordinates();
            return getListOfStringsMatchingLastWord(args, String.valueOf(coordinate == 0 ? position.posX : coordinate == 1 ? position.posY : position.posZ));
        }
        if ((args.length == 2 && Arrays.asList("delete", "rename", "edit", "set", "link", "add", "removeoffer").contains(cmd))
            || (cmd.equals("delete") && args.length >= 3 && args[1].matches("[0-9]+"))) {
            List<String> references = new ArrayList<String>();
            for (Shop shop : MarketData.list()) { references.add(shop.id); if (!shop.displayName.contains(" ")) references.add(shop.displayName); }
            return getListOfStringsFromIterableMatchingLastWord(args, references);
        }
        return null;
    }
}
