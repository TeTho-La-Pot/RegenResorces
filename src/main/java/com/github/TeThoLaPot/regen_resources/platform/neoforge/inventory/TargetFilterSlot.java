package com.github.TeThoLaPot.regen_resources.platform.neoforge.inventory;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * ターゲット用のゴースト枠。実アイテムは保持せず、メニュー側の表示用スタックを見せる。
 */
public final class TargetFilterSlot extends Slot {

    private final RegenTargetMenu owner;
    private final int visibleIndex;

    public TargetFilterSlot(RegenTargetMenu owner, int visibleIndex, int x, int y) {
        super(owner.filterContainer(), visibleIndex, x, y);
        this.owner = owner;
        this.visibleIndex = visibleIndex;
    }

    /** スクロール込みのターゲット配列インデックス */
    public int targetIndex() {
        return owner.targetIndexForVisible(visibleIndex);
    }

    public int visibleIndex() {
        return visibleIndex;
    }

    public RegenTargetMenu owner() {
        return owner;
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return false;
    }

    @Override
    public boolean mayPickup(Player player) {
        return false;
    }

    @Override
    public boolean isFake() {
        return true;
    }
}
