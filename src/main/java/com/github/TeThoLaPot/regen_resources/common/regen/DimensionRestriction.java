package com.github.TeThoLaPot.regen_resources.common.regen;

import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * alpha ({@code 8d2444e}) の {@code dimension} / {@code dimension_exclusion} と同じ意味。
 * {@code null} はディメンション制限なし（すべてのディメンションでマッチ）。
 */
public final class DimensionRestriction {

    private final Set<ResourceLocation> ids;
    /** {@code false}: リスト内のみ許可 / {@code true}: リスト内は除外 */
    private final boolean exclusion;

    private DimensionRestriction(Set<ResourceLocation> ids, boolean exclusion) {
        this.ids = ids;
        this.exclusion = exclusion;
    }

    /**
     * @param rawList {@code dimension} の配列（フィールド無しなら {@code null} = 制限なし）
     * @param exclusionFlag {@code dimension_exclusion}（フィールド無しなら {@code false} 扱い）
     */
    public static @Nullable DimensionRestriction fromJson(@Nullable List<String> rawList, @Nullable Boolean exclusionFlag) {
        if (rawList == null) {
            return null;
        }
        Set<ResourceLocation> dims = new LinkedHashSet<>();
        for (String raw : rawList) {
            if (raw == null) {
                continue;
            }
            String t = raw.trim();
            if (t.isEmpty()) {
                continue;
            }
            ResourceLocation rl = ResourceLocation.tryParse(t);
            if (rl != null) {
                dims.add(rl);
            }
        }
        boolean ex = exclusionFlag != null && exclusionFlag.booleanValue();
        if (dims.isEmpty()) {
            return null;
        }
        return new DimensionRestriction(Collections.unmodifiableSet(dims), ex);
    }

    public boolean matches(ResourceLocation dimensionLocation) {
        boolean in = ids.contains(dimensionLocation);
        return exclusion ? !in : in;
    }
}
