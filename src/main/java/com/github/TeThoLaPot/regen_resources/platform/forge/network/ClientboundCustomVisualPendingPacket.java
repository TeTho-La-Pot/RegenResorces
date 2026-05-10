/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.network.FriendlyByteBuf
 *  net.minecraftforge.api.distmarker.Dist
 *  net.minecraftforge.fml.loading.FMLEnvironment
 *  net.minecraftforge.network.NetworkEvent$Context
 *  org.jetbrains.annotations.Nullable
 */
package com.github.TeThoLaPot.regen_resources.platform.forge.network;

import com.github.TeThoLaPot.regen_resources.common.block.RegenBlocks;
import com.github.TeThoLaPot.regen_resources.common.block.RegenCustomVisualSpec;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.Nullable;

public record ClientboundCustomVisualPendingPacket(BlockPos pos, @Nullable RegenCustomVisualSpec spec) {
    public static void encode(ClientboundCustomVisualPendingPacket msg, FriendlyByteBuf buf) {
        buf.writeBlockPos(msg.pos());
        boolean has = msg.spec() != null;
        buf.writeBoolean(has);
        if (has) {
            msg.spec().writeBuf(buf);
        }
    }

    public static ClientboundCustomVisualPendingPacket decode(FriendlyByteBuf buf) {
        BlockPos pos = buf.readBlockPos();
        boolean has = buf.readBoolean();
        RegenCustomVisualSpec spec = has ? RegenCustomVisualSpec.readBuf(buf) : null;
        return new ClientboundCustomVisualPendingPacket(pos, spec);
    }

    public static void handle(ClientboundCustomVisualPendingPacket msg, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> {
            if (FMLEnvironment.dist != Dist.CLIENT) {
                return;
            }
            RegenBlocks.preparePendingCustomSpecClient(msg.pos(), msg.spec());
        });
        ctx.setPacketHandled(true);
    }
}

