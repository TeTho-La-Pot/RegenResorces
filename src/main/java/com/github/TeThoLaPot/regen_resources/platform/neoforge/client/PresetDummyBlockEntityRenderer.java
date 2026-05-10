package com.github.TeThoLaPot.regen_resources.platform.neoforge.client;

import com.github.TeThoLaPot.regen_resources.common.block.AbstractPresetDummyBlock;
import com.github.TeThoLaPot.regen_resources.common.block.CustomPresetDummyBlock;
import com.github.TeThoLaPot.regen_resources.common.block.RegenBlockEntity;
import com.github.TeThoLaPot.regen_resources.common.block.StrippedLogPresetDummyBlock;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.RenderTypeHelper;
import net.neoforged.neoforge.client.model.data.ModelData;

/**
 * チャンク側は面なしモデルにしているため、ここで mimicked 状態をフル描画する。
 * {@link BlockRenderDispatcher#renderSingleBlock} は {@link RenderShape#MODEL} のみを期待する。
 */
public final class PresetDummyBlockEntityRenderer implements BlockEntityRenderer<RegenBlockEntity> {

    public PresetDummyBlockEntityRenderer(@SuppressWarnings("unused") BlockEntityRendererProvider.Context context) {}

    @Override
    public void render(
            RegenBlockEntity be,
            float partialTick,
            PoseStack pose,
            MultiBufferSource buffer,
            int packedLight,
            int packedOverlay) {
        Level level = be.getLevel();
        if (level == null) {
            return;
        }
        BlockState shell = be.getBlockState();
        if (!(shell.getBlock() instanceof AbstractPresetDummyBlock)) {
            return;
        }
        BlockState mimic = be.getMimicAppearance();
        if (mimic == null) {
            if (shell.getBlock() instanceof StrippedLogPresetDummyBlock) {
                mimic = Blocks.STRIPPED_OAK_LOG.defaultBlockState();
            } else if (shell.getBlock() instanceof CustomPresetDummyBlock) {
                mimic = Blocks.STONE.defaultBlockState();
            } else {
                return;
            }
        }
        if (mimic.getRenderShape() != RenderShape.MODEL) {
            mimic = mimic.getBlock().defaultBlockState();
        }
        pose.pushPose();
        BlockRenderDispatcher dispatcher = Minecraft.getInstance().getBlockRenderer();
        var model = dispatcher.getBlockModel(mimic);
        int rgb = Minecraft.getInstance().getBlockColors().getColor(mimic, level, be.getBlockPos(), 0);
        float r = (float) (rgb >> 16 & 255) / 255.0F;
        float g = (float) (rgb >> 8 & 255) / 255.0F;
        float b = (float) (rgb & 255) / 255.0F;
        RandomSource random = RandomSource.create(42L);
        ModelData modelData = ModelData.EMPTY;
        PoseStack.Pose last = pose.last();
        for (RenderType rt : model.getRenderTypes(mimic, random, modelData)) {
            dispatcher.getModelRenderer()
                    .renderModel(
                            last,
                            buffer.getBuffer(RenderTypeHelper.getEntityRenderType(rt, false)),
                            mimic,
                            model,
                            r,
                            g,
                            b,
                            packedLight,
                            packedOverlay,
                            modelData,
                            rt);
        }
        pose.popPose();
    }
}
