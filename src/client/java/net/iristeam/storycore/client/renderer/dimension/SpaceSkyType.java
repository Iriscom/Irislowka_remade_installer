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
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

import static net.iristeam.storycore.client.StoryCoreClient.id;

public class SpaceSkyType implements DimensionRenderingRegistry.SkyRenderer {
    private static final Identifier THE_END = id("textures/sky/space_sky.png");
    @Override
    public void render(WorldRenderContext worldRenderContext) {
        MatrixStack matrices = worldRenderContext.matrixStack();
        renderEndSky(matrices);
    }
    private void renderEndSky(MatrixStack matrices) {
        RenderSystem.enableBlend();
        RenderSystem.depthMask(false);
        RenderSystem.setShader(GameRenderer::getPositionTexColorProgram);
        RenderSystem.setShaderTexture(0, THE_END);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuffer();

        for(int i = 0; i < 6; ++i) {
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
            float col_begin = (float)(l) / 3.0F;
            float l_begin = (float)(i1) / 2.0F;
            float col_end = (float)(l + 1) / 3.0F;
            float l_end = (float)(i1 + 1) / 2.0F;

            float size = 200.0F;
            float distance = 200.0F;
            Matrix4f matrix4f = matrices.peek().getPositionMatrix();
            bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
            bufferBuilder.vertex(matrix4f, -size, -distance, -size).texture(col_end  , l_end  ).color(255, 255, 255, 255).next();
            bufferBuilder.vertex(matrix4f, -size, -distance,  size).texture(col_begin, l_end  ).color(255, 255, 255, 255).next();
            bufferBuilder.vertex(matrix4f,  size, -distance,  size).texture(col_begin, l_begin).color(255, 255, 255, 255).next();
            bufferBuilder.vertex(matrix4f,  size, -distance, -size).texture(col_end  , l_begin).color(255, 255, 255, 255).next();
            tessellator.draw();
            matrices.pop();
        }

        RenderSystem.depthMask(true);
        RenderSystem.disableBlend();
    }
}
