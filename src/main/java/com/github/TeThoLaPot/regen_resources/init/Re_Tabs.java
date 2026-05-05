package com.github.TeThoLaPot.regen_resources.init;

import com.github.TeThoLaPot.regen_resources.RegenConstants;
import com.github.TeThoLaPot.regen_resources.init.block.Re_Blocks;
import com.github.TeThoLaPot.regen_resources.init.item.Re_Items;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class Re_Tabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, RegenConstants.MOD_ID);

    public static final RegistryObject<CreativeModeTab> REGEN_TAB = CREATIVE_MODE_TABS.register("regen_tab",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("creativetab.regen_resources_tab"))
                    // アイコンは看板アイテムの「破壊装置」などにすると分かりやすいです
                    .icon(() -> new ItemStack(Re_Items.BREAK_STUFF.get()))
                    .displayItems((parameters, output) -> {
                        // 1. 共通の再生待ちブロック
                        output.accept(Re_Items.REGEN_BLOCK_ITEM.get());

                        // 2. 専用アイテム類
                        output.accept(Re_Items.BREAK_STUFF.get());
                        output.accept(Re_Items.ANCIENT_FRAGMENT.get());
                    })
                    .build()
    );
}

