/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.github.TeThoLaPot.tt_core.TT_core
 *  com.github.TeThoLaPot.tt_core.api.ITTTaskExecutor
 *  com.github.TeThoLaPot.tt_core.api.TTDataUtils
 *  com.github.TeThoLaPot.tt_core.data.TTDataBank
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction$Axis
 *  net.minecraft.core.HolderGetter
 *  net.minecraft.core.registries.Registries
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.NbtUtils
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.LevelAccessor
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.Blocks
 *  net.minecraft.world.level.block.RotatedPillarBlock
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.level.block.state.properties.Property
 *  net.minecraft.world.level.chunk.LevelChunk
 *  net.minecraftforge.event.level.BlockEvent$BreakEvent
 *  net.minecraftforge.event.server.ServerStartingEvent
 *  net.minecraftforge.eventbus.api.EventPriority
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.ModList
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber$Bus
 *  net.minecraftforge.network.PacketDistributor
 */
package com.github.TeThoLaPot.regen_resources.platform.forge.event;

import com.github.TeThoLaPot.regen_resources.common.block.RegenBlockEntity;
import com.github.TeThoLaPot.regen_resources.common.block.RegenBlocks;
import com.github.TeThoLaPot.regen_resources.common.block.RegenCustomVisualSpec;
import com.github.TeThoLaPot.regen_resources.common.block.RegenStrippedLogResolver;
import com.github.TeThoLaPot.regen_resources.common.block.RegenVisual;
import com.github.TeThoLaPot.regen_resources.common.item.BreakStuffItem;
import com.github.TeThoLaPot.regen_resources.common.regen.RegenMineMarker;
import com.github.TeThoLaPot.regen_resources.common.regen.RegenRule;
import com.github.TeThoLaPot.regen_resources.common.regen.RegenRuleRegistry;
import com.github.TeThoLaPot.regen_resources.common.tt.RegenSetBlockTtGuard;
import com.github.TeThoLaPot.regen_resources.platform.forge.block.Re_Blocks;
import com.github.TeThoLaPot.regen_resources.platform.forge.config.RegenPresetIo;
import com.github.TeThoLaPot.regen_resources.platform.forge.event.FtbUltimineChainProbe;
import com.github.TeThoLaPot.regen_resources.platform.forge.event.OreHarvesterChainProbe;
import com.github.TeThoLaPot.regen_resources.platform.forge.event.RegenOreHarvest;
import com.github.TeThoLaPot.regen_resources.platform.forge.event.RegenOreMineEligibility;
import com.github.TeThoLaPot.regen_resources.platform.forge.event.RegenSneakMineTracker;
import com.github.TeThoLaPot.regen_resources.platform.forge.network.ClientboundCustomVisualPendingPacket;
import com.github.TeThoLaPot.regen_resources.platform.forge.network.ClientboundStrippedPendingPacket;
import com.github.TeThoLaPot.regen_resources.platform.forge.network.RegenResourcesNetwork;
import com.github.TeThoLaPot.tt_core.TT_core;
import com.github.TeThoLaPot.tt_core.api.ITTTaskExecutor;
import com.github.TeThoLaPot.tt_core.api.TTDataUtils;
import com.github.TeThoLaPot.tt_core.data.TTDataBank;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

@Mod.EventBusSubscriber(modid="regen_resources", bus=Mod.EventBusSubscriber.Bus.FORGE)
public final class RegenRegenForgeEvents {
    private static final String EXECUTOR_ID = "regen_process";
    private static final String TAG_REGEN_TICKET = "regen_ticket";
    private static final String TAG_EXECUTE_AT = "execute_at";
    private static final String TAG_RESTORE_RL = "restore_rl";
    private static final ITTTaskExecutor REGEN_PROCESS_EXECUTOR = (level, data) -> {
        BlockPos pos = TTDataUtils.getBlockPos((CompoundTag)data, (String)"pos");
        CompoundTag persisted = TT_core.getBlockData((ServerLevel)level, (BlockPos)pos);
        if (data.m_128403_(TAG_REGEN_TICKET)) {
            UUID expected = data.m_128342_(TAG_REGEN_TICKET);
            if (!persisted.m_128403_(TAG_REGEN_TICKET) || !persisted.m_128342_(TAG_REGEN_TICKET).equals(expected)) {
                return;
            }
        }
        if (!level.m_8055_(pos).m_60713_((Block)Re_Blocks.REGEN_BLOCK.get())) {
            if (data.m_128403_(TAG_REGEN_TICKET) && persisted.m_128403_(TAG_REGEN_TICKET) && persisted.m_128342_(TAG_REGEN_TICKET).equals(data.m_128342_(TAG_REGEN_TICKET))) {
                TT_core.removeBlockData((ServerLevel)level, (BlockPos)pos);
            }
            return;
        }
        BlockState restore = data.m_128425_("state", 10) ? NbtUtils.m_247651_((HolderGetter)level.m_9598_().m_255025_(Registries.f_256747_), (CompoundTag)data.m_128469_("state")) : Blocks.f_50016_.m_49966_();
        byte snap = data.m_128425_("rr_src_snap", 1) ? data.m_128445_("rr_src_snap") : (byte)0;
        int update = 3;
        try (RegenSetBlockTtGuard ignored = RegenSetBlockTtGuard.acquire();){
            level.m_7731_(pos, restore, update);
        }
        TT_core.removeBlockData((ServerLevel)level, (BlockPos)pos);
        if (snap == 2) {
            CompoundTag eligibleOnly = new CompoundTag();
            eligibleOnly.m_128344_("rr_src", (byte)2);
            TT_core.saveBlockData((ServerLevel)level, (BlockPos)pos, (CompoundTag)eligibleOnly);
        }
    };

    private RegenRegenForgeEvents() {
    }

    @SubscribeEvent
    public static void onServerStarting(ServerStartingEvent event) {
        RegenRuleRegistry.setRules(RegenPresetIo.loadOrCreateDefaults());
        TTDataBank.registerExecutor((String)EXECUTOR_ID, (ITTTaskExecutor)REGEN_PROCESS_EXECUTOR);
    }

    @SubscribeEvent(priority=EventPriority.LOWEST, receiveCanceled=true)
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        boolean alreadyCanceled = event.isCanceled();
        LevelAccessor levelAccessor = event.getLevel();
        if (!(levelAccessor instanceof ServerLevel)) {
            return;
        }
        ServerLevel level = (ServerLevel)levelAccessor;
        Player player = event.getPlayer();
        if (player == null) {
            return;
        }
        if (!(player instanceof ServerPlayer)) {
            return;
        }
        ServerPlayer serverPlayer = (ServerPlayer)player;
        if (player.m_7500_()) {
            return;
        }
        if (RegenRegenForgeEvents.holdsBreakStuffRemoveMode(player)) {
            return;
        }
        BlockPos pos = event.getPos();
        BlockState broken = event.getState();
        if (broken.m_60713_((Block)Re_Blocks.REGEN_BLOCK.get())) {
            return;
        }
        ResourceLocation dim = level.m_46472_().m_135782_();
        RegenRule rule = RegenRuleRegistry.firstMatch(dim, broken);
        if (rule == null) {
            return;
        }
        BlockPos posImmutable = pos.m_7949_();
        if (!RegenOreMineEligibility.allows(level, posImmutable, rule)) {
            return;
        }
        if (ModList.get().isLoaded("oreharvester") && (serverPlayer.m_6047_() || RegenSneakMineTracker.wasSneakMining(serverPlayer, posImmutable) || OreHarvesterChainProbe.willChain(level, serverPlayer))) {
            return;
        }
        if (FtbUltimineChainProbe.isAvailable() && (FtbUltimineChainProbe.isChaining() || FtbUltimineChainProbe.isPressed(serverPlayer))) {
            return;
        }
        BlockState brokenSnapshot = broken;
        RegenRule ruleSnapshot = rule;
        byte priorSrc = RegenMineMarker.readSourceByte(TT_core.getBlockData((ServerLevel)level, (BlockPos)posImmutable));
        if (alreadyCanceled) {
            level.m_7654_().execute(() -> RegenRegenForgeEvents.commitOreBreakRegen(level, posImmutable, brokenSnapshot, ruleSnapshot, priorSrc));
            return;
        }
        event.setCanceled(true);
        if (!RegenOreHarvest.harvestAndRemove(serverPlayer, level, posImmutable, brokenSnapshot)) {
            event.setCanceled(false);
            return;
        }
        RegenRegenForgeEvents.commitOreBreakRegen(level, posImmutable, brokenSnapshot, ruleSnapshot, priorSrc);
    }

    public static void commitOreBreakRegen(ServerLevel level, BlockPos pos, BlockState brokenState, RegenRule rule) {
        RegenRegenForgeEvents.commitOreBreakRegen(level, pos, brokenState, rule, (byte)0);
    }

    public static void commitOreBreakRegen(ServerLevel level, BlockPos pos, BlockState brokenState, RegenRule rule, byte priorSourceMarker) {
        RegenBlockEntity rbe;
        BlockEntity blockEntity;
        LevelChunk chunk;
        if (!level.m_8055_(pos).m_60795_()) {
            return;
        }
        TT_core.removeBlockData((ServerLevel)level, (BlockPos)pos);
        CompoundTag data = new CompoundTag();
        TTDataUtils.putBlockPos((CompoundTag)data, (String)"pos", (BlockPos)pos);
        TTDataUtils.putBlockState((CompoundTag)data, (String)"state", (BlockState)brokenState);
        data.m_128359_("visual", rule.visual().m_7912_());
        data.m_128362_(TAG_REGEN_TICKET, UUID.randomUUID());
        data.m_128356_(TAG_EXECUTE_AT, level.m_46467_() + rule.delayTicks());
        data.m_128344_("rr_src_snap", priorSourceMarker);
        ResourceLocation restoreId = brokenState.m_60734_().m_204297_().m_205785_().m_135782_();
        data.m_128359_(TAG_RESTORE_RL, restoreId.toString());
        TT_core.saveBlockData((ServerLevel)level, (BlockPos)pos, (CompoundTag)data);
        TTDataBank.schedulePersistentTask((ServerLevel)level, (String)EXECUTOR_ID, (long)rule.delayTicks(), (CompoundTag)data.m_6426_());
        if (rule.visual() == RegenVisual.STRIPPED_LOG_PRESET || rule.visual() == RegenVisual.STRIPPED_LOG) {
            ResourceLocation strippedId = RegenStrippedLogResolver.resolveStrippedId(brokenState);
            RegenBlocks.preparePendingStrippedId(pos, strippedId);
            chunk = level.m_46745_(pos);
            RegenResourcesNetwork.CHANNEL.send(PacketDistributor.TRACKING_CHUNK.with(() -> chunk), (Object)new ClientboundStrippedPendingPacket(pos, strippedId));
        }
        RegenCustomVisualSpec customSpec = rule.customVisualSpec();
        if ((rule.visual() == RegenVisual.CUSTOM_PRESET || rule.visual() == RegenVisual.CUSTOM) && customSpec != null) {
            RegenBlocks.preparePendingCustomSpec(pos, customSpec);
            chunk = level.m_46745_(pos);
            RegenResourcesNetwork.CHANNEL.send(PacketDistributor.TRACKING_CHUNK.with(() -> chunk), (Object)new ClientboundCustomVisualPendingPacket(pos, customSpec));
        }
        BlockState waiting = (BlockState)((BlockState)((Block)Re_Blocks.REGEN_BLOCK.get()).m_49966_().m_61124_(RegenBlocks.VISUAL, (Comparable)((Object)rule.visual()))).m_61124_(RegenBlocks.AXIS, (Comparable)RegenRegenForgeEvents.axisFromBroken(brokenState));
        int update = 3;
        try (RegenSetBlockTtGuard ignored = RegenSetBlockTtGuard.acquire();){
            level.m_7731_(pos, waiting, update);
        }
        if (rule.visual() == RegenVisual.STRIPPED_LOG_PRESET || rule.visual() == RegenVisual.STRIPPED_LOG) {
            RegenBlockEntity rbe2;
            ResourceLocation strippedId = RegenStrippedLogResolver.resolveStrippedId(brokenState);
            BlockEntity blockEntity2 = level.m_7702_(pos);
            if (blockEntity2 instanceof RegenBlockEntity && (rbe2 = (RegenBlockEntity)blockEntity2).getStrippedBlockId() == null) {
                rbe2.setStrippedBlockId(strippedId);
            }
        }
        if ((rule.visual() == RegenVisual.CUSTOM_PRESET || rule.visual() == RegenVisual.CUSTOM) && customSpec != null && (blockEntity = level.m_7702_(pos)) instanceof RegenBlockEntity && (rbe = (RegenBlockEntity)blockEntity).getCustomVisualSpec() == null) {
            rbe.setCustomVisualSpec(customSpec);
        }
    }

    private static Direction.Axis axisFromBroken(BlockState brokenState) {
        if (brokenState.m_61138_((Property)RotatedPillarBlock.f_55923_)) {
            return (Direction.Axis)brokenState.m_61143_((Property)RotatedPillarBlock.f_55923_);
        }
        return Direction.Axis.Y;
    }

    private static boolean holdsBreakStuffRemoveMode(Player player) {
        for (InteractionHand hand : InteractionHand.values()) {
            ItemStack st = player.m_21120_(hand);
            if (!(st.m_41720_() instanceof BreakStuffItem) || !BreakStuffItem.isRemovalMode(st)) continue;
            return true;
        }
        return false;
    }
}

