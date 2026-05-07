package com.github.TeThoLaPot.regen_resources.jade;

import com.github.TeThoLaPot.regen_resources.common.block.RegenBlockEntity;
import com.github.TeThoLaPot.regen_resources.common.block.RegenBlocks;
import net.minecraft.world.level.block.Block;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

/**
 * alpha の Jade 連携をベースにした実装。
 * <p>Jade のサーバー同期は {@link BlockEntity} 向け API のため {@link RegenBlockEntity} に紐付ける。
 */
@WailaPlugin
public final class RegenResourcesJadePlugin implements IWailaPlugin {

    @Override
    public void register(IWailaCommonRegistration registration) {
        registration.registerBlockDataProvider(RegenResourcesJadeServerData.INSTANCE, RegenBlockEntity.class);
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerBlockComponent(RegenResourcesJadeProvider.INSTANCE, RegenBlocks.class);
        registration.registerBlockIcon(RegenResourcesJadeProvider.INSTANCE, RegenBlocks.class);
        registration.registerBlockComponent(RegenResourcesJadeRegenEligibleProvider.INSTANCE, Block.class);
    }
}

