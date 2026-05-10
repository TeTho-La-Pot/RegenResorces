/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  net.minecraft.core.registries.BuiltInRegistries
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.item.AxeItem
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.state.BlockState
 *  org.jetbrains.annotations.Nullable
 *  org.slf4j.Logger
 */
package com.github.TeThoLaPot.regen_resources.common.block;

import com.mojang.logging.LogUtils;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.Map;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public final class RegenStrippedLogResolver {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Map<Block, Block> STRIPPABLES = RegenStrippedLogResolver.lookupStrippables();

    private RegenStrippedLogResolver() {
    }

    private static Map<Block, Block> lookupStrippables() {
        for (String name : new String[]{"STRIPPABLES", "f_150683_"}) {
            try {
                Field f = AxeItem.class.getDeclaredField(name);
                f.setAccessible(true);
                Object value = f.get(null);
                if (!(value instanceof Map)) continue;
                Map map = (Map)value;
                return map;
            }
            catch (NoSuchFieldException f) {
            }
            catch (Throwable t) {
                LOGGER.warn("RegenResources: failed to read AxeItem.{} via reflection: {}", (Object)name, (Object)t.toString());
            }
        }
        for (Field f : AxeItem.class.getDeclaredFields()) {
            if (!Modifier.isStatic(f.getModifiers()) || !Map.class.isAssignableFrom(f.getType())) continue;
            try {
                f.setAccessible(true);
                Object value = f.get(null);
                if (!(value instanceof Map)) continue;
                Map map = (Map)value;
                LOGGER.info("RegenResources: AxeItem.{} resolved as STRIPPABLES candidate", (Object)f.getName());
                return map;
            }
            catch (Throwable throwable) {
                // empty catch block
            }
        }
        LOGGER.warn("RegenResources: could not locate AxeItem.STRIPPABLES; stripped log preset will not auto-resolve");
        return Collections.emptyMap();
    }

    @Nullable
    public static Block resolveStripped(Block source) {
        if (source == null) {
            return null;
        }
        Block mapped = STRIPPABLES.get(source);
        if (mapped != null) {
            return mapped;
        }
        if (STRIPPABLES.containsValue(source)) {
            return source;
        }
        return null;
    }

    @Nullable
    public static Block resolveStripped(BlockState source) {
        if (source == null) {
            return null;
        }
        return RegenStrippedLogResolver.resolveStripped(source.m_60734_());
    }

    @Nullable
    public static ResourceLocation resolveStrippedId(BlockState source) {
        Block b = RegenStrippedLogResolver.resolveStripped(source);
        if (b == null) {
            return null;
        }
        return BuiltInRegistries.f_256975_.m_7981_((Object)b);
    }

    @Nullable
    public static Block fromId(@Nullable ResourceLocation id) {
        if (id == null) {
            return null;
        }
        Block b = (Block)BuiltInRegistries.f_256975_.m_7745_(id);
        if (b == null) {
            return null;
        }
        ResourceLocation actual = BuiltInRegistries.f_256975_.m_7981_((Object)b);
        if (actual != null && actual.equals((Object)id)) {
            return b;
        }
        return null;
    }

    @Nullable
    public static ResourceLocation barkIdForStripped(@Nullable ResourceLocation strippedId) {
        Block stripped = RegenStrippedLogResolver.fromId(strippedId);
        if (stripped == null) {
            return null;
        }
        for (Map.Entry<Block, Block> e : STRIPPABLES.entrySet()) {
            ResourceLocation key;
            if (e.getValue() != stripped || (key = BuiltInRegistries.f_256975_.m_7981_((Object)e.getKey())) == null) continue;
            return key;
        }
        return null;
    }

    public static Map<Block, Block> getAllPairs() {
        return Collections.unmodifiableMap(STRIPPABLES);
    }
}

