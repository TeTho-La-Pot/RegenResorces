/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.registries.Registries
 *  net.minecraft.network.chat.Component
 *  net.minecraft.resources.ResourceKey
 *  net.minecraft.world.item.CreativeModeTab
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.ItemLike
 *  net.minecraftforge.registries.DeferredRegister
 *  net.minecraftforge.registries.RegistryObject
 */
package com.github.TeThoLaPot.regen_resources.platform.forge;

import com.github.TeThoLaPot.regen_resources.platform.forge.item.Re_Items;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public final class Re_CreativeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create((ResourceKey)Registries.CREATIVE_MODE_TAB, (String)"regen_resources");
    public static final RegistryObject<CreativeModeTab> REGEN_TAB = CREATIVE_MODE_TABS.register("regen_tab", () -> CreativeModeTab.builder().title((Component)Component.translatable((String)"creativetab.regen_resources_tab")).icon(() -> new ItemStack((ItemLike)Re_Items.BREAK_STUFF.get())).displayItems((parameters, output) -> {
        output.accept((ItemLike)Re_Items.PRESET_DUMMY_STONE.get());
        output.accept((ItemLike)Re_Items.PRESET_DUMMY_DEEPSLATE.get());
        output.accept((ItemLike)Re_Items.PRESET_DUMMY_NETHER.get());
        output.accept((ItemLike)Re_Items.PRESET_DUMMY_END.get());
        output.accept((ItemLike)Re_Items.PRESET_DUMMY_DEBRIS.get());
        output.accept((ItemLike)Re_Items.PRESET_DUMMY_STRIPPED_LOG.get());
        output.accept((ItemLike)Re_Items.PRESET_DUMMY_CUSTOM.get());
        output.accept((ItemLike)Re_Items.BREAK_STUFF.get());
        output.accept((ItemLike)Re_Items.ANCIENT_FRAGMENT.get());
    }).build());

    private Re_CreativeTabs() {
    }
}

