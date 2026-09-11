package net.zeronexus.quickstackcraft.forge.mixin;

import dev.architectury.networking.NetworkManager;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.InventoryMenu;
import net.zeronexus.quickstackcraft.client.InventoryToolbarLayout;
import net.zeronexus.quickstackcraft.client.ClientPreferences;
import net.zeronexus.quickstackcraft.client.ToolbarPreferences;
import net.zeronexus.quickstackcraft.client.tutorial.TutorialHover;
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
                        btn -> net.zeronexus.quickstackcraft.network.ModNetworking.sendToServer(new InventoryActionC2SPacket(
                                InventoryActionC2SPacket.Action.QUICK_STACK, ExternalSlotLocks.snapshot())))
                .bounds(InventoryToolbarLayout.buttonX(this.leftPos, 0), btnY, btnSize, btnSize)
                .build();
        quickstackcraft$quickStackButton.setTooltip(Tooltip.create(
                Component.translatable("quickstackcraft.button.quick_stack").append("\n").append(TutorialHover.hint())));
        quickstackcraft$quickStackButton.setTooltipDelay(250);

        quickstackcraft$restockButton = Button.builder(
                        Component.literal(InventoryToolbarLayout.RESTOCK_LABEL),
                        btn -> net.zeronexus.quickstackcraft.network.ModNetworking.sendToServer(new InventoryActionC2SPacket(
                                InventoryActionC2SPacket.Action.RESTOCK, ExternalSlotLocks.snapshot())))
                .bounds(InventoryToolbarLayout.buttonX(this.leftPos, 1), btnY, btnSize, btnSize)
                .build();
        quickstackcraft$restockButton.setTooltip(Tooltip.create(
                Component.translatable("quickstackcraft.button.restock").append("\n").append(TutorialHover.hint())));
        quickstackcraft$restockButton.setTooltipDelay(250);

        quickstackcraft$dumpButton = Button.builder(
                        Component.literal(InventoryToolbarLayout.DUMP_LABEL),
                        btn -> net.zeronexus.quickstackcraft.network.ModNetworking.sendToServer(new InventoryActionC2SPacket(
                                InventoryActionC2SPacket.Action.DUMP, ExternalSlotLocks.snapshot())))
                .bounds(InventoryToolbarLayout.buttonX(this.leftPos, 2), btnY, btnSize, btnSize)
                .build();
        quickstackcraft$dumpButton.setTooltip(Tooltip.create(
                Component.translatable("quickstackcraft.button.dump_all").append("\n").append(TutorialHover.hint())));
        quickstackcraft$dumpButton.setTooltipDelay(250);

        quickstackcraft$configButton = new UiIconButton(
                InventoryToolbarLayout.buttonX(this.leftPos, 3), btnY, btnSize, UiIcon.SETTINGS,
                Component.translatable("quickstackcraft.button.config"),
                btn -> QuickStackConfigScreen.open(this));

        this.addRenderableWidget(quickstackcraft$quickStackButton);
        this.addRenderableWidget(quickstackcraft$restockButton);
        this.addRenderableWidget(quickstackcraft$dumpButton);
        quickstackcraft$configButton.setTooltip(Tooltip.create(
                Component.translatable("quickstackcraft.button.config").append("\n").append(TutorialHover.hint())));
        this.addRenderableWidget(quickstackcraft$configButton);
        quickstackcraft$positionButtons();
    }

    @Inject(method = "render", at = @At("HEAD"))
    private void quickstackcraft$updateButtons(GuiGraphics graphics, int mouseX, int mouseY,
                                               float delta, CallbackInfo ci) {
        quickstackcraft$positionButtons();
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void quickstackcraft$hoverLesson(GuiGraphics graphics, int mouseX, int mouseY,
                                            float delta, CallbackInfo ci) {
        int chapter = -1;
        if (quickstackcraft$quickStackButton != null && quickstackcraft$quickStackButton.visible) {
            if (quickstackcraft$quickStackButton.isMouseOver(mouseX, mouseY)
                    || quickstackcraft$restockButton.isMouseOver(mouseX, mouseY)
                    || quickstackcraft$dumpButton.isMouseOver(mouseX, mouseY)) chapter = 0;
            else if (quickstackcraft$configButton.isMouseOver(mouseX, mouseY)) chapter = 3;
        }
        TutorialHover.update(this, graphics, mouseX, mouseY, chapter);
    }

    @Unique
    private void quickstackcraft$positionButtons() {
        if (quickstackcraft$quickStackButton == null) {
            return;
        }
        ToolbarPreferences preferences = ClientPreferences.inventoryToolbar();
        InventoryToolbarLayout.Position position = InventoryToolbarLayout.position(
                InventoryToolbarLayout.buttonX(this.leftPos, 0), InventoryToolbarLayout.buttonY(this.topPos),
                4, InventoryToolbarLayout.BUTTON_SIZE, InventoryToolbarLayout.BUTTON_GAP,
                this.width, this.height, preferences);
        Button[] buttons = {quickstackcraft$quickStackButton, quickstackcraft$restockButton,
                quickstackcraft$dumpButton, quickstackcraft$configButton};
        for (int index = 0; index < buttons.length; index++) {
            buttons[index].setPosition(position.x() + index * (InventoryToolbarLayout.BUTTON_SIZE
                    + InventoryToolbarLayout.BUTTON_GAP), position.y());
            buttons[index].visible = preferences.visible();
            buttons[index].active = preferences.visible();
        }
    }
}
