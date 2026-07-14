package net.zeronexus.quickstackcraft.client;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TranslationKeysTest {

    @Test
    void tutorialAndPrimaryUxKeysArePresent() {
        var stream = TranslationKeysTest.class.getResourceAsStream(
                "/assets/quickstackcraft/lang/en_us.json");
        assertNotNull(stream);
        JsonObject translations = JsonParser.parseReader(
                new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject();

        Set<String> required = Set.of(
                "quickstackcraft.config.tab.general",
                "quickstackcraft.config.tab.storage",
                "quickstackcraft.config.tab.appearance",
                "quickstackcraft.button.restock",
                "key.quickstackcraft.restock",
                "quickstackcraft.tooltip.native_lock",
                "quickstackcraft.tutorial.inventory_management.title",
                "quickstackcraft.tutorial.restock.title",
                "quickstackcraft.tutorial.craft_nearby.title",
                "quickstackcraft.tutorial.whitelist_blacklist_preview.title",
                "quickstackcraft.config.appearance_preview",
                "quickstackcraft.config.about",
                "quickstackcraft.about.owner",
                "quickstackcraft.about.mod_page",
                "quickstackcraft.about.modrinth",
                "quickstackcraft.about.repository",
                "quickstackcraft.about.support");

        for (String key : required) {
            assertTrue(translations.has(key), () -> "Missing translation key: " + key);
        }
    }
}
