package net.zeronexus.quickstackcraft.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

/**
 * Immutable block selected by a player's line of sight.
 */
public record BlockSelection(BlockPos position, ResourceLocation blockId) {

    public static Optional<BlockSelection> within(Player player, double reach) {
        if (player == null || player.level() == null) {
            return Optional.empty();
        }

        Vec3 start = player.getEyePosition(1.0F);
        Vec3 finish = start.add(player.getViewVector(1.0F).scale(reach));
        BlockHitResult result = player.level().clip(new ClipContext(
                start, finish, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player));
        if (result.getType() != HitResult.Type.BLOCK) {
            return Optional.empty();
        }

        BlockPos position = result.getBlockPos().immutable();
        ResourceLocation blockId = BuiltInRegistries.BLOCK.getKey(
                player.level().getBlockState(position).getBlock());
        return Optional.of(new BlockSelection(position, blockId));
    }
}
