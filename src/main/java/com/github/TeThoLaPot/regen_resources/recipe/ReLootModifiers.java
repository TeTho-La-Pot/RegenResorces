package com.github.TeThoLaPot.regen_resources.recipe;

import com.github.TeThoLaPot.regen_resources.RegenConstants;
import com.mojang.serialization.Codec;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ReLootModifiers {
    public static final DeferredRegister<Codec<? extends IGlobalLootModifier>> LOOT_MODIFIER_SERIALIZERS =
            DeferredRegister.create(ForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, RegenConstants.MOD_ID);

    public static final RegistryObject<Codec<? extends IGlobalLootModifier>> ANCIENT_DEBRIS_MODIFIER =
            LOOT_MODIFIER_SERIALIZERS.register("ancient_debris_modifier", AncientDebrisModifier.CODEC);

    public static void register(IEventBus eventBus) {
        LOOT_MODIFIER_SERIALIZERS.register(eventBus);
    }
}
