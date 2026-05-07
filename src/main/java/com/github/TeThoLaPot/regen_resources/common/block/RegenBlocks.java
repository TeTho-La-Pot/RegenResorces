package com.github.TeThoLaPot.regen_resources.common.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;

/**
 * プリセットごとに見た目だけ切り替わるブロック。
 */
public class RegenBlocks extends Block {

    public static final EnumProperty<RegenVisual> VISUAL =
            EnumProperty.create("visual", RegenVisual.class);

    public RegenBlocks(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(VISUAL, RegenVisual.STONE_PRESET));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(VISUAL);
    }
}
