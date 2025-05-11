package com.nms.nmsrocketlaunchermod.mixin;

import static com.nms.nmsrocketlaunchermod.item.ModItems.ROCKET_LAUNCHER;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.EnchantmentTarget;
import static net.minecraft.enchantment.Enchantments.MENDING;
import static net.minecraft.enchantment.Enchantments.UNBREAKING;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(EnchantmentHelper.class)
public abstract class EnchantmentHelperMixin {

    @Unique
    private static Enchantment currentEnchantment;

    @Redirect(method = "getPossibleEntries", at = @At(value = "INVOKE", target = "Lnet/minecraft/enchantment/Enchantment;isAvailableForRandomSelection()Z"))
    private static boolean captureEnchantment(Enchantment enchantment) {
        currentEnchantment = enchantment;
        return enchantment.isAvailableForRandomSelection();
    }

    @Redirect(method = "getPossibleEntries", at = @At(value = "INVOKE", target = "Lnet/minecraft/enchantment/EnchantmentTarget;isAcceptableItem(Lnet/minecraft/item/Item;)Z"))
    private static boolean isAcceptableItem(EnchantmentTarget enchantmentTarget, Item item) {
        ItemStack stack = new ItemStack(item);

        if (item == ROCKET_LAUNCHER) {
            return currentEnchantment.isAcceptableItem(stack) || currentEnchantment == MENDING
                    || currentEnchantment == UNBREAKING;
        }

        return enchantmentTarget.isAcceptableItem(item);
    }
}