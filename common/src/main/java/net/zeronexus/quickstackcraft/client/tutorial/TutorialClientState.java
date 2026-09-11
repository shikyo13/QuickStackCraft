package net.zeronexus.quickstackcraft.client.tutorial;

import net.zeronexus.quickstackcraft.network.ModNetworking;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.zeronexus.quickstackcraft.network.TutorialSeenC2SPacket;

public final class TutorialClientState {

    private static Object connectionIdentity;
    private static boolean statusReceived;
    private static boolean seen;
    private static boolean pendingFirstRun;

    private TutorialClientState() {}

    public static void receiveStatus(boolean tutorialSeen) {
        Minecraft minecraft = Minecraft.getInstance();
        refreshConnection(minecraft);
        statusReceived = true;
        seen = tutorialSeen;
        pendingFirstRun = !tutorialSeen;
    }

    public static void tick(Minecraft minecraft) {
        refreshConnection(minecraft);
        if (!statusReceived || seen || !pendingFirstRun || minecraft.player == null) {
            return;
        }
        if (minecraft.screen instanceof InventoryScreen inventoryScreen) {
            pendingFirstRun = false;
            TutorialScreen.openFirstRun(inventoryScreen);
        }
    }

    public static void markShown() {
        if (seen) {
            return;
        }
        seen = true;
        pendingFirstRun = false;
        ModNetworking.sendToServer(new TutorialSeenC2SPacket());
    }

    private static void refreshConnection(Minecraft minecraft) {
        Object current = minecraft.getConnection();
        if (current == connectionIdentity) {
            return;
        }
        connectionIdentity = current;
        statusReceived = false;
        seen = false;
        pendingFirstRun = false;
    }
}
