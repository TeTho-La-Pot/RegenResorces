package com.github.TeThoLaPot.regen_resources.common.regen;

import org.jetbrains.annotations.Nullable;

/**
 * サバイバル採掘後に再生シェル（TT・待機ブロック）を設置してよいか。
 * Forge のブイベントや Jade のサーバー同期などから共通利用する。
 */
public final class RegenMineEligibility {

    private RegenMineEligibility() {}

    /**
     * @param sourceMarker {@link RegenMineMarker#TT_SOURCE} の値（未設定は {@link RegenMineMarker#SRC_IMPLICIT}）
     * @param configAllowNaturalRegen 「自然・未マーク」を再生許可とみなすか（Forge コンフィグ）
     */
    public static boolean allowsAfterBreak(byte sourceMarker, boolean configAllowNaturalRegen) {
        return allowsAfterBreak(sourceMarker, configAllowNaturalRegen, null);
    }

    /**
     * @param ruleOverride プリセット JSON の {@code natural_regen}。未指定のときは従来どおり。
     */
    public static boolean allowsAfterBreak(
            byte sourceMarker, boolean configAllowNaturalRegen, @Nullable Boolean ruleOverride) {
        if (sourceMarker == RegenMineMarker.SRC_SURVIVAL) {
            return false;
        }
        if (sourceMarker == RegenMineMarker.SRC_ELIGIBLE) {
            return true;
        }
        if (ruleOverride != null) {
            return ruleOverride;
        }
        return configAllowNaturalRegen;
    }
}
