package com.github.TeThoLaPot.regen_resources.forge;

import com.github.TeThoLaPot.regen_resources.RegenConstants;
import com.github.TeThoLaPot.regen_resources.init.block.RegenBlocks;
import com.github.TeThoLaPot.regen_resources.init.block.RegenVisual;
import com.github.TeThoLaPot.regen_resources.init.block.Re_Blocks;
import com.github.TeThoLaPot.regen_resources.init.entity.RegenBlockEntity;
import com.github.TeThoLaPot.regen_resources.network.ClientboundRegenMimicPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

/**
 * 再生ブロックのミミック Hint（クライアント先行キャッシュ用）。
 */
@Mod.EventBusSubscriber(modid = RegenConstants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class RegenNetworking {

    private static final String PROTOCOL_VERSION = "1";

    @SuppressWarnings("deprecation")
    private static final ResourceLocation CHANNEL_NAME = new ResourceLocation(RegenConstants.MOD_ID, "regen_mimic");

    @SuppressWarnings("deprecation")
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            CHANNEL_NAME,
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals);

    private static int nextId = 0;

    private RegenNetworking() {}

    @SubscribeEvent
    public static void onCommonSetup(FMLCommonSetupEvent event) {
        CHANNEL.registerMessage(
                nextId++,
                ClientboundRegenMimicPacket.class,
                ClientboundRegenMimicPacket::encode,
                ClientboundRegenMimicPacket::decode,
                ClientboundRegenMimicPacket::handle);
    }

    /** TT ストレージに保存されている {@code state} / {@code restore_rl} 等を最小限コピーして Hint として送る。 */
    public static CompoundTag slimCopyForMimicHint(CompoundTag full) {
        if (full == null || full.isEmpty()) {
            return new CompoundTag();
        }
        CompoundTag slim = new CompoundTag();
        copyTagIfPresent(full, slim, "state");
        copyTagIfPresent(full, slim, RegenBlockEntity.TAG_RESTORE_RL);
        copyTagIfPresent(full, slim, "execute_at");
        return slim;
    }

    private static void copyTagIfPresent(CompoundTag from, CompoundTag to, String key) {
        if (!from.contains(key)) {
            return;
        }
        to.put(key, from.get(key).copy());
    }

    /**
     * チャンクロード中に {@link PacketDistributor#TRACKING_CHUNK} や {@link ServerLevel#getChunkAt} が絡むと
     * チャンクマネージャとデッドロックし、ロード進捗が止まることがある。次のサーバーティックに先送りする。
     */
    public static void scheduleMimicHintToChunkTrackers(ServerLevel server, BlockPos pos, CompoundTag ttSnapshot) {
        MinecraftServer minecraftServer = server.getServer();
        BlockPos immutable = pos.immutable();
        CompoundTag snapshot = ttSnapshot.copy();
        minecraftServer.execute(() -> {
            if (!(server.getBlockEntity(immutable) instanceof RegenBlockEntity be) || be.isRemoved()) {
                return;
            }
            BlockState bs = be.getBlockState();
            if (!bs.hasProperty(RegenBlocks.VISUAL) || bs.getValue(RegenBlocks.VISUAL) != RegenVisual.MIMIC) {
                return;
            }
            sendMimicHintToChunkTrackers(server, immutable, snapshot);
        });
    }

    static void sendMimicHintToChunkTrackers(ServerLevel server, BlockPos pos, CompoundTag ttSnapshot) {
        if (!server.getBlockState(pos).is(Re_Blocks.REGEN_BLOCK.get())) {
            return;
        }
        CompoundTag slim = slimCopyForMimicHint(ttSnapshot);
        if (slim.isEmpty()) {
            return;
        }
        ClientboundRegenMimicPacket packet = new ClientboundRegenMimicPacket(pos, slim);
        Vec3 ctr = Vec3.atCenterOf(pos);
        int vdChunks = Math.max(server.getServer().getPlayerList().getViewDistance(), 2);
        double radius = (vdChunks + 1) * 16.0;
        double radiusSq = radius * radius;
        for (ServerPlayer player : server.getServer().getPlayerList().getPlayers()) {
            if (!player.level().dimension().equals(server.dimension())) {
                continue;
            }
            if (player.distanceToSqr(ctr) > radiusSq) {
                continue;
            }
            CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), packet);
        }
    }
}
