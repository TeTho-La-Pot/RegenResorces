package com.github.TeThoLaPot.regen_resources.common.regen;

import com.github.TeThoLaPot.regen_resources.common.block.RegenVisual;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public record RegenRule(
        long delayTicks,
        RegenVisual visual,
        /** {@code null} のときすべてのディメンションでマッチ（alpha と同様） */
        @Nullable DimensionRestriction dimensionRestriction,
        List<ResourceLocation> blockIds,
        List<TagKey<Block>> blockTags
) {
}
