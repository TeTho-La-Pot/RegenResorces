package com.github.TeThoLaPot.regen_resources.platform.neoforge.network;

import com.github.TeThoLaPot.regen_resources.RegenResources;
import com.github.TeThoLaPot.regen_resources.platform.neoforge.client.screen.RegenSettingsScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** 再生設定 Screen をスナップショット付きで開く。 */
public record ClientboundOpenRegenSettingsPacket(RegenSettingsSnapshot snapshot) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<ClientboundOpenRegenSettingsPacket> TYPE =
            new CustomPacketPayload.Type<>(
                    ResourceLocation.fromNamespaceAndPath(RegenResources.MOD_ID, "open_regen_settings"));

    public static final StreamCodec<FriendlyByteBuf, ClientboundOpenRegenSettingsPacket> STREAM_CODEC =
            StreamCodec.composite(
                    RegenSettingsSnapshot.STREAM_CODEC,
                    ClientboundOpenRegenSettingsPacket::snapshot,
                    ClientboundOpenRegenSettingsPacket::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(ClientboundOpenRegenSettingsPacket msg, IPayloadContext ctx) {
        ctx.enqueueWork(
                () -> {
                    Minecraft mc = Minecraft.getInstance();
                    mc.setScreen(new RegenSettingsScreen(msg.snapshot()));
                });
    }
}
