package net.zeronexus.quickstackcraft.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record StorageListActionC2SPacket(Action action) implements CustomPacketPayload {

    public enum Action {
        SHOW_NEARBY,
        CYCLE_LOOKED_AT
    }

    public static final Type<StorageListActionC2SPacket> TYPE =
            new Type<>(ModNetworking.id("storage_list_action"));

    public static final StreamCodec<FriendlyByteBuf, StorageListActionC2SPacket> CODEC =
            StreamCodec.of(
                    (buffer, value) -> buffer.writeEnum(value.action()),
                    buffer -> new StorageListActionC2SPacket(buffer.readEnum(Action.class)));

    @Override
    public Type<StorageListActionC2SPacket> type() {
        return TYPE;
    }
}
