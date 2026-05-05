package com.github.TeThoLaPot.regen_resources.mixin;

import com.github.TeThoLaPot.regen_resources.Config;
import com.github.TeThoLaPot.regen_resources.init.block.Re_Blocks;
import com.github.TeThoLaPot.tt_core.TT_core;
import com.github.TeThoLaPot.tt_core.data.GlobalSavedData;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * TT への書き込みは行わず、再生対象マスで別ブロックに置き換わったときだけ座標データを消す。
 * 鉱石まわりの TT は {@link com.github.TeThoLaPot.regen_resources.forge.RegenGameplayEvents} の再生経路に限定する。
 * <p>
 * {@link TT_core#removeBlockData} を同期 {@code setBlock} 内で呼ぶと、スポーン準備やチャンク保存と SavedData /
 * チャンクのロックが交差してメインスレッドが分単位で止まることがある。削除だけ {@link MinecraftServer#execute} に逃がす。
 */
@Mixin(Level.class)
public abstract class LevelMixin {

    @Inject(method = "setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;II)Z", at = @At("HEAD"))
    private void onSetBlock(BlockPos pos, BlockState state, int flags, int recursionLeft, CallbackInfoReturnable<Boolean> cir) {
        if (!(((Object) this) instanceof ServerLevel serverLevel)) {
            return;
        }
        if (state.isAir()) {
            return;
        }

        if (state.is(Re_Blocks.REGEN_BLOCK.get())) {
            return;
        }

        GlobalSavedData global = GlobalSavedData.get(serverLevel);
        if (!global.hasAnyPosBlockData()) {
            return;
        }
        if (!global.chunkMayHoldPosBlockData(pos)) {
            return;
        }

        if (!global.containsPosBlockData(pos.asLong())) {
            return;
        }

        BlockState beforeReplace = serverLevel.getBlockState(pos);
        boolean regenRelevant = beforeReplace.is(Re_Blocks.REGEN_BLOCK.get())
                || Config.isRegenTarget(serverLevel, beforeReplace)
                || beforeReplace.isAir();
        if (!regenRelevant) {
            return;
        }

        BlockPos im = pos.immutable();
        BlockState planned = state;
        serverLevel.getServer().execute(() -> {
            if (!serverLevel.getBlockState(im).equals(planned)) {
                return;
            }
            if (!TT_core.hasBlockData(serverLevel, im)) {
                return;
            }
            TT_core.removeBlockData(serverLevel, im);
        });
    }
}
