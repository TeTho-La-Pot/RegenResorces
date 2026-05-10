/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.item.BlockItem
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.Item$Properties
 *  net.minecraft.world.level.block.Block
 *  net.minecraftforge.registries.DeferredRegister
 *  net.minecraftforge.registries.ForgeRegistries
 *  net.minecraftforge.registries.IForgeRegistry
 *  net.minecraftforge.registries.RegistryObject
 */
package com.github.TeThoLaPot.regen_resources.platform.forge.item;

import com.github.TeThoLaPot.regen_resources.common.item.BreakStuffItem;
import com.github.TeThoLaPot.regen_resources.common.item.PresetDummyBlockItem;
import com.github.TeThoLaPot.regen_resources.platform.forge.block.Re_Blocks;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryObject;

public final class Re_Items {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create((IForgeRegistry)ForgeRegistries.ITEMS, (String)"regen_resources");
    public static final RegistryObject<Item> REGEN_BLOCK_ITEM = ITEMS.register("regen_block", () -> new BlockItem((Block)Re_Blocks.REGEN_BLOCK.get(), new Item.Properties()));
    public static final RegistryObject<Item> PRESET_DUMMY_STONE = ITEMS.register("preset_dummy_stone", () -> new PresetDummyBlockItem((Block)Re_Blocks.PRESET_DUMMY_STONE.get(), new Item.Properties(), "tooltip.regen_resources.preset_dummy_stone"));
    public static final RegistryObject<Item> PRESET_DUMMY_DEEPSLATE = ITEMS.register("preset_dummy_deepslate", () -> new PresetDummyBlockItem((Block)Re_Blocks.PRESET_DUMMY_DEEPSLATE.get(), new Item.Properties(), "tooltip.regen_resources.preset_dummy_deepslate"));
    public static final RegistryObject<Item> PRESET_DUMMY_NETHER = ITEMS.register("preset_dummy_nether", () -> new PresetDummyBlockItem((Block)Re_Blocks.PRESET_DUMMY_NETHER.get(), new Item.Properties(), "tooltip.regen_resources.preset_dummy_nether"));
    public static final RegistryObject<Item> PRESET_DUMMY_END = ITEMS.register("preset_dummy_end", () -> new PresetDummyBlockItem((Block)Re_Blocks.PRESET_DUMMY_END.get(), new Item.Properties(), "tooltip.regen_resources.preset_dummy_end"));
    public static final RegistryObject<Item> PRESET_DUMMY_DEBRIS = ITEMS.register("preset_dummy_debris", () -> new PresetDummyBlockItem((Block)Re_Blocks.PRESET_DUMMY_DEBRIS.get(), new Item.Properties(), "tooltip.regen_resources.preset_dummy_debris"));
    public static final RegistryObject<Item> PRESET_DUMMY_STRIPPED_LOG = ITEMS.register("preset_dummy_stripped_log", () -> new PresetDummyBlockItem((Block)Re_Blocks.PRESET_DUMMY_STRIPPED_LOG.get(), new Item.Properties(), "tooltip.regen_resources.preset_dummy_stripped_log"));
    public static final RegistryObject<Item> PRESET_DUMMY_CUSTOM = ITEMS.register("preset_dummy_custom", () -> new PresetDummyBlockItem((Block)Re_Blocks.PRESET_DUMMY_CUSTOM.get(), new Item.Properties(), "tooltip.regen_resources.preset_dummy_custom"));
    public static final RegistryObject<Item> BREAK_STUFF = ITEMS.register("break_stuff", () -> new BreakStuffItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> ANCIENT_FRAGMENT = ITEMS.register("ancient_fragment", () -> new Item(new Item.Properties()));

    private Re_Items() {
    }
}

