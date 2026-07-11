package net.zeronexus.quickstackcraft.client.tutorial;

import net.minecraft.network.chat.Component;

public interface TutorialScene {
    Component title();

    double durationSeconds();

    Component caption(double timeSeconds);

    void render(TutorialRenderContext context, double timeSeconds);
}
