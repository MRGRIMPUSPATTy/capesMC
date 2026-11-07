package dev.capesmc.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import dev.capesmc.CustomBannerBlock;
import dev.capesmc.CustomBannerBlockEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;

/** Draws the custom banner as a two-sided textured quad (0.75 x 1.5 blocks). */
public class BannerRenderer implements BlockEntityRenderer<CustomBannerBlockEntity, BannerRenderer.State> {
    public static class State extends BlockEntityRenderState {
        public String code = "";
        public Direction facing = Direction.NORTH;
    }

    @Override public State createRenderState() { return new State(); }

    @Override
    public void extractRenderState(CustomBannerBlockEntity be, State s, float partialTick, Vec3 cameraPos,
                                   ModelFeatureRenderer.CrumblingOverlay crumbling) {
        BlockEntityRenderer.super.extractRenderState(be, s, partialTick, cameraPos, crumbling);
        s.code = be.getCode();
        s.facing = be.getBlockState().getValue(CustomBannerBlock.FACING);
    }

    @Override
    public void submit(State s, PoseStack ps, SubmitNodeCollector collector, CameraRenderState camera) {
        Identifier tex = ClientCosmetics.texture(s.code, true);
        if (tex == null) return;
        final int light = s.lightCoords;
        ps.pushPose();
        ps.translate(0.5, 0.0, 0.5);
        ps.mulPose(Axis.YP.rotationDegrees(-s.facing.toYRot()));
        collector.submitCustomGeometry(ps, RenderTypes.entityCutoutNoCull(tex), (pose, vc) -> {
            float w = 0.375f, h = 1.5f;
            // front (+Z)
            v(vc, pose, -w, 0, 0.01f, 0, 1, light, 1);
            v(vc, pose,  w, 0, 0.01f, 1, 1, light, 1);
            v(vc, pose,  w, h, 0.01f, 1, 0, light, 1);
            v(vc, pose, -w, h, 0.01f, 0, 0, light, 1);
            // back (-Z), mirrored so the design reads correctly from behind too
            v(vc, pose,  w, 0, -0.01f, 0, 1, light, -1);
            v(vc, pose, -w, 0, -0.01f, 1, 1, light, -1);
            v(vc, pose, -w, h, -0.01f, 1, 0, light, -1);
            v(vc, pose,  w, h, -0.01f, 0, 0, light, -1);
        });
        ps.popPose();
    }

    private static void v(VertexConsumer vc, PoseStack.Pose pose, float x, float y, float z, float u, float v, int light, float nz) {
        vc.addVertex(pose, x, y, z).setColor(-1).setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0f, 0f, nz);
    }
}
