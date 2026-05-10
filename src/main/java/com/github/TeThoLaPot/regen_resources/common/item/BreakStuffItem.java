package com.github.TeThoLaPot.regen_resources.common.item;

import com.github.TeThoLaPot.regen_resources.platform.neoforge.event.RegenBlockBreakEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
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
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * 破壊のオーブ（英: Destruction Orb）。スニーク＋使用でモード切替、破壊 ON 時は再生ブロックを
 * （{@link RegenBlockBreakEvents} で判定）ツールと同様に掘って除去する。
 */
public class BreakStuffItem extends Item {

    public static final String MODE_TAG = "mode";

    public BreakStuffItem(Properties properties) {
        super(properties);
    }

    private static CompoundTag customTagCopy(ItemStack stack) {
        CustomData cd = stack.get(DataComponents.CUSTOM_DATA);
        return cd != null ? cd.copyTag() : new CompoundTag();
    }

    private static void applyCustomTag(ItemStack stack, CompoundTag tag) {
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    public static void modeChange(ItemStack stack) {
        CompoundTag tag = customTagCopy(stack);
        tag.putInt(MODE_TAG, modeNum(stack) < 1 ? modeNum(stack) + 1 : 0);
        applyCustomTag(stack, tag);
    }

    /** 除去モードか（1 = ON）。イベント／モデルpredicate用。 */
    public static int modeNum(ItemStack stack) {
        CompoundTag tag = customTagCopy(stack);
        return tag.getInt(MODE_TAG);
    }

    public static boolean isRemovalMode(ItemStack stack) {
        return modeNum(stack) == 1;
    }

    /** モデル override 用（0/1）。 */
    public static float modeForProperty(ItemStack stack) {
        return modeNum(stack) == 1 ? 1.0F : 0.0F;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand useHand) {
        ItemStack stack = player.getItemInHand(useHand);
        if (player.isCrouching()) {
            if (!level.isClientSide()) {
                modeChange(stack);
            }
            player.playSound(SoundEvents.EXPERIENCE_ORB_PICKUP, 1.0F, 1.0F);
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
        }
        return InteractionResultHolder.pass(stack);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return modeNum(stack) != 0;
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        if (!level.isClientSide()) {
            CompoundTag tag = customTagCopy(stack);
            if (!tag.contains(MODE_TAG)) {
                tag.putInt(MODE_TAG, 0);
                applyCustomTag(stack, tag);
            }
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        Level level = context.level();
        if (level != null && level.isClientSide()) {
            Component sneakKey = Component.keybind("key.sneak").withStyle(ChatFormatting.DARK_PURPLE);
            Component useKey = Component.keybind("key.use").withStyle(ChatFormatting.DARK_PURPLE);

            if (modeNum(stack) == 0) {
                tooltip.add(Component.translatable("tooltip.regen_resources.break_stuff_off").withStyle(ChatFormatting.GRAY));
            } else {
                tooltip.add(Component.translatable("tooltip.regen_resources.break_stuff_on").withStyle(ChatFormatting.GRAY));
            }

            tooltip.add(Component.translatable("tooltip.regen_resources.break_stuff_description", sneakKey, useKey).withStyle(ChatFormatting.GRAY));
        }
    }
}
