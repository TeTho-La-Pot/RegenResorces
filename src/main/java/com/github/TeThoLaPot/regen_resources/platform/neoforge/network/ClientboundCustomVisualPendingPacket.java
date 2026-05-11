package com.github.TeThoLaPot.regen_resources.platform.neoforge.network;

import com.github.TeThoLaPot.regen_resources.RegenResources;
import com.github.TeThoLaPot.regen_resources.common.block.RegenBlockEntity;
import com.github.TeThoLaPot.regen_resources.common.block.RegenBlocks;
import com.github.TeThoLaPot.regen_resources.common.block.RegenCustomVisualSpec;
import com.github.TeThoLaPot.regen_resources.platform.neoforge.client.sync.RegenShellAppearanceClientRetry;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.Nullable;

/** クライアントがカスタム見た目を適用する前に待機マップへ載せる（チャンクトラッキング）。 */
public record ClientboundCustomVisualPendingPacket(BlockPos pos, @Nullable RegenCustomVisualSpec spec) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<ClientboundCustomVisualPendingPacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(RegenResources.MOD_ID, "custom_visual_pending_s2c"));

    public static final StreamCodec<FriendlyByteBuf, ClientboundCustomVisualPendingPacket> STREAM_CODEC =
            StreamCodec.of(
                    (buf, msg) -> {
                        buf.writeBlockPos(msg.pos());
                        buf.writeBoolean(msg.spec() != null);
                        if (msg.spec() != null) {
                            msg.spec().writeBuf(buf);
                        }
                    },
                    buf -> {
                        BlockPos pos = buf.readBlockPos();
                        RegenCustomVisualSpec spec = buf.readBoolean() ? RegenCustomVisualSpec.readBuf(buf) : null;
                        return new ClientboundCustomVisualPendingPacket(pos, spec);
                    });

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(ClientboundCustomVisualPendingPacket msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            RegenBlocks.preparePendingCustomSpecClient(msg.pos(), msg.spec());
            if (msg.spec() == null) {
                return;
            }
            Minecraft mc = Minecraft.getInstance();
            if (mc.level == null) {
                return;
            }
            BlockEntity be = mc.level.getBlockEntity(msg.pos());
            if (be instanceof RegenBlockEntity shell) {
                shell.setCustomVisualSpec(msg.spec());
            }
            RegenShellAppearanceClientRetry.remind(msg.pos(), null, msg.spec());
        });
    }
}
