package net.zeronexus.quickstackcraft.compat;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SlotMaskTest {

    @Test
    void marksOnlyInventorySlotsZeroThroughThirtyFive() {
        long mask = SlotMask.mark(0, 8, 9, 35, -1, 36, 63);

        assertTrue(SlotMask.contains(mask, 0));
        assertTrue(SlotMask.contains(mask, 8));
        assertTrue(SlotMask.contains(mask, 9));
        assertTrue(SlotMask.contains(mask, 35));
        assertFalse(SlotMask.contains(mask, -1));
        assertFalse(SlotMask.contains(mask, 36));
        assertFalse(SlotMask.contains(mask, 63));
    }

    @Test
    void convertsMinecraftInventorySlotsToItemLocksRawSlots() {
        assertEquals(27, SlotMask.toItemLocksRawSlot(0));
        assertEquals(35, SlotMask.toItemLocksRawSlot(8));
        assertEquals(0, SlotMask.toItemLocksRawSlot(9));
        assertEquals(26, SlotMask.toItemLocksRawSlot(35));
        assertEquals(40, SlotMask.toItemLocksRawSlot(40));
    }
}
