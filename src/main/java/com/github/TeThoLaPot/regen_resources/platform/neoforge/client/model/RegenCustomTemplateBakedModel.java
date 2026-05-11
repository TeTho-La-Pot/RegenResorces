package com.github.TeThoLaPot.regen_resources.platform.neoforge.client.model;

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
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.ChunkRenderTypeSet;
import net.neoforged.neoforge.client.model.IDynamicBakedModel;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

/** {@link RegenCustomVisualSpec} に基づき各面をテクスチャから張り替える。 */
public final class RegenCustomTemplateBakedModel implements IDynamicBakedModel {

    private static final FaceBakery FACE_BAKERY = new FaceBakery();
    private static final ConcurrentHashMap<RegenCustomVisualSpec, Map<Direction, List<BakedQuad>>> QUAD_CACHE =
            new ConcurrentHashMap<>();

    private final BakedModel original;

    public RegenCustomTemplateBakedModel(BakedModel original) {
        this.original = original;
    }

    @Override
    @NotNull
    public List<BakedQuad> getQuads(
            @Nullable BlockState state, @Nullable Direction side, @NotNull RandomSource rand, @NotNull ModelData data, @Nullable RenderType renderType) {
        RegenCustomVisualSpec spec = data.get(RegenBlockEntityModelProperties.CUSTOM_VISUAL_SPEC);
        if (spec != null) {
            Map<Direction, List<BakedQuad>> quads = quadsFor(spec);
            if (quads != null) {
                if (side == null) {
                    ArrayList<BakedQuad> all = new ArrayList<>(24);
                    for (Direction d : Direction.values()) {
                        List<BakedQuad> q = quads.get(d);
                        if (q != null) {
                            all.addAll(q);
                        }
                    }
                    return all;
                }
                return quads.getOrDefault(side, Collections.emptyList());
            }
        }
        return original.getQuads(state, side, rand, data, renderType);
    }

    @Override
    @NotNull
    public ChunkRenderTypeSet getRenderTypes(@NotNull BlockState state, @NotNull RandomSource rand, @NotNull ModelData data) {
        RegenCustomVisualSpec spec = data.get(RegenBlockEntityModelProperties.CUSTOM_VISUAL_SPEC);
        if (spec != null && quadsFor(spec) != null) {
            return ChunkRenderTypeSet.of(RenderType.solid());
        }
        return original.getRenderTypes(state, rand, data);
    }

    @Nullable
    private static Map<Direction, List<BakedQuad>> quadsFor(RegenCustomVisualSpec spec) {
        return QUAD_CACHE.computeIfAbsent(spec, RegenCustomTemplateBakedModel::buildQuads);
    }

    @Nullable
    private static Map<Direction, List<BakedQuad>> buildQuads(RegenCustomVisualSpec spec) {
        TextureAtlas atlas = Minecraft.getInstance().getModelManager().getAtlas(InventoryMenu.BLOCK_ATLAS);
        if (atlas == null) {
            return null;
        }
        TextureAtlasSprite missing = atlas.getSprite(MissingTextureAtlasSprite.getLocation());
        RegenTemplate template = spec.template();
        EnumMap<Direction, TextureAtlasSprite> spriteByFace = new EnumMap<>(Direction.class);
        boolean anyOk = false;
        for (Direction d : Direction.values()) {
            String slot = template.slotForFace(d);
            ResourceLocation rl = spec.textureFor(slot);
            if (rl == null) {
                continue;
            }
            TextureAtlasSprite sprite = atlas.getSprite(rl);
            if (sprite == null || sprite == missing) {
                continue;
            }
            spriteByFace.put(d, sprite);
            anyOk = true;
        }
        if (!anyOk) {
            return null;
        }
        EnumMap<Direction, List<BakedQuad>> map = new EnumMap<>(Direction.class);
        for (Direction d : Direction.values()) {
            TextureAtlasSprite sprite = spriteByFace.get(d);
            if (sprite == null) {
                map.put(d, Collections.emptyList());
            } else {
                map.put(d, List.of(bakeFace(d, sprite)));
            }
        }
        return map;
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

    public static void invalidateCache() {
        QUAD_CACHE.clear();
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
        RegenCustomVisualSpec spec = data.get(RegenBlockEntityModelProperties.CUSTOM_VISUAL_SPEC);
        if (spec != null) {
            TextureAtlas atlas = Minecraft.getInstance().getModelManager().getAtlas(InventoryMenu.BLOCK_ATLAS);
            if (atlas != null) {
                TextureAtlasSprite missing = atlas.getSprite(MissingTextureAtlasSprite.getLocation());
                String[] tryOrder = new String[] {"side", "all", "top", "north", "east", "south", "west", "up", "end", "bottom", "down"};
                for (String slot : tryOrder) {
                    ResourceLocation rl = spec.textureFor(slot);
                    if (rl == null) {
                        continue;
                    }
                    TextureAtlasSprite spr = atlas.getSprite(rl);
                    if (spr != null && spr != missing) {
                        return spr;
                    }
                }
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
}
