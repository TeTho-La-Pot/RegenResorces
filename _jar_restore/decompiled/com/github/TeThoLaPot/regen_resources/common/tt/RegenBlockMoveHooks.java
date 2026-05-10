/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.github.TeThoLaPot.tt_core.TT_core
 *  net.minecraft.core.BlockPos
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.world.level.block.state.BlockState
 */
package com.github.TeThoLaPot.regen_resources.common.tt;

import com.github.TeThoLaPot.regen_resources.common.tt.RegenSetBlockTtGuard;
import com.github.TeThoLaPot.regen_resources.platform.RegenPlatformServices;
import com.github.TeThoLaPot.tt_core.TT_core;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;

public final class RegenBlockMoveHooks {
    private RegenBlockMoveHooks() {
    }

    public static void afterMutation(ServerLevel level, BlockPos pos, BlockState newState, boolean physicallyMovedByPiston) {
        if (RegenSetBlockTtGuard.isSuppressed()) {
            return;
        }
        CompoundTag prior = TT_core.getBlockData((ServerLevel)level, (BlockPos)pos);
        boolean hadPlacementTt = !prior.m_128456_();
        TT_core.removeBlockData((ServerLevel)level, (BlockPos)pos);
        if (physicallyMovedByPiston && !newState.m_60795_()) {
            CompoundTag deny = new CompoundTag();
            deny.m_128344_("rr_src", (byte)1);
            TT_core.saveBlockData((ServerLevel)level, (BlockPos)pos, (CompoundTag)deny);
        }
        if (physicallyMovedByPiston || newState.m_60795_() || hadPlacementTt) {
            RegenPlatformServices.NETWORK.invalidateJadeProbe(level, pos);
        }
    }
}

