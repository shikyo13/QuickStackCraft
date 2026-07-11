package net.zeronexus.quickstackcraft.client;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ClientPreferencesTest {

    @Test
    void sanitizesRgbColorsWithoutChangingValidValues() {
        assertEquals("4FC3F7", ClientPreferences.sanitizeRgb("#4fc3f7", "FFD700"));
        assertEquals("FFD700", ClientPreferences.sanitizeRgb("not-a-color", "FFD700"));
    }
}
