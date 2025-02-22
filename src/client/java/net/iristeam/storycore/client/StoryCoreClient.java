package net.iristeam.storycore.client;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.DimensionRenderingRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.iristeam.storycore.client.gui.Iris.iris_scriptsScreen;

import net.iristeam.storycore.client.gui.Iris.iris_silitorScreen;
import net.iristeam.storycore.client.renderer.dimension.SpaceSkyType;
import net.iristeam.storycore.entity.ModEntitys;
import net.iristeam.storycore.world.dimensions.ModDimensions;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.api.ClientModInitializer;
import net.iristeam.storycore.client.renderer.entity.MdLoverseRenderer;
import net.minecraft.util.Identifier;
import net.iristeam.storycore.client.renderer.dimension.SpaseDimensionEffect;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StoryCoreClient implements ClientModInitializer {
    public static final String MOD_ID = "storycore";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final MinecraftClient MC = MinecraftClient.getInstance();
    private static final KeyBinding CONTROL = new KeyBinding("key.storycore.test", GLFW.GLFW_KEY_M, "key.categories.misc");
    private static final KeyBinding CONTROL1 = new KeyBinding("key.storycore.test1", GLFW.GLFW_KEY_I, "key.categories.misc");
    public static Identifier id(String name) {return new Identifier(MOD_ID, name);}
    @Override
    public void onInitializeClient() {
        KeyBindingHelper.registerKeyBinding(CONTROL);
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (CONTROL.wasPressed() && MC.currentScreen == null) client.setScreen(new iris_silitorScreen());
        });
        KeyBindingHelper.registerKeyBinding(CONTROL1);
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (CONTROL1.wasPressed() && MC.currentScreen == null) client.setScreen(new iris_scriptsScreen());
        });
        EntityRendererRegistry.register(ModEntitys.MD_LOVERSE, MdLoverseRenderer::new);
        DimensionRenderingRegistry.registerDimensionEffects(id("story_space"),new SpaseDimensionEffect());
        DimensionRenderingRegistry.registerSkyRenderer(ModDimensions.STORY_SPACE_LEVEL_KEY, new SpaceSkyType());
    }
}
