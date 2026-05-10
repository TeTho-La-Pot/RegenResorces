package com.github.TeThoLaPot.regen_resources.platform.neoforge.config;

import com.github.TeThoLaPot.regen_resources.common.block.RegenVisual;
import com.github.TeThoLaPot.regen_resources.common.regen.DimensionRestriction;
import com.github.TeThoLaPot.regen_resources.common.regen.RegenRule;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.annotations.SerializedName;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.neoforged.fml.loading.FMLPaths;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import static net.minecraft.core.registries.Registries.BLOCK;

/**
 * {@code config/RegenResources/RegenPresets/*.json}<br>
 * - <strong>alpha 形式:</strong> ルートに {@code preset} と {@code entries}（コミット 8d2444e と同等）<br>
 * - <strong>フラット形式:</strong> {@code visual}, {@code targets}, 任意で {@code dimensions} / {@code dimension_exclusion}
 */
public final class RegenPresetIo {

    private static final Logger LOGGER = LogUtils.getLogger();

    private static final String README_FILE = "README_RegenPresets.txt";
    private static final String README_TEXT =
            """
                    RegenResources — RegenPresets folder
                    - Put preset .json files here (see mod documentation for alpha vs flat format).
                    - Common config: .minecraft/config/regen_resources-common.toml (or your instance config folder).
                    - bootstrapVanillaPresetsWhenEmpty: if true, when this folder has no .json files, the mod writes built-in vanilla ore presets once.
                      If false, an empty folder means zero rules until you add JSON.
                    このフォルダに .json を配置します（形式はマニュアル参照）。
                    bootstrapVanillaPresetsWhenEmpty が true のときだけ、.json が無い場合にバニラ鉱石のサンプルが自動生成されます。
                    """;

    private static void writePresetDirectoryReadmeIfAbsent(Path dir) {
        Path readme = dir.resolve(README_FILE);
        if (Files.exists(readme)) {
            return;
        }
        try {
            Files.writeString(readme, README_TEXT.stripIndent(), StandardCharsets.UTF_8);
        } catch (IOException ex) {
            LOGGER.warn("RegenResources: could not write {}: {}", README_FILE, ex.toString());
        }
    }
    private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();
    private static final Gson GSON_PRETTY = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    private RegenPresetIo() {}

    public static Path rulesDir() {
        return FMLPaths.CONFIGDIR.get().resolve("RegenResources").resolve("RegenPresets");
    }

    /**
     * すべてのプリセットを読み、alpha / フラット混在可。
     * <p>バニラ既定 JSON の自動生成は {@link RegenResourcesForgeConfig#BOOTSTRAP_VANILLA_PRESETS_WHEN_EMPTY} が true
     * かつフォルダに .json が無いときだけ（共通コンフィグ読込後に実行されること）。
     */
    public static List<RegenRule> loadOrCreateDefaults() {
        Path dir = rulesDir();
        try {
            Files.createDirectories(dir);
            writePresetDirectoryReadmeIfAbsent(dir);
            bootstrapVanillaIfFolderEmpty(dir);
        } catch (IOException ex) {
            LOGGER.warn("RegenResources: RegenPresets bootstrap: {}", ex.toString());
        }

        List<RegenRule> rules = new ArrayList<>();
        try {
            List<Path> files = listJsonFilesSorted(dir);
            for (Path p : files) {
                try {
                    String json = Files.readString(p, StandardCharsets.UTF_8);
                    JsonObject obj = JsonParser.parseString(json).getAsJsonObject();
                    if (looksLikeAlphaPreset(obj)) {
                        rules.addAll(parseAlphaFile(p.getFileName().toString(), json));
                    } else {
                        RegenRule flat = parseFlatRule(obj);
                        if (flat != null) {
                            rules.add(flat);
                        }
                    }
                } catch (RuntimeException | IOException ex) {
                    LOGGER.warn("RegenResources: skip broken preset '{}': {}", p.getFileName(), ex.toString());
                }
            }
        } catch (IOException ex) {
            LOGGER.warn("RegenResources: could not list RegenPresets: {}", ex.toString());
        }
        if (rules.isEmpty()) {
            LOGGER.info(
                    "RegenResources: no regeneration rules loaded from {} (add .json presets there, or set bootstrapVanillaPresetsWhenEmpty=true in common config to auto-create vanilla ore presets).",
                    dir.toAbsolutePath());
        }
        return rules;
    }

    private static boolean looksLikeAlphaPreset(JsonObject obj) {
        return obj.has("preset")
                && obj.get("preset").isJsonPrimitive()
                && obj.has("entries")
                && obj.get("entries").isJsonArray();
    }

    private static List<RegenRule> parseAlphaFile(String fileNameLabel, String json) {
        PresetFileRoot root = GSON.fromJson(json, PresetFileRoot.class);
        List<RegenRule> out = new ArrayList<>();
        if (root == null || root.preset == null || root.preset.isBlank()) {
            LOGGER.warn("RegenResources: preset file '{}' has no 'preset' field", fileNameLabel);
            return out;
        }
        RegenVisual visual = RegenVisual.tryParseToken(root.preset.trim());
        if (visual == null) {
            LOGGER.warn(
                    "RegenResources: unknown preset id '{}' in {}, falling back to stone_preset visual (targets still load)",
                    root.preset,
                    fileNameLabel);
            visual = RegenVisual.STONE_PRESET;
        }
        if (root.entries == null || root.entries.isEmpty()) {
            return out;
        }
        for (PresetEntryJson e : root.entries) {
            long delay = delayTicksFrom(e);
            if (delay < 1) {
                LOGGER.warn("RegenResources: skip entry with invalid delay in {}", fileNameLabel);
                continue;
            }
            DimensionRestriction dr = DimensionRestriction.fromJson(e.dimension, e.dimensionExclusion);
            List<ResourceLocation> ids = new ArrayList<>();
            List<TagKey<Block>> tags = new ArrayList<>();
            addBlockSpecs(e.blocks, ids, tags);
            addBlockSpecs(e.targets, ids, tags);
            if (ids.isEmpty() && tags.isEmpty()) {
                continue;
            }
            out.add(new RegenRule(delay, visual, dr, List.copyOf(ids), List.copyOf(tags)));
        }
        return out;
    }

    private static long delayTicksFrom(PresetEntryJson e) {
        if (e.delayTicks != null && e.delayTicks > 0) {
            return e.delayTicks.longValue();
        }
        if (e.tick != null && e.tick > 0) {
            return e.tick.longValue();
        }
        return 0;
    }

    /** ディレクトリに json が無ければ、コンフィグが許すときだけ alpha 相当の既定プリセットを書き出す。 */
    private static void bootstrapVanillaIfFolderEmpty(Path presetsDir) throws IOException {
        if (!RegenResourcesForgeConfig.BOOTSTRAP_VANILLA_PRESETS_WHEN_EMPTY.get()) {
            return;
        }
        Files.createDirectories(presetsDir);
        if (!isEmptyJsonDirectory(presetsDir)) {
            return;
        }
        LOGGER.info("RegenResources: generating default RegenPresets (vanilla); disable with bootstrapVanillaPresetsWhenEmpty=false");
        writeUtf8(presetsDir.resolve("stone_preset.json"), GSON_PRETTY.toJson(vanillaStonePreset()));
        writeUtf8(presetsDir.resolve("deepslate_preset.json"), GSON_PRETTY.toJson(vanillaDeepslatePreset()));
        writeUtf8(presetsDir.resolve("nether_preset.json"), GSON_PRETTY.toJson(vanillaNetherPreset()));
        writeUtf8(presetsDir.resolve("end_preset.json"), GSON_PRETTY.toJson(emptyEndPreset()));
        writeUtf8(presetsDir.resolve("debris_preset.json"), GSON_PRETTY.toJson(vanillaDebrisPreset()));
    }

    private static boolean isEmptyJsonDirectory(Path dir) throws IOException {
        return listJsonFilesSorted(dir).isEmpty();
    }

    private static List<Path> listJsonFilesSorted(Path dir) throws IOException {
        List<Path> out = new ArrayList<>();
        if (!Files.isDirectory(dir)) {
            return out;
        }
        try (var stream = Files.list(dir)) {
            stream.filter(p -> p.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".json")).forEach(out::add);
        }
        out.sort(Comparator.comparing(p -> p.getFileName().toString()));
        return out;
    }

    private static void writeUtf8(Path path, String utf8) throws IOException {
        Files.createDirectories(path.getParent());
        Files.writeString(path, utf8, StandardCharsets.UTF_8);
    }

    /* ----- フラット（旧 TT 側） ----- */

    private static RegenRule parseFlatRule(JsonObject obj) {
        long delay = obj.has("delay_ticks") ? obj.get("delay_ticks").getAsLong() : 20L * 60L;
        RegenVisual visual = RegenVisual.STONE_PRESET;
        if (obj.has("visual")) {
            String v = obj.get("visual").getAsString();
            try {
                visual = RegenVisual.valueOf(v.trim().toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException ignored) {
                for (RegenVisual vv : RegenVisual.values()) {
                    if (vv.getSerializedName().equals(v)) {
                        visual = vv;
                        break;
                    }
                }
            }
        }

        DimensionRestriction dr = dimensionFromFlat(obj);

        List<ResourceLocation> ids = new ArrayList<>();
        List<TagKey<Block>> tags = new ArrayList<>();
        if (obj.has("targets")) {
            JsonArray a = obj.getAsJsonArray("targets");
            List<String> blockStrings = new ArrayList<>();
            for (JsonElement e : a) {
                blockStrings.add(e.getAsString());
            }
            addBlockSpecs(blockStrings, ids, tags);
        }
        if (ids.isEmpty() && tags.isEmpty()) {
            return null;
        }
        return new RegenRule(delay, visual, dr, List.copyOf(ids), List.copyOf(tags));
    }

    private static @Nullable DimensionRestriction dimensionFromFlat(JsonObject obj) {
        if (!obj.has("dimensions")) {
            return null;
        }
        JsonArray a = obj.getAsJsonArray("dimensions");
        List<String> dimStrings = new ArrayList<>();
        for (JsonElement e : a) {
            dimStrings.add(e.getAsString());
        }
        Boolean exclusionFlag =
                obj.has("dimension_exclusion") ? obj.get("dimension_exclusion").getAsBoolean() : null;
        return DimensionRestriction.fromJson(dimStrings, exclusionFlag);
    }

    private static void addBlockSpecs(@Nullable List<String> blocks, List<ResourceLocation> ids, List<TagKey<Block>> tags) {
        if (blocks == null) {
            return;
        }
        for (String s : blocks) {
            if (s == null || s.isBlank()) {
                continue;
            }
            String t = s.trim();
            if (t.startsWith("#")) {
                String id = t.substring(1);
                ResourceLocation rl = ResourceLocation.tryParse(id);
                if (rl != null) {
                    tags.add(TagKey.create(BLOCK, rl));
                }
            } else {
                ResourceLocation rl = ResourceLocation.tryParse(t);
                if (rl != null) {
                    ids.add(rl);
                }
            }
        }
    }

    /* ----- bootstrap データ（alpha と同趣旨） ----- */

    private static PresetFileRoot vanillaStonePreset() {
        PresetFileRoot r = new PresetFileRoot();
        r.preset = "stone_preset";
        r.entries = new ArrayList<>();
        PresetEntryJson common = new PresetEntryJson();
        common.tick = 200L;
        common.blocks = List.of(
                "minecraft:coal_ore",
                "minecraft:iron_ore",
                "minecraft:copper_ore",
                "minecraft:gold_ore",
                "minecraft:redstone_ore",
                "minecraft:emerald_ore",
                "minecraft:lapis_ore"
        );
        PresetEntryJson slow = new PresetEntryJson();
        slow.tick = 6000L;
        slow.blocks = List.of("minecraft:diamond_ore");
        r.entries.add(common);
        r.entries.add(slow);
        return r;
    }

    private static PresetFileRoot vanillaDeepslatePreset() {
        PresetFileRoot r = new PresetFileRoot();
        r.preset = "deepslate_preset";
        r.entries = new ArrayList<>();
        PresetEntryJson common = new PresetEntryJson();
        common.tick = 200L;
        common.blocks = List.of(
                "minecraft:deepslate_coal_ore",
                "minecraft:deepslate_iron_ore",
                "minecraft:deepslate_copper_ore",
                "minecraft:deepslate_gold_ore",
                "minecraft:deepslate_redstone_ore",
                "minecraft:deepslate_emerald_ore",
                "minecraft:deepslate_lapis_ore"
        );
        PresetEntryJson slow = new PresetEntryJson();
        slow.tick = 6000L;
        slow.blocks = List.of("minecraft:deepslate_diamond_ore");
        r.entries.add(common);
        r.entries.add(slow);
        return r;
    }

    private static PresetFileRoot vanillaNetherPreset() {
        PresetFileRoot r = new PresetFileRoot();
        r.preset = "nether_preset";
        r.entries = new ArrayList<>();
        PresetEntryJson e = new PresetEntryJson();
        e.tick = 200L;
        e.blocks = List.of("minecraft:nether_gold_ore", "minecraft:nether_quartz_ore");
        r.entries.add(e);
        return r;
    }

    private static PresetFileRoot vanillaDebrisPreset() {
        PresetFileRoot r = new PresetFileRoot();
        r.preset = "debris_preset";
        r.entries = new ArrayList<>();
        PresetEntryJson e = new PresetEntryJson();
        e.tick = 12000L;
        e.blocks = List.of("minecraft:ancient_debris");
        r.entries.add(e);
        return r;
    }

    private static PresetFileRoot emptyEndPreset() {
        PresetFileRoot r = new PresetFileRoot();
        r.preset = "end_preset";
        r.entries = new ArrayList<>();
        return r;
    }

    @SuppressWarnings("unused")
    private static final class PresetFileRoot {
        String preset;
        List<PresetEntryJson> entries;
    }

    @SuppressWarnings("unused")
    private static final class PresetEntryJson {
        @Nullable Long tick;
        @SerializedName("delay_ticks")
        @Nullable
        Long delayTicks;
        @Nullable List<String> dimension;
        @SerializedName("dimension_exclusion")
        @Nullable
        Boolean dimensionExclusion;
        @Nullable List<String> blocks;
        /** フラット形式と同じ «targets» キーをエントリ内に書いた場合 */
        @Nullable List<String> targets;
    }
}
