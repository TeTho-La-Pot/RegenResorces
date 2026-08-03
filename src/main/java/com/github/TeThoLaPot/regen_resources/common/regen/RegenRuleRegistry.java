package com.github.TeThoLaPot.regen_resources.common.regen;

import com.github.TeThoLaPot.regen_resources.common.block.RegenVisual;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

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
            if (rule == null
                    || (rule.dimensionRestriction() != null && !rule.dimensionRestriction().matches(dimensionId))
                    || !matches(broken, rule)) {
                continue;
            }
            return rule;
        }
        return null;
    }

    public static List<RegenRule> allCustomPresetMatches(ResourceLocation dimensionId, BlockState broken) {
        if (dimensionId == null || broken == null) {
            return List.of();
        }
        List<RegenRule> out = new ArrayList<>();
        for (RegenRule rule : RULES) {
            if (rule == null
                    || !isCustomVisual(rule.visual())
                    || rule.customVisualSpec() == null
                    || (rule.dimensionRestriction() != null && !rule.dimensionRestriction().matches(dimensionId))
                    || !matches(broken, rule)) {
                continue;
            }
            out.add(rule);
        }
        return out;
    }

    /**
     * 建築用カスタムダミー向け。再生マッチの dimensions は見ず、
     * {@code custom}/{@code custom_preset} のターゲットのみで判定する。
     */
    public static List<RegenRule> allCustomPresetMatchesIgnoringDimension(BlockState broken) {
        if (broken == null) {
            return List.of();
        }
        List<RegenRule> out = new ArrayList<>();
        for (RegenRule rule : RULES) {
            if (rule == null
                    || !isCustomVisual(rule.visual())
                    || rule.customVisualSpec() == null
                    || !matches(broken, rule)) {
                continue;
            }
            out.add(rule);
        }
        return out;
    }

    private static boolean isCustomVisual(@Nullable RegenVisual visual) {
        return visual == RegenVisual.CUSTOM || visual == RegenVisual.CUSTOM_PRESET;
    }

    public static boolean matchesPresetTargetsIgnoringDimension(BlockState broken) {
        if (broken == null || RULES.isEmpty()) {
            return false;
        }
        for (RegenRule rule : RULES) {
            if (rule == null || !matches(broken, rule)) {
                continue;
            }
            return true;
        }
        return false;
    }

    /** ルール内で最初にマッチしたターゲット（復元上書き用）。 */
    public static @Nullable RegenTargetSpec findMatchingTarget(BlockState state, RegenRule rule) {
        if (state == null || rule == null || rule.targets() == null) {
            return null;
        }
        for (RegenTargetSpec t : rule.targets()) {
            if (t != null && t.matches(state)) {
                return t;
            }
        }
        return null;
    }

    private static boolean matches(BlockState state, RegenRule rule) {
        if (rule.targets() == null || rule.targets().isEmpty()) {
            return false;
        }
        for (RegenTargetSpec t : rule.targets()) {
            if (t != null && t.matches(state)) {
                return true;
            }
        }
        return false;
    }
}
