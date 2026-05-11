/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.tags.TagKey
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.state.BlockState
 */
package com.github.TeThoLaPot.regen_resources.common.regen;

import com.github.TeThoLaPot.regen_resources.common.block.RegenVisual;
import com.github.TeThoLaPot.regen_resources.common.regen.RegenRule;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public final class RegenRuleRegistry {
    private static volatile List<RegenRule> RULES = List.of();

    private RegenRuleRegistry() {
    }

    public static void setRules(List<RegenRule> rules) {
        RULES = rules == null ? List.of() : List.copyOf(rules);
    }

    public static List<RegenRule> rules() {
        return RULES;
    }

    public static RegenRule firstMatch(ResourceLocation dimensionId, BlockState broken) {
        if (dimensionId == null || broken == null) {
            return null;
        }
        for (RegenRule rule : RULES) {
            if (rule == null || rule.dimensionRestriction() != null && !rule.dimensionRestriction().matches(dimensionId) || !RegenRuleRegistry.matches(broken, rule)) continue;
            return rule;
        }
        return null;
    }

    public static List<RegenRule> allCustomPresetMatches(ResourceLocation dimensionId, BlockState broken) {
        if (dimensionId == null || broken == null) {
            return List.of();
        }
        ArrayList<RegenRule> out = new ArrayList<RegenRule>();
        for (RegenRule rule : RULES) {
            if (rule == null || rule.visual() != RegenVisual.CUSTOM_PRESET || rule.customVisualSpec() == null || rule.dimensionRestriction() != null && !rule.dimensionRestriction().matches(dimensionId) || !RegenRuleRegistry.matches(broken, rule)) continue;
            out.add(rule);
        }
        return out;
    }

    public static boolean matchesPresetTargetsIgnoringDimension(BlockState broken) {
        if (broken == null || RULES.isEmpty()) {
            return false;
        }
        for (RegenRule rule : RULES) {
            if (rule == null || !RegenRuleRegistry.matches(broken, rule)) continue;
            return true;
        }
        return false;
    }

    private static boolean matches(BlockState state, RegenRule rule) {
        ResourceLocation blockId = state.getBlock().builtInRegistryHolder().key().location();
        if (rule.blockIds().contains(blockId)) {
            return true;
        }
        for (TagKey<Block> tag : rule.blockTags()) {
            if (!state.is(tag)) continue;
            return true;
        }
        return false;
    }
}

