package com.github.TeThoLaPot.regen_resources.util;

import com.github.TeThoLaPot.regen_resources.RegenConstants;
import net.minecraft.core.BlockPos;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * {@link RegenMiningHelpers#regenBlockTargetedByPick} が 1 ティック内に何度も呼ばれるのを抑える。
 * {@code hasCorrectToolForDrops} → {@link net.minecraftforge.event.entity.player.PlayerEvent.HarvestCheck} などで
 * 毎回レイキャストすると負荷・ワールド保存時の体感フリーズの原因になり得る。
 */
public final class RegenMiningRayTickCache {

    private static final Map<UUID, Optional<BlockPos>> SERVER = new HashMap<>();
    private static final Map<UUID, Optional<BlockPos>> CLIENT = new HashMap<>();

    private RegenMiningRayTickCache() {}

    static Optional<BlockPos> getOrCompute(UUID id, boolean clientSide, Supplier<Optional<BlockPos>> compute) {
        Map<UUID, Optional<BlockPos>> map = clientSide ? CLIENT : SERVER;
        return map.computeIfAbsent(id, u -> compute.get());
    }

    static void clearServer() {
        SERVER.clear();
        RegenBreakDigContext.clearServerMap();
    }

    static void clearClient() {
        CLIENT.clear();
        RegenBreakDigContext.clearClientMap();
    }

    @Mod.EventBusSubscriber(modid = RegenConstants.MOD_ID)
    static final class ClearOnTick {
        private ClearOnTick() {}

        @SubscribeEvent
        public static void onServerTickStart(TickEvent.ServerTickEvent event) {
            if (event.phase == TickEvent.Phase.START) {
                clearServer();
            }
        }

        @SubscribeEvent
        public static void onClientTickStart(TickEvent.ClientTickEvent event) {
            if (event.phase == TickEvent.Phase.START) {
                clearClient();
            }
        }
    }
}
