package com.github.TeThoLaPot.regen_resources.common.tt;

import com.github.TeThoLaPot.regen_resources.common.regen.RegenMineMarker;
import com.github.TeThoLaPot.regen_resources.platform.RegenPlatformServices;
import com.github.TeThoLaPot.tt_core.TT_core;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;

/**
 * ワールド上のブロック変化（ピストン移動など）後に TT を整合させる。
 * <p>データだけ削除すると {@link com.github.TeThoLaPot.regen_resources.common.regen.RegenMineMarker#SRC_IMPLICIT} となり、
 * コンフィグで自然再生が許可されている場合は「動かした鉱石」を掘ってもまた再生シェルが付く。
 * Regen_Ore 系はブロック変化でフラグを立て／消してこの穴を塞いでいる。
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
        // Jade プローブ結果は TT・可否に依存する。破壊(空気)や TT 削除時はキャッシュだけが古くなりがちなので明示的に捨てる。
        if (physicallyMovedByPiston || newState.isAir() || hadPlacementTt) {
            RegenPlatformServices.NETWORK.invalidateJadeProbe(level, pos);
        }
    }
}
