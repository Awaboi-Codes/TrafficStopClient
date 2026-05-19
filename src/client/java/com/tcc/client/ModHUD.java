package com.tcc.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import java.util.ArrayList;
import java.util.List;

public class ModHUD extends Screen {

    private final List<DraggablePanel> panels = new ArrayList<>();

    public ModHUD(Component title) {
        super(title);
    }

    @Override
    protected void init() {
        panels.clear();

        // Create the Panel
        DraggablePanel modPanel = new DraggablePanel("Movement", 30, 30, 140, 500);

        modPanel.addButton("Velocity Fly", 0, 30, 140, 20, () -> {
            TrafficStopClient.isFlying = !TrafficStopClient.isFlying;
        });

        panels.add(modPanel);
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        for (DraggablePanel panel : panels) {
            panel.render(context, this.font, mouseX, mouseY, this.width, this.height);
        }
    }

    @Override
    public boolean mouseClicked(net.minecraft.client.input.MouseButtonEvent mouseButtonEvent, boolean isDoubleClick) {
        // 1. Extract the button index (0 for left-click, 1 for right-click)
        int button = mouseButtonEvent.button();

        // 2. Fetch the current mouse coordinates scaled to the game's window size
        net.minecraft.client.Minecraft client = net.minecraft.client.Minecraft.getInstance();
        double mx = client.mouseHandler.xpos() * (double)client.getWindow().getGuiScaledWidth() / (double)client.getWindow().getWidth();
        double my = client.mouseHandler.ypos() * (double)client.getWindow().getGuiScaledHeight() / (double)client.getWindow().getHeight();

        // 3. Direct click data down into your custom panels click engine
        for (DraggablePanel panel : panels) {
            if (panel.handleMouseClick(mx, my, button)) {
                return true;
            }
        }

        // 4. Safely forward the event back up to the superclass
        return super.mouseClicked(mouseButtonEvent, isDoubleClick);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
