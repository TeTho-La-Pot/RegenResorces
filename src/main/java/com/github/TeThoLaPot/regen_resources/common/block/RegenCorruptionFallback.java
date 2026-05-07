package com.github.TeThoLaPot.regen_resources.common.block;

import com.github.TeThoLaPot.tt_core.TT_core;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * TT / 永続タスクが破損・欠落したとき、再生シェルをプリセットに応じた石の下地ブロックへ置き換える。
 * <p>{@link RegenVisual#DEBRIS} / {@link RegenVisual#DEBRIS_PRESET} は仕様としてネザーラック。
 */
public final class RegenCorruptionFallback {

    /** {@link com.github.TeThoLaPot.regen_resources.forge.RegenRegenForgeEvents} と同じキー。 */
    public static final String TT_EXECUTE_AT = "execute_at";

    /** 設置直後の TT 書き込み待ち・読込順のばらつき用（約 2 秒）。 */
    public static final int MIN_WATCHDOG_WARMUP_TICKS = 40;

    /** 実行予定 tick 超過後、executor の実行ラグを吸収する余裕（約 2 秒）。 */
    public static final int POST_EXECUTE_GRACE_TICKS = 40;

    private RegenCorruptionFallback() {}

    public static BlockState replacementFor(RegenVisual visual) {
        return switch (visual) {
            case STONE, STONE_PRESET -> Blocks.STONE.defaultBlockState();
            case DEEPSLATE, DEEPSLATE_PRESET -> Blocks.DEEPSLATE.defaultBlockState();
            case NETHER, NETHER_PRESET -> Blocks.NETHERRACK.defaultBlockState();
            case END, END_PRESET -> Blocks.END_STONE.defaultBlockState();
            case DEBRIS, DEBRIS_PRESET -> Blocks.NETHERRACK.defaultBlockState();
            case MIMIC -> Blocks.STONE.defaultBlockState();
        };
    }

    public static void apply(ServerLevel level, BlockPos pos, BlockState regenState) {
        if (!(regenState.getBlock() instanceof RegenBlocks)) {
            return;
        }
        RegenVisual visual = regenState.getValue(RegenBlocks.VISUAL);
        BlockState replacement = replacementFor(visual);
        TT_core.removeBlockData(level, pos);
        int flags = Block.UPDATE_NEIGHBORS | Block.UPDATE_CLIENTS;
        level.setBlock(pos, replacement, flags);
    }
}
