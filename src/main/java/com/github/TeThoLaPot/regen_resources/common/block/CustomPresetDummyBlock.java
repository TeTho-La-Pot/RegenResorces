/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.registries.BuiltInRegistries
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.InteractionResult
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.BlockGetter
 *  net.minecraft.world.level.Explosion
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.LevelReader
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.EntityBlock
 *  net.minecraft.world.level.block.SoundType
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  net.minecraft.world.level.block.entity.BlockEntityTicker
 *  net.minecraft.world.level.block.entity.BlockEntityType
 *  net.minecraft.world.level.block.state.BlockBehaviour$Properties
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.phys.BlockHitResult
 *  org.jetbrains.annotations.Nullable
 */
package com.github.TeThoLaPot.regen_resources.common.block;

import com.github.TeThoLaPot.regen_resources.common.block.RegenBlockEntity;
import com.github.TeThoLaPot.regen_resources.common.block.RegenCorruptionFallback;
import com.github.TeThoLaPot.regen_resources.common.block.RegenCustomVisualSpec;
import com.github.TeThoLaPot.regen_resources.common.block.RegenVisual;
import com.github.TeThoLaPot.regen_resources.common.regen.RegenRule;
import com.github.TeThoLaPot.regen_resources.common.regen.RegenRuleRegistry;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
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

public final class CustomPresetDummyBlock
extends Block
implements EntityBlock {
    private final Supplier<BlockEntityType<RegenBlockEntity>> blockEntityType;

    public CustomPresetDummyBlock(Supplier<BlockEntityType<RegenBlockEntity>> blockEntityType, BlockBehaviour.Properties properties) {
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
        BlockState sample = this.miningSample((BlockGetter)level, pos);
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
        Block heldBlock = Block.byItem((Item)stack.getItem());
        if (heldBlock.defaultBlockState().isAir()) {
            return null;
        }
        return heldBlock.defaultBlockState();
    }

    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        int nextIdx;
        RegenCustomVisualSpec nextSpec;
        if (hand != InteractionHand.MAIN_HAND) {
            return InteractionResult.PASS;
        }
        BlockState heldState = CustomPresetDummyBlock.heldBlockState(player);
        if (heldState == null) {
            return InteractionResult.PASS;
        }
        ResourceLocation dim = level.dimension().location();
        List<RegenRule> matches = RegenRuleRegistry.allCustomPresetMatches(dim, heldState);
        if (matches.isEmpty()) {
            return InteractionResult.PASS;
        }
        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof RegenBlockEntity)) {
            return InteractionResult.PASS;
        }
        RegenBlockEntity rbe = (RegenBlockEntity)be;
        ResourceLocation heldKey = BuiltInRegistries.BLOCK.getKey(heldState.getBlock());
        if (matches.size() == 1) {
            RegenCustomVisualSpec spec = matches.get(0).customVisualSpec();
            if (spec == null) {
                return InteractionResult.PASS;
            }
            if (Objects.equals(spec, rbe.getCustomVisualSpec())) {
                return InteractionResult.PASS;
            }
            if (level.isClientSide) {
                return InteractionResult.SUCCESS;
            }
            rbe.resetCustomPresetDummyCycle();
            rbe.setCustomVisualSpec(spec);
            return InteractionResult.SUCCESS;
        }
        if (!Objects.equals(heldKey, rbe.customPresetDummyCycleTarget())) {
            rbe.setCustomPresetDummyCycleTarget(heldKey);
            rbe.setCustomPresetDummyCycleIndex(-1);
        }
        if ((nextSpec = matches.get(nextIdx = (rbe.customPresetDummyCycleIndex() + 1) % matches.size()).customVisualSpec()) == null) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        rbe.setCustomPresetDummyCycleIndex(nextIdx);
        rbe.setCustomPresetDummyCycleTarget(heldKey);
        rbe.setCustomVisualSpec(nextSpec);
        return InteractionResult.SUCCESS;
    }
}

