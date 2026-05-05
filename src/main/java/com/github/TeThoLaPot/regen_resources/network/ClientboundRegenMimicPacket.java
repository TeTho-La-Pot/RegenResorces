package com.github.TeThoLaPot.regen_resources.network;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * TT 由来のミミック断片を即クライアントキャッシュへ流す（BE 標準パケットより前後が安定しない問題の補強）。
 */
public final class ClientboundRegenMimicPacket {

    private final BlockPos pos;
    private final CompoundTag hintPayload;

    public ClientboundRegenMimicPacket(BlockPos pos, CompoundTag hintPayload) {
        this.pos = pos;
        this.hintPayload = hintPayload;
    }

    public static void encode(ClientboundRegenMimicPacket msg, FriendlyByteBuf buffer) {
        buffer.writeBlockPos(msg.pos);
        buffer.writeNbt(msg.hintPayload != null ? msg.hintPayload : new CompoundTag());
    }

    public static ClientboundRegenMimicPacket decode(FriendlyByteBuf buffer) {
        BlockPos decodedPos = buffer.readBlockPos();
        CompoundTag nbt = buffer.readNbt();
        return new ClientboundRegenMimicPacket(decodedPos, nbt != null ? nbt : new CompoundTag());
    }

    public static void handle(ClientboundRegenMimicPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();
            Level level = mc.level;
            if (level == null || !level.isClientSide()) {
                return;
            }
            if (msg.hintPayload == null || msg.hintPayload.isEmpty()) {
                return;
            }
            RegenMimicClientCache.put(level, msg.pos, msg.hintPayload);
        });
        ctx.get().setPacketHandled(true);
    }
}
