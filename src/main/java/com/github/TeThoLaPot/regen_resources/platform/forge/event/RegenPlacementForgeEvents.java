/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.github.TeThoLaPot.tt_core.TT_core
 *  net.minecraft.core.BlockPos
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.level.LevelAccessor
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraftforge.common.util.BlockSnapshot
 *  net.minecraftforge.event.level.BlockEvent$EntityMultiPlaceEvent
 *  net.minecraftforge.event.level.BlockEvent$EntityPlaceEvent
 *  net.minecraftforge.eventbus.api.EventPriority
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber$Bus
 */
package com.github.TeThoLaPot.regen_resources.platform.forge.event;

import com.github.TeThoLaPot.regen_resources.common.regen.RegenRuleRegistry;
import com.github.TeThoLaPot.regen_resources.platform.RegenPlatformServices;
import com.github.TeThoLaPot.regen_resources.platform.forge.config.RegenResourcesForgeConfig;
import com.github.TeThoLaPot.tt_core.TT_core;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid="regen_resources", bus=Mod.EventBusSubscriber.Bus.FORGE)
public final class RegenPlacementForgeEvents {
    private RegenPlacementForgeEvents() {
    }

    @SubscribeEvent(priority=EventPriority.LOWEST)
    public static void onEntityPlace(BlockEvent.EntityPlaceEvent event) {
        if (event instanceof BlockEvent.EntityMultiPlaceEvent) {
            BlockEvent.EntityMultiPlaceEvent multi = (BlockEvent.EntityMultiPlaceEvent)event;
            RegenPlacementForgeEvents.onMultiPlace(multi);
            return;
        }
        LevelAccessor levelAccessor = event.getLevel();
        if (!(levelAccessor instanceof ServerLevel)) {
            return;
        }
        ServerLevel level = (ServerLevel)levelAccessor;
        RegenPlacementForgeEvents.maybeMark(level, event.getEntity(), event.getPos(), event.getPlacedBlock());
    }

    private static void onMultiPlace(BlockEvent.EntityMultiPlaceEvent event) {
        LevelAccessor levelAccessor = event.getLevel();
        if (!(levelAccessor instanceof ServerLevel)) {
            return;
        }
        ServerLevel level = (ServerLevel)levelAccessor;
        Entity entity = event.getEntity();
        for (BlockSnapshot snap : event.getReplacedBlockSnapshots()) {
            RegenPlacementForgeEvents.maybeMark(level, entity, snap.getPos(), snap.getCurrentBlock());
        }
    }

    private static void maybeMark(ServerLevel level, Entity entity, BlockPos pos, BlockState placed) {
        byte marker;
        ResourceLocation dim = level.dimension().location();
        if (RegenRuleRegistry.firstMatch(dim, placed) == null) {
            return;
        }
        if (entity instanceof Player) {
            Player player = (Player)entity;
            marker = player.isCreative() ? (byte)2 : 1;
        } else if (entity == null) {
            if (!((Boolean)RegenResourcesForgeConfig.COMMAND_LIKE_PLACEMENT_ELIGIBLE.get()).booleanValue()) {
                return;
            }
            marker = 2;
        } else {
            return;
        }
        CompoundTag patch = new CompoundTag();
        patch.putByte("rr_src", marker);
        TT_core.saveBlockData((ServerLevel)level, (BlockPos)pos, (CompoundTag)patch);
        RegenPlatformServices.NETWORK.invalidateJadeProbe(level, pos);
    }
}

