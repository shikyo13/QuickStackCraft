package net.zeronexus.quickstackcraft.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.zeronexus.quickstackcraft.compat.SlotMask;

public record QuickStackSlotC2SPacket(int containerId, int stateId, int inventorySlot,
                                     long externalProtection) implements CustomPacketPayload {

    public static final Type<QuickStackSlotC2SPacket> TYPE =
            new Type<>(ModNetworking.id("quick_stack_slot"));

    public static final StreamCodec<FriendlyByteBuf, QuickStackSlotC2SPacket> CODEC = StreamCodec.of(
            (buffer, value) -> {
                buffer.writeVarInt(value.containerId());
                buffer.writeVarInt(value.stateId());
                buffer.writeVarInt(value.inventorySlot());
                buffer.writeLong(value.externalProtection());
            },
            buffer -> new QuickStackSlotC2SPacket(buffer.readVarInt(), buffer.readVarInt(),
                    buffer.readVarInt(), buffer.readLong()));

    public boolean protects(int slot) {
        return SlotMask.contains(externalProtection, slot);
    }

    @Override
    public Type<QuickStackSlotC2SPacket> type() {
        return TYPE;
    }
}
