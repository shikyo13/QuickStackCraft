package net.zeronexus.quickstackcraft.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record TutorialStatusS2CPacket(boolean seen) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<TutorialStatusS2CPacket> TYPE =
            new CustomPacketPayload.Type<>(ModNetworking.id("tutorial_status"));

    public static final StreamCodec<FriendlyByteBuf, TutorialStatusS2CPacket> CODEC =
            StreamCodec.of(TutorialStatusS2CPacket::encode, TutorialStatusS2CPacket::decode);

    private static void encode(FriendlyByteBuf buffer, TutorialStatusS2CPacket packet) {
        buffer.writeBoolean(packet.seen);
    }

    private static TutorialStatusS2CPacket decode(FriendlyByteBuf buffer) {
        return new TutorialStatusS2CPacket(buffer.readBoolean());
    }

    @Override
    public Type<TutorialStatusS2CPacket> type() {
        return TYPE;
    }
}
