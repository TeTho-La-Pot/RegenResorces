package com.github.TeThoLaPot.regen_resources.init.item;

import com.github.TeThoLaPot.regen_resources.RegenConstants;
import com.github.TeThoLaPot.regen_resources.init.block.Re_Blocks;
import com.github.TeThoLaPot.regen_resources.init.item.BreakStuff;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class Re_Items {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, RegenConstants.MOD_ID);

    // 変数名を整理（他のクラスから Re_Items.BREAK_STUFF.get() で呼べるように）
    public static final RegistryObject<Item> BREAK_STUFF = ITEMS.register("break_stuff",
            () -> new BreakStuff(new Item.Properties().stacksTo(1)));

    /** 創造タブ／デバッグ用。サーバー側ではイベントでのみ設置すべきです。 */
    public static final RegistryObject<Item> REGEN_BLOCK_ITEM = ITEMS.register("regen_block",
            () -> new BlockItem(Re_Blocks.REGEN_BLOCK.get(),
                    new Item.Properties().stacksTo(64)));

    public static final RegistryObject<Item> ANCIENT_FRAGMENT = ITEMS.register("ancient_fragment",
            () -> new Item(new Item.Properties()));

    public static void register(IEventBus eventBus){
        ITEMS.register(eventBus);
    }
}
