package com.github.TeThoLaPot.regen_resources.jade;

import com.github.TeThoLaPot.regen_resources.RegenResources;
import com.github.TeThoLaPot.regen_resources.common.block.RegenCorruptionFallback;
import com.github.TeThoLaPot.regen_resources.common.regen.RegenMineEligibility;
import com.github.TeThoLaPot.regen_resources.common.regen.RegenMineMarker;
import com.github.TeThoLaPot.regen_resources.common.regen.RegenRuleRegistry;
import com.github.TeThoLaPot.regen_resources.forge.RegenResourcesForgeConfig;
import com.github.TeThoLaPot.regen_resources.forge.block.Re_Blocks;
import com.github.TeThoLaPot.tt_core.TT_core;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;

import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IServerDataProvider;

/**
 * Jade が TT 側の再生データを参照できるよう、サーバーからクライアントへ同期する。
 * alpha の {@code RegenResourcesJadeServerData} と同じ役割。
 */
public enum RegenResourcesJadeServerData implements IServerDataProvider<BlockAccessor> {
    INSTANCE;

    /** {@link BlockAccessor#getServerData()} に載せる実行時刻（ゲームティック）。 */
    public static final String SYNC_EXECUTE_AT = "regen_j_sync_execute_at";
    /** 復元先ブロックのレジストリ ID 文字列。 */
    public static final String SYNC_RESTORE_RL = "regen_j_sync_restore_rl";
    /** プリセットに一致する再生対象ブロックか（{@link RegenResourcesJadeRegenEligibleProvider} 用）。 */
    public static final String SYNC_RULE_MATCH = "regen_j_rule_match";
    /** {@link RegenMineEligibility#allowsAfterBreak(byte, boolean)} に基づく可否。 */
    public static final String SYNC_ALLOWS_REGEN = "regen_j_allows_regen";
    /** プローブ応答時のブロック ID（座標だけではキャッシュが古くなるため検証用）。 */
    public static final String SYNC_PROBE_BLOCK = "regen_j_probe_block";

    public static final ResourceLocation UID =
            ResourceLocation.fromNamespaceAndPath(RegenResources.MOD_ID, "regen_server_data");

    @Override
    public ResourceLocation getUid() {
        return UID;
    }

    @Override
    public void appendServerData(CompoundTag sync, BlockAccessor accessor) {
        if (!(accessor.getLevel() instanceof ServerLevel server)) {
            return;
        }
        BlockPos pos = accessor.getPosition();
        BlockState state = accessor.getBlockState();

        if (state.is(Re_Blocks.REGEN_BLOCK.get())) {
            CompoundTag stored = TT_core.getBlockData(server, pos);
            if (!stored.isEmpty()) {
                if (stored.contains(RegenCorruptionFallback.TT_EXECUTE_AT)) {
                    sync.putLong(SYNC_EXECUTE_AT, stored.getLong(RegenCorruptionFallback.TT_EXECUTE_AT));
                }
                String restore = stored.getString("restore_rl");
                if (!restore.isBlank()) {
                    sync.putString(SYNC_RESTORE_RL, restore.trim());
                }
            }
        }

        if (RegenRuleRegistry.firstMatch(server.dimension().location(), state) != null) {
            putRuleEligibility(server, pos, sync);
        }
    }

    /**
     * Jade のサーバー経路を使わず、独自ネットワークで送る用のタグ（alpha に近い「サーバー権威を別経路で渡す」）。
     */
    public static CompoundTag buildJadeRuleProbeTag(ServerLevel server, BlockPos pos, BlockState state) {
        CompoundTag tag = new CompoundTag();
        tag.putInt("x", pos.getX());
        tag.putInt("y", pos.getY());
        tag.putInt("z", pos.getZ());
        tag.putString(SYNC_PROBE_BLOCK, BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString());
        if (RegenRuleRegistry.firstMatch(server.dimension().location(), state) == null) {
            tag.putBoolean(SYNC_RULE_MATCH, false);
            tag.putBoolean(SYNC_ALLOWS_REGEN, false);
            return tag;
        }
        putRuleEligibility(server, pos, tag);
        return tag;
    }

    private static void putRuleEligibility(ServerLevel server, BlockPos pos, CompoundTag sync) {
        sync.putBoolean(SYNC_RULE_MATCH, true);
        CompoundTag placement = TT_core.getBlockData(server, pos);
        byte src = RegenMineMarker.readSourceByte(placement);
        boolean allow =
                RegenMineEligibility.allowsAfterBreak(src, RegenResourcesForgeConfig.ALLOW_NATURAL_REGEN.get());
        sync.putBoolean(SYNC_ALLOWS_REGEN, allow);
    }
}

