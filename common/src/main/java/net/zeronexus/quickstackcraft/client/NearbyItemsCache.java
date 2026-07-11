package net.zeronexus.quickstackcraft.client;

import net.minecraft.world.item.Item;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * Client-side cache of items available in nearby containers.
 * Populated by server responses to scan queries, used by JEI handlers
 * to accurately show which recipes can be crafted from nearby storage.
 */
public final class NearbyItemsCache {

    private static Map<Item, Integer> nearbyItems = Map.of();
    private static long lastQueryTick = -100;
    private static long revision = 0;
    private static final int REFRESH_INTERVAL = 20; // 1 second

    private NearbyItemsCache() {}

    /** Get the count of a specific item available in nearby containers. */
    public static int getAvailable(Item item) {
        return nearbyItems.getOrDefault(item, 0);
    }

    /** Update the cache with fresh data from the server. */
    public static void update(Map<Item, Integer> items) {
        nearbyItems = new HashMap<>(items);
        revision++;
    }

    /** Check if the cache is stale and needs a server refresh. */
    public static boolean needsRefresh(long currentTick) {
        return currentTick - lastQueryTick >= REFRESH_INTERVAL;
    }

    /** Mark that a query was just sent to avoid spamming. */
    public static void markQueried(long currentTick) {
        lastQueryTick = currentTick;
    }

    /** Iterate over all cached item entries. */
    public static void forEachItem(BiConsumer<Item, Integer> action) {
        nearbyItems.forEach(action);
    }

    /** Monotonic value bumped whenever nearby item data changes on the client. */
    public static long revision() {
        return revision;
    }

    /** Clear cache (e.g., when menu closes). */
    public static void clear() {
        nearbyItems = Map.of();
        lastQueryTick = -100;
        revision++;
    }
}
