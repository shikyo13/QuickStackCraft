package net.zeronexus.quickstackcraft.network;

import net.minecraft.network.FriendlyByteBuf;

/**
 * Client -> Server: request a scan of nearby containers to report available items.
 * Empty payload - the server uses the player's position for scanning.
 */
public record NearbyItemsScanC2SPacket() implements PacketPayload {

    public static final PacketPayload.Type<NearbyItemsScanC2SPacket> TYPE =
            new PacketPayload.Type<>(ModNetworking.id("nearby_scan"));

    public static final PacketCodec<FriendlyByteBuf, NearbyItemsScanC2SPacket> CODEC =
            PacketCodec.of(
                    (buf, pkt) -> {},
                    buf -> new NearbyItemsScanC2SPacket()
            );

    @Override
    public Type<NearbyItemsScanC2SPacket> type() {
        return TYPE;
    }
}
