package com.github.TeThoLaPot.regen_resources;

import com.github.TeThoLaPot.regen_resources.init.block.RegenVisual;
import com.github.TeThoLaPot.regen_resources.preset.RegenBlockRule;
import com.github.TeThoLaPot.regen_resources.preset.RegenPresetIo;
import com.mojang.logging.LogUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.registries.ForgeRegistries;
import org.slf4j.Logger;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/**
 * Forge common toml は {@code config/RegenResources/regen-resources-common.toml}。
 * ブロック一覧は {@code config/RegenResources/RegenPresets/*.json} を参照する。
 * <p>
 * RegenResources が TT_Core に座標データを書き込むのは、{@link #isRegenTarget} が真のブロック／
 * 再生シェル {@link com.github.TeThoLaPot.regen_resources.init.block.Re_Blocks#REGEN_BLOCK} に関する経路のみ。
 */
public class Config {

    private static final Logger LOGGER = LogUtils.getLogger();

    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    public static final ForgeConfigSpec.BooleanValue ENABLE_CHANGE_ANCIENT_DEBRIS_DROPS;
    public static final ForgeConfigSpec.BooleanValue GATHER_BREAK_LOOT_AT_PLAYER;
    public static final ForgeConfigSpec.BooleanValue ALLOW_FAKE_PLAYER_REGEN;

    /**
     * ワールド読み込み直後は {@code regen_process} をこのサーバーティック数だけ実行しない（キューに戻して先送り）。0 で無効。
     */
    public static final ForgeConfigSpec.IntValue REGEN_PROCESS_SERVER_WARMUP_TICKS;

    /** 調査用: 再生シェル同期・regen_process・鉱石破壊からの設置をログへ出す。普段は false。 */
    public static final ForgeConfigSpec.BooleanValue REGEN_DIAGNOSTIC_TRACE;

    public static final String ORIGIN_PLAYER = "player";
    /** ピストン等によるブロック移動（{@link net.minecraft.world.level.block.Block#UPDATE_MOVE_BY_PISTON}）。 */
    public static final String ORIGIN_MACHINERY = "machinery";

    private static volatile Map<ResourceLocation, List<RegenBlockRule>> presetRuleTable = Map.of();

    static {
        BUILDER.push("General Settings");
        BUILDER.comment(
                "再生ブロック一覧は JSON: config/RegenResources/RegenPresets/（サーバー側 config を優先）。",
                "初回のみバニラ用プリセットが自動生成されます。"
        );
        ENABLE_CHANGE_ANCIENT_DEBRIS_DROPS = BUILDER
                .comment("Ancient debris のドロップ差し替え（レシピ側）を有効にする")
                .define("Change Ancient Debris drops", false);

        GATHER_BREAK_LOOT_AT_PLAYER = BUILDER
                .comment("採掘ドロップを足元へ落としてすぐ拾えるようにする（バニラの収集音）。true 推奨。")
                .define("gatherBreakLootNearPlayer", true);

        ALLOW_FAKE_PLAYER_REGEN = BUILDER
                .comment("FakePlayer が壊したときに再生予約するか（鉱車・機械採掘）。")
                .define("allowFakePlayerRegen", false);

        BUILDER.pop();

        BUILDER.push("Integration");
        REGEN_PROCESS_SERVER_WARMUP_TICKS = BUILDER
                .comment(
                        "シングル／統合サーバーで再接続した直後、チャンク準備とぶつかって読み込みが止まる場合がある。",
                        "このティック数経過するまで regen_process は実行せずキューに戻す。0 で無効。"
                )
                .defineInRange("regenProcessServerWarmupTicks", 200, 0, 72000);

        REGEN_DIAGNOSTIC_TRACE = BUILDER
                .comment(
                        "調査用ログを latest.log に出す（BE 二重 flush・regen_process・シェル設置など）。完了後は false に戻す。",
                        "手順の一覧: RegenResources/DEV_VERIFICATION.md を参照。"
                )
                .define("diagnosticTraceLog", false);
        BUILDER.pop();
    }

    public static final ForgeConfigSpec SPEC = BUILDER.build();

    public static synchronized void invalidateEntryCache() {
        rebuildEntryCache();
    }

    private static synchronized void rebuildEntryCache() {
        try {
            Path presets = presetDirectory();
            Files.createDirectories(presets);
            RegenPresetIo.bootstrapVanillaIfFolderEmpty(presets);
            presetRuleTable = RegenPresetIo.loadAllMerged(presets);
            LOGGER.info("RegenResources: loaded {} regen block ids from RegenPresets", presetRuleTable.size());
        } catch (Exception e) {
            LOGGER.warn("RegenResources: failed to load RegenPresets (no regen rules until fixed): {}", e.toString());
            presetRuleTable = Map.of();
        }
    }

    /** config/RegenResources/RegenPresets */
    public static Path presetDirectory() {
        return FMLPaths.CONFIGDIR.get().resolve("RegenResources").resolve("RegenPresets");
    }

    public static @org.jetbrains.annotations.Nullable RegenBlockRule matchRule(Level level, BlockState state) {
        ResourceLocation bid = ForgeRegistries.BLOCKS.getKey(state.getBlock());
        if (bid == null) {
            return null;
        }
        Map<ResourceLocation, List<RegenBlockRule>> table = presetRuleTable;
        if (table.isEmpty()) {
            rebuildEntryCache();
            table = presetRuleTable;
        }
        List<RegenBlockRule> rules = table.get(bid);
        if (rules == null || rules.isEmpty()) {
            return null;
        }
        ResourceLocation dim = level.dimension().location();
        for (RegenBlockRule r : rules) {
            if (r.matchesDimension(dim)) {
                return r;
            }
        }
        return null;
    }

    public static boolean isRegenTarget(Level level, BlockState state) {
        return matchRule(level, state) != null;
    }

    public static long getDelayTicksFor(Level level, BlockState state) {
        RegenBlockRule r = matchRule(level, state);
        return r != null ? r.delayTicks : 200L;
    }

    public static RegenVisual resolveWaitingVisual(Level level, BlockState brokenState) {
        RegenBlockRule r = matchRule(level, brokenState);
        return r != null ? r.visual : RegenVisual.STONE_PRESET;
    }

    /**
     * 再生が許されるか（TT に保存された由来）。サバイバルでの設置のみ {@link #ORIGIN_PLAYER} で不可。機械移動後は {@link #ORIGIN_MACHINERY} で不可。
     */
    public static boolean placementAllowsRegen(CompoundTag data) {
        String o = data.getString("origin");
        return !o.equals(ORIGIN_PLAYER) && !o.equals(ORIGIN_MACHINERY);
    }
}
