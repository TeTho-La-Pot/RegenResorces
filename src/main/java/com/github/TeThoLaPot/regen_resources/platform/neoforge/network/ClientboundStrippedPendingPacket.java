package com.github.TeThoLaPot.regen_resources.platform.neoforge.network;

import com.github.TeThoLaPot.regen_resources.RegenResources;
import com.github.TeThoLaPot.regen_resources.common.block.RegenBlockEntity;
import com.github.TeThoLaPot.regen_resources.common.block.RegenBlocks;
import com.github.TeThoLaPot.regen_resources.common.clientbridge.RegenStrippedCompositeClientHooks;
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

/** クライアントがストリップ原木見た目を適用する前に待機マップへ載せる（チャンクトラッキング）。 */
public record ClientboundStrippedPendingPacket(BlockPos pos, @Nullable ResourceLocation strippedId) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<ClientboundStrippedPendingPacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(RegenResources.MOD_ID, "stripped_pending_s2c"));

    public static final StreamCodec<FriendlyByteBuf, ClientboundStrippedPendingPacket> STREAM_CODEC =
            StreamCodec.of(
                    (buf, msg) -> {
                        buf.writeBlockPos(msg.pos());
                        buf.writeBoolean(msg.strippedId() != null);
                        if (msg.strippedId() != null) {
                            buf.writeResourceLocation(msg.strippedId());
                        }
                    },
                    buf -> {
                        BlockPos pos = buf.readBlockPos();
                        ResourceLocation id = buf.readBoolean() ? buf.readResourceLocation() : null;
                        return new ClientboundStrippedPendingPacket(pos, id);
                    });

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(ClientboundStrippedPendingPacket msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            RegenBlocks.preparePendingStrippedIdClient(msg.pos(), msg.strippedId());
            if (msg.strippedId() == null) {
                return;
            }
            Minecraft mc = Minecraft.getInstance();
            if (mc.level != null) {
                BlockEntity be = mc.level.getBlockEntity(msg.pos());
                // 同期パケット順で BE が先行／後発どちらでも、ここで見た目 ID を確定させる。
                if (be instanceof RegenBlockEntity shell) {
                    shell.setStrippedBlockId(msg.strippedId());
                }
            }
            RegenStrippedCompositeClientHooks.scheduleWarm(msg.pos(), msg.strippedId());
            RegenShellAppearanceClientRetry.remind(msg.pos(), msg.strippedId(), null);
        });
    }
}
