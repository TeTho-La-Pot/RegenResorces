/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.github.TeThoLaPot.tt_core.TT_core
 *  net.minecraft.core.BlockPos
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.server.level.ServerLevel
 *  org.jetbrains.annotations.Nullable
 */
package com.github.TeThoLaPot.regen_resources.platform.forge.event;

import com.github.TeThoLaPot.regen_resources.common.regen.RegenMineEligibility;
import com.github.TeThoLaPot.regen_resources.common.regen.RegenMineMarker;
import com.github.TeThoLaPot.regen_resources.common.regen.RegenRule;
import com.github.TeThoLaPot.regen_resources.platform.RegenPlatformServices;
import com.github.TeThoLaPot.tt_core.TT_core;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.Nullable;

final class RegenOreMineEligibility {
    private RegenOreMineEligibility() {
    }

    static boolean allows(ServerLevel level, BlockPos pos) {
        return RegenOreMineEligibility.allows(level, pos, null);
    }

    static boolean allows(ServerLevel level, BlockPos pos, @Nullable RegenRule matchedRule) {
        CompoundTag d = TT_core.getBlockData((ServerLevel)level, (BlockPos)pos);
        byte src = RegenMineMarker.readSourceByte(d);
        Boolean override = matchedRule == null ? null : matchedRule.naturalRegenOverride();
        return RegenMineEligibility.allowsAfterBreak(src, RegenPlatformServices.CONFIG.allowNaturalRegen(), override);
    }
}

