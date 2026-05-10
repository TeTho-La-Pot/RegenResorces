/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.Tag
 *  net.minecraft.network.FriendlyByteBuf
 *  net.minecraft.resources.ResourceLocation
 *  org.jetbrains.annotations.Nullable
 */
package com.github.TeThoLaPot.regen_resources.common.block;

import com.github.TeThoLaPot.regen_resources.common.block.RegenTemplate;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public record RegenCustomVisualSpec(RegenTemplate template, Map<String, ResourceLocation> textures, @Nullable ResourceLocation miningSampleBlockId) {
    private static final String TAG_TEMPLATE = "template";
    private static final String TAG_TEXTURES = "textures";
    private static final String TAG_MINING_SAMPLE = "mining_sample";

    public RegenCustomVisualSpec(RegenTemplate template, Map<String, ResourceLocation> textures, @Nullable ResourceLocation miningSampleBlockId) {
        if (template == null) {
            throw new IllegalArgumentException("template must not be null");
        }
        this.template = template;
        this.textures = textures == null ? Collections.emptyMap() : Collections.unmodifiableMap(new LinkedHashMap<>(textures));
        this.miningSampleBlockId = miningSampleBlockId;
    }

    @Nullable
    public ResourceLocation textureFor(String slot) {
        return this.textures.get(slot);
    }

    public CompoundTag writeNbt() {
        CompoundTag tag = new CompoundTag();
        tag.putString(TAG_TEMPLATE, this.template.getSerializedName());
        CompoundTag tex = new CompoundTag();
        for (Map.Entry<String, ResourceLocation> e : this.textures.entrySet()) {
            if (e.getValue() == null) continue;
            tex.putString(e.getKey(), e.getValue().toString());
        }
        tag.put(TAG_TEXTURES, (Tag)tex);
        if (this.miningSampleBlockId != null) {
            tag.putString(TAG_MINING_SAMPLE, this.miningSampleBlockId.toString());
        }
        return tag;
    }

    @Nullable
    public static RegenCustomVisualSpec readNbt(@Nullable CompoundTag tag) {
        if (tag == null || tag.isEmpty()) {
            return null;
        }
        if (!tag.contains(TAG_TEMPLATE, 8)) {
            return null;
        }
        RegenTemplate template = RegenTemplate.fromSerializedName(tag.getString(TAG_TEMPLATE));
        if (template == null) {
            return null;
        }
        LinkedHashMap<String, ResourceLocation> tex = new LinkedHashMap<String, ResourceLocation>();
        if (tag.contains(TAG_TEXTURES, 10)) {
            CompoundTag texTag = tag.getCompound(TAG_TEXTURES);
            for (String key : texTag.getAllKeys()) {
                ResourceLocation rl;
                if (!texTag.contains(key, 8) || (rl = ResourceLocation.tryParse((String)texTag.getString(key))) == null) continue;
                tex.put(key, rl);
            }
        }
        ResourceLocation miningSample = null;
        if (tag.contains(TAG_MINING_SAMPLE, 8)) {
            miningSample = ResourceLocation.tryParse((String)tag.getString(TAG_MINING_SAMPLE));
        }
        return new RegenCustomVisualSpec(template, tex, miningSample);
    }

    public void writeBuf(FriendlyByteBuf buf) {
        buf.writeUtf(this.template.getSerializedName());
        buf.writeVarInt(this.textures.size());
        for (Map.Entry<String, ResourceLocation> e : this.textures.entrySet()) {
            buf.writeUtf(e.getKey());
            buf.writeResourceLocation(e.getValue());
        }
        boolean hasSample = this.miningSampleBlockId != null;
        buf.writeBoolean(hasSample);
        if (hasSample) {
            buf.writeResourceLocation(this.miningSampleBlockId);
        }
    }

    @Nullable
    public static RegenCustomVisualSpec readBuf(FriendlyByteBuf buf) {
        ResourceLocation miningSample;
        String tplName = buf.readUtf();
        RegenTemplate template = RegenTemplate.fromSerializedName(tplName);
        int n = buf.readVarInt();
        LinkedHashMap<String, ResourceLocation> tex = new LinkedHashMap<String, ResourceLocation>();
        for (int i = 0; i < n; ++i) {
            String slot = buf.readUtf();
            ResourceLocation rl = buf.readResourceLocation();
            tex.put(slot, rl);
        }
        boolean hasSample = buf.readBoolean();
        ResourceLocation resourceLocation = miningSample = hasSample ? buf.readResourceLocation() : null;
        if (template == null) {
            return null;
        }
        return new RegenCustomVisualSpec(template, tex, miningSample);
    }
}

