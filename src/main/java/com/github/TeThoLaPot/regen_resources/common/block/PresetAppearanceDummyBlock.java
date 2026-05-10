package com.github.TeThoLaPot.regen_resources.common.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * 建築用プリセット見た目のダミー。採掘速度・音・爆発耐性は対応するプリセット資源に合わせる。
 */
public final class PresetAppearanceDummyBlock extends Block {

    private final RegenVisual appearanceVisual;

    public PresetAppearanceDummyBlock(RegenVisual appearanceVisual, Properties properties) {
        super(properties);
        this.appearanceVisual = appearanceVisual;
    }

    private BlockState sample() {
        return RegenCorruptionFallback.miningSampleFor(appearanceVisual);
    }

    @Override
    public float getExplosionResistance(BlockState state, BlockGetter level, BlockPos pos, Explosion explosion) {
        BlockState s = sample();
        return s.getBlock().getExplosionResistance(s, level, pos, explosion);
    }

    @Override
    public SoundType getSoundType(BlockState state, LevelReader level, BlockPos pos, @Nullable Entity entity) {
        BlockState s = sample();
        return s.getBlock().getSoundType(s, level, pos, entity);
    }

    @Override
    public float getDestroyProgress(BlockState state, Player player, BlockGetter level, BlockPos pos) {
        BlockState s = sample();
        return s.getDestroyProgress(player, level, pos);
    }

    @Override
    public boolean canHarvestBlock(BlockState state, BlockGetter level, BlockPos pos, Player player) {
        BlockState s = sample();
        return s.canHarvestBlock(level, pos, player);
    }
}
