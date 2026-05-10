package com.github.TeThoLaPot.regen_resources.platform.neoforge;

import com.github.TeThoLaPot.regen_resources.platform.neoforge.item.Re_Items;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * 他タブへ混入する「再生シェル」アイテムを除去する（{@link com.github.TeThoLaPot.regen_resources.common.item.RegenBlockItem} の二重防御）。
 */
public final class RegenCreativeTabEvents {

    private RegenCreativeTabEvents() {}

    public static void stripRegenShellItem(BuildCreativeModeTabContentsEvent event) {
        var shellItem = Re_Items.REGEN_BLOCK_ITEM.get();
        // 反復中に集合を変更しないよう、参照を先に集める
        List<ItemStack> toRemove = new ArrayList<>();
        for (ItemStack s : event.getParentEntries()) {
            if (!s.isEmpty() && s.is(shellItem)) {
                toRemove.add(s);
            }
        }
        for (ItemStack s : event.getSearchEntries()) {
            if (!s.isEmpty() && s.is(shellItem) && !toRemove.contains(s)) {
                toRemove.add(s);
            }
        }
        for (ItemStack s : toRemove) {
            event.remove(s, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
        }
    }
}
