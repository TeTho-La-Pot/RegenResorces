package com.github.TeThoLaPot.regen_resources.common.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Jade がサーバー同期データを取得できるようにするためのプレースホルダ。
 * 再生状態そのものは {@link com.github.TeThoLaPot.tt_core.TT_core} に保存される。
 */
public final class RegenBlockEntity extends BlockEntity {

    public RegenBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }
}
