package com.github.TeThoLaPot.regen_resources.platform.neoforge.client.widget;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;

/**
 * 塗りつぶし型チェックボックス（選択時は枠内を明るいグレーで塗る）。
 * entries / 状態トグルなど、本 mod のチェック UI はこれで統一する。
 */
public final class FilledCheckbox extends AbstractWidget {

    public static final int BOX = 14;

    private boolean selected;
    private final Consumer<Boolean> onChanged;

    public FilledCheckbox(int x, int y, boolean selected, Consumer<Boolean> onChanged) {
        super(x, y, BOX, BOX, Component.empty());
        this.selected = selected;
        this.onChanged = onChanged;
    }

    public boolean selected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        selected = !selected;
        onChanged.accept(selected);
    }

    @Override
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        drawBox(graphics, this.getX(), this.getY(), selected, this.isHoveredOrFocused());
    }

    /** 枠だけ描画（ラベル付きトグルなどから共用）。 */
    public static void drawBox(GuiGraphics graphics, int x, int y, boolean selected, boolean hovered) {
        graphics.fill(x, y, x + BOX, y + BOX, 0xFF2A2A2A);
        graphics.renderOutline(x, y, BOX, BOX, hovered ? 0xFFFFFFFF : 0xFF8B8B8B);
        if (selected) {
            graphics.fill(x + 3, y + 3, x + BOX - 3, y + BOX - 3, 0xFFE0E0E0);
        }
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        output.add(
                NarratedElementType.TITLE,
                Component.translatable(selected ? "gui.narrate.checkbox.checked" : "gui.narrate.checkbox.unchecked"));
    }
}
