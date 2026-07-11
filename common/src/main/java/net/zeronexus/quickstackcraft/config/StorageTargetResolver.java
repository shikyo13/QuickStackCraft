package net.zeronexus.quickstackcraft.config;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

/**
 * Converts recipe-viewer item ingredients into block IDs used by the storage whitelist and blacklist.
 */
public final class StorageTargetResolver {

    private StorageTargetResolver() {}

    public static Optional<ResourceLocation> fromItemStack(ItemStack stack) {
        if (stack == null || stack.isEmpty() || !(stack.getItem() instanceof BlockItem blockItem)) {
            return Optional.empty();
        }
        return Optional.ofNullable(BuiltInRegistries.BLOCK.getKey(blockItem.getBlock()));
    }
}
