package net.zeronexus.quickstackcraft.client;

import net.minecraft.client.gui.GuiGraphics;

public enum UiIcon {
    QUICK_STACK,
    DUMP,
    SETTINGS,
    TUTORIAL,
    ABOUT,
    REMOVE,
    PREVIOUS,
    NEXT,
    PLAY,
    PAUSE,
    REPLAY,
    CHECK,
    BLOCKED;

    public void render(GuiGraphics graphics, int x, int y, int color) {
        switch (this) {
            case QUICK_STACK -> renderQuickStack(graphics, x, y, color);
            case DUMP -> renderDump(graphics, x, y, color);
            case SETTINGS -> renderGear(graphics, x, y, color);
            case TUTORIAL -> renderTutorial(graphics, x, y, color);
            case ABOUT -> renderInfo(graphics, x, y, color);
            case REMOVE -> renderRemove(graphics, x, y, color);
            case PREVIOUS -> renderChevron(graphics, x, y, color, false);
            case NEXT -> renderChevron(graphics, x, y, color, true);
            case PLAY -> renderPlay(graphics, x, y, color);
            case PAUSE -> renderPause(graphics, x, y, color);
            case REPLAY -> renderReplay(graphics, x, y, color);
            case CHECK -> renderCheck(graphics, x, y, color);
            case BLOCKED -> renderBlocked(graphics, x, y, color);
        }
    }

    public static void renderLock(GuiGraphics graphics, int x, int y, int color) {
        graphics.fill(x + 1, y, x + 5, y + 1, color);
        graphics.fill(x, y + 1, x + 1, y + 4, color);
        graphics.fill(x + 5, y + 1, x + 6, y + 4, color);
        graphics.fill(x, y + 3, x + 6, y + 8, color);
        graphics.fill(x + 2, y + 5, x + 4, y + 7, 0xFF3A2C16);
    }

    private static void renderQuickStack(GuiGraphics g, int x, int y, int c) {
        chest(g, x, y + 5, c);
        g.fill(x + 5, y, x + 7, y + 5, c);
        g.fill(x + 3, y + 2, x + 9, y + 4, c);
    }

    private static void renderDump(GuiGraphics g, int x, int y, int c) {
        chest(g, x, y + 6, c);
        g.fill(x + 2, y, x + 4, y + 5, c);
        g.fill(x + 7, y, x + 9, y + 5, c);
        g.fill(x + 1, y + 3, x + 5, y + 5, c);
        g.fill(x + 6, y + 3, x + 10, y + 5, c);
    }

    private static void chest(GuiGraphics g, int x, int y, int c) {
        g.fill(x + 1, y, x + 11, y + 2, c);
        g.fill(x, y + 2, x + 12, y + 9, c);
        g.fill(x + 1, y + 3, x + 11, y + 8, 0xFF8A5A2B);
        g.fill(x + 5, y + 2, x + 7, y + 5, 0xFFFFD36A);
    }

    private static void renderGear(GuiGraphics g, int x, int y, int c) {
        g.fill(x + 4, y, x + 8, y + 12, c);
        g.fill(x, y + 4, x + 12, y + 8, c);
        g.fill(x + 2, y + 2, x + 10, y + 10, c);
        g.fill(x + 4, y + 4, x + 8, y + 8, 0xFF303030);
    }

    private static void renderTutorial(GuiGraphics g, int x, int y, int c) {
        g.fill(x, y + 1, x + 5, y + 11, c);
        g.fill(x + 7, y + 1, x + 12, y + 11, c);
        g.fill(x + 5, y + 3, x + 7, y + 12, c);
        g.fill(x + 2, y + 3, x + 4, y + 4, 0xFF303030);
        g.fill(x + 8, y + 3, x + 10, y + 4, 0xFF303030);
    }

    private static void renderInfo(GuiGraphics g, int x, int y, int c) {
        g.fill(x + 4, y, x + 8, y + 3, c);
        g.fill(x + 4, y + 5, x + 8, y + 12, c);
    }

    private static void renderRemove(GuiGraphics g, int x, int y, int c) {
        for (int i = 0; i < 8; i++) {
            g.fill(x + 2 + i, y + 2 + i, x + 3 + i, y + 3 + i, c);
            g.fill(x + 9 - i, y + 2 + i, x + 10 - i, y + 3 + i, c);
        }
    }

    private static void renderChevron(GuiGraphics g, int x, int y, int c, boolean right) {
        for (int i = 0; i < 5; i++) {
            int px = right ? x + 3 + i : x + 8 - i;
            g.fill(px, y + 2 + i, px + 2, y + 4 + i, c);
            g.fill(px, y + 10 - i, px + 2, y + 12 - i, c);
        }
    }

    private static void renderPlay(GuiGraphics g, int x, int y, int c) {
        for (int i = 0; i < 6; i++) {
            g.fill(x + 3 + i, y + 2 + i, x + 5 + i, y + 11 - i, c);
        }
    }

    private static void renderPause(GuiGraphics g, int x, int y, int c) {
        g.fill(x + 2, y + 2, x + 5, y + 11, c);
        g.fill(x + 7, y + 2, x + 10, y + 11, c);
    }

    private static void renderReplay(GuiGraphics g, int x, int y, int c) {
        g.fill(x + 2, y + 2, x + 9, y + 4, c);
        g.fill(x + 8, y + 3, x + 10, y + 10, c);
        g.fill(x + 3, y + 9, x + 9, y + 11, c);
        g.fill(x, y, x + 4, y + 5, c);
    }

    private static void renderCheck(GuiGraphics g, int x, int y, int c) {
        g.fill(x + 1, y + 6, x + 4, y + 9, c);
        g.fill(x + 3, y + 8, x + 6, y + 11, c);
        g.fill(x + 5, y + 5, x + 8, y + 9, c);
        g.fill(x + 7, y + 2, x + 11, y + 6, c);
    }

    private static void renderBlocked(GuiGraphics g, int x, int y, int c) {
        g.renderOutline(x + 1, y + 1, 10, 10, c);
        for (int i = 0; i < 8; i++) {
            g.fill(x + 2 + i, y + 2 + i, x + 4 + i, y + 4 + i, c);
        }
    }
}
