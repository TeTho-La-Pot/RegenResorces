package com.github.TeThoLaPot.regen_resources.preset;

import com.github.TeThoLaPot.regen_resources.init.block.RegenVisual;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;

import org.jetbrains.annotations.Nullable;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * {@code config/RegenResources/RegenPresets/*.json}
 * を読み、ブロックごとのルールを構築する。
 */
public final class RegenPresetIo {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    private RegenPresetIo() {}

    /** ディレクトリに json が 1 つも無ければバニラ用プリセットを書き出す。 */
    public static void bootstrapVanillaIfFolderEmpty(Path presetsDir) throws IOException {
        Files.createDirectories(presetsDir);
        if (!isEmptyJsonDirectory(presetsDir)) {
            return;
        }
        LOGGER.info("RegenResources: generating default RegenPresets (vanilla)");
        writeUtf8(presetsDir.resolve("stone_preset.json"), toJson(vanillaStonePreset()));
        writeUtf8(presetsDir.resolve("deepslate_preset.json"), toJson(vanillaDeepslatePreset()));
        writeUtf8(presetsDir.resolve("nether_preset.json"), toJson(vanillaNetherPreset()));
        writeUtf8(presetsDir.resolve("end_preset.json"), toJson(emptyEndPreset()));
        writeUtf8(presetsDir.resolve("debris_preset.json"), toJson(vanillaDebrisPreset()));
    }

    /** すべてのプリセットファイルを順に適用したマップを返す。 */
    public static Map<ResourceLocation, List<RegenBlockRule>> loadAllMerged(Path presetsDir) {
        Map<ResourceLocation, List<RegenBlockRule>> merged = new LinkedHashMap<>();

        if (!Files.isDirectory(presetsDir)) {
            return Map.of();
        }

        final List<Path> files;
        try {
            files = listJsonFilesSorted(presetsDir);
        } catch (IOException ex) {
            LOGGER.warn("RegenResources: could not list RegenPresets: {}", ex.toString());
            return Map.of();
        }
        for (Path json : files) {
            try {
                mergeFile(json, merged);
            } catch (Exception e) {
                LOGGER.warn("RegenResources: skip broken preset '{}': {}", json.getFileName(), e.toString());
            }
        }
        return immutableCopy(merged);
    }

    private static boolean isEmptyJsonDirectory(Path dir) throws IOException {
        List<Path> list = listJsonFilesSorted(dir);
        return list.isEmpty();
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

    private static void mergeFile(Path path, Map<ResourceLocation, List<RegenBlockRule>> sink) throws IOException {
        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            PresetFileRoot root = GSON.fromJson(reader, PresetFileRoot.class);
            if (root == null || root.preset == null || root.preset.isBlank()) {
                LOGGER.warn("RegenResources: preset file '{}' has no 'preset' field", path.getFileName());
                return;
            }
            RegenVisual visual = RegenVisual.tryParseToken(root.preset.trim());
            if (visual == null) {
                LOGGER.warn("RegenResources: unknown preset id '{}' in {}", root.preset, path.getFileName());
                return;
            }
            if (root.entries == null || root.entries.isEmpty()) {
                return;
            }
            for (PresetEntryJson e : root.entries) {
                long delay = delayTicksFrom(e);
                if (delay < 1) {
                    LOGGER.warn("RegenResources: skip entry with invalid tick in {}", path.getFileName());
                    continue;
                }
                DimensionRestriction dr = DimensionRestriction.fromJson(e.dimension, e.dimensionExclusion);
                if (e.blocks == null || e.blocks.isEmpty()) {
                    continue;
                }
                RegenBlockRule rule = new RegenBlockRule(delay, visual, dr);
                for (String bs : e.blocks) {
                    if (bs == null || bs.isBlank()) {
                        continue;
                    }
                    ResourceLocation id = ResourceLocation.tryParse(bs.trim());
                    if (id == null) {
                        LOGGER.warn("RegenResources: bad block id '{}' in {}", bs, path.getFileName());
                        continue;
                    }
                    sink.computeIfAbsent(id, k -> new ArrayList<>()).add(rule);
                }
            }
        }
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

    private static Map<ResourceLocation, List<RegenBlockRule>> immutableCopy(Map<ResourceLocation, List<RegenBlockRule>> in) {
        Map<ResourceLocation, List<RegenBlockRule>> out = new LinkedHashMap<>();
        for (var e : in.entrySet()) {
            out.put(e.getKey(), List.copyOf(e.getValue()));
        }
        return Map.copyOf(out);
    }

    private static void writeUtf8(Path path, String utf8) throws IOException {
        Files.createDirectories(path.getParent());
        Files.writeString(path, utf8, StandardCharsets.UTF_8);
    }

    private static String toJson(Object o) {
        return GSON.toJson(o);
    }

    /* ---------------- bootstrap data ---------------- */

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

    /* ---------------- Gson DTOs ---------------- */

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
        @Nullable
        List<String> dimension;
        @SerializedName("dimension_exclusion")
        @Nullable
        Boolean dimensionExclusion;
        @Nullable
        List<String> blocks;
    }
}
