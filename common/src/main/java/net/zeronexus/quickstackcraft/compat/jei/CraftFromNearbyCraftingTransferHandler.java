package net.zeronexus.quickstackcraft.compat.jei;

import dev.architectury.networking.NetworkManager;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.zeronexus.quickstackcraft.compat.ExternalSlotLocks;
import net.zeronexus.quickstackcraft.network.RecipeTransferC2SPacket;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * JEI transfer handler for CraftingMenu (3x3 crafting table).
 * Replaces JEI's built-in CraftingRecipeTransferHandler so the [+] button
 * checks nearby containers for ingredient availability.
 */
public class CraftFromNearbyCraftingTransferHandler implements IRecipeTransferHandler<CraftingMenu, RecipeHolder<CraftingRecipe>> {

    private static final Logger LOGGER = LoggerFactory.getLogger("QuickStackCraft-JEI");

    private final IRecipeTransferHandlerHelper transferHelper;

    public CraftFromNearbyCraftingTransferHandler(IRecipeTransferHandlerHelper transferHelper) {
        this.transferHelper = transferHelper;
    }

    @Override
    public Class<CraftingMenu> getContainerClass() {
        return CraftingMenu.class;
    }

    @Override
    public Optional<MenuType<CraftingMenu>> getMenuType() {
        return Optional.of(MenuType.CRAFTING);
    }

    @Override
    public RecipeType<RecipeHolder<CraftingRecipe>> getRecipeType() {
        return RecipeTypes.CRAFTING;
    }

    @Override
    public @Nullable IRecipeTransferError transferRecipe(
            CraftingMenu menu, RecipeHolder<CraftingRecipe> recipe,
            IRecipeSlotsView recipeSlots, Player player,
            boolean maxTransfer, boolean doTransfer) {

        if (!doTransfer) {
            return JeiTransferSupport.availabilityStatus(recipeSlots, player, transferHelper);
        }

        LOGGER.info("[QuickStackCraft] Craft from nearby: recipe={}", recipe.id());
        NetworkManager.sendToServer(new RecipeTransferC2SPacket(
                menu.containerId, recipe.id(), maxTransfer,
                ExternalSlotLocks.snapshot()));
        return null;
    }
}
