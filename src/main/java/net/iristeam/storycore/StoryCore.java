package net.iristeam.storycore;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.iristeam.storycore.entity.ModEntitys;
import net.iristeam.storycore.entity.custom.MdLoverseEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StoryCore implements ModInitializer {

    public static final String MOD_ID = "storycore";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static MinecraftServer MS;
    public static Identifier id(String name) {return new Identifier(MOD_ID, name);}
    @Override
    public void onInitialize() {
        FabricDefaultAttributeRegistry.register(ModEntitys.MD_LOVERSE, MdLoverseEntity.setAttributes());
        ServerLifecycleEvents.SERVER_STARTING.register(server -> MS = server);
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> MS = null);
    }
}
