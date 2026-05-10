package com.github.TeThoLaPot.regen_resources.platform.neoforge.event;

import com.github.TeThoLaPot.regen_resources.common.item.BreakStuffItem;
import com.github.TeThoLaPot.regen_resources.common.regen.RegenRule;
import com.github.TeThoLaPot.regen_resources.common.regen.RegenRuleRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Set;

/**
 * OreHarvester は起点ブロックの {@link BlockEvent.BreakEvent} をトリガにしつつ、
 * 周辺鉱石は独自処理で破壊する（周辺分の BreakEvent が飛ばないことがある）。
 * そのため、起点時にクラスタを先読みし、クラスタ分を足元ドロップで回収してから再生シェルを置く。
 */
public final class OreHarvesterCompatForgeEvents {

    private static final int MAX_CLUSTER = 512;

    private OreHarvesterCompatForgeEvents() {}

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onBreakStartCaptureCluster(BlockEvent.BreakEvent event) {
        if (!ModList.get().isLoaded("oreharvester")) {
            return;
        }
        if (!(event.getLevel() instanceof ServerLevel level)) {
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
            // 破壊 ON（破壊のオーブ）で掘った場合は「そのまま破壊」させ、再生シェルを置かない。
            return;
        }

        BlockPos origin = event.getPos().immutable();
        BlockState state = event.getState();

        ResourceLocation dim = level.dimension().location();
        RegenRule rule = RegenRuleRegistry.firstMatch(dim, state);
        if (rule == null) {
            return;
        }
        if (!RegenOreMineEligibility.allows(level, origin)) {
            return;
        }

        Set<BlockPos> cluster = collectCluster(level, origin, state);
        if (cluster.isEmpty()) {
            return;
        }

        // OreHarvester 側の dropBlock と競合するとドロップが足元に来ないので、
        // ここで BreakEvent をキャンセルしてクラスタ分を当 MOD の foot-drop 収穫へ切り替える。
        event.setCanceled(true);

        // (OreHarvester と同じく) 周辺は BreakEvent が飛ばない場合があるので、ここで一括処理する。
        level.getServer().execute(() -> {
            for (BlockPos p : cluster) {
                BlockState current = level.getBlockState(p);
                if (current.getBlock() != state.getBlock()) {
                    continue;
                }
                if (!RegenOreHarvest.harvestAndRemove(sp, level, p, current)) {
                    continue;
                }
                RegenRegenForgeEvents.commitOreBreakRegen(level, p, current, rule);
            }
        });
    }

    private static Set<BlockPos> collectCluster(ServerLevel level, BlockPos origin, BlockState matchState) {
        Set<BlockPos> visited = new HashSet<>();
        ArrayDeque<BlockPos> q = new ArrayDeque<>();
        q.add(origin);
        visited.add(origin);

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
                // blockstate が同一でなくても同種鉱石ならまとめたいが、まずは OreHarvester と同じ「同ブロック」単位で合わせる
                visited.add(nxt.immutable());
                q.add(nxt);
                if (visited.size() >= MAX_CLUSTER) {
                    break;
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

