package com.github.TeThoLaPot.regen_resources.platform.forge;

import com.github.TeThoLaPot.regen_resources.platform.RegenPlatformNetwork;
import com.github.TeThoLaPot.regen_resources.platform.forge.network.ClientboundJadeRegenProbeInvalidatePacket;
import com.github.TeThoLaPot.regen_resources.platform.forge.network.RegenResourcesNetwork;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.network.PacketDistributor;

public final class RegenForgePlatformNetwork implements RegenPlatformNetwork {
    @Override
    public void invalidateJadeProbe(ServerLevel level, BlockPos pos) {
        BlockPos ip = pos.immutable();
        var pkt = new ClientboundJadeRegenProbeInvalidatePacket(level.dimension().location(), ip);
        RegenResourcesNetwork.CHANNEL.send(PacketDistributor.TRACKING_CHUNK.with(() -> level.getChunkAt(ip)), pkt);
    }
}

