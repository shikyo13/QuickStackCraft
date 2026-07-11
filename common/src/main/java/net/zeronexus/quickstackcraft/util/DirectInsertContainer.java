package net.zeronexus.quickstackcraft.util;

import net.minecraft.world.item.ItemStack;

/**
 * Implemented by platform storage adapters that can insert through the real mod API.
 */
public interface DirectInsertContainer {

    ItemStack insertDirect(ItemStack stack);
}
