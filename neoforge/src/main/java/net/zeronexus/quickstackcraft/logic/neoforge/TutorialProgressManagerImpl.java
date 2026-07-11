package net.zeronexus.quickstackcraft.logic.neoforge;

import net.minecraft.world.entity.player.Player;

public final class TutorialProgressManagerImpl {

    private TutorialProgressManagerImpl() {}

    public static boolean hasSeen(Player player) {
        return player.getData(FavoritesManagerImpl.TUTORIAL_SEEN.get());
    }

    public static void markSeen(Player player) {
        player.setData(FavoritesManagerImpl.TUTORIAL_SEEN.get(), true);
    }
}
