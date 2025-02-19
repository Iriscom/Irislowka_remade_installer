package net.iristeam.storycore.entity.custom;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animatable.instance.SingletonAnimatableInstanceCache;
import software.bernie.geckolib.core.animation.*;
import software.bernie.geckolib.core.object.PlayState;

public class MdLoverseEntity  extends AnimalEntity implements GeoEntity {
    private AnimatableInstanceCache cache = new SingletonAnimatableInstanceCache(this);

    public MdLoverseEntity(EntityType<? extends AnimalEntity> entityType, World world) {
        super(entityType, world);
    }

    public static DefaultAttributeContainer.Builder setAttributes() {
        return AnimalEntity.createMobAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 16.0D)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 4.0f)
                .add(EntityAttributes.GENERIC_ATTACK_SPEED, 2.0f)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.4f);
    }

    @Override
    protected void initGoals() {
//        this.goalSelector.add(1, new SwimGoal(this));
//        this.goalSelector.add(2, new MeleeAttackGoal(this, 1.2D, false));
//        this.goalSelector.add(3, new WanderAroundFarGoal(this, 0.75f, 1));
//
//        this.goalSelector.add(4, new LookAroundGoal(this));
//
//        this.targetSelector.add(2, new ActiveTargetGoal<>(this, PlayerEntity.class, true));
//        this.targetSelector.add(2, new ActiveTargetGoal<>(this, MerchantEntity.class, true));
//        this.targetSelector.add(3, new ActiveTargetGoal<>(this, ChickenEntity.class, true));
    }


    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllerRegistrar) {
        controllerRegistrar.add(new AnimationController<>(this, "controller", 0, this::predicate));
    }

    private <T extends GeoAnimatable> PlayState predicate(AnimationState<T> tAnimationState) {
//        if(tAnimationState.isMoving()) {
//            tAnimationState.getController().setAnimation(RawAnimation.begin().then("animation.mdloverse.walk", Animation.LoopType.LOOP));
//        }

        tAnimationState.getController().setAnimation(RawAnimation.begin().then("animation.mdloverse.idle", Animation.LoopType.LOOP));
        return PlayState.CONTINUE;
    }


    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    @Nullable
    @Override
    public PassiveEntity createChild(ServerWorld world, PassiveEntity entity) {
        return null;
    }
}