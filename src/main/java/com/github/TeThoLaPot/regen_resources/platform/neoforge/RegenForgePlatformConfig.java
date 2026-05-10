package com.github.TeThoLaPot.regen_resources.platform.neoforge;

import com.github.TeThoLaPot.regen_resources.platform.RegenPlatformConfig;
import com.github.TeThoLaPot.regen_resources.platform.neoforge.config.RegenResourcesForgeConfig;

public final class RegenForgePlatformConfig implements RegenPlatformConfig {
    @Override
    public boolean allowNaturalRegen() {
        return RegenResourcesForgeConfig.ALLOW_NATURAL_REGEN.get();
    }
}

