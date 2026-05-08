package com.github.TeThoLaPot.regen_resources.platform;

/**
 * Platform-provided configuration access.
 *
 * <p>Keep {@code common} free of loader-specific config APIs (Forge/NeoForge).
 */
public interface RegenPlatformConfig {
    boolean allowNaturalRegen();
}

