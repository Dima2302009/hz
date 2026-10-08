package com.example.dread.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.ai.goal.WanderAroundFarGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

/**
 * Наблюдатель: замирает, пока хоть один игрок смотрит на него (как Weeping Angel).
 * Стоит отвернуться — он идёт к тебе. Ослепляет и «затемняет» при ударе.
 * Исчезает с рассветом.
 */
public class WatcherEntity extends HostileEntity {
    private boolean frozen = false;

    public WatcherEntity(EntityType<? extends WatcherEntity> type, World world) {
        super(type, world);
        this.experiencePoints = 20;
    }

    public static DefaultAttributeContainer.Builder createAttributes() {
        return HostileEntity.createHostileAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 40.0)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.38)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 6.0)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 48.0)
                .add(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, 0.6);
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(1, new SwimGoal(this));
        this.goalSelector.add(2, new StalkGoal(this, 1.0));
        this.goalSelector.add(6, new WanderAroundFarGoal(this, 0.6));
        this.goalSelector.add(7, new LookAtEntityGoal(this, PlayerEntity.class, 24.0f));
        this.targetSelector.add(1, new ActiveTargetGoal<>(this, PlayerEntity.class, false));
    }

    public boolean isFrozen() {
        return frozen;
    }

    @Override
    public void tick() {
        if (!this.getWorld().isClient) {
            this.frozen = isBeingWatched();
            if (this.frozen) {
                this.getNavigation().stop();
            }
        }
        super.tick();
    }

    /** Пока замёрз, сам двигаться не может (гравитация и откидывание работают). */
    @Override
    public void travel(Vec3d movementInput) {
        if (this.frozen) {
            super.travel(Vec3d.ZERO);
            return;
        }
        super.travel(movementInput);
    }

    @Override
    public void tickMovement() {
        super.tickMovement();
        // С рассветом растворяется в дыму
        if (!this.getWorld().isClient
                && this.age > 100
                && this.getWorld().isDay()
                && this.random.nextInt(100) == 0
                && this.getWorld().isSkyVisible(this.getBlockPos())) {
            if (this.getWorld() instanceof ServerWorld sw) {
                sw.spawnParticles(ParticleTypes.LARGE_SMOKE, getX(), getBodyY(0.5), getZ(),
                        25, 0.3, 0.8, 0.3, 0.02);
            }
            this.discard();
        }
    }

    private boolean isBeingWatched() {
        for (PlayerEntity player : this.getWorld().getPlayers()) {
            if (player.isSpectator()) continue;
            if (player.squaredDistanceTo(this) > 64.0 * 64.0) continue;

            Vec3d look = player.getRotationVec(1.0f).normalize();
            Vec3d to = new Vec3d(
                    this.getX() - player.getX(),
                    this.getBodyY(0.5) - player.getEyeY(),
                    this.getZ() - player.getZ()
            ).normalize();

            // dot > 0.55 ~ угол зрения примерно ±56°
            if (look.dotProduct(to) > 0.55 && player.canSee(this)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean tryAttack(Entity target) {
        boolean hit = super.tryAttack(target);
        if (hit && target instanceof LivingEntity living) {
            living.addStatusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS, 100, 0), this);
            living.addStatusEffect(new StatusEffectInstance(StatusEffects.DARKNESS, 200, 0), this);
        }
        return hit;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.ENTITY_ENDERMAN_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ENTITY_ENDERMAN_DEATH;
    }

    /** Атакующая цель, которая не работает, пока моб «заморожен» взглядом. */
    static class StalkGoal extends MeleeAttackGoal {
        private final WatcherEntity watcher;

        StalkGoal(WatcherEntity mob, double speed) {
            super(mob, speed, false);
            this.watcher = mob;
        }

        @Override
        public boolean canStart() {
            return !watcher.frozen && super.canStart();
        }

        @Override
        public boolean shouldContinue() {
            return !watcher.frozen && super.shouldContinue();
        }
    }
}
