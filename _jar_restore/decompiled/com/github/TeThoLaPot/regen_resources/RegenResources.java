/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  net.minecraftforge.fml.ModLoadingContext
 *  net.minecraftforge.fml.common.Mod
 *  net.minecraftforge.fml.config.IConfigSpec
 *  net.minecraftforge.fml.config.ModConfig$Type
 *  net.minecraftforge.fml.loading.FMLPaths
 *  org.slf4j.Logger
 */
package com.github.TeThoLaPot.regen_resources;

import com.github.TeThoLaPot.regen_resources.platform.forge.RegenResourcesForgeBootstrap;
import com.github.TeThoLaPot.regen_resources.platform.forge.config.RegenResourcesForgeConfig;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.FileAttribute;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.IConfigSpec;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.loading.FMLPaths;
import org.slf4j.Logger;

@Mod(value="regen_resources")
public final class RegenResources {
    public static final String MOD_ID = "regen_resources";
    public static final String CONFIG_FOLDER = "RegenResources";
    private static final String COMMON_CONFIG_RELATIVE_PATH = "RegenResources/regen_resources-common.toml";
    private static final Logger LOGGER = LogUtils.getLogger();

    public RegenResources() {
        RegenResources.migrateLegacyCommonConfigIfPresent();
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, (IConfigSpec)RegenResourcesForgeConfig.SPEC, COMMON_CONFIG_RELATIVE_PATH);
        LOGGER.info("{} loaded (skeleton).", (Object)MOD_ID);
        RegenResourcesForgeBootstrap.bootstrap();
    }

    private static void migrateLegacyCommonConfigIfPresent() {
        try {
            Path configDir = FMLPaths.CONFIGDIR.get();
            Path legacy = configDir.resolve("regen_resources-common.toml");
            Path target = configDir.resolve(CONFIG_FOLDER).resolve("regen_resources-common.toml");
            if (!Files.exists(legacy, new LinkOption[0])) {
                return;
            }
            if (Files.exists(target, new LinkOption[0])) {
                LOGGER.info("{}: legacy config '{}' found but target '{}' already exists; leaving legacy untouched.", new Object[]{MOD_ID, legacy, target});
                return;
            }
            Files.createDirectories(target.getParent(), new FileAttribute[0]);
            Files.move(legacy, target, StandardCopyOption.ATOMIC_MOVE);
            LOGGER.info("{}: migrated legacy common config '{}' -> '{}'.", new Object[]{MOD_ID, legacy, target});
        }
        catch (IOException ex) {
            LOGGER.warn("{}: failed to migrate legacy common config: {}", (Object)MOD_ID, (Object)ex.toString());
        }
    }
}

