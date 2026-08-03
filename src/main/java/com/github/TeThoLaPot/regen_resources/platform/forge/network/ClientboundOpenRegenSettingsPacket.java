package com.github.TeThoLaPot.regen_resources.platform.forge.network;

import com.github.TeThoLaPot.regen_resources.platform.forge.client.screen.RegenSettingsScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** 再生設定 Screen をスナップショット付きで開く。 */
public record ClientboundOpenRegenSettingsPacket(RegenSettingsSnapshot snapshot) {

    public static void encode(ClientboundOpenRegenSettingsPacket msg, FriendlyByteBuf buf) {
        RegenSettingsSnapshot.encode(msg.snapshot(), buf);
    }

    public static ClientboundOpenRegenSettingsPacket decode(FriendlyByteBuf buf) {
        return new ClientboundOpenRegenSettingsPacket(RegenSettingsSnapshot.decode(buf));
    }

    public static void handle(ClientboundOpenRegenSettingsPacket msg, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> {
            if (FMLEnvironment.dist != Dist.CLIENT) {
                return;
            }
            Minecraft mc = Minecraft.getInstance();
            mc.setScreen(new RegenSettingsScreen(msg.snapshot()));
        });
        ctx.setPacketHandled(true);
    }
}
