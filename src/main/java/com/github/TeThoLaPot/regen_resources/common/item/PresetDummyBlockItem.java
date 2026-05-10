package com.github.TeThoLaPot.regen_resources.common.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class PresetDummyBlockItem extends BlockItem {

    private final String tooltipTranslationKey;

    public PresetDummyBlockItem(Block block, Properties properties, String tooltipTranslationKey) {
        super(block, properties);
        this.tooltipTranslationKey = tooltipTranslationKey;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        tooltip.add(Component.translatable(tooltipTranslationKey));
    }
}
