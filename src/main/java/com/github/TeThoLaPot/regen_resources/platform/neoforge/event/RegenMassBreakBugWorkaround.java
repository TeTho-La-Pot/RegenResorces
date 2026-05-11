package com.github.TeThoLaPot.regen_resources.platform.neoforge.event;

import com.github.TeThoLaPot.regen_resources.platform.neoforge.config.RegenResourcesForgeConfig;
import com.mojang.logging.LogUtils;
import java.lang.reflect.Field;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

/**
 * 連鎖採掘系 MOD（OreHarvester 等）が、同一ブロック採掘中のしゃがみ状態変化で誤って一括破壊を起動する件の回避。
 * <p>1.20.1 Forge 版と同じく {@link Player#isCrouching()} を「採掘セッション開始時」と比較し、変化したら
 * {@link ServerMiningInterrupt} でサーバー採掘状態を打ち切り、{@link OreHarvesterChainProbe#clearCacheFor} で OH のキャッシュを捨てる。
 * <p>現行 OreHarvester は {@code harvestSpeedCache} があると {@code onOreHarvest} でしゃがみを再確認せず一括破壊するため、
 * しゃがみが不要な設定でない限り、しゃがんでいないティックでは毎回キャッシュを捨てて一括経路を上書きする。
 */
public final class RegenMassBreakBugWorkaround {

    private static final Logger LOG = LogUtils.getLogger();
    private static final long IDLE_EXPIRE_MS = 2000L;
    private static final Map<UUID, Snapshot> SNAPSHOTS = new ConcurrentHashMap<>();

    /** null = 未解決。true = 一括にしゃがみ必須（既定）。false = 「しゃがまないときだけ」一括（oreHarvestWithoutSneak）。 */
    @Nullable
    private static volatile Boolean ohVeinRequiresCrouchCached;

    private RegenMassBreakBugWorkaround() {}

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onBreakSpeed(PlayerEvent.BreakSpeed event) {
        if (event.isCanceled()) {
            return;
        }
        if (!RegenResourcesForgeConfig.MASS_BREAK_BUG_WORKAROUND.get()) {
            return;
        }
        Player player = event.getEntity();
        if (!(player instanceof ServerPlayer sp)) {
            return;
        }
        if (sp.isCreative()) {
            return;
        }
        BlockPos pos = event.getPosition().orElse(null);
        if (pos == null) {
            return;
        }
        pos = pos.immutable();

        if (ModList.get().isLoaded("oreharvester") && shouldClearOhHarvestSpeedCache(sp)) {
            OreHarvesterChainProbe.clearCacheFor(sp.serverLevel(), sp);
        }

        boolean sneakNow = sp.isCrouching();
        long now = System.currentTimeMillis();
        UUID id = sp.getUUID();
        Snapshot prev = SNAPSHOTS.get(id);
        if (prev == null || !prev.pos.equals(pos) || now - prev.lastSeenMs > IDLE_EXPIRE_MS) {
            SNAPSHOTS.put(id, new Snapshot(pos, sneakNow, now));
            return;
        }
        if (prev.sneakAtStart != sneakNow) {
            try {
                ServerMiningInterrupt.cancelDestroyProgress(sp, pos);
                OreHarvesterChainProbe.clearCacheFor(sp.serverLevel(), sp);
            } catch (Throwable t) {
                LOG.debug("Failed to abort mining for {} at {}", sp.getName().getString(), pos, t);
            }
            SNAPSHOTS.put(id, new Snapshot(pos, sneakNow, now));
            return;
        }
        prev.lastSeenMs = now;
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        SNAPSHOTS.remove(event.getEntity().getUUID());
    }

    /**
     * OH の {@code onHarvestBreakSpeed} はしゃがんでいないと早期 return するが {@code harvestSpeedCache} を消さない。
     * そのままだとブロック破壊時にキャッシュ経路で一括が走るため、前提を満たしていないなら毎ティック掃除する。
     */
    private static boolean shouldClearOhHarvestSpeedCache(ServerPlayer sp) {
        if (!ohVeinRequiresCrouch()) {
            return sp.isCrouching();
        }
        return !sp.isCrouching();
    }

    /** @return true なら一括は「しゃがみ中のみ」。false なら「非しゃがみ中のみ」（OH の oreHarvestWithoutSneak）。 */
    private static boolean ohVeinRequiresCrouch() {
        Boolean cached = ohVeinRequiresCrouchCached;
        if (cached != null) {
            return cached;
        }
        synchronized (RegenMassBreakBugWorkaround.class) {
            cached = ohVeinRequiresCrouchCached;
            if (cached != null) {
                return cached;
            }
            boolean requiresCrouch = true;
            try {
                Class<?> cfg = Class.forName("com.natamus.oreharvester.config.ConfigHandler");
                Field f = cfg.getField("oreHarvestWithoutSneak");
                boolean withoutSneak = f.getBoolean(null);
                requiresCrouch = !withoutSneak;
            } catch (ReflectiveOperationException | LinkageError e) {
                LOG.debug("OreHarvester ConfigHandler.oreHarvestWithoutSneak not readable; assuming sneak-required vein", e);
            }
            ohVeinRequiresCrouchCached = requiresCrouch;
            return requiresCrouch;
        }
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
