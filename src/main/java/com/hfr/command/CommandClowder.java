package com.hfr.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;

import com.hfr.blocks.BlockDummyable;
import com.hfr.blocks.ModBlocks;
import com.hfr.clowder.Clowder;
import com.hfr.clowder.Clowder.ScheduledTeleport;
import com.hfr.clowder.ClowderFlag;
import com.hfr.clowder.flag.CustomFlagService;
import com.hfr.config.XFConfig;
import com.hfr.guide.XFGuideBook;
import com.hfr.clowder.CityLevel;
import com.hfr.clowder.ClowderTerritory;
import com.hfr.clowder.ClowderTerritory.Ownership;
import com.hfr.clowder.ClowderTerritory.TerritoryMeta;
import com.hfr.clowder.ClowderTerritory.Zone;
import com.hfr.clowder.events.RegionOwnershipChangedEvent;
import com.hfr.data.ClowderData;
import com.hfr.items.ModItems;
import com.hfr.main.MainRegistry;
import com.hfr.packet.PacketDispatcher;
import com.hfr.packet.effect.ClowderFlagPacket;
import com.hfr.tileentity.clowder.TileEntityFlag;
import com.hfr.tileentity.clowder.TileEntityFlagBig;
import com.hfr.tileentity.prop.TileEntityProp;
import com.hfr.util.ParserUtil;
import com.hfr.clowder.ClowderTerritory.CoordPair;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import net.minecraft.block.Block;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.util.ForgeDirection;

import static com.hfr.main.MainRegistry.sub;

public class CommandClowder extends CommandBase {

	private static final HashMap<String, Long> CITY_UPGRADE_CONFIRMATIONS = new HashMap();
	private static final HashMap<String, WarConfirmation> WAR_CONFIRMATIONS = new HashMap();


	//private static final CoordPair wbpospos = new CoordPair(4274, 1335);
	//private static final CoordPair wbnegneg = new CoordPair(2278, 4);

	//private boolean insideBorders(CoordPair flagLoc) {
	//	return flagLoc.x >= wbnegneg.x && flagLoc.x <= wbpospos.x && flagLoc.z >= wbnegneg.z && flagLoc.z <= wbpospos.z;
	//}

	@Override
	public String getCommandName() {
		return "clowder";
	}

	@Override
	public List getCommandAliases() {
		return new ArrayList() {
			{
				add("clowder");
				add("c");
			}
		};
	}

	@Override
	public String getCommandUsage(ICommandSender sender) {
		return "/clowder help";
	}

	public boolean canCommandSenderUseCommand(ICommandSender p_71519_1_)
	{ //2THIS DOES!!! well probably plus that, whatever!
		return true;
	}

	@Override
	public int getRequiredPermissionLevel() {
		return 0; // Allows all players to execute the command
		//1WRONG
	}

	//fuck bob and his entire ass spaghetti monster of a command system


	//todone fix
	//private String sanitizeFactionName(String name) {
	//	String sanitized = name;
	//	for (Map.Entry<String, String> entry : sub.entrySet()) {
	//		sanitized = sanitized.replaceAll(entry.getKey(), entry.getValue());
	//	}
	//	return sanitized;
	//}

	private boolean containsBannedWord(String name) {
		for (String pattern : sub.keySet()) {
			if (name.toLowerCase().matches(".*" + pattern + ".*")) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void processCommand(ICommandSender sender, String[] args) {

		//todo add a /c merge command to merge into other factions (with consent ofc)

		if(Clowder.clowders.size() == 0)
			ClowderData.getData(sender.getEntityWorld());

		if(args.length < 1) {
			sender.addChatMessage(new ChatComponentText(ERROR + getCommandUsage(sender)));
			return;
		}

		String cmd = args[0].toLowerCase();

		if(cmd.equals("help") || cmd.equals("man")) {
			cmdHelp(sender, args.length > 1 ? args[1] : "1");
			return;
		}

		if(cmd.equals("create")) {
			if(!requireArgs(sender, cmd, args, 2)) return;
			String rawName = joinArgs(args, 1);

			if(containsBannedWord(rawName)) {
				sender.addChatMessage(new ChatComponentText(ERROR + "That faction name is not allowed."));
				return;
			}

			cmdCreate(sender, rawName);
			return;
		}

		if(cmd.equals("merge")) { if(!requireArgs(sender, cmd, args, 2)) return; cmdMerge(sender, joinArgs(args, 1)); return; }
		if(cmd.equals("acceptmerge")) { if(!requireArgs(sender, cmd, args, 2)) return; acceptMerge(sender, joinArgs(args, 1)); return; }
		if(cmd.equals("gracebuild")) { cmdGraceBuild(sender); return; }
		if(cmd.equals("comrades")) { cmdComrades(sender); return; }
		if(cmd.equals("alliance") || cmd.equals("allies") || cmd.equals("allylist")) { cmdAlliance(sender); return; }
		if(cmd.equals("color")) { if(!requireArgs(sender, cmd, args, 2)) return; cmdColor(sender, args[1]); return; }

		if(cmd.equals("info")) {
			cmdInfo(sender, args.length > 1 ? joinArgs(args, 1) : null);
			return;
		}

		if(cmd.equals("rename")) { if(!requireArgs(sender, cmd, args, 2)) return; cmdRename(sender, joinArgs(args, 1)); return; }
		if(cmd.equals("list")) { cmdList(sender); return; }
		if(cmd.equals("motd")) { if(!requireArgs(sender, cmd, args, 2)) return; cmdMOTD(sender, args); return; }
		if(cmd.equals("owner")) { if(!requireArgs(sender, cmd, args, 2)) return; cmdOwner(sender, args[1]); return; }
		if(cmd.equals("apply")) { if(!requireArgs(sender, cmd, args, 2)) return; cmdApply(sender, joinArgs(args, 1)); return; }
		if(cmd.equals("leave")) { cmdLeave(sender); return; }
		if(cmd.equals("accept")) { if(!requireArgs(sender, cmd, args, 2)) return; cmdAccept(sender, args[1]); return; }
		if(cmd.equals("befriend") || cmd.equals("ally")) { if(!requireArgs(sender, cmd, args, 2)) return; cmdBefriend(sender, joinArgs(args, 1)); return; }
		if(cmd.equals("acceptfriend") || cmd.equals("acceptally")) { if(!requireArgs(sender, cmd, args, 2)) return; cmdAcceptFriend(sender, args[1]); return; }
		if(cmd.equals("deny")) { if(!requireArgs(sender, cmd, args, 2)) return; cmdDeny(sender, args[1]); return; }
		if(cmd.equals("applicants")) { cmdApplicants(sender); return; }
		if(cmd.equals("kick")) { if(!requireArgs(sender, cmd, args, 2)) return; cmdKick(sender, args[1]); return; }
		if(cmd.equals("unfriend") || cmd.equals("unally")) { if(!requireArgs(sender, cmd, args, 2)) return; cmdUnfriend(sender, joinArgs(args, 1)); return; }

		if(cmd.equals("listflags")) {
			cmdListflags(sender, args.length > 1 ? args[1] : "1");
			return;
		}

		if(cmd.equals("flag")) { if(!requireArgs(sender, cmd, args, 2)) return; cmdFlag(sender, args); return; }
		if(cmd.equals("retreat")) { cmdRetreat(sender); return; }
		if(cmd.equals("sethome")) { cmdSethome(sender); return; }
		if(cmd.equals("setallywarp")) { cmdSetAllyWarp(sender); return; }
		if(cmd.equals("home")) { cmdHome(sender); return; }
		if(cmd.equals("allywarp")) { if(!requireArgs(sender, cmd, args, 2)) return; cmdAllyWarp(sender, joinArgs(args, 1)); return; }
		if(cmd.equals("addwarp") || cmd.equals("setwarp")) { if(!requireArgs(sender, cmd, args, 2)) return; cmdAddWarp(sender, args[1]); return; }
		if(cmd.equals("delwarp")) { if(!requireArgs(sender, cmd, args, 2)) return; cmdDelWarp(sender, args[1]); return; }
		if(cmd.equals("warp")) { if(!requireArgs(sender, cmd, args, 2)) return; cmdWarp(sender, args[1]); return; }
		if(cmd.equals("warps")) { cmdWarps(sender); return; }
		if(cmd.equals("balance")) { cmdBalance(sender); return; }
		if(cmd.equals("deposit")) { if(!requireArgs(sender, cmd, args, 2)) return; cmdDeposit(sender, args[1]); return; }
		if(cmd.equals("withdraw")) { if(!requireArgs(sender, cmd, args, 2)) return; sender.addChatMessage(new ChatComponentText(CRITICAL + "This command is currently disabled!")); return; }

		if(cmd.equals("claim") || cmd.equals("city")) {
			if(args.length > 1 && args[1].equalsIgnoreCase("upgrade")) {
				cmdCityUpgrade(sender);
			} else if(args.length > 1) {
				cmdClaim(sender, joinArgs(args, 1));
			} else {
				sender.addChatMessage(new ChatComponentText(ERROR + "Invalid format. Usage: /c claim <city name> or /c city upgrade"));
			}
			return;
		}

		if(cmd.equals("promote")) { if(!requireArgs(sender, cmd, args, 2)) return; cmdPromote(sender, args[1]); return; }
		if(cmd.equals("demote")) { if(!requireArgs(sender, cmd, args, 2)) return; cmdDemote(sender, args[1]); return; }
		if(cmd.equals("nameclaim")) { if(!requireArgs(sender, cmd, args, 2)) return; cmdNameClaim(sender, joinArgs(args, 1)); return; }
		if(cmd.equals("declarewar") || cmd.equals("war")) { if(!requireArgs(sender, cmd, args, 2)) return; cmdDeclareWar(sender, joinArgs(args, 1)); return; }
		if(cmd.equals("listwars")) { cmdListWars(sender); return; }
		if(cmd.equals("peace")) {
			if(!requireArgs(sender, cmd, args, 2)) return;
			// Faction names containing spaces should be written with underscores when a transfer city is supplied.
			String city = args.length > 2 ? joinArgs(args, 2) : null;
			cmdRequestPeace(sender, args[1], city);
			return;
		}
		if(cmd.equals("acceptpeace")) { if(!requireArgs(sender, cmd, args, 2)) return; cmdAcceptPeace(sender, joinArgs(args, 1)); return; }
		if(cmd.equals("ceasefire")) { if(!requireArgs(sender, cmd, args, 2)) return; cmdRequestCeasefire(sender, joinArgs(args, 1)); return; }
		if(cmd.equals("acceptceasefire")) { if(!requireArgs(sender, cmd, args, 2)) return; cmdAcceptCeasefire(sender, joinArgs(args, 1)); return; }
		if(cmd.equals("surrender")) { if(!requireArgs(sender, cmd, args, 2)) return; cmdSurrender(sender, joinArgs(args, 1)); return; }
		if(cmd.equals("acceptsurrender")) { if(!requireArgs(sender, cmd, args, 2)) return; cmdAcceptSurrender(sender, joinArgs(args, 1)); return; }
		if(cmd.equals("defendally")) { if(!requireArgs(sender, cmd, args, 2)) return; cmdDefendAlly(sender, joinArgs(args, 1)); return; }

		sender.addChatMessage(new ChatComponentText(ERROR + "Unknown command. Usage: " + getCommandUsage(sender)));
	}

	private boolean requireArgs(ICommandSender sender, String cmd, String[] args, int minArgs) {
		if(args.length >= minArgs)
			return true;
		sender.addChatMessage(new ChatComponentText(ERROR + "Invalid format. Usage: " + getUsageFor(cmd)));
		return false;
	}

	private String joinArgs(String[] args, int start) {
		return String.join(" ", Arrays.copyOfRange(args, start, args.length));
	}

	private String getUsageFor(String cmd) {
		if(cmd.equals("create")) return "/c create <name>";
		if(cmd.equals("merge")) return "/c merge <faction>";
		if(cmd.equals("acceptmerge")) return "/c acceptmerge <faction>";
		if(cmd.equals("color")) return "/c color <hexadecimal>";
		if(cmd.equals("rename")) return "/c rename <name>";
		if(cmd.equals("motd")) return "/c motd <message>";
		if(cmd.equals("owner")) return "/c owner <player>";
		if(cmd.equals("apply")) return "/c apply <faction>";
		if(cmd.equals("accept")) return "/c accept <player>";
		if(cmd.equals("befriend") || cmd.equals("ally")) return "/c befriend <faction>";
		if(cmd.equals("acceptfriend") || cmd.equals("acceptally")) return "/c acceptfriend <player>";
		if(cmd.equals("deny")) return "/c deny <player>";
		if(cmd.equals("kick")) return "/c kick <player>";
		if(cmd.equals("unfriend") || cmd.equals("unally")) return "/c unfriend <faction>";
		if(cmd.equals("flag")) return "/c flag <flag>|seturl <postimages URL>|clear|reload";
		if(cmd.equals("allywarp")) return "/c allywarp <faction>";
		if(cmd.equals("addwarp") || cmd.equals("setwarp")) return "/c setwarp <name>";
		if(cmd.equals("delwarp")) return "/c delwarp <name>";
		if(cmd.equals("warp")) return "/c warp <name>";
		if(cmd.equals("deposit")) return "/c deposit <amount>";
		if(cmd.equals("withdraw")) return "/c withdraw <amount>";
		if(cmd.equals("promote")) return "/c promote <player>";
		if(cmd.equals("demote")) return "/c demote <player>";
		if(cmd.equals("nameclaim")) return "/c nameclaim <name>";
		if(cmd.equals("declarewar") || cmd.equals("war")) return "/c declarewar <faction>";
		if(cmd.equals("listwars")) return "/c listwars";
		if(cmd.equals("peace")) return "/c peace <faction> {city}";
		if(cmd.equals("acceptpeace")) return "/c acceptpeace <faction>";
		if(cmd.equals("ceasefire")) return "/c ceasefire <faction>";
		if(cmd.equals("acceptceasefire")) return "/c acceptceasefire <faction>";
		if(cmd.equals("surrender")) return "/c surrender <faction>";
		if(cmd.equals("acceptsurrender")) return "/c acceptsurrender <faction>";
		if(cmd.equals("defendally")) return "/c defendally <ally>";
		return getCommandUsage(null);
	}

	private void cmdHelp(ICommandSender sender, String page) {

		int p = this.parseInt(sender, page);
		int pages = 6;

		if(p < 1 || p > pages)
			p = 1;

		sender.addChatMessage(new ChatComponentText(HELP + "/clowder [command] <args...> {optional args...}"));
		sender.addChatMessage(new ChatComponentText(INFO + "Commands [" + p + "/" + pages + "]:"));

		if(p == 1) {
			sender.addChatMessage(new ChatComponentText(TITLE + "Basics & faction info"));
			sender.addChatMessage(new ChatComponentText(COMMAND + "-help {page}" + TITLE + " - Shows these help pages"));
			sender.addChatMessage(new ChatComponentText(INFO + "Handbook: " + XFGuideBook.getFallbackHelp()));
			sender.addChatMessage(new ChatComponentText(COMMAND + "-create <name>" + TITLE + " - Creates a faction"));
			sender.addChatMessage(new ChatComponentText(COMMAND + "-info {faction}" + TITLE + " - Shows info on your faction or another faction"));
			sender.addChatMessage(new ChatComponentText(COMMAND + "-list" + TITLE + " - Lists all factions"));
			sender.addChatMessage(new ChatComponentText(COMMAND + "-comrades" + TITLE + " - Shows all members of your faction"));
			sender.addChatMessage(new ChatComponentText(COMMAND + "-alliance/allies" + TITLE + " - Shows all allied factions"));
			sender.addChatMessage(new ChatComponentText(COMMAND + "-leave" + TITLE + " - Leaves your faction"));
			sender.addChatMessage(new ChatComponentText(INFO + "Tip: faction spaces are saved as underscores; use underscores in commands."));
			sender.addChatMessage(new ChatComponentText(INFO + "/clowder help 2"));
		}

		if(p == 2) {
			sender.addChatMessage(new ChatComponentText(TITLE + "Joining & member management"));
			sender.addChatMessage(new ChatComponentText(COMMAND + "-apply <faction>" + TITLE + " - Applies to join a faction"));
			sender.addChatMessage(new ChatComponentText(COMMAND_ADMIN + "-applicants" + TITLE + " - Lists faction applications"));
			sender.addChatMessage(new ChatComponentText(COMMAND_ADMIN + "-accept <player>" + TITLE + " - Accepts a player's application"));
			sender.addChatMessage(new ChatComponentText(COMMAND_ADMIN + "-deny <player>" + TITLE + " - Denies a player's application"));
			sender.addChatMessage(new ChatComponentText(COMMAND_ADMIN + "-kick <player>" + TITLE + " - Kicks a member"));
			sender.addChatMessage(new ChatComponentText(COMMAND_ADMIN + "-promote <player>" + TITLE + " - Promotes a member to officer"));
			sender.addChatMessage(new ChatComponentText(COMMAND_ADMIN + "-demote <player>" + TITLE + " - Demotes an officer to member"));
			sender.addChatMessage(new ChatComponentText(COMMAND_LEADER + "-owner <player>" + TITLE + " - Transfers faction ownership"));
			sender.addChatMessage(new ChatComponentText(INFO + "/clowder help 3"));
		}

		if(p == 3) {
			sender.addChatMessage(new ChatComponentText(TITLE + "Faction settings & appearance"));
			sender.addChatMessage(new ChatComponentText(COMMAND_LEADER + "-rename <name>" + TITLE + " - Renames your faction"));
			sender.addChatMessage(new ChatComponentText(COMMAND_LEADER + "-color <hexadecimal>" + TITLE + " - Sets the faction color"));
			sender.addChatMessage(new ChatComponentText(COMMAND_LEADER + "-motd <message>" + TITLE + " - Sets the faction MotD"));
			sender.addChatMessage(new ChatComponentText(COMMAND + "-listflags {page}" + TITLE + " - Lists available flags"));
			sender.addChatMessage(new ChatComponentText(COMMAND_LEADER + "-flag <flag>" + TITLE + " - Sets the faction flag"));
			sender.addChatMessage(new ChatComponentText(COMMAND_ADMIN + "-flag seturl <postimages URL>" + TITLE + " - Imports a safe custom faction flag"));
			sender.addChatMessage(new ChatComponentText(COMMAND_LEADER + "-gracebuild" + TITLE + " - Activates one-time 48h faction build grace"));
			sender.addChatMessage(new ChatComponentText(INFO + "/clowder help 4"));
		}

		if(p == 4) {
			sender.addChatMessage(new ChatComponentText(TITLE + "Homes, warps & cities"));
			sender.addChatMessage(new ChatComponentText(COMMAND_LEADER + "-sethome" + TITLE + " - Sets the faction home"));
			sender.addChatMessage(new ChatComponentText(COMMAND + "-home" + TITLE + " - Teleports to the faction home"));
			sender.addChatMessage(new ChatComponentText(COMMAND + "-setwarp <name>" + TITLE + " - Creates a faction warp"));
			sender.addChatMessage(new ChatComponentText(COMMAND + "-delwarp <name>" + TITLE + " - Removes a faction warp"));
			sender.addChatMessage(new ChatComponentText(COMMAND + "-warp <name>" + TITLE + " - Teleports to a faction warp"));
			sender.addChatMessage(new ChatComponentText(COMMAND + "-warps" + TITLE + " - Lists faction warps"));
			sender.addChatMessage(new ChatComponentText(COMMAND + "-claim <city name>" + TITLE + " - Creates a named City Center"));
			sender.addChatMessage(new ChatComponentText(COMMAND_LEADER + "-city upgrade" + TITLE + " - Upgrades the City Center for your current claim"));
			sender.addChatMessage(new ChatComponentText(COMMAND_LEADER + "-nameclaim <name>" + TITLE + " - Renames the current territory"));
			sender.addChatMessage(new ChatComponentText(INFO + "/clowder help 5"));
		}

		if(p == 5) {
			sender.addChatMessage(new ChatComponentText(TITLE + "Prestige, alliances & merges"));
			sender.addChatMessage(new ChatComponentText(COMMAND + "-balance" + TITLE + " - Shows faction prestige"));
			sender.addChatMessage(new ChatComponentText(COMMAND + "-deposit <amount>" + TITLE + " - Turns prestige items into digiprestige"));
			sender.addChatMessage(new ChatComponentText(COMMAND + "-withdraw <amount>" + TITLE + " - Withdraws digiprestige as prestige items"));
			sender.addChatMessage(new ChatComponentText(COMMAND_LEADER + "-befriend <faction>" + TITLE + " - Sends an alliance offer"));
			sender.addChatMessage(new ChatComponentText(COMMAND_LEADER + "-acceptfriend <player>" + TITLE + " - Accepts an alliance offer"));
			sender.addChatMessage(new ChatComponentText(COMMAND_LEADER + "-unfriend <faction>" + TITLE + " - Cancels an alliance"));
			sender.addChatMessage(new ChatComponentText(COMMAND_LEADER + "-setallywarp" + TITLE + " - Sets the alliance rally point"));
			sender.addChatMessage(new ChatComponentText(COMMAND + "-allywarp <faction>" + TITLE + " - Teleports to an ally rally point"));
			sender.addChatMessage(new ChatComponentText(COMMAND_LEADER + "-merge <faction>" + TITLE + " - Requests to merge into another faction"));
			sender.addChatMessage(new ChatComponentText(COMMAND_LEADER + "-acceptmerge <faction>" + TITLE + " - Accepts a merge request"));
			sender.addChatMessage(new ChatComponentText(INFO + "/clowder help 6"));
		}

		if(p == 6) {
			sender.addChatMessage(new ChatComponentText(TITLE + "War & related utility commands"));
			sender.addChatMessage(new ChatComponentText(COMMAND_LEADER + "-declarewar <faction>" + TITLE + " - Declares war on another faction"));
			sender.addChatMessage(new ChatComponentText(COMMAND + "-listwars" + TITLE + " - Lists your faction active wars"));
			sender.addChatMessage(new ChatComponentText(COMMAND_LEADER + "-peace <faction> {city}" + TITLE + " - Proposes peace, optionally transferring a city"));
			sender.addChatMessage(new ChatComponentText(COMMAND_LEADER + "-acceptpeace <faction>" + TITLE + " - Accepts a peace proposal"));
			sender.addChatMessage(new ChatComponentText(COMMAND_LEADER + "-ceasefire <faction>" + TITLE + " - Proposes a ceasefire"));
			sender.addChatMessage(new ChatComponentText(COMMAND_LEADER + "-acceptceasefire <faction>" + TITLE + " - Accepts a ceasefire"));
			sender.addChatMessage(new ChatComponentText(COMMAND_LEADER + "-surrender <faction>" + TITLE + " - Offers surrender"));
			sender.addChatMessage(new ChatComponentText(COMMAND_LEADER + "-acceptsurrender <faction>" + TITLE + " - Accepts enemy surrender"));
			sender.addChatMessage(new ChatComponentText(COMMAND_LEADER + "-defendally <ally>" + TITLE + " - Joins an ally's active wars"));
			sender.addChatMessage(new ChatComponentText(INFO + "/xmap for a claim map, /xflags for conquest flags, /xmulti for a multitool."));
		}
	}

	
	private void cmdGraceBuild(ICommandSender sender) {
		EntityPlayer player = getCommandSenderAsPlayer(sender);
		Clowder clowder = Clowder.getClowderFromPlayer(player);
		if(clowder == null) { sender.addChatMessage(new ChatComponentText(ERROR + "You are not in any faction!")); return; }
		if(clowder.getPermLevel(player.getDisplayName()) <= 2) { sender.addChatMessage(new ChatComponentText(ERROR + "Only faction leaders can activate build grace!")); return; }
		if(!XFConfig.graceBuildEnabled) { sender.addChatMessage(new ChatComponentText(ERROR + "Build grace is disabled on this server.")); return; }
		if(XFConfig.graceBuildOneTimeUse && clowder.buildGraceUsed) { sender.addChatMessage(new ChatComponentText(ERROR + "This faction already used build grace.")); return; }
		if(!clowder.activeWars.isEmpty()) { sender.addChatMessage(new ChatComponentText(ERROR + "Cannot activate while in active war.")); return; }
		clowder.buildGraceUsed = true;
		clowder.buildGraceUntil = System.currentTimeMillis() + XFConfig.graceBuildDurationMs;
		clowder.notifyAll(player.worldObj, new ChatComponentText(INFO + "Build grace activated for " + (XFConfig.graceBuildDurationMs / (60L * 60L * 1000L)) + " hours."));
		clowder.save(player.worldObj);
	}
private void cmdCreate(ICommandSender sender, String name) {

		EntityPlayer player = getCommandSenderAsPlayer(sender);
		String factionName = Clowder.canonicalizeClowderName(name);

		if(factionName.isEmpty()) { sender.addChatMessage(new ChatComponentText(ERROR + "Faction name cannot be empty.")); return; }

		if(Clowder.getClowderFromPlayer(player) == null) {

			if(Clowder.getClowderFromName(factionName) == null) {
				Clowder.createClowder(player, factionName);
				sender.addChatMessage(new ChatComponentText(TITLE + "Created faction " + factionName + "!"));
				sender.addChatMessage(new ChatComponentText(INFO + "Use /c claim to get started!"));
				sender.addChatMessage(new ChatComponentText(INFO + "and use /c sethome to set a faction home!"));
			} else {
				sender.addChatMessage(new ChatComponentText(ERROR + "This name is already taken!"));
			}

		} else {
			sender.addChatMessage(new ChatComponentText(ERROR + "You can not create a new faction while already being in one!"));
		}
	}



	private void cmdDisband(ICommandSender sender, String name) {

		EntityPlayer player = getCommandSenderAsPlayer(sender);
		Clowder clowder = Clowder.getClowderFromPlayer(player);

		if(clowder != null) {

			if(Clowder.normalizeClowderName(name).equals(Clowder.normalizeClowderName(clowder.name))) {

				if(clowder.disbandClowder(player)) {
					//wait ten minutes before allowing the player to create a new faction, or maybe just prevent them from creating a new one for a while. This is to prevent abuse of the disband command.
					//todo maybe add a cooldown system for creating new factions after disbanding?
					sender.addChatMessage(new ChatComponentText(CRITICAL + "Your faction was disbanded!"));
				} else {
					sender.addChatMessage(new ChatComponentText(ERROR + "Can not disband a faction you do not own!"));
				}

			} else {
				sender.addChatMessage(new ChatComponentText(ERROR + "Confirmation unsuccessful. Please enter the faction name to disband the faction."));
			}

		} else {
			sender.addChatMessage(new ChatComponentText(ERROR + "You are not in any faction!"));
		}
	}

	private void acceptMerge(ICommandSender sender, String name) {

		EntityPlayer player = getCommandSenderAsPlayer(sender);
		Clowder clowder = Clowder.getClowderFromPlayer(player); // receiver
		Clowder target = Clowder.getClowderFromName(name);      // requester

		if (clowder != null) {

			if (target != null) {

				if (clowder.potentialMerges.containsKey(target)) {

					// CORRECT direction: requester gets absorbed
					target.mergeWith(clowder, player.worldObj);

					clowder.potentialMerges.remove(target);

					sender.addChatMessage(new ChatComponentText(
							TITLE + "Successfully merged " + target.name + " into your faction!"
					));

				} else {
					sender.addChatMessage(new ChatComponentText(
							ERROR + "There is no pending merge request from this faction!"
					));
				}

			} else {
				sender.addChatMessage(new ChatComponentText(
						ERROR + "There is no faction with this name!"
				));
			}

		} else {
			sender.addChatMessage(new ChatComponentText(
					ERROR + "You are not in any faction!"
			));
		}
	}

	private void cmdMerge(ICommandSender sender, String name) {

		EntityPlayer player = getCommandSenderAsPlayer(sender);
		Clowder clowder = Clowder.getClowderFromPlayer(player);
		Clowder target = Clowder.getClowderFromName(name);

		if(clowder != null) {

			if(target != null) {
				//ONLY LEADERS CAN MERGE
				if(clowder.getPermLevel(player.getDisplayName()) > 2 ) {




					if(clowder != target) {

						sender.addChatMessage(new ChatComponentText("Sent merge request to " + target.name + "!"));
						target.potentialMerges.put(clowder, System.currentTimeMillis());
						//ADD A CHAT MESSAGE TO THE TARGET FACTION TELLING THEM THAT THEY HAVE A MERGE REQUEST PENDING, AND HOW TO ACCEPT IT. MAYBE ALSO ADD A TIME LIMIT ON THE MERGE REQUEST? LIKE 5 MINUTES OR SOMETHING? AFTER THAT THE REQUEST EXPIRES AND THEY HAVE TO SEND A NEW ONE IF THEY STILL WANT TO MERGE.
						target.notifyLeader(player.worldObj, new ChatComponentText(INFO + sender.getCommandSenderName() + " of " + clowder.name + " has sent a merge request! Use /c acceptmerge or /c denymerge"));

						//DO NOT DO THAT, OP ONLY, IF AT ALL
						//clowder.mergeWith(target, player.worldObj);
						//sender.addChatMessage(new ChatComponentText(TITLE + "Successfully merged with " + target.name + "!"));

					} else {
						sender.addChatMessage(new ChatComponentText(ERROR + "You cannot merge with your own faction!"));
					}

				} else {
					//non OFFICER+ BEHAVIOR
					sender.addChatMessage(new ChatComponentText(ERROR + "You do not have permission to merge the faction!"));
				}

			} else {
				sender.addChatMessage(new ChatComponentText(ERROR + "There is no faction with this name!"));
			}

		} else {
			sender.addChatMessage(new ChatComponentText(ERROR + "You are not in any faction!"));
		}
	}

	
	private void cmdComrades(ICommandSender sender) {

		EntityPlayer player = getCommandSenderAsPlayer(sender);
		Clowder clowder = Clowder.getClowderFromPlayer(player);

		if(clowder != null) {

			sender.addChatMessage(new ChatComponentText(TITLE + clowder.getDecoratedName()));

			for(String s : clowder.members.keySet())
				sender.addChatMessage(new ChatComponentText(LIST + s));

		} else {
			sender.addChatMessage(new ChatComponentText(ERROR + "You are not in any faction!"));
		}
	}

	private void cmdAlliance(ICommandSender sender) {

		EntityPlayer player = getCommandSenderAsPlayer(sender);
		Clowder clowder = Clowder.getClowderFromPlayer(player);

		if (clowder != null) {

			sender.addChatMessage(new ChatComponentText(TITLE + clowder.getDecoratedName() + " Allies:"));
			if(clowder.allies.isEmpty())
				sender.addChatMessage(new ChatComponentText(LIST + "None"));
			else
				for (Clowder s : clowder.allies.keySet())
					sender.addChatMessage(new ChatComponentText(LIST + s.name));

			if(!clowder.potentialFriends.isEmpty())
				sender.addChatMessage(new ChatComponentText(INFO + "Pending alliance offers from: " + formatStringSet(clowder.potentialFriends)));

		} else {
			sender.addChatMessage(new ChatComponentText(ERROR + "You are not in any clowder!"));
		}
	}

	private void cmdBefriend(ICommandSender sender, String name) {

		EntityPlayer envoy = getCommandSenderAsPlayer(sender);
		Clowder diplomat = Clowder.getClowderFromPlayer(envoy);

		if(diplomat != null) {

			//if(diplomat.suzerain == null)
			//{

				if(diplomat.getPermLevel(envoy.getDisplayName()) > 1) {

					Clowder toApply = Clowder.getClowderFromName(name);

					if(toApply != null) {

						if(diplomat.allies.get(toApply) == null)
						{

							diplomat.notifyAll(envoy.worldObj, new ChatComponentText(INFO + sender.getCommandSenderName() + " sent an alliance offer to " + toApply.getDecoratedName() + "!"));
							toApply.potentialFriends.add(envoy.getDisplayName());
							toApply.notifyAll(envoy.worldObj, new ChatComponentText(INFO + "Player " + sender.getCommandSenderName() + " of " + diplomat.name + " wishes to form an alliance!"));
							toApply.notifyAll(envoy.worldObj, new ChatComponentText(INFO + " Use /c acceptfriend " + sender.getCommandSenderName() + " to accept the offer."));

						} else
							sender.addChatMessage(new ChatComponentText(ERROR + "We are already allies!"));


					} else {
						sender.addChatMessage(new ChatComponentText(ERROR + "There is no clowder with this name!"));
					}
				}
				else
				{
					sender.addChatMessage(new ChatComponentText(ERROR + "You lack the permissions for foreign diplomacy!"));
				}
			//} else
			//{
			//	sender.addChatMessage(new ChatComponentText(ERROR + "Tributaries cannot form alliances!"));
			//}
		}
		else {
			sender.addChatMessage(new ChatComponentText(ERROR + "You need to be in a clowder!"));
		}
	}

	private void cmdAcceptFriend(ICommandSender sender, String name) {

		EntityPlayer player = getCommandSenderAsPlayer(sender);
		Clowder clowder = Clowder.getClowderFromPlayer(player);

		if(clowder != null) {

			if(clowder.suzerain == null) {


				if(clowder.getPermLevel(player.getDisplayName()) > 1) {

					if(clowder.potentialFriends.contains(name))
					{ //checks if the name of the guy you typed in command actually applied to become your ALLY
						//why is it the PERSON, it should be the FACTION. STUPID FUCKING BOB OR WEEDER OR WHOEVER THE FUCK

						if(Clowder.getClowderFromPlayerName(name) != null)
						{

							Clowder friend = Clowder.getClowderFromPlayerName(name); //clowder of guy who offered to ALLY


							if (friend != clowder) //prevent becoming your own tributary
							{
								sender.addChatMessage(new ChatComponentText(INFO + "We accepted " + name + "'s offer to make " + friend.name + " our ally!"));
								friend.notifyAll(player.worldObj, new ChatComponentText(INFO +  clowder.name + " accepted our offer. We are now their ally."));



								//allah bookmark - install the actual ally shit here
								clowder.addAlly(player.worldObj, friend);
								friend.addAlly(player.worldObj, clowder);
								//friend.addPeaceTreaty(60, player.worldObj);

								//for cancelling wars against the tributary
								//if(clowder.enemy == friend)
								//{
								//	clowder.pussy(player.worldObj);
								//	friend.notifyAll(player.worldObj, new ChatComponentText(INFO + "Because " + clowder.name + " accepted our alliance offer, their war goals against us were cancelled."));
								//	clowder.notifyAll(player.worldObj, new ChatComponentText(INFO + "Because " + friend.name + " is now our ally, our war goals against them have been cancelled."));
//
								//}





							}
							else
								sender.addChatMessage(new ChatComponentText(ERROR + "We cannot become our own ally"));
						}
						else {
							sender.addChatMessage(new ChatComponentText(ERROR + "This player is not in another clowder!"));
						}

						clowder.potentialFriends.remove(name);

					}
					else
						sender.addChatMessage(new ChatComponentText(ERROR + "This player has no active application!"));
				} else
					sender.addChatMessage(new ChatComponentText(ERROR + "You lack the permissions to manage applications!"));
			}
			else
				sender.addChatMessage(new ChatComponentText(ERROR + "Tributaries cannot form alliances!"));
		} else
			sender.addChatMessage(new ChatComponentText(ERROR + "You are not in any clowder!"));
	}

	private void cmdUnfriend(ICommandSender sender, String kickee) {

		EntityPlayer player = getCommandSenderAsPlayer(sender);
		Clowder clowder = Clowder.getClowderFromPlayer(player);
		Clowder formerFriend = Clowder.getClowderFromName(kickee);

		if (clowder != null) {

			//no escape for bitches
			//if (!clowder.bitch) {
			//fuck yo whole bitch system


				if (clowder.getPermLevel(player.getDisplayName()) > 1) {

					if (formerFriend != null) {

						if (clowder == formerFriend) {

							sender.addChatMessage(
									new ChatComponentText(CRITICAL + "You can not unfriend yourself, idiot!"));

						} else {
							formerFriend.notifyAll(player.worldObj, new ChatComponentText(INFO + clowder.name + " has cancelled our alliance!"));
							clowder.removeAlly(player.worldObj, kickee);
							formerFriend.removeAlly(player.worldObj, clowder.name);
							long until = System.currentTimeMillis() + XFConfig.allianceBreakCooldownMs;
							clowder.formerAllyNoWarUntil.put(formerFriend.name, until);
							formerFriend.formerAllyNoWarUntil.put(clowder.name, until);
							// modid instead of modname

							clowder.notifyAll(player.worldObj, new ChatComponentText(INFO + "Friendship with " + kickee + " has ended!"));
						}
					} else {
						sender.addChatMessage(new ChatComponentText(ERROR + "This action does not exist."));
					}

				} else {
					sender.addChatMessage(new ChatComponentText(ERROR + "You lack the permissions to cancel alliances!"));
				}


			//} else
			//	sender.addChatMessage(new ChatComponentText(
			//			ERROR + "Bitches cannot perform diplomacy"));

		} else {
			sender.addChatMessage(new ChatComponentText(ERROR + "You are not in any clowder!"));
		}
	}

	private void cmdColor(ICommandSender sender, String color) {

		EntityPlayer player = getCommandSenderAsPlayer(sender);
		Clowder clowder = Clowder.getClowderFromPlayer(player);

		if(clowder != null) {

			if(clowder.getPermLevel(player.getDisplayName()) > 1) {
				int c = ParserUtil.parseColor(color);

				if(c < 0) {
					sender.addChatMessage(new ChatComponentText(ERROR + "Incorrect color format!"));
				} else {
					clowder.setColor(c, player);
					sender.addChatMessage(new ChatComponentText(INFO + "Set faction color to " + color + "!"));
					PacketDispatcher.wrapper.sendTo(new ClowderFlagPacket(clowder, ""), (EntityPlayerMP) player);
				}
			} else {
				sender.addChatMessage(new ChatComponentText(ERROR + "You lack the permissions to change this factiion's color!"));
			}

		} else {
			sender.addChatMessage(new ChatComponentText(ERROR + "You are not in any faction!"));
		}
	}

	private void cmdInfo(ICommandSender sender, String name) {

		EntityPlayer player = getCommandSenderAsPlayer(sender);

		Clowder clowder = name == null ? Clowder.getClowderFromPlayer(player) : Clowder.getClowderFromName(name);

		if(clowder != null) {

			sender.addChatMessage(new ChatComponentText(TITLE + clowder.getDecoratedName()));
			sender.addChatMessage(new ChatComponentText(TITLE + clowder.motd));
			sender.addChatMessage(new ChatComponentText(LIST + "Owner: " + clowder.leader));
			sender.addChatMessage(new ChatComponentText(LIST + "Players considered online: " + clowder.getPlayersOnline() + "/" + clowder.members.keySet().size()));
			sender.addChatMessage(new ChatComponentText(LIST + "Raidable? " + clowder.isRaidable()));
			sender.addChatMessage(new ChatComponentText(LIST + "Members: " + clowder.members.size()));
			sender.addChatMessage(new ChatComponentText(LIST + "Prestige: " + clowder.round(clowder.getPrestige())));
			sender.addChatMessage(new ChatComponentText(LIST + " -generating: " + clowder.round(clowder.getPrestigeGen()) + " per hour"));
			sender.addChatMessage(new ChatComponentText(LIST + " -requires: " + clowder.round(clowder.getPrestigeReq()) + " at all times"));
			sender.addChatMessage(new ChatComponentText(LIST + " -net: " + clowder.round(clowder.getHourlyNetPrestige()) + " per hour"));
			sender.addChatMessage(new ChatComponentText(LIST + "Color: " + Integer.toHexString(clowder.color).toUpperCase()));
			sender.addChatMessage(new ChatComponentText(LIST + "Allies: " + formatAllyList(clowder)));
			sender.addChatMessage(new ChatComponentText(LIST + "Enemies: " + formatEnemyList(clowder)));
			sender.addChatMessage(new ChatComponentText(LIST + "Cities: " + ClowderTerritory.getCityClaims(clowder).size()));
			for(Object cityObj : ClowderTerritory.getCityClaims(clowder)) {
				TerritoryMeta city = (TerritoryMeta)cityObj;
				ClowderTerritory.refreshCityMetaFromTile(city);
				CityLevel level = city.getCityLevel();
				World cityWorld = DimensionManager.getWorld(city.dimensionId);
				TileEntity te = cityWorld == null ? null : cityWorld.getTileEntity(city.flagX, city.flagY, city.flagZ);
				if(te instanceof TileEntityFlag)
					level = ((TileEntityFlag)te).cityLevel;
				sender.addChatMessage(new ChatComponentText(LIST + " - " + city.cityName + " [" + level.displayName + "] dim " + city.dimensionId + " center X:" + city.flagX + " Y:" + city.flagY + " Z:" + city.flagZ + " radius " + XFConfig.cityRadius(level) + " upkeep " + XFConfig.cityUpkeep(level)));
			}

		} else {
			sender.addChatMessage(new ChatComponentText(ERROR + "You are not in any faction!"));
		}
	}

	/*private void cmdInfo2(ICommandSender sender, String name) {

		EntityPlayer player = getCommandSenderAsPlayer(sender);
		Clowder clowder = Clowder.getClowderFromName(name);

		if(clowder != null) {

			sender.addChatMessage(new ChatComponentText(TITLE + clowder.getDecoratedName()));
			sender.addChatMessage(new ChatComponentText(TITLE + clowder.motd));
			sender.addChatMessage(new ChatComponentText(LIST + "Owner: " + clowder.leader));
			sender.addChatMessage(new ChatComponentText(LIST + "Members: " + clowder.members.size()));
			sender.addChatMessage(new ChatComponentText(LIST + "Prestige: " + clowder.round(clowder.getPrestige())));
			sender.addChatMessage(new ChatComponentText(LIST + "Color: " + Integer.toHexString(clowder.color).toUpperCase()));
			sender.addChatMessage(new ChatComponentText(LIST + "Enemies: " + formatEnemyList(clowder)));
			sender.addChatMessage(new ChatComponentText(LIST + "Cities: " + ClowderTerritory.getCityClaims(clowder).size()));
			for(Object cityObj : ClowderTerritory.getCityClaims(clowder)) {
				TerritoryMeta city = (TerritoryMeta)cityObj;
				ClowderTerritory.refreshCityMetaFromTile(city);
				CityLevel level = city.getCityLevel();
				World cityWorld = DimensionManager.getWorld(city.dimensionId);
				TileEntity te = cityWorld == null ? null : cityWorld.getTileEntity(city.flagX, city.flagY, city.flagZ);
				if(te instanceof TileEntityFlag)
					level = ((TileEntityFlag)te).cityLevel;
				sender.addChatMessage(new ChatComponentText(LIST + " - " + city.cityName + " [" + level.displayName + "] dim " + city.dimensionId + " center X:" + city.flagX + " Y:" + city.flagY + " Z:" + city.flagZ + " radius " + XFConfig.cityRadius(level) + " upkeep " + XFConfig.cityUpkeep(level)));
			}

		} else {
			sender.addChatMessage(new ChatComponentText(ERROR + "This faction does not exist!"));
		}
	}*/

	private void cmdRename(ICommandSender sender, String name) {

		EntityPlayer player = getCommandSenderAsPlayer(sender);
		Clowder clowder = Clowder.getClowderFromPlayer(player);
		String factionName = Clowder.canonicalizeClowderName(name);

		if(factionName.isEmpty()) { sender.addChatMessage(new ChatComponentText(ERROR + "Faction name cannot be empty.")); return; }

		if(clowder != null) {

			if(Clowder.getClowderFromName(factionName) == null) {

				if(clowder.getPermLevel(player.getDisplayName()) > 1) {
					clowder.rename(factionName, player);
					sender.addChatMessage(new ChatComponentText(TITLE + "Renamed faction to " + factionName + "!"));
					PacketDispatcher.wrapper.sendTo(new ClowderFlagPacket(clowder, ""), (EntityPlayerMP) player);
				} else {
					sender.addChatMessage(new ChatComponentText(ERROR + "You lack the permissions to rename this faction!"));
				}

			} else {
				sender.addChatMessage(new ChatComponentText(ERROR + "This name is already taken!"));
			}

		} else {
			sender.addChatMessage(new ChatComponentText(ERROR + "You are not in any faction!"));
		}
	}

	private void cmdList(ICommandSender sender) {

		EntityPlayer player = getCommandSenderAsPlayer(sender);

		for(Clowder c : Clowder.clowders) {

			sender.addChatMessage(new ChatComponentText(TITLE + c.getDecoratedName() + " - " + c.motd));
			sender.addChatMessage(new ChatComponentText(LIST + c.members.size() + " members"));
		}

		if(Clowder.clowders.isEmpty()) {
			sender.addChatMessage(new ChatComponentText(TITLE + "There are no factions as of now. Use /clowder create <name>"));
			sender.addChatMessage(new ChatComponentText(TITLE + "to start your own faction!"));
		}
	}

	private void cmdMOTD(ICommandSender sender, String[] motd) {

		EntityPlayer player = getCommandSenderAsPlayer(sender);
		Clowder clowder = Clowder.getClowderFromPlayer(player);

		if(clowder != null) {

			if(clowder.getPermLevel(player.getDisplayName()) > 1) {

				String stitched = "";

				for(int i = 1; i < motd.length; i++)
					stitched += motd[i] + " ";

				stitched = stitched.trim();

				clowder.setMotd(stitched, player);
				sender.addChatMessage(new ChatComponentText(TITLE + "Set faction MotD to " + stitched + "!"));
			} else {
				sender.addChatMessage(new ChatComponentText(ERROR + "You lack the permissions to change this faction's MOTD!"));
			}

		} else {
			sender.addChatMessage(new ChatComponentText(ERROR + "You are not in any faction!"));
		}
	}

	private void cmdOwner(ICommandSender sender, String owner) {

		EntityPlayer player = getCommandSenderAsPlayer(sender);
		Clowder clowder = Clowder.getClowderFromPlayer(player);

		if(clowder != null) {

			if(clowder.getPermLevel(player.getDisplayName()) > 2) {

				if(clowder.members.get(owner) != null) {

					clowder.transferOwnership(player.worldObj, owner);
					sender.addChatMessage(new ChatComponentText(INFO + "Transfered leadership to player " + owner + "!"));
					clowder.notifyLeader(player.worldObj, new ChatComponentText(INFO + "You are now this faction's new leader!"));

				} else {
					sender.addChatMessage(new ChatComponentText(ERROR + "This player is not in your faction!"));
				}

			} else {
				sender.addChatMessage(new ChatComponentText(ERROR + "You can not change the color of a faction you do not own!"));
			}

		} else {
			sender.addChatMessage(new ChatComponentText(ERROR + "You are not in any faction!"));
		}
	}

	private void cmdApply(ICommandSender sender, String name) {

		EntityPlayer player = getCommandSenderAsPlayer(sender);
		Clowder clowder = Clowder.getClowderFromPlayer(player);

		if(clowder == null) {

			Clowder toApply = Clowder.getClowderFromName(name);

			if(toApply != null) {

				sender.addChatMessage(new ChatComponentText(INFO + "Sent application to " + toApply.getDecoratedName() + "!"));
				toApply.applications.add(player.getDisplayName());
				toApply.notifyLeader(player.worldObj, new ChatComponentText(INFO + "Player " + sender.getCommandSenderName() + " would like to join your faction!"));

			} else {
				sender.addChatMessage(new ChatComponentText(ERROR + "There is no faction with this name!"));
			}

		} else {
			sender.addChatMessage(new ChatComponentText(ERROR + "You are already in a faction!"));
		}
	}

	private void cmdLeave(ICommandSender sender) {

		EntityPlayer player = getCommandSenderAsPlayer(sender);
		Clowder clowder = Clowder.getClowderFromPlayer(player);

		if(clowder != null) {

			if(clowder.getPermLevel(player.getDisplayName()) < 3) {

				clowder.removeMember(player.worldObj, player.getDisplayName());
				sender.addChatMessage(new ChatComponentText(CRITICAL + "You left this faction!"));

			} else {
				sender.addChatMessage(new ChatComponentText(ERROR + "You can not leave a faction you own!"));
			}

		} else {
			sender.addChatMessage(new ChatComponentText(ERROR + "You are not in any faction!"));
		}
	}

	private void cmdAccept(ICommandSender sender, String name) {

		EntityPlayer player = getCommandSenderAsPlayer(sender);
		Clowder clowder = Clowder.getClowderFromPlayer(player);

		if(clowder != null) {

			if(clowder.getPermLevel(player.getDisplayName()) > 1) {

				if(clowder.applications.contains(name)) {

					if(Clowder.getClowderFromName(name) == null) {
						clowder.addMember(player.worldObj, name);
						sender.addChatMessage(new ChatComponentText(INFO + "Added player " + name + " to your faction!"));
						clowder.notifyPlayer(player.worldObj, name, new ChatComponentText(INFO + "You have been accepted into " + clowder.getDecoratedName() + "!"));
					} else {
						sender.addChatMessage(new ChatComponentText(ERROR + "This player is already in another faction!"));
					}

					clowder.applications.remove(name);

				} else {
					sender.addChatMessage(new ChatComponentText(ERROR + "This player has no active application!"));
				}

			} else {
				sender.addChatMessage(new ChatComponentText(ERROR + "You lack the permissions to manage applications!"));
			}

		} else {
			sender.addChatMessage(new ChatComponentText(ERROR + "You are not in any faction!"));
		}
	}

	private void cmdDeny(ICommandSender sender, String name) {

		EntityPlayer player = getCommandSenderAsPlayer(sender);
		Clowder clowder = Clowder.getClowderFromPlayer(player);

		if(clowder != null) {

			if(clowder.getPermLevel(player.getDisplayName()) > 1) {

				if(clowder.applications.contains(name)) {

					if(Clowder.getClowderFromName(name) == null) {
						sender.addChatMessage(new ChatComponentText(INFO + "Denied player " + name + "'s application!")); //dumbass alert
					} else {
						sender.addChatMessage(new ChatComponentText(ERROR + "This player is already in another faction!"));
					}

					clowder.applications.remove(name);

				} else {
					sender.addChatMessage(new ChatComponentText(ERROR + "This player has no active application!"));
				}

			} else {
				sender.addChatMessage(new ChatComponentText(ERROR + "You lack the permissions to manage applications!"));
			}

		} else {
			sender.addChatMessage(new ChatComponentText(ERROR + "You are not in any faction!"));
		}
	}

	private void cmdApplicants(ICommandSender sender) {

		EntityPlayer player = getCommandSenderAsPlayer(sender);
		Clowder clowder = Clowder.getClowderFromPlayer(player);

		if(clowder != null) {

			if(clowder.getPermLevel(player.getDisplayName()) > 1) {

				sender.addChatMessage(new ChatComponentText(TITLE + "Applicants:"));
				int cnt = 0;

				for (String key : clowder.applications) {
					sender.addChatMessage(new ChatComponentText(LIST + "-" + key));
					cnt++;
				}

				if(cnt == 0)
					sender.addChatMessage(new ChatComponentText(LIST + "None!"));

			} else {
				sender.addChatMessage(new ChatComponentText(ERROR + "You lack the permissions to manage applications!"));
			}

		} else {
			sender.addChatMessage(new ChatComponentText(ERROR + "You are not in any faction!"));
		}
	}

	private void cmdKick(ICommandSender sender, String kickee) {

		EntityPlayer player = getCommandSenderAsPlayer(sender);
		Clowder clowder = Clowder.getClowderFromPlayer(player);

		if(clowder != null) {

			if(clowder.getPermLevel(player.getDisplayName()) > 1) {

				if(clowder.members.get(kickee) != null) {

					if(player.getDisplayName().equals(kickee)) {

						sender.addChatMessage(new ChatComponentText(CRITICAL + "You can not kick yourself, idiot!"));

					} else {
						clowder.notifyPlayer(player.worldObj, kickee, new ChatComponentText(CRITICAL + "You have been kicked from your faction!"));
						clowder.removeMember(player.worldObj, kickee);
						sender.addChatMessage(new ChatComponentText(INFO + "Kicked player " + kickee + "!"));
					}
				} else {
					sender.addChatMessage(new ChatComponentText(ERROR + "This player is not in your faction!"));
				}

			} else {
				sender.addChatMessage(new ChatComponentText(ERROR + "You lack the permissions to kick members!"));
			}

		} else {
			sender.addChatMessage(new ChatComponentText(ERROR + "You are not in any faction!"));
		}
	}

	private void cmdListflags(ICommandSender sender, String page) {

		int fpp = 20;

		int p = this.parseInt(sender, page);
		int pages = (int) Math.ceil(((double)ClowderFlag.getFlags().size()) / fpp);

		if(p < 1 || p > pages)
			p = 1;

		sender.addChatMessage(new ChatComponentText(TITLE + "[" + p + "/" + pages + "] List of availible flags:"));

		for(int i = (p - 1) * fpp; (i < p * fpp) && (i < ClowderFlag.values().length); i++) {
			if(ClowderFlag.values()[i].show)
				sender.addChatMessage(new ChatComponentText(LIST + "-" + ClowderFlag.values()[i].name));
		}

	}

	private void cmdFlag(ICommandSender sender, String[] args) {

		EntityPlayer player = getCommandSenderAsPlayer(sender);
		Clowder clowder = Clowder.getClowderFromPlayer(player);

		if(clowder != null) {

			if(clowder.getPermLevel(player.getDisplayName()) > 2 ) {

				String flag = args[1];
				if(flag.equalsIgnoreCase("seturl")) {
					if(args.length < 3) { sender.addChatMessage(new ChatComponentText(ERROR + "Invalid format. Usage: /c flag seturl <https://i.postimg.cc/...>")); return; }
					// Clients must never load arbitrary remote URLs directly. The server imports, validates, strips metadata,
					// caches and syncs sanitized PNG bytes so malicious URLs cannot target every client.
					if(!XFConfig.enableCustomFactionFlags) { sender.addChatMessage(new ChatComponentText(ERROR + "Custom faction flags are disabled on this server.")); return; }
					CustomFlagService.importUrl((EntityPlayerMP)player, clowder, args[2]);
					return;
				}
				if(flag.equalsIgnoreCase("clear")) { if(!XFConfig.enableCustomFactionFlags) { sender.addChatMessage(new ChatComponentText(ERROR + "Custom faction flags are disabled on this server.")); return; } CustomFlagService.clearFlag((EntityPlayerMP)player, clowder); return; }
				if(flag.equalsIgnoreCase("reload")) { if(!XFConfig.enableCustomFactionFlags) { sender.addChatMessage(new ChatComponentText(ERROR + "Custom faction flags are disabled on this server.")); return; } CustomFlagService.reloadFlag((EntityPlayerMP)player, clowder); return; }

				ClowderFlag f = ClowderFlag.getFromName(flag.toLowerCase());

				if(f != ClowderFlag.NONE) {

					clowder.flag = f;
					sender.addChatMessage(new ChatComponentText(INFO + "Changed flag to " + flag + "!"));
					PacketDispatcher.wrapper.sendTo(new ClowderFlagPacket(clowder, ""), (EntityPlayerMP) player);

				} else {
					sender.addChatMessage(new ChatComponentText(ERROR + "This flag does not exist!"));
				}

			} else {
				sender.addChatMessage(new ChatComponentText(ERROR + "You lack the permissions to change this faction's flag!"));
			}

		} else {
			sender.addChatMessage(new ChatComponentText(ERROR + "You are not in any faction!"));
		}
	}

	private void cmdRetreat(ICommandSender sender) {

		EntityPlayer player = getCommandSenderAsPlayer(sender);
		Clowder clowder = Clowder.getClowderFromPlayer(player);

		if(clowder != null) {

			if(!Clowder.retreating.contains(player.getDisplayName())) {
				//System.out.println("POV: I mog you");
				sender.addChatMessage(new ChatComponentText(INFO + "POV: I mog you"));
				//clowder.notifyAll(player.worldObj, new ChatComponentText(INFO + "Player " + player.getDisplayName() + " is retreating!"));
				//sender.addChatMessage(new ChatComponentText(INFO + "You will be automatically kicked in 10 minutes!"));
				//Clowder.retreating.add(player.getDisplayName());

			}// else {
			//sender.addChatMessage(new ChatComponentText(ERROR + "You are already retreating!"));
			//}

		} else {
			sender.addChatMessage(new ChatComponentText(ERROR + "You are not in any faction!"));
		}
	}

	private void cmdSethome(ICommandSender sender) {

		EntityPlayer player = getCommandSenderAsPlayer(sender);
		Clowder clowder = Clowder.getClowderFromPlayer(player);

		if (clowder != null) {
			// level 1 member level 2 officer level 3 leader
			if (clowder.getPermLevel(player.getDisplayName()) > 1) {

				Ownership owner = ClowderTerritory.getOwnerFromInts(player.worldObj, (int) player.posX, (int) player.posZ);

				if (owner != null && owner.zone == Zone.FACTION && owner.owner == clowder) {

					if (clowder.sethomeDelay <= 0)
					{
						clowder.setHome(player.posX, player.posY, player.posZ, player);
						clowder.notifyAll(player.worldObj, new ChatComponentText(INFO + "Home set!"));
						clowder.addSethomeDelay(600, player.worldObj); //10 minute delay
					}
					else {
						sender.addChatMessage(
								new ChatComponentText(ERROR + "Please wait " + (int)clowder.sethomeDelay + " seconds to set home again!"));
					}


				} else {
					sender.addChatMessage(
							new ChatComponentText(ERROR + "You can not set the home outside of your claimed land!"));
				}

			} else {
				sender.addChatMessage(
						new ChatComponentText(ERROR + "You lack the permissions to set this faction's home point!"));
			}

		} else {
			sender.addChatMessage(new ChatComponentText(ERROR + "You are not in any faction!"));
		}
	}

	private void cmdSetAllyWarp(ICommandSender sender) {

		EntityPlayer player = getCommandSenderAsPlayer(sender);
		Clowder clowder = Clowder.getClowderFromPlayer(player);

		if (clowder != null) {
			// level 1 member level 2 officer level 3 leader
			if (clowder.getPermLevel(player.getDisplayName()) > 1) {

				Ownership owner = ClowderTerritory.getOwnerFromInts(player.worldObj, (int) player.posX, (int) player.posZ);

				if (owner != null && owner.zone == Zone.FACTION && owner.owner == clowder) {



					if (clowder.sethomeDelay <= 0)
					{
						clowder.setAllyWarp(player.posX, player.posY, player.posZ, player);
						clowder.notifyAll(player.worldObj, new ChatComponentText(INFO + "Ally Warp set!"));
						clowder.addSethomeDelay(600, player.worldObj); //10 minute delay
					}
					else {
						sender.addChatMessage(
								new ChatComponentText(ERROR + "Please wait " + (int)clowder.sethomeDelay + " seconds to move the alliance rally-point!"));
					}



				} else {
					sender.addChatMessage(
							new ChatComponentText(ERROR + "You can not set the Ally Warp outside of your claimed land!"));
				}

			} else {
				sender.addChatMessage(
						new ChatComponentText(ERROR + "You lack the permissions to set this faction's Ally Warp point!"));
			}

		} else {
			sender.addChatMessage(new ChatComponentText(ERROR + "You are not in any faction!"));
		}
	}

	private void cmdHome(ICommandSender sender) {

		EntityPlayerMP player = getCommandSenderAsPlayer(sender);
		Clowder clowder = Clowder.getClowderFromPlayer(player);

		if(clowder != null) {

			if(clowder.areWarpsDisabled()) {
				sender.addChatMessage(new ChatComponentText(ERROR + "Warps are disabled while your faction is in " + clowder.getBankruptcyStageName() + "."));
				return;
			}

			Ownership owner = ClowderTerritory.getOwnerFromInts(player.worldObj, (int)player.posX, (int)player.posZ);

			if(owner != null && (owner.zone == Zone.WARZONE || (owner.zone == Zone.FACTION && (owner.owner != clowder && clowder.allies.get(owner.owner) == null) ) ) ) { //allow warp from allied territory

				sender.addChatMessage(new ChatComponentText(ERROR + "You can not teleport home in foreign territory!"));

			} else {

				sender.addChatMessage(new ChatComponentText(INFO + "Please stand still for 10 seconds!"));
				clowder.teleports.put(System.currentTimeMillis() + 10000L, new ScheduledTeleport(clowder.homeDim, clowder.homeX, clowder.homeY, clowder.homeZ, player.getDisplayName(), true));
			}

		} else {
			sender.addChatMessage(new ChatComponentText(ERROR + "You are not in any faction!"));
		}
	}

	private void cmdAllyWarp(ICommandSender sender, String name) {

		EntityPlayerMP player = getCommandSenderAsPlayer(sender);
		Clowder clowder = Clowder.getClowderFromPlayer(player);
		Clowder ally = Clowder.getClowderFromName(name);

		if (clowder != null) {
			if(clowder.areAllyWarpsDisabled()) {
				sender.addChatMessage(new ChatComponentText(ERROR + "Ally warps are disabled while your faction is in " + clowder.getBankruptcyStageName() + "."));
				return;
			}
			if (ally != null) {
				if(ally.areAllyWarpsDisabled()) {
					sender.addChatMessage(new ChatComponentText(ERROR + ally.name + " cannot receive ally warps while in " + ally.getBankruptcyStageName() + "."));
					return;
				}

				Ownership owner = ClowderTerritory.getOwnerFromInts(player.worldObj, (int) player.posX, (int) player.posZ);

				if (owner != null
						&& (owner.zone == Zone.WARZONE || (owner.zone == Zone.FACTION && (owner.owner != clowder && clowder.allies.get(owner.owner) == null) ) ) ) {

					System.out.println("alliesS get owner name: " + clowder.alliesS.get(owner.owner.name) + " owner.owner name: " + owner.owner.name);
					sender.addChatMessage(new ChatComponentText(ERROR + "You can not teleport to an Ally Warp from unfriendly territory!"));

				} else
				{
					//area where ally warp teleportation is executed. put ally restrictions here

					if(ally.allies.get(clowder) != null)
					{

						if(ally.allyWarpX != 0 && ally.allyWarpY != 0 && ally.allyWarpZ != 0)
						{
							sender.addChatMessage(new ChatComponentText(INFO + "Please stand still for 10 seconds!"));
							clowder.teleports.put(System.currentTimeMillis() + 10000L, new ScheduledTeleport(ally.allyWarpDim, ally.allyWarpX,
									ally.allyWarpY, ally.allyWarpZ, player.getDisplayName(), true, true, ally.name));
						}
						else
							sender.addChatMessage(new ChatComponentText(ERROR + name + " did not set an alliance rally-point!"));

					}
					else
						sender.addChatMessage(new ChatComponentText(ERROR + name + " is not our ally!"));
				}

			}

			else {
				sender.addChatMessage(new ChatComponentText(ERROR + name + " is not a valid faction!"));
			}
		}
		else {
			sender.addChatMessage(new ChatComponentText(ERROR + "You are not in any faction!"));
		}
	}

	private void cmdAddWarp(ICommandSender sender, String name) {

		EntityPlayer player = getCommandSenderAsPlayer(sender);
		Clowder clowder = Clowder.getClowderFromPlayer(player);

		if(clowder != null) {

			if(clowder.areWarpsDisabled()) {
				sender.addChatMessage(new ChatComponentText(ERROR + "Warps are disabled while your faction is in " + clowder.getBankruptcyStageName() + "."));
				return;
			}

			if(clowder.warps.containsKey(name)) {
				sender.addChatMessage(new ChatComponentText(ERROR + "This warp already exists!"));
				return;
			}

			if(clowder.getPrestige() < XFConfig.warpCost) {
				sender.addChatMessage(new ChatComponentText(ERROR + "You need at least " + Clowder.round(XFConfig.warpCost) + " prestige to create a warp!"));
				return;
			}

			int code = clowder.tryAddWarp(player, (int)player.posX, (int)player.posY, (int)player.posZ, name);

			if(code == 0) {
				clowder.notifyAll(player.worldObj, new ChatComponentText(INFO + "Created warp " + name + "!"));
				clowder.addPrestige(-XFConfig.warpCost, player.worldObj);
				clowder.save(player.worldObj);
			} else if(code == 1) {
				sender.addChatMessage(new ChatComponentText(ERROR + "Cannot create warp outside of your territory!"));
			} else if(code == 2) {
				sender.addChatMessage(new ChatComponentText(ERROR + "No nearby warp tents!"));
			}

		} else {
			sender.addChatMessage(new ChatComponentText(ERROR + "You are not in any faction!"));
		}
	}

	private void cmdDelWarp(ICommandSender sender, String name) {

		EntityPlayer player = getCommandSenderAsPlayer(sender);
		Clowder clowder = Clowder.getClowderFromPlayer(player);

		if(clowder != null) {

			if(clowder.warps.containsKey(name)) {
				clowder.warps.remove(name);
				clowder.save(player.worldObj);
				sender.addChatMessage(new ChatComponentText(INFO + "Deleted warp!"));
			} else {
				sender.addChatMessage(new ChatComponentText(ERROR + "This warp does not exist!"));
			}

		} else {
			sender.addChatMessage(new ChatComponentText(ERROR + "You are not in any faction!"));
		}
	}

	private void cmdWarp(ICommandSender sender, String name) {

		EntityPlayerMP player = getCommandSenderAsPlayer(sender);
		Clowder clowder = Clowder.getClowderFromPlayer(player);

		if (clowder != null) {

			if(clowder.areWarpsDisabled()) {
				sender.addChatMessage(new ChatComponentText(ERROR + "Warps are disabled while your faction is in " + clowder.getBankruptcyStageName() + "."));
				return;
			}

			if (clowder.warps.containsKey(name)) {

				Ownership owner = ClowderTerritory.getOwnerFromInts(player.worldObj, (int) player.posX, (int) player.posZ);

				if (owner != null
						//&& (owner.zone == Zone.WARZONE || (owner.zone == Zone.FACTION && owner.owner != clowder))) {
						&& (owner.zone == Zone.WARZONE || (owner.zone == Zone.FACTION && (owner.owner != clowder && clowder.allies.get(owner.owner) == null) ) ) ) { //allow warp from allied territory

					sender.addChatMessage(new ChatComponentText(ERROR + "You can not warp in unfriendly territory!"));
					return;
				}

				int[] warp = clowder.warps.get(name);

				if (warp == null) {
					return;
				}

				World warpWorld = net.minecraftforge.common.DimensionManager.getWorld(warp.length > 3 ? warp[3] : 0);
				if(warpWorld == null) { sender.addChatMessage(new ChatComponentText(ERROR + "Warp dimension is not loaded.")); return; }
				IChunkProvider provider = warpWorld.getChunkProvider();

				for (int i = 2; i <= 5; i++) {

					ForgeDirection dir = ForgeDirection.getOrientation(i);

					provider.loadChunk((warp[0] + dir.offsetX * 2) >> 4, (warp[2] + dir.offsetZ * 2) >> 4);

					int tentX = warp[0] + dir.offsetX * 2;
					int tentZ = warp[2] + dir.offsetZ * 2;

					Block block = warpWorld.getBlock(tentX, warp[1], tentZ);

					if (block == ModBlocks.tp_tent) {

						int[] pos = ((BlockDummyable) ModBlocks.tp_tent).findCore(warpWorld, tentX, warp[1],
								tentZ);

						if (pos != null) {

							provider.loadChunk(pos[0] >> 4, pos[2] >> 4);
							TileEntityProp tent = (TileEntityProp) warpWorld.getTileEntity(pos[0], pos[1],
									pos[2]);

							if (tent.warp.equals(name) && tent.operational()) {

								sender.addChatMessage(
										new ChatComponentText(INFO + "Please stand still for 10 seconds!"));
								clowder.teleports.put(System.currentTimeMillis() + 10000L, new ScheduledTeleport(
										warp.length > 3 ? warp[3] : 0, warp[0], warp[1], warp[2], player.getDisplayName(), name));

								return;
							}
						}
					}
				}

				sender.addChatMessage(new ChatComponentText(
						ERROR + "Warp tent not found! Make sure it still exists or remove this warp!"));

			} else {
				sender.addChatMessage(new ChatComponentText(ERROR + "This warp does not exist!"));
			}

		} else {
			sender.addChatMessage(new ChatComponentText(ERROR + "You are not in any faction!"));
		}
	}

	private void cmdWarps(ICommandSender sender) {

		EntityPlayer player = getCommandSenderAsPlayer(sender);
		Clowder clowder = Clowder.getClowderFromPlayer(player);

		if(clowder != null) {

			sender.addChatMessage(new ChatComponentText(TITLE + "Availible warps:"));

			for(String s : clowder.warps.keySet()) {
				int[] pos = clowder.warps.get(s);
				sender.addChatMessage(new ChatComponentText(LIST + s));
				sender.addChatMessage(new ChatComponentText(LIST + " x:" + pos[0] + " y:" + pos[1] + " z:" + pos[2]));
			}

		} else {
			sender.addChatMessage(new ChatComponentText(ERROR + "You are not in any faction!"));
		}
	}

	private void cmdBalance(ICommandSender sender) {

		EntityPlayer player = getCommandSenderAsPlayer(sender);
		Clowder clowder = Clowder.getClowderFromPlayer(player);

		if(clowder != null) {

			sender.addChatMessage(new ChatComponentText(INFO + "Current prestige balance: " + LIST + Clowder.round(clowder.getPrestige()) + TITLE + " (" + clowder.getBankruptcyStageName() + ")"));
			sender.addChatMessage(new ChatComponentText(INFO + "Generation: " + LIST + Clowder.round(clowder.getPrestigeGen()) + "/h" + TITLE + ", upkeep: " + LIST + Clowder.round(clowder.getHourlyUpkeepCost()) + "/h" + TITLE + ", war: " + LIST + Clowder.round(clowder.getHourlyWarCost()) + "/h"));
			sender.addChatMessage(new ChatComponentText(INFO + "Net prestige: " + LIST + Clowder.round(clowder.getHourlyNetPrestige()) + "/h"));

		} else {
			sender.addChatMessage(new ChatComponentText(ERROR + "You are not in any faction!"));
		}
	}

	private void cmdDeposit(ICommandSender sender, String a) {

		EntityPlayerMP player = getCommandSenderAsPlayer(sender);
		Clowder clowder = Clowder.getClowderFromPlayer(player);
		int amount = parseInt(sender, a);

		if(clowder != null) {

			if(amount <= 0) {
				sender.addChatMessage(new ChatComponentText(ERROR + "You cannot deposit 0 or less prestige!"));
				return;
			}

			for(int i = 0; i < amount; i++) {

				if(player.inventory.hasItem(ModItems.province_point)) {
					player.inventory.consumeInventoryItem(ModItems.province_point);
					clowder.addPrestige(1, player.worldObj);
				} else {
					sender.addChatMessage(new ChatComponentText(INFO + "Deposited " + i + " prestige!"));
					clowder.save(player.worldObj);
					player.inventoryContainer.detectAndSendChanges();
					return;
				}
			}

			sender.addChatMessage(new ChatComponentText(INFO + "Deposited " + amount + " prestige!"));
			clowder.save(player.worldObj);
			player.inventoryContainer.detectAndSendChanges();

		} else {
			sender.addChatMessage(new ChatComponentText(ERROR + "You are not in any faction!"));
		}
	}

	private void cmdWithdraw(ICommandSender sender, String a) {

		EntityPlayerMP player = getCommandSenderAsPlayer(sender);
		Clowder clowder = Clowder.getClowderFromPlayer(player);
		int amount = parseInt(sender, a);

		if(clowder != null) {

			if(amount <= 0) {
				sender.addChatMessage(new ChatComponentText(ERROR + "You cannot withdraw 0 or less prestige!"));
				return;
			}

			amount = Math.min(amount, (int)clowder.getPrestige());

			if(clowder.getPrestige() == 0) {
				sender.addChatMessage(new ChatComponentText(INFO + "It seems like you're bankrupt."));
				return;
			}

			clowder.addPrestige(-1, player.worldObj);

			for(int i = 0; i < amount; i++) {

				if(!player.inventory.addItemStackToInventory(new ItemStack(ModItems.province_point))) {
					sender.addChatMessage(new ChatComponentText(INFO + "Withdrew " + i + " prestige!"));
					clowder.save(player.worldObj);
					player.inventoryContainer.detectAndSendChanges();
					return;
				}
			}

			sender.addChatMessage(new ChatComponentText(INFO + "Withdrew " + amount + " prestige!"));
			clowder.save(player.worldObj);
			player.inventoryContainer.detectAndSendChanges();

		} else {
			sender.addChatMessage(new ChatComponentText(ERROR + "You are not in any faction!"));
		}
	}

	//private void cmdClaim(ICommandSender sender, String cityName) {
//
	//	EntityPlayer player = getCommandSenderAsPlayer(sender);
	//	Clowder clowder = Clowder.getClowderFromPlayer(player);
	//	if (clowder != null) {
	//		//if(!clowder.bitch)
	//		//{
	//			if (clowder.getPermLevel(player.getDisplayName()) > 1) {
	//				TerritoryMeta meta = ClowderTerritory.territories
	//						.get(ClowderTerritory.coordsToCode(new CoordPair(player.chunkCoordX, player.chunkCoordZ)));
	//				//if (meta != null) {
	//					if (meta.owner.zone == Zone.WILDERNESS
	//							|| (meta.owner.zone == Zone.FACTION && meta.owner.owner.getPrestige() <= 0
	//							&& meta.owner.owner.getPrestigeGen() - meta.owner.owner.getPrestigeReq() < 0)) {
	//						TileEntity te = sender.getEntityWorld().getTileEntity(meta.flagX, meta.flagY, meta.flagZ);
	//						if (te != null && te instanceof TileEntityFlagBig) {
	//							TileEntityFlagBig flag = (TileEntityFlagBig) te;
	//							//if (insideBorders(new CoordPair(flag.xCoord / 16, flag.zCoord / 16))) {
//
	//								if (clowder.getPrestige() >= flag.getCost()) {
	//									// Handling prestige
	//									clowder.addPrestige((float) (-flag.getCost()), sender.getEntityWorld());
	//									clowder.addPrestigeReq((float) flag.getCost(), sender.getEntityWorld());
//
	//									Ownership oldOwner = meta.owner;
	//									// Setting the owner of the flag and the chunks, making the flag cappable
	//									flag.owner = clowder;
	//									flag.markDirty();
	//									for (CoordPair a : flag.claim)
	//										//todone e
	//										ClowderTerritory.setOwnerForCoord(sender.getEntityWorld(), a, clowder,
	//												flag.xCoord, flag.yCoord, flag.zCoord, flag.provinceName);
	//									flag.isCappable = true;
//
	//									MinecraftForge.EVENT_BUS.post(new RegionOwnershipChangedEvent(oldOwner,meta.owner,flag.provinceName));
//
	//									flag.markDirty();
	//									ClowderData.getData(sender.getEntityWorld()).markDirty();
	//								} else {
	//									sender.addChatMessage(new ChatComponentText(ERROR
	//											+ "You already claimed to your capacity. Get more prestige and make sure you have enough to maintain your claims!"));
	//								}
	//							//} else {
	//							//	sender.addChatMessage(new ChatComponentText(ERROR + "This province is out of bounds."));
	//							//}
	//						} else {
	//							sender.addChatMessage(
	//									new ChatComponentText(ERROR + "Wait.. there is no flag! Let an admin know!"));
	//						}
	//					} else {
	//						sender.addChatMessage(new ChatComponentText(ERROR + "You cannot claim here"));
	//					}
	//				//} else {
	//				//	sender.addChatMessage(new ChatComponentText(ERROR
	//				//			+ "You are not standing in any region (most likely you are in the ocean or out of the map)"));
	//				//}
	//			} else {
	//				sender.addChatMessage(
	//						new ChatComponentText(ERROR + "Your authority is not high enough to manage territory"));}
	//		//} else {
	//		//	sender.addChatMessage(
	//		//			new ChatComponentText(ERROR + "Bitches cannot claim land"));}
//
	//	} else {
	//		sender.addChatMessage(new ChatComponentText(ERROR + "You are not in any clowder"));
	//	}
	//}

	private void cmdClaim(ICommandSender sender, String cityName) {

		EntityPlayer player = getCommandSenderAsPlayer(sender);
		Clowder clowder = Clowder.getClowderFromPlayer(player);

		if(cityName == null || cityName.trim().isEmpty()) {
			sender.addChatMessage(new ChatComponentText(ERROR + "Usage: /c claim <city name>"));
			return;
		}
		cityName = cityName.trim();
		if(cityName.length() < XFConfig.claimNameMinLength || cityName.length() > XFConfig.claimNameMaxLength) {
			sender.addChatMessage(new ChatComponentText(ERROR + "City names must be " + XFConfig.claimNameMinLength + "-" + XFConfig.claimNameMaxLength + " characters."));
			return;
		}
		if(XFConfig.claimNameRequireUnique && !ClowderTerritory.isCityNameAvailable(cityName, null)) {
			sender.addChatMessage(new ChatComponentText(ERROR + "A city named " + cityName + " already exists."));
			return;
		}

		if(clowder != null) {

			float foundingCost = clowder.getCityFoundingCost();
			float foundingUpkeep = XFConfig.cityUpkeep(CityLevel.SETTLEMENT);
			if(clowder.getPrestige() < foundingCost || clowder.getPrestigeReq() + foundingUpkeep > clowder.getPrestige() - foundingCost) {
				sender.addChatMessage(new ChatComponentText(ERROR + "Founding a City Center requires " + foundingCost + " prestige and " + foundingUpkeep + " upkeep capacity."));
				return;
			}

			if(player.inventory.hasItem(Item.getItemFromBlock(ModBlocks.clowder_flag))) {
				sender.addChatMessage(new ChatComponentText(ERROR + "You already have a flag in your inventory!"));
				return;
			}

			ItemStack stack = new ItemStack(ModBlocks.clowder_flag);
			stack.stackTagCompound = new NBTTagCompound();
			stack.stackTagCompound.setString("cityName", cityName);
			stack.setStackDisplayName("City Center: " + cityName);
			player.inventory.addItemStackToInventory(stack);
			player.inventoryContainer.detectAndSendChanges();
			sender.addChatMessage(new ChatComponentText(INFO + "Place the City Center to found " + cityName + ". Founding cost is " + foundingCost + " prestige and " + foundingUpkeep + " upkeep capacity."));

		} else {
			sender.addChatMessage(new ChatComponentText(ERROR + "You are not in any faction!"));
		}
	}
	private void cmdCityUpgrade(ICommandSender sender) {
		EntityPlayer player = getCommandSenderAsPlayer(sender);
		Clowder clowder = Clowder.getClowderFromPlayer(player);
		if(clowder == null) {
			sender.addChatMessage(new ChatComponentText(ERROR + "You are not in any faction!"));
			return;
		}
		if(clowder.getPermLevel(player.getDisplayName()) < 2) {
			sender.addChatMessage(new ChatComponentText(ERROR + "You lack the permissions to upgrade cities!"));
			return;
		}
		TerritoryMeta meta = ClowderTerritory.getMetaFromIntCoords(player.worldObj, (int)player.posX, (int)player.posZ);
		if(meta == null || meta.owner == null || meta.owner.owner != clowder) {
			sender.addChatMessage(new ChatComponentText(ERROR + "Stand inside one of your city claims to upgrade it."));
			return;
		}
		TileEntity te = player.worldObj.getTileEntity(meta.flagX, meta.flagY, meta.flagZ);
		if(!(te instanceof TileEntityFlag)) {
			sender.addChatMessage(new ChatComponentText(ERROR + "This claim is not attached to a City Center."));
			return;
		}
		TileEntityFlag city = (TileEntityFlag)te;
		CityLevel next = city.cityLevel.next();
		if(next == null) {
			sender.addChatMessage(new ChatComponentText(ERROR + city.name + " is already a Capital."));
			return;
		}

		String confirmKey = player.getDisplayName() + ":" + meta.flagX + ":" + meta.flagY + ":" + meta.flagZ + ":" + next.ordinal();
		Long confirmUntil = CITY_UPGRADE_CONFIRMATIONS.get(confirmKey);
		long now = System.currentTimeMillis();
		if(confirmUntil == null || confirmUntil < now) {
			CITY_UPGRADE_CONFIRMATIONS.put(confirmKey, now + 10000L);
			sender.addChatMessage(new ChatComponentText(INFO + "Upgrade " + city.name + " to " + next.displayName + " for " + XFConfig.cityUpgradeCost(next) + " prestige."));
			sender.addChatMessage(new ChatComponentText(INFO + "New city level will be " + next.level() + " with radius " + XFConfig.cityRadius(next) + " and " + XFConfig.cityUpkeep(next) + " upkeep. Run /c city upgrade again within 10 seconds to confirm."));
			return;
		}

		CITY_UPGRADE_CONFIRMATIONS.remove(confirmKey);
		if(city.upgradeCity())
			sender.addChatMessage(new ChatComponentText(INFO + city.name + " upgraded to " + city.cityLevel.displayName + " (level " + city.cityLevel.level() + ", radius " + XFConfig.cityRadius(city.cityLevel) + ", upkeep " + XFConfig.cityUpkeep(city.cityLevel) + ")."));
		else
			sender.addChatMessage(new ChatComponentText(ERROR + "Upgrade requires " + XFConfig.cityUpgradeCost(next) + " prestige and sequential upkeep capacity."));
	}

	//I have no idea how territories work, but it looks retarded

	//todo here

	private void cmdPromote(ICommandSender sender, String promotee) {

		EntityPlayer player = getCommandSenderAsPlayer(sender);
		Clowder clowder = Clowder.getClowderFromPlayer(player);

		if(clowder != null) {

			if(clowder.getPermLevel(player.getDisplayName()) > 2) {

				if(clowder.members.get(promotee) != null) {

					if(clowder.getPermLevel(promotee) == 1) {

						clowder.promote(player.worldObj, promotee);

					} else {
						sender.addChatMessage(new ChatComponentText(ERROR + "This player is already promoted!"));
					}

				} else {
					sender.addChatMessage(new ChatComponentText(ERROR + "This player is not in your faction!"));
				}

			} else {
				sender.addChatMessage(new ChatComponentText(ERROR + "You lack the permissions to promote members!"));
			}

		} else {
			sender.addChatMessage(new ChatComponentText(ERROR + "You are not in any faction!"));
		}
	}

	private void cmdDemote(ICommandSender sender, String demotee) {

		EntityPlayer player = getCommandSenderAsPlayer(sender);
		Clowder clowder = Clowder.getClowderFromPlayer(player);

		if(clowder != null) {

			if(clowder.getPermLevel(player.getDisplayName()) > 2) {

				if(clowder.members.get(demotee) != null) {

					if(demotee.equals(player.getDisplayName())) {
						sender.addChatMessage(new ChatComponentText(ERROR + "You can't demote yourself!"));
						return;
					}

					if(clowder.getPermLevel(demotee) == 2) {

						clowder.demote(player.worldObj, demotee);

					} else if(clowder.getPermLevel(demotee) != 3) {
						sender.addChatMessage(new ChatComponentText(ERROR + "This player is already demoted!"));
					} else {
						sender.addChatMessage(new ChatComponentText(ERROR + "Are you seriously trying to demote the faction's leader?"));
					}

				} else {
					sender.addChatMessage(new ChatComponentText(ERROR + "This player is not in your faction!"));
				}

			} else {
				sender.addChatMessage(new ChatComponentText(ERROR + "You lack the permissions to demote members!"));
			}

		} else {
			sender.addChatMessage(new ChatComponentText(ERROR + "You are not in any faction!"));
		}
	}

	private void cmdNameClaim(ICommandSender sender, String name) {

		EntityPlayer player = getCommandSenderAsPlayer(sender);
		Clowder clowder = Clowder.getClowderFromPlayer(player);

		if(clowder == null) {
			sender.addChatMessage(new ChatComponentText(ERROR + "You are not in any faction!"));
			return;
		}

		if(clowder.getPermLevel(player.getDisplayName()) < (XFConfig.claimRenameOfficersAllowed ? 2 : 3)) {
			sender.addChatMessage(new ChatComponentText(ERROR + "You lack the permissions to rename cities!"));
			return;
		}

		TerritoryMeta meta = ClowderTerritory.getMetaFromIntCoords(player.worldObj, (int)player.posX, (int)player.posZ);

		if(meta == null || meta.owner == null || !meta.isCityClaim()) {
			sender.addChatMessage(new ChatComponentText(ERROR + "You are not in a city claim!"));
			return;
		}

		if(meta.owner.owner != clowder)  {
			sender.addChatMessage(new ChatComponentText(ERROR + "You cannot rename a foreign city!"));
			return;
		}

		if(name == null || name.trim().isEmpty()) {
			sender.addChatMessage(new ChatComponentText(ERROR + "Claim names cannot be blank."));
			return;
		}
		name = name.trim();
		if(name.length() < XFConfig.claimNameMinLength || name.length() > XFConfig.claimNameMaxLength) {
			sender.addChatMessage(new ChatComponentText(ERROR + "Claim names must be " + XFConfig.claimNameMinLength + "-" + XFConfig.claimNameMaxLength + " characters."));
			return;
		}

		TileEntity tile = player.worldObj.getTileEntity(meta.flagX, meta.flagY, meta.flagZ);

		if(!(tile instanceof TileEntityFlag)) {
			sender.addChatMessage(new ChatComponentText(ERROR + "The City Center that is connected to this city could not be found! Are the chunks unloaded?"));
			return;
		}

		if(XFConfig.claimNameRequireUnique && !ClowderTerritory.isCityNameAvailable(name, meta)) {
			sender.addChatMessage(new ChatComponentText(ERROR + "A city named " + name + " already exists."));
			return;
		}

		int renamed = ClowderTerritory.renameClaimsForCity(player.worldObj, meta.flagX, meta.flagY, meta.flagZ, name);
		if(renamed <= 0) {
			sender.addChatMessage(new ChatComponentText(ERROR + "No city claims were found for this City Center."));
			return;
		}

		sender.addChatMessage(new ChatComponentText(INFO + "Your city has been renamed! It might take a few moments for all chunks to assume the new name."));
	}

	private void cmdDeclareWar(ICommandSender sender, String targetName) {
		if (!CommandClowderAdmin.WARENABLED) {
			sender.addChatMessage(new ChatComponentText(ERROR + "War declarations are currently disabled by admins."));
			return;
		}
		EntityPlayer player = getCommandSenderAsPlayer(sender);
		Clowder me = Clowder.getClowderFromPlayer(player);
		Clowder target = Clowder.getClowderFromName(targetName);
		WarValidation validation = validateWarDeclaration(sender, player, me, target);
		if(!validation.ok)
			return;

		long now = System.currentTimeMillis();
		String confirmKey = warConfirmationKey(player, target);
		WarConfirmation pending = WAR_CONFIRMATIONS.get(confirmKey);
		if(pending == null || pending.expiresAt < now || !pending.attacker.equals(me.name) || !pending.target.equals(target.name)) {
			WAR_CONFIRMATIONS.put(confirmKey, new WarConfirmation(me.name, target.name, now + 10000L));
			sender.addChatMessage(new ChatComponentText(CRITICAL + "Declaring war on " + target.name + " will cost " + Clowder.round(validation.cost) + " prestige."));
			sender.addChatMessage(new ChatComponentText(CRITICAL + "War upkeep starts at " + Clowder.round(XFConfig.activeWarUpkeep) + " prestige per hour and rises every hour."));
			sender.addChatMessage(new ChatComponentText(INFO + "Run the same war command against " + target.name + " again within 10 seconds to confirm."));
			return;
		}

		WAR_CONFIRMATIONS.remove(confirmKey);
		validation = validateWarDeclaration(sender, player, me, target);
		if(!validation.ok)
			return;

		me.addPrestige(-validation.cost, player.worldObj);
		me.activeWars.add(target.name);
		target.activeWars.add(me.name);
		me.warDeclaredAt.put(target.name, now);
		target.warDeclaredAt.put(me.name, now);
		ClowderData.getData(player.worldObj).markDirty();
		MinecraftServer.getServer().getConfigurationManager().sendChatMsg(new ChatComponentText(
				EnumChatFormatting.DARK_RED + "[WAR] " + EnumChatFormatting.GOLD + me.name + EnumChatFormatting.RED + " has declared war on " + EnumChatFormatting.GOLD + target.name + EnumChatFormatting.RED + "!"));
	}

	private WarValidation validateWarDeclaration(ICommandSender sender, EntityPlayer player, Clowder me, Clowder target) {
		if (me == null) { sender.addChatMessage(new ChatComponentText(ERROR + "You are not in any faction!")); return new WarValidation(false, 0F); }
		if (target == null || me == target) { sender.addChatMessage(new ChatComponentText(ERROR + "That faction does not exist.")); return new WarValidation(false, 0F); }
		if (me.getPermLevel(player.getDisplayName()) < 3) { sender.addChatMessage(new ChatComponentText(ERROR + "You lack the permissions to declare war.")); return new WarValidation(false, 0F); }
		if (areFactionsAtWar(me, target)) { sender.addChatMessage(new ChatComponentText(ERROR + "Your faction is already at war with " + target.name + ".")); return new WarValidation(false, 0F); }
		if(!(CommandClowderAdmin.LEGACY_WAR_ENABLED || CommandClowderAdmin.WAR_ONLINE_CHECK_DISABLED) && target.getOnlineMemberCount() < XFConfig.warOnlinePlayerThreshold && !Clowder.forceOnline) {
			sender.addChatMessage(new ChatComponentText(ERROR + "You can only declare war on factions that are currently online (" + XFConfig.warOnlinePlayerThreshold + "+ members)."));
			return new WarValidation(false, 0F);
		}
		if (!XFConfig.alliesCanDeclareWarOnEachOther && me.allies.containsKey(target)) {
			sender.addChatMessage(new ChatComponentText(ERROR + "You can not declare war on an ally."));
			return new WarValidation(false, 0F);
		}
		long now = System.currentTimeMillis();
		if(!CommandClowderAdmin.WAR_COOLDOWNS_DISABLED) {
			Long cd = me.noWarUntil.get(target.name);
			if (cd != null && cd > now) { sender.addChatMessage(new ChatComponentText(ERROR + "You cannot declare war on " + target.name + " for " + formatDuration(cd - now) + ".")); return new WarValidation(false, 0F); }
			Long allyCd = me.formerAllyNoWarUntil.get(target.name);
			if (allyCd != null && allyCd > now) { sender.addChatMessage(new ChatComponentText(ERROR + "You recently broke alliance with " + target.name + "; wait " + formatDuration(allyCd - now) + ".")); return new WarValidation(false, 0F); }
		}
		if(me.isInfrastructureDisabled()) {
			sender.addChatMessage(new ChatComponentText(ERROR + "Fallen Nations cannot declare war."));
			return new WarValidation(false, 0F);
		}
		float declarationCost = me.getWarDeclarationCost(target);
		if(me.getPrestige() < declarationCost) {
			sender.addChatMessage(new ChatComponentText(ERROR + "Declaring war requires " + Clowder.round(declarationCost) + " prestige."));
			return new WarValidation(false, declarationCost);
		}
		return new WarValidation(true, declarationCost);
	}

	private void cmdListWars(ICommandSender sender) {
		EntityPlayer player = getCommandSenderAsPlayer(sender);
		Clowder me = Clowder.getClowderFromPlayer(player);
		if(me == null) { sender.addChatMessage(new ChatComponentText(ERROR + "You are not in any faction!")); return; }
		LinkedHashSet<String> enemies = collectWarEnemyNames(me);
		if(enemies.isEmpty()) { sender.addChatMessage(new ChatComponentText(INFO + "Your faction has no active wars.")); return; }
		sender.addChatMessage(new ChatComponentText(TITLE + "Active wars for " + me.name + ":"));
		for(String enemyName : enemies) {
			Clowder enemy = Clowder.getClowderFromName(enemyName);
			long started = getWarStartedAt(me, enemy);
			String declaredBy = me.warDeclaredAt.containsKey(enemyName) ? me.name : enemy != null && enemy.warDeclaredAt.containsKey(me.name) ? enemy.name : "Unknown";
			String duration = started > 0L ? formatDuration(System.currentTimeMillis() - started) : "unknown duration";
			String state = enemy != null && me.canRaid(enemy) ? "raidable" : "active / raid-gated";
			sender.addChatMessage(new ChatComponentText(LIST + " - " + enemyName + " | declared by: " + declaredBy + " | duration: " + duration + " | state: " + state));
		}
	}

	private void cmdRequestPeace(ICommandSender sender, String targetName, String transferCity) {
		if (CommandClowderAdmin.LEGACY_WAR_ENABLED) {
			sender.addChatMessage(new ChatComponentText(ERROR + "Legacy war mode is enabled: peace/ceasefire/surrender mechanics are disabled."));
			return;
		}
		EntityPlayer player = getCommandSenderAsPlayer(sender);
		Clowder me = Clowder.getClowderFromPlayer(player);
		Clowder target = Clowder.getClowderFromName(targetName);
		if (me == null || target == null || me == target) return;
		if (me.getPermLevel(player.getDisplayName()) < 3) return;
		me.peaceRequests.add(target.name);
		if(transferCity != null && !transferCity.trim().isEmpty()) {
			if(!XFConfig.peaceCityTransfersEnabled) { sender.addChatMessage(new ChatComponentText(ERROR + "Peace city transfers are disabled on this server.")); return; }
			TerritoryMeta city = ClowderTerritory.getCityByName(me, transferCity.trim());
			if(city == null) {
				sender.addChatMessage(new ChatComponentText(ERROR + "Unknown city: " + transferCity));
				return;
			}
			me.peaceRequests.add(target.name + ":city:" + city.cityName);
			target.notifyAll(player.worldObj, new ChatComponentText(INFO + me.name + " has offered peace including transfer of " + city.cityName + ". Use /c acceptpeace " + me.name));
		} else {
			target.notifyAll(player.worldObj, new ChatComponentText(INFO + me.name + " has offered peace. Use /c acceptpeace " + me.name));
		}
	}
	private void cmdAcceptPeace(ICommandSender sender, String targetName) {
		if (CommandClowderAdmin.LEGACY_WAR_ENABLED) {
			sender.addChatMessage(new ChatComponentText(ERROR + "Legacy war mode is enabled: peace/ceasefire/surrender mechanics are disabled."));
			return;
		}
		EntityPlayer player = getCommandSenderAsPlayer(sender);
		Clowder me = Clowder.getClowderFromPlayer(player);
		Clowder target = Clowder.getClowderFromName(targetName);
		if (me == null || target == null || me == target || me.getPermLevel(player.getDisplayName()) < 3) return;
		if(!(CommandClowderAdmin.LEGACY_WAR_ENABLED || CommandClowderAdmin.WAR_STATE_CHECK_DISABLED) && !me.isAtWarWith(target)) return;
		if(!target.peaceRequests.contains(me.name)) return;
		target.peaceRequests.remove(me.name);
		String prefix = me.name + ":city:";
		for(Object requestObj : new ArrayList(target.peaceRequests)) {
			String request = (String)requestObj;
			if(request.startsWith(prefix)) {
				if(!XFConfig.peaceCityTransfersEnabled) { target.peaceRequests.remove(request); continue; }
				String cityName = request.substring(prefix.length());
				TerritoryMeta city = ClowderTerritory.getCityByName(target, cityName);
				if(city != null) {
					int chunks = ClowderTerritory.transferCity(player.worldObj, city, me);
					MinecraftServer.getServer().getConfigurationManager().sendChatMsg(new ChatComponentText(EnumChatFormatting.GREEN + "[WAR] " + cityName + " transferred to " + me.name + " (" + chunks + " chunks)."));
				}
				target.peaceRequests.remove(request);
			}
		}
		me.clearWarStateWith(target); target.clearWarStateWith(me);
		if(!CommandClowderAdmin.WAR_COOLDOWNS_DISABLED) {
			long until = System.currentTimeMillis() + XFConfig.peaceCooldownMs;
			me.noWarUntil.put(target.name, until); target.noWarUntil.put(me.name, until);
		}
		MinecraftServer.getServer().getConfigurationManager().sendChatMsg(new ChatComponentText(EnumChatFormatting.GREEN + "[WAR] " + me.name + " and " + target.name + " have agreed to peace."));
	}
	private void cmdRequestCeasefire(ICommandSender sender, String targetName) {
		if (CommandClowderAdmin.LEGACY_WAR_ENABLED) {
			sender.addChatMessage(new ChatComponentText(ERROR + "Legacy war mode is enabled: peace/ceasefire/surrender mechanics are disabled."));
			return;
		}
		EntityPlayer player = getCommandSenderAsPlayer(sender);
		Clowder me = Clowder.getClowderFromPlayer(player);
		Clowder target = Clowder.getClowderFromName(targetName);
		if (me == null || target == null || me == target || me.getPermLevel(player.getDisplayName()) < 3) return;
		if(!(CommandClowderAdmin.LEGACY_WAR_ENABLED || CommandClowderAdmin.WAR_STATE_CHECK_DISABLED) && !me.isAtWarWith(target)) return;
		me.ceasefireRequests.add(target.name);
		target.notifyAll(player.worldObj, new ChatComponentText(INFO + me.name + " has proposed a ceasefire. Use /c acceptceasefire " + me.name));
	}
	private void cmdAcceptCeasefire(ICommandSender sender, String targetName) {
		if (CommandClowderAdmin.LEGACY_WAR_ENABLED) {
			sender.addChatMessage(new ChatComponentText(ERROR + "Legacy war mode is enabled: peace/ceasefire/surrender mechanics are disabled."));
			return;
		}
		EntityPlayer player = getCommandSenderAsPlayer(sender);
		Clowder me = Clowder.getClowderFromPlayer(player);
		Clowder target = Clowder.getClowderFromName(targetName);
		if (me == null || target == null || me == target || me.getPermLevel(player.getDisplayName()) < 3) return;
		if(!target.ceasefireRequests.contains(me.name)) return;
		target.ceasefireRequests.remove(me.name);
		me.clearWarStateWith(target); target.clearWarStateWith(me);
		if(!CommandClowderAdmin.WAR_COOLDOWNS_DISABLED) {
			long until = System.currentTimeMillis() + XFConfig.ceasefireCooldownMs;
			me.noWarUntil.put(target.name, until); target.noWarUntil.put(me.name, until);
		}
	}
	private void cmdSurrender(ICommandSender sender, String targetName) {
		if (CommandClowderAdmin.LEGACY_WAR_ENABLED) {
			sender.addChatMessage(new ChatComponentText(ERROR + "Legacy war mode is enabled: peace/ceasefire/surrender mechanics are disabled."));
			return;
		}
		EntityPlayer player = getCommandSenderAsPlayer(sender);
		Clowder me = Clowder.getClowderFromPlayer(player);
		Clowder target = Clowder.getClowderFromName(targetName);
		if (me == null || target == null || me == target || me.getPermLevel(player.getDisplayName()) < 3) return;
		if(!(CommandClowderAdmin.LEGACY_WAR_ENABLED || CommandClowderAdmin.WAR_STATE_CHECK_DISABLED) && !me.isAtWarWith(target)) return;
		me.surrenderRequests.add(target.name);
		target.notifyAll(player.worldObj, new ChatComponentText(CRITICAL + me.name + " offers surrender. Use /c acceptsurrender " + me.name + " to accept."));
	}
	private void cmdAcceptSurrender(ICommandSender sender, String targetName) {
		if (CommandClowderAdmin.LEGACY_WAR_ENABLED) {
			sender.addChatMessage(new ChatComponentText(ERROR + "Legacy war mode is enabled: peace/ceasefire/surrender mechanics are disabled."));
			return;
		}
		EntityPlayer player = getCommandSenderAsPlayer(sender);
		Clowder me = Clowder.getClowderFromPlayer(player);
		Clowder target = Clowder.getClowderFromName(targetName);
		if (me == null || target == null || me == target || me.getPermLevel(player.getDisplayName()) < 3) return;
		if(!target.surrenderRequests.contains(me.name)) return;
		target.surrenderRequests.remove(me.name);
		if(XFConfig.surrenderTransfersCities) {
			for(Object cityObj : ClowderTerritory.getCityClaims(target)) {
				TerritoryMeta city = (TerritoryMeta)cityObj;
				ClowderTerritory.transferCity(player.worldObj, city, me);
			}
		}
		target.beginSurrenderTribute(me, player.worldObj);
		me.clearWarStateWith(target); target.clearWarStateWith(me);
		if(!CommandClowderAdmin.WAR_COOLDOWNS_DISABLED) {
			long until = System.currentTimeMillis() + XFConfig.surrenderCooldownMs;
			me.noWarUntil.put(target.name, until); target.noWarUntil.put(me.name, until);
		}
		MinecraftServer.getServer().getConfigurationManager().sendChatMsg(new ChatComponentText(EnumChatFormatting.GREEN + "[WAR] " + me.name + " accepted " + target.name + "'s surrender. " + target.name + " will pay " + Math.round(XFConfig.surrenderPrestigeTransferPercent * 100F) + "% of prestige generation to " + me.name + " for " + (XFConfig.surrenderTributeDurationMs / (60L * 60L * 1000L)) + " hours."));
	}

	private void cmdDefendAlly(ICommandSender sender, String allyName) {
		if(!XFConfig.alliesCanJoinWars) { sender.addChatMessage(new ChatComponentText(ERROR + "Allies joining wars is disabled on this server.")); return; }
		EntityPlayer player = getCommandSenderAsPlayer(sender);
		Clowder me = Clowder.getClowderFromPlayer(player);
		Clowder ally = Clowder.getClowderFromName(allyName);
		if (me == null || ally == null) return;
		if (me.getPermLevel(player.getDisplayName()) < 3) return;
		if (!me.allies.containsKey(ally)) return;
		int joined = 0;
		for (String enemyName : ally.activeWars) {
			if (!enemyName.equals(me.name)) {
				me.activeWars.add(enemyName);
				me.warDeclaredAt.put(enemyName, System.currentTimeMillis());
				Clowder enemy = Clowder.getClowderFromName(enemyName);
				if (enemy != null) {
					enemy.activeWars.add(me.name);
					enemy.warDeclaredAt.put(me.name, System.currentTimeMillis());
				}
				joined++;
			}
		}
		me.defendingAllies.add(ally.name);
		ClowderData.getData(player.worldObj).markDirty();
		sender.addChatMessage(new ChatComponentText(INFO + "Joined " + joined + " war(s) to defend ally " + ally.name + "."));
	}

	@Override
	public List addTabCompletionOptions(ICommandSender sender, String[] args) {
		if(args.length == 1)
			return getListOfStringsMatchingLastWord(args, getPlayerCommandNames());

		String cmd = args[0].toLowerCase();
		if(isPlayerCompletionCommand(cmd))
			return getListOfStringsMatchingLastWord(args, MinecraftServer.getServer().getAllUsernames());

		if(isFactionCompletionCommand(cmd))
			return getListOfStringsMatchingLastWord(args, getFactionCompletionNames());

		if(cmd.equals("flag"))
			return getListOfStringsMatchingLastWord(args, getFlagCompletionNames());

		return null;
	}

	private static boolean areFactionsAtWar(Clowder a, Clowder b) {
		return a != null && b != null && (a.activeWars.contains(b.name) || b.activeWars.contains(a.name));
	}

	private static LinkedHashSet<String> collectWarEnemyNames(Clowder clowder) {
		LinkedHashSet<String> enemies = new LinkedHashSet<String>();
		if(clowder == null)
			return enemies;
		enemies.addAll(clowder.activeWars);
		for(Clowder other : Clowder.clowders) {
			if(other != null && other != clowder && other.activeWars.contains(clowder.name))
				enemies.add(other.name);
		}
		enemies.remove(clowder.name);
		return enemies;
	}

	private static String formatAllyList(Clowder clowder) {
		if(clowder == null || clowder.allies.isEmpty())
			return "None";
		StringBuilder sb = new StringBuilder();
		for(Clowder ally : clowder.allies.keySet()) {
			if(ally == null)
				continue;
			if(sb.length() > 0)
				sb.append(", ");
			sb.append(ally.name);
		}
		return sb.length() == 0 ? "None" : sb.toString();
	}

	private static String formatStringSet(java.util.Set<String> values) {
		if(values == null || values.isEmpty())
			return "None";
		StringBuilder sb = new StringBuilder();
		for(String value : values) {
			if(value == null || value.isEmpty())
				continue;
			if(sb.length() > 0)
				sb.append(", ");
			sb.append(value);
		}
		return sb.length() == 0 ? "None" : sb.toString();
	}

	private static String formatEnemyList(Clowder clowder) {
		LinkedHashSet<String> enemies = collectWarEnemyNames(clowder);
		if(enemies.isEmpty())
			return "None";
		StringBuilder sb = new StringBuilder();
		for(String enemy : enemies) {
			if(sb.length() > 0)
				sb.append(", ");
			sb.append(enemy);
		}
		return sb.toString();
	}

	private static long getWarStartedAt(Clowder a, Clowder b) {
		long started = 0L;
		if(a != null && b != null) {
			Long ab = a.warDeclaredAt.get(b.name);
			Long ba = b.warDeclaredAt.get(a.name);
			if(ab != null) started = ab.longValue();
			if(ba != null && (started == 0L || ba.longValue() < started)) started = ba.longValue();
		}
		return started;
	}

	private static String warConfirmationKey(EntityPlayer player, Clowder target) {
		return player.getDisplayName() + ":" + (target == null ? "" : target.name.toLowerCase());
	}

	private static String formatDuration(long millis) {
		long seconds = Math.max(0L, millis / 1000L);
		long hours = seconds / 3600L;
		long minutes = (seconds % 3600L) / 60L;
		long secs = seconds % 60L;
		if(hours > 0L) return hours + "h " + minutes + "m";
		if(minutes > 0L) return minutes + "m " + secs + "s";
		return secs + "s";
	}

	private static class WarConfirmation {
		final String attacker;
		final String target;
		final long expiresAt;
		WarConfirmation(String attacker, String target, long expiresAt) { this.attacker = attacker; this.target = target; this.expiresAt = expiresAt; }
	}

	private static class WarValidation {
		final boolean ok;
		final float cost;
		WarValidation(boolean ok, float cost) { this.ok = ok; this.cost = cost; }
	}

	private String[] getPlayerCommandNames() {
		return new String[] { "help", "create", "info", "list", "comrades", "alliance", "allies", "allylist", "leave", "apply",
				"applicants", "accept", "deny", "kick", "owner", "promote", "demote", "rename", "color", "motd",
				"listflags", "flag", "gracebuild", "sethome", "home", "setwarp", "addwarp", "delwarp", "warp", "warps",
				"claim", "city", "nameclaim", "balance", "deposit", "withdraw", "befriend", "ally", "acceptfriend",
				"acceptally", "unfriend", "unally", "setallywarp", "allywarp", "merge", "acceptmerge", "declarewar", "war", "listwars",
				"peace", "acceptpeace", "ceasefire", "acceptceasefire", "surrender", "acceptsurrender", "defendally" };
	}

	private boolean isPlayerCompletionCommand(String cmd) {
		return cmd.equals("owner") || cmd.equals("accept") || cmd.equals("acceptfriend") || cmd.equals("acceptally")
				|| cmd.equals("deny") || cmd.equals("kick") || cmd.equals("promote") || cmd.equals("demote");
	}

	private boolean isFactionCompletionCommand(String cmd) {
		return cmd.equals("info") || cmd.equals("apply") || cmd.equals("befriend") || cmd.equals("ally")
				|| cmd.equals("unfriend") || cmd.equals("unally") || cmd.equals("allywarp") || cmd.equals("merge")
				|| cmd.equals("acceptmerge") || cmd.equals("declarewar") || cmd.equals("war") || cmd.equals("peace") || cmd.equals("acceptpeace")
				|| cmd.equals("ceasefire") || cmd.equals("acceptceasefire") || cmd.equals("surrender")
				|| cmd.equals("acceptsurrender") || cmd.equals("defendally");
	}

	private String[] getFactionCompletionNames() {
		String[] names = new String[Clowder.clowders.size()];
		for(int i = 0; i < Clowder.clowders.size(); i++)
			names[i] = Clowder.canonicalizeClowderName(Clowder.clowders.get(i).name);
		return names;
	}

	private String[] getFlagCompletionNames() {
		ClowderFlag[] flags = ClowderFlag.values();
		String[] names = new String[flags.length];
		for(int i = 0; i < flags.length; i++)
			names[i] = flags[i].name;
		return names;
	}

	public static final String ERROR = EnumChatFormatting.RED.toString();
	public static final String CRITICAL = EnumChatFormatting.DARK_RED.toString();
	public static final String TITLE = EnumChatFormatting.GOLD.toString();
	public static final String LIST = EnumChatFormatting.BLUE.toString();
	public static final String HELP = EnumChatFormatting.DARK_GREEN.toString();
	public static final String INFO = EnumChatFormatting.GREEN.toString();
	public static final String COMMAND = EnumChatFormatting.RED.toString();
	public static final String COMMAND_LEADER = EnumChatFormatting.DARK_RED.toString();
	public static final String COMMAND_ADMIN = EnumChatFormatting.DARK_PURPLE.toString();
}
