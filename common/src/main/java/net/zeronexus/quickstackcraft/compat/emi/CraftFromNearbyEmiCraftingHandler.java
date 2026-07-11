package net.zeronexus.quickstackcraft.compat.emi;

import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.inventory.Slot;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class CraftFromNearbyEmiCraftingHandler extends AbstractCraftFromNearbyEmiHandler<CraftingMenu> {

    @Override
    public List<Slot> getInputSources(CraftingMenu handler) {
        List<Slot> slots = new ArrayList<>();
        for (int i = 1; i < 10; i++) {
            slots.add(handler.getSlot(i));
        }
        for (int i = 10; i < 46; i++) {
            slots.add(handler.getSlot(i));
        }
        return slots;
    }

    @Override
    public List<Slot> getCraftingSlots(CraftingMenu handler) {
        List<Slot> slots = new ArrayList<>();
        for (int i = 1; i < 10; i++) {
            slots.add(handler.getSlot(i));
        }
        return slots;
    }

    @Override
    public @Nullable Slot getOutputSlot(CraftingMenu handler) {
        return handler.getSlot(0);
    }
}
