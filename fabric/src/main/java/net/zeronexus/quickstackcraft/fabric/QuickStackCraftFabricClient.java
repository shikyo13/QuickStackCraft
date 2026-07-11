package net.zeronexus.quickstackcraft.fabric;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.zeronexus.quickstackcraft.QuickStackCraft;
import net.zeronexus.quickstackcraft.client.ModKeybinds;

public class QuickStackCraftFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ModKeybinds.all().forEach(KeyBindingHelper::registerKeyBinding);
        QuickStackCraft.initClient();
    }
}
