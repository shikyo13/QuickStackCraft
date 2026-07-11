package net.zeronexus.quickstackcraft.network;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.item.Item;

import java.util.HashMap;
import java.util.Map;

/**
 * Server -> Client: reports available items in nearby containers.
 * Carries a map of Item -> total count across all nearby containers.
 */
public record NearbyItemsSyncS2CPacket(Map<Item, Integer> items) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<NearbyItemsSyncS2CPacket> TYPE =
            new CustomPacketPayload.Type<>(ModNetworking.id("nearby_items"));

    public static final StreamCodec<RegistryFriendlyByteBuf, NearbyItemsSyncS2CPacket> CODEC =
            StreamCodec.of(NearbyItemsSyncS2CPacket::encode, NearbyItemsSyncS2CPacket::decode);

    private static void encode(RegistryFriendlyByteBuf buf, NearbyItemsSyncS2CPacket pkt) {
        buf.writeVarInt(pkt.items.size());
        for (Map.Entry<Item, Integer> entry : pkt.items.entrySet()) {
            buf.writeVarInt(BuiltInRegistries.ITEM.getId(entry.getKey()));
            buf.writeVarInt(entry.getValue());
        }
    }

    private static NearbyItemsSyncS2CPacket decode(RegistryFriendlyByteBuf buf) {
        int size = buf.readVarInt();
        Map<Item, Integer> items = new HashMap<>(size);
        for (int i = 0; i < size; i++) {
            Item item = BuiltInRegistries.ITEM.byId(buf.readVarInt());
            int count = buf.readVarInt();
            items.put(item, count);
        }
        return new NearbyItemsSyncS2CPacket(items);
    }

    @Override
    public Type<NearbyItemsSyncS2CPacket> type() {
        return TYPE;
    }
}
