package net.iristeam.storycore.world.dimensions;

import net.iristeam.storycore.StoryCore;
import net.minecraft.registry.Registerable;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.intprovider.UniformIntProvider;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionOptions;
import net.minecraft.world.dimension.DimensionType;
import static net.iristeam.storycore.StoryCore.id;
import java.util.OptionalLong;

public class ModDimensions {
    public static final RegistryKey<DimensionOptions> STORY_SPACE_KEY = RegistryKey.of(RegistryKeys.DIMENSION,
            new Identifier(StoryCore.MOD_ID, "story_space"));
    public static final RegistryKey<World> STORY_SPACE_LEVEL_KEY = RegistryKey.of(RegistryKeys.WORLD,
            new Identifier(StoryCore.MOD_ID, "story_space"));
    public static final RegistryKey<DimensionType> STORY_SPACE_DIM_TYPE = RegistryKey.of(RegistryKeys.DIMENSION_TYPE,
            new Identifier(StoryCore.MOD_ID, "story_space_type"));

    public static void bootstrapType(Registerable<DimensionType> context) {
        context.register(STORY_SPACE_DIM_TYPE, new DimensionType(
                OptionalLong.of(12000), // fixedTime
                false, // hasSkylight
                false, // hasCeiling
                false, // ultraWarm
                false, // natural
                1.0, // coordinateScale
                true, // bedWorks
                false, // respawnAnchorWorks
                0, // minY
                256, // height
                256, // logicalHeight
                BlockTags.INFINIBURN_OVERWORLD, // infiniburn
                id("story_space"), // effectsLocation
                1.0f, // ambientLight
                new DimensionType.MonsterSettings(false, false, UniformIntProvider.create(0, 0), 0)));
    }
}
