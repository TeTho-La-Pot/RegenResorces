package com.github.TeThoLaPot.regen_resources.platform.neoforge;

import com.github.TeThoLaPot.regen_resources.RegenResources;
import com.github.TeThoLaPot.regen_resources.platform.neoforge.item.Re_Items;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * クリエイティブタブ。「再生中の資源」({@code regen_block}) は建築・テスト用ダミーとツール類のみ（シェル本体は含めない）。
 */
public final class Re_CreativeTabs {

    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, RegenResources.MOD_ID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> REGEN_TAB =
            CREATIVE_MODE_TABS.register(
                    "regen_tab",
                    () -> CreativeModeTab.builder()
                            .title(Component.translatable("creativetab.regen_resources_tab"))
                            .icon(() -> new ItemStack(Re_Items.BREAK_STUFF.get()))
                            .displayItems((parameters, output) -> {
                                output.accept(Re_Items.PRESET_DUMMY_STONE.get());
                                output.accept(Re_Items.PRESET_DUMMY_DEEPSLATE.get());
                                output.accept(Re_Items.PRESET_DUMMY_NETHER.get());
                                output.accept(Re_Items.PRESET_DUMMY_END.get());
                                output.accept(Re_Items.PRESET_DUMMY_DEBRIS.get());
                                output.accept(Re_Items.PRESET_DUMMY_STRIPPED_LOG.get());
                                output.accept(Re_Items.PRESET_DUMMY_CUSTOM.get());
                                output.accept(Re_Items.BREAK_STUFF.get());
                                output.accept(Re_Items.ANCIENT_FRAGMENT.get());
                            })
                            .build()
            );

    private Re_CreativeTabs() {}
}
