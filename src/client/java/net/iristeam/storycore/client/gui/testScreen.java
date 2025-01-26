package net.iristeam.storycore.client.gui;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.MinecraftClient;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;

import static net.iristeam.storycore.client.StoryCoreClient.MOD_ID;
import static net.minecraft.command.EntitySelectorReader.RANDOM;

import java.util.ArrayList;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.List;


//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//


@Environment(EnvType.CLIENT)
public class testScreen extends Screen {
    public testScreen() {
        super(Text.literal("Test Screen"));  // Исправление: передаем заголовок прямо здесь
    }
    private static final Identifier SPARK_TEXTURE = new Identifier(MOD_ID, "textures/misc/spark.png");
    private static final List<Spark> sparks = new ArrayList<>();
    private int lastMouseX = -1;
    private int lastMouseY = -1;


    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        // Проверяем, изменились ли координаты мыши
        if (mouseX != lastMouseX || mouseY != lastMouseY) {
            lastMouseX = mouseX;
            lastMouseY = mouseY;

            // Генерируем новые частицы только при движении мыши
            generateSparks(mouseX, mouseY);
        }

        // Отображаем искры
        renderSparks(context, delta);
    }

    private void generateSparks(int mouseX, int mouseY) {
        // Генерация новых искр
        if (sparks.size() < 50) {
            sparks.add(new Spark(mouseX, mouseY));
        }
    }

    private void renderSparks(DrawContext context, float delta) {
        // Рисование искр с физикой
        for (int i = 0; i < sparks.size(); i++) {
            Spark spark = sparks.get(i);

            spark.update(delta);
            renderSpark(context, spark);

            // Удаление искры, если она слишком старая или за пределами экрана
            if (spark.age > spark.maxAge || spark.isOutOfBounds()) {
                sparks.remove(i);
                i--;
            }
        }
    }

    private void renderSpark(DrawContext context, Spark spark) {
        float alpha = 1.0f - (spark.age / (float) spark.maxAge); // Постепенное исчезновение
        float scaleFactor = 0.5f + 0.5f * (float)Math.sin(spark.age * 0.05f); // Небольшая вариация размера
        float rotation = (spark.age * 2) % 360; // Медленный поворот

        RenderSystem.enableBlend();
        RenderSystem.setShaderTexture(0, SPARK_TEXTURE);
        RenderSystem.blendFuncSeparate(
                GlStateManager.SrcFactor.SRC_ALPHA.value,
                GlStateManager.DstFactor.ONE.value,
                GlStateManager.SrcFactor.SRC_ALPHA.value,
                GlStateManager.DstFactor.ONE.value
        );

        MatrixStack matrices = context.getMatrices();
        matrices.push();

        matrices.translate(spark.x + 8, spark.y + 8, 0);
        matrices.multiply(net.minecraft.util.math.RotationAxis.POSITIVE_Z.rotationDegrees(rotation));
        matrices.scale(scaleFactor, scaleFactor, 1.0f);
        matrices.translate(-8, -8, 0);

        // Применяем белый цвет (без цветовой вариации)
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, alpha); // Белый цвет

        context.drawTexture(SPARK_TEXTURE, 0, 0, 0, 0, 16, 16, 16, 16);

        matrices.pop();
        RenderSystem.disableBlend();
    }

    private static class Spark {
        double x, y;
        double velocityX, velocityY;
        int age;
        int maxAge;

        public Spark(int startX, int startY) {
            this.x = startX;
            this.y = startY;
            this.velocityX = (Math.random() * 2 - 1);  // Случайная скорость по горизонтали
            this.velocityY = (Math.random() * 2 - 1);  // Случайная скорость по вертикали
            this.age = 0;
            this.maxAge = 30 + (int)(Math.random() * 40);  // Случайная продолжительность жизни (от 20 до 40)
        }

        public void update(float delta) {
            velocityY += 0.05 * delta;  // Легкая гравитация
            x += velocityX * delta;
            y += velocityY * delta;
            age++;
        }

        public boolean isOutOfBounds() {
            return x < 0 || x > MinecraftClient.getInstance().getWindow().getScaledWidth() || y > MinecraftClient.getInstance().getWindow().getScaledHeight();
        }
    }
    private void drawTexture(MatrixStack matrices, double x, double y, int i, int i1, int i2, int i3) {
    }


    @Override
    public boolean shouldCloseOnEsc() {
        return true;
    }
}