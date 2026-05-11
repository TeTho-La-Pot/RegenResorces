package com.github.TeThoLaPot.regen_resources.platform.neoforge.client;

import com.github.TeThoLaPot.regen_resources.RegenResources;
import com.github.TeThoLaPot.regen_resources.platform.neoforge.item.Re_Items;
import com.github.TeThoLaPot.regen_resources.common.item.BreakStuffItem;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

/** クライアント：モデル override 用の item property を登録。 */
public final class RegenResourcesClientSetup {

    private RegenResourcesClientSetup() {}

    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            RegenStrippedCompositeWarmup.register();
            ItemProperties.register(
                    Re_Items.BREAK_STUFF.get(),
                    ResourceLocation.fromNamespaceAndPath(RegenResources.MOD_ID, "mode"),
                    (stack, level, entity, seed) -> BreakStuffItem.modeForProperty(stack)
            );
            ItemProperties.register(
                    Re_Items.REGEN_BLOCK_ITEM.get(),
                    ResourceLocation.fromNamespaceAndPath(RegenResources.MOD_ID, RegenVisualItemProperty.PROPERTY_PATH),
                    (stack, level, entity, seed) -> RegenVisualItemProperty.predicateValue(stack)
            );
        });
    }
}
