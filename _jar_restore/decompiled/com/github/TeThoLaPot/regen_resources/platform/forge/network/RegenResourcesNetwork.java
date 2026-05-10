/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraftforge.network.NetworkRegistry
 *  net.minecraftforge.network.simple.SimpleChannel
 */
package com.github.TeThoLaPot.regen_resources.platform.forge.network;

import com.github.TeThoLaPot.regen_resources.platform.forge.network.ClientboundCustomVisualPendingPacket;
import com.github.TeThoLaPot.regen_resources.platform.forge.network.ClientboundJadeRegenProbeInvalidatePacket;
import com.github.TeThoLaPot.regen_resources.platform.forge.network.ClientboundJadeRegenProbePacket;
import com.github.TeThoLaPot.regen_resources.platform.forge.network.ClientboundStrippedPendingPacket;
import com.github.TeThoLaPot.regen_resources.platform.forge.network.ServerboundJadeRegenProbePacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public final class RegenResourcesNetwork {
    private static final String PROTOCOL = "1";
    private static int packetId;
    public static final SimpleChannel CHANNEL;

    private RegenResourcesNetwork() {
    }

    public static void register() {
        packetId = 0;
        CHANNEL.registerMessage(packetId++, ServerboundJadeRegenProbePacket.class, ServerboundJadeRegenProbePacket::encode, ServerboundJadeRegenProbePacket::decode, ServerboundJadeRegenProbePacket::handle);
        CHANNEL.registerMessage(packetId++, ClientboundJadeRegenProbePacket.class, ClientboundJadeRegenProbePacket::encode, ClientboundJadeRegenProbePacket::decode, ClientboundJadeRegenProbePacket::handle);
        CHANNEL.registerMessage(packetId++, ClientboundJadeRegenProbeInvalidatePacket.class, ClientboundJadeRegenProbeInvalidatePacket::encode, ClientboundJadeRegenProbeInvalidatePacket::decode, ClientboundJadeRegenProbeInvalidatePacket::handle);
        CHANNEL.registerMessage(packetId++, ClientboundStrippedPendingPacket.class, ClientboundStrippedPendingPacket::encode, ClientboundStrippedPendingPacket::decode, ClientboundStrippedPendingPacket::handle);
        CHANNEL.registerMessage(packetId++, ClientboundCustomVisualPendingPacket.class, ClientboundCustomVisualPendingPacket::encode, ClientboundCustomVisualPendingPacket::decode, ClientboundCustomVisualPendingPacket::handle);
    }

    static {
        CHANNEL = NetworkRegistry.newSimpleChannel((ResourceLocation)ResourceLocation.fromNamespaceAndPath((String)"regen_resources", (String)"network"), () -> PROTOCOL, PROTOCOL::equals, PROTOCOL::equals);
    }
}

