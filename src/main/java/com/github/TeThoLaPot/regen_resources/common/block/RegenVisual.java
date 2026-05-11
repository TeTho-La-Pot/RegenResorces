/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.util.StringRepresentable
 */
package com.github.TeThoLaPot.regen_resources.common.block;

import net.minecraft.util.StringRepresentable;

public enum RegenVisual implements StringRepresentable
{
    MIMIC("mimic"),
    STONE("stone"),
    DEEPSLATE("deepslate"),
    NETHER("nether"),
    END("end"),
    DEBRIS("debris"),
    STONE_PRESET("stone_preset"),
    DEEPSLATE_PRESET("deepslate_preset"),
    NETHER_PRESET("nether_preset"),
    END_PRESET("end_preset"),
    DEBRIS_PRESET("debris_preset"),
    STRIPPED_LOG("stripped_log"),
    STRIPPED_LOG_PRESET("stripped_log_preset"),
    CUSTOM("custom"),
    CUSTOM_PRESET("custom_preset");

    private final String id;

    private RegenVisual(String id) {
        this.id = id;
    }

    public String getSerializedName() {
        return this.id;
    }

    public float itemPredicateValue() {
        return this.ordinal();
    }

    public static RegenVisual fromSerializedName(String name) {
        if (name == null || name.isEmpty()) {
            return STONE_PRESET;
        }
        for (RegenVisual v : RegenVisual.values()) {
            if (!v.id.equals(name)) continue;
            return v;
        }
        return STONE_PRESET;
    }

    public static RegenVisual tryParseToken(String token) {
        if (token == null || token.isBlank()) {
            return null;
        }
        String t = token.trim();
        for (RegenVisual v : RegenVisual.values()) {
            if (!v.id.equals(t) && !v.name().equalsIgnoreCase(t)) continue;
            return v;
        }
        return null;
    }
}

