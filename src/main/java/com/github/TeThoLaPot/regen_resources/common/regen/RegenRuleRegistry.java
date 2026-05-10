package com.github.TeThoLaPot.regen_resources.common.regen;

import com.github.TeThoLaPot.regen_resources.common.block.RegenVisual;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

public final class RegenRuleRegistry {

    private static volatile List<RegenRule> RULES = List.of();

    private RegenRuleRegistry() {}

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
            if (rule == null) {
                continue;
            }
            if (rule.dimensionRestriction() != null && !rule.dimensionRestriction().matches(dimensionId)) {
                continue;
            }
            if (matches(broken, rule)) {
                return rule;
            }
        }
        return null;
    }

    /**
     * ディメンション無視でブロックだけ見る（クライアントが Jade のサーバー問い合わせを行うかのヒューリスティック用）。
     */
    public static boolean matchesPresetTargetsIgnoringDimension(BlockState broken) {
        if (broken == null || RULES.isEmpty()) {
            return false;
        }
        for (RegenRule rule : RULES) {
            if (rule != null && matches(broken, rule)) {
                return true;
            }
        }
        return false;
    }

    /**
     * {@code preset == custom_preset} のルールのうち、渡されたブロックにマッチするもの（ダミーブロックのサイクル用）。
     */
    public static List<RegenRule> matchingCustomPresetRules(Block heldBlock) {
        if (heldBlock == null || RULES.isEmpty()) {
            return List.of();
        }
        BlockState state = heldBlock.defaultBlockState();
        List<RegenRule> out = new ArrayList<>();
        for (RegenRule rule : RULES) {
            if (rule != null && rule.visual() == RegenVisual.CUSTOM_PRESET && matches(state, rule)) {
                out.add(rule);
            }
        }
        return out;
    }

    private static boolean matches(BlockState state, RegenRule rule) {
        var blockId = state.getBlock().builtInRegistryHolder().key().location();
        if (rule.blockIds().contains(blockId)) {
            return true;
        }
        for (var tag : rule.blockTags()) {
            if (state.is(tag)) {
                return true;
            }
        }
        return false;
    }
}
