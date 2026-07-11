package net.zeronexus.quickstackcraft.client;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;

import java.time.Duration;

public final class UiIconButton extends Button {

    private UiIcon icon;

    public UiIconButton(int x, int y, int size, UiIcon icon, Component label, OnPress onPress) {
        super(x, y, size, size, label, onPress, DEFAULT_NARRATION);
        this.icon = icon;
        setTooltip(Tooltip.create(label));
        setTooltipDelay(Duration.ofMillis(250));
    }

    public void setIcon(UiIcon icon) {
        this.icon = icon;
    }

    @Override
    public void renderString(GuiGraphics graphics, Font font, int textColor) {
        int iconX = getX() + (getWidth() - 12) / 2;
        int iconY = getY() + (getHeight() - 12) / 2;
        int color = active ? (isHoveredOrFocused() ? 0xFFFFFFA0 : 0xFFFFFFFF) : 0xFF777777;
        icon.render(graphics, iconX, iconY, color);
    }
}
