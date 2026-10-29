package net.zeronexus.quickstackcraft.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record RecipeBookTransferC2SPacket(RecipeTransferC2SPacket transfer) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<RecipeBookTransferC2SPacket> TYPE =
            new CustomPacketPayload.Type<>(ModNetworking.id("recipe_book_transfer"));

    public static final StreamCodec<RegistryFriendlyByteBuf, RecipeBookTransferC2SPacket> CODEC =
            StreamCodec.of(
                    (buffer, packet) -> RecipeTransferC2SPacket.CODEC.encode(buffer, packet.transfer()),
                    buffer -> new RecipeBookTransferC2SPacket(RecipeTransferC2SPacket.CODEC.decode(buffer)));

    @Override
    public Type<RecipeBookTransferC2SPacket> type() {
        return TYPE;
    }
}
