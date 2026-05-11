package com.github.TeThoLaPot.regen_resources.platform.neoforge.event;

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

/**
 * OreHarvester の内部キャッシュ／クラスタ探索へ反射でアクセスする。
 * <p>1.20.1 時代は {@code com.natamus.oreharvester_common_forge.*}。現行 Serilum は Common 統合で
 * {@code com.natamus.oreharvester.*}（例: {@code Variables#harvestSpeedCache}）に移行しているため両方を試す。
 */
final class OreHarvesterChainProbe {

    private static final Logger LOG = LogUtils.getLogger();
    private static final long CACHE_TTL_MS = 1000L;
    private static final String[] VARIABLES_CLASS_CANDIDATES = {
        "com.natamus.oreharvester.data.Variables",
        "com.natamus.oreharvester_common_forge.data.Variables",
        "com.natamus.oreharvester_common_neoforge.data.Variables"
    };
    private static final String[] UTIL_CLASS_CANDIDATES = {
        "com.natamus.oreharvester.util.Util",
        "com.natamus.oreharvester_common_forge.util.Util",
        "com.natamus.oreharvester_common_neoforge.util.Util"
    };
    @Nullable
    private static final Field HARVEST_SPEED_CACHE_FIELD;
    @Nullable
    private static final Method GET_ORES_NEXT_METHOD;

    private OreHarvesterChainProbe() {}

    static boolean willChain(ServerLevel level, ServerPlayer player) {
        if (HARVEST_SPEED_CACHE_FIELD == null) {
            return false;
        }
        try {
            Object raw = HARVEST_SPEED_CACHE_FIELD.get(null);
            if (!(raw instanceof Map<?, ?> map) || map.isEmpty()) {
                return false;
            }
            long now = System.currentTimeMillis();
            for (Object entryObj : map.entrySet()) {
                if (!(entryObj instanceof Map.Entry<?, ?> entry)) {
                    continue;
                }
                Object k = entry.getKey();
                if (!(k instanceof Pair<?, ?> key)) {
                    continue;
                }
                if (key.getFirst() != level || key.getSecond() != player) {
                    continue;
                }
                Object value = entry.getValue();
                if (value instanceof Pair<?, ?> v && v.getFirst() instanceof Date date) {
                    return now - date.getTime() <= CACHE_TTL_MS;
                }
                return true;
            }
        } catch (IllegalAccessException ignored) {
        }
        return false;
    }

    static boolean clearCacheFor(ServerLevel level, ServerPlayer player) {
        if (HARVEST_SPEED_CACHE_FIELD == null) {
            return false;
        }
        try {
            Object raw = HARVEST_SPEED_CACHE_FIELD.get(null);
            if (!(raw instanceof Map<?, ?> map) || map.isEmpty()) {
                return false;
            }
            boolean removed = false;
            var it = map.entrySet().iterator();
            while (it.hasNext()) {
                var entry = it.next();
                Object k = entry.getKey();
                if (!(k instanceof Pair<?, ?> key)) {
                    continue;
                }
                if (key.getFirst() != level || key.getSecond() != player) {
                    continue;
                }
                it.remove();
                removed = true;
            }
            return removed;
        } catch (IllegalAccessException | UnsupportedOperationException ignored) {
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
            if (!(raw instanceof List<?> list)) {
                return null;
            }
            ArrayList<BlockPos> out = new ArrayList<>(list.size());
            for (Object o : list) {
                if (o instanceof BlockPos p) {
                    out.add(p.immutable());
                }
            }
            return out;
        } catch (ReflectiveOperationException e) {
            LOG.debug("OreHarvester cluster reflection call failed; falling back", e);
            return null;
        }
    }

    @Nullable
    private static Field resolveHarvestSpeedCacheField() {
        for (String className : VARIABLES_CLASS_CANDIDATES) {
            try {
                Class<?> variables = Class.forName(className);
                Field field = variables.getDeclaredField("harvestSpeedCache");
                field.setAccessible(true);
                LOG.debug("OreHarvester harvestSpeedCache bound to {}", className);
                return field;
            } catch (ClassNotFoundException | NoSuchFieldException e) {
                LOG.debug("OreHarvester Variables not at {} ({})", className, e.toString());
            }
        }
        LOG.debug("OreHarvester internal cache not found on any known classpath; chain probe disabled");
        return null;
    }

    @Nullable
    private static Method resolveGetOresNextMethod() {
        for (String className : UTIL_CLASS_CANDIDATES) {
            try {
                Class<?> util = Class.forName(className);
                Method method = util.getDeclaredMethod("getOresNextToEachOther", Level.class, BlockPos.class, Block.class);
                method.setAccessible(true);
                LOG.debug("OreHarvester getOresNextToEachOther bound to {}", className);
                return method;
            } catch (ClassNotFoundException | NoSuchMethodException e) {
                LOG.debug("OreHarvester Util not at {} ({})", className, e.toString());
            }
        }
        LOG.debug("OreHarvester cluster algorithm not found; will use internal BFS as fallback");
        return null;
    }

    static {
        HARVEST_SPEED_CACHE_FIELD = resolveHarvestSpeedCacheField();
        GET_ORES_NEXT_METHOD = resolveGetOresNextMethod();
    }
}
