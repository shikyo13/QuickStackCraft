package net.zeronexus.quickstackcraft.client.tutorial;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.zeronexus.quickstackcraft.client.LayeredScreen;
import net.zeronexus.quickstackcraft.client.UiIcon;
import net.zeronexus.quickstackcraft.client.UiIconButton;
import org.lwjgl.glfw.GLFW;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public final class TutorialScreen extends LayeredScreen {

    private static final int MAX_PANEL_WIDTH = 540;
    private static final int MAX_PANEL_HEIGHT = 344;

    private final Screen parent;
    private final boolean firstRun;
    private final List<TutorialScene> scenes = TutorialScenes.all();
    private final TutorialPlaybackController playback;
    private final List<Button> chapterButtons = new ArrayList<>();
    private UiIconButton previousButton;
    private UiIconButton nextButton;
    private UiIconButton playPauseButton;
    private boolean markedShown;
    private TutorialSeekBar seekBar;
    private Boolean lastPaused;

    private TutorialScreen(Screen parent, int initialScene, boolean firstRun) {
        super(Component.translatable("quickstackcraft.tutorial.title"));
        this.parent = parent;
        this.firstRun = firstRun;
        this.playback = new TutorialPlaybackController(
                scenes.stream().mapToDouble(TutorialScene::durationSeconds).toArray(), initialScene);
    }

    public static void open(Screen parent) { open(parent, 0); }

    public static void open(Screen parent, int chapter) {
        Minecraft.getInstance().setScreen(new TutorialScreen(parent, chapter, false));
    }

    static void openFirstRun(Screen parent) {
        Minecraft.getInstance().setScreen(new TutorialScreen(parent, 0, true));
    }

    @Override
    protected void init() {
        if (firstRun && !markedShown) {
            markedShown = true;
            TutorialClientState.markShown();
        }

        chapterButtons.clear();
        lastPaused = null;
        Layout layout = layout();
        int gap = 2;
        int chapterWidth = Math.max(34, (layout.contentWidth() - gap * (scenes.size() - 1)) / scenes.size());
        int x = layout.contentLeft();
        for (int index = 0; index < scenes.size(); index++) {
            final int sceneIndex = index;
            TutorialScene scene = scenes.get(index);
            Button button = Button.builder(scene.title(), pressed -> playback.setScene(sceneIndex))
                    .bounds(x, layout.chapterY(), chapterWidth, 20)
                    .build();
            button.setTooltip(Tooltip.create(scene.title()));
            button.setTooltipDelay(Duration.ofMillis(300));
            chapterButtons.add(button);
            addRenderableWidget(button);
            x += chapterWidth + gap;
        }

        seekBar = addRenderableWidget(new TutorialSeekBar(layout.contentLeft(), layout.progressY(),
                layout.contentWidth(), playback, 0xFFFFD34E));

        int controlsY = layout.controlsY();
        int size = 22;
        previousButton = addIcon(layout.contentLeft(), controlsY, size, UiIcon.PREVIOUS,
                "quickstackcraft.tutorial.previous", pressed -> playback.previous());
        addIcon(layout.contentLeft() + size + 2, controlsY, size, UiIcon.REPLAY,
                "quickstackcraft.tutorial.replay", pressed -> playback.replay());
        playPauseButton = addIcon(layout.contentLeft() + (size + 2) * 2, controlsY, size, UiIcon.PAUSE,
                "quickstackcraft.tutorial.pause", pressed -> playback.togglePaused());
        nextButton = addIcon(layout.contentLeft() + (size + 2) * 3, controlsY, size, UiIcon.NEXT,
                "quickstackcraft.tutorial.next", pressed -> playback.next());

        int doneWidth = 72;
        int doneX = layout.panelRight() - 12 - doneWidth;
        addRenderableWidget(Button.builder(Component.translatable("gui.done"), pressed -> onClose())
                .bounds(doneX, controlsY, doneWidth, 22)
                .build());
    }

    private UiIconButton addIcon(
            int x, int y, int size, UiIcon icon, String tooltipKey, Button.OnPress onPress) {
        UiIconButton button = new UiIconButton(x, y, size, icon,
                Component.translatable(tooltipKey), onPress);
        addRenderableWidget(button);
        return button;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        playback.onFrame(System.nanoTime());
        updateControls();
        renderBackground(graphics, mouseX, mouseY, delta);
        TutorialGraphicsBridge.resetDepth(graphics);
        Layout layout = layout();

        graphics.fill(layout.panelLeft(), layout.panelTop(),
                layout.panelRight(), layout.panelBottom(), 0xF0111315);
        graphics.renderOutline(layout.panelLeft(), layout.panelTop(),
                layout.panelWidth(), layout.panelHeight(), 0xFF7A7A7A);
        graphics.drawCenteredString(font, title, width / 2, layout.panelTop() + 8, 0xFFFFFFFF);
        drawCenteredWrapped(graphics, Component.translatable("quickstackcraft.tutorial.first_run_note"),
                layout.noteY(), layout.contentWidth(), 0xFFAAAAAA);

        TutorialRenderContext.Bounds viewport = layout.viewport();
        graphics.fill(viewport.x(), viewport.y(),
                viewport.x() + viewport.width(), viewport.y() + viewport.height(), 0xFF202326);
        graphics.renderOutline(viewport.x(), viewport.y(), viewport.width(), viewport.height(), 0xFF555A5F);

        TutorialScene scene = scenes.get(playback.sceneIndex());
        MinecraftTutorialRenderContext context = new MinecraftTutorialRenderContext(graphics, viewport);
        scene.render(context, playback.elapsedSeconds());

        graphics.drawWordWrap(font, scene.caption(playback.elapsedSeconds()),
                layout.contentLeft(), layout.captionY(), layout.contentWidth(), 0xFFE8E8E8);

        if (layout.contentWidth() >= 280) {
            graphics.drawCenteredString(font, Component.translatable("quickstackcraft.tutorial.time",
                    (int) playback.elapsedSeconds(), (int) playback.duration()),
                    (layout.contentLeft() + 100 + layout.panelRight() - 90) / 2,
                    layout.controlsY() + 7, 0xFFB5C0C4);
        }

        renderWidgets(graphics, mouseX, mouseY, delta);
    }

    private void updateControls() {
        if (playPauseButton != null && (lastPaused == null || lastPaused != playback.paused())) {
            lastPaused = playback.paused();
            playPauseButton.setMessage(Component.translatable(playback.paused()
                    ? "quickstackcraft.tutorial.play" : "quickstackcraft.tutorial.pause"));
            playPauseButton.setIcon(playback.paused() ? UiIcon.PLAY : UiIcon.PAUSE);
            playPauseButton.setTooltip(Tooltip.create(Component.translatable(playback.paused()
                    ? "quickstackcraft.tutorial.play"
                    : "quickstackcraft.tutorial.pause")));
        }
        if (previousButton != null) {
            previousButton.active = playback.sceneIndex() > 0 || playback.elapsedSeconds() > 1.0D;
        }
        if (nextButton != null) {
            nextButton.active = playback.sceneIndex() + 1 < playback.sceneCount();
        }
        for (int index = 0; index < chapterButtons.size(); index++) {
            chapterButtons.get(index).active = index != playback.sceneIndex();
        }
    }

    private void drawCenteredWrapped(
            GuiGraphics graphics, Component text, int y, int maxWidth, int color) {
        for (var line : font.split(text, maxWidth)) {
            graphics.drawString(font, line, width / 2 - font.width(line) / 2, y, color, false);
            y += 9;
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (getFocused() == seekBar && (keyCode == GLFW.GLFW_KEY_LEFT || keyCode == GLFW.GLFW_KEY_RIGHT
                || keyCode == GLFW.GLFW_KEY_HOME || keyCode == GLFW.GLFW_KEY_END)) {
            return seekBar.keyPressed(keyCode, scanCode, modifiers);
        }
        if (keyCode == GLFW.GLFW_KEY_SPACE) {
            playback.togglePaused();
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_LEFT) {
            playback.previous();
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_RIGHT) {
            playback.next();
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_R) {
            playback.replay();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean isPauseScreen() {
        return true;
    }

    @Override
    public void onClose() {
        Minecraft.getInstance().setScreen(parent);
    }

    private Layout layout() {
        int panelWidth = Math.min(MAX_PANEL_WIDTH, Math.max(200, width - 12));
        int panelHeight = Math.max(180, Math.min(MAX_PANEL_HEIGHT, height - 8));
        int panelLeft = (width - panelWidth) / 2;
        int panelTop = Math.max(4, (height - panelHeight) / 2);
        int contentLeft = panelLeft + 12;
        int contentWidth = panelWidth - 24;
        int noteY = panelTop + 19;
        int noteHeight = font.split(Component.translatable("quickstackcraft.tutorial.first_run_note"),
                contentWidth).size() * 9;
        int chapterY = noteY + noteHeight + 3;
        int viewportY = chapterY + 24;
        int controlsY = panelTop + panelHeight - 26;
        int progressY = controlsY - 16;
        int captionY = progressY - 38;
        int viewportHeight = Math.max(16, captionY - viewportY - 6);
        return new Layout(panelLeft, panelTop, panelWidth, panelHeight,
                contentLeft, contentWidth, noteY, chapterY,
                new TutorialRenderContext.Bounds(contentLeft, viewportY, contentWidth, viewportHeight),
                captionY, progressY, controlsY);
    }

    private record Layout(
            int panelLeft,
            int panelTop,
            int panelWidth,
            int panelHeight,
            int contentLeft,
            int contentWidth,
            int noteY,
            int chapterY,
            TutorialRenderContext.Bounds viewport,
            int captionY,
            int progressY,
            int controlsY) {
        int panelRight() {
            return panelLeft + panelWidth;
        }

        int panelBottom() {
            return panelTop + panelHeight;
        }
    }
}
