/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.github.TeThoLaPot.tt_core.TT_core
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.level.LevelAccessor
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.phys.BlockHitResult
 *  net.minecraft.world.phys.HitResult
 *  net.minecraft.world.phys.HitResult$Type
 *  net.minecraftforge.event.level.BlockEvent$BreakEvent
 *  net.minecraftforge.eventbus.api.EventPriority
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber$Bus
 */
package com.github.TeThoLaPot.regen_resources.platform.forge.event;

import com.github.TeThoLaPot.regen_resources.common.item.BreakStuffItem;
import com.github.TeThoLaPot.regen_resources.common.regen.RegenMineMarker;
import com.github.TeThoLaPot.regen_resources.common.regen.RegenRule;
import com.github.TeThoLaPot.regen_resources.common.regen.RegenRuleRegistry;
import com.github.TeThoLaPot.regen_resources.platform.forge.event.FtbUltimineChainProbe;
import com.github.TeThoLaPot.regen_resources.platform.forge.event.RegenOreHarvest;
import com.github.TeThoLaPot.regen_resources.platform.forge.event.RegenOreMineEligibility;
import com.github.TeThoLaPot.regen_resources.platform.forge.event.RegenRegenForgeEvents;
import com.github.TeThoLaPot.tt_core.TT_core;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid="regen_resources", bus=Mod.EventBusSubscriber.Bus.FORGE)
public final class FtbUltimineCompatForgeEvents {
    private FtbUltimineCompatForgeEvents() {
    }

    @SubscribeEvent(priority=EventPriority.HIGHEST)
    public static void onBreakStartCaptureCluster(BlockEvent.BreakEvent event) {
        List<BlockPos> cluster;
        if (!FtbUltimineChainProbe.isAvailable()) {
            return;
        }
        LevelAccessor levelAccessor = event.getLevel();
        if (!(levelAccessor instanceof ServerLevel)) {
            return;
        }
        ServerLevel level = (ServerLevel)levelAccessor;
        Player player = event.getPlayer();
        if (!(player instanceof ServerPlayer)) {
            return;
        }
        ServerPlayer sp = (ServerPlayer)player;
        if (sp.m_7500_()) {
            return;
        }
        if (FtbUltimineCompatForgeEvents.holdsBreakStuffRemoveMode((Player)sp)) {
            return;
        }
        if (FtbUltimineChainProbe.isChaining()) {
            event.setCanceled(true);
            return;
        }
        if (!FtbUltimineChainProbe.isPressed(sp)) {
            return;
        }
        BlockPos origin = event.getPos().m_7949_();
        BlockState state = event.getState();
        ResourceLocation dim = level.m_46472_().m_135782_();
        RegenRule rule = RegenRuleRegistry.firstMatch(dim, state);
        if (rule == null) {
            return;
        }
        if (!RegenOreMineEligibility.allows(level, origin, rule)) {
            return;
        }
        Direction face = Direction.UP;
        HitResult hit = FtbUltimineChainProbe.rayTrace(sp);
        if (hit instanceof BlockHitResult) {
            BlockHitResult bhr = (BlockHitResult)hit;
            if (hit.m_6662_() == HitResult.Type.BLOCK) {
                face = bhr.m_82434_();
            }
        }
        if ((cluster = FtbUltimineChainProbe.findCluster(sp, origin, face)) == null || cluster.isEmpty()) {
            return;
        }
        event.setCanceled(true);
        HashMap<BlockPos, BlockState> stateSnapshots = new HashMap<BlockPos, BlockState>(cluster.size() + 1);
        HashMap<BlockPos, Byte> srcSnapshots = new HashMap<BlockPos, Byte>(cluster.size() + 1);
        FtbUltimineCompatForgeEvents.ensureSnapshot(level, origin, stateSnapshots, srcSnapshots);
        for (BlockPos p : cluster) {
            FtbUltimineCompatForgeEvents.ensureSnapshot(level, p, stateSnapshots, srcSnapshots);
        }
        RegenRule ruleSnapshot = rule;
        level.m_7654_().execute(() -> {
            for (Map.Entry entry : stateSnapshots.entrySet()) {
                BlockPos p = (BlockPos)entry.getKey();
                BlockState snapState = (BlockState)entry.getValue();
                BlockState current = level.m_8055_(p);
                if (current.m_60734_() != snapState.m_60734_() || !RegenOreHarvest.harvestAndRemove(sp, level, p, current)) continue;
                Byte snap = (Byte)srcSnapshots.get(p);
                byte priorSrc = snap != null ? snap : (byte)0;
                RegenRegenForgeEvents.commitOreBreakRegen(level, p, current, ruleSnapshot, priorSrc);
            }
        });
    }

    private static void ensureSnapshot(ServerLevel level, BlockPos pos, Map<BlockPos, BlockState> states, Map<BlockPos, Byte> srcs) {
        if (states.containsKey(pos)) {
            return;
        }
        states.put(pos, level.m_8055_(pos));
        srcs.put(pos, RegenMineMarker.readSourceByte(TT_core.getBlockData((ServerLevel)level, (BlockPos)pos)));
    }

    private static boolean holdsBreakStuffRemoveMode(Player player) {
        for (InteractionHand hand : InteractionHand.values()) {
            if (!(player.m_21120_(hand).m_41720_() instanceof BreakStuffItem) || !BreakStuffItem.isRemovalMode(player.m_21120_(hand))) continue;
            return true;
        }
        return false;
    }
}

