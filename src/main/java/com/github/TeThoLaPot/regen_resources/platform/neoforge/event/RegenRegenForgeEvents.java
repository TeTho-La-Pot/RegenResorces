package com.github.TeThoLaPot.regen_resources.platform.neoforge.event;

import com.github.TeThoLaPot.regen_resources.common.block.RegenCorruptionFallback;
import com.github.TeThoLaPot.regen_resources.common.block.RegenBlocks;
import com.github.TeThoLaPot.regen_resources.common.item.BreakStuffItem;
import com.github.TeThoLaPot.regen_resources.common.regen.RegenMineMarker;
import com.github.TeThoLaPot.regen_resources.common.regen.RegenRule;
import com.github.TeThoLaPot.regen_resources.common.regen.RegenRuleRegistry;
import com.github.TeThoLaPot.regen_resources.common.tt.RegenSetBlockTtGuard;
import com.github.TeThoLaPot.regen_resources.platform.neoforge.config.RegenResourcesForgeConfig;
import com.github.TeThoLaPot.regen_resources.platform.neoforge.RegenResourcesForgeBootstrap;
import com.github.TeThoLaPot.regen_resources.platform.neoforge.block.Re_Blocks;
import com.github.TeThoLaPot.tt_core.TT_core;
import com.github.TeThoLaPot.tt_core.api.ITTTaskExecutor;
import com.github.TeThoLaPot.tt_core.api.TTDataUtils;
import com.github.TeThoLaPot.tt_core.data.TTDataBank;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;

import java.util.UUID;

/**
 * NeoForge サーバーイベント配線。
 * - 起動時に config JSON をロード
 * - executor を登録
 * <p>クリエイティブはバニラの破壊（ドロップなし）のままとし、{@link BlockEvent.BreakEvent} には介入しない。
 * <p>サバイバルで再生シェルを設置するかは TT の {@code rr_src} と {@link RegenResourcesForgeConfig#ALLOW_NATURAL_REGEN} で決める。
 * 設置しない場合はイベントをキャンセルせず、バニラの破壊・ドロップのまま。
 * <p>再生シェルを設置する場合のみ {@link BlockEvent.BreakEvent} をキャンセルし foot-drop 収穫後に TT・シェルを置く。
 */
public final class RegenRegenForgeEvents {

    private static final String EXECUTOR_ID = "regen_process";
    /** alpha と同様。キューに載ったタスクと {@link TT_core} のブロックデータが一致するときだけ復元する。 */
    private static final String TAG_REGEN_TICKET = "regen_ticket";
    private static final String TAG_EXECUTE_AT = RegenCorruptionFallback.TT_EXECUTE_AT;
    private static final String TAG_RESTORE_RL = "restore_rl";

    private RegenRegenForgeEvents() {}

    @SubscribeEvent
    public static void onServerStarting(ServerStartingEvent event) {
        RegenResourcesForgeBootstrap.applyPresetRulesFromDisk();
        TTDataBank.registerExecutor(EXECUTOR_ID, REGEN_PROCESS_EXECUTOR);
    }

    // OreHarvester など「BreakEvent を見て連鎖破壊を始動する」系 MOD と共存するため、
    // こちらのキャンセルは最後（LOWEST）に寄せる。
    // ただし、一括破壊側がイベントをキャンセルして独自破壊する場合もあるため、cancel 済みも受け取って
    // 「実際に空気になった」ことを確認してからシェル設置だけ追従する。
    @SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = true)
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        boolean alreadyCanceled = event.isCanceled();
        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;
        }
        Player player = event.getPlayer();
        if (player == null) {
            return;
        }
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        if (player.isCreative()) {
            return;
        }
        if (holdsBreakStuffRemoveMode(player)) {
            return;
        }

        BlockPos pos = event.getPos();
        BlockState broken = event.getState();

        if (broken.is(Re_Blocks.REGEN_BLOCK.get())) {
            return;
        }

        ResourceLocation dim = level.dimension().location();
        RegenRule rule = RegenRuleRegistry.firstMatch(dim, broken);
        if (rule == null) {
            return;
        }

        BlockPos posImmutable = pos.immutable();
        if (!RegenOreMineEligibility.allows(level, posImmutable)) {
            return;
        }

        // OreHarvester は起点の BreakEvent だけを使い、周辺鉱石は独自処理で壊すため
        // こちらの「キャンセル＋手動ドロップ」は二重ドロップの原因になる。互換側へ任せる。
        if (ModList.get().isLoaded("oreharvester")) {
            return;
        }

        BlockState brokenSnapshot = broken;
        RegenRule ruleSnapshot = rule;

        if (alreadyCanceled) {
            // 他 MOD がキャンセルして自前で破壊する場合（OreHarvester 等）:
            // こちらはドロップ生成を行わず、次 tick に「空気化していれば」シェル設置だけ行う。
            // （既に誰かがシェルを置いている／TT が入っている場合は commit 側のガードで弾かれる）
            level.getServer()
                    .execute(() -> commitOreBreakRegen(level, posImmutable, brokenSnapshot, ruleSnapshot));
            return;
        }

        event.setCanceled(true);
        if (!RegenOreHarvest.harvestAndRemove(serverPlayer, level, posImmutable, brokenSnapshot)) {
            event.setCanceled(false);
            return;
        }

        commitOreBreakRegen(level, posImmutable, brokenSnapshot, ruleSnapshot);
    }

    /**
     * alpha の {@code commitOreBreakRegen} と同じ経路: 実行キューに載せてから TT・シェル設置。
     */
    public static void commitOreBreakRegen(ServerLevel level, BlockPos pos, BlockState brokenState, RegenRule rule) {
        if (!level.getBlockState(pos).isAir()) {
            return;
        }

        // 以前のサイクルのキュー・ブロックマップを残すとタスクが複数走り、復元タイミングが不定になる
        CompoundTag priorPlacement = TT_core.getBlockData(level, pos);
        byte sourceSnap = RegenMineMarker.readSourceByte(priorPlacement);

        TT_core.removeBlockData(level, pos);

        CompoundTag data = new CompoundTag();
        TTDataUtils.putBlockPos(data, "pos", pos);
        TTDataUtils.putBlockState(data, "state", brokenState);
        data.putString("visual", rule.visual().getSerializedName());
        data.putUUID(TAG_REGEN_TICKET, UUID.randomUUID());
        data.putLong(TAG_EXECUTE_AT, level.getGameTime() + rule.delayTicks());
        data.putByte(RegenMineMarker.TT_SNAPSHOT, sourceSnap);
        var restoreId = brokenState.getBlock().builtInRegistryHolder().key().location();
        data.putString(TAG_RESTORE_RL, restoreId.toString());

        TT_core.saveBlockData(level, pos, data);
        TTDataBank.schedulePersistentTask(level, EXECUTOR_ID, rule.delayTicks(), data.copy());

        BlockState waiting =
                Re_Blocks.REGEN_BLOCK.get().defaultBlockState().setValue(RegenBlocks.VISUAL, rule.visual());
        int update = Block.UPDATE_NEIGHBORS | Block.UPDATE_CLIENTS;
        try (RegenSetBlockTtGuard ignored = RegenSetBlockTtGuard.acquire()) {
            level.setBlock(pos, waiting, update);
        }
    }

    private static boolean holdsBreakStuffRemoveMode(Player player) {
        for (InteractionHand hand : InteractionHand.values()) {
            ItemStack st = player.getItemInHand(hand);
            if (st.getItem() instanceof BreakStuffItem && BreakStuffItem.isRemovalMode(st)) {
                return true;
            }
        }
        return false;
    }

    private static final ITTTaskExecutor REGEN_PROCESS_EXECUTOR = (level, data) -> {
        BlockPos pos = TTDataUtils.getBlockPos(data, "pos");
        CompoundTag persisted = TT_core.getBlockData(level, pos);

        if (data.hasUUID(TAG_REGEN_TICKET)) {
            UUID expected = data.getUUID(TAG_REGEN_TICKET);
            if (!persisted.hasUUID(TAG_REGEN_TICKET) || !persisted.getUUID(TAG_REGEN_TICKET).equals(expected)) {
                return;
            }
        }

        if (!level.getBlockState(pos).is(Re_Blocks.REGEN_BLOCK.get())) {
            if (data.hasUUID(TAG_REGEN_TICKET)
                    && persisted.hasUUID(TAG_REGEN_TICKET)
                    && persisted.getUUID(TAG_REGEN_TICKET).equals(data.getUUID(TAG_REGEN_TICKET))) {
                TT_core.removeBlockData(level, pos);
            }
            return;
        }

        BlockState restore =
                data.contains("state", CompoundTag.TAG_COMPOUND)
                        ? NbtUtils.readBlockState(
                                level.registryAccess().lookupOrThrow(Registries.BLOCK),
                                data.getCompound("state"))
                        : Blocks.AIR.defaultBlockState();
        byte snap =
                data.contains(RegenMineMarker.TT_SNAPSHOT, CompoundTag.TAG_BYTE)
                        ? data.getByte(RegenMineMarker.TT_SNAPSHOT)
                        : RegenMineMarker.SRC_IMPLICIT;
        int update = Block.UPDATE_NEIGHBORS | Block.UPDATE_CLIENTS;
        level.setBlock(pos, restore, update);
        TT_core.removeBlockData(level, pos);
        if (snap == RegenMineMarker.SRC_ELIGIBLE) {
            CompoundTag eligibleOnly = new CompoundTag();
            eligibleOnly.putByte(RegenMineMarker.TT_SOURCE, RegenMineMarker.SRC_ELIGIBLE);
            TT_core.saveBlockData(level, pos, eligibleOnly);
        }
    };
}

