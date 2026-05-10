/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.github.TeThoLaPot.tt_core.TT_core
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.registries.BuiltInRegistries
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.state.BlockState
 *  snownee.jade.api.BlockAccessor
 *  snownee.jade.api.IServerDataProvider
 */
package com.github.TeThoLaPot.regen_resources.platform.forge.compat.jade;

import com.github.TeThoLaPot.regen_resources.common.regen.RegenMineEligibility;
import com.github.TeThoLaPot.regen_resources.common.regen.RegenMineMarker;
import com.github.TeThoLaPot.regen_resources.common.regen.RegenRule;
import com.github.TeThoLaPot.regen_resources.common.regen.RegenRuleRegistry;
import com.github.TeThoLaPot.regen_resources.platform.RegenPlatformServices;
import com.github.TeThoLaPot.regen_resources.platform.forge.block.Re_Blocks;
import com.github.TeThoLaPot.tt_core.TT_core;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IServerDataProvider;

public enum RegenResourcesJadeServerData implements IServerDataProvider<BlockAccessor>
{
    INSTANCE;

    public static final String SYNC_EXECUTE_AT = "regen_j_sync_execute_at";
    public static final String SYNC_RESTORE_RL = "regen_j_sync_restore_rl";
    public static final String SYNC_RULE_MATCH = "regen_j_rule_match";
    public static final String SYNC_ALLOWS_REGEN = "regen_j_allows_regen";
    public static final String SYNC_PROBE_BLOCK = "regen_j_probe_block";
    public static final ResourceLocation UID;

    public ResourceLocation getUid() {
        return UID;
    }

    public void appendServerData(CompoundTag sync, BlockAccessor accessor) {
        RegenRule matched;
        CompoundTag stored;
        Level level = accessor.getLevel();
        if (!(level instanceof ServerLevel)) {
            return;
        }
        ServerLevel server = (ServerLevel)level;
        BlockPos pos = accessor.getPosition();
        BlockState state = accessor.getBlockState();
        if (state.m_60713_((Block)Re_Blocks.REGEN_BLOCK.get()) && !(stored = TT_core.getBlockData((ServerLevel)server, (BlockPos)pos)).m_128456_()) {
            String restore;
            if (stored.m_128441_("execute_at")) {
                sync.m_128356_(SYNC_EXECUTE_AT, stored.m_128454_("execute_at"));
            }
            if (!(restore = stored.m_128461_("restore_rl")).isBlank()) {
                sync.m_128359_(SYNC_RESTORE_RL, restore.trim());
            }
        }
        if ((matched = RegenRuleRegistry.firstMatch(server.m_46472_().m_135782_(), state)) != null) {
            RegenResourcesJadeServerData.putRuleEligibility(server, pos, sync, matched);
        }
    }

    public static CompoundTag buildJadeRuleProbeTag(ServerLevel server, BlockPos pos, BlockState state) {
        CompoundTag tag = new CompoundTag();
        tag.m_128405_("x", pos.m_123341_());
        tag.m_128405_("y", pos.m_123342_());
        tag.m_128405_("z", pos.m_123343_());
        tag.m_128359_(SYNC_PROBE_BLOCK, BuiltInRegistries.f_256975_.m_7981_((Object)state.m_60734_()).toString());
        RegenRule matched = RegenRuleRegistry.firstMatch(server.m_46472_().m_135782_(), state);
        if (matched == null) {
            tag.m_128379_(SYNC_RULE_MATCH, false);
            tag.m_128379_(SYNC_ALLOWS_REGEN, false);
            return tag;
        }
        RegenResourcesJadeServerData.putRuleEligibility(server, pos, tag, matched);
        return tag;
    }

    private static void putRuleEligibility(ServerLevel server, BlockPos pos, CompoundTag sync, RegenRule matched) {
        sync.m_128379_(SYNC_RULE_MATCH, true);
        CompoundTag placement = TT_core.getBlockData((ServerLevel)server, (BlockPos)pos);
        byte src = RegenMineMarker.readSourceByte(placement);
        boolean allow = RegenMineEligibility.allowsAfterBreak(src, RegenPlatformServices.CONFIG.allowNaturalRegen(), matched.naturalRegenOverride());
        sync.m_128379_(SYNC_ALLOWS_REGEN, allow);
    }

    static {
        UID = ResourceLocation.fromNamespaceAndPath((String)"regen_resources", (String)"regen_server_data");
    }
}

