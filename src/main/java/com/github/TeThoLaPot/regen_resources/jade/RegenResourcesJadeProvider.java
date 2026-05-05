package com.github.TeThoLaPot.regen_resources.jade;

import com.github.TeThoLaPot.regen_resources.RegenConstants;
import com.github.TeThoLaPot.regen_resources.init.block.RegenBlocks;
import com.github.TeThoLaPot.regen_resources.init.entity.RegenBlockEntity;
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

public enum RegenResourcesJadeProvider implements IBlockComponentProvider {
    INSTANCE;

    @Override
    public ResourceLocation getUid() {
        return ResourceLocation.fromNamespaceAndPath(RegenConstants.MOD_ID, "regen_info");
    }

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        if (!(accessor.getBlockEntity() instanceof RegenBlockEntity be)) {
            return;
        }
        CompoundTag srv = accessor.getServerData();
        long executeAt = 0L;
        if (srv.contains(RegenResourcesJadeServerData.SYNC_EXECUTE_AT)) {
            executeAt = srv.getLong(RegenResourcesJadeServerData.SYNC_EXECUTE_AT);
        }
        long gt = accessor.getLevel().getGameTime();
        long remaining = executeAt - gt;
        if (remaining > 0) {
            tooltip.add(Component.translatable("jade.regen_resources.time_remaining", remaining / 20));
        }

        String rl = srv.getString(RegenResourcesJadeServerData.SYNC_RESTORE_RL);
        if (rl.isEmpty()) {
            String fromBe = be.getRestoreRlString();
            rl = fromBe != null ? fromBe : "";
        }
        if (!rl.isEmpty()) {
            ResourceLocation id = ResourceLocation.tryParse(rl);
            Component targetName = id != null
                    ? BuiltInRegistries.BLOCK.getOptional(id)
                            .map(Block::getName)
                            .orElse(Component.literal(rl))
                    : Component.literal(rl);
            tooltip.add(Component.translatable("jade.regen_resources.target", targetName));
        }
    }

    @Override
    public @Nullable IElement getIcon(BlockAccessor accessor, IPluginConfig config, IElement currentIcon) {
        if (!(accessor.getBlockEntity() instanceof RegenBlockEntity)) {
            return null;
        }
        var mimic = RegenBlocks.mimicStateAt(accessor.getLevel(), accessor.getPosition());
        ItemStack stack = new ItemStack(mimic.getBlock().asItem());
        if (stack.isEmpty()) {
            stack = new ItemStack(Blocks.STONE);
        }
        return IElementHelper.get().item(stack);
    }
}
