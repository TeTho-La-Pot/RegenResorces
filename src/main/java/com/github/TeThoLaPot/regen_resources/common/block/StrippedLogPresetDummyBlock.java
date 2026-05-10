package com.github.TeThoLaPot.regen_resources.common.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Supplier;

/** 原木・木を持って使用すると、対応する棒付き見た目になる建築用ダミー。 */
public final class StrippedLogPresetDummyBlock extends AbstractPresetDummyBlock {

    public StrippedLogPresetDummyBlock(Supplier<BlockEntityType<RegenBlockEntity>> blockEntityType, Properties properties) {
        super(blockEntityType, RegenVisual.LOG_PRESET, properties);
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
        BlockState heldDefault = heldBlock.defaultBlockState();
        if (!heldDefault.is(BlockTags.LOGS)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        ResourceLocation heldId = BuiltInRegistries.BLOCK.getKey(heldBlock);
        if (heldId == null) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        ResourceLocation strippedId = strippedCounterpartId(heldId);
        if (strippedId == null) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        Optional<Block> strippedHolder = BuiltInRegistries.BLOCK.getOptional(strippedId);
        if (strippedHolder.isEmpty()) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        Block strippedBlock = strippedHolder.get();
        BlockState strippedState = strippedBlock.defaultBlockState();
        if (heldDefault.hasProperty(RotatedPillarBlock.AXIS) && strippedState.hasProperty(RotatedPillarBlock.AXIS)) {
            strippedState =
                    strippedState.setValue(RotatedPillarBlock.AXIS, heldDefault.getValue(RotatedPillarBlock.AXIS));
        }
        RegenBlockEntity be = blockEntity(level, pos);
        if (be != null) {
            be.setMimicAppearance(strippedState);
            be.syncToClients();
        }
        return ItemInteractionResult.sidedSuccess(level.isClientSide());
    }

    /**
     * {@code oak_log → stripped_oak_log}、{@code crimson_stem → stripped_crimson_stem} のように {@code stripped_<path>} を試す。
     */
    @Nullable
    static ResourceLocation strippedCounterpartId(ResourceLocation logId) {
        String path = logId.getPath();
        String ns = logId.getNamespace();
        if (!(path.endsWith("_log") || path.endsWith("_stem") || path.endsWith("_wood"))) {
            return null;
        }
        return ResourceLocation.fromNamespaceAndPath(ns, "stripped_" + path);
    }
}
