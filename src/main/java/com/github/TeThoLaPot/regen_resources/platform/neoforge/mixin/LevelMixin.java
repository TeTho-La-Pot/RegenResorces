package com.github.TeThoLaPot.regen_resources.platform.neoforge.mixin;

import com.github.TeThoLaPot.regen_resources.common.tt.RegenBlockMoveHooks;
import com.github.TeThoLaPot.regen_resources.common.tt.RegenSetBlockTtGuard;
import java.util.ArrayDeque;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * {@code Level#setBlock} が成功したあと、その座標の再生用 TT を整合させる。
 * <p>RETURN にしているのは {@code setBlock==false} のときに誤って触らないため。
 * 1.20.1 と同様、{@link Block#UPDATE_MOVE_BY_PISTON}（64）でピストン経路を検出する。
 * <p>補足（1.21）：{@link net.minecraft.world.level.block.piston.PistonMovingBlockEntity#finalTick()} は
 * {@code setBlock(..., 3)} とし 64 を立てない。{@code HEAD} で退避した旧状態が {@link Blocks#MOVING_PISTON}
 * なら、フラグに依存せずピストンによる確定配置とみなす（再帰呼び出しは {@link ArrayDeque} で整合）。
 */
@Mixin(Level.class)
public abstract class LevelMixin {

    @Unique
    private static final ThreadLocal<ArrayDeque<BlockState>> REGEN_RESOURCES$PRE_SETBLOCK =
            ThreadLocal.withInitial(ArrayDeque::new);

    @Inject(
            method = "setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;II)Z",
            at = @At("HEAD"))
    private void regen_resources$rememberPreSetBlock(
            BlockPos pos, BlockState state, int flags, int recursionLeft, CallbackInfoReturnable<Boolean> cir) {
        Level level = (Level) (Object) this;
        REGEN_RESOURCES$PRE_SETBLOCK.get().push(level.getBlockState(pos));
    }

    @Inject(
            method = "setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;II)Z",
            at = @At("RETURN"))
    private void regen_resources$afterSuccessfulSetBlock(
            BlockPos pos,
            BlockState state,
            int flags,
            int recursionLeft,
            CallbackInfoReturnable<Boolean> cir) {
        ArrayDeque<BlockState> stack = REGEN_RESOURCES$PRE_SETBLOCK.get();
        BlockState oldSnapshot = stack.isEmpty() ? null : stack.pop();

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
        boolean pistonFlag = (flags & Block.UPDATE_MOVE_BY_PISTON) != 0;
        BlockState placed = serverLevel.getBlockState(pos);
        boolean pistonLike =
                pistonFlag
                        || (oldSnapshot != null
                                && oldSnapshot.is(Blocks.MOVING_PISTON)
                                && !placed.isAir());
        RegenBlockMoveHooks.afterMutation(serverLevel, pos, placed, pistonLike);
    }
}
