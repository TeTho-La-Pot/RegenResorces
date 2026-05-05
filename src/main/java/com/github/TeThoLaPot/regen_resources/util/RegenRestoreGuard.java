package com.github.TeThoLaPot.regen_resources.util;

import net.minecraft.core.BlockPos;

/**
 * 鉱石再生処理（占位 {@code regen_block} の設置または鉱石化）の同期的な一区間のみ、そのマスの破壊を抑止します。
 */
public final class RegenRestoreGuard {

    private static final ThreadLocal<Integer> GUARD_DEPTH = ThreadLocal.withInitial(() -> 0);
    /** 復元処理が触っているサーバー側座標（null 時はロックなし）。 */
    private static BlockPos activePos;

    private RegenRestoreGuard() {}

    public static boolean isBlockingBreakAt(BlockPos pos) {
        if (GUARD_DEPTH.get() == 0) {
            return false;
        }
        return activePos != null && activePos.equals(pos);
    }

    public static void runAt(BlockPos pos, Runnable runnable) {
        BlockPos prev = activePos;
        BlockPos immutable = pos.immutable();
        activePos = immutable;
        GUARD_DEPTH.set(GUARD_DEPTH.get() + 1);
        try {
            runnable.run();
        } finally {
            int d = GUARD_DEPTH.get() - 1;
            if (d <= 0) {
                GUARD_DEPTH.remove();
            } else {
                GUARD_DEPTH.set(d);
            }
            activePos = prev;
        }
    }
}
