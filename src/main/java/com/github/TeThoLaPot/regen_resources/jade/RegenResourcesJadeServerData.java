package com.github.TeThoLaPot.regen_resources.jade;

import com.github.TeThoLaPot.regen_resources.RegenConstants;
import com.github.TeThoLaPot.regen_resources.init.entity.RegenBlockEntity;
import com.github.TeThoLaPot.tt_core.TT_core;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IServerDataProvider;

/**
 * Jade が {@link TT_core} 側の再生データを参照できるよう、サーバーからクライアントへ同期する。
 */
public enum RegenResourcesJadeServerData implements IServerDataProvider<BlockAccessor> {
    INSTANCE;

    /** {@link BlockAccessor#getServerData()} に載せる実行時刻（ゲームティック）。 */
    public static final String SYNC_EXECUTE_AT = "regen_j_sync_execute_at";
    /** 復元先ブロックのレジストリ ID 文字列。 */
    public static final String SYNC_RESTORE_RL = "regen_j_sync_restore_rl";

    public static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath(RegenConstants.MOD_ID, "regen_server_data");

    @Override
    public ResourceLocation getUid() {
        return UID;
    }

    @Override
    public void appendServerData(CompoundTag sync, BlockAccessor accessor) {
        if (!(accessor.getLevel() instanceof ServerLevel server)) {
            return;
        }
        BlockEntity raw = accessor.getBlockEntity();
        if (!(raw instanceof RegenBlockEntity regen)) {
            return;
        }
        BlockPos pos = accessor.getPosition();
        CompoundTag stored = TT_core.getBlockData(server, pos);

        long executeAt = regen.getExecuteAt();
        String restoreRl = regen.getRestoreRlString();
        if (!stored.isEmpty()) {
            if (stored.contains("execute_at")) {
                executeAt = stored.getLong("execute_at");
            }
            if (stored.contains(RegenBlockEntity.TAG_RESTORE_RL)) {
                restoreRl = stored.getString(RegenBlockEntity.TAG_RESTORE_RL);
            }
        }

        sync.putLong(SYNC_EXECUTE_AT, executeAt);
        if (restoreRl != null && !restoreRl.isBlank()) {
            sync.putString(SYNC_RESTORE_RL, restoreRl.trim());
        }
    }
}
