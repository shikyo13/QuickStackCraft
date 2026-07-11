package net.zeronexus.quickstackcraft.compat;

/**
 * Bit-mask helpers for client-side slot protection systems.
 */
public final class SlotMask {

    public static final int INVENTORY_SLOT_COUNT = 36;

    private SlotMask() {}

    public static long mark(int... slots) {
        long mask = 0L;
        for (int slot : slots) {
            if (isInventorySlot(slot)) {
                mask |= 1L << slot;
            }
        }
        return mask;
    }

    public static boolean contains(long mask, int slot) {
        return isInventorySlot(slot) && ((mask >>> slot) & 1L) == 1L;
    }

    public static int toItemLocksRawSlot(int inventorySlot) {
        if (inventorySlot >= 0 && inventorySlot <= 8) {
            return inventorySlot + 27;
        }
        if (inventorySlot >= 9 && inventorySlot < INVENTORY_SLOT_COUNT) {
            return inventorySlot - 9;
        }
        return inventorySlot;
    }

    private static boolean isInventorySlot(int slot) {
        return slot >= 0 && slot < INVENTORY_SLOT_COUNT;
    }
}
