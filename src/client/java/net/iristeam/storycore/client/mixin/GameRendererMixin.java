package net.iristeam.storycore.client.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.iristeam.storycore.client.interfaces.MinecraftClientInterface;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.BufferBuilderStorage;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.Window;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.MathHelper;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
    private static final Window window = MinecraftClient.getInstance().getWindow();
    private static final float sec2pi = 3000/180;
    @Shadow
    private MinecraftClient client;
    @Shadow
    private BufferBuilderStorage buffers;
    @Shadow @Final private static Logger LOGGER;

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;draw()V"))
    public void render(CallbackInfo ci,@Local DrawContext drawContext) {
        if (((MinecraftClientInterface)this.client).isTeleporting()){
            int a = (int) (Math.abs(MathHelper.sin((float) Math.toRadians((double) (System.currentTimeMillis() - ((MinecraftClientInterface)this.client).getStartTime()) /sec2pi)))*255);
//            LOGGER.info(String.valueOf(((int) System.currentTimeMillis()-((MinecraftClientInterface)this.client).getStartTime())));
//            LOGGER.info(String.valueOf((System.currentTimeMillis()-((MinecraftClientInterface)this.client).getStartTime())/sec2pi));
//            LOGGER.info(String.valueOf(MathHelper.cos((float) Math.toDegrees(System.currentTimeMillis()-((MinecraftClientInterface)this.client).getStartTime()/5.5))));
//            LOGGER.info(String.valueOf(a));
            drawContext.fill(0,0,window.getScaledWidth(),window.getScaledHeight(), ColorHelper.Argb.getArgb(a,0,0,0));
            if ((System.currentTimeMillis() - ((MinecraftClientInterface)this.client).getStartTime()) /sec2pi>180)
                ((MinecraftClientInterface)this.client).setTeleporting(false);
        }
    }
}
