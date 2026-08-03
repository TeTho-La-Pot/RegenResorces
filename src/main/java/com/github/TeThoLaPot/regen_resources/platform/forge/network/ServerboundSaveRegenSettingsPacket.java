package com.github.TeThoLaPot.regen_resources.platform.forge.network;

import com.github.TeThoLaPot.regen_resources.platform.forge.RegenResourcesForgeBootstrap;
import com.github.TeThoLaPot.regen_resources.platform.forge.config.RegenPresetIo;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

/**
 * 設定 UI の保存。{@code files} が権威ある一覧（含まれない既存ファイルは削除）。
 */
public record ServerboundSaveRegenSettingsPacket(List<RegenSettingsSnapshot.PresetFile> files) {

    public static void encode(ServerboundSaveRegenSettingsPacket msg, FriendlyByteBuf buf) {
        RegenSettingsSnapshot.encodeFiles(msg.files(), buf);
    }

    public static ServerboundSaveRegenSettingsPacket decode(FriendlyByteBuf buf) {
        return new ServerboundSaveRegenSettingsPacket(RegenSettingsSnapshot.decodeFiles(buf));
    }

    public static void handle(ServerboundSaveRegenSettingsPacket msg, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player == null) {
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
            player.sendSystemMessage(
                    Component.translatable("commands.regen_resources.settings.save.shells_unchanged"));
        });
        ctx.setPacketHandled(true);
    }
}
