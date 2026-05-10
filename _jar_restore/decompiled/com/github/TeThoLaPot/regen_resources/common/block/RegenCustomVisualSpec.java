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
        textures = textures == null ? Collections.emptyMap() : Collections.unmodifiableMap(new LinkedHashMap<String, ResourceLocation>(textures));
    }

    @Nullable
    public ResourceLocation textureFor(String slot) {
        return this.textures.get(slot);
    }

    public CompoundTag writeNbt() {
        CompoundTag tag = new CompoundTag();
        tag.m_128359_(TAG_TEMPLATE, this.template.m_7912_());
        CompoundTag tex = new CompoundTag();
        for (Map.Entry<String, ResourceLocation> e : this.textures.entrySet()) {
            if (e.getValue() == null) continue;
            tex.m_128359_(e.getKey(), e.getValue().toString());
        }
        tag.m_128365_(TAG_TEXTURES, (Tag)tex);
        if (this.miningSampleBlockId != null) {
            tag.m_128359_(TAG_MINING_SAMPLE, this.miningSampleBlockId.toString());
        }
        return tag;
    }

    @Nullable
    public static RegenCustomVisualSpec readNbt(@Nullable CompoundTag tag) {
        if (tag == null || tag.m_128456_()) {
            return null;
        }
        if (!tag.m_128425_(TAG_TEMPLATE, 8)) {
            return null;
        }
        RegenTemplate template = RegenTemplate.fromSerializedName(tag.m_128461_(TAG_TEMPLATE));
        if (template == null) {
            return null;
        }
        LinkedHashMap<String, ResourceLocation> tex = new LinkedHashMap<String, ResourceLocation>();
        if (tag.m_128425_(TAG_TEXTURES, 10)) {
            CompoundTag texTag = tag.m_128469_(TAG_TEXTURES);
            for (String key : texTag.m_128431_()) {
                ResourceLocation rl;
                if (!texTag.m_128425_(key, 8) || (rl = ResourceLocation.m_135820_((String)texTag.m_128461_(key))) == null) continue;
                tex.put(key, rl);
            }
        }
        ResourceLocation miningSample = null;
        if (tag.m_128425_(TAG_MINING_SAMPLE, 8)) {
            miningSample = ResourceLocation.m_135820_((String)tag.m_128461_(TAG_MINING_SAMPLE));
        }
        return new RegenCustomVisualSpec(template, tex, miningSample);
    }

    public void writeBuf(FriendlyByteBuf buf) {
        buf.m_130070_(this.template.m_7912_());
        buf.m_130130_(this.textures.size());
        for (Map.Entry<String, ResourceLocation> e : this.textures.entrySet()) {
            buf.m_130070_(e.getKey());
            buf.m_130085_(e.getValue());
        }
        boolean hasSample = this.miningSampleBlockId != null;
        buf.writeBoolean(hasSample);
        if (hasSample) {
            buf.m_130085_(this.miningSampleBlockId);
        }
    }

    @Nullable
    public static RegenCustomVisualSpec readBuf(FriendlyByteBuf buf) {
        ResourceLocation miningSample;
        String tplName = buf.m_130277_();
        RegenTemplate template = RegenTemplate.fromSerializedName(tplName);
        int n = buf.m_130242_();
        LinkedHashMap<String, ResourceLocation> tex = new LinkedHashMap<String, ResourceLocation>();
        for (int i = 0; i < n; ++i) {
            String slot = buf.m_130277_();
            ResourceLocation rl = buf.m_130281_();
            tex.put(slot, rl);
        }
        boolean hasSample = buf.readBoolean();
        ResourceLocation resourceLocation = miningSample = hasSample ? buf.m_130281_() : null;
        if (template == null) {
            return null;
        }
        return new RegenCustomVisualSpec(template, tex, miningSample);
    }
}

