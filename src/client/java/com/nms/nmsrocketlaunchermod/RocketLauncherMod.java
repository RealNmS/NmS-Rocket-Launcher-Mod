package com.nms.nmsrocketlaunchermod;

import com.nms.nmsrocketlaunchermod.entity.ModEntities;
import com.nms.nmsrocketlaunchermod.item.ModItems;

import net.fabricmc.api.ModInitializer;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RocketLauncherMod implements ModInitializer {
    public static final String MOD_ID = "nmsrocketlaunchermod";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static final SoundEvent ROCKET_LAUNCH_SOUND = registerSound("rocket_launch");
    public static final SoundEvent ROCKET_LOOP_SOUND = registerSound("rocket_loop");

    private static SoundEvent registerSound(String path) {
        Identifier id = new Identifier(MOD_ID, path);
        return Registry.register(Registries.SOUND_EVENT, id, SoundEvent.of(id));
    }

    @Override
    public void onInitialize() {
        LOGGER.info("NmS' Rocket Launcher Mod is loading...");
        ModItems.registerItems();
        ModEntities.registerEntities();
        ModEnchantments.registerEnchantments();
        LOGGER.info("NmS' Rocket Launcher Mod initialized!");
    }
}
