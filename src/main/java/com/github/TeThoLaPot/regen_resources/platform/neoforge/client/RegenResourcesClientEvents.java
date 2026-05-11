package com.github.TeThoLaPot.regen_resources.platform.neoforge.client;

import com.github.TeThoLaPot.regen_resources.RegenResources;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
/** クライアント Mod イベント（単一の {@link RegenResources} と整合させ、登録漏れを防ぐ）。 */
@SuppressWarnings("deprecation")
@EventBusSubscriber(modid = RegenResources.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class RegenResourcesClientEvents {

    private RegenResourcesClientEvents() {}

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        RegenResourcesClientSetup.onClientSetup(event);
    }
}
