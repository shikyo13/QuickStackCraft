package net.zeronexus.quickstackcraft.compat.jei;

import mezz.jei.api.gui.handlers.IGuiProperties;
import mezz.jei.api.gui.handlers.IScreenHandler;
import net.minecraft.client.gui.screens.Screen;
import net.zeronexus.quickstackcraft.client.QuickStackConfigScreen;

/**
 * Gives JEI the settings panel bounds so its ingredient list remains available.
 */
public final class QuickStackConfigJeiScreenHandler
        implements IScreenHandler<QuickStackConfigScreen> {

    @Override
    public IGuiProperties apply(QuickStackConfigScreen screen) {
        QuickStackConfigScreen.DropArea area = screen.panelArea();
        if (area.width() <= 0 || area.height() <= 0 || screen.width <= 0 || screen.height <= 0) {
            return null;
        }
        return new Properties(
                QuickStackConfigScreen.class,
                area.x(),
                area.y(),
                area.width(),
                area.height(),
                screen.width,
                screen.height);
    }

    private record Properties(
            Class<? extends Screen> getScreenClass,
            int getGuiLeft,
            int getGuiTop,
            int getGuiXSize,
            int getGuiYSize,
            int getScreenWidth,
            int getScreenHeight) implements IGuiProperties {
    }
}
