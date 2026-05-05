package com.github.TeThoLaPot.regen_resources.init.block;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

/**
 * {@code regen_block} の blockstate。{@link #MIMIC} のみ BER、それ以外は block モデルをそのまま表示。
 */
public enum RegenVisual implements StringRepresentable {
    MIMIC("mimic"),
    /** 系統ごとの共通テクスチャ（強制プリセット時） */
    STONE("stone"),
    DEEPSLATE("deepslate"),
    NETHER("nether"),
    END("end"),
    DEBRIS("debris"),
    /** JSON プリセットおよび待機モデル参照。 */
    STONE_PRESET("stone_preset"),
    DEEPSLATE_PRESET("deepslate_preset"),
    NETHER_PRESET("nether_preset"),
    END_PRESET("end_preset"),
    DEBRIS_PRESET("debris_preset");

    private final String id;

    RegenVisual(String id) {
        this.id = id;
    }

    public static RegenVisual fromConfig(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            return STONE_PRESET;
        }
        RegenVisual v = tryParseToken(raw);
        return v != null ? v : STONE_PRESET;
    }

    /** @param token JSON の {@code preset} や RegenVisual の id。 */
    public static @Nullable RegenVisual tryParseToken(@Nullable String token) {
        if (token == null || token.isEmpty()) {
            return null;
        }
        String s = token.trim().toLowerCase(Locale.ROOT);
        for (RegenVisual v : values()) {
            if (v.id.equals(s)) {
                return v;
            }
        }
        return null;
    }

    public static RegenVisual fromMappedToken(String token) {
        RegenVisual v = tryParseToken(token);
        return v != null ? v : STONE_PRESET;
    }

    /** {@link #MIMIC} 以外・プリセット待機モデル時の硬度・採掘速度・適正ツールの参照となるバニラブロック。 */
    public BlockState presetMiningMaterial() {
        return switch (this) {
            case MIMIC -> throw new UnsupportedOperationException("presetMiningMaterial called for mimic");
            case STONE, STONE_PRESET -> Blocks.STONE.defaultBlockState();
            case DEEPSLATE, DEEPSLATE_PRESET -> Blocks.DEEPSLATE.defaultBlockState();
            case NETHER, NETHER_PRESET -> Blocks.NETHERRACK.defaultBlockState();
            case DEBRIS, DEBRIS_PRESET -> Blocks.ANCIENT_DEBRIS.defaultBlockState();
            case END, END_PRESET -> Blocks.END_STONE.defaultBlockState();
        };
    }

    @Override
    public @NotNull String getSerializedName() {
        return id;
    }
}
