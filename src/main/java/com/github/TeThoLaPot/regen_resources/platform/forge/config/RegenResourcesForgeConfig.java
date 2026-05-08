package com.github.TeThoLaPot.regen_resources.platform.forge.config;

import com.github.TeThoLaPot.regen_resources.RegenResources;
import net.minecraftforge.common.ForgeConfigSpec;

/**
 * COMMON。サーバー／単体ワールドで共有される既定値。
 */
public final class RegenResourcesForgeConfig {

    private static final String CFG = "config." + RegenResources.MOD_ID + ".";

    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    public static final ForgeConfigSpec.BooleanValue ALLOW_NATURAL_REGEN = BUILDER
            .comment(
                    "Survival mining: if true, unmarked blocks (typical world-gen ore) get the regenerating shell placed.",
                    "If false, only creative/command-marked (rr_src=eligible) placements get the shell; others break vanilla-only.")
            .translation(CFG + "allow_natural_generation_regen")
            .define("allowNaturalGenerationRegen", true);

    public static final ForgeConfigSpec.BooleanValue COMMAND_LIKE_PLACEMENT_ELIGIBLE = BUILDER
            .comment(
                    "When true, EntityPlaceEvent with no entity (e.g. /setblock) sets rr_src=eligible so survival mining places the shell.",
                    "When false, those placements stay unmarked and follow allowNaturalGenerationRegen only.")
            .translation(CFG + "command_like_placement_eligible")
            .define("commandLikePlacementEligible", true);

    public static final ForgeConfigSpec.BooleanValue CHANGE_ANCIENT_DEBRIS_DROPS = BUILDER
            .comment(
                    "When true, mined ancient debris (without Silk Touch) drops regen_resources:ancient_fragment instead of debris items,",
                    "with Fortune applying to fragment count (see loot modifier). When false, vanilla debris drops unchanged.")
            .translation(CFG + "change_ancient_debris_drops")
            .define("changeAncientDebrisDrops", true);

    public static final ForgeConfigSpec SPEC = BUILDER.build();

    private RegenResourcesForgeConfig() {}
}
