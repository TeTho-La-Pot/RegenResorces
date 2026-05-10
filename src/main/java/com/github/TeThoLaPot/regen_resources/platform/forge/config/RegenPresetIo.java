/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.Gson
 *  com.google.gson.GsonBuilder
 *  com.google.gson.JsonArray
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  com.google.gson.JsonParser
 *  com.google.gson.annotations.SerializedName
 *  com.mojang.logging.LogUtils
 *  net.minecraft.core.registries.Registries
 *  net.minecraft.resources.ResourceKey
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.tags.TagKey
 *  net.minecraft.world.level.block.Block
 *  net.minecraftforge.fml.loading.FMLPaths
 *  org.jetbrains.annotations.Nullable
 *  org.slf4j.Logger
 */
package com.github.TeThoLaPot.regen_resources.platform.forge.config;

import com.github.TeThoLaPot.regen_resources.common.block.RegenCustomVisualSpec;
import com.github.TeThoLaPot.regen_resources.common.block.RegenTemplate;
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
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Stream;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.fml.loading.FMLPaths;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public final class RegenPresetIo {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();
    private static final Gson GSON_PRETTY = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static final String DIM_OVERWORLD = "minecraft:overworld";
    private static final String DIM_NETHER = "minecraft:the_nether";

    private RegenPresetIo() {
    }

    public static Path rulesDir() {
        return FMLPaths.CONFIGDIR.get().resolve("RegenResources").resolve("RegenPresets");
    }

    public static List<RegenRule> loadOrCreateDefaults() {
        Path dir = RegenPresetIo.rulesDir();
        try {
            Files.createDirectories(dir, new FileAttribute[0]);
            RegenPresetIo.ensureVanillaDefaultPresets(dir);
        }
        catch (IOException ex) {
            LOGGER.warn("RegenResources: RegenPresets bootstrap: {}", (Object)ex.toString());
        }
        ArrayList<RegenRule> rules = new ArrayList<RegenRule>();
        try {
            List<Path> files = RegenPresetIo.listJsonFilesSorted(dir);
            for (Path p : files) {
                try {
                    String json = Files.readString(p, StandardCharsets.UTF_8);
                    JsonObject obj = JsonParser.parseString((String)json).getAsJsonObject();
                    if (RegenPresetIo.looksLikeAlphaPreset(obj)) {
                        rules.addAll(RegenPresetIo.parseAlphaFile(p.getFileName().toString(), json));
                        continue;
                    }
                    RegenRule flat = RegenPresetIo.parseFlatRule(obj);
                    if (flat == null) continue;
                    rules.add(flat);
                }
                catch (IOException | RuntimeException ex) {
                    LOGGER.warn("RegenResources: skip broken preset '{}': {}", (Object)p.getFileName(), (Object)ex.toString());
                }
            }
        }
        catch (IOException ex) {
            LOGGER.warn("RegenResources: could not list RegenPresets: {}", (Object)ex.toString());
        }
        return rules;
    }

    private static boolean looksLikeAlphaPreset(JsonObject obj) {
        boolean hasPreset = obj.has("preset") && obj.get("preset").isJsonPrimitive();
        return hasPreset && obj.has("entries") && obj.get("entries").isJsonArray();
    }

    @Nullable
    private static String blankToNull(@Nullable String s) {
        if (s == null) {
            return null;
        }
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    private static List<RegenRule> parseAlphaFile(String fileNameLabel, String json) {
        String presetToken;
        PresetFileRoot root = (PresetFileRoot)GSON.fromJson(json, PresetFileRoot.class);
        ArrayList<RegenRule> out = new ArrayList<RegenRule>();
        String string = presetToken = root != null ? RegenPresetIo.blankToNull(root.preset) : null;
        if (root == null || presetToken == null) {
            LOGGER.warn("RegenResources: preset file '{}' has no non-blank 'preset' field", (Object)fileNameLabel);
            return out;
        }
        RegenVisual visual = RegenVisual.tryParseToken(presetToken);
        if (visual == null) {
            LOGGER.warn("RegenResources: unknown preset id '{}' in {}", (Object)presetToken, (Object)fileNameLabel);
            return out;
        }
        if (root.entries == null || root.entries.isEmpty()) {
            return out;
        }
        long rootDefaultDelay = RegenPresetIo.resolveDelayTicksAlias(root.delayTicks, root.tick, "root of '" + fileNameLabel + "'");
        for (PresetEntryJson e : root.entries) {
            long delay = RegenPresetIo.alphaEntryDelayTicks(e, rootDefaultDelay, "entry in '" + fileNameLabel + "'");
            if (delay < 1L) {
                LOGGER.warn("RegenResources: skip entry with invalid delay (no effective tick/delay_ticks entry or root) in {}", (Object)fileNameLabel);
                continue;
            }
            DimensionRestriction dr = DimensionRestriction.fromJson(e.dimensions, e.dimensionExclusion);
            ArrayList<ResourceLocation> ids = new ArrayList<ResourceLocation>();
            ArrayList<TagKey<Block>> tags = new ArrayList<TagKey<Block>>();
            RegenPresetIo.addBlockSpecs(e.targets, ids, tags);
            if (ids.isEmpty() && tags.isEmpty()) continue;
            RegenCustomVisualSpec custom = null;
            if ((visual == RegenVisual.CUSTOM_PRESET || visual == RegenVisual.CUSTOM) && (custom = RegenPresetIo.parseCustomSpec(fileNameLabel, e.template, e.textures, e.miningSample)) == null) {
                LOGGER.warn("RegenResources: skip custom entry without valid template/textures in {}", (Object)fileNameLabel);
                continue;
            }
            out.add(new RegenRule(delay, visual, dr, List.copyOf(ids), List.copyOf(tags), e.naturalRegen, custom));
        }
        return out;
    }

    private static long alphaEntryDelayTicks(PresetEntryJson e, long rootDefaultDelayOrZero, String contextForLog) {
        long inner = RegenPresetIo.resolveDelayTicksAlias(e.delayTicks, e.tick, contextForLog);
        if (inner > 0L) {
            return inner;
        }
        if (rootDefaultDelayOrZero > 0L) {
            return rootDefaultDelayOrZero;
        }
        return 0L;
    }

    private static long resolveDelayTicksAlias(@Nullable Long delayTicksField, @Nullable Long tickField, String contextForLog) {
        Long t;
        Long d = delayTicksField != null && delayTicksField > 0L ? Long.valueOf(delayTicksField) : null;
        Long l = t = tickField != null && tickField > 0L ? Long.valueOf(tickField) : null;
        if (d != null && t != null && !d.equals(t)) {
            LOGGER.warn("RegenResources: {}: fields 'delay_ticks' ({}) and 'tick' ({}) differ; using 'tick'", new Object[]{contextForLog, d, t});
            return t;
        }
        if (t != null) {
            return t;
        }
        if (d != null) {
            return d;
        }
        return 0L;
    }

    @Nullable
    private static Long readOptionalPositiveDelayFromJson(JsonObject obj, String key) {
        if (!obj.has(key) || !obj.get(key).isJsonPrimitive()) {
            return null;
        }
        try {
            long v = obj.get(key).getAsLong();
            return v > 0L ? Long.valueOf(v) : null;
        }
        catch (RuntimeException ex) {
            return null;
        }
    }

    private static List<Path> listJsonFilesSorted(Path dir) throws IOException {
        ArrayList<Path> out = new ArrayList<Path>();
        if (!Files.isDirectory(dir, new LinkOption[0])) {
            return out;
        }
        try (Stream<Path> stream = Files.list(dir);){
            stream.filter(p -> p.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".json")).forEach(out::add);
        }
        out.sort(Comparator.comparing(p -> p.getFileName().toString()));
        return out;
    }

    /**
     * Writes bundled vanilla examples for any default filenames that are not present yet.
     * (Previously we only wrote when the folder had no .json at all, so a single custom file blocked all defaults.)
     */
    private static void ensureVanillaDefaultPresets(Path presetsDir) throws IOException {
        Files.createDirectories(presetsDir, new FileAttribute[0]);
        int n = 0;
        n += RegenPresetIo.writeDefaultJsonIfAbsent(presetsDir.resolve("stone_preset.json"), GSON_PRETTY.toJson(RegenPresetIo.vanillaStonePreset()));
        n += RegenPresetIo.writeDefaultJsonIfAbsent(presetsDir.resolve("deepslate_preset.json"), GSON_PRETTY.toJson(RegenPresetIo.vanillaDeepslatePreset()));
        n += RegenPresetIo.writeDefaultJsonIfAbsent(presetsDir.resolve("nether_preset.json"), GSON_PRETTY.toJson(RegenPresetIo.vanillaNetherPreset()));
        n += RegenPresetIo.writeDefaultJsonIfAbsent(presetsDir.resolve("end_preset.json"), GSON_PRETTY.toJson(RegenPresetIo.emptyEndPreset()));
        n += RegenPresetIo.writeDefaultJsonIfAbsent(presetsDir.resolve("debris_preset.json"), GSON_PRETTY.toJson(RegenPresetIo.vanillaDebrisPreset()));
        n += RegenPresetIo.writeDefaultJsonIfAbsent(presetsDir.resolve("stripped_log_preset.json"), GSON_PRETTY.toJson(RegenPresetIo.vanillaStrippedLogPreset()));
        if (n > 0) {
            LOGGER.info("RegenResources: wrote {} missing default RegenPresets under {}", n, presetsDir);
        }
    }

    private static int writeDefaultJsonIfAbsent(Path path, String utf8) throws IOException {
        if (Files.exists(path, LinkOption.NOFOLLOW_LINKS)) {
            return 0;
        }
        RegenPresetIo.writeUtf8(path, utf8);
        return 1;
    }

    private static void writeUtf8(Path path, String utf8) throws IOException {
        Files.createDirectories(path.getParent(), new FileAttribute[0]);
        Files.writeString(path, (CharSequence)utf8, StandardCharsets.UTF_8, new OpenOption[0]);
    }

    private static RegenRule parseFlatRule(JsonObject obj) {
        Long tickRoot;
        Long delayTicksRoot = RegenPresetIo.readOptionalPositiveDelayFromJson(obj, "delay_ticks");
        long delay = RegenPresetIo.resolveDelayTicksAlias(delayTicksRoot, tickRoot = RegenPresetIo.readOptionalPositiveDelayFromJson(obj, "tick"), "(flat rule)");
        if (delay < 1L) {
            delay = 1200L;
        }
        RegenVisual visual = RegenPresetIo.parseFlatVisualToken(obj);
        DimensionRestriction dr = RegenPresetIo.dimensionFromFlat(obj);
        ArrayList<ResourceLocation> ids = new ArrayList<ResourceLocation>();
        ArrayList<TagKey<Block>> tags = new ArrayList<TagKey<Block>>();
        RegenPresetIo.addBlockSpecs(RegenPresetIo.targetsFromFlatJson(obj), ids, tags);
        if (ids.isEmpty() && tags.isEmpty()) {
            return null;
        }
        Boolean naturalRegen = obj.has("natural_regen") ? Boolean.valueOf(obj.get("natural_regen").getAsBoolean()) : null;
        RegenCustomVisualSpec custom = null;
        if (visual == RegenVisual.CUSTOM_PRESET || visual == RegenVisual.CUSTOM) {
            String miningSample;
            String templateName = obj.has("template") ? obj.get("template").getAsString() : null;
            ArrayList<String> textureEntries = new ArrayList<String>();
            if (obj.has("textures") && obj.get("textures").isJsonObject()) {
                JsonObject t = obj.getAsJsonObject("textures");
                for (Map.Entry e : t.entrySet()) {
                    if (!((JsonElement)e.getValue()).isJsonPrimitive()) continue;
                    textureEntries.add((String)e.getKey() + "=" + ((JsonElement)e.getValue()).getAsString());
                }
            }
            if ((custom = RegenPresetIo.parseCustomSpecFromFlatStrings(templateName, textureEntries, miningSample = obj.has("mining_sample") ? obj.get("mining_sample").getAsString() : null)) == null) {
                LOGGER.warn("RegenResources: skip flat custom rule without valid template/textures");
                return null;
            }
        }
        return new RegenRule(delay, visual, dr, List.copyOf(ids), List.copyOf(tags), naturalRegen, custom);
    }

    private static RegenVisual parseFlatVisualToken(JsonObject obj) {
        String raw;
        String string = raw = obj.has("preset") && obj.get("preset").isJsonPrimitive() ? obj.get("preset").getAsString().trim() : "";
        if (raw.isEmpty()) {
            return RegenVisual.STONE_PRESET;
        }
        try {
            return RegenVisual.valueOf(raw.toUpperCase(Locale.ROOT));
        }
        catch (IllegalArgumentException illegalArgumentException) {
            for (RegenVisual vv : RegenVisual.values()) {
                if (!vv.getSerializedName().equals(raw)) continue;
                return vv;
            }
            return RegenVisual.STONE_PRESET;
        }
    }

    @Nullable
    private static List<String> targetsFromFlatJson(JsonObject obj) {
        ArrayList<String> out = new ArrayList<String>();
        RegenPresetIo.appendJsonStringArray(obj, "targets", out);
        return out.isEmpty() ? null : out;
    }

    private static void appendJsonStringArray(JsonObject obj, String key, List<String> out) {
        if (!obj.has(key) || !obj.get(key).isJsonArray()) {
            return;
        }
        for (JsonElement e : obj.getAsJsonArray(key)) {
            if (!e.isJsonPrimitive()) continue;
            out.add(e.getAsString());
        }
    }

    @Nullable
    private static RegenCustomVisualSpec parseCustomSpec(String fileNameLabel, @Nullable String templateName, @Nullable Map<String, String> textures, @Nullable String miningSample) {
        RegenTemplate template = RegenTemplate.fromSerializedName(templateName);
        if (template == null) {
            LOGGER.warn("RegenResources: unknown or missing 'template' in {}", (Object)fileNameLabel);
            return null;
        }
        LinkedHashMap<String, ResourceLocation> tex = new LinkedHashMap<String, ResourceLocation>();
        if (textures != null) {
            for (Map.Entry<String, String> e : textures.entrySet()) {
                if (e.getValue() == null || e.getValue().isBlank()) continue;
                if (!template.slots().contains(e.getKey())) {
                    LOGGER.warn("RegenResources: texture slot '{}' not in template '{}' in {}", new Object[]{e.getKey(), template.getSerializedName(), fileNameLabel});
                    continue;
                }
                ResourceLocation rl = ResourceLocation.tryParse((String)e.getValue().trim());
                if (rl == null) {
                    LOGGER.warn("RegenResources: invalid texture id '{}' for slot '{}' in {}", new Object[]{e.getValue(), e.getKey(), fileNameLabel});
                    continue;
                }
                tex.put(e.getKey(), rl);
            }
        }
        if (tex.isEmpty()) {
            LOGGER.warn("RegenResources: no valid textures for template '{}' in {}", (Object)template.getSerializedName(), (Object)fileNameLabel);
            return null;
        }
        ResourceLocation sample = null;
        if (miningSample != null && !miningSample.isBlank()) {
            sample = ResourceLocation.tryParse((String)miningSample.trim());
        }
        return new RegenCustomVisualSpec(template, tex, sample);
    }

    @Nullable
    private static RegenCustomVisualSpec parseCustomSpecFromFlatStrings(@Nullable String templateName, List<String> textureEntries, @Nullable String miningSample) {
        LinkedHashMap<String, String> tex = new LinkedHashMap<String, String>();
        for (String s : textureEntries) {
            int idx = s.indexOf(61);
            if (idx < 0) continue;
            tex.put(s.substring(0, idx), s.substring(idx + 1));
        }
        return RegenPresetIo.parseCustomSpec("(flat)", templateName, tex, miningSample);
    }

    @Nullable
    private static DimensionRestriction dimensionFromFlat(JsonObject obj) {
        if (!obj.has("dimensions")) {
            return null;
        }
        JsonArray a = obj.getAsJsonArray("dimensions");
        ArrayList<String> dimStrings = new ArrayList<String>();
        for (JsonElement e : a) {
            dimStrings.add(e.getAsString());
        }
        Boolean exclusionFlag = obj.has("dimension_exclusion") ? Boolean.valueOf(obj.get("dimension_exclusion").getAsBoolean()) : null;
        return DimensionRestriction.fromJson(dimStrings, exclusionFlag);
    }

    private static void addBlockSpecs(@Nullable List<String> blocks, List<ResourceLocation> ids, List<TagKey<Block>> tags) {
        if (blocks == null) {
            return;
        }
        for (String s : blocks) {
            if (s == null || s.isBlank()) continue;
            String t = s.trim();
            if (t.startsWith("#")) {
                String id = t.substring(1);
                if (!ResourceLocation.isValidResourceLocation((String)id)) continue;
                ResourceLocation rl = new ResourceLocation(id);
                tags.add((TagKey<Block>)TagKey.create((ResourceKey)Registries.BLOCK, (ResourceLocation)rl));
                continue;
            }
            if (!ResourceLocation.isValidResourceLocation((String)t)) continue;
            ResourceLocation rl = new ResourceLocation(t);
            ids.add(rl);
        }
    }

    private static PresetEntryJson presetEntry(long tick, String blockId) {
        PresetEntryJson e = new PresetEntryJson();
        e.tick = tick;
        e.targets = List.of(blockId);
        return e;
    }

    private static PresetEntryJson presetEntry(long tick, String blockId, List<String> dimensions) {
        PresetEntryJson e = RegenPresetIo.presetEntry(tick, blockId);
        e.dimensions = dimensions;
        return e;
    }

    private static PresetFileRoot vanillaStonePreset() {
        PresetFileRoot r = new PresetFileRoot();
        r.preset = "stone_preset";
        r.entries = new ArrayList<PresetEntryJson>();
        r.entries.add(RegenPresetIo.presetEntry(1000L, "minecraft:coal_ore"));
        r.entries.add(RegenPresetIo.presetEntry(1500L, "minecraft:copper_ore"));
        r.entries.add(RegenPresetIo.presetEntry(2000L, "minecraft:iron_ore"));
        r.entries.add(RegenPresetIo.presetEntry(2000L, "minecraft:gold_ore"));
        r.entries.add(RegenPresetIo.presetEntry(2000L, "minecraft:lapis_ore"));
        r.entries.add(RegenPresetIo.presetEntry(2000L, "minecraft:redstone_ore"));
        r.entries.add(RegenPresetIo.presetEntry(10000L, "minecraft:diamond_ore"));
        r.entries.add(RegenPresetIo.presetEntry(10000L, "minecraft:emerald_ore"));
        return r;
    }

    private static PresetFileRoot vanillaDeepslatePreset() {
        PresetFileRoot r = new PresetFileRoot();
        r.preset = "deepslate_preset";
        r.entries = new ArrayList<PresetEntryJson>();
        r.entries.add(RegenPresetIo.presetEntry(1000L, "minecraft:deepslate_coal_ore"));
        r.entries.add(RegenPresetIo.presetEntry(1500L, "minecraft:deepslate_copper_ore"));
        r.entries.add(RegenPresetIo.presetEntry(2000L, "minecraft:deepslate_iron_ore"));
        r.entries.add(RegenPresetIo.presetEntry(2000L, "minecraft:deepslate_gold_ore"));
        r.entries.add(RegenPresetIo.presetEntry(2000L, "minecraft:deepslate_lapis_ore"));
        r.entries.add(RegenPresetIo.presetEntry(2000L, "minecraft:deepslate_redstone_ore"));
        r.entries.add(RegenPresetIo.presetEntry(10000L, "minecraft:deepslate_diamond_ore"));
        r.entries.add(RegenPresetIo.presetEntry(10000L, "minecraft:deepslate_emerald_ore"));
        return r;
    }

    private static PresetFileRoot vanillaNetherPreset() {
        PresetFileRoot r = new PresetFileRoot();
        r.preset = "nether_preset";
        r.entries = new ArrayList<PresetEntryJson>();
        r.entries.add(RegenPresetIo.presetEntry(1800L, "minecraft:nether_gold_ore", List.of(DIM_NETHER)));
        r.entries.add(RegenPresetIo.presetEntry(2700L, "minecraft:nether_gold_ore", List.of(DIM_OVERWORLD)));
        r.entries.add(RegenPresetIo.presetEntry(1200L, "minecraft:nether_quartz_ore", List.of(DIM_NETHER)));
        r.entries.add(RegenPresetIo.presetEntry(1800L, "minecraft:nether_quartz_ore", List.of(DIM_OVERWORLD)));
        return r;
    }

    private static PresetFileRoot vanillaDebrisPreset() {
        PresetFileRoot r = new PresetFileRoot();
        r.preset = "debris_preset";
        r.entries = new ArrayList<PresetEntryJson>();
        r.entries.add(RegenPresetIo.presetEntry(16000L, "minecraft:ancient_debris"));
        return r;
    }

    private static PresetFileRoot emptyEndPreset() {
        PresetFileRoot r = new PresetFileRoot();
        r.preset = "end_preset";
        r.entries = new ArrayList<PresetEntryJson>();
        return r;
    }

    private static PresetFileRoot vanillaStrippedLogPreset() {
        PresetFileRoot r = new PresetFileRoot();
        r.preset = "stripped_log_preset";
        r.entries = new ArrayList<PresetEntryJson>();
        PresetEntryJson e = new PresetEntryJson();
        e.tick = 1000L;
        e.naturalRegen = Boolean.FALSE;
        e.targets = List.of("#minecraft:logs");
        r.entries.add(e);
        return r;
    }

    private static final class PresetFileRoot {
        @SerializedName(value="preset")
        @Nullable
        String preset;
        @SerializedName(value="delay_ticks")
        @Nullable
        Long delayTicks;
        @Nullable
        Long tick;
        List<PresetEntryJson> entries;

        private PresetFileRoot() {
        }
    }

    private static final class PresetEntryJson {
        @Nullable
        Long tick;
        @SerializedName(value="delay_ticks")
        @Nullable
        Long delayTicks;
        @SerializedName(value="dimensions")
        @Nullable
        List<String> dimensions;
        @SerializedName(value="dimension_exclusion")
        @Nullable
        Boolean dimensionExclusion;
        @SerializedName(value="targets")
        @Nullable
        List<String> targets;
        @SerializedName(value="natural_regen")
        @Nullable
        Boolean naturalRegen;
        @Nullable
        String template;
        @Nullable
        Map<String, String> textures;
        @SerializedName(value="mining_sample")
        @Nullable
        String miningSample;

        private PresetEntryJson() {
        }
    }
}

