/*
 * Decompiled with CFR 0.152.
 */
package com.github.TeThoLaPot.regen_resources.platform.forge;

import com.github.TeThoLaPot.regen_resources.platform.RegenPlatformConfig;
import com.github.TeThoLaPot.regen_resources.platform.forge.config.RegenResourcesForgeConfig;

public final class RegenForgePlatformConfig
implements RegenPlatformConfig {
    @Override
    public boolean allowNaturalRegen() {
        return (Boolean)RegenResourcesForgeConfig.ALLOW_NATURAL_REGEN.get();
    }
}

