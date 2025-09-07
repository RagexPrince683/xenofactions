package com.hfr.command;

//import com.yourmod.imageblock.ImagePlacerUtil;
import com.hfr.inventory.gui.GuiCustomImageAdd;
import com.hfr.util.ImagePlacerUtil;
//import com.yourmod.imageblock.storage.CustomImageStorage;
import com.hfr.data.CustomImageStorage;
import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.World;

import java.util.List;
import java.util.UUID;

public class CommandXCustomImage extends CommandBase {
    @Override
    public String getCommandName() { return "xcustomimage"; }
    @Override
    public String getCommandUsage(ICommandSender sender) { return "/xcustomimage <add|delete|list> ..."; } //place
    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        if (!(sender instanceof EntityPlayerMP)) {
            sender.addChatMessage(new ChatComponentText("Only players may use this command."));
            return;
        }
        EntityPlayerMP player = (EntityPlayerMP) sender;
        World world = player.worldObj;
        CustomImageStorage storage = CustomImageStorage.get(world);

        if (args.length == 0) {
            player.addChatMessage(new ChatComponentText(getCommandUsage(sender)));
            return;
        }

        String sub = args[0];
        if ("add".equalsIgnoreCase(sub)) {
            if (args.length < 2) {
                player.addChatMessage(new ChatComponentText("Usage: /xcustomimage add <name>"));
                return;
            }
            String name = args[1];
            if (player instanceof EntityPlayerMP) {
                // open the GUI client-side
                Minecraft.getMinecraft().displayGuiScreen(new GuiCustomImageAdd(name));
            }
        } else if ("delete".equalsIgnoreCase(sub)) {
            if (args.length < 2) {
                player.addChatMessage(new ChatComponentText("Usage: /xcustomimage delete <index>"));
                return;
            }
            int idx = parseIntWithMin(sender, args[1], 0);
            boolean ok = storage.deleteImage(player.getUniqueID(), idx);
            player.addChatMessage(new ChatComponentText(ok ? "Deleted." : "Delete failed."));
        } else if ("list".equalsIgnoreCase(sub)) {
            List<NBTTagCompound> list = storage.getList(player.getUniqueID());
            if (list.isEmpty()) {
                player.addChatMessage(new ChatComponentText("No custom images."));
            } else {
                for (int i = 0; i < list.size(); i++) {
                    NBTTagCompound c = list.get(i);
                    player.addChatMessage(new ChatComponentText("[" + i + "] " + c.getString("name") + " -> " + c.getString("url")));
                }
            }
        }
        //else if ("place".equalsIgnoreCase(sub)) {
        //    if (args.length < 2) {
        //        player.addChatMessage(new ChatComponentText("Usage: /xcustomimage place <name>"));
        //        return;
        //    }
        //    String name = args[1];
        //    // find image by name
        //    List<NBTTagCompound> list = storage.getList(player.getUniqueID());
        //    NBTTagCompound chosen = null;
        //    for (NBTTagCompound c : list) if (name.equals(c.getString("name"))) { chosen = c; break; }
        //    if (chosen == null) {
        //        player.addChatMessage(new ChatComponentText("Image name not found."));
        //        return;
        //    }
        //    // attempt to place at target block face
        //    boolean placed = ImagePlacerUtil.placeWallImageAtLook(player, chosen.getString("url"), player.getUniqueID().toString(), name);
        //    player.addChatMessage(new ChatComponentText(placed ? "Placed." : "Place failed (no target or blocked)."));
        //}
        else {
            player.addChatMessage(new ChatComponentText(getCommandUsage(sender)));
        }
    }
}
