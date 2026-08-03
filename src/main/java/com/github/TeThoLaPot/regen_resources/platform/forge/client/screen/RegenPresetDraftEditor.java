package com.github.TeThoLaPot.regen_resources.platform.forge.client.screen;

import com.github.TeThoLaPot.regen_resources.common.regen.RegenTargetSpec;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** 設定 UI 用のプリセット JSON 下書き操作。 */
public final class RegenPresetDraftEditor {

    public enum RootMode {
        /** ルートに global_tick（互換: tick / delay_ticks）があるモード */
        GLOBAL_TICK,
        ENTRIES;

        public String token() {
            return this == GLOBAL_TICK ? "global_tick" : "entries";
        }

        public static RootMode fromToken(String s) {
            if (s != null && s.equalsIgnoreCase("entries")) {
                return ENTRIES;
            }
            return GLOBAL_TICK;
        }
    }

    private RegenPresetDraftEditor() {}

    public static JsonObject parse(String json) {
        try {
            return JsonParser.parseString(json).getAsJsonObject();
        } catch (RuntimeException ex) {
            JsonObject fallback = new JsonObject();
            fallback.addProperty("preset", "stone_preset");
            fallback.add("entries", new JsonArray());
            return fallback;
        }
    }

    public static RootMode detectMode(JsonObject root) {
        if (root.has("global_tick")
                || root.has("default_tick")
                || root.has("tick")
                || root.has("delay_ticks")) {
            return RootMode.GLOBAL_TICK;
        }
        return RootMode.ENTRIES;
    }

    public static long readRootTick(JsonObject root) {
        Long v = readPositiveLong(root, "global_tick");
        if (v != null) {
            return v;
        }
        v = readPositiveLong(root, "default_tick");
        if (v != null) {
            return v;
        }
        v = readPositiveLong(root, "tick");
        if (v != null) {
            return v;
        }
        v = readPositiveLong(root, "delay_ticks");
        if (v != null) {
            return v;
        }
        return 1200L;
    }

    public static void applyRootMode(JsonObject root, RootMode mode, long tickValue) {
        clearRootTickKeys(root);
        if (mode == RootMode.GLOBAL_TICK) {
            root.addProperty("global_tick", Math.max(1L, tickValue));
        }
        if (!root.has("entries") || !root.get("entries").isJsonArray()) {
            root.add("entries", new JsonArray());
        }
        if (mode == RootMode.ENTRIES) {
            ensureEntriesHaveTick(root, 1200L);
        }
    }

    /** entries モードでは各 entry に entry_tick を必ず持たせる。 */
    public static void ensureEntriesHaveTick(JsonObject root, long fallback) {
        for (JsonElement el : entries(root)) {
            if (!el.isJsonObject()) {
                continue;
            }
            JsonObject e = el.getAsJsonObject();
            if (!hasOwnEntryTick(e)) {
                setEntryTick(e, fallback);
            }
        }
    }

    public static JsonObject newEntry(RootMode mode, long defaultTick) {
        JsonObject e = new JsonObject();
        ensureTargetsKey(e);
        if (mode == RootMode.ENTRIES) {
            setEntryTick(e, defaultTick);
        }
        return e;
    }

    /** 全 entry に targets キーを必ず持たせる。 */
    public static void ensureEntriesHaveTargets(JsonObject root) {
        for (JsonElement el : entries(root)) {
            if (el.isJsonObject()) {
                ensureTargetsKey(el.getAsJsonObject());
            }
        }
    }

    private static void clearRootTickKeys(JsonObject root) {
        root.remove("global_tick");
        root.remove("default_tick");
        root.remove("tick");
        root.remove("delay_ticks");
    }

    public static JsonArray entries(JsonObject root) {
        if (!root.has("entries") || !root.get("entries").isJsonArray()) {
            JsonArray a = new JsonArray();
            root.add("entries", a);
            return a;
        }
        return root.getAsJsonArray("entries");
    }

    public static JsonObject entryAt(JsonObject root, int index) {
        JsonArray arr = entries(root);
        while (arr.size() <= index) {
            arr.add(new JsonObject());
        }
        return arr.get(index).getAsJsonObject();
    }

    public static void removeEntry(JsonObject root, int index) {
        JsonArray arr = entries(root);
        if (index >= 0 && index < arr.size()) {
            arr.remove(index);
        }
    }

    public static boolean hasOwnEntryTick(JsonObject entry) {
        return readPositiveLong(entry, "entry_tick") != null
                || readPositiveLong(entry, "tick") != null
                || readPositiveLong(entry, "delay_ticks") != null;
    }

    public static long readEntryTick(JsonObject entry, long fallback) {
        Long v = readPositiveLong(entry, "entry_tick");
        if (v != null) {
            return v;
        }
        v = readPositiveLong(entry, "tick");
        if (v != null) {
            return v;
        }
        v = readPositiveLong(entry, "delay_ticks");
        if (v != null) {
            return v;
        }
        return Math.max(1L, fallback);
    }

    public static void setEntryTick(JsonObject entry, long tick) {
        entry.remove("tick");
        entry.remove("delay_ticks");
        entry.addProperty("entry_tick", Math.max(1L, tick));
    }

    public static void clearEntryTick(JsonObject entry) {
        entry.remove("entry_tick");
        entry.remove("tick");
        entry.remove("delay_ticks");
    }

    public static boolean hasTargetsKey(JsonObject entry) {
        return entry.has("targets");
    }

    public static void ensureTargetsKey(JsonObject entry) {
        if (!entry.has("targets") || !entry.get("targets").isJsonArray()) {
            entry.add("targets", new JsonArray());
        }
    }

    public static void clearTargetsKey(JsonObject entry) {
        entry.remove("targets");
    }

    public static boolean hasDimensionsKey(JsonObject entry) {
        return entry.has("dimensions") || entry.has("dimension_exclusion");
    }

    public static void ensureDimensionsKey(JsonObject entry) {
        if (!entry.has("dimensions") || !entry.get("dimensions").isJsonArray()) {
            entry.add("dimensions", new JsonArray());
        }
        if (!entry.has("dimension_exclusion")) {
            entry.addProperty("dimension_exclusion", false);
        }
    }

    public static void clearDimensionsKey(JsonObject entry) {
        entry.remove("dimensions");
        entry.remove("dimension_exclusion");
    }

    public static String readDimensionsText(JsonObject entry) {
        return String.join(" ", readDimensionsList(entry));
    }

    public static List<String> readDimensionsList(JsonObject entry) {
        List<String> out = new ArrayList<>();
        if (!entry.has("dimensions") || !entry.get("dimensions").isJsonArray()) {
            return out;
        }
        for (JsonElement el : entry.getAsJsonArray("dimensions")) {
            if (!el.isJsonPrimitive()) {
                continue;
            }
            String s = el.getAsString().trim();
            if (!s.isEmpty()) {
                out.add(s);
            }
        }
        return out;
    }

    public static void writeDimensionsList(JsonObject entry, List<String> dims) {
        JsonArray arr = new JsonArray();
        if (dims != null) {
            for (String d : dims) {
                if (d != null && !d.isBlank()) {
                    arr.add(d.trim());
                }
            }
        }
        entry.add("dimensions", arr);
    }

    public static void writeDimensionsText(JsonObject entry, String text) {
        List<String> dims = new ArrayList<>();
        if (text != null) {
            for (String part : text.trim().split("[\\s,]+")) {
                String t = part.trim();
                if (!t.isEmpty()) {
                    dims.add(t);
                }
            }
        }
        writeDimensionsList(entry, dims);
    }

    public static boolean readDimensionExclusion(JsonObject entry) {
        if (!entry.has("dimension_exclusion") || !entry.get("dimension_exclusion").isJsonPrimitive()) {
            return false;
        }
        try {
            return entry.get("dimension_exclusion").getAsBoolean();
        } catch (RuntimeException ignored) {
            return false;
        }
    }

    public static void setDimensionExclusion(JsonObject entry, boolean exclusion) {
        entry.addProperty("dimension_exclusion", exclusion);
    }

    public static boolean hasNaturalRegenKey(JsonObject entry) {
        return entry.has("natural_regen");
    }

    public static void ensureNaturalRegenKey(JsonObject entry) {
        if (!entry.has("natural_regen")) {
            entry.addProperty("natural_regen", false);
        }
    }

    public static void clearNaturalRegenKey(JsonObject entry) {
        entry.remove("natural_regen");
    }

    public static boolean readNaturalRegen(JsonObject entry) {
        if (!entry.has("natural_regen") || !entry.get("natural_regen").isJsonPrimitive()) {
            return false;
        }
        try {
            return entry.get("natural_regen").getAsBoolean();
        } catch (RuntimeException ignored) {
            return false;
        }
    }

    public static void setNaturalRegen(JsonObject entry, boolean value) {
        entry.addProperty("natural_regen", value);
    }

    public static boolean hasMiningSampleKey(JsonObject entry) {
        return entry.has("mining_sample");
    }

    public static void ensureMiningSampleKey(JsonObject entry) {
        if (!entry.has("mining_sample")) {
            entry.addProperty("mining_sample", "");
        }
    }

    public static void clearMiningSampleKey(JsonObject entry) {
        entry.remove("mining_sample");
    }

    public static String readMiningSample(JsonObject entry) {
        if (!entry.has("mining_sample") || !entry.get("mining_sample").isJsonPrimitive()) {
            return "";
        }
        try {
            return entry.get("mining_sample").getAsString();
        } catch (RuntimeException ignored) {
            return "";
        }
    }

    public static void setMiningSample(JsonObject entry, String value) {
        entry.addProperty("mining_sample", value == null ? "" : value);
    }

    public static boolean hasTemplateKey(JsonObject entry) {
        return entry.has("template");
    }

    public static void ensureTemplateKey(JsonObject entry) {
        if (!entry.has("template")) {
            entry.addProperty("template", "");
        }
        ensureTexturesKey(entry);
    }

    public static void clearTemplateKey(JsonObject entry) {
        entry.remove("template");
        clearTexturesKey(entry);
    }

    public static String readTemplate(JsonObject entry) {
        if (!entry.has("template") || !entry.get("template").isJsonPrimitive()) {
            return "";
        }
        try {
            return entry.get("template").getAsString();
        } catch (RuntimeException ignored) {
            return "";
        }
    }

    public static void setTemplate(JsonObject entry, String value) {
        entry.addProperty("template", value == null ? "" : value);
    }

    public static boolean hasTexturesKey(JsonObject entry) {
        return entry.has("textures");
    }

    public static void ensureTexturesKey(JsonObject entry) {
        if (!entry.has("textures") || !entry.get("textures").isJsonObject()) {
            entry.add("textures", new JsonObject());
        }
    }

    public static void clearTexturesKey(JsonObject entry) {
        entry.remove("textures");
    }

    /** slot → texture id（空文字は未設定）。不正キーは除外。 */
    public static Map<String, String> readTextures(JsonObject entry) {
        Map<String, String> out = new LinkedHashMap<>();
        if (!entry.has("textures") || !entry.get("textures").isJsonObject()) {
            return out;
        }
        JsonObject tex = entry.getAsJsonObject("textures");
        for (Map.Entry<String, JsonElement> e : tex.entrySet()) {
            if (e.getKey() == null || e.getKey().isBlank()) {
                continue;
            }
            if (e.getValue() == null || !e.getValue().isJsonPrimitive()) {
                continue;
            }
            try {
                out.put(e.getKey(), e.getValue().getAsString());
            } catch (RuntimeException ignored) {
            }
        }
        return out;
    }

    public static void writeTextures(JsonObject entry, Map<String, String> textures) {
        JsonObject tex = new JsonObject();
        if (textures != null) {
            for (Map.Entry<String, String> e : textures.entrySet()) {
                if (e.getKey() == null || e.getKey().isBlank()) {
                    continue;
                }
                String v = e.getValue();
                if (v != null && !v.isBlank()) {
                    tex.addProperty(e.getKey(), v);
                }
            }
        }
        entry.add("textures", tex);
    }

    public static void setTextureSlot(JsonObject entry, String slot, @Nullable String textureId) {
        ensureTexturesKey(entry);
        JsonObject tex = entry.getAsJsonObject("textures");
        if (textureId == null || textureId.isBlank()) {
            tex.remove(slot);
        } else {
            tex.addProperty(slot, textureId);
        }
    }

    private static @Nullable Long readPositiveLong(JsonObject obj, String key) {
        if (!obj.has(key) || !obj.get(key).isJsonPrimitive()) {
            return null;
        }
        try {
            long v = obj.get(key).getAsLong();
            return v > 0L ? Math.max(1L, v) : null;
        } catch (RuntimeException ignored) {
            return null;
        }
    }

    public static List<RegenTargetSpec> readTargets(JsonObject entry) {
        List<RegenTargetSpec> out = new ArrayList<>();
        if (!entry.has("targets") || !entry.get("targets").isJsonArray()) {
            return out;
        }
        for (JsonElement el : entry.getAsJsonArray("targets")) {
            RegenTargetSpec spec = RegenTargetSpec.parse(el);
            if (spec != null) {
                out.add(spec);
            }
        }
        return out;
    }

    /** プリセット内の全 entry の targets をまとめて返す。 */
    public static List<RegenTargetSpec> readAllTargets(JsonObject root) {
        List<RegenTargetSpec> out = new ArrayList<>();
        if (root == null) {
            return out;
        }
        for (JsonElement el : entries(root)) {
            if (el.isJsonObject()) {
                out.addAll(readTargets(el.getAsJsonObject()));
            }
        }
        return out;
    }

    public static void writeTargets(JsonObject entry, List<RegenTargetSpec> targets) {
        JsonArray arr = new JsonArray();
        for (RegenTargetSpec t : targets) {
            if (t != null) {
                arr.add(t.toJson());
            }
        }
        entry.add("targets", arr);
    }

    public static int targetCount(JsonObject entry) {
        return readTargets(entry).size();
    }

    public static String summarizeTargets(@Nullable JsonObject entry) {
        if (entry == null) {
            return "0";
        }
        return Integer.toString(targetCount(entry));
    }
}
