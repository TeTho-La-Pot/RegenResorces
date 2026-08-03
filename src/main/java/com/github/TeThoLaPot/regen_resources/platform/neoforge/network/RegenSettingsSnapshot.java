package com.github.TeThoLaPot.regen_resources.platform.neoforge.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import com.github.TeThoLaPot.regen_resources.platform.neoforge.config.RegenPresetIo;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** 設定 UI 用のプリセットファイルスナップショット。 */
public record RegenSettingsSnapshot(List<PresetFile> files) {

    public record PresetFile(String name, String json) {
        public static final StreamCodec<FriendlyByteBuf, PresetFile> STREAM_CODEC =
                StreamCodec.composite(
                        ByteBufCodecs.STRING_UTF8,
                        PresetFile::name,
                        ByteBufCodecs.STRING_UTF8,
                        PresetFile::json,
                        PresetFile::new);
    }

    public static final StreamCodec<FriendlyByteBuf, RegenSettingsSnapshot> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.collection(ArrayList::new, PresetFile.STREAM_CODEC),
                    RegenSettingsSnapshot::files,
                    RegenSettingsSnapshot::new);

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
