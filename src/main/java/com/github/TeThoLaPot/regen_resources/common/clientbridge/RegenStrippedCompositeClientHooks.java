package com.github.TeThoLaPot.regen_resources.common.clientbridge;

import java.util.Objects;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

/**
 * クライアント専用の合成ストリップ見た目ウォームアップは common モジュールから直接参照できないため、{@link #install}
 * で登録する。
 */
public final class RegenStrippedCompositeClientHooks {

    @FunctionalInterface
    public interface Impl {
        void scheduleWarm(BlockPos pos, ResourceLocation strippedBlockId);
    }

    private static Impl impl = (p, id) -> {};

    private RegenStrippedCompositeClientHooks() {}

    public static synchronized void install(@Nullable Impl i) {
        impl = i != null ? i : (p, id) -> {};
    }

    /** ログ／ stem 側面合成スプライトのクォードキャッシュ構築をメインスレッドにディファーして予約する。 */
    public static void scheduleWarm(BlockPos pos, @Nullable ResourceLocation strippedBlockId) {
        if (strippedBlockId == null) {
            return;
        }
        Objects.requireNonNull(pos, "pos");
        impl.scheduleWarm(pos.immutable(), strippedBlockId);
    }
}
