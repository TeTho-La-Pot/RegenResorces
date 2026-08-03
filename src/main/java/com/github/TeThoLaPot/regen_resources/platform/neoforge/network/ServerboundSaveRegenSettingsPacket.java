package com.github.TeThoLaPot.regen_resources.platform.neoforge.network;

import com.github.TeThoLaPot.regen_resources.RegenResources;
import com.github.TeThoLaPot.regen_resources.platform.neoforge.RegenResourcesForgeBootstrap;
import com.github.TeThoLaPot.regen_resources.platform.neoforge.config.RegenPresetIo;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 設定 UI の保存。{@code files} が権威ある一覧（含まれない既存ファイルは削除）。
 */
public record ServerboundSaveRegenSettingsPacket(List<RegenSettingsSnapshot.PresetFile> files)
        implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<ServerboundSaveRegenSettingsPacket> TYPE =
            new CustomPacketPayload.Type<>(
                    ResourceLocation.fromNamespaceAndPath(RegenResources.MOD_ID, "save_regen_settings"));

    public static final StreamCodec<FriendlyByteBuf, ServerboundSaveRegenSettingsPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.collection(ArrayList::new, RegenSettingsSnapshot.PresetFile.STREAM_CODEC),
                    ServerboundSaveRegenSettingsPacket::files,
                    ServerboundSaveRegenSettingsPacket::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(ServerboundSaveRegenSettingsPacket msg, IPayloadContext ctx) {
        ctx.enqueueWork(
                () -> {
                    if (!(ctx.player() instanceof ServerPlayer player)) {
                        return;
                    }
                    if (!player.hasPermissions(2)) {
                        player.sendSystemMessage(Component.translatable("commands.regen_resources.settings.save.denied"));
                        return;
                    }
                    if (RegenPresetIo.rulesDir() == null) {
                        player.sendSystemMessage(Component.translatable("commands.regen_resources.settings.save.no_world"));
                        return;
                    }

                    Set<String> keep = new HashSet<>();
                    for (RegenSettingsSnapshot.PresetFile file : msg.files()) {
                        if (!RegenPresetIo.isValidPresetFileName(file.name())) {
                            player.sendSystemMessage(
                                    Component.translatable(
                                            "commands.regen_resources.settings.save.bad_name", file.name()));
                            return;
                        }
                        String err = RegenPresetIo.writePresetJson(file.name(), file.json());
                        if (err != null) {
                            player.sendSystemMessage(
                                    Component.translatable(
                                            "commands.regen_resources.settings.save.failed", file.name(), err));
                            return;
                        }
                        keep.add(file.name());
                    }

                    for (String existing : RegenPresetIo.listPresetFileNames()) {
                        if (!keep.contains(existing)) {
                            RegenPresetIo.deletePresetJson(existing);
                        }
                    }

                    RegenResourcesForgeBootstrap.applyPresetRulesFromDisk();
                    player.sendSystemMessage(Component.translatable("commands.regen_resources.settings.save.ok"));
                    player.sendSystemMessage(Component.translatable("commands.regen_resources.settings.save.shells_unchanged"));
                });
    }
}
