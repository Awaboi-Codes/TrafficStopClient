package com.tcc.client;

import net.minecraft.client.gui.GuiGraphicsExtractor;
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
        DraggablePanel movementPanel = new DraggablePanel("Movement", 10, 30, 100, 500);

        movementPanel.addButton(
                "Velocity Fly",
                () -> TrafficStopClient.isFlying,
                () -> { TrafficStopClient.isFlying = !TrafficStopClient.isFlying; }
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
                "Boat Fly",
                () -> TrafficStopClient.isBoatFly,
                () -> { TrafficStopClient.isBoatFly = !TrafficStopClient.isBoatFly; }
        );

        DraggablePanel renderPanel = new DraggablePanel("Render", 120, 30, 100, 500);

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

        DraggablePanel combatPanel = new DraggablePanel("Combat", 230, 30, 100, 500);

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

        DraggablePanel automationPanel = new DraggablePanel("Automation", 340, 30, 100, 500);
        DraggablePanel miscPanel = new DraggablePanel("Miscellaneous", 450, 30, 100, 500);

        miscPanel.addButton(
                "Fast Use",
                () -> TrafficStopClient.isFastUse,
                () -> { TrafficStopClient.isFastUse = !TrafficStopClient.isFastUse; }
        );

        panels.add(movementPanel);
        panels.add(renderPanel);
        panels.add(combatPanel);
        panels.add(automationPanel);
        panels.add(miscPanel);
    }

    public void render(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        // --- TOP LEFT CORNER BRANDING DISPLAY ---
        int padding = 8;
        int logoSize = 16; // Width and Height dimensions for a square logo

        // Draw the Logo Texture
        context.blit(RenderPipelines.GUI_TEXTURED, LOGO_TEXTURE, padding, logoSize, logoSize, 0, 0, logoSize, logoSize, logoSize, logoSize);

        // Draw the Custom Branding Text right next to the logo
        int textX = padding + logoSize + 6;
        int textY = padding + (logoSize - 8) / 2;
        // 26.1 REFACTOR: Replace context.drawString with context.text
        context.text(this.font, "TrafficStop v1.0.0", textX, textY, 0xFFFF2900, true);

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
