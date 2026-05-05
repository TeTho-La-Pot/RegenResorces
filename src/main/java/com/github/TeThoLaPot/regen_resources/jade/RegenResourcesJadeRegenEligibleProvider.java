package com.github.TeThoLaPot.regen_resources.jade;

import com.github.TeThoLaPot.regen_resources.Config;
import com.github.TeThoLaPot.regen_resources.RegenConstants;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

/**
 * RegenPresets 対象ブロックにカーソルを合わせたときに一行追加する。
 */
public enum RegenResourcesJadeRegenEligibleProvider implements IBlockComponentProvider {
    INSTANCE;

    @Override
    public ResourceLocation getUid() {
        return ResourceLocation.fromNamespaceAndPath(RegenConstants.MOD_ID, "regen_eligible");
    }

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        Level level = accessor.getLevel();
        BlockState state = accessor.getBlockState();
        if (!Config.isRegenTarget(level, state)) {
            return;
        }
        tooltip.add(Component.translatable("jade.regen_resources.regen_eligible").withStyle(ChatFormatting.GREEN));
    }
}
