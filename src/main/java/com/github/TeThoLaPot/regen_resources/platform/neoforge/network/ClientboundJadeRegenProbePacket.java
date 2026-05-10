package com.github.TeThoLaPot.regen_resources.platform.neoforge.network;

import com.github.TeThoLaPot.regen_resources.RegenResources;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ClientboundJadeRegenProbePacket(CompoundTag payload) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<ClientboundJadeRegenProbePacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(RegenResources.MOD_ID, "jade_probe_s2c"));

    public static final StreamCodec<FriendlyByteBuf, ClientboundJadeRegenProbePacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.COMPOUND_TAG,
                    ClientboundJadeRegenProbePacket::payload,
                    ClientboundJadeRegenProbePacket::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(ClientboundJadeRegenProbePacket msg, IPayloadContext ctx) {
        ctx.enqueueWork(
                () -> {
                    Minecraft mc = Minecraft.getInstance();
                    RegenJadeProbeClientCache.applyReply(mc, msg.payload);
                });
    }
}
