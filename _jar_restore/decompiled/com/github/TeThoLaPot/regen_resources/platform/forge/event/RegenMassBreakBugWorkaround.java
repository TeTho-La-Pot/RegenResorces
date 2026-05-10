/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.network.protocol.game.ServerboundPlayerActionPacket$Action
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.entity.player.Player
 *  net.minecraftforge.event.entity.player.PlayerEvent$BreakSpeed
 *  net.minecraftforge.event.entity.player.PlayerEvent$PlayerLoggedOutEvent
 *  net.minecraftforge.eventbus.api.EventPriority
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber$Bus
 *  org.slf4j.Logger
 */
package com.github.TeThoLaPot.regen_resources.platform.forge.event;

import com.github.TeThoLaPot.regen_resources.platform.forge.config.RegenResourcesForgeConfig;
import com.github.TeThoLaPot.regen_resources.platform.forge.event.OreHarvesterChainProbe;
import com.mojang.logging.LogUtils;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;

@Mod.EventBusSubscriber(modid="regen_resources", bus=Mod.EventBusSubscriber.Bus.FORGE)
public final class RegenMassBreakBugWorkaround {
    private static final Logger LOG = LogUtils.getLogger();
    private static final long IDLE_EXPIRE_MS = 2000L;
    private static final Map<UUID, Snapshot> SNAPSHOTS = new ConcurrentHashMap<UUID, Snapshot>();

    private RegenMassBreakBugWorkaround() {
    }

    @SubscribeEvent(priority=EventPriority.LOWEST)
    public static void onBreakSpeed(PlayerEvent.BreakSpeed event) {
        if (event.isCanceled()) {
            return;
        }
        if (!((Boolean)RegenResourcesForgeConfig.MASS_BREAK_BUG_WORKAROUND.get()).booleanValue()) {
            return;
        }
        Player player = event.getEntity();
        if (!(player instanceof ServerPlayer)) {
            return;
        }
        ServerPlayer sp = (ServerPlayer)player;
        if (sp.m_7500_()) {
            return;
        }
        Optional opt = event.getPosition();
        if (opt.isEmpty()) {
            return;
        }
        BlockPos pos = ((BlockPos)opt.get()).m_7949_();
        boolean sneakNow = sp.m_6047_();
        long now = System.currentTimeMillis();
        UUID id = sp.m_20148_();
        Snapshot prev = SNAPSHOTS.get(id);
        if (prev == null || !prev.pos.equals((Object)pos) || now - prev.lastSeenMs > 2000L) {
            SNAPSHOTS.put(id, new Snapshot(pos, sneakNow, now));
            return;
        }
        if (prev.sneakAtStart != sneakNow) {
            try {
                sp.f_8941_.m_214168_(pos, ServerboundPlayerActionPacket.Action.ABORT_DESTROY_BLOCK, Direction.UP, sp.m_284548_().m_151558_(), 0);
                sp.m_284548_().m_6801_(sp.m_19879_(), pos, -1);
                OreHarvesterChainProbe.clearCacheFor(sp.m_284548_(), sp);
            }
            catch (Throwable t) {
                LOG.debug("Failed to abort mining for {} at {}", new Object[]{sp.m_7755_().getString(), pos, t});
            }
            SNAPSHOTS.put(id, new Snapshot(pos, sneakNow, now));
            return;
        }
        prev.lastSeenMs = now;
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        SNAPSHOTS.remove(event.getEntity().m_20148_());
    }

    private static final class Snapshot {
        final BlockPos pos;
        final boolean sneakAtStart;
        long lastSeenMs;

        Snapshot(BlockPos pos, boolean sneakAtStart, long lastSeenMs) {
            this.pos = pos;
            this.sneakAtStart = sneakAtStart;
            this.lastSeenMs = lastSeenMs;
        }
    }
}

