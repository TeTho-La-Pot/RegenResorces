package com.github.TeThoLaPot.regen_resources.platform.neoforge.client.screen;

import com.github.TeThoLaPot.regen_resources.common.block.RegenTemplate;
import com.github.TeThoLaPot.regen_resources.platform.neoforge.client.widget.CheckMarkButton;
import com.github.TeThoLaPot.regen_resources.platform.neoforge.client.widget.CycleSelectWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/** custom / custom_preset 用: template・textures・mining_sample を一式編集。 */
public final class RegenCustomVisualScreen extends Screen {

    public record Draft(RegenTemplate template, Map<String, String> textures, String miningSample) {}

    private static final int PANEL_W = 280;
    private static final int PANEL_H = 240;

    private final Screen parent;
    private final Consumer<Draft> onConfirm;

    private RegenTemplate template;
    private final Map<String, String> textures = new LinkedHashMap<>();
    private String miningSample;

    private int leftPos;
    private int topPos;
    private final List<LabelDraw> labels = new ArrayList<>();
    private final List<SlotPreview> slotPreviews = new ArrayList<>();

    public RegenCustomVisualScreen(Screen parent, Draft initial, Consumer<Draft> onConfirm) {
        super(Component.translatable("screen.regen_resources.custom_visual.title"));
        this.parent = parent;
        this.onConfirm = onConfirm;
        this.template = initial.template() != null ? initial.template() : RegenTemplate.CUBE_ALL;
        this.miningSample = initial.miningSample() == null ? "" : initial.miningSample();
        syncTexturesToTemplate(initial.textures());
    }

    private void syncTexturesToTemplate(@Nullable Map<String, String> source) {
        Map<String, String> prev = new LinkedHashMap<>(textures);
        if (source != null) {
            prev.putAll(source);
        }
        textures.clear();
        for (String slot : template.slots()) {
            String v = prev.get(slot);
            textures.put(slot, v == null ? "" : v);
        }
    }

    @Override
    protected void init() {
        this.leftPos = (this.width - PANEL_W) / 2;
        this.topPos = (this.height - PANEL_H) / 2;
        rebuildBody();
    }

    private void rebuildBody() {
        this.clearWidgets();
        labels.clear();
        slotPreviews.clear();

        int x = this.leftPos + 10;
        int y = this.topPos + 20;
        int fieldW = PANEL_W - 20;

        Component templateLabel = Component.translatable("screen.regen_resources.custom_visual.label.template");
        int templateLabelW = this.font.width(templateLabel) + 4;
        labels.add(new LabelDraw(x, y + 5, templateLabel));
        List<RegenTemplate> opts = Arrays.asList(RegenTemplate.values());
        this.addRenderableWidget(
                new CycleSelectWidget<>(
                        x + templateLabelW,
                        y,
                        Math.max(40, fieldW - templateLabelW),
                        18,
                        opts,
                        template,
                        t -> Component.literal(t.getSerializedName()),
                        picked -> {
                            if (picked == null || picked == template) {
                                return;
                            }
                            template = picked;
                            syncTexturesToTemplate(null);
                            rebuildBody();
                        }));
        y += 22;

        for (String slot : template.slots()) {
            final String slotKey = slot;
            Component slotLabel = Component.literal(slot);
            int labelW = this.font.width(slotLabel) + 4;
            labels.add(new LabelDraw(x, y + 5, slotLabel));
            String current = textures.getOrDefault(slotKey, "");
            Component btnText = current.isBlank()
                    ? Component.translatable("screen.regen_resources.custom_visual.btn.texture_select")
                    : Component.literal(current);
            int preview = 18;
            int btnW = Math.max(40, fieldW - labelW - preview - 4);
            int btnX = x + labelW + preview + 2;
            slotPreviews.add(new SlotPreview(x + labelW, y + 1, slotKey));
            this.addRenderableWidget(
                    Button.builder(btnText, b -> openTexturePick(slotKey))
                            .bounds(btnX, y, btnW, 18)
                            .build());
            y += 22;
        }

        Component miningLabel = Component.translatable("screen.regen_resources.custom_visual.label.mining_sample");
        int miningLabelW = this.font.width(miningLabel) + 4;
        labels.add(new LabelDraw(x, y + 5, miningLabel));
        Component miningBtn = miningSample == null || miningSample.isBlank()
                ? Component.translatable("screen.regen_resources.custom_visual.btn.mining_select")
                : Component.literal(miningSample);
        this.addRenderableWidget(
                Button.builder(miningBtn, b -> openMiningPick())
                        .bounds(x + miningLabelW, y, Math.max(40, fieldW - miningLabelW), 18)
                        .build());

        int btnY = this.topPos + PANEL_H - 26;
        this.addRenderableWidget(
                Button.builder(Component.literal("✕"), b -> cancel())
                        .bounds(this.leftPos + 8, btnY, 20, 20)
                        .build());
        this.addRenderableWidget(new CheckMarkButton(this.leftPos + PANEL_W - 28, btnY, 20, 20, this::confirm));
    }

    private void openTexturePick(String slot) {
        this.minecraft.setScreen(new RegenTexturePickScreen(this, id -> {
            textures.put(slot, id.toString());
            this.minecraft.setScreen(this);
            rebuildBody();
        }));
    }

    private void openMiningPick() {
        this.minecraft.setScreen(RegenTargetPickScreen.forBlockId(this, id -> {
            miningSample = id.toString();
            this.minecraft.setScreen(this);
            rebuildBody();
        }));
    }

    private void confirm() {
        Map<String, String> outTex = new LinkedHashMap<>();
        for (Map.Entry<String, String> e : textures.entrySet()) {
            if (e.getValue() != null && !e.getValue().isBlank()) {
                outTex.put(e.getKey(), e.getValue());
            }
        }
        onConfirm.accept(new Draft(template, outTex, miningSample == null ? "" : miningSample));
        this.minecraft.setScreen(parent);
    }

    private void cancel() {
        this.minecraft.setScreen(parent);
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fill(0, 0, this.width, this.height, 0xC0101010);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics, mouseX, mouseY, partialTick);
        drawPanel(graphics);

        int titleX = this.width / 2 - this.font.width(this.title) / 2;
        graphics.drawString(this.font, this.title, titleX, this.topPos + 6, 0x404040, false);

        for (LabelDraw label : labels) {
            graphics.drawString(this.font, label.text(), label.x(), label.y(), 0x404040, false);
        }

        TextureAtlas atlas = Minecraft.getInstance().getModelManager().getAtlas(InventoryMenu.BLOCK_ATLAS);
        ResourceLocation missing = MissingTextureAtlasSprite.getLocation();
        for (SlotPreview preview : slotPreviews) {
            String idStr = textures.getOrDefault(preview.slot(), "");
            if (idStr.isBlank()) {
                graphics.fill(preview.x(), preview.y(), preview.x() + 16, preview.y() + 16, 0xFF373737);
                graphics.renderOutline(preview.x(), preview.y(), 16, 16, 0xFF555555);
                continue;
            }
            ResourceLocation rl = ResourceLocation.tryParse(idStr);
            if (rl == null) {
                graphics.fill(preview.x(), preview.y(), preview.x() + 16, preview.y() + 16, 0xFF5A2020);
                continue;
            }
            TextureAtlasSprite sprite = atlas.getSprite(rl);
            if (sprite == null || sprite.contents().name().equals(missing)) {
                graphics.fill(preview.x(), preview.y(), preview.x() + 16, preview.y() + 16, 0xFF5A2020);
            } else {
                graphics.blit(preview.x(), preview.y(), 0, 16, 16, sprite);
            }
        }

        for (var renderable : this.renderables) {
            renderable.render(graphics, mouseX, mouseY, partialTick);
        }

        graphics.pose().pushPose();
        graphics.pose().translate(0, 0, 300);
        for (var renderable : this.renderables) {
            if (renderable instanceof CycleSelectWidget<?> cycle) {
                cycle.renderOverlayFront(graphics);
            }
        }
        graphics.pose().popPose();

        for (var renderable : this.renderables) {
            if (renderable instanceof AbstractWidget w && w.getY() >= this.topPos + PANEL_H - 30) {
                w.render(graphics, mouseX, mouseY, partialTick);
            }
        }
    }

    private void drawPanel(GuiGraphics graphics) {
        int x = this.leftPos;
        int y = this.topPos;
        graphics.fill(x, y, x + PANEL_W, y + PANEL_H, 0xFF000000);
        graphics.fill(x + 1, y + 1, x + PANEL_W - 1, y + PANEL_H - 1, 0xFFC6C6C6);
        graphics.fill(x + 1, y + 1, x + PANEL_W - 1, y + 2, 0xFFFFFFFF);
        graphics.fill(x + 1, y + 1, x + 2, y + PANEL_H - 1, 0xFFFFFFFF);
        graphics.fill(x + 1, y + PANEL_H - 2, x + PANEL_W - 1, y + PANEL_H - 1, 0xFF555555);
        graphics.fill(x + PANEL_W - 2, y + 1, x + PANEL_W - 1, y + PANEL_H - 1, 0xFF555555);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private record LabelDraw(int x, int y, Component text) {}

    private record SlotPreview(int x, int y, String slot) {}
}
