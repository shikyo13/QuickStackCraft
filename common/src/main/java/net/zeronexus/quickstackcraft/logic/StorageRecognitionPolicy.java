package net.zeronexus.quickstackcraft.logic;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.Container;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BarrelBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.zeronexus.quickstackcraft.config.QuickStackSettings;

import java.util.List;
import java.util.Set;

/**
 * Applies the server's storage whitelist/blacklist and conservative automatic detection.
 */
public final class StorageRecognitionPolicy {

    private static final List<TagKey<Block>> STORAGE_TAGS = List.of(
            blockTag("c", "chests"),
            blockTag("c", "barrels"),
            blockTag("c", "shulker_boxes"),
            blockTag("quickstackcraft", "eligible_storage"));

    private static final Set<String> STORAGE_ONLY_MODS = Set.of(
            "dimstorage",
            "enderstorage",
            "expandedstorage",
            "extended_drawers",
            "functionalstorage",
            "ironchest",
            "ironchests",
            "sophisticatedbackpacks",
            "sophisticatedstorage",
            "storagedrawers");
    private static final Set<String> NETWORK_STORAGE_MODS = Set.of(
            "ae2",
            "appliedenergistics2",
            "refinedstorage",
            "refinedstorage2");

    private StorageRecognitionPolicy() {}

    public static boolean accepts(BlockState state, BlockEntity blockEntity, Container inventory) {
        ResourceLocation blockId = BuiltInRegistries.BLOCK.getKey(state.getBlock());

        if (QuickStackSettings.configuredWhitelist.contains(blockId) || StorageBlockLists.isWhitelisted(blockId)) {
            return true;
        }
        if (QuickStackSettings.configuredBlacklist.contains(blockId) || StorageBlockLists.isBlacklisted(blockId)) {
            return false;
        }
        if (NETWORK_STORAGE_MODS.contains(blockId.getNamespace())) {
            return false;
        }
        if (QuickStackSettings.storageDetection == QuickStackSettings.StorageDetection.ANY_ITEM_INVENTORY) {
            return true;
        }

        boolean recognized = STORAGE_TAGS.stream().anyMatch(state::is)
                || isVanillaStorage(blockEntity)
                || STORAGE_ONLY_MODS.contains(blockId.getNamespace());
        if (recognized) {
            return true;
        }

        return QuickStackSettings.capacityFallbackEnabled
                && inventory.getContainerSize() >= QuickStackSettings.capacityFallbackThreshold;
    }

    private static boolean isVanillaStorage(BlockEntity blockEntity) {
        return blockEntity instanceof ChestBlockEntity
                || blockEntity instanceof BarrelBlockEntity
                || blockEntity instanceof ShulkerBoxBlockEntity;
    }

    private static TagKey<Block> blockTag(String namespace, String path) {
        return TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath(namespace, path));
    }
}
