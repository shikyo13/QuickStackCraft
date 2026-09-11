package net.zeronexus.quickstackcraft.network;

import net.minecraft.network.FriendlyByteBuf;

public record OpenContainerTransferC2SPacket(TransferKind kind, int containerId) implements PacketPayload {

    public enum TransferKind {
        MATCHING_ITEMS,
        ALL_ITEMS
    }

    public static final Type<OpenContainerTransferC2SPacket> TYPE =
            new Type<>(ModNetworking.id("open_container_deposit"));

    public static final PacketCodec<FriendlyByteBuf, OpenContainerTransferC2SPacket> CODEC =
            PacketCodec.of(
                    (buffer, value) -> {
                        buffer.writeEnum(value.kind());
                        buffer.writeVarInt(value.containerId());
                    },
                    buffer -> new OpenContainerTransferC2SPacket(
                            buffer.readEnum(TransferKind.class), buffer.readVarInt()));

    @Override
    public Type<OpenContainerTransferC2SPacket> type() {
        return TYPE;
    }
}
