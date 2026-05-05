package com.github.TeThoLaPot.regen_resources.network;

import com.github.TeThoLaPot.regen_resources.init.block.Re_Blocks;
import com.github.TeThoLaPot.regen_resources.init.entity.RegenBlockEntity;
import com.github.TeThoLaPot.tt_core.api.TTDataUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.concurrent.ConcurrentHashMap;

/** チャンク内 BE 公式パケットより遅れるクライアント側のずれ埋め（硬度・適正ツール・Jade 周辺）。 */
public final class RegenMimicClientCache {

    private static final ConcurrentHashMap<String, BlockState> ENTRIES = new ConcurrentHashMap<>();

    private RegenMimicClientCache() {}

    private static String storageKey(ResourceKey<Level> dim, BlockPos pos) {
        return dim.location().toString() + '|' + pos.asLong();
    }

    public static void put(Level clientLevel, BlockPos pos, CompoundTag hintPayload) {
        if (!clientLevel.isClientSide()) {
            return;
        }
        if (!clientLevel.getBlockState(pos).is(Re_Blocks.REGEN_BLOCK.get())) {
            return;
        }
        BlockState mimic = decodeMimicFromHintPayload(hintPayload);
        if (mimic == null || mimic.isAir()) {
            return;
        }
        ENTRIES.put(storageKey(clientLevel.dimension(), pos), mimic);
    }

    public static @Nullable BlockState peek(Level level, BlockPos pos) {
        if (!level.isClientSide()) {
            return null;
        }
        if (!level.getBlockState(pos).is(Re_Blocks.REGEN_BLOCK.get())) {
            clear(level.dimension(), pos);
            return null;
        }
        return ENTRIES.get(storageKey(level.dimension(), pos));
    }

    public static void clear(ResourceKey<Level> dimension, BlockPos pos) {
        ENTRIES.remove(storageKey(dimension, pos));
    }

    static @Nullable BlockState decodeMimicFromHintPayload(CompoundTag data) {
        if (data == null || data.isEmpty()) {
            return null;
        }
        if (data.contains("state")) {
            BlockState fromStateKey = TTDataUtils.getBlockState(data, "state");
            if (fromStateKey != null && !fromStateKey.isAir()) {
                return fromStateKey;
            }
        }
        if (data.contains(RegenBlockEntity.TAG_RESTORE_RL)) {
            ResourceLocation id = ResourceLocation.tryParse(data.getString(RegenBlockEntity.TAG_RESTORE_RL).trim());
            if (id == null) {
                return null;
            }
            Block block = BuiltInRegistries.BLOCK.get(id);
            BlockState fallback = block.defaultBlockState();
            if (!fallback.isAir()) {
                return fallback;
            }
        }
        return null;
    }
}
