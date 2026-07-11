package net.zeronexus.quickstackcraft.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record OpenContainerTransferC2SPacket(TransferKind kind) implements CustomPacketPayload {

    public enum TransferKind {
        MATCHING_ITEMS,
        ALL_ITEMS
    }

    public static final Type<OpenContainerTransferC2SPacket> TYPE =
            new Type<>(ModNetworking.id("open_container_deposit"));

    public static final StreamCodec<FriendlyByteBuf, OpenContainerTransferC2SPacket> CODEC =
            StreamCodec.of(
                    (buffer, value) -> buffer.writeEnum(value.kind()),
                    buffer -> new OpenContainerTransferC2SPacket(buffer.readEnum(TransferKind.class)));

    @Override
    public Type<OpenContainerTransferC2SPacket> type() {
        return TYPE;
    }
}
