package com.tcc.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier; // Correct Yarn mapping asset import path
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;

public class ModHUD extends Screen {

    private final List<DraggablePanel> panels = new ArrayList<>();

    // Correctly structured using standard Yarn method formats
    private static final Identifier LOGO_TEXTURE = Identifier.fromNamespaceAndPath("trafficstopclient", "textures/gui/logo.png");

    public ModHUD(Component title) {
        super(title);
    }

    @Override
    protected void init() {
        Minecraft mc = Minecraft.getInstance();

        panels.clear();

        // Create the Panel
        DraggablePanel movementPanel = new DraggablePanel("Movement", 10, 10, 100, 520);

        movementPanel.addButton(
                "Velocity Fly",
                () -> TrafficStopClient.isFlying,
                () -> { TrafficStopClient.isFlying = !TrafficStopClient.isFlying; }
        );

        movementPanel.addButton(
                "Boat Fly",
                () -> TrafficStopClient.isBoatFly,
                () -> { TrafficStopClient.isBoatFly = !TrafficStopClient.isBoatFly; }
        );

        movementPanel.addButton(
                "Elytra Fly",
                () -> TrafficStopClient.isElytraFly,
                () -> { TrafficStopClient.isElytraFly = !TrafficStopClient.isElytraFly; }
        );

        movementPanel.addButton(
                "Ground Spoof",
                () -> TrafficStopClient.isGroundSpoof,
                () -> { TrafficStopClient.isGroundSpoof = !TrafficStopClient.isGroundSpoof; }
        );

        movementPanel.addButton(
                "Elytra Boost",
                () -> TrafficStopClient.isElytraBoost,
                () -> { TrafficStopClient.isElytraBoost = !TrafficStopClient.isElytraBoost; }
        );

        movementPanel.addButton(
                "Strafe",
                () -> TrafficStopClient.isStrafe,
                () -> { TrafficStopClient.isStrafe = !TrafficStopClient.isStrafe; }
        );

        DraggablePanel renderPanel = new DraggablePanel("Render", 120, 10, 100, 520);

        renderPanel.addButton(
                "Xray",
                () -> TrafficStopClient.isXray,
                () -> { TrafficStopClient.isXray = !TrafficStopClient.isXray; }
        );

        renderPanel.addButton(
                "Player ESP",
                () -> TrafficStopClient.isPlayerESP,
                () -> {
                    TrafficStopClient.isPlayerESP = !TrafficStopClient.isPlayerESP;
                    mc.player.playSound(TrafficStopClient.MOD_TOGGLE, 1.0f, 1.0f);
                }
        );

        renderPanel.addButton(
                "Chest ESP",
                () -> TrafficStopClient.isChestESP,
                () -> { TrafficStopClient.isChestESP = !TrafficStopClient.isChestESP; }
        );

        renderPanel.addButton(
                "Base ESP",
                () -> TrafficStopClient.isBaseESP,
                () -> { TrafficStopClient.isBaseESP = !TrafficStopClient.isBaseESP; }
        );

        DraggablePanel combatPanel = new DraggablePanel("Combat", 230, 10, 100, 520);

        combatPanel.addButton(
                "Critical Packets",
                () -> TrafficStopClient.isCriticals,
                () -> { TrafficStopClient.isCriticals = !TrafficStopClient.isCriticals; }
        );

        combatPanel.addButton(
                "Protection",
                () -> TrafficStopClient.isProtectSelf,
                () -> { TrafficStopClient.isProtectSelf = !TrafficStopClient.isProtectSelf; }
        );

        combatPanel.addButton(
                "Kill Aura",
                () -> TrafficStopClient.isKillAura,
                () -> { TrafficStopClient.isKillAura = !TrafficStopClient.isKillAura; }
        );

        combatPanel.addButton(
                "Enderman",
                () -> TrafficStopClient.isEnderman,
                () -> { TrafficStopClient.isEnderman = !TrafficStopClient.isEnderman; }
        );

        DraggablePanel automationPanel = new DraggablePanel("Automation", 340, 10, 100, 520);
        DraggablePanel miscPanel = new DraggablePanel("Miscellaneous", 450, 10, 100, 520);

        miscPanel.addButton(
                "Fast Use",
                () -> TrafficStopClient.isFastUse,
                () -> { TrafficStopClient.isFastUse = !TrafficStopClient.isFastUse; }
        );

        DraggablePanel dupePanel = new DraggablePanel("Dupes", 560, 10, 100, 520);

        panels.add(movementPanel);
        panels.add(renderPanel);
        panels.add(combatPanel);
        panels.add(automationPanel);
        panels.add(miscPanel);
        panels.add(dupePanel);
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        // Render your normal draggable module panels
        for (DraggablePanel panel : panels) {
            panel.render(context, this.font, mouseX, mouseY, this.width, this.height);
        }
    }

    @Override
    public boolean mouseClicked(net.minecraft.client.input.MouseButtonEvent mouseButtonEvent, boolean isDoubleClick) {
        int button = mouseButtonEvent.button();

        net.minecraft.client.Minecraft client = net.minecraft.client.Minecraft.getInstance();
        double mx = client.mouseHandler.xpos() * (double)client.getWindow().getGuiScaledWidth() / (double)client.getWindow().getWidth();
        double my = client.mouseHandler.ypos() * (double)client.getWindow().getGuiScaledHeight() / (double)client.getWindow().getHeight();

        for (DraggablePanel panel : panels) {
            if (panel.handleMouseClick(mx, my, button)) {
                return true;
            }
        }

        return super.mouseClicked(mouseButtonEvent, isDoubleClick);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
