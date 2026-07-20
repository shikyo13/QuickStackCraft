package net.zeronexus.quickstackcraft.mixin;

import dev.architectury.networking.NetworkManager;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.zeronexus.quickstackcraft.client.ClientFavoritesCache;
import net.zeronexus.quickstackcraft.client.ModKeybinds;
import net.zeronexus.quickstackcraft.client.QuickStackConfigScreen;
import net.zeronexus.quickstackcraft.client.UiIcon;
import net.zeronexus.quickstackcraft.client.UiIconButton;
import net.zeronexus.quickstackcraft.compat.ExternalSlotLocks;
import net.zeronexus.quickstackcraft.network.InventoryActionC2SPacket;
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

    @Shadow
    protected abstract boolean isHovering(int left, int top, int width, int height, double mouseX, double mouseY);

    private AbstractContainerScreenMixin(Component title) {
        super(title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void quickstackcraft$addOpenStorageButtons(CallbackInfo ci) {
        if ((Object) this instanceof QuickStackConfigScreen) {
            return;
        }
        if (!quickstackcraft$isOpenStorageScreen()) {
            return;
        }

        int size = 18;
        int x = this.leftPos + 2;
        int y = Math.max(2, this.topPos - size - 2);

        this.addRenderableWidget(new UiIconButton(x, y, size, UiIcon.QUICK_STACK,
                Component.translatable("quickstackcraft.button.quick_stack_storage"),
                button -> quickstackcraft$sendStorageTransfer(false)));
        this.addRenderableWidget(new UiIconButton(x + size + 2, y, size, UiIcon.DUMP,
                Component.translatable("quickstackcraft.button.dump_storage"),
                button -> quickstackcraft$sendStorageTransfer(true)));
        this.addRenderableWidget(new UiIconButton(x + (size + 2) * 2, y, size, UiIcon.SETTINGS,
                Component.translatable("quickstackcraft.button.config"),
                button -> QuickStackConfigScreen.open(this)));
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
                if (this.isHovering(slot.x, slot.y, 16, 16, mouseX, mouseY)) {
                    hoveredTooltip = Component.translatable("quickstackcraft.tooltip.native_lock");
                }
            }
        }
        if (hoveredTooltip != null) {
            graphics.renderTooltip(this.font, hoveredTooltip, mouseX, mouseY);
        }
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
        if ((Object) this instanceof QuickStackConfigScreen) {
            return;
        }
        if (ModKeybinds.QUICK_STACK.matches(keyCode, scanCode)) {
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
