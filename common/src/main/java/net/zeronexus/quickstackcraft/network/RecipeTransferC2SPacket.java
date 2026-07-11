package net.zeronexus.quickstackcraft.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.display.RecipeDisplayId;
import net.zeronexus.quickstackcraft.compat.SlotMask;
import org.jetbrains.annotations.Nullable;

/**
 * Client -> Server: request server-validated craft-grid placement for a recipe.
 */
public record RecipeTransferC2SPacket(int containerId,
                                      @Nullable RecipeDisplayId displayId,
                                      @Nullable ResourceLocation recipeId,
                                      boolean maxTransfer,
                                      long lockedMask) implements CustomPacketPayload {

    public RecipeTransferC2SPacket {
        if ((displayId == null) == (recipeId == null)) {
            throw new IllegalArgumentException("Exactly one recipe identifier is required");
        }
    }

    public static RecipeTransferC2SPacket forDisplay(int containerId, RecipeDisplayId displayId,
                                                     boolean maxTransfer, long lockedMask) {
        return new RecipeTransferC2SPacket(
                containerId, displayId, null, maxTransfer, lockedMask);
    }

    public static RecipeTransferC2SPacket forRecipe(int containerId, ResourceLocation recipeId,
                                                    boolean maxTransfer, long lockedMask) {
        return new RecipeTransferC2SPacket(
                containerId, null, recipeId, maxTransfer, lockedMask);
    }

    public static final CustomPacketPayload.Type<RecipeTransferC2SPacket> TYPE =
            new CustomPacketPayload.Type<>(ModNetworking.id("recipe_transfer"));

    public static final StreamCodec<RegistryFriendlyByteBuf, RecipeTransferC2SPacket> CODEC =
            StreamCodec.of(RecipeTransferC2SPacket::encode, RecipeTransferC2SPacket::decode);

    private static void encode(RegistryFriendlyByteBuf buffer, RecipeTransferC2SPacket packet) {
        buffer.writeVarInt(packet.containerId);
        buffer.writeBoolean(packet.displayId != null);
        if (packet.displayId != null) {
            buffer.writeVarInt(packet.displayId.index());
        } else {
            buffer.writeResourceLocation(packet.recipeId);
        }
        buffer.writeBoolean(packet.maxTransfer);
        buffer.writeLong(packet.lockedMask);
    }

    private static RecipeTransferC2SPacket decode(RegistryFriendlyByteBuf buffer) {
        int containerId = buffer.readVarInt();
        boolean display = buffer.readBoolean();
        RecipeDisplayId displayId = display ? new RecipeDisplayId(buffer.readVarInt()) : null;
        ResourceLocation recipeId = display ? null : buffer.readResourceLocation();
        return new RecipeTransferC2SPacket(
                containerId, displayId, recipeId, buffer.readBoolean(), buffer.readLong());
    }

    public boolean isLocked(int slot) {
        return SlotMask.contains(lockedMask, slot);
    }

    @Override
    public Type<RecipeTransferC2SPacket> type() {
        return TYPE;
    }
}
