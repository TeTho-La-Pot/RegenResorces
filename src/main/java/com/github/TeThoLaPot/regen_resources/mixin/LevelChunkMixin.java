package com.github.TeThoLaPot.regen_resources.mixin;

import com.github.TeThoLaPot.regen_resources.common.tt.RegenBlockMoveHooks;
import com.github.TeThoLaPot.regen_resources.common.tt.RegenSetBlockTtGuard;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * {@link Level#setBlock} が経由しないピストン系更新を拾う（{@code LevelChunk#setBlockState(..., movedByPiston)}）。
 */
@Mixin(LevelChunk.class)
public abstract class LevelChunkMixin {

    @Inject(
            method = "setBlockState(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Z)Lnet/minecraft/world/level/block/state/BlockState;",
            at = @At("RETURN"))
    private void regen_resources$afterChunkSetBlockState(
            BlockPos pos,
            BlockState state,
            boolean isMovedByPiston,
            CallbackInfoReturnable<BlockState> cir) {
        if (!isMovedByPiston) {
            return;
        }
        if (cir.getReturnValue() == null) {
            return;
        }
        LevelChunk chunk = (LevelChunk) (Object) this;
        Level level = chunk.getLevel();
        if (level.isClientSide() || !(level instanceof ServerLevel serverLevel)) {
            return;
        }
        if (RegenSetBlockTtGuard.isSuppressed()) {
            return;
        }
        RegenBlockMoveHooks.afterMutation(serverLevel, pos.immutable(), state, true);
    }
}
