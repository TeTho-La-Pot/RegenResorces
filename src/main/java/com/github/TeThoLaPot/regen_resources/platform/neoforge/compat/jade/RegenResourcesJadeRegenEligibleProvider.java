package com.github.TeThoLaPot.regen_resources.platform.neoforge.compat.jade;

import com.github.TeThoLaPot.regen_resources.RegenResources;
import com.github.TeThoLaPot.regen_resources.platform.neoforge.network.RegenJadeProbeClientCache;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

/**
 * RegenPresets 対象ブロックにカーソルを合わせたときに一行追加する。
 * ブロックエンティティありは {@link RegenResourcesJadeServerData}、無しは alpha と同様にサーバー権威データを独自パケットで受ける。
 */
public enum RegenResourcesJadeRegenEligibleProvider implements IBlockComponentProvider {
    INSTANCE;

    @Override
    public ResourceLocation getUid() {
        return ResourceLocation.fromNamespaceAndPath(RegenResources.MOD_ID, "regen_eligible");
    }

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        CompoundTag srv = accessor.getServerData();
        CompoundTag effective;
        if (srv.getBoolean(RegenResourcesJadeServerData.SYNC_RULE_MATCH)) {
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
        if (!effective.getBoolean(RegenResourcesJadeServerData.SYNC_RULE_MATCH)) {
            return;
        }
        if (effective.getBoolean(RegenResourcesJadeServerData.SYNC_ALLOWS_REGEN)) {
            tooltip.add(Component.translatable("jade.regen_resources.regen_eligible").withStyle(ChatFormatting.GREEN));
        } else {
            tooltip.add(Component.translatable("jade.regen_resources.regen_not_eligible").withStyle(ChatFormatting.RED));
        }
    }
}

