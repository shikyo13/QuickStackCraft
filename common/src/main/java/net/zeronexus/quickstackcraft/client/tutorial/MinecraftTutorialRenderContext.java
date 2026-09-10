package net.zeronexus.quickstackcraft.client.tutorial;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.zeronexus.quickstackcraft.client.InventoryToolbarLayout;
import net.zeronexus.quickstackcraft.client.UiIcon;
import net.zeronexus.quickstackcraft.client.ToolbarPreferences;
import net.zeronexus.quickstackcraft.client.UiIconButton;

import java.util.List;

final class MinecraftTutorialRenderContext implements TutorialRenderContext {

    private static final ResourceLocation CHEST_GUI =
            ResourceLocation.withDefaultNamespace("textures/gui/container/generic_54.png");
    private static final ResourceLocation INVENTORY_GUI =
            ResourceLocation.withDefaultNamespace("textures/gui/container/inventory.png");
    private static final ResourceLocation CRAFTING_GUI =
            ResourceLocation.withDefaultNamespace("textures/gui/container/crafting_table.png");
    private static final ResourceLocation TUTORIAL_BACKDROP =
            ResourceLocation.fromNamespaceAndPath(
                    "quickstackcraft", "textures/gui/tutorial_forest.png");
    private static final int BACKDROP_WIDTH = 3440;
    private static final int BACKDROP_HEIGHT = 1440;
    private static final int BACKDROP_CENTER_Y = 620;
    private static final ResourceLocation RECIPE_BOOK_GUI =
            ResourceLocation.withDefaultNamespace("textures/gui/recipe_book.png");
    private static final ResourceLocation RECIPE_BOOK_BUTTON =
            ResourceLocation.withDefaultNamespace("recipe_book/button");
    private static final ResourceLocation RECIPE_SLOT =
            ResourceLocation.withDefaultNamespace("recipe_book/slot_craftable");
    private static final ResourceLocation RECIPE_TAB =
            ResourceLocation.withDefaultNamespace("recipe_book/tab");
    private static final ResourceLocation RECIPE_TAB_SELECTED =
            ResourceLocation.withDefaultNamespace("recipe_book/tab_selected");
    private static final ResourceLocation FILTER_DISABLED =
            ResourceLocation.withDefaultNamespace("recipe_book/filter_disabled");
    private static final Button QUICK_STACK_BUTTON =
            textToolbarButton(InventoryToolbarLayout.QUICK_STACK_LABEL, 0);
    private static final Button RESTOCK_BUTTON =
            textToolbarButton(InventoryToolbarLayout.RESTOCK_LABEL, 1);
    private static final Button DUMP_BUTTON =
            textToolbarButton(InventoryToolbarLayout.DUMP_LABEL, 2);
    private static final UiIconButton SETTINGS_BUTTON = new UiIconButton(
            InventoryToolbarLayout.buttonX(0, 3), InventoryToolbarLayout.OFFSET_Y,
            InventoryToolbarLayout.BUTTON_SIZE, UiIcon.SETTINGS, Component.empty(), button -> {});
    private static EditBox recipeSearch;

    private final GuiGraphics graphics;
    private final Minecraft minecraft;
    private final Bounds viewport;
    private double sceneFit = 1.0D;
    private double scenePanX;
    private double sceneZoom = 1.0D;

    MinecraftTutorialRenderContext(GuiGraphics graphics, Bounds viewport) {
        this.graphics = graphics;
        this.minecraft = Minecraft.getInstance();
        this.viewport = viewport;
    }

    @Override
    public Bounds viewport() {
        return viewport;
    }

    @Override
    public double transition(double time, double start, double end) {
        return TutorialTimeline.transition(time, start, end);
    }

    @Override
    public void beginScene(double panX, double panY, double zoom) {
        graphics.flush();
        graphics.enableScissor(viewport.x(), viewport.y(),
                viewport.x() + viewport.width(), viewport.y() + viewport.height());
        double fit = Math.min(viewport.width() / (double) DESIGN_WIDTH,
                viewport.height() / (double) DESIGN_HEIGHT);
        sceneFit = fit;
        scenePanX = panX;
        sceneZoom = zoom;
        PoseStack pose = graphics.pose();
        pose.pushPose();
        pose.translate(viewport.x() + viewport.width() / 2.0D,
                viewport.y() + viewport.height() / 2.0D, 0.0D);
        pose.scale((float) (fit * zoom), (float) (fit * zoom), 1.0F);
        pose.translate(-DESIGN_WIDTH / 2.0D + panX, -DESIGN_HEIGHT / 2.0D + panY, 0.0D);
    }

    @Override
    public void endScene() {
        graphics.flush();
        graphics.pose().popPose();
        graphics.disableScissor();
        TutorialGraphicsBridge.resetDepth(graphics);
    }

    @Override
    public void renderWorldBackdrop() {
        int visibleWidth = Math.max(DESIGN_WIDTH,
                (int) Math.ceil(viewport.width() / (sceneFit * sceneZoom)) + 4);
        int left = (int) Math.floor(DESIGN_WIDTH / 2.0D - scenePanX - visibleWidth / 2.0D);
        int sourceHeight = Math.min(BACKDROP_HEIGHT,
                Math.max(1, (int) Math.round(BACKDROP_WIDTH
                        * (DESIGN_HEIGHT / (double) visibleWidth))));
        int sourceY = Math.max(0,
                Math.min(BACKDROP_HEIGHT - sourceHeight, BACKDROP_CENTER_Y - sourceHeight / 2));

        TutorialGraphicsBridge.blitRegionScaled(
                graphics,
                TUTORIAL_BACKDROP,
                left,
                0,
                visibleWidth,
                DESIGN_HEIGHT,
                0,
                sourceY,
                BACKDROP_WIDTH,
                sourceHeight,
                BACKDROP_WIDTH,
                BACKDROP_HEIGHT);
        graphics.fill(left, 0, left + visibleWidth, DESIGN_HEIGHT, 0x38000000);
    }

    @Override
    public void renderBlock(ItemStack stack, int centerX, int centerY, float scale, int outlineColor) {
        PoseStack pose = graphics.pose();
        pose.pushPose();
        pose.translate(centerX, centerY, 100.0F);
        pose.scale(scale, scale, 1.0F);
        graphics.renderItem(stack, -8, -8);
        pose.popPose();
        TutorialGraphicsBridge.resetDepth(graphics);
        if (outlineColor != 0) {
            int left = (int) Math.floor(centerX - 8.0F * scale);
            int top = (int) Math.floor(centerY - 8.0F * scale);
            int right = (int) Math.ceil(centerX + 8.0F * scale);
            int bottom = (int) Math.ceil(centerY + 8.0F * scale);
            graphics.renderOutline(left, top, right - left, bottom - top, outlineColor);
        }
    }

    @Override
    public void renderChest(int centerX, int groundY, float size, float openness, int outlineColor) {
        Bounds bounds = TutorialGraphicsBridge.renderChest(
                graphics, centerX, groundY, size, openness);
        if (outlineColor != 0) {
            graphics.renderOutline(
                    bounds.x(), bounds.y(), bounds.width(), bounds.height(), outlineColor);
        }
    }

    @Override
    public void renderChestGui(int x, int y, float scale, List<SlotItem> items) {
        withGuiScale(x, y, scale, () -> {
            TutorialGraphicsBridge.blit(graphics, CHEST_GUI,
                    0, 0, 0, 0, 176, 72, 256, 256);
            TutorialGraphicsBridge.blit(graphics, CHEST_GUI,
                    0, 72, 0, 126, 176, 96, 256, 256);
            graphics.drawString(minecraft.font, Component.translatable("container.chest"),
                    8, 6, 0xFF404040, false);
            graphics.drawString(minecraft.font, Component.translatable("container.inventory"),
                    8, 73, 0xFF404040, false);
            for (SlotItem item : items) {
                int slotX = 8 + (item.slot() % 9) * 18;
                int slotY = 18 + (item.slot() / 9) * 18;
                renderSlotItem(item, slotX, slotY);
            }
        });
    }

    @Override
    public void renderInventoryGui(int x, int y, float scale, List<SlotItem> items) {
        renderInventoryGui(x, y, scale, items, ToolbarPreferences.DEFAULT);
    }

    @Override
    public void renderInventoryGui(int x, int y, float scale, List<SlotItem> items, ToolbarPreferences toolbar) {
        renderGuiTexture(INVENTORY_GUI, x, y, scale, 176, 166);
        withGuiScale(x, y, scale, () -> {
            graphics.drawString(minecraft.font, Component.translatable("container.crafting"),
                    97, 8, 0xFF404040, false);
            for (SlotItem item : items) {
                int slot = item.slot();
                int slotX;
                int slotY;
                if (slot >= 0 && slot < 9) {
                    slotX = 8 + slot * 18;
                    slotY = 142;
                } else {
                    int main = Math.max(0, slot - 9);
                    slotX = 8 + (main % 9) * 18;
                    slotY = 84 + (main / 9) * 18;
                }
                renderSlotItem(item, slotX, slotY);
            }
            if (toolbar.visible()) {
                graphics.pose().pushPose();
                graphics.pose().translate(toolbar.offsetX(), toolbar.offsetY(), 0);
                renderInventoryToolbarLocal(null, false);
                graphics.pose().popPose();
            }
        });
    }

    @Override
    public void renderButtonSettings(int x, int y, boolean visible, int offsetX, int offsetY) {
        withGuiScale(x, y, 0.75F, () -> {
            graphics.fill(0, 0, 228, 168, 0xF0111315);
            graphics.renderOutline(0, 0, 228, 168, 0xFF7A7A7A);
            graphics.drawCenteredString(minecraft.font,
                    Component.translatable("quickstackcraft.config.section.buttons"), 114, 8, 0xFFFFFFFF);
            demoButton(8, 25, 104, "quickstackcraft.config.inventory_buttons", false);
            demoButton(114, 25, 106, "quickstackcraft.config.storage_buttons", true);
            demoButton(8, 49, 212, visible ? "quickstackcraft.config.buttons_shown"
                    : "quickstackcraft.config.buttons_hidden", true);
            graphics.drawString(minecraft.font, Component.translatable("quickstackcraft.config.button_offset_x"),
                    8, 82, 0xFFE4E4E4, false);
            graphics.drawString(minecraft.font, Component.translatable("quickstackcraft.config.button_offset_y"),
                    8, 107, 0xFFE4E4E4, false);
            for (int row = 0; row < 2; row++) {
                int top = 76 + row * 25;
                graphics.fill(161, top, 220, top + 20, 0xFF000000);
                graphics.renderOutline(161, top, 59, 20, 0xFFA0A0A0);
                graphics.drawString(minecraft.font, Integer.toString(row == 0 ? offsetX : offsetY),
                        165, top + 6, 0xFFE4E4E4, false);
            }
            demoButton(8, 132, 212, "quickstackcraft.config.reset_button_position", true);
        });
    }

    private void demoButton(int x, int y, int width, String key, boolean active) {
        Button button = Button.builder(Component.translatable(key), ignored -> {}).bounds(x, y, width, 20).build();
        button.active = active;
        button.render(graphics, -1000, -1000, 0);
    }

    @Override
    public void renderCraftingGui(
            int x, int y, float scale, List<SlotItem> gridItems, ItemStack result) {
        renderGuiTexture(CRAFTING_GUI, x, y, scale, 176, 166);
        withGuiScale(x, y, scale, () -> {
            graphics.drawString(minecraft.font, Component.translatable("container.crafting"),
                    29, 6, 0xFF404040, false);
            graphics.drawString(minecraft.font, Component.translatable("container.inventory"),
                    8, 72, 0xFF404040, false);
            TutorialGraphicsBridge.blitSprite(graphics, RECIPE_BOOK_BUTTON, 5, 31, 20, 18);
            for (SlotItem item : gridItems) {
                int slotX = 30 + (item.slot() % 3) * 18;
                int slotY = 17 + (item.slot() / 3) * 18;
                renderSlotItem(item, slotX, slotY);
            }
            if (!result.isEmpty()) {
                graphics.renderItem(result, 124, 35);
            }
        });
    }

    @Override
    public void renderRecipeBook(
            int x, int y, float scale, ItemStack recipe, boolean highlighted) {
        withGuiScale(x, y, scale, () -> {
            TutorialGraphicsBridge.blit(
                    graphics, RECIPE_BOOK_GUI, 0, 0, 1, 1, 147, 166, 256, 256);
            recipeSearch().render(graphics, -1000, -1000, 0.0F);
            TutorialGraphicsBridge.blitSprite(graphics, FILTER_DISABLED, 110, 12, 26, 16);
            renderRecipeTab(3, true, new ItemStack(Items.COMPASS));
            renderRecipeTab(30, false, new ItemStack(Items.IRON_AXE));
            renderRecipeTab(57, false, new ItemStack(Items.BRICKS));
            renderRecipeTab(84, false, new ItemStack(Items.LAVA_BUCKET));
            renderRecipeTab(111, false, new ItemStack(Items.REDSTONE));

            List<ItemStack> results = List.of(
                    new ItemStack(Items.WOODEN_PICKAXE),
                    new ItemStack(Items.STONE_PICKAXE),
                    recipe,
                    new ItemStack(Items.GOLDEN_PICKAXE),
                    new ItemStack(Items.DIAMOND_PICKAXE));
            for (int index = 0; index < results.size(); index++) {
                int slotX = 11 + (index % 4) * 29;
                int slotY = 31 + (index / 4) * 29;
                TutorialGraphicsBridge.blitSprite(graphics, RECIPE_SLOT, slotX, slotY, 25, 25);
                graphics.renderItem(results.get(index), slotX + 4, slotY + 4);
                if (highlighted && index == 2) {
                    graphics.renderOutline(slotX, slotY, 25, 25, 0xFFFFD34E);
                }
            }
            graphics.drawCenteredString(minecraft.font, "1/1", 73, 142, 0xFF404040);
        });
    }

    @Override
    public void renderItem(ItemStack stack, double centerX, double centerY, float scale) {
        PoseStack pose = graphics.pose();
        pose.pushPose();
        pose.translate(centerX, centerY, 220.0D);
        pose.scale(scale, scale, 1.0F);
        graphics.renderItem(stack, -8, -8);
        pose.popPose();
        TutorialGraphicsBridge.resetDepth(graphics);
    }

    @Override
    public void renderCursor(double x, double y, boolean pressed) {
        int px = (int) Math.round(x);
        int py = (int) Math.round(y);
        int shadow = 0xFF202020;
        int white = 0xFFFFFFFF;
        graphics.fill(px, py, px + 2, py + 12, shadow);
        graphics.fill(px + 2, py + 2, px + 4, py + 10, shadow);
        graphics.fill(px + 4, py + 4, px + 7, py + 8, shadow);
        graphics.fill(px + 1, py + 1, px + 2, py + 10, white);
        graphics.fill(px + 2, py + 2, px + 3, py + 8, white);
        graphics.fill(px + 3, py + 4, px + 5, py + 7, white);
        if (pressed) {
            graphics.renderOutline(px - 4, py - 4, 18, 18, 0xFFFFD36A);
        }
    }

    @Override
    public void renderInventoryToolbar(
            int x, int y, float scale, InventoryAction action, boolean pressed) {
        withGuiScale(x, y, scale, () -> renderInventoryToolbarLocal(action, pressed));
    }

    @Override
    public void renderBadge(int centerX, int y, Component text, int color, UiIcon icon) {
        Font font = minecraft.font;
        int width = font.width(text) + 26;
        int left = centerX - width / 2;
        graphics.fill(left, y, left + width, y + 18, 0xE0181A1C);
        graphics.renderOutline(left, y, width, 18, color);
        icon.render(graphics, left + 3, y + 3, color);
        graphics.drawString(font, text, left + 19, y + 5, 0xFFFFFFFF, false);
    }

    private void renderGuiTexture(ResourceLocation texture, int x, int y, float scale, int width, int height) {
        withGuiScale(x, y, scale, () -> TutorialGraphicsBridge.blit(
                graphics, texture, 0, 0, 0, 0, width, height, 256, 256));
    }

    private void withGuiScale(int x, int y, float scale, Runnable action) {
        PoseStack pose = graphics.pose();
        pose.pushPose();
        pose.translate(x, y, 150.0F);
        pose.scale(scale, scale, 1.0F);
        action.run();
        pose.popPose();
    }

    private void renderSlotItem(SlotItem item, int x, int y) {
        graphics.renderItem(item.stack(), x, y);
        graphics.renderItemDecorations(minecraft.font, item.stack(), x, y);
        if (item.locked()) {
            graphics.renderOutline(x, y, 16, 16, 0xFFF2C14E);
            UiIcon.renderLock(graphics, x + 9, y + 1, 0xFFF9D36A);
        }
    }

    private EditBox recipeSearch() {
        if (recipeSearch == null) {
            recipeSearch = new EditBox(minecraft.font, 25, 13, 81, 14,
                    Component.translatable("itemGroup.search"));
            recipeSearch.setMaxLength(50);
            recipeSearch.setTextColor(0xFFFFFF);
            recipeSearch.setValue("pickaxe");
            recipeSearch.setFocused(false);
        }
        return recipeSearch;
    }

    private void renderRecipeTab(int y, boolean selected, ItemStack icon) {
        TutorialGraphicsBridge.blitSprite(
                graphics, selected ? RECIPE_TAB_SELECTED : RECIPE_TAB, -28, y, 35, 27);
        graphics.renderItem(icon, -23, y + 5);
    }

    private void renderInventoryToolbarLocal(InventoryAction action, boolean pressed) {
        renderToolbarWidget(QUICK_STACK_BUTTON, pressed && action == InventoryAction.QUICK_STACK);
        renderToolbarWidget(RESTOCK_BUTTON, pressed && action == InventoryAction.RESTOCK);
        renderToolbarWidget(DUMP_BUTTON, pressed && action == InventoryAction.DUMP);
        renderToolbarWidget(SETTINGS_BUTTON, false);
    }

    private void renderToolbarWidget(Button button, boolean hovered) {
        int mouseX = hovered ? button.getX() + button.getWidth() / 2 : -1000;
        int mouseY = hovered ? button.getY() + button.getHeight() / 2 : -1000;
        button.render(graphics, mouseX, mouseY, 0.0F);
    }

    private static Button textToolbarButton(String text, int index) {
        return Button.builder(Component.literal(text), button -> {})
                .bounds(InventoryToolbarLayout.buttonX(0, index), InventoryToolbarLayout.OFFSET_Y,
                        InventoryToolbarLayout.BUTTON_SIZE, InventoryToolbarLayout.BUTTON_SIZE)
                .build();
    }
}
