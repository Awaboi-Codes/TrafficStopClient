package com.tsc.client.util.render;

import com.tsc.client.TrafficStopClient;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;

import java.util.ArrayList;
import java.util.List;

// :::1
public class HUDRenderUtils implements ClientModInitializer {
    private static Minecraft mc = Minecraft.getInstance();
    @Override
    public void onInitializeClient() {
        // Attach our rendering code to before the chat hud layer. Our layer will render right before the chat. The API will take care of z spacing.
        HudElementRegistry.attachElementBefore(VanillaHudElements.CHAT, Identifier.fromNamespaceAndPath("tsc-client", "before_chat"), HUDRenderUtils::render);
    }

    private static void render(GuiGraphics graphics, DeltaTracker tickCounter) {
        int color = 0xFFFF0000; // Red
        int targetColor = 0xFF00FF00; // Green

        // You can use the Util.getMillis() function to get the current time in milliseconds.
        // Divide by 1000 to get seconds.
        double currentTime = Util.getMillis() / 1000.0;

        // "lerp" simply means "linear interpolation", which is a fancy way of saying "blend".
        float lerpedAmount = Mth.abs(Mth.sin((float) currentTime));
        int lerpedColor = ARGB.linearLerp(lerpedAmount, color, targetColor);

        // Draw a square with the lerped color.
        // x1, x2, y1, y2, color
        //graphics.fill(0, 0, 10, 10, lerpedColor);
        graphics.drawString(mc.font, "Traffic Stop Client", 3, 3, lerpedColor, false);

        List<String> activecheats = getActiveCheats();
        int y = 20;
        for (String cheat : activecheats) {
            graphics.drawString(mc.font, cheat, 3, y, lerpedColor, false);
            y += 11;
        }
    }

    private static List<String> getActiveCheats() {
        List<String> active = new ArrayList<>();
        if (TrafficStopClient.isPlayerESP) active.add("Player ESP");
        if (TrafficStopClient.isChestESP) active.add("Chest ESP");
        if (TrafficStopClient.isXray) active.add("Xray");
        if (TrafficStopClient.isCriticals) active.add("Criticals");
        if (TrafficStopClient.isKillAura) active.add("Kill Aura");
        if (TrafficStopClient.isProtectSelf) active.add("Protect Self");
        if (TrafficStopClient.isEnderman) active.add("Enderman");
        if (TrafficStopClient.isFastUse) active.add("Fast Use");
        if (TrafficStopClient.isFlying) active.add("Fly");
        if (TrafficStopClient.isElytraFly) active.add("Elytra Fly");
        if (TrafficStopClient.isNoFall) active.add("No Fall");
        if (TrafficStopClient.isElytraBoost) active.add("Elytra Boost");
        if (TrafficStopClient.isBoatFly) active.add("Boat Fly");
        if (TrafficStopClient.isStrafe) active.add("Strafe");
        if (TrafficStopClient.isBridge) active.add("Bridge");
        if (TrafficStopClient.isTrenchBot) active.add("Trench Bot");
        return active;
    }
}
// :::1