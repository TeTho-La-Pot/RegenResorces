/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraftforge.network.PacketDistributor
 */
package com.github.TeThoLaPot.regen_resources.platform.forge;

import com.github.TeThoLaPot.regen_resources.platform.RegenPlatformNetwork;
import com.github.TeThoLaPot.regen_resources.platform.forge.network.ClientboundJadeRegenProbeInvalidatePacket;
import com.github.TeThoLaPot.regen_resources.platform.forge.network.RegenResourcesNetwork;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.network.PacketDistributor;

public final class RegenForgePlatformNetwork
implements RegenPlatformNetwork {
    @Override
    public void invalidateJadeProbe(ServerLevel level, BlockPos pos) {
        BlockPos ip = pos.m_7949_();
        ClientboundJadeRegenProbeInvalidatePacket pkt = new ClientboundJadeRegenProbeInvalidatePacket(level.m_46472_().m_135782_(), ip);
        RegenResourcesNetwork.CHANNEL.send(PacketDistributor.TRACKING_CHUNK.with(() -> level.m_46745_(ip)), (Object)pkt);
    }
}

