package com.github.TeThoLaPot.regen_resources.init.block;

import com.github.TeThoLaPot.regen_resources.RegenConstants;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class Re_Blocks {
    // ブロック登録用のレジストリを作成
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, RegenConstants.MOD_ID);

    /**
     * 再生中の鉱石。採掘速度・爆発耐性・適正ツールは復元対象（ミミック）に依存し {@link RegenBlocks} で解決。
     * プロパティの hardness は BE 未準備時のフォールバック（採掘はイベントで制御。
     */
    public static final RegistryObject<Block> REGEN_BLOCK = BLOCKS.register("regen_block",
            () -> new RegenBlocks(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE)
                    /* BreakSpeed でミミックに合わせ補正。ここは石に近い基準のみ。 */
                    .strength(1.5F, 6.0F)
                    .requiresCorrectToolForDrops()
                    .noLootTable()
            )
    );

    /**
     * 他に独自の鉱石などを追加する場合もここへ記述します。
     * 例: public static final RegistryObject<Block> TEST_ORE = BLOCKS.register(...);
     */
}


