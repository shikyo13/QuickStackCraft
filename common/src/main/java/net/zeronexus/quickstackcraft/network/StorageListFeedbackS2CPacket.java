package net.zeronexus.quickstackcraft.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.zeronexus.quickstackcraft.logic.StorageListState;

public record StorageListFeedbackS2CPacket(
        BlockPos blockPos,
        ResourceLocation blockId,
        StorageListState state) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<StorageListFeedbackS2CPacket> TYPE =
            new CustomPacketPayload.Type<>(ModNetworking.id("storage_list_feedback"));

    public static final StreamCodec<FriendlyByteBuf, StorageListFeedbackS2CPacket> CODEC =
            StreamCodec.of(StorageListFeedbackS2CPacket::encode, StorageListFeedbackS2CPacket::decode);

    private static void encode(FriendlyByteBuf buffer, StorageListFeedbackS2CPacket packet) {
        buffer.writeBlockPos(packet.blockPos);
        buffer.writeResourceLocation(packet.blockId);
        buffer.writeEnum(packet.state);
    }

    private static StorageListFeedbackS2CPacket decode(FriendlyByteBuf buffer) {
        return new StorageListFeedbackS2CPacket(
                buffer.readBlockPos(),
                buffer.readResourceLocation(),
                buffer.readEnum(StorageListState.class));
    }

    @Override
    public Type<StorageListFeedbackS2CPacket> type() {
        return TYPE;
    }
}
