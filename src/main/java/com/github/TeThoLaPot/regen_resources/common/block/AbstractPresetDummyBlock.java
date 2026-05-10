package com.github.TeThoLaPot.regen_resources.common.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

/**
 * 原木／カスタム用プリセットダミー共通：{@link RegenBlockEntity} に保存した見た目ブロックへ採掘・音・爆発耐性を合わせる。
 */
public abstract class AbstractPresetDummyBlock extends Block implements EntityBlock {

    private final Supplier<BlockEntityType<RegenBlockEntity>> blockEntityType;
    private final RegenVisual fallbackMiningVisual;

    protected AbstractPresetDummyBlock(
            Supplier<BlockEntityType<RegenBlockEntity>> blockEntityType,
            RegenVisual fallbackMiningVisual,
            Properties properties) {
        super(properties);
        this.blockEntityType = blockEntityType;
        this.fallbackMiningVisual = fallbackMiningVisual;
    }

    @Nullable
    protected RegenBlockEntity blockEntity(BlockGetter level, BlockPos pos) {
        BlockEntity be = level.getBlockEntity(pos);
        return be instanceof RegenBlockEntity r ? r : null;
    }

    protected BlockState miningSample(BlockGetter level, BlockPos pos) {
        RegenBlockEntity r = blockEntity(level, pos);
        if (r != null && r.getMimicAppearance() != null) {
            return r.getMimicAppearance();
        }
        return RegenCorruptionFallback.miningSampleFor(fallbackMiningVisual);
    }

    /**
     * チャンクメッシュは {@code models/block/preset_dummy_*.json} のプレースホルダ（面なし）のみ。
     * 実見た目はクライアント側 BlockEntityRenderer が描画する。
     */
    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new RegenBlockEntity(blockEntityType.get(), pos, state);
    }

    @Override
    public float getExplosionResistance(BlockState state, BlockGetter level, BlockPos pos, Explosion explosion) {
        BlockState s = miningSample(level, pos);
        return s.getBlock().getExplosionResistance(s, level, pos, explosion);
    }

    @Override
    public SoundType getSoundType(BlockState state, LevelReader level, BlockPos pos, @Nullable Entity entity) {
        BlockState s = miningSample(level, pos);
        return s.getBlock().getSoundType(s, level, pos, entity);
    }

    @Override
    public float getDestroyProgress(BlockState state, Player player, BlockGetter level, BlockPos pos) {
        BlockState s = miningSample(level, pos);
        return s.getDestroyProgress(player, level, pos);
    }

    @Override
    public boolean canHarvestBlock(BlockState state, BlockGetter level, BlockPos pos, Player player) {
        BlockState s = miningSample(level, pos);
        return s.canHarvestBlock(level, pos, player);
    }
}
