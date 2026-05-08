package com.github.TeThoLaPot.regen_resources;

import com.github.TeThoLaPot.regen_resources.platform.forge.RegenResourcesForgeBootstrap;
import com.github.TeThoLaPot.regen_resources.platform.forge.config.RegenResourcesForgeConfig;
import com.mojang.logging.LogUtils;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import org.slf4j.Logger;

@Mod(RegenResources.MOD_ID)
public final class RegenResources {

    public static final String MOD_ID = "regen_resources";

    private static final Logger LOGGER = LogUtils.getLogger();

    public RegenResources() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, RegenResourcesForgeConfig.SPEC);
        LOGGER.info("{} loaded (skeleton).", MOD_ID);
        RegenResourcesForgeBootstrap.bootstrap();
    }
}
