package net.zeronexus.quickstackcraft.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.zeronexus.quickstackcraft.compat.SlotMask;

/**
 * Client -> Server: request server-validated craft-grid placement for a recipe.
 */
public record RecipeTransferC2SPacket(int containerId, ResourceLocation recipeId, boolean maxTransfer,
                                      long lockedMask) implements PacketPayload {

    public static final PacketPayload.Type<RecipeTransferC2SPacket> TYPE =
            new PacketPayload.Type<>(ModNetworking.id("recipe_transfer"));

    public static final PacketCodec<FriendlyByteBuf, RecipeTransferC2SPacket> CODEC =
            PacketCodec.of(RecipeTransferC2SPacket::encode, RecipeTransferC2SPacket::decode);

    private static void encode(FriendlyByteBuf buffer, RecipeTransferC2SPacket packet) {
        buffer.writeVarInt(packet.containerId);
        buffer.writeResourceLocation(packet.recipeId);
        buffer.writeBoolean(packet.maxTransfer);
        buffer.writeLong(packet.lockedMask);
    }

    private static RecipeTransferC2SPacket decode(FriendlyByteBuf buffer) {
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
