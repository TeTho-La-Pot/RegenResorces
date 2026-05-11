package com.github.TeThoLaPot.regen_resources.platform.neoforge.event;

import com.mojang.logging.LogUtils;
import java.lang.reflect.Field;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

/**
 * サーバー側の「いま掘っている途中」の状態を止める。
 * <p>{@link ServerPlayerGameMode#handleBlockBreakAction} の {@code ABORT_DESTROY_BLOCK} だけでは
 * {@code hasDelayedDestroy}（進捗が足りず離したあとの遅延破壊）が残り、次ティックの {@link ServerPlayerGameMode#tick()}
 * で破壊が進むことがあるため、反射でそのフラグと進捗表示をまとめて落とす。
 * <p>OreHarvester の鉱脈一括は別経路でブロックを消すため、それ単体の停止には {@link OreHarvesterChainProbe} 側が必要。
 */
final class ServerMiningInterrupt {

    private static final Logger LOG = LogUtils.getLogger();

    @SuppressWarnings("JavaReflectionMemberAccess")
    @Nullable
    private static final Field F_HAS_DELAYED_DESTROY;

    @SuppressWarnings("JavaReflectionMemberAccess")
    @Nullable
    private static final Field F_DELAYED_DESTROY_POS;

    @SuppressWarnings("JavaReflectionMemberAccess")
    @Nullable
    private static final Field F_DESTROY_POS;

    @SuppressWarnings("JavaReflectionMemberAccess")
    @Nullable
    private static final Field F_LAST_SENT_STATE;

    static {
        Field hasDelayed = null;
        Field delayedPos = null;
        Field destroyPos = null;
        Field lastSent = null;
        try {
            hasDelayed = ServerPlayerGameMode.class.getDeclaredField("hasDelayedDestroy");
            hasDelayed.setAccessible(true);
            delayedPos = ServerPlayerGameMode.class.getDeclaredField("delayedDestroyPos");
            delayedPos.setAccessible(true);
            destroyPos = ServerPlayerGameMode.class.getDeclaredField("destroyPos");
            destroyPos.setAccessible(true);
            lastSent = ServerPlayerGameMode.class.getDeclaredField("lastSentState");
            lastSent.setAccessible(true);
        } catch (ReflectiveOperationException e) {
            LOG.warn("ServerPlayerGameMode fields not accessible; mining cancel may be incomplete after MC update", e);
        }
        F_HAS_DELAYED_DESTROY = hasDelayed;
        F_DELAYED_DESTROY_POS = delayedPos;
        F_DESTROY_POS = destroyPos;
        F_LAST_SENT_STATE = lastSent;
    }

    private ServerMiningInterrupt() {}

    /**
     * バニラの連続採掘・遅延破壊を打ち切り、ヒビ表示を消す。
     *
     * @param breakSpeedPos {@link net.neoforged.neoforge.event.entity.player.PlayerEvent.BreakSpeed} の対象位置
     */
    static void cancelDestroyProgress(ServerPlayer sp, BlockPos breakSpeedPos) {
        ServerPlayerGameMode gm = sp.gameMode;
        ServerLevel level = sp.serverLevel();
        int pid = sp.getId();

        BlockPos delayedPosSnapshot = null;
        BlockPos destroyPosSnapshot = null;
        try {
            if (F_HAS_DELAYED_DESTROY != null && F_HAS_DELAYED_DESTROY.getBoolean(gm) && F_DELAYED_DESTROY_POS != null) {
                delayedPosSnapshot = ((BlockPos) F_DELAYED_DESTROY_POS.get(gm)).immutable();
            }
            if (F_DESTROY_POS != null) {
                destroyPosSnapshot = ((BlockPos) F_DESTROY_POS.get(gm)).immutable();
            }
        } catch (ReflectiveOperationException e) {
            LOG.debug("Could not read destroy positions", e);
        }

        try {
            if (F_HAS_DELAYED_DESTROY != null) {
                F_HAS_DELAYED_DESTROY.setBoolean(gm, false);
            }
            if (F_LAST_SENT_STATE != null) {
                F_LAST_SENT_STATE.setInt(gm, -1);
            }
        } catch (ReflectiveOperationException e) {
            LOG.debug("Could not reset delayed destroy flags", e);
        }

        BlockPos abortAt = breakSpeedPos != null ? breakSpeedPos : destroyPosSnapshot;
        if (abortAt != null) {
            try {
                gm.handleBlockBreakAction(
                        abortAt,
                        ServerboundPlayerActionPacket.Action.ABORT_DESTROY_BLOCK,
                        Direction.UP,
                        level.getMaxBuildHeight(),
                        0);
            } catch (Throwable t) {
                LOG.debug("handleBlockBreakAction ABORT failed for {}", sp.getName().getString(), t);
            }
        }

        if (delayedPosSnapshot != null) {
            level.destroyBlockProgress(pid, delayedPosSnapshot, -1);
        }
        if (destroyPosSnapshot != null) {
            level.destroyBlockProgress(pid, destroyPosSnapshot, -1);
        }
        if (breakSpeedPos != null
                && (delayedPosSnapshot == null || !delayedPosSnapshot.equals(breakSpeedPos))
                && (destroyPosSnapshot == null || !destroyPosSnapshot.equals(breakSpeedPos))) {
            level.destroyBlockProgress(pid, breakSpeedPos, -1);
        }
    }
}
