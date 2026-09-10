package net.zeronexus.quickstackcraft.logic;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.zeronexus.quickstackcraft.util.ContainerAccess;
import net.zeronexus.quickstackcraft.util.InventoryUtil;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.IntPredicate;

/**
 * Tops up existing player stacks from nearby storage without occupying empty slots.
 */
public final class InventoryRestockService {

    private InventoryRestockService() {}

    public static TransferResult restockPlayerInventory(
            Player player,
            List<ContainerAccess> sources,
            IntPredicate protectedSlot) {
        return restockInventory(player.getInventory(), sources, protectedSlot);
    }

    static TransferResult restockInventory(Inventory inventory, List<ContainerAccess> sources,
                                           IntPredicate protectedSlot) {
        Set<ContainerAccess> changedSources = new LinkedHashSet<>();
        int restoredItems = 0;

        for (int slot = InventoryUtil.HOTBAR_START; slot < InventoryUtil.MAIN_INV_END; slot++) {
            if (protectedSlot.test(slot)) {
                continue;
            }

            ItemStack target = inventory.getItem(slot);
            if (target.isEmpty()) {
                continue;
            }

            int stackLimit = Math.min(target.getMaxStackSize(), inventory.getMaxStackSize(target));
            int needed = stackLimit - target.getCount();
            if (needed <= 0) {
                continue;
            }

            for (ContainerAccess source : sources) {
                ItemStack extracted = source.extractItem(target, needed);
                if (extracted.isEmpty()) {
                    continue;
                }

                int accepted = Math.min(needed, extracted.getCount());
                target.grow(accepted);
                needed -= accepted;
                restoredItems += accepted;
                changedSources.add(source);
                if (needed == 0) {
                    break;
                }
            }
            inventory.setItem(slot, target);
        }

        return TransferResult.fromChangedContainers(restoredItems, changedSources);
    }
}
