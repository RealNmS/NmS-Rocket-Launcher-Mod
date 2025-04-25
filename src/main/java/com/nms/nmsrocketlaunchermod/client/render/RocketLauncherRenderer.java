package com.nms.nmsrocketlaunchermod.client.render;

import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3f;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.render.entity.model.EntityModelLoader;

import com.nms.nmsrocketlaunchermod.RocketLauncherMod;
import com.nms.nmsrocketlaunchermod.client.model.RocketLauncherModel;

import net.minecraft.client.render.RenderLayer;

public class RocketLauncherRenderer {
    private static final Identifier TEXTURE = new Identifier(RocketLauncherMod.MOD_ID,
            "textures/item/rocket_launcher.png");
    private final RocketLauncherModel model;

    public RocketLauncherRenderer(EntityModelLoader modelLoader) {
        EntityModelLayer layer = new EntityModelLayer(new Identifier(RocketLauncherMod.MOD_ID, "rocket_launcher"),
                "main");
        this.model = new RocketLauncherModel(modelLoader.getModelPart(layer));
    }

    public void render(ItemStack stack, ModelTransformation.Mode mode, MatrixStack matrices,
            VertexConsumerProvider vertexConsumers, int light, int overlay) {
        matrices.push();

        // Adjust position and scale based on the transformation mode
        if (mode == ModelTransformation.Mode.FIRST_PERSON_RIGHT_HAND ||
                mode == ModelTransformation.Mode.FIRST_PERSON_LEFT_HAND) {
            // First-person view adjustments
            matrices.translate(0.2F, 0.2F, 0.1F);
            matrices.scale(0.4F, 0.4F, 0.4F);
        } else if (mode == ModelTransformation.Mode.THIRD_PERSON_RIGHT_HAND ||
                mode == ModelTransformation.Mode.THIRD_PERSON_LEFT_HAND) {
            // Third-person view adjustments
            matrices.translate(0.0F, 0.15F, 0.2F);
            matrices.scale(0.5F, 0.5F, 0.5F);
            matrices.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(180.0F));
        } else if (mode == ModelTransformation.Mode.GUI) {
            // GUI view adjustments
            matrices.translate(0.5F, 0.5F, 0.0F);
            matrices.scale(0.6F, 0.6F, 0.6F);
            matrices.multiply(Vec3f.POSITIVE_Z.getDegreesQuaternion(180.0F));
        } else if (mode == ModelTransformation.Mode.GROUND) {
            // Ground view adjustments
            matrices.translate(0.5F, 0.25F, 0.5F);
            matrices.scale(0.5F, 0.5F, 0.5F);
        } else if (mode == ModelTransformation.Mode.FIXED) {
            // Fixed view adjustments (like item frames)
            matrices.translate(0.5F, 0.5F, 0.5F);
            matrices.scale(0.75F, 0.75F, 0.75F);
        }

        // Check if item is loaded with a rocket
        boolean isLoaded = stack.hasNbt() && stack.getNbt().getBoolean("loaded");

        // Render the model
        model.setLoaded(isLoaded);
        model.render(matrices, vertexConsumers.getBuffer(RenderLayer.getEntitySolid(TEXTURE)),
                light, overlay, 1.0F, 1.0F, 1.0F, 1.0F);

        matrices.pop();
    }
}