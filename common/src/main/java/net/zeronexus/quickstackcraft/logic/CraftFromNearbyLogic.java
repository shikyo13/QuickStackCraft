package net.zeronexus.quickstackcraft.logic;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.zeronexus.quickstackcraft.util.ContainerAccess;
import net.zeronexus.quickstackcraft.util.InventoryUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.IntPredicate;

/**
 * Resolves and places a server-owned crafting recipe from inventory and nearby storage.
 */
public final class CraftFromNearbyLogic {

    private static final int MAX_TRANSFER_COUNT = 64;

    private CraftFromNearbyLogic() {}

    public static CraftResult execute(Player player, List<Ingredient> ingredients,
                                      List<Slot> craftSlots, List<ContainerAccess> containers,
                                      IntPredicate isProtected, boolean maxTransfer) {
        long needed = ingredients.stream()
                .limit(craftSlots.size())
                .filter(ingredient -> !ingredient.isEmpty())
                .count();
        if (needed == 0) {
            return new CraftResult(0, 0);
        }

        Plan plan = createPlan(ingredients, craftSlots,
                collectAvailable(player, craftSlots, containers, isProtected), maxTransfer);
        if (plan == null || !canClearGrid(player.getInventory(), craftSlots, isProtected)) {
            return new CraftResult(0, needed);
        }

        if (!clearGrid(player.getInventory(), craftSlots, isProtected)) {
            return new CraftResult(0, needed);
        }

        // Re-plan after clearing because grid contents have moved into normal inventory slots.
        plan = createPlan(ingredients, craftSlots,
                collectAvailable(player, craftSlots, containers, isProtected), maxTransfer);
        if (plan == null) {
            return new CraftResult(0, needed);
        }

        Set<ContainerAccess> usedContainers = new HashSet<>();
        List<ItemStack> extracted = new ArrayList<>(plan.stacks().size());
        for (ItemStack selected : plan.stacks()) {
            int pulled = extractSelected(player.getInventory(), containers, selected,
                    plan.amount(), isProtected, usedContainers);
            if (pulled != plan.amount()) {
                if (pulled > 0) {
                    extracted.add(selected.copyWithCount(pulled));
                }
                restoreExtracted(player, craftSlots, extracted, isProtected);
                markChanged(usedContainers);
                return new CraftResult(0, needed);
            }
            extracted.add(selected.copyWithCount(plan.amount()));
        }

        for (int i = 0; i < plan.gridSlots().size(); i++) {
            craftSlots.get(plan.gridSlots().get(i)).set(extracted.get(i));
        }
        player.getInventory().setChanged();
        markChanged(usedContainers);

        return new CraftResult(plan.gridSlots().size(), needed);
    }

    private static Plan createPlan(List<Ingredient> ingredients, List<Slot> craftSlots,
                                   List<StackGroup> groups, boolean maxTransfer) {
        List<Integer> requiredGridSlots = new ArrayList<>();
        for (int i = 0; i < Math.min(ingredients.size(), craftSlots.size()); i++) {
            if (!ingredients.get(i).isEmpty()) {
                requiredGridSlots.add(i);
            }
        }

        Map<Integer, Integer> available = new HashMap<>();
        for (int i = 0; i < groups.size(); i++) {
            available.put(i, groups.get(i).count());
        }

        int firstAmount = maxTransfer ? MAX_TRANSFER_COUNT : 1;
        for (int amount = firstAmount; amount >= 1; amount--) {
            List<List<Integer>> options = new ArrayList<>(requiredGridSlots.size());
            for (int gridSlot : requiredGridSlots) {
                Ingredient ingredient = ingredients.get(gridSlot);
                List<Integer> matches = new ArrayList<>();
                for (int groupIndex = 0; groupIndex < groups.size(); groupIndex++) {
                    ItemStack stack = groups.get(groupIndex).stack();
                    if (stack.getMaxStackSize() >= amount && ingredient.test(stack)) {
                        matches.add(groupIndex);
                    }
                }
                options.add(matches);
            }

            ChoiceAllocator.Allocation<Integer> allocation =
                    ChoiceAllocator.allocate(options, available, amount);
            if (!allocation.complete()) {
                continue;
            }

            List<ItemStack> selected = new ArrayList<>(allocation.selected().size());
            for (int groupIndex : allocation.selected()) {
                selected.add(groups.get(groupIndex).stack().copyWithCount(amount));
            }
            return new Plan(requiredGridSlots, selected, amount);
        }
        return null;
    }

    private static List<StackGroup> collectAvailable(Player player, List<Slot> craftSlots,
                                                     List<ContainerAccess> containers,
                                                     IntPredicate isProtected) {
        List<StackGroup> groups = new ArrayList<>();
        Inventory inventory = player.getInventory();
        for (int slot = InventoryUtil.HOTBAR_START; slot < InventoryUtil.MAIN_INV_END; slot++) {
            if (!isProtected.test(slot)) {
                addStack(groups, inventory.getItem(slot));
            }
        }
        for (Slot craftSlot : craftSlots) {
            addStack(groups, craftSlot.getItem());
        }
        for (ContainerAccess access : containers) {
            Container container = access.container();
            for (int slot = 0; slot < container.getContainerSize(); slot++) {
                addStack(groups, container.getItem(slot));
            }
        }
        return groups;
    }

    private static void addStack(List<StackGroup> groups, ItemStack stack) {
        if (stack.isEmpty()) {
            return;
        }
        for (int i = 0; i < groups.size(); i++) {
            StackGroup group = groups.get(i);
            if (ItemStack.isSameItemSameTags(group.stack(), stack)) {
                groups.set(i, new StackGroup(group.stack(), saturatedAdd(group.count(), stack.getCount())));
                return;
            }
        }
        groups.add(new StackGroup(stack.copyWithCount(1), stack.getCount()));
    }

    private static boolean canClearGrid(Inventory inventory, List<Slot> craftSlots,
                                        IntPredicate isProtected) {
        List<ItemStack> simulated = new ArrayList<>(InventoryUtil.MAIN_INV_END);
        for (int slot = InventoryUtil.HOTBAR_START; slot < InventoryUtil.MAIN_INV_END; slot++) {
            simulated.add(inventory.getItem(slot).copy());
        }
        for (Slot craftSlot : craftSlots) {
            if (!insertIntoInventory(simulated, craftSlot.getItem(), isProtected).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private static boolean clearGrid(Inventory inventory, List<Slot> craftSlots,
                                     IntPredicate isProtected) {
        for (Slot craftSlot : craftSlots) {
            if (!craftSlot.hasItem()) {
                continue;
            }
            ItemStack removed = craftSlot.remove(craftSlot.getItem().getCount());
            ItemStack remainder = insertIntoInventory(inventory, removed, isProtected);
            if (!remainder.isEmpty()) {
                craftSlot.set(remainder);
                return false;
            }
        }
        return true;
    }

    private static int extractSelected(Inventory inventory, List<ContainerAccess> containers,
                                       ItemStack selected, int amount, IntPredicate isProtected,
                                       Set<ContainerAccess> usedContainers) {
        int remaining = amount;
        for (int slot = InventoryUtil.HOTBAR_START;
             slot < InventoryUtil.MAIN_INV_END && remaining > 0; slot++) {
            if (isProtected.test(slot)) {
                continue;
            }
            ItemStack existing = inventory.getItem(slot);
            if (ItemStack.isSameItemSameTags(existing, selected)) {
                ItemStack removed = inventory.removeItem(slot, Math.min(existing.getCount(), remaining));
                remaining -= removed.getCount();
            }
        }

        for (ContainerAccess access : containers) {
            if (remaining == 0) {
                break;
            }
            int before = remaining;
            remaining = extractFromContainer(access.container(), selected, remaining);
            if (remaining < before) {
                usedContainers.add(access);
            }
        }
        return amount - remaining;
    }

    private static int extractFromContainer(Container container, ItemStack selected, int remaining) {
        for (int slot = 0; slot < container.getContainerSize() && remaining > 0; slot++) {
            ItemStack existing = container.getItem(slot);
            if (!existing.isEmpty() && ItemStack.isSameItemSameTags(existing, selected)) {
                ItemStack removed = container.removeItem(slot, Math.min(existing.getCount(), remaining));
                remaining -= removed.getCount();
            }
        }
        return remaining;
    }

    private static ItemStack insertIntoInventory(Inventory inventory, ItemStack stack,
                                                 IntPredicate isProtected) {
        ItemStack remaining = stack.copy();
        for (int slot = InventoryUtil.HOTBAR_START;
             slot < InventoryUtil.MAIN_INV_END && !remaining.isEmpty(); slot++) {
            if (isProtected.test(slot)) {
                continue;
            }
            ItemStack existing = inventory.getItem(slot);
            if (!existing.isEmpty() && ItemStack.isSameItemSameTags(existing, remaining)) {
                int moved = Math.min(existing.getMaxStackSize() - existing.getCount(), remaining.getCount());
                if (moved > 0) {
                    existing.grow(moved);
                    remaining.shrink(moved);
                }
            }
        }
        for (int slot = InventoryUtil.HOTBAR_START;
             slot < InventoryUtil.MAIN_INV_END && !remaining.isEmpty(); slot++) {
            if (isProtected.test(slot) || !inventory.getItem(slot).isEmpty()) {
                continue;
            }
            int moved = Math.min(remaining.getMaxStackSize(), remaining.getCount());
            inventory.setItem(slot, remaining.copyWithCount(moved));
            remaining.shrink(moved);
        }
        return remaining;
    }

    private static ItemStack insertIntoInventory(List<ItemStack> inventory, ItemStack stack,
                                                 IntPredicate isProtected) {
        ItemStack remaining = stack.copy();
        for (int slot = 0; slot < inventory.size() && !remaining.isEmpty(); slot++) {
            if (isProtected.test(slot)) {
                continue;
            }
            ItemStack existing = inventory.get(slot);
            if (!existing.isEmpty() && ItemStack.isSameItemSameTags(existing, remaining)) {
                int moved = Math.min(existing.getMaxStackSize() - existing.getCount(), remaining.getCount());
                if (moved > 0) {
                    existing.grow(moved);
                    remaining.shrink(moved);
                }
            }
        }
        for (int slot = 0; slot < inventory.size() && !remaining.isEmpty(); slot++) {
            if (isProtected.test(slot) || !inventory.get(slot).isEmpty()) {
                continue;
            }
            int moved = Math.min(remaining.getMaxStackSize(), remaining.getCount());
            inventory.set(slot, remaining.copyWithCount(moved));
            remaining.shrink(moved);
        }
        return remaining;
    }

    private static void restoreExtracted(Player player, List<Slot> craftSlots,
                                         List<ItemStack> extracted, IntPredicate isProtected) {
        Inventory inventory = player.getInventory();
        for (ItemStack stack : extracted) {
            ItemStack remainder = insertIntoInventory(inventory, stack, isProtected);
            for (Slot craftSlot : craftSlots) {
                if (remainder.isEmpty()) {
                    break;
                }
                if (!craftSlot.hasItem()) {
                    craftSlot.set(remainder);
                    remainder = ItemStack.EMPTY;
                }
            }
            if (!remainder.isEmpty()) {
                player.drop(remainder, false);
            }
        }
    }

    private static int saturatedAdd(int left, int right) {
        return (int) Math.min(Integer.MAX_VALUE, (long) left + right);
    }

    private static void markChanged(Set<ContainerAccess> containers) {
        for (ContainerAccess container : containers) {
            container.container().setChanged();
        }
    }

    private record StackGroup(ItemStack stack, int count) {}

    private record Plan(List<Integer> gridSlots, List<ItemStack> stacks, int amount) {}

    public record CraftResult(int placed, long needed) {
        public boolean isComplete() {
            return placed == needed;
        }
    }
}
