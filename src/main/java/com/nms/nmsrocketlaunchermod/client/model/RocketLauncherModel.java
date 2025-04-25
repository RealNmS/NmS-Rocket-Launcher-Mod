package com.nms.nmsrocketlaunchermod.client.model;

import net.minecraft.client.model.Model;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;

public class RocketLauncherModel extends Model {
    private final ModelPart base;
    private final ModelPart barrel;
    private final ModelPart handle;
    private final ModelPart rocket; // Rocket part that's only visible when loaded
    private boolean isLoaded = false;

    public RocketLauncherModel(ModelPart root) {
        super(RenderLayer::getEntitySolid);
        this.base = root.getChild("base");
        this.barrel = root.getChild("barrel");
        this.handle = root.getChild("handle");
        this.rocket = root.getChild("rocket");
    }

    public static TexturedModelData getTexturedModelData() {
        ModelData modelData = new ModelData();
        ModelPartData modelPartData = modelData.getRoot();

        // Create the base of the rocket launcher
        modelPartData.addChild("base",
                ModelPartBuilder.create().uv(0, 0)
                        .cuboid(-2.0F, -2.0F, -8.0F, 4.0F, 4.0F, 16.0F),
                ModelTransform.pivot(0.0F, 0.0F, 0.0F));

        // Create the barrel
        modelPartData.addChild("barrel",
                ModelPartBuilder.create().uv(0, 20)
                        .cuboid(-1.5F, -1.5F, -12.0F, 3.0F, 3.0F, 4.0F),
                ModelTransform.pivot(0.0F, 0.0F, 0.0F));

        // Create the handle
        modelPartData.addChild("handle",
                ModelPartBuilder.create().uv(24, 20)
                        .cuboid(-1.0F, 0.0F, -2.0F, 2.0F, 6.0F, 3.0F),
                ModelTransform.pivot(0.0F, 2.0F, 4.0F));

        // Create the rocket (only visible when loaded)
        modelPartData.addChild("rocket",
                ModelPartBuilder.create().uv(40, 0)
                        .cuboid(-1.0F, -1.0F, -14.0F, 2.0F, 2.0F, 6.0F),
                ModelTransform.pivot(0.0F, 0.0F, 0.0F));

        return TexturedModelData.of(modelData, 64, 64);
    }

    public void setLoaded(boolean loaded) {
        this.isLoaded = loaded;
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumer vertices, int light, int overlay,
            float red, float green, float blue, float alpha) {
        base.render(matrices, vertices, light, overlay, red, green, blue, alpha);
        barrel.render(matrices, vertices, light, overlay, red, green, blue, alpha);
        handle.render(matrices, vertices, light, overlay, red, green, blue, alpha);

        // Only render the rocket part if the launcher is loaded
        if (isLoaded) {
            rocket.render(matrices, vertices, light, overlay, red, green, blue, alpha);
        }
    }
}