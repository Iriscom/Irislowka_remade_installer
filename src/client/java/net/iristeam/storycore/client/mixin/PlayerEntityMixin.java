package net.iristeam.storycore.client.mixin;

import net.iristeam.storycore.world.dimensions.ModDimensions;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.PositionFlag;
import net.minecraft.server.command.TeleportCommand;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionTypes;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.*;
import java.util.EnumSet;
import java.util.Set;

@Mixin(Entity.class)
public abstract class PlayerEntityMixin {
    @Shadow public abstract void tick();

    @Shadow @Final private static Logger LOGGER;

    @Shadow public abstract boolean teleport(ServerWorld world, double destX, double destY, double destZ, Set<PositionFlag> flags, float yaw, float pitch);

    @Unique
    Entity entity = (Entity) (Object) this;
    @Inject(method = "tick",at = @At("TAIL"))
    private void noTick(CallbackInfo ci) {
        if (entity instanceof PlayerEntity && entity.getWorld().getDimensionKey() == DimensionTypes.OVERWORLD
                && entity.getBlockY() >= 600) {
            Set<PositionFlag> set = EnumSet.noneOf(PositionFlag.class);
            entity.teleport(entity.getServer().getWorld(ModDimensions.STORY_SPACE_LEVEL_KEY),entity.getX(), entity.getY(), entity.getZ(),
                    set ,entity.getYaw(),entity.getPitch());
            LOGGER.info("PlayerEntityMixin tick");
        }
//        TeleportCommand
        entity.setNoGravity(entity.getWorld().getDimensionKey() == ModDimensions.STORY_SPACE_DIM_TYPE);
    }
}
