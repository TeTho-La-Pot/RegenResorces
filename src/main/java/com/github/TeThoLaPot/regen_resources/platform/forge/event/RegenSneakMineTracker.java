/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.server.level.ServerPlayer
 */
package com.github.TeThoLaPot.regen_resources.platform.forge.event;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;

final class RegenSneakMineTracker {
    private static final long TTL_MS = 1500L;
    private static final Map<UUID, Entry> RECENT = new ConcurrentHashMap<UUID, Entry>();

    private RegenSneakMineTracker() {
    }

    static void note(ServerPlayer player, BlockPos pos) {
        RECENT.put(player.getUUID(), new Entry(pos.immutable(), System.currentTimeMillis()));
    }

    static boolean wasSneakMining(ServerPlayer player, BlockPos pos) {
        Entry e = RECENT.get(player.getUUID());
        if (e == null) {
            return false;
        }
        if (System.currentTimeMillis() - e.timestampMs > 1500L) {
            RECENT.remove(player.getUUID(), e);
            return false;
        }
        return e.pos.equals(pos);
    }

    static void clear(ServerPlayer player) {
        RECENT.remove(player.getUUID());
    }

    private record Entry(BlockPos pos, long timestampMs) {
    }
}

