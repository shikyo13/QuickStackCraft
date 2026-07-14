package net.zeronexus.quickstackcraft.logic;

import net.minecraft.core.BlockPos;
import net.zeronexus.quickstackcraft.util.ContainerAccess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Result of a quick stack or dump operation.
 * Carries aggregate counts and the positions of containers that received items.
 */
public record TransferResult(int itemsMoved, int containersUsed,
                             List<BlockPos> blockPositions, List<Integer> entityIds) {

    public static final TransferResult EMPTY = new TransferResult(0, 0, List.of(), List.of());

    public boolean didSomething() {
        return itemsMoved > 0;
    }

    public static TransferResult fromChangedContainers(
            int itemsMoved, Collection<ContainerAccess> containers) {
        if (itemsMoved <= 0 || containers.isEmpty()) {
            return EMPTY;
        }

        List<BlockPos> blocks = new ArrayList<>();
        List<Integer> entities = new ArrayList<>();
        for (ContainerAccess container : containers) {
            container.container().setChanged();
            if (container.isBlockContainer()) {
                blocks.add(container.blockPos());
            } else if (container.entity() != null) {
                entities.add(container.entity().getId());
            }
        }
        return new TransferResult(
                itemsMoved, containers.size(), List.copyOf(blocks), List.copyOf(entities));
    }
}
