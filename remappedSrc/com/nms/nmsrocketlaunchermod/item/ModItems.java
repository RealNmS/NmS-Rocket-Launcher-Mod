package com.nms.nmsrocketlaunchermod.item;

import com.nms.nmsrocketlaunchermod.RocketLauncherMod;

import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class ModItems {
    public static final Item ROCKET_LAUNCHER = new RocketLauncherItem(
            new Item.Settings().maxCount(1).group(ItemGroup.COMBAT));

    public static void registerItems() {
        Registry.register(
                Registry.ITEM,
                new Identifier(RocketLauncherMod.MOD_ID, "rocket_launcher"),
                ROCKET_LAUNCHER);
    }
}