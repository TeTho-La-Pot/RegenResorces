/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.registries.BuiltInRegistries
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.MutableComponent
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.ItemLike
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.Blocks
 *  org.jetbrains.annotations.Nullable
 *  snownee.jade.api.BlockAccessor
 *  snownee.jade.api.IBlockComponentProvider
 *  snownee.jade.api.ITooltip
 *  snownee.jade.api.config.IPluginConfig
 *  snownee.jade.api.ui.IElement
 *  snownee.jade.api.ui.IElementHelper
 */
package com.github.TeThoLaPot.regen_resources.platform.forge.compat.jade;

import com.github.TeThoLaPot.regen_resources.platform.forge.block.Re_Blocks;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.Nullable;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.IElement;
import snownee.jade.api.ui.IElementHelper;

public enum RegenResourcesJadeProvider implements IBlockComponentProvider
{
    INSTANCE;


    public ResourceLocation getUid() {
        return ResourceLocation.fromNamespaceAndPath((String)"regen_resources", (String)"regen_info");
    }

    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        String rl;
        if (!accessor.getBlockState().is((Block)Re_Blocks.REGEN_BLOCK.get())) {
            return;
        }
        CompoundTag srv = accessor.getServerData();
        long executeAt = srv.getLong("regen_j_sync_execute_at");
        if (executeAt > 0L) {
            long gt = accessor.getLevel().getGameTime();
            long remainingTicks = executeAt - gt;
            if (remainingTicks > 0L) {
                int secondsRoundedUp = (int)Math.ceil((double)remainingTicks / 20.0);
                tooltip.add((Component)Component.translatable((String)"jade.regen_resources.time_until_seconds", (Object[])new Object[]{secondsRoundedUp}));
            } else {
                tooltip.add((Component)Component.translatable((String)"jade.regen_resources.time_until_imminent"));
            }
        }
        if (!(rl = srv.getString("regen_j_sync_restore_rl")).isEmpty()) {
            ResourceLocation id = ResourceLocation.tryParse((String)rl);
            MutableComponent targetName = id != null
                    ? BuiltInRegistries.BLOCK.getOptional(id).map(Block::getName).orElse(Component.literal(rl))
                    : Component.literal(rl);
            tooltip.add((Component)Component.translatable((String)"jade.regen_resources.regen_target", (Object[])new Object[]{targetName}));
        }
    }

    @Nullable
    public IElement getIcon(BlockAccessor accessor, IPluginConfig config, IElement currentIcon) {
        ResourceLocation id;
        if (!accessor.getBlockState().is((Block)Re_Blocks.REGEN_BLOCK.get())) {
            return null;
        }
        String rl = accessor.getServerData().getString("regen_j_sync_restore_rl");
        ItemStack stack = ItemStack.EMPTY;
        if (!rl.isEmpty() && (id = ResourceLocation.tryParse((String)rl)) != null) {
            stack = BuiltInRegistries.BLOCK.getOptional(id).map(b -> new ItemStack((ItemLike)b.asItem())).orElse(ItemStack.EMPTY);
        }
        if (stack.isEmpty()) {
            stack = new ItemStack((ItemLike)Blocks.STONE);
        }
        return IElementHelper.get().item(stack);
    }
}

