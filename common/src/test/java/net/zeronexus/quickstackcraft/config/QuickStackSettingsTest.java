package net.zeronexus.quickstackcraft.config;

import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class QuickStackSettingsTest {

    @TempDir
    Path tempDir;

    @Test
    void loadsSettingsWithClampedNumericValuesAndBlockLists() throws Exception {
        Path file = tempDir.resolve("quickstackcraft.properties");
        Files.writeString(file, """
                storageDetection=any_item_inventory
                searchRadius=99
                capacityFallback=true
                capacityThreshold=0
                outlineColor=12ABEF
                outlineOpacity=1.5
                outlineLifetimeMs=50
                storageWhitelist=minecraft:dropper, sophisticatedstorage:controller
                storageBlacklist=minecraft:furnace, bad value
                """);

        QuickStackSettings.load(file);

        assertEquals(QuickStackSettings.StorageDetection.ANY_ITEM_INVENTORY, QuickStackSettings.storageDetection);
        assertEquals(64, QuickStackSettings.searchRadius);
        assertTrue(QuickStackSettings.capacityFallbackEnabled);
        assertEquals(1, QuickStackSettings.capacityFallbackThreshold);
        assertEquals("12ABEF", QuickStackSettings.outlineRgb);
        assertEquals(1.0D, QuickStackSettings.outlineOpacity);
        assertEquals(100, QuickStackSettings.outlineLifetimeMs);
        assertTrue(QuickStackSettings.configuredWhitelist.contains(new ResourceLocation("minecraft", "dropper")));
        assertTrue(QuickStackSettings.configuredWhitelist.contains(new ResourceLocation("sophisticatedstorage", "controller")));
        assertTrue(QuickStackSettings.configuredBlacklist.contains(new ResourceLocation("minecraft", "furnace")));
    }

    @Test
    void appliesSanitizedSnapshotAndPersistsEditableSettings() throws Exception {
        Path file = tempDir.resolve("quickstackcraft.properties");
        QuickStackSettings.load(file);

        QuickStackSettings.apply(new QuickStackSettings.Snapshot(
                QuickStackSettings.StorageDetection.ANY_ITEM_INVENTORY,
                true,
                0,
                500,
                "#bad-value",
                1.5D,
                50));

        Properties properties = new Properties();
        try (var reader = Files.newBufferedReader(file)) {
            properties.load(reader);
        }

        assertEquals(QuickStackSettings.StorageDetection.ANY_ITEM_INVENTORY, QuickStackSettings.storageDetection);
        assertEquals("ANY_ITEM_INVENTORY", properties.getProperty("storageDetection"));
        assertTrue(QuickStackSettings.capacityFallbackEnabled);
        assertEquals(1, QuickStackSettings.capacityFallbackThreshold);
        assertEquals(64, QuickStackSettings.searchRadius);
        assertEquals("FFD700", QuickStackSettings.outlineRgb);
        assertEquals(1.0D, QuickStackSettings.outlineOpacity);
        assertEquals(100, QuickStackSettings.outlineLifetimeMs);
    }
}
