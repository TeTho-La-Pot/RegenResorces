package com.github.TeThoLaPot.regen_resources.common.regen;

import com.github.TeThoLaPot.regen_resources.common.block.RegenCustomVisualSpec;
import com.github.TeThoLaPot.regen_resources.common.block.RegenVisual;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public record RegenRule(
        long delayTicks,
        RegenVisual visual,
        @Nullable DimensionRestriction dimensionRestriction,
        List<RegenTargetSpec> targets,
        @Nullable Boolean naturalRegenOverride,
        @Nullable RegenCustomVisualSpec customVisualSpec) {

    public RegenRule(
            long delayTicks,
            RegenVisual visual,
            @Nullable DimensionRestriction dimensionRestriction,
            List<RegenTargetSpec> targets,
            @Nullable Boolean naturalRegenOverride) {
        this(delayTicks, visual, dimensionRestriction, targets, naturalRegenOverride, null);
    }
}
