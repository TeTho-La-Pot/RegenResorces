/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Position
 *  net.minecraft.core.Vec3i
 *  net.minecraft.core.registries.Registries
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.stats.Stats
 *  net.minecraft.util.Mth
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.item.ItemEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.enchantment.Enchantment
 *  net.minecraft.world.item.enchantment.EnchantmentHelper
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.LevelReader
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.level.storage.loot.LootParams$Builder
 *  net.minecraft.world.level.storage.loot.parameters.LootContextParams
 *  net.minecraft.world.phys.Vec3
 */
package com.github.TeThoLaPot.regen_resources.platform.forge.event;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Position;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;

final class RegenOreHarvest {
    private static final int SHORT_ITEM_PICKUP_DELAY = 2;

    private RegenOreHarvest() {
    }

    static boolean harvestAndRemove(ServerPlayer player, ServerLevel level, BlockPos pos, BlockState state) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        ItemStack tool = player.getMainHandItem();
        // ServerPlayerGameMode#destroyBlock と同様、適正ツールでない場合はドロップ・経験値・採掘統計を付与しない。
        boolean canHarvest = state.canHarvestBlock(level, pos, player);
        List<ItemStack> drops = List.of();
        if (canHarvest) {
            LootParams.Builder lootBuilder = new LootParams.Builder(level)
                    .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(pos))
                    .withParameter(LootContextParams.TOOL, tool)
                    .withOptionalParameter(LootContextParams.THIS_ENTITY, player)
                    .withOptionalParameter(LootContextParams.BLOCK_ENTITY, blockEntity);
            drops = state.getBlock().getDrops(state, lootBuilder);
        }
        if (!RegenOreHarvest.destroySilently(level, pos)) {
            return false;
        }
        tool.mineBlock((Level)level, state, pos, (Player)player);
        Vec3 spawn = player.position();
        if (canHarvest) {
            for (ItemStack drop : drops) {
                if (drop.isEmpty()) continue;
                RegenOreHarvest.spawnItemNearFeet(level, player, spawn, drop.copy());
            }
            int fortuneLevel = RegenOreHarvest.enchantLevel(level, tool, "minecraft:fortune");
            int silkTouchLevel = RegenOreHarvest.enchantLevel(level, tool, "minecraft:silk_touch");
            int exp = state.getExpDrop((LevelReader)level, level.random, pos, fortuneLevel, silkTouchLevel);
            if (exp > 0) {
                state.getBlock().popExperience(level, BlockPos.containing((Position)spawn), exp);
            }
            player.awardStat(Stats.BLOCK_MINED.get(state.getBlock()));
            player.causeFoodExhaustion(0.005f);
        }
        return true;
    }

    private static void spawnItemNearFeet(ServerLevel level, ServerPlayer player, Vec3 feet, ItemStack stack) {
        double x = feet.x;
        double y = feet.y + 0.05;
        double z = feet.z;
        ItemEntity entity = new ItemEntity((Level)level, x, y, z, stack);
        entity.setPickUpDelay(2);
        entity.setThrower(player.getUUID());
        float spread = 0.07f;
        float angle = level.random.nextFloat() * ((float)Math.PI * 2);
        entity.setDeltaMovement((double)(spread * Mth.sin((float)angle)), 0.06 + level.random.nextDouble() * 0.04, (double)(spread * Mth.cos((float)angle)));
        level.addFreshEntity((Entity)entity);
    }

    private static int enchantLevel(ServerLevel level, ItemStack tool, String enchantRl) {
        if (tool.isEmpty()) {
            return 0;
        }
        Enchantment enchantment = (Enchantment)level.registryAccess().registryOrThrow(Registries.ENCHANTMENT).get(ResourceLocation.parse((String)enchantRl));
        return enchantment != null ? EnchantmentHelper.getItemEnchantmentLevel((Enchantment)enchantment, (ItemStack)tool) : 0;
    }

    private static boolean destroySilently(ServerLevel level, BlockPos pos) {
        BlockState current = level.getBlockState(pos);
        if (current.isAir()) {
            return true;
        }
        return level.removeBlock(pos, false);
    }
}

