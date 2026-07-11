package net.zeronexus.quickstackcraft.logic;

import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.zeronexus.quickstackcraft.compat.ClassNameRules;

import java.util.List;

/**
 * Identifies actual content slots in supported open storage menus.
 */
public final class OpenContainerSource {

    private OpenContainerSource() {}

    public static List<Slot> contentSlots(AbstractContainerMenu menu) {
        if (menu == null) {
            return List.of();
        }
        return menu.slots.stream()
                .filter(slot -> ClassNameRules.isSophisticatedStorageSlot(slot.getClass()))
                .toList();
    }

    public static boolean supports(AbstractContainerMenu menu) {
        return menu != null && menu.slots.stream()
                .anyMatch(slot -> ClassNameRules.isSophisticatedStorageSlot(slot.getClass()));
    }
}
