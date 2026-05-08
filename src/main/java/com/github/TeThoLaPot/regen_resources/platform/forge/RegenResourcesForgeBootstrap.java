package com.github.TeThoLaPot.regen_resources.platform.forge;

import com.github.TeThoLaPot.regen_resources.common.regen.RegenRuleRegistry;
import com.github.TeThoLaPot.regen_resources.platform.RegenPlatformServices;
import com.github.TeThoLaPot.regen_resources.platform.forge.block.Re_Blocks;
import com.github.TeThoLaPot.regen_resources.platform.forge.config.RegenPresetIo;
import com.github.TeThoLaPot.regen_resources.platform.forge.item.Re_Items;
import com.github.TeThoLaPot.regen_resources.platform.forge.loot.ReLootModifiers;
import com.github.TeThoLaPot.regen_resources.platform.forge.network.RegenResourcesNetwork;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

/**
 * Forge 固有の wiring（DeferredRegister の登録など）。NeoForge 移植時はここを差し替える想定。
 */
public final class RegenResourcesForgeBootstrap {

    private RegenResourcesForgeBootstrap() {}

    public static void bootstrap() {
        RegenPlatformServices.install(new RegenForgePlatformConfig(), new RegenForgePlatformNetwork());
        var modBus = FMLJavaModLoadingContext.get().getModEventBus();
        Re_Blocks.BLOCKS.register(modBus);
        Re_Blocks.BLOCK_ENTITY_TYPES.register(modBus);
        Re_Items.ITEMS.register(modBus);
        Re_CreativeTabs.CREATIVE_MODE_TABS.register(modBus);
        ReLootModifiers.LOOT_MODIFIER_SERIALIZERS.register(modBus);
        modBus.addListener(RegenResourcesForgeBootstrap::onCommonSetup);
    }

    private static void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(
                () -> {
                    RegenRuleRegistry.setRules(RegenPresetIo.loadOrCreateDefaults());
                    RegenResourcesNetwork.register();
                });
    }
}

