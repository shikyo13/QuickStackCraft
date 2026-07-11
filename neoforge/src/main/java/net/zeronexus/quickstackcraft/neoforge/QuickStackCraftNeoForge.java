package net.zeronexus.quickstackcraft.neoforge;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.zeronexus.quickstackcraft.QuickStackCraft;
import net.zeronexus.quickstackcraft.client.ModKeybinds;
import net.zeronexus.quickstackcraft.command.QuickStackCommands;
import net.zeronexus.quickstackcraft.config.QuickStackSettings;
import net.zeronexus.quickstackcraft.logic.StorageBlockLists;
import net.zeronexus.quickstackcraft.logic.neoforge.FavoritesManagerImpl;
import net.zeronexus.quickstackcraft.network.ModNetworking;
import net.minecraft.server.level.ServerPlayer;

@Mod(QuickStackCraft.MOD_ID)
public class QuickStackCraftNeoForge {

    public QuickStackCraftNeoForge(IEventBus modEventBus) {
        QuickStackSettings.load(FMLPaths.CONFIGDIR.get().resolve("quickstackcraft.properties"));
        StorageBlockLists.load(FMLPaths.CONFIGDIR.get().resolve("quickstackcraft-storage-lists.properties"));

        QuickStackCraft.init();

        // Register attachment types
        FavoritesManagerImpl.ATTACHMENTS.register(modEventBus);

        // Register keybinds and client events
        modEventBus.addListener(this::registerKeybinds);
        modEventBus.addListener(this::onClientSetup);

        // Game event: sync favorites on login
        NeoForge.EVENT_BUS.addListener(this::onPlayerLogin);
        NeoForge.EVENT_BUS.addListener(this::onRegisterCommands);
    }

    private void onClientSetup(FMLClientSetupEvent event) {
        QuickStackCraft.initClient();
    }

    private void registerKeybinds(RegisterKeyMappingsEvent event) {
        ModKeybinds.all().forEach(event::register);
    }

    private void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer sp) {
            ModNetworking.syncFavoritesToPlayer(sp);
            ModNetworking.syncConfigToPlayer(sp);
            ModNetworking.syncTutorialToPlayer(sp);
        }
    }

    private void onRegisterCommands(RegisterCommandsEvent event) {
        QuickStackCommands.register(event.getDispatcher());
    }
}
