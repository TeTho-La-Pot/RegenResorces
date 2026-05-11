package com.github.TeThoLaPot.regen_resources.platform.neoforge.client.model;

import com.github.TeThoLaPot.regen_resources.common.block.RegenBlockEntityModelProperties;
import com.github.TeThoLaPot.regen_resources.common.block.RegenBlocks;
import com.github.TeThoLaPot.regen_resources.common.block.RegenStrippedLogResolver;
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
import net.neoforged.neoforge.client.ChunkRenderTypeSet;
import net.neoforged.neoforge.client.model.IDynamicBakedModel;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

/** ModelData のストリップ ID に応じて原木見た目を動的に組み立てる。 */
public final class RegenStrippedLogBakedModel implements IDynamicBakedModel {

    private static final FaceBakery FACE_BAKERY = new FaceBakery();
    private static final ConcurrentHashMap<ResourceLocation, Map<Direction, List<BakedQuad>>> COMPOSITE_QUAD_CACHE_WOOD =
            new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<LogCompositeCacheKey, Map<Direction, List<BakedQuad>>> COMPOSITE_QUAD_CACHE_LOG =
            new ConcurrentHashMap<>();

    private final BakedModel original;

    public RegenStrippedLogBakedModel(BakedModel original) {
        this.original = original;
    }

    @Nullable
    private BakedModel resolveTarget(@Nullable ResourceLocation id) {
        if (id == null) {
            return null;
        }
        Block block = BuiltInRegistries.BLOCK.get(id);
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
        Block block = BuiltInRegistries.BLOCK.get(id);
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
            case X -> face == Direction.WEST || face == Direction.EAST;
            case Y -> face == Direction.DOWN || face == Direction.UP;
            case Z -> face == Direction.NORTH || face == Direction.SOUTH;
        };
    }

    @Nullable
    private static Map<Direction, List<BakedQuad>> compositeQuadsFor(ResourceLocation strippedId, @Nullable BlockState shellState) {
        Direction.Axis axis = Direction.Axis.Y;
        if (shellState != null && shellState.hasProperty(RegenBlocks.AXIS)) {
            axis = shellState.getValue(RegenBlocks.AXIS);
        }
        return compositeQuadsCached(strippedId, axis);
    }

    @Nullable
    private static Map<Direction, List<BakedQuad>> compositeQuadsCached(ResourceLocation strippedId, Direction.Axis shellAxis) {
        if (useLogStyleEndCaps(strippedId)) {
            return COMPOSITE_QUAD_CACHE_LOG.computeIfAbsent(
                    new LogCompositeCacheKey(strippedId, shellAxis), k -> buildLogCompositeQuads(k.strippedId(), k.axis()));
        }
        return COMPOSITE_QUAD_CACHE_WOOD.computeIfAbsent(strippedId, RegenStrippedLogBakedModel::buildWoodCompositeQuads);
    }

    /** 側面マスク合成スプライトのクォードキャッシュを事前生成し、再生シェル出現直後のチラつきを抑える。 */
    public static void prefetchCompositeCache(ResourceLocation strippedId, Direction.Axis shellAxis) {
        compositeQuadsCached(strippedId, shellAxis);
    }

    @Nullable
    private static Map<Direction, List<BakedQuad>> buildWoodCompositeQuads(ResourceLocation strippedId) {
        TextureAtlas atlas = blockAtlas();
        if (atlas == null) {
            return null;
        }
        TextureAtlasSprite sideSprite = loadCompositeSideSprite(atlas, strippedId);
        if (sideSprite == null) {
            return null;
        }
        EnumMap<Direction, List<BakedQuad>> map = new EnumMap<>(Direction.class);
        for (Direction d : Direction.values()) {
            map.put(d, List.of(bakeFace(d, sideSprite)));
        }
        return map;
    }

    @Nullable
    private static Map<Direction, List<BakedQuad>> buildLogCompositeQuads(ResourceLocation strippedId, Direction.Axis axis) {
        TextureAtlas atlas = blockAtlas();
        if (atlas == null) {
            return null;
        }
        TextureAtlasSprite sideSprite = loadCompositeSideSprite(atlas, strippedId);
        if (sideSprite == null) {
            return null;
        }
        TextureAtlasSprite missing = atlas.getSprite(MissingTextureAtlasSprite.getLocation());
        TextureAtlasSprite endSprite = sideSprite;
        ResourceLocation barkId = RegenStrippedLogResolver.barkIdForStripped(strippedId);
        if (barkId != null) {
            ResourceLocation topRl =
                    ResourceLocation.fromNamespaceAndPath(barkId.getNamespace(), "block/" + barkId.getPath() + "_top");
            TextureAtlasSprite top = atlas.getSprite(topRl);
            if (top != null && top != missing) {
                endSprite = top;
            }
        }
        EnumMap<Direction, List<BakedQuad>> map = new EnumMap<>(Direction.class);
        for (Direction d : Direction.values()) {
            TextureAtlasSprite spr = isPillarEndFace(d, axis) ? endSprite : sideSprite;
            map.put(d, List.of(bakeFace(d, spr)));
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
        BlockFaceUV uv = new BlockFaceUV(new float[] {0.0f, 0.0f, 16.0f, 16.0f}, 0);
        BlockElementFace elemFace = new BlockElementFace(null, -1, "", uv);
        return FACE_BAKERY.bakeQuad(
                new Vector3f(0.0f, 0.0f, 0.0f),
                new Vector3f(16.0f, 16.0f, 16.0f),
                elemFace,
                sprite,
                face,
                BlockModelRotation.X0_Y0,
                null,
                true);
    }

    @Override
    @NotNull
    public List<BakedQuad> getQuads(
            @Nullable BlockState state, @Nullable Direction side, @NotNull RandomSource rand, @NotNull ModelData data, @Nullable RenderType renderType) {
        ResourceLocation id = data.get(RegenBlockEntityModelProperties.STRIPPED_BLOCK);
        if (id != null) {
            Map<Direction, List<BakedQuad>> compositeQuads = compositeQuadsFor(id, state);
            if (compositeQuads != null) {
                if (side == null) {
                    ArrayList<BakedQuad> all = new ArrayList<>(6);
                    for (Direction d : Direction.values()) {
                        all.addAll(compositeQuads.get(d));
                    }
                    return all;
                }
                return compositeQuads.getOrDefault(side, Collections.emptyList());
            }
            BakedModel target = resolveTarget(id);
            if (target != null) {
                BlockState ts = targetState(id);
                return target.getQuads(ts, side, rand, ModelData.EMPTY, renderType);
            }
        }
        return original.getQuads(state, side, rand, data, renderType);
    }

    @Override
    @NotNull
    public ChunkRenderTypeSet getRenderTypes(@NotNull BlockState state, @NotNull RandomSource rand, @NotNull ModelData data) {
        ResourceLocation id = data.get(RegenBlockEntityModelProperties.STRIPPED_BLOCK);
        if (id != null) {
            Map<Direction, List<BakedQuad>> compositeQuads = compositeQuadsFor(id, state);
            if (compositeQuads != null) {
                return ChunkRenderTypeSet.of(RenderType.solid());
            }
            BakedModel target = resolveTarget(id);
            if (target != null) {
                BlockState ts = targetState(id);
                return target.getRenderTypes(ts != null ? ts : state, rand, ModelData.EMPTY);
            }
        }
        return original.getRenderTypes(state, rand, data);
    }

    public static void invalidateCompositeCache() {
        COMPOSITE_QUAD_CACHE_WOOD.clear();
        COMPOSITE_QUAD_CACHE_LOG.clear();
    }

    @Override
    public boolean useAmbientOcclusion() {
        return original.useAmbientOcclusion();
    }

    @Override
    public boolean isGui3d() {
        return original.isGui3d();
    }

    @Override
    public boolean usesBlockLight() {
        return original.usesBlockLight();
    }

    @Override
    public boolean isCustomRenderer() {
        return false;
    }

    @Override
    @NotNull
    public TextureAtlasSprite getParticleIcon() {
        return original.getParticleIcon();
    }

    @Override
    @NotNull
    public TextureAtlasSprite getParticleIcon(@NotNull ModelData data) {
        ResourceLocation id = data.get(RegenBlockEntityModelProperties.STRIPPED_BLOCK);
        if (id != null) {
            TextureAtlas atlas = Minecraft.getInstance().getModelManager().getAtlas(InventoryMenu.BLOCK_ATLAS);
            if (atlas != null) {
                TextureAtlasSprite spr = atlas.getSprite(RegenCompositeSpriteSource.sideSpriteId(id));
                TextureAtlasSprite missing = atlas.getSprite(MissingTextureAtlasSprite.getLocation());
                if (spr != null && spr != missing) {
                    return spr;
                }
            }
            BakedModel target = resolveTarget(id);
            if (target != null) {
                return target.getParticleIcon(ModelData.EMPTY);
            }
        }
        return original.getParticleIcon();
    }

    @Override
    @NotNull
    public ItemTransforms getTransforms() {
        return original.getTransforms();
    }

    @Override
    @NotNull
    public net.minecraft.client.renderer.block.model.ItemOverrides getOverrides() {
        return original.getOverrides();
    }

    private record LogCompositeCacheKey(ResourceLocation strippedId, Direction.Axis axis) {}
}
