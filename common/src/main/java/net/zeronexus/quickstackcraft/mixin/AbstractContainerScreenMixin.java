package net.zeronexus.quickstackcraft.mixin;

import dev.architectury.networking.NetworkManager;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.ChatFormatting;
import java.util.ArrayList;
import java.util.List;
import net.zeronexus.quickstackcraft.client.ClientFavoritesCache;
import net.zeronexus.quickstackcraft.client.ClientPreferences;
import net.zeronexus.quickstackcraft.client.InventoryToolbarLayout;
import net.zeronexus.quickstackcraft.client.ToolbarPreferences;
import net.zeronexus.quickstackcraft.client.tutorial.TutorialHover;
import net.zeronexus.quickstackcraft.client.ModKeybinds;
import net.zeronexus.quickstackcraft.client.QuickStackConfigScreen;
import net.zeronexus.quickstackcraft.client.UiIcon;
import net.zeronexus.quickstackcraft.client.UiIconButton;
import net.zeronexus.quickstackcraft.compat.ExternalSlotLocks;
import net.zeronexus.quickstackcraft.network.InventoryActionC2SPacket;
import net.zeronexus.quickstackcraft.network.QuickStackSlotC2SPacket;
import net.zeronexus.quickstackcraft.network.FavoriteToggleC2SPacket;
import net.zeronexus.quickstackcraft.network.OpenContainerTransferC2SPacket;
import net.zeronexus.quickstackcraft.logic.OpenContainerSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractContainerScreen.class)
public abstract class AbstractContainerScreenMixin<T extends AbstractContainerMenu> extends Screen {

    @Shadow protected T menu;
    @Shadow protected int leftPos;
    @Shadow protected int topPos;
    @Shadow protected Slot hoveredSlot;

    @Unique private Button[] quickstackcraft$storageButtons = new Button[0];

    @Shadow
    protected abstract boolean isHovering(int left, int top, int width, int height, double mouseX, double mouseY);

    private AbstractContainerScreenMixin(Component title) {
        super(title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void quickstackcraft$addOpenStorageButtons(CallbackInfo ci) {
        quickstackcraft$storageButtons = new Button[0];
        if ((Object) this instanceof QuickStackConfigScreen) {
            return;
        }
        if (!quickstackcraft$isOpenStorageScreen()) {
            return;
        }

        int size = 18;
        int x = this.leftPos + 2;
        int y = Math.max(2, this.topPos - size - 2);

        Button quickStack = this.addRenderableWidget(new UiIconButton(x, y, size, UiIcon.QUICK_STACK,
                Component.translatable("quickstackcraft.button.quick_stack_storage"),
                button -> quickstackcraft$sendStorageTransfer(false)));
        Button dump = this.addRenderableWidget(new UiIconButton(x + size + 2, y, size, UiIcon.DUMP,
                Component.translatable("quickstackcraft.button.dump_storage"),
                button -> quickstackcraft$sendStorageTransfer(true)));
        Button settings = this.addRenderableWidget(new UiIconButton(x + (size + 2) * 2, y, size, UiIcon.SETTINGS,
                Component.translatable("quickstackcraft.button.config"),
                button -> QuickStackConfigScreen.open(this)));
        quickstackcraft$storageButtons = new Button[] {quickStack, dump, settings};
        for (Button button : quickstackcraft$storageButtons) {
            button.setTooltip(net.minecraft.client.gui.components.Tooltip.create(
                    button.getMessage().copy().append("\n").append(TutorialHover.hint())));
        }
        quickstackcraft$positionStorageButtons();
    }

    @Inject(method = "render", at = @At("HEAD"))
    private void quickstackcraft$updateStorageButtons(GuiGraphics graphics, int mouseX, int mouseY,
                                                     float delta, CallbackInfo ci) {
        quickstackcraft$positionStorageButtons();
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void quickstackcraft$hoverStorageLesson(GuiGraphics graphics, int mouseX, int mouseY,
                                                   float delta, CallbackInfo ci) {
        if (quickstackcraft$storageButtons.length > 0) {
            int chapter = -1;
            for (int index = 0; index < quickstackcraft$storageButtons.length; index++) {
                Button button = quickstackcraft$storageButtons[index];
                if (button.visible && button.isMouseOver(mouseX, mouseY)) chapter = index == 2 ? 3 : 0;
            }
            TutorialHover.update(this, graphics, mouseX, mouseY, chapter);
        }
    }

    @Unique
    private void quickstackcraft$positionStorageButtons() {
        if (quickstackcraft$storageButtons.length == 0) {
            return;
        }
        ToolbarPreferences preferences = ClientPreferences.storageToolbar();
        InventoryToolbarLayout.Position position = InventoryToolbarLayout.position(
                this.leftPos + 2, Math.max(2, this.topPos - 20), 3, 18, 2,
                this.width, this.height, preferences);
        for (int index = 0; index < quickstackcraft$storageButtons.length; index++) {
            Button button = quickstackcraft$storageButtons[index];
            button.setPosition(position.x() + index * 20, position.y());
            button.visible = preferences.visible();
            button.active = preferences.visible();
        }
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void quickstackcraft$renderSlotLocks(GuiGraphics graphics, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if ((Object) this instanceof QuickStackConfigScreen) {
            return;
        }
        long externalLocks = ExternalSlotLocks.renderSnapshot();
        Component hoveredTooltip = null;
        for (Slot slot : this.menu.slots) {
            int inventorySlot = slot.getContainerSlot();
            boolean external = inventorySlot >= 0 && inventorySlot < 36
                    && (externalLocks & (1L << inventorySlot)) != 0L;
            if (quickstackcraft$isPlayerInventorySlot(slot)
                    && ClientFavoritesCache.isFavorited(inventorySlot)
                    && !external) {
                int x = this.leftPos + slot.x;
                int y = this.topPos + slot.y;
                quickstackcraft$renderLockBorder(graphics, x, y, 0xFFF2C14E);
                UiIcon.renderLock(graphics, x + 9, y + 1, 0xFFF9D36A);
                if (!slot.hasItem() && this.isHovering(slot.x, slot.y, 16, 16, mouseX, mouseY)) {
                    hoveredTooltip = Component.translatable("quickstackcraft.tooltip.native_lock");
                }
            }
        }
        if (hoveredTooltip != null) {
            graphics.renderTooltip(this.font, hoveredTooltip, mouseX, mouseY);
        }
    }

    @Inject(method = "getTooltipFromContainerItem", at = @At("RETURN"), cancellable = true)
    private void quickstackcraft$appendLockTooltip(ItemStack stack, CallbackInfoReturnable<List<Component>> cir) {
        if (this.hoveredSlot == null || !quickstackcraft$isPlayerInventorySlot(this.hoveredSlot)
                || this.hoveredSlot.getItem() != stack) return;
        int index = this.hoveredSlot.getContainerSlot();
        if (!ClientFavoritesCache.isFavorited(index)
                || (ExternalSlotLocks.renderSnapshot() & (1L << index)) != 0) return;
        List<Component> tooltip = new ArrayList<>(cir.getReturnValue());
        tooltip.add(Component.translatable("quickstackcraft.tooltip.native_lock").withStyle(ChatFormatting.GOLD));
        cir.setReturnValue(tooltip);
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void quickstackcraft$toggleSlotLock(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        if ((Object) this instanceof QuickStackConfigScreen) {
            return;
        }
        if (button != 0 || !Screen.hasAltDown()) {
            return;
        }
        if (ExternalSlotLocks.isToggleGestureActive()) {
            return;
        }

        for (Slot slot : this.menu.slots) {
            if (quickstackcraft$isPlayerInventorySlot(slot)
                    && this.isHovering(slot.x, slot.y, 16, 16, mouseX, mouseY)) {
                NetworkManager.sendToServer(new FavoriteToggleC2SPacket(slot.getContainerSlot()));
                cir.setReturnValue(true);
                return;
            }
        }
    }

    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    private void quickstackcraft$handleTransferKey(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        if ((Object) this instanceof QuickStackConfigScreen
                || this.getFocused() instanceof EditBox editBox && editBox.isFocused()) {
            return;
        }
        if (TutorialHover.consumes(this, keyCode, scanCode)) {
            cir.setReturnValue(true);
        } else if (ModKeybinds.QUICK_STACK_HOVERED.matches(keyCode, scanCode)) {
            quickstackcraft$sendHoveredStack();
            cir.setReturnValue(true);
        } else if (ModKeybinds.QUICK_STACK.matches(keyCode, scanCode)) {
            if (quickstackcraft$isOpenStorageScreen()) {
                quickstackcraft$sendStorageTransfer(false);
            } else {
                quickstackcraft$sendInventoryAction(InventoryActionC2SPacket.Action.QUICK_STACK);
            }
            cir.setReturnValue(true);
        } else if (ModKeybinds.RESTOCK.matches(keyCode, scanCode)) {
            quickstackcraft$sendInventoryAction(InventoryActionC2SPacket.Action.RESTOCK);
            cir.setReturnValue(true);
        } else if (ModKeybinds.DUMP_ALL.matches(keyCode, scanCode)) {
            if (quickstackcraft$isOpenStorageScreen()) {
                quickstackcraft$sendStorageTransfer(true);
            } else {
                quickstackcraft$sendInventoryAction(InventoryActionC2SPacket.Action.DUMP);
            }
            cir.setReturnValue(true);
        } else if (ModKeybinds.CONFIG.matches(keyCode, scanCode)) {
            QuickStackConfigScreen.open(this);
            cir.setReturnValue(true);
        }
    }

    @Unique
    private void quickstackcraft$sendHoveredStack() {
        if (this.minecraft == null || this.minecraft.player == null) {
            return;
        }
        String error = null;
        if (!this.minecraft.player.containerMenu.getCarried().isEmpty()) {
            error = "quickstackcraft.message.cursor_occupied";
        } else if (this.hoveredSlot == null || !this.hoveredSlot.isActive()
                || this.hoveredSlot.container != this.minecraft.player.getInventory()
                || !quickstackcraft$isPlayerInventorySlot(this.hoveredSlot) || !this.hoveredSlot.hasItem()) {
            error = "quickstackcraft.message.hover_inventory_stack";
        }
        if (error != null) {
            this.minecraft.player.displayClientMessage(Component.translatable(error), true);
            return;
        }
        AbstractContainerMenu activeMenu = this.minecraft.player.containerMenu;
        NetworkManager.sendToServer(new QuickStackSlotC2SPacket(activeMenu.containerId, activeMenu.getStateId(),
                this.hoveredSlot.getContainerSlot(), ExternalSlotLocks.snapshot()));
    }

    @Unique
    private boolean quickstackcraft$isOpenStorageScreen() {
        return OpenContainerSource.supports(this.menu);
    }

    @Unique
    private void quickstackcraft$sendStorageTransfer(boolean dumpAll) {
        OpenContainerTransferC2SPacket.TransferKind kind = dumpAll
                ? OpenContainerTransferC2SPacket.TransferKind.ALL_ITEMS
                : OpenContainerTransferC2SPacket.TransferKind.MATCHING_ITEMS;
        NetworkManager.sendToServer(new OpenContainerTransferC2SPacket(kind, this.menu.containerId));
    }

    @Unique
    private void quickstackcraft$sendInventoryAction(InventoryActionC2SPacket.Action action) {
        NetworkManager.sendToServer(new InventoryActionC2SPacket(action, ExternalSlotLocks.snapshot()));
    }

    @Unique
    private static void quickstackcraft$renderLockBorder(GuiGraphics graphics, int x, int y, int color) {
        graphics.fill(x, y, x + 16, y + 1, color);
        graphics.fill(x, y + 15, x + 16, y + 16, color);
        graphics.fill(x, y, x + 1, y + 16, color);
        graphics.fill(x + 15, y, x + 16, y + 16, color);
    }

    @Unique
    private static boolean quickstackcraft$isPlayerInventorySlot(Slot slot) {
        int index = slot.getContainerSlot();
        return slot.container instanceof Inventory && index >= 0 && index < 36;
    }
}
