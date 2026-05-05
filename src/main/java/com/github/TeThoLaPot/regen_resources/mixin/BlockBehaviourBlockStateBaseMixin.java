package com.github.TeThoLaPot.regen_resources.mixin;

import com.github.TeThoLaPot.regen_resources.init.block.Re_Blocks;
import com.github.TeThoLaPot.regen_resources.init.block.RegenBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Forge の進捗式は BlockState の getDestroySpeed(世界, pos) を使う。静的 hardness の再生ブロックはここだけ差し替えると採掘速度がミミックに一致する。
 */
@Mixin(BlockBehaviour.BlockStateBase.class)
public abstract class BlockBehaviourBlockStateBaseMixin {

    @Inject(
            method = "getDestroySpeed(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;)F",
            at = @At("HEAD"),
            cancellable = true)
    private void regen_resources$mimicDestroySpeed(BlockGetter level, BlockPos pos,
            CallbackInfoReturnable<Float> cir) {
        BlockState state = (BlockState) (Object) this;
        if (!state.is(Re_Blocks.REGEN_BLOCK.get()) || level == null || pos == null) {
            return;
        }
        BlockState mimic = RegenBlocks.mimicStateAt(level, pos, state);
        cir.setReturnValue(mimic.getDestroySpeed(level, pos));
    }
}
