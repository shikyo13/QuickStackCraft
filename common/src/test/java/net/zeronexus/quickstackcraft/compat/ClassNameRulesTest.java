package net.zeronexus.quickstackcraft.compat;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClassNameRulesTest {

    @Test
    void recognizesSophisticatedStorageContentSlotsOnly() {
        assertTrue(ClassNameRules.isSophisticatedStorageSlot(
                "net.p3pp3rf1y.sophisticatedcore.common.gui.StorageInventorySlot"));
        assertFalse(ClassNameRules.isSophisticatedStorageSlot(
                "net.p3pp3rf1y.sophisticatedcore.common.gui.UpgradeSlot"));
        assertFalse(ClassNameRules.isSophisticatedStorageSlot(
                "net.minecraft.world.inventory.Slot"));
    }
}
