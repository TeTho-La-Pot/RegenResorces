package com.github.TeThoLaPot.regen_resources.platform.neoforge.config;

import com.github.TeThoLaPot.regen_resources.common.block.RegenCustomVisualSpec;
import com.github.TeThoLaPot.regen_resources.common.block.RegenTemplate;
import com.github.TeThoLaPot.regen_resources.common.block.RegenVisual;
import com.github.TeThoLaPot.regen_resources.common.regen.DimensionRestriction;
import com.github.TeThoLaPot.regen_resources.common.regen.RegenRule;
import com.github.TeThoLaPot.regen_resources.common.regen.RegenTargetSpec;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.annotations.SerializedName;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;
import net.neoforged.fml.loading.FMLPaths;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Stream;

/**
 * ワールドの {@code serverconfig/RegenResources/RegenPresets/*.json}。<br>
 * 1.20.1 Forge 版と同じ alpha / フラット形式（{@code natural_regen}、カスタム {@code template}/{@code textures} 含む）。
 */
public final class RegenPresetIo {

    private static final Logger LOGGER = LogUtils.getLogger();

    /** NeoForge のワールド別 SERVER config と同じ階層名。 */
    private static final LevelResource SERVERCONFIG = new LevelResource("serverconfig");

    private static final String README_FILE = "README_RegenPresets.txt";
    private static final String README_TEXT =
            """
                    RegenResources — RegenPresets (per-world)
                    - Path: <world>/serverconfig/RegenResources/RegenPresets/
                    - Put preset .json files here (see mod documentation for alpha vs flat format).
                    - Common config: regen_resources-common.toml → bootstrapVanillaPresetsWhenEmpty
                    - When that option is true, built-in filenames that are still missing (e.g. stone_preset.json) are created once; existing files are never overwritten.
                    ワールドごとのフォルダです。<world>/serverconfig/RegenResources/RegenPresets/ に .json を置きます。
                    bootstrapVanillaPresetsWhenEmpty が true のとき、不足している既定ファイル名だけサンプルが自動生成されます（上書きしません）。
                    """;

    private static final String DIM_OVERWORLD = "minecraft:overworld";
    private static final String DIM_NETHER = "minecraft:the_nether";

    private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();
    private static final Gson GSON_PRETTY = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    /** 現在のワールドのプリセットディレクトリ。ワールド未ロード時は null。 */
    private static volatile @Nullable Path activeWorldRulesDir;

    private RegenPresetIo() {}

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

    /** 旧グローバル配置（マイグレーション元）。 */
    public static Path legacyGlobalRulesDir() {
        return FMLPaths.CONFIGDIR.get().resolve("RegenResources").resolve("RegenPresets");
    }

    /** {@code <world>/serverconfig/RegenResources/RegenPresets}。ワールド未バインド時は null。 */
    public static @Nullable Path rulesDir() {
        return activeWorldRulesDir;
    }

    public static Path rulesDirFor(MinecraftServer server) {
        return server.getWorldPath(SERVERCONFIG).resolve("RegenResources").resolve("RegenPresets");
    }

    public static void bindWorld(MinecraftServer server) {
        Path dir = rulesDirFor(server);
        activeWorldRulesDir = dir;
        try {
            Files.createDirectories(dir);
            Path readme = dir.resolve(README_FILE);
            boolean firstSetup = !Files.exists(readme, LinkOption.NOFOLLOW_LINKS);
            writePresetDirectoryReadmeIfAbsent(dir);
            // 初回のみ旧グローバルから移行。空フォルダへの再入場で毎回コピーし直さない。
            if (firstSetup) {
                migrateFromLegacyGlobalIfNeeded(dir);
            }
        } catch (IOException ex) {
            LOGGER.warn("RegenResources: could not prepare world RegenPresets {}: {}", dir, ex.toString());
        }
        LOGGER.info("RegenResources: RegenPresets bound to {}", dir.toAbsolutePath());
    }

    public static void unbindWorld() {
        activeWorldRulesDir = null;
    }

    /** ワールド未バインド時は空。ファイル名は {@code *.json} のファイル名のみ。 */
    public static List<String> listPresetFileNames() {
        Path dir = activeWorldRulesDir;
        if (dir == null) {
            return List.of();
        }
        try {
            return listJsonFilesSorted(dir).stream().map(p -> p.getFileName().toString()).toList();
        } catch (IOException ex) {
            LOGGER.warn("RegenResources: list presets failed: {}", ex.toString());
            return List.of();
        }
    }

    public static @Nullable String readPresetJson(String fileName) {
        Path path = resolvePresetFile(fileName);
        if (path == null) {
            return null;
        }
        try {
            return Files.readString(path, StandardCharsets.UTF_8);
        } catch (IOException ex) {
            LOGGER.warn("RegenResources: read preset '{}' failed: {}", fileName, ex.toString());
            return null;
        }
    }

    /**
     * @return null なら成功、非 null はエラーメッセージキーまたは説明
     */
    public static @Nullable String writePresetJson(String fileName, String json) {
        Path path = resolvePresetFile(fileName);
        if (path == null) {
            return "invalid_name";
        }
        try {
            JsonParser.parseString(json);
        } catch (RuntimeException ex) {
            return "invalid_json";
        }
        try {
            writeUtf8(path, json);
            return null;
        } catch (IOException ex) {
            LOGGER.warn("RegenResources: write preset '{}' failed: {}", fileName, ex.toString());
            return "io_error";
        }
    }

    public static @Nullable String deletePresetJson(String fileName) {
        Path path = resolvePresetFile(fileName);
        if (path == null) {
            return "invalid_name";
        }
        try {
            Files.deleteIfExists(path);
            return null;
        } catch (IOException ex) {
            LOGGER.warn("RegenResources: delete preset '{}' failed: {}", fileName, ex.toString());
            return "io_error";
        }
    }

    public static String defaultEmptyPresetJson(String presetToken) {
        String token = presetToken == null || presetToken.isBlank() ? "stone_preset" : presetToken.trim();
        Map<String, Object> root = new LinkedHashMap<>();
        root.put("preset", token);
        root.put("global_tick", 1200);
        root.put("entries", List.of());
        return GSON_PRETTY.toJson(root);
    }

    public static boolean isValidPresetFileName(String fileName) {
        if (fileName == null) {
            return false;
        }
        String n = fileName.trim();
        if (!n.toLowerCase(Locale.ROOT).endsWith(".json")) {
            return false;
        }
        String base = n.substring(0, n.length() - 5);
        if (base.isEmpty() || base.length() > 64) {
            return false;
        }
        for (int i = 0; i < base.length(); i++) {
            char c = base.charAt(i);
            if (!(c >= 'a' && c <= 'z')
                    && !(c >= '0' && c <= '9')
                    && c != '_'
                    && c != '-') {
                return false;
            }
        }
        return true;
    }

    private static @Nullable Path resolvePresetFile(String fileName) {
        Path dir = activeWorldRulesDir;
        if (dir == null || !isValidPresetFileName(fileName)) {
            return null;
        }
        Path resolved = dir.resolve(fileName.trim()).normalize();
        if (!resolved.startsWith(dir.normalize())) {
            return null;
        }
        return resolved;
    }

    /**
     * ワールド側に JSON が無く、旧 {@code config/RegenResources/RegenPresets} にだけある場合は一度コピーする。
     */
    private static void migrateFromLegacyGlobalIfNeeded(Path worldDir) throws IOException {
        List<Path> worldJson = listJsonFilesSorted(worldDir);
        if (!worldJson.isEmpty()) {
            return;
        }
        Path legacy = legacyGlobalRulesDir();
        if (!Files.isDirectory(legacy)) {
            return;
        }
        List<Path> legacyJson = listJsonFilesSorted(legacy);
        if (legacyJson.isEmpty()) {
            return;
        }
        int n = 0;
        for (Path src : legacyJson) {
            Path dest = worldDir.resolve(src.getFileName().toString());
            if (Files.exists(dest, LinkOption.NOFOLLOW_LINKS)) {
                continue;
            }
            Files.copy(src, dest);
            n++;
        }
        if (n > 0) {
            LOGGER.info(
                    "RegenResources: migrated {} preset file(s) from {} to world {}",
                    n,
                    legacy.toAbsolutePath(),
                    worldDir.toAbsolutePath());
        }
    }

    /**
     * すべてのプリセットを読み、alpha / フラット混在可。
     * <p>ワールド未バインド時は空リスト（グローバル {@code config/} へは書き込まない）。
     * <p>読み込み順: {@code <world>/serverconfig/.../RegenPresets/} を優先し、
     * 同名ファイルが無いものだけ旧 {@code config/RegenResources/RegenPresets/} からも読む
     * （ダミー／再生の両方で両ロケーションを使う）。
     * <p>{@link RegenResourcesForgeConfig#BOOTSTRAP_VANILLA_PRESETS_WHEN_EMPTY} が true のとき、
     * <strong>バインド中ワールド</strong>の serverconfig にだけ不足している既定 JSON を生成する。
     */
    public static List<RegenRule> loadOrCreateDefaults() {
        Path dir = activeWorldRulesDir;
        if (dir == null) {
            return List.of();
        }
        try {
            Files.createDirectories(dir);
            writePresetDirectoryReadmeIfAbsent(dir);
            ensureVanillaDefaultPresets(dir);
        } catch (IOException ex) {
            LOGGER.warn("RegenResources: RegenPresets bootstrap: {}", ex.toString());
        }

        List<RegenRule> rules = new ArrayList<>();
        java.util.LinkedHashSet<String> loadedNames = new java.util.LinkedHashSet<>();
        appendRulesFromDir(dir, rules, loadedNames, true);
        Path legacy = legacyGlobalRulesDir();
        if (Files.isDirectory(legacy)) {
            appendRulesFromDir(legacy, rules, loadedNames, false);
        }
        if (rules.isEmpty()) {
            LOGGER.info(
                    "RegenResources: no regeneration rules loaded from {} (or legacy {}) — add .json presets or enable bootstrapVanillaPresetsWhenEmpty in common config.",
                    dir.toAbsolutePath(),
                    legacy.toAbsolutePath());
        }
        return rules;
    }

    /**
     * @param claimNames true なら読んだファイル名を {@code loadedNames} に登録（ワールド側）
     * @param onlyIfNameFree false のとき、既に読んだ同名はスキップ（レガシー側）
     */
    private static void appendRulesFromDir(
            Path dir, List<RegenRule> rules, java.util.Set<String> loadedNames, boolean claimNames) {
        try {
            for (Path p : listJsonFilesSorted(dir)) {
                String name = p.getFileName().toString();
                if (!claimNames && loadedNames.contains(name)) {
                    continue;
                }
                try {
                    String json = Files.readString(p, StandardCharsets.UTF_8);
                    JsonObject obj = JsonParser.parseString(json).getAsJsonObject();
                    if (looksLikeAlphaPreset(obj)) {
                        rules.addAll(parseAlphaFile(name, json));
                    } else {
                        RegenRule flat = parseFlatRule(obj);
                        if (flat != null) {
                            rules.add(flat);
                        }
                    }
                    if (claimNames) {
                        loadedNames.add(name);
                    }
                } catch (RuntimeException | IOException ex) {
                    LOGGER.warn("RegenResources: skip broken preset '{}': {}", name, ex.toString());
                }
            }
        } catch (IOException ex) {
            LOGGER.warn("RegenResources: could not list RegenPresets {}: {}", dir, ex.toString());
        }
    }

    private static boolean looksLikeAlphaPreset(JsonObject obj) {
        boolean hasPreset = obj.has("preset") && obj.get("preset").isJsonPrimitive();
        return hasPreset && obj.has("entries") && obj.get("entries").isJsonArray();
    }

    private static @Nullable String blankToNull(@Nullable String s) {
        if (s == null) {
            return null;
        }
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    private static List<RegenRule> parseAlphaFile(String fileNameLabel, String json) {
        PresetFileRoot root = GSON.fromJson(json, PresetFileRoot.class);
        List<RegenRule> out = new ArrayList<>();
        String presetToken = root != null ? blankToNull(root.preset) : null;
        if (root == null || presetToken == null) {
            LOGGER.warn("RegenResources: preset file '{}' has no non-blank 'preset' field", fileNameLabel);
            return out;
        }
        RegenVisual visual = RegenVisual.tryParseToken(presetToken);
        if (visual == null) {
            LOGGER.warn("RegenResources: unknown preset id '{}' in {}", presetToken, fileNameLabel);
            return out;
        }
        if (root.entries == null || root.entries.isEmpty()) {
            return out;
        }
        long rootDefaultDelay = resolveRootDelayTicks(root, "root of '" + fileNameLabel + "'");
        for (PresetEntryJson e : root.entries) {
            long delay = alphaEntryDelayTicks(e, rootDefaultDelay, "entry in '" + fileNameLabel + "'");
            if (delay < 1L) {
                LOGGER.warn(
                        "RegenResources: skip entry with invalid delay (no effective entry_tick/tick/delay_ticks or global_tick) in {}",
                        fileNameLabel);
                continue;
            }
            DimensionRestriction dr = DimensionRestriction.fromJson(e.dimensions, e.dimensionExclusion);
            List<RegenTargetSpec> targets = new ArrayList<>();
            addTargetSpecs(e.targets, targets);
            addTargetSpecs(e.blocks, targets);
            if (targets.isEmpty()) {
                continue;
            }
            RegenCustomVisualSpec custom = null;
            if (visual == RegenVisual.CUSTOM_PRESET || visual == RegenVisual.CUSTOM) {
                custom = parseCustomSpec(fileNameLabel, e.template, e.textures, e.miningSample);
                if (custom == null) {
                    LOGGER.warn("RegenResources: skip custom entry without valid template/textures in {}", fileNameLabel);
                    continue;
                }
            }
            out.add(new RegenRule(delay, visual, dr, List.copyOf(targets), e.naturalRegen, custom));
        }
        return out;
    }

    private static long alphaEntryDelayTicks(PresetEntryJson e, long rootDefaultDelayOrZero, String contextForLog) {
        long inner = resolveEntryDelayTicks(e, contextForLog);
        if (inner > 0L) {
            return inner;
        }
        if (rootDefaultDelayOrZero > 0L) {
            return rootDefaultDelayOrZero;
        }
        return 0L;
    }

    /** global_tick → default_tick → tick → delay_ticks */
    private static long resolveRootDelayTicks(PresetFileRoot root, String contextForLog) {
        return firstPositiveDelay(
                contextForLog,
                new String[] {"global_tick", "default_tick", "tick", "delay_ticks"},
                root.globalTick,
                root.defaultTick,
                root.tick,
                root.delayTicks);
    }

    /** entry_tick → tick → delay_ticks */
    private static long resolveEntryDelayTicks(PresetEntryJson e, String contextForLog) {
        return firstPositiveDelay(
                contextForLog,
                new String[] {"entry_tick", "tick", "delay_ticks"},
                e.entryTick,
                e.tick,
                e.delayTicks);
    }

    private static long firstPositiveDelay(String contextForLog, String[] names, Long... fields) {
        Long chosen = null;
        String chosenName = null;
        for (int i = 0; i < fields.length; i++) {
            Long v = fields[i] != null && fields[i] > 0L ? fields[i] : null;
            if (v == null) {
                continue;
            }
            String name = i < names.length ? names[i] : ("field" + i);
            if (chosen == null) {
                chosen = v;
                chosenName = name;
            } else if (!chosen.equals(v)) {
                LOGGER.warn(
                        "RegenResources: {}: fields '{}' ({}) and '{}' ({}) differ; using '{}'",
                        contextForLog,
                        chosenName,
                        chosen,
                        name,
                        v,
                        chosenName);
            }
        }
        return chosen != null ? chosen : 0L;
    }

    private static long resolveDelayTicksAlias(@Nullable Long delayTicksField, @Nullable Long tickField, String contextForLog) {
        return firstPositiveDelay(
                contextForLog, new String[] {"tick", "delay_ticks"}, tickField, delayTicksField);
    }

    private static @Nullable Long readOptionalPositiveDelayFromJson(JsonObject obj, String key) {
        if (!obj.has(key) || !obj.get(key).isJsonPrimitive()) {
            return null;
        }
        try {
            long v = obj.get(key).getAsLong();
            return v > 0L ? v : null;
        } catch (RuntimeException ex) {
            return null;
        }
    }

    private static List<Path> listJsonFilesSorted(Path dir) throws IOException {
        List<Path> out = new ArrayList<>();
        if (!Files.isDirectory(dir)) {
            return out;
        }
        try (Stream<Path> stream = Files.list(dir)) {
            stream.filter(p -> p.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".json")).forEach(out::add);
        }
        out.sort(Comparator.comparing(p -> p.getFileName().toString()));
        return out;
    }

    private static void ensureVanillaDefaultPresets(Path presetsDir) throws IOException {
        if (!RegenResourcesForgeConfig.BOOTSTRAP_VANILLA_PRESETS_WHEN_EMPTY.getAsBoolean()) {
            return;
        }
        Files.createDirectories(presetsDir);
        int n = 0;
        n += writeDefaultJsonIfAbsent(presetsDir.resolve("stone_preset.json"), GSON_PRETTY.toJson(vanillaStonePreset()));
        n += writeDefaultJsonIfAbsent(presetsDir.resolve("deepslate_preset.json"), GSON_PRETTY.toJson(vanillaDeepslatePreset()));
        n += writeDefaultJsonIfAbsent(presetsDir.resolve("nether_preset.json"), GSON_PRETTY.toJson(vanillaNetherPreset()));
        n += writeDefaultJsonIfAbsent(presetsDir.resolve("end_preset.json"), GSON_PRETTY.toJson(emptyEndPreset()));
        n += writeDefaultJsonIfAbsent(presetsDir.resolve("debris_preset.json"), GSON_PRETTY.toJson(vanillaDebrisPreset()));
        n += writeDefaultJsonIfAbsent(presetsDir.resolve("stripped_log_preset.json"), GSON_PRETTY.toJson(vanillaStrippedLogPreset()));
        if (n > 0) {
            LOGGER.info("RegenResources: wrote {} missing default RegenPresets under {}", n, presetsDir);
        }
    }

    private static int writeDefaultJsonIfAbsent(Path path, String utf8) throws IOException {
        if (Files.exists(path, LinkOption.NOFOLLOW_LINKS)) {
            return 0;
        }
        writeUtf8(path, utf8);
        return 1;
    }

    private static void writeUtf8(Path path, String utf8) throws IOException {
        Files.createDirectories(path.getParent());
        Files.writeString(path, utf8, StandardCharsets.UTF_8);
    }

    private static RegenRule parseFlatRule(JsonObject obj) {
        long delay = firstPositiveDelay(
                "(flat rule)",
                new String[] {"global_tick", "entry_tick", "default_tick", "tick", "delay_ticks"},
                readOptionalPositiveDelayFromJson(obj, "global_tick"),
                readOptionalPositiveDelayFromJson(obj, "entry_tick"),
                readOptionalPositiveDelayFromJson(obj, "default_tick"),
                readOptionalPositiveDelayFromJson(obj, "tick"),
                readOptionalPositiveDelayFromJson(obj, "delay_ticks"));
        if (delay < 1L) {
            delay = 1200L;
        }
        RegenVisual visual = parseFlatVisualToken(obj);
        DimensionRestriction dr = dimensionFromFlat(obj);
        List<RegenTargetSpec> targets = parseTargetsFromJsonArray(obj.get("targets"));
        if (targets.isEmpty()) {
            return null;
        }
        Boolean naturalRegen = obj.has("natural_regen") ? obj.get("natural_regen").getAsBoolean() : null;
        RegenCustomVisualSpec custom = null;
        if (visual == RegenVisual.CUSTOM_PRESET || visual == RegenVisual.CUSTOM) {
            String templateName = obj.has("template") ? obj.get("template").getAsString() : null;
            List<String> textureEntries = new ArrayList<>();
            if (obj.has("textures") && obj.get("textures").isJsonObject()) {
                JsonObject t = obj.getAsJsonObject("textures");
                for (Map.Entry<String, JsonElement> e : t.entrySet()) {
                    if (!e.getValue().isJsonPrimitive()) {
                        continue;
                    }
                    textureEntries.add(e.getKey() + "=" + e.getValue().getAsString());
                }
            }
            String miningSample = obj.has("mining_sample") ? obj.get("mining_sample").getAsString() : null;
            custom = parseCustomSpecFromFlatStrings(templateName, textureEntries, miningSample);
            if (custom == null) {
                LOGGER.warn("RegenResources: skip flat custom rule without valid template/textures");
                return null;
            }
        }
        return new RegenRule(delay, visual, dr, List.copyOf(targets), naturalRegen, custom);
    }

    private static RegenVisual parseFlatVisualToken(JsonObject obj) {
        String raw = "";
        if (obj.has("preset") && obj.get("preset").isJsonPrimitive()) {
            raw = obj.get("preset").getAsString().trim();
        }
        if (raw.isEmpty() && obj.has("visual") && obj.get("visual").isJsonPrimitive()) {
            raw = obj.get("visual").getAsString().trim();
        }
        if (raw.isEmpty()) {
            return RegenVisual.STONE_PRESET;
        }
        try {
            return RegenVisual.valueOf(raw.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ignored) {
            for (RegenVisual vv : RegenVisual.values()) {
                if (vv.getSerializedName().equals(raw)) {
                    return vv;
                }
            }
            return RegenVisual.STONE_PRESET;
        }
    }

    private static @Nullable List<String> targetsFromFlatJson(JsonObject obj) {
        List<String> out = new ArrayList<>();
        appendJsonStringArray(obj, "targets", out);
        return out.isEmpty() ? null : out;
    }

    private static List<RegenTargetSpec> parseTargetsFromJsonArray(@Nullable JsonElement el) {
        List<RegenTargetSpec> out = new ArrayList<>();
        if (el == null || !el.isJsonArray()) {
            return out;
        }
        for (JsonElement item : el.getAsJsonArray()) {
            RegenTargetSpec spec = RegenTargetSpec.parse(item);
            if (spec != null) {
                out.add(spec);
            }
        }
        return out;
    }

    /** Gson が文字列配列として読んだ場合と、手動 JsonArray の両方に対応。 */
    private static void addTargetSpecs(@Nullable List<?> raw, List<RegenTargetSpec> out) {
        if (raw == null) {
            return;
        }
        for (Object o : raw) {
            if (o == null) {
                continue;
            }
            if (o instanceof String s) {
                RegenTargetSpec spec = RegenTargetSpec.parseStringToken(s);
                if (spec != null) {
                    out.add(spec);
                }
            } else if (o instanceof JsonElement je) {
                RegenTargetSpec spec = RegenTargetSpec.parse(je);
                if (spec != null) {
                    out.add(spec);
                }
            } else if (o instanceof Map<?, ?> map) {
                // Gson がオブジェクトを Map にした場合
                JsonObject obj = GSON.toJsonTree(map).getAsJsonObject();
                RegenTargetSpec spec = RegenTargetSpec.parse(obj);
                if (spec != null) {
                    out.add(spec);
                }
            }
        }
    }

    private static void appendJsonStringArray(JsonObject obj, String key, List<String> out) {
        if (!obj.has(key) || !obj.get(key).isJsonArray()) {
            return;
        }
        for (JsonElement e : obj.getAsJsonArray(key)) {
            if (!e.isJsonPrimitive()) {
                continue;
            }
            out.add(e.getAsString());
        }
    }

    private static @Nullable RegenCustomVisualSpec parseCustomSpec(
            String fileNameLabel,
            @Nullable String templateName,
            @Nullable Map<String, String> textures,
            @Nullable String miningSample) {
        RegenTemplate template = RegenTemplate.fromSerializedName(templateName);
        if (template == null) {
            LOGGER.warn("RegenResources: unknown or missing 'template' in {}", fileNameLabel);
            return null;
        }
        LinkedHashMap<String, ResourceLocation> tex = new LinkedHashMap<>();
        if (textures != null) {
            for (Map.Entry<String, String> e : textures.entrySet()) {
                if (e.getValue() == null || e.getValue().isBlank()) {
                    continue;
                }
                if (!template.slots().contains(e.getKey())) {
                    LOGGER.warn(
                            "RegenResources: texture slot '{}' not in template '{}' in {}",
                            e.getKey(),
                            template.getSerializedName(),
                            fileNameLabel);
                    continue;
                }
                ResourceLocation rl = ResourceLocation.tryParse(e.getValue().trim());
                if (rl == null) {
                    LOGGER.warn(
                            "RegenResources: invalid texture id '{}' for slot '{}' in {}",
                            e.getValue(),
                            e.getKey(),
                            fileNameLabel);
                    continue;
                }
                tex.put(e.getKey(), rl);
            }
        }
        if (tex.isEmpty()) {
            LOGGER.warn("RegenResources: no valid textures for template '{}' in {}", template.getSerializedName(), fileNameLabel);
            return null;
        }
        ResourceLocation sample = null;
        if (miningSample != null && !miningSample.isBlank()) {
            sample = ResourceLocation.tryParse(miningSample.trim());
        }
        return new RegenCustomVisualSpec(template, tex, sample);
    }

    private static @Nullable RegenCustomVisualSpec parseCustomSpecFromFlatStrings(
            @Nullable String templateName, List<String> textureEntries, @Nullable String miningSample) {
        LinkedHashMap<String, String> tex = new LinkedHashMap<>();
        for (String s : textureEntries) {
            int idx = s.indexOf('=');
            if (idx < 0) {
                continue;
            }
            tex.put(s.substring(0, idx), s.substring(idx + 1));
        }
        return parseCustomSpec("(flat)", templateName, tex, miningSample);
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
        Boolean exclusionFlag = obj.has("dimension_exclusion") ? obj.get("dimension_exclusion").getAsBoolean() : null;
        return DimensionRestriction.fromJson(dimStrings, exclusionFlag);
    }

    private static PresetEntryJson presetEntry(long tick, String blockId) {
        PresetEntryJson e = new PresetEntryJson();
        e.entryTick = tick;
        e.targets = List.of(blockId);
        return e;
    }

    private static PresetEntryJson presetEntry(long tick, String blockId, List<String> dimensions) {
        PresetEntryJson e = presetEntry(tick, blockId);
        e.dimensions = dimensions;
        return e;
    }

    private static PresetFileRoot vanillaStonePreset() {
        PresetFileRoot r = new PresetFileRoot();
        r.preset = "stone_preset";
        r.entries = new ArrayList<>();
        r.entries.add(presetEntry(1000L, "minecraft:coal_ore"));
        r.entries.add(presetEntry(1500L, "minecraft:copper_ore"));
        r.entries.add(presetEntry(2000L, "minecraft:iron_ore"));
        r.entries.add(presetEntry(2000L, "minecraft:gold_ore"));
        r.entries.add(presetEntry(2000L, "minecraft:lapis_ore"));
        r.entries.add(presetEntry(2000L, "minecraft:redstone_ore"));
        r.entries.add(presetEntry(10000L, "minecraft:diamond_ore"));
        r.entries.add(presetEntry(10000L, "minecraft:emerald_ore"));
        return r;
    }

    private static PresetFileRoot vanillaDeepslatePreset() {
        PresetFileRoot r = new PresetFileRoot();
        r.preset = "deepslate_preset";
        r.entries = new ArrayList<>();
        r.entries.add(presetEntry(1000L, "minecraft:deepslate_coal_ore"));
        r.entries.add(presetEntry(1500L, "minecraft:deepslate_copper_ore"));
        r.entries.add(presetEntry(2000L, "minecraft:deepslate_iron_ore"));
        r.entries.add(presetEntry(2000L, "minecraft:deepslate_gold_ore"));
        r.entries.add(presetEntry(2000L, "minecraft:deepslate_lapis_ore"));
        r.entries.add(presetEntry(2000L, "minecraft:deepslate_redstone_ore"));
        r.entries.add(presetEntry(10000L, "minecraft:deepslate_diamond_ore"));
        r.entries.add(presetEntry(10000L, "minecraft:deepslate_emerald_ore"));
        return r;
    }

    private static PresetFileRoot vanillaNetherPreset() {
        PresetFileRoot r = new PresetFileRoot();
        r.preset = "nether_preset";
        r.entries = new ArrayList<>();
        r.entries.add(presetEntry(1800L, "minecraft:nether_gold_ore", List.of(DIM_NETHER)));
        r.entries.add(presetEntry(2700L, "minecraft:nether_gold_ore", List.of(DIM_OVERWORLD)));
        r.entries.add(presetEntry(1200L, "minecraft:nether_quartz_ore", List.of(DIM_NETHER)));
        r.entries.add(presetEntry(1800L, "minecraft:nether_quartz_ore", List.of(DIM_OVERWORLD)));
        return r;
    }

    private static PresetFileRoot vanillaDebrisPreset() {
        PresetFileRoot r = new PresetFileRoot();
        r.preset = "debris_preset";
        r.entries = new ArrayList<>();
        r.entries.add(presetEntry(16000L, "minecraft:ancient_debris"));
        return r;
    }

    private static PresetFileRoot emptyEndPreset() {
        PresetFileRoot r = new PresetFileRoot();
        r.preset = "end_preset";
        r.entries = new ArrayList<>();
        return r;
    }

    private static PresetFileRoot vanillaStrippedLogPreset() {
        PresetFileRoot r = new PresetFileRoot();
        r.preset = "stripped_log_preset";
        r.entries = new ArrayList<>();
        PresetEntryJson e = new PresetEntryJson();
        e.entryTick = 1000L;
        e.naturalRegen = Boolean.FALSE;
        e.targets = List.of("#minecraft:logs");
        r.entries.add(e);
        return r;
    }

    @SuppressWarnings("unused")
    private static final class PresetFileRoot {
        @SerializedName("preset")
        @Nullable
        String preset;

        @SerializedName("global_tick")
        @Nullable
        Long globalTick;

        @SerializedName("default_tick")
        @Nullable
        Long defaultTick;

        @SerializedName("delay_ticks")
        @Nullable
        Long delayTicks;

        @Nullable
        Long tick;

        List<PresetEntryJson> entries;
    }

    @SuppressWarnings("unused")
    private static final class PresetEntryJson {
        @SerializedName("entry_tick")
        @Nullable
        Long entryTick;

        @Nullable
        Long tick;

        @SerializedName("delay_ticks")
        @Nullable
        Long delayTicks;

        @SerializedName("dimensions")
        @Nullable
        List<String> dimensions;

        @SerializedName("dimension_exclusion")
        @Nullable
        Boolean dimensionExclusion;

        @SerializedName("targets")
        @Nullable
        List<Object> targets;

        /** 旧 NeoForge ブートストラップ互換（1.20.1 は targets のみ） */
        @Nullable
        List<Object> blocks;

        @SerializedName("natural_regen")
        @Nullable
        Boolean naturalRegen;

        @Nullable
        String template;

        @Nullable
        Map<String, String> textures;

        @SerializedName("mining_sample")
        @Nullable
        String miningSample;
    }
}
