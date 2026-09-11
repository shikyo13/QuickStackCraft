package net.zeronexus.quickstackcraft.compat.jei;

import net.zeronexus.quickstackcraft.network.ModNetworking;

import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.zeronexus.quickstackcraft.compat.ExternalSlotLocks;
import net.zeronexus.quickstackcraft.network.RecipeTransferC2SPacket;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * JEI transfer handler for InventoryMenu (2x2 player inventory crafting grid).
 * Replaces JEI's built-in PlayerRecipeTransferHandler so the [+] button
 * checks nearby containers for ingredient availability.
 */
public class CraftFromNearbyTransferHandler implements IRecipeTransferHandler<InventoryMenu, CraftingRecipe> {

    private static final Logger LOGGER = LoggerFactory.getLogger("QuickStackCraft-JEI");

    @Override
    public Class<InventoryMenu> getContainerClass() {
        return InventoryMenu.class;
    }

    @Override
    public Optional<MenuType<InventoryMenu>> getMenuType() {
        return Optional.empty();
    }

    @Override
    public RecipeType<CraftingRecipe> getRecipeType() {
        return RecipeTypes.CRAFTING;
    }

    @Override
    public @Nullable IRecipeTransferError transferRecipe(
            InventoryMenu menu, CraftingRecipe recipe,
            IRecipeSlotsView recipeSlots, Player player,
            boolean maxTransfer, boolean doTransfer) {

        var ingredients = JeiTransferSupport.readIngredientChoices(recipeSlots);

        if (!doTransfer) {
            return JeiTransferSupport.availabilityStatus(ingredients, player);
        }

        LOGGER.info("[QuickStackCraft] Craft from nearby (inventory): recipe={}", recipe.getId());
        ModNetworking.sendToServer(new RecipeTransferC2SPacket(
                menu.containerId, recipe.getId(), maxTransfer,
                ExternalSlotLocks.snapshot()));
        return null;
    }
}
