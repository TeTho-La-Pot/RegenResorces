/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.util.Pair
 *  com.mojang.logging.LogUtils
 *  net.minecraft.core.BlockPos
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.Block
 *  org.jetbrains.annotations.Nullable
 *  org.slf4j.Logger
 */
package com.github.TeThoLaPot.regen_resources.platform.forge.event;

import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

final class OreHarvesterChainProbe {
    private static final Logger LOG = LogUtils.getLogger();
    private static final long CACHE_TTL_MS = 1000L;
    @Nullable
    private static final Field HARVEST_SPEED_CACHE_FIELD;
    @Nullable
    private static final Method GET_ORES_NEXT_METHOD;

    private OreHarvesterChainProbe() {
    }

    static boolean willChain(ServerLevel level, ServerPlayer player) {
        if (HARVEST_SPEED_CACHE_FIELD == null) {
            return false;
        }
        try {
            Map map;
            Object raw = HARVEST_SPEED_CACHE_FIELD.get(null);
            if (!(raw instanceof Map) || (map = (Map)raw).isEmpty()) {
                return false;
            }
            long now = System.currentTimeMillis();
            for (Map.Entry entry : map.entrySet()) {
                Pair v;
                Object object;
                Pair key;
                Object k = entry.getKey();
                if (!(k instanceof Pair) || (key = (Pair)k).getFirst() != level || key.getSecond() != player) continue;
                Object value = entry.getValue();
                if (value instanceof Pair && (object = (v = (Pair)value).getFirst()) instanceof Date) {
                    Date date = (Date)object;
                    return now - date.getTime() <= 1000L;
                }
                return true;
            }
        }
        catch (IllegalAccessException illegalAccessException) {
            // empty catch block
        }
        return false;
    }

    static boolean clearCacheFor(ServerLevel level, ServerPlayer player) {
        if (HARVEST_SPEED_CACHE_FIELD == null) {
            return false;
        }
        try {
            Map map;
            Object raw = HARVEST_SPEED_CACHE_FIELD.get(null);
            if (!(raw instanceof Map) || (map = (Map)raw).isEmpty()) {
                return false;
            }
            boolean removed = false;
            Iterator it = map.entrySet().iterator();
            while (it.hasNext()) {
                Pair key;
                Map.Entry entry = it.next();
                Object k = entry.getKey();
                if (!(k instanceof Pair) || (key = (Pair)k).getFirst() != level || key.getSecond() != player) continue;
                it.remove();
                removed = true;
            }
            return removed;
        }
        catch (IllegalAccessException | UnsupportedOperationException ignored) {
            return false;
        }
    }

    @Nullable
    static List<BlockPos> findOreCluster(Level level, BlockPos origin, Block block) {
        if (GET_ORES_NEXT_METHOD == null) {
            return null;
        }
        try {
            Object raw = GET_ORES_NEXT_METHOD.invoke(null, level, origin, block);
            if (!(raw instanceof List)) {
                return null;
            }
            List list = (List)raw;
            ArrayList<BlockPos> out = new ArrayList<BlockPos>(list.size());
            for (Object o : list) {
                if (!(o instanceof BlockPos)) continue;
                BlockPos p = (BlockPos)o;
                out.add(p.m_7949_());
            }
            return out;
        }
        catch (ReflectiveOperationException e) {
            LOG.debug("OreHarvester cluster reflection call failed; falling back", (Throwable)e);
            return null;
        }
    }

    static {
        Field f = null;
        try {
            Class<?> variables = Class.forName("com.natamus.oreharvester_common_forge.data.Variables");
            f = variables.getDeclaredField("harvestSpeedCache");
            f.setAccessible(true);
        }
        catch (ClassNotFoundException | NoSuchFieldException e) {
            LOG.debug("OreHarvester internal cache not found; chain probe disabled", (Throwable)e);
        }
        HARVEST_SPEED_CACHE_FIELD = f;
        Method m = null;
        try {
            Class<?> util = Class.forName("com.natamus.oreharvester_common_forge.util.Util");
            m = util.getDeclaredMethod("getOresNextToEachOther", Level.class, BlockPos.class, Block.class);
            m.setAccessible(true);
        }
        catch (ClassNotFoundException | NoSuchMethodException e) {
            LOG.debug("OreHarvester cluster algorithm not found; will use internal BFS as fallback", (Throwable)e);
        }
        GET_ORES_NEXT_METHOD = m;
    }
}

