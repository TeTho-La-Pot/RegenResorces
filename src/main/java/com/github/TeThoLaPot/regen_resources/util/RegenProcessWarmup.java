package com.github.TeThoLaPot.regen_resources.util;

import net.minecraft.server.MinecraftServer;

import java.util.Map;
import java.util.WeakHashMap;

/**
 * {@code regen_process} をサーバー起動直後に走らせないためのウォームアップ終了ティック。
 */
public final class RegenProcessWarmup {

    private static final Map<MinecraftServer, Integer> WARMUP_UNTIL_SERVER_TICK = new WeakHashMap<>();

    private RegenProcessWarmup() {}

    public static void registerServerWarmup(MinecraftServer server, int warmupTicks) {
        if (warmupTicks <= 0) {
            return;
        }
        WARMUP_UNTIL_SERVER_TICK.put(server, server.getTickCount() + warmupTicks);
    }

    public static boolean shouldDeferRegenProcess(MinecraftServer server, int configWarmupTicks) {
        if (configWarmupTicks <= 0) {
            return false;
        }
        Integer until = WARMUP_UNTIL_SERVER_TICK.get(server);
        if (until == null) {
            return false;
        }
        return server.getTickCount() < until;
    }
}
