package net.zeronexus.quickstackcraft.fabric.mixin;

import dev.architectury.networking.NetworkManager;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.InventoryMenu;
import net.zeronexus.quickstackcraft.client.InventoryToolbarLayout;
import net.zeronexus.quickstackcraft.client.QuickStackConfigScreen;
import net.zeronexus.quickstackcraft.client.UiIcon;
import net.zeronexus.quickstackcraft.client.UiIconButton;
import net.zeronexus.quickstackcraft.compat.ExternalSlotLocks;
import net.zeronexus.quickstackcraft.network.InventoryActionC2SPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.time.Duration;

@Mixin(InventoryScreen.class)
public abstract class InventoryScreenMixin extends AbstractContainerScreen<InventoryMenu> {

    private InventoryScreenMixin() { super(null, null, Component.empty()); }

    @Unique private Button quickstackcraft$quickStackButton;
    @Unique private Button quickstackcraft$restockButton;
    @Unique private Button quickstackcraft$dumpButton;
    @Unique private Button quickstackcraft$configButton;

    @Inject(method = "init", at = @At("TAIL"))
    private void quickstackcraft$addButtons(CallbackInfo ci) {
        int btnY = InventoryToolbarLayout.buttonY(this.topPos);
        int btnSize = InventoryToolbarLayout.BUTTON_SIZE;

        quickstackcraft$quickStackButton = Button.builder(
                        Component.literal(InventoryToolbarLayout.QUICK_STACK_LABEL),
                        btn -> NetworkManager.sendToServer(new InventoryActionC2SPacket(
                                InventoryActionC2SPacket.Action.QUICK_STACK, ExternalSlotLocks.snapshot())))
                .bounds(InventoryToolbarLayout.buttonX(this.leftPos, 0), btnY, btnSize, btnSize)
                .build();
        quickstackcraft$quickStackButton.setTooltip(Tooltip.create(
                Component.translatable("quickstackcraft.button.quick_stack")));
        quickstackcraft$quickStackButton.setTooltipDelay(Duration.ofMillis(250));

        quickstackcraft$restockButton = Button.builder(
                        Component.literal(InventoryToolbarLayout.RESTOCK_LABEL),
                        btn -> NetworkManager.sendToServer(new InventoryActionC2SPacket(
                                InventoryActionC2SPacket.Action.RESTOCK, ExternalSlotLocks.snapshot())))
                .bounds(InventoryToolbarLayout.buttonX(this.leftPos, 1), btnY, btnSize, btnSize)
                .build();
        quickstackcraft$restockButton.setTooltip(Tooltip.create(
                Component.translatable("quickstackcraft.button.restock")));
        quickstackcraft$restockButton.setTooltipDelay(Duration.ofMillis(250));

        quickstackcraft$dumpButton = Button.builder(
                        Component.literal(InventoryToolbarLayout.DUMP_LABEL),
                        btn -> NetworkManager.sendToServer(new InventoryActionC2SPacket(
                                InventoryActionC2SPacket.Action.DUMP, ExternalSlotLocks.snapshot())))
                .bounds(InventoryToolbarLayout.buttonX(this.leftPos, 2), btnY, btnSize, btnSize)
                .build();
        quickstackcraft$dumpButton.setTooltip(Tooltip.create(
                Component.translatable("quickstackcraft.button.dump_all")));
        quickstackcraft$dumpButton.setTooltipDelay(Duration.ofMillis(250));

        quickstackcraft$configButton = new UiIconButton(
                InventoryToolbarLayout.buttonX(this.leftPos, 3), btnY, btnSize, UiIcon.SETTINGS,
                Component.translatable("quickstackcraft.button.config"),
                btn -> QuickStackConfigScreen.open(this));

        this.addRenderableWidget(quickstackcraft$quickStackButton);
        this.addRenderableWidget(quickstackcraft$restockButton);
        this.addRenderableWidget(quickstackcraft$dumpButton);
        this.addRenderableWidget(quickstackcraft$configButton);
    }
}
