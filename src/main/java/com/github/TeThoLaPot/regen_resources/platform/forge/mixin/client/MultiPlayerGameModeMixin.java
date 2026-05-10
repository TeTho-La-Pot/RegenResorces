/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.multiplayer.MultiPlayerGameMode
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.level.block.state.BlockState
 *  org.spongepowered.asm.mixin.Final
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package com.github.TeThoLaPot.regen_resources.platform.forge.mixin.client;

import com.github.TeThoLaPot.regen_resources.platform.forge.client.RegenShellClientBreakRules;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={MultiPlayerGameMode.class})
public abstract class MultiPlayerGameModeMixin {
    @Shadow
    @Final
    private Minecraft minecraft;
    @Shadow
    private float destroyProgress;

    @Inject(method={"destroyBlock"}, at={@At(value="HEAD")}, cancellable=true)
    private void regen_resources$cancelPredictedDestroyAtLastMoment(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (this.minecraft.player == null || this.minecraft.level == null) {
            return;
        }
        BlockState st = this.minecraft.level.getBlockState(pos);
        if (RegenShellClientBreakRules.shouldBlockSurvivalRegenBreak((Player)this.minecraft.player, st)) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method={"continueDestroyBlock"}, at={@At(value="TAIL")})
    private void regen_resources$rollbackProgressAfterDeniedDestroy(BlockPos pos, Direction face, CallbackInfoReturnable<Boolean> cir) {
        if (this.minecraft.player == null || this.minecraft.level == null) {
            return;
        }
        BlockState st = this.minecraft.level.getBlockState(pos);
        if (!RegenShellClientBreakRules.shouldBlockSurvivalRegenBreak((Player)this.minecraft.player, st)) {
            return;
        }
        if (this.destroyProgress >= 1.0f) {
            this.destroyProgress = 0.998f;
        }
    }
}

