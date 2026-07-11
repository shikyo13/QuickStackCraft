package net.zeronexus.quickstackcraft.logic;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.vehicle.ContainerEntity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.zeronexus.quickstackcraft.util.ContainerAccess;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Scans for nearby containers using chunk-based BlockEntity iteration
 * instead of brute-force position checking.
 */
public final class ContainerScanner {

    private ContainerScanner() {}

    /**
     * Find all accessible containers within the given radius of a position.
     * Results are sorted by distance from the center position (closest first).
     */
    public static List<ContainerAccess> findNearby(Level level, Vec3 center, int radius, boolean includeEntities) {
        List<ContainerAccess> results = new ArrayList<>();
        double radiusSq = (double) radius * radius;
        BlockPos centerBlock = BlockPos.containing(center);

        // Track positions we've already processed (for double chest dedup)
        Set<BlockPos> processed = new HashSet<>();

        // Compute chunk range overlapping the scan sphere
        int minChunkX = SectionPos.blockToSectionCoord(centerBlock.getX() - radius);
        int maxChunkX = SectionPos.blockToSectionCoord(centerBlock.getX() + radius);
        int minChunkZ = SectionPos.blockToSectionCoord(centerBlock.getZ() - radius);
        int maxChunkZ = SectionPos.blockToSectionCoord(centerBlock.getZ() + radius);

        for (int cx = minChunkX; cx <= maxChunkX; cx++) {
            for (int cz = minChunkZ; cz <= maxChunkZ; cz++) {
                LevelChunk chunk = level.getChunkSource().getChunkNow(cx, cz);
                if (chunk == null) continue;

                for (Map.Entry<BlockPos, BlockEntity> entry : chunk.getBlockEntities().entrySet()) {
                    BlockPos pos = entry.getKey();
                    BlockEntity be = entry.getValue();

                    if (processed.contains(pos)) continue;

                    double distSq = center.distanceToSqr(Vec3.atCenterOf(pos));
                    if (distSq > radiusSq) continue;

                    Container container = getContainerFromBlockEntity(level, pos, be, processed);
                    if (container == null) continue;
                    if (!StorageRecognitionPolicy.accepts(level.getBlockState(pos), be, container)) continue;

                    results.add(new ContainerAccess(container, pos, distSq));
                }
            }
        }

        // Scan entity inventories (donkeys, llamas, chest boats, chest minecarts)
        if (includeEntities) {
            AABB scanBox = new AABB(
                center.x - radius, center.y - radius, center.z - radius,
                center.x + radius, center.y + radius, center.z + radius
            );
            for (Entity entity : level.getEntities(null, scanBox)) {
                Container container = getContainerFromEntity(entity);
                if (container == null) continue;

                double distSq = center.distanceToSqr(entity.position());
                if (distSq > radiusSq) continue;

                results.add(new ContainerAccess(container, entity, distSq));
            }
        }

        results.sort(Comparator.comparingDouble(ContainerAccess::distanceSq));
        return results;
    }

    /**
     * Extract a Container from a BlockEntity, handling double chest merging and locked containers.
     * Returns null if the block entity is not a valid container.
     */
    private static Container getContainerFromBlockEntity(Level level, BlockPos pos, BlockEntity be, Set<BlockPos> processed) {
        BlockState state = level.getBlockState(pos);

        // Handle vanilla double chests - only process LEFT and SINGLE to avoid duplicates
        if (state.getBlock() instanceof ChestBlock chestBlock) {
            if (state.hasProperty(ChestBlock.TYPE)) {
                ChestType type = state.getValue(ChestBlock.TYPE);
                if (type == ChestType.RIGHT) {
                    processed.add(pos);
                    return null;
                }
            }
            Container merged = ChestBlock.getContainer(chestBlock, state, level, pos, true);
            if (merged != null) {
                processed.add(pos);
                ChestType type = state.getValue(ChestBlock.TYPE);
                if (type == ChestType.LEFT) {
                    processed.add(pos.relative(ChestBlock.getConnectedDirection(state)));
                }
                return merged;
            }
            return null;
        }

        // Vanilla Container interface (chests, barrels, hoppers, etc.)
        if (be instanceof Container c) {
            processed.add(pos);
            return c;
        }

        // Modded containers: try platform-specific capability (IItemHandler on NeoForge)
        Container capContainer = getContainerFromCapability(level, pos);
        if (capContainer != null) {
            processed.add(pos);
            return capContainer;
        }

        return null;
    }

    /**
     * Platform-specific: try to get a Container from a block via capabilities.
     * On NeoForge, queries IItemHandler capability and wraps it.
     * On Fabric, returns null (most Fabric mods implement Container directly).
     */
    @ExpectPlatform
    public static Container getContainerFromCapability(Level level, BlockPos pos) {
        throw new AssertionError("Not implemented");
    }

    public static boolean hasItemStorage(Level level, BlockPos pos) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof Container container && container.getContainerSize() > 0) {
            return true;
        }
        Container capability = getContainerFromCapability(level, pos);
        return capability != null && capability.getContainerSize() > 0;
    }

    /**
     * Extract a Container from an entity (donkeys, llamas, chest boats, minecarts).
     */
    private static Container getContainerFromEntity(Entity entity) {
        // Chest boats, chest minecarts, hopper minecarts
        if (entity instanceof ContainerEntity ce) {
            return ce;
        }
        // TODO: Donkeys/mules/llamas need an accessor mixin for their inventory
        return null;
    }
}
