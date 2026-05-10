/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraftforge.api.distmarker.Dist
 *  net.minecraftforge.eventbus.api.IEventBus
 *  net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent
 *  net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext
 *  net.minecraftforge.fml.loading.FMLEnvironment
 */
package com.github.TeThoLaPot.regen_resources.platform.forge;

import com.github.TeThoLaPot.regen_resources.common.regen.RegenRuleRegistry;
import com.github.TeThoLaPot.regen_resources.platform.RegenPlatformServices;
import com.github.TeThoLaPot.regen_resources.platform.forge.Re_CreativeTabs;
import com.github.TeThoLaPot.regen_resources.platform.forge.RegenForgePlatformConfig;
import com.github.TeThoLaPot.regen_resources.platform.forge.RegenForgePlatformNetwork;
import com.github.TeThoLaPot.regen_resources.platform.forge.block.Re_Blocks;
import com.github.TeThoLaPot.regen_resources.platform.forge.client.model.RegenCompositeSpriteSourceRegistry;
import com.github.TeThoLaPot.regen_resources.platform.forge.config.RegenPresetIo;
import com.github.TeThoLaPot.regen_resources.platform.forge.item.Re_Items;
import com.github.TeThoLaPot.regen_resources.platform.forge.loot.ReLootModifiers;
import com.github.TeThoLaPot.regen_resources.platform.forge.network.RegenResourcesNetwork;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;

public final class RegenResourcesForgeBootstrap {
    private RegenResourcesForgeBootstrap() {
    }

    public static void bootstrap() {
        RegenPlatformServices.install(new RegenForgePlatformConfig(), new RegenForgePlatformNetwork());
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        Re_Blocks.BLOCKS.register(modBus);
        Re_Blocks.BLOCK_ENTITY_TYPES.register(modBus);
        Re_Items.ITEMS.register(modBus);
        Re_CreativeTabs.CREATIVE_MODE_TABS.register(modBus);
        ReLootModifiers.LOOT_MODIFIER_SERIALIZERS.register(modBus);
        modBus.addListener(RegenResourcesForgeBootstrap::onCommonSetup);
        if (FMLEnvironment.dist == Dist.CLIENT) {
            ClientBootstrap.registerEarly();
        }
    }

    private static void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            RegenRuleRegistry.setRules(RegenPresetIo.loadOrCreateDefaults());
            RegenResourcesNetwork.register();
        });
    }

    private static final class ClientBootstrap {
        private ClientBootstrap() {
        }

        static void registerEarly() {
            RegenCompositeSpriteSourceRegistry.ensureRegistered();
        }
    }
}

