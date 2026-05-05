package com.github.TeThoLaPot.regen_resources.init.item;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;


public class BreakStuff extends Item {
    public BreakStuff(Properties pProperties) {
        super(pProperties);
    }

    private void modeChange(ItemStack stack){
        if (stack.getTag() == null){
            stack.setTag(new CompoundTag());
        }
        stack.getTag().putInt("mode", modeNum(stack) < 1 ? modeNum(stack) + 1 : 0);
    }

    public int modeNum(ItemStack stack){
        if (stack.getTag() == null) {
            return 0;
        }
        return stack.getTag().getInt("mode");
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand useHand) {
        ItemStack stack = player.getItemInHand(useHand);
        if (player.isCrouching()){
            if (!level.isClientSide){
                modeChange(stack);
            }
            player.playSound(SoundEvents.EXPERIENCE_ORB_PICKUP, 1.0F, 1.0F);
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
        }
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        if (modeNum(stack) == 0) {return false;}
        return true;
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        if (!level.isClientSide) {
            if (stack.getTag() == null) {
                stack.setTag(new CompoundTag());
                stack.getTag().putInt("mode", 0);
            }
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        if (level != null && level.isClientSide) {

            Component sneakKey = Component.keybind("key.sneak").withStyle(ChatFormatting.DARK_PURPLE);
            Component useKey = Component.keybind("key.use").withStyle(ChatFormatting.DARK_PURPLE);

            if (modeNum(stack) == 0){
                tooltip.add(Component.translatable("tooltip.regen_resources.break_stuff_off").withStyle(ChatFormatting.GRAY));
            } else {
                tooltip.add(Component.translatable("tooltip.regen_resources.break_stuff_on").withStyle(ChatFormatting.GRAY));
            }

            tooltip.add(Component.translatable("tooltip.regen_resources.break_stuff_description", sneakKey, useKey).withStyle(ChatFormatting.GRAY));
        }
    }
}
