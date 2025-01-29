package net.iristeam.storycore.client.mixin;

import net.iristeam.storycore.client.interfaces.InGameHudInterface;
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
import java.util.Objects;
import java.util.Set;

import static net.iristeam.storycore.client.StoryCoreClient.MC;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin {

    private long startTime;
    private boolean teleporting;
    @Unique
    PlayerEntity entity = (PlayerEntity) (Object) this;
    @Inject(method = "tick",at = @At("TAIL"))
    private void noTick(CallbackInfo ci) {
        if (!teleporting&&entity.getServer()!=null && entity.getWorld().getDimensionKey() == DimensionTypes.OVERWORLD && entity.getBlockY() >= 600) {
            startTime = System.currentTimeMillis();
            teleporting = true;
            ((InGameHudInterface)MC.inGameHud).setTeleporting(true);
        }
        if(teleporting && System.currentTimeMillis() - startTime > 1500){
            teleporting = false;
            Set<PositionFlag> set = EnumSet.noneOf(PositionFlag.class);
            entity.teleport(entity.getServer().getWorld(ModDimensions.STORY_SPACE_LEVEL_KEY),0,75, 0,
                    set,entity.getYaw(),entity.getPitch());
        }
    }
}
