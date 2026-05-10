/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.Direction
 *  net.minecraft.util.StringRepresentable
 *  org.jetbrains.annotations.Nullable
 */
package com.github.TeThoLaPot.regen_resources.common.block;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.Nullable;

public enum RegenTemplate implements StringRepresentable
{
    CUBE_ALL("cube_all", "all"),
    CUBE_COLUMN("cube_column", "end", "side"),
    CUBE_BOTTOM_TOP("cube_bottom_top", "top", "side", "bottom"),
    CUBE("cube", "up", "down", "north", "south", "east", "west");

    private final String id;
    private final Set<String> slots;

    private RegenTemplate(String id, String ... slots) {
        this.id = id;
        this.slots = Collections.unmodifiableSet(new LinkedHashSet<String>(Arrays.asList(slots)));
    }

    public String getSerializedName() {
        return this.id;
    }

    public Set<String> slots() {
        return this.slots;
    }

    public String slotForFace(Direction face) {
        return switch (this) {
            default -> throw new IncompatibleClassChangeError();
            case CUBE_ALL -> "all";
            case CUBE_COLUMN -> {
                if (face == Direction.UP || face == Direction.DOWN) {
                    yield "end";
                }
                yield "side";
            }
            case CUBE_BOTTOM_TOP -> {
                switch (face) {
                    case UP: {
                        yield "top";
                    }
                    case DOWN: {
                        yield "bottom";
                    }
                }
                yield "side";
            }
            case CUBE -> face.getName();
        };
    }

    @Nullable
    public static RegenTemplate fromSerializedName(@Nullable String name) {
        if (name == null || name.isBlank()) {
            return null;
        }
        String t = name.trim();
        for (RegenTemplate r : RegenTemplate.values()) {
            if (!r.id.equalsIgnoreCase(t) && !r.name().equalsIgnoreCase(t)) continue;
            return r;
        }
        return null;
    }
}

