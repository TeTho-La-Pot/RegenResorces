package com.github.TeThoLaPot.regen_resources.util;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * {@link net.minecraftforge.event.entity.player.PlayerEvent.BreakSpeed} が届いたブロック座標を保持する。
 * {@code hasCorrectToolForDrops} / {@link net.minecraftforge.event.entity.player.PlayerEvent.HarvestCheck} では
 * 視線レイよりこちらを優先し、レイキャスト回数を減らす。
 */
public final class RegenBreakDigContext {

    private static final Map<UUID, BlockPos> SERVER = new HashMap<>();
    private static final Map<UUID, BlockPos> CLIENT = new HashMap<>();

    private RegenBreakDigContext() {}

    public static void set(Player player, BlockPos pos) {
        map(player).put(player.getUUID(), pos);
    }

    public static void clear(Player player) {
        map(player).remove(player.getUUID());
    }

    public static void clearServerMap() {
        SERVER.clear();
    }

    public static void clearClientMap() {
        CLIENT.clear();
    }

    public static BlockPos peek(Player player) {
        return map(player).get(player.getUUID());
    }

    private static Map<UUID, BlockPos> map(Player player) {
        return player.level().isClientSide() ? CLIENT : SERVER;
    }
}
