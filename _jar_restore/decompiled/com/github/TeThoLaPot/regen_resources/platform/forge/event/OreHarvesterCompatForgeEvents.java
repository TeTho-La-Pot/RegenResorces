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
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.LevelAccessor
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraftforge.event.entity.player.PlayerEvent$BreakSpeed
 *  net.minecraftforge.event.level.BlockEvent$BreakEvent
 *  net.minecraftforge.eventbus.api.EventPriority
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.ModList
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber$Bus
 */
package com.github.TeThoLaPot.regen_resources.platform.forge.event;

import com.github.TeThoLaPot.regen_resources.common.item.BreakStuffItem;
import com.github.TeThoLaPot.regen_resources.common.regen.RegenMineMarker;
import com.github.TeThoLaPot.regen_resources.common.regen.RegenRule;
import com.github.TeThoLaPot.regen_resources.common.regen.RegenRuleRegistry;
import com.github.TeThoLaPot.regen_resources.platform.forge.event.OreHarvesterChainProbe;
import com.github.TeThoLaPot.regen_resources.platform.forge.event.RegenOreHarvest;
import com.github.TeThoLaPot.regen_resources.platform.forge.event.RegenOreMineEligibility;
import com.github.TeThoLaPot.regen_resources.platform.forge.event.RegenRegenForgeEvents;
import com.github.TeThoLaPot.regen_resources.platform.forge.event.RegenSneakMineTracker;
import com.github.TeThoLaPot.tt_core.TT_core;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid="regen_resources", bus=Mod.EventBusSubscriber.Bus.FORGE)
public final class OreHarvesterCompatForgeEvents {
    private static final int MAX_CLUSTER = 512;

    private OreHarvesterCompatForgeEvents() {
    }

    @SubscribeEvent(priority=EventPriority.HIGHEST)
    public static void onBreakStartCaptureCluster(BlockEvent.BreakEvent event) {
        if (!ModList.get().isLoaded("oreharvester")) {
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
        if (OreHarvesterCompatForgeEvents.holdsBreakStuffRemoveMode((Player)sp)) {
            return;
        }
        BlockPos origin = event.getPos().m_7949_();
        boolean sneaking = sp.m_6047_();
        boolean wasSneakMining = RegenSneakMineTracker.wasSneakMining(sp, origin);
        boolean ohWillChain = OreHarvesterChainProbe.willChain(level, sp);
        if (!(sneaking || wasSneakMining || ohWillChain)) {
            return;
        }
        BlockState state = event.getState();
        ResourceLocation dim = level.m_46472_().m_135782_();
        RegenRule rule = RegenRuleRegistry.firstMatch(dim, state);
        if (rule == null) {
            return;
        }
        if (!RegenOreMineEligibility.allows(level, origin, rule)) {
            return;
        }
        Set<BlockPos> cluster = OreHarvesterCompatForgeEvents.resolveCluster(level, origin, state);
        if (cluster.isEmpty()) {
            return;
        }
        event.setCanceled(true);
        HashMap<BlockPos, BlockState> stateSnapshots = new HashMap<BlockPos, BlockState>(cluster.size());
        HashMap<BlockPos, Byte> srcSnapshots = new HashMap<BlockPos, Byte>(cluster.size());
        for (BlockPos p : cluster) {
            stateSnapshots.put(p, level.m_8055_(p));
            srcSnapshots.put(p, RegenMineMarker.readSourceByte(TT_core.getBlockData((ServerLevel)level, (BlockPos)p)));
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

    @SubscribeEvent
    public static void onBreakSpeedTrackSneakMining(PlayerEvent.BreakSpeed event) {
        if (!ModList.get().isLoaded("oreharvester")) {
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
        if (!sp.m_6047_()) {
            return;
        }
        BlockPos pos = event.getPosition().orElse(null);
        if (pos == null) {
            return;
        }
        ServerLevel level = sp.m_284548_();
        ResourceLocation dim = level.m_46472_().m_135782_();
        if (RegenRuleRegistry.firstMatch(dim, event.getState()) == null) {
            return;
        }
        RegenSneakMineTracker.note(sp, pos);
    }

    private static Set<BlockPos> resolveCluster(ServerLevel level, BlockPos origin, BlockState matchState) {
        List<BlockPos> oh = OreHarvesterChainProbe.findOreCluster((Level)level, origin, matchState.m_60734_());
        if (oh != null) {
            HashSet<BlockPos> set = new HashSet<BlockPos>(oh.size() + 1);
            set.add(origin);
            set.addAll(oh);
            return set;
        }
        return OreHarvesterCompatForgeEvents.collectClusterFallback(level, origin, matchState);
    }

    private static Set<BlockPos> collectClusterFallback(ServerLevel level, BlockPos origin, BlockState matchState) {
        HashSet<BlockPos> visited = new HashSet<BlockPos>();
        ArrayDeque<BlockPos> q = new ArrayDeque<BlockPos>();
        q.add(origin);
        visited.add(origin);
        block0: while (!q.isEmpty() && visited.size() < 512) {
            BlockPos cur = (BlockPos)q.removeFirst();
            for (Direction d : Direction.values()) {
                BlockState st;
                BlockPos nxt = cur.m_121945_(d);
                if (visited.contains(nxt) || (st = level.m_8055_(nxt)).m_60734_() != matchState.m_60734_()) continue;
                visited.add(nxt.m_7949_());
                q.add(nxt);
                if (visited.size() >= 512) continue block0;
            }
        }
        return visited;
    }

    private static boolean holdsBreakStuffRemoveMode(Player player) {
        for (InteractionHand hand : InteractionHand.values()) {
            if (!(player.m_21120_(hand).m_41720_() instanceof BreakStuffItem) || !BreakStuffItem.isRemovalMode(player.m_21120_(hand))) continue;
            return true;
        }
        return false;
    }
}

