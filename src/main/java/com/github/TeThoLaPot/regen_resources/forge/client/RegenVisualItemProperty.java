package com.github.TeThoLaPot.regen_resources.forge.client;

import com.github.TeThoLaPot.regen_resources.common.block.RegenBlocks;
import com.github.TeThoLaPot.regen_resources.common.block.RegenVisual;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

/**
 * 再生待ちブロックアイテムの {@code BlockStateTag.visual} を数値へ写し、アイテムモデルの predicate と対応させる。
 * <p>Minecraft は各閾値に対して実際の値が閾値以上なら一致とみなし、複数ヒットするとリスト末尾の一致が採用される。
 * そのため {@code overrides} は閾値の昇順（0, 1, 2…）に並べる。
 */
public final class RegenVisualItemProperty {

    public static final String PROPERTY_PATH = "regen_visual";

    private RegenVisualItemProperty() {}

    public static float predicateValue(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains("BlockStateTag", 10)) {
            return RegenVisual.STONE_PRESET.itemPredicateValue();
        }
        CompoundTag bst = tag.getCompound("BlockStateTag");
        String visualKey = RegenBlocks.VISUAL.getName();
        String raw = bst.contains(visualKey, 8) ? bst.getString(visualKey) : "";
        RegenVisual v = RegenVisual.fromSerializedName(raw);
        return v.itemPredicateValue();
    }
}
