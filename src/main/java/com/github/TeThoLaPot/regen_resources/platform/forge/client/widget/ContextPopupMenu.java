package com.github.TeThoLaPot.regen_resources.platform.forge.client.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import java.util.ArrayList;
import java.util.List;

/** 右クリック用の横並びコンテキストメニュー（ホバー／ホイールで選択、クリックで実行）。 */
public final class ContextPopupMenu {

    public record Item(Component label, Runnable action) {}

    private final List<Item> items = new ArrayList<>();
    private int x;
    private int y;
    private boolean open;
    private int width;
    private int height;
    private int itemW;
    private int selectedIndex;
    /** ホイール操作後は、マウス位置で選択を上書きしない。 */
    private boolean wheelArmed;

    private static final int PAD_X = 6;
    private static final int PAD_Y = 4;
    private static final int ITEM_H = 16;
    private static final int GAP = 2;
    private static final int OUTER = 3;

    public void open(int mouseX, int mouseY, List<Item> next) {
        items.clear();
        items.addAll(next);
        open = !items.isEmpty();
        selectedIndex = 0;
        wheelArmed = false;
        if (!open) {
            return;
        }
        var font = Minecraft.getInstance().font;
        int maxLabel = 32;
        for (Item it : items) {
            maxLabel = Math.max(maxLabel, font.width(it.label()));
        }
        itemW = maxLabel + PAD_X * 2;
        width = OUTER * 2 + items.size() * itemW + Math.max(0, items.size() - 1) * GAP;
        height = OUTER * 2 + ITEM_H;
        x = mouseX;
        y = mouseY;
        int screenW = Minecraft.getInstance().getWindow().getGuiScaledWidth();
        int screenH = Minecraft.getInstance().getWindow().getGuiScaledHeight();
        if (x + width > screenW) {
            x = Math.max(0, screenW - width);
        }
        if (y + height > screenH) {
            y = Math.max(0, screenH - height);
        }
    }

    public void close() {
        open = false;
        items.clear();
        selectedIndex = 0;
        wheelArmed = false;
    }

    public boolean isOpen() {
        return open;
    }

    public boolean isMouseOver(double mouseX, double mouseY) {
        return open && mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }

    public void render(GuiGraphics graphics, int mouseX, int mouseY) {
        if (!open) {
            return;
        }
        if (!isMouseOver(mouseX, mouseY)) {
            wheelArmed = false;
        } else if (!wheelArmed) {
            int under = indexAt(mouseX, mouseY);
            if (under >= 0) {
                selectedIndex = under;
            }
        }
        var font = Minecraft.getInstance().font;
        graphics.fill(x, y, x + width, y + height, 0xF0181828);
        graphics.renderOutline(x, y, width, height, 0xFFE0E0E0);

        int ix = x + OUTER;
        int iy = y + OUTER;
        for (int i = 0; i < items.size(); i++) {
            Item it = items.get(i);
            boolean hot = i == selectedIndex;
            graphics.fill(ix, iy, ix + itemW, iy + ITEM_H, hot ? 0xFF5A5A6A : 0xFF2A2A38);
            graphics.renderOutline(ix, iy, itemW, ITEM_H, hot ? 0xFFFFFFFF : 0xFF888898);
            int textX = ix + (itemW - font.width(it.label())) / 2;
            graphics.drawString(font, it.label(), textX, iy + (ITEM_H - 8) / 2, hot ? 0xFFFFFF : 0xFFE0E0E0, false);
            ix += itemW + GAP;
        }
    }

    private int indexAt(double mouseX, double mouseY) {
        if (mouseX < x + OUTER || mouseY < y + OUTER || mouseX >= x + width - OUTER || mouseY >= y + height - OUTER) {
            return -1;
        }
        int localX = (int) mouseX - x - OUTER;
        int stride = itemW + GAP;
        int idx = localX / stride;
        int within = localX % stride;
        if (within >= itemW || idx < 0 || idx >= items.size()) {
            return -1;
        }
        return idx;
    }

    /** @return true if consumed */
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!open) {
            return false;
        }
        if (isMouseOver(mouseX, mouseY) && !items.isEmpty()) {
            int idx = wheelArmed ? selectedIndex : indexAt(mouseX, mouseY);
            if (idx < 0) {
                idx = selectedIndex;
            }
            if (idx >= 0 && idx < items.size()) {
                Item it = items.get(idx);
                close();
                it.action().run();
                return true;
            }
        }
        close();
        return true;
    }

    /** メニュー上でホイール → 編集／タグ切り替えなどを巡回。 */
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (!open || items.isEmpty() || !isMouseOver(mouseX, mouseY)) {
            return false;
        }
        int step = delta > 0 ? -1 : 1;
        int next = selectedIndex + step;
        if (next < 0 || next >= items.size()) {
            return true;
        }
        selectedIndex = next;
        wheelArmed = true;
        return true;
    }
}
