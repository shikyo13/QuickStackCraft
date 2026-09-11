package net.zeronexus.quickstackcraft.network;

import net.minecraft.network.FriendlyByteBuf;

public record StorageListActionC2SPacket(Action action) implements PacketPayload {

    public enum Action {
        SHOW_NEARBY,
        CYCLE_LOOKED_AT
    }

    public static final Type<StorageListActionC2SPacket> TYPE =
            new Type<>(ModNetworking.id("storage_list_action"));

    public static final PacketCodec<FriendlyByteBuf, StorageListActionC2SPacket> CODEC =
            PacketCodec.of(
                    (buffer, value) -> buffer.writeEnum(value.action()),
                    buffer -> new StorageListActionC2SPacket(buffer.readEnum(Action.class)));

    @Override
    public Type<StorageListActionC2SPacket> type() {
        return TYPE;
    }
}
