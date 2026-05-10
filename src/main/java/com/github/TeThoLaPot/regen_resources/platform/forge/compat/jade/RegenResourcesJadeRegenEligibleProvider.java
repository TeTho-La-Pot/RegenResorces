/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.ChatFormatting
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.network.chat.Component
 *  net.minecraft.resources.ResourceLocation
 *  snownee.jade.api.BlockAccessor
 *  snownee.jade.api.IBlockComponentProvider
 *  snownee.jade.api.ITooltip
 *  snownee.jade.api.config.IPluginConfig
 */
package com.github.TeThoLaPot.regen_resources.platform.forge.compat.jade;

import com.github.TeThoLaPot.regen_resources.platform.forge.network.RegenJadeProbeClientCache;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public enum RegenResourcesJadeRegenEligibleProvider implements IBlockComponentProvider
{
    INSTANCE;


    public ResourceLocation getUid() {
        return ResourceLocation.fromNamespaceAndPath((String)"regen_resources", (String)"regen_eligible");
    }

    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        CompoundTag effective;
        CompoundTag srv = accessor.getServerData();
        if (srv.getBoolean("regen_j_rule_match")) {
            effective = srv;
        } else {
            CompoundTag cached = RegenJadeProbeClientCache.get(accessor);
            if (cached != null) {
                effective = cached;
            } else {
                RegenJadeProbeClientCache.requestIfNeeded(accessor);
                return;
            }
        }
        if (!effective.getBoolean("regen_j_rule_match")) {
            return;
        }
        if (effective.getBoolean("regen_j_allows_regen")) {
            tooltip.add((Component)Component.translatable((String)"jade.regen_resources.regen_eligible").withStyle(ChatFormatting.GREEN));
        } else {
            tooltip.add((Component)Component.translatable((String)"jade.regen_resources.regen_not_eligible").withStyle(ChatFormatting.RED));
        }
    }
}

