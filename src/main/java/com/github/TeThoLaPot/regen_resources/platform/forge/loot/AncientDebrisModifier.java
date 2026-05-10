/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.Suppliers
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  com.mojang.serialization.codecs.RecordCodecBuilder$Instance
 *  it.unimi.dsi.fastutil.objects.ObjectArrayList
 *  net.minecraft.util.Mth
 *  net.minecraft.util.RandomSource
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.enchantment.Enchantment
 *  net.minecraft.world.item.enchantment.EnchantmentHelper
 *  net.minecraft.world.item.enchantment.Enchantments
 *  net.minecraft.world.level.ItemLike
 *  net.minecraft.world.level.storage.loot.LootContext
 *  net.minecraft.world.level.storage.loot.parameters.LootContextParams
 *  net.minecraft.world.level.storage.loot.predicates.LootItemCondition
 *  net.minecraftforge.common.loot.IGlobalLootModifier
 *  net.minecraftforge.common.loot.LootModifier
 */
package com.github.TeThoLaPot.regen_resources.platform.forge.loot;

import com.github.TeThoLaPot.regen_resources.platform.forge.config.RegenResourcesForgeConfig;
import com.github.TeThoLaPot.regen_resources.platform.forge.item.Re_Items;
import com.google.common.base.Suppliers;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.function.Supplier;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;

public class AncientDebrisModifier
extends LootModifier {
    public static final Supplier<Codec<AncientDebrisModifier>> CODEC = Suppliers.memoize(() ->
            RecordCodecBuilder.create(inst -> LootModifier.codecStart(inst).apply(inst, AncientDebrisModifier::new)));

    public AncientDebrisModifier(LootItemCondition[] conditionsIn) {
        super(conditionsIn);
    }

    private static int fragmentCount(RandomSource rand, int fortuneLevel) {
        int count = 4;
        for (int i = 0; i < fortuneLevel; ++i) {
            if (!(rand.nextFloat() < 0.1f)) continue;
            ++count;
        }
        return Mth.clamp((int)count, (int)4, (int)64);
    }

    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        if (!((Boolean)RegenResourcesForgeConfig.CHANGE_ANCIENT_DEBRIS_DROPS.get()).booleanValue()) {
            return generatedLoot;
        }
        ItemStack tool = (ItemStack)context.getParamOrNull(LootContextParams.TOOL);
        int fortune = EnchantmentHelper.getItemEnchantmentLevel((Enchantment)Enchantments.BLOCK_FORTUNE, (ItemStack)tool);
        int count = AncientDebrisModifier.fragmentCount(context.getRandom(), fortune);
        generatedLoot.clear();
        generatedLoot.add(new ItemStack((ItemLike)Re_Items.ANCIENT_FRAGMENT.get(), count));
        return generatedLoot;
    }

    public Codec<? extends IGlobalLootModifier> codec() {
        return CODEC.get();
    }
}

