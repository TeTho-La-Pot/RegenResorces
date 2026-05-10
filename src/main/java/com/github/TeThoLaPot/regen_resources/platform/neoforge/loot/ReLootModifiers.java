package com.github.TeThoLaPot.regen_resources.platform.neoforge.loot;

import com.github.TeThoLaPot.regen_resources.RegenResources;
import com.mojang.serialization.MapCodec;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

/** GLM（古代の残骸ドロップ差し替え等）の登録。 */
public final class ReLootModifiers {

    public static final DeferredRegister<MapCodec<? extends IGlobalLootModifier>> LOOT_MODIFIER_SERIALIZERS =
            DeferredRegister.create(NeoForgeRegistries.GLOBAL_LOOT_MODIFIER_SERIALIZERS, RegenResources.MOD_ID);

    public static final DeferredHolder<MapCodec<? extends IGlobalLootModifier>, MapCodec<AncientDebrisModifier>> ANCIENT_DEBRIS_MODIFIER =
            LOOT_MODIFIER_SERIALIZERS.register("ancient_debris_modifier", () -> AncientDebrisModifier.CODEC.get());

    private ReLootModifiers() {}
}
