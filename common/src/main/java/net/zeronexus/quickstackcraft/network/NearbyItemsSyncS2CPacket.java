package net.zeronexus.quickstackcraft.network;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.Item;

import java.util.HashMap;
import java.util.Map;

/**
 * Server -> Client: reports available items in nearby containers.
 * Carries a map of Item -> total count across all nearby containers.
 */
public record NearbyItemsSyncS2CPacket(Map<Item, Integer> items) implements PacketPayload {

    public static final PacketPayload.Type<NearbyItemsSyncS2CPacket> TYPE =
            new PacketPayload.Type<>(ModNetworking.id("nearby_items"));

    public static final PacketCodec<FriendlyByteBuf, NearbyItemsSyncS2CPacket> CODEC =
            PacketCodec.of(NearbyItemsSyncS2CPacket::encode, NearbyItemsSyncS2CPacket::decode);

    private static void encode(FriendlyByteBuf buf, NearbyItemsSyncS2CPacket pkt) {
        buf.writeVarInt(pkt.items.size());
        for (Map.Entry<Item, Integer> entry : pkt.items.entrySet()) {
            buf.writeVarInt(BuiltInRegistries.ITEM.getId(entry.getKey()));
            buf.writeVarInt(entry.getValue());
        }
    }

    private static NearbyItemsSyncS2CPacket decode(FriendlyByteBuf buf) {
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
