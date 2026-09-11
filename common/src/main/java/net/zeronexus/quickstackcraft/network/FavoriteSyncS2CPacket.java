package net.zeronexus.quickstackcraft.network;

import net.minecraft.network.FriendlyByteBuf;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public record FavoriteSyncS2CPacket(Set<Integer> slots) implements PacketPayload {

    public static final PacketPayload.Type<FavoriteSyncS2CPacket> TYPE =
            new PacketPayload.Type<>(ModNetworking.id("sync_favs"));

    public static final PacketCodec<FriendlyByteBuf, FavoriteSyncS2CPacket> CODEC =
            PacketCodec.of(
                    (buffer, packet) -> buffer.writeCollection(packet.slots(), FriendlyByteBuf::writeVarInt),
                    buffer -> new FavoriteSyncS2CPacket(new HashSet<>(buffer.readList(FriendlyByteBuf::readVarInt))));

    @Override
    public Type<FavoriteSyncS2CPacket> type() {
        return TYPE;
    }
}
