package com.github.TeThoLaPot.regen_resources.platform;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

/**
 * Platform-provided networking hooks.
 *
 * <p>Used to invalidate client-side caches (e.g. Jade probe) without pulling
 * Forge/NeoForge networking APIs into {@code common}.
 */
public interface RegenPlatformNetwork {
    void invalidateJadeProbe(ServerLevel level, BlockPos pos);
}

