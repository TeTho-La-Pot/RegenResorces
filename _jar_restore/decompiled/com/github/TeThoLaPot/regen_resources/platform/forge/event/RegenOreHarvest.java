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
        int silkTouchLevel;
        BlockEntity blockEntity = level.m_7702_(pos);
        ItemStack tool = player.m_21205_();
        LootParams.Builder lootBuilder = new LootParams.Builder(level).m_287286_(LootContextParams.f_81460_, (Object)Vec3.m_82512_((Vec3i)pos)).m_287286_(LootContextParams.f_81463_, (Object)tool).m_287289_(LootContextParams.f_81455_, (Object)player).m_287289_(LootContextParams.f_81462_, (Object)blockEntity);
        List drops = state.m_60734_().m_49635_(state, lootBuilder);
        if (!RegenOreHarvest.destroySilently(level, pos)) {
            return false;
        }
        tool.m_41686_((Level)level, state, pos, (Player)player);
        Vec3 spawn = player.m_20182_();
        for (ItemStack drop : drops) {
            if (drop.m_41619_()) continue;
            RegenOreHarvest.spawnItemNearFeet(level, player, spawn, drop.m_41777_());
        }
        int fortuneLevel = RegenOreHarvest.enchantLevel(level, tool, "minecraft:fortune");
        int exp = state.getExpDrop((LevelReader)level, level.f_46441_, pos, fortuneLevel, silkTouchLevel = RegenOreHarvest.enchantLevel(level, tool, "minecraft:silk_touch"));
        if (exp > 0) {
            state.m_60734_().m_49805_(level, BlockPos.m_274446_((Position)spawn), exp);
        }
        player.m_36246_(Stats.f_12949_.m_12902_((Object)state.m_60734_()));
        player.m_36399_(0.005f);
        return true;
    }

    private static void spawnItemNearFeet(ServerLevel level, ServerPlayer player, Vec3 feet, ItemStack stack) {
        double x = feet.f_82479_;
        double y = feet.f_82480_ + 0.05;
        double z = feet.f_82481_;
        ItemEntity entity = new ItemEntity((Level)level, x, y, z, stack);
        entity.m_32010_(2);
        entity.m_32052_(player.m_20148_());
        float spread = 0.07f;
        float angle = level.f_46441_.m_188501_() * ((float)Math.PI * 2);
        entity.m_20334_((double)(spread * Mth.m_14031_((float)angle)), 0.06 + level.f_46441_.m_188500_() * 0.04, (double)(spread * Mth.m_14089_((float)angle)));
        level.m_7967_((Entity)entity);
    }

    private static int enchantLevel(ServerLevel level, ItemStack tool, String enchantRl) {
        if (tool.m_41619_()) {
            return 0;
        }
        Enchantment enchantment = (Enchantment)level.m_9598_().m_175515_(Registries.f_256762_).m_7745_(ResourceLocation.parse((String)enchantRl));
        return enchantment != null ? EnchantmentHelper.m_44843_((Enchantment)enchantment, (ItemStack)tool) : 0;
    }

    private static boolean destroySilently(ServerLevel level, BlockPos pos) {
        BlockState current = level.m_8055_(pos);
        if (current.m_60795_()) {
            return true;
        }
        return level.m_7471_(pos, false);
    }
}

