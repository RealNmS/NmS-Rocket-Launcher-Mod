package com.nms.nmsrocketlaunchermod.entity;

import net.minecraft.entity.Entity;
import net.minecraft.world.explosion.Explosion;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.entity.player.PlayerEntity;
import java.util.List;

public class RocketEntity extends Entity {
    private LivingEntity owner;

    private static final TrackedData<Float> VELOCITY_X = DataTracker.registerData(RocketEntity.class,
            TrackedDataHandlerRegistry.FLOAT);
    private static final TrackedData<Float> VELOCITY_Y = DataTracker.registerData(RocketEntity.class,
            TrackedDataHandlerRegistry.FLOAT);
    private static final TrackedData<Float> VELOCITY_Z = DataTracker.registerData(RocketEntity.class,
            TrackedDataHandlerRegistry.FLOAT);

    private static final double PARTICLE_OFFSET = 0.5;
    private static final double VERTICAL_OFFSET = 0.5;

    private static final SoundEvent FLIGHT_SOUND = new SoundEvent(
            new Identifier("nmsrocketlaunchermod", "rocket_loop"));
    private int soundCooldown = 0;

    public RocketEntity(EntityType<? extends RocketEntity> type, World world) {
        super(type, world);
        this.setNoGravity(true);
    }

    public RocketEntity(World world, LivingEntity owner) {
        this(ModEntities.ROCKET, world);
        this.owner = owner;
        this.setPosition(owner.getX(), owner.getEyeY() - 0.1, owner.getZ());
    }

    @Override
    protected void initDataTracker() {
        this.dataTracker.startTracking(VELOCITY_X, 0f);
        this.dataTracker.startTracking(VELOCITY_Y, 0f);
        this.dataTracker.startTracking(VELOCITY_Z, 0f);
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.world.isClient && this.soundCooldown-- <= 0) {
            this.world.playSound(
                    null,
                    this.getX(),
                    this.getY(),
                    this.getZ(),
                    FLIGHT_SOUND,
                    SoundCategory.NEUTRAL,
                    0.7F,
                    0.9F + this.world.random.nextFloat() * 0.2F);
            soundCooldown = 5;
        }

        if (!this.world.isClient) {
            Vec3d velocity = this.getVelocity();

            if (this.getPassengerList().isEmpty() && this.age > 5) {
                this.setVelocity(velocity);
            }

            this.setPosition(
                    this.getX() + velocity.x,
                    this.getY() + velocity.y,
                    this.getZ() + velocity.z);

            if (this.world.getBlockCollisions(this, this.getBoundingBox()).iterator().hasNext()) {
                this.onCollision(null);
            }

            this.dataTracker.set(VELOCITY_X, (float) velocity.x);
            this.dataTracker.set(VELOCITY_Y, (float) velocity.y);
            this.dataTracker.set(VELOCITY_Z, (float) velocity.z);

            double maxSpeed = 2.0; // Adjust this value
            if (velocity.lengthSquared() > maxSpeed * maxSpeed) {
                this.setVelocity(velocity.normalize().multiply(maxSpeed));
            }
        } else {
            float vx = this.dataTracker.get(VELOCITY_X);
            float vy = this.dataTracker.get(VELOCITY_Y);
            float vz = this.dataTracker.get(VELOCITY_Z);
            Vec3d velocity = new Vec3d(vx, vy, vz);

            this.prevX = this.getX();
            this.prevY = this.getY();
            this.prevZ = this.getZ();

            this.setPosition(
                    this.getX() + velocity.x,
                    this.getY() + velocity.y,
                    this.getZ() + velocity.z);

            if (velocity.lengthSquared() > 0.01) { // Only if moving
                Vec3d dir = velocity.normalize();
                Vec3d particlePos = this.getPos()
                        .subtract(dir.multiply(PARTICLE_OFFSET)) // Offset behind rocket
                        .add(0, VERTICAL_OFFSET, 0); // Adjust vertical position

                this.world.addParticle(ParticleTypes.FLAME,
                        particlePos.x, particlePos.y, particlePos.z,
                        0, 0, 0);

                if (this.age % 2 == 0) {
                    this.world.addParticle(ParticleTypes.SMOKE,
                            particlePos.x, particlePos.y, particlePos.z,
                            (this.random.nextFloat() - 0.5) * 0.1,
                            0.05,
                            (this.random.nextFloat() - 0.5) * 0.1);
                }
            }
        }

        if (!getPassengerList().isEmpty()) {
            Entity passenger = getPassengerList().get(0);
            if (passenger instanceof PlayerEntity player) {
                float vx = this.dataTracker.get(VELOCITY_X);
                float vy = this.dataTracker.get(VELOCITY_Y);
                float vz = this.dataTracker.get(VELOCITY_Z);
                Vec3d velocity = new Vec3d(vx, vy, vz);

                Vec3d lookVec = player.getRotationVec(1.0F).normalize();

                float steeringStrength = 0.15f;
                double speedRetention = 0.85;

                Vec3d steering = lookVec.multiply(steeringStrength);
                Vec3d newVelocity = velocity
                        .multiply(speedRetention)
                        .add(steering);

                double maxSpeed = 4.0;
                if (newVelocity.lengthSquared() > maxSpeed * maxSpeed) {
                    newVelocity = newVelocity.normalize().multiply(maxSpeed);
                }

                this.setVelocity(newVelocity);
            }
        }
    }

    protected void onCollision(HitResult hitResult) {
        if (!this.world.isClient) {
            float explosionPower = 12.0F;

            List<LivingEntity> entities = this.world.getEntitiesByClass(
                    LivingEntity.class,
                    this.getBoundingBox().expand(explosionPower),
                    e -> true);

            for (LivingEntity entity : entities) {
                double distance = this.distanceTo(entity);
                if (distance <= explosionPower) {
                    float damage = explosionPower * (1.0F - (float) (distance / explosionPower));

                    // Use owner for damage source if available
                    if (this.owner != null) {
                        // Proper damage source for explosion attribution
                        DamageSource source = DamageSource.explosion(this.owner);

                        entity.damage(source, damage);
                        entity.setAttacker(this.owner);
                    } else {
                        // Fallback to generic explosion damage
                        entity.damage(DamageSource.explosion((LivingEntity) null), damage);
                    }
                }
            }

            // Create visual explosion
            this.world.createExplosion(
                    this,
                    this.getX(),
                    this.getY(),
                    this.getZ(),
                    explosionPower,
                    true,
                    Explosion.DestructionType.BREAK);

            this.discard();
        }
    }

    @Override
    public boolean canAddPassenger(Entity passenger) {
        return this.getPassengerList().isEmpty();
    }

    @Override
    public double getMountedHeightOffset() {
        return 0.0;
    }

    protected ItemStack getItemStack() {
        return ItemStack.EMPTY;
    }

    @Override
    public Packet<?> createSpawnPacket() {
        return new EntitySpawnS2CPacket(this);
    }

    @Override
    protected void readCustomDataFromNbt(NbtCompound nbt) {
        if (nbt.contains("VelocityX")) {
            float vx = (float) nbt.getDouble("VelocityX");
            float vy = (float) nbt.getDouble("VelocityY");
            float vz = (float) nbt.getDouble("VelocityZ");
            this.setVelocity(new Vec3d(vx, vy, vz));
            this.dataTracker.set(VELOCITY_X, vx);
            this.dataTracker.set(VELOCITY_Y, vy);
            this.dataTracker.set(VELOCITY_Z, vz);
        }
    }

    @Override
    protected void writeCustomDataToNbt(NbtCompound nbt) {
        Vec3d velocity = this.getVelocity();
        nbt.putDouble("VelocityX", velocity.x);
        nbt.putDouble("VelocityY", velocity.y);
        nbt.putDouble("VelocityZ", velocity.z);
    }

    @Override
    public void updatePassengerPosition(Entity passenger) {
        if (this.hasPassenger(passenger)) {
            Vec3d offset = new Vec3d(0, this.getMountedHeightOffset(), 0);
            passenger.setPosition(this.getX() + offset.x, this.getY() + offset.y, this.getZ() + offset.z);

            if (passenger instanceof PlayerEntity) {
                this.setYaw(passenger.getYaw());
                this.setPitch(passenger.getPitch());
            }
        }
    }
}