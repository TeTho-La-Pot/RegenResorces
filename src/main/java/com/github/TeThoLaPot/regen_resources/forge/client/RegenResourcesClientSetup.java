package com.github.TeThoLaPot.regen_resources.forge.client;

import com.github.TeThoLaPot.regen_resources.RegenResources;
import com.github.TeThoLaPot.regen_resources.forge.item.Re_Items;
import com.github.TeThoLaPot.regen_resources.common.item.BreakStuffItem;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

/**
 * Forge クライアント側の wiring。
 * モデル override 用の item property を登録する。
 */
@Mod.EventBusSubscriber(modid = RegenResources.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class RegenResourcesClientSetup {

    private RegenResourcesClientSetup() {}

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            ItemProperties.register(
                    Re_Items.BREAK_STUFF.get(),
                    new ResourceLocation(RegenResources.MOD_ID, "mode"),
                    (stack, level, entity, seed) -> BreakStuffItem.modeForProperty(stack)
            );
            ItemProperties.register(
                    Re_Items.REGEN_BLOCK_ITEM.get(),
                    new ResourceLocation(RegenResources.MOD_ID, RegenVisualItemProperty.PROPERTY_PATH),
                    (stack, level, entity, seed) -> RegenVisualItemProperty.predicateValue(stack)
            );
        });
    }
}
