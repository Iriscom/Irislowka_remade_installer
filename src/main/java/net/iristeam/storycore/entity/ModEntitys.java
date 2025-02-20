package net.iristeam.storycore.entity;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.iristeam.storycore.entity.custom.MdLoverseEntity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

import static net.iristeam.storycore.StoryCore.id;

public class ModEntitys {
    public static final EntityType<MdLoverseEntity> MD_LOVERSE = Registry.register(Registries.ENTITY_TYPE,
            id("mdloverse"),
            FabricEntityTypeBuilder.create(SpawnGroup.CREATURE, MdLoverseEntity::new)
                    .dimensions(EntityDimensions.fixed(1f, 1.8f)).build());
}
