package com.tcc.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier; // Correct Yarn mapping asset import path
import java.util.ArrayList;
import java.util.List;

public class ModHUD extends Screen {

    private final List<DraggablePanel> panels = new ArrayList<>();

    // Correctly structured using standard Yarn method formats
    private static final Identifier LOGO_TEXTURE = Identifier.fromNamespaceAndPath("trafficstopclient", "textures/gui/logo.png");

    public ModHUD(Component title) {
        super(title);
    }

    @Override
    protected void init() {
        panels.clear();

        // Create the Panel
        DraggablePanel movementPanel = new DraggablePanel("Movement", 10, 30, 140, 500);

        movementPanel.addButton(
                "Velocity Fly",
                () -> TrafficStopClient.isFlying,
                () -> { TrafficStopClient.isFlying = !TrafficStopClient.isFlying; }
        );

        movementPanel.addButton(
                "No Fall",
                () -> TrafficStopClient.isNoFallActive,
                () -> { TrafficStopClient.isNoFallActive = !TrafficStopClient.isNoFallActive; }
        );

        movementPanel.addButton(
                "Jump Hack",
                () -> TrafficStopClient.isJumpHackActive,
                () -> { TrafficStopClient.isJumpHackActive = !TrafficStopClient.isJumpHackActive; }
        );

        movementPanel.addButton(
                "Elytra Boost",
                () -> TrafficStopClient.isElytraBoost,
                () -> { TrafficStopClient.isElytraBoost = !TrafficStopClient.isElytraBoost; }
        );

        movementPanel.addButton(
                "Boat Fly",
                () -> TrafficStopClient.isBoatFly,
                () -> { TrafficStopClient.isBoatFly = !TrafficStopClient.isBoatFly; }
        );

        DraggablePanel renderPanel = new DraggablePanel("Render", 190, 30, 140, 500);

        renderPanel.addButton(
                "Xray",
                () -> TrafficStopClient.isXray,
                () -> { TrafficStopClient.isXray = !TrafficStopClient.isXray; }
        );

        renderPanel.addButton(
                "ESP",
                () -> TrafficStopClient.isESP,
                () -> { TrafficStopClient.isESP = !TrafficStopClient.isESP; }
        );

        DraggablePanel combatPanel = new DraggablePanel("Combat", 360, 30, 140, 500);

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
                "Protection",
                () -> TrafficStopClient.isProtectSelf,
                () -> { TrafficStopClient.isProtectSelf = !TrafficStopClient.isProtectSelf; }
        );

        DraggablePanel automationPanel = new DraggablePanel("Automation", 530, 30, 140, 500);

        panels.add(movementPanel);
        panels.add(renderPanel);
        panels.add(combatPanel);
        panels.add(automationPanel);
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        // --- TOP LEFT CORNER BRANDING DISPLAY ---
        int padding = 8;
        int logoSize = 16; // Width and Height dimensions for a square logo

        // Draw the Logo Texture
        context.blit(RenderPipelines.GUI_TEXTURED, LOGO_TEXTURE, padding, logoSize, logoSize, 0, 0, logoSize, logoSize, logoSize, logoSize);

        // Draw the Custom Branding Text right next to the logo
        int textX = padding + logoSize + 6;
        int textY = padding + (logoSize - 8) / 2;
        context.drawString(this.font, "TrafficStop v1.0.0", textX, textY, 0xFFFF2900, true);

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
