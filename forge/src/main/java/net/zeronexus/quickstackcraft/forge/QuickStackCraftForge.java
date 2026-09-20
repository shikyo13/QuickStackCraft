package net.zeronexus.quickstackcraft.forge;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.zeronexus.quickstackcraft.QuickStackCraft;
import net.zeronexus.quickstackcraft.command.QuickStackCommands;
import net.zeronexus.quickstackcraft.config.QuickStackSettings;
import net.zeronexus.quickstackcraft.logic.StorageBlockLists;
import net.zeronexus.quickstackcraft.logic.forge.FavoritesManagerImpl;
import net.zeronexus.quickstackcraft.network.ModNetworking;
import net.minecraft.server.level.ServerPlayer;

@Mod(QuickStackCraft.MOD_ID)
public class QuickStackCraftForge {

    public QuickStackCraftForge() {
        QuickStackSettings.load(FMLPaths.CONFIGDIR.get().resolve("quickstackcraft.properties"));
        StorageBlockLists.load(FMLPaths.CONFIGDIR.get().resolve("quickstackcraft-storage-lists.properties"));

        QuickStackCraft.init();

        // Game event: sync favorites on login
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.addListener(this::onPlayerLogin);
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.addListener(this::onRegisterCommands);
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
