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
        CompoundTag tag = stack.m_41783_();
        if (tag == null || !tag.m_128425_("BlockStateTag", 10)) {
            return RegenVisual.STONE_PRESET.itemPredicateValue();
        }
        CompoundTag bst = tag.m_128469_("BlockStateTag");
        String raw = bst.m_128425_(visualKey = RegenBlocks.VISUAL.m_61708_(), 8) ? bst.m_128461_(visualKey) : "";
        RegenVisual v = RegenVisual.fromSerializedName(raw);
        return v.itemPredicateValue();
    }
}

