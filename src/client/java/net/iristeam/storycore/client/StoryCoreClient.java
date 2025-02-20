package net.iristeam.storycore.client;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.DimensionRenderingRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.iristeam.storycore.client.gui.Iris.irisScreen;

import net.iristeam.storycore.client.renderer.dimension.SpaceSkyType;
import net.iristeam.storycore.client.renderer.entity.MdLoverseRenderer;
import net.iristeam.storycore.entity.ModEntitys;
import net.iristeam.storycore.entity.custom.MdLoverseEntity;
import net.iristeam.storycore.world.dimensions.ModDimensions;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Overlay;
import net.minecraft.client.option.KeyBinding;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.iristeam.storycore.client.gui.testScreen;
import net.iristeam.storycore.client.renderer.dimension.SpaseDimensionEffect;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StoryCoreClient implements ClientModInitializer {
    public static final String MOD_ID = "storycore";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final MinecraftClient MC = MinecraftClient.getInstance();
    private static final KeyBinding CONTROL = new KeyBinding("key.storycore.test", GLFW.GLFW_KEY_G, "key.categories.misc");
    public static Identifier id(String name) {return new Identifier(MOD_ID, name);}
    @Override
    public void onInitializeClient() {
        KeyBindingHelper.registerKeyBinding(CONTROL);
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (CONTROL.wasPressed() && MC.currentScreen == null) client.setScreen(new irisScreen());
        });

        EntityRendererRegistry.register(ModEntitys.MD_LOVERSE, MdLoverseRenderer::new);

        DimensionRenderingRegistry.registerDimensionEffects(id("story_space"),new SpaseDimensionEffect());
        DimensionRenderingRegistry.registerSkyRenderer(ModDimensions.STORY_SPACE_LEVEL_KEY, new SpaceSkyType());
    }
}
