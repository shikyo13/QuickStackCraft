package net.zeronexus.quickstackcraft.network;

import net.minecraft.network.FriendlyByteBuf;

public record TutorialSeenC2SPacket() implements PacketPayload {

    public static final PacketPayload.Type<TutorialSeenC2SPacket> TYPE =
            new PacketPayload.Type<>(ModNetworking.id("tutorial_seen"));

    public static final PacketCodec<FriendlyByteBuf, TutorialSeenC2SPacket> CODEC =
            PacketCodec.of(TutorialSeenC2SPacket::encode, TutorialSeenC2SPacket::decode);

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
