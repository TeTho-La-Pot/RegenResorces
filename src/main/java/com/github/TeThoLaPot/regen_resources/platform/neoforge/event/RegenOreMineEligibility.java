package com.github.TeThoLaPot.regen_resources.platform.neoforge.event;

import com.github.TeThoLaPot.regen_resources.common.regen.RegenMineEligibility;
import com.github.TeThoLaPot.regen_resources.common.regen.RegenMineMarker;
import com.github.TeThoLaPot.regen_resources.common.regen.RegenRule;
import com.github.TeThoLaPot.regen_resources.platform.RegenPlatformServices;
import com.github.TeThoLaPot.tt_core.TT_core;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.Nullable;

/** 再生シェル（TT・待機ブロック）を設置していいか。破壊そのものの可否ではない。 */
final class RegenOreMineEligibility {

    private RegenOreMineEligibility() {}

    static boolean allows(ServerLevel level, BlockPos pos) {
        return allows(level, pos, null);
    }

    static boolean allows(ServerLevel level, BlockPos pos, @Nullable RegenRule matchedRule) {
        CompoundTag d = TT_core.getBlockData(level, pos);
        byte src = RegenMineMarker.readSourceByte(d);
        Boolean override = matchedRule == null ? null : matchedRule.naturalRegenOverride();
        return RegenMineEligibility.allowsAfterBreak(src, RegenPlatformServices.CONFIG.allowNaturalRegen(), override);
    }
}
