package com.github.TeThoLaPot.regen_resources.platform.forge.event;

import com.github.TeThoLaPot.regen_resources.common.regen.RegenMineEligibility;
import com.github.TeThoLaPot.regen_resources.common.regen.RegenMineMarker;
import com.github.TeThoLaPot.regen_resources.platform.RegenPlatformServices;
import com.github.TeThoLaPot.tt_core.TT_core;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;

/** 再生シェル（TT・待機ブロック）を設置していいか。破壊そのものの可否ではない（軽量参照）。 */
final class RegenOreMineEligibility {

    private RegenOreMineEligibility() {}

    static boolean allows(ServerLevel level, BlockPos pos) {
        CompoundTag d = TT_core.getBlockData(level, pos);
        byte src = RegenMineMarker.readSourceByte(d);
        return RegenMineEligibility.allowsAfterBreak(src, RegenPlatformServices.CONFIG.allowNaturalRegen());
    }
}

