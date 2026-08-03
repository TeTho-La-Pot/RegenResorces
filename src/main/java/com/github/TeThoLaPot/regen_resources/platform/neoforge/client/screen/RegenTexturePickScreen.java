package com.github.TeThoLaPot.regen_resources.platform.neoforge.client.screen;

import com.github.TeThoLaPot.regen_resources.platform.neoforge.client.widget.CheckMarkButton;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

/** BLOCK_ATLAS のテクスチャ ID 検索。一覧は上、検索は下。 */
public final class RegenTexturePickScreen extends Screen {

    private static final int PANEL_W = 280;
    private static final int PANEL_H = 220;
    private static final int ROW_H = 18;
    private static final int SUGGEST_MAX = 5;

    private final Screen parent;
    private final Consumer<ResourceLocation> onPicked;

    private EditBox searchBox;
    private List<ResourceLocation> allEntries = List.of();
    private List<ResourceLocation> filtered = List.of();
    private int scrollOffset;
    private int selectedIndex = -1;
    private int suggestHover = -1;

    private int leftPos;
    private int topPos;
    private int listX;
    private int listY;
    private int listW;
    private int listH;

    public RegenTexturePickScreen(Screen parent, Consumer<ResourceLocation> onPicked) {
        super(Component.translatable("screen.regen_resources.texture_pick.title"));
        this.parent = parent;
        this.onPicked = onPicked;
    }

    @Override
    protected void init() {
        this.leftPos = (this.width - PANEL_W) / 2;
        this.topPos = (this.height - PANEL_H) / 2;

        int searchY = this.topPos + PANEL_H - 48;
        int btnY = this.topPos + PANEL_H - 26;
        this.listX = this.leftPos + 8;
        this.listY = this.topPos + 18;
        this.listW = PANEL_W - 16;
        this.listH = searchY - 4 - this.listY;

        this.allEntries = buildEntries();
        this.filtered = new ArrayList<>(allEntries);

        this.searchBox = new EditBox(
                this.font,
                this.leftPos + 8,
                searchY,
                PANEL_W - 16,
                18,
                Component.translatable("screen.regen_resources.texture_pick.search"));
        this.searchBox.setMaxLength(256);
        this.searchBox.setHint(Component.translatable("screen.regen_resources.texture_pick.search"));
        this.searchBox.setResponder(s -> applyFilter());
        this.addRenderableWidget(this.searchBox);
        this.setInitialFocus(this.searchBox);

        this.addRenderableWidget(
                Button.builder(Component.literal("✕"), b -> cancel())
                        .bounds(this.leftPos + 8, btnY, 20, 20)
                        .build());
        this.addRenderableWidget(new CheckMarkButton(this.leftPos + PANEL_W - 28, btnY, 20, 20, this::confirm));
    }

    private static List<ResourceLocation> buildEntries() {
        TextureAtlas atlas = Minecraft.getInstance().getModelManager().getAtlas(InventoryMenu.BLOCK_ATLAS);
        ResourceLocation missing = MissingTextureAtlasSprite.getLocation();
        List<ResourceLocation> out = new ArrayList<>();
        for (ResourceLocation id : atlas.getTextures().keySet()) {
            if (id == null || id.equals(missing)) {
                continue;
            }
            // 主に block/ 系。composite 等もパスで絞り込み可能
            String path = id.getPath();
            if (path.startsWith("block/") || path.contains("/block/")) {
                out.add(id);
            }
        }
        out.sort(Comparator.comparing(ResourceLocation::toString));
        return out;
    }

    private void applyFilter() {
        String q = this.searchBox.getValue().trim().toLowerCase(Locale.ROOT);
        if (q.isEmpty()) {
            filtered = new ArrayList<>(allEntries);
        } else {
            filtered = allEntries.stream()
                    .filter(id -> id.toString().toLowerCase(Locale.ROOT).contains(q)
                            || id.getPath().toLowerCase(Locale.ROOT).contains(q))
                    .toList();
        }
        scrollOffset = 0;
        selectedIndex = filtered.isEmpty() ? -1 : 0;
        suggestHover = -1;
    }

    private List<ResourceLocation> suggestions() {
        String q = this.searchBox != null ? this.searchBox.getValue().trim().toLowerCase(Locale.ROOT) : "";
        if (q.isEmpty()) {
            return List.of();
        }
        return filtered.stream().limit(SUGGEST_MAX).toList();
    }

    private void confirm() {
        if (selectedIndex < 0 || selectedIndex >= filtered.size()) {
            return;
        }
        onPicked.accept(filtered.get(selectedIndex));
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

        graphics.fill(listX, listY, listX + listW, listY + listH, 0xFF373737);
        graphics.renderOutline(listX, listY, listW, listH, 0xFF555555);

        TextureAtlas atlas = Minecraft.getInstance().getModelManager().getAtlas(InventoryMenu.BLOCK_ATLAS);
        ResourceLocation missing = MissingTextureAtlasSprite.getLocation();

        int visibleRows = Math.max(1, listH / ROW_H);
        int maxScroll = Math.max(0, filtered.size() - visibleRows);
        scrollOffset = Math.min(scrollOffset, maxScroll);

        graphics.enableScissor(listX + 1, listY + 1, listX + listW - 1, listY + listH - 1);
        for (int row = 0; row < visibleRows; row++) {
            int idx = scrollOffset + row;
            if (idx >= filtered.size()) {
                break;
            }
            ResourceLocation id = filtered.get(idx);
            int ry = listY + row * ROW_H;
            boolean selected = idx == selectedIndex;
            boolean hover = mouseX >= listX && mouseX < listX + listW && mouseY >= ry && mouseY < ry + ROW_H
                    && mouseY < listY + listH;
            if (selected || hover) {
                graphics.fill(listX + 1, ry, listX + listW - 1, ry + ROW_H, selected ? 0xFF6B6B6B : 0xFF505050);
            }
            TextureAtlasSprite sprite = atlas.getSprite(id);
            if (sprite != null && !sprite.contents().name().equals(missing)) {
                graphics.blit(listX + 2, ry + 1, 0, 16, 16, sprite);
            }
            String text = id.toString();
            int textMax = listW - 24;
            if (this.font.width(text) > textMax) {
                text = this.font.plainSubstrByWidth(text, textMax - this.font.width("…")) + "…";
            }
            graphics.drawString(this.font, text, listX + 22, ry + 5, 0xFFE0E0E0, false);
        }
        graphics.disableScissor();

        for (var widget : this.renderables) {
            widget.render(graphics, mouseX, mouseY, partialTick);
        }

        graphics.pose().pushPose();
        graphics.pose().translate(0.0F, 0.0F, 400.0F);
        renderSuggestions(graphics, mouseX, mouseY);
        graphics.pose().popPose();
    }

    private void renderSuggestions(GuiGraphics graphics, int mouseX, int mouseY) {
        List<ResourceLocation> sug = suggestions();
        if (sug.isEmpty() || searchBox == null || !searchBox.isFocused()) {
            suggestHover = -1;
            return;
        }
        int sw = searchBox.getWidth();
        int sh = sug.size() * ROW_H + 4;
        int sx = searchBox.getX();
        int sy = searchBox.getY() - sh - 2;
        graphics.fill(sx, sy, sx + sw, sy + sh, 0xF0101020);
        graphics.renderOutline(sx, sy, sw, sh, 0xFFAA55FF);

        suggestHover = -1;
        for (int i = 0; i < sug.size(); i++) {
            int ry = sy + 2 + i * ROW_H;
            boolean hot = mouseX >= sx && mouseX < sx + sw && mouseY >= ry && mouseY < ry + ROW_H;
            if (hot) {
                suggestHover = i;
                graphics.fill(sx + 1, ry, sx + sw - 1, ry + ROW_H, 0xFF5A5A6A);
            }
            String text = sug.get(i).toString();
            if (this.font.width(text) > sw - 8) {
                text = this.font.plainSubstrByWidth(text, sw - 12) + "…";
            }
            graphics.drawString(this.font, text, sx + 4, ry + 5, hot ? 0xFFFFFF55 : 0xFFCCCCCC, false);
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
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && suggestHover >= 0) {
            List<ResourceLocation> sug = suggestions();
            if (suggestHover < sug.size()) {
                ResourceLocation chosen = sug.get(suggestHover);
                int idx = filtered.indexOf(chosen);
                if (idx >= 0) {
                    selectedIndex = idx;
                    searchBox.setValue(chosen.toString());
                    searchBox.moveCursorToEnd(false);
                }
                return true;
            }
        }
        if (button == 0 && mouseX >= listX && mouseX < listX + listW && mouseY >= listY && mouseY < listY + listH) {
            int row = (int) ((mouseY - listY) / ROW_H);
            int idx = scrollOffset + row;
            if (idx >= 0 && idx < filtered.size()) {
                selectedIndex = idx;
                clearSearchFocus();
                return true;
            }
        }
        boolean handled = super.mouseClicked(mouseX, mouseY, button);
        if (button == 0 && (searchBox == null || !searchBox.isMouseOver(mouseX, mouseY))) {
            clearSearchFocus();
        }
        return handled;
    }

    private void clearSearchFocus() {
        if (searchBox != null && searchBox.isFocused()) {
            searchBox.setFocused(false);
        }
        if (this.getFocused() == searchBox) {
            this.setFocused(null);
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (mouseX >= listX && mouseX < listX + listW && mouseY >= listY && mouseY < listY + listH) {
            int visibleRows = Math.max(1, listH / ROW_H);
            int maxScroll = Math.max(0, filtered.size() - visibleRows);
            scrollOffset = Math.max(0, Math.min(maxScroll, scrollOffset - (int) scrollY));
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
