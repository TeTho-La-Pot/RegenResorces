/*
 * Decompiled with CFR 0.152.
 */
package com.github.TeThoLaPot.regen_resources.common.tt;

public final class RegenSetBlockTtGuard
implements AutoCloseable {
    private static final ThreadLocal<Integer> DEPTH = ThreadLocal.withInitial(() -> 0);

    private RegenSetBlockTtGuard() {
    }

    public static RegenSetBlockTtGuard acquire() {
        DEPTH.set(DEPTH.get() + 1);
        return new RegenSetBlockTtGuard();
    }

    public static boolean isSuppressed() {
        return DEPTH.get() > 0;
    }

    @Override
    public void close() {
        int d = DEPTH.get() - 1;
        if (d <= 0) {
            DEPTH.remove();
        } else {
            DEPTH.set(d);
        }
    }
}

