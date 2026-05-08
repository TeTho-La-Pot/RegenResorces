package com.github.TeThoLaPot.regen_resources.platform.forge.network;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record ClientboundJadeRegenProbePacket(CompoundTag payload) {

    public static void encode(ClientboundJadeRegenProbePacket msg, FriendlyByteBuf buf) {
        buf.writeNbt(msg.payload);
    }

    public static ClientboundJadeRegenProbePacket decode(FriendlyByteBuf buf) {
        CompoundTag tag = buf.readNbt();
        return new ClientboundJadeRegenProbePacket(tag != null ? tag : new CompoundTag());
    }

    public static void handle(ClientboundJadeRegenProbePacket msg, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(
                () -> {
                    if (FMLEnvironment.dist != Dist.CLIENT) {
                        return;
                    }
                    Minecraft mc = Minecraft.getInstance();
                    RegenJadeProbeClientCache.applyReply(mc, msg.payload);
                });
        ctx.setPacketHandled(true);
    }
}
