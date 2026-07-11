package net.zeronexus.quickstackcraft.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record TutorialSeenC2SPacket() implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<TutorialSeenC2SPacket> TYPE =
            new CustomPacketPayload.Type<>(ModNetworking.id("tutorial_seen"));

    public static final StreamCodec<FriendlyByteBuf, TutorialSeenC2SPacket> CODEC =
            StreamCodec.of(TutorialSeenC2SPacket::encode, TutorialSeenC2SPacket::decode);

    private static void encode(FriendlyByteBuf buffer, TutorialSeenC2SPacket packet) {
    }

    private static TutorialSeenC2SPacket decode(FriendlyByteBuf buffer) {
        return new TutorialSeenC2SPacket();
    }

    @Override
    public Type<TutorialSeenC2SPacket> type() {
        return TYPE;
    }
}
