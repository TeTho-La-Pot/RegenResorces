package com.github.TeThoLaPot.regen_resources.platform.forge.client.widget;

import com.github.TeThoLaPot.regen_resources.common.regen.RegenTargetSpec;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/** Create 風: ホバー中にターゲットのアイコンを一定間隔で切り替えるプレビュー。 */
public final class TargetCycleHoverPreview {

    private static final long CYCLE_MS = 900L;
    private static final int PANEL_PAD = 4;
    private static final int ICON = 16;
    private static final int PANEL_W = PANEL_PAD * 2 + ICON + 2;
    private static final int PANEL_H = PANEL_PAD * 2 + ICON + 2;
    private static final int MAX_TAG_SAMPLES = 24;

    private final List<ItemStack> icons = new ArrayList<>();
    private long hoverStartMs = -1L;

    public void setFromTargets(List<RegenTargetSpec> targets) {
        icons.clear();
        hoverStartMs = -1L;
        if (targets == null) {
            return;
        }
        Set<Item> seen = new LinkedHashSet<>();
        for (RegenTargetSpec spec : targets) {
            if (spec == null) {
                continue;
            }
            if (spec.isTag()) {
                appendTagSamples(spec, seen);
            } else {
                Block block = BuiltInRegistries.BLOCK.get(spec.id());
                if (block != null && !block.defaultBlockState().isAir()) {
                    addUnique(new ItemStack(block), seen);
                }
            }
        }
    }

    private void appendTagSamples(RegenTargetSpec spec, Set<Item> seen) {
        TagKey<Block> key = TagKey.create(Registries.BLOCK, spec.id());
        Optional<HolderSet.Named<Block>> tag = BuiltInRegistries.BLOCK.getTag(key);
        if (tag.isEmpty()) {
            addUnique(new ItemStack(Items.NAME_TAG), seen);
            return;
        }
        int n = 0;
        for (Holder<Block> holder : tag.get()) {
            Block block = holder.value();
            if (block.defaultBlockState().isAir()) {
                continue;
            }
            if (addUnique(new ItemStack(block), seen)) {
                if (++n >= MAX_TAG_SAMPLES) {
                    break;
                }
            }
        }
        if (n == 0) {
            addUnique(new ItemStack(Items.NAME_TAG), seen);
        }
    }

    private boolean addUnique(ItemStack stack, Set<Item> seen) {
        if (!seen.add(stack.getItem())) {
            return false;
        }
        icons.add(stack);
        return true;
    }

    public boolean isEmpty() {
        return icons.isEmpty();
    }

    public void resetHover() {
        hoverStartMs = -1L;
    }

    /**
     * アンカー付近にアイコンのみのプレビュー枠を描く。ホバー中のみ。
     *
     * @param anchorX アンカー左
     * @param anchorY アンカー上
     * @param anchorW アンカー幅
     */
    public void renderNearAnchor(GuiGraphics graphics, boolean hovered, int anchorX, int anchorY, int anchorW) {
        if (!hovered || icons.isEmpty()) {
            hoverStartMs = -1L;
            return;
        }
        long now = Util.getMillis();
        if (hoverStartMs < 0L) {
            hoverStartMs = now;
        }
        int idx = (int) (((now - hoverStartMs) / CYCLE_MS) % icons.size());
        ItemStack icon = icons.get(idx);

        int px = anchorX + (anchorW - PANEL_W) / 2;
        int py = anchorY - PANEL_H - 4;
        if (py < 2) {
            py = anchorY + 20;
        }

        graphics.fill(px, py, px + PANEL_W, py + PANEL_H, 0xF0101018);
        graphics.renderOutline(px, py, PANEL_W, PANEL_H, 0xFF8B8B8B);
        int sx = px + (PANEL_W - ICON) / 2;
        int sy = py + PANEL_PAD;
        graphics.fill(sx - 1, sy - 1, sx + ICON + 1, sy + ICON + 1, 0xFF373737);
        graphics.renderOutline(sx - 1, sy - 1, ICON + 2, ICON + 2, 0xFF555555);
        graphics.renderItem(icon, sx, sy);
    }
}
