package com.github.TeThoLaPot.regen_resources.forge;

import com.github.TeThoLaPot.regen_resources.RegenResources;
import com.github.TeThoLaPot.regen_resources.common.regen.RegenMineMarker;
import com.github.TeThoLaPot.regen_resources.common.regen.RegenRuleRegistry;
import com.github.TeThoLaPot.tt_core.TT_core;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 再生対象ブロックの設置時だけ TT に 1 バイトマーカーを載せる（チャンク全域のスキャンはしない）。
 * {@link EventPriority#LOWEST}: 他 MOD のキャンセル後に実行。
 */
@Mod.EventBusSubscriber(modid = RegenResources.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class RegenPlacementForgeEvents {

    private RegenPlacementForgeEvents() {}

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onEntityPlace(BlockEvent.EntityPlaceEvent event) {
        if (event instanceof BlockEvent.EntityMultiPlaceEvent multi) {
            onMultiPlace(multi);
            return;
        }
        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;
        }
        maybeMark(level, event.getEntity(), event.getPos(), event.getPlacedBlock());
    }

    private static void onMultiPlace(BlockEvent.EntityMultiPlaceEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;
        }
        Entity entity = event.getEntity();
        for (BlockSnapshot snap : event.getReplacedBlockSnapshots()) {
            maybeMark(level, entity, snap.getPos(), snap.getCurrentBlock());
        }
    }

    private static void maybeMark(ServerLevel level, Entity entity, BlockPos pos, BlockState placed) {
        ResourceLocation dim = level.dimension().location();
        if (RegenRuleRegistry.firstMatch(dim, placed) == null) {
            return;
        }

        byte marker;
        if (entity instanceof Player player) {
            marker = player.isCreative() ? RegenMineMarker.SRC_ELIGIBLE : RegenMineMarker.SRC_SURVIVAL;
        } else if (entity == null) {
            if (!RegenResourcesForgeConfig.COMMAND_LIKE_PLACEMENT_ELIGIBLE.get()) {
                return;
            }
            marker = RegenMineMarker.SRC_ELIGIBLE;
        } else {
            return;
        }

        CompoundTag patch = new CompoundTag();
        patch.putByte(RegenMineMarker.TT_SOURCE, marker);
        TT_core.saveBlockData(level, pos, patch);
    }
}
