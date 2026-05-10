package com.github.TeThoLaPot.regen_resources.platform.neoforge.client;

import com.github.TeThoLaPot.regen_resources.RegenResources;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

/** クライアント専用エントリ（FML クライアントライフサイクル用リスナー）。 */
@Mod(value = RegenResources.MOD_ID, dist = Dist.CLIENT)
public final class RegenResourcesClientEntry {

    public RegenResourcesClientEntry(IEventBus modEventBus) {
        modEventBus.addListener(RegenResourcesClientSetup::onClientSetup);
    }
}
