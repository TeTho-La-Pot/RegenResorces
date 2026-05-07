package com.github.TeThoLaPot.regen_resources.jade;

import com.github.TeThoLaPot.regen_resources.RegenResources;
import com.github.TeThoLaPot.regen_resources.common.block.RegenCorruptionFallback;
import com.github.TeThoLaPot.regen_resources.forge.block.Re_Blocks;
import com.github.TeThoLaPot.tt_core.TT_core;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
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
        if (!accessor.getBlockState().is(Re_Blocks.REGEN_BLOCK.get())) {
            return;
        }
        BlockPos pos = accessor.getPosition();
        CompoundTag stored = TT_core.getBlockData(server, pos);
        if (stored.isEmpty()) {
            return;
        }
        if (stored.contains(RegenCorruptionFallback.TT_EXECUTE_AT)) {
            sync.putLong(SYNC_EXECUTE_AT, stored.getLong(RegenCorruptionFallback.TT_EXECUTE_AT));
        }
        String restore = stored.getString("restore_rl");
        if (!restore.isBlank()) {
            sync.putString(SYNC_RESTORE_RL, restore.trim());
        }
    }
}

