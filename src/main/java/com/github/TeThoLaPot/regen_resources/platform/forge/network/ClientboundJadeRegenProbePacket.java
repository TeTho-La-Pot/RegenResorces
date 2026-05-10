/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.network.FriendlyByteBuf
 *  net.minecraftforge.api.distmarker.Dist
 *  net.minecraftforge.fml.loading.FMLEnvironment
 *  net.minecraftforge.network.NetworkEvent$Context
 */
package com.github.TeThoLaPot.regen_resources.platform.forge.network;

import com.github.TeThoLaPot.regen_resources.platform.forge.network.RegenJadeProbeClientCache;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.network.NetworkEvent;

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
        ctx.enqueueWork(() -> {
            if (FMLEnvironment.dist != Dist.CLIENT) {
                return;
            }
            Minecraft mc = Minecraft.getInstance();
            RegenJadeProbeClientCache.applyReply(mc, msg.payload);
        });
        ctx.setPacketHandled(true);
    }
}

