package net.zeronexus.quickstackcraft.logic.fabric;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class FavoritesManagerImpl {

    private FavoritesManagerImpl() {}

    public static boolean isFavorited(Player player, int slot) {
        return getStorage(player).isFavorited(player.getUUID(), slot);
    }

    public static void toggle(Player player, int slot) {
        getStorage(player).toggle(player.getUUID(), slot);
    }

    public static Set<Integer> getAll(Player player) {
        return getStorage(player).getAll(player.getUUID());
    }

    public static void setAll(Player player, Set<Integer> slots) {
        getStorage(player).setAll(player.getUUID(), slots);
    }

    private static FavoritesSavedData getStorage(Player player) {
        if (player instanceof ServerPlayer sp) {
            MinecraftServer server = sp.getServer();
            if (server != null) {
                ServerLevel overworld = server.overworld();
                return overworld.getDataStorage().computeIfAbsent(
                        FavoritesSavedData.factory(), "quickstackcraft_favorites"
                );
            }
        }
        return FavoritesSavedData.EMPTY;
    }

    public static class FavoritesSavedData extends SavedData {
        private final Map<UUID, Set<Integer>> data = new HashMap<>();
        static final FavoritesSavedData EMPTY = new FavoritesSavedData();

        public static SavedData.Factory<FavoritesSavedData> factory() {
            return new SavedData.Factory<>(FavoritesSavedData::new, FavoritesSavedData::load, null);
        }

        boolean isFavorited(UUID playerId, int slot) {
            return data.getOrDefault(playerId, Set.of()).contains(slot);
        }

        void toggle(UUID playerId, int slot) {
            Set<Integer> slots = data.computeIfAbsent(playerId, k -> new HashSet<>());
            if (slots.contains(slot)) {
                slots.remove(slot);
            } else {
                slots.add(slot);
            }
            setDirty();
        }

        Set<Integer> getAll(UUID playerId) {
            return Set.copyOf(data.getOrDefault(playerId, Set.of()));
        }

        void setAll(UUID playerId, Set<Integer> slots) {
            data.put(playerId, new HashSet<>(slots));
            setDirty();
        }

        @Override
        public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
            for (Map.Entry<UUID, Set<Integer>> entry : data.entrySet()) {
                ListTag list = new ListTag();
                for (int slot : entry.getValue()) {
                    list.add(IntTag.valueOf(slot));
                }
                tag.put(entry.getKey().toString(), list);
            }
            return tag;
        }

        public static FavoritesSavedData load(CompoundTag tag, HolderLookup.Provider registries) {
            FavoritesSavedData savedData = new FavoritesSavedData();
            for (String key : tag.getAllKeys()) {
                UUID uuid = UUID.fromString(key);
                ListTag list = tag.getList(key, Tag.TAG_INT);
                Set<Integer> slots = new HashSet<>();
                for (int i = 0; i < list.size(); i++) {
                    slots.add(list.getInt(i));
                }
                savedData.data.put(uuid, slots);
            }
            return savedData;
        }
    }
}
