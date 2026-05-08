package com.github.TeThoLaPot.regen_resources.forge.network;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** ピストン移動など TT が変わった座標の Jade プローブキャッシュをクライアントで捨てる。 */
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
        ctx.enqueueWork(
                () -> {
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
