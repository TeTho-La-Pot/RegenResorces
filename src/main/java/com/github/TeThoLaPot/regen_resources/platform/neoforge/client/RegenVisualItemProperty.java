package com.github.TeThoLaPot.regen_resources.platform.neoforge.client;

import com.github.TeThoLaPot.regen_resources.common.block.RegenBlocks;
import com.github.TeThoLaPot.regen_resources.common.block.RegenVisual;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

/**
 * 再生待ちブロックアイテムの {@code BlockStateTag.visual} を数値へ写し、アイテムモデルの predicate と対応させる。
 */
public final class RegenVisualItemProperty {

    public static final String PROPERTY_PATH = "regen_visual";

    private RegenVisualItemProperty() {}

    public static float predicateValue(ItemStack stack) {
        CustomData cd = stack.get(DataComponents.CUSTOM_DATA);
        CompoundTag tag = cd != null ? cd.copyTag() : new CompoundTag();
        if (!tag.contains("BlockStateTag", 10)) {
            return RegenVisual.STONE_PRESET.itemPredicateValue();
        }
        CompoundTag bst = tag.getCompound("BlockStateTag");
        String visualKey = RegenBlocks.VISUAL.getName();
        String raw = bst.contains(visualKey, 8) ? bst.getString(visualKey) : "";
        RegenVisual v = RegenVisual.fromSerializedName(raw);
        return v.itemPredicateValue();
    }
}
