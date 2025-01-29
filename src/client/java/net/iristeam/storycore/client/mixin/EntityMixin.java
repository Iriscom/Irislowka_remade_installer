package net.iristeam.storycore.client.mixin;

import net.iristeam.storycore.world.dimensions.ModDimensions;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.PositionFlag;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.dimension.DimensionTypes;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.EnumSet;
import java.util.Set;

@Mixin(Entity.class)
public abstract class EntityMixin {

    @Unique
    Entity entity = (Entity) (Object) this;
    @Inject(method = "tick",at = @At("TAIL"))
    private void noTick(CallbackInfo ci) {
        entity.setNoGravity(entity.getWorld().getDimensionKey() == ModDimensions.STORY_SPACE_DIM_TYPE);
    }
}
