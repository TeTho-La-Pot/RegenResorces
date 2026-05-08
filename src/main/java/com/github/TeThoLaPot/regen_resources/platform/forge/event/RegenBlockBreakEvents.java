package com.github.TeThoLaPot.regen_resources.platform.forge.event;

import com.github.TeThoLaPot.regen_resources.RegenResources;
import com.github.TeThoLaPot.regen_resources.common.item.BreakStuffItem;
import com.github.TeThoLaPot.regen_resources.platform.forge.block.Re_Blocks;
import com.github.TeThoLaPot.tt_core.TT_core;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Regen_Ore の RegenBreakEvent に相当: 再生待ちブロックは、除去 ON の破壊装置を利き手のどちらかに持って
 * 破壊したときだけ撤去でき、それ以外は破壊できない（クリエイティブは常に可）。
 */
@Mod.EventBusSubscriber(modid = RegenResources.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class RegenBlockBreakEvents {

    private RegenBlockBreakEvents() {}

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onRegenBlockBreak(BlockEvent.BreakEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;
        }
        BlockState state = event.getState();
        if (!state.is(Re_Blocks.REGEN_BLOCK.get())) {
            return;
        }

        Player player = event.getPlayer();
        BlockPos pos = event.getPos();

        if (player == null) {
            event.setCanceled(true);
            return;
        }

        if (player.getAbilities().instabuild) {
            TT_core.removeBlockData(level, pos);
            return;
        }

        if (holdsActiveBreakStuff(player)) {
            TT_core.removeBlockData(level, pos);
            return;
        }

        event.setCanceled(true);
    }

    private static boolean holdsActiveBreakStuff(Player player) {
        return isActiveBreakStuff(player.getMainHandItem()) || isActiveBreakStuff(player.getOffhandItem());
    }

    private static boolean isActiveBreakStuff(ItemStack stack) {
        return stack.getItem() instanceof BreakStuffItem && BreakStuffItem.isRemovalMode(stack);
    }
}

