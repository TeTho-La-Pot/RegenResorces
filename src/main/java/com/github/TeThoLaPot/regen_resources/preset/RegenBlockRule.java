package com.github.TeThoLaPot.regen_resources.preset;

import com.github.TeThoLaPot.regen_resources.init.block.RegenVisual;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public final class RegenBlockRule {

    public final long delayTicks;
    public final RegenVisual visual;
    public final @Nullable DimensionRestriction dimensionRestriction;

    public RegenBlockRule(long delayTicks, RegenVisual visual, @Nullable DimensionRestriction dimensionRestriction) {
        this.delayTicks = delayTicks;
        this.visual = visual;
        this.dimensionRestriction = dimensionRestriction;
    }

    public boolean matchesDimension(ResourceLocation dimensionId) {
        if (dimensionRestriction == null) {
            return true;
        }
        return dimensionRestriction.matches(dimensionId);
    }
}
