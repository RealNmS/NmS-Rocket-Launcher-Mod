package com.nms.nmsrocketlaunchermod;

import static com.nms.nmsrocketlaunchermod.item.ModItems.ROCKET_LAUNCHER;
import com.nms.nmsrocketlaunchermod.item.RocketLauncherItem;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentTarget;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class ModEnchantments {
    public static Enchantment RELOAD;

    public static class ReloadEnchantment extends Enchantment {
        public ReloadEnchantment() {
            super(Rarity.UNCOMMON, EnchantmentTarget.BREAKABLE, new EquipmentSlot[] { EquipmentSlot.MAINHAND });
        }

        @Override
        public boolean isAcceptableItem(ItemStack stack) {
            Item item = stack.getItem();

            return item == ROCKET_LAUNCHER;
        }

        @Override
        public int getMaxLevel() {
            return 3;
        }
    }

    public static void registerEnchantments() {
        // Register during mod initialization
        RELOAD = Registry.register(
                Registry.ENCHANTMENT,
                new Identifier("nmsrocketlaunchermod", "reload"),
                new ReloadEnchantment());
    }
}