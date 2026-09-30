package com.hfr.client.journeymap;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentText;

/** Local presentation preference. It never changes server-side containment or zoning. */
public final class ClientBorderVisuals {
    private static boolean enabled = true;

    private ClientBorderVisuals() { }

    public static boolean enabled() { return enabled; }

    public static void toggle() {
        enabled = !enabled;
        if (Minecraft.getMinecraft().thePlayer != null)
            Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("Border visuals " + (enabled ? "shown" : "hidden") + "."));
    }
}
