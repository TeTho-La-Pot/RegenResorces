package com.github.TeThoLaPot.regen_resources.forge;

import com.github.TeThoLaPot.regen_resources.Config;
import com.github.TeThoLaPot.regen_resources.RegenConstants;
import com.github.TeThoLaPot.regen_resources.init.block.Re_Blocks;
import com.github.TeThoLaPot.regen_resources.init.block.RegenBlocks;
import com.github.TeThoLaPot.regen_resources.init.entity.RegenBlockEntity;
import com.github.TeThoLaPot.regen_resources.init.item.BreakStuff;
import com.github.TeThoLaPot.regen_resources.util.NearPlayerDropUtil;
import com.github.TeThoLaPot.regen_resources.util.RegenDiag;
import com.github.TeThoLaPot.regen_resources.util.RegenHarvestContext;
import com.github.TeThoLaPot.regen_resources.util.RegenProcessWarmup;
import com.github.TeThoLaPot.regen_resources.util.RegenRestoreGuard;
import com.github.TeThoLaPot.tt_core.TT_core;
import com.github.TeThoLaPot.tt_core.data.TTDataBank;
import com.github.TeThoLaPot.tt_core.api.TTDataUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.piston.PistonStructureResolver;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.level.PistonEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = RegenConstants.MOD_ID)
public final class RegenGameplayEvents {

    private RegenGameplayEvents() {}

    @SubscribeEvent
    public static void onServerStartedForRegenWarmup(ServerStartedEvent event) {
        RegenProcessWarmup.registerServerWarmup(
                event.getServer(),
                Config.REGEN_PROCESS_SERVER_WARMUP_TICKS.get());
    }

    private static boolean allowsRegenBreaker(Player player) {
        if (!(player instanceof ServerPlayer)) return false;
        if (!Config.ALLOW_FAKE_PLAYER_REGEN.get() && player instanceof FakePlayer) return false;
        return true;
    }

    private static boolean holdsBreakStuffRemoveMode(Player player) {
        for (InteractionHand hand : InteractionHand.values()) {
            ItemStack st = player.getItemInHand(hand);
            if (st.getItem() instanceof BreakStuff bs && bs.modeNum(st) == 1) {
                return true;
            }
        }
        return false;
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onDenyBreakingDuringOreRestore(BlockEvent.BreakEvent event) {
        if (!(event.getLevel() instanceof ServerLevel)) return;
        if (!RegenRestoreGuard.isBlockingBreakAt(event.getPos())) return;
        Player breaker = event.getPlayer();
        if (breaker != null && holdsBreakStuffRemoveMode(breaker)) return;
        event.setCanceled(true);
    }

    private static boolean appliesRegenOreBreak(BlockEvent.BreakEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return false;
        Player breaker = event.getPlayer();
        if (breaker == null) return false;
        if (holdsBreakStuffRemoveMode(breaker)) return false;
        if (breaker.isCreative()) return false;
        if (!allowsRegenBreaker(breaker)) return false;
        if (event.isCanceled()) return false;

        BlockState state = event.getState();
        if (!Config.isRegenTarget(level, state)) return false;
        return Config.placementAllowsRegen(TT_core.getBlockData(level, event.getPos()));
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onTryBreakRegenPlaceholder(BlockEvent.BreakEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        BlockState state = event.getState();
        if (!state.is(Re_Blocks.REGEN_BLOCK.get())) return;

        Player player = event.getPlayer();
        if (player == null) return;
        if (player.isCreative()) return;

        if (!allowsRegenBreaker(player)) {
            event.setCanceled(true);
            return;
        }

        if (holdsBreakStuffRemoveMode(player)) {
            TT_core.removeBlockData(level, event.getPos());
            return;
        }
        event.setCanceled(true);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onPistonStripRegen(PistonEvent.Pre event) {
        if (event.isCanceled()) return;
        if (!(event.getLevel() instanceof ServerLevel sl)) return;

        PistonStructureResolver resolver = event.getStructureHelper();
        if (resolver == null || !resolver.resolve()) return;

        for (BlockPos p : resolver.getToPush()) {
            stripRegenAt(sl, p);
        }
        for (BlockPos p : resolver.getToDestroy()) {
            stripRegenAt(sl, p);
        }
    }

    private static void stripRegenAt(ServerLevel level, BlockPos pos) {
        if (!TT_core.hasBlockData(level, pos)) {
            return;
        }
        BlockState cur = level.getBlockState(pos);
        if (cur.is(Re_Blocks.REGEN_BLOCK.get()) || Config.isRegenTarget(level, cur)) {
            TT_core.removeBlockData(level, pos);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void applyMiningXpDirect(BlockEvent.BreakEvent event) {
        if (!Config.GATHER_BREAK_LOOT_AT_PLAYER.get()) return;
        if (!appliesRegenOreBreak(event)) return;
        if (!(event.getPlayer() instanceof ServerPlayer sp)) return;

        int xp = event.getExpToDrop();
        if (xp > 0) {
            sp.giveExperiencePoints(xp);
            event.setExpToDrop(0);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void beginGatherDropsNearPlayer(BlockEvent.BreakEvent event) {
        if (!Config.GATHER_BREAK_LOOT_AT_PLAYER.get()) return;
        if (!appliesRegenOreBreak(event)) return;

        ServerPlayer breaker = (ServerPlayer) event.getPlayer();
        RegenHarvestContext.setHarvestingPlayer(breaker);
    }

    @SubscribeEvent(priority = EventPriority.NORMAL)
    public static void gatherBreakLoot(EntityJoinLevelEvent event) {
        if (!Config.GATHER_BREAK_LOOT_AT_PLAYER.get()) return;
        if (!(event.getLevel() instanceof ServerLevel)) return;

        if (RegenHarvestContext.isGatherLootReentrant()) {
            return;
        }

        ServerPlayer breaker = RegenHarvestContext.peekHarvestingPlayer();
        if (breaker == null) return;

        Entity spawned = event.getEntity();
        if (spawned instanceof ExperienceOrb) {
            event.setCanceled(true);
            return;
        }

        if (spawned instanceof ItemEntity item) {
            event.setCanceled(true);
            RegenHarvestContext.runSpawningHarvestDrops(() ->
                    NearPlayerDropUtil.dropStacksAtFeetForVanillaPickup(breaker, item.getItem().copy()));
        }
    }

    @SubscribeEvent(priority = EventPriority.NORMAL)
    public static void onOreBreakScheduleRegen(BlockEvent.BreakEvent event) {
        if (!appliesRegenOreBreak(event)) return;

        if (!(event.getLevel() instanceof ServerLevel level)) return;
        BlockPos pos = event.getPos().immutable();
        BlockState brokenState = event.getState();
        commitOreBreakRegen(level, pos, brokenState);
    }

    /**
     * 鉱石破壊に伴う TT 保存・永続タスク・再生シェル設置。{@code server.execute} 1 回にまとめる（試行錯誤前の素直な経路）。
     */
    private static void commitOreBreakRegen(ServerLevel level, BlockPos pos, BlockState brokenState) {
        level.getServer().execute(() -> {
            if (!level.getBlockState(pos).isAir()) {
                RegenDiag.log(
                        "commitOreBreakRegen abort: pos not air after execute pos={} block={}",
                        pos,
                        ForgeRegistries.BLOCKS.getKey(level.getBlockState(pos).getBlock()));
                return;
            }

            long delayTicks = Config.getDelayTicksFor(level, brokenState);

            CompoundTag data = new CompoundTag();
            data.putUUID("regen_ticket", UUID.randomUUID());
            TTDataUtils.putBlockPos(data, "pos", pos);
            TTDataUtils.putBlockState(data, "state", brokenState);
            ResourceLocation brokenId = ForgeRegistries.BLOCKS.getKey(brokenState.getBlock());
            if (brokenId != null) {
                data.putString(RegenBlockEntity.TAG_RESTORE_RL, brokenId.toString());
            }
            data.putLong("execute_at", level.getGameTime() + delayTicks);
            TT_core.saveBlockData(level, pos, data);

            CompoundTag taskHandle = new CompoundTag();
            TTDataUtils.putBlockPos(taskHandle, "pos", pos);
            TTDataBank.schedulePersistentTask(level, "regen_process", delayTicks, taskHandle);

            UUID placedTicket = data.getUUID("regen_ticket");
            RegenDiag.log(
                    "commitOreBreakRegen queued TT + task pos={} ticket={} delayTicks={} executeAtGameTime={}",
                    pos,
                    placedTicket,
                    delayTicks,
                    data.getLong("execute_at"));
            ServerLevel sl = level;
            sl.getServer().execute(() -> RegenRestoreGuard.runAt(pos, () -> {
                if (!sl.getBlockState(pos).isAir()) {
                    RegenDiag.log(
                            "commitOreBreakRegen shell skip: not air pos={} block={}",
                            pos,
                            ForgeRegistries.BLOCKS.getKey(sl.getBlockState(pos).getBlock()));
                    return;
                }
                CompoundTag still = TT_core.getBlockData(sl, pos);
                if (!still.hasUUID("regen_ticket") || !still.getUUID("regen_ticket").equals(placedTicket)) {
                    RegenDiag.log(
                            "commitOreBreakRegen shell skip: ticket mismatch pos={} expected={} hasTicket={}",
                            pos,
                            placedTicket,
                            still.hasUUID("regen_ticket") ? still.getUUID("regen_ticket") : "absent");
                    TTDataBank.cancelPersistentTasksAt(sl, pos);
                    return;
                }
                RegenBlocks regenBlock = (RegenBlocks) Re_Blocks.REGEN_BLOCK.get();
                BlockState regen = regenBlock.defaultBlockState()
                        .setValue(RegenBlocks.VISUAL, Config.resolveWaitingVisual(sl, brokenState));
                int update = Block.UPDATE_NEIGHBORS | Block.UPDATE_CLIENTS | Block.UPDATE_IMMEDIATE;
                sl.setBlock(pos, regen, update, 512);
                RegenDiag.log("commitOreBreakRegen shell placed pos={} ticket={}", pos, placedTicket);
                if (sl.getBlockEntity(pos) instanceof RegenBlockEntity be) {
                    be.syncFromWorldStorage(sl, pos);
                }
            }));
        });
    }

    /**
     * 生存でプレイヤーが置いたうち、{@code config/RegenResources/RegenPresets} に登録されたブロック
     * （{@link Config#isRegenTarget} が真）にだけ {@code origin=player} を TT に記録する。
     */
    @SubscribeEvent
    public static void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        if (!(event.getEntity() instanceof ServerPlayer player) || player.isCreative()) {
            return;
        }
        BlockState placed = event.getState();
        if (!Config.isRegenTarget(level, placed)) {
            return;
        }
        CompoundTag data = new CompoundTag();
        data.putString("origin", Config.ORIGIN_PLAYER);
        TT_core.saveBlockData(level, event.getPos(), data);
    }
}
