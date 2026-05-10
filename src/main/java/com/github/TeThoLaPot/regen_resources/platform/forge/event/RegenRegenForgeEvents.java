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
        if (data.hasUUID(TAG_REGEN_TICKET)) {
            UUID expected = data.getUUID(TAG_REGEN_TICKET);
            if (!persisted.hasUUID(TAG_REGEN_TICKET) || !persisted.getUUID(TAG_REGEN_TICKET).equals(expected)) {
                return;
            }
        }
        if (!level.getBlockState(pos).is((Block)Re_Blocks.REGEN_BLOCK.get())) {
            if (data.hasUUID(TAG_REGEN_TICKET) && persisted.hasUUID(TAG_REGEN_TICKET) && persisted.getUUID(TAG_REGEN_TICKET).equals(data.getUUID(TAG_REGEN_TICKET))) {
                TT_core.removeBlockData((ServerLevel)level, (BlockPos)pos);
            }
            return;
        }
        BlockState restore = data.contains("state", 10) ? NbtUtils.readBlockState((HolderGetter)level.registryAccess().lookupOrThrow(Registries.BLOCK), (CompoundTag)data.getCompound("state")) : Blocks.AIR.defaultBlockState();
        byte snap = data.contains("rr_src_snap", 1) ? data.getByte("rr_src_snap") : (byte)0;
        int update = 3;
        try (RegenSetBlockTtGuard ignored = RegenSetBlockTtGuard.acquire();){
            level.setBlock(pos, restore, update);
        }
        TT_core.removeBlockData((ServerLevel)level, (BlockPos)pos);
        if (snap == 2) {
            CompoundTag eligibleOnly = new CompoundTag();
            eligibleOnly.putByte("rr_src", (byte)2);
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
        if (player.isCreative()) {
            return;
        }
        if (RegenRegenForgeEvents.holdsBreakStuffRemoveMode(player)) {
            return;
        }
        BlockPos pos = event.getPos();
        BlockState broken = event.getState();
        if (broken.is((Block)Re_Blocks.REGEN_BLOCK.get())) {
            return;
        }
        ResourceLocation dim = level.dimension().location();
        RegenRule rule = RegenRuleRegistry.firstMatch(dim, broken);
        if (rule == null) {
            return;
        }
        BlockPos posImmutable = pos.immutable();
        if (!RegenOreMineEligibility.allows(level, posImmutable, rule)) {
            return;
        }
        if (ModList.get().isLoaded("oreharvester") && (serverPlayer.isCrouching() || RegenSneakMineTracker.wasSneakMining(serverPlayer, posImmutable) || OreHarvesterChainProbe.willChain(level, serverPlayer))) {
            return;
        }
        if (FtbUltimineChainProbe.isAvailable() && (FtbUltimineChainProbe.isChaining() || FtbUltimineChainProbe.isPressed(serverPlayer))) {
            return;
        }
        BlockState brokenSnapshot = broken;
        RegenRule ruleSnapshot = rule;
        byte priorSrc = RegenMineMarker.readSourceByte(TT_core.getBlockData((ServerLevel)level, (BlockPos)posImmutable));
        if (alreadyCanceled) {
            level.getServer().execute(() -> RegenRegenForgeEvents.commitOreBreakRegen(level, posImmutable, brokenSnapshot, ruleSnapshot, priorSrc));
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
        if (!level.getBlockState(pos).isAir()) {
            return;
        }
        TT_core.removeBlockData((ServerLevel)level, (BlockPos)pos);
        CompoundTag data = new CompoundTag();
        TTDataUtils.putBlockPos((CompoundTag)data, (String)"pos", (BlockPos)pos);
        TTDataUtils.putBlockState((CompoundTag)data, (String)"state", (BlockState)brokenState);
        data.putString("visual", rule.visual().getSerializedName());
        data.putUUID(TAG_REGEN_TICKET, UUID.randomUUID());
        data.putLong(TAG_EXECUTE_AT, level.getGameTime() + rule.delayTicks());
        data.putByte("rr_src_snap", priorSourceMarker);
        ResourceLocation restoreId = brokenState.getBlock().builtInRegistryHolder().key().location();
        data.putString(TAG_RESTORE_RL, restoreId.toString());
        TT_core.saveBlockData((ServerLevel)level, (BlockPos)pos, (CompoundTag)data);
        TTDataBank.schedulePersistentTask((ServerLevel)level, (String)EXECUTOR_ID, (long)rule.delayTicks(), (CompoundTag)data.copy());
        if (rule.visual() == RegenVisual.STRIPPED_LOG_PRESET || rule.visual() == RegenVisual.STRIPPED_LOG) {
            ResourceLocation strippedId = RegenStrippedLogResolver.resolveStrippedId(brokenState);
            RegenBlocks.preparePendingStrippedId(pos, strippedId);
            final LevelChunk strippedChunk = level.getChunkAt(pos);
            RegenResourcesNetwork.CHANNEL.send(PacketDistributor.TRACKING_CHUNK.with(() -> strippedChunk), new ClientboundStrippedPendingPacket(pos, strippedId));
        }
        RegenCustomVisualSpec customSpec = rule.customVisualSpec();
        if ((rule.visual() == RegenVisual.CUSTOM_PRESET || rule.visual() == RegenVisual.CUSTOM) && customSpec != null) {
            RegenBlocks.preparePendingCustomSpec(pos, customSpec);
            final LevelChunk customChunk = level.getChunkAt(pos);
            RegenResourcesNetwork.CHANNEL.send(PacketDistributor.TRACKING_CHUNK.with(() -> customChunk), new ClientboundCustomVisualPendingPacket(pos, customSpec));
        }
        BlockState waiting = Re_Blocks.REGEN_BLOCK.get().defaultBlockState().setValue(RegenBlocks.VISUAL, rule.visual()).setValue(RegenBlocks.AXIS, RegenRegenForgeEvents.axisFromBroken(brokenState));
        int update = 3;
        try (RegenSetBlockTtGuard ignored = RegenSetBlockTtGuard.acquire();){
            level.setBlock(pos, waiting, update);
        }
        if (rule.visual() == RegenVisual.STRIPPED_LOG_PRESET || rule.visual() == RegenVisual.STRIPPED_LOG) {
            RegenBlockEntity rbe2;
            ResourceLocation strippedId = RegenStrippedLogResolver.resolveStrippedId(brokenState);
            BlockEntity blockEntity2 = level.getBlockEntity(pos);
            if (blockEntity2 instanceof RegenBlockEntity && (rbe2 = (RegenBlockEntity)blockEntity2).getStrippedBlockId() == null) {
                rbe2.setStrippedBlockId(strippedId);
            }
        }
        if ((rule.visual() == RegenVisual.CUSTOM_PRESET || rule.visual() == RegenVisual.CUSTOM) && customSpec != null && (blockEntity = level.getBlockEntity(pos)) instanceof RegenBlockEntity && (rbe = (RegenBlockEntity)blockEntity).getCustomVisualSpec() == null) {
            rbe.setCustomVisualSpec(customSpec);
        }
    }

    private static Direction.Axis axisFromBroken(BlockState brokenState) {
        if (brokenState.hasProperty((Property)RotatedPillarBlock.AXIS)) {
            return (Direction.Axis)brokenState.getValue((Property)RotatedPillarBlock.AXIS);
        }
        return Direction.Axis.Y;
    }

    private static boolean holdsBreakStuffRemoveMode(Player player) {
        for (InteractionHand hand : InteractionHand.values()) {
            ItemStack st = player.getItemInHand(hand);
            if (!(st.getItem() instanceof BreakStuffItem) || !BreakStuffItem.isRemovalMode(st)) continue;
            return true;
        }
        return false;
    }
}

