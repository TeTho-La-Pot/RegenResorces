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
        if (state.is((Block)Re_Blocks.REGEN_BLOCK.get()) && !(stored = TT_core.getBlockData((ServerLevel)server, (BlockPos)pos)).isEmpty()) {
            String restore;
            if (stored.contains("execute_at")) {
                sync.putLong(SYNC_EXECUTE_AT, stored.getLong("execute_at"));
            }
            if (!(restore = stored.getString("restore_rl")).isBlank()) {
                sync.putString(SYNC_RESTORE_RL, restore.trim());
            }
        }
        if ((matched = RegenRuleRegistry.firstMatch(server.dimension().location(), state)) != null) {
            RegenResourcesJadeServerData.putRuleEligibility(server, pos, sync, matched);
        }
    }

    public static CompoundTag buildJadeRuleProbeTag(ServerLevel server, BlockPos pos, BlockState state) {
        CompoundTag tag = new CompoundTag();
        tag.putInt("x", pos.getX());
        tag.putInt("y", pos.getY());
        tag.putInt("z", pos.getZ());
        tag.putString(SYNC_PROBE_BLOCK, BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString());
        RegenRule matched = RegenRuleRegistry.firstMatch(server.dimension().location(), state);
        if (matched == null) {
            tag.putBoolean(SYNC_RULE_MATCH, false);
            tag.putBoolean(SYNC_ALLOWS_REGEN, false);
            return tag;
        }
        RegenResourcesJadeServerData.putRuleEligibility(server, pos, tag, matched);
        return tag;
    }

    private static void putRuleEligibility(ServerLevel server, BlockPos pos, CompoundTag sync, RegenRule matched) {
        sync.putBoolean(SYNC_RULE_MATCH, true);
        CompoundTag placement = TT_core.getBlockData((ServerLevel)server, (BlockPos)pos);
        byte src = RegenMineMarker.readSourceByte(placement);
        boolean allow = RegenMineEligibility.allowsAfterBreak(src, RegenPlatformServices.CONFIG.allowNaturalRegen(), matched.naturalRegenOverride());
        sync.putBoolean(SYNC_ALLOWS_REGEN, allow);
    }

    static {
        UID = ResourceLocation.fromNamespaceAndPath((String)"regen_resources", (String)"regen_server_data");
    }
}

