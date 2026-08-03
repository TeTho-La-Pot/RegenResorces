package com.github.TeThoLaPot.regen_resources.platform.forge.client.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/**
 * 通常ボタンテクスチャの上に、バニラ checkbox と同じチェックマークだけを重ねる決定ボタン。
 */
public final class CheckMarkButton extends Button {

    private static final ResourceLocation CHECKMARK =
            ResourceLocation.fromNamespaceAndPath("regen_resources", "textures/gui/widget/checkmark.png");
    private static final int TEXTURE_SIZE = 20;

    public CheckMarkButton(int x, int y, int width, int height, Runnable onPress) {
        super(x, y, width, height, Component.empty(), b -> onPress.run(), DEFAULT_NARRATION);
    }

    @Override
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.renderWidget(graphics, mouseX, mouseY, partialTick);
        graphics.setColor(1.0F, 1.0F, 1.0F, this.alpha);
        RenderSystem.enableBlend();
        // バニラ checkbox スプライトと同サイズ感で中央に配置
        int size = Math.min(this.width, this.height);
        int bx = this.getX() + (this.width - size) / 2;
        int by = this.getY() + (this.height - size) / 2;
        graphics.blit(CHECKMARK, bx, by, size, size, 0.0F, 0.0F, TEXTURE_SIZE, TEXTURE_SIZE, TEXTURE_SIZE, TEXTURE_SIZE);
        graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
    }
}
