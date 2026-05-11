package com.github.TeThoLaPot.regen_resources.platform.neoforge.client.sync;

import com.github.TeThoLaPot.regen_resources.common.block.RegenBlockEntity;
import com.github.TeThoLaPot.regen_resources.common.block.RegenBlocks;
import com.github.TeThoLaPot.regen_resources.common.block.RegenCustomVisualSpec;
import com.github.TeThoLaPot.regen_resources.common.clientbridge.RegenStrippedCompositeClientHooks;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

/**
 * バニラブロック／BE パケットとカスタム pending の競合があっても、数秒間だけ見た目適用を再試行する。
 * <p>ブロック状態は変わらないまま ModelData だけが変わるため、チャンクメッシュが古い見ためのまま残ることがある。
 * その場合はバニラの {@link net.minecraft.client.renderer.LevelRenderer#setBlocksDirty} で周辺セクションのみ再評価する（軽い）。
 */
public final class RegenShellAppearanceClientRetry {

    private static final long MAX_RETRY_TICKS = 100L;

    private record PendingShell(
            @Nullable ResourceLocation stripped, @Nullable RegenCustomVisualSpec custom, long deadlineGameTime) {}

    private static final Map<BlockPos, PendingShell> PENDING = new ConcurrentHashMap<>();

    private RegenShellAppearanceClientRetry() {}

    /**
     * 当該座標±1 のセクションだけ dirty。近接ブロック破壊で直る事例と同等の効果で、広範囲の再構築はしない。
     */
    private static void nudgeRebuild(Minecraft mc, BlockPos pos) {
        if (mc.levelRenderer == null) {
            return;
        }
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();
        mc.levelRenderer.setBlocksDirty(x, y, z, x, y, z);
    }

    public static void remind(BlockPos pos, @Nullable ResourceLocation stripped, @Nullable RegenCustomVisualSpec custom) {
        if (stripped == null && custom == null) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (!(mc.level instanceof ClientLevel lvl)) {
            return;
        }
        BlockPos ip = pos.immutable();
        long deadline = lvl.getGameTime() + MAX_RETRY_TICKS;
        PENDING.merge(
                ip,
                new PendingShell(stripped, custom, deadline),
                (a, b) ->
                        new PendingShell(
                                b.stripped() != null ? b.stripped() : a.stripped(),
                                b.custom() != null ? b.custom() : a.custom(),
                                Math.max(a.deadlineGameTime(), b.deadlineGameTime())));
        nudgeRebuild(mc, ip);
    }

    public static void tick() {
        Minecraft mc = Minecraft.getInstance();
        if (!(mc.level instanceof ClientLevel lvl)) {
            if (!PENDING.isEmpty()) {
                PENDING.clear();
            }
            return;
        }
        long now = lvl.getGameTime();
        Iterator<Map.Entry<BlockPos, PendingShell>> it = PENDING.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<BlockPos, PendingShell> e = it.next();
            PendingShell want = e.getValue();
            if (now > want.deadlineGameTime()) {
                it.remove();
                continue;
            }
            BlockPos pos = e.getKey();
            BlockEntity be = lvl.getBlockEntity(pos);
            if (!(be instanceof RegenBlockEntity shell) || !(shell.getBlockState().getBlock() instanceof RegenBlocks)) {
                continue;
            }
            boolean strippedOk =
                    want.stripped() == null || Objects.equals(shell.getStrippedBlockId(), want.stripped());
            boolean customOk =
                    want.custom() == null || Objects.equals(shell.getCustomVisualSpec(), want.custom());
            boolean needNudge = false;
            if (!strippedOk && want.stripped() != null) {
                shell.setStrippedBlockId(want.stripped());
                RegenStrippedCompositeClientHooks.scheduleWarm(pos, want.stripped());
                needNudge = true;
            }
            if (!customOk && want.custom() != null) {
                shell.setCustomVisualSpec(want.custom());
                needNudge = true;
            }
            strippedOk =
                    want.stripped() == null || Objects.equals(shell.getStrippedBlockId(), want.stripped());
            customOk =
                    want.custom() == null || Objects.equals(shell.getCustomVisualSpec(), want.custom());
            if (strippedOk && customOk) {
                needNudge = true;
                it.remove();
            }
            if (needNudge) {
                nudgeRebuild(mc, pos);
            }
        }
    }
}
