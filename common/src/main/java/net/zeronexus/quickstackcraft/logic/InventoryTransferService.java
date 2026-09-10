package net.zeronexus.quickstackcraft.logic;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.zeronexus.quickstackcraft.util.ContainerAccess;
import net.zeronexus.quickstackcraft.util.InventoryUtil;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BooleanSupplier;
import java.util.function.IntConsumer;
import java.util.function.IntPredicate;
import java.util.function.Supplier;

/**
 * Moves item stacks from an explicit set of sources into nearby storage endpoints.
 */
public final class InventoryTransferService {

    public enum DepositMode {
        MATCHING_STORAGE,
        ANY_STORAGE
    }

    private record SourceSlot(
            Supplier<ItemStack> contents,
            IntConsumer consume,
            BooleanSupplier accessible) {
    }

    private InventoryTransferService() {}

    public static TransferResult movePlayerInventory(
            Player player,
            List<ContainerAccess> destinations,
            boolean preserveHotbar,
            IntPredicate protectedSlot,
            DepositMode mode) {
        return moveInventory(player.getInventory(), destinations, preserveHotbar, protectedSlot, mode);
    }

    static TransferResult moveInventory(Inventory inventory, List<ContainerAccess> destinations,
                                        boolean preserveHotbar, IntPredicate protectedSlot, DepositMode mode) {
        int firstSlot = preserveHotbar ? InventoryUtil.MAIN_INV_START : InventoryUtil.HOTBAR_START;
        List<SourceSlot> sources = new ArrayList<>(InventoryUtil.MAIN_INV_END - firstSlot);

        for (int index = firstSlot; index < InventoryUtil.MAIN_INV_END; index++) {
            if (protectedSlot.test(index)) {
                continue;
            }
            int inventoryIndex = index;
            sources.add(new SourceSlot(
                    () -> inventory.getItem(inventoryIndex),
                    amount -> consumeInventorySlot(inventory, inventoryIndex, amount),
                    () -> true));
        }

        return move(sources, destinations, mode);
    }

    public static TransferResult moveOpenContainer(
            Player player,
            List<Slot> menuSlots,
            List<ContainerAccess> destinations,
            DepositMode mode) {
        List<SourceSlot> sources = new ArrayList<>(menuSlots.size());
        for (Slot slot : menuSlots) {
            sources.add(new SourceSlot(
                    slot::getItem,
                    amount -> consumeMenuSlot(player, slot, amount),
                    () -> slot.mayPickup(player)));
        }
        return move(sources, destinations, mode);
    }

    private static TransferResult move(
            List<SourceSlot> sources,
            List<ContainerAccess> destinations,
            DepositMode mode) {
        Set<ContainerAccess> changedDestinations = new LinkedHashSet<>();
        int movedItems = 0;

        for (SourceSlot source : sources) {
            if (!source.accessible().getAsBoolean()) {
                continue;
            }

            ItemStack available = source.contents().get();
            if (available.isEmpty()) {
                continue;
            }

            ItemStack remainder = available.copy();
            for (ContainerAccess destination : orderedDestinations(destinations, available, mode)) {
                int countBefore = remainder.getCount();
                remainder = destination.insertItem(remainder);
                if (remainder.getCount() != countBefore) {
                    changedDestinations.add(destination);
                }
                if (remainder.isEmpty()) {
                    break;
                }
            }

            int transferred = available.getCount() - remainder.getCount();
            if (transferred > 0) {
                source.consume().accept(transferred);
                movedItems += transferred;
            }
        }

        return TransferResult.fromChangedContainers(movedItems, changedDestinations);
    }

    private static List<ContainerAccess> orderedDestinations(
            List<ContainerAccess> destinations,
            ItemStack item,
            DepositMode mode) {
        if (mode == DepositMode.ANY_STORAGE) {
            return destinations;
        }

        return destinations.stream()
                .filter(destination -> destination.containsItem(item))
                .sorted(Comparator.comparingInt(
                        (ContainerAccess destination) -> destination.countItem(item)).reversed())
                .toList();
    }

    private static void consumeInventorySlot(Inventory inventory, int slot, int amount) {
        ItemStack stack = inventory.getItem(slot);
        stack.shrink(amount);
        inventory.setItem(slot, stack.isEmpty() ? ItemStack.EMPTY : stack);
    }

    private static void consumeMenuSlot(Player player, Slot slot, int amount) {
        ItemStack removed = slot.remove(amount);
        if (!removed.isEmpty()) {
            slot.onTake(player, removed);
        }
    }

}
