package net.zeronexus.quickstackcraft.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.zeronexus.quickstackcraft.config.QuickStackSettings;

import java.util.HashSet;
import java.util.Comparator;
import java.util.Set;

final class ConfigPacketCodecs {

    private static final int MAX_TARGET_IDS = 4096;
    private static final int MAX_TARGET_ID_LENGTH = 256;

    private ConfigPacketCodecs() {}

    static void writeSnapshot(FriendlyByteBuf buffer, QuickStackSettings.Snapshot snapshot) {
        QuickStackSettings.Snapshot sanitized = QuickStackSettings.sanitize(snapshot);
        buffer.writeUtf(sanitized.storageDetection().name());
        buffer.writeBoolean(sanitized.capacityFallbackEnabled());
        buffer.writeVarInt(sanitized.capacityFallbackThreshold());
        buffer.writeVarInt(sanitized.searchRadius());
        buffer.writeUtf(sanitized.outlineRgb());
        buffer.writeDouble(sanitized.outlineOpacity());
        buffer.writeVarInt(sanitized.outlineLifetimeMs());
    }

    static QuickStackSettings.Snapshot readSnapshot(FriendlyByteBuf buffer) {
        QuickStackSettings.StorageDetection detection;
        try {
            detection = QuickStackSettings.StorageDetection.valueOf(buffer.readUtf(64));
        } catch (IllegalArgumentException ignored) {
            detection = QuickStackSettings.StorageDetection.RECOGNIZED_STORAGE;
        }
        return QuickStackSettings.sanitize(new QuickStackSettings.Snapshot(
                detection,
                buffer.readBoolean(),
                buffer.readVarInt(),
                buffer.readVarInt(),
                buffer.readUtf(16),
                buffer.readDouble(),
                buffer.readVarInt()));
    }

    static void writeIds(FriendlyByteBuf buffer, Set<ResourceLocation> ids) {
        var sortedIds = ids.stream()
                .sorted(Comparator.comparing(ResourceLocation::toString))
                .filter(id -> id.toString().length() <= MAX_TARGET_ID_LENGTH)
                .limit(MAX_TARGET_IDS)
                .toList();
        buffer.writeVarInt(sortedIds.size());
        for (ResourceLocation id : sortedIds) {
            buffer.writeUtf(id.toString());
        }
    }

    static Set<ResourceLocation> readIds(FriendlyByteBuf buffer) {
        int count = buffer.readVarInt();
        if (count < 0 || count > MAX_TARGET_IDS) {
            throw new IllegalArgumentException("Invalid target ID count: " + count);
        }
        Set<ResourceLocation> ids = new HashSet<>(count);
        for (int i = 0; i < count; i++) {
            ResourceLocation id = ResourceLocation.tryParse(buffer.readUtf(MAX_TARGET_ID_LENGTH));
            if (id != null) {
                ids.add(id);
            }
        }
        return ids;
    }
}
