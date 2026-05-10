package com.github.TeThoLaPot.regen_resources.common.block;

import com.github.TeThoLaPot.tt_core.TT_core;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * Jade がサーバー同期データを取得できるようにするためのプレースホルダ。
 * 再生状態そのものは {@link com.github.TeThoLaPot.tt_core.TT_core} に保存される。
 * <p>サーバー側ティックで、データ欠落・期限超過の再生シェルを {@link RegenCorruptionFallback} で除去する。
 * <p>プリセットダミーブロックは {@link #mimicAppearance} で見た目・採掘参照を保持する。
 */
public final class RegenBlockEntity extends BlockEntity {

    private static final String TAG_MIMIC = "MimicAppearance";
    private static final String TAG_CUSTOM_CYCLE = "CustomPresetCycle";

    /** {@link RegenBlocks} シェル用：チャンク荷降ろしでリセットされる経過 tick。 */
    private int watchdogWarmupTicks;

    @Nullable
    private BlockState mimicAppearance;

    /** {@link CustomPresetDummyBlock} が同一ブロックに複数ルールがあるときのサイクル。 */
    private int customPresetCycle;

    public RegenBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Nullable
    public BlockState getMimicAppearance() {
        return mimicAppearance;
    }

    public void setMimicAppearance(@Nullable BlockState mimicAppearance) {
        this.mimicAppearance = mimicAppearance;
    }

    public int getCustomPresetCycle() {
        return customPresetCycle;
    }

    public void advanceCustomPresetCycle() {
        this.customPresetCycle++;
    }

    public void syncToClients() {
        setChanged();
        if (level != null && !level.isClientSide()) {
            BlockState st = getBlockState();
            level.sendBlockUpdated(worldPosition, st, st, Block.UPDATE_CLIENTS);
        }
    }

    public static void tick(Level level, BlockPos pos, BlockState state, RegenBlockEntity be) {
        if (!(level instanceof ServerLevel sl)) {
            return;
        }
        if (!(state.getBlock() instanceof RegenBlocks)) {
            return;
        }

        be.watchdogWarmupTicks++;
        if (be.watchdogWarmupTicks < RegenCorruptionFallback.MIN_WATCHDOG_WARMUP_TICKS) {
            return;
        }

        CompoundTag data = TT_core.getBlockData(sl, pos);

        if (data.isEmpty()) {
            RegenCorruptionFallback.apply(sl, pos, state);
            return;
        }

        if (!data.contains(RegenCorruptionFallback.TT_EXECUTE_AT, CompoundTag.TAG_LONG)) {
            RegenCorruptionFallback.apply(sl, pos, state);
            return;
        }

        long executeAt = data.getLong(RegenCorruptionFallback.TT_EXECUTE_AT);
        long now = sl.getGameTime();
        if (now <= executeAt + RegenCorruptionFallback.POST_EXECUTE_GRACE_TICKS) {
            return;
        }

        RegenCorruptionFallback.apply(sl, pos, state);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (mimicAppearance != null) {
            tag.put(TAG_MIMIC, NbtUtils.writeBlockState(mimicAppearance));
        }
        if (customPresetCycle != 0) {
            tag.putInt(TAG_CUSTOM_CYCLE, customPresetCycle);
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        mimicAppearance = null;
        customPresetCycle = 0;
        if (tag.contains(TAG_MIMIC, Tag.TAG_COMPOUND)) {
            try {
                mimicAppearance =
                        NbtUtils.readBlockState(registries.lookupOrThrow(Registries.BLOCK), tag.getCompound(TAG_MIMIC));
            } catch (RuntimeException ignored) {
                mimicAppearance = null;
            }
        }
        if (tag.contains(TAG_CUSTOM_CYCLE, Tag.TAG_INT)) {
            customPresetCycle = tag.getInt(TAG_CUSTOM_CYCLE);
        }
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        saveAdditional(tag, registries);
        return tag;
    }
}
