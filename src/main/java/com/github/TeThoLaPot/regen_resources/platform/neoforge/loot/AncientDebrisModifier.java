package com.github.TeThoLaPot.regen_resources.platform.neoforge.loot;

import com.google.common.base.Suppliers;
import com.github.TeThoLaPot.regen_resources.platform.neoforge.config.RegenResourcesForgeConfig;
import com.github.TeThoLaPot.regen_resources.platform.neoforge.item.Re_Items;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;

import java.util.function.Supplier;

/**
 * 残骸ドロップを欠片へ差し替え。幸運はレベルごとに独立して 10% で欠片が 1 個増えるだけ。
 * 無効化は {@link RegenResourcesForgeConfig#CHANGE_ANCIENT_DEBRIS_DROPS}。
 */
public class AncientDebrisModifier extends LootModifier {

    public static final Supplier<MapCodec<AncientDebrisModifier>> CODEC =
            Suppliers.memoize(() -> RecordCodecBuilder.mapCodec(inst -> codecStart(inst).apply(inst, AncientDebrisModifier::new)));

    public AncientDebrisModifier(LootItemCondition[] conditionsIn) {
        super(conditionsIn);
    }

    private static int fragmentCount(RandomSource rand, int fortuneLevel) {
        int count = 4;
        for (int i = 0; i < fortuneLevel; i++) {
            if (rand.nextFloat() < 0.10f) {
                count++;
            }
        }
        return Mth.clamp(count, 4, 64);
    }

    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        if (!RegenResourcesForgeConfig.CHANGE_ANCIENT_DEBRIS_DROPS.get()) {
            return generatedLoot;
        }
        ItemStack tool = context.getParamOrNull(LootContextParams.TOOL);
        int fortune =
                context.getLevel().registryAccess().lookupOrThrow(Registries.ENCHANTMENT)
                        .get(Enchantments.FORTUNE)
                        .map(h -> EnchantmentHelper.getItemEnchantmentLevel(h, tool))
                        .orElse(0);
        int count = fragmentCount(context.getRandom(), fortune);
        generatedLoot.clear();
        generatedLoot.add(new ItemStack(Re_Items.ANCIENT_FRAGMENT.get(), count));
        return generatedLoot;
    }

    @Override
    public MapCodec<? extends IGlobalLootModifier> codec() {
        return CODEC.get();
    }
}
