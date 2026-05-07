package com.github.TeThoLaPot.regen_resources.forge;

import com.github.TeThoLaPot.regen_resources.common.block.RegenBlocks;
import com.github.TeThoLaPot.regen_resources.common.item.BreakStuffItem;
import com.github.TeThoLaPot.regen_resources.common.regen.RegenRule;
import com.github.TeThoLaPot.regen_resources.common.regen.RegenRuleRegistry;
import com.github.TeThoLaPot.regen_resources.forge.block.Re_Blocks;
import com.github.TeThoLaPot.regen_resources.forge.config.RegenPresetIo;
import com.github.TeThoLaPot.tt_core.TT_core;
import com.github.TeThoLaPot.tt_core.api.ITTTaskExecutor;
import com.github.TeThoLaPot.tt_core.api.TTDataUtils;
import com.github.TeThoLaPot.tt_core.data.TTDataBank;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.UUID;

/**
 * Forge 固有のイベント配線。
 * - 起動時に config JSON をロード
 * - executor を登録
 * - ブロック破壊で再生をスケジュール
 * <p>再生対象鉱石は Regen_Ore と同様、{@link BlockEvent.BreakEvent} をキャンセルして loot を評価し、
 * ドロップと経験値はプレイヤー足元に出して短いピックアップ遅延で疑似的な直接収納とする（満杯時は地面に残る）。
 */
@Mod.EventBusSubscriber(modid = com.github.TeThoLaPot.regen_resources.RegenResources.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class RegenRegenForgeEvents {

    private static final String EXECUTOR_ID = "regen_process";
    /** alpha と同様。キューに載ったタスクと {@link TT_core} のブロックデータが一致するときだけ復元する。 */
    private static final String TAG_REGEN_TICKET = "regen_ticket";
    private static final String TAG_EXECUTE_AT = "execute_at";
    private static final String TAG_RESTORE_RL = "restore_rl";

    private RegenRegenForgeEvents() {}

    @SubscribeEvent
    public static void onServerStarting(ServerStartingEvent event) {
        RegenRuleRegistry.setRules(RegenPresetIo.loadOrCreateDefaults());
        TTDataBank.registerExecutor(EXECUTOR_ID, REGEN_PROCESS_EXECUTOR);
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (event.isCanceled()) {
            return;
        }
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
        BlockState brokenSnapshot = broken;
        RegenRule ruleSnapshot = rule;

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
    private static void commitOreBreakRegen(ServerLevel level, BlockPos pos, BlockState brokenState, RegenRule rule) {
        if (!level.getBlockState(pos).isAir()) {
            return;
        }

        // 以前のサイクルのキュー・ブロックマップを残すとタスクが複数走り、復元タイミングが不定になる
        TT_core.removeBlockData(level, pos);

        CompoundTag data = new CompoundTag();
        TTDataUtils.putBlockPos(data, "pos", pos);
        TTDataUtils.putBlockState(data, "state", brokenState);
        data.putString("visual", rule.visual().getSerializedName());
        data.putUUID(TAG_REGEN_TICKET, UUID.randomUUID());
        data.putLong(TAG_EXECUTE_AT, level.getGameTime() + rule.delayTicks());
        var restoreId = brokenState.getBlock().builtInRegistryHolder().key().location();
        data.putString(TAG_RESTORE_RL, restoreId.toString());

        TT_core.saveBlockData(level, pos, data);
        TTDataBank.schedulePersistentTask(level, EXECUTOR_ID, rule.delayTicks(), data.copy());

        BlockState waiting = Re_Blocks.REGEN_BLOCK.get().defaultBlockState()
                .setValue(RegenBlocks.VISUAL, rule.visual());
        int update = Block.UPDATE_NEIGHBORS | Block.UPDATE_CLIENTS;
        level.setBlock(pos, waiting, update);
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

        BlockState restore = TTDataUtils.readBlockState(data, "state", level.registryAccess());
        int update = Block.UPDATE_NEIGHBORS | Block.UPDATE_CLIENTS;
        level.setBlock(pos, restore, update);
        TT_core.removeBlockData(level, pos);
    };
}
