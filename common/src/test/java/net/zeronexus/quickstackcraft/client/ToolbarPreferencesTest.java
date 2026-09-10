package net.zeronexus.quickstackcraft.client;

import org.junit.jupiter.api.Test;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

class ToolbarPreferencesTest {

    @Test
    void olderClientFilesKeepBothToolbarsVisibleAtTheirDefaultPositions() {
        Properties values = new Properties();
        values.setProperty("outlineColor", "4FC3F7");
        assertEquals(ToolbarPreferences.DEFAULT, ToolbarPreferences.read(values, "inventoryButtons"));
        assertEquals(ToolbarPreferences.DEFAULT, ToolbarPreferences.read(values, "storageButtons"));
    }

    @Test
    void independentVisibilityAndPixelOffsetsSurvivePersistence() {
        Properties values = new Properties();
        ToolbarPreferences inventory = new ToolbarPreferences(false, -137, 52);
        ToolbarPreferences storage = new ToolbarPreferences(true, 48, -19);
        inventory.write(values, "inventoryButtons");
        storage.write(values, "storageButtons");
        assertEquals(inventory, ToolbarPreferences.read(values, "inventoryButtons"));
        assertEquals(storage, ToolbarPreferences.read(values, "storageButtons"));
    }

    @Test
    void malformedPreferencesDoNotHideOrLoseTheToolbar() {
        Properties values = new Properties();
        values.setProperty("inventoryButtons.visible", "maybe");
        values.setProperty("inventoryButtons.offsetX", "not a number");
        values.setProperty("inventoryButtons.offsetY", "99999999999999");
        assertEquals(ToolbarPreferences.DEFAULT, ToolbarPreferences.read(values, "inventoryButtons"));
    }

    @Test
    void positionFollowsItsAnchorWhenRecipeBookMovesTheInventory() {
        ToolbarPreferences preferences = new ToolbarPreferences(true, -20, 14);
        var before = InventoryToolbarLayout.position(200, 100, 4, 12, 1, 600, 360, preferences);
        var after = InventoryToolbarLayout.position(277, 100, 4, 12, 1, 600, 360, preferences);
        assertEquals(77, after.x() - before.x());
        assertEquals(114, after.y());
    }

    @Test
    void resizingKeepsEveryButtonOnScreenWithoutRewritingOffsets() {
        ToolbarPreferences preferences = new ToolbarPreferences(true, 4000, -4000);
        var position = InventoryToolbarLayout.position(120, 62, 4, 12, 1, 320, 240, preferences);
        assertEquals(269, position.x());
        assertEquals(0, position.y());
        assertEquals(4000, preferences.offsetX());
        assertEquals(new ToolbarPreferences(true, 4096, -4096),
                new ToolbarPreferences(true, Integer.MAX_VALUE, Integer.MIN_VALUE));
    }
}
