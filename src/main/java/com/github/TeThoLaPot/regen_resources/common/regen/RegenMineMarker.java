package com.github.TeThoLaPot.regen_resources.common.regen;

import net.minecraft.nbt.CompoundTag;

/**
 * 再生対象ブロック座標にだけ載せる軽量フラグ（設置時のみマージ）。
 */
public final class RegenMineMarker {

    /** 設置由来。省略時は「自然生成など／未マーク」としてコンフィグに従う。 */
    public static final String TT_SOURCE = "rr_src";
    /** 再生タスク NBT 内で {@link #TT_SOURCE} を復元後に戻すための退避。 */
    public static final String TT_SNAPSHOT = "rr_src_snap";

    /** {@link #TT_SOURCE} 未設定相当（読み取り専用論理値）。 */
    public static final byte SRC_IMPLICIT = 0;
    /** サバイバル設置 → 再生しない。 */
    public static final byte SRC_SURVIVAL = 1;
    /** クリエイティブ設置 or コマンド類似設置 → 再生する。 */
    public static final byte SRC_ELIGIBLE = 2;

    private RegenMineMarker() {}

    public static byte readSourceByte(CompoundTag d) {
        if (!d.contains(TT_SOURCE, CompoundTag.TAG_BYTE)) {
            return SRC_IMPLICIT;
        }
        return d.getByte(TT_SOURCE);
    }
}
