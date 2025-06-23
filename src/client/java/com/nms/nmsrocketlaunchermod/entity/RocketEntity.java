package com.nms.nmsrocketlaunchermod.entity;

import net.minecraft.entity.*;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.List;

public class RocketEntity extends Entity {
    private LivingEntity owner;

    private static final TrackedData<Float> VELOCITY_X = DataTracker.registerData(RocketEntity.class,
            TrackedDataHandlerRegistry.FLOAT);
    private static final TrackedData<Float> VELOCITY_Y = DataTracker.registerData(RocketEntity.class,
            TrackedDataHandlerRegistry.FLOAT);
    private static final TrackedData<Float> VELOCITY_Z = DataTracker.registerData(RocketEntity.class,
            TrackedDataHandlerRegistry.FLOAT);

    private static final double PARTICLE_OFFSET = 1.2;
    private static final double VERTICAL_OFFSET = 0.5;

    private static final SoundEvent FLIGHT_SOUND = SoundEvent.of(new Identifier("nmsrocketlaunchermod", "rocket_loop"));
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
            this.world.playSound(null, this.getX(), this.getY(), this.getZ(),
                    FLIGHT_SOUND, SoundCategory.NEUTRAL,
                    0.7F, 0.9F + this.world.random.nextFloat() * 0.2F);
            soundCooldown = 5;
        }

        if (!this.world.isClient) {
            Vec3d velocity = this.getVelocity();

            if (this.getPassengerList().isEmpty() && this.age > 5) {
                this.setVelocity(velocity);
            }

            this.setPosition(this.getPos().add(velocity));

            if (this.world.getBlockCollisions(this, this.getBoundingBox()).iterator().hasNext()) {
                this.onCollision(null);
            }

            this.dataTracker.set(VELOCITY_X, (float) velocity.x);
            this.dataTracker.set(VELOCITY_Y, (float) velocity.y);
            this.dataTracker.set(VELOCITY_Z, (float) velocity.z);

            double maxSpeed = 2.0;
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
            this.setPosition(this.getPos().add(velocity));

            if (velocity.lengthSquared() > 0.01) {
                Vec3d dir = velocity.normalize();
                Vec3d particlePos = this.getPos()
                        .subtract(dir.multiply(PARTICLE_OFFSET))
                        .add(0, VERTICAL_OFFSET, 0);

                for (int i = 0; i < 4; i++) {
                    double offsetX = (this.random.nextFloat() - 0.5) * 0.1;
                    double offsetY = (this.random.nextFloat() - 0.5) * 0.1;
                    double offsetZ = (this.random.nextFloat() - 0.5) * 0.1;
                    this.world.addParticle(ParticleTypes.FLAME,
                            particlePos.x,
                            particlePos.y,
                            particlePos.z,
                            offsetX,
                            offsetY,
                            offsetZ);
                }

                if (this.age % 2 == 0) {
                    for (int i = 0; i < 3; i++) {
                        double spreadX = (this.random.nextFloat() - 0.5) * 0.2;
                        double spreadY = this.random.nextFloat() * 0.1;
                        double spreadZ = (this.random.nextFloat() - 0.5) * 0.2;
                        this.world.addParticle(ParticleTypes.LARGE_SMOKE,
                                particlePos.x,
                                particlePos.y,
                                particlePos.z,
                                spreadX,
                                spreadY,
                                spreadZ);
                    }
                }
            }
        }

        if (!getPassengerList().isEmpty()) {
            Entity passenger = getPassengerList().get(0);
            if (passenger instanceof PlayerEntity player) {
                Vec3d velocity = this.getVelocity();
                Vec3d lookVec = player.getRotationVec(1.0F).normalize();

                float steeringStrength = 0.15f;
                double speedRetention = 0.85;

                Vec3d steering = lookVec.multiply(steeringStrength);
                Vec3d newVelocity = velocity.multiply(speedRetention).add(steering);

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
                    if (owner != null) {
                        entity.setAttacker(owner);
                    }
                }
            }

            this.world.createExplosion(this, this.getX(), this.getY(), this.getZ(),
                    explosionPower, true, World.ExplosionSourceType.MOB);

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

    @Override
    protected void readCustomDataFromNbt(NbtCompound nbt) {
        Vec3d velocity = new Vec3d(
                nbt.getDouble("VelocityX"),
                nbt.getDouble("VelocityY"),
                nbt.getDouble("VelocityZ"));
        this.setVelocity(velocity);
        this.dataTracker.set(VELOCITY_X, (float) velocity.x);
        this.dataTracker.set(VELOCITY_Y, (float) velocity.y);
        this.dataTracker.set(VELOCITY_Z, (float) velocity.z);
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

    @Override
    public Packet<ClientPlayPacketListener> createSpawnPacket() {
        return new EntitySpawnS2CPacket(this);
    }

    protected ItemStack getItemStack() {
        return ItemStack.EMPTY;
    }
}
