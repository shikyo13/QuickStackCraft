package net.zeronexus.quickstackcraft.network;

import net.minecraft.network.FriendlyByteBuf;

public record ConfigRequestC2SPacket(long requestId) implements PacketPayload {

    public static final PacketPayload.Type<ConfigRequestC2SPacket> TYPE =
            new PacketPayload.Type<>(ModNetworking.id("config_request"));

    public static final PacketCodec<FriendlyByteBuf, ConfigRequestC2SPacket> CODEC =
            PacketCodec.of(ConfigRequestC2SPacket::encode, ConfigRequestC2SPacket::decode);

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
