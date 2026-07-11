package net.zeronexus.quickstackcraft.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record ConfigRequestC2SPacket(long requestId) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<ConfigRequestC2SPacket> TYPE =
            new CustomPacketPayload.Type<>(ModNetworking.id("config_request"));

    public static final StreamCodec<FriendlyByteBuf, ConfigRequestC2SPacket> CODEC =
            StreamCodec.of(ConfigRequestC2SPacket::encode, ConfigRequestC2SPacket::decode);

    private static void encode(FriendlyByteBuf buffer, ConfigRequestC2SPacket packet) {
        buffer.writeVarLong(packet.requestId);
    }

    private static ConfigRequestC2SPacket decode(FriendlyByteBuf buffer) {
        return new ConfigRequestC2SPacket(buffer.readVarLong());
    }

    @Override
    public Type<ConfigRequestC2SPacket> type() {
        return TYPE;
    }
}
