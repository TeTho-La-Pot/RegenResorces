package com.github.TeThoLaPot.regen_resources.platform.neoforge.client.model;

import com.github.TeThoLaPot.regen_resources.RegenResources;
import com.github.TeThoLaPot.regen_resources.common.block.RegenBlocks;
import com.github.TeThoLaPot.regen_resources.common.block.RegenVisual;
import com.github.TeThoLaPot.regen_resources.platform.neoforge.block.Re_Blocks;
import com.mojang.logging.LogUtils;
import java.util.Map;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ModelEvent;
import org.slf4j.Logger;

/** ストリップ原木／カスタムプリセット用の動的ベイクモデルをレジストリへ差し込む。 */
@SuppressWarnings("deprecation")
@EventBusSubscriber(modid = RegenResources.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class RegenStrippedLogModelEvents {

    private static final Logger LOGGER = LogUtils.getLogger();

    private RegenStrippedLogModelEvents() {}

    @SubscribeEvent
    public static void onModifyBaking(ModelEvent.ModifyBakingResult event) {
        RegenStrippedLogBakedModel.invalidateCompositeCache();
        RegenCustomTemplateBakedModel.invalidateCache();
        Map<ModelResourceLocation, BakedModel> models = event.getModels();
        replaceVariant(models, RegenVisual.STRIPPED_LOG, ModelKind.STRIPPED_LOG);
        replaceVariant(models, RegenVisual.STRIPPED_LOG_PRESET, ModelKind.STRIPPED_LOG);
        replaceVariant(models, RegenVisual.CUSTOM, ModelKind.CUSTOM);
        replaceVariant(models, RegenVisual.CUSTOM_PRESET, ModelKind.CUSTOM);

        Block strippedDummyBlock = Re_Blocks.PRESET_DUMMY_STRIPPED_LOG.get();
        for (Direction.Axis axis : Direction.Axis.values()) {
            BlockState strippedDummy = strippedDummyBlock.defaultBlockState().setValue(RotatedPillarBlock.AXIS, axis);
            ModelResourceLocation strippedDummyKey = BlockModelShaper.stateToModelLocation(strippedDummy);
            BakedModel strippedDummyModel = models.get(strippedDummyKey);
            if (strippedDummyModel == null || strippedDummyModel instanceof RegenStrippedLogBakedModel) {
                continue;
            }
            models.put(strippedDummyKey, new RegenStrippedLogBakedModel(strippedDummyModel));
        }

        BlockState customDummyState = Re_Blocks.PRESET_DUMMY_CUSTOM.get().defaultBlockState();
        ModelResourceLocation customDummyKey = BlockModelShaper.stateToModelLocation(customDummyState);
        BakedModel customDummyModel = models.get(customDummyKey);
        if (customDummyModel != null && !(customDummyModel instanceof RegenCustomTemplateBakedModel)) {
            models.put(customDummyKey, new RegenCustomTemplateBakedModel(customDummyModel));
        }
    }

    private static void replaceVariant(Map<ModelResourceLocation, BakedModel> models, RegenVisual visual, ModelKind kind) {
        for (Direction.Axis axis : Direction.Axis.values()) {
            BlockState state =
                    Re_Blocks.REGEN_BLOCK.get().defaultBlockState().setValue(RegenBlocks.VISUAL, visual).setValue(RegenBlocks.AXIS, axis);
            ModelResourceLocation key = BlockModelShaper.stateToModelLocation(state);
            BakedModel original = models.get(key);
            if (original == null) {
                LOGGER.debug("RegenResources: model variant not found for {}", key);
                continue;
            }
            BakedModel wrapped =
                    switch (kind) {
                        case STRIPPED_LOG ->
                                original instanceof RegenStrippedLogBakedModel ? null : new RegenStrippedLogBakedModel(original);
                        case CUSTOM ->
                                original instanceof RegenCustomTemplateBakedModel ? null : new RegenCustomTemplateBakedModel(original);
                    };
            if (wrapped == null) {
                continue;
            }
            models.put(key, wrapped);
        }
    }

    private enum ModelKind {
        STRIPPED_LOG,
        CUSTOM
    }
}
