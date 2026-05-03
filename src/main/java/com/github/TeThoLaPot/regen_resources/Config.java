package com.github.TeThoLaPot.regen_resources;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;

public class Config {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    // 再生対象のブロックIDリスト
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> REGEN_BLOCKS;
    // 基本の再生待機時間 (10秒 = 200tick)
    public static final ForgeConfigSpec.LongValue REGEN_DELAY;
    // ブロックごとの個別設定 (例: "minecraft:diamond_ore=1200")
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> CUSTOM_REGEN_DELAYS;
    // 自然生成されたものを再生するか (あなたのこだわり設定用)
    public static final ForgeConfigSpec.BooleanValue REGEN_NATURAL_ORES;
    public static final ForgeConfigSpec.ConfigValue<Boolean> ENABLE_CHANGE_ANCIENT_DEBRIS_DROPS;

    static {
        BUILDER.push("General Settings");

        REGEN_BLOCKS = BUILDER
                .comment("再生対象にするブロックのIDリストです。")
                .defineList("regenBlocks",
                        List.of(
                                "minecraft:coal_ore", "minecraft:deepslate_coal_ore",
                                "minecraft:iron_ore", "minecraft:deepslate_iron_ore",
                                "minecraft:copper_ore", "minecraft:deepslate_copper_ore",
                                "minecraft:gold_ore", "minecraft:deepslate_gold_ore",
                                "minecraft:redstone_ore", "minecraft:deepslate_redstone_ore",
                                "minecraft:emerald_ore", "minecraft:deepslate_emerald_ore",
                                "minecraft:lapis_ore", "minecraft:deepslate_lapis_ore",
                                "minecraft:diamond_ore", "minecraft:deepslate_diamond_ore",
                                "minecraft:nether_gold_ore", "minecraft:nether_quartz_ore",
                                "minecraft:ancient_debris"
                        ),
                        obj -> obj instanceof String);

        REGEN_DELAY = BUILDER
                .comment("デフォルトの再生時間(Tick)。20tick = 1秒。")
                .defineInRange("regenDelay", 200L, 1L, Long.MAX_VALUE);

        CUSTOM_REGEN_DELAYS = BUILDER
                .comment("ブロックID=時間の形式で個別設定。例: 'minecraft:diamond_ore=1200'")
                .defineList("customRegenDelays",
                        List.of("minecraft:diamond_ore=6000", "minecraft:ancient_debris=12000"),
                        obj -> obj instanceof String);

        REGEN_NATURAL_ORES = BUILDER
                .comment("自然生成された鉱石を再生するかどうか。")
                .define("regenNaturalOres", true);

        ENABLE_CHANGE_ANCIENT_DEBRIS_DROPS = BUILDER.comment("Replaces ancient debris drops with mod-specific items")
                .define("Change Ancient Debris drops", false);

        BUILDER.pop();
    }

    public static final ForgeConfigSpec SPEC = BUILDER.build();

    /**
     * 再生対象かどうかの判定
     */
    public static boolean isRegenTarget(BlockState state) {
        ResourceLocation id = ForgeRegistries.BLOCKS.getKey(state.getBlock());
        return id != null && REGEN_BLOCKS.get().contains(id.toString());
    }

    /**
     * ブロックごとの再生時間を取得。設定がなければデフォルト値を返す。
     */
    public static long getDelayForBlock(BlockState state) {
        ResourceLocation res = ForgeRegistries.BLOCKS.getKey(state.getBlock());
        if (res == null) return REGEN_DELAY.get();

        String id = res.toString();
        for (String entry : CUSTOM_REGEN_DELAYS.get()) {
            String[] split = entry.split("=");
            if (split.length == 2 && split[0].equals(id)) {
                try {
                    return Long.parseLong(split[1]);
                } catch (NumberFormatException e) {
                    return REGEN_DELAY.get();
                }
            }
        }
        return REGEN_DELAY.get();
    }
}

