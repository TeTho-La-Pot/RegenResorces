package com.github.TeThoLaPot.regen_resources.util;

import com.github.TeThoLaPot.regen_resources.Config;
import com.mojang.logging.LogUtils;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;

/**
 * {@link Config} の診断フラグがオンのときだけ {@link #log} を出す。恒久運用向けではなく切り分け用。
 * <p>
 * 検証の手順・ログの読み方はリポジトリの {@code DEV_VERIFICATION.md} を参照。
 */
public final class RegenDiag {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static int lastWarmupDeferLoggedTick = Integer.MIN_VALUE;

    private RegenDiag() {}

    public static boolean trace() {
        try {
            return Boolean.TRUE.equals(Config.REGEN_DIAGNOSTIC_TRACE.get());
        } catch (IllegalStateException | NullPointerException e) {
            return false;
        }
    }

    public static void log(String fmt, Object... args) {
        if (trace()) {
            LOGGER.info("[RegenDiag] " + fmt, args);
        }
    }

    /** ウォームアップ defer が同一ティックで大量に走るため、ティックごとに一度だけ。 */
    public static void logWarmupDeferOncePerTick(MinecraftServer server) {
        if (!trace()) {
            return;
        }
        int t = server.getTickCount();
        if (t == lastWarmupDeferLoggedTick) {
            return;
        }
        lastWarmupDeferLoggedTick = t;
        LOGGER.info("[RegenDiag] regen_process deferred (warmup), serverTick={}", t);
    }
}
