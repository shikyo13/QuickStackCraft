package net.zeronexus.quickstackcraft.compat.jei;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.ingredient.IRecipeSlotView;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.zeronexus.quickstackcraft.compat.RecipeViewerTransferHelper;

import java.util.List;

final class JeiTransferSupport {

    private JeiTransferSupport() {}

    static IRecipeTransferError availabilityStatus(IRecipeSlotsView slotsView, Player player,
                                                   IRecipeTransferHandlerHelper transferHelper) {
        RecipeViewerTransferHelper.requestNearbyRefreshIfNeeded(player);
        List<IRecipeSlotView> inputSlots = slotsView.getSlotViews(RecipeIngredientRole.INPUT);
        List<Integer> missing = RecipeViewerTransferHelper.missingIngredientSlots(
                inputSlots.stream().map(JeiTransferSupport::readIngredientChoices).toList(), player);
        if (missing.isEmpty()) {
            return new CraftFromNearbyAvailable();
        }
        return transferHelper.createUserErrorForMissingSlots(
                Component.translatable("quickstackcraft.jei.craft_nearby_missing"),
                missing.stream().map(inputSlots::get).toList());
    }

    private static List<ItemStack> readIngredientChoices(IRecipeSlotView slot) {
        return deduplicate(slot.getIngredients(VanillaTypes.ITEM_STACK)
                .filter(item -> !item.isEmpty())
                .map(item -> item.copyWithCount(1))
                .toList());
    }

    private static List<ItemStack> deduplicate(List<ItemStack> candidates) {
        java.util.ArrayList<ItemStack> unique = new java.util.ArrayList<>();
        for (ItemStack candidate : candidates) {
            if (unique.stream().noneMatch(existing -> ItemStack.isSameItemSameTags(existing, candidate))) {
                unique.add(candidate);
            }
        }
        return List.copyOf(unique);
    }
}
