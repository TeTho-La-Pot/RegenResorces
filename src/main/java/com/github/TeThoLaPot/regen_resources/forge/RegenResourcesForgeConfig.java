package com.github.TeThoLaPot.regen_resources.forge;

import com.github.TeThoLaPot.regen_resources.RegenResources;
import net.minecraftforge.common.ForgeConfigSpec;

/**
 * COMMON。サーバー／単体ワールドで共有される既定値。
 */
public final class RegenResourcesForgeConfig {

    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    public static final ForgeConfigSpec.BooleanValue ALLOW_NATURAL_REGEN = BUILDER
            .comment(
                    "Survival mining: if true, unmarked blocks (typical world-gen ore) get the regenerating shell placed.",
                    "If false, only creative/command-marked (rr_src=eligible) placements get the shell; others break vanilla-only.")
            .define("allowNaturalGenerationRegen", true);

    public static final ForgeConfigSpec.BooleanValue COMMAND_LIKE_PLACEMENT_ELIGIBLE = BUILDER
            .comment(
                    "When true, EntityPlaceEvent with no entity (e.g. /setblock) sets rr_src=eligible so survival mining places the shell.",
                    "When false, those placements stay unmarked and follow allowNaturalGenerationRegen only.")
            .define("commandLikePlacementEligible", true);

    public static final ForgeConfigSpec SPEC = BUILDER.build();

    private RegenResourcesForgeConfig() {}
}
