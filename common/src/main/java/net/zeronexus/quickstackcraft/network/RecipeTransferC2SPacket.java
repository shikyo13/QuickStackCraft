package net.zeronexus.quickstackcraft.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.zeronexus.quickstackcraft.compat.SlotMask;

/**
 * Client -> Server: request server-validated craft-grid placement for a recipe.
 */
public record RecipeTransferC2SPacket(int containerId, ResourceLocation recipeId, boolean maxTransfer,
                                      long lockedMask) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<RecipeTransferC2SPacket> TYPE =
            new CustomPacketPayload.Type<>(ModNetworking.id("recipe_transfer"));

    public static final StreamCodec<RegistryFriendlyByteBuf, RecipeTransferC2SPacket> CODEC =
            StreamCodec.of(RecipeTransferC2SPacket::encode, RecipeTransferC2SPacket::decode);

    private static void encode(RegistryFriendlyByteBuf buffer, RecipeTransferC2SPacket packet) {
        buffer.writeVarInt(packet.containerId);
        buffer.writeResourceLocation(packet.recipeId);
        buffer.writeBoolean(packet.maxTransfer);
        buffer.writeLong(packet.lockedMask);
    }

    private static RecipeTransferC2SPacket decode(RegistryFriendlyByteBuf buffer) {
        return new RecipeTransferC2SPacket(
                buffer.readVarInt(), buffer.readResourceLocation(),
                buffer.readBoolean(), buffer.readLong());
    }

    public boolean isLocked(int slot) {
        return SlotMask.contains(lockedMask, slot);
    }

    @Override
    public Type<RecipeTransferC2SPacket> type() {
        return TYPE;
    }
}
