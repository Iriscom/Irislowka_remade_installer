package net.iristeam.storycore.client.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.iristeam.storycore.client.interfaces.InGameHudInterface;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.Window;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Date;

import static net.iristeam.storycore.client.StoryCoreClient.*;

@Environment(EnvType.CLIENT)
@Mixin({InGameHud.class})
public class InGameHudMixin implements InGameHudInterface {
    private static final float sec2pi = 3000/180;
    private static boolean tel = false;//defaults false
    private static boolean tel1 = false;//defaults false
    private static long startTime;
    private static final Identifier CLOCKS              = Identifier.of(MOD_ID,"textures/gui/in-game/clocks.png");
    private static final Identifier ANTENA              = Identifier.of(MOD_ID,"textures/gui/in-game/antena1.png");
    private static final Identifier SUN                 = Identifier.of(MOD_ID,"textures/gui/in-game/sun1.png");
    private static final Identifier RAIN                = Identifier.of(MOD_ID,"textures/gui/in-game/rain.png");
    private static final Identifier THUNDER             = Identifier.of(MOD_ID,"textures/gui/in-game/thunder.png");
    private static final Identifier WARNING             = Identifier.of(MOD_ID,"textures/gui/in-game/warning.png");
    private static final Identifier PEREKLADKA1         = Identifier.of(MOD_ID,"textures/gui/in-game/perekladka1.png");
    private static final Identifier PEREKLADKA2         = Identifier.of(MOD_ID,"textures/gui/in-game/perekladka2.png");
    private static final Identifier PEREKLADKA3         = Identifier.of(MOD_ID,"textures/gui/in-game/perekladka3.png");
    private static final Identifier STRELKA_VPRAVO_BLUE = Identifier.of(MOD_ID,"textures/gui/in-game/strelka_vpravo_blue.png");
    private static final Identifier STRELKA_VLEVO_BLUE  = Identifier.of(MOD_ID,"textures/gui/in-game/strelka_vlevo_blue.png");
    private static final Window window = MinecraftClient.getInstance().getWindow();
    private static final TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
    private static World weather = MC.world;
    @Inject(
            method = "render",
            at = @At("TAIL")
    )
    public void render(DrawContext context, float tickDelta, CallbackInfo ci) {
//        LOGGER.info(String.valueOf(tel));
        if (tel1){
            startTime = System.currentTimeMillis();
            tel1 = false;
        }
        if (tel){
            int a = (int) (Math.abs(MathHelper.sin((float) Math.toRadians((double) (System.currentTimeMillis() - startTime) /sec2pi)))*255);
//            LOGGER.info(String.valueOf(((int) System.currentTimeMillis()-startTime)/5.5));
//            LOGGER.info(String.valueOf(Math.toDegrees(System.currentTimeMillis()-startTime/5.5)));
//            LOGGER.info(String.valueOf(MathHelper.cos((float) Math.toDegrees(System.currentTimeMillis()-startTime/5.5))));
            LOGGER.info(String.valueOf(a));
            context.fill(0,0,window.getScaledWidth(),window.getScaledHeight(), ColorHelper.Argb.getArgb(a,0,0,0));
            if ((System.currentTimeMillis() - startTime) /sec2pi>180) tel = false;
            return;
        }
        if(MC==null) return;
        if(weather==null){ weather = MC.world; return;}
        Date time = new Date();
//        context.drawText(textRenderer, Text.of(String.valueOf(window.getFramebufferWidth())),  10,10,2,false);
        context.drawTexture(PEREKLADKA1, 4, 25, 0, 0, 96, 1, 96, 1);
        context.drawTexture(PEREKLADKA1, window.getScaledWidth() - 100, 24, 0, 0, 96, 1, 96, 1);
        context.drawTexture(PEREKLADKA3, window.getScaledWidth() - 66, 13, 0, 0, 1, 11, 1, 11);
        context.drawTexture(PEREKLADKA3, window.getScaledWidth() - 46, 13, 0, 0, 1, 11, 1, 11);
        context.drawTexture(ANTENA, 20, 5, 0, 0, 16, 16, 16, 16);
        context.drawTexture(CLOCKS, window.getScaledWidth() - 99, 16, 0, 0, 5, 5, 5, 5);
        context.drawText(textRenderer, time.getHours() + ":" + String.format("%02d", time.getMinutes()), window.getScaledWidth() - 93, 15, 16777215, false);
        context.drawText(textRenderer, 12 + "°", window.getScaledWidth() - 63, 15, 16777215, false);
        if (weather.getDimension().bedWorks()) {
            if (weather.isThundering()) {
                context.drawText(textRenderer, "гроза", window.getScaledWidth() - 38, 15, 16777215, false);
                context.drawTexture(THUNDER, window.getScaledWidth() - 44, 16, 0, 0, 5, 5, 5, 5);
            } else if (weather.isRaining()) {
                context.drawText(textRenderer, "дождь", window.getScaledWidth() - 38, 15, 16777215, false);
                context.drawTexture(RAIN, window.getScaledWidth() - 44, 16, 0, 0, 5, 5, 5, 5);
            } else {
                context.drawText(textRenderer, "ясно", window.getScaledWidth() - 38, 15, 16777215, false);
                context.drawTexture(SUN, window.getScaledWidth() - 44, 16, 0, 0, 5, 5, 5, 5);

            }
        } else context.drawText(textRenderer, "непонятно", window.getScaledWidth() - 38, 15, 16777215, false);

    }

    @Override
    public boolean isTeleporting() {
        LOGGER.info(String.valueOf(tel));
        return tel;
    }

    @Override
    public void setTeleporting(boolean Telep) {
        tel = Telep;
        tel1 = Telep;
        LOGGER.info(String.valueOf(tel));
    }
}
