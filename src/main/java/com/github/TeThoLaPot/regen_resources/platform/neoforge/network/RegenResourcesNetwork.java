package com.github.TeThoLaPot.regen_resources.platform.neoforge.network;

import com.github.TeThoLaPot.regen_resources.RegenResources;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

/** NeoForge {@link RegisterPayloadHandlersEvent} でペイロードを登録する。 */
public final class RegenResourcesNetwork {

    private RegenResourcesNetwork() {}

    public static void register(RegisterPayloadHandlersEvent event) {
        var reg = event.registrar(RegenResources.MOD_ID);
        reg.playToServer(
                ServerboundJadeRegenProbePacket.TYPE,
                ServerboundJadeRegenProbePacket.STREAM_CODEC,
                ServerboundJadeRegenProbePacket::handle);
        reg.playToClient(
                ClientboundJadeRegenProbePacket.TYPE,
                ClientboundJadeRegenProbePacket.STREAM_CODEC,
                ClientboundJadeRegenProbePacket::handle);
        reg.playToClient(
                ClientboundJadeRegenProbeInvalidatePacket.TYPE,
                ClientboundJadeRegenProbeInvalidatePacket.STREAM_CODEC,
                ClientboundJadeRegenProbeInvalidatePacket::handle);
    }
}
