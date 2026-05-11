package com.github.TeThoLaPot.regen_resources.platform.neoforge.network;

import com.github.TeThoLaPot.regen_resources.common.block.RegenCustomVisualSpec;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

/**
 * 再生シェル見た目のサーバー→クライアント同期。
 * NeoForge のチャンクトラッキングだけだと採掘者などが漏れることがあるため、視距離ベースのフォールバックも併用する。
 */
public final class RegenShellServerSync {

    /** {@link net.minecraft.server.players.PlayerList#getViewDistance()} に足すマージン（チャンク単位） */
    private static final int VIEW_DISTANCE_EXTRA_CHUNKS = 3;

    private RegenShellServerSync() {}

    public static ArrayList<ServerPlayer> shellAppearanceRecipients(ServerLevel level, ChunkPos targetChunk) {
        MinecraftServer server = level.getServer();
        int vd = 12;
        if (server != null) {
            vd = Math.max(2, server.getPlayerList().getViewDistance()) + VIEW_DISTANCE_EXTRA_CHUNKS;
        }
        ArrayList<ServerPlayer> out = new ArrayList<>();
        HashSet<UUID> seen = new HashSet<>();
        for (ServerPlayer player : level.players()) {
            if (player.level() != level) {
                continue;
            }
            boolean include = player.getChunkTrackingView().contains(targetChunk);
            if (!include) {
                ChunkPos pc = player.chunkPosition();
                int dx = Math.abs(pc.x - targetChunk.x);
                int dz = Math.abs(pc.z - targetChunk.z);
                if (Math.max(dx, dz) <= vd) {
                    include = true;
                }
            }
            if (include && seen.add(player.getUUID())) {
                out.add(player);
            }
        }
        return out;
    }

    public static void broadcastShellPendingPayloads(
            ServerLevel level,
            BlockPos shellAt,
            @Nullable ResourceLocation strippedId,
            boolean sendStrippedPending,
            @Nullable RegenCustomVisualSpec customSpec,
            boolean sendCustomPending) {
        ChunkPos cp = new ChunkPos(shellAt.immutable());
        ClientboundStrippedPendingPacket strippedPkt =
                sendStrippedPending && strippedId != null
                        ? new ClientboundStrippedPendingPacket(shellAt.immutable(), strippedId)
                        : null;
        ClientboundCustomVisualPendingPacket customPkt =
                sendCustomPending && customSpec != null
                        ? new ClientboundCustomVisualPendingPacket(shellAt.immutable(), customSpec)
                        : null;
        if (strippedPkt == null && customPkt == null) {
            return;
        }
        for (ServerPlayer player : shellAppearanceRecipients(level, cp)) {
            if (strippedPkt != null) {
                PacketDistributor.sendToPlayer(player, strippedPkt);
            }
            if (customPkt != null) {
                PacketDistributor.sendToPlayer(player, customPkt);
            }
        }
    }

    /** 採掘者など、上記集合に含まれないクライアント向けの明示送信（冪等）。 */
    public static void alsoSendShellPendingToPrimary(
            ServerLevel level,
            @Nullable ServerPlayer primary,
            BlockPos shellAt,
            @Nullable ResourceLocation strippedId,
            boolean sendStrippedPending,
            @Nullable RegenCustomVisualSpec customSpec,
            boolean sendCustomPending) {
        if (primary == null || primary.level() != level) {
            return;
        }
        if (sendStrippedPending && strippedId != null) {
            PacketDistributor.sendToPlayer(primary, new ClientboundStrippedPendingPacket(shellAt.immutable(), strippedId));
        }
        if (sendCustomPending && customSpec != null) {
            PacketDistributor.sendToPlayer(primary, new ClientboundCustomVisualPendingPacket(shellAt.immutable(), customSpec));
        }
    }

    public static void broadcastBlockEntityToChunkTrackers(ServerLevel level, BlockPos pos) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be == null) {
            return;
        }
        be.setChanged();
        Packet<ClientGamePacketListener> packet = ClientboundBlockEntityDataPacket.create(be);
        if (packet == null) {
            return;
        }
        ChunkPos cp = new ChunkPos(pos.immutable());
        for (ServerPlayer player : shellAppearanceRecipients(level, cp)) {
            player.connection.send(packet);
        }
    }

    public static void alsoSendBlockEntityToPrimary(ServerLevel level, @Nullable ServerPlayer primary, BlockPos pos) {
        if (primary == null || primary.level() != level) {
            return;
        }
        BlockEntity be = level.getBlockEntity(pos);
        if (be == null) {
            return;
        }
        be.setChanged();
        Packet<ClientGamePacketListener> packet = ClientboundBlockEntityDataPacket.create(be);
        if (packet != null) {
            primary.connection.send(packet);
        }
    }
}
