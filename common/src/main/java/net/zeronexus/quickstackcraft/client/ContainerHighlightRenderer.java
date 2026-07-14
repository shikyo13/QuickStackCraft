package net.zeronexus.quickstackcraft.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ShapeRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.zeronexus.quickstackcraft.logic.StorageListState;
import net.zeronexus.quickstackcraft.network.ContainerHighlightS2CPacket;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.List;

public final class ContainerHighlightRenderer {

    private static final Collection<BlockHighlight> blockHighlights = new ArrayDeque<>();
    private static final Collection<EntityHighlight> entityHighlights = new ArrayDeque<>();
    private static ListStateHighlight listStateHighlight;
    private static ContainerHighlightS2CPacket.HighlightKind highlightKind =
            ContainerHighlightS2CPacket.HighlightKind.DESTINATION;

    private ContainerHighlightRenderer() {}

    public static void onHighlightReceived(
            ContainerHighlightS2CPacket.HighlightKind kind,
            List<BlockPos> positions,
            List<Integer> entityIds) {
        long expiry = System.currentTimeMillis() + ClientPreferences.outlineLifetimeMs();
        blockHighlights.clear();
        entityHighlights.clear();
        highlightKind = kind;
        positions.forEach(position -> blockHighlights.add(new BlockHighlight(position, expiry)));
        entityIds.forEach(entityId -> entityHighlights.add(new EntityHighlight(entityId, expiry)));
    }

    public static void onListStateFeedback(BlockPos position, StorageListState state) {
        listStateHighlight = new ListStateHighlight(
                position.immutable(), state, System.currentTimeMillis() + 2500L);
    }

    public static void tick() {
        long now = System.currentTimeMillis();
        blockHighlights.removeIf(highlight -> now >= highlight.expiry);
        entityHighlights.removeIf(highlight -> now >= highlight.expiry);
        if (listStateHighlight != null && now >= listStateHighlight.expiry) {
            listStateHighlight = null;
        }
    }

    public static boolean hasHighlights() {
        return !blockHighlights.isEmpty() || !entityHighlights.isEmpty() || listStateHighlight != null;
    }

    public static void renderHighlights(PoseStack poseStack, MultiBufferSource bufferSource, Vec3 cameraPos) {
        if (!hasHighlights()) {
            return;
        }

        VertexConsumer lines = bufferSource.getBuffer(RenderType.lines());
        float[] color = highlightKind == ContainerHighlightS2CPacket.HighlightKind.SOURCE
                ? StorageHighlightPalette.sourceRgb()
                : StorageHighlightPalette.destinationRgb();
        float alpha = (float) Math.max(0.0D, Math.min(1.0D, ClientPreferences.outlineOpacity()));

        for (BlockHighlight highlight : blockHighlights) {
            renderBox(poseStack, lines, new AABB(highlight.pos).inflate(0.002), cameraPos, color, alpha);
        }

        ListStateHighlight activeListState = listStateHighlight;
        if (activeListState != null) {
            renderBox(poseStack, lines, new AABB(activeListState.pos).inflate(0.006), cameraPos,
                    StorageHighlightPalette.listStateRgb(activeListState.state), 1.0F);
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level != null) {
            for (EntityHighlight highlight : entityHighlights) {
                Entity entity = minecraft.level.getEntity(highlight.entityId);
                if (entity != null) {
                    renderBox(poseStack, lines, entity.getBoundingBox().inflate(0.002), cameraPos, color, alpha);
                }
            }
        }
    }

    private static void renderBox(
            PoseStack poseStack,
            VertexConsumer lines,
            AABB box,
            Vec3 cameraPos,
            float[] color,
            float alpha) {
        ShapeRenderer.renderLineBox(poseStack, lines,
                box.minX - cameraPos.x, box.minY - cameraPos.y, box.minZ - cameraPos.z,
                box.maxX - cameraPos.x, box.maxY - cameraPos.y, box.maxZ - cameraPos.z,
                color[0], color[1], color[2], alpha);
    }

    private record BlockHighlight(BlockPos pos, long expiry) {}
    private record EntityHighlight(int entityId, long expiry) {}
    private record ListStateHighlight(BlockPos pos, StorageListState state, long expiry) {}
}
