package net.zeronexus.quickstackcraft.logic.fabric;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public final class TutorialProgressManagerImpl {

    private TutorialProgressManagerImpl() {}

    public static boolean hasSeen(Player player) {
        return storage(player).hasSeen(player.getUUID());
    }

    public static void markSeen(Player player) {
        storage(player).markSeen(player.getUUID());
    }

    private static TutorialSavedData storage(Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            MinecraftServer server = serverPlayer.getServer();
            if (server != null) {
                ServerLevel overworld = server.overworld();
                return overworld.getDataStorage().computeIfAbsent(
                        TutorialSavedData.factory(), "quickstackcraft_tutorial");
            }
        }
        return TutorialSavedData.EMPTY;
    }

    static final class TutorialSavedData extends SavedData {
        private static final TutorialSavedData EMPTY = new TutorialSavedData();
        private final Set<UUID> seenPlayers = new HashSet<>();

        static SavedData.Factory<TutorialSavedData> factory() {
            return new SavedData.Factory<>(TutorialSavedData::new, TutorialSavedData::load, null);
        }

        boolean hasSeen(UUID playerId) {
            return seenPlayers.contains(playerId);
        }

        void markSeen(UUID playerId) {
            if (seenPlayers.add(playerId)) {
                setDirty();
            }
        }

        @Override
        public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
            for (UUID playerId : seenPlayers) {
                tag.putBoolean(playerId.toString(), true);
            }
            return tag;
        }

        static TutorialSavedData load(CompoundTag tag, HolderLookup.Provider registries) {
            TutorialSavedData data = new TutorialSavedData();
            for (String key : tag.getAllKeys()) {
                try {
                    if (tag.getBoolean(key)) {
                        data.seenPlayers.add(UUID.fromString(key));
                    }
                } catch (IllegalArgumentException ignored) {
                }
            }
            return data;
        }
    }
}
