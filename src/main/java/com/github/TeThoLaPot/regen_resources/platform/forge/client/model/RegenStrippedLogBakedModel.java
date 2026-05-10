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
        Block block = (Block)BuiltInRegistries.BLOCK.get(id);
        if (block == null || block == Blocks.AIR) {
            return null;
        }
        ResourceLocation actualId = BuiltInRegistries.BLOCK.getKey(block);
        if (actualId == null || !actualId.equals(id)) {
            return null;
        }
        return Minecraft.getInstance().getBlockRenderer().getBlockModelShaper().getBlockModel(block.defaultBlockState());
    }

    @Nullable
    private BlockState targetState(@Nullable ResourceLocation id) {
        if (id == null) {
            return null;
        }
        Block block = (Block)BuiltInRegistries.BLOCK.get(id);
        if (block == null || block == Blocks.AIR) {
            return null;
        }
        return block.defaultBlockState();
    }

    private static boolean useLogStyleEndCaps(ResourceLocation strippedId) {
        String p = strippedId.getPath();
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
        if (shellState != null && shellState.hasProperty(RegenBlocks.AXIS)) {
            axis = (Direction.Axis)shellState.getValue(RegenBlocks.AXIS);
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
        TextureAtlasSprite missing = atlas.getSprite(MissingTextureAtlasSprite.getLocation());
        TextureAtlasSprite endSprite = sideSprite;
        ResourceLocation barkId = RegenStrippedLogResolver.barkIdForStripped(strippedId);
        if (barkId != null && (top = atlas.getSprite(topRl = ResourceLocation.fromNamespaceAndPath((String)barkId.getNamespace(), (String)("block/" + barkId.getPath() + "_top")))) != null && top != missing) {
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
        TextureAtlasSprite sideSprite = atlas.getSprite(sideRl);
        TextureAtlasSprite missing = atlas.getSprite(MissingTextureAtlasSprite.getLocation());
        if (sideSprite == null || sideSprite == missing) {
            return null;
        }
        return sideSprite;
    }

    @Nullable
    private static TextureAtlas blockAtlas() {
        return Minecraft.getInstance().getModelManager().getAtlas(InventoryMenu.BLOCK_ATLAS);
    }

    private static BakedQuad bakeFace(Direction face, TextureAtlasSprite sprite) {
        BlockFaceUV uv = new BlockFaceUV(new float[]{0.0f, 0.0f, 16.0f, 16.0f}, 0);
        BlockElementFace elemFace = new BlockElementFace(null, -1, "", uv);
        return FACE_BAKERY.bakeQuad(new Vector3f(0.0f, 0.0f, 0.0f), new Vector3f(16.0f, 16.0f, 16.0f), elemFace, sprite, face, (ModelState)BlockModelRotation.X0_Y0, null, true, MODEL_LOC);
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
                return ChunkRenderTypeSet.of((RenderType[])new RenderType[]{RenderType.solid()});
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

    public boolean useAmbientOcclusion() {
        return this.original.useAmbientOcclusion();
    }

    public boolean isGui3d() {
        return this.original.isGui3d();
    }

    public boolean usesBlockLight() {
        return this.original.usesBlockLight();
    }

    public boolean isCustomRenderer() {
        return false;
    }

    @NotNull
    public TextureAtlasSprite getParticleIcon() {
        return this.original.getParticleIcon();
    }

    @NotNull
    public TextureAtlasSprite getParticleIcon(@NotNull ModelData data) {
        ResourceLocation id = (ResourceLocation)data.get(RegenBlockEntityModelProperties.STRIPPED_BLOCK);
        if (id != null) {
            BakedModel target;
            TextureAtlas atlas = Minecraft.getInstance().getModelManager().getAtlas(InventoryMenu.BLOCK_ATLAS);
            if (atlas != null) {
                TextureAtlasSprite spr = atlas.getSprite(RegenCompositeSpriteSource.sideSpriteId(id));
                TextureAtlasSprite missing = atlas.getSprite(MissingTextureAtlasSprite.getLocation());
                if (spr != null && spr != missing) {
                    return spr;
                }
            }
            if ((target = this.resolveTarget(id)) != null) {
                return target.getParticleIcon(ModelData.EMPTY);
            }
        }
        return this.original.getParticleIcon();
    }

    @NotNull
    public ItemTransforms getTransforms() {
        return this.original.getTransforms();
    }

    @NotNull
    public ItemOverrides getOverrides() {
        return this.original.getOverrides();
    }

    private record LogCompositeCacheKey(ResourceLocation strippedId, Direction.Axis axis) {
    }
}

