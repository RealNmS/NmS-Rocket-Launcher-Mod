package com.nms.nmsrocketlaunchermod.client.render;

import com.nms.nmsrocketlaunchermod.entity.RocketEntity;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3f;

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

        // Position adjustment
        matrices.translate(0, 0.15, 0);

        // Get the actual rotation from player's look direction
        if (!entity.getPassengerList().isEmpty() && entity.getPassengerList().get(0) instanceof PlayerEntity) {
            PlayerEntity player = (PlayerEntity) entity.getPassengerList().get(0);

            // Use player's precise look angles
            float pitch = player.getPitch(tickDelta);
            float adjustedYaw = player.getYaw(tickDelta);

            // Apply full rotation
            matrices.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(90 - adjustedYaw));
            matrices.multiply(Vec3f.POSITIVE_Z.getDegreesQuaternion(90 + pitch)); // Negative for correct pitch
        } else {
            // Fallback to entity rotation when no player
            matrices.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(90 - yaw));
            matrices.multiply(Vec3f.POSITIVE_Z.getDegreesQuaternion(90 + entity.getPitch()));
        }

        // Rotate 90 degrees forward to make firework item point correctly
        // matrices.multiply(Vec3f.POSITIVE_Z.getDegreesQuaternion(90));

        // Scale up
        matrices.scale(3.5F, 3.5F, 3.5F);

        MinecraftClient.getInstance().getItemRenderer().renderItem(
                ROCKET_ITEM,
                ModelTransformation.Mode.GROUND,
                light,
                OverlayTexture.DEFAULT_UV,
                matrices,
                vertexConsumers,
                0);

        matrices.pop();
    }

    @Override
    public Identifier getTexture(RocketEntity entity) {
        return new Identifier("minecraft", "textures/item/firework_rocket.png");
    }
}