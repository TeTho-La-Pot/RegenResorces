package com.github.TeThoLaPot.regen_resources.forge;

import com.github.TeThoLaPot.regen_resources.RegenConstants;
import com.github.TeThoLaPot.regen_resources.init.block.Re_Blocks;
import com.github.TeThoLaPot.regen_resources.init.block.RegenBlocks;
import com.github.TeThoLaPot.regen_resources.util.RegenBreakDigContext;
import com.github.TeThoLaPot.regen_resources.util.RegenMiningHelpers;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Optional;

/**
 * 「再生中の鉱石」は静的 hardness を持つが、採掘進捗は BlockState#getDestroySpeed(世界,pos) が使われるため Mixin でミミックに委譲する。適正ツールはイベントで評価する。
 */
@Mod.EventBusSubscriber(modid = RegenConstants.MOD_ID)
public final class RegenOreMiningForgeEvents {

    private RegenOreMiningForgeEvents() {}

    /**
     * Forge が採掘速度計算で渡すブロック座標を保持し、{@link RegenMiningHelpers#regenBlockTargetedByPick} が
     * 毎回 {@code player.pick} しないようにする。
     */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onBreakSpeedCapturePos(PlayerEvent.BreakSpeed event) {
        if (!event.getState().is(Re_Blocks.REGEN_BLOCK.get())) {
            RegenBreakDigContext.clear(event.getEntity());
            return;
        }
        @SuppressWarnings("unchecked")
        Optional<BlockPos> opt = event.getPosition();
        opt.ifPresent(p -> RegenBreakDigContext.set(event.getEntity(), p.immutable()));
    }

    @SubscribeEvent
    public static void onHarvestCheck(PlayerEvent.HarvestCheck event) {
        BlockState target = event.getTargetBlock();
        if (!target.is(Re_Blocks.REGEN_BLOCK.get())) {
            return;
        }
        Player player = event.getEntity();
        BlockPos pos = RegenMiningHelpers.regenBlockTargetedByPick(player);
        if (pos == null) {
            return;
        }
        BlockState mimic = RegenBlocks.mimicStateAt(player.level(), pos);
        event.setCanHarvest(ForgeHooks.isCorrectToolForDrops(mimic, player));
    }

}
