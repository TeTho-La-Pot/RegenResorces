package com.github.TeThoLaPot.regen_resources.platform;

/**
 * Single injection point for loader-specific services (Forge/NeoForge).
 *
 * <p>Defaults are no-op to keep datagen/edge environments stable.
 */
public final class RegenPlatformServices {

    private RegenPlatformServices() {}

    public static volatile RegenPlatformConfig CONFIG = () -> false;

    public static volatile RegenPlatformNetwork NETWORK = (level, pos) -> {};

    public static void install(RegenPlatformConfig config, RegenPlatformNetwork network) {
        if (config != null) {
            CONFIG = config;
        }
        if (network != null) {
            NETWORK = network;
        }
    }
}

