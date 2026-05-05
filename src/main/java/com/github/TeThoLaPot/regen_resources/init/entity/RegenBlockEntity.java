package com.github.TeThoLaPot.regen_resources.init.entity;

import com.github.TeThoLaPot.regen_resources.init.block.RegenBlocks;
import com.github.TeThoLaPot.regen_resources.network.RegenMimicClientCache;
import com.github.TeThoLaPot.regen_resources.util.RegenDiag;
import com.github.TeThoLaPot.tt_core.TT_core;
import com.github.TeThoLaPot.tt_core.api.TTDataUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * 再生ブロックの見た目（旧鉱石に合わせる）と Jade / TT / クライアント同期。
 */
public class RegenBlockEntity extends BlockEntity {

    /** TT / ネットワーク同期共通: 復元対象ブロック ID（状態は {@code mimic} と併記）。 */
    public static final String TAG_RESTORE_RL = "restore_rl";

    private long executeAt;
    private BlockState mimicState = Blocks.STONE.defaultBlockState();
    /** TT / NBT の {@link #TAG_RESTORE_RL}。ミミック解決前でも Jade 同期に使う。 */
    private @Nullable String restoreRlString;

    public RegenBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntities.REGEN_ORE_ENTITY.get(), pos, state);
    }

    /**
     * チャンク更新でクライアントへ載る初期 NBT が {@link #syncFromWorldStorage} より先に並ぶ問題への対処。
     * サーバーでレベルに載ったあと TT ストレージからミミックを読む。
     * <p>
     * 同じ呼び出し内で {@link TT_core#getBlockData} や SavedData へ触ると、チャンク生成・スポーン地点準備中に
     * ロックの取り方が衝突しワールド入場が 0〜N ％で止まる（クライアントは無応答に見える）ことがある。
     * そのため {@link MinecraftServer#execute} で次のサーバータスクに遅延する。
     */
    @Override
    public void setLevel(Level level) {
        super.setLevel(level);
        if (level.isClientSide() || !(level instanceof ServerLevel server)) {
            return;
        }
        MinecraftServer minecraftServer = server.getServer();
        BlockPos immutablePos = worldPosition.immutable();
        minecraftServer.execute(() -> {
            if (isRemoved()) {
                return;
            }
            if (!(this.level instanceof ServerLevel srv)) {
                return;
            }
            if (srv.getBlockEntity(immutablePos) != this) {
                return;
            }
            CompoundTag data = TT_core.getBlockData(srv, immutablePos);
            if (data == null || data.isEmpty()) {
                return;
            }
            applyPayloadFromStorage(data);
            setChanged();
        });
    }

    public long getExecuteAt() {
        return executeAt;
    }

    /** 見た目・輪郭に使う元ブロック。無いときはストーンの立方体。 */
    public @NotNull BlockState getMimicState() {
        return mimicState == null || mimicState.isAir() ? Blocks.STONE.defaultBlockState() : mimicState;
    }

    /** Jade 用: 復元先ブロック ID（未設定なら null）。 */
    public @Nullable String getRestoreRlString() {
        return restoreRlString == null || restoreRlString.isBlank() ? null : restoreRlString.trim();
    }

    /** サーバー側 {@link com.github.TeThoLaPot.tt_core.TT_core#getBlockData} のペイロードと整合させ、クライアントへ同期する */
    public void syncFromWorldStorage(ServerLevel level, BlockPos pos) {
        RegenDiag.log("syncFromWorldStorage begin pos={}", pos);
        CompoundTag data = TT_core.getBlockData(level, pos);
        applyPayloadFromStorage(data);
        flushToWatchingClients(level);
        RegenDiag.log("syncFromWorldStorage flush immediate pos={}", pos);
        /*
         * 設置ティックではクライアント側のブロック状態更新と並び順がずれ、BE データがストーンのまま採掘判定に使われる。
         * 次ティックで再送すると硬度・適正ツールがすぐ収束する。
         */
        scheduleFlushToWatchingClients(level);
    }

    private void flushToWatchingClients(ServerLevel server) {
        setChanged();
        sendBlockEntityPacketToClients(server);
        int notify = Block.UPDATE_NEIGHBORS | Block.UPDATE_CLIENTS | Block.UPDATE_IMMEDIATE;
        server.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), notify);
    }

    private void scheduleFlushToWatchingClients(ServerLevel server) {
        MinecraftServer minecraftServer = server.getServer();
        BlockPos immutablePos = worldPosition.immutable();
        minecraftServer.execute(() -> {
            if (!(server.getBlockEntity(immutablePos) instanceof RegenBlockEntity be)) {
                return;
            }
            if (be.isRemoved()) {
                return;
            }
            RegenDiag.log("syncFromWorldStorage flush deferred (execute) pos={}", immutablePos);
            be.flushToWatchingClients(server);
        });
    }

    /**
     * 統合環境では {@code TRACKING_CHUNK} だけでは届かないことがあるので、視界半径内のプレイヤーへ直接送る。
     */
    private void sendBlockEntityPacketToClients(ServerLevel server) {
        ClientboundBlockEntityDataPacket pkt = ClientboundBlockEntityDataPacket.create(this);
        if (pkt == null) {
            return;
        }
        Vec3 ctr = Vec3.atCenterOf(worldPosition);
        int vdChunks = Math.max(server.getServer().getPlayerList().getViewDistance(), 2);
        double radius = (vdChunks + 1) * 16.0;
        double radiusSq = radius * radius;

        for (ServerPlayer player : server.getServer().getPlayerList().getPlayers()) {
            if (!player.level().dimension().equals(server.dimension())) {
                continue;
            }
            if (player.distanceToSqr(ctr) > radiusSq) {
                continue;
            }
            player.connection.send(pkt);
        }
    }

    private void applyPayloadFromStorage(CompoundTag data) {
        if (data == null) {
            return;
        }
        if (data.contains("execute_at")) {
            executeAt = data.getLong("execute_at");
        }
        if (data.contains(TAG_RESTORE_RL)) {
            restoreRlString = data.getString(TAG_RESTORE_RL);
        }
        if (data.contains("state")) {
            mimicState = TTDataUtils.getBlockState(data, "state");
            if (mimicState != null && !mimicState.isAir()) {
                return;
            }
        }
        applyRestoreRl(data.getString(TAG_RESTORE_RL));
    }

    private void applySyncedClientTag(CompoundTag tag) {
        if (tag.contains("execute_at")) {
            executeAt = tag.getLong("execute_at");
        }
        if (tag.contains(TAG_RESTORE_RL)) {
            restoreRlString = tag.getString(TAG_RESTORE_RL);
        }
        if (tag.contains("mimic")) {
            CompoundTag raw = tag.getCompound("mimic");
            if (!raw.isEmpty()) {
                BlockState decoded = TTDataUtils.getBlockState(tag, "mimic");
                if (decoded != null && !decoded.isAir()) {
                    mimicState = decoded;
                    return;
                }
            }
        }
        applyRestoreRl(tag.getString(TAG_RESTORE_RL));
    }

    private void applyRestoreRl(String rawRl) {
        if (rawRl == null || rawRl.isBlank()) {
            return;
        }
        restoreRlString = rawRl.trim();
        ResourceLocation id = ResourceLocation.tryParse(rawRl.trim());
        if (id == null) {
            return;
        }
        var block = BuiltInRegistries.BLOCK.get(id);
        if (block.defaultBlockState().isAir()) {
            return;
        }
        mimicState = block.defaultBlockState();
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putLong("execute_at", executeAt);
        TTDataUtils.putBlockState(tag, "mimic", getMimicState());
        ResourceLocation bk = BuiltInRegistries.BLOCK.getKey(getMimicState().getBlock());
        if (bk != null) {
            tag.putString(TAG_RESTORE_RL, bk.toString());
            restoreRlString = bk.toString();
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        executeAt = tag.getLong("execute_at");
        if (tag.contains(TAG_RESTORE_RL)) {
            restoreRlString = tag.getString(TAG_RESTORE_RL);
        }
        if (tag.contains("mimic")) {
            mimicState = TTDataUtils.getBlockState(tag, "mimic");
        }
        if (mimicState == null || mimicState.isAir()) {
            applyRestoreRl(tag.getString(TAG_RESTORE_RL));
        }
    }

    @Override
    public @NotNull CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        tag.putLong("execute_at", executeAt);
        TTDataUtils.putBlockState(tag, "mimic", getMimicState());
        ResourceLocation bk = BuiltInRegistries.BLOCK.getKey(getMimicState().getBlock());
        if (bk != null) {
            tag.putString(TAG_RESTORE_RL, bk.toString());
        }
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        super.handleUpdateTag(tag);
        applySyncedClientTag(tag);
        if (level != null && level.isClientSide()) {
            RegenMimicClientCache.clear(level.dimension(), worldPosition);
        }
    }

    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(net.minecraft.network.Connection net, ClientboundBlockEntityDataPacket pkt) {
        CompoundTag tag = pkt.getTag();
        if (tag != null) {
            applySyncedClientTag(tag);
        }
        if (level != null && level.isClientSide()) {
            RegenMimicClientCache.clear(level.dimension(), worldPosition);
        }
    }

}
