package com.github.TeThoLaPot.regen_resources;

import com.github.TeThoLaPot.regen_resources.core.RegenPersistentTaskRegistration;
import com.github.TeThoLaPot.regen_resources.init.Re_Tabs;
import com.github.TeThoLaPot.regen_resources.init.block.Re_Blocks;
import com.github.TeThoLaPot.regen_resources.init.entity.BlockEntities;
import com.github.TeThoLaPot.regen_resources.init.item.Re_Items;
import com.github.TeThoLaPot.regen_resources.recipe.ReLootModifiers;
import com.mojang.logging.LogUtils;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

/**
 * Forge ブートストラップ（NeoForge は同等のモッドクラスへ差し替え）。
 * ゲームプレイイベントは {@link com.github.TeThoLaPot.regen_resources.forge.RegenGameplayEvents}。
 */
@Mod(RegenConstants.MOD_ID)
public class RegenResources {

    /** 互換参照用（{@link RegenConstants#MOD_ID} と同じ）。 */
    public static final String MOD_ID = RegenConstants.MOD_ID;

    private static final Logger LOGGER = LogUtils.getLogger();

    public RegenResources() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        Re_Blocks.BLOCKS.register(modEventBus);
        BlockEntities.BLOCK_ENTITIES.register(modEventBus);
        Re_Items.register(modEventBus);
        Re_Tabs.CREATIVE_MODE_TABS.register(modEventBus);
        ReLootModifiers.register(modEventBus);

        modEventBus.addListener((ModConfigEvent e) -> {
            if (!MOD_ID.equals(e.getConfig().getModId())) {
                return;
            }
            if (e instanceof ModConfigEvent.Loading || e instanceof ModConfigEvent.Reloading) {
                Config.invalidateEntryCache();
            }
        });

        RegenPersistentTaskRegistration.registerExecutors();

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.SPEC, "RegenResources/regen-resources-common.toml");
        LOGGER.info("RegenResources (Forge bootstrap)");
    }
}
