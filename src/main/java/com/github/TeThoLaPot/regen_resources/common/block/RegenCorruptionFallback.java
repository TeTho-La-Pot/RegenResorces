/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.github.TeThoLaPot.tt_core.TT_core
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.registries.BuiltInRegistries
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.world.level.BlockGetter
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.Blocks
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  net.minecraft.world.level.block.state.BlockState
 *  org.jetbrains.annotations.Nullable
 */
package com.github.TeThoLaPot.regen_resources.common.block;

import com.github.TeThoLaPot.regen_resources.common.block.RegenBlockEntity;
import com.github.TeThoLaPot.regen_resources.common.block.RegenBlocks;
import com.github.TeThoLaPot.regen_resources.common.block.RegenCustomVisualSpec;
import com.github.TeThoLaPot.regen_resources.common.block.RegenStrippedLogResolver;
import com.github.TeThoLaPot.regen_resources.common.block.RegenVisual;
import com.github.TeThoLaPot.tt_core.TT_core;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public final class RegenCorruptionFallback {
    public static final String TT_EXECUTE_AT = "execute_at";
    public static final int MIN_WATCHDOG_WARMUP_TICKS = 40;
    public static final int POST_EXECUTE_GRACE_TICKS = 40;

    private RegenCorruptionFallback() {
    }

    public static BlockState replacementFor(RegenVisual visual) {
        return RegenCorruptionFallback.replacementFor(visual, null, null);
    }

    public static BlockState replacementFor(RegenVisual visual, @Nullable ResourceLocation strippedBlockId) {
        return RegenCorruptionFallback.replacementFor(visual, strippedBlockId, null);
    }

    public static BlockState replacementFor(RegenVisual visual, @Nullable ResourceLocation strippedBlockId, @Nullable RegenCustomVisualSpec customSpec) {
        return switch (visual) {
            default -> throw new IncompatibleClassChangeError();
            case RegenVisual.STONE, RegenVisual.STONE_PRESET -> Blocks.STONE.defaultBlockState();
            case RegenVisual.DEEPSLATE, RegenVisual.DEEPSLATE_PRESET -> Blocks.DEEPSLATE.defaultBlockState();
            case RegenVisual.NETHER, RegenVisual.NETHER_PRESET -> Blocks.NETHERRACK.defaultBlockState();
            case RegenVisual.END, RegenVisual.END_PRESET -> Blocks.END_STONE.defaultBlockState();
            case RegenVisual.DEBRIS, RegenVisual.DEBRIS_PRESET -> Blocks.NETHERRACK.defaultBlockState();
            case RegenVisual.MIMIC -> Blocks.STONE.defaultBlockState();
            case RegenVisual.STRIPPED_LOG, RegenVisual.STRIPPED_LOG_PRESET -> RegenCorruptionFallback.strippedLogState(strippedBlockId);
            case RegenVisual.CUSTOM, RegenVisual.CUSTOM_PRESET -> RegenCorruptionFallback.customSampleState(customSpec);
        };
    }

    private static BlockState strippedLogState(@Nullable ResourceLocation strippedBlockId) {
        Block b = RegenStrippedLogResolver.fromId(strippedBlockId);
        if (b != null) {
            return b.defaultBlockState();
        }
        return Blocks.STRIPPED_OAK_LOG.defaultBlockState();
    }

    private static BlockState customSampleState(@Nullable RegenCustomVisualSpec customSpec) {
        Block b;
        if (customSpec != null && customSpec.miningSampleBlockId() != null && (b = (Block)BuiltInRegistries.BLOCK.get(customSpec.miningSampleBlockId())) != null && b != Blocks.AIR) {
            return b.defaultBlockState();
        }
        return Blocks.STONE.defaultBlockState();
    }

    public static BlockState miningSampleFor(RegenVisual visual) {
        return RegenCorruptionFallback.miningSampleFor(visual, (ResourceLocation)null, null);
    }

    public static BlockState miningSampleFor(RegenVisual visual, @Nullable ResourceLocation strippedBlockId) {
        return RegenCorruptionFallback.miningSampleFor(visual, strippedBlockId, null);
    }

    public static BlockState miningSampleFor(RegenVisual visual, @Nullable ResourceLocation strippedBlockId, @Nullable RegenCustomVisualSpec customSpec) {
        if (visual == RegenVisual.DEBRIS || visual == RegenVisual.DEBRIS_PRESET) {
            return Blocks.ANCIENT_DEBRIS.defaultBlockState();
        }
        if (visual == RegenVisual.STRIPPED_LOG || visual == RegenVisual.STRIPPED_LOG_PRESET) {
            return RegenCorruptionFallback.strippedLogState(strippedBlockId);
        }
        if (visual == RegenVisual.CUSTOM || visual == RegenVisual.CUSTOM_PRESET) {
            return RegenCorruptionFallback.customSampleState(customSpec);
        }
        return RegenCorruptionFallback.replacementFor(visual);
    }

    @Nullable
    public static ResourceLocation strippedIdFromBE(@Nullable BlockGetter level, @Nullable BlockPos pos) {
        if (level == null || pos == null) {
            return null;
        }
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof RegenBlockEntity) {
            RegenBlockEntity rbe = (RegenBlockEntity)be;
            return rbe.getStrippedBlockId();
        }
        return null;
    }

    @Nullable
    public static RegenCustomVisualSpec customSpecFromBE(@Nullable BlockGetter level, @Nullable BlockPos pos) {
        if (level == null || pos == null) {
            return null;
        }
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof RegenBlockEntity) {
            RegenBlockEntity rbe = (RegenBlockEntity)be;
            return rbe.getCustomVisualSpec();
        }
        return null;
    }

    public static BlockState miningSampleFor(RegenVisual visual, @Nullable BlockGetter level, @Nullable BlockPos pos) {
        return RegenCorruptionFallback.miningSampleFor(visual, RegenCorruptionFallback.strippedIdFromBE(level, pos), RegenCorruptionFallback.customSpecFromBE(level, pos));
    }

    public static void apply(ServerLevel level, BlockPos pos, BlockState regenState) {
        if (!(regenState.getBlock() instanceof RegenBlocks)) {
            return;
        }
        RegenVisual visual = regenState.getValue(RegenBlocks.VISUAL);
        ResourceLocation strippedId = RegenCorruptionFallback.strippedIdFromBE((BlockGetter)level, pos);
        RegenCustomVisualSpec customSpec = RegenCorruptionFallback.customSpecFromBE((BlockGetter)level, pos);
        BlockState replacement = RegenCorruptionFallback.replacementFor(visual, strippedId, customSpec);
        TT_core.removeBlockData((ServerLevel)level, (BlockPos)pos);
        int flags = 3;
        level.setBlock(pos, replacement, flags);
    }
}

