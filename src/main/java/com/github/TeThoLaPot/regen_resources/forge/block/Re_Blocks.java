package com.github.TeThoLaPot.regen_resources.forge.block;

import com.github.TeThoLaPot.regen_resources.RegenResources;
import com.github.TeThoLaPot.regen_resources.common.block.RegenBlocks;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * Forge 専用（DeferredRegister）。NeoForge 移植時は同等のレジストリ API に置換する。
 */
public final class Re_Blocks {

    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, RegenResources.MOD_ID);

    public static final RegistryObject<Block> REGEN_BLOCK = BLOCKS.register(
            "regen_block",
            () -> new RegenBlocks(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE)
                    .strength(1.5F, 6.0F)
                    .requiresCorrectToolForDrops()
                    .noLootTable()
            )
    );

    private Re_Blocks() {}
}
