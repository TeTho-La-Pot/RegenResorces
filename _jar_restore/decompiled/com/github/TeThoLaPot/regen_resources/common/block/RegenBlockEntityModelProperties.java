/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraftforge.client.model.data.ModelProperty
 */
package com.github.TeThoLaPot.regen_resources.common.block;

import com.github.TeThoLaPot.regen_resources.common.block.RegenCustomVisualSpec;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.model.data.ModelProperty;

public final class RegenBlockEntityModelProperties {
    public static final ModelProperty<ResourceLocation> STRIPPED_BLOCK = new ModelProperty();
    public static final ModelProperty<RegenCustomVisualSpec> CUSTOM_VISUAL_SPEC = new ModelProperty();

    private RegenBlockEntityModelProperties() {
    }
}

