package com.github.TeThoLaPot.regen_resources.common.block;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.model.data.ModelProperty;

public final class RegenBlockEntityModelProperties {
    public static final ModelProperty<ResourceLocation> STRIPPED_BLOCK = new ModelProperty<>();
    public static final ModelProperty<RegenCustomVisualSpec> CUSTOM_VISUAL_SPEC = new ModelProperty<>();

    private RegenBlockEntityModelProperties() {
    }
}

