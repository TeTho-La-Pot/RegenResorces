/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.state.BlockState
 */
package com.github.TeThoLaPot.regen_resources.platform.forge.client;

import com.github.TeThoLaPot.regen_resources.common.item.BreakStuffItem;
import com.github.TeThoLaPot.regen_resources.platform.forge.block.Re_Blocks;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public final class RegenShellClientBreakRules {
    private RegenShellClientBreakRules() {
    }

    public static boolean shouldBlockSurvivalRegenBreak(Player player, BlockState state) {
        if (!state.m_60713_((Block)Re_Blocks.REGEN_BLOCK.get())) {
            return false;
        }
        if (player.m_150110_().f_35937_) {
            return false;
        }
        for (InteractionHand hand : InteractionHand.values()) {
            ItemStack stack = player.m_21120_(hand);
            if (!(stack.m_41720_() instanceof BreakStuffItem) || !BreakStuffItem.isRemovalMode(stack)) continue;
            return false;
        }
        return true;
    }
}

