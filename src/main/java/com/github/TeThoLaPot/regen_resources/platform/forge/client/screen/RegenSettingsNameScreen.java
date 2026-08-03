package com.github.TeThoLaPot.regen_resources.platform.forge.client.screen;

import com.github.TeThoLaPot.regen_resources.platform.forge.config.RegenPresetIo;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.Locale;
import java.util.function.Consumer;

/** 新規作成／名前編集用の名称入力サブ UI。 */
public final class RegenSettingsNameScreen extends Screen {

    public enum Mode {
        CREATE,
        RENAME
    }

    private final Screen parent;
    private final Mode mode;
    private final String initialName;
    private final Consumer<String> onConfirm;
    private EditBox nameBox;
    private Component error = Component.empty();

    public RegenSettingsNameScreen(Screen parent, Mode mode, String initialName, Consumer<String> onConfirm) {
        super(
                mode == Mode.CREATE
                        ? Component.translatable("screen.regen_resources.settings.name.create_title")
                        : Component.translatable("screen.regen_resources.settings.name.rename_title"));
        this.parent = parent;
        this.mode = mode;
        this.initialName = initialName == null ? "" : initialName;
        this.onConfirm = onConfirm;
    }

    @Override
    protected void init() {
        int boxW = 200;
        int boxX = (this.width - boxW) / 2;
        int boxY = this.height / 2 - 20;
        this.nameBox = new EditBox(this.font, boxX, boxY, boxW, 20, Component.translatable("screen.regen_resources.settings.name.field"));
        this.nameBox.setMaxLength(64);
        String seed = stripJson(initialName);
        this.nameBox.setValue(seed);
        this.addRenderableWidget(this.nameBox);
        this.setInitialFocus(this.nameBox);

        int btnW = 90;
        this.addRenderableWidget(
                Button.builder(Component.translatable("gui.cancel"), b -> this.minecraft.setScreen(parent))
                        .bounds(boxX, boxY + 36, btnW, 20)
                        .build());
        this.addRenderableWidget(
                Button.builder(Component.translatable("gui.done"), b -> tryConfirm())
                        .bounds(boxX + boxW - btnW, boxY + 36, btnW, 20)
                        .build());
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        boolean handled = super.mouseClicked(mouseX, mouseY, button);
        if (button == 0 && nameBox != null && !nameBox.isMouseOver(mouseX, mouseY)) {
            nameBox.setFocused(false);
            if (this.getFocused() == nameBox) {
                this.setFocused(null);
            }
        }
        return handled;
    }

    private void tryConfirm() {
        String raw = this.nameBox.getValue().trim().toLowerCase(Locale.ROOT);
        if (raw.isEmpty()) {
            error = Component.translatable("screen.regen_resources.settings.name.empty");
            return;
        }
        if (!raw.endsWith(".json")) {
            raw = raw + ".json";
        }
        if (!RegenPresetIo.isValidPresetFileName(raw)) {
            error = Component.translatable("screen.regen_resources.settings.name.invalid");
            return;
        }
        onConfirm.accept(raw);
        this.minecraft.setScreen(parent);
    }

    private static String stripJson(String name) {
        if (name != null && name.toLowerCase(Locale.ROOT).endsWith(".json")) {
            return name.substring(0, name.length() - 5);
        }
        return name == null ? "" : name;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        graphics.drawCenteredString(this.font, this.title, this.width / 2, this.height / 2 - 48, 0xFFFFFF);
        if (error != null && !error.getString().isEmpty()) {
            graphics.drawCenteredString(this.font, error, this.width / 2, this.height / 2 + 64, 0xFF5555);
        }
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(parent);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
