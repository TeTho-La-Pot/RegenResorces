package com.github.TeThoLaPot.regen_resources.platform.neoforge.menu;

import com.github.TeThoLaPot.regen_resources.RegenResources;
import com.github.TeThoLaPot.regen_resources.platform.neoforge.inventory.RegenTargetMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class Re_Menus {

    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(Registries.MENU, RegenResources.MOD_ID);

    public static final DeferredHolder<MenuType<?>, MenuType<RegenTargetMenu>> TARGET_EDIT = MENUS.register(
            "target_edit",
            () -> new MenuType<>(RegenTargetMenu::new, FeatureFlags.VANILLA_SET));

    private Re_Menus() {}
}
