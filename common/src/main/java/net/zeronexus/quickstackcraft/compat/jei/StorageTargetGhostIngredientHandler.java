package net.zeronexus.quickstackcraft.compat.jei;

import mezz.jei.api.gui.handlers.IGhostIngredientHandler;
import mezz.jei.api.ingredients.ITypedIngredient;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.world.item.ItemStack;
import net.zeronexus.quickstackcraft.client.QuickStackConfigScreen;

import java.util.List;
import java.util.Optional;

/**
 * Exposes the storage include/exclude panels as JEI ghost ingredient targets.
 */
public final class StorageTargetGhostIngredientHandler
        implements IGhostIngredientHandler<QuickStackConfigScreen> {

    @Override
    public <I> List<Target<I>> getTargetsTyped(
            QuickStackConfigScreen screen, ITypedIngredient<I> ingredient, boolean doStart) {
        Optional<ItemStack> stack = ingredient.getItemStack();
        if (stack.isEmpty() || !screen.canAcceptDrop(stack.get())) {
            return List.of();
        }

        ItemStack droppedStack = stack.get().copy();
        java.util.ArrayList<Target<I>> targets = new java.util.ArrayList<>();
        for (QuickStackConfigScreen.TargetList targetList : QuickStackConfigScreen.TargetList.values()) {
            if (screen.isTargetListVisible(targetList)) {
                targets.add(target(screen, droppedStack, targetList));
            }
        }
        return targets;
    }

    private static <I> Target<I> target(
            QuickStackConfigScreen screen,
            ItemStack stack,
            QuickStackConfigScreen.TargetList targetList) {
        QuickStackConfigScreen.DropArea area = screen.dropArea(targetList);
        return new Target<>() {
            @Override
            public Rect2i getArea() {
                return new Rect2i(area.x(), area.y(), area.width(), area.height());
            }

            @Override
            public void accept(I ignored) {
                screen.acceptDrop(stack, targetList);
            }
        };
    }

    @Override
    public void onComplete() {
    }
}
