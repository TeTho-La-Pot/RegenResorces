/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package com.github.TeThoLaPot.regen_resources.common.regen;

import org.jetbrains.annotations.Nullable;

public final class RegenMineEligibility {
    private RegenMineEligibility() {
    }

    public static boolean allowsAfterBreak(byte sourceMarker, boolean configAllowNaturalRegen) {
        return RegenMineEligibility.allowsAfterBreak(sourceMarker, configAllowNaturalRegen, null);
    }

    public static boolean allowsAfterBreak(byte sourceMarker, boolean configAllowNaturalRegen, @Nullable Boolean ruleOverride) {
        if (sourceMarker == 1) {
            return false;
        }
        if (sourceMarker == 2) {
            return true;
        }
        if (ruleOverride != null) {
            return ruleOverride;
        }
        return configAllowNaturalRegen;
    }
}

