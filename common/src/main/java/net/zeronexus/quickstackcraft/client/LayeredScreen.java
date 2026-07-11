package net.zeronexus.quickstackcraft.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** Draws vanilla widgets without applying the fullscreen background a second time. */
public abstract class LayeredScreen extends Screen {

    private boolean renderingWidgets;

    protected LayeredScreen(Component title) {
        super(title);
    }

    protected final void renderWidgets(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        renderingWidgets = true;
        try {
            super.render(graphics, mouseX, mouseY, delta);
        } finally {
            renderingWidgets = false;
        }
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        if (!renderingWidgets) {
            super.renderBackground(graphics, mouseX, mouseY, delta);
        }
    }
}
