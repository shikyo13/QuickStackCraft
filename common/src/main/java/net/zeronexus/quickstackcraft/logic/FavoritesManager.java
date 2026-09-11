package net.zeronexus.quickstackcraft.logic;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.world.entity.player.Player;

import java.util.Set;

/**
 * Platform-abstracted favorites manager.
 * Both loaders persist player slot locks in world saved data.
 * Server is source of truth - client stores a local cache synced via packets.
 */
public final class FavoritesManager {

    private FavoritesManager() {}

    @ExpectPlatform
    public static boolean isFavorited(Player player, int slot) {
        throw new AssertionError("Not implemented");
    }

    @ExpectPlatform
    public static void toggle(Player player, int slot) {
        throw new AssertionError("Not implemented");
    }

    @ExpectPlatform
    public static Set<Integer> getAll(Player player) {
        throw new AssertionError("Not implemented");
    }

    @ExpectPlatform
    public static void setAll(Player player, Set<Integer> slots) {
        throw new AssertionError("Not implemented");
    }
}
