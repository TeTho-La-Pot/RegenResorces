package com.github.TeThoLaPot.regen_resources.init.entity;

import com.github.TeThoLaPot.regen_resources.RegenConstants;
import com.github.TeThoLaPot.regen_resources.init.block.Re_Blocks; // 前のステップで修正したクラス
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class BlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, RegenConstants.MOD_ID);

    // 共通のRegenBlockEntity一つに統合
    public static final RegistryObject<BlockEntityType<RegenBlockEntity>> REGEN_ORE_ENTITY =
            BLOCK_ENTITIES.register("regen_ore_entity", () ->
                    BlockEntityType.Builder.of(RegenBlockEntity::new, Re_Blocks.REGEN_BLOCK.get()).build(null)
            );
}
