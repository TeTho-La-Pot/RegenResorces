package com.github.TeThoLaPot.regen_resources.core;

import com.github.TeThoLaPot.regen_resources.Config;
import com.github.TeThoLaPot.regen_resources.init.block.Re_Blocks;
import com.github.TeThoLaPot.regen_resources.util.RegenDiag;
import com.github.TeThoLaPot.regen_resources.util.RegenProcessWarmup;
import com.github.TeThoLaPot.regen_resources.util.RegenRestoreGuard;
import com.github.TeThoLaPot.tt_core.TT_core;
import com.github.TeThoLaPot.tt_core.data.TTDataBank;
import com.github.TeThoLaPot.tt_core.api.TTDataUtils;
import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;
import org.slf4j.Logger;

/**
 * {@link TTDataBank}（{@code tt_core.data}）へのキュー処理登録のみ。NeoForge でもイベント購読は不要でそのまま呼べます。
 */
public final class RegenPersistentTaskRegistration {

    private static final Logger LOGGER = LogUtils.getLogger();

    private RegenPersistentTaskRegistration() {}

    public static void registerExecutors() {
        TTDataBank.registerExecutor("regen_process", (level, data) -> {
            if (RegenProcessWarmup.shouldDeferRegenProcess(
                    level.getServer(), Config.REGEN_PROCESS_SERVER_WARMUP_TICKS.get())) {
                RegenDiag.logWarmupDeferOncePerTick(level.getServer());
                TTDataBank.schedulePersistentTask(level, "regen_process", 1, data.copy());
                return;
            }

            BlockPos pos = TTDataUtils.getBlockPos(data, "pos");
            /*
             * 新形式: task.data は座標のみ。復元ペイロードは {@code pos_*} SavedData（唯一の正）から読む。
             * 旧形式: 互換のため task.data 内に state 等が埋まっているセーブも解釈する。
             */
            CompoundTag payload = data.contains("state") ? data : TT_core.getBlockData(level, pos);
            if (!payload.contains("state")) {
                LOGGER.warn("regen_process: missing restore state at {}, dropping task side-effects", pos);
                return;
            }
            BlockState restoreState = TTDataUtils.getBlockState(payload, "state");

            ServerLevel serverLevel = level;
            ResourceLocation restoreId = ForgeRegistries.BLOCKS.getKey(restoreState.getBlock());
            RegenDiag.log("regen_process run pos={} restoreBlock={}", pos, restoreId);
            serverLevel.getServer().execute(() -> RegenRestoreGuard.runAt(pos, () -> {
                BlockState current = serverLevel.getBlockState(pos);
                if (!current.is(Re_Blocks.REGEN_BLOCK.get())) {
                    RegenDiag.log(
                            "regen_process skip: not regen_block at {} (current={}), clearing TT",
                            pos,
                            ForgeRegistries.BLOCKS.getKey(current.getBlock()));
                    TT_core.removeBlockData(serverLevel, pos);
                    return;
                }
                int u = Block.UPDATE_NEIGHBORS | Block.UPDATE_CLIENTS | Block.UPDATE_IMMEDIATE;
                serverLevel.setBlock(pos, restoreState, u, 512);
                TT_core.removeBlockData(serverLevel, pos);
                RegenDiag.log("regen_process restored ore at {}", pos);
            }));
        });
    }
}
