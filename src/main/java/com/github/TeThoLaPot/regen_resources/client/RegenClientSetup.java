package com.github.TeThoLaPot.regen_resources.client;

import com.github.TeThoLaPot.regen_resources.RegenConstants;
import com.github.TeThoLaPot.regen_resources.init.entity.BlockEntities;
import com.github.TeThoLaPot.regen_resources.init.item.BreakStuff;
import com.github.TeThoLaPot.regen_resources.init.item.Re_Items;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent.RegisterRenderers;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = RegenConstants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class RegenClientSetup {

    @SubscribeEvent
    public static void registerRenderers(RegisterRenderers event) {
        event.registerBlockEntityRenderer(BlockEntities.REGEN_ORE_ENTITY.get(), RegenBlockEntityRenderer::new);
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> ItemProperties.register(
                Re_Items.BREAK_STUFF.get(),
                new ResourceLocation(RegenConstants.MOD_ID, "mode"),
                (stack, level, entity, seed) ->
                        stack.getItem() instanceof BreakStuff bs ? (float) bs.modeNum(stack) : 0.0F));
    }
}
