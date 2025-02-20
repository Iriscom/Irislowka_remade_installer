package net.iristeam.storycore.client.renderer.entity.model;

//import irisportal.entities.custom.TurretEntity;
//import net.minecraft.resources.ResourceLocation;
import net.iristeam.storycore.entity.custom.MdLoverseEntity;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.model.GeoModel;

import static net.iristeam.storycore.StoryCore.id;


public class MdLoverseModel extends GeoModel<MdLoverseEntity> {

    @Override
    public Identifier getModelResource(MdLoverseEntity mdLoverseEntity) {
        return id("geo/entity/mdloverse.geo.json");
    }

    @Override
    public Identifier getTextureResource(MdLoverseEntity mdLoverseEntity) {
        return id("textures/entity/mdloverse.png");
    }

    @Override
    public Identifier getAnimationResource(MdLoverseEntity mdLoverseEntity) {
        return id("animations/entity/mdloverse.animation.json");
    }
}
