/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.entity.BlockEntityType
 *  net.minecraft.world.level.block.entity.BlockEntityType$Builder
 *  net.minecraft.world.level.block.state.BlockBehaviour$Properties
 *  net.minecraft.world.level.material.MapColor
 *  net.minecraftforge.registries.DeferredRegister
 *  net.minecraftforge.registries.ForgeRegistries
 *  net.minecraftforge.registries.IForgeRegistry
 *  net.minecraftforge.registries.RegistryObject
 */
package com.github.TeThoLaPot.regen_resources.platform.forge.block;

import com.github.TeThoLaPot.regen_resources.common.block.CustomPresetDummyBlock;
import com.github.TeThoLaPot.regen_resources.common.block.PresetAppearanceDummyBlock;
import com.github.TeThoLaPot.regen_resources.common.block.RegenBlockEntity;
import com.github.TeThoLaPot.regen_resources.common.block.RegenBlocks;
import com.github.TeThoLaPot.regen_resources.common.block.RegenVisual;
import com.github.TeThoLaPot.regen_resources.common.block.StrippedLogPresetDummyBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryObject;

public final class Re_Blocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create((IForgeRegistry)ForgeRegistries.BLOCKS, (String)"regen_resources");
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create((IForgeRegistry)ForgeRegistries.BLOCK_ENTITY_TYPES, (String)"regen_resources");
    private static final RegistryObject<BlockEntityType<RegenBlockEntity>>[] REGEN_BLOCK_ENTITY_HOLDER = new RegistryObject[1];
    public static final RegistryObject<Block> REGEN_BLOCK = BLOCKS.register("regen_block", () -> new RegenBlocks(() -> (BlockEntityType)REGEN_BLOCK_ENTITY_HOLDER[0].get(), BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(1.5f, 6.0f).requiresCorrectToolForDrops().noLootTable()));
    public static final RegistryObject<Block> PRESET_DUMMY_STONE = BLOCKS.register("preset_dummy_stone", () -> new PresetAppearanceDummyBlock(RegenVisual.STONE_PRESET, Re_Blocks.dummyProps()));
    public static final RegistryObject<Block> PRESET_DUMMY_DEEPSLATE = BLOCKS.register("preset_dummy_deepslate", () -> new PresetAppearanceDummyBlock(RegenVisual.DEEPSLATE_PRESET, Re_Blocks.dummyProps()));
    public static final RegistryObject<Block> PRESET_DUMMY_NETHER = BLOCKS.register("preset_dummy_nether", () -> new PresetAppearanceDummyBlock(RegenVisual.NETHER_PRESET, Re_Blocks.dummyProps()));
    public static final RegistryObject<Block> PRESET_DUMMY_END = BLOCKS.register("preset_dummy_end", () -> new PresetAppearanceDummyBlock(RegenVisual.END_PRESET, Re_Blocks.dummyProps()));
    public static final RegistryObject<Block> PRESET_DUMMY_DEBRIS = BLOCKS.register("preset_dummy_debris", () -> new PresetAppearanceDummyBlock(RegenVisual.DEBRIS_PRESET, Re_Blocks.dummyProps()));
    public static final RegistryObject<Block> PRESET_DUMMY_STRIPPED_LOG = BLOCKS.register("preset_dummy_stripped_log", () -> new StrippedLogPresetDummyBlock(() -> (BlockEntityType)REGEN_BLOCK_ENTITY_HOLDER[0].get(), Re_Blocks.dummyProps()));
    public static final RegistryObject<Block> PRESET_DUMMY_CUSTOM = BLOCKS.register("preset_dummy_custom", () -> new CustomPresetDummyBlock(() -> (BlockEntityType)REGEN_BLOCK_ENTITY_HOLDER[0].get(), Re_Blocks.dummyProps()));
    public static final RegistryObject<BlockEntityType<RegenBlockEntity>> REGEN_BLOCK_ENTITY = BLOCK_ENTITY_TYPES.register("regen_block", () -> BlockEntityType.Builder.of((pos, state) -> new RegenBlockEntity((BlockEntityType)REGEN_BLOCK_ENTITY_HOLDER[0].get(), pos, state), (Block[])new Block[]{(Block)REGEN_BLOCK.get(), (Block)PRESET_DUMMY_STRIPPED_LOG.get(), (Block)PRESET_DUMMY_CUSTOM.get()}).build(null));

    private static BlockBehaviour.Properties dummyProps() {
        return BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(1.5f, 6.0f).requiresCorrectToolForDrops();
    }

    private Re_Blocks() {
    }

    static {
        Re_Blocks.REGEN_BLOCK_ENTITY_HOLDER[0] = REGEN_BLOCK_ENTITY;
    }
}

