package com.github.TeThoLaPot.regen_resources.forge.item;

import com.github.TeThoLaPot.regen_resources.RegenResources;
import com.github.TeThoLaPot.regen_resources.forge.block.Re_Blocks;
import com.github.TeThoLaPot.regen_resources.common.item.BreakStuffItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * Forge 専用（DeferredRegister）。NeoForge 移植時は同等のレジストリ API に置換する。
 */
public final class Re_Items {

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, RegenResources.MOD_ID);

    public static final RegistryObject<Item> REGEN_BLOCK_ITEM = ITEMS.register(
            "regen_block",
            () -> new BlockItem(Re_Blocks.REGEN_BLOCK.get(), new Item.Properties())
    );

    public static final RegistryObject<Item> BREAK_STUFF = ITEMS.register(
            "break_stuff",
            () -> new BreakStuffItem(new Item.Properties().stacksTo(1))
    );

    /** 残骸掘りで得る破片（4 個クラフトで残骸 1 に戻す）。 */
    public static final RegistryObject<Item> ANCIENT_FRAGMENT =
            ITEMS.register("ancient_fragment", () -> new Item(new Item.Properties()));

    private Re_Items() {}
}
