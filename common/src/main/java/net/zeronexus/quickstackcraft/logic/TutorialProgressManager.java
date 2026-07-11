package net.zeronexus.quickstackcraft.logic;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.world.entity.player.Player;

public final class TutorialProgressManager {

    private TutorialProgressManager() {}

    @ExpectPlatform
    public static boolean hasSeen(Player player) {
        throw new AssertionError("Not implemented");
    }

    @ExpectPlatform
    public static void markSeen(Player player) {
        throw new AssertionError("Not implemented");
    }
}
