package com.nms.nmsrocketlaunchermod.item;

import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.item.*;
import net.minecraft.item.Item.Settings;

import java.util.function.Function;

import com.nms.nmsrocketlaunchermod.RocketLauncherMod;
import com.nms.nmsrocketlaunchermod.item.*;

public class ModItems {
    public static final Item ROCKET_LAUNCHER = new RocketLauncherItem(
            new Item.Settings().maxCount(1).group(ItemGroup.COMBAT));

    public static void registerItems() { // Use the original method name
        Registry.register(
                Registry.ITEM,
                new Identifier(RocketLauncherMod.MOD_ID, "rocket_launcher"),
                ROCKET_LAUNCHER);
    }
}