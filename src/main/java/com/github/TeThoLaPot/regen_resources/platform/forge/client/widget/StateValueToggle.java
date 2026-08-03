package com.github.TeThoLaPot.regen_resources.platform.forge.client.widget;

import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import java.util.function.Consumer;

/** マッチ値用。1行・固定高。長い名称はホバー時マーキー。 */
public final class StateValueToggle extends AbstractWidget {

    private static final int GAP = 4;

    private final String fullLabel;
    private boolean selected;
    private final Consumer<Boolean> onChanged;
    private long marqueeStartMs = -1L;

    public StateValueToggle(int x, int y, int width, String label, boolean selected, Consumer<Boolean> onChanged) {
        super(x, y, width, 18, Component.literal(label));
        this.fullLabel = label;
        this.selected = selected;
        this.onChanged = onChanged;
    }

    public boolean selected() {
        return selected;
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        selected = !selected;
        onChanged.accept(selected);
        marqueeStartMs = Util.getMillis();
    }

    @Override
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        Font font = Minecraft.getInstance().font;
        int bx = this.getX();
        int by = this.getY() + (this.height - FilledCheckbox.BOX) / 2;
        FilledCheckbox.drawBox(graphics, bx, by, selected, this.isHoveredOrFocused());

        int textX = bx + FilledCheckbox.BOX + GAP;
        int textY = this.getY() + (this.height - 8) / 2;
        int clipRight = this.getX() + this.width;
        int textMaxW = Math.max(1, clipRight - textX);
        int textW = font.width(fullLabel);
        boolean hot = this.isHovered();

        graphics.enableScissor(textX, this.getY(), clipRight, this.getY() + this.height);
        if (hot && textW > textMaxW) {
            if (marqueeStartMs < 0L) {
                marqueeStartMs = Util.getMillis();
            }
            int offset = marqueeOffset(textW - textMaxW, Util.getMillis() - marqueeStartMs);
            graphics.drawString(font, fullLabel, textX - offset, textY, 0x404040, false);
        } else {
            marqueeStartMs = -1L;
            if (textW > textMaxW) {
                graphics.drawString(font, font.plainSubstrByWidth(fullLabel, textMaxW - font.width("…")) + "…", textX, textY, 0x404040, false);
            } else {
                graphics.drawString(font, fullLabel, textX, textY, 0x404040, false);
            }
        }
        graphics.disableScissor();
    }

    static int marqueeOffset(int overflow, long elapsedMs) {
        if (overflow <= 0) {
            return 0;
        }
        final long pauseStart = 900L;
        final long scrollMs = Math.max(2500L, overflow * 55L);
        final long pauseEnd = 900L;
        final long pauseReset = 400L;
        long cycle = pauseStart + scrollMs + pauseEnd + pauseReset;
        long t = elapsedMs % cycle;
        if (t < pauseStart) {
            return 0;
        }
        t -= pauseStart;
        if (t < scrollMs) {
            float p = t / (float) scrollMs;
            return Mth.clamp(Math.round(overflow * p), 0, overflow);
        }
        t -= scrollMs;
        if (t < pauseEnd) {
            return overflow;
        }
        return 0;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        output.add(NarratedElementType.TITLE, Component.literal(fullLabel));
    }
}
