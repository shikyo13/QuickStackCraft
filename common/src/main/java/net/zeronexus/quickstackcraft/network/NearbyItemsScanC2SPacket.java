package net.zeronexus.quickstackcraft.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * Client -> Server: request a scan of nearby containers to report available items.
 * Empty payload - the server uses the player's position for scanning.
 */
public record NearbyItemsScanC2SPacket() implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<NearbyItemsScanC2SPacket> TYPE =
            new CustomPacketPayload.Type<>(ModNetworking.id("nearby_scan"));

    public static final StreamCodec<FriendlyByteBuf, NearbyItemsScanC2SPacket> CODEC =
            StreamCodec.of(
                    (buf, pkt) -> {},
                    buf -> new NearbyItemsScanC2SPacket()
            );

    @Override
    public Type<NearbyItemsScanC2SPacket> type() {
        return TYPE;
    }
}
