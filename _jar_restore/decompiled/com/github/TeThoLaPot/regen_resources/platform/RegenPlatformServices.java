/*
 * Decompiled with CFR 0.152.
 */
package com.github.TeThoLaPot.regen_resources.platform;

import com.github.TeThoLaPot.regen_resources.platform.RegenPlatformConfig;
import com.github.TeThoLaPot.regen_resources.platform.RegenPlatformNetwork;

public final class RegenPlatformServices {
    public static volatile RegenPlatformConfig CONFIG = () -> false;
    public static volatile RegenPlatformNetwork NETWORK = (level, pos) -> {};

    private RegenPlatformServices() {
    }

    public static void install(RegenPlatformConfig config, RegenPlatformNetwork network) {
        if (config != null) {
            CONFIG = config;
        }
        if (network != null) {
            NETWORK = network;
        }
    }
}

