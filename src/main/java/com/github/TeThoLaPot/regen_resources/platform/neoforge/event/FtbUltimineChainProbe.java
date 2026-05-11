package com.github.TeThoLaPot.regen_resources.platform.neoforge.event;

import com.mojang.logging.LogUtils;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.HitResult;
import net.neoforged.fml.ModList;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

/** FTB Ultimine 連鎖採掘の検出（リフレクション）。 */
final class FtbUltimineChainProbe {

    private static final Logger LOG = LogUtils.getLogger();
    static final String MOD_ID = "ftbultimine";
    private static final boolean LOADED = ModList.get().isLoaded(MOD_ID);
    @Nullable
    private static final Field INSTANCE_FIELD;
    @Nullable
    private static final Field IS_BREAKING_BLOCK_FIELD;
    @Nullable
    private static final Method GET_OR_CREATE_PLAYER_DATA_METHOD;
    @Nullable
    private static final Method IS_PRESSED_METHOD;
    @Nullable
    private static final Method UPDATE_BLOCKS_METHOD;
    @Nullable
    private static final Method CACHED_POSITIONS_METHOD;
    @Nullable
    private static final Method RAY_TRACE_METHOD;
    @Nullable
    private static final Method GET_MAX_BLOCKS_METHOD;

    private FtbUltimineChainProbe() {}

    static boolean isAvailable() {
        return LOADED && INSTANCE_FIELD != null && GET_OR_CREATE_PLAYER_DATA_METHOD != null && IS_PRESSED_METHOD != null;
    }

    static boolean isChaining() {
        if (!isAvailable() || IS_BREAKING_BLOCK_FIELD == null) {
            return false;
        }
        try {
            Object instance = INSTANCE_FIELD.get(null);
            if (instance == null) {
                return false;
            }
            return IS_BREAKING_BLOCK_FIELD.getBoolean(instance);
        } catch (IllegalAccessException e) {
            return false;
        }
    }

    static boolean isPressed(ServerPlayer player) {
        if (!isAvailable()) {
            return false;
        }
        try {
            Object instance = INSTANCE_FIELD.get(null);
            if (instance == null) {
                return false;
            }
            Object data = GET_OR_CREATE_PLAYER_DATA_METHOD.invoke(instance, player);
            if (data == null) {
                return false;
            }
            Object pressed = IS_PRESSED_METHOD.invoke(data);
            return pressed instanceof Boolean b && b;
        } catch (ReflectiveOperationException e) {
            return false;
        }
    }

    @Nullable
    static HitResult rayTrace(ServerPlayer player) {
        if (!isAvailable() || RAY_TRACE_METHOD == null) {
            return null;
        }
        try {
            Object result = RAY_TRACE_METHOD.invoke(null, player);
            return result instanceof HitResult hr ? hr : null;
        } catch (ReflectiveOperationException e) {
            return null;
        }
    }

    @Nullable
    static List<BlockPos> findCluster(ServerPlayer player, BlockPos origin, Direction face) {
        if (!isAvailable() || UPDATE_BLOCKS_METHOD == null || CACHED_POSITIONS_METHOD == null) {
            return null;
        }
        try {
            Object instance = INSTANCE_FIELD.get(null);
            if (instance == null) {
                return null;
            }
            Object data = GET_OR_CREATE_PLAYER_DATA_METHOD.invoke(instance, player);
            if (data == null) {
                return null;
            }
            int maxBlocks = resolveMaxBlocks(player);
            if (maxBlocks <= 0) {
                return null;
            }
            UPDATE_BLOCKS_METHOD.invoke(data, player, origin, face, false, maxBlocks);
            Object posCol = CACHED_POSITIONS_METHOD.invoke(data);
            if (!(posCol instanceof Collection<?> cb) || cb.isEmpty()) {
                return null;
            }
            ArrayList<BlockPos> out = new ArrayList<>(cb.size());
            for (Object o : cb) {
                if (o instanceof BlockPos p) {
                    out.add(p.immutable());
                }
            }
            return out;
        } catch (ReflectiveOperationException e) {
            LOG.debug("FTBUltimine cluster reflection call failed", e);
            return null;
        }
    }

    private static int resolveMaxBlocks(ServerPlayer player) {
        if (GET_MAX_BLOCKS_METHOD == null) {
            return 64;
        }
        try {
            Object v = GET_MAX_BLOCKS_METHOD.invoke(null, player);
            return v instanceof Integer i ? i : 64;
        } catch (ReflectiveOperationException e) {
            return 64;
        }
    }

    static {
        Field instanceField = null;
        Field isBreakingField = null;
        Method getOrCreatePlayerData = null;
        Method isPressed = null;
        Method updateBlocks = null;
        Method cachedPositions = null;
        Method rayTrace = null;
        Method getMaxBlocks = null;
        if (LOADED) {
            try {
                Class<?> ftbu = Class.forName("dev.ftb.mods.ftbultimine.FTBUltimine");
                instanceField = ftbu.getDeclaredField("instance");
                instanceField.setAccessible(true);
                isBreakingField = ftbu.getDeclaredField("isBreakingBlock");
                isBreakingField.setAccessible(true);
                getOrCreatePlayerData = ftbu.getMethod("getOrCreatePlayerData", Player.class);
            } catch (ReflectiveOperationException e) {
                LOG.debug("FTBUltimine class introspection failed; chain probe disabled", e);
            }
            try {
                Class<?> data = Class.forName("dev.ftb.mods.ftbultimine.FTBUltiminePlayerData");
                isPressed = data.getMethod("isPressed");
                cachedPositions = data.getMethod("cachedPositions");
                updateBlocks =
                        data.getMethod("updateBlocks", ServerPlayer.class, BlockPos.class, Direction.class, boolean.class, int.class);
                rayTrace = data.getMethod("rayTrace", ServerPlayer.class);
            } catch (ReflectiveOperationException e) {
                LOG.debug("FTBUltiminePlayerData introspection failed; chain probe disabled", e);
            }
            try {
                Class<?> conf = Class.forName("dev.ftb.mods.ftbultimine.config.FTBUltimineServerConfig");
                getMaxBlocks = conf.getMethod("getMaxBlocks", ServerPlayer.class);
            } catch (ReflectiveOperationException e) {
                LOG.debug("FTBUltimineServerConfig.getMaxBlocks not found; using fallback", e);
            }
        }
        INSTANCE_FIELD = instanceField;
        IS_BREAKING_BLOCK_FIELD = isBreakingField;
        GET_OR_CREATE_PLAYER_DATA_METHOD = getOrCreatePlayerData;
        IS_PRESSED_METHOD = isPressed;
        UPDATE_BLOCKS_METHOD = updateBlocks;
        CACHED_POSITIONS_METHOD = cachedPositions;
        RAY_TRACE_METHOD = rayTrace;
        GET_MAX_BLOCKS_METHOD = getMaxBlocks;
    }
}
