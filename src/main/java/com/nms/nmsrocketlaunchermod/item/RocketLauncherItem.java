package com.nms.nmsrocketlaunchermod.item;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import com.nms.nmsrocketlaunchermod.ModEnchantments;
import com.nms.nmsrocketlaunchermod.entity.RocketEntity;

import java.util.List;

public class RocketLauncherItem extends Item {
    private static final int BASE_COOLDOWN = 40;
    private static final float COOLDOWN_REDUCTION_PER_LEVEL = 0.2f; // 20% reduction per level

    private static final SoundEvent SHOOT_SOUND = new SoundEvent(
            new Identifier("nmsrocketlaunchermod", "rocket_launch"));

    public RocketLauncherItem(Settings settings) {
        super(settings.maxDamage(256));
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return true;
    }

    @Override
    public int getEnchantability() {
        return 30; // Same as iron tools (15 = gold, 10 = stone)
    }

    @Override
    public boolean canRepair(ItemStack stack, ItemStack ingredient) {
        return ingredient.isOf(Items.IRON_INGOT); // Allow repair with iron
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);

        // Get reload enchantment level
        int reloadLevel = EnchantmentHelper.getLevel(ModEnchantments.RELOAD, stack);

        // Calculate adjusted cooldown
        int adjustedCooldown = (int) (BASE_COOLDOWN * (1 - COOLDOWN_REDUCTION_PER_LEVEL * reloadLevel));
        adjustedCooldown = Math.max(5, adjustedCooldown); // Minimum 0.25 second cooldown

        if (user.getItemCooldownManager().isCoolingDown(this)) {
            return TypedActionResult.fail(stack);
        }

        // Client-side particles (shooter's perspective)
        if (world.isClient()) {
            spawnShootParticles(user);
        }

        if (!world.isClient()) {
            // Spawn rocket
            RocketEntity rocket = new RocketEntity(world, user);
            Vec3d lookVec = user.getRotationVec(1.0F).normalize();

            rocket.setYaw(user.getYaw());
            rocket.setPitch(user.getPitch());

            rocket.setVelocity(lookVec.multiply(1));
            rocket.setPosition(
                    user.getX(),
                    user.getEyeY() - 0.1,
                    user.getZ());

            world.playSound(
                    null, // No specific player
                    user.getX(), user.getY(), user.getZ(),
                    SHOOT_SOUND,
                    SoundCategory.PLAYERS,
                    1.0F, // Volume
                    0.8F + world.random.nextFloat() * 0.4F // Pitch variation
            );

            world.spawnEntity(rocket);
            user.startRiding(rocket);
            stack.damage(1, user, (p) -> p.sendToolBreakStatus(hand));

            // Server-side particles (visible to all players)
            ServerWorld serverWorld = (ServerWorld) world;
            spawnShootParticles(serverWorld, user);
        }

        user.getItemCooldownManager().set(this, adjustedCooldown); // Set dynamic cooldown
        return TypedActionResult.success(stack, world.isClient());
    }

    private void spawnShootParticles(PlayerEntity user) {
        Vec3d pos = user.getEyePos();
        Vec3d lookVec = user.getRotationVec(1.0F).normalize();
        Vec3d particlePos = pos.add(lookVec.multiply(0.5));

        for (int i = 0; i < 8; i++) {
            double spread = 0.15;
            user.world.addParticle(ParticleTypes.CLOUD,
                    particlePos.x,
                    particlePos.y,
                    particlePos.z,
                    (lookVec.x + (user.world.random.nextDouble() - 0.5) * spread) * 0.2,
                    (lookVec.y + (user.world.random.nextDouble() - 0.5) * spread) * 0.2,
                    (lookVec.z + (user.world.random.nextDouble() - 0.5) * spread) * 0.2);
        }
    }

    private void spawnShootParticles(ServerWorld world, PlayerEntity user) {
        Vec3d pos = user.getEyePos();
        Vec3d lookVec = user.getRotationVec(1.0F).normalize();
        Vec3d particlePos = pos.add(lookVec.multiply(0.5));

        world.spawnParticles(
                ParticleTypes.CLOUD,
                particlePos.x,
                particlePos.y,
                particlePos.z,
                12, // Number of particles
                0.2, // Horizontal spread
                0.2, // Vertical spread
                0.2, // Depth spread
                0.1 // Speed
        );
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        int reloadLevel = EnchantmentHelper.getLevel(ModEnchantments.RELOAD, stack);
        float seconds = (BASE_COOLDOWN * (1 - COOLDOWN_REDUCTION_PER_LEVEL * reloadLevel)) / 20f;

        tooltip.add(new TranslatableText("tooltip.nmsrocketlaunchermod.cooldown",
                String.format("%.1f", seconds))
                .formatted(Formatting.GRAY));

        super.appendTooltip(stack, world, tooltip, context);
    }
}