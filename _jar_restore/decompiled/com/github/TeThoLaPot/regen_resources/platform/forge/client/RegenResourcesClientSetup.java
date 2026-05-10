/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.renderer.item.ItemProperties
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.item.Item
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber$Bus
 *  net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent
 */
package com.github.TeThoLaPot.regen_resources.platform.forge.client;

import com.github.TeThoLaPot.regen_resources.common.item.BreakStuffItem;
import com.github.TeThoLaPot.regen_resources.platform.forge.client.RegenVisualItemProperty;
import com.github.TeThoLaPot.regen_resources.platform.forge.item.Re_Items;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid="regen_resources", bus=Mod.EventBusSubscriber.Bus.MOD)
public final class RegenResourcesClientSetup {
    private RegenResourcesClientSetup() {
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            ItemProperties.register((Item)((Item)Re_Items.BREAK_STUFF.get()), (ResourceLocation)new ResourceLocation("regen_resources", "mode"), (stack, level, entity, seed) -> BreakStuffItem.modeForProperty(stack));
            ItemProperties.register((Item)((Item)Re_Items.REGEN_BLOCK_ITEM.get()), (ResourceLocation)new ResourceLocation("regen_resources", "regen_visual"), (stack, level, entity, seed) -> RegenVisualItemProperty.predicateValue(stack));
        });
    }
}

