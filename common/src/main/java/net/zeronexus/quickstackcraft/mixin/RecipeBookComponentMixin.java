package net.zeronexus.quickstackcraft.mixin;

import dev.architectury.networking.NetworkManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.world.entity.player.StackedItemContents;
import net.minecraft.world.inventory.RecipeBookMenu;
import net.minecraft.world.inventory.RecipeBookType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.display.RecipeDisplayId;
import net.zeronexus.quickstackcraft.client.NearbyItemsCache;
import net.zeronexus.quickstackcraft.compat.ExternalSlotLocks;
import net.zeronexus.quickstackcraft.network.NearbyItemsScanC2SPacket;
import net.zeronexus.quickstackcraft.network.RecipeTransferC2SPacket;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(net.minecraft.client.gui.screens.recipebook.RecipeBookComponent.class)
public abstract class RecipeBookComponentMixin {

    @Shadow
    @Final
    protected RecipeBookMenu menu;

    @Shadow
    protected Minecraft minecraft;

    @Shadow
    @Final
    private StackedItemContents stackedContents;

    private long quickstackcraft$lastNearbyRevision = -1;

    @Shadow
    public abstract boolean isVisible();

    @Shadow
    private void updateStackedContents() {
        throw new AssertionError();
    }

    @Redirect(
            method = "mouseClicked",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;handlePlaceRecipe(ILnet/minecraft/world/item/crafting/display/RecipeDisplayId;Z)V"
            )
    )
    private void quickstackcraft$placeRecipeFromNearby(
            MultiPlayerGameMode gameMode, int containerId, RecipeDisplayId recipe, boolean maxTransfer) {
        if (quickstackcraft$shouldUseNearbyItems()) {
            NetworkManager.sendToServer(RecipeTransferC2SPacket.forDisplay(
                    containerId, recipe, maxTransfer,
                    ExternalSlotLocks.snapshot()));
        } else {
            gameMode.handlePlaceRecipe(containerId, recipe, maxTransfer);
        }
    }

    @Inject(
            method = "updateStackedContents",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/screens/recipebook/RecipeBookComponent;updateCollections(ZZ)V"
            )
    )
    private void quickstackcraft$includeNearbyItemsInRecipeBook(CallbackInfo ci) {
        if (!quickstackcraft$shouldUseNearbyItems()) {
            return;
        }

        NearbyItemsCache.forEachItem((item, count) -> {
            if (count > 0) {
                stackedContents.accountStack(new ItemStack(item, count));
            }
        });
        quickstackcraft$lastNearbyRevision = NearbyItemsCache.revision();
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void quickstackcraft$refreshNearbyRecipeBookItems(CallbackInfo ci) {
        if (!quickstackcraft$shouldUseNearbyItems()) {
            return;
        }

        long gameTime = minecraft.player.level().getGameTime();
        if (NearbyItemsCache.needsRefresh(gameTime)) {
            NearbyItemsCache.markQueried(gameTime);
            NetworkManager.sendToServer(new NearbyItemsScanC2SPacket());
        }

        if (quickstackcraft$lastNearbyRevision != NearbyItemsCache.revision()) {
            updateStackedContents();
        }
    }

    private boolean quickstackcraft$shouldUseNearbyItems() {
        return isVisible()
                && minecraft != null
                && minecraft.player != null
                && menu != null
                && menu.getRecipeBookType() == RecipeBookType.CRAFTING;
    }
}
