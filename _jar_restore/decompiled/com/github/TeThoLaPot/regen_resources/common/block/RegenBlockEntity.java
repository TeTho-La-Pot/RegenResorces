/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.github.TeThoLaPot.tt_core.TT_core
 *  net.minecraft.core.BlockPos
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.Tag
 *  net.minecraft.network.Connection
 *  net.minecraft.network.protocol.Packet
 *  net.minecraft.network.protocol.game.ClientGamePacketListener
 *  net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  net.minecraft.world.level.block.entity.BlockEntityType
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraftforge.client.model.data.ModelData
 *  net.minecraftforge.client.model.data.ModelData$Builder
 *  org.jetbrains.annotations.Nullable
 */
package com.github.TeThoLaPot.regen_resources.common.block;

import com.github.TeThoLaPot.regen_resources.common.block.RegenBlockEntityModelProperties;
import com.github.TeThoLaPot.regen_resources.common.block.RegenBlocks;
import com.github.TeThoLaPot.regen_resources.common.block.RegenCorruptionFallback;
import com.github.TeThoLaPot.regen_resources.common.block.RegenCustomVisualSpec;
import com.github.TeThoLaPot.tt_core.TT_core;
import java.util.Objects;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;
import org.jetbrains.annotations.Nullable;

public final class RegenBlockEntity
extends BlockEntity {
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
        if (!(level instanceof ServerLevel)) {
            return;
        }
        ServerLevel sl = (ServerLevel)level;
        if (!(state.m_60734_() instanceof RegenBlocks)) {
            return;
        }
        ++be.watchdogWarmupTicks;
        if (be.watchdogWarmupTicks < 40) {
            return;
        }
        CompoundTag data = TT_core.getBlockData((ServerLevel)sl, (BlockPos)pos);
        if (data.m_128456_()) {
            RegenCorruptionFallback.apply(sl, pos, state);
            return;
        }
        if (!data.m_128425_("execute_at", 4)) {
            RegenCorruptionFallback.apply(sl, pos, state);
            return;
        }
        long executeAt = data.m_128454_("execute_at");
        long now = sl.m_46467_();
        if (now <= executeAt + 40L) {
            return;
        }
        RegenCorruptionFallback.apply(sl, pos, state);
    }

    @Nullable
    public ResourceLocation getStrippedBlockId() {
        return this.strippedBlockId;
    }

    @Nullable
    public RegenCustomVisualSpec getCustomVisualSpec() {
        return this.customVisualSpec;
    }

    public int customPresetDummyCycleIndex() {
        return this.customPresetDummyCycleIndex;
    }

    public void setCustomPresetDummyCycleIndex(int customPresetDummyCycleIndex) {
        this.customPresetDummyCycleIndex = customPresetDummyCycleIndex;
        this.m_6596_();
    }

    @Nullable
    public ResourceLocation customPresetDummyCycleTarget() {
        return this.customPresetDummyCycleTarget;
    }

    public void setCustomPresetDummyCycleTarget(@Nullable ResourceLocation customPresetDummyCycleTarget) {
        this.customPresetDummyCycleTarget = customPresetDummyCycleTarget;
        this.m_6596_();
    }

    public void resetCustomPresetDummyCycle() {
        this.customPresetDummyCycleIndex = -1;
        this.customPresetDummyCycleTarget = null;
        this.m_6596_();
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
        this.m_6596_();
        if (this.f_58857_ != null && !this.f_58857_.m_5776_()) {
            BlockState state = this.m_58900_();
            this.f_58857_.m_7260_(this.m_58899_(), state, state, 2);
        }
    }

    public void setCustomVisualSpec(@Nullable RegenCustomVisualSpec spec) {
        if (Objects.equals(this.customVisualSpec, spec)) {
            return;
        }
        this.customVisualSpec = spec;
        this.m_6596_();
        if (this.f_58857_ != null && !this.f_58857_.m_5776_()) {
            BlockState state = this.m_58900_();
            this.f_58857_.m_7260_(this.m_58899_(), state, state, 2);
        }
    }

    protected void m_183515_(CompoundTag tag) {
        super.m_183515_(tag);
        if (this.strippedBlockId != null) {
            tag.m_128359_(TAG_STRIPPED_BLOCK, this.strippedBlockId.toString());
        }
        if (this.customVisualSpec != null) {
            tag.m_128365_(TAG_CUSTOM_SPEC, (Tag)this.customVisualSpec.writeNbt());
        }
        tag.m_128405_(TAG_CUSTOM_DUMMY_CYCLE, this.customPresetDummyCycleIndex);
        if (this.customPresetDummyCycleTarget != null) {
            tag.m_128359_(TAG_CUSTOM_DUMMY_CYCLE_TARGET, this.customPresetDummyCycleTarget.toString());
        }
    }

    public void m_142466_(CompoundTag tag) {
        super.m_142466_(tag);
        if (tag.m_128425_(TAG_STRIPPED_BLOCK, 8)) {
            String s = tag.m_128461_(TAG_STRIPPED_BLOCK);
            this.strippedBlockId = ResourceLocation.m_135820_((String)s);
        } else {
            this.strippedBlockId = null;
        }
        this.customVisualSpec = tag.m_128425_(TAG_CUSTOM_SPEC, 10) ? RegenCustomVisualSpec.readNbt(tag.m_128469_(TAG_CUSTOM_SPEC)) : null;
        this.customPresetDummyCycleIndex = tag.m_128425_(TAG_CUSTOM_DUMMY_CYCLE, 3) ? tag.m_128451_(TAG_CUSTOM_DUMMY_CYCLE) : -1;
        this.customPresetDummyCycleTarget = tag.m_128425_(TAG_CUSTOM_DUMMY_CYCLE_TARGET, 8) ? ResourceLocation.m_135820_((String)tag.m_128461_(TAG_CUSTOM_DUMMY_CYCLE_TARGET)) : null;
    }

    public CompoundTag m_5995_() {
        CompoundTag tag = super.m_5995_();
        this.m_183515_(tag);
        return tag;
    }

    @Nullable
    public Packet<ClientGamePacketListener> m_58483_() {
        return ClientboundBlockEntityDataPacket.m_195640_((BlockEntity)this);
    }

    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        ResourceLocation prevStripped = this.strippedBlockId;
        RegenCustomVisualSpec prevCustom = this.customVisualSpec;
        CompoundTag tag = pkt.m_131708_();
        if (tag != null) {
            this.m_142466_(tag);
        }
        if (!Objects.equals(prevStripped, this.strippedBlockId) || !Objects.equals(prevCustom, this.customVisualSpec)) {
            this.requestModelDataUpdate();
            if (this.f_58857_ != null) {
                BlockState state = this.m_58900_();
                this.f_58857_.m_7260_(this.m_58899_(), state, state, 3);
            }
        }
    }

    public void handleUpdateTag(CompoundTag tag) {
        ResourceLocation prevStripped = this.strippedBlockId;
        RegenCustomVisualSpec prevCustom = this.customVisualSpec;
        this.m_142466_(tag);
        if (!Objects.equals(prevStripped, this.strippedBlockId) || !Objects.equals(prevCustom, this.customVisualSpec)) {
            this.requestModelDataUpdate();
        }
    }

    public ModelData getModelData() {
        if (this.strippedBlockId == null && this.customVisualSpec == null) {
            return ModelData.EMPTY;
        }
        ModelData.Builder b = ModelData.builder();
        if (this.strippedBlockId != null) {
            b.with(RegenBlockEntityModelProperties.STRIPPED_BLOCK, (Object)this.strippedBlockId);
        }
        if (this.customVisualSpec != null) {
            b.with(RegenBlockEntityModelProperties.CUSTOM_VISUAL_SPEC, (Object)this.customVisualSpec);
        }
        return b.build();
    }
}

