package com.github.TeThoLaPot.regen_resources.common.block;

import com.github.TeThoLaPot.regen_resources.common.clientbridge.RegenStrippedCompositeClientHooks;
import com.github.TeThoLaPot.tt_core.TT_core;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * Jade / TT_core と連携する再生シェル用 BE。1.20.1 と同様にストリップ ID・カスタム見た目・カスタムダミー用サイクルを保持する。
 */
public final class RegenBlockEntity extends BlockEntity {

    private static final String TAG_STRIPPED_BLOCK = "stripped_block";
    private static final String TAG_CUSTOM_SPEC = "custom_spec";
    private static final String TAG_CUSTOM_DUMMY_CYCLE = "custom_dummy_cycle";
    private static final String TAG_CUSTOM_DUMMY_CYCLE_TARGET = "custom_dummy_cycle_tgt";

    private int watchdogWarmupTicks;
    @Nullable
    private ResourceLocation strippedBlockId;
    @Nullable
    private RegenCustomVisualSpec customVisualSpec;
    private int customPresetDummyCycleIndex = -1;
    @Nullable
    private ResourceLocation customPresetDummyCycleTarget;

    public RegenBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
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

    @Nullable
    public ResourceLocation getStrippedBlockId() {
        return strippedBlockId;
    }

    @Nullable
    public RegenCustomVisualSpec getCustomVisualSpec() {
        return customVisualSpec;
    }

    public int customPresetDummyCycleIndex() {
        return customPresetDummyCycleIndex;
    }

    public void setCustomPresetDummyCycleIndex(int customPresetDummyCycleIndex) {
        this.customPresetDummyCycleIndex = customPresetDummyCycleIndex;
        setChanged();
    }

    @Nullable
    public ResourceLocation customPresetDummyCycleTarget() {
        return customPresetDummyCycleTarget;
    }

    public void setCustomPresetDummyCycleTarget(@Nullable ResourceLocation customPresetDummyCycleTarget) {
        this.customPresetDummyCycleTarget = customPresetDummyCycleTarget;
        setChanged();
    }

    public void resetCustomPresetDummyCycle() {
        this.customPresetDummyCycleIndex = -1;
        this.customPresetDummyCycleTarget = null;
        setChanged();
    }

    public void initStrippedBlockId(@Nullable ResourceLocation id) {
        this.strippedBlockId = id;
    }

    public void initCustomVisualSpec(@Nullable RegenCustomVisualSpec spec) {
        this.customVisualSpec = spec;
    }

    public void setStrippedBlockId(@Nullable ResourceLocation id) {
        if (Objects.equals(this.strippedBlockId, id)) {
            return;
        }
        this.strippedBlockId = id;
        setChanged();
        if (level != null && level.isClientSide()) {
            requestModelDataUpdate();
            BlockState st = getBlockState();
            level.sendBlockUpdated(getBlockPos(), st, st, Block.UPDATE_CLIENTS);
            return;
        }
        if (level != null) {
            BlockState st = getBlockState();
            level.sendBlockUpdated(getBlockPos(), st, st, Block.UPDATE_CLIENTS);
        }
    }

    public void setCustomVisualSpec(@Nullable RegenCustomVisualSpec spec) {
        if (Objects.equals(this.customVisualSpec, spec)) {
            return;
        }
        this.customVisualSpec = spec;
        setChanged();
        if (level != null && level.isClientSide()) {
            requestModelDataUpdate();
            BlockState st = getBlockState();
            level.sendBlockUpdated(getBlockPos(), st, st, Block.UPDATE_CLIENTS);
            return;
        }
        if (level != null) {
            BlockState st = getBlockState();
            level.sendBlockUpdated(getBlockPos(), st, st, Block.UPDATE_CLIENTS);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (strippedBlockId != null) {
            tag.putString(TAG_STRIPPED_BLOCK, strippedBlockId.toString());
        }
        if (customVisualSpec != null) {
            tag.put(TAG_CUSTOM_SPEC, customVisualSpec.writeNbt());
        }
        tag.putInt(TAG_CUSTOM_DUMMY_CYCLE, customPresetDummyCycleIndex);
        if (customPresetDummyCycleTarget != null) {
            tag.putString(TAG_CUSTOM_DUMMY_CYCLE_TARGET, customPresetDummyCycleTarget.toString());
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains(TAG_STRIPPED_BLOCK, Tag.TAG_STRING)) {
            strippedBlockId = ResourceLocation.tryParse(tag.getString(TAG_STRIPPED_BLOCK));
        } else {
            strippedBlockId = null;
        }
        customVisualSpec =
                tag.contains(TAG_CUSTOM_SPEC, Tag.TAG_COMPOUND)
                        ? RegenCustomVisualSpec.readNbt(tag.getCompound(TAG_CUSTOM_SPEC))
                        : null;
        customPresetDummyCycleIndex =
                tag.contains(TAG_CUSTOM_DUMMY_CYCLE, Tag.TAG_INT) ? tag.getInt(TAG_CUSTOM_DUMMY_CYCLE) : -1;
        customPresetDummyCycleTarget =
                tag.contains(TAG_CUSTOM_DUMMY_CYCLE_TARGET, Tag.TAG_STRING)
                        ? ResourceLocation.tryParse(tag.getString(TAG_CUSTOM_DUMMY_CYCLE_TARGET))
                        : null;
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

    @Override
    public void handleUpdateTag(CompoundTag tag, HolderLookup.Provider registries) {
        ResourceLocation prevStripped = strippedBlockId;
        RegenCustomVisualSpec prevCustom = customVisualSpec;
        super.handleUpdateTag(tag, registries);
        if (!Objects.equals(prevStripped, strippedBlockId) || !Objects.equals(prevCustom, customVisualSpec)) {
            requestModelDataUpdate();
            if (level != null && level.isClientSide() && strippedBlockId != null) {
                RegenStrippedCompositeClientHooks.scheduleWarm(getBlockPos(), strippedBlockId);
            }
        }
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt, HolderLookup.Provider lookupProvider) {
        ResourceLocation prevStripped = strippedBlockId;
        RegenCustomVisualSpec prevCustom = customVisualSpec;
        CompoundTag tag = pkt.getTag();
        if (tag != null && !tag.isEmpty()) {
            loadAdditional(tag, lookupProvider);
        }
        if (!Objects.equals(prevStripped, strippedBlockId) || !Objects.equals(prevCustom, customVisualSpec)) {
            requestModelDataUpdate();
            if (level != null) {
                BlockState state = getBlockState();
                level.sendBlockUpdated(getBlockPos(), state, state, Block.UPDATE_CLIENTS);
            }
            if (level != null && level.isClientSide() && strippedBlockId != null) {
                RegenStrippedCompositeClientHooks.scheduleWarm(getBlockPos(), strippedBlockId);
            }
        }
    }

    @Override
    public ModelData getModelData() {
        if (strippedBlockId == null && customVisualSpec == null) {
            return ModelData.EMPTY;
        }
        ModelData.Builder b = ModelData.builder();
        if (strippedBlockId != null) {
            b.with(RegenBlockEntityModelProperties.STRIPPED_BLOCK, strippedBlockId);
        }
        if (customVisualSpec != null) {
            b.with(RegenBlockEntityModelProperties.CUSTOM_VISUAL_SPEC, customVisualSpec);
        }
        return b.build();
    }
}
