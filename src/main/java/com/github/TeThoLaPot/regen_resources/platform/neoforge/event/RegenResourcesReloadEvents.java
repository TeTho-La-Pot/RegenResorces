package com.github.TeThoLaPot.regen_resources.platform.neoforge.event;

import com.github.TeThoLaPot.regen_resources.platform.neoforge.config.RegenPresetsReloadListener;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.AddReloadListenerEvent;

/** {@link AddReloadListenerEvent} でプリセット JSON のホットリロードを登録する。 */
public final class RegenResourcesReloadEvents {

    private RegenResourcesReloadEvents() {}

    @SubscribeEvent
    public static void onAddReloadListeners(AddReloadListenerEvent event) {
        event.addListener(new RegenPresetsReloadListener());
    }
}
