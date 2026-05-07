package com.github.TeThoLaPot.regen_resources.common.block;

import com.github.TeThoLaPot.tt_core.TT_core;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Jade がサーバー同期データを取得できるようにするためのプレースホルダ。
 * 再生状態そのものは {@link com.github.TeThoLaPot.tt_core.TT_core} に保存される。
 * <p>サーバー側ティックで、データ欠落・期限超過の再生シェルを {@link RegenCorruptionFallback} で除去する。
 */
public final class RegenBlockEntity extends BlockEntity {

    /** BE が載ってからの経過（チャンク荷降ろしでリセットされる）。 */
    private int watchdogWarmupTicks;

    public RegenBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, RegenBlockEntity be) {
        if (!(level instanceof ServerLevel sl)) {
            return;
        }
        if (!(state.getBlock() instanceof RegenBlocks)) {
            return;
        }

        be.watchdogWarmupTicks++;
        if (be.watchdogWarmupTicks < RegenCorruptionFallback.MIN_WATCHDOG_WARMUP_TICKS) {
            return;
        }

        CompoundTag data = TT_core.getBlockData(sl, pos);

        if (data.isEmpty()) {
            RegenCorruptionFallback.apply(sl, pos, state);
            return;
        }

        if (!data.contains(RegenCorruptionFallback.TT_EXECUTE_AT, CompoundTag.TAG_LONG)) {
            RegenCorruptionFallback.apply(sl, pos, state);
            return;
        }

        long executeAt = data.getLong(RegenCorruptionFallback.TT_EXECUTE_AT);
        long now = sl.getGameTime();
        if (now <= executeAt + RegenCorruptionFallback.POST_EXECUTE_GRACE_TICKS) {
            return;
        }

        RegenCorruptionFallback.apply(sl, pos, state);
    }
}
