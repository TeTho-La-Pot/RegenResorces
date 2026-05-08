package com.github.TeThoLaPot.regen_resources.platform.forge.compat.jade;

import com.github.TeThoLaPot.regen_resources.RegenResources;
import com.github.TeThoLaPot.regen_resources.platform.forge.block.Re_Blocks;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.Nullable;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.IElement;
import snownee.jade.api.ui.IElementHelper;

/**
 * 再生待ちブロックにカーソルを合わせたときの表示（残り時間、復元先）。
 * alpha の {@code RegenResourcesJadeProvider} を現行データ形式に合わせたもの。
 */
public enum RegenResourcesJadeProvider implements IBlockComponentProvider {
    INSTANCE;

    @Override
    public ResourceLocation getUid() {
        return ResourceLocation.fromNamespaceAndPath(RegenResources.MOD_ID, "regen_info");
    }

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        if (!accessor.getBlockState().is(Re_Blocks.REGEN_BLOCK.get())) {
            return;
        }
        CompoundTag srv = accessor.getServerData();
        long executeAt = srv.getLong(RegenResourcesJadeServerData.SYNC_EXECUTE_AT);
        if (executeAt > 0) {
            long gt = accessor.getLevel().getGameTime();
            long remainingTicks = executeAt - gt;
            if (remainingTicks > 0) {
                int secondsRoundedUp = (int) Math.ceil(remainingTicks / 20.0);
                tooltip.add(Component.translatable("jade.regen_resources.time_until_seconds", secondsRoundedUp));
            } else {
                tooltip.add(Component.translatable("jade.regen_resources.time_until_imminent"));
            }
        }

        String rl = srv.getString(RegenResourcesJadeServerData.SYNC_RESTORE_RL);
        if (!rl.isEmpty()) {
            ResourceLocation id = ResourceLocation.tryParse(rl);
            Component targetName = id != null
                    ? BuiltInRegistries.BLOCK.getOptional(id)
                            .map(Block::getName)
                            .orElse(Component.literal(rl))
                    : Component.literal(rl);
            tooltip.add(Component.translatable("jade.regen_resources.regen_target", targetName));
        }
    }

    @Override
    public @Nullable IElement getIcon(BlockAccessor accessor, IPluginConfig config, IElement currentIcon) {
        if (!accessor.getBlockState().is(Re_Blocks.REGEN_BLOCK.get())) {
            return null;
        }
        String rl = accessor.getServerData().getString(RegenResourcesJadeServerData.SYNC_RESTORE_RL);
        ItemStack stack = ItemStack.EMPTY;
        if (!rl.isEmpty()) {
            ResourceLocation id = ResourceLocation.tryParse(rl);
            if (id != null) {
                stack = BuiltInRegistries.BLOCK.getOptional(id)
                        .map(b -> new ItemStack(b.asItem()))
                        .orElse(ItemStack.EMPTY);
            }
        }
        if (stack.isEmpty()) {
            stack = new ItemStack(Blocks.STONE);
        }
        return IElementHelper.get().item(stack);
    }
}

