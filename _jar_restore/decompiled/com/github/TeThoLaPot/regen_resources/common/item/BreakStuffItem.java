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
        stack.m_41784_().m_128405_(MODE_TAG, BreakStuffItem.modeNum(stack) < 1 ? BreakStuffItem.modeNum(stack) + 1 : 0);
    }

    public static int modeNum(ItemStack stack) {
        CompoundTag tag = stack.m_41783_();
        if (tag == null) {
            return 0;
        }
        return tag.m_128451_(MODE_TAG);
    }

    public static boolean isRemovalMode(ItemStack stack) {
        return BreakStuffItem.modeNum(stack) == 1;
    }

    public static float modeForProperty(ItemStack stack) {
        return BreakStuffItem.modeNum(stack) == 1 ? 1.0f : 0.0f;
    }

    public InteractionResultHolder<ItemStack> m_7203_(Level level, Player player, InteractionHand useHand) {
        ItemStack stack = player.m_21120_(useHand);
        if (player.m_6047_()) {
            if (!level.m_5776_()) {
                BreakStuffItem.modeChange(stack);
            }
            player.m_5496_(SoundEvents.f_11871_, 1.0f, 1.0f);
            return InteractionResultHolder.m_19092_((Object)stack, (boolean)level.m_5776_());
        }
        return InteractionResultHolder.m_19098_((Object)stack);
    }

    public boolean m_5812_(ItemStack stack) {
        return BreakStuffItem.modeNum(stack) != 0;
    }

    public void m_6883_(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        if (!level.m_5776_() && stack.m_41783_() == null) {
            stack.m_41751_(new CompoundTag());
            stack.m_41783_().m_128405_(MODE_TAG, 0);
        }
    }

    public void m_7373_(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        if (level != null && level.m_5776_()) {
            MutableComponent sneakKey = Component.m_237117_((String)"key.sneak").m_130940_(ChatFormatting.DARK_PURPLE);
            MutableComponent useKey = Component.m_237117_((String)"key.use").m_130940_(ChatFormatting.DARK_PURPLE);
            if (BreakStuffItem.modeNum(stack) == 0) {
                tooltip.add((Component)Component.m_237115_((String)"tooltip.regen_resources.break_stuff_off").m_130940_(ChatFormatting.GRAY));
            } else {
                tooltip.add((Component)Component.m_237115_((String)"tooltip.regen_resources.break_stuff_on").m_130940_(ChatFormatting.GRAY));
            }
            tooltip.add((Component)Component.m_237110_((String)"tooltip.regen_resources.break_stuff_description", (Object[])new Object[]{sneakKey, useKey}).m_130940_(ChatFormatting.GRAY));
        }
    }
}

