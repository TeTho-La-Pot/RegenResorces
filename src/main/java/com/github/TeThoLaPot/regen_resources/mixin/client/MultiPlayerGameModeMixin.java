package com.github.TeThoLaPot.regen_resources.mixin.client;

import com.github.TeThoLaPot.regen_resources.forge.client.RegenShellClientBreakRules;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MultiPlayerGameMode.class)
public abstract class MultiPlayerGameModeMixin {

    @Shadow
    @Final
    private Minecraft minecraft;

    @Shadow
    private float destroyProgress;

    @Inject(method = "destroyBlock", at = @At("HEAD"), cancellable = true)
    private void regen_resources$cancelPredictedDestroyAtLastMoment(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (minecraft.player == null || minecraft.level == null) {
            return;
        }
        BlockState st = minecraft.level.getBlockState(pos);
        if (RegenShellClientBreakRules.shouldBlockSurvivalRegenBreak(minecraft.player, st)) {
            cir.setReturnValue(false);
        }
    }

    /**
     * 進捗が 1 に達したとき {@link #regen_resources$cancelPredictedDestroyAtLastMoment} で破壊は却下されるが、
     * ローカル進捗が 1 のまま残ると毎ティック {@code destroyBlock} が呼ばれ続けるため、ギリギリ手前に戻す。
     */
    @Inject(method = "continueDestroyBlock", at = @At("TAIL"))
    private void regen_resources$rollbackProgressAfterDeniedDestroy(
            BlockPos pos, Direction face, CallbackInfoReturnable<Boolean> cir) {
        if (minecraft.player == null || minecraft.level == null) {
            return;
        }
        BlockState st = minecraft.level.getBlockState(pos);
        if (!RegenShellClientBreakRules.shouldBlockSurvivalRegenBreak(minecraft.player, st)) {
            return;
        }
        if (destroyProgress >= 1.0F) {
            destroyProgress = 0.998F;
        }
    }
}
