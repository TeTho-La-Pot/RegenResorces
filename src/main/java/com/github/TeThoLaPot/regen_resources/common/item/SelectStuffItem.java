package com.github.TeThoLaPot.regen_resources.common.item;

import com.github.TeThoLaPot.regen_resources.platform.neoforge.command.RegenSettingsCommands;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * 選択のオーブ（英: Selection Orb）。クリエイティブ時に右クリックで再生設定 UI を開く。
 */
public class SelectStuffItem extends Item {

    public SelectStuffItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand useHand) {
        ItemStack stack = player.getItemInHand(useHand);
        if (!player.isCreative()) {
            return InteractionResultHolder.pass(stack);
        }
        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            RegenSettingsCommands.openSettingsFor(serverPlayer);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        Level level = context.level();
        if (level != null && level.isClientSide()) {
            tooltip.add(Component.translatable("tooltip.regen_resources.select_stuff_creative")
                    .withStyle(ChatFormatting.DARK_PURPLE));
            Component useKey = Component.keybind("key.use").withStyle(ChatFormatting.DARK_PURPLE);
            tooltip.add(Component.translatable("tooltip.regen_resources.select_stuff_description", useKey)
                    .withStyle(ChatFormatting.GRAY));
        }
    }
}
