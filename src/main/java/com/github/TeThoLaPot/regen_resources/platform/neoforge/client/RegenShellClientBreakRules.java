package com.github.TeThoLaPot.regen_resources.platform.neoforge.client;

import com.github.TeThoLaPot.regen_resources.common.item.BreakStuffItem;
import com.github.TeThoLaPot.regen_resources.platform.neoforge.event.RegenBlockBreakEvents;
import com.github.TeThoLaPot.regen_resources.platform.neoforge.block.Re_Blocks;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

/**
 * {@link RegenBlockBreakEvents} と同じ条件をクライアント側でも使う。
 */
public final class RegenShellClientBreakRules {

    private RegenShellClientBreakRules() {}

    /**
     * 再生シェルを生存で破壊してよいのは破壊 ON の破壊のオーブ（クリエは常に可）。
     * これが true のとき、クライアント予測の {@code destroyBlock} を防ぎ、進捗を「直前」で止める。
     */
    public static boolean shouldBlockSurvivalRegenBreak(Player player, BlockState state) {
        if (!state.is(Re_Blocks.REGEN_BLOCK.get())) {
            return false;
        }
        if (player.getAbilities().instabuild) {
            return false;
        }
        for (InteractionHand hand : InteractionHand.values()) {
            ItemStack stack = player.getItemInHand(hand);
            if (stack.getItem() instanceof BreakStuffItem && BreakStuffItem.isRemovalMode(stack)) {
                return false;
            }
        }
        return true;
    }
}
