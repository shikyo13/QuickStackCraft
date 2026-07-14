package net.zeronexus.quickstackcraft.client;

import net.zeronexus.quickstackcraft.logic.StorageListState;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TutorialVisualParityTest {

    @Test
    void inventoryToolbarFitsBetweenRecipeBookAndPanelEdge() {
        int recipeBookRight = 124;
        int inventoryWidth = 176;

        assertEquals(124, InventoryToolbarLayout.buttonX(0, 0));
        assertEquals(137, InventoryToolbarLayout.buttonX(0, 1));
        assertEquals(150, InventoryToolbarLayout.buttonX(0, 2));
        assertEquals(163, InventoryToolbarLayout.buttonX(0, 3));
        assertTrue(InventoryToolbarLayout.buttonX(0, 0) >= recipeBookRight);
        assertTrue(InventoryToolbarLayout.buttonX(0, 3) + InventoryToolbarLayout.BUTTON_SIZE
                <= inventoryWidth);
    }

    @Test
    void whitelistAndBlacklistColorsAreSharedWithTutorialScenes() {
        assertEquals(0xFF4FDB6B,
                StorageHighlightPalette.listStateArgb(StorageListState.WHITELISTED));
        assertEquals(0xFFF0454D,
                StorageHighlightPalette.listStateArgb(StorageListState.BLACKLISTED));
        assertEquals(0xFF40C7EB,
                StorageHighlightPalette.listStateArgb(StorageListState.DEFAULT));
        assertEquals(0xFF45D4E8, StorageHighlightPalette.sourceArgb());
    }

    @Test
    void destinationAppearanceUsesTheSelectedColorAndOpacity() {
        assertEquals(0x804FC3F7,
                StorageHighlightPalette.destinationArgb("4FC3F7", 0.5D));
        assertEquals(0xFFFFD700,
                StorageHighlightPalette.destinationArgb("not-a-color", 1.5D));
    }
}
