package net.zeronexus.quickstackcraft.compat.emi;

import dev.emi.emi.api.recipe.EmiCraftingRecipe;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.handler.EmiCraftContext;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class CraftFromNearbyEmiInventoryHandler extends AbstractCraftFromNearbyEmiHandler<InventoryMenu> {

    @Override
    public List<Slot> getInputSources(InventoryMenu handler) {
        List<Slot> slots = new ArrayList<>();
        for (int i = 1; i < 5; i++) {
            slots.add(handler.getSlot(i));
        }
        for (int i = 9; i < 45; i++) {
            slots.add(handler.getSlot(i));
        }
        return slots;
    }

    @Override
    public List<Slot> getCraftingSlots(InventoryMenu handler) {
        List<Slot> slots = new ArrayList<>();
        slots.add(handler.getSlot(1));
        slots.add(handler.getSlot(2));
        slots.add(null);
        slots.add(handler.getSlot(3));
        slots.add(handler.getSlot(4));
        slots.add(null);
        slots.add(null);
        slots.add(null);
        slots.add(null);
        return slots;
    }

    @Override
    public List<Slot> getCraftingSlots(EmiRecipe recipe, InventoryMenu handler) {
        if (recipe instanceof EmiCraftingRecipe crafting && crafting.shapeless) {
            List<Slot> slots = new ArrayList<>();
            for (int i = 1; i < 5; i++) {
                slots.add(handler.getSlot(i));
            }
            return slots;
        }
        return getCraftingSlots(handler);
    }

    @Override
    public @Nullable Slot getOutputSlot(InventoryMenu handler) {
        return handler.getSlot(0);
    }

    @Override
    public boolean supportsRecipe(EmiRecipe recipe) {
        if (!super.supportsRecipe(recipe)) {
            return false;
        }
        return !(recipe instanceof EmiCraftingRecipe crafting) || crafting.canFit(2, 2);
    }

    @Override
    public boolean canCraft(EmiRecipe recipe, EmiCraftContext<InventoryMenu> context) {
        if (recipe instanceof EmiCraftingRecipe crafting && !crafting.canFit(2, 2)) {
            return false;
        }
        return super.canCraft(recipe, context);
    }
}
