package net.zeronexus.quickstackcraft.client.tutorial;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.zeronexus.quickstackcraft.client.UiIcon;
import net.zeronexus.quickstackcraft.client.ToolbarPreferences;

import java.util.List;

public interface TutorialRenderContext {

    int DESIGN_WIDTH = 480;
    int DESIGN_HEIGHT = 180;

    record Bounds(int x, int y, int width, int height) {}
    record SlotItem(int slot, ItemStack stack, boolean locked) {}
    enum InventoryAction { QUICK_STACK, RESTOCK, DUMP }

    Bounds viewport();

    double transition(double time, double start, double end);

    void beginScene(double panX, double panY, double zoom);

    void endScene();

    void renderWorldBackdrop();

    void renderBlock(ItemStack stack, int centerX, int centerY, float scale, int outlineColor);

    void renderChest(int centerX, int groundY, float size, float openness, int outlineColor);

    void renderChestGui(int x, int y, float scale, List<SlotItem> items);

    void renderInventoryGui(int x, int y, float scale, List<SlotItem> items);

    default void renderInventoryGui(int x, int y, float scale, List<SlotItem> items, ToolbarPreferences toolbar) {
        renderInventoryGui(x, y, scale, items);
    }

    default void renderButtonSettings(int x, int y, boolean visible, int offsetX, int offsetY) {}

    void renderCraftingGui(int x, int y, float scale, List<SlotItem> gridItems, ItemStack result);

    void renderRecipeBook(int x, int y, float scale, ItemStack recipe, boolean highlighted);

    void renderItem(ItemStack stack, double centerX, double centerY, float scale);

    void renderCursor(double x, double y, boolean pressed);

    void renderInventoryToolbar(int x, int y, float scale, InventoryAction action, boolean pressed);

    void renderBadge(int centerX, int y, Component text, int color, UiIcon icon);
}
