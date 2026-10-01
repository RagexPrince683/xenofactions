package com.hfr.inventory.gui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import com.hfr.packet.PacketDispatcher;
import com.hfr.packet.client.AdminEditorActionPacket;
import com.hfr.packet.client.AdminEditorNavigatePacket;
import com.hfr.main.ClientProxy;
import com.hfr.clowder.TerritoryCoordinateBounds;
import com.hfr.tdm.BlockAreaEdges;

/** Draggable, session-positioned admin panel. Buttons call the existing server-authoritative commands. */
public final class GUIAdminEditor extends GuiScreen {
    private static int panelX = -1, panelY = -1;
    private int page, spawnIndex, spawnType, rewardIndex, spawnMode;
    private static final String[] MODE_IDS = { "DEATHMATCH", "BOMB", "FFA" };
    private static final String[] MODE_LABELS = { "TDM", "Search and Destroy", "FFA" };
    private static final String[] REWARD_KEYS = { "killscorereward", "killscore", "lossscore", "roundwinscore", "plantscore", "defusescore" };
    private static boolean visualize = true;
    private static NBTTagCompound liveSelection = new NBTTagCompound();
    private String confirmation = "";
    private final NBTTagCompound data;
    private final RenderItem itemRenderer = new RenderItem();
    private GuiTextField input;
    private boolean dragging;
    private int dragX, dragY;
    private int panelWidth, panelHeight;

    public GUIAdminEditor(NBTTagCompound data) {
        this.data = data == null ? new NBTTagCompound() : data;
        page = this.data.getInteger("page");
        spawnMode = this.data.getInteger("editorMode");
        spawnIndex = this.data.getInteger("spawnIndex");
        spawnType = this.data.getInteger("spawnType");
        rewardIndex = this.data.getInteger("rewardIndex");
        updateSelection(this.data.getCompoundTag("selection"));
    }
    public static void updateSelection(NBTTagCompound selection) {
        liveSelection = selection == null ? new NBTTagCompound() : (NBTTagCompound)selection.copy();
        if (Minecraft.getMinecraft().currentScreen instanceof GUIAdminEditor)
            ((GUIAdminEditor)Minecraft.getMinecraft().currentScreen).data.setTag("selection", liveSelection.copy());
    }
    public static void clearSelectionPreview() { liveSelection = new NBTTagCompound(); }
    public static boolean hasActiveSelectionPreview() { return liveSelection.hasKey("type"); }
    public static void drawSelectionPreview() {
        Minecraft mc = Minecraft.getMinecraft();
        NBTTagCompound s = liveSelection;
        if (mc.currentScreen != null || mc.theWorld == null || mc.thePlayer == null || !s.hasKey("type")
                || s.getInteger("dim") != mc.thePlayer.dimension) return;
        ScaledResolution size = new ScaledResolution(mc, mc.displayWidth, mc.displayHeight);
        int x = size.getScaledWidth() - 96, y = 8, cell = 4, side = 17 * cell;
        Gui.drawRect(x - 4, y - 4, x + 91, y + 105, 0xCC18212D);
        String name = s.getString("type") + (s.getString("map").length() == 0 ? "" : " / " + s.getString("map"));
        mc.fontRenderer.drawString(name.length() > 15 ? name.substring(0, 15) : name, x, y, 0xFFFFFF);
        y += 13;
        Gui.drawRect(x, y, x + side, y + side, 0xFF293540);
        int cx = (int)Math.floor(mc.thePlayer.posX) >> 4, cz = (int)Math.floor(mc.thePlayer.posZ) >> 4;
        if (s.getBoolean("hasA") && s.getBoolean("hasB")) {
            BlockAreaEdges edges = BlockAreaEdges.of(s.getInteger("ax"), s.getInteger("az"), s.getInteger("bx"), s.getInteger("bz"));
            long originX = ((long)cx - 8L) * 16L, originZ = ((long)cz - 8L) * 16L;
            if (edges.maxXExclusive > originX && edges.minX < originX + 272L
                    && edges.maxZExclusive > originZ && edges.minZ < originZ + 272L) {
                int x1 = previewPixel(edges.minX, originX, x, cell), x2 = previewPixel(edges.maxXExclusive, originX, x, cell);
                int z1 = previewPixel(edges.minZ, originZ, y, cell), z2 = previewPixel(edges.maxZExclusive, originZ, y, cell);
                x2 = Math.max(x1 + 1, x2); z2 = Math.max(z1 + 1, z2);
                Gui.drawRect(x1, z1, x2, z1 + 1, 0xFFFFFFFF);
                Gui.drawRect(x1, z2 - 1, x2, z2, 0xFFFFFFFF);
                Gui.drawRect(x1, z1, x1 + 1, z2, 0xFFFFFFFF);
                Gui.drawRect(x2 - 1, z1, x2, z2, 0xFFFFFFFF);
            }
        }
        if (s.getBoolean("hasA")) hudPoint(x, y, cell, cx, cz, s.getInteger("ax"), s.getInteger("az"), 0xFFFFFF66);
        if (s.getBoolean("hasB")) hudPoint(x, y, cell, cx, cz, s.getInteger("bx"), s.getInteger("bz"), 0xFFFF66FF);
        Gui.drawRect(x + 8 * cell + 1, y + 8 * cell + 1, x + 8 * cell + 3, y + 8 * cell + 3, 0xFFFFFFFF);
        mc.fontRenderer.drawString("A: " + (s.getBoolean("hasA") ? s.getInteger("ax") + "," + s.getInteger("ay") + "," + s.getInteger("az") : "unset"), x, y + side + 3, 0xFFFFFF66);
        mc.fontRenderer.drawString("B: " + (s.getBoolean("hasB") ? s.getInteger("bx") + "," + s.getInteger("by") + "," + s.getInteger("bz") : "unset"), x, y + side + 14, 0xFFFF66FF);
    }
    private static void hudPoint(int x, int y, int cell, int cx, int cz, int px, int pz, int color) {
        long originX = ((long)cx - 8L) * 16L, originZ = ((long)cz - 8L) * 16L;
        if (px >= originX && px < originX + 272L && pz >= originZ && pz < originZ + 272L) {
            int sx = previewPixel(px, originX, x, cell), sz = previewPixel(pz, originZ, y, cell);
            Gui.drawRect(sx, sz, sx + 2, sz + 2, color);
        }
    }
    private String map() { return data.getString("map"); }
    private String mapArg() { return map().length() == 0 ? "global" : map(); }
    private String kitMapArg() { return map().length() == 0 ? "global" : "global".equals(map()) ? "@map:global" : map(); }
    private String requestMapArg() { return map().length() == 0 ? "@global" : map(); }
    private String team() { return data.getString("team").length() == 0 ? "red" : data.getString("team"); }
    private int kitIndex() { return data.getInteger("kitIndex"); }
    private NBTTagList list(String key) { return data.getTagList(key, 10); }
    private NBTTagList spawnList() { return list("spawns_" + MODE_IDS[spawnMode]); }
    private static String modeLabel(String id) { return "BOMB".equals(id) ? "Search and Destroy" : "FFA".equals(id) ? "FFA" : "TDM"; }
    private NBTTagList kits() { return list(team() + "Kits"); }
    private String selectedOrFirstMap() {
        String selected = data.getString("selectedMap");
        return selected.length() > 0 ? selected : list("maps").tagCount() > 0 ? list("maps").getCompoundTagAt(0).getString("name") : "@global";
    }

    @Override public void initGui() {
        String previousInput = input == null ? "" : input.getText();
        boolean inputFocused = input != null && input.isFocused();
        spawnIndex = Math.max(0, Math.min(spawnIndex, Math.max(0, spawnList().tagCount() - 1)));
        panelWidth = Math.min(438, width - 8); panelHeight = Math.min(290, height - 8);
        if (panelX < 0) panelX = (width - panelWidth) / 2;
        if (panelY < 0) panelY = (height - panelHeight) / 2;
        panelX = Math.max(0, Math.min(width - panelWidth, panelX)); panelY = Math.max(0, Math.min(height - panelHeight, panelY));
        buttonList.clear();
        buttonList.add(new GuiButton(0, panelX + 5, panelY + 19, 52, 20, "Maps"));
        buttonList.add(new GuiButton(1, panelX + 60, panelY + 19, 52, 20, "Kits"));
        buttonList.add(new GuiButton(2, panelX + 115, panelY + 19, 62, 20, "Spawns"));
        buttonList.add(new GuiButton(3, panelX + 180, panelY + 19, 54, 20, "Areas"));
        buttonList.add(new GuiButton(5, panelX + 237, panelY + 19, 64, 20, "Settings"));
        buttonList.add(new GuiButton(4, panelX + panelWidth - 58, panelY + 19, 52, 20, "Close"));
        if (page == 0) {
            add(10,"Prev map",0,0); add(11,"Next map",0,1); add(12,"Create",1,0); add(13,"Delete",1,1);
            add(14,"Select map",2,0); add(15,"Cycle mode",2,1); add(16,"Hardcore",3,0); add(17,"Economy",3,1);
            add(18,"Killstreaks",4,0); add(19,"Set score",4,1); add(52,"Set timer",5,0);
            add(58,"Enforce border",5,1);
            add(68,"Vote: TDM",6,0); add(69,"Vote: S&D",6,1); add(70,"Vote: FFA",7,0); add(74,"View next mode",7,1);
            add(75,"Map voting",8,0);
        } else if (page == 1) {
            add(20,"Red / Blue",0,0); add(53,"Map / Global",0,1); add(21,"Prev kit",1,0); add(22,"Next kit",1,1);
            add(23,"Load to inv",2,0); add(24,"Create from inv",2,1); add(25,"Clone kit",3,0); add(26,"Delete kit",3,1);
            add(27,"Rename kit",4,0); add(28,"Save edit",4,1); add(29,"Cancel edit",5,0); add(54,"Set S&D cost",5,1);
            add(71,"Kit: TDM",6,0); add(72,"Kit: S&D",6,1); add(73,"Kit: FFA",7,0);
        } else if (page == 2) {
            add(30,"Prev spawn",0,0); add(31,"Next spawn",0,1); add(32,"Spawn type",1,0); add(33,"Add here",1,1);
            add(34,"Update here",2,0); add(35,"Teleport",2,1); add(36,"Remove",3,0); add(37,"Next mode",3,1);
        } else if (page == 3) {
            add(40,"Map bounds",0,0); add(41,"Bombsite A",0,1); add(42,"Bombsite B",1,0); add(43,"Safezone",1,1);
            add(44,"Warzone",2,0); add(45,"Wilderness",2,1); add(46,"Point A here",3,0); add(47,"Point B here",3,1);
            add(48,"Commit",4,0); add(49,"Cancel",4,1); add(50,"Clear bounds",5,0); add(51,"Preview on/off",5,1);
            add(55,"Refresh here",6,0); add(56,"Border exempt",6,1);
            add(57,"Use wand",7,0);
        } else {
            add(60,"Next reward",0,0); add(61,"Set amount",0,1); add(62,"Terrorist team",1,0);
            add(63,"Clear spawns",1,1); add(64,"Clear site A",2,0); add(65,"Clear site B",2,1);
            add(67,"World boundary",3,0);
        }
        input = new GuiTextField(fontRendererObj, panelX + 7, panelY + panelHeight - 24, 207, 18);
        input.setMaxStringLength(64);
        input.setText(previousInput);
        input.setFocused(inputFocused);
    }
    private void add(int id, String label, int row, int col) {
        GuiButton button = new GuiButton(id, panelX + 7 + col * 106, panelY + 48 + row * 22, 103, 20, label);
        if (map().length() == 0 && ((id >= 13 && id <= 19) || id == 52 || (id >= 30 && id <= 36)
                || (id >= 40 && id <= 42) || id == 50 || id == 58 || (id >= 60 && id <= 65)
                || (id >= 68 && id <= 70) || id == 75)) button.enabled = false;
        if (kitIndex() >= kits().tagCount() && (id == 23 || id == 25 || id == 26 || id == 27 || id == 54 || id == 21 || id == 22
                || (id >= 71 && id <= 73))) button.enabled = false;
        if (spawnList().tagCount() == 0 && (id == 30 || id == 31 || id == 34 || id == 35 || id == 36)) button.enabled = false;
        buttonList.add(button);
    }
    private void command(String text, boolean refresh) {
        PacketDispatcher.wrapper.sendToServer(new AdminEditorActionPacket(data.getString("token"), map(), team(), kitIndex(), text, refresh));
    }
    private void navigate(String targetMap, String targetTeam, int targetKit) {
        PacketDispatcher.wrapper.sendToServer(new AdminEditorNavigatePacket(data.getString("token"), targetMap, targetTeam,
                targetKit, page, spawnMode, spawnIndex, spawnType, rewardIndex, true));
    }
    private void syncContext() {
        PacketDispatcher.wrapper.sendToServer(new AdminEditorNavigatePacket(data.getString("token"), requestMapArg(), team(),
                kitIndex(), page, spawnMode, spawnIndex, spawnType, rewardIndex, false));
    }
    private void execute(String text) { confirmation = ""; command(text, true); }
    private boolean confirm(String what) {
        if (what.equals(confirmation)) { confirmation = ""; return true; }
        confirmation = what; return false;
    }

    @Override protected void actionPerformed(GuiButton button) {
        int id = button.id;
        if ((id >= 0 && id <= 3) || id == 5) { page = id == 5 ? 4 : id; confirmation = ""; initGui(); syncContext(); return; }
        if (id == 4) { mc.displayGuiScreen(null); return; }
        String map = mapArg(), team = team();
        if (id == 10 || id == 11) {
            NBTTagList maps = list("maps"); if (maps.tagCount() == 0) return;
            int current = -1; for (int i = 0; i < maps.tagCount(); i++) if (maps.getCompoundTagAt(i).getString("name").equals(map())) current = i;
            int next = current < 0 ? (id == 11 ? 0 : maps.tagCount() - 1)
                    : (current + (id == 11 ? 1 : maps.tagCount() - 1)) % maps.tagCount();
            navigate(maps.getCompoundTagAt(next).getString("name"), team, 0); return;
        }
        if (id == 12) { if (input.getText().matches("[A-Za-z0-9_-]{1,32}")) execute("/tdm map create " + input.getText()); return; }
        if (id == 13) { if (map().length() > 0 && confirm("map:" + map)) execute("/tdm map delete " + map); return; }
        if (id == 14) { if (map().length() > 0) execute("/tdm map select " + map + " " + MODE_IDS[spawnMode].toLowerCase()); return; }
        if (id == 15) { String mode = data.getString("mode"); execute("/tdm map mode " + map + " " + ("DEATHMATCH".equals(mode) ? "bomb" : "BOMB".equals(mode) ? "ffa" : "deathmatch")); return; }
        if (id == 16) { execute("/tdm map hardcorerespawns " + map + " " + !data.getBoolean("hardcore")); return; }
        if (id == 17) { execute("/tdm map economy " + map + " " + !data.getBoolean("economy")); return; }
        if (id == 18) { execute("/tdm map killstreaks " + map + " " + !data.getBoolean("killstreaks")); return; }
        if (id == 19 || id == 52) { if (input.getText().matches("default|[0-9]{1,8}")) execute("/tdm map " + (id == 19 ? "scorelimit" : "timer") + " " + map + " " + input.getText() + " " + MODE_IDS[spawnMode].toLowerCase()); return; }
        if (id == 58) { execute("/tdm map border " + map + " " + (data.getBoolean("mapBorder") ? "off" : "on")); return; }
        if (id >= 68 && id <= 70) { String modeId = MODE_IDS[id - 68]; execute("/tdm map voteable " + map + " " + modeId.toLowerCase() + " " + !data.getBoolean("enabled_" + modeId)); return; }
        if (id == 75) { execute("/tdm map voteable " + map + " " + !data.getBoolean("votingEnabled")); return; }
        if (id == 74) { spawnMode = (spawnMode + 1) % 3; spawnIndex = 0; spawnType = spawnMode == 2 ? 2 : 0; initGui(); syncContext(); return; }
        if (id == 67) { execute("/tdm boundaryview " + (data.getBoolean("boundaryViewOn") ? "off" : "on")); return; }
        if (id == 20) { navigate(requestMapArg(), "red".equals(team) ? "blue" : "red", 0); return; }
        if (id == 53) { navigate(map().length() == 0 ? selectedOrFirstMap() : "@global", team, 0); return; }
        if (id == 21 || id == 22) {
            int count = kits().tagCount(); if (count == 0) return;
            int next = (kitIndex() + (id == 22 ? 1 : count - 1)) % count;
            navigate(requestMapArg(), team, next); return;
        }
        if (id == 23) { command("/tdm kit edit " + team + " " + (kitIndex() + 1) + " " + kitMapArg(), false); mc.displayGuiScreen(null); return; }
        if (id == 24) { execute("/tdm kit add " + team + " " + kitMapArg()); return; }
        if (id == 25) { execute("/tdm kit clone " + team + " " + (kitIndex() + 1) + " " + kitMapArg()); return; }
        if (id == 26) { if (confirm("kit:" + map + team + kitIndex())) execute("/tdm kit remove " + team + " " + (kitIndex() + 1) + " " + kitMapArg()); return; }
        if (id == 27) { if (input.getText().trim().length() > 0 && input.getText().length() <= 64) execute("/tdm kit rename " + team + " " + (kitIndex() + 1) + " " + kitMapArg() + " " + input.getText().trim()); return; }
        if (id == 28) { execute("/tdm kit commit"); return; }
        if (id == 29) { execute("/tdm kit cancel"); return; }
        if (id == 54) { if (input.getText().matches("[0-9]{1,8}")) execute("/tdm kit cost " + team + " " + (kitIndex() + 1) + " " + kitMapArg() + " " + input.getText()); return; }
        if (id >= 71 && id <= 73 && kitIndex() < kits().tagCount()) {
            String modeId = MODE_IDS[id - 71];
            boolean enable = kits().getCompoundTagAt(kitIndex()).getBoolean("disabled_" + modeId);
            execute("/tdm kit mode " + team + " " + (kitIndex() + 1) + " " + kitMapArg() + " " + modeId.toLowerCase() + " " + enable);
            return;
        }
        NBTTagList spawns = spawnList();
        if (id == 37) { spawnMode = (spawnMode + 1) % 3; spawnIndex = 0; spawnType = spawnMode == 2 ? 2 : 0; initGui(); syncContext(); return; }
        if (id == 30 || id == 31) { if (spawns.tagCount() > 0) spawnIndex = (spawnIndex + (id == 31 ? 1 : spawns.tagCount() - 1)) % spawns.tagCount(); syncContext(); return; }
        if (id == 32) { spawnType = spawnMode == 2 ? 2 : (spawnType == 0 ? 1 : 0); syncContext(); return; }
        if (id == 33) { execute("/tdm map addspawn " + map + " " + (spawnMode == 2 ? "ffa" : spawnType == 0 ? "red" : "blue") + " " + MODE_IDS[spawnMode].toLowerCase()); return; }
        if (id >= 34 && id <= 36) {
            if (spawnIndex >= spawns.tagCount()) return;
            if (id == 36 && !confirm("spawn:" + map + spawnIndex)) return;
            execute("/tdm map " + (id == 34 ? "updatespawn" : id == 35 ? "tpspawn" : "removespawn") + " " + map + " " + (spawnIndex + 1) + " " + MODE_IDS[spawnMode].toLowerCase()); return;
        }
        String type = data.getCompoundTag("selection").getString("type");
        if (id >= 40 && id <= 45) {
            String chosen = id == 40 ? "map" : id == 41 ? "bomb_a" : id == 42 ? "bomb_b" : id == 43 ? "safezone" : id == 44 ? "warzone" : "wilderness";
            execute((id <= 42 ? "/tdm editor select " + chosen + " " + map : "/xc editor select " + chosen)); return;
        }
        String prefix = type.equals("MAP") || type.startsWith("BOMB_") ? "/tdm editor " : "/xc editor ";
        if (id == 46 || id == 47) { execute(prefix + "point " + (id == 46 ? "a" : "b") + " " + type); return; }
        if (id == 48) { execute(prefix + "commit " + type); return; }
        if (id == 49) { execute(prefix + "cancel"); return; }
        if (id == 50) { if (confirm("bounds:" + map)) execute("/tdm map clearbounds " + map); return; }
        if (id == 51) { visualize = !visualize; return; }
        if (id == 55) { navigate(requestMapArg(), team, kitIndex()); return; }
        if (id == 56) { execute("/xc editor select border_exempt"); return; }
        if (id == 57) { mc.displayGuiScreen(null); return; }
        if (id == 60) { rewardIndex = (rewardIndex + 1) % REWARD_KEYS.length; syncContext(); return; }
        if (id == 61) { if (input.getText().matches("[0-9]{1,8}")) execute("/tdm map " + REWARD_KEYS[rewardIndex] + " " + map + " " + input.getText()); return; }
        if (id == 62) { execute("/tdm map terroristteam " + map + " " + ("red".equals(data.getString("terroristTeam")) ? "blue" : "red")); return; }
        if (id == 63) { if (confirm("spawns-all:" + map + spawnMode)) execute("/tdm map clearspawns " + map + " " + MODE_IDS[spawnMode].toLowerCase()); return; }
        if (id == 64 || id == 65) { if (confirm("site:" + map + id)) execute("/tdm map bombsite " + map + " " + (id == 64 ? "a" : "b") + " clear"); return; }
    }

    @Override public void drawScreen(int mx, int my, float partial) {
        drawRect(panelX, panelY, panelX + panelWidth, panelY + panelHeight, 0xEE18212D);
        drawRect(panelX, panelY, panelX + panelWidth, panelY + 17, 0xFF2A4C68);
        String activeType = data.getCompoundTag("selection").getString("type");
        String mode = page == 0 ? "Maps" : page == 1 ? "Kits" : page == 2 ? "Spawns" : page == 3 ? "Areas" : "Settings";
        String title = "XF Admin " + mode + "  " + (map().length() == 0 ? "global" : map()) + "  " + (activeType.length() == 0 ? "idle" : activeType
                + " A:" + (data.getCompoundTag("selection").getBoolean("hasA") ? "yes" : "no")
                + " B:" + (data.getCompoundTag("selection").getBoolean("hasB") ? "yes" : "no"))
                + "  view:" + (visualize ? "on" : "off") + "  (drag)";
        drawString(fontRendererObj, shorten(title, 60), panelX + 7, panelY + 5, 0xFFFFFF);
        int rx = panelX + 226, y = panelY + 47;
        if (page == 0) {
            line("Map: " + (map().length() == 0 ? "none" : map()), rx, y, 0xFFFFFF);
            line("Selected: " + data.getString("selectedMap") + " \u2014 " + modeLabel(data.getString("activeMode")), rx, y + 12, 0xBFDFFF);
            line("Editing: " + MODE_LABELS[spawnMode] + " (default " + modeLabel(data.getString("mode")) + ")", rx, y + 24, 0xDDDDDD);
            line("Score limit: " + data.getInteger("score_" + MODE_IDS[spawnMode]) + " (0=default)", rx, y + 36, 0xDDDDDD);
            line("Timer: " + data.getInteger("seconds_" + MODE_IDS[spawnMode]) + "s (0=default)", rx, y + 48, 0xDDDDDD);
            line("Spawns (" + MODE_LABELS[spawnMode] + "): " + spawnList().tagCount(), rx, y + 60, 0xDDDDDD);
            line("Bounds: " + areaText("bounds"), rx, y + 72, 0x74C5FF);
            line("Enforced map border: " + (data.getBoolean("mapBorder") ? "ON" : "OFF"), rx, y + 84, 0x74C5FF);
            line("Search and Destroy A: " + areaText("bombA"), rx, y + 96, 0xFFDA67);
            line("Search and Destroy B: " + areaText("bombB"), rx, y + 108, 0xFFDA67);
            line("Legacy / unassigned: " + data.getInteger("legacySpawns") + " / " + data.getInteger("unassignedSpawns"), rx, y + 120, 0xAABBC8);
            line("Voting: TDM " + (data.getBoolean("enabled_DEATHMATCH") ? "ON" : "OFF")
                    + "  S&D " + (data.getBoolean("enabled_BOMB") ? "ON" : "OFF")
                    + "  FFA " + (data.getBoolean("enabled_FFA") ? "ON" : "OFF"), rx, y + 135, 0xAABBC8);
            line("Map voting: " + (data.getBoolean("votingEnabled") ? "ON" : "OFF"), rx, y + 147, 0xAABBC8);
        } else if (page == 1) {
            NBTTagList kits = kits();
            line((map().length() == 0 ? "Global" : map()) + " / " + team().toUpperCase(), rx, y, 0xFFFFFF);
            line("Direct kits: " + kits.tagCount(), rx, y + 12, 0xDDDDDD);
            if (kitIndex() < kits.tagCount()) {
                NBTTagCompound kit = kits.getCompoundTagAt(kitIndex());
                line("#" + (kitIndex() + 1) + " " + kit.getString("name"), rx, y + 25, 0xFFE7A0);
                line("Search and Destroy buy score: " + kit.getInteger("cost"), rx, y + 37, 0xDDDDDD);
                line("Modes: " + (kit.getBoolean("disabled_DEATHMATCH") ? "" : "TDM ")
                        + (kit.getBoolean("disabled_BOMB") ? "" : "S&D ")
                        + (kit.getBoolean("disabled_FFA") ? "" : "FFA"), rx, y + 49, 0xA7D8FF);
            }
            drawInventory(rx, y + 56);
            line(shorten(data.getString("kitEdit"), 38), rx, panelY + panelHeight - 41, 0xA7F0B4);
        } else if (page == 2) {
            NBTTagList spawns = spawnList();
            line("Map: " + map(), rx, y, 0xFFFFFF);
            line("Gamemode: " + MODE_LABELS[spawnMode] + "  Spawns: " + spawns.tagCount(), rx, y + 12, 0xFFFFFF);
            line("New type: " + (spawnMode == 2 ? "FFA" : spawnType == 0 ? "RED" : "BLUE"), rx, y + 25, 0xA7D8FF);
            if (spawnIndex < spawns.tagCount()) {
                NBTTagCompound spawn = spawns.getCompoundTagAt(spawnIndex);
                line("#" + (spawnIndex + 1) + " " + spawn.getString("team").toUpperCase(), rx, y + 34, 0xFFE7A0);
                line("Dim " + spawn.getInteger("dim") + "  " + spawn.getInteger("x") + ", " + spawn.getInteger("y") + ", " + spawn.getInteger("z"), rx, y + 47, 0xDDDDDD);
                line(spawn.getBoolean("rot") ? "Facing " + (int)spawn.getFloat("yaw") + " / " + (int)spawn.getFloat("pitch") : "Legacy facing: unchanged", rx, y + 60, 0xDDDDDD);
            }
        } else if (page == 3) {
            NBTTagCompound selection = data.getCompoundTag("selection");
            line("Type: " + (selection.hasKey("type") ? selection.getString("type")
                    + (selection.getString("map").length() == 0 ? "" : " / " + selection.getString("map")) : "none"), rx, y, 0xFFFFFF);
            line("A: " + (selection.getBoolean("hasA") ? coords(selection,"a") : "unset"), rx, y + 13, 0xDDDDDD);
            line("B: " + (selection.getBoolean("hasB") ? coords(selection,"b") : "unset"), rx, y + 26, 0xDDDDDD);
            line("Dim: " + (selection.hasKey("type") ? selection.getInteger("dim") : data.getInteger("playerDim")), rx, y + 39, 0xDDDDDD);
            line("Preview: " + (visualize ? "ON" : "OFF"), rx, y + 52, 0xA7D8FF);
            line("Left click: Point A", rx, y + 65, 0xA7F0B4);
            line("Right click: Point B", rx, y + 78, 0xA7F0B4);
            if (visualize) drawAreaPreview(rx, y + 94);
        } else {
            line("Map: " + map(), rx, y, 0xFFFFFF);
            line("Terrorists: " + data.getString("terroristTeam"), rx, y + 13, 0xDDDDDD);
            line("Hardcore: " + data.getBoolean("hardcore"), rx, y + 26, 0xDDDDDD);
            line("Economy: " + data.getBoolean("economy"), rx, y + 39, 0xDDDDDD);
            line("Killstreaks: " + data.getBoolean("killstreaks"), rx, y + 52, 0xDDDDDD);
            line("Reward: " + REWARD_KEYS[rewardIndex], rx, y + 73, 0xFFE7A0);
            line("Amount: " + data.getInteger(REWARD_KEYS[rewardIndex]), rx, y + 86, 0xDDDDDD);
            line("World: per-spawn; bounds are separate.", rx, y + 107, 0xAABBC8);
            line("In-world boundary: " + (data.getBoolean("boundaryViewOn") ? "ON" : "OFF"), rx, y + 120, 0x74C5FF);
        }
        if (confirmation.length() > 0) line("Click again to confirm: " + confirmation, panelX + 7, panelY + panelHeight - 37, 0xFF7979);
        if (confirmation.length() == 0 && (page == 0 || page == 1 || page == 4)) line("Input: name or value", panelX + 7, panelY + panelHeight - 37, 0xAABBC8);
        input.drawTextBox();
        super.drawScreen(mx, my, partial);
    }
    private String areaText(String key) {
        NBTTagCompound a = data.getCompoundTag(key);
        return a.getBoolean("a") && a.getBoolean("b") ? "dim " + a.getInteger("dim") + " [" + a.getInteger("x1") + "," + a.getInteger("z1") + "] to [" + a.getInteger("x2") + "," + a.getInteger("z2") + "]" : "unset";
    }
    private String coords(NBTTagCompound a, String prefix) { return a.getInteger(prefix + "x") + "," + a.getInteger(prefix + "y") + "," + a.getInteger(prefix + "z"); }
    private String shorten(String s, int max) { return s.length() <= max ? s : s.substring(0, max - 3) + "..."; }
    private void line(String text, int x, int y, int color) { drawString(fontRendererObj, shorten(text, 40), x, y, color); }
    private void drawInventory(int x, int y) {
        ItemStack[] slots = new ItemStack[40]; NBTTagList tags = list("preview");
        for (int i = 0; i < tags.tagCount(); i++) {
            NBTTagCompound tag = tags.getCompoundTagAt(i); int slot = tag.getByte("slot") & 255;
            if (slot < slots.length) slots[slot] = ItemStack.loadItemStackFromNBT(tag.getCompoundTag("item"));
        }
        RenderHelper.enableGUIStandardItemLighting();
        for (int i = 0; i < 36; i++) {
            int col = i < 9 ? i : (i - 9) % 9, row = i < 9 ? 3 : (i - 9) / 9;
            int sx = x + col * 18, sy = y + row * 18;
            drawRect(sx - 1, sy - 1, sx + 17, sy + 17, 0xAA344354);
            if (slots[i] != null) itemRenderer.renderItemAndEffectIntoGUI(fontRendererObj, mc.getTextureManager(), slots[i], sx, sy);
        }
        for (int i = 0; i < 4; i++) {
            int sx = x + i * 18, sy = y + 76;
            drawRect(sx - 1, sy - 1, sx + 17, sy + 17, 0xAA526273);
            if (slots[36 + i] != null) itemRenderer.renderItemAndEffectIntoGUI(fontRendererObj, mc.getTextureManager(), slots[36 + i], sx, sy);
        }
        RenderHelper.disableStandardItemLighting();
        line("Armor: boots / legs / chest / helm", x, y + 96, 0xAABBC8);
    }
    private void drawAreaPreview(int x, int y) {
        int cell = 7, side = 17 * cell;
        drawRect(x, y, x + side, y + side, 0xBB293540);
        NBTTagList zones = list("zones");
        for (int i = 0; i < zones.tagCount(); i++) {
            NBTTagCompound zone = zones.getCompoundTagAt(i);
            TerritoryCoordinateBounds.Bounds bx = TerritoryCoordinateBounds.forCoordinate(data.getInteger("zoneCX") + zone.getByte("x"));
            TerritoryCoordinateBounds.Bounds bz = TerritoryCoordinateBounds.forCoordinate(data.getInteger("zoneCZ") + zone.getByte("z"));
            previewRect(x, y, cell, bx.minInclusive, bz.minInclusive, bx.maxExclusive, bz.maxExclusive,
                    zone.getByte("type") == 1 ? 0xBB4DAE72 : 0xBBD65A55, false);
        }
        outline(data.getCompoundTag("bounds"), x, y, cell, 0xFF66BBFF);
        outline(data.getCompoundTag("bombA"), x, y, cell, 0xFFFFCC55);
        outline(data.getCompoundTag("bombB"), x, y, cell, 0xFFFF9955);
        NBTTagCompound selection = data.getCompoundTag("selection");
        if (selection.getBoolean("hasA") && selection.getBoolean("hasB")) {
            NBTTagCompound area = new NBTTagCompound(); area.setBoolean("a",true); area.setBoolean("b",true);
            area.setInteger("dim",selection.getInteger("dim")); area.setInteger("x1",selection.getInteger("ax")); area.setInteger("z1",selection.getInteger("az"));
            area.setInteger("x2",selection.getInteger("bx")); area.setInteger("z2",selection.getInteger("bz")); outline(area,x,y,cell,0xFFFFFFFF);
        }
        if (selection.getBoolean("hasA")) markPoint(selection.getInteger("ax"), selection.getInteger("az"), x, y, cell, 0xFFFFFF66);
        if (selection.getBoolean("hasB")) markPoint(selection.getInteger("bx"), selection.getInteger("bz"), x, y, cell, 0xFFFF66FF);
        drawRect(x + 8 * cell + 2, y + 8 * cell + 2, x + 8 * cell + 5, y + 8 * cell + 5, 0xFFFFFFFF);
        line("Nearby chunks: green safe, red war", x, y + side + 4, 0xAABBC8);
    }
    private void outline(NBTTagCompound a, int x, int y, int cell, int color) {
        if (!a.getBoolean("a") || !a.getBoolean("b") || a.getInteger("dim") != data.getInteger("playerDim")) return;
        BlockAreaEdges edges = BlockAreaEdges.of(a.getInteger("x1"), a.getInteger("z1"),
                a.getInteger("x2"), a.getInteger("z2"));
        previewRect(x, y, cell, edges.minX, edges.minZ, edges.maxXExclusive, edges.maxZExclusive, color, true);
    }
    private void markPoint(int px, int pz, int x, int y, int cell, int color) {
        long worldX = previewOrigin(data.getInteger("playerX")), worldZ = previewOrigin(data.getInteger("playerZ"));
        if (px < worldX || px >= worldX + 272 || pz < worldZ || pz >= worldZ + 272) return;
        int sx = previewPixel(px, worldX, x, cell), sz = previewPixel(pz, worldZ, y, cell);
        drawRect(Math.max(x, sx - 1), Math.max(y, sz - 1), Math.min(x + 17 * cell, sx + 2),
                Math.min(y + 17 * cell, sz + 2), color);
    }
    private static long previewOrigin(int playerBlock) { return ((long)(playerBlock >> 4) - 8L) * 16L; }
    private static int previewPixel(long blockEdge, long origin, int pixelOrigin, int cell) {
        long offset = Math.max(0L, Math.min(272L, blockEdge - origin));
        return pixelOrigin + (int)(offset * cell / 16L);
    }
    private void previewRect(int x, int y, int cell, long minX, long minZ, long maxX, long maxZ, int color, boolean outline) {
        long worldX = previewOrigin(data.getInteger("playerX")), worldZ = previewOrigin(data.getInteger("playerZ"));
        if (maxX <= worldX || minX >= worldX + 272L || maxZ <= worldZ || minZ >= worldZ + 272L) return;
        int x1 = previewPixel(minX, worldX, x, cell), x2 = previewPixel(maxX, worldX, x, cell);
        int z1 = previewPixel(minZ, worldZ, y, cell), z2 = previewPixel(maxZ, worldZ, y, cell);
        if (x2 <= x1) x2 = Math.min(x + 17 * cell, x1 + 1);
        if (z2 <= z1) z2 = Math.min(y + 17 * cell, z1 + 1);
        if (x1 >= x2 || z1 >= z2) return;
        if (!outline) { drawRect(x1, z1, x2, z2, color); return; }
        drawRect(x1, z1, x2, z1 + 1, color); drawRect(x1, z2 - 1, x2, z2, color);
        drawRect(x1, z1, x1 + 1, z2, color); drawRect(x2 - 1, z1, x2, z2, color);
    }
    @Override protected void mouseClicked(int x, int y, int button) {
        if (button == 0 && x >= panelX && x < panelX + panelWidth && y >= panelY && y < panelY + 17) {
            dragging = true; dragX = x - panelX; dragY = y - panelY; return;
        }
        super.mouseClicked(x,y,button); input.mouseClicked(x,y,button);
    }
    @Override protected void mouseClickMove(int x, int y, int button, long elapsed) {
        if (dragging) { panelX = Math.max(0, Math.min(width-panelWidth,x-dragX)); panelY = Math.max(0,Math.min(height-panelHeight,y-dragY)); initGui(); }
        else super.mouseClickMove(x,y,button,elapsed);
    }
    @Override protected void mouseMovedOrUp(int x, int y, int button) { dragging = false; super.mouseMovedOrUp(x,y,button); }
    @Override protected void keyTyped(char character, int code) {
        if (code == ClientProxy.adminEditor.getKeyCode()) { mc.displayGuiScreen(null); return; }
        if (input.textboxKeyTyped(character,code)) return;
        super.keyTyped(character,code);
    }
    @Override public boolean doesGuiPauseGame() { return false; }
}
