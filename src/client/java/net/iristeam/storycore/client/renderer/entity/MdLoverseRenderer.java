package net.iristeam.storycore.client.renderer.entity;




import net.iristeam.storycore.client.renderer.entity.model.MdLoverseModel;
import net.iristeam.storycore.entity.custom.MdLoverseEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

import static net.iristeam.storycore.StoryCore.id;

public class MdLoverseRenderer extends GeoEntityRenderer<MdLoverseEntity> {



    public MdLoverseRenderer(EntityRendererFactory.Context renderManager) {
        super(renderManager, new MdLoverseModel());
    }

    @Override
    public Identifier getTextureLocation(MdLoverseEntity animatable) {
        return id("textures/entity/mdloverse.png");
    }

    @Override
    public void render(MdLoverseEntity entity, float entityYaw, float partialTick, MatrixStack poseStack,
                       VertexConsumerProvider bufferSource, int packedLight) {
        if(entity.isBaby()) {
            poseStack.scale(0.4f, 0.4f, 0.4f);
        }

        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }
}