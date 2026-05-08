package com.github.TeThoLaPot.regen_resources.platform.forge.event;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * 再生対象鉱石は loot を評価し、ドロップと経験値をプレイヤー足元に出す。
 * アイテムはピックアップ遅延を短くし、インベントリに余裕があるときはほぼ即拾い（疑似的な直接収納）。
 * 満杯時は地面に残るため、インベントリへの直接挿入は行わない。
 */
final class RegenOreHarvest {

    /** バニラのブロックドロップ既定（10 tick）より短くする */
    private static final int SHORT_ITEM_PICKUP_DELAY = 2;

    private RegenOreHarvest() {}

    /**
     * サーバー側でブロックを「掘った結果」と同等にしつつ除去する。
     *
     * @return {@code false} のとき呼び出し側がイベント処理を続けられるようにする（フェイルセーフ）。
     */
    static boolean harvestAndRemove(ServerPlayer player, ServerLevel level, BlockPos pos, BlockState state) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        ItemStack tool = player.getMainHandItem();

        LootParams.Builder lootBuilder =
                new LootParams.Builder(level)
                        .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(pos))
                        .withParameter(LootContextParams.TOOL, tool)
                        .withOptionalParameter(LootContextParams.THIS_ENTITY, player)
                        .withOptionalParameter(LootContextParams.BLOCK_ENTITY, blockEntity);

        List<ItemStack> drops = state.getBlock().getDrops(state, lootBuilder);

        if (!destroySilently(level, pos)) {
            return false;
        }

        tool.mineBlock(level, state, pos, player);

        Vec3 spawn = player.position();

        for (ItemStack drop : drops) {
            if (!drop.isEmpty()) {
                spawnItemNearFeet(level, player, spawn, drop.copy());
            }
        }

        int fortuneLevel = enchantLevel(level, tool, "minecraft:fortune");
        int silkTouchLevel = enchantLevel(level, tool, "minecraft:silk_touch");
        int exp = state.getExpDrop(level, level.random, pos, fortuneLevel, silkTouchLevel);
        if (exp > 0) {
            state.getBlock().popExperience(level, BlockPos.containing(spawn), exp);
        }

        player.awardStat(Stats.BLOCK_MINED.get(state.getBlock()));
        player.causeFoodExhaustion(0.005F);

        return true;
    }

    private static void spawnItemNearFeet(ServerLevel level, ServerPlayer player, Vec3 feet, ItemStack stack) {
        double x = feet.x;
        double y = feet.y + 0.05;
        double z = feet.z;
        ItemEntity entity = new ItemEntity(level, x, y, z, stack);
        entity.setPickUpDelay(SHORT_ITEM_PICKUP_DELAY);
        entity.setThrower(player.getUUID());
        float spread = 0.07F;
        float angle = level.random.nextFloat() * ((float) Math.PI * 2);
        entity.setDeltaMovement(
                spread * Mth.sin(angle), 0.06 + level.random.nextDouble() * 0.04, spread * Mth.cos(angle));
        level.addFreshEntity(entity);
    }

    private static int enchantLevel(ServerLevel level, ItemStack tool, String enchantRl) {
        if (tool.isEmpty()) {
            return 0;
        }
        Enchantment enchantment =
                level.registryAccess().registryOrThrow(Registries.ENCHANTMENT).get(ResourceLocation.parse(enchantRl));
        return enchantment != null ? EnchantmentHelper.getItemEnchantmentLevel(enchantment, tool) : 0;
    }

    /** BreakEvent を再発火させずにブロックだけ除去する（ドロップは生成しない）。 */
    private static boolean destroySilently(ServerLevel level, BlockPos pos) {
        BlockState current = level.getBlockState(pos);
        if (current.isAir()) {
            return true;
        }
        return level.removeBlock(pos, false);
    }
}

