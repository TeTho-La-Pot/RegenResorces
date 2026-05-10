/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.level.chunk.LevelChunk
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package com.github.TeThoLaPot.regen_resources.platform.forge.mixin;

import com.github.TeThoLaPot.regen_resources.common.tt.RegenBlockMoveHooks;
import com.github.TeThoLaPot.regen_resources.common.tt.RegenSetBlockTtGuard;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={LevelChunk.class})
public abstract class LevelChunkMixin {
    @Inject(method={"setBlockState(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Z)Lnet/minecraft/world/level/block/state/BlockState;"}, at={@At(value="RETURN")})
    private void regen_resources$afterChunkSetBlockState(BlockPos pos, BlockState state, boolean isMovedByPiston, CallbackInfoReturnable<BlockState> cir) {
        if (!isMovedByPiston) {
            return;
        }
        if (cir.getReturnValue() == null) {
            return;
        }
        LevelChunk chunk = (LevelChunk)this;
        Level level = chunk.m_62953_();
        if (level.m_5776_() || !(level instanceof ServerLevel)) {
            return;
        }
        ServerLevel serverLevel = (ServerLevel)level;
        if (RegenSetBlockTtGuard.isSuppressed()) {
            return;
        }
        RegenBlockMoveHooks.afterMutation(serverLevel, pos.m_7949_(), state, true);
    }
}

