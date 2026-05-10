package com.github.TeThoLaPot.regen_resources.platform.neoforge.network;

import com.github.TeThoLaPot.regen_resources.RegenResources;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** ピストン移動など TT が変わった座標の Jade プローブキャッシュをクライアントで捨てる。 */
public record ClientboundJadeRegenProbeInvalidatePacket(ResourceLocation dimensionId, BlockPos pos) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<ClientboundJadeRegenProbeInvalidatePacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(RegenResources.MOD_ID, "jade_invalidate_s2c"));

    public static final StreamCodec<FriendlyByteBuf, ClientboundJadeRegenProbeInvalidatePacket> STREAM_CODEC =
            StreamCodec.composite(
                    ResourceLocation.STREAM_CODEC,
                    ClientboundJadeRegenProbeInvalidatePacket::dimensionId,
                    BlockPos.STREAM_CODEC,
                    ClientboundJadeRegenProbeInvalidatePacket::pos,
                    ClientboundJadeRegenProbeInvalidatePacket::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(ClientboundJadeRegenProbeInvalidatePacket msg, IPayloadContext ctx) {
        ctx.enqueueWork(
                () -> {
                    Minecraft mc = Minecraft.getInstance();
                    if (mc.level == null) {
                        return;
                    }
                    if (!mc.level.dimension().location().equals(msg.dimensionId())) {
                        return;
                    }
                    RegenJadeProbeClientCache.invalidate(msg.dimensionId(), msg.pos());
                });
    }
}
