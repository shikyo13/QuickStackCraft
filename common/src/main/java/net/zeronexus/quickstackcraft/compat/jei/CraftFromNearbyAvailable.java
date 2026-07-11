package net.zeronexus.quickstackcraft.compat.jei;

import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import net.minecraft.network.chat.Component;

/**
 * COSMETIC error: ingredients are available (player inv + nearby containers).
 * Shows a green [+] button with a tooltip explaining the feature.
 */
class CraftFromNearbyAvailable implements IRecipeTransferError {
    @Override
    public Type getType() {
        return Type.COSMETIC;
    }

    @Override
    public int getButtonHighlightColor() {
        return 0x8040FF40;
    }

    @Override
    public void getTooltip(ITooltipBuilder tooltip) {
        tooltip.add(Component.translatable("quickstackcraft.jei.craft_nearby_tooltip"));
    }
}
