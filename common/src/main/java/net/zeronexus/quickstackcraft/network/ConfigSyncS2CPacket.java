package net.zeronexus.quickstackcraft.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.zeronexus.quickstackcraft.config.QuickStackSettings;

import java.util.Set;

public record ConfigSyncS2CPacket(
        QuickStackSettings.Snapshot settings,
        Set<ResourceLocation> includedTargets,
        Set<ResourceLocation> excludedTargets,
        boolean canEdit,
        long revision,
        long requestId,
        SyncReason reason) implements CustomPacketPayload {

    public enum SyncReason {
        INITIAL,
        RESPONSE,
        SAVED,
        REJECTED
    }

    public static final CustomPacketPayload.Type<ConfigSyncS2CPacket> TYPE =
            new CustomPacketPayload.Type<>(ModNetworking.id("config_sync"));

    public static final StreamCodec<FriendlyByteBuf, ConfigSyncS2CPacket> CODEC =
            StreamCodec.of(ConfigSyncS2CPacket::encode, ConfigSyncS2CPacket::decode);

    private static void encode(FriendlyByteBuf buffer, ConfigSyncS2CPacket packet) {
        ConfigPacketCodecs.writeSnapshot(buffer, packet.settings);
        ConfigPacketCodecs.writeIds(buffer, packet.includedTargets);
        ConfigPacketCodecs.writeIds(buffer, packet.excludedTargets);
        buffer.writeBoolean(packet.canEdit);
        buffer.writeVarLong(packet.revision);
        buffer.writeVarLong(packet.requestId);
        buffer.writeEnum(packet.reason);
    }

    private static ConfigSyncS2CPacket decode(FriendlyByteBuf buffer) {
        return new ConfigSyncS2CPacket(
                ConfigPacketCodecs.readSnapshot(buffer),
                ConfigPacketCodecs.readIds(buffer),
                ConfigPacketCodecs.readIds(buffer),
                buffer.readBoolean(),
                buffer.readVarLong(),
                buffer.readVarLong(),
                buffer.readEnum(SyncReason.class));
    }

    @Override
    public Type<ConfigSyncS2CPacket> type() {
        return TYPE;
    }
}
