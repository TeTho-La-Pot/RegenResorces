package com.github.TeThoLaPot.regen_resources.platform.neoforge.client.sync;

import com.github.TeThoLaPot.regen_resources.RegenResources;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/** {@link RegenShellAppearanceClientRetry#tick()} を駆動する。 */
@SuppressWarnings("deprecation")
@EventBusSubscriber(modid = RegenResources.MOD_ID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public final class RegenShellAppearanceClientTicker {

    private RegenShellAppearanceClientTicker() {}

    @SubscribeEvent
    public static void onClientTickPost(ClientTickEvent.Post event) {
        RegenShellAppearanceClientRetry.tick();
    }
}
