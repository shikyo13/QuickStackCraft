package net.zeronexus.quickstackcraft.network;

import net.minecraft.network.FriendlyByteBuf;

public record TutorialStatusS2CPacket(boolean seen) implements PacketPayload {

    public static final PacketPayload.Type<TutorialStatusS2CPacket> TYPE =
            new PacketPayload.Type<>(ModNetworking.id("tutorial_status"));

    public static final PacketCodec<FriendlyByteBuf, TutorialStatusS2CPacket> CODEC =
            PacketCodec.of(TutorialStatusS2CPacket::encode, TutorialStatusS2CPacket::decode);

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
