/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.core.BlockPos
 *  net.minecraft.network.FriendlyByteBuf
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraftforge.api.distmarker.Dist
 *  net.minecraftforge.fml.loading.FMLEnvironment
 *  net.minecraftforge.network.NetworkEvent$Context
 */
package com.github.TeThoLaPot.regen_resources.platform.forge.network;

import com.github.TeThoLaPot.regen_resources.platform.forge.network.RegenJadeProbeClientCache;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.network.NetworkEvent;

public record ClientboundJadeRegenProbeInvalidatePacket(ResourceLocation dimensionId, BlockPos pos) {
    public static void encode(ClientboundJadeRegenProbeInvalidatePacket msg, FriendlyByteBuf buf) {
        buf.writeResourceLocation(msg.dimensionId());
        buf.writeBlockPos(msg.pos());
    }

    public static ClientboundJadeRegenProbeInvalidatePacket decode(FriendlyByteBuf buf) {
        return new ClientboundJadeRegenProbeInvalidatePacket(buf.readResourceLocation(), buf.readBlockPos());
    }

    public static void handle(ClientboundJadeRegenProbeInvalidatePacket msg, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> {
            if (FMLEnvironment.dist != Dist.CLIENT) {
                return;
            }
            Minecraft mc = Minecraft.getInstance();
            if (mc.level == null) {
                return;
            }
            if (!mc.level.dimension().location().equals(msg.dimensionId())) {
                return;
            }
            RegenJadeProbeClientCache.invalidate(msg.dimensionId(), msg.pos());
        });
        ctx.setPacketHandled(true);
    }
}

