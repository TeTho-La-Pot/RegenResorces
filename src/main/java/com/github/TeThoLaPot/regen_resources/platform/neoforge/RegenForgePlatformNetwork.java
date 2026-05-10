package com.github.TeThoLaPot.regen_resources.platform.neoforge;

import com.github.TeThoLaPot.regen_resources.platform.RegenPlatformNetwork;
import com.github.TeThoLaPot.regen_resources.platform.neoforge.network.ClientboundJadeRegenProbeInvalidatePacket;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.neoforge.network.PacketDistributor;

public final class RegenForgePlatformNetwork implements RegenPlatformNetwork {
    @Override
    public void invalidateJadeProbe(ServerLevel level, BlockPos pos) {
        BlockPos ip = pos.immutable();
        var pkt = new ClientboundJadeRegenProbeInvalidatePacket(level.dimension().location(), ip);
        PacketDistributor.sendToPlayersTrackingChunk(level, new ChunkPos(ip), pkt);
    }
}
