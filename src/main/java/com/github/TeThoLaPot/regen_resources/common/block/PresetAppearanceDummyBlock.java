/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.level.BlockGetter
 *  net.minecraft.world.level.Explosion
 *  net.minecraft.world.level.LevelReader
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.SoundType
 *  net.minecraft.world.level.block.state.BlockBehaviour$Properties
 *  net.minecraft.world.level.block.state.BlockState
 *  org.jetbrains.annotations.Nullable
 */
package com.github.TeThoLaPot.regen_resources.common.block;

import com.github.TeThoLaPot.regen_resources.common.block.RegenCorruptionFallback;
import com.github.TeThoLaPot.regen_resources.common.block.RegenVisual;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class PresetAppearanceDummyBlock
extends Block {
    private final RegenVisual appearance;

    public PresetAppearanceDummyBlock(RegenVisual appearance, BlockBehaviour.Properties properties) {
        super(properties);
        this.appearance = appearance;
    }

    public RegenVisual appearance() {
        return this.appearance;
    }

    protected BlockState miningSample(BlockGetter level, BlockPos pos) {
        return RegenCorruptionFallback.miningSampleFor(this.appearance, level, pos);
    }

    public float getExplosionResistance(BlockState state, BlockGetter level, BlockPos pos, Explosion explosion) {
        BlockState sample = this.miningSample(level, pos);
        return sample.getBlock().getExplosionResistance(sample, level, pos, explosion);
    }

    public SoundType getSoundType(BlockState state, LevelReader level, BlockPos pos, @Nullable Entity entity) {
        BlockState sample = this.miningSample((BlockGetter)level, pos);
        return sample.getBlock().getSoundType(sample, level, pos, entity);
    }

    public float getDestroyProgress(BlockState state, Player player, BlockGetter level, BlockPos pos) {
        BlockState sample = this.miningSample(level, pos);
        return sample.getDestroyProgress(player, level, pos);
    }

    public boolean canHarvestBlock(BlockState state, BlockGetter level, BlockPos pos, Player player) {
        BlockState sample = this.miningSample(level, pos);
        return sample.canHarvestBlock(level, pos, player);
    }
}

