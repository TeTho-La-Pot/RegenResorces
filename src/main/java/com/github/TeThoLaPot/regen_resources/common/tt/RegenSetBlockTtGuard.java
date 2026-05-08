package com.github.TeThoLaPot.regen_resources.common.tt;

/**
 * {@link com.github.TeThoLaPot.regen_resources.mixin.LevelMixin} が {@code Level#setBlock} 成功時に TT を消す際、
 * {@link com.github.TeThoLaPot.regen_resources.forge.RegenRegenForgeEvents} の {@code commitOreBreakRegen} のように
 * 先に {@code TT_core#saveBlockData} してからシェルを置く経路ではデータが消えてしまうため、その {@code setBlock} だけ除外する。
 */
public final class RegenSetBlockTtGuard implements AutoCloseable {

    private static final ThreadLocal<Integer> DEPTH = ThreadLocal.withInitial(() -> 0);

    private RegenSetBlockTtGuard() {}

    /** try-with-resources で囲んだ間は LevelMixin の TT 削除を行わない。 */
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
