package com.github.TeThoLaPot.regen_resources.platform.forge.client.widget;

import com.github.TeThoLaPot.regen_resources.common.regen.RegenTargetSpec;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

import java.util.List;

/** ターゲット選択ボタン。ホバー中は Create 風に登録ブロックを循環表示する。 */
public final class TargetSelectButton extends Button {

    private final TargetCycleHoverPreview preview = new TargetCycleHoverPreview();

    public TargetSelectButton(int x, int y, int width, int height, Component message, OnPress onPress) {
        super(x, y, width, height, message, onPress, DEFAULT_NARRATION);
    }

    public void setTargets(List<RegenTargetSpec> targets) {
        preview.setFromTargets(targets);
    }

    /** 最前面オーバーレイとして呼ぶ。 */
    public void renderHoverPreview(GuiGraphics graphics) {
        preview.renderNearAnchor(
                graphics, this.isHoveredOrFocused(), this.getX(), this.getY(), this.getWidth());
    }
}
