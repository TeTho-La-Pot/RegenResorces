package com.github.TeThoLaPot.regen_resources.forge.loot;

import com.google.common.base.Suppliers;
import com.github.TeThoLaPot.regen_resources.forge.RegenResourcesForgeConfig;
import com.github.TeThoLaPot.regen_resources.forge.item.Re_Items;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;

import java.util.function.Supplier;

/**
 * alpha の {@code AncientDebrisModifier} と同様に残骸ドロップを欠片へ差し替え。
 * シルクタッチは datapack の条件で修飾子スキップ。幸運はレベルごとに独立して 10% で欠片が 1 個増えるだけ。
 * 無効化は {@link RegenResourcesForgeConfig#CHANGE_ANCIENT_DEBRIS_DROPS}。
 */
public class AncientDebrisModifier extends LootModifier {

    public static final Supplier<Codec<AncientDebrisModifier>> CODEC =
            Suppliers.memoize(() -> RecordCodecBuilder.create(inst -> codecStart(inst).apply(inst, AncientDebrisModifier::new)));

    public AncientDebrisModifier(LootItemCondition[] conditionsIn) {
        super(conditionsIn);
    }

    /** 基底 4 + 幸運レベル N だけ N 回、各 10% で +1（期待値ごく小さめ）。 */
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
        int fortune = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.BLOCK_FORTUNE, tool);
        int count = fragmentCount(context.getRandom(), fortune);
        generatedLoot.clear();
        generatedLoot.add(new ItemStack(Re_Items.ANCIENT_FRAGMENT.get(), count));
        return generatedLoot;
    }

    @Override
    public Codec<? extends IGlobalLootModifier> codec() {
        return CODEC.get();
    }
}
