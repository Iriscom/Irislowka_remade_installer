package net.iristeam.storycore.client.renderer.entity.model;

//import irisportal.entities.custom.TurretEntity;
//import net.minecraft.resources.ResourceLocation;
import net.iristeam.storycore.entity.custom.MdLoverseEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animatable.model.CoreGeoBone;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.loading.json.raw.Bone;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.model.data.EntityModelData;

import software.bernie.geckolib.core.animatable.instance.SingletonAnimatableInstanceCache;
import static net.iristeam.storycore.StoryCore.id;


public class MdLoverseModel extends GeoModel<MdLoverseEntity> implements GeoAnimatable {

    private final AnimatableInstanceCache cache = new SingletonAnimatableInstanceCache(this);

    @Override
    public Identifier getModelResource(MdLoverseEntity entity) {
        return id("geo/entity/mdloverse.geo.json");
    }

    @Override
    public Identifier getTextureResource(MdLoverseEntity entity) {
        return id("textures/entity/mdloverse.png");
    }

    @Override
    public Identifier getAnimationResource(MdLoverseEntity entity) {
        return id("animations/entity/mdloverse.animation.json");
    }

    /**
     * Реализует вращение головы на 360°.
     */
    @Override
    public void setCustomAnimations(MdLoverseEntity entity, long instanceId, AnimationState<MdLoverseEntity> animationState) {
        super.setCustomAnimations(entity, instanceId, animationState);

        // Получаем кость головы
        CoreGeoBone headBone = getAnimationProcessor().getBone("Head");
        if (headBone == null) return;

        // Берём yaw и pitch напрямую без ограничений
        float netHeadYaw = animationState.getData(software.bernie.geckolib.constant.DataTickets.ENTITY_MODEL_DATA).netHeadYaw();
        float headPitch = animationState.getData(software.bernie.geckolib.constant.DataTickets.ENTITY_MODEL_DATA).headPitch();

        // Прямое применение углов для 360° вращения
        headBone.setRotY((float) Math.toRadians(netHeadYaw));   // Yaw — вращение влево-вправо (без ограничений)
        headBone.setRotX((float) Math.toRadians(headPitch));    // Pitch — наклон вверх-вниз
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        // Контроллеры не требуются
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    @Override
    public double getTick(Object o) {
        if (o instanceof MdLoverseEntity entity) {
            return entity.age + MinecraftClient.getInstance().getTickDelta();
        }
        return 0;
    }
}