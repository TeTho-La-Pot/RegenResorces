package com.github.TeThoLaPot.regen_resources.platform.neoforge.client;

import com.github.TeThoLaPot.regen_resources.common.block.RegenBlocks;
import com.github.TeThoLaPot.regen_resources.common.clientbridge.RegenStrippedCompositeClientHooks;
import com.github.TeThoLaPot.regen_resources.platform.neoforge.client.model.RegenStrippedLogBakedModel;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/** {@link RegenStrippedCompositeClientHooks} に合成テクスチャのプリフェッチを登録する。 */
public final class RegenStrippedCompositeWarmup {

    private RegenStrippedCompositeWarmup() {}

    public static void register() {
        RegenStrippedCompositeClientHooks.install(RegenStrippedCompositeWarmup::schedule);
    }

    private static void schedule(BlockPos pos, ResourceLocation strippedBlockId) {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null) {
            return;
        }
        mc.execute(() -> warmupAt(pos, strippedBlockId));
    }

    private static void warmupAt(BlockPos pos, ResourceLocation strippedBlockId) {
        Minecraft mc = Minecraft.getInstance();
        Level level = mc.level;
        if (level == null) {
            return;
        }
        BlockState state = level.getBlockState(pos);
        Direction.Axis axis = Direction.Axis.Y;
        if (state.hasProperty(RegenBlocks.AXIS)) {
            axis = state.getValue(RegenBlocks.AXIS);
        }
        RegenStrippedLogBakedModel.prefetchCompositeCache(strippedBlockId, axis);
    }
}
