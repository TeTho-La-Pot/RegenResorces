/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.network.FriendlyByteBuf
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraftforge.api.distmarker.Dist
 *  net.minecraftforge.fml.loading.FMLEnvironment
 *  net.minecraftforge.network.NetworkEvent$Context
 *  org.jetbrains.annotations.Nullable
 */
package com.github.TeThoLaPot.regen_resources.platform.forge.network;

import com.github.TeThoLaPot.regen_resources.common.block.RegenBlocks;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.Nullable;

public record ClientboundStrippedPendingPacket(BlockPos pos, @Nullable ResourceLocation strippedId) {
    public static void encode(ClientboundStrippedPendingPacket msg, FriendlyByteBuf buf) {
        buf.writeBlockPos(msg.pos());
        boolean has = msg.strippedId() != null;
        buf.writeBoolean(has);
        if (has) {
            buf.writeResourceLocation(msg.strippedId());
        }
    }

    public static ClientboundStrippedPendingPacket decode(FriendlyByteBuf buf) {
        BlockPos pos = buf.readBlockPos();
        boolean has = buf.readBoolean();
        ResourceLocation id = has ? buf.readResourceLocation() : null;
        return new ClientboundStrippedPendingPacket(pos, id);
    }

    public static void handle(ClientboundStrippedPendingPacket msg, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> {
            if (FMLEnvironment.dist != Dist.CLIENT) {
                return;
            }
            RegenBlocks.preparePendingStrippedIdClient(msg.pos(), msg.strippedId());
        });
        ctx.setPacketHandled(true);
    }
}

