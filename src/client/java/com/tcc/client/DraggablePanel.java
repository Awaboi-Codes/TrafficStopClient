package com.tcc.client;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.lwjgl.glfw.GLFW;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class DraggablePanel {
    private int x, y, width, height;
    private String title;
    private boolean isDragging = false;
    private int dragOffsetX = 0;
    private int dragOffsetY = 0;
    private final List<PanelButton> buttons = new ArrayList<>();

    // Scroll and Layout State
    private int scrollOffset = 0;
    private final int headerHeight = 14;
    private final int innerPadding = 0;
    private final int buttonSpacing = 0;

    public DraggablePanel(String title, int x, int y, int width, int height) {
        this.title = title;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    // Kept identical signature to match ModHUD.java init() calls
    public void addButton(String label, Supplier<Boolean> activeSupplier, PanelAction action) {
        // Automatically forced to stack cleanly; relativeX/Y are ignored for auto-layout
        this.buttons.add(new PanelButton(label, 98, 20, activeSupplier, action));
    }

    public void render(GuiGraphicsExtractor context, Font font, int mouseX, int mouseY, int screenWidth, int screenHeight) {
        // 1. Handle Dragging Logic
        long nativeWindowHandle = GLFW.glfwGetCurrentContext();
        boolean isLeftClickHeld = GLFW.glfwGetMouseButton(nativeWindowHandle, 0) == GLFW.GLFW_PRESS;

        if (isLeftClickHeld) {
            if (!isDragging) {
                if (mouseX >= x && mouseX <= (x + width) && mouseY >= y && mouseY <= (y + headerHeight)) {
                    isDragging = true;
                    dragOffsetX = mouseX - x;
                    dragOffsetY = mouseY - y;
                }
            } else {
                x = mouseX - dragOffsetX;
                y = mouseY - dragOffsetY;

                if (x < 0) x = 0;
                if (y < 0) y = 0;
                if (x + width > screenWidth) x = screenWidth - width;
                if (y + height > screenHeight) y = screenHeight - height;
            }
        } else {
            isDragging = false;
        }

        // 2. Render Panel Body and Header Backgrounds
        context.fill(x, y, x + width, y + height, 0xFF2D2D2D);      // Gray body
        context.fill(x, y, x + width, y + headerHeight, 0xFFFF5900); // Header bar

        // Borders
        context.fill(x, y, x + width, y + 1, 0xFFFFFFFF);
        context.fill(x, y + height - 1, x + width, y + height, 0xFFFFFFFF);
        context.fill(x, y, x + 1, y + height, 0xFFFFFFFF);
        context.fill(x + width - 1, y, x + width, y + height, 0xFFFFFFFF);

        // Header Text
        context.text(font, title, x + 6, y + 3, 0xFFFFFFFF, false);

        // 3. Setup Scissor Clipping for Scrolling
        int contentStartY = y + headerHeight + innerPadding;
        int viewBottomY = y + height - innerPadding;
        int viewHeight = viewBottomY - contentStartY;

        int currentRelativeY = -scrollOffset;
        int totalContentHeight = 0;

        // Clip all drawings to the panel window frame bounds
        context.enableScissor(x + 1, contentStartY, x + width - 1, viewBottomY);

        // 4. Stacking Layout Loop
        for (PanelButton btn : buttons) {
            int currentAbsoluteY = contentStartY + currentRelativeY;

            // Auto centers button horizontally inside panel bounds
            int btnX = x + (width - btn.w) / 2;

            btn.render(context, font, btnX, currentAbsoluteY, mouseX, mouseY);

            int verticalStep = btn.h + buttonSpacing;
            currentRelativeY += verticalStep;
            totalContentHeight += verticalStep;
        }

        context.disableScissor();

        // 5. Restrict scroll boundary extensions
        int maxScrollableDistance = Math.max(0, totalContentHeight - viewHeight - buttonSpacing);
        if (scrollOffset > maxScrollableDistance) {
            scrollOffset = maxScrollableDistance;
        }
    }

    // Process scroll delta calculations
    public boolean handleMouseScroll(double mouseX, double mouseY, double scrollAmount) {
        if (mouseX >= x && mouseX <= (x + width) && mouseY >= y && mouseY <= (y + height)) {
            scrollOffset -= (int)(scrollAmount * 12); // Scroll speed tuning multiplier
            if (scrollOffset < 0) {
                scrollOffset = 0;
            }
            return true;
        }
        return false;
    }

    public boolean handleMouseClick(double mouseX, double mouseY, int button) {
        if (button == 0) {
            int contentStartY = y + headerHeight + innerPadding;
            int viewBottomY = y + height - innerPadding;

            if (mouseY < contentStartY || mouseY > viewBottomY || mouseX < x || mouseX > x + width) {
                return false;
            }

            int currentRelativeY = -scrollOffset;
            for (PanelButton btn : buttons) {
                int btnAbsY = contentStartY + currentRelativeY;
                int btnX = x + (width - btn.w) / 2;

                if (btn.isHovered((int)mouseX, (int)mouseY, btnX, btnAbsY)) {
                    btn.action.onClick();
                    return true;
                }
                currentRelativeY += btn.h + buttonSpacing;
            }
        }
        return false;
    }

    private static class PanelButton {
        String label;
        int w, h;
        Supplier<Boolean> activeSupplier;
        PanelAction action;

        PanelButton(String label, int w, int h, Supplier<Boolean> activeSupplier, PanelAction action) {
            this.label = label;
            this.w = w;
            this.h = h;
            this.activeSupplier = activeSupplier;
            this.action = action;
        }

        boolean isHovered(int mx, int my, int absX, int absY) {
            return mx >= absX && mx <= (absX + w) && my >= absY && my <= (absY + h);
        }

        void render(GuiGraphicsExtractor context, Font font, int absX, int absY, int mx, int my) {
            boolean hovered = isHovered(mx, my, absX, absY);
            boolean isActive = activeSupplier.get();

            int color = hovered ? 0xFF3D3D3D : 0xFF222222;
            context.fill(absX, absY, absX + w, absY + h, color);

            if (isActive) {
                int barWidth = 1;
                int barColor = 0xFFFF5900;
                context.fill(absX + w - barWidth, absY, absX + w, absY + h, barColor);
            }

            int textX = absX + 6;
            int textY = absY + (h - 8) / 2;
            int textColor = isActive ? 0xFFFFFFFF : 0xFFAAAAAA;
            context.text(font, label, textX, textY, textColor, false);
        }
    }
}
