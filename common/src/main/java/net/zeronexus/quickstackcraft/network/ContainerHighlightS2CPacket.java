package net.zeronexus.quickstackcraft.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.ArrayList;
import java.util.List;

public record ContainerHighlightS2CPacket(List<BlockPos> blockPositions, List<Integer> entityIds) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<ContainerHighlightS2CPacket> TYPE =
            new CustomPacketPayload.Type<>(ModNetworking.id("highlight"));

    public static final StreamCodec<FriendlyByteBuf, ContainerHighlightS2CPacket> CODEC =
            StreamCodec.of(ContainerHighlightS2CPacket::encode, ContainerHighlightS2CPacket::decode);

    private static void encode(FriendlyByteBuf buf, ContainerHighlightS2CPacket pkt) {
        buf.writeVarInt(pkt.blockPositions.size());
        for (BlockPos pos : pkt.blockPositions) {
            buf.writeVarInt(pos.getX());
            buf.writeVarInt(pos.getY());
            buf.writeVarInt(pos.getZ());
        }
        buf.writeVarInt(pkt.entityIds.size());
        for (int id : pkt.entityIds) {
            buf.writeVarInt(id);
        }
    }

    private static ContainerHighlightS2CPacket decode(FriendlyByteBuf buf) {
        int blockCount = buf.readVarInt();
        List<BlockPos> positions = new ArrayList<>(blockCount);
        for (int i = 0; i < blockCount; i++) {
            positions.add(new BlockPos(buf.readVarInt(), buf.readVarInt(), buf.readVarInt()));
        }
        int entityCount = buf.readVarInt();
        List<Integer> entityIds = new ArrayList<>(entityCount);
        for (int i = 0; i < entityCount; i++) {
            entityIds.add(buf.readVarInt());
        }
        return new ContainerHighlightS2CPacket(positions, entityIds);
    }

    @Override
    public Type<ContainerHighlightS2CPacket> type() {
        return TYPE;
    }
}
