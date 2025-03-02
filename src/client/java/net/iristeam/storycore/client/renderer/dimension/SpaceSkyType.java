package net.iristeam.storycore.client.renderer.dimension;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.rendering.v1.DimensionRenderingRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

import static net.iristeam.storycore.client.StoryCoreClient.*;
import static net.iristeam.storycore.util.PlanetPositionSystem.getPosition;
import static net.iristeam.storycore.util.PlanetPositionSystem.getScale;

public class SpaceSkyType implements DimensionRenderingRegistry.SkyRenderer {
    private static final Identifier THE_END = id("textures/sky/space_sky.png");
    private static final Identifier EARTH = id("textures/sky/earth.jpg");
    private static final Identifier TEST = id("textures/sky/gradient.png");
    private static final Identifier SOLAR_SYSTEM[] = {TEST, TEST, TEST, EARTH, TEST, TEST, TEST, TEST, TEST};

    @Override
    public void render(WorldRenderContext worldRenderContext) {
        MatrixStack matrices = worldRenderContext.matrixStack();
//        renderEndSky(matrices);
        renderSolarSystem(matrices);
    }

    private void renderEndSky(MatrixStack matrices) {
        RenderSystem.enableBlend();
        RenderSystem.depthMask(false);
        RenderSystem.setShader(GameRenderer::getPositionTexColorProgram);
        RenderSystem.setShaderTexture(0, THE_END);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuffer();

        for (int i = 0; i < 6; ++i) {
            matrices.push();
            if (i == 1) {
                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(90.0F));
            }

            if (i == 2) {
                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-90.0F));
            }

            if (i == 3) {
                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(180.0F));
            }

            if (i == 4) {
                matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(90.0F));
            }

            if (i == 5) {
                matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(-90.0F));
            }
            int l = i % 3;
            int i1 = i / 4 % 2;
            float col_begin = (float) (l) / 3.0F;
            float l_begin = (float) (i1) / 2.0F;
            float col_end = (float) (l + 1) / 3.0F;
            float l_end = (float) (i1 + 1) / 2.0F;

            float size = 200.0F;
            float distance = 200.0F;
            Matrix4f matrix4f = matrices.peek().getPositionMatrix();
            bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
            bufferBuilder.vertex(matrix4f, -size, -distance, -size).texture(col_end, l_end).color(255, 255, 255, 255).next();
            bufferBuilder.vertex(matrix4f, -size, -distance, size).texture(col_begin, l_end).color(255, 255, 255, 255).next();
            bufferBuilder.vertex(matrix4f, size, -distance, size).texture(col_begin, l_begin).color(255, 255, 255, 255).next();
            bufferBuilder.vertex(matrix4f, size, -distance, -size).texture(col_end, l_begin).color(255, 255, 255, 255).next();
            tessellator.draw();
            matrices.pop();
        }

        RenderSystem.depthMask(true);
        RenderSystem.disableBlend();
    }

    private void renderSolarSystem(MatrixStack matrices) {
        RenderSystem.enableDepthTest();
        RenderSystem.depthFunc(GL11.GL_LEQUAL);
        RenderSystem.enableBlend();

        matrices.push();
        matrices.translate(-MC.getCameraEntity().getX(), 100 - MC.getCameraEntity().getY(), -MC.getCameraEntity().getZ());
        matrices.scale(10,10,10);
        for (int i = 0; i < 9; i++) renderSphere(matrices, 1, 20, i);
        matrices.pop();

        RenderSystem.disableBlend();
        RenderSystem.disableDepthTest();
    }

    public static void renderSphere(MatrixStack matrices, float radius, int segments, int id) {
        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        RenderSystem.setShaderTexture(0, SOLAR_SYSTEM[id]);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuffer();
        matrices.push();
        Vec2f pos = getPosition(id);
        Vec3d scale = getScale(id);
        matrices.translate(pos.x, 0.0F, pos.y);
        matrices.scale((float) scale.x, (float) scale.y, (float) scale.z);

        for (int i = 0; i < segments; ++i) {
            float theta1 = (float) (Math.PI * i / segments);
            float theta2 = (float) (Math.PI * (i + 1) / segments);
            float v0 = ((float) i) / ((float) segments);
            float v1 = ((float) (i + 1)) / ((float) segments);
            for (int j = 0; j < segments; ++j) {
                float phi1 = (float) (2 * Math.PI * j / segments);
                float phi2 = (float) (2 * Math.PI * (j + 1) / segments);

                float x1 = radius * (float) (Math.sin(theta1) * Math.cos(phi1));
                float z1 = radius * (float) (Math.sin(theta1) * Math.sin(phi1));
                float y1 = radius * (float) Math.cos(theta1);

                float x2 = radius * (float) (Math.sin(theta1) * Math.cos(phi2));
                float z2 = radius * (float) (Math.sin(theta1) * Math.sin(phi2));
                float y2 = radius * (float) Math.cos(theta1);

                float x3 = radius * (float) (Math.sin(theta2) * Math.cos(phi2));
                float z3 = radius * (float) (Math.sin(theta2) * Math.sin(phi2));
                float y3 = radius * (float) Math.cos(theta2);

                float x4 = radius * (float) (Math.sin(theta2) * Math.cos(phi1));
                float z4 = radius * (float) (Math.sin(theta2) * Math.sin(phi1));
                float y4 = radius * (float) Math.cos(theta2);

                Matrix4f matrix4f = matrices.peek().getPositionMatrix();
                float u0 = (float) (2 * j) / ((float) segments);
                float u1 = (float) (2 * (j + 1)) / ((float) segments);
                bufferBuilder.begin(VertexFormat.DrawMode.TRIANGLE_FAN, VertexFormats.POSITION_TEXTURE);
                bufferBuilder.vertex(matrix4f, x1, y1, z1).texture(u0, v0).next();//.color(255,   0,   0, 255)
                bufferBuilder.vertex(matrix4f, x2, y2, z2).texture(u1, v0).next();//.color(255, 255, 255, 255)
                bufferBuilder.vertex(matrix4f, x3, y3, z3).texture(u1, v1).next();//.color(255, 255, 255, 255)
                bufferBuilder.vertex(matrix4f, x4, y4, z4).texture(u0, v1).next();//.color(  0,   0, 255, 255)

                tessellator.draw();
            }
        }
        matrices.pop();
    }
}