package com.github.TeThoLaPot.regen_resources.platform.forge.menu;

import com.github.TeThoLaPot.regen_resources.RegenResources;
import com.github.TeThoLaPot.regen_resources.platform.forge.inventory.RegenTargetMenu;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class Re_Menus {

    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(ForgeRegistries.MENU_TYPES, RegenResources.MOD_ID);

    public static final RegistryObject<MenuType<RegenTargetMenu>> TARGET_EDIT = MENUS.register(
            "target_edit",
            () -> new MenuType<>(RegenTargetMenu::new, FeatureFlags.VANILLA_SET));

    private Re_Menus() {}
}
