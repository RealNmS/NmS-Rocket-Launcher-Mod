package com.nms.nmsrocketlaunchermod.item;

import com.nms.nmsrocketlaunchermod.RocketLauncherMod;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public class ModItems {
    public static final Item ROCKET_LAUNCHER = new RocketLauncherItem(
            new Item.Settings().maxCount(1));

    public static final ItemGroup ROCKET_GROUP = FabricItemGroup.builder(
            new Identifier(RocketLauncherMod.MOD_ID, "rocket_group"))
            .icon(() -> new ItemStack(ROCKET_LAUNCHER))
            .displayName(Text.translatable("itemGroup.nmsrocketlaunchermod"))
            .entries((displayContext, entries) -> entries.add(ROCKET_LAUNCHER))
            .build();

    public static void registerItems() {
        Registry.register(Registries.ITEM, new Identifier(RocketLauncherMod.MOD_ID, "rocket_launcher"),
                ROCKET_LAUNCHER);
    }
}
