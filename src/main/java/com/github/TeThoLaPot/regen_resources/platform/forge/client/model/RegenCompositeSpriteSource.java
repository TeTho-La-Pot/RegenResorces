/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.platform.NativeImage
 *  com.mojang.blaze3d.platform.NativeImage$Format
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.Codec
 *  net.minecraft.client.renderer.texture.SpriteContents
 *  net.minecraft.client.renderer.texture.atlas.SpriteSource
 *  net.minecraft.client.renderer.texture.atlas.SpriteSource$Output
 *  net.minecraft.client.renderer.texture.atlas.SpriteSource$SpriteSupplier
 *  net.minecraft.client.renderer.texture.atlas.SpriteSourceType
 *  net.minecraft.client.resources.metadata.animation.AnimationMetadataSection
 *  net.minecraft.client.resources.metadata.animation.FrameSize
 *  net.minecraft.core.registries.BuiltInRegistries
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.server.packs.resources.Resource
 *  net.minecraft.server.packs.resources.ResourceManager
 *  net.minecraft.world.level.block.Block
 *  org.slf4j.Logger
 */
package com.github.TeThoLaPot.regen_resources.platform.forge.client.model;

import com.github.TeThoLaPot.regen_resources.common.block.RegenStrippedLogResolver;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.atlas.SpriteSource;
import net.minecraft.client.renderer.texture.atlas.SpriteSourceType;
import net.minecraft.client.resources.metadata.animation.AnimationMetadataSection;
import net.minecraft.client.resources.metadata.animation.FrameSize;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.level.block.Block;
import org.slf4j.Logger;

public final class RegenCompositeSpriteSource
implements SpriteSource {
    public static final RegenCompositeSpriteSource INSTANCE = new RegenCompositeSpriteSource();
    public static final Codec<RegenCompositeSpriteSource> CODEC = Codec.unit(INSTANCE);
    public static final SpriteSourceType TYPE = new SpriteSourceType(CODEC);
    public static final String COMPOSITE_PREFIX = "block/regen_combined/";
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final ResourceLocation MASK_TEXTURE = ResourceLocation.fromNamespaceAndPath((String)"regen_resources", (String)"textures/regen_mask/log.png");

    public static ResourceLocation sideSpriteId(ResourceLocation strippedId) {
        return ResourceLocation.fromNamespaceAndPath((String)"regen_resources", (String)(COMPOSITE_PREFIX + strippedId.getNamespace() + "_" + strippedId.getPath()));
    }

    public void run(ResourceManager rm, SpriteSource.Output output) {
        NativeImage mask = RegenCompositeSpriteSource.readImage(rm, MASK_TEXTURE);
        if (mask == null) {
            LOGGER.warn("RegenResources: composite mask {} not found; skipping log composite generation", (Object)MASK_TEXTURE);
            return;
        }
        try (NativeImage nativeImage = mask;){
            Map<Block, Block> pairs = RegenStrippedLogResolver.getAllPairs();
            int generated = 0;
            for (Map.Entry<Block, Block> entry : pairs.entrySet()) {
                Block bark = entry.getKey();
                Block stripped = entry.getValue();
                if (bark == null || stripped == null) continue;
                ResourceLocation barkId = BuiltInRegistries.BLOCK.getKey(bark);
                ResourceLocation strippedId = BuiltInRegistries.BLOCK.getKey(stripped);
                if (barkId == null || strippedId == null) continue;
                generated += RegenCompositeSpriteSource.composePair(rm, output, mask, barkId, strippedId);
            }
            LOGGER.info("RegenResources: generated {} composite log sprites for {} pair(s)", (Object)generated, (Object)pairs.size());
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static int composePair(ResourceManager rm, SpriteSource.Output output, NativeImage mask, ResourceLocation barkId, ResourceLocation strippedId) {
        NativeImage barkSide = RegenCompositeSpriteSource.readBlockSideTexture(rm, barkId);
        NativeImage strippedSide = RegenCompositeSpriteSource.readBlockSideTexture(rm, strippedId);
        if (barkSide == null || strippedSide == null) {
            if (barkSide != null) {
                barkSide.close();
            }
            if (strippedSide != null) {
                strippedSide.close();
            }
            return 0;
        }
        try {
            NativeImage sideComposite = RegenCompositeSpriteSource.composite(mask, barkSide, strippedSide);
            RegenCompositeSpriteSource.registerSprite(output, RegenCompositeSpriteSource.sideSpriteId(strippedId), sideComposite);
            int n = 1;
            return n;
        }
        finally {
            barkSide.close();
            strippedSide.close();
        }
    }

    private static NativeImage readBlockSideTexture(ResourceManager rm, ResourceLocation blockId) {
        String[] swaps;
        String path;
        String ns = blockId.getNamespace();
        NativeImage img = RegenCompositeSpriteSource.readImage(rm, ResourceLocation.fromNamespaceAndPath((String)ns, (String)("textures/block/" + (path = blockId.getPath()) + ".png")));
        if (img != null) {
            return img;
        }
        for (String swap : swaps = new String[]{"_wood:_log", "_hyphae:_stem", "_wood:_stem"}) {
            String alt;
            String[] kv = swap.split(":", 2);
            String from = kv[0];
            String to = kv[1];
            if (!path.contains(from) || (img = RegenCompositeSpriteSource.readImage(rm, ResourceLocation.fromNamespaceAndPath((String)ns, (String)("textures/block/" + (alt = path.replace(from, to)) + ".png")))) == null) continue;
            return img;
        }
        return null;
    }

    private static boolean registerSprite(SpriteSource.Output output, ResourceLocation outRl, NativeImage composite) {
        SpriteContents contents = new SpriteContents(outRl, new FrameSize(composite.getWidth(), composite.getHeight()), composite, AnimationMetadataSection.EMPTY);
        output.add(outRl, (SpriteSource.SpriteSupplier)new HoldingSpriteSupplier(contents));
        return true;
    }

    private static NativeImage composite(NativeImage mask, NativeImage bark, NativeImage stripped) {
        int barkFrameH = bark.getWidth();
        int strippedFrameH = stripped.getWidth();
        int outW = Math.max(bark.getWidth(), stripped.getWidth());
        int outH = Math.max(barkFrameH, strippedFrameH);
        if (outW <= 0 || outH <= 0) {
            return new NativeImage(1, 1, false);
        }
        NativeImage out = new NativeImage(NativeImage.Format.RGBA, outW, outH, false);
        for (int y = 0; y < outH; ++y) {
            for (int x = 0; x < outW; ++x) {
                int barkArgb = RegenCompositeSpriteSource.sampleNearest(bark, x, y, outW, outH, bark.getWidth(), barkFrameH);
                int strippedArgb = RegenCompositeSpriteSource.sampleNearest(stripped, x, y, outW, outH, stripped.getWidth(), strippedFrameH);
                int maskArgb = RegenCompositeSpriteSource.sampleNearest(mask, x, y, outW, outH, mask.getWidth(), mask.getHeight());
                int maskA = maskArgb >>> 24 & 0xFF;
                float t = (float)maskA / 255.0f;
                int br = barkArgb >>> 16 & 0xFF;
                int bg = barkArgb >>> 8 & 0xFF;
                int bb = barkArgb & 0xFF;
                int ba = barkArgb >>> 24 & 0xFF;
                int sr = strippedArgb >>> 16 & 0xFF;
                int sg = strippedArgb >>> 8 & 0xFF;
                int sb = strippedArgb & 0xFF;
                int rr = Math.round((float)br * (1.0f - t) + (float)sr * t);
                int gg = Math.round((float)bg * (1.0f - t) + (float)sg * t);
                int bbn = Math.round((float)bb * (1.0f - t) + (float)sb * t);
                int outAbgr = ba << 24 | bbn << 16 | gg << 8 | rr;
                out.setPixelRGBA(x, y, outAbgr);
            }
        }
        return out;
    }

    private static int sampleNearest(NativeImage img, int x, int y, int outW, int outH, int srcW, int srcH) {
        int sx = (int)((long)x * (long)srcW / (long)Math.max(1, outW));
        int sy = (int)((long)y * (long)srcH / (long)Math.max(1, outH));
        if (sx >= srcW) {
            sx = srcW - 1;
        }
        if (sy >= srcH) {
            sy = srcH - 1;
        }
        int abgr = img.getPixelRGBA(sx, sy);
        int a = abgr >>> 24 & 0xFF;
        int b = abgr >>> 16 & 0xFF;
        int g = abgr >>> 8 & 0xFF;
        int r = abgr & 0xFF;
        return a << 24 | r << 16 | g << 8 | b;
    }

    private static NativeImage readImage(ResourceManager rm, ResourceLocation rl) {
        try {
            Resource res = rm.getResourceOrThrow(rl);
            try (InputStream is = res.open()) {
                return NativeImage.read(is);
            }
        } catch (IOException e) {
            return null;
        }
    }

    public SpriteSourceType type() {
        return TYPE;
    }

    private static final class HoldingSpriteSupplier
    implements SpriteSource.SpriteSupplier {
        private SpriteContents stored;

        HoldingSpriteSupplier(SpriteContents stored) {
            this.stored = stored;
        }

        public SpriteContents get() {
            SpriteContents ret = this.stored;
            this.stored = null;
            return ret;
        }

        public void discard() {
            if (this.stored != null) {
                this.stored.close();
                this.stored = null;
            }
        }
    }
}

