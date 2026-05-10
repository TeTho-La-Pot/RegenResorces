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
        return dim + "/" + pos.m_121878_();
    }

    public static void applyReply(Minecraft mc, CompoundTag payload) {
        if (mc.f_91073_ == null || payload == null || payload.m_128456_()) {
            return;
        }
        BlockPos pos = new BlockPos(payload.m_128451_("x"), payload.m_128451_("y"), payload.m_128451_("z"));
        ResourceLocation dim = mc.f_91073_.m_46472_().m_135782_();
        String k = RegenJadeProbeClientCache.key(dim, pos);
        CACHE.put(k, new Entry(payload.m_6426_(), mc.f_91073_.m_46467_()));
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
        if (!level.m_5776_()) {
            return null;
        }
        ResourceLocation dim = level.m_46472_().m_135782_();
        String k = RegenJadeProbeClientCache.key(dim, pos = accessor.getPosition());
        Entry e = CACHE.get(k);
        if (e == null) {
            return null;
        }
        long age = level.m_46467_() - e.receivedTick;
        boolean negative = !e.tag.m_128471_("regen_j_rule_match");
        long l = ttl = negative ? 80L : 200L;
        if (age > ttl) {
            CACHE.remove(k);
            return null;
        }
        if (e.tag.m_128451_("x") != pos.m_123341_() || e.tag.m_128451_("y") != pos.m_123342_() || e.tag.m_128451_("z") != pos.m_123343_()) {
            return null;
        }
        String expectedRl = BuiltInRegistries.f_256975_.m_7981_((Object)accessor.getBlockState().m_60734_()).toString();
        String cachedRl = e.tag.m_128461_("regen_j_probe_block");
        if (cachedRl.isEmpty() || !cachedRl.equals(expectedRl)) {
            CACHE.remove(k);
            return null;
        }
        return e.tag;
    }

    public static void requestIfNeeded(BlockAccessor accessor) {
        Long last;
        Level level = accessor.getLevel();
        if (!level.m_5776_()) {
            return;
        }
        if (accessor.getBlockEntity() != null) {
            return;
        }
        if (!RegenRuleRegistry.matchesPresetTargetsIgnoringDimension(accessor.getBlockState())) {
            return;
        }
        ResourceLocation dim = level.m_46472_().m_135782_();
        BlockPos pos = accessor.getPosition();
        long gt = level.m_46467_();
        String k = RegenJadeProbeClientCache.key(dim, pos);
        Entry e = CACHE.get(k);
        if (e != null) {
            long hold;
            boolean negative = !e.tag.m_128471_("regen_j_rule_match");
            long l = hold = negative ? 80L : 200L;
            if (gt - e.receivedTick < hold) {
                return;
            }
        }
        if ((last = LAST_REQUEST_TICK.get(k)) != null && gt - last < 8L) {
            return;
        }
        LAST_REQUEST_TICK.put(k, gt);
        RegenResourcesNetwork.CHANNEL.sendToServer((Object)new ServerboundJadeRegenProbePacket(dim, pos));
    }

    private record Entry(CompoundTag tag, long receivedTick) {
    }
}

