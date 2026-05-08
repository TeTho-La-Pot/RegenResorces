package com.github.TeThoLaPot.regen_resources.common.tt;

import com.github.TeThoLaPot.regen_resources.common.regen.RegenMineMarker;
import com.github.TeThoLaPot.regen_resources.forge.network.ClientboundJadeRegenProbeInvalidatePacket;
import com.github.TeThoLaPot.regen_resources.forge.network.RegenResourcesNetwork;
import com.github.TeThoLaPot.tt_core.TT_core;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.network.PacketDistributor;

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
        TT_core.removeBlockData(level, pos);
        if (physicallyMovedByPiston && !newState.isAir()) {
            CompoundTag deny = new CompoundTag();
            deny.putByte(RegenMineMarker.TT_SOURCE, RegenMineMarker.SRC_SURVIVAL);
            TT_core.saveBlockData(level, pos, deny);
        }
        if (physicallyMovedByPiston) {
            BlockPos ip = pos.immutable();
            var pkt = new ClientboundJadeRegenProbeInvalidatePacket(level.dimension().location(), ip);
            RegenResourcesNetwork.CHANNEL.send(PacketDistributor.TRACKING_CHUNK.with(() -> level.getChunkAt(ip)), pkt);
        }
    }
}
