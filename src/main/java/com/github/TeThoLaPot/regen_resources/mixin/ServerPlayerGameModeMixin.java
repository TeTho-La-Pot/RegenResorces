package com.github.TeThoLaPot.regen_resources.mixin;

import com.github.TeThoLaPot.regen_resources.util.RegenHarvestContext;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayerGameMode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerGameMode.class)
public class ServerPlayerGameModeMixin {

    @Inject(method = "destroyBlock", at = @At("RETURN"))
    private void regen_resources$clearHarvestContext(BlockPos ignored, CallbackInfoReturnable<Boolean> cir) {
        RegenHarvestContext.clear();
    }
}
