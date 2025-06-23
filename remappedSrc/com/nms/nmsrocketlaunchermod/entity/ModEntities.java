package com.nms.nmsrocketlaunchermod.entity;

import com.nms.nmsrocketlaunchermod.RocketLauncherMod;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class ModEntities {
    public static final EntityType<RocketEntity> ROCKET = FabricEntityTypeBuilder
            .<RocketEntity>create(SpawnGroup.MISC, (type, world) -> new RocketEntity(type, world))
            .dimensions(EntityDimensions.fixed(0.5F, 0.5F))
            .trackRangeBlocks(128)
            .trackedUpdateRate(10)
            .build();

    public static void registerEntities() {
        Registry.register(Registry.ENTITY_TYPE, new Identifier(RocketLauncherMod.MOD_ID, "rocket"), ROCKET);
    }
}