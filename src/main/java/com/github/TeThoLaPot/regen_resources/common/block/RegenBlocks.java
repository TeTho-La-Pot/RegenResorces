package com.github.TeThoLaPot.regen_resources.common.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

/**
 * プリセットごとに見た目だけ切り替わるブロック。
 */
public class RegenBlocks extends Block implements EntityBlock {

    public static final EnumProperty<RegenVisual> VISUAL =
            EnumProperty.create("visual", RegenVisual.class);

    private final Supplier<BlockEntityType<RegenBlockEntity>> blockEntityType;

    public RegenBlocks(Supplier<BlockEntityType<RegenBlockEntity>> blockEntityType, Properties properties) {
        super(properties);
        this.blockEntityType = blockEntityType;
        registerDefaultState(stateDefinition.any().setValue(VISUAL, RegenVisual.STONE_PRESET));
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new RegenBlockEntity(blockEntityType.get(), pos, state);
    }

    private static BlockState miningSample(BlockState shellState) {
        return RegenCorruptionFallback.miningSampleFor(shellState.getValue(VISUAL));
    }

    @Override
    public float getExplosionResistance(BlockState state, BlockGetter level, BlockPos pos, Explosion explosion) {
        BlockState sample = miningSample(state);
        return sample.getBlock().getExplosionResistance(sample, level, pos, explosion);
    }

    @Override
    public SoundType getSoundType(BlockState state, LevelReader level, BlockPos pos, @Nullable Entity entity) {
        BlockState sample = miningSample(state);
        return sample.getBlock().getSoundType(sample, level, pos, entity);
    }

    @Nullable
    @Override
    @SuppressWarnings("unchecked")
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide()) {
            return null;
        }
        BlockEntityType<RegenBlockEntity> expected = blockEntityType.get();
        if (expected != type) {
            return null;
        }
        return (BlockEntityTicker<T>) (Level lvl, BlockPos pos, BlockState st, T te) ->
                RegenBlockEntity.tick(lvl, pos, st, (RegenBlockEntity) te);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(VISUAL);
    }
}
