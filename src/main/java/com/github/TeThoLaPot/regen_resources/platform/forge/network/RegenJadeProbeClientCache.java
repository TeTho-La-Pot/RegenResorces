package com.github.TeThoLaPot.regen_resources.platform.forge.network;

import com.github.TeThoLaPot.regen_resources.common.regen.RegenRuleRegistry;
import com.github.TeThoLaPot.regen_resources.platform.forge.compat.jade.RegenResourcesJadeServerData;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import snownee.jade.api.BlockAccessor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * alpha と同様に Jade のサーバー経路に頼らず、専用パケットで届いた可否を保持する。
 */
public final class RegenJadeProbeClientCache {

    private static final Map<String, Entry> CACHE = new ConcurrentHashMap<>();
    private static final Map<String, Long> LAST_REQUEST_TICK = new ConcurrentHashMap<>();

    private static final int CACHE_TTL_TICKS = 200;
    private static final int REQUEST_INTERVAL_TICKS = 8;
    private static final int NEGATIVE_CACHE_HOLD_TICKS = 80;

    private record Entry(CompoundTag tag, long receivedTick) {}

    private RegenJadeProbeClientCache() {}

    private static String key(ResourceLocation dim, BlockPos pos) {
        return dim + "/" + pos.asLong();
    }

    public static void applyReply(Minecraft mc, CompoundTag payload) {
        if (mc.level == null || payload == null || payload.isEmpty()) {
            return;
        }
        BlockPos pos = new BlockPos(payload.getInt("x"), payload.getInt("y"), payload.getInt("z"));
        ResourceLocation dim = mc.level.dimension().location();
        String k = key(dim, pos);
        CACHE.put(k, new Entry(payload.copy(), mc.level.getGameTime()));
    }

    /** サーバーが TT を書き換えた座標（ピストン移動など）でキャッシュを捨て、再プローブさせる。 */
    public static void invalidate(ResourceLocation dimensionId, BlockPos pos) {
        String k = key(dimensionId, pos);
        CACHE.remove(k);
        LAST_REQUEST_TICK.remove(k);
    }

    /** Jade の {@link BlockAccessor#getServerData()} に載っていないときのフォールバック。 */
    public static CompoundTag get(BlockAccessor accessor) {
        Level level = accessor.getLevel();
        if (!level.isClientSide()) {
            return null;
        }
        ResourceLocation dim = level.dimension().location();
        BlockPos pos = accessor.getPosition();
        String k = key(dim, pos);
        Entry e = CACHE.get(k);
        if (e == null) {
            return null;
        }
        long age = level.getGameTime() - e.receivedTick;
        boolean negative = !e.tag.getBoolean(RegenResourcesJadeServerData.SYNC_RULE_MATCH);
        long ttl = negative ? NEGATIVE_CACHE_HOLD_TICKS : CACHE_TTL_TICKS;
        if (age > ttl) {
            CACHE.remove(k);
            return null;
        }
        if (e.tag.getInt("x") != pos.getX()
                || e.tag.getInt("y") != pos.getY()
                || e.tag.getInt("z") != pos.getZ()) {
            return null;
        }
        String expectedRl = BuiltInRegistries.BLOCK.getKey(accessor.getBlockState().getBlock()).toString();
        String cachedRl = e.tag.getString(RegenResourcesJadeServerData.SYNC_PROBE_BLOCK);
        if (cachedRl.isEmpty() || !cachedRl.equals(expectedRl)) {
            CACHE.remove(k);
            return null;
        }
        return e.tag;
    }

    public static void requestIfNeeded(BlockAccessor accessor) {
        Level level = accessor.getLevel();
        if (!level.isClientSide()) {
            return;
        }
        if (accessor.getBlockEntity() != null) {
            return;
        }
        if (!RegenRuleRegistry.matchesPresetTargetsIgnoringDimension(accessor.getBlockState())) {
            return;
        }
        ResourceLocation dim = level.dimension().location();
        BlockPos pos = accessor.getPosition();
        long gt = level.getGameTime();
        String k = key(dim, pos);

        Entry e = CACHE.get(k);
        if (e != null) {
            boolean negative = !e.tag.getBoolean(RegenResourcesJadeServerData.SYNC_RULE_MATCH);
            long hold = negative ? NEGATIVE_CACHE_HOLD_TICKS : CACHE_TTL_TICKS;
            if (gt - e.receivedTick < hold) {
                return;
            }
        }

        Long last = LAST_REQUEST_TICK.get(k);
        if (last != null && gt - last < REQUEST_INTERVAL_TICKS) {
            return;
        }
        LAST_REQUEST_TICK.put(k, gt);
        RegenResourcesNetwork.CHANNEL.sendToServer(new ServerboundJadeRegenProbePacket(dim, pos));
    }
}
