/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.level.block.Block
 *  snownee.jade.api.IBlockComponentProvider
 *  snownee.jade.api.IServerDataProvider
 *  snownee.jade.api.IWailaClientRegistration
 *  snownee.jade.api.IWailaCommonRegistration
 *  snownee.jade.api.IWailaPlugin
 *  snownee.jade.api.WailaPlugin
 */
package com.github.TeThoLaPot.regen_resources.platform.forge.compat.jade;

import com.github.TeThoLaPot.regen_resources.common.block.RegenBlockEntity;
import com.github.TeThoLaPot.regen_resources.common.block.RegenBlocks;
import com.github.TeThoLaPot.regen_resources.platform.forge.compat.jade.RegenResourcesJadeProvider;
import com.github.TeThoLaPot.regen_resources.platform.forge.compat.jade.RegenResourcesJadeRegenEligibleProvider;
import com.github.TeThoLaPot.regen_resources.platform.forge.compat.jade.RegenResourcesJadeServerData;
import net.minecraft.world.level.block.Block;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

@WailaPlugin
public final class RegenResourcesJadePlugin
implements IWailaPlugin {
    public void register(IWailaCommonRegistration registration) {
        registration.registerBlockDataProvider((IServerDataProvider)RegenResourcesJadeServerData.INSTANCE, RegenBlockEntity.class);
    }

    public void registerClient(IWailaClientRegistration registration) {
        registration.registerBlockComponent((IBlockComponentProvider)RegenResourcesJadeProvider.INSTANCE, RegenBlocks.class);
        registration.registerBlockIcon((IBlockComponentProvider)RegenResourcesJadeProvider.INSTANCE, RegenBlocks.class);
        registration.registerBlockComponent((IBlockComponentProvider)RegenResourcesJadeRegenEligibleProvider.INSTANCE, Block.class);
    }
}

