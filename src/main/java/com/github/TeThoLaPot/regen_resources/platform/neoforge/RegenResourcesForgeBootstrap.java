package com.github.TeThoLaPot.regen_resources.platform.neoforge;

import com.github.TeThoLaPot.regen_resources.common.regen.RegenRuleRegistry;
import com.github.TeThoLaPot.regen_resources.platform.RegenPlatformServices;
import com.github.TeThoLaPot.regen_resources.platform.neoforge.block.Re_Blocks;
import com.github.TeThoLaPot.regen_resources.platform.neoforge.config.RegenPresetIo;
import com.github.TeThoLaPot.regen_resources.platform.neoforge.event.OreHarvesterCompatForgeEvents;
import com.github.TeThoLaPot.regen_resources.platform.neoforge.event.RegenBlockBreakEvents;
import com.github.TeThoLaPot.regen_resources.platform.neoforge.event.RegenMiningDelegateForgeEvents;
import com.github.TeThoLaPot.regen_resources.platform.neoforge.event.RegenPlacementForgeEvents;
import com.github.TeThoLaPot.regen_resources.platform.neoforge.event.RegenRegenForgeEvents;
import com.github.TeThoLaPot.regen_resources.platform.neoforge.item.Re_Items;
import com.github.TeThoLaPot.regen_resources.platform.neoforge.loot.ReLootModifiers;
import com.github.TeThoLaPot.regen_resources.platform.neoforge.config.RegenResourcesForgeConfig;
import com.github.TeThoLaPot.regen_resources.platform.neoforge.network.RegenResourcesNetwork;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.NeoForge;

/** NeoForge：DeferredRegister とネットワーク登録。 */
public final class RegenResourcesForgeBootstrap {

    private RegenResourcesForgeBootstrap() {}

    public static void bootstrap(IEventBus modEventBus) {
        RegenPlatformServices.install(new RegenForgePlatformConfig(), new RegenForgePlatformNetwork());
        registerGameEvents();
        modEventBus.addListener(RegenResourcesForgeBootstrap::onCommonConfigLoading);
        modEventBus.addListener(RegenResourcesForgeBootstrap::onCommonConfigReloading);
        modEventBus.addListener(RegenResourcesNetwork::register);
        Re_Blocks.BLOCKS.register(modEventBus);
        Re_Blocks.BLOCK_ENTITY_TYPES.register(modEventBus);
        Re_Items.ITEMS.register(modEventBus);
        Re_CreativeTabs.CREATIVE_MODE_TABS.register(modEventBus);
        ReLootModifiers.LOOT_MODIFIER_SERIALIZERS.register(modEventBus);
    }

    private static void registerGameEvents() {
        NeoForge.EVENT_BUS.register(OreHarvesterCompatForgeEvents.class);
        NeoForge.EVENT_BUS.register(RegenBlockBreakEvents.class);
        NeoForge.EVENT_BUS.register(RegenMiningDelegateForgeEvents.class);
        NeoForge.EVENT_BUS.register(RegenPlacementForgeEvents.class);
        NeoForge.EVENT_BUS.register(RegenRegenForgeEvents.class);
    }

    /**
     * COMMON の toml がディスクから読み込まれた後にプリセットを読む。
     * {@link net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent} では {@link RegenResourcesForgeConfig} の値が
     * まだファイルと同期していないことがあり、bootstrapVanillaPresetsWhenEmpty や JSON 再生成が効かない原因になる。
     */
    private static void onCommonConfigLoading(ModConfigEvent.Loading event) {
        if (event.getConfig().getSpec() != RegenResourcesForgeConfig.SPEC) {
            return;
        }
        applyPresetRulesFromDisk();
    }

    private static void onCommonConfigReloading(ModConfigEvent.Reloading event) {
        if (event.getConfig().getSpec() != RegenResourcesForgeConfig.SPEC) {
            return;
        }
        applyPresetRulesFromDisk();
    }

    public static void applyPresetRulesFromDisk() {
        RegenRuleRegistry.setRules(RegenPresetIo.loadOrCreateDefaults());
    }
}
