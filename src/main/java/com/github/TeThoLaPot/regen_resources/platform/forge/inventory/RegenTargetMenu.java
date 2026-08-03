package com.github.TeThoLaPot.regen_resources.platform.forge.inventory;

import com.github.TeThoLaPot.regen_resources.common.regen.RegenTargetSpec;
import com.github.TeThoLaPot.regen_resources.platform.forge.menu.Re_Menus;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;

import java.util.ArrayList;
import java.util.List;

/**
 * ターゲット編集用メニュー（クライアント専用）。
 * <p>プレイヤースロットのクリックをサーバへ送ると inventoryMenu と食い違い、見た目と判定がずれる。
 * Screen 側でローカル操作のみ行うこと。
 * <p>フィルタ枠は 2×9 の表示窓。{@link #MAX_TARGETS} まで保持し、行スクロールで閲覧する。
 */
public final class RegenTargetMenu extends AbstractContainerMenu {

    /** 保持できるターゲット上限（表示窓より多い分はスクロール）。 */
    public static final int MAX_TARGETS = 54;
    public static final int FILTER_COLS = 9;
    public static final int FILTER_ROWS = 2;
    public static final int VISIBLE_SLOTS = FILTER_COLS * FILTER_ROWS;

    /** バニラ3段チェストと同じ座標（image 176x166）。 */
    public static final int FILTER_SLOT_Y0 = 18;
    public static final int PLAYER_INV_Y0 = 84;
    public static final int HOTBAR_Y = 142;

    private final List<RegenTargetSpec> targets = new ArrayList<>();
    private final SimpleContainer filterContainer = new SimpleContainer(VISIBLE_SLOTS);
    private int filterScrollRows;

    public RegenTargetMenu(int containerId, Inventory playerInv) {
        this(containerId, playerInv, List.of());
    }

    public RegenTargetMenu(int containerId, Inventory playerInv, List<RegenTargetSpec> initial) {
        super(Re_Menus.TARGET_EDIT.get(), containerId);
        if (initial != null) {
            for (RegenTargetSpec t : initial) {
                if (t != null && targets.size() < MAX_TARGETS) {
                    targets.add(t);
                }
            }
        }
        syncFilterItems();

        for (int i = 0; i < VISIBLE_SLOTS; i++) {
            int col = i % FILTER_COLS;
            int row = i / FILTER_COLS;
            this.addSlot(new TargetFilterSlot(this, i, 8 + col * 18, FILTER_SLOT_Y0 + row * 18));
        }

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInv, col + (row + 1) * 9, 8 + col * 18, PLAYER_INV_Y0 + row * 18) {
                    @Override
                    public boolean mayPickup(Player player) {
                        return false;
                    }

                    @Override
                    public boolean mayPlace(ItemStack stack) {
                        return false;
                    }
                });
            }
        }
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInv, col, 8 + col * 18, HOTBAR_Y) {
                @Override
                public boolean mayPickup(Player player) {
                    return false;
                }

                @Override
                public boolean mayPlace(ItemStack stack) {
                    return false;
                }
            });
        }
    }

    public Container filterContainer() {
        return filterContainer;
    }

    public List<RegenTargetSpec> targets() {
        return targets;
    }

    public int filterScrollRows() {
        return filterScrollRows;
    }

    public int maxFilterScrollRows() {
        int occupied = targets.size();
        // 空き1枠を見せる（上限未満のとき）
        int slotsNeeded = Math.min(MAX_TARGETS, occupied < MAX_TARGETS ? occupied + 1 : occupied);
        int rowsNeeded = Math.max(FILTER_ROWS, (slotsNeeded + FILTER_COLS - 1) / FILTER_COLS);
        return Math.max(0, rowsNeeded - FILTER_ROWS);
    }

    public boolean setFilterScrollRows(int rows) {
        int next = Mth.clamp(rows, 0, maxFilterScrollRows());
        if (next == filterScrollRows) {
            return false;
        }
        filterScrollRows = next;
        syncFilterItems();
        return true;
    }

    public boolean scrollFilter(int rowDelta) {
        return setFilterScrollRows(filterScrollRows - rowDelta);
    }

    /** 表示スロット番号 → ターゲット配列インデックス */
    public int targetIndexForVisible(int visibleSlot) {
        return filterScrollRows * FILTER_COLS + visibleSlot;
    }

    public void clampScroll() {
        int max = maxFilterScrollRows();
        if (filterScrollRows > max) {
            filterScrollRows = max;
            syncFilterItems();
        }
    }

    public void addTarget(RegenTargetSpec spec) {
        if (spec != null && targets.size() < MAX_TARGETS) {
            targets.add(spec);
            // 追加した枠が見えるよう末尾へ
            int row = (targets.size() - 1) / FILTER_COLS;
            setFilterScrollRows(Math.max(0, row - FILTER_ROWS + 1));
            syncFilterItems();
        }
    }

    public void setTargetAt(int index, RegenTargetSpec spec) {
        if (spec == null) {
            return;
        }
        if (index >= 0 && index < targets.size()) {
            targets.set(index, spec);
        } else if (targets.size() < MAX_TARGETS) {
            targets.add(spec);
        }
        clampScroll();
        syncFilterItems();
    }

    public void replaceTarget(int index, RegenTargetSpec spec) {
        if (spec != null && index >= 0 && index < targets.size()) {
            targets.set(index, spec);
            syncFilterItems();
        }
    }

    public void removeTarget(int index) {
        if (index >= 0 && index < targets.size()) {
            targets.remove(index);
            clampScroll();
            syncFilterItems();
        }
    }

    public void clearTargets() {
        targets.clear();
        filterScrollRows = 0;
        syncFilterItems();
    }

    public void clearCarried() {
        this.setCarried(ItemStack.EMPTY);
    }

    public void syncFilterItems() {
        for (int i = 0; i < VISIBLE_SLOTS; i++) {
            int ti = targetIndexForVisible(i);
            if (ti < targets.size()) {
                filterContainer.setItem(i, iconFor(targets.get(ti)));
            } else {
                filterContainer.setItem(i, ItemStack.EMPTY);
            }
        }
    }

    private static ItemStack iconFor(RegenTargetSpec spec) {
        if (spec.isTag()) {
            return new ItemStack(Items.NAME_TAG);
        }
        Block block = BuiltInRegistries.BLOCK.get(spec.id());
        return block == null ? ItemStack.EMPTY : new ItemStack(block);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }
}
