package com.github.TeThoLaPot.regen_resources;

import com.mojang.logging.LogUtils;
import com.github.TeThoLaPot.regen_resources.forge.RegenResourcesForgeBootstrap;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;

@Mod(RegenResources.MOD_ID)
public final class RegenResources {

    public static final String MOD_ID = "regen_resources";

    private static final Logger LOGGER = LogUtils.getLogger();

    public RegenResources() {
        LOGGER.info("{} loaded (skeleton).", MOD_ID);
        RegenResourcesForgeBootstrap.bootstrap();
    }
}
