/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.ChatFormatting
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.MutableComponent
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.InteractionResultHolder
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.Item$Properties
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.TooltipFlag
 *  net.minecraft.world.level.Level
 *  org.jetbrains.annotations.Nullable
 */
package com.github.TeThoLaPot.regen_resources.common.item;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class BreakStuffItem
extends Item {
    public static final String MODE_TAG = "mode";

    public BreakStuffItem(Item.Properties properties) {
        super(properties);
    }

    public static void modeChange(ItemStack stack) {
        stack.getOrCreateTag().putInt(MODE_TAG, BreakStuffItem.modeNum(stack) < 1 ? BreakStuffItem.modeNum(stack) + 1 : 0);
    }

    public static int modeNum(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null) {
            return 0;
        }
        return tag.getInt(MODE_TAG);
    }

    public static boolean isRemovalMode(ItemStack stack) {
        return BreakStuffItem.modeNum(stack) == 1;
    }

    public static float modeForProperty(ItemStack stack) {
        return BreakStuffItem.modeNum(stack) == 1 ? 1.0f : 0.0f;
    }

    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand useHand) {
        ItemStack stack = player.getItemInHand(useHand);
        if (player.isCrouching()) {
            if (!level.isClientSide()) {
                BreakStuffItem.modeChange(stack);
            }
            player.playSound(SoundEvents.EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
        }
        return InteractionResultHolder.pass(stack);
    }

    public boolean isFoil(ItemStack stack) {
        return BreakStuffItem.modeNum(stack) != 0;
    }

    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        if (!level.isClientSide() && stack.getTag() == null) {
            stack.setTag(new CompoundTag());
            stack.getTag().putInt(MODE_TAG, 0);
        }
    }

    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        if (level != null && level.isClientSide()) {
            MutableComponent sneakKey = Component.keybind((String)"key.sneak").withStyle(ChatFormatting.DARK_PURPLE);
            MutableComponent useKey = Component.keybind((String)"key.use").withStyle(ChatFormatting.DARK_PURPLE);
            if (BreakStuffItem.modeNum(stack) == 0) {
                tooltip.add((Component)Component.translatable((String)"tooltip.regen_resources.break_stuff_off").withStyle(ChatFormatting.GRAY));
            } else {
                tooltip.add((Component)Component.translatable((String)"tooltip.regen_resources.break_stuff_on").withStyle(ChatFormatting.GRAY));
            }
            tooltip.add((Component)Component.translatable((String)"tooltip.regen_resources.break_stuff_description", (Object[])new Object[]{sneakKey, useKey}).withStyle(ChatFormatting.GRAY));
        }
    }
}

