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
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create((ResourceKey)Registries.f_279569_, (String)"regen_resources");
    public static final RegistryObject<CreativeModeTab> REGEN_TAB = CREATIVE_MODE_TABS.register("regen_tab", () -> CreativeModeTab.builder().m_257941_((Component)Component.m_237115_((String)"creativetab.regen_resources_tab")).m_257737_(() -> new ItemStack((ItemLike)Re_Items.BREAK_STUFF.get())).m_257501_((parameters, output) -> {
        output.m_246326_((ItemLike)Re_Items.PRESET_DUMMY_STONE.get());
        output.m_246326_((ItemLike)Re_Items.PRESET_DUMMY_DEEPSLATE.get());
        output.m_246326_((ItemLike)Re_Items.PRESET_DUMMY_NETHER.get());
        output.m_246326_((ItemLike)Re_Items.PRESET_DUMMY_END.get());
        output.m_246326_((ItemLike)Re_Items.PRESET_DUMMY_DEBRIS.get());
        output.m_246326_((ItemLike)Re_Items.PRESET_DUMMY_STRIPPED_LOG.get());
        output.m_246326_((ItemLike)Re_Items.PRESET_DUMMY_CUSTOM.get());
        output.m_246326_((ItemLike)Re_Items.BREAK_STUFF.get());
        output.m_246326_((ItemLike)Re_Items.ANCIENT_FRAGMENT.get());
    }).m_257652_());

    private Re_CreativeTabs() {
    }
}

