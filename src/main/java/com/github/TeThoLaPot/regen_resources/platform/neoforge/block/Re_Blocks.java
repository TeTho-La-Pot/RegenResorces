package com.github.TeThoLaPot.regen_resources.platform.neoforge.block;

import com.github.TeThoLaPot.regen_resources.RegenResources;
import com.github.TeThoLaPot.regen_resources.common.block.RegenBlockEntity;
import com.github.TeThoLaPot.regen_resources.common.block.RegenBlocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** NeoForge {@link DeferredRegister} でブロックと BlockEntity を登録。 */
public final class Re_Blocks {

    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(Registries.BLOCK, RegenResources.MOD_ID);

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, RegenResources.MOD_ID);

    @SuppressWarnings("unchecked")
    private static final DeferredHolder<BlockEntityType<?>, BlockEntityType<RegenBlockEntity>>[] REGEN_BLOCK_ENTITY_HOLDER =
            new DeferredHolder[1];

    public static final DeferredHolder<Block, RegenBlocks> REGEN_BLOCK = BLOCKS.register(
            "regen_block",
            () -> new RegenBlocks(() -> REGEN_BLOCK_ENTITY_HOLDER[0].get(), BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE)
                    .strength(1.5F, 6.0F)
                    .requiresCorrectToolForDrops()
                    .noLootTable()
            )
    );

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<RegenBlockEntity>> REGEN_BLOCK_ENTITY =
            BLOCK_ENTITY_TYPES.register(
                    "regen_block",
                    () -> BlockEntityType.Builder.of(
                                    (pos, state) -> new RegenBlockEntity(REGEN_BLOCK_ENTITY_HOLDER[0].get(), pos, state),
                                    REGEN_BLOCK.get())
                            .build(null));

    static {
        REGEN_BLOCK_ENTITY_HOLDER[0] = REGEN_BLOCK_ENTITY;
    }

    private Re_Blocks() {}
}
