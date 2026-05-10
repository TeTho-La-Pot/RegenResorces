package com.github.TeThoLaPot.regen_resources;

import com.github.TeThoLaPot.regen_resources.platform.neoforge.RegenResourcesForgeBootstrap;
import com.github.TeThoLaPot.regen_resources.platform.neoforge.config.RegenResourcesForgeConfig;
import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import org.slf4j.Logger;

@Mod(RegenResources.MOD_ID)
public final class RegenResources {

    public static final String MOD_ID = "regen_resources";

    private static final Logger LOGGER = LogUtils.getLogger();

    public RegenResources(IEventBus modEventBus, ModContainer container) {
        container.registerConfig(ModConfig.Type.COMMON, RegenResourcesForgeConfig.SPEC);
        LOGGER.info("{} loaded (NeoForge).", MOD_ID);
        RegenResourcesForgeBootstrap.bootstrap(modEventBus);
    }
}
