package com.github.TeThoLaPot.regen_resources.platform.neoforge.compat.jei;

import com.github.TeThoLaPot.regen_resources.RegenResources;
import com.github.TeThoLaPot.regen_resources.platform.neoforge.client.screen.RegenTargetEditScreen;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.gui.handlers.IGhostIngredientHandler;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

@JeiPlugin
public final class RegenResourcesJeiPlugin implements IModPlugin {

    @Override
    public ResourceLocation getPluginUid() {
        return ResourceLocation.fromNamespaceAndPath(RegenResources.MOD_ID, "jei");
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        registration.addGhostIngredientHandler(RegenTargetEditScreen.class, new TargetGhostHandler());
    }

    private static final class TargetGhostHandler implements IGhostIngredientHandler<RegenTargetEditScreen> {

        @Override
        public <I> List<Target<I>> getTargetsTyped(
                RegenTargetEditScreen screen, ITypedIngredient<I> ingredient, boolean doStart) {
            List<Target<I>> out = new ArrayList<>();
            Object raw = ingredient.getIngredient();
            if (!(raw instanceof ItemStack stack) || !(stack.getItem() instanceof BlockItem)) {
                return out;
            }
            int slots = RegenTargetEditScreen.maxRegisterSlots();
            for (int i = 0; i < slots; i++) {
                final int index = i;
                Rect2i area = screen.getRegisterSlotArea(index);
                out.add(new Target<>() {
                    @Override
                    public Rect2i getArea() {
                        return area;
                    }

                    @Override
                    public void accept(I ingredientValue) {
                        if (ingredientValue instanceof ItemStack dropped) {
                            screen.acceptGhostAtIndex(index, dropped);
                        }
                    }
                });
            }
            return out;
        }

        @Override
        public void onComplete() {}
    }
}
