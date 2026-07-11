package net.zeronexus.quickstackcraft.client;

import dev.architectury.networking.NetworkManager;
import net.minecraft.client.Minecraft;
import net.zeronexus.quickstackcraft.compat.ExternalSlotLocks;
import net.zeronexus.quickstackcraft.network.InventoryActionC2SPacket;
import net.zeronexus.quickstackcraft.network.StorageListActionC2SPacket;

/**
 * Handles global keybinds while no GUI screen is open.
 */
public final class WorldKeybindHandler {

    private WorldKeybindHandler() {}

    public static void tick(Minecraft minecraft) {
        if (minecraft.player == null || minecraft.screen != null) {
            return;
        }

        while (ModKeybinds.QUICK_STACK.consumeClick()) {
            sendInventoryAction(InventoryActionC2SPacket.Action.QUICK_STACK);
        }
        while (ModKeybinds.DUMP_ALL.consumeClick()) {
            sendInventoryAction(InventoryActionC2SPacket.Action.DUMP);
        }
        while (ModKeybinds.PREVIEW_STORAGE.consumeClick()) {
            NetworkManager.sendToServer(new StorageListActionC2SPacket(
                    StorageListActionC2SPacket.Action.SHOW_NEARBY));
        }
        while (ModKeybinds.CYCLE_STORAGE_LIST.consumeClick()) {
            NetworkManager.sendToServer(new StorageListActionC2SPacket(
                    StorageListActionC2SPacket.Action.CYCLE_LOOKED_AT));
        }
        while (ModKeybinds.CONFIG.consumeClick()) {
            QuickStackConfigScreen.open(null);
        }
    }

    private static void sendInventoryAction(InventoryActionC2SPacket.Action action) {
        NetworkManager.sendToServer(new InventoryActionC2SPacket(action, ExternalSlotLocks.snapshot()));
    }
}
