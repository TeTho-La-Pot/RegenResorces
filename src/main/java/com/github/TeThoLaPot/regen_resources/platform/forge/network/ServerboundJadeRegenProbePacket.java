package com.github.TeThoLaPot.regen_resources.platform.forge.network;

import com.github.TeThoLaPot.regen_resources.platform.forge.compat.jade.RegenResourcesJadeServerData;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;
import snownee.jade.Jade;

import java.util.function.Supplier;

/** クライアントが Jade 表示用にサーバーへ TT・プリセット整合の可否を問い合わせる。 */
public record ServerboundJadeRegenProbePacket(ResourceLocation dimensionId, BlockPos pos) {

    public static void encode(ServerboundJadeRegenProbePacket msg, FriendlyByteBuf buf) {
        buf.writeResourceLocation(msg.dimensionId);
        buf.writeBlockPos(msg.pos);
    }

    public static ServerboundJadeRegenProbePacket decode(FriendlyByteBuf buf) {
        return new ServerboundJadeRegenProbePacket(buf.readResourceLocation(), buf.readBlockPos());
    }

    public static void handle(ServerboundJadeRegenProbePacket msg, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(
                () -> {
                    ServerPlayer player = ctx.getSender();
                    if (player == null) {
                        return;
                    }
                    if (!(player.level() instanceof ServerLevel sl)) {
                        return;
                    }
                    if (!player.level().dimension().location().equals(msg.dimensionId)) {
                        return;
                    }
                    BlockPos pos = msg.pos;
                    if (!sl.hasChunkAt(pos)) {
                        return;
                    }
                    if (player.blockPosition().distSqr(pos) > Jade.MAX_DISTANCE_SQR) {
                        return;
                    }
                    BlockState state = sl.getBlockState(pos);
                    ClientboundJadeRegenProbePacket reply =
                            new ClientboundJadeRegenProbePacket(RegenResourcesJadeServerData.buildJadeRuleProbeTag(sl, pos, state));
                    RegenResourcesNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), reply);
                });
        ctx.setPacketHandled(true);
    }
}
