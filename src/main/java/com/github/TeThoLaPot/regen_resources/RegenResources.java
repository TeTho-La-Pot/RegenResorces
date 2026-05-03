package com.github.TeThoLaPot.regen_resources;

import com.github.TeThoLaPot.regen_resources.init.block.RegenBlocks;
import com.github.TeThoLaPot.tt_core.TT_core;
import com.github.TeThoLaPot.tt_core.api.TTDataBank;
import com.github.TeThoLaPot.tt_core.api.TTDataUtils;
import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import org.slf4j.Logger;

@Mod(RegenResources.MOD_ID)
public class RegenResources {
    // MOD IDを他から参照できるように public に
    public static final String MOD_ID = "regen_resources";
    private static final Logger LOGGER = LogUtils.getLogger();

    public RegenResources() {
        TTDataBank.registerExecutor("regen_process", (level, data) -> {
            BlockPos pos = TTDataUtils.getBlockPos(data, "pos");
            BlockState state = TTDataUtils.getBlockState(data, "state");

            // 現在の場所が「RegenBlock（再生待ちブロック）」であることを確認してから再生
            if (level.getBlockState(pos).getBlock() instanceof RegenBlocks) {
                level.setBlock(pos, state, 3);
                // 再生が完了したので、API側の座標データもクリーンアップ
                TT_core.removeBlockData(level, pos);
            }
        });


        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.SPEC);
        MinecraftForge.EVENT_BUS.register(this);
    }
}

