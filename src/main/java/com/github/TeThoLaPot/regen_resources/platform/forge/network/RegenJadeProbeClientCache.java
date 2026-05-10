/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.registries.BuiltInRegistries
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.level.Level
 *  snownee.jade.api.BlockAccessor
 */
package com.github.TeThoLaPot.regen_resources.platform.forge.network;

import com.github.TeThoLaPot.regen_resources.common.regen.RegenRuleRegistry;
import com.github.TeThoLaPot.regen_resources.platform.forge.network.RegenResourcesNetwork;
import com.github.TeThoLaPot.regen_resources.platform.forge.network.ServerboundJadeRegenProbePacket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import snownee.jade.api.BlockAccessor;

public final class RegenJadeProbeClientCache {
    private static final Map<String, Entry> CACHE = new ConcurrentHashMap<String, Entry>();
    private static final Map<String, Long> LAST_REQUEST_TICK = new ConcurrentHashMap<String, Long>();
    private static final int CACHE_TTL_TICKS = 200;
    private static final int REQUEST_INTERVAL_TICKS = 8;
    private static final int NEGATIVE_CACHE_HOLD_TICKS = 80;

    private RegenJadeProbeClientCache() {
    }

    private static String key(ResourceLocation dim, BlockPos pos) {
        return dim + "/" + pos.asLong();
    }

    public static void applyReply(Minecraft mc, CompoundTag payload) {
        if (mc.level == null || payload == null || payload.isEmpty()) {
            return;
        }
        BlockPos pos = new BlockPos(payload.getInt("x"), payload.getInt("y"), payload.getInt("z"));
        ResourceLocation dim = mc.level.dimension().location();
        String k = RegenJadeProbeClientCache.key(dim, pos);
        CACHE.put(k, new Entry(payload.copy(), mc.level.getGameTime()));
    }

    public static void invalidate(ResourceLocation dimensionId, BlockPos pos) {
        String k = RegenJadeProbeClientCache.key(dimensionId, pos);
        CACHE.remove(k);
        LAST_REQUEST_TICK.remove(k);
    }

    public static CompoundTag get(BlockAccessor accessor) {
        long ttl;
        BlockPos pos;
        Level level = accessor.getLevel();
        if (!level.isClientSide()) {
            return null;
        }
        ResourceLocation dim = level.dimension().location();
        String k = RegenJadeProbeClientCache.key(dim, pos = accessor.getPosition());
        Entry e = CACHE.get(k);
        if (e == null) {
            return null;
        }
        long age = level.getGameTime() - e.receivedTick;
        boolean negative = !e.tag.getBoolean("regen_j_rule_match");
        long l = ttl = negative ? 80L : 200L;
        if (age > ttl) {
            CACHE.remove(k);
            return null;
        }
        if (e.tag.getInt("x") != pos.getX() || e.tag.getInt("y") != pos.getY() || e.tag.getInt("z") != pos.getZ()) {
            return null;
        }
        String expectedRl = BuiltInRegistries.BLOCK.getKey(accessor.getBlockState().getBlock()).toString();
        String cachedRl = e.tag.getString("regen_j_probe_block");
        if (cachedRl.isEmpty() || !cachedRl.equals(expectedRl)) {
            CACHE.remove(k);
            return null;
        }
        return e.tag;
    }

    public static void requestIfNeeded(BlockAccessor accessor) {
        Long last;
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
        String k = RegenJadeProbeClientCache.key(dim, pos);
        Entry e = CACHE.get(k);
        if (e != null) {
            long hold;
            boolean negative = !e.tag.getBoolean("regen_j_rule_match");
            long l = hold = negative ? 80L : 200L;
            if (gt - e.receivedTick < hold) {
                return;
            }
        }
        if ((last = LAST_REQUEST_TICK.get(k)) != null && gt - last < 8L) {
            return;
        }
        LAST_REQUEST_TICK.put(k, gt);
        RegenResourcesNetwork.CHANNEL.sendToServer(new ServerboundJadeRegenProbePacket(dim, pos));
    }

    private record Entry(CompoundTag tag, long receivedTick) {
    }
}

