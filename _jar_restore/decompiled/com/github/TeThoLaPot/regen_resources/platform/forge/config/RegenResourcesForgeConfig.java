/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraftforge.common.ForgeConfigSpec
 *  net.minecraftforge.common.ForgeConfigSpec$BooleanValue
 *  net.minecraftforge.common.ForgeConfigSpec$Builder
 */
package com.github.TeThoLaPot.regen_resources.platform.forge.config;

import net.minecraftforge.common.ForgeConfigSpec;

public final class RegenResourcesForgeConfig {
    private static final String CFG = "config.regen_resources.";
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec.BooleanValue ALLOW_NATURAL_REGEN = BUILDER.comment(new String[]{"Survival mining: if true, unmarked blocks (typical world-gen ore) get the regenerating shell placed.", "If false, only creative/command-marked (rr_src=eligible) placements get the shell; others break vanilla-only."}).translation("config.regen_resources.allow_natural_generation_regen").define("allowNaturalGenerationRegen", true);
    public static final ForgeConfigSpec.BooleanValue COMMAND_LIKE_PLACEMENT_ELIGIBLE = BUILDER.comment(new String[]{"When true, EntityPlaceEvent with no entity (e.g. /setblock) sets rr_src=eligible so survival mining places the shell.", "When false, those placements stay unmarked and follow allowNaturalGenerationRegen only."}).translation("config.regen_resources.command_like_placement_eligible").define("commandLikePlacementEligible", true);
    public static final ForgeConfigSpec.BooleanValue CHANGE_ANCIENT_DEBRIS_DROPS = BUILDER.comment(new String[]{"When true, mined ancient debris (without Silk Touch) drops regen_resources:ancient_fragment instead of debris items,", "with Fortune applying to fragment count (see loot modifier). When false, vanilla debris drops unchanged."}).translation("config.regen_resources.change_ancient_debris_drops").define("changeAncientDebrisDrops", true);
    public static final ForgeConfigSpec.BooleanValue MASS_BREAK_BUG_WORKAROUND = BUILDER.comment(new String[]{"Workaround for vein-mining mods (e.g. OreHarvester) that misfire when the player's sneak state changes mid-mining.", "When true, the player's mining progress is forcibly reset whenever the sneak state toggles while mining the same block.", "This forces the mining session to restart from a consistent sneak state, preventing chain triggers from going off unintentionally.", "Set to false once the upstream vein-mining mod fixes its own behavior."}).translation("config.regen_resources.mass_break_bug_workaround").define("massBreakBugWorkaround", true);
    public static final ForgeConfigSpec SPEC = BUILDER.build();

    private RegenResourcesForgeConfig() {
    }
}

