package com.tcc.client;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import org.lwjgl.glfw.GLFW;
import java.util.ArrayList;
import java.util.List;

public class DraggablePanel {
    private int x, y, width, height;
    private String title;
    private boolean isDragging = false;
    private int dragOffsetX = 0;
    private int dragOffsetY = 0;
    private final List<PanelButton> buttons = new ArrayList<>();

    public DraggablePanel(String title, int x, int y, int width, int height) {
        this.title = title;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    // Method to easily attach buttons to this specific box instance
    public void addButton(String label, int relativeX, int relativeY, int btnWidth, int btnHeight, PanelAction action) {
        this.buttons.add(new PanelButton(label, relativeX, relativeY, btnWidth, btnHeight, action));
    }

    public void render(GuiGraphics context, Font font, int mouseX, int mouseY, int screenWidth, int screenHeight) {
        // 1. Handle Window Handle Pointer dynamically
        long nativeWindowHandle = GLFW.glfwGetCurrentContext();
        boolean isLeftClickHeld = GLFW.glfwGetMouseButton(nativeWindowHandle, 0) == GLFW.GLFW_PRESS;

        // 2. Handle Dragging Engine Math
        if (isLeftClickHeld) {
            if (!isDragging) {
                if (mouseX >= x && mouseX <= (x + width) && mouseY >= y && mouseY <= (y + 14)) {
                    isDragging = true;
                    dragOffsetX = mouseX - x;
                    dragOffsetY = mouseY - y;
                }
            } else {
                x = mouseX - dragOffsetX;
                y = mouseY - dragOffsetY;

                // Keep bounded inside monitor bounds
                if (x < 0) x = 0;
                if (y < 0) y = 0;
                if (x + width > screenWidth) x = screenWidth - width;
                if (y + height > screenHeight) y = screenHeight - height;
            }
        } else {
            isDragging = false;
        }

        // 3. Render Base Box Backgrounds
        context.fill(x, y, x + width, y + height, 0xFF2D2D2D);      // Gray body
        context.fill(x, y, x + width, y + 14, 0xFF4A4A4A);         // Header title bar

        // Borders
        context.fill(x, y, x + width, y + 1, 0xFFFFFFFF);
        context.fill(x, y + height - 1, x + width, y + height, 0xFFFFFFFF);
        context.fill(x, y, x + 1, y + height, 0xFFFFFFFF);
        context.fill(x + width - 1, y, x + width, y + height, 0xFFFFFFFF);

        // Header Title Text
        context.drawString(font, title, x + 6, y + 3, 0xFFFFFFFF, false);

        // 4. Render and Update Attached Buttons
        for (PanelButton btn : buttons) {
            btn.render(context, font, x, y, mouseX, mouseY);
        }
    }

    // Must be called from the Screen's mouseClicked method to handle button presses
    public boolean handleMouseClick(double mouseX, double mouseY, int button) {
        if (button == 0) { // Left Click
            for (PanelButton btn : buttons) {
                if (btn.isHovered((int)mouseX, (int)mouseY, x, y)) {
                    btn.action.onClick();
                    return true;
                }
            }
        }
        return false;
    }

    // Helper Inner Class managing the individual buttons inside this panel
    private static class PanelButton {
        String label;
        int relX, relY, w, h;
        PanelAction action;

        PanelButton(String label, int relX, int relY, int w, int h, PanelAction action) {
            this.label = label;
            this.relX = relX;
            this.relY = relY;
            this.w = w;
            this.h = h;
            this.action = action;
        }

        boolean isHovered(int mx, int my, int panelX, int panelY) {
            int absoluteX = panelX + relX;
            int absoluteY = panelY + relY;
            return mx >= absoluteX && mx <= (absoluteX + w) && my >= absoluteY && my <= (absoluteY + h);
        }

        void render(GuiGraphics context, Font font, int panelX, int panelY, int mx, int my) {
            int absX = panelX + relX;
            int absY = panelY + relY;
            boolean hovered = isHovered(mx, my, panelX, panelY);

            // Change color profile depending on mouse hover position state
            int color = hovered ? 0xFF666666 : 0xFF444444;
            context.fill(absX, absY, absX + w, absY + h, color);
            context.renderOutline(absX, absY, w, h, 0xFFFFFFFF);

            // Draw button text label centered inside its frame layout
            int textWidth = font.width(label);
            int textX = absX + (w - textWidth) / 2;
            int textY = absY + (h - 8) / 2;
            context.drawString(font, label, textX, textY, 0xFFFFFFFF, false);
        }
    }
}
