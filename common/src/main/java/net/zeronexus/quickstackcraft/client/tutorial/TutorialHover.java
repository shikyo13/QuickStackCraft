package net.zeronexus.quickstackcraft.client.tutorial;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** Contextual lessons on the mod's controls, using the player's forward key. */
public final class TutorialHover {
    private static Screen owner;
    private static int chapter = -1;
    private static long started;
    private static boolean ready = true;

    private TutorialHover() {}

    public static Component hint() {
        return Component.translatable("quickstackcraft.tutorial.hover_hint",
                Minecraft.getInstance().options.keyUp.getTranslatedKeyMessage());
    }

    public static void update(Screen screen, GuiGraphics graphics, int mouseX, int mouseY, int targetChapter) {
        Minecraft mc = Minecraft.getInstance();
        InputConstants.Key key = InputConstants.getKey(mc.options.keyUp.saveString());
        boolean held = key.getType() == InputConstants.Type.KEYSYM
                && InputConstants.isKeyDown(mc.getWindow().getWindow(), key.getValue());
        if (!held) ready = true;
        if (owner != screen || chapter != targetChapter || !held || !mc.isWindowActive()) started = 0;
        owner = screen;
        chapter = targetChapter;
        if (!ready || !held || !mc.isWindowActive() || targetChapter < 0 || mc.player == null
                || !mc.player.containerMenu.getCarried().isEmpty()) return;
        long now = System.nanoTime();
        if (started == 0) started = now;
        double progress = Math.min(1, (now - started) / 650_000_000.0);
        int x = Math.min(screen.width - 46, mouseX + 9);
        int y = Math.min(screen.height - 8, mouseY + 18);
        graphics.pose().pushPose();
        graphics.pose().translate(0, 0, 500);
        graphics.fill(x, y, x + 38, y + 4, 0xFF34383C);
        graphics.fill(x, y, x + (int) (38 * progress), y + 4, 0xFFFFD34E);
        graphics.pose().popPose();
        if (progress >= 1) {
            ready = false;
            started = 0;
            TutorialScreen.open(screen, targetChapter);
        }
    }

    public static boolean consumes(Screen screen, int keyCode, int scanCode) {
        return owner == screen && chapter >= 0 && Minecraft.getInstance().options.keyUp.matches(keyCode, scanCode);
    }
}
