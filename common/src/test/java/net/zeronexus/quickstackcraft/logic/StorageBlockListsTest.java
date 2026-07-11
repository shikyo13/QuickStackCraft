package net.zeronexus.quickstackcraft.logic;

import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

class StorageBlockListsTest {

    @TempDir
    Path tempDir;

    @Test
    void allowAndDenyListsAreMutuallyExclusive() {
        StorageBlockLists.resetForTests();
        ResourceLocation barrel = ResourceLocation.fromNamespaceAndPath("minecraft", "barrel");

        assertTrue(StorageBlockLists.whitelist(barrel));
        assertTrue(StorageBlockLists.isWhitelisted(barrel));

        assertTrue(StorageBlockLists.blacklist(barrel));
        assertFalse(StorageBlockLists.isWhitelisted(barrel));
        assertTrue(StorageBlockLists.isBlacklisted(barrel));
    }

    @Test
    void persistsAndReloadsRuntimeTargetLists() throws Exception {
        Path file = tempDir.resolve("targets.properties");
        StorageBlockLists.resetForTests();
        StorageBlockLists.load(file);

        ResourceLocation chest = ResourceLocation.fromNamespaceAndPath("minecraft", "chest");
        ResourceLocation furnace = ResourceLocation.fromNamespaceAndPath("minecraft", "furnace");
        StorageBlockLists.whitelist(chest);
        StorageBlockLists.blacklist(furnace);

        assertTrue(Files.exists(file));

        StorageBlockLists.resetForTests();
        StorageBlockLists.load(file);

        assertTrue(StorageBlockLists.isWhitelisted(chest));
        assertTrue(StorageBlockLists.isBlacklisted(furnace));
    }

    @Test
    void replacesTargetListsAndKeepsAllowEntriesAuthoritative() {
        StorageBlockLists.resetForTests();
        ResourceLocation chest = ResourceLocation.fromNamespaceAndPath("minecraft", "chest");
        ResourceLocation barrel = ResourceLocation.fromNamespaceAndPath("minecraft", "barrel");

        StorageBlockLists.replace(Set.of(chest), Set.of(chest, barrel));

        assertTrue(StorageBlockLists.isWhitelisted(chest));
        assertFalse(StorageBlockLists.isBlacklisted(chest));
        assertTrue(StorageBlockLists.isBlacklisted(barrel));
    }

    @Test
    void cyclesDefaultWhitelistBlacklistAndBackToDefault() {
        StorageBlockLists.resetForTests();
        ResourceLocation chest = ResourceLocation.fromNamespaceAndPath("minecraft", "chest");

        assertEquals(StorageListState.DEFAULT, StorageBlockLists.state(chest));
        assertEquals(StorageListState.WHITELISTED, StorageBlockLists.cycle(chest));
        assertTrue(StorageBlockLists.isWhitelisted(chest));
        assertEquals(StorageListState.BLACKLISTED, StorageBlockLists.cycle(chest));
        assertFalse(StorageBlockLists.isWhitelisted(chest));
        assertTrue(StorageBlockLists.isBlacklisted(chest));
        assertEquals(StorageListState.DEFAULT, StorageBlockLists.cycle(chest));
        assertFalse(StorageBlockLists.isWhitelisted(chest));
        assertFalse(StorageBlockLists.isBlacklisted(chest));
    }
}
