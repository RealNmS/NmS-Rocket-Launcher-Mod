package com.nms.nmsrocketlaunchermod.client;

import com.nms.nmsrocketlaunchermod.client.render.RocketEntityRenderer;
import com.nms.nmsrocketlaunchermod.entity.ModEntities;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;

public class RocketLauncherModClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // Register the rocket entity renderer
        EntityRendererRegistry.register(ModEntities.ROCKET, RocketEntityRenderer::new);
    }
}