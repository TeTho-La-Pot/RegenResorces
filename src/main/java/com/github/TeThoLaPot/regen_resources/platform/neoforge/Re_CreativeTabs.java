package com.github.TeThoLaPot.regen_resources.platform.neoforge;

import com.github.TeThoLaPot.regen_resources.RegenResources;
import com.github.TeThoLaPot.regen_resources.common.block.RegenBlocks;
import com.github.TeThoLaPot.regen_resources.common.block.RegenVisual;
import com.github.TeThoLaPot.regen_resources.platform.neoforge.item.Re_Items;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** NeoForge クリエイティブタブ登録。 */
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
                                for (RegenVisual visual : RegenVisual.creativeItemVariants()) {
                                    ItemStack stack = new ItemStack(Re_Items.REGEN_BLOCK_ITEM.get(), 1);
                                    CompoundTag root = new CompoundTag();
                                    CompoundTag bst = new CompoundTag();
                                    bst.putString(
                                            RegenBlocks.VISUAL.getName(),
                                            visual.getSerializedName());
                                    root.put("BlockStateTag", bst);
                                    stack.set(DataComponents.CUSTOM_DATA, CustomData.of(root));
                                    output.accept(stack);
                                }
                                output.accept(Re_Items.BREAK_STUFF.get());
                                output.accept(Re_Items.ANCIENT_FRAGMENT.get());
                            })
                            .build()
            );

    private Re_CreativeTabs() {}
}
