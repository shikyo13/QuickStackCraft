package net.zeronexus.quickstackcraft.util;

import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * Unified wrapper around block entity containers and entity containers.
 * Provides a common interface for quick stack, dump, and restock operations.
 */
public class ContainerAccess {
    private final Container container;
    private final BlockPos blockPos;
    private final Entity entity;
    private final double distanceSq;

    public ContainerAccess(Container container, BlockPos blockPos, double distanceSq) {
        this.container = container;
        this.blockPos = blockPos.immutable();
        this.entity = null;
        this.distanceSq = distanceSq;
    }

    public ContainerAccess(Container container, Entity entity, double distanceSq) {
        this.container = container;
        this.blockPos = null;
        this.entity = entity;
        this.distanceSq = distanceSq;
    }

    public Container container() {
        return container;
    }

    public double distanceSq() {
        return distanceSq;
    }

    public BlockPos blockPos() {
        return blockPos;
    }

    public Entity entity() {
        return entity;
    }

    public boolean isBlockContainer() {
        return blockPos != null;
    }

    /**
     * Check if this container already holds at least one of the given item type.
     */
    public boolean containsItem(ItemStack stack) {
        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack existing = container.getItem(i);
            if (!existing.isEmpty() && ItemStack.isSameItemSameComponents(existing, stack)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Count how many of a given item type are in this container (total across all slots).
     */
    public int countItem(ItemStack stack) {
        int total = 0;
        int slot = container.getContainerSize();
        while (slot > 0) {
            ItemStack existing = container.getItem(--slot);
            if (!existing.isEmpty() && ItemStack.isSameItemSameComponents(existing, stack)) {
                total += existing.getCount();
            }
        }
        return total;
    }

    /**
     * Try to insert an item stack into this container.
     * Returns the remainder that couldn't fit (empty stack if everything was inserted).
     */
    public ItemStack insertItem(ItemStack stack) {
        if (stack.isEmpty()) return ItemStack.EMPTY;

        if (container instanceof DirectInsertContainer insertionTarget) {
            return insertionTarget.insertDirect(stack.copy());
        }

        return insertThroughSlots(stack);
    }

    private ItemStack insertThroughSlots(ItemStack stack) {
        ItemStack toInsert = stack.copy();

        // First pass: fill existing stacks of the same type
        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack slot = container.getItem(i);
            if (!slot.isEmpty()
                    && ItemStack.isSameItemSameComponents(slot, toInsert)
                    && container.canPlaceItem(i, toInsert)) {
                int limit = Math.min(slot.getMaxStackSize(), container.getMaxStackSize(toInsert));
                int space = limit - slot.getCount();
                if (space > 0) {
                    int transfer = Math.min(space, toInsert.getCount());
                    slot.grow(transfer);
                    container.setItem(i, slot);
                    toInsert.shrink(transfer);
                    if (toInsert.isEmpty()) return ItemStack.EMPTY;
                }
            }
        }

        // Second pass: fill empty slots
        for (int i = 0; i < container.getContainerSize(); i++) {
            if (container.getItem(i).isEmpty() && container.canPlaceItem(i, toInsert)) {
                int limit = Math.min(toInsert.getMaxStackSize(), container.getMaxStackSize(toInsert));
                int transfer = Math.min(limit, toInsert.getCount());
                if (transfer <= 0) {
                    continue;
                }
                ItemStack placed = toInsert.copyWithCount(transfer);
                container.setItem(i, placed);
                toInsert.shrink(transfer);
                if (toInsert.isEmpty()) return ItemStack.EMPTY;
            }
        }

        return toInsert;
    }

    @Override
    public String toString() {
        if (blockPos != null) {
            return "ContainerAccess[block=" + blockPos + "]";
        }
        return "ContainerAccess[entity=" + entity + "]";
    }
}
