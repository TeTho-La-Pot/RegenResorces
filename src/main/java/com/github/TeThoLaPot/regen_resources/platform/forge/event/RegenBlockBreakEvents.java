/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.github.TeThoLaPot.tt_core.TT_core
 *  net.minecraft.core.BlockPos
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.LevelAccessor
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraftforge.event.level.BlockEvent$BreakEvent
 *  net.minecraftforge.eventbus.api.EventPriority
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber$Bus
 */
package com.github.TeThoLaPot.regen_resources.platform.forge.event;

import com.github.TeThoLaPot.regen_resources.common.item.BreakStuffItem;
import com.github.TeThoLaPot.regen_resources.platform.forge.block.Re_Blocks;
import com.github.TeThoLaPot.tt_core.TT_core;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid="regen_resources", bus=Mod.EventBusSubscriber.Bus.FORGE)
public final class RegenBlockBreakEvents {
    private RegenBlockBreakEvents() {
    }

    @SubscribeEvent(priority=EventPriority.HIGH)
    public static void onRegenBlockBreak(BlockEvent.BreakEvent event) {
        LevelAccessor levelAccessor = event.getLevel();
        if (!(levelAccessor instanceof ServerLevel)) {
            return;
        }
        ServerLevel level = (ServerLevel)levelAccessor;
        BlockState state = event.getState();
        if (!state.is((Block)Re_Blocks.REGEN_BLOCK.get())) {
            return;
        }
        Player player = event.getPlayer();
        BlockPos pos = event.getPos();
        if (player == null) {
            event.setCanceled(true);
            return;
        }
        if (player.getAbilities().instabuild) {
            TT_core.removeBlockData((ServerLevel)level, (BlockPos)pos);
            return;
        }
        if (RegenBlockBreakEvents.holdsActiveBreakStuff(player)) {
            TT_core.removeBlockData((ServerLevel)level, (BlockPos)pos);
            return;
        }
        event.setCanceled(true);
    }

    private static boolean holdsActiveBreakStuff(Player player) {
        return RegenBlockBreakEvents.isActiveBreakStuff(player.getMainHandItem()) || RegenBlockBreakEvents.isActiveBreakStuff(player.getOffhandItem());
    }

    private static boolean isActiveBreakStuff(ItemStack stack) {
        return stack.getItem() instanceof BreakStuffItem && BreakStuffItem.isRemovalMode(stack);
    }
}

