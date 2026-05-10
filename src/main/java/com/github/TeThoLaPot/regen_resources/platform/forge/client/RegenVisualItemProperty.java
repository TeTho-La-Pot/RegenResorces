/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.world.item.ItemStack
 */
package com.github.TeThoLaPot.regen_resources.platform.forge.client;

import com.github.TeThoLaPot.regen_resources.common.block.RegenBlocks;
import com.github.TeThoLaPot.regen_resources.common.block.RegenVisual;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

public final class RegenVisualItemProperty {
    public static final String PROPERTY_PATH = "regen_visual";

    private RegenVisualItemProperty() {
    }

    public static float predicateValue(ItemStack stack) {
        String visualKey;
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains("BlockStateTag", 10)) {
            return RegenVisual.STONE_PRESET.itemPredicateValue();
        }
        CompoundTag bst = tag.getCompound("BlockStateTag");
        String raw = bst.contains(visualKey = RegenBlocks.VISUAL.getName(), 8) ? bst.getString(visualKey) : "";
        RegenVisual v = RegenVisual.fromSerializedName(raw);
        return v.itemPredicateValue();
    }
}

