package com.github.TeThoLaPot.regen_resources.platform.neoforge.client.widget;

import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Create シーケンス風のホイール選択ボックス。
 * ホバー中にホイール／TAB で候補をプレビューし、クリックで確定する。
 */
public final class CycleSelectWidget<T> extends AbstractWidget {

    private final List<T> options;
    private final Function<T, Component> labeler;
    private final Consumer<T> onChanged;
    /** ホイールで動くプレビュー位置 */
    private int index;
    /** 確定済み位置（未クリックのスクロールはここに戻る） */
    private int committedIndex;
    private long marqueeStartMs = -1L;

    public CycleSelectWidget(
            int x,
            int y,
            int width,
            int height,
            List<T> options,
            T initial,
            Function<T, Component> labeler,
            Consumer<T> onChanged) {
        super(x, y, width, height, Component.empty());
        this.options = new ArrayList<>(options);
        this.labeler = labeler;
        this.onChanged = onChanged;
        this.index = Math.max(0, this.options.indexOf(initial));
        if (this.index < 0 || this.index >= this.options.size()) {
            this.index = 0;
        }
        this.committedIndex = this.index;
    }

    public T value() {
        if (options.isEmpty()) {
            return null;
        }
        return options.get(Mth.clamp(index, 0, options.size() - 1));
    }

    public T committedValue() {
        if (options.isEmpty()) {
            return null;
        }
        return options.get(Mth.clamp(committedIndex, 0, options.size() - 1));
    }

    public void setValue(T value) {
        int i = options.indexOf(value);
        if (i >= 0) {
            index = i;
            committedIndex = i;
        }
    }

    public void setOptions(List<T> next, T selected) {
        options.clear();
        options.addAll(next);
        index = Math.max(0, options.indexOf(selected));
        if (index < 0 || index >= options.size()) {
            index = 0;
        }
        committedIndex = index;
    }

    public boolean shouldShowOverlay() {
        return this.isHovered() && !options.isEmpty();
    }

    /** スクロール判定はボックス本体のみ。 */
    public boolean isMouseOverSelectArea(double mouseX, double mouseY) {
        return !options.isEmpty() && this.isMouseOver(mouseX, mouseY);
    }

    private int[] overlayBox() {
        int lineH = 14;
        int pad = 4;
        int headerH = 14;
        int footerH = 12;
        int maxVisible = Math.min(Math.max(options.size(), 1), 8);
        int boxH = headerH + maxVisible * lineH + footerH + pad;
        int boxW = Math.max(this.width + 40, 140);
        int boxX = this.getX();
        int boxY = this.getY() - boxH - 2;
        if (boxY < 4) {
            boxY = this.getY() + this.height + 2;
        }
        return new int[] {boxX, boxY, boxW, boxH};
    }

    @Override
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        if (!this.isHovered()) {
            index = committedIndex;
        }
        boolean hot = this.isHovered();
        int bg = hot ? 0xFF3A3A3A : 0xFF2A2A2A;
        graphics.fill(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, bg);
        graphics.renderOutline(this.getX(), this.getY(), this.width, this.height, 0xFF8B8B8B);

        Font font = Minecraft.getInstance().font;
        String label = options.isEmpty() ? "-" : labeler.apply(value()).getString();
        int pad = 4;
        int textX = this.getX() + pad;
        int textY = this.getY() + (this.height - 8) / 2;
        int textMaxW = Math.max(1, this.width - pad * 2);
        int textW = font.width(label);

        graphics.enableScissor(textX, this.getY(), this.getX() + this.width - pad, this.getY() + this.height);
        if (hot && textW > textMaxW) {
            if (marqueeStartMs < 0L) {
                marqueeStartMs = Util.getMillis();
            }
            int offset = StateValueToggle.marqueeOffset(textW - textMaxW, Util.getMillis() - marqueeStartMs);
            graphics.drawString(font, label, textX - offset, textY, 0xFFE0E0E0, false);
        } else {
            marqueeStartMs = -1L;
            if (textW > textMaxW) {
                graphics.drawString(
                        font,
                        font.plainSubstrByWidth(label, textMaxW - font.width("…")) + "…",
                        textX,
                        textY,
                        0xFFE0E0E0,
                        false);
            } else {
                graphics.drawString(font, label, textX, textY, 0xFFE0E0E0, false);
            }
        }
        graphics.disableScissor();
    }

    public void renderOverlayFront(GuiGraphics graphics) {
        if (!shouldShowOverlay()) {
            return;
        }
        Font font = Minecraft.getInstance().font;
        int[] box = overlayBox();
        int boxX = box[0];
        int boxY = box[1];
        int boxW = box[2];
        int boxH = box[3];
        int lineH = 14;
        int pad = 4;
        int headerH = 14;
        int footerH = 12;
        int maxVisible = Math.min(options.size(), 8);

        graphics.fill(boxX, boxY, boxX + boxW, boxY + boxH, 0xFF101020);
        graphics.renderOutline(boxX, boxY, boxW, boxH, 0xFFAA55FF);
        graphics.drawString(
                font,
                Component.translatable("screen.regen_resources.settings.cycle.title"),
                boxX + pad,
                boxY + 3,
                0xFF88CCFF,
                false);

        int start = Math.max(0, index - maxVisible / 2);
        int end = Math.min(options.size(), start + maxVisible);
        start = Math.max(0, end - maxVisible);

        int rowY = boxY + headerH;
        int rowTextMax = boxW - pad * 2 - font.width("-> ");
        for (int i = start; i < end; i++) {
            boolean sel = i == index;
            String prefix = sel ? "-> " : "> ";
            String text = labeler.apply(options.get(i)).getString();
            if (font.width(text) > rowTextMax) {
                text = font.plainSubstrByWidth(text, Math.max(1, rowTextMax - font.width("…"))) + "…";
            }
            graphics.drawString(font, prefix + text, boxX + pad, rowY, sel ? 0xFFFFFF55 : 0xFFCCCCCC, false);
            rowY += lineH;
        }
        graphics.drawString(
                font,
                Component.translatable("screen.regen_resources.settings.cycle.hint"),
                boxX + pad,
                boxY + boxH - footerH,
                0xFF888888,
                false);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0 || options.isEmpty() || !this.isMouseOver(mouseX, mouseY)) {
            return false;
        }
        commitCurrent();
        return true;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (!isMouseOverSelectArea(mouseX, mouseY)) {
            return false;
        }
        previewCycle(scrollY > 0 ? -1 : 1);
        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (options.isEmpty() || !this.isHovered()) {
            return false;
        }
        if (keyCode == 258) {
            previewCycle(Screen.hasShiftDown() ? -1 : 1);
            return true;
        }
        return false;
    }

    private void previewCycle(int delta) {
        if (options.isEmpty()) {
            return;
        }
        int next = Mth.clamp(index + delta, 0, options.size() - 1);
        if (next != index) {
            index = next;
        }
    }

    private void commitCurrent() {
        if (options.isEmpty()) {
            return;
        }
        index = Mth.clamp(index, 0, options.size() - 1);
        boolean changed = committedIndex != index;
        committedIndex = index;
        if (changed && onChanged != null) {
            onChanged.accept(value());
        }
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        this.defaultButtonNarrationText(output);
    }
}
