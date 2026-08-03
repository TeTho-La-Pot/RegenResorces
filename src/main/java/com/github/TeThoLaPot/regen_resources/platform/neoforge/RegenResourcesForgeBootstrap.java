package com.github.TeThoLaPot.regen_resources.platform.neoforge;

import com.github.TeThoLaPot.regen_resources.common.regen.RegenRuleRegistry;
import com.github.TeThoLaPot.regen_resources.platform.RegenPlatformServices;
import com.github.TeThoLaPot.regen_resources.platform.neoforge.block.Re_Blocks;
import com.github.TeThoLaPot.regen_resources.platform.neoforge.config.RegenPresetIo;
import com.github.TeThoLaPot.regen_resources.platform.neoforge.command.RegenSettingsCommands;
import com.github.TeThoLaPot.regen_resources.platform.neoforge.event.OreHarvesterCompatForgeEvents;
import com.github.TeThoLaPot.regen_resources.platform.neoforge.event.RegenBlockBreakEvents;
import com.github.TeThoLaPot.regen_resources.platform.neoforge.event.RegenMiningDelegateForgeEvents;
import com.github.TeThoLaPot.regen_resources.platform.neoforge.event.RegenMassBreakBugWorkaround;
import com.github.TeThoLaPot.regen_resources.platform.neoforge.event.RegenPlacementForgeEvents;
import com.github.TeThoLaPot.regen_resources.platform.neoforge.event.RegenRegenForgeEvents;
import com.github.TeThoLaPot.regen_resources.platform.neoforge.event.RegenResourcesReloadEvents;
import com.github.TeThoLaPot.regen_resources.platform.neoforge.item.Re_Items;
import com.github.TeThoLaPot.regen_resources.platform.neoforge.loot.ReLootModifiers;
import com.github.TeThoLaPot.regen_resources.platform.neoforge.config.RegenResourcesForgeConfig;
import com.github.TeThoLaPot.regen_resources.platform.neoforge.client.model.RegenCompositeSpriteSourceRegistry;
import com.github.TeThoLaPot.regen_resources.platform.neoforge.network.RegenResourcesNetwork;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;

/** NeoForge：DeferredRegister とネットワーク登録。 */
public final class RegenResourcesForgeBootstrap {

    private RegenResourcesForgeBootstrap() {}

    public static void bootstrap(IEventBus modEventBus) {
        if (FMLEnvironment.dist == Dist.CLIENT) {
            RegenCompositeSpriteSourceRegistry.ensureRegistered();
        }
        RegenPlatformServices.install(new RegenForgePlatformConfig(), new RegenForgePlatformNetwork());
        registerGameEvents();
        modEventBus.addListener(RegenResourcesForgeBootstrap::onCommonConfigLoading);
        modEventBus.addListener(RegenResourcesForgeBootstrap::onCommonConfigReloading);
        modEventBus.addListener(RegenCreativeTabEvents::stripRegenShellItem);
        modEventBus.addListener(RegenResourcesNetwork::register);
        Re_Blocks.BLOCKS.register(modEventBus);
        Re_Blocks.BLOCK_ENTITY_TYPES.register(modEventBus);
        Re_Items.ITEMS.register(modEventBus);
        Re_CreativeTabs.CREATIVE_MODE_TABS.register(modEventBus);
        ReLootModifiers.LOOT_MODIFIER_SERIALIZERS.register(modEventBus);
        com.github.TeThoLaPot.regen_resources.platform.neoforge.menu.Re_Menus.MENUS.register(modEventBus);
    }

    private static void registerGameEvents() {
        NeoForge.EVENT_BUS.register(OreHarvesterCompatForgeEvents.class);
        NeoForge.EVENT_BUS.register(RegenBlockBreakEvents.class);
        NeoForge.EVENT_BUS.register(RegenMiningDelegateForgeEvents.class);
        NeoForge.EVENT_BUS.register(RegenPlacementForgeEvents.class);
        NeoForge.EVENT_BUS.register(RegenRegenForgeEvents.class);
        NeoForge.EVENT_BUS.register(RegenResourcesReloadEvents.class);
        NeoForge.EVENT_BUS.register(RegenMassBreakBugWorkaround.class);
        NeoForge.EVENT_BUS.register(RegenSettingsCommands.class);
    }

    /**
     * COMMON の toml がディスクから読み込まれた／再読込されたあと。
     * <p>フラグ自体（自然再生など）は NeoForge が {@code config/regen_resources-common.toml} から供給する。
     * プリセット JSON の再適用はワールドバインド時のみ実体があり、未バインドなら空ルールになる。
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
