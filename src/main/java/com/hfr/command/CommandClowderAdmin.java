package com.hfr.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.hfr.clowder.Clowder;
import com.hfr.clowder.ClowderTerritory;
import com.hfr.clowder.ClowderTerritory.CoordPair;
import com.hfr.clowder.ClowderTerritory.Zone;
import com.hfr.clowder.ClowderEvents;
import com.hfr.clowder.PlayerProtectionData;
import com.hfr.config.XFConfig;
import com.hfr.guide.XFGuideBook;
import com.hfr.data.ClowderData;
import com.hfr.packet.PacketDispatcher;
import com.hfr.packet.effect.ClowderFlagPacket;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

public class CommandClowderAdmin extends CommandBase {

	public static boolean WARENABLED = false;
	public static boolean WAR_COOLDOWNS_DISABLED = false;
	public static boolean WAR_ONLINE_CHECK_DISABLED = false;
	public static boolean WAR_STATE_CHECK_DISABLED = false;
	public static boolean LEGACY_WAR_ENABLED = false;

	@Override
	public String getCommandName() {
		return "xclowder";
	}

	@Override
    public List getCommandAliases()
    {
        return new ArrayList() {{ add("xclowder"); add("xc"); }};
    }

	@Override
	public String getCommandUsage(ICommandSender sender) {
		return "/xclowder help";
	}

	@Override
    public int getRequiredPermissionLevel()
    {
        return 3;
    }

	@Override
	public void processCommand(ICommandSender sender, String[] args) {


		if(Clowder.clowders.size() == 0)
			ClowderData.getData(sender.getEntityWorld());

		if(args.length < 1) {
			sender.addChatMessage(new ChatComponentText(ERROR + getCommandUsage(sender)));
			return;
		}

		String cmd = args[0].toLowerCase();

		if(cmd.equals("help") || cmd.equals("man")) { cmdHelp(sender, args.length > 1 ? args[1] : "1"); return; }
		if(cmd.equals("forcejoin") || cmd.equals("fj")) { if(!requireArgs(sender, cmd, args, 2)) return; cmdForcejoin(sender, joinArgs(args, 1)); return; }
		if(cmd.equals("forcekick") || cmd.equals("fk")) { if(!requireArgs(sender, cmd, args, 2)) return; cmdForcekick(sender, args[1]); return; }
		if(cmd.equals("forcedisband") || cmd.equals("fd")) { if(!requireArgs(sender, cmd, args, 2)) return; cmdForcedisband(sender, joinArgs(args, 1)); return; }
		if(cmd.equals("forcerename") || cmd.equals("fr")) { if(!requireArgs(sender, cmd, args, 2)) return; cmdForceRename(sender, joinArgs(args, 1)); return; }
		if(cmd.equals("hijack") || cmd.equals("hi")) { cmdHijack(sender); return; }
		if(cmd.equals("deletedata") || cmd.equals("deldat")) { cmdDeletedata(sender); return; }
		if(cmd.equals("setclaim") || cmd.equals("sc")) { if(!requireArgs(sender, cmd, args, 4)) return; cmdSetclaim(sender, args[1], args[2], args[3]); return; }
		if(cmd.equals("addprestige") || cmd.equals("ap") || cmd.equals("addprestig")) { if(!requireArgs(sender, cmd, args, 3)) return; cmdAddPrestige(sender, joinArgs(args, 1, args.length - 1), args[args.length - 1]); return; }
		if(cmd.equals("disband")) { if(!requireArgs(sender, cmd, args, 2)) return; cmdDisband(sender, joinArgs(args, 1)); return; }
		if(cmd.equals("rename")) { if(!requireArgs(sender, cmd, args, 2)) return; cmdRename(sender, joinArgs(args, 1)); return; }

		if (cmd.equals("warenable")) {
			WARENABLED = true;
			sender.addChatMessage(new ChatComponentText(INFO + "War declarations enabled!"));

			// Notify and play sound for all players
			for (Object obj : MinecraftServer.getServer().getConfigurationManager().playerEntityList) {
				if (obj instanceof EntityPlayerMP) {
					EntityPlayerMP player = (EntityPlayerMP) obj;

					// Broadcast message
					player.addChatMessage(new ChatComponentText(INFO + "⚔ War declarations have been ENABLED!"));

					// Play Wither spawn sound at each player’s position
					player.worldObj.playSoundEffect(
							player.posX,
							player.posY,
							player.posZ,
							"mob.wither.spawn",
							5.0F, // volume
							0.5F  // pitch
					);
				}
			}

			return;
		}

		if(cmd.equals("wardisable")) {
			WARENABLED = false;
			for (Object obj : MinecraftServer.getServer().getConfigurationManager().playerEntityList) {
				if (obj instanceof EntityPlayerMP) {
					EntityPlayerMP player = (EntityPlayerMP) obj;
					player.worldObj.playSoundEffect(
							player.posX,
							player.posY,
							player.posZ,
							"mob.wither.death",
							5.0F, // volume
							0.5F  // pitch
					);
					player.addChatMessage(new ChatComponentText(INFO + "⚔ War declarations have been DISABLED!"));
				}
			}
			for (Clowder c : Clowder.clowders) {
				c.activeWars.clear();
				c.defendingAllies.clear();
			}
			sender.addChatMessage(new ChatComponentText(INFO + "War declarations disabled; active wars cleared."));
			return;
		}

		if(cmd.equals("newplayerprotection")) { if(!XFConfig.enableNewPlayerProtection) { sender.addChatMessage(new ChatComponentText(ERROR + "New-player protection module is disabled in the config.")); return; } ClowderEvents.newPlayerProtectionEnabled = !ClowderEvents.newPlayerProtectionEnabled; sender.addChatMessage(new ChatComponentText(INFO + "New player protection is now " + (ClowderEvents.newPlayerProtectionEnabled ? "enabled" : "disabled") + ".")); return; }
		if(cmd.equals("resetnewplayerprotection")) { cmdResetNewPlayerProtection(sender); return; }
		if(cmd.equals("endnewplayerprotection")) { cmdEndNewPlayerProtection(sender); return; }
		if(cmd.equals("skipwarcooldowns")) { WAR_COOLDOWNS_DISABLED = !WAR_COOLDOWNS_DISABLED; sender.addChatMessage(new ChatComponentText(INFO + "War cooldown skipping is now " + (WAR_COOLDOWNS_DISABLED ? "ENABLED" : "DISABLED") + ".")); return; }
		if(cmd.equals("ignorewarcooldowncheck")) { WAR_COOLDOWNS_DISABLED = !WAR_COOLDOWNS_DISABLED; sender.addChatMessage(new ChatComponentText(INFO + "War cooldown checks are now " + (WAR_COOLDOWNS_DISABLED ? "IGNORED" : "ENFORCED") + ".")); return; }
		if(cmd.equals("ignorewaronlinecheck")) { WAR_ONLINE_CHECK_DISABLED = !WAR_ONLINE_CHECK_DISABLED; sender.addChatMessage(new ChatComponentText(INFO + "War online checks are now " + (WAR_ONLINE_CHECK_DISABLED ? "IGNORED" : "ENFORCED") + ".")); return; }
		if(cmd.equals("ignorewarstatecheck")) { WAR_STATE_CHECK_DISABLED = !WAR_STATE_CHECK_DISABLED; sender.addChatMessage(new ChatComponentText(INFO + "War state checks are now " + (WAR_STATE_CHECK_DISABLED ? "IGNORED" : "ENFORCED") + ".")); return; }
		if(cmd.equals("skipwarcooldown")) {
			for (Clowder c : Clowder.clowders) { c.noWarUntil.clear(); c.formerAllyNoWarUntil.clear(); }
			sender.addChatMessage(new ChatComponentText(INFO + "All faction war cooldowns have been reset to 0."));
			return;
		}
		if(cmd.equals("enablelegacywar")) {
			LEGACY_WAR_ENABLED = !LEGACY_WAR_ENABLED;
			if(LEGACY_WAR_ENABLED) { WAR_COOLDOWNS_DISABLED = true; WAR_ONLINE_CHECK_DISABLED = true; WAR_STATE_CHECK_DISABLED = true; }
			sender.addChatMessage(new ChatComponentText(INFO + "Legacy war mode is now " + (LEGACY_WAR_ENABLED ? "ENABLED" : "DISABLED") + "."));
			return;
		}
		if(cmd.equals("resetbuildgrace") || cmd.equals("rbg")) { if(!requireArgs(sender, cmd, args, 2)) return; cmdResetBuildGrace(sender, joinArgs(args, 1)); return; }
		if(cmd.equals("endbuildgrace") || cmd.equals("ebg")) { if(!requireArgs(sender, cmd, args, 2)) return; cmdEndBuildGrace(sender, joinArgs(args, 1)); return; }

		sender.addChatMessage(new ChatComponentText(ERROR + "Unknown command. Usage: " + getCommandUsage(sender)));
	}

	private boolean requireArgs(ICommandSender sender, String cmd, String[] args, int minArgs) {
		if(args.length >= minArgs)
			return true;
		sender.addChatMessage(new ChatComponentText(ERROR + "Invalid format. Usage: " + getUsageFor(cmd)));
		return false;
	}

	private String joinArgs(String[] args, int start) {
		return joinArgs(args, start, args.length);
	}

	private String joinArgs(String[] args, int start, int end) {
		return String.join(" ", Arrays.copyOfRange(args, start, end));
	}

	private String getUsageFor(String cmd) {
		if(cmd.equals("forcejoin") || cmd.equals("fj")) return "/xc forcejoin <faction>";
		if(cmd.equals("forcekick") || cmd.equals("fk")) return "/xc forcekick <player>";
		if(cmd.equals("forcedisband") || cmd.equals("fd")) return "/xc forcedisband <faction>";
		if(cmd.equals("forcerename") || cmd.equals("fr")) return "/xc forcerename <name>";
		if(cmd.equals("setclaim") || cmd.equals("sc")) return "/xc setclaim <wild/safe/war> <s/c> <radius>";
		if(cmd.equals("addprestige") || cmd.equals("ap") || cmd.equals("addprestig")) return "/xc addprestige <faction> <amount>";
		if(cmd.equals("disband")) return "/xc disband <faction>";
		if(cmd.equals("rename")) return "/xc rename <name>";
		if(cmd.equals("resetbuildgrace") || cmd.equals("rbg")) return "/xc resetbuildgrace <faction>";
		if(cmd.equals("endbuildgrace") || cmd.equals("ebg")) return "/xc endbuildgrace <faction>";
		return getCommandUsage(null);
	}

	
	private void cmdHelp(ICommandSender sender, String page) {

		int p = this.parseInt(sender, page);
		int pages = 3;

		if(p < 1 || p > pages)
			p = 1;

		sender.addChatMessage(new ChatComponentText(HELP + "/xclowder [command] <args...>"));
		sender.addChatMessage(new ChatComponentText(INFO + "Admin commands [" + p + "/" + pages + "]:"));

		if(p == 1) {
			sender.addChatMessage(new ChatComponentText(TITLE + "Faction administration"));
			sender.addChatMessage(new ChatComponentText(COMMAND_ADMIN + "-forcejoin <faction>" + TITLE + " - Forcefully joins a faction"));
			sender.addChatMessage(new ChatComponentText(INFO + "Handbook: " + XFGuideBook.getFallbackHelp()));
			sender.addChatMessage(new ChatComponentText(COMMAND_ADMIN + "-forcekick <player>" + TITLE + " - Forcefully kicks a player from their faction"));
			sender.addChatMessage(new ChatComponentText(COMMAND_ADMIN + "-forcedisband <faction>" + TITLE + " - Forcefully disbands a faction"));
			sender.addChatMessage(new ChatComponentText(COMMAND_ADMIN + "-forcerename <name>" + TITLE + " - Forcefully renames your faction"));
			sender.addChatMessage(new ChatComponentText(COMMAND_ADMIN + "-hijack" + TITLE + " - Forcefully overrides faction leadership"));
			sender.addChatMessage(new ChatComponentText(COMMAND_ADMIN + "-deletedata" + TITLE + " - Deletes all clowder data (CAUTION!!)"));
			sender.addChatMessage(new ChatComponentText(COMMAND_ADMIN + "-disband <faction>" + TITLE + " - Disbands your faction with confirmation"));
			sender.addChatMessage(new ChatComponentText(COMMAND_ADMIN + "-rename <name>" + TITLE + " - Renames your faction"));
			sender.addChatMessage(new ChatComponentText(INFO + "/xclowder help 2"));
		}

		if(p == 2) {
			sender.addChatMessage(new ChatComponentText(TITLE + "Claims, prestige & protection"));
			sender.addChatMessage(new ChatComponentText(COMMAND_ADMIN + "-setclaim <wild/safe/war> <s/c> <radius>" + TITLE + " - Claims chunks in a radius"));
			sender.addChatMessage(new ChatComponentText(COMMAND_ADMIN + "-addprestige <faction> <amount>" + TITLE + " - Adds prestige (negative values subtract)"));
			sender.addChatMessage(new ChatComponentText(COMMAND_ADMIN + "-newplayerprotection" + TITLE + " - Toggles starter protection"));
			sender.addChatMessage(new ChatComponentText(COMMAND_ADMIN + "-resetnewplayerprotection" + TITLE + " - Resets starter protection timers"));
			sender.addChatMessage(new ChatComponentText(COMMAND_ADMIN + "-endnewplayerprotection" + TITLE + " - Ends starter protection timers"));
			sender.addChatMessage(new ChatComponentText(COMMAND_ADMIN + "-resetbuildgrace <faction>" + TITLE + " - Resets faction build grace"));
			sender.addChatMessage(new ChatComponentText(COMMAND_ADMIN + "-endbuildgrace <faction>" + TITLE + " - Immediately ends faction build grace"));
			sender.addChatMessage(new ChatComponentText(INFO + "/xclowder help 3"));
		}

		if(p == 3) {
			sender.addChatMessage(new ChatComponentText(TITLE + "War controls"));
			sender.addChatMessage(new ChatComponentText(COMMAND_ADMIN + "-warenable" + TITLE + " - Enables war mode"));
			sender.addChatMessage(new ChatComponentText(COMMAND_ADMIN + "-wardisable" + TITLE + " - Disables war mode and clears active wars"));
			sender.addChatMessage(new ChatComponentText(COMMAND_ADMIN + "-skipwarcooldowns" + TITLE + " - Toggles global war cooldown bypass"));
			sender.addChatMessage(new ChatComponentText(COMMAND_ADMIN + "-skipwarcooldown" + TITLE + " - Clears all war cooldown timers"));
			sender.addChatMessage(new ChatComponentText(COMMAND_ADMIN + "-ignorewarcooldowncheck" + TITLE + " - Toggles cooldown check bypass"));
			sender.addChatMessage(new ChatComponentText(COMMAND_ADMIN + "-ignorewaronlinecheck" + TITLE + " - Toggles online member check bypass"));
			sender.addChatMessage(new ChatComponentText(COMMAND_ADMIN + "-ignorewarstatecheck" + TITLE + " - Toggles at-war state check bypass"));
			sender.addChatMessage(new ChatComponentText(COMMAND_ADMIN + "-enablelegacywar" + TITLE + " - Ignores war checks and disables treaty mechanics"));
		}
	}

	
	private void cmdForcejoin(ICommandSender sender, String name) {

		EntityPlayer player = getCommandSenderAsPlayer(sender);
		Clowder clowder = Clowder.getClowderFromPlayer(player);
		
		if(clowder == null) {
			
			Clowder tojoin = Clowder.getClowderFromName(name);
				
			if(tojoin != null) {

				tojoin.addMember(player.worldObj, player.getDisplayName());
				sender.addChatMessage(new ChatComponentText(INFO + "You have joined " + tojoin.getDecoratedName() + "!"));
				
			} else {
				sender.addChatMessage(new ChatComponentText(ERROR + "There is no faction with this name!"));
			}
			
		} else {
			sender.addChatMessage(new ChatComponentText(ERROR + "You are already in a faction!"));
		}
	}
	
	private void cmdForcekick(ICommandSender sender, String name) {

		EntityPlayer player = getCommandSenderAsPlayer(sender);
		EntityPlayer kickee = player.worldObj.getPlayerEntityByName(name);
		Clowder clowder = Clowder.getClowderFromPlayer(kickee);
		
		if(clowder != null) {
			
			if(!clowder.leader.equals(kickee.getDisplayName())) {
				
				clowder.removeMember(player.worldObj, kickee.getDisplayName());
				sender.addChatMessage(new ChatComponentText(INFO + "You have kicked " + kickee.getDisplayName() + " from the faction " + clowder.getDecoratedName() + "!"));
				
			} else {
				sender.addChatMessage(new ChatComponentText(ERROR + "You cannot kick a leader from his faction!"));
			}
			
		} else {
			sender.addChatMessage(new ChatComponentText(ERROR + "This player is not in a faction!"));
		}
	}
	
	private void cmdForcedisband(ICommandSender sender, String name) {

		EntityPlayer player = getCommandSenderAsPlayer(sender);
		Clowder clowder = Clowder.getClowderFromName(name);
		
		if(clowder != null) {
			
			clowder.disbandClowder(player.worldObj);
			sender.addChatMessage(new ChatComponentText(INFO + "Faction " + clowder.getDecoratedName() + " has been disbanded!"));
			
		} else {
			sender.addChatMessage(new ChatComponentText(ERROR + "There is no faction with this name!"));
		}
	}

	private void cmdForceRename(ICommandSender sender, String name) {

		EntityPlayer player = getCommandSenderAsPlayer(sender);
		Clowder clowder = Clowder.getClowderFromPlayer(player);
		String factionName = Clowder.canonicalizeClowderName(name);

		if(factionName.isEmpty()) { sender.addChatMessage(new ChatComponentText(ERROR + "Faction name cannot be empty.")); return; }

		if(clowder != null) {

			if(Clowder.getClowderFromName(factionName) == null) {

				clowder.rename(factionName, player);
				sender.addChatMessage(new ChatComponentText(TITLE + "Renamed faction to " + factionName + "!"));
				PacketDispatcher.wrapper.sendTo(new ClowderFlagPacket(clowder, ""), (EntityPlayerMP) player);

			} else {
				sender.addChatMessage(new ChatComponentText(ERROR + "This name is already taken!"));
			}

		} else {
			sender.addChatMessage(new ChatComponentText(ERROR + "You are not in any faction!"));
		}
	}
	
	private void cmdHijack(ICommandSender sender) {

		EntityPlayer player = getCommandSenderAsPlayer(sender);
		Clowder clowder = Clowder.getClowderFromPlayer(player);
		
		if(clowder != null) {
			
			if(!clowder.leader.equals(player.getDisplayName())) {
				
				clowder.transferOwnership(player.worldObj, player.getDisplayName());
				sender.addChatMessage(new ChatComponentText(INFO + "You have assumed ownership of this faction!"));
				
			} else {
				sender.addChatMessage(new ChatComponentText(ERROR + "You are already this faction's leader!"));
			}
			
		} else {
			sender.addChatMessage(new ChatComponentText(ERROR + "You are not in any faction!"));
		}
	}
	
	private void cmdDeletedata(ICommandSender sender) {

		EntityPlayer player = getCommandSenderAsPlayer(sender);

		ClowderTerritory.territories.clear();
		
		while(Clowder.clowders.size() > 0)
			Clowder.clowders.get(0).disbandClowder(player.worldObj);
		
		Clowder.inverseMap.clear();
		Clowder.retreating.clear();
		ClowderData.getData(player.worldObj).markDirty();
		sender.addChatMessage(new ChatComponentText(EnumChatFormatting.OBFUSCATED + "" + EnumChatFormatting.DARK_PURPLE + "All data has been deleted!"));
	}
	
	private void cmdSetclaim(ICommandSender sender, String zo, String s, String r) {

		EntityPlayer player = getCommandSenderAsPlayer(sender);
		Zone zone = zo.equals("war") ? Zone.WARZONE : zo.equals("safe") ? Zone.SAFEZONE : zo.equals("wild") ? Zone.WILDERNESS : null;
		int shape = s.equals("s") ? 0 : s.equals("c") ? 1 : -1;
		int radius = this.parseInt(sender, r);

		int xCoord = (int)player.posX;
		int zCoord = (int)player.posZ;
		
		if(zone != null) {
			
			if(shape >= 0) {
				
				if(shape == 0)
					radius--;
				
				if(radius < 0 || radius > 25) {
					sender.addChatMessage(new ChatComponentText(ERROR + "Invalid radius! Must be between 1 and 25"));
				} else {
					
					int count = 0;
					
					for(int x = -radius; x <= radius; x++) {
						for(int z = -radius; z <= radius; z++) {

							int posX = xCoord + x * 16;
							int posZ = zCoord + z * 16;
							CoordPair loc = ClowderTerritory.getCoordPair(posX, posZ);
							
							if(shape == 0 || Math.sqrt(Math.pow(x, 2) + Math.pow(z, 2)) < radius) {
								ClowderTerritory.setZoneForCoord(player.worldObj, loc, zone);
								count++;
							}
						}
					}
					
					sender.addChatMessage(new ChatComponentText(INFO + "Changed " + count + " chunks to " + zone.toString() + "!"));
				}
				
			} else {
				sender.addChatMessage(new ChatComponentText(ERROR + "Invalid shape! Applicable: s (square), c (circle)"));
			}
			
		} else {
			sender.addChatMessage(new ChatComponentText(ERROR + "Invalid zone! Applicable: wild, safe, war"));
		}
	}
	
	private void cmdAddPrestige(ICommandSender sender, String name, String amount) {

		EntityPlayer player = getCommandSenderAsPlayer(sender);
		Clowder clowder = Clowder.getClowderFromName(name);
		int am = this.parseInt(sender, amount);
		
		if(clowder != null) {
			
			clowder.addPrestige(am, player.worldObj);
			sender.addChatMessage(new ChatComponentText(INFO + "Added " + am + " prestige to faction " + clowder.getDecoratedName() + "!"));
			
		} else {
			sender.addChatMessage(new ChatComponentText(ERROR + "There is no faction with this name!"));
		}
	}
	
	//private void cmdCreate(ICommandSender sender, String name) {
//
	//	EntityPlayer player = getCommandSenderAsPlayer(sender);
	//
	//	if(Clowder.getClowderFromPlayer(player) == null) {
	//
	//		if(Clowder.getClowderFromName(name) == null) {
	//			Clowder.createClowder(player, name);
	//			sender.addChatMessage(new ChatComponentText(TITLE + "Created faction " + name + "!"));
	//			sender.addChatMessage(new ChatComponentText(INFO + "Use /c claim to get started!"));
	//		} else {
	//			sender.addChatMessage(new ChatComponentText(ERROR + "This name is already taken!"));
	//		}
	//
	//	} else {
	//		sender.addChatMessage(new ChatComponentText(ERROR + "You can not create a new faction while already being in one!"));
	//	}
	//} ur a fuckin idiot
	
	private void cmdDisband(ICommandSender sender, String name) {

		EntityPlayer player = getCommandSenderAsPlayer(sender);
		Clowder clowder = Clowder.getClowderFromPlayer(player);
		
		if(clowder != null) {
				
			if(Clowder.normalizeClowderName(name).equals(Clowder.normalizeClowderName(clowder.name))) {
				
				if(clowder.disbandClowder(player)) {
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

	private void cmdResetBuildGrace(ICommandSender sender, String name) {

		Clowder clowder = Clowder.getClowderFromName(name);

		if(clowder == null) {
			sender.addChatMessage(new ChatComponentText(ERROR + "Faction not found."));
			return;
		}

		long now = System.currentTimeMillis();

		// 24 hours
		clowder.buildGraceUntil = now + (24L * 60L * 60L * 1000L);

		// allow reuse
		clowder.buildGraceUsed = false;

		clowder.save(sender.getEntityWorld());

		sender.addChatMessage(new ChatComponentText(
				INFO + "Reset build grace for faction " + clowder.name + "."
		));

		clowder.notifyAll(sender.getEntityWorld(),
				new ChatComponentText(
						CommandClowder.INFO + "Your faction build grace has been reset by an administrator."
				));
	}

	private void cmdEndBuildGrace(ICommandSender sender, String name) {

		Clowder clowder = Clowder.getClowderFromName(name);

		if(clowder == null) {
			sender.addChatMessage(new ChatComponentText(ERROR + "Faction not found."));
			return;
		}

		clowder.buildGraceUntil = 0L;

		clowder.save(sender.getEntityWorld());

		sender.addChatMessage(new ChatComponentText(
				INFO + "Ended build grace for faction " + clowder.name + "."
		));

		clowder.notifyAll(sender.getEntityWorld(),
				new ChatComponentText(
						CommandClowder.CRITICAL + "Your faction build grace was ended by an administrator."
				));
	}
	private void cmdResetNewPlayerProtection(ICommandSender sender) {

		long now = System.currentTimeMillis();

		for(PlayerProtectionData.ProtectionEntry entry : PlayerProtectionData.getAll().values()) {

			entry.pvpGraceUntil = now + XFConfig.pvpGraceDurationMs;
			entry.keepInvUntil = now + XFConfig.keepInventoryDurationMs;
		}

		PlayerProtectionData.save();

		sender.addChatMessage(new ChatComponentText(
				INFO + "Reset new-player protection for all players."
		));
	}

	private void cmdEndNewPlayerProtection(ICommandSender sender) {

		for (PlayerProtectionData.ProtectionEntry entry : PlayerProtectionData.getAll().values()) {

			entry.pvpGraceUntil = 0L;
			entry.keepInvUntil = 0L;
		}

		PlayerProtectionData.save();

		sender.addChatMessage(new ChatComponentText(
				INFO + "Ended new-player protection for all players."
		));
	}
	
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
	
	@Override
    public List addTabCompletionOptions(ICommandSender sender, String[] args) {
		if(args.length == 1)
			return getListOfStringsMatchingLastWord(args, getAdminCommandNames());

		String cmd = args[0].toLowerCase();
		if(cmd.equals("forcekick") || cmd.equals("fk"))
			return getListOfStringsMatchingLastWord(args, MinecraftServer.getServer().getAllUsernames());

		if(isFactionCompletionCommand(cmd))
			return getListOfStringsMatchingLastWord(args, getFactionCompletionNames());

		return null;
    }

	private String[] getAdminCommandNames() {
		return new String[] { "help", "forcejoin", "fj", "forcekick", "fk", "forcedisband", "fd", "forcerename", "fr",
				"hijack", "hi", "deletedata", "deldat", "setclaim", "sc", "addprestige", "ap", "disband", "rename",
				"warenable", "wardisable", "newplayerprotection", "resetnewplayerprotection", "endnewplayerprotection",
				"skipwarcooldowns", "ignorewarcooldowncheck", "ignorewaronlinecheck", "ignorewarstatecheck",
				"skipwarcooldown", "enablelegacywar", "resetbuildgrace", "rbg", "endbuildgrace", "ebg" };
	}

	private boolean isFactionCompletionCommand(String cmd) {
		return cmd.equals("forcejoin") || cmd.equals("fj") || cmd.equals("forcedisband") || cmd.equals("fd")
				|| cmd.equals("addprestige") || cmd.equals("ap") || cmd.equals("addprestig") || cmd.equals("disband")
				|| cmd.equals("resetbuildgrace") || cmd.equals("rbg") || cmd.equals("endbuildgrace") || cmd.equals("ebg");
	}

	private String[] getFactionCompletionNames() {
		String[] names = new String[Clowder.clowders.size()];
		for(int i = 0; i < Clowder.clowders.size(); i++)
			names[i] = Clowder.canonicalizeClowderName(Clowder.clowders.get(i).name);
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
