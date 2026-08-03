package com.github.TeThoLaPot.regen_resources.platform.forge.network;

import com.github.TeThoLaPot.regen_resources.platform.forge.config.RegenPresetIo;
import net.minecraft.network.FriendlyByteBuf;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** 設定 UI 用のプリセットファイルスナップショット。 */
public record RegenSettingsSnapshot(List<PresetFile> files) {

    public record PresetFile(String name, String json) {
        public static void encode(PresetFile file, FriendlyByteBuf buf) {
            buf.writeUtf(file.name());
            buf.writeUtf(file.json(), MAX_JSON_LENGTH);
        }

        public static PresetFile decode(FriendlyByteBuf buf) {
            String name = buf.readUtf();
            String json = buf.readUtf(MAX_JSON_LENGTH);
            return new PresetFile(name, json);
        }
    }

    /** プリセット JSON 1 件あたりの上限（既定の 32767 では設定量が増えると溢れる）。 */
    private static final int MAX_JSON_LENGTH = 1_048_576;

    public static void encode(RegenSettingsSnapshot snapshot, FriendlyByteBuf buf) {
        encodeFiles(snapshot.files(), buf);
    }

    public static RegenSettingsSnapshot decode(FriendlyByteBuf buf) {
        return new RegenSettingsSnapshot(decodeFiles(buf));
    }

    public static void encodeFiles(List<PresetFile> files, FriendlyByteBuf buf) {
        buf.writeVarInt(files.size());
        for (PresetFile file : files) {
            PresetFile.encode(file, buf);
        }
    }

    public static List<PresetFile> decodeFiles(FriendlyByteBuf buf) {
        int size = buf.readVarInt();
        List<PresetFile> out = new ArrayList<>(Math.min(size, 64));
        for (int i = 0; i < size; i++) {
            out.add(PresetFile.decode(buf));
        }
        return out;
    }

    public Map<String, String> asMap() {
        LinkedHashMap<String, String> map = new LinkedHashMap<>();
        for (PresetFile f : files) {
            map.put(f.name(), f.json());
        }
        return map;
    }

    public static RegenSettingsSnapshot fromDisk() {
        List<PresetFile> list = new ArrayList<>();
        for (String name : RegenPresetIo.listPresetFileNames()) {
            String json = RegenPresetIo.readPresetJson(name);
            if (json != null) {
                list.add(new PresetFile(name, json));
            }
        }
        return new RegenSettingsSnapshot(List.copyOf(list));
    }
}
