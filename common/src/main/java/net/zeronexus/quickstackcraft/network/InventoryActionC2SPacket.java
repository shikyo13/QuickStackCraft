package net.zeronexus.quickstackcraft.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.zeronexus.quickstackcraft.compat.SlotMask;

public record InventoryActionC2SPacket(Action action, long externalProtection) implements CustomPacketPayload {

    public enum Action {
        QUICK_STACK,
        DUMP
    }

    public static final Type<InventoryActionC2SPacket> TYPE =
            new Type<>(ModNetworking.id("inventory_action"));

    public static final StreamCodec<FriendlyByteBuf, InventoryActionC2SPacket> CODEC =
            StreamCodec.of(
                    (buffer, value) -> {
                        buffer.writeEnum(value.action());
                        buffer.writeLong(value.externalProtection());
                    },
                    buffer -> new InventoryActionC2SPacket(
                            buffer.readEnum(Action.class), buffer.readLong()));

    public boolean protects(int inventorySlot) {
        return SlotMask.contains(externalProtection, inventorySlot);
    }

    @Override
    public Type<InventoryActionC2SPacket> type() {
        return TYPE;
    }
}
