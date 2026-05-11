package com.github.TeThoLaPot.regen_resources.common.tt;

import com.github.TeThoLaPot.regen_resources.common.regen.RegenMineMarker;
import com.github.TeThoLaPot.regen_resources.platform.RegenPlatformServices;
import com.github.TeThoLaPot.tt_core.TT_core;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;

/**
 * ワールド上のブロック変化後に TT を整合させる。1.20.1 Forge 版と同一ロジックを維持する。
 * <p><b>1.20.1 の範囲</b>：{@link RegenMineMarker#SRC_SURVIVAL}（「自然再生 ON でも採掘後に再生シェルを置かない」）を付けるのは
 * <b>ピストンによる物理移動として検知できた場合だけ</b>（{@link net.minecraft.world.level.block.Block#UPDATE_MOVE_BY_PISTON}
 * が付いた {@link net.minecraft.world.level.Level#setBlock}、または {@link net.minecraft.world.level.chunk.LevelChunk#setBlockState}
 * の moved-by-piston 経路）。一般的な設置／破壊の {@code setBlock} で survival を書く処理はしない。
 * <p>なおすべての成功的な {@link net.minecraft.world.level.Level#setBlock} で当座標の TT はいったん削除される（1.20.1 同等）。
 * <p>1.21 補足：{@link net.minecraft.world.level.block.piston.PistonMovingBlockEntity#finalTick()} は {@code setBlock(..., 3)} のみで
 * {@code UPDATE_MOVE_BY_PISTON} が付かない。そのため {@link com.github.TeThoLaPot.regen_resources.platform.neoforge.mixin.LevelMixin} で
 * 「直前の状態が {@link net.minecraft.world.level.block.Blocks#MOVING_PISTON}」かつ「配置後が非空気」ならピストン経路と同等に扱う。
 */
public final class RegenBlockMoveHooks {

    private RegenBlockMoveHooks() {}

    /**
     * @param physicallyMovedByPiston {@link net.minecraft.world.level.chunk.LevelChunk#setBlockState}
     *                                  の moved-by-piston 経路、または {@link net.minecraft.world.level.block.Block#UPDATE_MOVE_BY_PISTON}
     */
    public static void afterMutation(ServerLevel level, BlockPos pos, BlockState newState, boolean physicallyMovedByPiston) {
        if (RegenSetBlockTtGuard.isSuppressed()) {
            return;
        }
        CompoundTag prior = TT_core.getBlockData(level, pos);
        boolean hadPlacementTt = !prior.isEmpty();
        TT_core.removeBlockData(level, pos);
        if (physicallyMovedByPiston && !newState.isAir()) {
            CompoundTag deny = new CompoundTag();
            deny.putByte(RegenMineMarker.TT_SOURCE, RegenMineMarker.SRC_SURVIVAL);
            TT_core.saveBlockData(level, pos, deny);
        }
        if (physicallyMovedByPiston || newState.isAir() || hadPlacementTt) {
            RegenPlatformServices.NETWORK.invalidateJadeProbe(level, pos);
        }
    }
}
