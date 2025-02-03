package net.iristeam.storycore.client.mixin;

import net.iristeam.storycore.client.interfaces.MinecraftClientInterface;
import net.iristeam.storycore.world.dimensions.ModDimensions;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.PositionFlag;
import net.minecraft.world.dimension.DimensionTypes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.EnumSet;
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
            ((MinecraftClientInterface)MC).setTeleporting(true);
        }
        if(teleporting && System.currentTimeMillis() - startTime > 1500){
            teleporting = false;
            Set<PositionFlag> set = EnumSet.noneOf(PositionFlag.class);
            entity.teleport(entity.getServer().getWorld(ModDimensions.STORY_SPACE_LEVEL_KEY),0,75, 0,
                    set,entity.getYaw(),entity.getPitch());
        }
    }
}
