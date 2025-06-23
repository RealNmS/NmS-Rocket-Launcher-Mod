package com.nms.nmsrocketlaunchermod.item;

import com.nms.nmsrocketlaunchermod.RocketLauncherMod;

import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.ItemGroups;

public class ModItems {
    public static final Item ROCKET_LAUNCHER = new RocketLauncherItem(
            new Item.Settings().maxCount(1));

    public static void registerItems() {
        Registry.register(Registries.ITEM, new Identifier(RocketLauncherMod.MOD_ID, "rocket_launcher"),
                ROCKET_LAUNCHER);

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.COMBAT).register((entries) -> {
            entries.add(ROCKET_LAUNCHER);
        });
    }
}
