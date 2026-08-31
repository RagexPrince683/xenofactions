package com.hfr.command;

import com.hfr.packet.PacketDispatcher;
import com.hfr.packet.effect.TDMMenuDataPacket;
import com.hfr.tdm.TDMKitManager;
import com.hfr.tdm.TDMBombManager;
import com.hfr.tdm.TDMManager;
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

        if (args[0].equalsIgnoreCase("maps") || args[0].equalsIgnoreCase("listmaps")) {
            sendMapList(sender, world);
            return;
        }

        if (args[0].equalsIgnoreCase("vote")) {
            if (args.length < 2) {
                sender.addChatMessage(new ChatComponentText("Usage: /tdm vote <map>"));
                return;
            }

            EntityPlayer player = getCommandSenderAsPlayer(sender);
            String votedMap = TDMManager.voteForMap(world, player.getCommandSenderName(), args[1]);
            if (votedMap == null) {
                sender.addChatMessage(new ChatComponentText("Unable to vote. A TDM map vote must be active and the map must exist."));
                return;
            }

            sender.addChatMessage(new ChatComponentText("Voted for TDM map " + votedMap + "."));
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
                sender.addChatMessage(new ChatComponentText("Single-player BOMB testing is " + (TDMManager.isBombTestMode() ? "enabled" : "disabled") + ". Usage: /tdm bombtest <on|off>"));
                return;
            }
            Boolean enabled = parseToggle(args[1]);
            if (enabled == null) {
                sender.addChatMessage(new ChatComponentText("Usage: /tdm bombtest <on|off>"));
                return;
            }
            TDMManager.setBombTestMode(world, enabled.booleanValue());
            sender.addChatMessage(new ChatComponentText("Single-player BOMB testing " + (enabled.booleanValue() ? "enabled." : "disabled.")));
            return;
        }

        if (args[0].equalsIgnoreCase("testsound")) {
            processTestSound(sender, args, world);
            return;
        }

        if(args[0].equalsIgnoreCase("forceroundend")){if(args.length!=2){sender.addChatMessage(new ChatComponentText("Usage: /tdm forceroundend <red|blue|terrorist|ct|counterterrorist|abort>"));return;}String target=args[1].toLowerCase();boolean abort="abort".equals(target);TDMManager.Team winner=abort?null:("terrorist".equals(target)?TDMManager.getTerroristTeam(world):("ct".equals(target)||"counterterrorist".equals(target)?TDMManager.getCounterTerroristTeam(world):TDMManager.Team.fromName(target)));if(!abort&&winner==null){sender.addChatMessage(new ChatComponentText("Unknown round result: "+args[1]));return;}if(!TDMBombManager.forceRoundEnd(world,winner,abort)){sender.addChatMessage(new ChatComponentText("There is no active BOMB round to end."));return;}sender.addChatMessage(new ChatComponentText(abort?"BOMB round aborted.":winner.name+" administratively won the BOMB round."));return;}

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
            sender.addChatMessage(new ChatComponentText("Forced a 30 second TDM map vote."));
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
            helpLine(sender, false, "help [category]", "Example: /tdm help teams");
        } else if (category.equals("match")) {
            helpLine(sender, false, "vote <map>", "Vote for an enumerated map during a map vote.");
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
            helpLine(sender, true, "kit list [map|global]", "List configured loadouts and BOMB costs.");
            helpLine(sender, true, "kit add <red|blue> [map|global] [cost]", "Save inventory; example: /tdm kit add red arena 3");
            helpLine(sender, true, "kit remove <red|blue> <number> [map|global]", "Remove a numbered kit from kit list.");
            helpLine(sender,true,"utility <list|add|remove>","Manage BOMB buy-score utility definitions.");
            helpLine(sender,true,"killstreak <list|add|remove>","Manage kill-score reward definitions.");
        } else if (category.equals("maps")) {
            helpLine(sender, false, "maps", "List maps, modes, timers, point limits, and active votes.");
            if(isAdmin(sender)){helpLine(sender, true, "map <create|delete|select> <map>", "Manage maps.");
            helpLine(sender, true, "map addspawn <map> <red|blue|ffa>", "Add your current position.");
            helpLine(sender, true, "map <pointlimit|timer> <map> <value|default>", "Set DM/FFA point-score victory limit or timer (scorelimit is an alias).");
            helpLine(sender, true, "map mode <map> <deathmatch|bomb|ffa>", "Set isolated lifecycle policy.");
            helpLine(sender, true, "map bombsite <map> <a|b> <pos1|pos2|clear>", "Configure BOMB objective bounds.");
            helpLine(sender, true, "map terroristteam <map> <red|blue>", "Assign the BOMB Terrorist role.");
            helpLine(sender, true, "map <hardcorerespawns|economy> <map> <true|false>", "Configure BOMB-only policy.");}
        } else {
            helpLine(sender, true, "toggle", "Enable or disable TDM.");
            helpLine(sender, true, "forcemapvote", "Start a 30-second vote.");
            helpLine(sender, true, "forceroundend <red|blue|terrorist|ct|abort>", "End an active BOMB round.");
            helpLine(sender, true, "friendlyfire <on|off>", "Set team damage.");
            helpLine(sender, true, "autobalance <on|off|now>", "Configure or run team balancing.");
            helpLine(sender, true, "setteam <player> <red|blue>", "Assign an online player.");
            helpLine(sender, true, "bombtest <on|off|status>", "Transient single-player BOMB testing.");
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

    private void processKitCommand(ICommandSender sender, String[] args) {
        if (args.length < 2 || args[1].equalsIgnoreCase("help")) {
            sender.addChatMessage(new ChatComponentText("Usage: /tdm kit <list|add|remove> ..."));
            sender.addChatMessage(new ChatComponentText("  /tdm kit list [map|global]"));
            sender.addChatMessage(new ChatComponentText("  /tdm kit add <red|blue> [map|global]"));
            sender.addChatMessage(new ChatComponentText("  /tdm kit remove <red|blue> <number> [map|global]"));
            return;
        }

        if (args[1].equalsIgnoreCase("list")) {
            String mapName = args.length >= 3 ? normalizeKitMap(args[2]) : TDMManager.getSelectedMap(sender.getEntityWorld());
            sendKitList(sender, mapName);
            return;
        }

        if (args[1].equalsIgnoreCase("add") || args[1].equalsIgnoreCase("save")) {
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

        if (!action.equals("create") && !action.equals("delete") && !action.equals("select") && !action.equals("addspawn") && !action.equals("clearspawns") && !action.equals("scorelimit") && !action.equals("pointlimit") && !action.equals("timer") && !action.equals("mode") && !action.equals("terroristteam") && !action.equals("hardcorerespawns") && !action.equals("bombsite") && !action.equals("economy") && !action.equals("killstreaks") && !action.equals("killscorereward") && !action.equals("killscore") && !action.equals("defusescore") && !action.equals("lossscore") && !action.equals("plantscore") && !action.equals("roundwinscore")) {
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
            sender.addChatMessage(new ChatComponentText("Created TDM map: " + mapName));
            return;
        }

        if (action.equals("scorelimit") || action.equals("pointlimit") || action.equals("timer")) {
            configureMapSetting(sender, world, action.equals("pointlimit")?"scorelimit":action, mapName, args);
            return;
        }

        if(!TDMManager.hasMap(world,mapName)){sender.addChatMessage(new ChatComponentText("Unknown TDM map: "+mapName));return;}
        if(action.equals("economy")){if(args.length<4){sender.addChatMessage(new ChatComponentText("Usage: /tdm map economy <map> <true|false>"));return;}Boolean value=parseToggle(args[3]);if(value==null){sender.addChatMessage(new ChatComponentText("Economy must be true/false or on/off."));return;}TDMManager.TDMMap m=TDMManager.getMap(world,mapName);if(m.mode!=TDMManager.TDMGameMode.BOMB){sender.addChatMessage(new ChatComponentText("Economy is available only on BOMB maps."));return;}m.buyScoreEnabled=value.booleanValue();com.hfr.tdm.TDMData.get(world).markDirty();sender.addChatMessage(new ChatComponentText("Map "+mapName+" economy: "+value));return;}
        if(action.equals("killstreaks")){if(args.length<4){sender.addChatMessage(new ChatComponentText("Usage: /tdm map killstreaks <map> <true|false>"));return;}Boolean value=parseToggle(args[3]);TDMManager.TDMMap m=TDMManager.getMap(world,mapName);if(value==null||m.mode==TDMManager.TDMGameMode.BOMB){sender.addChatMessage(new ChatComponentText("Killstreaks require true/false on a DEATHMATCH or FFA map."));return;}m.killstreaksEnabled=value.booleanValue();com.hfr.tdm.TDMData.get(world).markDirty();sender.addChatMessage(new ChatComponentText("Map "+mapName+" killstreaks: "+value));return;}
        if(action.equals("killscorereward")){if(args.length<4){sender.addChatMessage(new ChatComponentText("Usage: /tdm map killscorereward <map> <amount>"));return;}int amount;try{amount=Integer.parseInt(args[3]);}catch(NumberFormatException e){sender.addChatMessage(new ChatComponentText("Amount must be a non-negative integer."));return;}if(amount<0){sender.addChatMessage(new ChatComponentText("Amount must be a non-negative integer."));return;}TDMManager.TDMMap m=TDMManager.getMap(world,mapName);if(m.mode==TDMManager.TDMGameMode.BOMB){sender.addChatMessage(new ChatComponentText("Kill score is available only in configured respawn modes."));return;}m.killScoreReward=amount;com.hfr.tdm.TDMData.get(world).markDirty();sender.addChatMessage(new ChatComponentText("Map "+mapName+" kill-score reward: "+amount));return;}
        if(action.equals("killscore")||action.equals("defusescore")||action.equals("lossscore")||action.equals("plantscore")||action.equals("roundwinscore")){if(args.length<4){sender.addChatMessage(new ChatComponentText("Usage: /tdm map "+action+" <map> <amount>"));return;}int amount;try{amount=Integer.parseInt(args[3]);}catch(NumberFormatException e){sender.addChatMessage(new ChatComponentText("Amount must be a non-negative integer."));return;}if(amount<0){sender.addChatMessage(new ChatComponentText("Amount must be a non-negative integer."));return;}TDMManager.TDMMap m=TDMManager.getMap(world,mapName);if(m.mode!=TDMManager.TDMGameMode.BOMB){sender.addChatMessage(new ChatComponentText("Economy rewards are available only on BOMB maps."));return;}if(action.equals("killscore"))m.killBuyScoreReward=amount;else if(action.equals("lossscore"))m.roundLossBuyScoreReward=amount;else if(action.equals("plantscore"))m.bombPlantBuyScoreReward=amount;else if(action.equals("roundwinscore"))m.roundWinBuyScoreReward=amount;else m.bombDefuseBuyScoreReward=amount;com.hfr.tdm.TDMData.get(world).markDirty();sender.addChatMessage(new ChatComponentText("Map "+mapName+" "+action+": "+amount));return;}
        if(action.equals("mode")){if(args.length<4){sender.addChatMessage(new ChatComponentText("Usage: /tdm map mode <map> <deathmatch|bomb|ffa>"));return;}TDMManager.TDMGameMode mode;try{mode=TDMManager.TDMGameMode.valueOf(args[3].toUpperCase());}catch(IllegalArgumentException e){sender.addChatMessage(new ChatComponentText("Mode must be deathmatch, bomb, or ffa."));return;}TDMManager.setMapMode(world,mapName,mode);String feedback="Map "+mapName+" mode set to "+mode.name().toLowerCase()+".";if(mode==TDMManager.TDMGameMode.BOMB&&TDMManager.isEnabled(world)&&TDMManager.getSelectedMap(world).equals(mapName)){if(TDMManager.isBombTestMode())feedback+=" Single-player BOMB testing is enabled.";else if(!com.hfr.tdm.TDMBombManager.hasBothTeams(world))feedback+=" Waiting for at least one RED and one BLUE player.";}sender.addChatMessage(new ChatComponentText(feedback));return;}
        if(action.equals("terroristteam")){if(args.length<4){sender.addChatMessage(new ChatComponentText("Usage: /tdm map terroristteam <map> <red|blue>"));return;}TDMManager.Team team=TDMManager.Team.fromName(args[3]);if(team==null){sender.addChatMessage(new ChatComponentText("Team must be red or blue."));return;}TDMManager.configureMap(world,mapName,null,team,null);sender.addChatMessage(new ChatComponentText("Map "+mapName+" Terrorists: "+team.name));return;}
        if (action.equals("hardcorerespawns")) {
            if (args.length < 4 || (!args[3].equalsIgnoreCase("true")
                    && !args[3].equalsIgnoreCase("false"))) {
                sender.addChatMessage(new ChatComponentText(
                        "Usage: /tdm map hardcorerespawns <map> <true|false>"));
                return;
            }
            TDMManager.TDMMap map = TDMManager.getMap(world, mapName);
            if (map.mode != TDMManager.TDMGameMode.BOMB) {
                sender.addChatMessage(new ChatComponentText(
                        "Hardcore round elimination is available only on competitive BOMB maps; Deathmatch and FFA are continuous."));
                return;
            }
            TDMManager.configureMap(world, mapName, null, null, Boolean.valueOf(args[3]));
            sender.addChatMessage(new ChatComponentText("Map " + mapName
                    + " hardcore respawns: " + args[3].toLowerCase()));
            return;
        }
        if(action.equals("bombsite")){if(args.length<5){sender.addChatMessage(new ChatComponentText("Usage: /tdm map bombsite <map> <a|b> <pos1|pos2|clear>"));return;}boolean a=args[3].equalsIgnoreCase("a");if(!a&&!args[3].equalsIgnoreCase("b")){sender.addChatMessage(new ChatComponentText("Bombsite must be A or B."));return;}if(args[4].equalsIgnoreCase("clear")){TDMManager.clearBombsite(world,mapName,a);sender.addChatMessage(new ChatComponentText("Cleared bombsite "+(a?"A":"B")+"."));return;}int corner=args[4].equalsIgnoreCase("pos1")?1:args[4].equalsIgnoreCase("pos2")?2:0;if(corner==0){sender.addChatMessage(new ChatComponentText("Use pos1, pos2, or clear."));return;}EntityPlayer p=getCommandSenderAsPlayer(sender);if(!TDMManager.setBombsite(world,mapName,a,corner,p.dimension,(int)Math.floor(p.posX),(int)Math.floor(p.posY),(int)Math.floor(p.posZ))){sender.addChatMessage(new ChatComponentText("Both bombsite corners must be in the same dimension."));return;}sender.addChatMessage(new ChatComponentText("Set bombsite "+(a?"A":"B")+" pos"+corner+"."));return;}

        if (action.equals("delete")) {
            if (!TDMManager.deleteMap(world, mapName)) {
                sender.addChatMessage(new ChatComponentText("Unknown TDM map: " + mapName));
                return;
            }
            sender.addChatMessage(new ChatComponentText("Deleted TDM map: " + mapName));
            return;
        }

        if (action.equals("select")) {
            if (!TDMManager.selectMap(world, mapName)) {
                sender.addChatMessage(new ChatComponentText("Unknown TDM map: " + mapName));
                return;
            }
            sender.addChatMessage(new ChatComponentText("Selected TDM map: " + mapName));
            return;
        }

        if (action.equals("clearspawns")) {
            if (!TDMManager.clearMapSpawns(world, mapName)) {
                sender.addChatMessage(new ChatComponentText("Unknown TDM map: " + mapName));
                return;
            }
            sender.addChatMessage(new ChatComponentText("Cleared spawns for TDM map: " + mapName));
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
            TDMManager.addMapSpawn(world, mapName, team, player.dimension, (int) player.posX, (int) player.posY, (int) player.posZ);
            sender.addChatMessage(new ChatComponentText(
                    "Spawn added for " + (team==null?"ffa":team.name) + " on map " + mapName + ". Total: " + TDMManager.getMapSpawnCount(world, mapName)
                            + " (red: " + TDMManager.getMapSpawnCount(world, mapName, TDMManager.Team.RED)
                            + ", blue: " + TDMManager.getMapSpawnCount(world, mapName, TDMManager.Team.BLUE) + ")"
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
            TDMManager.setMapRoundTicks(world, mapName, ticks);
            boolean bomb=TDMManager.getMap(world,mapName).mode==TDMManager.TDMGameMode.BOMB;
            int effectiveSeconds = (bomb?TDMManager.getEffectiveBombRoundTicks(world,mapName):TDMManager.getEffectiveRoundTicks(world, mapName)) / 20;
            sender.addChatMessage(new ChatComponentText("Map " + mapName + " round timer: " + (useDefault ? "default" : value + " seconds") + "; effective: " + effectiveSeconds + " seconds."));
        } else {
            TDMManager.setMapScoreLimit(world, mapName, value);
            boolean bomb=TDMManager.getMap(world,mapName).mode==TDMManager.TDMGameMode.BOMB;
            String label = bomb ? "round-win limit" : "score-point limit (100 points per kill)";
            sender.addChatMessage(new ChatComponentText("Map " + mapName + " " + label + ": " + (useDefault ? "default" : Integer.toString(value)) + "; effective: " + (bomb?TDMManager.getEffectiveBombScoreLimit(world,mapName):TDMManager.getEffectiveScoreLimit(world, mapName)) + "."));
        }
    }

    private void sendMapUsage(ICommandSender sender) {
        sender.addChatMessage(new ChatComponentText("Usage: /tdm map <create|delete|select|addspawn|clearspawns|mode|terroristteam|hardcorerespawns|bombsite|economy|lossscore|killscore|roundwinscore|plantscore|defusescore|scorelimit|timer|list>"));
        sender.addChatMessage(new ChatComponentText("  /tdm map scorelimit <map> <value|default> (DEATHMATCH: score points, 100 per kill; BOMB: round wins, default 13)"));
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
        sender.addChatMessage(new ChatComponentText("TDM maps (selected: " + (selected.length() == 0 ? "none" : selected) + "):"));
        for (String map : maps) {
            TDMManager.TDMMap details=TDMManager.getMap(world,map);
            if(details.mode==TDMManager.TDMGameMode.BOMB){sender.addChatMessage(new ChatComponentText("- "+map+": BOMB (hardcore="+details.hardcoreRespawns+"), T="+details.terroristTeam.name+", CT="+(details.terroristTeam==TDMManager.Team.RED?"blue":"red")+", economy="+details.buyScoreEnabled+", loss/kill/win/plant buy score="+details.roundLossBuyScoreReward+"/"+details.killBuyScoreReward+"/"+details.roundWinBuyScoreReward+"/"+details.bombPlantBuyScoreReward+", defuse buy score="+details.bombDefuseBuyScoreReward+", wins="+TDMManager.getEffectiveBombScoreLimit(world,map)+(details.bombScoreLimitOverride==0?" (default)":"")+", timer="+(TDMManager.getEffectiveBombRoundTicks(world,map)/20)+"s"+(details.bombRoundTicksOverride==0?" (default)":"")+", sites A="+details.bombsiteA.isComplete()+" B="+details.bombsiteB.isComplete()));continue;}
            boolean defaultScore = TDMManager.getScoreLimitOverride(world, map) == 0;
            boolean defaultTimer = TDMManager.getRoundTicksOverride(world, map) == 0;
            String scoreLabel = details.mode == TDMManager.TDMGameMode.DEATHMATCH ? "score points " : "score ";
            sender.addChatMessage(new ChatComponentText("- " + map + ": "+details.mode.name()+", continuous, " + scoreLabel + TDMManager.getEffectiveScoreLimit(world, map)
                    + (defaultScore ? " (default)" : "") + ", round " + (TDMManager.getEffectiveRoundTicks(world, map) / 20)
                    + "s" + (defaultTimer ? " (default)" : "") + ", respawn loadouts=economy-free"));
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
            message += entry.getKey() + "=" + entry.getValue();
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
            if (isAdmin(sender)) commands.addAll(Arrays.asList("kits", "kit", "toggle", "bombtest", "testsound", "forceroundend", "forcemapvote", "friendlyfire", "autobalance", "map", "addspawn", "setteam", "teamless", "clear"));
            return getListOfStringsMatchingLastWord(args, commands.toArray(new String[0]));
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("help")) return getListOfStringsMatchingLastWord(args, isAdmin(sender) ? new String[] {"general", "match", "teams", "loadouts", "kits", "maps", "admin"} : new String[] {"general", "match", "teams", "loadouts"});
        if (args.length == 2 && args[0].equalsIgnoreCase("skip")) return getListOfStringsMatchingLastWord(args, "yes", "no", "status");
        if (args.length == 2 && args[0].equalsIgnoreCase("vote")) return getListOfStringsMatchingLastWord(args, TDMManager.getMapNames(sender.getEntityWorld()).toArray(new String[0]));
        if(args.length==2&&(args[0].equalsIgnoreCase("utility")||args[0].equalsIgnoreCase("killstreak")))return getListOfStringsMatchingLastWord(args,isAdmin(sender)?new String[]{"list","buy","add","remove"}:new String[]{"list","buy"});
        if (!isAdmin(sender)) return null;
        if (args.length == 2 && args[0].equalsIgnoreCase("kit")) return getListOfStringsMatchingLastWord(args, "list", "add", "remove");
        if (args.length == 3 && args[0].equalsIgnoreCase("kit") && (args[1].equalsIgnoreCase("add") || args[1].equalsIgnoreCase("remove"))) return getListOfStringsMatchingLastWord(args, "red", "blue");
        if (args[0].equalsIgnoreCase("kit") && ((args.length == 3 && args[1].equalsIgnoreCase("list")) || (args.length == 4 && args[1].equalsIgnoreCase("add")) || (args.length == 5 && args[1].equalsIgnoreCase("remove")))) return completeMaps(args, sender, true);
        if (args.length == 2 && args[0].equalsIgnoreCase("map")) return getListOfStringsMatchingLastWord(args, "list", "create", "delete", "select", "addspawn", "clearspawns", "pointlimit", "scorelimit", "timer", "mode", "terroristteam", "hardcorerespawns", "bombsite", "economy", "killstreaks", "killscorereward", "killscore", "lossscore", "roundwinscore", "plantscore", "defusescore");
        if (args.length == 3 && args[0].equalsIgnoreCase("map") && !args[1].equalsIgnoreCase("create") && !args[1].equalsIgnoreCase("list")) return completeMaps(args, sender, false);
        if (args.length == 4 && args[0].equalsIgnoreCase("map")) {
            if (args[1].equalsIgnoreCase("mode")) return getListOfStringsMatchingLastWord(args, "deathmatch", "bomb", "ffa");
            if (args[1].equalsIgnoreCase("addspawn")) return getListOfStringsMatchingLastWord(args, "red", "blue", "ffa");
            if (args[1].equalsIgnoreCase("terroristteam")) return getListOfStringsMatchingLastWord(args, "red", "blue");
            if (args[1].equalsIgnoreCase("hardcorerespawns") || args[1].equalsIgnoreCase("economy") || args[1].equalsIgnoreCase("killstreaks")) return getListOfStringsMatchingLastWord(args, "true", "false");
            if (args[1].equalsIgnoreCase("scorelimit") || args[1].equalsIgnoreCase("pointlimit") || args[1].equalsIgnoreCase("timer")) return getListOfStringsMatchingLastWord(args, "default");
            if (args[1].equalsIgnoreCase("bombsite")) return getListOfStringsMatchingLastWord(args, "a", "b");
        }
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
