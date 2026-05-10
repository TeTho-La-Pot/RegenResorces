package com.github.TeThoLaPot.regen_resources.platform.neoforge.network;

import com.github.TeThoLaPot.regen_resources.RegenResources;
import com.github.TeThoLaPot.regen_resources.platform.neoforge.compat.jade.RegenResourcesJadeServerData;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** クライアントが Jade 表示用にサーバーへ TT・プリセット整合の可否を問い合わせる。 */
public record ServerboundJadeRegenProbePacket(ResourceLocation dimensionId, BlockPos pos) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<ServerboundJadeRegenProbePacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(RegenResources.MOD_ID, "jade_probe_c2s"));

    public static final StreamCodec<FriendlyByteBuf, ServerboundJadeRegenProbePacket> STREAM_CODEC =
            StreamCodec.composite(
                    ResourceLocation.STREAM_CODEC,
                    ServerboundJadeRegenProbePacket::dimensionId,
                    BlockPos.STREAM_CODEC,
                    ServerboundJadeRegenProbePacket::pos,
                    ServerboundJadeRegenProbePacket::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(ServerboundJadeRegenProbePacket msg, IPayloadContext ctx) {
        ctx.enqueueWork(
                () -> {
                    if (!(ctx.player() instanceof ServerPlayer player)) {
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
                    if (player.blockPosition().distSqr(pos) > 96.0 * 96.0) {
                        return;
                    }
                    BlockState state = sl.getBlockState(pos);
                    var replyTag = RegenResourcesJadeServerData.buildJadeRuleProbeTag(sl, pos, state);
                    PacketDistributor.sendToPlayer(player, new ClientboundJadeRegenProbePacket(replyTag));
                });
    }
}
