package net.zeronexus.quickstackcraft;

import dev.architectury.event.events.client.ClientTickEvent;
import net.zeronexus.quickstackcraft.client.ClientPreferences;
import net.zeronexus.quickstackcraft.client.ContainerHighlightRenderer;
import net.zeronexus.quickstackcraft.client.WorldKeybindHandler;
import net.zeronexus.quickstackcraft.client.tutorial.TutorialClientState;
import net.zeronexus.quickstackcraft.network.ModNetworking;

public final class QuickStackCraft {
    public static final String MOD_ID = "quickstackcraft";

    public static void init() {
        ModNetworking.register();
    }

    public static void initClient() {
        ClientPreferences.load();
        ClientTickEvent.CLIENT_POST.register(mc -> {
            ContainerHighlightRenderer.tick();
            WorldKeybindHandler.tick(mc);
            TutorialClientState.tick(mc);
        });
    }
}
