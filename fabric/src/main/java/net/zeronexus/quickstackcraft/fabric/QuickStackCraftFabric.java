package net.zeronexus.quickstackcraft.fabric;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.zeronexus.quickstackcraft.QuickStackCraft;
import net.zeronexus.quickstackcraft.command.QuickStackCommands;
import net.zeronexus.quickstackcraft.config.QuickStackSettings;
import net.zeronexus.quickstackcraft.logic.StorageBlockLists;
import net.zeronexus.quickstackcraft.network.ModNetworking;

import java.nio.file.Path;

public class QuickStackCraftFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        Path configDir = FabricLoader.getInstance().getConfigDir();
        QuickStackSettings.load(configDir.resolve("quickstackcraft.properties"));
        StorageBlockLists.load(configDir.resolve("quickstackcraft-storage-lists.properties"));

        QuickStackCraft.init();
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                QuickStackCommands.register(dispatcher));

        // Sync favorites to player on login
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ModNetworking.syncFavoritesToPlayer(handler.getPlayer());
            ModNetworking.syncConfigToPlayer(handler.getPlayer());
            ModNetworking.syncTutorialToPlayer(handler.getPlayer());
        });
    }
}
