package com.github.TeThoLaPot.regen_resources.common.block;

import com.github.TeThoLaPot.regen_resources.common.regen.RegenRule;
import com.github.TeThoLaPot.regen_resources.common.regen.RegenRuleRegistry;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public final class CustomPresetDummyBlock extends Block implements EntityBlock {
    private final Supplier<BlockEntityType<RegenBlockEntity>> blockEntityType;

    public CustomPresetDummyBlock(
            Supplier<BlockEntityType<RegenBlockEntity>> blockEntityType, BlockBehaviour.Properties properties) {
        super(properties);
        this.blockEntityType = blockEntityType;
    }

    private BlockState miningSample(BlockGetter level, BlockPos pos) {
        return RegenCorruptionFallback.miningSampleFor(RegenVisual.CUSTOM_PRESET, level, pos);
    }

    public float getExplosionResistance(BlockState state, BlockGetter level, BlockPos pos, Explosion explosion) {
        BlockState sample = this.miningSample(level, pos);
        return sample.getBlock().getExplosionResistance(sample, level, pos, explosion);
    }

    public SoundType getSoundType(BlockState state, LevelReader level, BlockPos pos, @Nullable Entity entity) {
        BlockState sample = this.miningSample(level, pos);
        return sample.getBlock().getSoundType(sample, level, pos, entity);
    }

    public float getDestroyProgress(BlockState state, Player player, BlockGetter level, BlockPos pos) {
        BlockState sample = this.miningSample(level, pos);
        return sample.getDestroyProgress(player, level, pos);
    }

    public boolean canHarvestBlock(BlockState state, BlockGetter level, BlockPos pos, Player player) {
        BlockState sample = this.miningSample(level, pos);
        return sample.canHarvestBlock(level, pos, player);
    }

    @Nullable
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new RegenBlockEntity(this.blockEntityType.get(), pos, state);
    }

    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return null;
    }

    @Nullable
    private static BlockState heldBlockState(Player player) {
        ItemStack stack = player.getItemInHand(InteractionHand.MAIN_HAND);
        if (stack.isEmpty()) {
            return null;
        }
        Block heldBlock = Block.byItem(stack.getItem());
        if (heldBlock.defaultBlockState().isAir()) {
            return null;
        }
        return heldBlock.defaultBlockState();
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
        if (hand != InteractionHand.MAIN_HAND) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        BlockState heldState = heldBlockState(player);
        if (heldState == null) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        List<RegenRule> matches = RegenRuleRegistry.allCustomPresetMatchesIgnoringDimension(heldState);
        if (matches.isEmpty()) {
            // クライアントにルールが無いときはサーバ判定に任せる（設置はサーバが止める）
            if (level.isClientSide() && RegenRuleRegistry.rules().isEmpty()) {
                return ItemInteractionResult.SUCCESS;
            }
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        if (level.isClientSide()) {
            // マッチ時は設置をキャンセル
            return ItemInteractionResult.SUCCESS;
        }

        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof RegenBlockEntity rbe)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        ResourceLocation heldKey = BuiltInRegistries.BLOCK.getKey(heldState.getBlock());
        RegenCustomVisualSpec nextSpec;
        if (matches.size() == 1) {
            nextSpec = matches.get(0).customVisualSpec();
            if (nextSpec == null) {
                return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
            }
            if (!Objects.equals(nextSpec, rbe.getCustomVisualSpec())) {
                rbe.resetCustomPresetDummyCycle();
                rbe.setCustomVisualSpec(nextSpec);
            }
            // 既に同じ見た目でも CONSUME（設置キャンセル）
            return ItemInteractionResult.CONSUME;
        }

        if (!Objects.equals(heldKey, rbe.customPresetDummyCycleTarget())) {
            rbe.setCustomPresetDummyCycleTarget(heldKey);
            rbe.setCustomPresetDummyCycleIndex(-1);
        }
        int nextIdx = (rbe.customPresetDummyCycleIndex() + 1) % matches.size();
        nextSpec = matches.get(nextIdx).customVisualSpec();
        if (nextSpec == null) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        rbe.setCustomPresetDummyCycleIndex(nextIdx);
        rbe.setCustomPresetDummyCycleTarget(heldKey);
        rbe.setCustomVisualSpec(nextSpec);
        return ItemInteractionResult.CONSUME;
    }
}
