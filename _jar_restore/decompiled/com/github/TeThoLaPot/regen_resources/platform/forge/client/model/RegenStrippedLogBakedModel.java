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
 *  net.minecraft.core.Direction$Axis
 *  net.minecraft.core.registries.BuiltInRegistries
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.util.RandomSource
 *  net.minecraft.world.inventory.InventoryMenu
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.Blocks
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
import com.github.TeThoLaPot.regen_resources.common.block.RegenBlocks;
import com.github.TeThoLaPot.regen_resources.common.block.RegenStrippedLogResolver;
import com.github.TeThoLaPot.regen_resources.platform.forge.client.model.RegenCompositeSpriteSource;
import java.util.ArrayList;
import java.util.Collection;
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
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.ChunkRenderTypeSet;
import net.minecraftforge.client.model.IDynamicBakedModel;
import net.minecraftforge.client.model.data.ModelData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

public final class RegenStrippedLogBakedModel
implements IDynamicBakedModel {
    private static final FaceBakery FACE_BAKERY = new FaceBakery();
    private static final ResourceLocation MODEL_LOC = ResourceLocation.fromNamespaceAndPath((String)"regen_resources", (String)"block/regen_stripped_log_dynamic");
    private static final ConcurrentHashMap<ResourceLocation, Map<Direction, List<BakedQuad>>> COMPOSITE_QUAD_CACHE_WOOD = new ConcurrentHashMap();
    private static final ConcurrentHashMap<LogCompositeCacheKey, Map<Direction, List<BakedQuad>>> COMPOSITE_QUAD_CACHE_LOG = new ConcurrentHashMap();
    private final BakedModel original;

    public RegenStrippedLogBakedModel(BakedModel original) {
        this.original = original;
    }

    @Nullable
    private BakedModel resolveTarget(@Nullable ResourceLocation id) {
        if (id == null) {
            return null;
        }
        Block block = (Block)BuiltInRegistries.f_256975_.m_7745_(id);
        if (block == null || block == Blocks.f_50016_) {
            return null;
        }
        ResourceLocation actualId = BuiltInRegistries.f_256975_.m_7981_((Object)block);
        if (actualId == null || !actualId.equals((Object)id)) {
            return null;
        }
        return Minecraft.m_91087_().m_91289_().m_110907_().m_110893_(block.m_49966_());
    }

    @Nullable
    private BlockState targetState(@Nullable ResourceLocation id) {
        if (id == null) {
            return null;
        }
        Block block = (Block)BuiltInRegistries.f_256975_.m_7745_(id);
        if (block == null || block == Blocks.f_50016_) {
            return null;
        }
        return block.m_49966_();
    }

    private static boolean useLogStyleEndCaps(ResourceLocation strippedId) {
        String p = strippedId.m_135815_();
        if (p.contains("_wood") || p.contains("_hyphae")) {
            return false;
        }
        return p.contains("_log") || p.contains("_stem");
    }

    private static boolean isPillarEndFace(Direction face, Direction.Axis axis) {
        return switch (axis) {
            default -> throw new IncompatibleClassChangeError();
            case Direction.Axis.X -> {
                if (face == Direction.WEST || face == Direction.EAST) {
                    yield true;
                }
                yield false;
            }
            case Direction.Axis.Y -> {
                if (face == Direction.DOWN || face == Direction.UP) {
                    yield true;
                }
                yield false;
            }
            case Direction.Axis.Z -> face == Direction.NORTH || face == Direction.SOUTH;
        };
    }

    @Nullable
    private static Map<Direction, List<BakedQuad>> compositeQuadsFor(ResourceLocation strippedId, @Nullable BlockState shellState) {
        Direction.Axis axis = Direction.Axis.Y;
        if (shellState != null && shellState.m_61138_(RegenBlocks.AXIS)) {
            axis = (Direction.Axis)shellState.m_61143_(RegenBlocks.AXIS);
        }
        if (RegenStrippedLogBakedModel.useLogStyleEndCaps(strippedId)) {
            return COMPOSITE_QUAD_CACHE_LOG.computeIfAbsent(new LogCompositeCacheKey(strippedId, axis), k -> RegenStrippedLogBakedModel.buildLogCompositeQuads(k.strippedId(), k.axis()));
        }
        return COMPOSITE_QUAD_CACHE_WOOD.computeIfAbsent(strippedId, RegenStrippedLogBakedModel::buildWoodCompositeQuads);
    }

    @Nullable
    private static Map<Direction, List<BakedQuad>> buildWoodCompositeQuads(ResourceLocation strippedId) {
        TextureAtlas atlas = RegenStrippedLogBakedModel.blockAtlas();
        if (atlas == null) {
            return null;
        }
        TextureAtlasSprite sideSprite = RegenStrippedLogBakedModel.loadCompositeSideSprite(atlas, strippedId);
        if (sideSprite == null) {
            return null;
        }
        EnumMap<Direction, List<BakedQuad>> map = new EnumMap<Direction, List<BakedQuad>>(Direction.class);
        for (Direction d : Direction.values()) {
            map.put(d, List.of(RegenStrippedLogBakedModel.bakeFace(d, sideSprite)));
        }
        return map;
    }

    @Nullable
    private static Map<Direction, List<BakedQuad>> buildLogCompositeQuads(ResourceLocation strippedId, Direction.Axis axis) {
        ResourceLocation topRl;
        TextureAtlasSprite top;
        TextureAtlas atlas = RegenStrippedLogBakedModel.blockAtlas();
        if (atlas == null) {
            return null;
        }
        TextureAtlasSprite sideSprite = RegenStrippedLogBakedModel.loadCompositeSideSprite(atlas, strippedId);
        if (sideSprite == null) {
            return null;
        }
        TextureAtlasSprite missing = atlas.m_118316_(MissingTextureAtlasSprite.m_118071_());
        TextureAtlasSprite endSprite = sideSprite;
        ResourceLocation barkId = RegenStrippedLogResolver.barkIdForStripped(strippedId);
        if (barkId != null && (top = atlas.m_118316_(topRl = ResourceLocation.fromNamespaceAndPath((String)barkId.m_135827_(), (String)("block/" + barkId.m_135815_() + "_top")))) != null && top != missing) {
            endSprite = top;
        }
        EnumMap<Direction, List<BakedQuad>> map = new EnumMap<Direction, List<BakedQuad>>(Direction.class);
        for (Direction d : Direction.values()) {
            TextureAtlasSprite spr = RegenStrippedLogBakedModel.isPillarEndFace(d, axis) ? endSprite : sideSprite;
            map.put(d, List.of(RegenStrippedLogBakedModel.bakeFace(d, spr)));
        }
        return map;
    }

    @Nullable
    private static TextureAtlasSprite loadCompositeSideSprite(TextureAtlas atlas, ResourceLocation strippedId) {
        ResourceLocation sideRl = RegenCompositeSpriteSource.sideSpriteId(strippedId);
        TextureAtlasSprite sideSprite = atlas.m_118316_(sideRl);
        TextureAtlasSprite missing = atlas.m_118316_(MissingTextureAtlasSprite.m_118071_());
        if (sideSprite == null || sideSprite == missing) {
            return null;
        }
        return sideSprite;
    }

    @Nullable
    private static TextureAtlas blockAtlas() {
        return Minecraft.m_91087_().m_91304_().m_119428_(InventoryMenu.f_39692_);
    }

    private static BakedQuad bakeFace(Direction face, TextureAtlasSprite sprite) {
        BlockFaceUV uv = new BlockFaceUV(new float[]{0.0f, 0.0f, 16.0f, 16.0f}, 0);
        BlockElementFace elemFace = new BlockElementFace(null, -1, "", uv);
        return FACE_BAKERY.m_111600_(new Vector3f(0.0f, 0.0f, 0.0f), new Vector3f(16.0f, 16.0f, 16.0f), elemFace, sprite, face, (ModelState)BlockModelRotation.X0_Y0, null, true, MODEL_LOC);
    }

    @NotNull
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @NotNull RandomSource rand, @NotNull ModelData data, @Nullable RenderType renderType) {
        ResourceLocation id = (ResourceLocation)data.get(RegenBlockEntityModelProperties.STRIPPED_BLOCK);
        if (id != null) {
            Map<Direction, List<BakedQuad>> compositeQuads = RegenStrippedLogBakedModel.compositeQuadsFor(id, state);
            if (compositeQuads != null) {
                if (side == null) {
                    ArrayList<BakedQuad> all = new ArrayList<BakedQuad>(6);
                    for (Direction d : Direction.values()) {
                        all.addAll((Collection<BakedQuad>)compositeQuads.get(d));
                    }
                    return all;
                }
                return compositeQuads.getOrDefault(side, Collections.emptyList());
            }
            BakedModel target = this.resolveTarget(id);
            if (target != null) {
                BlockState ts = this.targetState(id);
                return target.getQuads(ts, side, rand, ModelData.EMPTY, renderType);
            }
        }
        return this.original.getQuads(state, side, rand, data, renderType);
    }

    @NotNull
    public ChunkRenderTypeSet getRenderTypes(@NotNull BlockState state, @NotNull RandomSource rand, @NotNull ModelData data) {
        ResourceLocation id = (ResourceLocation)data.get(RegenBlockEntityModelProperties.STRIPPED_BLOCK);
        if (id != null) {
            Map<Direction, List<BakedQuad>> compositeQuads = RegenStrippedLogBakedModel.compositeQuadsFor(id, state);
            if (compositeQuads != null) {
                return ChunkRenderTypeSet.of((RenderType[])new RenderType[]{RenderType.m_110451_()});
            }
            BakedModel target = this.resolveTarget(id);
            if (target != null) {
                BlockState ts = this.targetState(id);
                return target.getRenderTypes(ts != null ? ts : state, rand, ModelData.EMPTY);
            }
        }
        return this.original.getRenderTypes(state, rand, data);
    }

    public static void invalidateCompositeCache() {
        COMPOSITE_QUAD_CACHE_WOOD.clear();
        COMPOSITE_QUAD_CACHE_LOG.clear();
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
        ResourceLocation id = (ResourceLocation)data.get(RegenBlockEntityModelProperties.STRIPPED_BLOCK);
        if (id != null) {
            BakedModel target;
            TextureAtlas atlas = Minecraft.m_91087_().m_91304_().m_119428_(InventoryMenu.f_39692_);
            if (atlas != null) {
                TextureAtlasSprite spr = atlas.m_118316_(RegenCompositeSpriteSource.sideSpriteId(id));
                TextureAtlasSprite missing = atlas.m_118316_(MissingTextureAtlasSprite.m_118071_());
                if (spr != null && spr != missing) {
                    return spr;
                }
            }
            if ((target = this.resolveTarget(id)) != null) {
                return target.getParticleIcon(ModelData.EMPTY);
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

    private record LogCompositeCacheKey(ResourceLocation strippedId, Direction.Axis axis) {
    }
}

