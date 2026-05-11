package com.github.TeThoLaPot.regen_resources.platform.neoforge.event;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;

/** しゃがみ採掘の直後位置を短時間だけ記録（OreHarvester 連鎖判定用）。 */
final class RegenSneakMineTracker {

    private static final long TTL_MS = 1500L;
    private static final Map<UUID, Entry> RECENT = new ConcurrentHashMap<>();

    private RegenSneakMineTracker() {}

    static void note(ServerPlayer player, BlockPos pos) {
        RECENT.put(player.getUUID(), new Entry(pos.immutable(), System.currentTimeMillis()));
    }

    static boolean wasSneakMining(ServerPlayer player, BlockPos pos) {
        Entry e = RECENT.get(player.getUUID());
        if (e == null) {
            return false;
        }
        if (System.currentTimeMillis() - e.timestampMs > TTL_MS) {
            RECENT.remove(player.getUUID(), e);
            return false;
        }
        return e.pos.equals(pos);
    }

    private record Entry(BlockPos pos, long timestampMs) {}
}
