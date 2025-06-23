package com.nms.nmsrocketlaunchermod.client.render;

import com.nms.nmsrocketlaunchermod.entity.RocketEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;

public class RocketEntityRenderer extends EntityRenderer<RocketEntity> {
    private static final ItemStack ROCKET_ITEM = new ItemStack(Items.FIREWORK_ROCKET);

    public RocketEntityRenderer(EntityRendererFactory.Context context) {
        super(context);
        this.shadowRadius = 0.15F;
    }

    @Override
    public void render(RocketEntity entity, float yaw, float tickDelta, MatrixStack matrices,
            VertexConsumerProvider vertexConsumers, int light) {
        matrices.push();

        matrices.translate(0, 0.15, 0);

        if (!entity.getPassengerList().isEmpty() && entity.getPassengerList().get(0) instanceof PlayerEntity player) {
            float pitch = player.getPitch(tickDelta);
            float adjustedYaw = player.getYaw(tickDelta);

            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(90 - adjustedYaw));
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(90 + pitch));
        } else {
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(90 - yaw));
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(90 + entity.getPitch()));
        }

        matrices.scale(3.5F, 3.5F, 3.5F);

        MinecraftClient.getInstance().getItemRenderer().renderItem(
                ROCKET_ITEM,
                ModelTransformationMode.GROUND,
                light,
                OverlayTexture.DEFAULT_UV,
                matrices,
                vertexConsumers,
                entity.getWorld(),
                0);

        matrices.pop();
    }

    @Override
    public Identifier getTexture(RocketEntity entity) {
        return new Identifier("minecraft", "textures/item/firework_rocket.png");
    }
}