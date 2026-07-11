package net.zeronexus.quickstackcraft.client.tutorial;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.model.ChestModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;

final class TutorialGraphicsBridge {

    private static final ModelPart CHEST_ROOT = ChestModel.createSingleBodyLayer().bakeRoot();
    private static final ModelPart CHEST_LID = CHEST_ROOT.getChild("lid");
    private static final ModelPart CHEST_BOTTOM = CHEST_ROOT.getChild("bottom");
    private static final ModelPart CHEST_LOCK = CHEST_ROOT.getChild("lock");

    private TutorialGraphicsBridge() {}

    static void blit(
            GuiGraphics graphics,
            ResourceLocation texture,
            int x,
            int y,
            int u,
            int v,
            int width,
            int height,
            int textureWidth,
            int textureHeight) {
        graphics.blit(RenderType::guiTextured, texture, x, y,
                (float) u, (float) v, width, height, textureWidth, textureHeight);
    }

    static void blitSprite(
            GuiGraphics graphics, ResourceLocation sprite, int x, int y, int width, int height) {
        graphics.blitSprite(RenderType::guiTextured, sprite, x, y, width, height);
    }

    static void blitRegionScaled(
            GuiGraphics graphics,
            ResourceLocation texture,
            int x,
            int y,
            int width,
            int height,
            int sourceX,
            int sourceY,
            int sourceWidth,
            int sourceHeight,
            int textureWidth,
            int textureHeight) {
        PoseStack pose = graphics.pose();
        pose.pushPose();
        pose.translate(x, y, 0.0F);
        pose.scale(width / (float) sourceWidth, height / (float) sourceHeight, 1.0F);
        blit(graphics, texture, 0, 0, sourceX, sourceY,
                sourceWidth, sourceHeight, textureWidth, textureHeight);
        pose.popPose();
    }

    static TutorialRenderContext.Bounds renderChest(
            GuiGraphics graphics, int centerX, int groundY, float size, float openness) {
        resetDepth(graphics);
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(true);

        var bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
        float clampedOpenness = Math.max(0.0F, Math.min(1.0F, openness));
        CHEST_LID.xRot = -(float) (Math.PI * 0.5D * clampedOpenness);
        CHEST_LOCK.xRot = CHEST_LID.xRot;

        float[] unanchored = projectChestExtents(0.0F, 0.0F, size);
        float originX = centerX - (unanchored[0] + unanchored[2]) * 0.5F;
        float originY = groundY - unanchored[3];
        TutorialRenderContext.Bounds bounds = boundsFromExtents(
                projectChestExtents(originX, originY, size));

        PoseStack pose = graphics.pose();
        pose.pushPose();
        transformChest(pose, originX, originY, size, 180.0F);
        VertexConsumer consumer = Sheets.CHEST_LOCATION.buffer(
                bufferSource, RenderType::entityCutoutNoCull);
        CHEST_BOTTOM.render(pose, consumer, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY);
        CHEST_LID.render(pose, consumer, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY);
        CHEST_LOCK.render(pose, consumer, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY);
        bufferSource.endBatch();
        pose.popPose();
        resetDepth(graphics);
        return bounds;
    }

    private static float[] projectChestExtents(float originX, float originY, float size) {
        PoseStack pose = new PoseStack();
        transformChest(pose, originX, originY, size, 0.0F);
        float[] extents = {
                Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY,
                Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY
        };
        CHEST_ROOT.visit(pose, (partPose, path, cubeIndex, cube) -> {
            float[] xs = {cube.minX / 16.0F, cube.maxX / 16.0F};
            float[] ys = {cube.minY / 16.0F, cube.maxY / 16.0F};
            float[] zs = {cube.minZ / 16.0F, cube.maxZ / 16.0F};
            for (float x : xs) {
                for (float y : ys) {
                    for (float z : zs) {
                        Vector3f point = new Vector3f(x, y, z);
                        partPose.pose().transformPosition(point);
                        extents[0] = Math.min(extents[0], point.x);
                        extents[1] = Math.min(extents[1], point.y);
                        extents[2] = Math.max(extents[2], point.x);
                        extents[3] = Math.max(extents[3], point.y);
                    }
                }
            }
        });
        return extents;
    }

    private static TutorialRenderContext.Bounds boundsFromExtents(float[] extents) {
        int padding = 1;
        int left = (int) Math.floor(extents[0]) - padding;
        int top = (int) Math.floor(extents[1]) - padding;
        int right = (int) Math.ceil(extents[2]) + padding;
        int bottom = (int) Math.ceil(extents[3]) + padding;
        return new TutorialRenderContext.Bounds(
                left, top, Math.max(1, right - left), Math.max(1, bottom - top));
    }

    private static void transformChest(
            PoseStack pose, float originX, float originY, float size, float z) {
        pose.translate(originX, originY, z);
        pose.scale(size, -size, size);
        pose.mulPose(Axis.XP.rotationDegrees(20.0F));
        pose.mulPose(Axis.YP.rotationDegrees(-35.0F));
        pose.translate(-0.5F, -0.5F, -0.5F);
    }

    static void resetDepth(GuiGraphics graphics) {
        graphics.flush();
        RenderSystem.depthMask(true);
        RenderSystem.clearDepth(1.0D);
        RenderSystem.clear(GL11.GL_DEPTH_BUFFER_BIT);
        RenderSystem.disableDepthTest();
    }
}
