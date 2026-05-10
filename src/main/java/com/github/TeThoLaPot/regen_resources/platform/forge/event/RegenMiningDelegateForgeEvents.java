/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.level.BlockGetter
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraftforge.event.entity.player.PlayerEvent$BreakSpeed
 *  net.minecraftforge.event.entity.player.PlayerEvent$HarvestCheck
 *  net.minecraftforge.eventbus.api.EventPriority
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber$Bus
 *  org.jetbrains.annotations.Nullable
 */
package com.github.TeThoLaPot.regen_resources.platform.forge.event;

import com.github.TeThoLaPot.regen_resources.common.block.CustomPresetDummyBlock;
import com.github.TeThoLaPot.regen_resources.common.block.PresetAppearanceDummyBlock;
import com.github.TeThoLaPot.regen_resources.common.block.RegenBlocks;
import com.github.TeThoLaPot.regen_resources.common.block.RegenCorruptionFallback;
import com.github.TeThoLaPot.regen_resources.common.block.RegenVisual;
import com.github.TeThoLaPot.regen_resources.common.block.StrippedLogPresetDummyBlock;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.Nullable;

@Mod.EventBusSubscriber(modid="regen_resources", bus=Mod.EventBusSubscriber.Bus.FORGE)
public final class RegenMiningDelegateForgeEvents {
    private static final float MIN_INTRINSIC = 1.0E-4f;

    private RegenMiningDelegateForgeEvents() {
    }

    @Nullable
    private static RegenVisual resolveMiningVisual(BlockState shell) {
        if (shell.getBlock() instanceof RegenBlocks) {
            return shell.getValue(RegenBlocks.VISUAL);
        }
        if (shell.getBlock() instanceof StrippedLogPresetDummyBlock) {
            return RegenVisual.STRIPPED_LOG_PRESET;
        }
        if (shell.getBlock() instanceof CustomPresetDummyBlock) {
            return RegenVisual.CUSTOM_PRESET;
        }
        Block block = shell.getBlock();
        if (block instanceof PresetAppearanceDummyBlock) {
            PresetAppearanceDummyBlock d = (PresetAppearanceDummyBlock)block;
            return d.appearance();
        }
        return null;
    }

    @SubscribeEvent(priority=EventPriority.HIGH)
    public static void onHarvestCheck(PlayerEvent.HarvestCheck event) {
        BlockState shell = event.getTargetBlock();
        RegenVisual vis = RegenMiningDelegateForgeEvents.resolveMiningVisual(shell);
        if (vis == null) {
            return;
        }
        Player player = event.getEntity();
        BlockState sample = RegenCorruptionFallback.miningSampleFor(vis);
        event.setCanHarvest(player.hasCorrectToolForDrops(sample));
    }

    @SubscribeEvent(priority=EventPriority.HIGH)
    public static void onBreakSpeed(PlayerEvent.BreakSpeed event) {
        if (event.isCanceled()) {
            return;
        }
        BlockState shell = event.getState();
        RegenVisual vis = RegenMiningDelegateForgeEvents.resolveMiningVisual(shell);
        if (vis == null) {
            return;
        }
        Optional opt = event.getPosition();
        if (opt.isEmpty()) {
            return;
        }
        BlockPos pos = (BlockPos)opt.get();
        Player player = event.getEntity();
        Level level = player.level();
        float shellIntr = shell.getDestroySpeed((BlockGetter)level, pos);
        BlockState sample = RegenCorruptionFallback.miningSampleFor(vis, (BlockGetter)level, pos);
        float sampIntr = sample.getDestroySpeed((BlockGetter)level, pos);
        if (shellIntr < 1.0E-4f || sampIntr <= 0.0f) {
            event.setCanceled(true);
            return;
        }
        event.setNewSpeed(event.getOriginalSpeed() * (shellIntr / sampIntr));
    }
}

