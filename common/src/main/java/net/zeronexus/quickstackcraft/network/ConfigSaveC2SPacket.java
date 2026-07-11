package net.zeronexus.quickstackcraft.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.zeronexus.quickstackcraft.config.QuickStackSettings;

import java.util.Set;

public record ConfigSaveC2SPacket(
        QuickStackSettings.Snapshot settings,
        Set<ResourceLocation> includedTargets,
        Set<ResourceLocation> excludedTargets,
        long requestId,
        long baseRevision) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<ConfigSaveC2SPacket> TYPE =
            new CustomPacketPayload.Type<>(ModNetworking.id("config_save"));

    public static final StreamCodec<FriendlyByteBuf, ConfigSaveC2SPacket> CODEC =
            StreamCodec.of(ConfigSaveC2SPacket::encode, ConfigSaveC2SPacket::decode);

    private static void encode(FriendlyByteBuf buffer, ConfigSaveC2SPacket packet) {
        ConfigPacketCodecs.writeSnapshot(buffer, packet.settings);
        ConfigPacketCodecs.writeIds(buffer, packet.includedTargets);
        ConfigPacketCodecs.writeIds(buffer, packet.excludedTargets);
        buffer.writeVarLong(packet.requestId);
        buffer.writeVarLong(packet.baseRevision);
    }

    private static ConfigSaveC2SPacket decode(FriendlyByteBuf buffer) {
        return new ConfigSaveC2SPacket(
                ConfigPacketCodecs.readSnapshot(buffer),
                ConfigPacketCodecs.readIds(buffer),
                ConfigPacketCodecs.readIds(buffer),
                buffer.readVarLong(),
                buffer.readVarLong());
    }

    @Override
    public Type<ConfigSaveC2SPacket> type() {
        return TYPE;
    }
}
