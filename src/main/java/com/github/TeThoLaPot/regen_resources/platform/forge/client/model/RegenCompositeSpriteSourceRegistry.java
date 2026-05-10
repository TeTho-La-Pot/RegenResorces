/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.BiMap
 *  com.mojang.logging.LogUtils
 *  net.minecraft.client.renderer.texture.atlas.SpriteSourceType
 *  net.minecraft.client.renderer.texture.atlas.SpriteSources
 *  net.minecraft.resources.ResourceLocation
 *  org.slf4j.Logger
 */
package com.github.TeThoLaPot.regen_resources.platform.forge.client.model;

import com.github.TeThoLaPot.regen_resources.platform.forge.client.model.RegenCompositeSpriteSource;
import com.google.common.collect.BiMap;
import com.mojang.logging.LogUtils;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import net.minecraft.client.renderer.texture.atlas.SpriteSourceType;
import net.minecraft.client.renderer.texture.atlas.SpriteSources;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;

public final class RegenCompositeSpriteSourceRegistry {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final ResourceLocation TYPE_ID = ResourceLocation.fromNamespaceAndPath((String)"regen_resources", (String)"composite_strippable");
    private static volatile boolean registered = false;

    private RegenCompositeSpriteSourceRegistry() {
    }

    public static void ensureRegistered() {
        RegenCompositeSpriteSourceRegistry.registerOnce();
    }

    private static synchronized void registerOnce() {
        if (registered) {
            return;
        }
        registered = true;
        BiMap<ResourceLocation, SpriteSourceType> types = RegenCompositeSpriteSourceRegistry.lookupTypesMap();
        if (types == null) {
            LOGGER.warn("RegenResources: could not access SpriteSources.TYPES via reflection; composite log textures will fall back to vanilla baked model.");
            return;
        }
        if (types.containsKey(TYPE_ID)) {
            return;
        }
        types.put(TYPE_ID, RegenCompositeSpriteSource.TYPE);
        LOGGER.info("RegenResources: registered SpriteSourceType '{}'", (Object)TYPE_ID);
    }

    private static BiMap<ResourceLocation, SpriteSourceType> lookupTypesMap() {
        for (String name : new String[]{"TYPES", "TYPES"}) {
            try {
                Field f = SpriteSources.class.getDeclaredField(name);
                f.setAccessible(true);
                Object value = f.get(null);
                if (!(value instanceof BiMap)) continue;
                BiMap map = (BiMap)value;
                return map;
            }
            catch (NoSuchFieldException f) {
            }
            catch (Throwable t) {
                LOGGER.warn("RegenResources: failed to read SpriteSources.{}: {}", (Object)name, (Object)t.toString());
            }
        }
        for (Field f : SpriteSources.class.getDeclaredFields()) {
            if (!Modifier.isStatic(f.getModifiers()) || !BiMap.class.isAssignableFrom(f.getType())) continue;
            try {
                f.setAccessible(true);
                Object value = f.get(null);
                if (!(value instanceof BiMap)) continue;
                BiMap map = (BiMap)value;
                LOGGER.info("RegenResources: SpriteSources.{} resolved as TYPES candidate", (Object)f.getName());
                return map;
            }
            catch (Throwable throwable) {
                // empty catch block
            }
        }
        return null;
    }
}

