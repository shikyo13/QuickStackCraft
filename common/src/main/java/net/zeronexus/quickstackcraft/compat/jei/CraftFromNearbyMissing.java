package net.zeronexus.quickstackcraft.compat.jei;

import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import net.minecraft.network.chat.Component;

/**
 * USER_FACING error: ingredients are NOT available anywhere
 * (not in player inventory, not in nearby containers).
 * Shows a red/disabled [+] button with a tooltip.
 */
class CraftFromNearbyMissing implements IRecipeTransferError {
    @Override
    public Type getType() {
        return Type.USER_FACING;
    }

    @Override
    public void getTooltip(ITooltipBuilder tooltip) {
        tooltip.add(Component.translatable("quickstackcraft.jei.craft_nearby_missing"));
    }
}
