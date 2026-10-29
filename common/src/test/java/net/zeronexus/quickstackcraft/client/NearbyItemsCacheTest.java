package net.zeronexus.quickstackcraft.client;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NearbyItemsCacheTest {

    @Test
    void refreshesAndInvalidatesWhenWorldTimeMovesBackward() {
        NearbyItemsCache.clear();
        NearbyItemsCache.markQueried(12_000);
        long previousRevision = NearbyItemsCache.revision();

        assertFalse(NearbyItemsCache.needsRefresh(12_019));
        assertTrue(NearbyItemsCache.needsRefresh(12_020));
        assertTrue(NearbyItemsCache.needsRefresh(100));
        assertEquals(previousRevision + 1, NearbyItemsCache.revision());

        NearbyItemsCache.markQueried(100);
        assertFalse(NearbyItemsCache.needsRefresh(101));
        assertTrue(NearbyItemsCache.needsRefresh(120));
    }
}
