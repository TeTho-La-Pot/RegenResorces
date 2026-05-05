package com.github.TeThoLaPot.regen_resources.util;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

/**
 * インベントリ直叩きではなく足元へ即時収集できるドロップにし、収集音が鳴るようにする。
 */
public final class NearPlayerDropUtil {

    /** ほぼ次ティックから拾える */
    public static final int PICKUP_DELAY_TICKS = 0;

    private NearPlayerDropUtil() {}

    public static void dropStacksAtFeetForVanillaPickup(ServerPlayer player, ItemStack stack) {
        if (stack.isEmpty()) {
            return;
        }
        ItemStack copy = stack.copy();
        while (!copy.isEmpty()) {
            int n = Math.min(copy.getCount(), copy.getMaxStackSize());
            ItemStack piece = copy.split(n);
            ItemEntity dropped = player.spawnAtLocation(piece);
            if (dropped != null) {
                dropped.setPickUpDelay(PICKUP_DELAY_TICKS);
                dropped.setDeltaMovement(Vec3.ZERO);
            }
        }
    }
}
