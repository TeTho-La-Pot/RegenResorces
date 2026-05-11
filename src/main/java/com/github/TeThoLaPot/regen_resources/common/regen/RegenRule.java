/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.tags.TagKey
 *  net.minecraft.world.level.block.Block
 *  org.jetbrains.annotations.Nullable
 */
package com.github.TeThoLaPot.regen_resources.common.regen;

import com.github.TeThoLaPot.regen_resources.common.block.RegenCustomVisualSpec;
import com.github.TeThoLaPot.regen_resources.common.block.RegenVisual;
import com.github.TeThoLaPot.regen_resources.common.regen.DimensionRestriction;
import java.util.List;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

public record RegenRule(long delayTicks, RegenVisual visual, @Nullable DimensionRestriction dimensionRestriction, List<ResourceLocation> blockIds, List<TagKey<Block>> blockTags, @Nullable Boolean naturalRegenOverride, @Nullable RegenCustomVisualSpec customVisualSpec) {
    public RegenRule(long delayTicks, RegenVisual visual, @Nullable DimensionRestriction dimensionRestriction, List<ResourceLocation> blockIds, List<TagKey<Block>> blockTags, @Nullable Boolean naturalRegenOverride) {
        this(delayTicks, visual, dimensionRestriction, blockIds, blockTags, naturalRegenOverride, null);
    }
}

