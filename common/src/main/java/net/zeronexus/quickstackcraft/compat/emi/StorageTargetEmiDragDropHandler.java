package net.zeronexus.quickstackcraft.compat.emi;

import dev.emi.emi.api.EmiDragDropHandler;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;
import net.zeronexus.quickstackcraft.client.QuickStackConfigScreen;

/**
 * Accepts EMI sidebar stacks in the storage whitelist and blacklist panels.
 */
public final class StorageTargetEmiDragDropHandler
        implements EmiDragDropHandler<QuickStackConfigScreen> {

    @Override
    public boolean dropStack(QuickStackConfigScreen screen, EmiIngredient ingredient, int x, int y) {
        ItemStack stack = firstAcceptedStack(screen, ingredient);
        return !stack.isEmpty() && screen.acceptDrop(stack, x, y);
    }

    @Override
    public void render(QuickStackConfigScreen screen, EmiIngredient ingredient, GuiGraphics graphics,
                       int mouseX, int mouseY, float delta) {
        if (firstAcceptedStack(screen, ingredient).isEmpty()) {
            return;
        }
        renderTarget(graphics, screen.dropArea(QuickStackConfigScreen.TargetList.INCLUDED), 0x554F9A62);
        renderTarget(graphics, screen.dropArea(QuickStackConfigScreen.TargetList.EXCLUDED), 0x55B85462);
    }

    private static ItemStack firstAcceptedStack(QuickStackConfigScreen screen, EmiIngredient ingredient) {
        for (EmiStack emiStack : ingredient.getEmiStacks()) {
            ItemStack stack = emiStack.getItemStack();
            if (!stack.isEmpty() && screen.canAcceptDrop(stack)) {
                return stack.copy();
            }
        }
        return ItemStack.EMPTY;
    }

    private static void renderTarget(
            GuiGraphics graphics, QuickStackConfigScreen.DropArea area, int color) {
        graphics.fill(area.x(), area.y(), area.x() + area.width(), area.y() + area.height(), color);
    }
}
