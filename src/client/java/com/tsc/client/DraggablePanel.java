package com.tsc.client;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
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

    private int scrollOffset = 0;
    private final int headerHeight = 14;
    private final int innerPadding = 0;
    private final int buttonSpacing = 0;
    private static final int PLUS_BTN_W = 14;
    private static final int SETTINGS_ROW_H = 22;

    public DraggablePanel(String title, int x, int y, int width, int height) {
        this.title = title;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public void addButton(String label, Supplier<Boolean> activeSupplier, PanelAction action) {
        this.buttons.add(new PanelButton(label, 98, 20, activeSupplier, action));
    }

    public void addButton(String label, Supplier<Boolean> activeSupplier, PanelAction action, List<SettingsEntry> settings) {
        this.buttons.add(new PanelButton(label, 98, 20, activeSupplier, action, settings));
    }

    public void render(GuiGraphics context, Font font, int mouseX, int mouseY, int screenWidth, int screenHeight) {
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

        context.fill(x, y, x + width, y + height, 0xFF2D2D2D);
        context.fill(x, y, x + width, y + headerHeight, 0xFFFF5900);
        context.fill(x, y, x + width, y + 1, 0xFFFFFFFF);
        context.fill(x, y + height - 1, x + width, y + height, 0xFFFFFFFF);
        context.fill(x, y, x + 1, y + height, 0xFFFFFFFF);
        context.fill(x + width - 1, y, x + width, y + height, 0xFFFFFFFF);
        context.drawString(font, title, x + 6, y + 3, 0xFFFFFFFF, false);

        int contentStartY = y + headerHeight + innerPadding;
        int viewBottomY = y + height - innerPadding;
        int viewHeight = viewBottomY - contentStartY;

        int currentRelativeY = -scrollOffset;
        int totalContentHeight = 0;

        context.enableScissor(x + 1, contentStartY, x + width - 1, viewBottomY);

        for (PanelButton btn : buttons) {
            int currentAbsoluteY = contentStartY + currentRelativeY;

            // + / - toggle button
            int plusX = x + 2;
            boolean plusHovered = mouseX >= plusX && mouseX <= plusX + PLUS_BTN_W
                    && mouseY >= currentAbsoluteY && mouseY <= currentAbsoluteY + btn.h;
            context.fill(plusX, currentAbsoluteY, plusX + PLUS_BTN_W, currentAbsoluteY + btn.h,
                    plusHovered ? 0xFF505050 : 0xFF333333);
            String expandChar = btn.settingsOpen ? "-" : "+";
            context.drawString(font, expandChar, plusX + (btn.settingsOpen ? 4 : 3),
                    currentAbsoluteY + (btn.h - 8) / 2, 0xFFFF5900, false);

            // Main button
            int btnX = x + PLUS_BTN_W + 4;
            int btnW = width - PLUS_BTN_W - 6;
            btn.render(context, font, btnX, currentAbsoluteY, mouseX, mouseY, btnW);

            int verticalStep = btn.h + buttonSpacing;
            currentRelativeY += verticalStep;
            totalContentHeight += verticalStep;

            // Inline settings rows if expanded
            if (btn.settingsOpen && btn.settings != null) {
                for (SettingsEntry entry : btn.settings) {
                    int rowY = contentStartY + currentRelativeY;

                    // Settings row background
                    context.fill(x + 1, rowY, x + width - 1, rowY + SETTINGS_ROW_H, 0xFF1A1A1A);
                    // Left accent bar
                    context.fill(x + 1, rowY, x + 3, rowY + SETTINGS_ROW_H, 0xFFFF5900);

                    entry.render(context, font, x + 6, rowY, width - 8, SETTINGS_ROW_H, mouseX, mouseY);

                    currentRelativeY += SETTINGS_ROW_H + buttonSpacing;
                    totalContentHeight += SETTINGS_ROW_H + buttonSpacing;
                }
            }
        }

        context.disableScissor();

        int maxScrollableDistance = Math.max(0, totalContentHeight - viewHeight - buttonSpacing);
        if (scrollOffset > maxScrollableDistance) scrollOffset = maxScrollableDistance;
    }

    public boolean handleMouseScroll(double mouseX, double mouseY, double scrollAmount) {
        if (mouseX >= x && mouseX <= (x + width) && mouseY >= y && mouseY <= (y + height)) {
            scrollOffset -= (int)(scrollAmount * 12);
            if (scrollOffset < 0) scrollOffset = 0;
            return true;
        }
        return false;
    }

    public boolean handleMouseClick(double mouseX, double mouseY, int button) {
        if (button != 0) return false;

        int contentStartY = y + headerHeight + innerPadding;
        int viewBottomY = y + height - innerPadding;

        if (mouseY < contentStartY || mouseY > viewBottomY || mouseX < x || mouseX > x + width) {
            return false;
        }

        int currentRelativeY = -scrollOffset;
        for (PanelButton btn : buttons) {
            int btnAbsY = contentStartY + currentRelativeY;

            // + button click — toggle settings open
            int plusX = x + 2;
            if (mouseX >= plusX && mouseX <= plusX + PLUS_BTN_W
                    && mouseY >= btnAbsY && mouseY <= btnAbsY + btn.h) {
                btn.settingsOpen = !btn.settingsOpen;
                return true;
            }

            // Main button click
            int btnX = x + PLUS_BTN_W + 4;
            int btnW = width - PLUS_BTN_W - 6;
            if (mouseX >= btnX && mouseX <= btnX + btnW
                    && mouseY >= btnAbsY && mouseY <= btnAbsY + btn.h) {
                btn.action.onClick();
                return true;
            }

            currentRelativeY += btn.h + buttonSpacing;

            // Settings row clicks
            if (btn.settingsOpen && btn.settings != null) {
                for (SettingsEntry entry : btn.settings) {
                    int rowY = contentStartY + currentRelativeY;
                    if (mouseY >= rowY && mouseY <= rowY + SETTINGS_ROW_H
                            && mouseX >= x + 1 && mouseX <= x + width - 1) {
                        entry.onClick((int) mouseX, (int) mouseY, x + 6, rowY, width - 8, SETTINGS_ROW_H);
                        return true;
                    }
                    currentRelativeY += SETTINGS_ROW_H + buttonSpacing;
                }
            }
        }
        return false;
    }

    // --- SettingsEntry interface ---
    public interface SettingsEntry {
        void render(GuiGraphics context, Font font, int rowX, int rowY, int rowW, int rowH, int mouseX, int mouseY);
        void onClick(int mouseX, int mouseY, int rowX, int rowY, int rowW, int rowH);
    }

    // --- Built-in: Toggle setting ---
    public static class ToggleSetting implements SettingsEntry {
        private final String label;
        private final Supplier<Boolean> getter;
        private final Runnable setter;

        public ToggleSetting(String label, Supplier<Boolean> getter, Runnable setter) {
            this.label = label;
            this.getter = getter;
            this.setter = setter;
        }

        @Override
        public void render(GuiGraphics context, Font font, int rowX, int rowY, int rowW, int rowH, int mouseX, int mouseY) {
            boolean active = getter.get();
            context.drawString(font, label, rowX, rowY + (rowH - 8) / 2, 0xFF888888, false);
            // Small toggle indicator on the right
            int boxSize = 8;
            int boxX = rowX + rowW - boxSize - 2;
            int boxY = rowY + (rowH - boxSize) / 2;
            context.fill(boxX, boxY, boxX + boxSize, boxY + boxSize, active ? 0xFFFF5900 : 0xFF444444);
        }

        @Override
        public void onClick(int mouseX, int mouseY, int rowX, int rowY, int rowW, int rowH) {
            setter.run();
        }
    }

    // --- Built-in: Slider setting ---
    public static class SliderSetting implements SettingsEntry {
        private final String label;
        private final Supplier<Double> getter;
        private final java.util.function.Consumer<Double> setter;
        private final double min, max;

        public SliderSetting(String label, double min, double max, Supplier<Double> getter, java.util.function.Consumer<Double> setter) {
            this.label = label;
            this.min = min;
            this.max = max;
            this.getter = getter;
            this.setter = setter;
        }

        @Override
        public void render(GuiGraphics context, Font font, int rowX, int rowY, int rowW, int rowH, int mouseX, int mouseY) {
            double val = getter.get();
            context.drawString(font, label + ": " + String.format("%.1f", val),
                    rowX, rowY + 2, 0xFF888888, false);

            int trackX = rowX;
            int trackY = rowY + rowH - 7;
            int trackW = rowW - 4;
            int trackH = 3;

            // Track background
            context.fill(trackX, trackY, trackX + trackW, trackY + trackH, 0xFF444444);
            // Fill
            int fillW = (int)((val - min) / (max - min) * trackW);
            context.fill(trackX, trackY, trackX + fillW, trackY + trackH, 0xFFFF5900);
            // Handle
            context.fill(trackX + fillW - 1, trackY - 2, trackX + fillW + 1, trackY + trackH + 2, 0xFFFFFFFF);
        }

        @Override
        public void onClick(int mouseX, int mouseY, int rowX, int rowY, int rowW, int rowH) {
            int trackX = rowX;
            int trackW = rowW - 4;
            double t = Math.max(0, Math.min(1, (double)(mouseX - trackX) / trackW));
            setter.accept(min + t * (max - min));
        }
    }

    private static class PanelButton {
        String label;
        int w, h;
        Supplier<Boolean> activeSupplier;
        PanelAction action;
        List<SettingsEntry> settings;
        boolean settingsOpen = false;

        PanelButton(String label, int w, int h, Supplier<Boolean> activeSupplier, PanelAction action) {
            this.label = label;
            this.w = w;
            this.h = h;
            this.activeSupplier = activeSupplier;
            this.action = action;
            this.settings = null;
        }

        PanelButton(String label, int w, int h, Supplier<Boolean> activeSupplier, PanelAction action, List<SettingsEntry> settings) {
            this.label = label;
            this.w = w;
            this.h = h;
            this.activeSupplier = activeSupplier;
            this.action = action;
            this.settings = settings;
        }

        boolean isHovered(int mx, int my, int absX, int absY, int overrideW) {
            return mx >= absX && mx <= (absX + overrideW) && my >= absY && my <= (absY + h);
        }

        void render(GuiGraphics context, Font font, int absX, int absY, int mx, int my, int overrideW) {
            boolean hovered = isHovered(mx, my, absX, absY, overrideW);
            boolean isActive = activeSupplier.get();

            context.fill(absX, absY, absX + overrideW, absY + h, hovered ? 0xFF3D3D3D : 0xFF222222);

            if (isActive) {
                context.fill(absX + overrideW - 1, absY, absX + overrideW, absY + h, 0xFFFF5900);
            }

            context.drawString(font, label, absX + 6, absY + (h - 8) / 2,
                    isActive ? 0xFFFFFFFF : 0xFFAAAAAA, false);
        }
    }
}