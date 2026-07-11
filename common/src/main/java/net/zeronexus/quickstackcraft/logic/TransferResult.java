package net.zeronexus.quickstackcraft.logic;

import net.minecraft.core.BlockPos;

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
}
