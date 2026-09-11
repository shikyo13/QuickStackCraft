package net.zeronexus.quickstackcraft.network;

import net.minecraft.network.FriendlyByteBuf;

public record FavoriteToggleC2SPacket(int slot) implements PacketPayload {

    public static final PacketPayload.Type<FavoriteToggleC2SPacket> TYPE =
            new PacketPayload.Type<>(ModNetworking.id("toggle_fav"));

    public static final PacketCodec<FriendlyByteBuf, FavoriteToggleC2SPacket> CODEC =
            PacketCodec.of(
                    (buffer, packet) -> buffer.writeVarInt(packet.slot()),
                    buffer -> new FavoriteToggleC2SPacket(buffer.readVarInt()));

    @Override
    public Type<FavoriteToggleC2SPacket> type() {
        return TYPE;
    }
}
