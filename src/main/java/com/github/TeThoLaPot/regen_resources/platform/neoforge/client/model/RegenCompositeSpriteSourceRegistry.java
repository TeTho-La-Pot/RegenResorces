package com.github.TeThoLaPot.regen_resources.platform.neoforge.client.model;

import com.google.common.collect.BiMap;
import com.mojang.logging.LogUtils;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import net.minecraft.client.renderer.texture.atlas.SpriteSourceType;
import net.minecraft.client.renderer.texture.atlas.SpriteSources;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;

/** {@link RegenCompositeSpriteSource} を {@link SpriteSources} のレジストリへ注入する（アトラス JSON の type と対応）。 */
public final class RegenCompositeSpriteSourceRegistry {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final ResourceLocation TYPE_ID = ResourceLocation.fromNamespaceAndPath("regen_resources", "composite_strippable");
    private static volatile boolean registered;

    private RegenCompositeSpriteSourceRegistry() {}

    public static void ensureRegistered() {
        registerOnce();
    }

    private static synchronized void registerOnce() {
        if (registered) {
            return;
        }
        registered = true;
        BiMap<ResourceLocation, SpriteSourceType> types = lookupTypesMap();
        if (types == null) {
            LOGGER.warn("RegenResources: could not access SpriteSources.TYPES via reflection; composite log textures will fall back to vanilla baked model.");
            return;
        }
        if (types.containsKey(TYPE_ID)) {
            return;
        }
        types.put(TYPE_ID, RegenCompositeSpriteSource.TYPE);
        LOGGER.info("RegenResources: registered SpriteSourceType '{}'", TYPE_ID);
    }

    private static BiMap<ResourceLocation, SpriteSourceType> lookupTypesMap() {
        for (String name : new String[] {"TYPES", "TYPES"}) {
            try {
                Field f = SpriteSources.class.getDeclaredField(name);
                f.setAccessible(true);
                Object value = f.get(null);
                if (!(value instanceof BiMap<?, ?> map)) {
                    continue;
                }
                @SuppressWarnings("unchecked")
                BiMap<ResourceLocation, SpriteSourceType> typed = (BiMap<ResourceLocation, SpriteSourceType>) map;
                return typed;
            } catch (NoSuchFieldException ignored) {
            } catch (Throwable t) {
                LOGGER.warn("RegenResources: failed to read SpriteSources.{}: {}", name, t.toString());
            }
        }
        for (Field f : SpriteSources.class.getDeclaredFields()) {
            if (!Modifier.isStatic(f.getModifiers()) || !BiMap.class.isAssignableFrom(f.getType())) {
                continue;
            }
            try {
                f.setAccessible(true);
                Object value = f.get(null);
                if (!(value instanceof BiMap<?, ?> map)) {
                    continue;
                }
                LOGGER.info("RegenResources: SpriteSources.{} resolved as TYPES candidate", f.getName());
                @SuppressWarnings("unchecked")
                BiMap<ResourceLocation, SpriteSourceType> typed = (BiMap<ResourceLocation, SpriteSourceType>) map;
                return typed;
            } catch (Throwable ignored) {
            }
        }
        return null;
    }
}
