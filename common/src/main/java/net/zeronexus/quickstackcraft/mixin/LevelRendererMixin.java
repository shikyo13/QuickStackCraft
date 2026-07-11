package net.zeronexus.quickstackcraft.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.phys.Vec3;
import net.zeronexus.quickstackcraft.client.ContainerHighlightRenderer;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public abstract class LevelRendererMixin {

    @Inject(method = "renderLevel", at = @At("RETURN"))
    private void quickstackcraft$renderContainerHighlights(
            DeltaTracker deltaTracker, boolean renderBlockOutline,
            Camera camera, GameRenderer gameRenderer, LightTexture lightTexture,
            Matrix4f modelView, Matrix4f projection, CallbackInfo ci) {

        if (!ContainerHighlightRenderer.hasHighlights()) return;

        Vec3 cameraPos = camera.getPosition();
        PoseStack poseStack = new PoseStack();
        poseStack.mulPose(modelView);

        MultiBufferSource.BufferSource bufferSource =
                net.minecraft.client.Minecraft.getInstance().renderBuffers().bufferSource();

        ContainerHighlightRenderer.renderHighlights(poseStack, bufferSource, cameraPos);
        bufferSource.endBatch();
    }
}
