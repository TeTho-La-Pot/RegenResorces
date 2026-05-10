package com.github.TeThoLaPot.regen_resources.platform.neoforge.mixin;

import com.github.TeThoLaPot.regen_resources.common.tt.RegenBlockMoveHooks;
import com.github.TeThoLaPot.regen_resources.common.tt.RegenSetBlockTtGuard;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * {@code Level#setBlock} が成功したあと、その座標の再生用 TT を整合させる。
 * <p>RETURN にしているのは {@code setBlock==false} のときに誤って触らないため。
 * Regen_Ore 1.4.6 系と同様にピストン由来は {@link Block#UPDATE_MOVE_BY_PISTON}（値 64）で判別し、
 * 移動先にブロックがある場合は {@link com.github.TeThoLaPot.regen_resources.common.regen.RegenMineMarker#SRC_SURVIVAL}
 * を載せて「自然再生コンフィグ許可でも掘っても再生しない」状態にする。
 */
@Mixin(Level.class)
public abstract class LevelMixin {

    @Inject(
            method = "setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;II)Z",
            at = @At("RETURN"))
    private void regen_resources$afterSuccessfulSetBlock(
            BlockPos pos,
            BlockState state,
            int flags,
            int recursionLeft,
            CallbackInfoReturnable<Boolean> cir) {
        if (!Boolean.TRUE.equals(cir.getReturnValue())) {
            return;
        }
        Level level = (Level) (Object) this;
        if (level.isClientSide() || !(level instanceof ServerLevel serverLevel)) {
            return;
        }
        if (RegenSetBlockTtGuard.isSuppressed()) {
            return;
        }
        boolean piston = (flags & Block.UPDATE_MOVE_BY_PISTON) != 0;
        RegenBlockMoveHooks.afterMutation(serverLevel, pos, state, piston);
    }
}
