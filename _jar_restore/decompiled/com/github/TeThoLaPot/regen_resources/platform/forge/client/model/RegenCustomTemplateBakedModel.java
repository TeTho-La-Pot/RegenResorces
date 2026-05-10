/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.renderer.RenderType
 *  net.minecraft.client.renderer.block.model.BakedQuad
 *  net.minecraft.client.renderer.block.model.BlockElementFace
 *  net.minecraft.client.renderer.block.model.BlockFaceUV
 *  net.minecraft.client.renderer.block.model.FaceBakery
 *  net.minecraft.client.renderer.block.model.ItemOverrides
 *  net.minecraft.client.renderer.block.model.ItemTransforms
 *  net.minecraft.client.renderer.texture.MissingTextureAtlasSprite
 *  net.minecraft.client.renderer.texture.TextureAtlas
 *  net.minecraft.client.renderer.texture.TextureAtlasSprite
 *  net.minecraft.client.resources.model.BakedModel
 *  net.minecraft.client.resources.model.BlockModelRotation
 *  net.minecraft.client.resources.model.ModelState
 *  net.minecraft.core.Direction
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.util.RandomSource
 *  net.minecraft.world.inventory.InventoryMenu
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraftforge.client.ChunkRenderTypeSet
 *  net.minecraftforge.client.model.IDynamicBakedModel
 *  net.minecraftforge.client.model.data.ModelData
 *  org.jetbrains.annotations.NotNull
 *  org.jetbrains.annotations.Nullable
 *  org.joml.Vector3f
 */
package com.github.TeThoLaPot.regen_resources.platform.forge.client.model;

import com.github.TeThoLaPot.regen_resources.common.block.RegenBlockEntityModelProperties;
import com.github.TeThoLaPot.regen_resources.common.block.RegenCustomVisualSpec;
import com.github.TeThoLaPot.regen_resources.common.block.RegenTemplate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockElementFace;
import net.minecraft.client.renderer.block.model.BlockFaceUV;
import net.minecraft.client.renderer.block.model.FaceBakery;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.ChunkRenderTypeSet;
import net.minecraftforge.client.model.IDynamicBakedModel;
import net.minecraftforge.client.model.data.ModelData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

public final class RegenCustomTemplateBakedModel
implements IDynamicBakedModel {
    private static final FaceBakery FACE_BAKERY = new FaceBakery();
    private static final ResourceLocation MODEL_LOC = ResourceLocation.fromNamespaceAndPath((String)"regen_resources", (String)"block/regen_custom_dynamic");
    private static final ConcurrentHashMap<RegenCustomVisualSpec, Map<Direction, List<BakedQuad>>> QUAD_CACHE = new ConcurrentHashMap();
    private final BakedModel original;

    public RegenCustomTemplateBakedModel(BakedModel original) {
        this.original = original;
    }

    @NotNull
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @NotNull RandomSource rand, @NotNull ModelData data, @Nullable RenderType renderType) {
        Map<Direction, List<BakedQuad>> quads;
        RegenCustomVisualSpec spec = (RegenCustomVisualSpec)data.get(RegenBlockEntityModelProperties.CUSTOM_VISUAL_SPEC);
        if (spec != null && (quads = RegenCustomTemplateBakedModel.quadsFor(spec)) != null) {
            if (side == null) {
                ArrayList<BakedQuad> all = new ArrayList<BakedQuad>(6);
                for (Direction d : Direction.values()) {
                    List<BakedQuad> q = quads.get(d);
                    if (q == null) continue;
                    all.addAll(q);
                }
                return all;
            }
            return quads.getOrDefault(side, Collections.emptyList());
        }
        return this.original.getQuads(state, side, rand, data, renderType);
    }

    @NotNull
    public ChunkRenderTypeSet getRenderTypes(@NotNull BlockState state, @NotNull RandomSource rand, @NotNull ModelData data) {
        RegenCustomVisualSpec spec = (RegenCustomVisualSpec)data.get(RegenBlockEntityModelProperties.CUSTOM_VISUAL_SPEC);
        if (spec != null && RegenCustomTemplateBakedModel.quadsFor(spec) != null) {
            return ChunkRenderTypeSet.of((RenderType[])new RenderType[]{RenderType.m_110451_()});
        }
        return this.original.getRenderTypes(state, rand, data);
    }

    @Nullable
    private static Map<Direction, List<BakedQuad>> quadsFor(RegenCustomVisualSpec spec) {
        return QUAD_CACHE.computeIfAbsent(spec, RegenCustomTemplateBakedModel::buildQuads);
    }

    @Nullable
    private static Map<Direction, List<BakedQuad>> buildQuads(RegenCustomVisualSpec spec) {
        TextureAtlas atlas = Minecraft.m_91087_().m_91304_().m_119428_(InventoryMenu.f_39692_);
        if (atlas == null) {
            return null;
        }
        TextureAtlasSprite missing = atlas.m_118316_(MissingTextureAtlasSprite.m_118071_());
        RegenTemplate template = spec.template();
        EnumMap<Direction, TextureAtlasSprite> spriteByFace = new EnumMap<Direction, TextureAtlasSprite>(Direction.class);
        boolean anyOk = false;
        for (Direction d : Direction.values()) {
            TextureAtlasSprite sprite;
            String slot = template.slotForFace(d);
            ResourceLocation rl = spec.textureFor(slot);
            if (rl == null || (sprite = atlas.m_118316_(rl)) == null || sprite == missing) continue;
            spriteByFace.put(d, sprite);
            anyOk = true;
        }
        if (!anyOk) {
            return null;
        }
        EnumMap<Direction, List<BakedQuad>> map = new EnumMap<Direction, List<BakedQuad>>(Direction.class);
        for (Direction d : Direction.values()) {
            TextureAtlasSprite sprite = (TextureAtlasSprite)spriteByFace.get(d);
            if (sprite == null) {
                map.put(d, Collections.emptyList());
                continue;
            }
            map.put(d, List.of(RegenCustomTemplateBakedModel.bakeFace(d, sprite)));
        }
        return map;
    }

    private static BakedQuad bakeFace(Direction face, TextureAtlasSprite sprite) {
        BlockFaceUV uv = new BlockFaceUV(new float[]{0.0f, 0.0f, 16.0f, 16.0f}, 0);
        BlockElementFace elemFace = new BlockElementFace(null, -1, "", uv);
        return FACE_BAKERY.m_111600_(new Vector3f(0.0f, 0.0f, 0.0f), new Vector3f(16.0f, 16.0f, 16.0f), elemFace, sprite, face, (ModelState)BlockModelRotation.X0_Y0, null, true, MODEL_LOC);
    }

    public static void invalidateCache() {
        QUAD_CACHE.clear();
    }

    public boolean m_7541_() {
        return this.original.m_7541_();
    }

    public boolean m_7539_() {
        return this.original.m_7539_();
    }

    public boolean m_7547_() {
        return this.original.m_7547_();
    }

    public boolean m_7521_() {
        return false;
    }

    @NotNull
    public TextureAtlasSprite m_6160_() {
        return this.original.m_6160_();
    }

    @NotNull
    public TextureAtlasSprite getParticleIcon(@NotNull ModelData data) {
        TextureAtlas atlas;
        RegenCustomVisualSpec spec = (RegenCustomVisualSpec)data.get(RegenBlockEntityModelProperties.CUSTOM_VISUAL_SPEC);
        if (spec != null && (atlas = Minecraft.m_91087_().m_91304_().m_119428_(InventoryMenu.f_39692_)) != null) {
            String[] tryOrder;
            TextureAtlasSprite missing = atlas.m_118316_(MissingTextureAtlasSprite.m_118071_());
            for (String slot : tryOrder = new String[]{"side", "all", "top", "north", "east", "south", "west", "up", "end", "bottom", "down"}) {
                TextureAtlasSprite spr;
                ResourceLocation rl = spec.textureFor(slot);
                if (rl == null || (spr = atlas.m_118316_(rl)) == null || spr == missing) continue;
                return spr;
            }
        }
        return this.original.m_6160_();
    }

    @NotNull
    public ItemTransforms m_7442_() {
        return this.original.m_7442_();
    }

    @NotNull
    public ItemOverrides m_7343_() {
        return this.original.m_7343_();
    }
}

