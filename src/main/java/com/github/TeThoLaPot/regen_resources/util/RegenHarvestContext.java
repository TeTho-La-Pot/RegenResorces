package com.github.TeThoLaPot.regen_resources.util;

import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

/**
 * 鉱石破壊の同期区間（{@code ServerPlayerGameMode#destroyBlock}）だけ有効になるハーベスタを保持します。
 */
public final class RegenHarvestContext {
    private static final ThreadLocal<ServerPlayer> HARVESTING_PLAYER = new ThreadLocal<>();
    /** EntityJoinLevelEvent 内で足元へ ItemEntity を出す際、自分の spawn がさらに join を起こして再入するためのカウンタ。 */
    private static final ThreadLocal<Integer> DROP_RESPAWN_DEPTH = ThreadLocal.withInitial(() -> 0);

    private RegenHarvestContext() {}

    /** ItemEntity が join で再びこのハンドラに入ったときに true（外側でのみドロップ引き換えを行う）。 */
    public static boolean isGatherLootReentrant() {
        return DROP_RESPAWN_DEPTH.get() > 0;
    }

    private static void enterDropRespawn() {
        DROP_RESPAWN_DEPTH.set(DROP_RESPAWN_DEPTH.get() + 1);
    }

    private static void exitDropRespawn() {
        int d = DROP_RESPAWN_DEPTH.get() - 1;
        if (d <= 0) {
            DROP_RESPAWN_DEPTH.remove();
        } else {
            DROP_RESPAWN_DEPTH.set(d);
        }
    }

    /** 足元への ItemEntity スポーン区間のみ（イベント再入時は gather で無視させる）。 */
    public static void runSpawningHarvestDrops(Runnable spawnDrops) {
        enterDropRespawn();
        try {
            spawnDrops.run();
        } finally {
            exitDropRespawn();
        }
    }

    public static void setHarvestingPlayer(@Nullable ServerPlayer player) {
        if (player == null) HARVESTING_PLAYER.remove();
        else HARVESTING_PLAYER.set(player);
    }

    @Nullable
    public static ServerPlayer peekHarvestingPlayer() {
        return HARVESTING_PLAYER.get();
    }

    public static void clear() {
        HARVESTING_PLAYER.remove();
    }
}
