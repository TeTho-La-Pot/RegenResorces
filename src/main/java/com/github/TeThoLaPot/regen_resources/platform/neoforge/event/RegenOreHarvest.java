package com.github.TeThoLaPot.regen_resources.platform.neoforge.event;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * 再生対象鉱石は loot を評価し、ドロップと経験値をプレイヤー足元に出す。
 */
final class RegenOreHarvest {

    private static final int SHORT_ITEM_PICKUP_DELAY = 2;

    private RegenOreHarvest() {}

    static boolean harvestAndRemove(ServerPlayer player, ServerLevel level, BlockPos pos, BlockState state) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        ItemStack tool = player.getMainHandItem();

        // ServerPlayerGameMode#destroyBlock と同様、適正ツールでない場合はドロップ・経験値・採掘統計を付与しない。
        boolean canHarvest = state.canHarvestBlock(level, pos, player);
        List<ItemStack> drops =
                canHarvest ? Block.getDrops(state, level, pos, blockEntity, player, tool) : List.of();

        if (!destroySilently(level, pos)) {
            return false;
        }

        tool.mineBlock(level, state, pos, player);

        Vec3 spawn = player.position();

        if (canHarvest) {
            for (ItemStack drop : drops) {
                if (!drop.isEmpty()) {
                    spawnItemNearFeet(level, player, spawn, drop.copy());
                }
            }

            int rawExp = state.getExpDrop(level, pos, blockEntity, player, tool);
            int exp = EnchantmentHelper.processBlockExperience(level, tool, rawExp);
            if (exp > 0) {
                state.getBlock().popExperience(level, BlockPos.containing(spawn), exp);
            }

            player.awardStat(Stats.BLOCK_MINED.get(state.getBlock()));
            player.causeFoodExhaustion(0.005F);
        }

        return true;
    }

    private static void spawnItemNearFeet(ServerLevel level, ServerPlayer player, Vec3 feet, ItemStack stack) {
        double x = feet.x;
        double y = feet.y + 0.05;
        double z = feet.z;
        ItemEntity entity = new ItemEntity(level, x, y, z, stack);
        entity.setPickUpDelay(SHORT_ITEM_PICKUP_DELAY);
        entity.setThrower(player);
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
        return level.registryAccess()
                .registryOrThrow(Registries.ENCHANTMENT)
                .getHolder(ResourceKey.create(Registries.ENCHANTMENT, ResourceLocation.parse(enchantRl)))
                .map(h -> EnchantmentHelper.getItemEnchantmentLevel(h, tool))
                .orElse(0);
    }

    private static boolean destroySilently(ServerLevel level, BlockPos pos) {
        BlockState current = level.getBlockState(pos);
        if (current.isAir()) {
            return true;
        }
        return level.removeBlock(pos, false);
    }
}
