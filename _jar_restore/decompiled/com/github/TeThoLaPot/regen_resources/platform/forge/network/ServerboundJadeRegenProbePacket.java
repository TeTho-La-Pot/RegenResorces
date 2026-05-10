/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Vec3i
 *  net.minecraft.network.FriendlyByteBuf
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraftforge.network.NetworkEvent$Context
 *  net.minecraftforge.network.PacketDistributor
 *  snownee.jade.Jade
 */
package com.github.TeThoLaPot.regen_resources.platform.forge.network;

import com.github.TeThoLaPot.regen_resources.platform.forge.compat.jade.RegenResourcesJadeServerData;
import com.github.TeThoLaPot.regen_resources.platform.forge.network.ClientboundJadeRegenProbePacket;
import com.github.TeThoLaPot.regen_resources.platform.forge.network.RegenResourcesNetwork;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;
import snownee.jade.Jade;

public record ServerboundJadeRegenProbePacket(ResourceLocation dimensionId, BlockPos pos) {
    public static void encode(ServerboundJadeRegenProbePacket msg, FriendlyByteBuf buf) {
        buf.m_130085_(msg.dimensionId);
        buf.m_130064_(msg.pos);
    }

    public static ServerboundJadeRegenProbePacket decode(FriendlyByteBuf buf) {
        return new ServerboundJadeRegenProbePacket(buf.m_130281_(), buf.m_130135_());
    }

    public static void handle(ServerboundJadeRegenProbePacket msg, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player == null) {
                return;
            }
            Level patt1550$temp = player.m_9236_();
            if (!(patt1550$temp instanceof ServerLevel)) {
                return;
            }
            ServerLevel sl = (ServerLevel)patt1550$temp;
            if (!player.m_9236_().m_46472_().m_135782_().equals((Object)msg.dimensionId)) {
                return;
            }
            BlockPos pos = msg.pos;
            if (!sl.m_46805_(pos)) {
                return;
            }
            if (player.m_20183_().m_123331_((Vec3i)pos) > (double)Jade.MAX_DISTANCE_SQR) {
                return;
            }
            BlockState state = sl.m_8055_(pos);
            ClientboundJadeRegenProbePacket reply = new ClientboundJadeRegenProbePacket(RegenResourcesJadeServerData.buildJadeRuleProbeTag(sl, pos, state));
            RegenResourcesNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), (Object)reply);
        });
        ctx.setPacketHandled(true);
    }
}

