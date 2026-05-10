/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 *  net.minecraft.resources.ResourceKey
 *  net.minecraftforge.common.loot.IGlobalLootModifier
 *  net.minecraftforge.registries.DeferredRegister
 *  net.minecraftforge.registries.ForgeRegistries$Keys
 *  net.minecraftforge.registries.RegistryObject
 */
package com.github.TeThoLaPot.regen_resources.platform.forge.loot;

import com.github.TeThoLaPot.regen_resources.platform.forge.loot.AncientDebrisModifier;
import com.mojang.serialization.Codec;
import net.minecraft.resources.ResourceKey;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ReLootModifiers {
    public static final DeferredRegister<Codec<? extends IGlobalLootModifier>> LOOT_MODIFIER_SERIALIZERS = DeferredRegister.create((ResourceKey)ForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, (String)"regen_resources");
    public static final RegistryObject<Codec<? extends IGlobalLootModifier>> ANCIENT_DEBRIS_MODIFIER = LOOT_MODIFIER_SERIALIZERS.register("ancient_debris_modifier", AncientDebrisModifier.CODEC);

    private ReLootModifiers() {
    }
}

