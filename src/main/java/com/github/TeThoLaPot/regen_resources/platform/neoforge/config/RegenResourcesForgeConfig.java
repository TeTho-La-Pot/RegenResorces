package com.github.TeThoLaPot.regen_resources.platform.neoforge.config;

import com.github.TeThoLaPot.regen_resources.RegenResources;
import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * COMMON。サーバー／単体ワールドで共有される既定値。
 */
public final class RegenResourcesForgeConfig {

    private static final String CFG = "config." + RegenResources.MOD_ID + ".";

    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.BooleanValue ALLOW_NATURAL_REGEN = BUILDER
            .comment(
                    "Survival mining: if true, unmarked blocks (typical world-gen ore) get the regenerating shell placed.",
                    "If false, only creative/command-marked (rr_src=eligible) placements get the shell; others break vanilla-only.")
            .translation(CFG + "allow_natural_generation_regen")
            .define("allowNaturalGenerationRegen", true);

    public static final ModConfigSpec.BooleanValue COMMAND_LIKE_PLACEMENT_ELIGIBLE = BUILDER
            .comment(
                    "When true, EntityPlaceEvent with no entity (e.g. /setblock) sets rr_src=eligible so survival mining places the shell.",
                    "When false, those placements stay unmarked and follow allowNaturalGenerationRegen only.")
            .translation(CFG + "command_like_placement_eligible")
            .define("commandLikePlacementEligible", true);

    public static final ModConfigSpec.BooleanValue CHANGE_ANCIENT_DEBRIS_DROPS = BUILDER
            .comment(
                    "When true, mined ancient debris (without Silk Touch) drops regen_resources:ancient_fragment instead of debris items,",
                    "with Fortune applying to fragment count (see loot modifier). When false, vanilla debris drops unchanged.")
            .translation(CFG + "change_ancient_debris_drops")
            .define("changeAncientDebrisDrops", true);

    /**
     * true のとき、組み込みの既定ファイル名（stone_preset.json など）が無ければ生成する（既存ファイルは上書きしない）。
     * false なら自動生成せず、手動で JSON を置くまでルールゼロになり得る。
     */
    public static final ModConfigSpec.BooleanValue BOOTSTRAP_VANILLA_PRESETS_WHEN_EMPTY = BUILDER
            .comment(
                    "If true, missing built-in preset JSON files under config/RegenResources/RegenPresets/ are created (existing files are never overwritten).",
                    "If false, no automatic preset files are written.")
            .translation(CFG + "bootstrap_vanilla_presets_when_empty")
            .define("bootstrapVanillaPresetsWhenEmpty", true);

    public static final ModConfigSpec SPEC = BUILDER.build();

    private RegenResourcesForgeConfig() {}
}
