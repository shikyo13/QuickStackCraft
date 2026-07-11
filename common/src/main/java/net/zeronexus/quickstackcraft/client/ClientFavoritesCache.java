package net.zeronexus.quickstackcraft.client;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Client-side cache of native locked slot indices.
 * Updated by FavoriteSyncS2CPacket from server.
 * The legacy class name is retained so existing save and packet identifiers stay compatible.
 */
public final class ClientFavoritesCache {

    private static Set<Integer> favoritedSlots = Collections.emptySet();

    private ClientFavoritesCache() {}

    public static void update(Set<Integer> slots) {
        favoritedSlots = Set.copyOf(slots);
    }

    public static boolean isFavorited(int slot) {
        return favoritedSlots.contains(slot);
    }

    public static Set<Integer> getAll() {
        return favoritedSlots;
    }

    public static void clear() {
        favoritedSlots = Collections.emptySet();
    }
}
