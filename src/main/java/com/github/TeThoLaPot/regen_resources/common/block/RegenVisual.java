package com.github.TeThoLaPot.regen_resources.common.block;

import net.minecraft.util.StringRepresentable;

/**
 * `assets/.../blockstates/regen_block.json` の `visual=<name>` と一致させる。
 * 見た目切り替えのみ（プリセット/ディメンションルールは別管理）。
 */
public enum RegenVisual implements StringRepresentable {
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
    /** 原木系プリセット（blockstates / アイテム predicate は他プリセットと同列に追加）。 */
    LOG_PRESET("log_preset"),
    /** {@code custom_preset.json} 由来のカスタムプリセット。 */
    CUSTOM_PRESET("custom_preset");

    private final String id;

    RegenVisual(String id) {
        this.id = id;
    }

    @Override
    public String getSerializedName() {
        return id;
    }

    /** アイテムモデル predicate 用の値（models/item の overrides は昇順）。 */
    public float itemPredicateValue() {
        return ordinal();
    }

    public static RegenVisual fromSerializedName(String name) {
        if (name == null || name.isEmpty()) {
            return STONE_PRESET;
        }
        for (RegenVisual v : values()) {
            if (v.id.equals(name)) {
                return v;
            }
        }
        return STONE_PRESET;
    }

    /** alpha の {@code preset} フィールド用。不明なら {@code null}。 */
    public static RegenVisual tryParseToken(String token) {
        if (token == null || token.isBlank()) {
            return null;
        }
        String t = token.trim();
        for (RegenVisual v : values()) {
            if (v.id.equals(t) || v.name().equalsIgnoreCase(t)) {
                return v;
            }
        }
        return null;
    }
}
