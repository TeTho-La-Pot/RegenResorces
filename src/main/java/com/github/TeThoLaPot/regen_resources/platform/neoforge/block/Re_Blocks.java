package com.github.TeThoLaPot.regen_resources.platform.neoforge.block;

import com.github.TeThoLaPot.regen_resources.RegenResources;
import com.github.TeThoLaPot.regen_resources.common.block.PresetAppearanceDummyBlock;
import com.github.TeThoLaPot.regen_resources.common.block.RegenBlockEntity;
import com.github.TeThoLaPot.regen_resources.common.block.RegenBlocks;
import com.github.TeThoLaPot.regen_resources.common.block.RegenVisual;
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

    public static final DeferredHolder<Block, PresetAppearanceDummyBlock> PRESET_DUMMY_STONE = BLOCKS.register(
            "preset_dummy_stone",
            () -> new PresetAppearanceDummyBlock(RegenVisual.STONE_PRESET, dummyProps()));
    public static final DeferredHolder<Block, PresetAppearanceDummyBlock> PRESET_DUMMY_DEEPSLATE = BLOCKS.register(
            "preset_dummy_deepslate",
            () -> new PresetAppearanceDummyBlock(RegenVisual.DEEPSLATE_PRESET, dummyProps()));
    public static final DeferredHolder<Block, PresetAppearanceDummyBlock> PRESET_DUMMY_NETHER = BLOCKS.register(
            "preset_dummy_nether",
            () -> new PresetAppearanceDummyBlock(RegenVisual.NETHER_PRESET, dummyProps()));
    public static final DeferredHolder<Block, PresetAppearanceDummyBlock> PRESET_DUMMY_END = BLOCKS.register(
            "preset_dummy_end",
            () -> new PresetAppearanceDummyBlock(RegenVisual.END_PRESET, dummyProps()));
    public static final DeferredHolder<Block, PresetAppearanceDummyBlock> PRESET_DUMMY_DEBRIS = BLOCKS.register(
            "preset_dummy_debris",
            () -> new PresetAppearanceDummyBlock(RegenVisual.DEBRIS_PRESET, dummyProps()));
    public static final DeferredHolder<Block, PresetAppearanceDummyBlock> PRESET_DUMMY_STRIPPED_LOG = BLOCKS.register(
            "preset_dummy_stripped_log",
            () -> new PresetAppearanceDummyBlock(RegenVisual.LOG_PRESET, dummyProps()));
    /** カスタムプリセット用の建築ダミー（採掘挙動は石プリセットに合わせる）。 */
    public static final DeferredHolder<Block, PresetAppearanceDummyBlock> PRESET_DUMMY_CUSTOM = BLOCKS.register(
            "preset_dummy_custom",
            () -> new PresetAppearanceDummyBlock(RegenVisual.STONE_PRESET, dummyProps()));

    private static BlockBehaviour.Properties dummyProps() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.STONE)
                .strength(1.5F, 6.0F)
                .requiresCorrectToolForDrops();
    }

    static {
        REGEN_BLOCK_ENTITY_HOLDER[0] = REGEN_BLOCK_ENTITY;
    }

    private Re_Blocks() {}
}
