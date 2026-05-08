package com.github.TeThoLaPot.regen_resources.jade;

import com.github.TeThoLaPot.regen_resources.common.block.RegenBlockEntity;
import com.github.TeThoLaPot.regen_resources.common.block.RegenBlocks;
import net.minecraft.world.level.block.Block;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

/**
 * alpha と同様に Jade はプロバイダ登録のみ。
 * 無タイルブロックの可否は Forge チャネル（{@link com.github.TeThoLaPot.regen_resources.forge.network.RegenResourcesNetwork}）でサーバーから取得する。
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

