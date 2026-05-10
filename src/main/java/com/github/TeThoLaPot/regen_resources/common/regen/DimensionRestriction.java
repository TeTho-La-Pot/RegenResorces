/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.resources.ResourceLocation
 *  org.jetbrains.annotations.Nullable
 */
package com.github.TeThoLaPot.regen_resources.common.regen;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public final class DimensionRestriction {
    private final Set<ResourceLocation> ids;
    private final boolean exclusion;

    private DimensionRestriction(Set<ResourceLocation> ids, boolean exclusion) {
        this.ids = ids;
        this.exclusion = exclusion;
    }

    @Nullable
    public static DimensionRestriction fromJson(@Nullable List<String> rawList, @Nullable Boolean exclusionFlag) {
        boolean ex;
        if (rawList == null) {
            return null;
        }
        LinkedHashSet<ResourceLocation> dims = new LinkedHashSet<ResourceLocation>();
        for (String raw : rawList) {
            ResourceLocation rl;
            String t;
            if (raw == null || (t = raw.trim()).isEmpty() || (rl = ResourceLocation.tryParse((String)t)) == null) continue;
            dims.add(rl);
        }
        boolean bl = ex = exclusionFlag != null && exclusionFlag != false;
        if (dims.isEmpty()) {
            return null;
        }
        return new DimensionRestriction(Collections.unmodifiableSet(dims), ex);
    }

    public boolean matches(ResourceLocation dimensionLocation) {
        boolean in = this.ids.contains(dimensionLocation);
        return this.exclusion ? !in : in;
    }
}

