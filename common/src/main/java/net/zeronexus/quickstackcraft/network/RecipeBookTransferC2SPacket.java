package net.zeronexus.quickstackcraft.network;

import net.minecraft.network.FriendlyByteBuf;

public record RecipeBookTransferC2SPacket(RecipeTransferC2SPacket transfer) implements PacketPayload {
    public static final PacketPayload.Type<RecipeBookTransferC2SPacket> TYPE =
            new PacketPayload.Type<>(ModNetworking.id("recipe_book_transfer"));

    public static final PacketCodec<FriendlyByteBuf, RecipeBookTransferC2SPacket> CODEC =
            PacketCodec.of(
                    (buffer, packet) -> RecipeTransferC2SPacket.CODEC.encode(buffer, packet.transfer()),
                    buffer -> new RecipeBookTransferC2SPacket(RecipeTransferC2SPacket.CODEC.decode(buffer)));

    @Override
    public Type<RecipeBookTransferC2SPacket> type() {
        return TYPE;
    }
}
