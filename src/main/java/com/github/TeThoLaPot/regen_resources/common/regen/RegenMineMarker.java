/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.nbt.CompoundTag
 */
package com.github.TeThoLaPot.regen_resources.common.regen;

import net.minecraft.nbt.CompoundTag;

public final class RegenMineMarker {
    public static final String TT_SOURCE = "rr_src";
    public static final String TT_SNAPSHOT = "rr_src_snap";
    public static final byte SRC_IMPLICIT = 0;
    public static final byte SRC_SURVIVAL = 1;
    public static final byte SRC_ELIGIBLE = 2;

    private RegenMineMarker() {
    }

    public static byte readSourceByte(CompoundTag d) {
        if (!d.contains(TT_SOURCE, 1)) {
            return 0;
        }
        return d.getByte(TT_SOURCE);
    }
}

