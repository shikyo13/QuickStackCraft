package net.zeronexus.quickstackcraft.compat.emi;

import dev.architectury.networking.NetworkManager;
import dev.emi.emi.api.recipe.EmiPlayerInventory;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.VanillaEmiRecipeCategories;
import dev.emi.emi.api.recipe.handler.EmiCraftContext;
import dev.emi.emi.api.recipe.handler.StandardRecipeHandler;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.zeronexus.quickstackcraft.client.NearbyItemsCache;
import net.zeronexus.quickstackcraft.compat.ExternalSlotLocks;
import net.zeronexus.quickstackcraft.compat.RecipeViewerTransferHelper;
import net.zeronexus.quickstackcraft.network.RecipeTransferC2SPacket;

import java.util.ArrayList;
import java.util.List;

abstract class AbstractCraftFromNearbyEmiHandler<T extends AbstractContainerMenu> implements StandardRecipeHandler<T> {

    @Override
    public EmiPlayerInventory getInventory(AbstractContainerScreen<T> screen) {
        Player player = Minecraft.getInstance().player;
        if (player != null) {
            RecipeViewerTransferHelper.requestNearbyRefreshIfNeeded(player);
        }

        long externalLockedMask = ExternalSlotLocks.snapshot();
        List<EmiStack> stacks = new ArrayList<>();
        for (Slot slot : getInputSources(screen.getMenu())) {
            int inventorySlot = slot.getContainerSlot();
            if (slot.container instanceof Inventory
                    && inventorySlot >= 0 && inventorySlot < 36
                    && RecipeViewerTransferHelper.isProtectedInventorySlot(
                            inventorySlot, externalLockedMask)) {
                continue;
            }
            stacks.add(EmiStack.of(slot.getItem()));
        }
        NearbyItemsCache.forEachItem((item, count) -> stacks.add(EmiStack.of(new ItemStack(item, count))));
        return new EmiPlayerInventory(stacks);
    }

    @Override
    public boolean supportsRecipe(EmiRecipe recipe) {
        ResourceLocation id = recipe.getId();
        return recipe.getCategory() == VanillaEmiRecipeCategories.CRAFTING
                && recipe.supportsRecipeTree()
                && id != null
                && !id.getPath().startsWith("/");
    }

    @Override
    public boolean canCraft(EmiRecipe recipe, EmiCraftContext<T> context) {
        return StandardRecipeHandler.super.canCraft(recipe, context);
    }

    @Override
    public boolean craft(EmiRecipe recipe, EmiCraftContext<T> context) {
        Player player = Minecraft.getInstance().player;
        if (player == null) {
            return false;
        }

        ResourceLocation id = recipe.getId();
        if (id == null || id.getPath().startsWith("/")) {
            return false;
        }

        NetworkManager.sendToServer(new RecipeTransferC2SPacket(
                context.getScreenHandler().containerId,
                id,
                context.getAmount() > 1,
                ExternalSlotLocks.snapshot()));
        return true;
    }
}
