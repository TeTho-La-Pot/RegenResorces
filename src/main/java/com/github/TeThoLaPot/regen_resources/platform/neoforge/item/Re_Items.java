package com.github.TeThoLaPot.regen_resources.platform.neoforge.item;

import com.github.TeThoLaPot.regen_resources.RegenResources;
import com.github.TeThoLaPot.regen_resources.platform.neoforge.block.Re_Blocks;
import com.github.TeThoLaPot.regen_resources.common.item.BreakStuffItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** NeoForge {@link DeferredRegister} でアイテムを登録。 */
public final class Re_Items {

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(Registries.ITEM, RegenResources.MOD_ID);

    public static final DeferredHolder<Item, BlockItem> REGEN_BLOCK_ITEM = ITEMS.register(
            "regen_block",
            () -> new BlockItem(Re_Blocks.REGEN_BLOCK.get(), new Item.Properties())
    );

    public static final DeferredHolder<Item, BreakStuffItem> BREAK_STUFF = ITEMS.register(
            "break_stuff",
            () -> new BreakStuffItem(new Item.Properties().stacksTo(1))
    );

    /** 残骸掘りで得る破片（4 個クラフトで残骸 1 に戻す）。 */
    public static final DeferredHolder<Item, Item> ANCIENT_FRAGMENT =
            ITEMS.register("ancient_fragment", () -> new Item(new Item.Properties()));

    private Re_Items() {}
}
