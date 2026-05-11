package com.github.TeThoLaPot.regen_resources.platform.neoforge.event;

import com.github.TeThoLaPot.regen_resources.common.item.BreakStuffItem;
import com.github.TeThoLaPot.regen_resources.common.regen.RegenMineMarker;
import com.github.TeThoLaPot.regen_resources.common.regen.RegenRule;
import com.github.TeThoLaPot.regen_resources.common.regen.RegenRuleRegistry;
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
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.BlockEvent;

/**
 * OreHarvester は起点ブロックの {@link BlockEvent.BreakEvent} をトリガにしつつ、
 * 周辺鉱石は独自処理で破壊する（周辺分の BreakEvent が飛ばないことがある）。
 * しゃがみ連鎖時のみクラスタを先読みして foot-drop → 再生シェルへ切り替える。
 */
public final class OreHarvesterCompatForgeEvents {

    private static final int MAX_CLUSTER = 512;

    private OreHarvesterCompatForgeEvents() {}

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onBreakStartCaptureCluster(BlockEvent.BreakEvent event) {
        if (!ModList.get().isLoaded("oreharvester")) {
            return;
        }
        LevelAccessor levelAccessor = event.getLevel();
        if (!(levelAccessor instanceof ServerLevel level)) {
            return;
        }
        Player player = event.getPlayer();
        if (!(player instanceof ServerPlayer sp)) {
            return;
        }
        if (sp.isCreative()) {
            return;
        }
        if (holdsBreakStuffRemoveMode(sp)) {
            return;
        }
        BlockPos origin = event.getPos().immutable();
        boolean sneaking = sp.isCrouching();
        boolean wasSneakMining = RegenSneakMineTracker.wasSneakMining(sp, origin);
        boolean ohWillChain = OreHarvesterChainProbe.willChain(level, sp);
        if (!(sneaking || wasSneakMining || ohWillChain)) {
            return;
        }

        BlockState state = event.getState();
        ResourceLocation dim = level.dimension().location();
        RegenRule rule = RegenRuleRegistry.firstMatch(dim, state);
        if (rule == null) {
            return;
        }
        if (!RegenOreMineEligibility.allows(level, origin, rule)) {
            return;
        }

        Set<BlockPos> cluster = resolveCluster(level, origin, state);
        if (cluster.isEmpty()) {
            return;
        }

        event.setCanceled(true);

        HashMap<BlockPos, BlockState> stateSnapshots = new HashMap<>(cluster.size());
        HashMap<BlockPos, Byte> srcSnapshots = new HashMap<>(cluster.size());
        for (BlockPos p : cluster) {
            stateSnapshots.put(p, level.getBlockState(p));
            srcSnapshots.put(p, RegenMineMarker.readSourceByte(TT_core.getBlockData(level, p)));
        }
        RegenRule ruleSnapshot = rule;
        level.getServer()
                .execute(
                        () -> {
                            for (Map.Entry<BlockPos, BlockState> e : stateSnapshots.entrySet()) {
                                BlockPos p = e.getKey();
                                BlockState snapState = e.getValue();
                                BlockState current = level.getBlockState(p);
                                if (current.getBlock() != snapState.getBlock()) {
                                    continue;
                                }
                                if (!RegenOreHarvest.harvestAndRemove(sp, level, p, current)) {
                                    continue;
                                }
                                Byte snap = srcSnapshots.get(p);
                                byte priorSrc = snap != null ? snap : RegenMineMarker.SRC_IMPLICIT;
                                RegenRegenForgeEvents.commitOreBreakRegen(level, p, current, ruleSnapshot, priorSrc, sp);
                            }
                        });
    }

    @SubscribeEvent
    public static void onBreakSpeedTrackSneakMining(PlayerEvent.BreakSpeed event) {
        if (!ModList.get().isLoaded("oreharvester")) {
            return;
        }
        Player player = event.getEntity();
        if (!(player instanceof ServerPlayer sp)) {
            return;
        }
        if (sp.isCreative()) {
            return;
        }
        if (!sp.isCrouching()) {
            return;
        }
        BlockPos pos = event.getPosition().orElse(null);
        if (pos == null) {
            return;
        }
        ServerLevel level = sp.serverLevel();
        ResourceLocation dim = level.dimension().location();
        if (RegenRuleRegistry.firstMatch(dim, event.getState()) == null) {
            return;
        }
        RegenSneakMineTracker.note(sp, pos);
    }

    private static Set<BlockPos> resolveCluster(ServerLevel level, BlockPos origin, BlockState matchState) {
        List<BlockPos> oh = OreHarvesterChainProbe.findOreCluster(level, origin, matchState.getBlock());
        if (oh != null) {
            HashSet<BlockPos> set = new HashSet<>(oh.size() + 1);
            set.add(origin);
            set.addAll(oh);
            return set;
        }
        return collectClusterFallback(level, origin, matchState);
    }

    private static Set<BlockPos> collectClusterFallback(ServerLevel level, BlockPos origin, BlockState matchState) {
        HashSet<BlockPos> visited = new HashSet<>();
        ArrayDeque<BlockPos> q = new ArrayDeque<>();
        q.add(origin);
        visited.add(origin);
        outer:
        while (!q.isEmpty() && visited.size() < MAX_CLUSTER) {
            BlockPos cur = q.removeFirst();
            for (Direction d : Direction.values()) {
                BlockPos nxt = cur.relative(d);
                if (visited.contains(nxt)) {
                    continue;
                }
                BlockState st = level.getBlockState(nxt);
                if (st.getBlock() != matchState.getBlock()) {
                    continue;
                }
                visited.add(nxt.immutable());
                q.add(nxt);
                if (visited.size() >= MAX_CLUSTER) {
                    break outer;
                }
            }
        }
        return visited;
    }

    private static boolean holdsBreakStuffRemoveMode(Player player) {
        for (InteractionHand hand : InteractionHand.values()) {
            if (player.getItemInHand(hand).getItem() instanceof BreakStuffItem
                    && BreakStuffItem.isRemovalMode(player.getItemInHand(hand))) {
                return true;
            }
        }
        return false;
    }
}
