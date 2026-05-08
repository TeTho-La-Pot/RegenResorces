package com.github.TeThoLaPot.regen_resources.forge.network;

import com.github.TeThoLaPot.regen_resources.RegenResources;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

/**
 * Jade 以外でも使える最小チャネル。無タイルブロックの再生可否はサーバー権威で問い合わせる。
 */
public final class RegenResourcesNetwork {

    private static final String PROTOCOL = "1";
    private static int packetId;

    public static final SimpleChannel CHANNEL =
            NetworkRegistry.newSimpleChannel(
                    ResourceLocation.fromNamespaceAndPath(RegenResources.MOD_ID, "network"),
                    () -> PROTOCOL,
                    PROTOCOL::equals,
                    PROTOCOL::equals);

    private RegenResourcesNetwork() {}

    public static void register() {
        packetId = 0;
        CHANNEL.registerMessage(
                packetId++,
                ServerboundJadeRegenProbePacket.class,
                ServerboundJadeRegenProbePacket::encode,
                ServerboundJadeRegenProbePacket::decode,
                ServerboundJadeRegenProbePacket::handle);
        CHANNEL.registerMessage(
                packetId++,
                ClientboundJadeRegenProbePacket.class,
                ClientboundJadeRegenProbePacket::encode,
                ClientboundJadeRegenProbePacket::decode,
                ClientboundJadeRegenProbePacket::handle);
        CHANNEL.registerMessage(
                packetId++,
                ClientboundJadeRegenProbeInvalidatePacket.class,
                ClientboundJadeRegenProbeInvalidatePacket::encode,
                ClientboundJadeRegenProbeInvalidatePacket::decode,
                ClientboundJadeRegenProbeInvalidatePacket::handle);
    }
}
