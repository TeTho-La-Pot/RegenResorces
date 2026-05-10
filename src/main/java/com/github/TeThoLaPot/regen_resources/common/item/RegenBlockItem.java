package com.github.TeThoLaPot.regen_resources.common.item;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;

/**
 * 世界内の再生シェル用ブロックアイテム。
 * 1.21 ではクリエイティブの生成 API が変わっているため、タブから除外する処理は
 * {@link com.github.TeThoLaPot.regen_resources.platform.neoforge.RegenCreativeTabEvents} に任せる。
 */
public final class RegenBlockItem extends BlockItem {
    public RegenBlockItem(Block block, Properties properties) {
        super(block, properties);
    }
}
