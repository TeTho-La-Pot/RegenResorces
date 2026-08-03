package com.github.TeThoLaPot.regen_resources.platform.neoforge.command;

import com.github.TeThoLaPot.regen_resources.platform.neoforge.network.ClientboundOpenRegenSettingsPacket;
import com.github.TeThoLaPot.regen_resources.platform.neoforge.network.RegenSettingsSnapshot;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * {@code /rr settings} でワールド再生設定 UI を開く（OP レベル 2）。
 * 設定ファイルは {@code <world>/serverconfig/RegenResources/RegenPresets/}。
 */
public final class RegenSettingsCommands {

    private static final int PERMISSION_SETTINGS = 2;

    private RegenSettingsCommands() {}

    @SubscribeEvent
    public static void register(RegisterCommandsEvent event) {
        registerTree(event.getDispatcher());
    }

    private static void registerTree(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("regenresources").then(settingsNode()));
        dispatcher.register(Commands.literal("rr").then(settingsNode()));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> settingsNode() {
        return Commands.literal("settings")
                .requires(src -> src.hasPermission(PERMISSION_SETTINGS))
                .executes(ctx -> openSettings(ctx.getSource()));
    }

    private static int openSettings(CommandSourceStack source) {
        ServerPlayer player = source.getPlayer();
        if (player == null) {
            source.sendFailure(Component.translatable("commands.regen_resources.settings.players_only"));
            return 0;
        }
        openSettingsFor(player);
        source.sendSuccess(() -> Component.translatable("commands.regen_resources.settings.opened"), false);
        return 1;
    }

    /** プレイヤーに設定 UI を開くパケットを送る（コマンド／選択のオーブ共通）。 */
    public static void openSettingsFor(ServerPlayer player) {
        RegenSettingsSnapshot snapshot = RegenSettingsSnapshot.fromDisk();
        PacketDistributor.sendToPlayer(player, new ClientboundOpenRegenSettingsPacket(snapshot));
    }
}
