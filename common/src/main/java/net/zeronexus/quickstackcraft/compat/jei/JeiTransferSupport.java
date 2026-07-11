package net.zeronexus.quickstackcraft.compat.jei;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.zeronexus.quickstackcraft.compat.RecipeViewerTransferHelper;

import java.util.List;

final class JeiTransferSupport {

    private JeiTransferSupport() {}

    static List<List<ItemStack>> readIngredientChoices(IRecipeSlotsView slotsView) {
        return slotsView.getSlotViews(RecipeIngredientRole.INPUT).stream()
                .map(slot -> slot.getIngredients(VanillaTypes.ITEM_STACK)
                        .filter(item -> !item.isEmpty())
                        .map(item -> item.copyWithCount(1))
                        .collect(java.util.stream.Collectors.collectingAndThen(
                                java.util.stream.Collectors.toList(), JeiTransferSupport::deduplicate)))
                .toList();
    }

    static IRecipeTransferError availabilityStatus(List<List<ItemStack>> choices, Player player) {
        RecipeViewerTransferHelper.requestNearbyRefreshIfNeeded(player);
        return RecipeViewerTransferHelper.hasIngredients(choices, player)
                ? new CraftFromNearbyAvailable()
                : new CraftFromNearbyMissing();
    }

    private static List<ItemStack> deduplicate(List<ItemStack> candidates) {
        java.util.ArrayList<ItemStack> unique = new java.util.ArrayList<>();
        for (ItemStack candidate : candidates) {
            if (unique.stream().noneMatch(existing -> ItemStack.isSameItemSameComponents(existing, candidate))) {
                unique.add(candidate);
            }
        }
        return List.copyOf(unique);
    }
}
