package com.github.TeThoLaPot.regen_resources.forge.loot;

import com.github.TeThoLaPot.regen_resources.RegenResources;
import com.mojang.serialization.Codec;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/** alpha の {@code recipe.ReLootModifiers} に相当する GLM 登録 */
public final class ReLootModifiers {

    public static final DeferredRegister<Codec<? extends IGlobalLootModifier>> LOOT_MODIFIER_SERIALIZERS =
            DeferredRegister.create(
                    ForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS,
                    RegenResources.MOD_ID);

    public static final RegistryObject<Codec<? extends IGlobalLootModifier>> ANCIENT_DEBRIS_MODIFIER =
            LOOT_MODIFIER_SERIALIZERS.register("ancient_debris_modifier", AncientDebrisModifier.CODEC);

    private ReLootModifiers() {}
}
