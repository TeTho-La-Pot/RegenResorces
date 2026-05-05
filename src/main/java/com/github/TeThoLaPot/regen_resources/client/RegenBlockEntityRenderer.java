package com.github.TeThoLaPot.regen_resources.client;

import com.github.TeThoLaPot.regen_resources.init.block.RegenBlocks;
import com.github.TeThoLaPot.regen_resources.init.block.RegenVisual;
import com.github.TeThoLaPot.regen_resources.init.entity.RegenBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RegenBlockEntityRenderer implements BlockEntityRenderer<RegenBlockEntity> {

    public RegenBlockEntityRenderer(BlockEntityRendererProvider.Context ctx) {}

    @Override
    public void render(RegenBlockEntity be, float partialTick, PoseStack pose, MultiBufferSource buffer,
                       int packedLight, int packedOverlay) {
        BlockState self = be.getBlockState();
        if (!self.hasProperty(RegenBlocks.VISUAL) || self.getValue(RegenBlocks.VISUAL) != RegenVisual.MIMIC) {
            return;
        }
        BlockState mimic = be.getMimicState();
        if (mimic.isAir()) {
            return;
        }
        pose.pushPose();
        Minecraft.getInstance().getBlockRenderer()
                .renderSingleBlock(mimic, pose, buffer, packedLight, OverlayTexture.NO_OVERLAY);
        pose.popPose();
    }
}
