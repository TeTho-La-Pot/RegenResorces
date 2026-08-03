package com.github.TeThoLaPot.regen_resources.common.regen;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * 1 ターゲット（ブロック ID またはタグ）と、任意の再生前マッチ／再生後復元プロパティ。
 * <p>JSON は従来どおり文字列 {@code "minecraft:coal_ore"} / {@code "#minecraft:logs"}、
 * または {@code {"id":"...","match":{...},"restore":{...}}} / {@code {"tag":"..."}}。
 */
public final class RegenTargetSpec {

    private final boolean tag;
    private final ResourceLocation id;
    /** プロパティ名 → 許可する値名（いずれか）。空なら状態条件なし。 */
    private final Map<String, Set<String>> matchProperties;
    /** プロパティ名 → 復元値。空なら破壊時状態のまま。 */
    private final Map<String, String> restoreProperties;

    public RegenTargetSpec(
            boolean tag,
            ResourceLocation id,
            Map<String, Set<String>> matchProperties,
            Map<String, String> restoreProperties) {
        this.tag = tag;
        this.id = id;
        this.matchProperties = freezeMatch(matchProperties);
        this.restoreProperties = freezeRestore(restoreProperties);
    }

    public static RegenTargetSpec block(ResourceLocation id) {
        return new RegenTargetSpec(false, id, Map.of(), Map.of());
    }

    public static RegenTargetSpec ofTag(ResourceLocation id) {
        return new RegenTargetSpec(true, id, Map.of(), Map.of());
    }

    public boolean isTag() {
        return tag;
    }

    public ResourceLocation id() {
        return id;
    }

    public Map<String, Set<String>> matchProperties() {
        return matchProperties;
    }

    public Map<String, String> restoreProperties() {
        return restoreProperties;
    }

    public RegenTargetSpec withMatch(Map<String, Set<String>> match) {
        return new RegenTargetSpec(tag, id, match, restoreProperties);
    }

    public RegenTargetSpec withRestore(Map<String, String> restore) {
        return new RegenTargetSpec(tag, id, matchProperties, restore);
    }

    public boolean matches(BlockState state) {
        if (state == null || state.isAir()) {
            return false;
        }
        if (tag) {
            if (!state.is(TagKey.create(Registries.BLOCK, id))) {
                return false;
            }
        } else {
            ResourceLocation blockId = BuiltInRegistries.BLOCK.getKey(state.getBlock());
            if (!id.equals(blockId)) {
                return false;
            }
        }
        return propertiesMatch(state, matchProperties);
    }

    /** 破壊時状態に restore を適用した状態を返す（restore 空ならそのまま）。 */
    public BlockState applyRestore(BlockState broken) {
        if (broken == null || restoreProperties.isEmpty()) {
            return broken;
        }
        BlockState out = broken;
        for (Map.Entry<String, String> e : restoreProperties.entrySet()) {
            out = setPropertyByName(out, e.getKey(), e.getValue());
        }
        return out;
    }

    public JsonElement toJson() {
        boolean simple = matchProperties.isEmpty() && restoreProperties.isEmpty();
        if (simple) {
            return new JsonPrimitive(tag ? "#" + id : id.toString());
        }
        JsonObject obj = new JsonObject();
        if (tag) {
            obj.addProperty("tag", id.toString());
        } else {
            obj.addProperty("id", id.toString());
        }
        if (!matchProperties.isEmpty()) {
            obj.add("match", matchToJson(matchProperties));
        }
        if (!restoreProperties.isEmpty()) {
            obj.add("restore", restoreToJson(restoreProperties));
        }
        return obj;
    }

    public static @Nullable RegenTargetSpec parse(@Nullable JsonElement el) {
        if (el == null || el.isJsonNull()) {
            return null;
        }
        if (el.isJsonPrimitive() && el.getAsJsonPrimitive().isString()) {
            return parseStringToken(el.getAsString());
        }
        if (!el.isJsonObject()) {
            return null;
        }
        JsonObject obj = el.getAsJsonObject();
        boolean isTag;
        ResourceLocation rl;
        if (obj.has("tag") && obj.get("tag").isJsonPrimitive()) {
            isTag = true;
            rl = ResourceLocation.tryParse(obj.get("tag").getAsString().trim());
        } else if (obj.has("id") && obj.get("id").isJsonPrimitive()) {
            isTag = false;
            String raw = obj.get("id").getAsString().trim();
            if (raw.startsWith("#")) {
                isTag = true;
                rl = ResourceLocation.tryParse(raw.substring(1));
            } else {
                rl = ResourceLocation.tryParse(raw);
            }
        } else {
            return null;
        }
        if (rl == null) {
            return null;
        }
        Map<String, Set<String>> match = parseMatch(obj.get("match"));
        Map<String, String> restore = parseRestore(obj.get("restore"));
        return new RegenTargetSpec(isTag, rl, match, restore);
    }

    public static @Nullable RegenTargetSpec parseStringToken(@Nullable String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        String t = raw.trim();
        if (t.startsWith("#")) {
            ResourceLocation rl = ResourceLocation.tryParse(t.substring(1));
            return rl == null ? null : ofTag(rl);
        }
        ResourceLocation rl = ResourceLocation.tryParse(t);
        return rl == null ? null : block(rl);
    }

    public String displayId() {
        return tag ? "#" + id : id.toString();
    }

    private static boolean propertiesMatch(BlockState state, Map<String, Set<String>> required) {
        if (required.isEmpty()) {
            return true;
        }
        for (Map.Entry<String, Set<String>> e : required.entrySet()) {
            Property<?> prop = findProperty(state, e.getKey());
            if (prop == null) {
                return false;
            }
            String valueName = getValueName(state, prop);
            if (!e.getValue().contains(valueName)) {
                return false;
            }
        }
        return true;
    }

    private static @Nullable Property<?> findProperty(BlockState state, String name) {
        for (Property<?> p : state.getProperties()) {
            if (p.getName().equals(name)) {
                return p;
            }
        }
        return null;
    }

    private static <T extends Comparable<T>> String getValueName(BlockState state, Property<T> prop) {
        return prop.getName(state.getValue(prop));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static BlockState setPropertyByName(BlockState state, String propName, String valueName) {
        Property<?> prop = findProperty(state, propName);
        if (prop == null) {
            return state;
        }
        Optional<?> parsed = prop.getValue(valueName);
        if (parsed.isEmpty()) {
            return state;
        }
        return state.setValue((Property) prop, (Comparable) parsed.get());
    }

    private static Map<String, Set<String>> parseMatch(@Nullable JsonElement el) {
        if (el == null || !el.isJsonObject()) {
            return Map.of();
        }
        Map<String, Set<String>> out = new LinkedHashMap<>();
        for (Map.Entry<String, JsonElement> e : el.getAsJsonObject().entrySet()) {
            Set<String> values = new LinkedHashSet<>();
            JsonElement v = e.getValue();
            if (v.isJsonArray()) {
                for (JsonElement item : v.getAsJsonArray()) {
                    if (item.isJsonPrimitive()) {
                        values.add(item.getAsString());
                    }
                }
            } else if (v.isJsonPrimitive()) {
                values.add(v.getAsString());
            }
            if (!values.isEmpty()) {
                out.put(e.getKey(), Collections.unmodifiableSet(values));
            }
        }
        return Collections.unmodifiableMap(out);
    }

    private static Map<String, String> parseRestore(@Nullable JsonElement el) {
        if (el == null || !el.isJsonObject()) {
            return Map.of();
        }
        Map<String, String> out = new LinkedHashMap<>();
        for (Map.Entry<String, JsonElement> e : el.getAsJsonObject().entrySet()) {
            if (e.getValue().isJsonPrimitive()) {
                out.put(e.getKey(), e.getValue().getAsString());
            }
        }
        return Collections.unmodifiableMap(out);
    }

    private static JsonObject matchToJson(Map<String, Set<String>> match) {
        JsonObject obj = new JsonObject();
        for (Map.Entry<String, Set<String>> e : match.entrySet()) {
            JsonArray arr = new JsonArray();
            for (String v : e.getValue()) {
                arr.add(v);
            }
            obj.add(e.getKey(), arr);
        }
        return obj;
    }

    private static JsonObject restoreToJson(Map<String, String> restore) {
        JsonObject obj = new JsonObject();
        for (Map.Entry<String, String> e : restore.entrySet()) {
            obj.addProperty(e.getKey(), e.getValue());
        }
        return obj;
    }

    private static Map<String, Set<String>> freezeMatch(Map<String, Set<String>> in) {
        if (in == null || in.isEmpty()) {
            return Map.of();
        }
        Map<String, Set<String>> out = new LinkedHashMap<>();
        for (Map.Entry<String, Set<String>> e : in.entrySet()) {
            out.put(e.getKey(), Collections.unmodifiableSet(new LinkedHashSet<>(e.getValue())));
        }
        return Collections.unmodifiableMap(out);
    }

    private static Map<String, String> freezeRestore(Map<String, String> in) {
        if (in == null || in.isEmpty()) {
            return Map.of();
        }
        return Collections.unmodifiableMap(new LinkedHashMap<>(in));
    }
}
