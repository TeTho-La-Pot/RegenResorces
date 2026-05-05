package com.github.TeThoLaPot.regen_resources.jade;

import com.github.TeThoLaPot.regen_resources.init.block.RegenBlocks;
import com.github.TeThoLaPot.regen_resources.init.entity.RegenBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import snownee.jade.addon.harvest.HarvestToolProvider;
import snownee.jade.api.Accessor;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.ITooltip;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.Identifiers;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;
import snownee.jade.impl.WailaClientRegistration;
import snownee.jade.impl.config.PluginConfig;

@WailaPlugin
public class RegenResourcesJadePlugin implements IWailaPlugin {

    @Override
    public void register(IWailaCommonRegistration registration) {
        registration.registerBlockDataProvider(RegenResourcesJadeServerData.INSTANCE, BlockEntity.class);
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.addTooltipCollectedCallback(2500, RegenResourcesJadePlugin::replaceHarvestToolWithMimic);
        registration.registerBlockComponent(RegenResourcesJadeProvider.INSTANCE, RegenBlocks.class);
        registration.registerBlockIcon(RegenResourcesJadeProvider.INSTANCE, RegenBlocks.class);
        registration.registerBlockComponent(RegenResourcesJadeRegenEligibleProvider.INSTANCE, net.minecraft.world.level.block.Block.class);
    }

    private static void replaceHarvestToolWithMimic(ITooltip tooltip, Accessor<?> accessor) {
        if (!(accessor instanceof BlockAccessor ba)) {
            return;
        }
        if (!(ba.getBlockEntity() instanceof RegenBlockEntity)) {
            return;
        }
        tooltip.remove(Identifiers.MC_HARVEST_TOOL);
        tooltip.remove(Identifiers.MC_EFFECTIVE_TOOL);
        tooltip.remove(Identifiers.MC_HARVEST_TOOL_NEW_LINE);
        tooltip.remove(Identifiers.MC_HARVEST_TOOL_CREATIVE);

        Level level = ba.getLevel();
        BlockPos pos = ba.getPosition();
        BlockState mimic = RegenBlocks.mimicStateAt(level, pos);

        BlockAccessor mimicAccessor = WailaClientRegistration.INSTANCE.blockAccessor()
                .from(ba)
                .blockState(mimic)
                .blockEntity(() -> null)
                .build();

        HarvestToolProvider.INSTANCE.appendTooltip(tooltip, mimicAccessor, PluginConfig.INSTANCE);
    }
}
