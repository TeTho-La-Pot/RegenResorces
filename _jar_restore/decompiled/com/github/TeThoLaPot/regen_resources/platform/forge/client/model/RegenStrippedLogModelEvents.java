/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  net.minecraft.client.renderer.block.BlockModelShaper
 *  net.minecraft.client.resources.model.BakedModel
 *  net.minecraft.client.resources.model.ModelResourceLocation
 *  net.minecraft.core.Direction$Axis
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.RotatedPillarBlock
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.level.block.state.properties.Property
 *  net.minecraftforge.api.distmarker.Dist
 *  net.minecraftforge.client.event.ModelEvent$ModifyBakingResult
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber$Bus
 *  org.slf4j.Logger
 */
package com.github.TeThoLaPot.regen_resources.platform.forge.client.model;

import com.github.TeThoLaPot.regen_resources.common.block.RegenBlocks;
import com.github.TeThoLaPot.regen_resources.common.block.RegenVisual;
import com.github.TeThoLaPot.regen_resources.platform.forge.block.Re_Blocks;
import com.github.TeThoLaPot.regen_resources.platform.forge.client.model.RegenCustomTemplateBakedModel;
import com.github.TeThoLaPot.regen_resources.platform.forge.client.model.RegenStrippedLogBakedModel;
import com.mojang.logging.LogUtils;
import java.util.Map;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;

@Mod.EventBusSubscriber(modid="regen_resources", bus=Mod.EventBusSubscriber.Bus.MOD, value={Dist.CLIENT})
public final class RegenStrippedLogModelEvents {
    private static final Logger LOGGER = LogUtils.getLogger();

    private RegenStrippedLogModelEvents() {
    }

    @SubscribeEvent
    public static void onModifyBaking(ModelEvent.ModifyBakingResult event) {
        RegenStrippedLogBakedModel.invalidateCompositeCache();
        RegenCustomTemplateBakedModel.invalidateCache();
        Map models = event.getModels();
        RegenStrippedLogModelEvents.replaceVariant(models, RegenVisual.STRIPPED_LOG, ModelKind.STRIPPED_LOG);
        RegenStrippedLogModelEvents.replaceVariant(models, RegenVisual.STRIPPED_LOG_PRESET, ModelKind.STRIPPED_LOG);
        RegenStrippedLogModelEvents.replaceVariant(models, RegenVisual.CUSTOM, ModelKind.CUSTOM);
        RegenStrippedLogModelEvents.replaceVariant(models, RegenVisual.CUSTOM_PRESET, ModelKind.CUSTOM);
        Block strippedDummyBlock = (Block)Re_Blocks.PRESET_DUMMY_STRIPPED_LOG.get();
        for (Direction.Axis axis : Direction.Axis.values()) {
            BlockState strippedDummy = (BlockState)strippedDummyBlock.m_49966_().m_61124_((Property)RotatedPillarBlock.f_55923_, (Comparable)axis);
            ModelResourceLocation strippedDummyKey = BlockModelShaper.m_110895_((BlockState)strippedDummy);
            BakedModel strippedDummyModel = (BakedModel)models.get(strippedDummyKey);
            if (strippedDummyModel == null || strippedDummyModel instanceof RegenStrippedLogBakedModel) continue;
            models.put(strippedDummyKey, new RegenStrippedLogBakedModel(strippedDummyModel));
        }
        BlockState customDummyState = ((Block)Re_Blocks.PRESET_DUMMY_CUSTOM.get()).m_49966_();
        ModelResourceLocation customDummyKey = BlockModelShaper.m_110895_((BlockState)customDummyState);
        BakedModel customDummyModel = (BakedModel)models.get(customDummyKey);
        if (customDummyModel != null && !(customDummyModel instanceof RegenCustomTemplateBakedModel)) {
            models.put(customDummyKey, new RegenCustomTemplateBakedModel(customDummyModel));
        }
    }

    private static void replaceVariant(Map<ResourceLocation, BakedModel> models, RegenVisual visual, ModelKind kind) {
        for (Direction.Axis axis : Direction.Axis.values()) {
            Object wrapped;
            BlockState state = (BlockState)((BlockState)((Block)Re_Blocks.REGEN_BLOCK.get()).m_49966_().m_61124_(RegenBlocks.VISUAL, (Comparable)((Object)visual))).m_61124_(RegenBlocks.AXIS, (Comparable)axis);
            ModelResourceLocation key = BlockModelShaper.m_110895_((BlockState)state);
            BakedModel original = models.get(key);
            if (original == null) {
                LOGGER.debug("RegenResources: model variant not found for {}", (Object)key);
                continue;
            }
            switch (kind) {
                default: {
                    throw new IncompatibleClassChangeError();
                }
                case STRIPPED_LOG: {
                    Object object;
                    if (original instanceof RegenStrippedLogBakedModel) {
                        object = null;
                        break;
                    }
                    object = new RegenStrippedLogBakedModel(original);
                    break;
                }
                case CUSTOM: {
                    Object object = wrapped = original instanceof RegenCustomTemplateBakedModel ? null : new RegenCustomTemplateBakedModel(original);
                }
            }
            if (wrapped == null) continue;
            models.put((ResourceLocation)key, (BakedModel)wrapped);
        }
    }

    private static enum ModelKind {
        STRIPPED_LOG,
        CUSTOM;

    }
}

