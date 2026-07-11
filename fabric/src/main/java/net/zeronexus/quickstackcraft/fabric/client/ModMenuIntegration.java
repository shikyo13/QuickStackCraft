package net.zeronexus.quickstackcraft.fabric.client;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.zeronexus.quickstackcraft.client.QuickStackConfigScreen;

public final class ModMenuIntegration implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> Minecraft.getInstance().player == null
                ? new JoinWorldNotice(parent)
                : QuickStackConfigScreen.create(parent);
    }

    private static final class JoinWorldNotice extends Screen {
        private final Screen parent;

        private JoinWorldNotice(Screen parent) {
            super(Component.translatable("quickstackcraft.config.title"));
            this.parent = parent;
        }

        @Override
        protected void init() {
            addRenderableWidget(Button.builder(
                            Component.translatable("gui.done"), button -> onClose())
                    .bounds(width / 2 - 100, height / 2 + 18, 200, 20)
                    .build());
        }

        @Override
        public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
            renderBackground(graphics, mouseX, mouseY, delta);
            graphics.drawCenteredString(font, title, width / 2, height / 2 - 28, 0xFFFFFFFF);
            graphics.drawCenteredString(font,
                    Component.translatable("quickstackcraft.config.join_world"),
                    width / 2, height / 2 - 4, 0xFFB8B8B8);
            super.render(graphics, mouseX, mouseY, delta);
        }

        @Override
        public void onClose() {
            Minecraft.getInstance().setScreen(parent);
        }
    }
}
