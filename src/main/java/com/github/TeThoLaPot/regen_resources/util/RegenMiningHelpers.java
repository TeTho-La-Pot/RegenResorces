package com.github.TeThoLaPot.regen_resources.util;

import com.github.TeThoLaPot.regen_resources.init.block.Re_Blocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.common.ForgeMod;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/** 視線由来の再生ブロック座標解決など、採掘・ツール判定の共通処理。 */
public final class RegenMiningHelpers {

    private RegenMiningHelpers() {}

    private static double blockReach(Player player) {
        AttributeInstance reachAttr = player.getAttribute(ForgeMod.BLOCK_REACH.get());
        return reachAttr != null ? reachAttr.getValue() : 4.5D;
    }

    /**
     * クロスヘアが当たっている {@link Re_Blocks#REGEN_BLOCK} の座標。無ければ null。
     * 同一ティック内の複数回呼び出しではレイキャストは 1 回に抑える（{@link RegenMiningRayTickCache}）。
     */
    public static @Nullable BlockPos regenBlockTargetedByPick(Player player) {
        return RegenMiningRayTickCache.getOrCompute(
                player.getUUID(),
                player.level().isClientSide(),
                () -> Optional.ofNullable(regenBlockTargetedByPickUncached(player))
        ).orElse(null);
    }

    private static @Nullable BlockPos regenBlockTargetedByPickUncached(Player player) {
        BlockPos fromDig = RegenBreakDigContext.peek(player);
        if (fromDig != null && player.level().getBlockState(fromDig).is(Re_Blocks.REGEN_BLOCK.get())) {
            return fromDig;
        }
        HitResult hit = player.pick(blockReach(player), 1f, false);
        if (!(hit instanceof BlockHitResult bhr) || bhr.getType() != HitResult.Type.BLOCK) {
            return null;
        }
        BlockPos pos = bhr.getBlockPos();
        if (!player.level().getBlockState(pos).is(Re_Blocks.REGEN_BLOCK.get())) {
            return null;
        }
        return pos;
    }
}
