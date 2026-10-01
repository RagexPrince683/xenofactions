package com.hfr.command;

import com.hfr.packet.PacketDispatcher;
import com.hfr.packet.effect.TDMMenuDataPacket;
import com.hfr.packet.effect.AdminEditorSnapshotPacket;
import com.hfr.tdm.TDMKitManager;
import com.hfr.tdm.TDMAdminKitEdit;
import com.hfr.tdm.AdminSelectionManager;
import com.hfr.tdm.AdminSelectionManager.Type;
import com.hfr.tdm.TDMBombManager;
import com.hfr.tdm.TDMManager;
import com.hfr.tdm.TDMMapOverlaySync;
import com.hfr.tdm.TDMPurchasableManager;
import com.hfr.config.XFConfig;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class CommandTDM extends CommandBase {

    private static final int TEAM_CHANGE_COOLDOWN_TICKS = 120 * 20;
    private final Map<String, Long> nextTeamChangeTick = new HashMap<String, Long>();

    @Override
    public String getCommandName() {
        return "tdm";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/tdm help";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        if (args.length == 0 || args[0].equalsIgnoreCase("help") || args[0].equalsIgnoreCase("man") || args[0].equalsIgnoreCase("?")) {
            sendHelp(sender, args.length > 1 ? args[1] : "general");
            return;
        }

        World world = sender.getEntityWorld();

        if (args[0].equalsIgnoreCase("overlay") || args[0].equalsIgnoreCase("boundaryview")) {
            EntityPlayerMP player = (EntityPlayerMP)getCommandSenderAsPlayer(sender);
            boolean boundaryView = args[0].equalsIgnoreCase("boundaryview");
            if (boundaryView && !isAdmin(sender)) {
                sender.addChatMessage(new ChatComponentText("In-world map boundary view is admin-only.")); return;
            }
            if (args.length < 2 || parseToggle(args[1]) == null) {
                sender.addChatMessage(new ChatComponentText("Usage: /tdm " + (boundaryView ? "boundaryview" : "overlay") + " <on|off>" + (boundaryView ? "" : " [map (admin only)]")));
                return;
            }
            boolean enabled = parseToggle(args[1]).booleanValue();
            if (boundaryView) TDMMapOverlaySync.setBoundary(player, enabled);
            else {
                String map = args.length > 2 && isAdmin(sender) ? TDMManager.normalizeMapName(args[2]) : "";
                if (map.length() > 0 && !TDMManager.hasMap(world, map)) {
                    sender.addChatMessage(new ChatComponentText("Unknown map: " + map)); return;
                }
                TDMMapOverlaySync.setOverlay(player, enabled, map);
            }
            sender.addChatMessage(new ChatComponentText((boundaryView ? "In-world map boundary" : "Persistent map overlay") + (enabled ? " enabled." : " disabled.")));
            return;
        }

        if (args[0].equalsIgnoreCase("maps") || args[0].equalsIgnoreCase("listmaps")) {
            sendMapList(sender, world);
            return;
        }

        if (args[0].equalsIgnoreCase("vote")) {
            if (args.length < 2) {
                sender.addChatMessage(new ChatComponentText("Usage: /tdm vote <map> <tdm|sd|ffa>"));
                return;
            }

            EntityPlayer player = getCommandSenderAsPlayer(sender);
            TDMManager.TDMGameMode voteMode = args.length > 2 ? TDMManager.parseMode(args[2]) : null;
            String votedMap = TDMManager.voteForMap(world, player.getCommandSenderName(), voteMode == null ? args[1] : TDMManager.pairId(args[1], voteMode));
            if (votedMap == null) {
                sender.addChatMessage(new ChatComponentText("Unable to vote. A TDM map vote must be active and the map must exist."));
                return;
            }

            sender.addChatMessage(new ChatComponentText("Voted for " + votedMap + "."));
            sendVoteCounts(sender, world);
            return;
        }

        if (args[0].equalsIgnoreCase("skip")) {
            EntityPlayer player = getCommandSenderAsPlayer(sender);
            if (args.length > 1 && args[1].equalsIgnoreCase("status")) {
                sender.addChatMessage(new ChatComponentText(TDMManager.getSkipVoteStatus(world)));
                return;
            }
            boolean yes = args.length < 2 || args[1].equalsIgnoreCase("yes");
            if (!yes && !args[1].equalsIgnoreCase("no")) {
                sender.addChatMessage(new ChatComponentText("Usage: /tdm skip [yes|no|status]"));
                return;
            }
            String result = TDMManager.castSkipVote(world, player, yes);
            if (result != null) sender.addChatMessage(new ChatComponentText(result));
            return;
        }
        if ((args[0].equalsIgnoreCase("utility") || args[0].equalsIgnoreCase("killstreak")) && args.length>=2 && args[1].equalsIgnoreCase("buy")) {
            EntityPlayer player=getCommandSenderAsPlayer(sender);int number;try{number=Integer.parseInt(args[2]);}catch(Exception e){sender.addChatMessage(new ChatComponentText("Usage: /tdm "+args[0]+" buy <number>"));return;}TDMPurchasableManager.Type type=args[0].equalsIgnoreCase("utility")?TDMPurchasableManager.Type.UTILITY:TDMPurchasableManager.Type.KILLSTREAK;boolean purchased=TDMPurchasableManager.purchase(player,type,number-1);sender.addChatMessage(new ChatComponentText(purchased?"Purchase accepted.":"Purchase rejected: unavailable, unauthorized, or insufficient funds."));return;
        }
        if ((args[0].equalsIgnoreCase("utility") || args[0].equalsIgnoreCase("killstreak")) && args.length>=2 && args[1].equalsIgnoreCase("list")) {
            TDMPurchasableManager.Type type=args[0].equalsIgnoreCase("utility")?TDMPurchasableManager.Type.UTILITY:TDMPurchasableManager.Type.KILLSTREAK;String map=TDMManager.getSelectedMap(sender.getEntityWorld());String[] names=TDMPurchasableManager.getNames(map,type);int[] costs=TDMPurchasableManager.getCosts(map,type);sender.addChatMessage(new ChatComponentText(args[0]+" purchases (use /tdm "+args[0]+" buy <number>):"));for(int i=0;i<names.length;i++)sender.addChatMessage(new ChatComponentText("  "+(i+1)+". "+names[i]+" — "+costs[i]+(type==TDMPurchasableManager.Type.UTILITY?" buy score":" kill score")));if(names.length==0)sender.addChatMessage(new ChatComponentText("  none configured"));return;
        }

        if (!isAdmin(sender)) {
            if (args[0].equalsIgnoreCase("menu") || args[0].equalsIgnoreCase("openmenu")) {
                openMenu(sender);
                return;
            }

            if (args[0].equalsIgnoreCase("teamchange") || args[0].equalsIgnoreCase("team") || args[0].equalsIgnoreCase("switchteam")) {
                processTeamChangeCommand(sender);
                return;
            }

            sender.addChatMessage(new ChatComponentText("Unknown or admin-only TDM command: " + args[0]));
            sender.addChatMessage(new ChatComponentText("Use /tdm help for player commands."));
            return;
        }

        if (args[0].equalsIgnoreCase("menu") || args[0].equalsIgnoreCase("openmenu")) {
            openMenu(sender);
            return;
        }

        if (args[0].equalsIgnoreCase("teamchange") || args[0].equalsIgnoreCase("team") || args[0].equalsIgnoreCase("switchteam")) {
            processTeamChangeCommand(sender);
            return;
        }

        if (args[0].equalsIgnoreCase("kits") || args[0].equalsIgnoreCase("listkits")) {
            processKitCommand(sender, prependArg("list", args));
            return;
        }

        if (args[0].equalsIgnoreCase("kit")) {
            processKitCommand(sender, args);
            return;
        }
        if (args[0].equalsIgnoreCase("editor")) {
            processEditorCommand(sender, args);
            return;
        }
        if (args[0].equalsIgnoreCase("utility") || args[0].equalsIgnoreCase("killstreak")) {
            processPurchasableCommand(sender,args,args[0].equalsIgnoreCase("utility")?TDMPurchasableManager.Type.UTILITY:TDMPurchasableManager.Type.KILLSTREAK);
            return;
        }

        if (args[0].equalsIgnoreCase("toggle")) {
            boolean enabled = TDMManager.toggle(world);
            sender.addChatMessage(new ChatComponentText("TDM: " + enabled));
            return;
        }

        if (args[0].equalsIgnoreCase("bombtest")) {
            if (args.length < 2 || args[1].equalsIgnoreCase("status")) {
                sender.addChatMessage(new ChatComponentText("Single-player Search and Destroy testing is " + (TDMManager.isBombTestMode() ? "enabled" : "disabled") + ". Usage: /tdm bombtest <on|off>"));
                return;
            }
            Boolean enabled = parseToggle(args[1]);
            if (enabled == null) {
                sender.addChatMessage(new ChatComponentText("Usage: /tdm bombtest <on|off>"));
                return;
            }
            TDMManager.setBombTestMode(world, enabled.booleanValue());
            sender.addChatMessage(new ChatComponentText("Single-player Search and Destroy testing " + (enabled.booleanValue() ? "enabled." : "disabled.")));
            return;
        }

        if (args[0].equalsIgnoreCase("testsound")) {
            processTestSound(sender, args, world);
            return;
        }

        if(args[0].equalsIgnoreCase("forceroundend")){if(args.length!=2){sender.addChatMessage(new ChatComponentText("Usage: /tdm forceroundend <red|blue|terrorist|ct|counterterrorist|abort>"));return;}String target=args[1].toLowerCase();boolean abort="abort".equals(target);TDMManager.Team winner=abort?null:("terrorist".equals(target)?TDMManager.getTerroristTeam(world):("ct".equals(target)||"counterterrorist".equals(target)?TDMManager.getCounterTerroristTeam(world):TDMManager.Team.fromName(target)));if(!abort&&winner==null){sender.addChatMessage(new ChatComponentText("Unknown round result: "+args[1]));return;}if(!TDMBombManager.forceRoundEnd(world,winner,abort)){sender.addChatMessage(new ChatComponentText("There is no active Search and Destroy round to end."));return;}sender.addChatMessage(new ChatComponentText(abort?"Search and Destroy round aborted.":winner.name+" administratively won the Search and Destroy round."));return;}

        if (args[0].equalsIgnoreCase("forcemapvote") || args[0].equalsIgnoreCase("forcevote")) {
            if (!TDMManager.isEnabled(world)) {
                sender.addChatMessage(new ChatComponentText("TDM must be enabled before forcing a map vote."));
                return;
            }

            if (TDMManager.getMapNames(world).isEmpty()) {
                sender.addChatMessage(new ChatComponentText("No TDM maps defined. Use /tdm map create <map> first."));
                return;
            }

            if (TDMManager.isMapVoteActive(world)) {
                sender.addChatMessage(new ChatComponentText("A TDM map vote is already active."));
                return;
            }

            TDMManager.startMapVote(world);
            sender.addChatMessage(new ChatComponentText(TDMManager.isMapVoteActive(world)
                    ? "Forced a 30 second TDM map vote." : "No vote started: no voteable alternative is available."));
            return;
        }

        if (args[0].equalsIgnoreCase("friendlyfire")) {
            if (args.length < 2) {
                sender.addChatMessage(new ChatComponentText("Friendly fire is " + TDMManager.isFriendlyFireEnabled(world) + ". Usage: /tdm friendlyfire <on|off>"));
                return;
            }

            Boolean enabled = parseToggle(args[1]);
            if (enabled == null) {
                sender.addChatMessage(new ChatComponentText("Usage: /tdm friendlyfire <on|off>"));
                return;
            }

            TDMManager.setFriendlyFireEnabled(world, enabled.booleanValue());
            sender.addChatMessage(new ChatComponentText("TDM friendly fire damage: " + (enabled.booleanValue() ? "on" : "off")));
            return;
        }

        if (args[0].equalsIgnoreCase("autobalance")) {
            if (args.length < 2) {
                sender.addChatMessage(new ChatComponentText("Auto balance is " + TDMManager.isAutoBalanceEnabled(world) + ". Usage: /tdm autobalance <on|off|now>"));
                return;
            }

            if (args[1].equalsIgnoreCase("now")) {
                int moved = TDMManager.balanceTeams(world);
                sender.addChatMessage(new ChatComponentText("TDM team balance complete. Players moved: " + moved));
                return;
            }

            Boolean enabled = parseToggle(args[1]);
            if (enabled == null) {
                sender.addChatMessage(new ChatComponentText("Usage: /tdm autobalance <on|off|now>"));
                return;
            }

            TDMManager.setAutoBalanceEnabled(world, enabled.booleanValue());
            sender.addChatMessage(new ChatComponentText("TDM auto balance: " + (enabled.booleanValue() ? "on" : "off")));
            return;
        }

        if (args[0].equalsIgnoreCase("map")) {
            processMapCommand(sender, args, world);
            return;
        }

        if (args[0].equalsIgnoreCase("addspawn")) {
            if (args.length < 2) {
                sender.addChatMessage(new ChatComponentText("Usage: /tdm addspawn <red|blue>"));
                return;
            }

            TDMManager.Team team = TDMManager.Team.fromName(args[1]);
            if (team == null) {
                sender.addChatMessage(new ChatComponentText("Unknown TDM team: " + args[1]));
                return;
            }

            EntityPlayer player = getCommandSenderAsPlayer(sender);
            TDMManager.addSpawn(
                    world,
                    team,
                    player.dimension,
                    (int) player.posX,
                    (int) player.posY,
                    (int) player.posZ
            );

            sender.addChatMessage(new ChatComponentText(
                    "Legacy spawn added for " + team.name + ". Total: " + TDMManager.getSpawnCount(world)
                            + " (red: " + TDMManager.getSpawnCount(world, TDMManager.Team.RED)
                            + ", blue: " + TDMManager.getSpawnCount(world, TDMManager.Team.BLUE) + ")"
            ));
            return;
        }

        if (args[0].equalsIgnoreCase("setteam")) {
            if (args.length < 3) {
                sender.addChatMessage(new ChatComponentText("Usage: /tdm setteam <player> <red|blue>"));
                return;
            }

            TDMManager.Team team = TDMManager.Team.fromName(args[2]);
            if (team == null) {
                sender.addChatMessage(new ChatComponentText("Unknown TDM team: " + args[2]));
                return;
            }

            TDMManager.setPlayerTeam(world, args[1], team);
            sender.addChatMessage(new ChatComponentText(args[1] + " assigned to " + team.name));
            return;
        }

        if (args[0].equalsIgnoreCase("teamless")) {
            EntityPlayer player = getCommandSenderAsPlayer(sender);
            TDMManager.makePlayerTeamless(player);
            sender.addChatMessage(new ChatComponentText("You are now a teamless TDM observer."));
            return;
        }

        if (args[0].equalsIgnoreCase("clear")) {
            TDMManager.clearSpawns(world);
            sender.addChatMessage(new ChatComponentText("Legacy TDM spawns cleared"));
            return;
        }

        sender.addChatMessage(new ChatComponentText("Unknown TDM command: " + args[0]));
        sender.addChatMessage(new ChatComponentText("Use /tdm help for available commands and examples."));
    }

    private void sendHelp(ICommandSender sender, String requested) {
        String category = requested == null ? "general" : requested.toLowerCase();
        if (category.equals("1") || category.equals("player")) category = "general";
        if (category.equals("2") || category.equals("round")) category = "match";
        if (category.equals("3")) category = "teams";
        if (category.equals("4")) category = "loadouts";
        if (category.equals("5") || category.equals("spawns")) category = "maps";
        if (category.equals("6") || category.equals("debug")) category = "admin";
        List<String> publicCategories = Arrays.asList("general", "match", "teams", "loadouts", "maps");
        List<String> adminCategories = Arrays.asList("kits", "admin");
        if (!publicCategories.contains(category) && (!isAdmin(sender) || !adminCategories.contains(category))) {
            sender.addChatMessage(new ChatComponentText(ERROR + "Unknown or unavailable help category: " + requested));
            category = "general";
        }
        sender.addChatMessage(new ChatComponentText(HELP + "TDM help [" + category + "] — /tdm help <category>"));
        sender.addChatMessage(new ChatComponentText(INFO + "PLAYER COMMANDS: match | teams | loadouts | maps"));
        if(isAdmin(sender))sender.addChatMessage(new ChatComponentText(INFO + "ADMINISTRATION: admin | teams | kits | maps"));
        if (category.equals("general")) {
            helpLine(sender, false, "menu", "Open the mode scoreboard/actions menu.");
            helpLine(sender, false, "maps", "List maps, modes, settings, and active votes.");
            helpLine(sender, false, "overlay <on|off>", "Show the active map, your spawns, and Search and Destroy sites on the map.");
            helpLine(sender, false, "help [category]", "Example: /tdm help teams");
        } else if (category.equals("match")) {
            helpLine(sender, false, "vote <map> <mode>", "Vote for a map and gamemode pairing.");
            helpLine(sender, false, "skip [yes|no|status]", "Vote to rotate; defaults to yes.");
        } else if (category.equals("teams")) {
            helpLine(sender, false, "teamchange", "Swap RED/BLUE (120-second cooldown; unavailable in FFA).");
            helpLine(sender, false, "menu", "Preferred team-change interface.");
            if(isAdmin(sender))helpLine(sender,true,"teamless","Place yourself in observer/teamless mode.");
        } else if(category.equals("loadouts")) {
            helpLine(sender,false,"menu","View/select kits and economy-free DM/FFA respawn loadouts.");
            helpLine(sender,false,"help match","Learn about voting and match flow.");
        } else if (category.equals("kits")) {
            sender.addChatMessage(new ChatComponentText(INFO+"Kits, Utility & Killstreaks"));
            helpLine(sender, true, "editor gui", "Open the draggable map, kit, spawn, and area editor.");
            helpLine(sender, true, "kit list [map|global]", "List configured loadouts and Search and Destroy costs.");
            helpLine(sender, true, "kit add <red|blue> [map|global] [cost]", "Save inventory; example: /tdm kit add red arena 3");
            helpLine(sender, true, "kit edit <red|blue> <number> [map|global]", "Load a direct kit into creative inventory; commit or cancel afterward.");
            helpLine(sender, true, "kit <commit|cancel|status>", "Finish or inspect an active kit inventory edit.");
            helpLine(sender, true, "kit <clone|rename|cost> ...", "Duplicate or change a direct kit definition.");
            helpLine(sender, true, "kit remove <red|blue> <number> [map|global]", "Remove a numbered kit from kit list.");
            helpLine(sender,true,"utility <list|add|remove>","Manage Search and Destroy buy-score utility definitions.");
            helpLine(sender,true,"killstreak <list|add|remove>","Manage kill-score reward definitions.");
        } else if (category.equals("maps")) {
            helpLine(sender, false, "maps", "List maps, modes, timers, point limits, and active votes.");
            helpLine(sender, false, "overlay <on|off>", "Toggle your persistent map overlay.");
            if(isAdmin(sender)){helpLine(sender, true, "map <create|delete|select> <map>", "Manage maps.");
            helpLine(sender, true, "boundaryview <on|off>", "Toggle your in-world map boundary view.");
            helpLine(sender, true, "overlay on [map]", "Preview the active or a named map as admin.");
            helpLine(sender, true, "map addspawn <map> <type> [mode]", "Add your position to one mode's spawn set.");
            helpLine(sender, true, "map <pointlimit|timer> <map> <value|default>", "Set DM/FFA point-score victory limit or timer (scorelimit is an alias).");
            helpLine(sender, true, "map mode <map> <tdm|sd|ffa>", "Set the default and selected mode.");
            helpLine(sender, true, "map voteable <map> <on|off>", "Include or exclude every mode of a map from votes.");
            helpLine(sender, true, "map voteable <map> <mode> <on|off>", "Offer a map and gamemode pairing in votes.");
            helpLine(sender, true, "map bombsite <map> <a|b> <pos1|pos2|clear>", "Configure Search and Destroy objective bounds.");
            helpLine(sender, true, "map border <map> <on|off>", "Enforce the selected map's horizontal border for match players.");
            helpLine(sender, true, "editor select <map|bomb_a|bomb_b> [map]", "Start a typed area selection; use editor point/commit/cancel.");
            helpLine(sender, true, "map <updatespawn|removespawn|tpspawn> <map> <number> [mode]", "Edit a spawn in one mode.");
            helpLine(sender, true, "map terroristteam <map> <red|blue>", "Assign the Search and Destroy Terrorist role.");
            helpLine(sender, true, "map <hardcorerespawns|economy> <map> <true|false>", "Configure Search and Destroy policy.");}
        } else {
            helpLine(sender, true, "toggle", "Enable or disable TDM.");
            helpLine(sender, true, "forcemapvote", "Start a 30-second vote.");
            helpLine(sender, true, "forceroundend <red|blue|terrorist|ct|abort>", "End an active Search and Destroy round.");
            helpLine(sender, true, "friendlyfire <on|off>", "Set team damage.");
            helpLine(sender, true, "autobalance <on|off|now>", "Configure or run team balancing.");
            helpLine(sender, true, "setteam <player> <red|blue>", "Assign an online player.");
            helpLine(sender, true, "bombtest <on|off|status>", "Transient single-player Search and Destroy testing.");
            helpLine(sender, true, "testsound <ctwin|twin|ctstart|tstart|bombplant>", "Test configured mode sounds.");
        }
    }

    private void helpLine(ICommandSender sender, boolean admin, String syntax, String description) {
        sender.addChatMessage(new ChatComponentText((admin ? COMMAND_ADMIN : COMMAND) + "/tdm " + syntax + TITLE + " — " + description));
    }

    private void openMenu(ICommandSender sender) {
        EntityPlayer player = getCommandSenderAsPlayer(sender);
        if (!TDMManager.isEnabled(player.worldObj)) {
            sender.addChatMessage(new ChatComponentText("TDM is not enabled."));
            return;
        }

        if (!(player instanceof EntityPlayerMP)) {
            sender.addChatMessage(new ChatComponentText("Only players can open the TDM menu."));
            return;
        }

        PacketDispatcher.wrapper.sendTo(new TDMMenuDataPacket((EntityPlayerMP) player, TDMManager.getTeamChangeCooldownSeconds(player)), (EntityPlayerMP) player);
    }

    private void processTestSound(ICommandSender sender, String[] args, World world) {
        if (!(sender instanceof EntityPlayerMP)) {
            sender.addChatMessage(new ChatComponentText("/tdm testsound must be run by an in-game operator."));
            return;
        }
        if (args.length != 2) {
            sender.addChatMessage(new ChatComponentText("Usage: /tdm testsound <ctwin|twin|ctstart|tstart|bombplant>"));
            return;
        }
        String type = args[1].toLowerCase();
        String eventType;
        String propertyName;
        String[] variants;
        boolean global;
        if ("ctwin".equals(type)) { eventType = "ct_victory_test"; propertyName = XFConfig.TDM_CT_WIN_SOUNDS_PROPERTY; variants = XFConfig.tdmCtWinSounds; global = true; }
        else if ("twin".equals(type)) { eventType = "t_victory_test"; propertyName = XFConfig.TDM_T_WIN_SOUNDS_PROPERTY; variants = XFConfig.tdmTWinSounds; global = true; }
        else if ("ctstart".equals(type)) { eventType = "ct_round_start_test"; propertyName = XFConfig.TDM_CT_ROUND_START_SOUNDS_PROPERTY; variants = XFConfig.tdmCtRoundStartSounds; global = false; }
        else if ("tstart".equals(type)) { eventType = "t_round_start_test"; propertyName = XFConfig.TDM_T_ROUND_START_SOUNDS_PROPERTY; variants = XFConfig.tdmTRoundStartSounds; global = false; }
        else if ("bombplant".equals(type)) { eventType = "bomb_planted_test"; propertyName = XFConfig.TDM_BOMB_PLANTED_SOUNDS_PROPERTY; variants = XFConfig.tdmBombPlantedSounds; global = true; }
        else { sender.addChatMessage(new ChatComponentText("Unknown sound type. Use ctwin, twin, ctstart, tstart, or bombplant.")); return; }
        EntityPlayerMP player = (EntityPlayerMP) sender;
        String selected = TDMManager.playConfiguredSound(world, eventType, variants, null, global ? null : player);
        if (selected == null) sender.addChatMessage(new ChatComponentText("TDM sound disabled: event=" + eventType + ", property=" + propertyName + ", raw=" + Arrays.toString(variants) + ", effective=" + normalizedSoundVariants(variants)));
        else sender.addChatMessage(new ChatComponentText("Dispatched TDM sound event " + selected + (global ? " to eligible TDM players in this dimension." : " to you.")));
    }

    private List<String> normalizedSoundVariants(String[] variants) {
        List<String> normalized = new ArrayList<String>();
        if (variants == null) return normalized;
        for (String variant : variants) {
            String eventId = TDMManager.normalizeSoundEventId(variant);
            if (eventId != null) normalized.add(eventId);
        }
        return normalized;
    }

    private String[] prependArg(String first, String[] args) {
        String[] newArgs = new String[args.length + 1];
        newArgs[0] = args[0];
        newArgs[1] = first;
        for (int i = 1; i < args.length; i++) {
            newArgs[i + 1] = args[i];
        }
        return newArgs;
    }

    private void processTeamChangeCommand(ICommandSender sender) {
        TDMManager.changePlayerTeamWithCooldown(getCommandSenderAsPlayer(sender));
    }

    private void processEditorCommand(ICommandSender sender, String[] args) {
        EntityPlayer player = getCommandSenderAsPlayer(sender);
        if (args.length >= 2 && args[1].equalsIgnoreCase("gui")) {
            String map = args.length >= 3 ? args[2] : null;
            TDMManager.Team team = args.length >= 4 ? TDMManager.Team.fromName(args[3]) : TDMManager.Team.RED;
            int index = 0;
            if (args.length >= 5) try { index = Math.max(0, Integer.parseInt(args[4]) - 1); } catch (NumberFormatException ignored) { }
            PacketDispatcher.wrapper.sendTo(new AdminEditorSnapshotPacket((EntityPlayerMP)player, map, team, index), (EntityPlayerMP)player);
            return;
        }
        if (args.length < 2 || args[1].equalsIgnoreCase("status")) {
            sender.addChatMessage(new ChatComponentText(AdminSelectionManager.status(player)));
            sender.addChatMessage(new ChatComponentText(TDMAdminKitEdit.status(player)));
            return;
        }
        String result;
        if (args[1].equalsIgnoreCase("select")) {
            if (args.length < 3) { sender.addChatMessage(new ChatComponentText("Usage: /tdm editor select <map|bomb_a|bomb_b> [map]")); return; }
            Type type;
            try { type = Type.valueOf(args[2].toUpperCase()); } catch (IllegalArgumentException e) { type = null; }
            if (type != Type.MAP && type != Type.BOMB_A && type != Type.BOMB_B) { sender.addChatMessage(new ChatComponentText("Use map, bomb_a, or bomb_b for /tdm editor.")); return; }
            String map = args.length >= 4 ? args[3] : TDMManager.getSelectedMap(player.worldObj);
            result = AdminSelectionManager.begin(player, type, map);
        } else if (args[1].equalsIgnoreCase("point")) {
            if (args.length < 3 || (!args[2].equalsIgnoreCase("a") && !args[2].equalsIgnoreCase("b"))) { sender.addChatMessage(new ChatComponentText("Usage: /tdm editor point <a|b>")); return; }
            AdminSelectionManager.Selection selection = AdminSelectionManager.get(player);
            result = selection == null || (args.length >= 4 && !selection.type.name().equalsIgnoreCase(args[3]))
                    ? "Selection type mismatch or no active selection." : AdminSelectionManager.pointHere(player, args[2].equalsIgnoreCase("a"));
        } else if (args[1].equalsIgnoreCase("commit")) {
            AdminSelectionManager.Selection selection = AdminSelectionManager.get(player);
            result = selection == null || (selection.type != Type.MAP && selection.type != Type.BOMB_A && selection.type != Type.BOMB_B)
                    || (args.length >= 3 && !selection.type.name().equalsIgnoreCase(args[2]))
                    ? "No active TDM area selection." : AdminSelectionManager.commit(player, selection.type);
        } else if (args[1].equalsIgnoreCase("cancel")) {
            AdminSelectionManager.clear(player); result = "Admin area selection cancelled.";
        } else { result = "Usage: /tdm editor <status|select|point|commit|cancel>"; }
        sender.addChatMessage(new ChatComponentText(result));
    }

    private void processKitCommand(ICommandSender sender, String[] args) {
        if (args.length < 2 || args[1].equalsIgnoreCase("help")) {
            sender.addChatMessage(new ChatComponentText("Usage: /tdm kit <list|add|edit|commit|cancel|clone|rename|remove> ..."));
            sender.addChatMessage(new ChatComponentText("  /tdm kit list [map|global]"));
            sender.addChatMessage(new ChatComponentText("  /tdm kit add <red|blue> [map|global]"));
            sender.addChatMessage(new ChatComponentText("  /tdm kit remove <red|blue> <number> [map|global]"));
            return;
        }

        if (args[1].equalsIgnoreCase("commit") || args[1].equalsIgnoreCase("cancel") || args[1].equalsIgnoreCase("status")) {
            EntityPlayer player = getCommandSenderAsPlayer(sender);
            String result = args[1].equalsIgnoreCase("commit") ? TDMAdminKitEdit.commit(player)
                    : args[1].equalsIgnoreCase("cancel") ? TDMAdminKitEdit.cancel(player) : TDMAdminKitEdit.status(player);
            sender.addChatMessage(new ChatComponentText(result));
            return;
        }

        if (args[1].equalsIgnoreCase("edit") || args[1].equalsIgnoreCase("clone") || args[1].equalsIgnoreCase("rename") || args[1].equalsIgnoreCase("cost") || args[1].equalsIgnoreCase("mode")) {
            if (args.length < 4) { sender.addChatMessage(new ChatComponentText("Usage: /tdm kit " + args[1] + " <red|blue> <number> [map|global] [new name for rename]")); return; }
            TDMManager.Team team = TDMManager.Team.fromName(args[2]);
            int number;
            try { number = Integer.parseInt(args[3]); } catch (NumberFormatException e) { number = 0; }
            if (team == null || number <= 0) { sender.addChatMessage(new ChatComponentText("Choose red/blue and a kit number from /tdm kit list.")); return; }
            String mapName = args.length >= 5 ? normalizeKitMap(args[4]) : TDMManager.getSelectedMap(sender.getEntityWorld());
            if (args[1].equalsIgnoreCase("edit")) {
                sender.addChatMessage(new ChatComponentText(TDMAdminKitEdit.begin(getCommandSenderAsPlayer(sender), mapName, team, number - 1)));
            } else if (args[1].equalsIgnoreCase("clone")) {
                sender.addChatMessage(new ChatComponentText(TDMKitManager.duplicateKit(mapName, team, number - 1) ? "Duplicated kit." : "No direct kit at that number."));
            } else if (args[1].equalsIgnoreCase("cost")) {
                int cost;
                try { cost = Integer.parseInt(args.length >= 6 ? args[5] : "-1"); } catch (NumberFormatException e) { cost = -1; }
                sender.addChatMessage(new ChatComponentText(TDMKitManager.setKitCost(mapName, team, number - 1, cost) ? "Updated Search and Destroy kit cost." : "Cost must be non-negative and the direct kit must exist."));
            } else if (args[1].equalsIgnoreCase("mode")) {
                TDMManager.TDMGameMode mode = args.length >= 6 ? TDMManager.parseMode(args[5]) : null;
                Boolean enabled = args.length >= 7 ? parseToggle(args[6]) : null;
                sender.addChatMessage(new ChatComponentText(mode != null && enabled != null
                        && TDMKitManager.setDirectKitMode(mapName, team, number - 1, mode, enabled.booleanValue())
                        ? "Kit " + mode.displayName + " availability: " + (enabled ? "enabled" : "disabled")
                        : "Usage: /tdm kit mode <red|blue> <number> <map|global> <tdm|sd|ffa> <on|off>"));
            } else {
                if (args.length < 6) { sender.addChatMessage(new ChatComponentText("Usage: /tdm kit rename <red|blue> <number> <map|global> <new name>")); return; }
                StringBuilder name = new StringBuilder();
                for (int i = 5; i < args.length; i++) { if (name.length() > 0) name.append(' '); name.append(args[i]); }
                sender.addChatMessage(new ChatComponentText(TDMKitManager.renameKit(mapName, team, number - 1, name.toString()) ? "Renamed kit." : "Could not rename that direct kit."));
            }
            return;
        }

        if (args[1].equalsIgnoreCase("list")) {
            String mapName = args.length >= 3 ? normalizeKitMap(args[2]) : TDMManager.getSelectedMap(sender.getEntityWorld());
            sendKitList(sender, mapName);
            return;
        }

        if (args[1].equalsIgnoreCase("add") || args[1].equalsIgnoreCase("save")) {
            if (sender instanceof EntityPlayer && TDMAdminKitEdit.isEditing((EntityPlayer)sender)) { sender.addChatMessage(new ChatComponentText("Finish or cancel the loaded kit edit first; use /tdm kit commit to save it.")); return; }
            if (args.length < 3) {
                sender.addChatMessage(new ChatComponentText("Usage: /tdm kit add <blue|red> [map|global] [cost]"));
                return;
            }

            TDMManager.Team team = TDMManager.Team.fromName(args[2]);
            if (team == null) {
                sender.addChatMessage(new ChatComponentText("Unknown TDM team: " + args[2]));
                sender.addChatMessage(new ChatComponentText("Usage: /tdm kit add <blue|red> [map|global] [cost]"));
                return;
            }

            EntityPlayer player = getCommandSenderAsPlayer(sender);
            String mapName = args.length >= 4 ? normalizeKitMap(args[3]) : TDMManager.getSelectedMap(sender.getEntityWorld());
            int cost=0;if(args.length>=5){try{cost=Integer.parseInt(args[4]);}catch(NumberFormatException e){sender.addChatMessage(new ChatComponentText("Kit cost must be a non-negative integer."));return;}if(cost<0){sender.addChatMessage(new ChatComponentText("Kit cost must be a non-negative integer."));return;}}
            int kitCount = TDMKitManager.addKit(mapName, team, player,cost);
            String mapText = mapName.length() > 0 ? " for map " + mapName : " as a global fallback";
            sender.addChatMessage(new ChatComponentText("Saved " + team.name + " kit #" + kitCount + mapText + " from your inventory to tdm_kits.txt (cost: " + (cost==0?"FREE":Integer.toString(cost)) + ")"));
            return;
        }

        if (args[1].equalsIgnoreCase("remove") || args[1].equalsIgnoreCase("delete")) {
            removeKit(sender, args);
            return;
        }

        sender.addChatMessage(new ChatComponentText("Unknown TDM kit command: " + args[1]));
        sender.addChatMessage(new ChatComponentText("Usage: /tdm kit <list|add|remove> ..."));
    }

    private void processPurchasableCommand(ICommandSender sender,String[] args,TDMPurchasableManager.Type type){
        String noun=type==TDMPurchasableManager.Type.UTILITY?"utility":"killstreak";
        if(args.length<2){sender.addChatMessage(new ChatComponentText("Usage: /tdm "+noun+" <list|add|remove> [map|global] [cost|number]"));return;}
        String mapName=args.length>=3?normalizeKitMap(args[2]):TDMManager.getSelectedMap(sender.getEntityWorld());
        if(args[1].equalsIgnoreCase("list")){String[] names=TDMPurchasableManager.getNames(mapName,type);int[] costs=TDMPurchasableManager.getCosts(mapName,type);sender.addChatMessage(new ChatComponentText(noun+" definitions for "+getMapDisplayName(mapName)+":"));for(int i=0;i<names.length;i++)sender.addChatMessage(new ChatComponentText("  "+(i+1)+"="+names[i]+" ["+costs[i]+"]"));if(names.length==0)sender.addChatMessage(new ChatComponentText("  none"));return;}
        if(args[1].equalsIgnoreCase("add")){int cost;if(args.length<4){sender.addChatMessage(new ChatComponentText("Usage: /tdm "+noun+" add <map|global> <cost>"));return;}try{cost=Integer.parseInt(args[3]);}catch(NumberFormatException e){sender.addChatMessage(new ChatComponentText("Cost must be a non-negative integer."));return;}if(cost<0){sender.addChatMessage(new ChatComponentText("Cost must be a non-negative integer."));return;}int count=TDMPurchasableManager.add(mapName,type,getCommandSenderAsPlayer(sender),cost);sender.addChatMessage(new ChatComponentText("Saved "+noun+" #"+count+" from your inventory."));return;}
        if(args[1].equalsIgnoreCase("remove")){int number;if(args.length<4){sender.addChatMessage(new ChatComponentText("Usage: /tdm "+noun+" remove <map|global> <number>"));return;}try{number=Integer.parseInt(args[3]);}catch(NumberFormatException e){sender.addChatMessage(new ChatComponentText("Number must come from /tdm "+noun+" list."));return;}sender.addChatMessage(new ChatComponentText(TDMPurchasableManager.remove(mapName,type,number-1)?"Removed "+noun+" #"+number:"No such "+noun+" definition."));return;}
        sender.addChatMessage(new ChatComponentText("Usage: /tdm "+noun+" <list|add|remove> ..."));
    }

    private void removeKit(ICommandSender sender, String[] args) {
        if (args.length < 4) {
            sender.addChatMessage(new ChatComponentText("Usage: /tdm kit remove <blue|red> <number> [map|global]"));
            return;
        }

        TDMManager.Team team = TDMManager.Team.fromName(args[2]);
        if (team == null) {
            sender.addChatMessage(new ChatComponentText("Unknown TDM team: " + args[2]));
            sender.addChatMessage(new ChatComponentText("Usage: /tdm kit remove <blue|red> <number> [map|global]"));
            return;
        }

        int kitNumber;
        try {
            kitNumber = Integer.parseInt(args[3]);
        } catch (NumberFormatException e) {
            sender.addChatMessage(new ChatComponentText("Kit number must be a number from /tdm kit list."));
            return;
        }

        String mapName = args.length >= 5 ? normalizeKitMap(args[4]) : TDMManager.getSelectedMap(sender.getEntityWorld());
        if (!TDMKitManager.removeKit(mapName, team, kitNumber - 1)) {
            sender.addChatMessage(new ChatComponentText("No " + team.name + " kit #" + kitNumber + " exists for " + getMapDisplayName(mapName) + ". Use /tdm kit list " + getMapDisplayName(mapName) + " to list kits."));
            return;
        }

        sender.addChatMessage(new ChatComponentText("Removed " + team.name + " kit #" + kitNumber + " from " + getMapDisplayName(mapName) + "."));
    }

    private void sendKitList(ICommandSender sender, String mapName) {
        String displayMap = getMapDisplayName(mapName);
        sender.addChatMessage(new ChatComponentText("TDM kits for " + displayMap + ":"));
        sendTeamKits(sender, mapName, TDMManager.Team.RED);
        sendTeamKits(sender, mapName, TDMManager.Team.BLUE);

        if (mapName.length() > 0) {
            sender.addChatMessage(new ChatComponentText("Global fallback kits:"));
            sendTeamKits(sender, "", TDMManager.Team.RED);
            sendTeamKits(sender, "", TDMManager.Team.BLUE);
        }
    }

    private void sendTeamKits(ICommandSender sender, String mapName, TDMManager.Team team) {
        String[] names = TDMKitManager.getDirectKitNames(mapName, team); int[] costs=TDMKitManager.getKitCosts(mapName,team);
        if (names.length == 0) {
            sender.addChatMessage(new ChatComponentText("  " + team.name + ": none"));
            return;
        }

        String message = "  " + team.name + ": ";
        for (int i = 0; i < names.length; i++) {
            if (i > 0) {
                message += ", ";
            }
            message += (i + 1) + "=" + names[i]+" ["+(costs[i]==0?"FREE":Integer.toString(costs[i]))+"]";
        }
        sender.addChatMessage(new ChatComponentText(message));
    }

    private String normalizeKitMap(String mapName) {
        if ("@map:global".equalsIgnoreCase(mapName)) return "global";
        String normalized = TDMManager.normalizeMapName(mapName);
        return normalized.equals("global") ? "" : normalized;
    }

    private String getMapDisplayName(String mapName) {
        return mapName.length() == 0 ? "global" : mapName;
    }

    private void processMapCommand(ICommandSender sender, String[] args, World world) {
        if (args.length < 2) {
            sendMapUsage(sender);
            return;
        }

        String action = args[1].toLowerCase();
        if (action.equals("list")) {
            sendMapList(sender, world);
            return;
        }

        if (!action.equals("create") && !action.equals("delete") && !action.equals("select") && !action.equals("voteable") && !action.equals("spawnfallback") && !action.equals("legacyspawns") && !action.equals("assignlegacyspawn") && !action.equals("addspawn") && !action.equals("updatespawn") && !action.equals("removespawn") && !action.equals("tpspawn") && !action.equals("clearbounds") && !action.equals("border") && !action.equals("clearspawns") && !action.equals("scorelimit") && !action.equals("pointlimit") && !action.equals("timer") && !action.equals("mode") && !action.equals("terroristteam") && !action.equals("hardcorerespawns") && !action.equals("bombsite") && !action.equals("economy") && !action.equals("killstreaks") && !action.equals("killscorereward") && !action.equals("killscore") && !action.equals("defusescore") && !action.equals("lossscore") && !action.equals("plantscore") && !action.equals("roundwinscore")) {
            sender.addChatMessage(new ChatComponentText("Unknown TDM map command: " + args[1]));
            sendMapUsage(sender);
            return;
        }

        if (args.length < 3) {
            if (action.equals("addspawn")) {
                sender.addChatMessage(new ChatComponentText("Usage: /tdm map addspawn <map> <red|blue|ffa>"));
            } else if (action.equals("scorelimit") || action.equals("pointlimit") || action.equals("timer")) {
                sender.addChatMessage(new ChatComponentText("Usage: /tdm map " + action + " <map> <" + (action.equals("timer") ? "seconds" : "points") + "|default>"));
            } else {
                sender.addChatMessage(new ChatComponentText("Usage: /tdm map " + args[1] + " <map>"));
            }
            return;
        }

        String mapName = TDMManager.normalizeMapName(args[2]);
        if (mapName.length() == 0) {
            sender.addChatMessage(new ChatComponentText("Map name cannot be empty."));
            return;
        }

        if (action.equals("create")) {
            if (!TDMManager.createMap(world, mapName)) {
                sender.addChatMessage(new ChatComponentText("TDM map already exists or has an invalid name: " + mapName));
                return;
            }
            sender.addChatMessage(new ChatComponentText("Created TDM map: " + mapName + " (voting off until enabled)."));
            return;
        }

        if (action.equals("scorelimit") || action.equals("pointlimit") || action.equals("timer")) {
            configureMapSetting(sender, world, action.equals("pointlimit")?"scorelimit":action, mapName, args);
            return;
        }

        if(!TDMManager.hasMap(world,mapName)){sender.addChatMessage(new ChatComponentText("Unknown TDM map: "+mapName));return;}
        if (action.equals("legacyspawns")) {
            java.util.List<TDMManager.SpawnPoint> preserved = TDMManager.getMap(world, mapName).legacyUnassignedSpawns;
            sender.addChatMessage(new ChatComponentText("Preserved unassigned spawns for " + mapName + ": " + preserved.size()));
            for (int i = 0; i < preserved.size(); i++) {
                TDMManager.SpawnPoint spawn = preserved.get(i);
                sender.addChatMessage(new ChatComponentText("#" + (i + 1) + " " + (spawn.team == null ? "neutral" : spawn.team.name)
                        + " dim " + spawn.dim + " " + spawn.x + "," + spawn.y + "," + spawn.z));
            }
            return;
        }
        if (action.equals("voteable")) {
            if (args.length == 4) {
                Boolean mapEnabled = parseToggle(args[3]);
                if (mapEnabled != null) {
                    TDMManager.setMapVotingEnabled(world, mapName, mapEnabled.booleanValue());
                    sender.addChatMessage(new ChatComponentText("Map " + mapName + " voting "
                            + (mapEnabled ? "enabled" : "disabled") + " for all modes."));
                    return;
                }
            }
            TDMManager.TDMGameMode mode = args.length > 3 ? TDMManager.parseMode(args[3]) : null;
            Boolean enabled = args.length > 4 ? parseToggle(args[4]) : null;
            if (mode == null || enabled == null || !TDMManager.setSupportedMode(world, mapName, mode, enabled.booleanValue())) {
                sender.addChatMessage(new ChatComponentText("Usage: /tdm map voteable <map> <on|off> OR <map> <tdm|sd|ffa> <on|off>. The active or final mode cannot be removed.")); return;
            }
            sender.addChatMessage(new ChatComponentText(TDMManager.pairLabel(mapName, mode) + " voting " + (enabled ? "enabled" : "disabled") + ".")); return;
        }
        if (action.equals("spawnfallback")) {
            TDMManager.TDMGameMode mode = args.length > 3 ? TDMManager.parseMode(args[3]) : null;
            TDMManager.TDMGameMode fallback = args.length > 4 ? TDMManager.parseMode(args[4]) : null;
            if (mode == null || args.length < 5 || (!args[4].equalsIgnoreCase("none") && fallback == null)
                    || !TDMManager.setSpawnFallback(world, mapName, mode, fallback)) {
                sender.addChatMessage(new ChatComponentText("Usage: /tdm map spawnfallback <map> <tdm|sd|ffa> <tdm|sd|ffa|none>")); return;
            }
            sender.addChatMessage(new ChatComponentText("Spawn fallback for " + TDMManager.pairLabel(mapName, mode) + ": " + (fallback == null ? "none" : fallback.displayName))); return;
        }
        if (action.equals("assignlegacyspawn")) {
            int index;
            try { index = Integer.parseInt(args.length > 3 ? args[3] : "0") - 1; }
            catch (NumberFormatException exception) { index = -1; }
            TDMManager.TDMGameMode mode = args.length > 4 ? TDMManager.parseMode(args[4]) : null;
            if (!TDMManager.assignLegacySpawn(world, mapName, index, mode)) {
                sender.addChatMessage(new ChatComponentText("Usage: /tdm map assignlegacyspawn <map> <number> <tdm|sd|ffa>. Category must match the mode.")); return;
            }
            sender.addChatMessage(new ChatComponentText("Moved preserved legacy spawn into " + TDMManager.pairLabel(mapName, mode) + ".")); return;
        }
        if (action.equals("clearbounds")) { TDMManager.clearMapBounds(world, mapName); sender.addChatMessage(new ChatComponentText("Cleared map bounds and disabled its border for " + mapName)); return; }
        if (action.equals("border")) {
            if (args.length < 4 || (!args[3].equalsIgnoreCase("on") && !args[3].equalsIgnoreCase("off"))) {
                sender.addChatMessage(new ChatComponentText("Usage: /tdm map border <map> <on|off>")); return;
            }
            boolean enabled = args[3].equalsIgnoreCase("on");
            if (!TDMManager.setMapBorderEnabled(world, mapName, enabled)) {
                sender.addChatMessage(new ChatComponentText("Set both map bounds corners before enabling its border.")); return;
            }
            sender.addChatMessage(new ChatComponentText("Map border for " + mapName + " is " + (enabled ? "on" : "off") + ".")); return;
        }
        if (action.equals("updatespawn") || action.equals("removespawn") || action.equals("tpspawn")) {
            if (args.length < 4) { sender.addChatMessage(new ChatComponentText("Usage: /tdm map " + action + " <map> <number>")); return; }
            int index; try { index = Integer.parseInt(args[3]) - 1; } catch (NumberFormatException e) { index = -1; }
            TDMManager.TDMMap map = TDMManager.getMap(world, mapName);
            TDMManager.TDMGameMode mode = args.length > 4 ? TDMManager.parseMode(args[4]) : map.mode;
            if (mode == null || index < 0 || index >= map.spawns(mode).size()) { sender.addChatMessage(new ChatComponentText("No spawn at that number for this gamemode.")); return; }
            TDMManager.SpawnPoint old = map.spawns(mode).get(index);
            if (action.equals("removespawn")) { TDMManager.removeMapSpawn(world, mapName, mode, index); sender.addChatMessage(new ChatComponentText("Removed spawn #" + (index + 1))); return; }
            EntityPlayerMP player = (EntityPlayerMP)getCommandSenderAsPlayer(sender);
            if (action.equals("updatespawn")) {
                TDMManager.updateMapSpawn(world, mapName, mode, index, new TDMManager.SpawnPoint(old.team, player.dimension, (int)Math.floor(player.posX), (int)Math.floor(player.posY), (int)Math.floor(player.posZ), true, player.rotationYaw, player.rotationPitch));
                sender.addChatMessage(new ChatComponentText("Updated spawn #" + (index + 1) + " from your position and facing."));
            } else {
                if (!net.minecraftforge.common.DimensionManager.isDimensionRegistered(old.dim)) { sender.addChatMessage(new ChatComponentText("Spawn dimension is unavailable.")); return; }
                if (player.dimension != old.dim) player.travelToDimension(old.dim);
                player.playerNetServerHandler.setPlayerLocation(old.x + .5D, old.y, old.z + .5D, old.hasRotation ? old.yaw : player.rotationYaw, old.hasRotation ? old.pitch : player.rotationPitch);
                sender.addChatMessage(new ChatComponentText("Teleported to spawn #" + (index + 1)));
            }
            return;
        }
        if(action.equals("economy")){if(args.length<4){sender.addChatMessage(new ChatComponentText("Usage: /tdm map economy <map> <true|false>"));return;}Boolean value=parseToggle(args[3]);if(value==null){sender.addChatMessage(new ChatComponentText("Economy must be true/false or on/off."));return;}TDMManager.TDMMap m=TDMManager.getMap(world,mapName);if(!m.supportedModes.contains(TDMManager.TDMGameMode.BOMB)){sender.addChatMessage(new ChatComponentText("Enable Search and Destroy on this map first."));return;}m.buyScoreEnabled=value.booleanValue();com.hfr.tdm.TDMData.get(world).markDirty();sender.addChatMessage(new ChatComponentText("Map "+mapName+" economy: "+value));return;}
        if(action.equals("killstreaks")){if(args.length<4){sender.addChatMessage(new ChatComponentText("Usage: /tdm map killstreaks <map> <true|false>"));return;}Boolean value=parseToggle(args[3]);TDMManager.TDMMap m=TDMManager.getMap(world,mapName);if(value==null||(!m.supportedModes.contains(TDMManager.TDMGameMode.DEATHMATCH)&&!m.supportedModes.contains(TDMManager.TDMGameMode.FFA))){sender.addChatMessage(new ChatComponentText("Killstreaks require a TDM or FFA pairing."));return;}m.killstreaksEnabled=value.booleanValue();com.hfr.tdm.TDMData.get(world).markDirty();sender.addChatMessage(new ChatComponentText("Map "+mapName+" killstreaks: "+value));return;}
        if(action.equals("killscorereward")){if(args.length<4){sender.addChatMessage(new ChatComponentText("Usage: /tdm map killscorereward <map> <amount>"));return;}int amount;try{amount=Integer.parseInt(args[3]);}catch(NumberFormatException e){sender.addChatMessage(new ChatComponentText("Amount must be a non-negative integer."));return;}if(amount<0){sender.addChatMessage(new ChatComponentText("Amount must be a non-negative integer."));return;}TDMManager.TDMMap m=TDMManager.getMap(world,mapName);if(!m.supportedModes.contains(TDMManager.TDMGameMode.DEATHMATCH)&&!m.supportedModes.contains(TDMManager.TDMGameMode.FFA)){sender.addChatMessage(new ChatComponentText("Kill score requires a TDM or FFA pairing."));return;}m.killScoreReward=amount;com.hfr.tdm.TDMData.get(world).markDirty();sender.addChatMessage(new ChatComponentText("Map "+mapName+" kill-score reward: "+amount));return;}
        if(action.equals("killscore")||action.equals("defusescore")||action.equals("lossscore")||action.equals("plantscore")||action.equals("roundwinscore")){if(args.length<4){sender.addChatMessage(new ChatComponentText("Usage: /tdm map "+action+" <map> <amount>"));return;}int amount;try{amount=Integer.parseInt(args[3]);}catch(NumberFormatException e){sender.addChatMessage(new ChatComponentText("Amount must be a non-negative integer."));return;}if(amount<0){sender.addChatMessage(new ChatComponentText("Amount must be a non-negative integer."));return;}TDMManager.TDMMap m=TDMManager.getMap(world,mapName);if(!m.supportedModes.contains(TDMManager.TDMGameMode.BOMB)){sender.addChatMessage(new ChatComponentText("Economy rewards require Search and Destroy."));return;}if(action.equals("killscore"))m.killBuyScoreReward=amount;else if(action.equals("lossscore"))m.roundLossBuyScoreReward=amount;else if(action.equals("plantscore"))m.bombPlantBuyScoreReward=amount;else if(action.equals("roundwinscore"))m.roundWinBuyScoreReward=amount;else m.bombDefuseBuyScoreReward=amount;com.hfr.tdm.TDMData.get(world).markDirty();sender.addChatMessage(new ChatComponentText("Map "+mapName+" "+action+": "+amount));return;}
        if(action.equals("mode")){TDMManager.TDMGameMode mode=args.length>=4?TDMManager.parseMode(args[3]):null;if(mode==null){sender.addChatMessage(new ChatComponentText("Usage: /tdm map mode <map> <tdm|sd|ffa>"));return;}TDMManager.setMapMode(world,mapName,mode);String feedback="Map "+mapName+" default mode set to "+mode.displayName+".";if(mode==TDMManager.TDMGameMode.BOMB&&TDMManager.isEnabled(world)&&TDMManager.getSelectedMap(world).equals(mapName)){if(TDMManager.isBombTestMode())feedback+=" Single-player Search and Destroy testing is enabled.";else if(!com.hfr.tdm.TDMBombManager.hasBothTeams(world))feedback+=" Waiting for at least one RED and one BLUE player.";}sender.addChatMessage(new ChatComponentText(feedback));return;}
        if(action.equals("terroristteam")){if(args.length<4){sender.addChatMessage(new ChatComponentText("Usage: /tdm map terroristteam <map> <red|blue>"));return;}TDMManager.Team team=TDMManager.Team.fromName(args[3]);if(team==null){sender.addChatMessage(new ChatComponentText("Team must be red or blue."));return;}TDMManager.configureMap(world,mapName,null,team,null);sender.addChatMessage(new ChatComponentText("Map "+mapName+" Terrorists: "+team.name));return;}
        if (action.equals("hardcorerespawns")) {
            if (args.length < 4 || (!args[3].equalsIgnoreCase("true")
                    && !args[3].equalsIgnoreCase("false"))) {
                sender.addChatMessage(new ChatComponentText(
                        "Usage: /tdm map hardcorerespawns <map> <true|false>"));
                return;
            }
            TDMManager.TDMMap map = TDMManager.getMap(world, mapName);
            if (!map.supportedModes.contains(TDMManager.TDMGameMode.BOMB)) {
                sender.addChatMessage(new ChatComponentText(
                        "Hardcore round elimination is available only for Search and Destroy; TDM and FFA are continuous."));
                return;
            }
            TDMManager.configureMap(world, mapName, null, null, Boolean.valueOf(args[3]));
            sender.addChatMessage(new ChatComponentText("Map " + mapName
                    + " hardcore respawns: " + args[3].toLowerCase()));
            return;
        }
        if (action.equals("bombsite")) {
            if (args.length < 5) { sender.addChatMessage(new ChatComponentText("Usage: /tdm map bombsite <map> <a|b> <pos1|pos2|clear>")); return; }
            boolean a = args[3].equalsIgnoreCase("a");
            if (!a && !args[3].equalsIgnoreCase("b")) { sender.addChatMessage(new ChatComponentText("Bombsite must be A or B.")); return; }
            if (args[4].equalsIgnoreCase("clear")) {
                TDMManager.clearBombsite(world, mapName, a);
                sender.addChatMessage(new ChatComponentText("Cleared bombsite " + (a ? "A" : "B") + "."));
                return;
            }
            int corner = args[4].equalsIgnoreCase("pos1") ? 1 : args[4].equalsIgnoreCase("pos2") ? 2 : 0;
            if (corner == 0) { sender.addChatMessage(new ChatComponentText("Use pos1, pos2, or clear.")); return; }
            EntityPlayer player = getCommandSenderAsPlayer(sender);
            Type type = a ? Type.BOMB_A : Type.BOMB_B;
            AdminSelectionManager.Selection selection = AdminSelectionManager.get(player);
            if (selection == null || selection.type != type || !mapName.equals(selection.map)) AdminSelectionManager.begin(player, type, mapName);
            sender.addChatMessage(new ChatComponentText(AdminSelectionManager.pointHere(player, corner == 1)));
            selection = AdminSelectionManager.get(player);
            if (selection != null && selection.type == type && selection.hasA && selection.hasB)
                sender.addChatMessage(new ChatComponentText(AdminSelectionManager.commit(player, type)));
            return;
        }

        if (action.equals("delete")) {
            if (!TDMManager.deleteMap(world, mapName)) {
                sender.addChatMessage(new ChatComponentText("Unknown TDM map: " + mapName));
                return;
            }
            sender.addChatMessage(new ChatComponentText("Deleted TDM map: " + mapName));
            return;
        }

        if (action.equals("select")) {
            TDMManager.TDMGameMode mode = args.length > 3 ? TDMManager.parseMode(args[3]) : TDMManager.getMap(world, mapName).mode;
            if (!TDMManager.selectMap(world, mapName, mode)) {
                sender.addChatMessage(new ChatComponentText("Unknown TDM map: " + mapName));
                return;
            }
            sender.addChatMessage(new ChatComponentText("Selected " + TDMManager.pairLabel(mapName, mode)));
            return;
        }

        if (action.equals("clearspawns")) {
            TDMManager.TDMGameMode mode = args.length > 3 ? TDMManager.parseMode(args[3]) : TDMManager.getMap(world, mapName).mode;
            if (!TDMManager.clearMapSpawns(world, mapName, mode)) {
                sender.addChatMessage(new ChatComponentText("Unknown TDM map: " + mapName));
                return;
            }
            sender.addChatMessage(new ChatComponentText("Cleared spawns for " + TDMManager.pairLabel(mapName, mode)));
            return;
        }

        if (action.equals("addspawn")) {
            if (args.length < 4) {
                sender.addChatMessage(new ChatComponentText("Usage: /tdm map addspawn <map> <red|blue|ffa>"));
                return;
            }

            TDMManager.Team team = args[3].equalsIgnoreCase("ffa") ? null : TDMManager.Team.fromName(args[3]);
            if (team == null && !args[3].equalsIgnoreCase("ffa")) {
                sender.addChatMessage(new ChatComponentText("Unknown TDM team: " + args[3]));
                return;
            }

            EntityPlayer player = getCommandSenderAsPlayer(sender);
            TDMManager.TDMGameMode mode = args.length > 4 ? TDMManager.parseMode(args[4])
                    : team == null ? TDMManager.TDMGameMode.FFA : TDMManager.getMap(world, mapName).mode;
            if (mode == null || (mode == TDMManager.TDMGameMode.FFA) != (team == null)) {
                sender.addChatMessage(new ChatComponentText("FFA uses neutral spawns; team modes use red/blue spawns.")); return;
            }
            TDMManager.addMapSpawn(world, mapName, mode, new TDMManager.SpawnPoint(team, player.dimension,
                    (int)Math.floor(player.posX), (int)Math.floor(player.posY), (int)Math.floor(player.posZ),
                    true, player.rotationYaw, player.rotationPitch));
            sender.addChatMessage(new ChatComponentText(
                    "Spawn added for " + (team==null?"ffa":team.name) + " on " + TDMManager.pairLabel(mapName, mode)
                            + ". Total: " + TDMManager.getMapSpawnCount(world, mapName, mode)
            ));
            return;
        }

        sendMapUsage(sender);
    }

    private void configureMapSetting(ICommandSender sender, World world, String action, String mapName, String[] args) {
        if (!TDMManager.hasMap(world, mapName)) {
            sender.addChatMessage(new ChatComponentText("Unknown TDM map: " + mapName));
            return;
        }
        if (args.length < 4) {
            sender.addChatMessage(new ChatComponentText("Usage: /tdm map " + action + " <map> <" + (action.equals("timer") ? "seconds" : "points") + "|default>"));
            return;
        }
        TDMManager.TDMGameMode settingMode = args.length > 4 ? TDMManager.parseMode(args[4]) : TDMManager.getMap(world, mapName).mode;
        if (settingMode == null) {
            sender.addChatMessage(new ChatComponentText("Mode must be tdm, sd, or ffa.")); return;
        }

        boolean useDefault = args[3].equalsIgnoreCase("default");
        int value = 0;
        if (!useDefault) {
            try {
                value = Integer.parseInt(args[3]);
            } catch (NumberFormatException e) {
                sender.addChatMessage(new ChatComponentText((action.equals("timer") ? "Seconds" : "Score limit") + " must be a positive integer or default."));
                return;
            }
            if (value < 0 || (action.equals("timer") && value == 0)) {
                sender.addChatMessage(new ChatComponentText((action.equals("timer") ? "Seconds must be a positive integer or default." : "Score limit must be a non-negative integer (0 restores the default).")));
                return;
            }
        }

        if (action.equals("timer")) {
            if (value > Integer.MAX_VALUE / 20) {
                sender.addChatMessage(new ChatComponentText("Round timer is too large; seconds must not exceed " + (Integer.MAX_VALUE / 20) + "."));
                return;
            }
            int ticks = value * 20;
            TDMManager.setMapRoundTicks(world, mapName, settingMode, ticks);
            boolean bomb=settingMode==TDMManager.TDMGameMode.BOMB;
            int effectiveSeconds = (bomb?TDMManager.getEffectiveBombRoundTicks(world,mapName):TDMManager.getEffectiveRoundTicks(world, mapName)) / 20;
            sender.addChatMessage(new ChatComponentText("Map " + mapName + " round timer: " + (useDefault ? "default" : value + " seconds") + "; effective: " + effectiveSeconds + " seconds."));
        } else {
            TDMManager.setMapScoreLimit(world, mapName, settingMode, value);
            boolean bomb=settingMode==TDMManager.TDMGameMode.BOMB;
            String label = bomb ? "round-win limit" : "score-point limit (100 points per kill)";
            sender.addChatMessage(new ChatComponentText("Map " + mapName + " " + label + ": " + (useDefault ? "default" : Integer.toString(value)) + "; effective: " + (bomb?TDMManager.getEffectiveBombScoreLimit(world,mapName):TDMManager.getEffectiveScoreLimit(world, mapName)) + "."));
        }
    }

    private void sendMapUsage(ICommandSender sender) {
        sender.addChatMessage(new ChatComponentText("Usage: /tdm map <create|delete|select|voteable|addspawn|updatespawn|removespawn|tpspawn|clearspawns|spawnfallback|legacyspawns|assignlegacyspawn|clearbounds|border|mode|terroristteam|hardcorerespawns|bombsite|economy|lossscore|killscore|roundwinscore|plantscore|defusescore|scorelimit|timer|list>"));
        sender.addChatMessage(new ChatComponentText("  /tdm map voteable <map> <on|off> (all modes) or <map> <tdm|sd|ffa> <on|off>"));
        sender.addChatMessage(new ChatComponentText("  /tdm map scorelimit <map> <value|default> (TDM: score points, 100 per kill; Search and Destroy: round wins, default 13)"));
        sender.addChatMessage(new ChatComponentText("  /tdm map timer <map> <seconds|default>"));
        sender.addChatMessage(new ChatComponentText("  /tdm map bombsite <map> <a|b> <pos1|pos2|clear>"));
    }

    private void sendMapList(ICommandSender sender, World world) {
        List<String> maps = TDMManager.getMapNames(world);
        if (maps.isEmpty()) {
            sender.addChatMessage(new ChatComponentText("No TDM maps defined. Admins can use /tdm map create <map>."));
            return;
        }

        String selected = TDMManager.getSelectedMap(world);
        sender.addChatMessage(new ChatComponentText("Match pairings (selected: " + (selected.length() == 0 ? "none" : TDMManager.pairLabel(selected, TDMManager.getGameMode(world))) + "):"));
        for (String map : maps) {
            TDMManager.TDMMap details=TDMManager.getMap(world,map);
            sender.addChatMessage(new ChatComponentText("- " + map + " voting: " + (details.votingEnabled ? "ON" : "OFF")));
            for (TDMManager.TDMGameMode mode : TDMManager.TDMGameMode.values()) {
                if (!details.supportedModes.contains(mode)) continue;
                String label = TDMManager.pairLabel(map, mode);
                sender.addChatMessage(new ChatComponentText("- " + label + ": spawns=" + details.spawns(mode).size()
                        + (mode == TDMManager.TDMGameMode.BOMB ? ", sites A=" + details.bombsiteA.isComplete()
                        + " B=" + details.bombsiteB.isComplete() + ", economy=" + details.buyScoreEnabled
                        + ", wins=" + TDMManager.getEffectiveBombScoreLimit(world, map)
                        : ", score=" + TDMManager.getEffectiveScoreLimit(world, map))));
            }
        }
        sendVoteCounts(sender, world);
    }

    private void sendVoteCounts(ICommandSender sender, World world) {
        Map<String, Integer> votes = TDMManager.getVoteCounts(world);
        if (votes.isEmpty()) {
            return;
        }

        String message = "Votes: ";
        boolean first = true;
        for (Map.Entry<String, Integer> entry : votes.entrySet()) {
            if (!first) {
                message += ", ";
            }
            message += TDMManager.voteLabel(entry.getKey()) + "=" + entry.getValue();
            first = false;
        }
        sender.addChatMessage(new ChatComponentText(message));
    }

    private String join(List<String> values) {
        String joined = "";
        for (int i = 0; i < values.size(); i++) {
            if (i > 0) {
                joined += ", ";
            }
            joined += values.get(i);
        }
        return joined;
    }

    private Boolean parseToggle(String value) {
        if (value.equalsIgnoreCase("on") || value.equalsIgnoreCase("true") || value.equalsIgnoreCase("enabled")) {
            return Boolean.TRUE;
        }

        if (value.equalsIgnoreCase("off") || value.equalsIgnoreCase("false") || value.equalsIgnoreCase("disabled")) {
            return Boolean.FALSE;
        }

        return null;
    }

    private boolean isAdmin(ICommandSender sender) {
        return sender.canCommandSenderUseCommand(4, getCommandName());
    }

    @Override
    public List addTabCompletionOptions(ICommandSender sender, String[] args) {
        if (args.length == 1) {
            List<String> commands = new ArrayList<String>(Arrays.asList("help", "maps", "vote", "skip", "menu", "teamchange", "utility", "killstreak"));
            commands.add("overlay");
            if (isAdmin(sender)) commands.addAll(Arrays.asList("boundaryview", "editor", "kits", "kit", "toggle", "bombtest", "testsound", "forceroundend", "forcemapvote", "friendlyfire", "autobalance", "map", "addspawn", "setteam", "teamless", "clear"));
            return getListOfStringsMatchingLastWord(args, commands.toArray(new String[0]));
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("help")) return getListOfStringsMatchingLastWord(args, isAdmin(sender) ? new String[] {"general", "match", "teams", "loadouts", "kits", "maps", "admin"} : new String[] {"general", "match", "teams", "loadouts"});
        if (args.length == 2 && args[0].equalsIgnoreCase("skip")) return getListOfStringsMatchingLastWord(args, "yes", "no", "status");
        if (args.length == 2 && args[0].equalsIgnoreCase("vote")) return getListOfStringsMatchingLastWord(args, TDMManager.getMapNames(sender.getEntityWorld()).toArray(new String[0]));
        if (args.length == 3 && args[0].equalsIgnoreCase("vote")) return getListOfStringsMatchingLastWord(args, "tdm", "sd", "ffa");
        if(args.length==2&&(args[0].equalsIgnoreCase("utility")||args[0].equalsIgnoreCase("killstreak")))return getListOfStringsMatchingLastWord(args,isAdmin(sender)?new String[]{"list","buy","add","remove"}:new String[]{"list","buy"});
        if (args.length == 2 && args[0].equalsIgnoreCase("overlay")) return getListOfStringsMatchingLastWord(args, "on", "off");
        if (!isAdmin(sender)) return null;
        if (args.length == 2 && args[0].equalsIgnoreCase("boundaryview")) return getListOfStringsMatchingLastWord(args, "on", "off");
        if (args.length == 3 && args[0].equalsIgnoreCase("overlay") && args[1].equalsIgnoreCase("on")) return completeMaps(args, sender, false);
        if (args.length == 2 && args[0].equalsIgnoreCase("kit")) return getListOfStringsMatchingLastWord(args, "list", "add", "edit", "commit", "cancel", "status", "clone", "rename", "cost", "mode", "remove");
        if (args.length == 2 && args[0].equalsIgnoreCase("editor")) return getListOfStringsMatchingLastWord(args, "gui", "status", "select", "point", "commit", "cancel");
        if (args.length == 3 && args[0].equalsIgnoreCase("editor") && args[1].equalsIgnoreCase("select")) return getListOfStringsMatchingLastWord(args, "map", "bomb_a", "bomb_b");
        if (args.length == 3 && args[0].equalsIgnoreCase("editor") && args[1].equalsIgnoreCase("point")) return getListOfStringsMatchingLastWord(args, "a", "b");
        if (args.length == 3 && args[0].equalsIgnoreCase("kit") && (args[1].equalsIgnoreCase("add") || args[1].equalsIgnoreCase("remove"))) return getListOfStringsMatchingLastWord(args, "red", "blue");
        if (args[0].equalsIgnoreCase("kit") && ((args.length == 3 && args[1].equalsIgnoreCase("list")) || (args.length == 4 && args[1].equalsIgnoreCase("add")) || (args.length == 5 && args[1].equalsIgnoreCase("remove")))) return completeMaps(args, sender, true);
        if (args.length == 2 && args[0].equalsIgnoreCase("map")) return getListOfStringsMatchingLastWord(args, "list", "create", "delete", "select", "voteable", "addspawn", "updatespawn", "removespawn", "tpspawn", "clearspawns", "spawnfallback", "legacyspawns", "assignlegacyspawn", "clearbounds", "border", "pointlimit", "scorelimit", "timer", "mode", "terroristteam", "hardcorerespawns", "bombsite", "economy", "killstreaks", "killscorereward", "killscore", "lossscore", "roundwinscore", "plantscore", "defusescore");
        if (args.length == 3 && args[0].equalsIgnoreCase("map") && !args[1].equalsIgnoreCase("create") && !args[1].equalsIgnoreCase("list")) return completeMaps(args, sender, false);
        if (args.length == 4 && args[0].equalsIgnoreCase("map")) {
            if (args[1].equalsIgnoreCase("voteable")) return getListOfStringsMatchingLastWord(args, "on", "off", "tdm", "sd", "ffa");
            if (args[1].equalsIgnoreCase("mode")) return getListOfStringsMatchingLastWord(args, "deathmatch", "bomb", "ffa");
            if (args[1].equalsIgnoreCase("addspawn")) return getListOfStringsMatchingLastWord(args, "red", "blue", "ffa");
            if (args[1].equalsIgnoreCase("terroristteam")) return getListOfStringsMatchingLastWord(args, "red", "blue");
            if (args[1].equalsIgnoreCase("hardcorerespawns") || args[1].equalsIgnoreCase("economy") || args[1].equalsIgnoreCase("killstreaks")) return getListOfStringsMatchingLastWord(args, "true", "false");
            if (args[1].equalsIgnoreCase("scorelimit") || args[1].equalsIgnoreCase("pointlimit") || args[1].equalsIgnoreCase("timer")) return getListOfStringsMatchingLastWord(args, "default");
            if (args[1].equalsIgnoreCase("bombsite")) return getListOfStringsMatchingLastWord(args, "a", "b");
        }
        if (args.length == 5 && args[0].equalsIgnoreCase("map") && args[1].equalsIgnoreCase("voteable")) return getListOfStringsMatchingLastWord(args, "on", "off");
        if (args.length == 5 && args[0].equalsIgnoreCase("map") && args[1].equalsIgnoreCase("bombsite")) return getListOfStringsMatchingLastWord(args, "pos1", "pos2", "clear");
        if (args.length == 2 && (args[0].equalsIgnoreCase("friendlyfire") || args[0].equalsIgnoreCase("bombtest"))) return getListOfStringsMatchingLastWord(args, "on", "off", "status");
        if (args.length == 2 && args[0].equalsIgnoreCase("autobalance")) return getListOfStringsMatchingLastWord(args, "on", "off", "now");
        if (args.length == 2 && args[0].equalsIgnoreCase("forceroundend")) return getListOfStringsMatchingLastWord(args, "red", "blue", "terrorist", "ct", "counterterrorist", "abort");
        if (args.length == 2 && args[0].equalsIgnoreCase("testsound")) return getListOfStringsMatchingLastWord(args, "ctwin", "twin", "ctstart", "tstart", "bombplant");
        if (args.length == 2 && args[0].equalsIgnoreCase("addspawn")) return getListOfStringsMatchingLastWord(args, "red", "blue");
        if (args.length == 2 && args[0].equalsIgnoreCase("setteam")) return getListOfStringsMatchingLastWord(args, MinecraftServer.getServer().getAllUsernames());
        if (args.length == 3 && args[0].equalsIgnoreCase("setteam")) return getListOfStringsMatchingLastWord(args, "red", "blue");
        return null;
    }

    private List completeMaps(String[] args, ICommandSender sender, boolean includeGlobal) {
        List<String> maps = new ArrayList<String>(TDMManager.getMapNames(sender.getEntityWorld()));
        if (includeGlobal) maps.add("global");
        return getListOfStringsMatchingLastWord(args, maps.toArray(new String[0]));
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }

    @Override
    public boolean canCommandSenderUseCommand(ICommandSender sender) {
        return true;
    }

    public static final String ERROR = EnumChatFormatting.RED.toString();
    public static final String TITLE = EnumChatFormatting.GOLD.toString();
    public static final String HELP = EnumChatFormatting.DARK_GREEN.toString();
    public static final String INFO = EnumChatFormatting.GREEN.toString();
    public static final String COMMAND = EnumChatFormatting.RED.toString();
    public static final String COMMAND_ADMIN = EnumChatFormatting.DARK_PURPLE.toString();
}
