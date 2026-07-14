package net.zeronexus.quickstackcraft.client.tutorial;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.zeronexus.quickstackcraft.client.InventoryToolbarLayout;
import net.zeronexus.quickstackcraft.client.StorageHighlightPalette;
import net.zeronexus.quickstackcraft.client.UiIcon;
import net.zeronexus.quickstackcraft.logic.StorageListState;

import java.util.ArrayList;
import java.util.List;

public final class TutorialScenes {

    private static final TutorialScene QUICK_STACK = new QuickStackScene();
    private static final TutorialScene RESTOCK = new RestockScene();
    private static final TutorialScene DUMP_AND_LOCKS = new DumpAndLocksScene();
    private static final TutorialScene WHITELIST_BLACKLIST = new WhitelistBlacklistScene();
    private static final TutorialScene STORAGE_PREVIEW = new StoragePreviewScene();
    private static final List<TutorialScene> SCENES = List.of(
            new CombinedScene("inventory_management", QUICK_STACK, RESTOCK, DUMP_AND_LOCKS),
            new CraftNearbyScene(),
            new CombinedScene(
                    "whitelist_blacklist_preview", WHITELIST_BLACKLIST, STORAGE_PREVIEW));

    private TutorialScenes() {}

    public static List<TutorialScene> all() {
        return SCENES;
    }

    private abstract static class BaseScene implements TutorialScene {
        private final String key;
        private final double duration;

        private BaseScene(String key, double duration) {
            this.key = key;
            this.duration = duration;
        }

        @Override
        public Component title() {
            return Component.translatable("quickstackcraft.tutorial." + key + ".title");
        }

        @Override
        public double durationSeconds() {
            return duration;
        }

        protected Component caption(int step) {
            return Component.translatable("quickstackcraft.tutorial." + key + ".caption." + step);
        }

        protected static TutorialRenderContext.SlotItem slot(int slot, ItemStack stack) {
            return new TutorialRenderContext.SlotItem(slot, stack, false);
        }

        protected static TutorialRenderContext.SlotItem locked(int slot, ItemStack stack) {
            return new TutorialRenderContext.SlotItem(slot, stack, true);
        }

        protected static double lerp(double from, double to, double progress) {
            return TutorialTimeline.lerp(from, to, progress);
        }

        protected static double curve(
                double start, double control1, double control2, double end, double progress) {
            return TutorialTimeline.cubicBezier(start, control1, control2, end, progress);
        }
    }

    private static final class CombinedScene extends BaseScene {
        private final List<TutorialScene> segments;

        private CombinedScene(String key, TutorialScene... segments) {
            super(key, totalDuration(segments));
            this.segments = List.of(segments);
        }

        @Override
        public Component caption(double time) {
            int index = segmentIndex(time);
            return segments.get(index).caption(localTime(time, index));
        }

        @Override
        public void render(TutorialRenderContext context, double time) {
            int index = segmentIndex(time);
            segments.get(index).render(context, localTime(time, index));
        }

        private int segmentIndex(double time) {
            double remaining = Math.max(0.0D, time);
            for (int index = 0; index < segments.size() - 1; index++) {
                double duration = segments.get(index).durationSeconds();
                if (remaining < duration) {
                    return index;
                }
                remaining -= duration;
            }
            return segments.size() - 1;
        }

        private double localTime(double time, int segmentIndex) {
            double local = Math.max(0.0D, time);
            for (int index = 0; index < segmentIndex; index++) {
                local -= segments.get(index).durationSeconds();
            }
            return Math.max(0.0D, local);
        }

        private static double totalDuration(TutorialScene[] segments) {
            double total = 0.0D;
            for (TutorialScene segment : segments) {
                total += segment.durationSeconds();
            }
            return total;
        }
    }

    private static final class QuickStackScene extends BaseScene {
        private QuickStackScene() {
            super("quick_stack", 18.0D);
        }

        @Override
        public Component caption(double time) {
            if (time < 4.5D) return caption(1);
            if (time < 9.0D) return caption(2);
            if (time < 13.5D) return caption(3);
            return caption(4);
        }

        @Override
        public void render(TutorialRenderContext context, double time) {
            double focus = context.transition(time, 5.5D, 7.5D);
            double zoom = lerp(1.0D, 1.07D, focus);
            context.beginScene(lerp(0.0D, -8.0D, focus), 0.0D, zoom);
            context.renderWorldBackdrop();

            float chestOpen = (float) (time < 15.5D
                    ? context.transition(time, 0.8D, 2.0D)
                    : 1.0D - context.transition(time, 15.5D, 17.0D));
            int chestOutline = time >= 10.5D && time < 15.5D
                    ? StorageHighlightPalette.destinationArgb() : 0;
            context.renderChest(392, 144, 44.0F, chestOpen, chestOutline);

            if (time < 4.5D) {
                context.renderChestGui(277, 4, 0.52F, List.of(
                        slot(0, new ItemStack(Items.COBBLESTONE, 24)),
                        slot(1, new ItemStack(Items.OAK_PLANKS, 18))));
            } else {
                boolean transferred = time >= 9.0D;
                List<TutorialRenderContext.SlotItem> inventory = transferred
                        ? List.of(
                                locked(12, new ItemStack(Items.DIAMOND_PICKAXE)),
                                slot(13, new ItemStack(Items.APPLE, 3)))
                        : List.of(
                                slot(9, new ItemStack(Items.COBBLESTONE, 32)),
                                slot(10, new ItemStack(Items.OAK_PLANKS, 16)),
                                locked(12, new ItemStack(Items.DIAMOND_PICKAXE)),
                                slot(13, new ItemStack(Items.APPLE, 3)));
                int inventoryX = 142;
                int inventoryY = 8;
                float inventoryScale = 0.72F;
                context.renderInventoryGui(inventoryX, inventoryY, inventoryScale, inventory);
                boolean pressed = time >= 7.5D && time < 8.2D;
                context.renderInventoryToolbar(inventoryX, inventoryY, inventoryScale,
                        TutorialRenderContext.InventoryAction.QUICK_STACK, pressed);
                double cursorMove = context.transition(time, 6.0D, 7.5D);
                context.renderCursor(
                        lerp(224, InventoryToolbarLayout.scaledButtonCenterX(
                                inventoryX, inventoryScale, 0), cursorMove),
                        lerp(31, InventoryToolbarLayout.scaledButtonCenterY(
                                inventoryY, inventoryScale), cursorMove),
                        pressed);

                double travel = context.transition(time, 9.0D, 12.0D);
                if (travel > 0.0D && travel < 1.0D) {
                    context.renderItem(new ItemStack(Items.COBBLESTONE, 32),
                            curve(154, 154, 320, 382, travel),
                            curve(74, 138, 142, 116, travel), 1.0F);
                    context.renderItem(new ItemStack(Items.OAK_PLANKS, 16),
                            curve(167, 167, 328, 390, travel),
                            curve(74, 142, 146, 124, travel), 1.0F);
                }
            }
            context.endScene();
        }
    }

    private static final class RestockScene extends BaseScene {
        private RestockScene() {
            super("restock", 18.0D);
        }

        @Override
        public Component caption(double time) {
            if (time < 4.5D) return caption(1);
            if (time < 9.0D) return caption(2);
            if (time < 13.5D) return caption(3);
            return caption(4);
        }

        @Override
        public void render(TutorialRenderContext context, double time) {
            double focus = context.transition(time, 5.5D, 7.5D);
            context.beginScene(lerp(0.0D, -8.0D, focus), 0.0D, lerp(1.0D, 1.07D, focus));
            context.renderWorldBackdrop();

            int sourceOutline = time >= 8.5D && time < 16.0D
                    ? StorageHighlightPalette.sourceArgb() : 0;
            float chestOpen = (float) (time < 15.5D
                    ? context.transition(time, 0.8D, 2.0D)
                    : 1.0D - context.transition(time, 15.5D, 17.0D));
            context.renderChest(392, 144, 44.0F, chestOpen, sourceOutline);

            if (time < 4.5D) {
                context.renderChestGui(277, 4, 0.52F, List.of(
                        slot(0, new ItemStack(Items.COBBLESTONE, 44)),
                        slot(1, new ItemStack(Items.OAK_PLANKS, 56)),
                        slot(2, new ItemStack(Items.COOKED_BEEF, 60))));
            } else {
                boolean restored = time >= 9.0D;
                List<TutorialRenderContext.SlotItem> inventory = List.of(
                        slot(9, new ItemStack(Items.COBBLESTONE, restored ? 64 : 20)),
                        slot(10, new ItemStack(Items.OAK_PLANKS, restored ? 64 : 8)),
                        locked(11, new ItemStack(Items.COOKED_BEEF, 4)));
                int inventoryX = 142;
                int inventoryY = 8;
                float inventoryScale = 0.72F;
                context.renderInventoryGui(inventoryX, inventoryY, inventoryScale, inventory);
                boolean pressed = time >= 7.5D && time < 8.2D;
                context.renderInventoryToolbar(inventoryX, inventoryY, inventoryScale,
                        TutorialRenderContext.InventoryAction.RESTOCK, pressed);
                double cursorMove = context.transition(time, 6.0D, 7.5D);
                context.renderCursor(
                        lerp(224, InventoryToolbarLayout.scaledButtonCenterX(
                                inventoryX, inventoryScale, 1), cursorMove),
                        lerp(31, InventoryToolbarLayout.scaledButtonCenterY(
                                inventoryY, inventoryScale), cursorMove),
                        pressed);

                double travel = context.transition(time, 9.0D, 12.0D);
                if (travel > 0.0D && travel < 1.0D) {
                    context.renderItem(new ItemStack(Items.COBBLESTONE, 44),
                            curve(382, 320, 210, 154, travel),
                            curve(116, 142, 140, 74, travel), 1.0F);
                    context.renderItem(new ItemStack(Items.OAK_PLANKS, 56),
                            curve(390, 328, 220, 167, travel),
                            curve(124, 146, 144, 74, travel), 1.0F);
                }
            }
            context.endScene();
        }
    }

    private static final class CraftNearbyScene extends BaseScene {
        private CraftNearbyScene() {
            super("craft_nearby", 25.0D);
        }

        @Override
        public Component caption(double time) {
            if (time < 5.0D) return caption(1);
            if (time < 10.0D) return caption(2);
            if (time < 15.0D) return caption(3);
            if (time < 20.0D) return caption(4);
            return caption(5);
        }

        @Override
        public void render(TutorialRenderContext context, double time) {
            double camera = context.transition(time, 8.5D, 10.0D);
            context.beginScene(lerp(0.0D, -12.0D, camera), 0.0D, lerp(1.0D, 1.06D, camera));
            context.renderWorldBackdrop();

            float chestOpen;
            if (time < 4.2D) {
                chestOpen = (float) context.transition(time, 0.8D, 1.8D);
            } else {
                chestOpen = (float) (1.0D - context.transition(time, 4.2D, 5.2D));
            }
            int sourceOutline = time >= 18.5D && time < 24.0D
                    ? StorageHighlightPalette.destinationArgb() : 0;
            context.renderChest(388, 144, 44.0F, chestOpen, sourceOutline);
            if (time >= 5.0D && time < 10.2D) {
                context.renderBlock(new ItemStack(Blocks.CRAFTING_TABLE), 309, 128, 2.6F,
                        time >= 8.5D
                                ? StorageHighlightPalette.listStateArgb(StorageListState.DEFAULT) : 0);
            }

            if (time < 5.0D) {
                context.renderChestGui(275, 4, 0.52F, List.of(
                        slot(0, new ItemStack(Items.IRON_INGOT, 3)),
                        slot(1, new ItemStack(Items.STICK, 2))));
            } else if (time < 10.2D) {
                context.renderInventoryGui(150, 7, 0.74F, List.of());
            } else {
                int[] gridSlots = {0, 1, 2, 4, 7};
                double[] targetX = {176, 190, 204, 190, 190};
                double[] targetY = {25, 25, 25, 39, 53};
                ItemStack[] stacks = {
                        new ItemStack(Items.IRON_INGOT),
                        new ItemStack(Items.IRON_INGOT),
                        new ItemStack(Items.IRON_INGOT),
                        new ItemStack(Items.STICK),
                        new ItemStack(Items.STICK)
                };
                double travel = context.transition(time, 15.5D, 18.5D);
                double[] ingredientProgress = new double[stacks.length];
                List<TutorialRenderContext.SlotItem> grid = new ArrayList<>();
                for (int i = 0; i < stacks.length; i++) {
                    ingredientProgress[i] = TutorialTimeline.clamp(travel * 2.1D - i * 0.275D);
                    if (ingredientProgress[i] >= 1.0D) {
                        grid.add(slot(gridSlots[i], stacks[i]));
                    }
                }
                ItemStack result = time >= 19.5D
                        ? new ItemStack(Items.IRON_PICKAXE) : ItemStack.EMPTY;
                context.renderCraftingGui(146, 5, 0.78F, grid, result);
                boolean recipeSelected = time >= 14.0D;
                context.renderRecipeBook(31, 5, 0.78F,
                        new ItemStack(Items.IRON_PICKAXE), recipeSelected);

                if (time >= 12.0D && time < 15.5D) {
                    double cursorMove = context.transition(time, 12.0D, 14.0D);
                    context.renderCursor(lerp(98, 95, cursorMove), lerp(15, 39, cursorMove),
                            time >= 14.0D && time < 14.7D);
                }

                if (travel > 0.0D && travel < 1.0D) {
                    for (int i = 0; i < stacks.length; i++) {
                        double progress = ingredientProgress[i];
                        if (progress > 0.0D && progress < 1.0D) {
                            context.renderItem(stacks[i],
                                    curve(386, 350, 292, targetX[i], progress),
                                    curve(117, 117, targetY[i], targetY[i], progress), 0.9F);
                        }
                    }
                }
            }
            context.endScene();
        }
    }

    private static final class DumpAndLocksScene extends BaseScene {
        private DumpAndLocksScene() {
            super("dump_locks", 18.0D);
        }

        @Override
        public Component caption(double time) {
            if (time < 6.0D) return caption(1);
            if (time < 12.0D) return caption(2);
            return caption(3);
        }

        @Override
        public void render(TutorialRenderContext context, double time) {
            context.beginScene(0.0D, 0.0D, 1.0D);
            context.renderWorldBackdrop();
            int destinationColor = time >= 11.5D && time < 17.0D
                    ? StorageHighlightPalette.destinationArgb() : 0;
            context.renderChest(392, 145, 42.0F,
                    (float) context.transition(time, 9.5D, 11.0D), destinationColor);
            context.renderBlock(new ItemStack(Blocks.BARREL), 333, 128, 2.45F,
                    destinationColor);

            boolean locked = time >= 4.6D;
            boolean dumped = time >= 9.8D;
            List<TutorialRenderContext.SlotItem> inventory = dumped
                    ? List.of(locked(12, new ItemStack(Items.NETHERITE_PICKAXE)))
                    : List.of(
                            slot(9, new ItemStack(Items.DIRT, 32)),
                            slot(10, new ItemStack(Items.REDSTONE, 18)),
                            slot(11, new ItemStack(Items.OAK_LOG, 12)),
                            locked
                                    ? locked(12, new ItemStack(Items.NETHERITE_PICKAXE))
                                    : slot(12, new ItemStack(Items.NETHERITE_PICKAXE)));
            int inventoryX = 126;
            int inventoryY = 7;
            float inventoryScale = 0.75F;
            context.renderInventoryGui(inventoryX, inventoryY, inventoryScale, inventory);

            if (time < 6.8D) {
                double lockMove = context.transition(time, 2.0D, 4.0D);
                context.renderCursor(lerp(205, 176, lockMove), lerp(28, 82, lockMove),
                        time >= 4.0D && time < 4.6D);
            } else {
                boolean pressed = time >= 9.0D && time < 9.6D;
                context.renderInventoryToolbar(inventoryX, inventoryY, inventoryScale,
                        TutorialRenderContext.InventoryAction.DUMP, pressed);
                double dumpMove = context.transition(time, 6.8D, 9.0D);
                context.renderCursor(
                        lerp(211, InventoryToolbarLayout.scaledButtonCenterX(
                                inventoryX, inventoryScale, 2), dumpMove),
                        lerp(30, InventoryToolbarLayout.scaledButtonCenterY(
                                inventoryY, inventoryScale), dumpMove),
                        pressed);
            }

            double travel = context.transition(time, 9.8D, 12.5D);
            if (travel > 0.0D && travel < 1.0D) {
                context.renderItem(new ItemStack(Items.DIRT, 32),
                        curve(138, 138, 326, 390, travel),
                        curve(76, 140, 144, 119, travel), 1.0F);
                context.renderItem(new ItemStack(Items.REDSTONE, 18),
                        curve(152, 152, 280, 333, travel),
                        curve(76, 142, 142, 119, travel), 1.0F);
                context.renderItem(new ItemStack(Items.OAK_LOG, 12),
                        curve(165, 165, 318, 382, travel),
                        curve(76, 144, 146, 124, travel), 1.0F);
            }
            context.endScene();
        }
    }

    private static final class StoragePreviewScene extends BaseScene {
        private StoragePreviewScene() {
            super("storage_preview", 15.0D);
        }

        @Override
        public Component caption(double time) {
            if (time < 5.0D) return caption(1);
            if (time < 10.0D) return caption(2);
            return caption(3);
        }

        @Override
        public void render(TutorialRenderContext context, double time) {
            context.beginScene(0.0D, 0.0D, 1.04D);
            context.renderWorldBackdrop();

            boolean activated = time >= 5.2D;
            int previewColor = StorageHighlightPalette.destinationArgb();
            int outlineColor = activated ? previewColor : 0;
            Component badge = Component.translatable(activated
                    ? "quickstackcraft.tutorial.storage_preview.eligible"
                    : "quickstackcraft.tutorial.storage_preview.hotkey");

            context.renderChest(360, 140, 46.0F, 0.0F, outlineColor);
            context.renderBlock(new ItemStack(Blocks.BARREL), 278, 128, 2.7F,
                    outlineColor);
            context.renderBadge(330, 20, badge,
                    activated ? previewColor
                            : StorageHighlightPalette.listStateArgb(StorageListState.DEFAULT),
                    activated ? UiIcon.CHECK : UiIcon.PLAY);
            context.endScene();
        }
    }

    private static final class WhitelistBlacklistScene extends BaseScene {
        private WhitelistBlacklistScene() {
            super("whitelist_blacklist", 20.0D);
        }

        @Override
        public Component caption(double time) {
            if (time < 5.0D) return caption(1);
            if (time < 10.0D) return caption(2);
            if (time < 15.0D) return caption(3);
            return caption(4);
        }

        @Override
        public void render(TutorialRenderContext context, double time) {
            context.beginScene(0.0D, 0.0D, 1.04D);
            context.renderWorldBackdrop();

            StorageListState state;
            Component badge;
            UiIcon badgeIcon;
            if (time < 5.0D) {
                state = StorageListState.DEFAULT;
                badge = Component.translatable(
                        "quickstackcraft.tutorial.whitelist_blacklist.hotkey");
                badgeIcon = UiIcon.PLAY;
            } else if (time < 10.0D) {
                state = StorageListState.WHITELISTED;
                badge = Component.translatable(
                        "quickstackcraft.tutorial.whitelist_blacklist.whitelisted");
                badgeIcon = UiIcon.CHECK;
            } else if (time < 15.0D) {
                state = StorageListState.BLACKLISTED;
                badge = Component.translatable(
                        "quickstackcraft.tutorial.whitelist_blacklist.blacklisted");
                badgeIcon = UiIcon.BLOCKED;
            } else {
                state = StorageListState.DEFAULT;
                badge = Component.translatable(
                        "quickstackcraft.tutorial.whitelist_blacklist.default");
                badgeIcon = UiIcon.REPLAY;
            }

            int stateColor = StorageHighlightPalette.listStateArgb(state);
            context.renderChest(360, 140, 46.0F, 0.0F, stateColor);
            context.renderBlock(new ItemStack(Blocks.BARREL), 278, 128, 2.7F, 0);
            context.renderBadge(330, 20, badge, stateColor, badgeIcon);
            context.endScene();
        }
    }

}
