package com.github.TeThoLaPot.regen_resources.mixin;

import com.github.TeThoLaPot.regen_resources.init.block.Re_Blocks;
import com.github.TeThoLaPot.regen_resources.init.block.RegenBlocks;
import com.github.TeThoLaPot.regen_resources.util.RegenMiningHelpers;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * {@code requiresCorrectToolForDrops()} があると Forge は HarvestCheck に回さずこのメソッドだけを見る。
 * 再生ブロックだけミミックのタグ Tier に丸める。
 */
@Mixin(Player.class)
public abstract class PlayerMixin {

    @Inject(method = "hasCorrectToolForDrops", at = @At("HEAD"), cancellable = true)
    private void regen_resources$delegateToolTier(BlockState state, CallbackInfoReturnable<Boolean> cir) {
        if (!state.is(Re_Blocks.REGEN_BLOCK.get())) {
            return;
        }
        Player self = (Player) (Object) this;
        BlockPos pos = RegenMiningHelpers.regenBlockTargetedByPick(self);
        if (pos == null) {
            return;
        }
        BlockState mimic = RegenBlocks.mimicStateAt(self.level(), pos);
        cir.setReturnValue(self.hasCorrectToolForDrops(mimic));
    }
}
