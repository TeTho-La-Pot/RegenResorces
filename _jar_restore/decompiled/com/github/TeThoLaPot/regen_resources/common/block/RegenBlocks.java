/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction$Axis
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.level.BlockGetter
 *  net.minecraft.world.level.Explosion
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.LevelReader
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.EntityBlock
 *  net.minecraft.world.level.block.SoundType
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  net.minecraft.world.level.block.entity.BlockEntityTicker
 *  net.minecraft.world.level.block.entity.BlockEntityType
 *  net.minecraft.world.level.block.state.BlockBehaviour$Properties
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.level.block.state.StateDefinition$Builder
 *  net.minecraft.world.level.block.state.properties.BlockStateProperties
 *  net.minecraft.world.level.block.state.properties.EnumProperty
 *  net.minecraft.world.level.block.state.properties.Property
 *  net.minecraftforge.fml.LogicalSide
 *  net.minecraftforge.fml.util.thread.EffectiveSide
 *  org.jetbrains.annotations.Nullable
 */
package com.github.TeThoLaPot.regen_resources.common.block;

import com.github.TeThoLaPot.regen_resources.common.block.RegenBlockEntity;
import com.github.TeThoLaPot.regen_resources.common.block.RegenCorruptionFallback;
import com.github.TeThoLaPot.regen_resources.common.block.RegenCustomVisualSpec;
import com.github.TeThoLaPot.regen_resources.common.block.RegenVisual;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.util.thread.EffectiveSide;
import org.jetbrains.annotations.Nullable;

public class RegenBlocks
extends Block
implements EntityBlock {
    public static final EnumProperty<RegenVisual> VISUAL = EnumProperty.m_61587_((String)"visual", RegenVisual.class);
    public static final EnumProperty<Direction.Axis> AXIS = BlockStateProperties.f_61365_;
    private static final ConcurrentHashMap<BlockPos, ResourceLocation> SERVER_PENDING = new ConcurrentHashMap();
    private static final ConcurrentHashMap<BlockPos, ResourceLocation> CLIENT_PENDING = new ConcurrentHashMap();
    private static final ConcurrentHashMap<BlockPos, RegenCustomVisualSpec> SERVER_PENDING_CUSTOM = new ConcurrentHashMap();
    private static final ConcurrentHashMap<BlockPos, RegenCustomVisualSpec> CLIENT_PENDING_CUSTOM = new ConcurrentHashMap();
    private final Supplier<BlockEntityType<RegenBlockEntity>> blockEntityType;

    public RegenBlocks(Supplier<BlockEntityType<RegenBlockEntity>> blockEntityType, BlockBehaviour.Properties properties) {
        super(properties);
        this.blockEntityType = blockEntityType;
        this.m_49959_((BlockState)((BlockState)((BlockState)this.f_49792_.m_61090_()).m_61124_(VISUAL, (Comparable)((Object)RegenVisual.STONE_PRESET))).m_61124_(AXIS, (Comparable)Direction.Axis.Y));
    }

    public static void preparePendingStrippedId(BlockPos pos, @Nullable ResourceLocation id) {
        RegenBlocks.putOrRemove(SERVER_PENDING, pos, id);
    }

    public static void preparePendingStrippedIdClient(BlockPos pos, @Nullable ResourceLocation id) {
        RegenBlocks.putOrRemove(CLIENT_PENDING, pos, id);
    }

    public static void preparePendingCustomSpec(BlockPos pos, @Nullable RegenCustomVisualSpec spec) {
        RegenBlocks.putOrRemoveAny(SERVER_PENDING_CUSTOM, pos, spec);
    }

    public static void preparePendingCustomSpecClient(BlockPos pos, @Nullable RegenCustomVisualSpec spec) {
        RegenBlocks.putOrRemoveAny(CLIENT_PENDING_CUSTOM, pos, spec);
    }

    private static void putOrRemove(ConcurrentHashMap<BlockPos, ResourceLocation> map, BlockPos pos, @Nullable ResourceLocation id) {
        if (id == null) {
            map.remove(pos);
        } else {
            map.put(pos.m_7949_(), id);
        }
    }

    private static <V> void putOrRemoveAny(ConcurrentHashMap<BlockPos, V> map, BlockPos pos, @Nullable V value) {
        if (value == null) {
            map.remove(pos);
        } else {
            map.put(pos.m_7949_(), value);
        }
    }

    @Nullable
    public BlockEntity m_142194_(BlockPos pos, BlockState state) {
        ConcurrentHashMap<BlockPos, RegenCustomVisualSpec> customMap;
        RegenCustomVisualSpec pendingCustom;
        RegenBlockEntity be = new RegenBlockEntity(this.blockEntityType.get(), pos, state);
        boolean client = EffectiveSide.get() == LogicalSide.CLIENT;
        ConcurrentHashMap<BlockPos, ResourceLocation> strippedMap = client ? CLIENT_PENDING : SERVER_PENDING;
        ResourceLocation pendingStripped = strippedMap.remove(pos);
        if (pendingStripped != null) {
            be.initStrippedBlockId(pendingStripped);
        }
        if ((pendingCustom = (customMap = client ? CLIENT_PENDING_CUSTOM : SERVER_PENDING_CUSTOM).remove(pos)) != null) {
            be.initCustomVisualSpec(pendingCustom);
        }
        return be;
    }

    private static BlockState miningSample(BlockState shellState) {
        return RegenCorruptionFallback.miningSampleFor((RegenVisual)((Object)shellState.m_61143_(VISUAL)));
    }

    private static BlockState miningSample(BlockState shellState, BlockGetter level, BlockPos pos) {
        return RegenCorruptionFallback.miningSampleFor((RegenVisual)((Object)shellState.m_61143_(VISUAL)), level, pos);
    }

    public float getExplosionResistance(BlockState state, BlockGetter level, BlockPos pos, Explosion explosion) {
        BlockState sample = RegenBlocks.miningSample(state, level, pos);
        return sample.m_60734_().getExplosionResistance(sample, level, pos, explosion);
    }

    public SoundType getSoundType(BlockState state, LevelReader level, BlockPos pos, @Nullable Entity entity) {
        BlockState sample = RegenBlocks.miningSample(state, (BlockGetter)level, pos);
        return sample.m_60734_().getSoundType(sample, level, pos, entity);
    }

    public float m_5880_(BlockState state, Player player, BlockGetter level, BlockPos pos) {
        BlockState sample = RegenBlocks.miningSample(state, level, pos);
        return sample.m_60625_(player, level, pos);
    }

    public boolean canHarvestBlock(BlockState state, BlockGetter level, BlockPos pos, Player player) {
        BlockState sample = RegenBlocks.miningSample(state, level, pos);
        return sample.canHarvestBlock(level, pos, player);
    }

    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> m_142354_(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.m_5776_()) {
            return null;
        }
        BlockEntityType<RegenBlockEntity> expected = this.blockEntityType.get();
        if (expected != type) {
            return null;
        }
        return (lvl, pos, st, te) -> RegenBlockEntity.tick(lvl, pos, st, (RegenBlockEntity)te);
    }

    protected void m_7926_(StateDefinition.Builder<Block, BlockState> builder) {
        builder.m_61104_(new Property[]{VISUAL, AXIS});
    }
}

