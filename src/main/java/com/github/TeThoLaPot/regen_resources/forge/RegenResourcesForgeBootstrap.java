package com.github.TeThoLaPot.regen_resources.forge;

import com.github.TeThoLaPot.regen_resources.forge.block.Re_Blocks;
import com.github.TeThoLaPot.regen_resources.forge.item.Re_Items;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

/**
 * Forge 固有の wiring（DeferredRegister の登録など）。NeoForge 移植時はここを差し替える想定。
 */
public final class RegenResourcesForgeBootstrap {

    private RegenResourcesForgeBootstrap() {}

    public static void bootstrap() {
        var modBus = FMLJavaModLoadingContext.get().getModEventBus();
        Re_Blocks.BLOCKS.register(modBus);
        Re_Blocks.BLOCK_ENTITY_TYPES.register(modBus);
        Re_Items.ITEMS.register(modBus);
        Re_CreativeTabs.CREATIVE_MODE_TABS.register(modBus);
    }
}

