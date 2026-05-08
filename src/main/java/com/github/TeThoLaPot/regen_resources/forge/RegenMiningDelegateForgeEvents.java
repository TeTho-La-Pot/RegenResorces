package com.github.TeThoLaPot.regen_resources.forge;

import com.github.TeThoLaPot.regen_resources.RegenResources;
import com.github.TeThoLaPot.regen_resources.common.block.RegenBlocks;
import com.github.TeThoLaPot.regen_resources.common.block.RegenCorruptionFallback;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Optional;

/**
 * 再生シェルの採掘は {@link RegenCorruptionFallback#miningSampleFor} のブロックに合わせる（debris は古代の残骸）。
 * ブロック自身のタグでは足りないため BreakSpeed / HarvestCheck で矯正する。
 */
@Mod.EventBusSubscriber(modid = RegenResources.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class RegenMiningDelegateForgeEvents {

    private static final float MIN_INTRINSIC = 1.0E-4F;

    private RegenMiningDelegateForgeEvents() {}

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onHarvestCheck(PlayerEvent.HarvestCheck event) {
        BlockState shell = event.getTargetBlock();
        if (!(shell.getBlock() instanceof RegenBlocks)) {
            return;
        }
        Player player = event.getEntity();
        BlockState sample = RegenCorruptionFallback.miningSampleFor(shell.getValue(RegenBlocks.VISUAL));
        event.setCanHarvest(player.hasCorrectToolForDrops(sample));
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onBreakSpeed(PlayerEvent.BreakSpeed event) {
        if (event.isCanceled()) {
            return;
        }
        BlockState shell = event.getState();
        if (!(shell.getBlock() instanceof RegenBlocks)) {
            return;
        }
        Optional<BlockPos> opt = event.getPosition();
        if (opt.isEmpty()) {
            return;
        }
        BlockPos pos = opt.get();
        Player player = event.getEntity();
        Level level = player.level();
        float shellIntr = shell.getDestroySpeed(level, pos);
        BlockState sample = RegenCorruptionFallback.miningSampleFor(shell.getValue(RegenBlocks.VISUAL));
        float sampIntr = sample.getDestroySpeed(level, pos);
        if (shellIntr < MIN_INTRINSIC || sampIntr <= 0) {
            event.setCanceled(true);
            return;
        }
        // vanilla: progress ∝ digSpeed / getDestroySpeed — より硬い参照ブロックは divisor が大きいので digSpeed を下げる。
        event.setNewSpeed(event.getOriginalSpeed() * (shellIntr / sampIntr));
    }
}
