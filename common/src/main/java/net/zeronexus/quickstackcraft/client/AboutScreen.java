package net.zeronexus.quickstackcraft.client;

import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

import java.net.URI;
import java.time.Duration;
import java.util.List;

public final class AboutScreen extends LayeredScreen {

    private static final String MOD_PAGE_URL =
            "https://www.curseforge.com/minecraft/mc-mods/quickstack-craft";
    private static final String MODRINTH_URL =
            "https://modrinth.com/mod/quickstack-craft";
    private static final String REPOSITORY_URL =
            "https://github.com/shikyo13/QuickStackCraft";
    private static final String SUPPORT_URL =
            "https://buymeacoffee.com/zerotheabsolute";

    private final Screen parent;

    public AboutScreen(Screen parent) {
        super(Component.translatable("quickstackcraft.about.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        Bounds panel = panelBounds();
        int buttonWidth = Math.min(220, panel.width() - 28);
        int buttonX = panel.x() + (panel.width() - buttonWidth) / 2;
        int firstButtonY = panel.y() + 64;

        addLinkButton(buttonX, firstButtonY, buttonWidth,
                Component.translatable("quickstackcraft.about.mod_page"), MOD_PAGE_URL);
        addLinkButton(buttonX, firstButtonY + 24, buttonWidth,
                Component.translatable("quickstackcraft.about.modrinth"), MODRINTH_URL);
        addLinkButton(buttonX, firstButtonY + 48, buttonWidth,
                Component.translatable("quickstackcraft.about.repository"), REPOSITORY_URL);
        addLinkButton(buttonX, firstButtonY + 72, buttonWidth,
                Component.translatable("quickstackcraft.about.support"), SUPPORT_URL);

        addRenderableWidget(Button.builder(Component.translatable("gui.done"), pressed -> onClose())
                .bounds(panel.x() + panel.width() - 84, panel.y() + panel.height() - 26, 72, 20)
                .build());
    }

    private void addLinkButton(int x, int y, int width, Component label, String url) {
        Button button = Button.builder(label, pressed -> Util.getPlatform().openUri(URI.create(url)))
                .bounds(x, y, width, 20)
                .build();
        button.setTooltip(Tooltip.create(Component.literal(url)));
        button.setTooltipDelay(Duration.ofMillis(300));
        addRenderableWidget(button);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        renderBackground(graphics, mouseX, mouseY, delta);
        Bounds panel = panelBounds();
        int right = panel.x() + panel.width();
        int bottom = panel.y() + panel.height();

        graphics.fill(panel.x(), panel.y(), right, bottom, 0xF0111315);
        graphics.renderOutline(panel.x(), panel.y(), panel.width(), panel.height(), 0xFF7A7A7A);
        graphics.drawCenteredString(font, title, width / 2, panel.y() + 9, 0xFFFFFFFF);

        int textWidth = panel.width() - 28;
        int textY = panel.y() + 29;
        textY = drawCenteredWrapped(graphics,
                Component.translatable("quickstackcraft.about.owner"), textY, textWidth, 0xFFFFD36A);
        drawCenteredWrapped(graphics,
                Component.translatable("quickstackcraft.about.description"), textY + 2,
                textWidth, 0xFFD6D6D6);

        renderWidgets(graphics, mouseX, mouseY, delta);
    }

    private int drawCenteredWrapped(
            GuiGraphics graphics, Component text, int y, int textWidth, int color) {
        List<FormattedCharSequence> lines = font.split(text, textWidth);
        for (FormattedCharSequence line : lines) {
            graphics.drawCenteredString(font, line, width / 2, y, color);
            y += font.lineHeight;
        }
        return y;
    }

    private Bounds panelBounds() {
        int panelWidth = Math.min(360, width - 20);
        int panelHeight = Math.min(190, height - 12);
        return new Bounds((width - panelWidth) / 2, (height - panelHeight) / 2,
                panelWidth, panelHeight);
    }

    @Override
    public void onClose() {
        Minecraft.getInstance().setScreen(parent);
    }

    private record Bounds(int x, int y, int width, int height) {}
}
