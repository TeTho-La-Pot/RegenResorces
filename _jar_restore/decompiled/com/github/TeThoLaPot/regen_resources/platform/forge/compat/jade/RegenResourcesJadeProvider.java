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
        if (!accessor.getBlockState().m_60713_((Block)Re_Blocks.REGEN_BLOCK.get())) {
            return;
        }
        CompoundTag srv = accessor.getServerData();
        long executeAt = srv.m_128454_("regen_j_sync_execute_at");
        if (executeAt > 0L) {
            long gt = accessor.getLevel().m_46467_();
            long remainingTicks = executeAt - gt;
            if (remainingTicks > 0L) {
                int secondsRoundedUp = (int)Math.ceil((double)remainingTicks / 20.0);
                tooltip.add((Component)Component.m_237110_((String)"jade.regen_resources.time_until_seconds", (Object[])new Object[]{secondsRoundedUp}));
            } else {
                tooltip.add((Component)Component.m_237115_((String)"jade.regen_resources.time_until_imminent"));
            }
        }
        if (!(rl = srv.m_128461_("regen_j_sync_restore_rl")).isEmpty()) {
            ResourceLocation id = ResourceLocation.m_135820_((String)rl);
            MutableComponent targetName = id != null ? (Component)BuiltInRegistries.f_256975_.m_6612_(id).map(Block::m_49954_).orElse(Component.m_237113_((String)rl)) : Component.m_237113_((String)rl);
            tooltip.add((Component)Component.m_237110_((String)"jade.regen_resources.regen_target", (Object[])new Object[]{targetName}));
        }
    }

    @Nullable
    public IElement getIcon(BlockAccessor accessor, IPluginConfig config, IElement currentIcon) {
        ResourceLocation id;
        if (!accessor.getBlockState().m_60713_((Block)Re_Blocks.REGEN_BLOCK.get())) {
            return null;
        }
        String rl = accessor.getServerData().m_128461_("regen_j_sync_restore_rl");
        ItemStack stack = ItemStack.f_41583_;
        if (!rl.isEmpty() && (id = ResourceLocation.m_135820_((String)rl)) != null) {
            stack = BuiltInRegistries.f_256975_.m_6612_(id).map(b -> new ItemStack((ItemLike)b.m_5456_())).orElse(ItemStack.f_41583_);
        }
        if (stack.m_41619_()) {
            stack = new ItemStack((ItemLike)Blocks.f_50069_);
        }
        return IElementHelper.get().item(stack);
    }
}

