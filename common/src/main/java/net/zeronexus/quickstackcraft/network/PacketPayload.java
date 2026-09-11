package net.zeronexus.quickstackcraft.network;

import net.minecraft.resources.ResourceLocation;

public interface PacketPayload {
    Type<?> type();
    record Type<T extends PacketPayload>(ResourceLocation id) {}
}
