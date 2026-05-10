package com.github.TeThoLaPot.regen_resources.common.block;

import com.github.TeThoLaPot.regen_resources.common.regen.RegenRule;
import com.github.TeThoLaPot.regen_resources.common.regen.RegenRuleRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import java.util.List;
import java.util.function.Supplier;

/** {@code custom_preset} に登録されたブロックを持って使用するとその見た目になる建築用ダミー（複数エントリは繰り返し使用で順に切替）。 */
public final class CustomPresetDummyBlock extends AbstractPresetDummyBlock {

    public CustomPresetDummyBlock(Supplier<BlockEntityType<RegenBlockEntity>> blockEntityType, Properties properties) {
        super(blockEntityType, RegenVisual.STONE_PRESET, properties);
    }

    @Override
    protected ItemInteractionResult useItemOn(
            ItemStack stack,
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            InteractionHand hand,
            BlockHitResult hitResult) {
        if (!(stack.getItem() instanceof BlockItem blockItem)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        Block heldBlock = blockItem.getBlock();
        List<RegenRule> matches = RegenRuleRegistry.matchingCustomPresetRules(heldBlock);
        if (matches.isEmpty()) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        RegenBlockEntity be = blockEntity(level, pos);
        if (be == null) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        BlockState mimic = heldBlock.defaultBlockState();
        be.setMimicAppearance(mimic);
        be.advanceCustomPresetCycle();
        be.syncToClients();
        return ItemInteractionResult.sidedSuccess(level.isClientSide());
    }
}
