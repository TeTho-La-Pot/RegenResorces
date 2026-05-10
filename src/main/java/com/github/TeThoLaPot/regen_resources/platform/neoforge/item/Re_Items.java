package com.github.TeThoLaPot.regen_resources.platform.neoforge.item;

import com.github.TeThoLaPot.regen_resources.RegenResources;
import com.github.TeThoLaPot.regen_resources.common.item.BreakStuffItem;
import com.github.TeThoLaPot.regen_resources.common.item.PresetDummyBlockItem;
import com.github.TeThoLaPot.regen_resources.common.item.RegenBlockItem;
import com.github.TeThoLaPot.regen_resources.platform.neoforge.block.Re_Blocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** NeoForge {@link DeferredRegister} でアイテムを登録。 */
public final class Re_Items {

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(Registries.ITEM, RegenResources.MOD_ID);

    /** 世界内の再生シェル用。クリエイティブには載せない（{@link RegenBlockItem}）。 */
    public static final DeferredHolder<Item, RegenBlockItem> REGEN_BLOCK_ITEM = ITEMS.register(
            "regen_block",
            () -> new RegenBlockItem(Re_Blocks.REGEN_BLOCK.get(), new Item.Properties())
    );

    public static final DeferredHolder<Item, PresetDummyBlockItem> PRESET_DUMMY_STONE = ITEMS.register(
            "preset_dummy_stone",
            () -> new PresetDummyBlockItem(
                    Re_Blocks.PRESET_DUMMY_STONE.get(), new Item.Properties(), "tooltip.regen_resources.preset_dummy_stone"));
    public static final DeferredHolder<Item, PresetDummyBlockItem> PRESET_DUMMY_DEEPSLATE = ITEMS.register(
            "preset_dummy_deepslate",
            () -> new PresetDummyBlockItem(
                    Re_Blocks.PRESET_DUMMY_DEEPSLATE.get(), new Item.Properties(), "tooltip.regen_resources.preset_dummy_deepslate"));
    public static final DeferredHolder<Item, PresetDummyBlockItem> PRESET_DUMMY_NETHER = ITEMS.register(
            "preset_dummy_nether",
            () -> new PresetDummyBlockItem(
                    Re_Blocks.PRESET_DUMMY_NETHER.get(), new Item.Properties(), "tooltip.regen_resources.preset_dummy_nether"));
    public static final DeferredHolder<Item, PresetDummyBlockItem> PRESET_DUMMY_END = ITEMS.register(
            "preset_dummy_end",
            () -> new PresetDummyBlockItem(
                    Re_Blocks.PRESET_DUMMY_END.get(), new Item.Properties(), "tooltip.regen_resources.preset_dummy_end"));
    public static final DeferredHolder<Item, PresetDummyBlockItem> PRESET_DUMMY_DEBRIS = ITEMS.register(
            "preset_dummy_debris",
            () -> new PresetDummyBlockItem(
                    Re_Blocks.PRESET_DUMMY_DEBRIS.get(), new Item.Properties(), "tooltip.regen_resources.preset_dummy_debris"));
    public static final DeferredHolder<Item, PresetDummyBlockItem> PRESET_DUMMY_STRIPPED_LOG = ITEMS.register(
            "preset_dummy_stripped_log",
            () -> new PresetDummyBlockItem(
                    Re_Blocks.PRESET_DUMMY_STRIPPED_LOG.get(), new Item.Properties(), "tooltip.regen_resources.preset_dummy_stripped_log"));
    public static final DeferredHolder<Item, PresetDummyBlockItem> PRESET_DUMMY_CUSTOM = ITEMS.register(
            "preset_dummy_custom",
            () -> new PresetDummyBlockItem(
                    Re_Blocks.PRESET_DUMMY_CUSTOM.get(), new Item.Properties(), "tooltip.regen_resources.preset_dummy_custom"));

    public static final DeferredHolder<Item, BreakStuffItem> BREAK_STUFF = ITEMS.register(
            "break_stuff",
            () -> new BreakStuffItem(new Item.Properties().stacksTo(1))
    );

    /** 残骸掘りで得る破片（4 個クラフトで残骸 1 に戻す）。 */
    public static final DeferredHolder<Item, Item> ANCIENT_FRAGMENT =
            ITEMS.register("ancient_fragment", () -> new Item(new Item.Properties()));

    private Re_Items() {}
}
