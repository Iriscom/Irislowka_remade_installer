package net.iristeam.storycore;

import net.fabricmc.api.ModInitializer;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StoryCore implements ModInitializer {

    public static final String MOD_ID = "storycore";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static Identifier id(String name) {return new Identifier(MOD_ID, name);}
    @Override
    public void onInitialize() {


    }
}
